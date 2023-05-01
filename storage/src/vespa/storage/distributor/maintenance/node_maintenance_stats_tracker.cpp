// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#include "node_maintenance_stats_tracker.h"
#include <ostream>

namespace storage::distributor {

const NodeMaintenanceStats NodeMaintenanceStatsTracker::_emptyNodeMaintenanceStats;

void
NodeMaintenanceStats::merge(const NodeMaintenanceStats& rhs)
{
    movingOut  += rhs.movingOut;
    syncing    += rhs.syncing;
    copyingIn  += rhs.copyingIn;
    copyingOut += rhs.copyingOut;
    total      += rhs.total;
}

namespace {

void
merge_bucket_spaces_stats(NodeMaintenanceStatsTracker::BucketSpacesStats& dest,
                          const NodeMaintenanceStatsTracker::BucketSpacesStats& src)
{
    for (const auto& entry : src) {
        auto bucket_space = entry.first;
        dest[bucket_space].merge(entry.second);
    }
}

}

void
NodeMaintenanceStatsTracker::merge(const NodeMaintenanceStatsTracker& rhs)
{
    for (const auto& entry : rhs._node_stats) {
        auto node_index = entry.first;
        merge_bucket_spaces_stats(_node_stats[node_index], entry.second);
    }
    _max_observed_time_since_last_gc = std::max(_max_observed_time_since_last_gc,
                                                rhs._max_observed_time_since_last_gc);
}

std::ostream&
operator<<(std::ostream& os, const NodeMaintenanceStats& stats)
{
    os << "NodeStats("
       << "movingOut="   << stats.movingOut
       << ",syncing="    << stats.syncing
       << ",copyingIn="  << stats.copyingIn
       << ",copyingOut=" << stats.copyingOut
       << ",total="      << stats.total
       << ")";
    return os;
}

NodeMaintenanceStatsTracker::NodeMaintenanceStatsTracker()
    : _node_stats(),
      _total_stats(),
      _max_observed_time_since_last_gc(0)
{}

NodeMaintenanceStatsTracker::~NodeMaintenanceStatsTracker() = default;

}

