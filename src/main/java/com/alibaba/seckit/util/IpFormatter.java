package com.alibaba.seckit.util;

/**
 * IpFormatter
 *
 * @author jianbai
 */
public class IpFormatter {

    private static final String PREFIX_8 = "0";
    private static final String PREFIX_16 = "0x";
    private static final String IP_DELIMITER = ".";

    public static String format8(byte[] ip) {
        return PREFIX_8 + Long.toOctalString(format10Actually(ip));
    }

    public static String format10(byte[] ip) {
        return String.valueOf(format10Actually(ip));
    }

    public static String format16(byte[] ip) {
        return PREFIX_16 + Long.toHexString(format10Actually(ip)).toLowerCase();
    }

    private static long format10Actually(byte[] ip) {
        long format10 = 0;
        format10 += (((long)ip[0]) & 0xff) << 24;
        format10 += (((long)ip[1]) & 0xff) << 16;
        format10 += (((long)ip[2]) & 0xff) << 8;
        format10 += ((long)ip[3]) & 0xff;

        return format10;
    }

    public static String format8Ddn(byte[] ip) {
        String format8 = "";
        format8 += PREFIX_8 + Integer.toOctalString((((int)ip[0]) & 0xff)) + IP_DELIMITER;
        format8 += PREFIX_8 + Integer.toOctalString((((int)ip[1]) & 0xff)) + IP_DELIMITER;
        format8 += PREFIX_8 + Integer.toOctalString((((int)ip[2]) & 0xff)) + IP_DELIMITER;
        format8 += PREFIX_8 + Integer.toOctalString((((int)ip[3]) & 0xff));

        return format8;
    }

    public static String format10Ddn(byte[] ip) {
        String format10 = "";
        format10 += ((((long)ip[0]) & 0xff)) + IP_DELIMITER;
        format10 += ((((long)ip[1]) & 0xff)) + IP_DELIMITER;
        format10 += ((((long)ip[2]) & 0xff)) + IP_DELIMITER;
        format10 += ((long)ip[3]) & 0xff;

        return format10;
    }

    public static String format16Ddn(byte[] ip) {
        String format16 = "";
        format16 += PREFIX_16 + Integer.toHexString((((int)ip[0]) & 0xff)).toLowerCase() + IP_DELIMITER;
        format16 += PREFIX_16 + Integer.toHexString((((int)ip[1]) & 0xff)).toLowerCase() + IP_DELIMITER;
        format16 += PREFIX_16 + Integer.toHexString((((int)ip[2]) & 0xff)).toLowerCase() + IP_DELIMITER;
        format16 += PREFIX_16 + Integer.toHexString((((int)ip[3]) & 0xff)).toLowerCase();

        return format16;
    }

    /**
     * Java环境下当为点分形式IP时, 当其字符串小于等于15位, 将直接视为点分十进制来处理, 及时可能为其他情况如: 点分八进制、点分十六进制
     * @param ip 待格式化的ip
     * @return 格式化后的ip
     */
    public static String formatJavaIp(String ip) {
        if (ip.length() > 15) {
            return ip;
        }

        String[] ipParts = ip.split("\\.");
        if (ipParts.length != 4) {
            return ip;
        }

        if (!StringUtil.isDigits(ipParts[0]) || !StringUtil.isDigits(ipParts[1])
                                              || !StringUtil.isDigits(ipParts[2])
                                              || !StringUtil.isDigits(ipParts[3])) {
            return ip;
        }

        String formatJavaIp = "";
        formatJavaIp += Integer.valueOf(ipParts[0]) + IP_DELIMITER;
        formatJavaIp += Integer.valueOf(ipParts[1]) + IP_DELIMITER;
        formatJavaIp += Integer.valueOf(ipParts[2]) + IP_DELIMITER;
        formatJavaIp += Integer.valueOf(ipParts[3]);
        return formatJavaIp;
    }
}
