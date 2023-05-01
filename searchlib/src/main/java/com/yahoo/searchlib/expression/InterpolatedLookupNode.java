// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.searchlib.expression;

import com.yahoo.vespa.objects.Deserializer;
import com.yahoo.vespa.objects.ObjectVisitor;
import com.yahoo.vespa.objects.Serializer;

/**
 * This function is an instruction to retrieve the value of a named attribute.
 *
 * @author arnej27959
 */
public class InterpolatedLookupNode extends UnaryFunctionNode {

    public static final int classId = registerClass(0x4000 + 39, InterpolatedLookupNode.class);
    private String attribute;

    /**
     * Constructs an empty result node.
     * <b>NOTE:</b> This instance is broken until non-optional member data is set.
     */
    public InterpolatedLookupNode() { }

    /**
     * Constructs an instance of this class with given attribute name
     * and lookup argument.
     *
     * @param attribute The attribute to retrieve.
     * @param arg Expression evaluating to the lookup argument.
     */
    public InterpolatedLookupNode(String attribute, ExpressionNode arg) {
        setAttributeName(attribute);
        addArg(arg);
    }

    /**
     * Returns the name of the attribute whose value we do lookup in.
     *
     * @return The attribute name.
     */
    public String getAttributeName() {
        return attribute;
    }

    /**
     * Sets the name of the attribute whose value we do lookup in.
     *
     * @param attribute The attribute to retrieve.
     * @return This, to allow chaining.
     */
    public InterpolatedLookupNode setAttributeName(String attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute name can not be null.");
        }
        this.attribute = attribute;
        return this;
    }

    @Override
    protected int onGetClassId() {
        return classId;
    }

    @Override
    protected void onSerialize(Serializer buf) {
        super.onSerialize(buf);
        putUtf8(buf, attribute);
    }

    @Override
    protected void onDeserialize(Deserializer buf) {
        super.onDeserialize(buf);
        attribute = getUtf8(buf);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + attribute.hashCode();
    }

    @Override
    protected boolean equalsUnaryFunction(UnaryFunctionNode obj) {
        // "arg" checked by superclass
        String otherAttr = ((InterpolatedLookupNode)obj).getAttributeName();
        return attribute.equals(otherAttr);
    }

    @Override
    public void visitMembers(ObjectVisitor visitor) {
        super.visitMembers(visitor);
        visitor.visit("attribute", attribute);
    }

}
