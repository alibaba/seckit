package com.alibaba.seckit.deserialize.testcase;

import java.io.Serializable;

public class InnerClass implements Serializable {

    public static class Inner implements Serializable {
        public String str;
    }


    public Inner inner;


    public InnerClass() {
        inner = new Inner();
    }
}
