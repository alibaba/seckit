package com.alibaba.seckit.url;

import com.alibaba.seckit.util.matcher.StringMatcher;
import com.alibaba.seckit.util.matcher.SubDomainMatcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class UrlToolImpl implements UrlTool {

    @Override
    public boolean isRedirectUrlAllowed(String url, String[] allowedDomains, String[] excludedDomains) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            // Browsers normalize backslashes and strip some whitespace differently from java.net.URI.
            if (c == '\\' || Character.isISOControl(c) || Character.isWhitespace(c) || Character.isSpaceChar(c)) {
                return false;
            }
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return false;
        }
        if (uri.getScheme() == null && url.startsWith("/") && !url.startsWith("//")) {
            return true;
        }
        String scheme = uri.getScheme();
        if (scheme != null && !"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return false;
        }
        String host = uri.getHost();
        if (host == null || uri.getRawUserInfo() != null || uri.getPort() > 65535) {
            return false;
        }
        StringMatcher allowedMatcher = new SubDomainMatcher(allowedDomains);
        StringMatcher excludedMatcher = new SubDomainMatcher(excludedDomains);
        host = host.toLowerCase(Locale.ROOT);
        return allowedMatcher.matches(host) && !excludedMatcher.matches(host);
    }

}
