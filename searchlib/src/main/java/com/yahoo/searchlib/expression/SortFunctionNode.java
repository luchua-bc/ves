// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.searchlib.expression;

/**
 * @author baldersheim
 */
public class SortFunctionNode extends UnaryFunctionNode {

    public static final int classId = registerClass(0x4000 + 137, SortFunctionNode.class);

    /**
     * Constructs an empty result node. <b>NOTE:</b> This instance is broken until non-optional member data is set.
     */
    public SortFunctionNode() {

    }

    /**
     * Constructs an instance of this class with given argument.
     *
     * @param arg The argument for this function.
     */
    public SortFunctionNode(ExpressionNode arg) {
        addArg(arg);
    }

    @Override
    protected int onGetClassId() {
        return classId;
    }

    @Override
    protected boolean equalsUnaryFunction(UnaryFunctionNode obj) {
        return true;
    }
}
