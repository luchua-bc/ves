// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.provision.maintenance;

import com.yahoo.component.Version;
import com.yahoo.config.provision.NodeType;
import com.yahoo.vespa.hosted.provision.NodeRepositoryTester;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author freva
 */
public class InfrastructureVersionsTest {

    private final Version defaultVersion = Version.fromString("6.13.37");
    private final NodeRepositoryTester tester = new NodeRepositoryTester();
    private final InfrastructureVersions infrastructureVersions =
            new InfrastructureVersions(tester.nodeRepository().database(), defaultVersion);

    private final Version version = Version.fromString("6.123.456");

    @Test
    public void can_only_downgrade_with_force() {
        assertTrue(infrastructureVersions.getTargetVersions().isEmpty());

        assertEquals(defaultVersion, infrastructureVersions.getTargetVersionFor(NodeType.config));
        infrastructureVersions.setTargetVersion(NodeType.config, version, false);
        assertEquals(version, infrastructureVersions.getTargetVersionFor(NodeType.config));

        // Upgrading to new version without force is fine
        Version newVersion = Version.fromString("6.123.457"); // version + 1
        infrastructureVersions.setTargetVersion(NodeType.config, newVersion, false);
        assertEquals(newVersion, infrastructureVersions.getTargetVersionFor(NodeType.config));

        // Downgrading to old version without force fails
        try {
            infrastructureVersions.setTargetVersion(NodeType.config, version, false);
            fail("Should not be able to downgrade without force");
        } catch (IllegalArgumentException ignored) { }

        infrastructureVersions.setTargetVersion(NodeType.config, version, true);
        assertEquals(version, infrastructureVersions.getTargetVersionFor(NodeType.config));
    }

    @Test
    public void can_only_set_version_on_certain_node_types() {
        // We can set version for config
        infrastructureVersions.setTargetVersion(NodeType.config, version, false);

        try {
            infrastructureVersions.setTargetVersion(NodeType.tenant, version, false);
            fail("Should not be able to set version for tenant nodes");
        } catch (IllegalArgumentException ignored) { }

        try {
            // Using 'force' does not help, force only applies to version downgrade
            infrastructureVersions.setTargetVersion(NodeType.tenant, version, true);
            fail("Should not be able to set version for tenant nodes");
        } catch (IllegalArgumentException ignored) { }
    }

    @Test
    public void can_store_multiple_versions() {
        Version version2 = Version.fromString("6.456.123");

        infrastructureVersions.setTargetVersion(NodeType.config, version, false);
        infrastructureVersions.setTargetVersion(NodeType.confighost, version2, false);
        infrastructureVersions.setTargetVersion(NodeType.proxyhost, version, false);
        infrastructureVersions.setTargetVersion(NodeType.controller, version, false);
        infrastructureVersions.setTargetVersion(NodeType.controllerhost, version2, false);

        Map<NodeType, Version> expected = new HashMap<>();
        expected.put(NodeType.config, version);
        expected.put(NodeType.confighost, version2);
        expected.put(NodeType.proxyhost, version);
        expected.put(NodeType.controller, version);
        expected.put(NodeType.controllerhost, version2);

        assertEquals(expected, infrastructureVersions.getTargetVersions());
    }
}
