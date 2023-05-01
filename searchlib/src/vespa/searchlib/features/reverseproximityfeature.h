// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include <vespa/searchlib/fef/blueprint.h>

namespace search::features {

/**
 * Implements the necessary config for reverse proximity.
 */
struct ReverseProximityConfig {
    ReverseProximityConfig();

    uint32_t fieldId;    // The id of field to process.
    uint32_t termA;      // The id of the first query term in the pair (a, b).
    uint32_t termB;      // The id of the second query term.
};

/**
 * Implements the executor for reverse proximity.
 */
class ReverseProximityExecutor : public fef::FeatureExecutor {
public:
    /**
     * Constructs an executor for reverse proximity.
     *
     * @param env    The query environment.
     * @param config The completeness config.
     */
    ReverseProximityExecutor(const fef::IQueryEnvironment &env,
                             const ReverseProximityConfig &config);
    void execute(uint32_t docId) override;

private:
    const ReverseProximityConfig &_config; // The proximity config.
    fef::TermFieldHandle          _termA;  // Handle to the first query term.
    fef::TermFieldHandle          _termB;  // Handle to the second query term.
    const fef::MatchData         *_md;

    void handle_bind_match_data(const fef::MatchData &md) override;
};

/**
 * Implements the blueprint for proximity.
 */
class ReverseProximityBlueprint : public fef::Blueprint {
public:
    ReverseProximityBlueprint();
    void visitDumpFeatures(const fef::IIndexEnvironment &env, fef::IDumpFeatureVisitor &visitor) const override;
    fef::Blueprint::UP createInstance() const override;
    fef::ParameterDescriptions getDescriptions() const override {
        return fef::ParameterDescriptions().desc().indexField(fef::ParameterCollection::ANY).number().number();
    }
    bool setup(const fef::IIndexEnvironment & env, const fef::ParameterList & params) override;
    fef::FeatureExecutor &createExecutor(const fef::IQueryEnvironment &env, vespalib::Stash &stash) const override;
private:
    ReverseProximityConfig _config;
};

}
