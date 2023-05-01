// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.model.test;

/**
 * This is a simple API for testing the plugin api exchange mechanism.
 *
 * @author  gjoranv
 */
public interface TestApi {
    public int getNumSimpleServices();
    public int getNumParentServices();
}
