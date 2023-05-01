// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#include <vespa/config/helper/configfetcher.h>
#include <vespa/config/subscription/configuri.h>
#include <vespa/config-stateserver.h>

namespace vespalib {
    struct HealthProducer;
    struct MetricsProducer;
    struct ComponentConfigProducer;
    class StateServer;
}
namespace slobrok {

class ReconfigurableStateServer : private config::IFetcherCallback<vespa::config::core::StateserverConfig> {
public:
    ReconfigurableStateServer(const config::ConfigUri & configUri,
                              vespalib::HealthProducer & healt,
                              vespalib::MetricsProducer & metrics,
                              vespalib::ComponentConfigProducer & component);
    ~ReconfigurableStateServer();
private:
    void configure(std::unique_ptr<vespa::config::core::StateserverConfig> config) override;
    vespalib::HealthProducer               & _health;
    vespalib::MetricsProducer              & _metrics;
    vespalib::ComponentConfigProducer      & _components;
    std::unique_ptr<config::ConfigFetcher>   _configFetcher;
    std::unique_ptr<vespalib::StateServer>   _server;
};

}
