// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.clustercontroller.utils.staterestapi.errors;

/**
 * @author hakonhall
 */
public class MissingResourceException extends StateRestApiException {

    public MissingResourceException(String resource) {
        super("Missing resource '" + resource + "'");
    }

}
