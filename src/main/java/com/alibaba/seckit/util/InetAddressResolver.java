package com.alibaba.seckit.util;

import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author renyi.cry
  */
public class InetAddressResolver {

    private static final String HOLDER_FIELD_NAME = "holder";

    private static final String HOLDER_CLASS = "java.net.InetAddress$InetAddressHolder";

    private static final String HOLDER_HOSTNAME_FIELD_NAME = "hostName";

    private static final Field HOLDER_FIELD;

    private static final Field HOST_NAME_FIELD;

    static {
        HOLDER_FIELD =  ReflectionUtil.findField(InetAddress.class, HOLDER_FIELD_NAME);
        HOLDER_FIELD.setAccessible(true);
        Class<?> holderClass = ClassUtil.classForNameQuietly(HOLDER_CLASS, InetAddress.class.getClassLoader());
        HOST_NAME_FIELD = ReflectionUtil.findField(holderClass, HOLDER_HOSTNAME_FIELD_NAME);
        HOST_NAME_FIELD.setAccessible(true);
    }

    public static String findInetAddressHoldingHostName(InetAddress inetAddress) {
        if (inetAddress == null) {
            return null;
        }

        return ReflectionUtil.getNestedFieldValue(inetAddress, HOLDER_FIELD, HOST_NAME_FIELD);
    }

    public static String resolve(InetAddress inetAddress) {
        String hostName = findInetAddressHoldingHostName(inetAddress);

        return hostName == null ? inetAddress.getHostAddress() : hostName;
    }

    public static List<String> resolves(InetAddress inetAddress) {
        List<String> hostnames = new ArrayList<>();
        hostnames.add(resolve(inetAddress));

        if (inetAddress instanceof Inet4Address) {
            byte[] address = inetAddress.getAddress();
            hostnames.add(IpFormatter.format8(address));
            hostnames.add(IpFormatter.format10(address));
            hostnames.add(IpFormatter.format16(address));
            hostnames.add(IpFormatter.format8Ddn(address));
            hostnames.add(IpFormatter.format10Ddn(address));
            hostnames.add(IpFormatter.format16Ddn(address));
        }

        return hostnames;
    }

}
