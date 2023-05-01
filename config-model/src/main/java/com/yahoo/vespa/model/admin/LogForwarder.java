// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.model.admin;

import com.yahoo.cloud.config.LogforwarderConfig;
import com.yahoo.config.model.producer.TreeConfigProducer;
import com.yahoo.vespa.model.AbstractService;
import com.yahoo.vespa.model.PortAllocBridge;
import java.util.Optional;

public class LogForwarder extends AbstractService implements LogforwarderConfig.Producer {

    public static class Config {
        public final String deploymentServer;
        public final String clientName;
        public final String splunkHome;
        public final Integer phoneHomeInterval;
        public final String role;

        private Config(String ds, String cn, String sh, Integer phi, String role) {
            this.deploymentServer = ds;
            this.clientName = cn;
            this.splunkHome = sh;
            this.phoneHomeInterval = phi;
            this.role = role;
        }
        public Config withDeploymentServer(String ds) {
            return new Config(ds, clientName, splunkHome, phoneHomeInterval, role);
        }
        public Config withClientName(String cn) {
            return new Config(deploymentServer, cn, splunkHome, phoneHomeInterval, role);
        }
        public Config withSplunkHome(String sh) {
            return new Config(deploymentServer, clientName, sh, phoneHomeInterval, role);
        }
        public Config withPhoneHomeInterval(Integer phi) {
            return new Config(deploymentServer, clientName, splunkHome, phi, role);
        }

        public Config withRole(String role) {
            return new Config(deploymentServer, clientName, splunkHome, phoneHomeInterval, role);
        }
    }

    private final Config config;

    /**
     * Creates a new LogForwarder instance.
     */
    // TODO: Use proper types?
    public LogForwarder(TreeConfigProducer<? super LogForwarder> parent, Config config) {
        super(parent, "logforwarder");
        this.config = config;
        setProp("clustertype", "hosts");
        setProp("clustername", "admin");
        // Make role information available in supermodel
        Optional.ofNullable(config.role).map(r -> setProp("identity.role", r));
    }

    public static Config cfg() {
        return new Config(null, null, null, null, null);
    }

    // LogForwarder does not need any ports.
    @Override
    public void allocatePorts(int start, PortAllocBridge from) { }

    /**
     * LogForwarder does not need any ports.
     *
     * @return The number of ports reserved by the LogForwarder
     */
    public int getPortCount() { return 0; }

    /**
     * @return The command used to start LogForwarder
     */
    @Override
    public Optional<String> getStartupCommand() { return Optional.of("exec $ROOT/bin/vespa-logforwarder-start -c " + getConfigId()); }

    @Override
    public void getConfig(LogforwarderConfig.Builder builder) {
        builder.deploymentServer(config.deploymentServer);
        builder.clientName(config.clientName);
        if (config.splunkHome != null) {
            builder.splunkHome(config.splunkHome);
        }
        if (config.phoneHomeInterval != null) {
            builder.phoneHomeInterval(config.phoneHomeInterval);
        }
        if (config.role != null) {
            builder.role(config.role);
        }
    }

    @Override
    public Optional<String> getPreShutdownCommand() {
        var builder = new LogforwarderConfig.Builder();
        getConfig(builder);
        var cfg = new LogforwarderConfig(builder);
        var home = cfg.splunkHome();
        String cmd = "$ROOT/bin/vespa-logforwarder-start -S -c " + getConfigId();
        return Optional.of(cmd);
    }

}
