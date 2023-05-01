// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.lang;

/**
 * A mutable integer
 *
 * @author bratseth
 */
public class MutableInteger {

    private int value;

    public MutableInteger(int value) {
        this.value = value;
    }

    public int get() { return value; }

    public void set(int value) { this.value = value; }

    /** Adds the increment to the current value and returns the resulting value */
    public int add(int increment) {
        value += increment;
        return value;
    }

    /** Increments the value by 1 and returns the value of this *before* incrementing */
    public int next() {
        value++;
        return value - 1;
    }

    /** Adds the increment to the current value and returns the resulting value */
    public int subtract(int increment) {
        value -= increment;
        return value;
    }

    @Override
    public String toString() { return Integer.toString(value); }

}
