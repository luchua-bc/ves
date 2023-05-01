// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.config.server.deploy;

import com.yahoo.config.application.api.ApplicationPackage;
import com.yahoo.config.application.api.FileRegistry;
import com.yahoo.config.provision.AllocatedHosts;
import com.yahoo.component.Version;

import java.io.IOException;
import java.util.Map;

/**
 * Interface for initializing zookeeper and deploying an application package to zookeeper.
 *
 * @author Ulf Lilleengen
 */
public class ZooKeeperDeployer {

    private final ZooKeeperClient zooKeeperClient;

    public ZooKeeperDeployer(ZooKeeperClient client) {
        this.zooKeeperClient = client;
    }

    /**
     * Deploys an application package to zookeeper
     *
     * @param applicationPackage the application package to persist.
     * @param fileRegistryMap the file registries to persist.
     * @param allocatedHosts the allocated hosts to persist.
     * @throws IOException if deploying fails
     */
    public void deploy(ApplicationPackage applicationPackage, Map<Version, FileRegistry> fileRegistryMap, 
                       AllocatedHosts allocatedHosts) throws IOException {
        zooKeeperClient.initialize();
        zooKeeperClient.writeApplicationPackage(applicationPackage);
        zooKeeperClient.write(fileRegistryMap);
        zooKeeperClient.write(allocatedHosts);
    }

    public void cleanup() {
        zooKeeperClient.cleanupZooKeeper();
    }

}
