package com.alibaba.seckit.ssrf;

public interface HttpClientsAdaptor {
    <T> T withSSRFChecking(T clientOrBuilder);
}
