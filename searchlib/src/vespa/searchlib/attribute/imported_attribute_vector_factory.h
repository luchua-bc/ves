// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include <vespa/vespalib/stllike/string.h>
#include <memory>

namespace search { struct IDocumentMetaStoreContext; }

namespace search::attribute {

class BitVectorSearchCache;
class ImportedAttributeVector;
class ReadableAttributeVector;
class ReferenceAttribute;

/*
 * Factory class used to create proper imported attribute vector based
 * on target attribute basic type.
 */
class ImportedAttributeVectorFactory {
public:
    static std::shared_ptr<ImportedAttributeVector>
    create(vespalib::stringref name,
           std::shared_ptr<ReferenceAttribute> reference_attribute,
           std::shared_ptr<IDocumentMetaStoreContext> document_meta_store,
           std::shared_ptr<attribute::ReadableAttributeVector> target_attribute,
           std::shared_ptr<const IDocumentMetaStoreContext> target_document_meta_store,
           bool use_search_cache);

    static std::shared_ptr<ImportedAttributeVector>
    create(vespalib::stringref name,
           std::shared_ptr<ReferenceAttribute> reference_attribute,
           std::shared_ptr<IDocumentMetaStoreContext> document_meta_store,
           std::shared_ptr<attribute::ReadableAttributeVector> target_attribute,
           std::shared_ptr<const IDocumentMetaStoreContext> target_document_meta_store,
           std::shared_ptr<BitVectorSearchCache> search_cache);
};

}
