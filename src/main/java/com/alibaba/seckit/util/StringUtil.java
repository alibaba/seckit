package com.alibaba.seckit.util;

/**
 * @author mingyi
  */
public class StringUtil {
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    public static boolean isDigits(String str) {
        if (StringUtil.isEmpty(str)) {
            return false;
        } else {
            for(int i = 0; i < str.length(); ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }
}
