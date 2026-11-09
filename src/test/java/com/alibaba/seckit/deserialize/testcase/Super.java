package com.alibaba.seckit.deserialize.testcase;

import java.io.Serializable;

public class Super implements Serializable {

    private PrivateField privateField;

    public Super() {
        privateField = new PrivateField();
    }
}
