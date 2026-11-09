package com.alibaba.seckit.deserialize.testcase;

import java.io.Serializable;

public class PrivateField implements Serializable {

    public String str;

    public PrivateField() {
        this.str = "str";
    }
}
