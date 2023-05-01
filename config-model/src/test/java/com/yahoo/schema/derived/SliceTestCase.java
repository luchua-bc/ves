// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.schema.derived;

import com.yahoo.component.ComponentId;
import com.yahoo.schema.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author bratseth
 */
public class SliceTestCase extends AbstractExportingTestCase {

    @Test
    void testSlice() throws IOException, ParseException {
        ComponentId.resetGlobalCountersForTests();
        DerivedConfiguration c = assertCorrectDeriving("slice");
    }

}
