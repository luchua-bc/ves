// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include "documentacceptedreply.h"

namespace documentapi {

/**
 * This reply class is used by operations that perform writes to VDS/search,
 * that is: Put, Remove, Update.
 */
class WriteDocumentReply : public DocumentAcceptedReply {
private:
    uint64_t _highestModificationTimestamp;

public:
    WriteDocumentReply(uint32_t type) :
        DocumentAcceptedReply(type),
        _highestModificationTimestamp(0)
    {}

    /**
     * Returns a unique VDS timestamp so that visiting up to and including that
     * timestamp will return a state including this operation but not any
     * operations sent to the same distributor after it. For PUT/UPDATE/REMOVE
     * operations this timestamp will be the timestamp of the operation.
     *
     * @return Returns the modification timestamp.
     */
    uint64_t getHighestModificationTimestamp() const {
        return _highestModificationTimestamp;
    }

    /**
     * Sets the modification timestamp.
     *
     * @param timestamp
     */
    void setHighestModificationTimestamp(uint64_t ts) {
        _highestModificationTimestamp = ts;
    }
};

}
