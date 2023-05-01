// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.grouping.request;

import java.util.Arrays;

/**
 * Represents the math.floor(expression) function
 *
 * @author baldersheim
 * @author bratseth
 */
public class MathFloorFunction extends FunctionNode {

    /**
     * Constructs a new instance of this class.
     *
     * @param exp The expression to evaluate, double value will be requested.
     */
    public MathFloorFunction(GroupingExpression exp) {
        this(null, null, exp);
    }

    private MathFloorFunction(String label, Integer level, GroupingExpression exp) {
        super("math.floor", label, level, Arrays.asList(exp));
    }

    @Override
    public MathFloorFunction copy() {
        return new MathFloorFunction(getLabel(), getLevelOrNull(), getArg(0).copy());
    }

}
