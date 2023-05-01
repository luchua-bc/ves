// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.jdisc.http.server.jetty;

import com.yahoo.jdisc.Metric;
import com.yahoo.jdisc.http.ServerConfig;
import com.yahoo.jdisc.service.CurrentContainer;

public class JDiscContext {
    final FilterResolver filterResolver;
    final CurrentContainer container;
    final Janitor janitor;
    final Metric metric;
    final ServerConfig serverConfig;

    public JDiscContext(FilterBindings filterBindings,
                        CurrentContainer container,
                        Janitor janitor,
                        Metric metric,
                        ServerConfig serverConfig) {

        this.filterResolver = new FilterResolver(filterBindings, metric, serverConfig.strictFiltering());
        this.container = container;
        this.janitor = janitor;
        this.metric = metric;
        this.serverConfig = serverConfig;
    }

    public boolean developerMode() {
        return serverConfig.developerMode();
    }
}
