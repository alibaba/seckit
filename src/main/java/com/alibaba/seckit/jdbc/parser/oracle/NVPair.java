package com.alibaba.seckit.jdbc.parser.oracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A descriptor entry containing either an atom or ordered, possibly repeated children. */
public final class NVPair {
    public static final int RHS_ATOM = 1;
    private static final int RHS_LIST = 2;

    private final String name;
    private final String atom;
    private final String serializedAtom;
    private final List<NVPair> children;

    NVPair(String name, String atom, String serializedAtom) {
        this.name = name;
        this.atom = atom;
        this.serializedAtom = serializedAtom;
        this.children = Collections.emptyList();
    }

    NVPair(String name, List<NVPair> children) {
        this.name = name;
        this.atom = null;
        this.serializedAtom = null;
        this.children = new ArrayList<>(children);
    }

    public String getName() {
        return name;
    }

    public int getRHSType() {
        return atom == null ? RHS_LIST : RHS_ATOM;
    }

    /** Returns the decoded atom, or null for a node with children. */
    public String getAtom() {
        return atom;
    }

    public int getListSize() {
        return children.size();
    }

    public NVPair getListElement(int index) {
        return children.get(index);
    }

    public void removeListElement(int index) {
        children.remove(index);
    }

    @Override
    public String toString() {
        StringBuilder descriptor = new StringBuilder();
        appendTo(descriptor);
        return descriptor.toString();
    }

    private void appendTo(StringBuilder descriptor) {
        descriptor.append('(').append(name).append('=');
        if (atom != null) {
            // Keep the original quote delimiters and escapes; decoded text may contain syntax.
            descriptor.append(serializedAtom);
        } else {
            for (NVPair child : children) {
                child.appendTo(descriptor);
            }
        }
        descriptor.append(')');
    }
}
