// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include <vespa/document/bucket/bucketspace.h>
#include <vespa/storage/bucketdb/bucketdatabase.h>

namespace storage::distributor {

class MaintenanceScanner
{
public:
    virtual ~MaintenanceScanner() = default;

    class ScanResult {
        bool _done;
        document::BucketSpace _bucketSpace;
        BucketDatabase::Entry _entry;

    public:
        [[nodiscard]] bool isDone() const noexcept { return _done; }
        document::BucketSpace getBucketSpace() const noexcept { return _bucketSpace; }
        const BucketDatabase::Entry& getEntry() const noexcept { return _entry; }

        static ScanResult createDone() { return ScanResult(true); }
        static ScanResult createNotDone(document::BucketSpace bucketSpace, BucketDatabase::Entry entry) {
            return ScanResult(bucketSpace, entry);
        }

    private:
        explicit ScanResult(bool done) : _done(done), _bucketSpace(document::BucketSpace::invalid()), _entry() {}
        ScanResult(document::BucketSpace bucketSpace, const BucketDatabase::Entry& e) : _done(false), _bucketSpace(bucketSpace), _entry(e) {}
    };

    virtual ScanResult scanNext() = 0;

    virtual void reset() = 0;
};

}
