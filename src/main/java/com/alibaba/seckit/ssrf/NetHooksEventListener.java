package com.alibaba.seckit.ssrf;

import java.util.EventListener;

/**
 * An {@link EventListener} registered to {@link SecurityNetHooksProvider}
 *
 * @author renyi.cry
 * @date 17/2/14
 */
public interface NetHooksEventListener extends EventListener {

    void handleBeforeTcpConnect(NetHooksEvent event);

    void handleBeforeTcpBind(NetHooksEvent event);


}
