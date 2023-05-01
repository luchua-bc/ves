// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include "name_repo.h"
#include <vespa/vespalib/util/printable.h>
#include <vespa/vespalib/stllike/string.h>
#include <memory>

namespace metrics {

struct AbstractCountMetric;
struct AbstractValueMetric;
class Metric;
class MetricSet;
class MetricSnapshot;
class MemoryConsumption;

/** Implement class to visit metrics. */
struct MetricVisitor {
    virtual ~MetricVisitor() {}

    /**
     * Visit a snapshot. Return true to visit content of the snapshot
     */
    virtual bool visitSnapshot(const MetricSnapshot&) { return true; }
    virtual void doneVisitingSnapshot(const MetricSnapshot&) {}

    /**
     * Visit a metric set.
     *
     * @param autoGenerated True for metric sets that are generated on the
     *                      fly such as in sum metrics.
     * @return True if you want to visit the content of this metric set.
     */
    virtual bool visitMetricSet(const MetricSet&, bool autoGenerated)
        { (void) autoGenerated; return true; }

    /**
     * Callback visitors can use if they need to know the tree traversal of
     * metric sets. This function is not called if visitMetricSet returned
     * false.
     */
    virtual void doneVisitingMetricSet(const MetricSet&) {}

    /**
     * Visit a primitive metric within an accepted metric set.
     *
     * @return True if you want to continue visiting, false to abort.
     */
    virtual bool visitCountMetric(const AbstractCountMetric& m,
                                  bool autoGenerated);
    /**
     * Visit a primitive metric within an accepted metric set.
     *
     * @return True if you want to continue visiting, false to abort.
     */
    virtual bool visitValueMetric(const AbstractValueMetric& m,
                                  bool autoGenerated);

    /**
     * Visit function for visiting primitive metrics in one function. Only
     * called if not overriding specific primitive metric functions.
     */
    virtual bool visitMetric(const Metric&, bool autoGenerated);

    /**
     * To avoid having to destruct the visitor in order to get any post
     * processing done you can use this hook. (Only called by functions knowing
     * they are top level, so you have to call this manually if visiting metrics
     * directly, and not through manager)
     */
    virtual void doneVisiting() {}
};

/**
 * A tag is a simple key-value mapping associated with a metric. Tags are used
 * for either constraining the set of metrics reported to a consumer (e.g. "do
 * not include metrics tagged as 'partofsum'") or to represent dimensions for
 * metric sets or individual metrics. In the former case, the key represents
 * the tag and the value remains empty. In the latter (dimension) case, the key
 * is the dimension name and the value is the dimension value.
 */
struct Tag
{
    const vespalib::string& key() const { return NameRepo::tagKey(_key); }
    const vespalib::string& value() const { return NameRepo::tagValue(_value); }

    Tag(vespalib::stringref k);
    Tag(vespalib::stringref k, vespalib::stringref v);
    Tag(const Tag &) noexcept;
    Tag & operator = (const Tag &);
    Tag(Tag &&) noexcept = default;
    Tag & operator = (Tag &&) = default;
    ~Tag();

    bool hasValue() const { return (_value != TagValueId::empty_handle); }

private:
    TagKeyId _key;
    TagValueId _value;
};

class Metric : public vespalib::Printable
{
public:
    using String = vespalib::string;
    using stringref = vespalib::stringref;
    using UP = std::unique_ptr<Metric>;
    using SP = std::shared_ptr<Metric>;
    using Tags = std::vector<Tag>;

    Metric(const String& name,
           Tags dimensions,
           const String& description,
           MetricSet* owner = 0);

    Metric(const Metric& other, MetricSet* owner);
    Metric(const Metric& rhs);
    Metric & operator = (const Metric& rhs);
    Metric(Metric && rhs) = default;
    Metric & operator = (Metric && rhs) = default;
    ~Metric();

    const vespalib::string& getName() const { return NameRepo::metricName(_name); }
    /**
     * Get mangled name iff the metric contains any dimensions, otherwise
     * the original metric name is returned.
     */
    const vespalib::string& getMangledName() const {
        return NameRepo::metricName(_mangledName);
    }
    vespalib::string getPath() const;
    std::vector<String> getPathVector() const;
    const vespalib::string& getDescription() const { return NameRepo::description(_description); }
    const Tags& getTags() const { return _tags; }
    /** Return whether there exists a tag with a key equal to 'tag' */
    bool hasTag(const String& tag) const;

    enum CopyType { CLONE, INACTIVE };
    /**
     * The clone function will clone metrics to an identical subtree of
     * metrics. Clone is primarily used for load metrics that wants to clone
     * a template metric for each loadtype. But it should work generically.
     *
     * @param type If set to inactive, sum metrics will evaluate to primitives
     *             and metrics can save memory by knowing no updates are coming.
     * @param includeUnused When creating snapshots we do not want to include
     *             unused metrics, but while generating sum metric sum in active
     *             metrics we want to. This has no affect if type is CLONE.
     */
    virtual Metric* clone(std::vector<Metric::UP> &ownerList, CopyType type, MetricSet* owner, bool includeUnused) const = 0;

    /**
     * Utility function for assigning values from one metric of identical type
     * to this metric. For simplicity sake it does a const cast and calls
     * addToSnapshot, which should not alter source if reset is false. This can
     * not be used to copy between active metrics and inactive copies.
     */
    virtual Metric* assignValues(const Metric& m);

    /** Reset all metric values. */
    virtual void reset() = 0;

    void print(std::ostream& out, bool verbose, const std::string& indent) const override {
        print(out, verbose, indent, 0);
    }
    virtual void print(std::ostream&, bool verbose, const std::string& indent,
                       uint64_t secondsPassed) const = 0;

    /**
     * Most metrics report numbers of some kind. To be able to report numbers
     * without having code to handle each possible metric type, these functions
     * exist to extract raw data to present easily.
     * @param id The part of the metric to extract. For instance, an average
     *           metric have average,
     */
    virtual int64_t getLongValue(stringref id) const = 0;
    virtual double getDoubleValue(stringref id) const = 0;

    /**
     * When snapshotting we need to be able to add data from one set of metrics
     * to another set of metrics taken at another time. MetricSet doesn't know
     * the type of the metrics it contains, so we need a generic function for
     * doing this. This function assumes metric given as input is of the exact
     * same type as the one it is called on for simplicity. This is true when
     * adding to snapshots as they have been created with clone and is thus
     * always exactly equal.
     *
     * @param m Metric of exact same type as this one. (Will core if wrong)
     * @param ownerList In case snapshot doesn't contain given metric, it can
     *                  create them and add them to ownerlist.
     */
    virtual void addToSnapshot(Metric& m, std::vector<Metric::UP> &ownerList) const = 0;

    /**
     * For sum metrics to work with metric sets, metric sets need operator+=.
     * To implement this, we need a function to add any metric type together.
     * This is different from adding to snapshot. When adding to snapshots we
     * add different time periods to the same metric, but when adding parts
     * together we add different metrics for the same time. For instance, an
     * average metric of queuesize, should just add new values to create new
     * average when adding to snapshot, but when adding parts, the averages
     * themselves should be added together.
     *
     * @param m Metric of exact same type as this one. (Will core if wrong)
     */
    virtual void addToPart(Metric& m) const = 0;

    virtual bool visit(MetricVisitor& visitor, bool tagAsAutoGenerated = false) const = 0;

    /** Used by sum metric to alter name of cloned metric for sum. */
    void setName(const String& name) {
        MetricNameId newName = NameRepo::metricId(name);
        _name = newName;
        assignMangledNameWithDimensions();
    }

    /** Used by sum metric to alter description of cloned metric for sum. */
    void setDescription(const vespalib::string& d) {
        _description = NameRepo::descriptionId(d);
    }
    /** Used by sum metric to alter tag of cloned metric for sum. */
    void setTags(Tags tags) {
        _tags = std::move(tags);
        assignMangledNameWithDimensions();
    }

    /** Set whether metrics have ever been set. */
    virtual bool used() const = 0;

    /** Returns true if this metric is registered in a metric set. */
    bool isRegistered() const { return (_owner != 0); }

    const MetricSet* getOwner() const { return _owner; }
    const MetricSet* getRoot() const;

    /** Used by metric set in register functions. */
    void setRegistered(MetricSet* owner) { _owner = owner; }

    virtual void addMemoryUsage(MemoryConsumption&) const;

    /** Print debug information of the metric tree. */
    virtual void printDebug(std::ostream&, const std::string& indent="") const;

    virtual bool isMetricSet() const { return false; }

    virtual bool is_sum_metric() const;

private:

    /**
     * A Tag instance is only considered to be a valid dimension if it contains
     * a value (i.e. not just a key as with legacy tags).
     */
    bool tagsSpecifyAtLeastOneDimension(const Tags&) const;

    /**
     * Iff _tags contains at least one valid dimension, update _mangledName to
     * be a string of the form 'metricname{key_0=value_0,key_1=value_1}' for
     * each pair of valid dimension key/values. _mangledName is left untouched
     * if no such valid dimension exists.
     */
    void assignMangledNameWithDimensions();

    /**
     * Sort _tags so that its entries are order in increasing lexicographic
     * order. Dimensions/tags must be in deterministic order for duplicate
     * detection based on name to work correctly.
     */
    void sortTagsInDeterministicOrder();

    vespalib::string createMangledNameWithDimensions() const;

    void verifyConstructionParameters();
    /**
     * Registers with metric set iff owner is not nullptr. This will implicitly
     * set _owner via a metric set callback to Metric::setRegistered().
     */
    void registerWithOwnerIfRequired(MetricSet* owner);

protected:
    MetricNameId _name;
    MetricNameId _mangledName;
    DescriptionId _description;
    std::vector<Tag> _tags;
    MetricSet* _owner;

};

} // metrics
