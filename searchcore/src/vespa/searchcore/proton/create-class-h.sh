#!/bin/sh
# Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

dir=`dirname $0`
. "$dir/create-base.sh"

cat <<EOF
#pragma once

$ns_open

class $class
{
private:
    $class(const $class &);
    $class &operator=(const $class &);
public:
    $class();
    virtual ~$class();
};

$ns_close

EOF
