// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include <vespa/vespalib/stllike/string.h>

namespace vespalib {

vespalib::string getErrorString(const int osError);

vespalib::string getLastErrorString();

}
