// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.jdisc.http.filter.security.misc;

import com.yahoo.jdisc.handler.ResponseHandler;
import com.yahoo.jdisc.http.filter.DiscFilterRequest;
import com.yahoo.jdisc.http.filter.SecurityRequestFilter;

/**
 * A no-op filter. Used for bindings that are whitelisted and do not require any authorization.
 *
 * @author mpolden
 */
@SuppressWarnings("unused") // Injected
public class NoopFilter implements SecurityRequestFilter {

    @Override
    public void filter(DiscFilterRequest request, ResponseHandler handler) {
    }

}
