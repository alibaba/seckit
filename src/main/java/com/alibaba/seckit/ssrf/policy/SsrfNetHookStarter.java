package com.alibaba.seckit.ssrf.policy;

/**
 * @author renyi.cry
  */
public interface SsrfNetHookStarter {

    void start();

    void stop();

    void setHost(String host);
}
