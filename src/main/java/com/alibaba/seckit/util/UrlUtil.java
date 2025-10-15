package com.alibaba.seckit.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtil {

	private static final char[] STOP_WORDS = { '/', '?', '#' };

	private static final EscapeStringEncoder encoder = new EscapeStringEncoder('-', new char[]{'_', '-'}, new char[]{'0', '1'});

	/**
	 * Parse url
	 *
	 * @param url to be parsed
	 * @param isHost param url is host or not
	 * @return formatted host
	 */
	public static String parseUrl(String url, boolean isHost) {
		if (isHost) {
			return url.toLowerCase();
		}

		try {
			return UrlUtil.getHost(url);
		} catch (URISyntaxException e) {
			return null;
		}

	}

	/**
	 * Check if the host is legal.
	 *
	 * @param host to be checked
	 * @return true if the host is legal, false otherwise
	 */
	public static boolean validateHost(String host) {
		char[] chs = host.toCharArray();

		for (int i = 0; i < chs.length; i++) {
			if (!((chs[i] >= 'a' && chs[i] <= 'z')
					|| (chs[i] >= 'A' && chs[i] <= 'Z')
					|| (chs[i] >= '0' && chs[i] <= '9')
					|| chs[i] == '-'
					|| chs[i] == '.'
					|| chs[i] == '_'
					|| chs[i] == ':'
					|| chs[i] == '['
					|| chs[i] == ']')) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get host from url.
	 *
	 * @param url to be parsed
	 * @return host
	 * @throws URISyntaxException if url is invalid
     */
	public static String getHost(String url) throws URISyntaxException {
		// 删除协议与参数，有些特殊的参数会导致URI解析出错
		// eg. http://www.baidu.com/?abc=123&aaa={name:1}
		url = getHostFormURL(url);
		if (url == null) {
			return null;
		}
		url = encoder.encode(url);
		String host = new URI(url).getHost();
		if (host == null) {
			return null;
		}
		host = encoder.decode(host);
		return host.toLowerCase();
	}

	/**
	 * delete path and queryString from url.
	 *
	 * @param url to be parsed
	 * @return host
	 */
    public static String getHostFormURL(String url) {

        int idx = url.indexOf("://");
        if (idx == -1) {
            if (url.startsWith("//")) {
                idx = 2;
            } else {
                return url;
            }
        } else {
            idx += 3;
        }
        for (; idx < url.length() - 1; idx++) {
            if (url.charAt(idx) != '/') {
                break; //skip '/' after schema
            }
        }
        int off = idx;
        for (char c : STOP_WORDS) {
            idx = url.indexOf(c, off);
            if (idx != -1) {
                url = url.substring(0, idx);
                break;
            }
        }
        return url;
    }

}
