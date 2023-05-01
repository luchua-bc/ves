// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.documentapi;

import com.yahoo.messagebus.Trace;

/**
 * This response is provided for successful document update operations. Use the
 * wasFound() method to check whether or not the document was actually found.
 *
 * @author Simon Thoresen Hult
 */
public class UpdateResponse extends Response {

    private final boolean wasFound;

    public UpdateResponse(long requestId, boolean wasFound) {
        this(requestId, wasFound, null);
    }

    public UpdateResponse(long requestId, boolean wasFound, Trace trace) {
        super(requestId, null, wasFound ? Outcome.SUCCESS : Outcome.NOT_FOUND, trace);
        this.wasFound = wasFound;
    }

    public boolean wasFound() {
        return wasFound;
    }

    @Override
    // TODO: fix this when/if NOT_FOUND is no longer a success.
    public boolean isSuccess() { return super.isSuccess() || outcome() == Outcome.NOT_FOUND; }

    @Override
    public int hashCode() {
        return super.hashCode() + Boolean.valueOf(wasFound).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UpdateResponse)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        UpdateResponse rhs = (UpdateResponse)obj;
        if (wasFound != rhs.wasFound) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Update" + super.toString() + " " + wasFound;
    }
}
