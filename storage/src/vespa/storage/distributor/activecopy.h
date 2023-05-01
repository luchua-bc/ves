// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include <vespa/storage/bucketdb/bucketdatabase.h>

namespace storage::lib { class Distribution; }
namespace storage::distributor {

class ActiveList;

struct ActiveCopy {
    constexpr ActiveCopy() noexcept : _nodeIndex(-1), _ideal(-1), _doc_count(0), _ready(false), _active(false) { }
    ActiveCopy(uint16_t node, const BucketDatabase::Entry& e, const std::vector<uint16_t>& idealState);

    vespalib::string getReason() const;
    friend std::ostream& operator<<(std::ostream& out, const ActiveCopy& e);

    static ActiveList calculate(const std::vector<uint16_t>& idealState,
                                const lib::Distribution&,
                                BucketDatabase::Entry&,
                                uint32_t max_activation_inhibited_out_of_sync_groups);

    uint16_t _nodeIndex;
    uint16_t _ideal;
    uint32_t _doc_count;
    bool     _ready;
    bool     _active;
};

class ActiveList : public vespalib::Printable {
    std::vector<ActiveCopy> _v;

public:
    ActiveList() {}
    ActiveList(std::vector<ActiveCopy>&& v) : _v(std::move(v)) { }

    ActiveCopy& operator[](size_t i) noexcept { return _v[i]; }
    const ActiveCopy& operator[](size_t i) const noexcept { return _v[i]; }
    [[nodiscard]] bool contains(uint16_t) const noexcept;
    [[nodiscard]] bool empty() const noexcept { return _v.empty(); }
    size_t size() const noexcept { return _v.size(); }
    void print(std::ostream&, bool verbose, const std::string& indent) const override;
};

}
