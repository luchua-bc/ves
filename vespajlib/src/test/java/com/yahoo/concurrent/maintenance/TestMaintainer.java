// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.concurrent.maintenance;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author mpolden
 */
class TestMaintainer extends Maintainer {

    private int totalRuns = 0;
    private double success = 1.0;
    private RuntimeException exceptionToThrow = null;

    public TestMaintainer(String name, JobControl jobControl, JobMetrics jobMetrics) {
        super(name, Duration.ofDays(1), Clock.systemUTC(), jobControl, jobMetrics, List.of(), false);
    }

    public int totalRuns() {
        return totalRuns;
    }

    public TestMaintainer successOnNextRun(double success) {
        this.success = success;
        return this;
    }

    public TestMaintainer throwOnNextRun(RuntimeException e) {
        this.exceptionToThrow = e;
        return this;
    }

    @Override
    protected double maintain() {
        if (exceptionToThrow != null) throw exceptionToThrow;
        totalRuns++;
        return success;
    }

}
