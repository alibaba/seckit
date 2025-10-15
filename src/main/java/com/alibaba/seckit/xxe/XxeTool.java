package com.alibaba.seckit.xxe;

public interface XxeTool {
    <T> T withXxeProtection(T builder);
}
