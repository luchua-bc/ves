// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.jrt;


import java.nio.ByteBuffer;


/**
 * 64-bit floating-point value
 **/
public class DoubleValue extends Value
{
    private double value;

    /**
     * Create from a Java-type value
     *
     * @param value the value
     **/
    public DoubleValue(double value) { this.value = value; }

    /**
     * Create by decoding the value from the given buffer
     *
     * @param src buffer where the value is stored
     **/
    DoubleValue(ByteBuffer src) {
        value = src.getDouble();
    }

    /**
     * @return DOUBLE
     **/
    public byte type() { return DOUBLE; }
    public int count() { return 1; }

    int bytes() { return 8; }
    void encode(ByteBuffer dst) {
        dst.putDouble(value);
    }

    public double asDouble() { return value; }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
