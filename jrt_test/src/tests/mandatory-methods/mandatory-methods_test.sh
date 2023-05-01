#!/bin/bash
# Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

if [ -z "$SOURCE_DIRECTORY" ]; then
    SOURCE_DIRECTORY="."
fi

fail=0

. ../../binref/env.sh
export PORT_1

$BINREF/compilejava -d . $SOURCE_DIRECTORY/RPCServer.java || fail=1

bash $SOURCE_DIRECTORY/dotest.sh || fail=1

$SOURCE_DIRECTORY/../../binref/progctl.sh $SOURCE_DIRECTORY/progdefs.sh stop all

exit $fail
