package com.alibaba.seckit.ssrf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.net.InetAddress;

/**
 * A {@link NetHooksEvent} represents a event for {@link NetHooksEventListener}
 * registered to {@link SecurityNetHooksProvider}
 *
 * @author renyi.cry
 * @date 17/2/14
 */
@Getter
@ToString
@AllArgsConstructor
public class NetHooksEvent {

    private InetAddress inetAddress;

    private int port;

}
