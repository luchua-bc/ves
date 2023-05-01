// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include "unaryfunctionnode.h"

namespace search {
namespace expression {

class ReverseFunctionNode : public UnaryFunctionNode
{
public:
    DECLARE_EXPRESSIONNODE(ReverseFunctionNode);
    ReverseFunctionNode() { }
    ReverseFunctionNode(ExpressionNode::UP  arg) : UnaryFunctionNode(std::move(arg)) { }
private:
    bool onExecute() const override;
};

}
}

