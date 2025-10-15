package com.alibaba.seckit.decorator;

public interface Decorator {

    boolean accept(Object clientOrBuilder);

    <T> T decorate(T clientOrBuilder);

}
