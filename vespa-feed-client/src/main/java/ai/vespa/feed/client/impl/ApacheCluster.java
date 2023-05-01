// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package ai.vespa.feed.client.impl;

import ai.vespa.feed.client.FeedClientBuilder.Compression;
import ai.vespa.feed.client.HttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import static ai.vespa.feed.client.FeedClientBuilder.Compression.auto;
import static ai.vespa.feed.client.FeedClientBuilder.Compression.gzip;
import static org.apache.hc.core5.http.ssl.TlsCiphers.excludeH2Blacklisted;
import static org.apache.hc.core5.http.ssl.TlsCiphers.excludeWeak;

/**
 * @author jonmv
 */
class ApacheCluster implements Cluster {

    private final List<Endpoint> endpoints = new ArrayList<>();
    private final List<BasicHeader> defaultHeaders = Arrays.asList(new BasicHeader(HttpHeaders.USER_AGENT, String.format("vespa-feed-client/%s", Vespa.VERSION)),
                                                                   new BasicHeader("Vespa-Client-Version", Vespa.VERSION));
    private final Header gzipEncodingHeader = new BasicHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
    private final RequestConfig requestConfig;
    private final Compression compression;
    private int someNumber = 0;

    private final ExecutorService dispatchExecutor = Executors.newFixedThreadPool(8, t -> new Thread(t, "request-dispatch-thread"));
    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor(t -> new Thread(t, "request-timeout-thread"));

    ApacheCluster(FeedClientBuilderImpl builder) throws IOException {
        for (int i = 0; i < builder.connectionsPerEndpoint; i++)
            for (URI endpoint : builder.endpoints)
                endpoints.add(new Endpoint(createHttpClient(builder), endpoint));
        this.requestConfig = createRequestConfig(builder);
        this.compression = builder.compression;
    }

    @Override
    public void dispatch(HttpRequest wrapped, CompletableFuture<HttpResponse> vessel) {
        Endpoint leastBusy = endpoints.get(0);
        int min = Integer.MAX_VALUE;
        int start = ++someNumber % endpoints.size();
        for (int i = 0; i < endpoints.size(); i++) {
            Endpoint endpoint = endpoints.get((i + start) % endpoints.size());
            int inflight = endpoint.inflight.get();
            if (inflight < min) {
                leastBusy = endpoint;
                min = inflight;
            }
        }
        Endpoint endpoint = leastBusy;
        endpoint.inflight.incrementAndGet();

        dispatchExecutor.execute(() -> {
            try {
                SimpleHttpRequest request = new SimpleHttpRequest(wrapped.method(), wrapped.path());
                request.setScheme(endpoint.url.getScheme());
                request.setAuthority(new URIAuthority(endpoint.url.getHost(), portOf(endpoint.url)));
                long timeoutMillis = wrapped.timeout() == null ? 190_000 : wrapped.timeout().toMillis() * 11 / 10 + 1_000;
                request.setConfig(RequestConfig.copy(requestConfig).setResponseTimeout(Timeout.ofMilliseconds(timeoutMillis)).build());
                defaultHeaders.forEach(request::setHeader);
                wrapped.headers().forEach((name, value) -> request.setHeader(name, value.get()));
                if (wrapped.body() != null) {
                    byte[] body = wrapped.body();
                    if (compression == gzip || compression == auto && body.length > 512) {
                        request.setHeader(gzipEncodingHeader);
                        body = gzipped(body);
                    }
                    request.setBody(body, ContentType.APPLICATION_JSON);
                }

                Future<?> future = endpoint.client.execute(request,
                                                           new FutureCallback<SimpleHttpResponse>() {
                                                               @Override public void completed(SimpleHttpResponse response) { vessel.complete(new ApacheHttpResponse(response)); }
                                                               @Override public void failed(Exception ex) { vessel.completeExceptionally(ex); }
                                                               @Override public void cancelled() { vessel.cancel(false); }
                                                           });
                // We've seen some requests time out, even with a response timeout,
                // so we schedule this to be absolutely sure we don't hang (for ever).
                Future<?> cancellation = timeoutExecutor.schedule(() -> { future.cancel(true); vessel.cancel(true); },
                                                                  timeoutMillis + 10_000,
                                                                  TimeUnit.MILLISECONDS);
                vessel.whenComplete((__, ___) -> cancellation.cancel(true));
            }
            catch (Throwable thrown) {
                vessel.completeExceptionally(thrown);
            }
            vessel.whenComplete((__, ___) -> endpoint.inflight.decrementAndGet());
        });
    }

    private byte[] gzipped(byte[] content) throws IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1 << 10);
        try (GZIPOutputStream zip = new GZIPOutputStream(buffer)) {
            zip.write(content);
        }
        return buffer.toByteArray();
    }

    @Override
    public void close() {
        Throwable thrown = null;
        dispatchExecutor.shutdownNow().forEach(Runnable::run);
        for (Endpoint endpoint : endpoints) {
            try {
                endpoint.client.close();
            }
            catch (Throwable t) {
                if (thrown == null) thrown = t;
                else thrown.addSuppressed(t);
            }
        }
        timeoutExecutor.shutdownNow().forEach(Runnable::run);
        if (thrown != null) throw new RuntimeException(thrown);
    }


    private static class Endpoint {

        private final CloseableHttpAsyncClient client;
        private final AtomicInteger inflight = new AtomicInteger(0);
        private final URI url;

        private Endpoint(CloseableHttpAsyncClient client, URI url) {
            this.client = client;
            this.url = url;

            this.client.start();
        }

    }

    @SuppressWarnings("deprecation")
    private static CloseableHttpAsyncClient createHttpClient(FeedClientBuilderImpl builder) throws IOException {
        SSLContext sslContext = builder.constructSslContext();
        String[] allowedCiphers = excludeH2Blacklisted(excludeWeak(sslContext.getSupportedSSLParameters().getCipherSuites()));
        if (allowedCiphers.length == 0)
            throw new IllegalStateException("No adequate SSL cipher suites supported by the JVM");

        ClientTlsStrategyBuilder tlsStrategyBuilder = ClientTlsStrategyBuilder.create()
                .setTlsDetailsFactory(TlsDetailsFactory::create)
                .setCiphers(allowedCiphers)
                .setSslContext(sslContext);
        if (builder.hostnameVerifier != null)
            tlsStrategyBuilder.setHostnameVerifier(builder.hostnameVerifier);

        return HttpAsyncClients.createHttp2Minimal(H2Config.custom()
                                                           .setMaxConcurrentStreams(builder.maxStreamsPerConnection)
                                                           .setCompressionEnabled(true)
                                                           .setPushEnabled(false)
                                                           .setInitialWindowSize(Integer.MAX_VALUE)
                                                           .build(),
                                                   IOReactorConfig.custom()
                                                                  .setIoThreadCount(2)
                                                                  .setTcpNoDelay(true)
                                                                  .setSoTimeout(Timeout.ofSeconds(10))
                                                                  .build(),
                                                   tlsStrategyBuilder.build());
    }

    private static int portOf(URI url) {
        return url.getPort() == -1 ? url.getScheme().equals("http") ? 80 : 443
                                   : url.getPort();
    }

    @SuppressWarnings("deprecation")
    private static RequestConfig createRequestConfig(FeedClientBuilderImpl b) {
        RequestConfig.Builder builder = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10))
                .setConnectionRequestTimeout(Timeout.DISABLED);
        if (b.proxy != null) builder.setProxy(new HttpHost(b.proxy.getScheme(), b.proxy.getHost(), b.proxy.getPort()));
        return builder.build();
    }

    private static class ApacheHttpResponse implements HttpResponse {

        private final SimpleHttpResponse wrapped;

        private ApacheHttpResponse(SimpleHttpResponse wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public int code() {
            return wrapped.getCode();
        }

        @Override
        public byte[] body() {
            return wrapped.getBodyBytes();
        }

        @Override
        public String contentType() {
            return wrapped.getContentType().getMimeType();
        }

        @Override
        public String toString() {
            return "HTTP response with code " + code() +
                   (body() != null ? " and body '" + wrapped.getBodyText() + "'" : "");
        }

    }

}
