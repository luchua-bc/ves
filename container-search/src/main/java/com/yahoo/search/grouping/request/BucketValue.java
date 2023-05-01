// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.grouping.request;

import java.util.Objects;

/**
 * This class represents a bucket in a {@link PredefinedFunction}. The generic T is the data type of the range values
 * 'from' and 'to'. The range is inclusive-from and exclusive-to. All supported data types are represented as subclasses
 * of this.
 *
 * @author Simon Thoresen Hult
 */
public class BucketValue extends GroupingExpression implements Comparable<BucketValue> {

    private final ConstantValue<?> from;
    private final ConstantValue<?> to;
    private final ConstantValueComparator comparator = new ConstantValueComparator();

    protected BucketValue(String label, Integer level, ConstantValue<?> inclusiveFrom, ConstantValue<?> exclusiveTo) {
        super("bucket[" + asImage(inclusiveFrom) + ", " + asImage(exclusiveTo) + ">", label, level);
        if (comparator.compare(exclusiveTo, inclusiveFrom) < 0) {
            throw new IllegalArgumentException("Bucket to-value can not be less than from-value.");
        }
        from = inclusiveFrom;
        to = exclusiveTo;
    }

    @Override
    public BucketValue copy() {
        return new BucketValue(getLabel(), getLevelOrNull(), getFrom().copy(), getTo().copy());
    }

    /**
     * Returns the inclusive-from value of this bucket.
     *
     * @return The from-value.
     */
    public ConstantValue<?> getFrom() {
        return from;
    }

    /**
     * Returns the exclusive-to value of this bucket.
     *
     * @return The to-value.
     */
    public ConstantValue<?> getTo() {
        return to;
    }

    @Override
    public int compareTo(BucketValue rhs) {
        if (comparator.compare(to, rhs.from) <= 0) return -1;
        if (comparator.compare(from, rhs.to) >= 0) return 1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ( ! (o instanceof BucketValue)) return false;

        BucketValue other = (BucketValue)o;
        if ( ! Objects.equals(this.from, other.from)) return false;
        if ( ! Objects.equals(this.to, other.to)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

}
