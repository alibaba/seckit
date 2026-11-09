package com.alibaba.seckit.deserialize.testcase.blocked;

import java.io.Serializable;

public class Blocked implements Serializable {

    public String str;

    public Blocked() {
        this.str = "blocked";
    }
}
