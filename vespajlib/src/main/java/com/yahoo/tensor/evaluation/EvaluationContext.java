// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.tensor.evaluation;

import com.yahoo.tensor.Tensor;

/**
 * An evaluation context which is passed down to all nested functions during evaluation.
 *
 * @author bratseth
 */
public interface EvaluationContext<NAMETYPE extends Name> extends TypeContext<NAMETYPE> {

    /** Returns the tensor bound to this name, or null if none */
    Tensor getTensor(String name);

}
