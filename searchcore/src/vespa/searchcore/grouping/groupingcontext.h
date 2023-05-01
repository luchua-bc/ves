// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include <vespa/searchlib/aggregation/grouping.h>
#include <vespa/vespalib/objects/nbostream.h>
#include <vespa/vespalib/util/time.h>
#include <vector>
#include <memory>

namespace search::grouping {

/**
 * A Grouping Context contains all grouping expressions that should be evaluated
 * for a particular pass, together with the ability to serialize and deserialize
 * the data from/to a byte buffer.
 **/
class GroupingContext
{
public:
    using UP = std::unique_ptr<GroupingContext>;
    using GroupingPtr =  std::shared_ptr<search::aggregation::Grouping>;
    using GroupingList = std::vector<GroupingPtr>;

private:
    const vespalib::Clock & _clock;
    vespalib::steady_time   _timeOfDoom;
    vespalib::nbostream     _os;
    GroupingList            _groupingList;
public:

    /**
     * Deserialize a grouping spec into this context.
     *
     * @param groupSpec The grouping specification to use for initialization.
     * @param groupSpecLen The length of the grouping specification, in bytes.
     **/
    void deserialize(const char *groupSpec, uint32_t groupSpecLen);

    /**
     * Create a new grouping context from a byte buffer.
     * @param groupSpec The grouping specification to use for initialization.
     * @param groupSpecLen The length of the grouping specification, in bytes.
     **/
    GroupingContext(const vespalib::Clock & clock, vespalib::steady_time timeOfDoom, const char *groupSpec, uint32_t groupSpecLen);

    /**
     * Create a new grouping context from a byte buffer.
     * @param groupSpec The grouping specification to use for initialization.
     * @param groupSpecLen The length of the grouping specification, in bytes.
     **/
    GroupingContext(const vespalib::Clock & clock, vespalib::steady_time timeOfDoom);

    /**
     * Shallow copy of references
     **/
    GroupingContext(const GroupingContext & rhs);

    GroupingContext &operator=(const GroupingContext &) = delete;

    /**
     * Add another grouping to this context.
     * @param g Pointer to the grouping object to become part of this context.
     **/
    void addGrouping(const GroupingPtr & g);

    /**
     * Reset the context to an empty state.
     **/
    void reset() { _groupingList.clear(); }

    /**
     * Return the internal list of grouping expressions in this context.
     * @return a list of groupings.
     **/
    GroupingList &getGroupingList() { return _groupingList; }

    /**
     * Serialize the grouping expressions in this context.
     **/
    void serialize();

    /**
     * Check whether this context contains any groupings.
     **/
    bool empty() const { return _groupingList.empty(); }

    /**
     * Obtain the grouping result.
     *
     * @return grouping result
     **/
    vespalib::nbostream & getResult() { return _os; }

    /**
     * Count number of fs4hits
     *
     * @return number of fs4 hits.
     */
    size_t countFS4Hits();
    /**
     * Will inject the distribution key in the FS4Hits aggregated so far.
     *
     * @param the distribution key.
     */
    void setDistributionKey(uint32_t distributionKey);
    /**
     * Obtain the time of doom.
     */
    vespalib::steady_time getTimeOfDoom() const { return _timeOfDoom; }
    /**
     * Figure out if ranking is necessary for any of the grouping requests here.
     * @return true if ranking is required.
     */
    bool needRanking() const;
};

}
