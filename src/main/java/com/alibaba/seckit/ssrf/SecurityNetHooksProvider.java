package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.util.ReflectionUtil;
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
            e.printStackTrace();
        }
    }

    public static void registerListener(NetHooksEventListener listener) {
        registeredListener.add(listener);
    }

    static void setProvider(Field field) throws Exception {

        if (field != null){
            field.setAccessible(true);
            // 这里有个很奇怪的坑...
            // 虽然setFinalField()里会处理掉field的final字段，但是因为新值需要把field作为delegate，
            // field.get()会先触发overrideFieldAccessor的赋值，导致检查isReadonly=true，field的赋值会失败报错，所以这里需要先执行一次
            // 但是在jdk21里面FieldAccessor逻辑不一样，会用memberName自己再读一遍，所以先执行之后反而会导致overrideFieldAccessor报错
            if(SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_20)) {
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
