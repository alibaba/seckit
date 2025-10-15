package com.alibaba.seckit.ssrf;

import java.util.EventListener;

/**
 * An {@link EventListener} registered to {@link SecurityNetHooksProvider}
 *
 * @author renyi.cry
  */
public interface NetHooksEventListener extends EventListener {

    void handleBeforeTcpConnect(NetHooksEvent event);

    void handleBeforeTcpBind(NetHooksEvent event);


}
