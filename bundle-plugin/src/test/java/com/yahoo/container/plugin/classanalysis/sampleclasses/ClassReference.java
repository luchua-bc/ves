// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.container.plugin.classanalysis.sampleclasses;

/**
 * @author Tony Vaagenes
 */
public class ClassReference {
    void classReference() {
        @SuppressWarnings("unused")
        Class<?> clazz = Interface1.class;
    }
}
