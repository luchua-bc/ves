// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.model.container.xml;

import com.yahoo.text.XML;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Replaces model id references in configs by their url.
 *
 * @author lesters
 * @author bratseth
 */
public class ModelIdResolver {

    private static Map<String, String> setupProvidedModels() {
        Map<String, String> models = new HashMap<>();
        models.put("minilm-l6-v2",          "https://data.vespa.oath.cloud/onnx_models/sentence_all_MiniLM_L6_v2.onnx");
        models.put("mpnet-base-v2",         "https://data.vespa.oath.cloud/onnx_models/sentence-all-mpnet-base-v2.onnx");
        models.put("bert-base-uncased",     "https://data.vespa.oath.cloud/onnx_models/bert-base-uncased-vocab.txt");
        models.put("flan-t5-vocab",         "https://data.vespa.oath.cloud/onnx_models/flan-t5-spiece.model");
        models.put("flan-t5-small-encoder", "https://data.vespa.oath.cloud/onnx_models/flan-t5-small-encoder-model.onnx");
        models.put("flan-t5-small-decoder", "https://data.vespa.oath.cloud/onnx_models/flan-t5-small-decoder-model.onnx");
        models.put("flan-t5-base-encoder",  "https://data.vespa.oath.cloud/onnx_models/flan-t5-base-encoder-model.onnx");
        models.put("flan-t5-base-decoder",  "https://data.vespa.oath.cloud/onnx_models/flan-t5-base-decoder-model.onnx");
        models.put("flan-t5-large-encoder", "https://data.vespa.oath.cloud/onnx_models/flan-t5-large-encoder-model.onnx");
        models.put("flan-t5-large-decoder", "https://data.vespa.oath.cloud/onnx_models/flan-t5-large-decoder-model.onnx");
        return Collections.unmodifiableMap(models);
    }

    private static final Map<String, String> providedModels = setupProvidedModels();

    /**
     * Finds any config values of type 'model' below the given config element and
     * supplies the url attribute of them if a model id is specified and hosted is true
     * (regardless of whether an url is already specified).
     *
     * @param component the XML element of any component
     */
    public static void resolveModelIds(Element component, boolean hosted) {
        for (Element config : XML.getChildren(component, "config")) {
            for (Element value : XML.getChildren(config))
                transformModelValue(value, hosted);
        }
    }

    /** Expands a model config value into regular config values. */
    private static void transformModelValue(Element value, boolean hosted) {
        if ( ! value.hasAttribute("model-id")) return;

        if (hosted) {
            value.setAttribute("url", modelIdToUrl(value.getTagName(), value.getAttribute("model-id")));
            value.removeAttribute("path");
        }
        else if ( ! value.hasAttribute("url") && ! value.hasAttribute("path")) {
            throw new IllegalArgumentException(value.getTagName() + " is configured with only a 'model-id'. " +
                                               "Add a 'path' or 'url' to deploy this outside Vespa Cloud");
        }
    }

    private static String modelIdToUrl(String valueName, String modelId) {
        if ( ! providedModels.containsKey(modelId))
            throw new IllegalArgumentException("Unknown model id '" + modelId + "' on '" + valueName + "'. Available models are [" +
                                               providedModels.keySet().stream().sorted().collect(Collectors.joining(", ")) + "]");
        return providedModels.get(modelId);
    }

}
