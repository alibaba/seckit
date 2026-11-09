package com.alibaba.seckit.util;

public final class CharMasks {

    private final boolean[] masks;

    public CharMasks(int size, String characters) {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
        masks = new boolean[size];
        for (int i = 0; i < characters.length(); i++) {
            char c = characters.charAt(i);
            if (c < size) {
                masks[c] = true;
            }
        }
    }

    public boolean contains(char c) {
        return c < masks.length && masks[c];
    }
}
