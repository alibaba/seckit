package com.alibaba.seckit.util;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

/**
 * @author mingyi
 * @date 2025/9/18
 */
public class IpAddressUtilForTest {
    // JDK从11开始对ip的解析变更为严格模式，不再支持类似192.168.1的模糊ip地址，在13版本开始，添加了jdk.net.allowAmbiguousIPAddressLiterals参数来重新允许解析
    // 因此如果jdk版本在11以上，并且不是13+的同时开启了jdk.net.allowAmbiguousIPAddressLiterals，某些测试就无法通过
    public static boolean isAllowAmbiguousIPAddress() {
        return SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_10) || (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_13) && Boolean.parseBoolean(
                System.getProperty("jdk.net.allowAmbiguousIPAddressLiterals", "false")));
    }
}
