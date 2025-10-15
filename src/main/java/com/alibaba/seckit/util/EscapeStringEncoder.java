package com.alibaba.seckit.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * @author mingyi
  */
public class EscapeStringEncoder {
    private char escapeChar = '%';
    private char[] encodeChars = new char[]{'%', '+', ' ', '&', '?', '=', ';', ':', '*', '[', ']', '{', '}', '(', ')', '|', '\\', '/', '\'', '"', '<', '>', '^', '`', '~', ','};
    private char[] availableChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private final BiMap<Character, String> encodeMap = HashBiMap.create();
    
    EscapeStringEncoder() {
        initMapping();
    }

    EscapeStringEncoder(char escapeChar, char[] encodeChars, char[] availableChars) {
        this.escapeChar = escapeChar;
        this.encodeChars = encodeChars;
        this.availableChars = availableChars;
        checkEscapeChar();
        initMapping();
    }

    public String encode(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (encodeMap.containsKey(c)) {
                sb.append(encodeMap.get(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String decode(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == escapeChar && i + 2 < str.length()) {
                String code = str.substring(i, i + 3);
                if (encodeMap.inverse().containsKey(code)) {
                    sb.append(encodeMap.inverse().get(code));
                    i += 2;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }
    
    private void checkEscapeChar() {
        for (char encodeChar : encodeChars) {
            if (encodeChar == escapeChar) {
                return;
            }
        }
        throw new IllegalArgumentException("escapeChar must be included in encodeChars");
    }
    private void initMapping() {
        int maxCount = availableChars.length * availableChars.length;
        
        if (encodeChars.length > maxCount) {
            throw new IllegalArgumentException("encodeChars count exceeds available combinations");
        }

        for (int i = 0; i < encodeChars.length; i++) {
            char encodeChar = encodeChars[i];
            int firstIndex = i / availableChars.length;
            int secondIndex = i % availableChars.length;
            
            String encoded = String.valueOf(escapeChar) + availableChars[firstIndex] + availableChars[secondIndex];
            
            encodeMap.put(encodeChar, encoded);
        }
    }
}
