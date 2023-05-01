// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

namespace search::diskindex {

enum class BitVectorKeyScope
{
    SHARED_WORDS,
    PERFIELD_WORDS
};

const char *getBitVectorKeyScopeSuffix(BitVectorKeyScope scope);

}
