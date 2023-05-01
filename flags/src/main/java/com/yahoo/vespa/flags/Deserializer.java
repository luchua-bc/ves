// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.flags;

/**
 * @author hakonhall
 */
@FunctionalInterface
public interface Deserializer<T> {
    T deserialize(RawFlag rawFlag);
}
