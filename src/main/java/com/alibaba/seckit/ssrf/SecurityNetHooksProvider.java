package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import sun.net.NetHooks;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link NetHooks.Provider} to delegate the origin one.
 *
 * @author renyi.cry
 */
@Slf4j
public class SecurityNetHooksProvider extends NetHooks.Provider {

    private NetHooks.Provider delegate;

    private static final List<NetHooksEventListener> registeredListener = new CopyOnWriteArrayList<>();

    public SecurityNetHooksProvider(Object provider) {
        delegate = (NetHooks.Provider)provider;
    }

    // auto-initiate
    static {

        try {
            setProvider(NetHooks.class.getDeclaredField("provider"));
        } catch (Exception e) {
            log.warn("Unable to install SSRF net hooks", e);
        }
    }

    public static void registerListener(NetHooksEventListener listener) {
        registeredListener.add(listener);
    }

    static void setProvider(Field field) throws Exception {

        if (field != null) {
            field.setAccessible(true);
            // Before JDK 21, remove final before reading so the cached field accessor remains writable.
            if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_20)) {
                ReflectionUtil.removeFinalModifierFromField(field);
            }
            ReflectionUtil.setFinalField(field, null, new SecurityNetHooksProvider((NetHooks.Provider)field.get(NetHooks.class)));
        }

    }

    @Override
    public void implBeforeTcpBind(FileDescriptor fileDescriptor, InetAddress inetAddress, int port) throws IOException {
        for (NetHooksEventListener netHooksEventListener : registeredListener) {
            netHooksEventListener.handleBeforeTcpBind(new NetHooksEvent(inetAddress, port));
        }
        delegate.implBeforeTcpBind(fileDescriptor, inetAddress, port);
    }

    @Override
    public void implBeforeTcpConnect(FileDescriptor fileDescriptor, InetAddress inetAddress, int port) throws IOException {
        for (NetHooksEventListener netHooksEventListener : registeredListener) {
            netHooksEventListener.handleBeforeTcpConnect(new NetHooksEvent(inetAddress, port));
        }
        delegate.implBeforeTcpConnect(fileDescriptor, inetAddress, port);
    }


}
