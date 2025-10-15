package com.alibaba.seckit.ssrf.policy;

/**
 * @author renyi.cry
  */
public class ExpiredCachedBasedSsrfNetHookStarter implements SsrfNetHookStarter {

    private String host;

    @Override
    public void start() {
        CompatibleSsrfCheckerRegister.startCachedNetHooking(host);
    }

    @Override
    public void stop() {
        // Do nothing
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

}
