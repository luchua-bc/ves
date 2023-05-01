#!/bin/sh
# Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

class=$1
guard=`echo $class | tr 'a-z' 'A-Z'`
name=`echo $class | tr 'A-Z' 'a-z'`

cat <<EOF
// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#include "$name.h"

namespace mbus {

$class::$class()
{
}

$class::~$class()
{
}

} // namespace mbus
EOF
