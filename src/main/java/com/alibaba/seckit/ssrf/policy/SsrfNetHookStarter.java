package com.alibaba.seckit.ssrf.policy;

/**
 * @author renyi.cry
 * @date 2018/4/2上午11:41
 */
public interface SsrfNetHookStarter {

    void start();

    void stop();

    void setHost(String host);
}
