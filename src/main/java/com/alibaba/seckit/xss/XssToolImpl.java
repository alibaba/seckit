package com.alibaba.seckit.xss;

import org.unbescape.html.HtmlEscape;
import org.unbescape.javascript.JavaScriptEscape;
import org.unbescape.javascript.JavaScriptEscapeLevel;
import org.unbescape.javascript.JavaScriptEscapeType;

public class XssToolImpl implements XssTool {

    @Override
    public String escapeHtml(String text) {
        return HtmlEscape.escapeHtml4(text);
    }

    @Override
    public String escapeJavaScript(String text) {
        return JavaScriptEscape.escapeJavaScript(text,
                JavaScriptEscapeType.UHEXA,
                JavaScriptEscapeLevel.LEVEL_3_ALL_NON_ALPHANUMERIC);
    }
}
