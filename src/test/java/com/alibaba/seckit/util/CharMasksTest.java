package com.alibaba.seckit.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharMasksTest {

    @Test
    public void testConfiguredCharactersAndDuplicates() {
        CharMasks masks = new CharMasks(128, "aab\0\n\u007f");
        assertTrue(masks.contains('a'));
        assertTrue(masks.contains('b'));
        assertTrue(masks.contains('\0'));
        assertTrue(masks.contains('\n'));
        assertTrue(masks.contains('\u007f'));
        assertFalse(masks.contains('c'));
        assertFalse(masks.contains('\u0080'));
        assertFalse(masks.contains('\uffff'));
    }

    @Test
    public void testOutOfRangeCharactersAreIgnored() {
        CharMasks masks = new CharMasks(128, "a\u0080\u4e2d\uffff");
        assertTrue(masks.contains('a'));
        assertFalse(masks.contains('\u0080'));
        assertFalse(masks.contains('\u4e2d'));
        assertFalse(masks.contains('\uffff'));
    }

    @Test
    public void testEmptyAndZeroSizedMasks() {
        assertFalse(new CharMasks(128, "").contains('a'));
        CharMasks masks = new CharMasks(0, "a\0");
        assertFalse(masks.contains('a'));
        assertFalse(masks.contains('\0'));
    }

    @Test
    public void testInstancesAreIndependentAndMembershipIsStable() {
        CharMasks first = new CharMasks(128, "a");
        CharMasks second = new CharMasks(128, "b");
        for (int i = 0; i < 3; i++) {
            assertTrue(first.contains('a'));
            assertFalse(first.contains('b'));
            assertTrue(second.contains('b'));
            assertFalse(second.contains('a'));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeSizeIsRejected() {
        new CharMasks(-1, "a");
    }
}
