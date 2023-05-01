// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include "documentretrieverbase.h"

namespace search { class IDocumentStore; }

namespace proton {

/**
 * The document retriever used by the store-only sub database.
 *
 * Handles retrieving of documents from the underlying document store.
 */
class MinimalDocumentRetriever : public DocumentRetrieverBase
{
    const std::shared_ptr<const document::DocumentTypeRepo> _repo;
    const search::IDocumentStore &_doc_store;

public:
    // meta_store and doc_store must out-live the MinimalDocumentRetriever.
    MinimalDocumentRetriever(const DocTypeName &docTypeName,
                             const std::shared_ptr<const document::DocumentTypeRepo> repo,
                             const IDocumentMetaStoreContext &meta_store,
                             const search::IDocumentStore &doc_store,
                             bool hasFields);
    ~MinimalDocumentRetriever() override;

    document::Document::UP getFullDocument(search::DocumentIdT lid) const override;
    void visitDocuments(const LidVector & lids, search::IDocumentVisitor & visitor, ReadConsistency) const override;
};
}  // namespace proton

