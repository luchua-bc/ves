// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include <vespa/storage/distributor/operations/sequenced_operation.h>
#include <vespa/storage/distributor/persistencemessagetracker.h>

namespace storage::api { class RemoveCommand; }

namespace storage::distributor {

class DistributorBucketSpace;

class RemoveOperation : public SequencedOperation
{
public:
    RemoveOperation(const DistributorNodeContext& node_ctx,
                    DistributorStripeOperationContext& op_ctx,
                    DistributorBucketSpace& bucketSpace,
                    std::shared_ptr<api::RemoveCommand> msg,
                    PersistenceOperationMetricSet& metric,
                    SequencingHandle sequencingHandle = SequencingHandle());
    ~RemoveOperation() override;

    void onStart(DistributorStripeMessageSender& sender) override;
    const char* getName() const noexcept override { return "remove"; };
    std::string getStatus() const override { return ""; };

    void onReceive(DistributorStripeMessageSender& sender, const std::shared_ptr<api::StorageReply> &) override;
    void onClose(DistributorStripeMessageSender& sender) override;

private:
    PersistenceMessageTrackerImpl       _trackerInstance;
    PersistenceMessageTracker&          _tracker;
    std::shared_ptr<api::RemoveCommand> _msg;
    const DistributorNodeContext&       _node_ctx;
    DistributorBucketSpace&             _bucketSpace;
};

}
