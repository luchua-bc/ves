// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.tensor;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author bratseth
 */
public class IndexedTensorTestCase {

    private final int vSize = 1;
    private final int wSize = 2;
    private final int xSize = 3;
    private final int ySize = 4;
    private final int zSize = 5;

    @Test
    public void testEmpty() {
        Tensor empty = Tensor.Builder.of(TensorType.empty).build();
        assertEquals(1, empty.size());
        assertEquals(0.0, empty.valueIterator().next(), 0.00000001);
        Tensor emptyFromString = Tensor.from(TensorType.empty, "{}");
        assertEquals(empty, emptyFromString);
    }

    @Test
    public void testSingleValue() {
        Tensor singleValue = Tensor.Builder.of(TensorType.empty).cell(TensorAddress.of(), 3.5).build();
        assertTrue(singleValue instanceof IndexedTensor);
        assertEquals("tensor():{3.5}", singleValue.toString());
        Tensor singleValueFromString = Tensor.from(TensorType.empty, "{3.5}");
        assertEquals("tensor():{3.5}", singleValueFromString.toString());
        assertTrue(singleValueFromString instanceof IndexedTensor);
        assertEquals(singleValue, singleValueFromString);
    }

    @Test
    public void testNegativeLabels() {
        TensorAddress numeric = TensorAddress.of(-1, 0, 1, 1234567, -1234567);
        assertEquals("-1", numeric.label(0));
        assertEquals("0", numeric.label(1));
        assertEquals("1", numeric.label(2));
        assertEquals("1234567", numeric.label(3));
        assertEquals("-1234567", numeric.label(4));
    }

    private void verifyFloat(String spec) {
        float [] floats = {1.0f, 2.0f, 3.0f};
        Tensor tensor = IndexedTensor.Builder.of(TensorType.fromSpec(spec), floats).build();
        int index = 0;
        for (Double cell : tensor.cells().values()) {
            assertEquals(cell, Double.valueOf(floats[index++]));
        }
    }
    private void verifyDouble(String spec) {
        double [] values = {1.0, 2.0, 3.0};
        Tensor tensor = IndexedTensor.Builder.of(TensorType.fromSpec(spec), values).build();
        int index = 0;
        for (Double cell : tensor.cells().values()) {
            assertEquals(cell, Double.valueOf(values[index++]));
        }
    }

    @Test
    public void testBoundHandoverBuilding() {
        verifyFloat("tensor<float>(x[3])");
        verifyDouble("tensor<float>(x[3])");
        verifyFloat("tensor<double>(x[3])");
        verifyDouble("tensor<double>(x[3])");
        try {
            verifyDouble("tensor<double>(x[4])");
            fail("Expect IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid size(3) of supplied value vector. Type specifies that size should be 4", e.getMessage());
        }
    }
    
    @Test
    public void testBoundBuilding() {
        TensorType type = new TensorType.Builder().indexed("v", vSize)
                                                  .indexed("w", wSize)
                                                  .indexed("x", xSize)
                                                  .indexed("y", ySize)
                                                  .indexed("z", zSize)
                                                  .build();
        assertBuildingVWXYZ(type);
    }

    @Test
    public void testUnboundBuilding() {
        TensorType type = new TensorType.Builder().indexed("w")
                                                  .indexed("v")
                                                  .indexed("x")
                                                  .indexed("y")
                                                  .indexed("z").build();
        assertBuildingVWXYZ(type);
    }
    
    @Test
    public void testUnderspecifiedBuilding() {
        TensorType type = new TensorType.Builder().indexed("x").build();
        Tensor.Builder builder = Tensor.Builder.of(type);
        builder.cell(47.0, 98);
        Tensor tensor = builder.build();
        assertEquals(47.0, tensor.sum(Collections.singletonList("x")).asDouble(), 0.000001);
    }
    
    private void assertBuildingVWXYZ(TensorType type) {
        IndexedTensor.Builder builder = IndexedTensor.Builder.of(type);
        // Build in scrambled order
        for (int v = 0; v < vSize; v++)
            for (int w = 0; w < wSize; w++)
                for (int y = 0; y < ySize; y++)
                    for (int x = xSize - 1; x >= 0; x--)
                        for (int z = 0; z < zSize; z++)
                            builder.cell(value(v, w, x, y, z), v, w, x, y, z);

        IndexedTensor tensor = builder.build();

        // Lookup by index arguments
        for (int v = 0; v < vSize; v++)
            for (int w = 0; w < wSize; w++)
                for (int y = 0; y < ySize; y++)
                    for (int x = xSize - 1; x >= 0; x--)
                        for (int z = 0; z < zSize; z++)
                            assertEquals(value(v, w, x, y, z), (int) tensor.get(v, w, x, y, z));


        // Lookup by TensorAddress argument
        for (int v = 0; v < vSize; v++)
            for (int w = 0; w < wSize; w++)
                for (int y = 0; y < ySize; y++)
                    for (int x = xSize - 1; x >= 0; x--)
                        for (int z = 0; z < zSize; z++)
                            assertEquals(value(v, w, x, y, z), (int) tensor.get(TensorAddress.of(v, w, x, y, z)));
        
        // Lookup from cells
        Map<TensorAddress, Double> cells = tensor.cells();
        assertEquals(tensor.size(), cells.size());
        for (int v = 0; v < vSize; v++)
            for (int w = 0; w < wSize; w++)
                for (int y = 0; y < ySize; y++)
                    for (int x = xSize - 1; x >= 0; x--)
                        for (int z = 0; z < zSize; z++)
                            assertEquals(value(v, w, x, y, z), cells.get(TensorAddress.of(v, w, x, y, z)).intValue());

        // Lookup from iterator
        Map<TensorAddress, Double> cellsOfIterator = new HashMap<>();
        for (Iterator<Tensor.Cell> i = tensor.cellIterator(); i.hasNext(); ) {
            Tensor.Cell cell = i.next();
            cellsOfIterator.put(cell.getKey(), cell.getValue());
            assertEquals(cell.getValue(), cell.getDoubleValue(), 0.00001);
            assertEquals(cell.getValue(), cell.getFloatValue(), 0.00001);
        }
        assertEquals(tensor.size(), cellsOfIterator.size());
        for (int v = 0; v < vSize; v++)
            for (int w = 0; w < wSize; w++)
                for (int y = 0; y < ySize; y++)
                    for (int x = xSize - 1; x >= 0; x--)
                        for (int z = 0; z < zSize; z++)
                            assertEquals(value(v, w, x, y, z), cellsOfIterator.get(TensorAddress.of(v, w, x, y, z)).intValue());

    }

    /** Returns a unique value for some given cell indexes */
    private int value(int v, int w, int x, int y, int z) {
        return v + 3 * w + 7 * x + 11 * y + 13 * z;
    }
    
}
