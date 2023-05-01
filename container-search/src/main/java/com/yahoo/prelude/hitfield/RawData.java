// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.prelude.hitfield;

/**
 * A representation of some binary data with unknown semantics
 *
 * @author arnej27959
 */
public final class RawData {

    private final byte[] content;

    /**
     * Constructor, takes ownership of the given byte array.
     *
     * @param content some bytes, handover
     */
    public RawData(byte[] content) {
        this.content = content;
    }

    /** Returns the internal byte array containing the actual data received */
    public byte[] getInternalData() {
        return content;
    }

    /**
     * An ascii string; non-ascii data is escaped with hex notation.
     * NB: not always uniquely reversible.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (byte b : content) {
            int i = b;
            i &= 0xFF;
            char cv = (char)i;
            if ((i > 31 && i < 127) || cv == '\n' || cv == '\t') {
                buf.append(cv);
            } else if (i < 16) {
                buf.append("\\x0");
                buf.append(Integer.toHexString(i));
            } else if (i < 256) {
                buf.append("\\x");
                buf.append(Integer.toHexString(i));
            } else {
                // XXX maybe we should only do this? creates possibly-invalid XML though.
                buf.append("&");
                buf.append(i);
                buf.append(";");
            }
        }
        return buf.toString();
    }

}
