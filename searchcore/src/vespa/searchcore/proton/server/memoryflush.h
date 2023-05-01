// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include <vespa/searchcore/proton/flushengine/iflushstrategy.h>
#include <vespa/vespalib/util/time.h>
#include <mutex>

namespace proton {

class MemoryFlush : public IFlushStrategy
{
public:
    struct Config
    {
        /// Global maxMemory
        uint64_t          maxGlobalMemory;
        /// Maximum global tls size.
        uint64_t          maxGlobalTlsSize;
        /// Maximum global disk bloat factor. When this limit is reached
        /// flush is forced.
        double            globalDiskBloatFactor;
        /// Maximum memory saved. When this limit is reached flush is forced.
        int64_t           maxMemoryGain;
        /// Maximum disk bloat factor. When this limit is reached
        /// flush is forced.
        double            diskBloatFactor;

        /// Maximum age of unflushed data.
        vespalib::duration maxTimeGain;
        Config();
        Config(uint64_t maxGlobalMemory_in,
               uint64_t maxGlobalTlsSize_in,
               double globalDiskBloatFactor_in,
               uint64_t maxMemoryGain_in,
               double diskBloatFactor_in,
               vespalib::duration maxTimeGain_in);
        bool operator == (const Config & rhs) const { return equal(rhs); }
        bool operator != (const Config & rhs) const { return ! equal(rhs); }
        bool equal(const Config & rhs) const {
            return (maxGlobalMemory == rhs.maxGlobalMemory) &&
                   (maxGlobalTlsSize == rhs.maxGlobalTlsSize) &&
                   (globalDiskBloatFactor == rhs.globalDiskBloatFactor) &&
                   (maxMemoryGain == rhs.maxMemoryGain) &&
                   (maxTimeGain == rhs.maxTimeGain) &&
                   (diskBloatFactor == rhs. diskBloatFactor);
        }
        vespalib::string toString() const;
    };

    enum OrderType { DEFAULT, MAXAGE, DISKBLOAT, TLSSIZE, MEMORY };

private:
    /// Needed as flushDone is called in different context from the rest
    mutable std::mutex _lock;
    Config             _config;
    /// The time when the strategy was started.
    vespalib::system_time  _startTime;

    class CompareTarget
    {
    public:
        CompareTarget(OrderType order, const flushengine::TlsStatsMap &tlsStatsMap)
            : _order(order),
              _tlsStatsMap(tlsStatsMap)
        { }

        bool operator ()(const FlushContext::SP &lfc, const FlushContext::SP &rfc) const;
    private:
        OrderType     _order;
        const flushengine::TlsStatsMap &_tlsStatsMap;
    };

public:
    using SP = std::shared_ptr<MemoryFlush>;

    MemoryFlush();
    explicit MemoryFlush(const Config &config) : MemoryFlush(config, vespalib::system_clock::now()) { }
    MemoryFlush(const Config &config, vespalib::system_time startTime);
    ~MemoryFlush();

    FlushContext::List
    getFlushTargets(const FlushContext::List &targetList,
                    const flushengine::TlsStatsMap &tlsStatsMap,
                    const flushengine::ActiveFlushStats& active_flushes) const override;

    void setConfig(const Config &config);
    Config getConfig() const;
};

} // namespace proton
