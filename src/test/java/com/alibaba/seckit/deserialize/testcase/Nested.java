package com.alibaba.seckit.deserialize.testcase;


import java.io.Serializable;

public class Nested extends Super implements Serializable {

    public String strInNested;

    public Nested() {
        strInNested = "test str";
    }
}
