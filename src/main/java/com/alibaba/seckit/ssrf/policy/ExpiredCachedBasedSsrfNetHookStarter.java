package com.alibaba.seckit.ssrf.policy;

/**
 * @author renyi.cry
 * @date 2018/4/2上午11:42
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
