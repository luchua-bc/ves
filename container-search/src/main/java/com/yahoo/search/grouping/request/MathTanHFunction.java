// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.grouping.request;

import java.util.Arrays;

/**
 * @author baldersheim
 * @author bratseth
 */
public class MathTanHFunction extends FunctionNode {

    /**
     * Constructs a new instance of this class.
     *
     * @param exp The expression to evaluate, double value will be requested.
     */
    public MathTanHFunction(GroupingExpression exp) {
        this(null, null, exp);
    }

    private MathTanHFunction(String label, Integer level, GroupingExpression exp) {
        super("math.tanh", label, level, Arrays.asList(exp));
    }

    @Override
    public MathTanHFunction copy() {
        return new MathTanHFunction(getLabel(), getLevelOrNull(), getArg(0).copy());
    }

}
