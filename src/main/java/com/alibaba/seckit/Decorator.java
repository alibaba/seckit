package com.alibaba.seckit;

public interface Decorator {
    boolean accept(Object clientOrBuilder);
    <T> T decorate(T clientOrBuilder);
}
