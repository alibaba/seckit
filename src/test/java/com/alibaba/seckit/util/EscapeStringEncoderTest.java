package com.alibaba.seckit.util;

import com.alibaba.seckit.SecurityUtil;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Test
    public void testDuplicateEncodeCharKeepsLastMapping() {
        EscapeStringEncoder escapeStringEncoder = new EscapeStringEncoder('%', new char[]{'%', '%'}, new char[]{'0', '1'});
        assertEquals("%01", escapeStringEncoder.encode("%"));
        assertEquals("%", escapeStringEncoder.decode("%01"));
        assertEquals("%00", escapeStringEncoder.decode("%00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollidingEncodedValuesAreRejected() {
        new EscapeStringEncoder('%', new char[]{'%', '+'}, new char[]{'0', '0'});
    }

    @Test
    public void testMainClassesDoNotReferenceUndeclaredGuava() throws Exception {
        // Guava is only a transitive provided dependency, so library users do not have it at runtime.
        Path classes = Paths.get(SecurityUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<Path> offenders;
        try (Stream<Path> files = Files.walk(classes)) {
            offenders = files.filter(path -> path.toString().endsWith(".class"))
                    .filter(EscapeStringEncoderTest::referencesGuava)
                    .collect(Collectors.toList());
        }
        assertTrue("main classes reference Guava: " + offenders, offenders.isEmpty());
    }

    private static boolean referencesGuava(Path classFile) {
        try {
            return new String(Files.readAllBytes(classFile), StandardCharsets.ISO_8859_1).contains("com/google/common/");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}