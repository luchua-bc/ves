// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#include "storagecommand.h"
#include <vespa/vespalib/util/exceptions.h>
#include <ostream>

namespace storage::api {

namespace {
    constexpr vespalib::duration MAX_TIMEOUT = 3600s;
}

StorageCommand::StorageCommand(const StorageCommand& other)
    : StorageMessage(other, generateMsgId(), 0),
      _timeout(other._timeout),
      _sourceIndex(other._sourceIndex)
{
}

StorageCommand::StorageCommand(const MessageType& type, Priority p)
    : StorageMessage(type, generateMsgId(), 0),
      _timeout(MAX_TIMEOUT),
      _sourceIndex(0xFFFF)
{
    setPriority(p);
}

StorageCommand::~StorageCommand() = default;

void
StorageCommand::print(std::ostream& out, bool, const std::string&) const
{
    out << "StorageCommand(" << getType().getName();
    if (getPriority() != NORMAL) out << ", priority = " << static_cast<int>(getPriority());
    if (_sourceIndex != 0xFFFF) out << ", source = " << _sourceIndex;
    out << ", timeout = " << vespalib::count_ms(_timeout) << " ms";
    out << ")";
}

}
