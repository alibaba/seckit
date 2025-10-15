package com.alibaba.seckit.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author mingyi
  */
public class EscapeStringEncoderTest {

    @Test
    public void testDefaultEncodeAndDecode() {
        EscapeStringEncoder escapeStringEncoder = new EscapeStringEncoder();
        
        String original = "hello%world+test";
        String encoded = escapeStringEncoder.encode(original);
        assertEquals("hello%00world%01test", encoded);
        
        String decoded = escapeStringEncoder.decode(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    public void testEncodeAndDecodeWithAllSpecialChars() {
        EscapeStringEncoder escapeStringEncoder = new EscapeStringEncoder();
        
        String original = "%+ &?=";
        String encoded = escapeStringEncoder.encode(original);
        assertEquals("%00%01%02%03%04%05", encoded);
        
        String decoded = escapeStringEncoder.decode(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    public void testEncodeAndDecodeWithNoSpecialChars() {
        EscapeStringEncoder escapeStringEncoder = new EscapeStringEncoder();
        
        String original = "hello world";
        String encoded = escapeStringEncoder.encode(original);
        assertEquals("hello%02world", encoded);
        
        String decoded = escapeStringEncoder.decode(encoded);
        assertEquals(original, decoded);
    }
    
    @Test
    public void testEncodeAndDecodeEmptyAndNull() {
        EscapeStringEncoder escapeStringEncoder = new EscapeStringEncoder();
        
        String encoded = escapeStringEncoder.encode("");
        assertEquals("", encoded);
        
        String decoded = escapeStringEncoder.decode("");
        assertEquals("", decoded);
        
        assertNull(escapeStringEncoder.encode(null));
        assertNull(escapeStringEncoder.decode(null));
    }
    
    @Test
    public void testCustomEscapeString() {
        char escapeChar = '#';
        char[] encodeChars = {'@', '#', '$'};
        char[] availableChars = {'a', 'b', 'c'};
        
        EscapeStringEncoder escapeStringEncoder = new EscapeStringEncoder(escapeChar, encodeChars, availableChars);
        
        String original = "test@string#with$special@chars";
        String encoded = escapeStringEncoder.encode(original);
        assertEquals("test#aastring#abwith#acspecial#aachars", encoded);
        
        String decoded = escapeStringEncoder.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCustomEscapeStringWithoutEncodeEscapeChar() {
        char escapeChar = '#';
        char[] encodeChars = {'@', '$'};
        char[] availableChars = {'a', 'b', 'c'};
        new EscapeStringEncoder(escapeChar, encodeChars, availableChars);
    }
    
    @Test
    public void testWithDuplicateChars() {
        char escapeChar = '%';
        char[] encodeChars = {'%', '+'};
        char[] availableChars = {'0', '1', '%'};
        
        EscapeStringEncoder escapeStringEncoder = new EscapeStringEncoder(escapeChar, encodeChars, availableChars);
        String original = "test%%with%01%%+duplicate%1%%+1chars";
        String encoded = escapeStringEncoder.encode(original);
        assertEquals("test%00%00with%0001%00%00%01duplicate%001%00%00%011chars", encoded);

        String decoded = escapeStringEncoder.decode(encoded);
        assertEquals(original, decoded);
    }
}