// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include <vespa/searchcore/proton/common/feedtoken.h>
#include <vespa/vespalib/stllike/string.h>

namespace vespalib {
    class Executor;
}
namespace proton {

class FeedOperation;
struct PacketWrapper;

/**
 * Class representing the current state of a feed handler.
 */
class FeedState {
public:
    enum Type { NORMAL, REPLAY_TRANSACTION_LOG, INIT };

private:
    Type _type;

protected:
    using FeedOperationUP = std::unique_ptr<FeedOperation>;
    using PacketWrapperSP = std::shared_ptr<PacketWrapper>;
    void throwExceptionInReceive(const vespalib::string &docType, uint64_t serialRangeFrom,
                                 uint64_t serialRangeTo, size_t packetSize);
    void throwExceptionInHandleOperation(const vespalib::string &docType, const FeedOperation &op);

public:
    FeedState(Type type) : _type(type) {}
    virtual ~FeedState() = default;

    Type getType() const { return _type; }
    vespalib::string getName() const;

    virtual void handleOperation(FeedToken token, FeedOperationUP op) = 0;
    virtual void receive(const PacketWrapperSP &wrap, vespalib::Executor &executor) = 0;
};

}  // namespace proton
