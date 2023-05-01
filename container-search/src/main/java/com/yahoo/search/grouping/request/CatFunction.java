// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.grouping.request;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a cat-function in a {@link GroupingExpression}. It evaluates to a byte array that equals the
 * concatenation of the binary result of all arguments in the order they were given to the constructor.
 *
 * @author Simon Thoresen Hult
 * @author bratseth
 */
public class CatFunction extends FunctionNode {

    /**
     * Constructs a new instance of this class.
     *
     * @param arg1 The first compulsory argument.
     * @param arg2 The second compulsory argument.
     * @param argN The optional arguments.
     */
    public CatFunction(GroupingExpression arg1, GroupingExpression arg2, GroupingExpression... argN) {
        this(null, null, asList(arg1, arg2, argN));
    }

    private CatFunction(String label, Integer level, List<GroupingExpression> args) {
        super("cat", label, level, args);
    }

    @Override
    public CatFunction copy() {
        return new CatFunction(getLabel(),
                               getLevelOrNull(),
                               args().stream().map(arg -> arg.copy()).toList());
    }

    /**
     * Constructs a new instance of this class from a list of arguments.
     *
     * @param args The arguments to pass to the constructor.
     * @return The created instance.
     * @throws IllegalArgumentException Thrown if the number of arguments is less than 2.
     */
    public static CatFunction newInstance(List<GroupingExpression> args) {
        if (args.size() < 2) {
            throw new IllegalArgumentException("Expected 2 or more arguments, got " + args.size() + ".");
        }
        return new CatFunction(null, null, args);
    }
}
