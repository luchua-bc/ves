// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.schema.processing;

import com.yahoo.config.application.api.DeployLogger;
import com.yahoo.document.CollectionDataType;
import com.yahoo.document.TensorDataType;
import com.yahoo.schema.RankProfileRegistry;
import com.yahoo.schema.Schema;
import com.yahoo.schema.document.HnswIndexParams;
import com.yahoo.schema.document.ImmutableSDField;
import com.yahoo.schema.document.SDField;
import com.yahoo.tensor.TensorType;
import com.yahoo.vespa.model.container.search.QueryProfiles;

/**
 * Class that processes and validates tensor fields.
 *
 * @author geirst
 */
public class TensorFieldProcessor extends Processor {

    public TensorFieldProcessor(Schema schema, DeployLogger deployLogger, RankProfileRegistry rankProfileRegistry, QueryProfiles queryProfiles) {
        super(schema, deployLogger, rankProfileRegistry, queryProfiles);
    }

    @Override
    public void process(boolean validate, boolean documentsOnly) {
        for (var field : schema.allConcreteFields()) {
            if ( field.getDataType() instanceof TensorDataType ) {
                if (validate) {
                    validateIndexingScripsForTensorField(field);
                    validateAttributeSettingForTensorField(field);
                    validateHnswIndexParametersRequiresIndexing(field);
                }
                processIndexSettingsForTensorField(field, validate);
            }
            else if (field.getDataType() instanceof CollectionDataType){
                if (validate) {
                    validateDataTypeForCollectionField(field);
                }
            }
        }
    }

    private void validateIndexingScripsForTensorField(SDField field) {
        if (field.doesIndexing() && !isTensorTypeThatSupportsHnswIndex(field)) {
            fail(schema, field, "A tensor of type '" + tensorTypeToString(field) + "' does not support having an 'index'. " +
                    "Currently, only tensors with 1 indexed dimension or 1 mapped + 1 indexed dimension support that.");
        }
    }

    private boolean isTensorTypeThatSupportsHnswIndex(ImmutableSDField field) {
        var type = ((TensorDataType)field.getDataType()).getTensorType();
        return isTensorTypeThatSupportsHnswIndex(type);
    }

    /**
     * Returns whether the given tensor type supports using HNSW index and
     * nearest neighbor search.
     */
    public static boolean isTensorTypeThatSupportsHnswIndex(TensorType type) {
        // Tensors with 1 indexed dimension support hnsw index (used for approximate nearest neighbor search).
        if ((type.dimensions().size() == 1) &&
                type.dimensions().get(0).isIndexed()) {
            return true;
        }
        // Tensors with 1 mapped + 1 indexed dimension support hnsw index (aka multiple vectors per document).
        if (type.dimensions().size() == 2) {
            var a = type.dimensions().get(0);
            var b = type.dimensions().get(1);
            if ((a.isMapped() && b.isIndexed()) ||
                    (a.isIndexed() && b.isMapped())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTensorTypeThatSupportsDirectStore(ImmutableSDField field) {
        var type = ((TensorDataType)field.getDataType()).getTensorType();
        // Tensors with at least one mapped/sparse dimensions can be "direct"
        // (currenty triggered by fast-search flag)
        for (var dim : type.dimensions()) {
            if (dim.isMapped()) {
                return true;
            }
        }
        return false;
    }

    private String tensorTypeToString(ImmutableSDField field) {
        return ((TensorDataType)field.getDataType()).getTensorType().toString();
    }

    private void validateAttributeSettingForTensorField(SDField field) {
        if (field.doesAttributing()) {
            var attribute = field.getAttributes().get(field.getName());
            if (attribute != null && attribute.isFastSearch()) {
                if (! isTensorTypeThatSupportsDirectStore(field)) {
                    fail(schema, field, "An attribute of type 'tensor' cannot be 'fast-search'.");
                }
            }
        }
    }

    private void validateHnswIndexParametersRequiresIndexing(SDField field) {
        var index = field.getIndex(field.getName());
        if (index != null && index.getHnswIndexParams().isPresent() && !field.doesIndexing()) {
            fail(schema, field, "A tensor that specifies hnsw index parameters must also specify 'index' in 'indexing'");
        }
    }

    private void processIndexSettingsForTensorField(SDField field, boolean validate) {
        if (!field.doesIndexing()) {
            return;
        }
        if (isTensorTypeThatSupportsHnswIndex(field)) {
            if (validate && !field.doesAttributing()) {
                fail(schema, field, "A tensor that has an index must also be an attribute.");
            }
            var index = field.getIndex(field.getName());
            // TODO: Calculate default params based on tensor dimension size
            var params = new HnswIndexParams();
            if (index != null) {
                params = params.overrideFrom(index.getHnswIndexParams());
            }
            field.getAttribute().setHnswIndexParams(params);
        }
    }

    private void validateDataTypeForCollectionField(SDField field) {
        if (((CollectionDataType)field.getDataType()).getNestedType() instanceof TensorDataType)
            fail(schema, field, "A field with collection type of tensor is not supported. Use simple type 'tensor' instead.");
    }

}
