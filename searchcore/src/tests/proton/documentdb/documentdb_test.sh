#!/bin/bash
# Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
set -e
$VALGRIND ./searchcore_documentdb_test_app
rm -rf typea tmp
