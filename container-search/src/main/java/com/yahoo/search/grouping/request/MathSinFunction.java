// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.grouping.request;

import java.util.Arrays;

/**
 * @author baldersheim
 * @author bratseth
 */
public class MathSinFunction extends FunctionNode {

    /**
     * Constructs a new instance of this class.
     *
     * @param exp The expression to evaluate, double value will be requested.
     */
    public MathSinFunction(GroupingExpression exp) {
        this(null, null, exp);
    }

    private MathSinFunction(String label, Integer level, GroupingExpression exp) {
        super("math.sin", label, level, Arrays.asList(exp));
    }

    @Override
    public MathSinFunction copy() {
        return new MathSinFunction(getLabel(), getLevelOrNull(), getArg(0).copy());
    }

}
