// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.document.annotation;

/**
 * @author <a href="mailto:einarmr@yahoo-inc.com">Einar M R Rosenvinge</a>
 */
public class IndexKeySpanNodeSpanTreeAdvTest extends SpanTreeAdvTest {

    @Override
    public void populateSpanTree() {
        super.populateSpanTree();
        tree.createIndex(SpanTree.IndexKey.SPAN_NODE);
    }
}
