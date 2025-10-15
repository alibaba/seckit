package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import com.alibaba.seckit.ssrf.checkcondition.SSRFNetHooksCheckCondition;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author renyi.cry
 * @date 17/3/7 下午6:21
 */
@Slf4j
public class SSRFNetHooksEventListener implements NetHooksEventListener {

    private final List<SSRFNetHooksCheckCondition> checkConditions;

    public SSRFNetHooksEventListener(SSRFNetHooksCheckCondition... conditions) {
        this.checkConditions = Collections.unmodifiableList(Arrays.asList(conditions));
    }

    @Override
    public void handleBeforeTcpConnect(NetHooksEvent event) {
        InetAddress address = event.getInetAddress();
        log.debug("Connecting: {}", address);
        check(address);
    }

    @Override
    public void handleBeforeTcpBind(NetHooksEvent event) {
    }

    protected void check(InetAddress address) {

        for (SSRFNetHooksCheckCondition checkCondition : checkConditions) {
            if (checkCondition.needsCheck(address)) {
                boolean allowedAddress = SSRFCheckerImpl.isAllowedAddress(address);

                if (!allowedAddress) {
                    log.warn("ssrf attack, address: {}", address);
                    throw new SSRFUnsafeConnectionException("unsafe address", address);
                } else {
                    log.info("ssrf check: {}, result: true", address.toString());
                }

            }
        }

    }
}
