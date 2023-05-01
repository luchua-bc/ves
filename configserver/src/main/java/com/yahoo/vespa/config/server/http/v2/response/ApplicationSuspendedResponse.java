// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.config.server.http.v2.response;

import com.yahoo.jdisc.Response;
import com.yahoo.vespa.config.server.http.JSONResponse;

public class ApplicationSuspendedResponse extends JSONResponse {
    public ApplicationSuspendedResponse(boolean suspended) {
        super(Response.Status.OK);
        object.setBool("suspended", suspended);
    }
}
