package com.yahoo.compress;

import net.openhft.hashing.LongHashFunction;

/**
 * Utility for hashing providing multiple hashing methods
 * @author baldersheim
 */
public class Hasher {
    /** Uses net.openhft.hashing.LongHashFunction.xx3() */
    public static long xxh3(byte [] data) {
        return LongHashFunction.xx3().hashBytes(data);
    }
}
