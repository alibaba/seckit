package com.alibaba.seckit.xss;

public interface XssTool {

    String escapeHtml(String text);

    String escapeJavaScript(String text);
}
