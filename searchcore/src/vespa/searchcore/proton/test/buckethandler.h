// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include <vespa/searchcore/proton/server/ibucketstatechangednotifier.h>
#include <vespa/searchcore/proton/server/ibucketstatechangedhandler.h>
#include <set>

namespace proton
{

namespace test
{

/**
 * Test bucket handler that forwards bucket state change notifications
 * as appropriate.
 */
class BucketHandler : public IBucketStateChangedNotifier
{
    std::set<IBucketStateChangedHandler *> _handlers;
public:
    BucketHandler();

    virtual
    ~BucketHandler();

    virtual void
    addBucketStateChangedHandler(IBucketStateChangedHandler *handler) override;

    virtual void
    removeBucketStateChangedHandler(IBucketStateChangedHandler *handler) override;

    void
    notifyBucketStateChanged(const document::BucketId &bucketId,
                             storage::spi::BucketInfo::ActiveState newState);
};


} // namespace test

} // namespace proton
