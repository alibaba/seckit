package com.alibaba.seckit.ssrf.policy;

/**
 * @author renyi.cry
 * @date 2018/4/2上午11:42
 */
public class ThreadLocalBasedSsrfNetHookStarter implements SsrfNetHookStarter {

    @Override
    public void start() {
        CompatibleSsrfCheckerRegister.start();
    }

    @Override
    public void stop() {
        CompatibleSsrfCheckerRegister.stop();
    }

    @Override
    public void setHost(String host) {
        // do nothing
    }

}
