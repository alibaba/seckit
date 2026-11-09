package com.alibaba.seckit.cli;

public class CLIToolImpl implements CLITool {

    @Override
    public String filter(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9') || c == '_' || c == '-'
                    || c == ',' || c == '.' || (c >= '\u4e00' && c <= '\u9fbb')) {
                result.append(c);
            }
        }
        return result.toString();
    }
}
