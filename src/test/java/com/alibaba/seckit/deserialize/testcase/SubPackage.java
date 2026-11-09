package com.alibaba.seckit.deserialize.testcase;

import com.alibaba.seckit.deserialize.testcase.allowed.Allowed;
import com.alibaba.seckit.deserialize.testcase.blocked.Blocked;

import java.io.Serializable;

public class SubPackage implements Serializable {
    public Allowed allowed;

    public Blocked blocked;

    public SubPackage() {
        this.allowed = new Allowed();
        this.blocked = new Blocked();
    }
}
