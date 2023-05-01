// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.grouping.request;

import java.util.Arrays;

/**
 * @author baldersheim
 * @author bratseth
 */
public class MathLog1pFunction extends FunctionNode {

    /**
     * Constructs a new instance of this class.
     *
     * @param exp The expression to evaluate, double value will be requested.
     */
    public MathLog1pFunction(GroupingExpression exp) {
        this(null, null, exp);
    }

    private MathLog1pFunction(String label, Integer level, GroupingExpression exp) {
        super("math.log1p", label, level, Arrays.asList(exp));
    }

    @Override
    public MathLog1pFunction copy() {
        return new MathLog1pFunction(getLabel(), getLevelOrNull(), getArg(0).copy());
    }

}
