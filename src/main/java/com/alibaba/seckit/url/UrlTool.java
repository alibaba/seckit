package com.alibaba.seckit.url;

public interface UrlTool {
    boolean isRedirectUrlAllowed(String url, String[] allowedDomains, String[] excludedDomains);
}
