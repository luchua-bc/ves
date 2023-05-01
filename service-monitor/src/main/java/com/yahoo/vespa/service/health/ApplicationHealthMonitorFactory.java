// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.service.health;

import com.yahoo.config.provision.ApplicationId;

/**
 * @author hakonhall
 */
@FunctionalInterface
interface ApplicationHealthMonitorFactory {
    ApplicationHealthMonitor create(ApplicationId applicationId);
}
