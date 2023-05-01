// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
/**
 * \class metrics::MetricManager
 *
 * A metrics-enabled application should have a single MetricManager.
 * You can register a number of MetricSets in the MetricManager. Each
 * metric in the metrics sets can be used by zero or more consumers,
 * configurable using readConfig().
 *
 * The consumers get their data by calling the getSnapshot() method,
 * which gives them a snapshot of all the current metrics which are
 * configured for the given name.
 *
 * Locking strategy:
 *
 * There are three locks in this class:
 *
 * Config lock:
 *   - This protects the class on config changes. It protects the _config and
 *     _consumerConfig members.
 *
 * Thread monitor (waiter):
 *   - This lock is kept by the worker thread while it is doing a work cycle,
 *     and it uses this monitor to sleep. It is used to make shutdown quick by
 *     interrupting thread, and to let functions called by clients be able to
 *     do a change while the worker thread is idle.
 *   - The log period is protected by the thread monitor.
 *   - The update hooks is protected by the thread monitor.
 *
 * Metric lock:
 *  - The metric log protects the active metric set when adding or removing
 *    metrics. Clients need to grab this lock before altering active metrics.
 *    The metric manager needs to grab this lock everytime it visits active
 *    metrics.
 *  - The metric log protects the snapshots. The snapshot writer is the metric
 *    worker thread and will grab the lock while editing them. Readers that
 *    aren't the worker thread itself must grab lock to be sure.
 *
 * If multiple locks is taken, the allowed locking order is:
 *   1. Thread monitor.
 *   2. Metric lock.
 *   3. Config lock.
 */
#pragma once

#include "metricset.h"
#include "metricsnapshot.h"
#include "memoryconsumption.h"
#include "valuemetric.h"
#include "updatehook.h"
#include <vespa/vespalib/stllike/hash_set.h>
#include <vespa/vespalib/util/jsonwriter.h>
#include <vespa/metrics/config-metricsmanager.h>
#include <vespa/config/subscription/configsubscriber.h>
#include <vespa/config/subscription/configuri.h>
#include <map>
#include <list>
#include <thread>

namespace metrics {

class MetricManager
{
public:

    struct Timer {
        virtual ~Timer() = default;
        virtual time_point getTime() const;
        time_point getTimeInMilliSecs() const { return getTime(); }
    };

    /**
     * Spec saved from config. If metricSetChildren has content, metric pointed
     * to is a metric set.
     */
    struct ConsumerSpec {
        vespalib::hash_set<Metric::String> includedMetrics;

        ConsumerSpec(ConsumerSpec &&) noexcept = default;
        ConsumerSpec & operator= (ConsumerSpec &&) noexcept = default;
        ConsumerSpec();
        ~ConsumerSpec();

        bool contains(const Metric& m) const {
            return (includedMetrics.find(m.getPath()) != includedMetrics.end());
        }

        vespalib::string toString() const;

        void addMemoryUsage(MemoryConsumption&) const;
    };

private:
    MetricSnapshot _activeMetrics;
    std::unique_ptr<config::ConfigSubscriber> _configSubscriber;
    std::unique_ptr<config::ConfigHandle<MetricsmanagerConfig>> _configHandle;
    std::unique_ptr<MetricsmanagerConfig> _config;
    std::map<Metric::String, ConsumerSpec> _consumerConfig;
    std::list<UpdateHook*> _periodicUpdateHooks;
    std::list<UpdateHook*> _snapshotUpdateHooks;
    mutable std::mutex _waiter;
    mutable std::condition_variable _cond;
    std::vector<std::shared_ptr<MetricSnapshotSet>> _snapshots;
    std::shared_ptr<MetricSnapshot> _totalMetrics;
    std::unique_ptr<Timer> _timer;
    std::atomic<time_point> _lastProcessedTime;
    // Should be added to config, but wont now due to problems with
    // upgrading
    bool _snapshotUnsetMetrics;
    bool _consumerConfigChanged;

    MetricSet _metricManagerMetrics;
    LongAverageMetric _periodicHookLatency;
    LongAverageMetric _snapshotHookLatency;
    LongAverageMetric _resetLatency;
    LongAverageMetric _snapshotLatency;
    LongAverageMetric _sleepTimes;
    std::atomic<bool> _stop_requested;
    std::thread       _thread;

    void request_stop() { _stop_requested.store(true, std::memory_order_relaxed); }
    bool stop_requested() const { return _stop_requested.load(std::memory_order_relaxed); }
    
public:
    MetricManager();
    MetricManager(std::unique_ptr<Timer> timer);
    ~MetricManager();

    void stop();

    void snapshotUnsetMetrics(bool doIt) { _snapshotUnsetMetrics = doIt; }

    /**
     * Add a metric update hook. This will always be called prior to
     * snapshotting and metric logging, to make the metrics the best as they can
     * be at those occasions.
     *
     * @param period Period for how often callback should be called.
     *               The default value of 0, means only before snapshotting or
     *               logging, while another value will give callbacks each
     *               period seconds. Expensive metrics to calculate will
     *               typically only want to do it before snapshotting, while
     *               inexpensive metrics might want to log their value every 5
     *               seconds or so. Any value of period >= the smallest snapshot
     *               time will behave identically as if period is set to 0.
     */
    void addMetricUpdateHook(UpdateHook&);

    /** Remove a metric update hook so it won't get any more updates. */
    void removeMetricUpdateHook(UpdateHook&);

    /**
     * Force a metric update for all update hooks. Useful if you want to ensure
     * nice values before reporting something.
     * This function can not be called from an update hook callback.
     */
    void updateMetrics();

    /**
     * Force event logging to happen now.
     * This function can not be called from an update hook callback.
     */
    void forceEventLogging();

    /**
     * Register a new metric to be included in the active metric set. You need
     * to have grabbed the metric lock in order to do this. (You also need to
     * grab that lock if you alter registration of already registered metric
     * set.) This function can not be called from an update hook callback.
     */
    void registerMetric(const MetricLockGuard& l, Metric& m) {
        assertMetricLockLocked(l);
        _activeMetrics.getMetrics().registerMetric(m);
    }

    /**
     * Unregister a metric from the active metric set. You need to have grabbed
     * the metric lock in order to do this. (You also need to grab that lock
     * if you alter registration of already registered metric set.)
     * This function can not be called from an update hook callback.
     */
    void unregisterMetric(const MetricLockGuard& l, Metric& m) {
        assertMetricLockLocked(l);
        _activeMetrics.getMetrics().unregisterMetric(m);
    }

    /**
     * Reset all metrics including all snapshots.
     * This function can not be called from an update hook callback.
     */
    void reset(system_time currentTime);

    /**
     * Read configuration. Before reading config, all metrics should be set
     * up first. By doing this, the metrics manager can optimize reporting
     * of consumers. readConfig() will start a config subscription. It should
     * not be called multiple times.
     */
    void init(const config::ConfigUri & uri, bool startThread);
    void init(const config::ConfigUri & uri) {
        init(uri, true);
    }

    /**
     * Visit a given snapshot for a given consumer. (Empty consumer name means
     * all metrics). This function can be used for various printing by using
     * various writer visitors in the metrics module, or your own.
     */
    void visit(const MetricLockGuard & guard, const MetricSnapshot&,
               MetricVisitor&, const std::string& consumer) const;

    /**
     * The metric lock protects against changes in metric structure. After
     * metric manager init, you need to take this lock if you want to add or
     * remove metrics from registered metric sets, to avoid that happening at
     * the same time as metrics are being visited. Also, when accessing
     * snapshots, you need to have this lock to prevent metric manager to alter
     * snapshots while you are accessing them.
     */
    MetricLockGuard getMetricLock() const {
        return MetricLockGuard(_waiter);
    }

    /** While accessing the active metrics you should have the metric lock. */
    MetricSnapshot& getActiveMetrics(const MetricLockGuard& l) {
        assertMetricLockLocked(l);
        return _activeMetrics;
    }
    const MetricSnapshot& getActiveMetrics(const MetricLockGuard& l) const {
        assertMetricLockLocked(l);
        return _activeMetrics;
    }

    /** While accessing the total metrics you should have the metric lock. */
    const MetricSnapshot& getTotalMetricSnapshot(const MetricLockGuard& l) const {
        assertMetricLockLocked(l);
        return *_totalMetrics;
    }
    /** While accessing snapshots you should have the metric lock. */
    const MetricSnapshot& getMetricSnapshot( const MetricLockGuard& guard, vespalib::duration period) const {
        return getMetricSnapshot(guard, period, false);
    }
    const MetricSnapshot& getMetricSnapshot( const MetricLockGuard&, vespalib::duration period, bool getInProgressSet) const;
    const MetricSnapshotSet& getMetricSnapshotSet(const MetricLockGuard&, vespalib::duration period) const;

    std::vector<time_point::duration> getSnapshotPeriods(const MetricLockGuard& l) const;

    // Public only for testing. The returned pointer is only valid while holding the lock.
    const ConsumerSpec * getConsumerSpec(const MetricLockGuard & guard, const Metric::String& consumer) const;

    /**
     * If you add or remove metrics from the active metric sets, normally,
     * snapshots will be recreated next snapshot period. However, if you want
     * to see the effects of such changes in status pages ahead of that, you
     * can call this function in order to check whether snapshots needs to be
     * regenerated and regenerate them if needed.
     */
    void checkMetricsAltered(const MetricLockGuard &);

    /** Used by unit tests to verify that we have processed for a given time. */
    time_point getLastProcessedTime() const { return _lastProcessedTime.load(std::memory_order_relaxed); }

    /** Used by unit tests to wake waiters after altering time. */
    void timeChangedNotification() const;

    MemoryConsumption::UP getMemoryConsumption(const MetricLockGuard & guard) const;

    bool isInitialized() const;

private:
    void takeSnapshots(const MetricLockGuard &, system_time timeToProcess);

    friend struct MetricManagerTest;
    friend struct SnapshotTest;

    void configure(const MetricLockGuard & guard, std::unique_ptr<MetricsmanagerConfig> conf);
    void run();
    time_point tick(const MetricLockGuard & guard, time_point currentTime);
    /**
     * Utility function for updating periodic metrics.
     *
     * @param updateTime Update metrics timed to update at this time.
     * @param outOfSchedule Force calls to all hooks. Don't screw up normal
     *                      schedule though. If not time to update yet, update
     *                      without adjusting schedule for next update.
     * @return Time of next hook to be called in the future.
     */
    time_point updatePeriodicMetrics(const MetricLockGuard & guard, time_point updateTime, bool outOfSchedule);
    void updateSnapshotMetrics(const MetricLockGuard & guard);

    void handleMetricsAltered(const MetricLockGuard & guard);

    using SnapSpec = std::pair<time_point::duration, std::string>;
    static std::vector<SnapSpec> createSnapshotPeriods( const MetricsmanagerConfig& config);
    void assertMetricLockLocked(const MetricLockGuard& g) const;
};

} // metrics

