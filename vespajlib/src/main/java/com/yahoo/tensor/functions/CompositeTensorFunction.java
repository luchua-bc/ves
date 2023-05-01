// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.tensor.functions;

import com.yahoo.tensor.Tensor;
import com.yahoo.tensor.TensorType;
import com.yahoo.tensor.evaluation.EvaluationContext;
import com.yahoo.tensor.evaluation.Name;
import com.yahoo.tensor.evaluation.TypeContext;

/**
 * A composite tensor function is a tensor function which can be expressed (less tersely)
 * as a tree of primitive tensor functions.
 *
 * @author bratseth
 */
public abstract class CompositeTensorFunction<NAMETYPE extends Name> extends TensorFunction<NAMETYPE> {

    /** Finds the type this produces by first converting it to a primitive function */
    @Override
    public final TensorType type(TypeContext<NAMETYPE> context) {
        return toPrimitive().type(context);
    }

    /** Evaluates this by first converting it to a primitive function */
    @Override
    public Tensor evaluate(EvaluationContext<NAMETYPE> context) {
        return toPrimitive().evaluate(context);
    }

}
