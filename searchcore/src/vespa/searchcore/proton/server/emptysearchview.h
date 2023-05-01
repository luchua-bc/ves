// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include <vespa/searchcore/proton/summaryengine/isearchhandler.h>

namespace proton {

class EmptySearchView : public ISearchHandler
{
public:
    using SP = std::shared_ptr<EmptySearchView>;

    EmptySearchView();

    std::unique_ptr<DocsumReply> getDocsums(const DocsumRequest & req) override;

    std::unique_ptr<SearchReply>
    match(const SearchRequest &req, vespalib::ThreadBundle &threadBundle) const override;
};

} // namespace proton

