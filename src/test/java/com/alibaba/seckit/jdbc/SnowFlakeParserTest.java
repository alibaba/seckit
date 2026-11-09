package com.alibaba.seckit.jdbc;

import com.alibaba.seckit.SecurityUtil;
import com.alibaba.seckit.jdbc.parser.SnowFlakeParser;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SnowFlakeParserTest {

    private static final String PREFIX = "jdbc:snowflake://";
    private static final String HOST = "account.snowflakecomputing.com";

    @Test
    public void testInterfaceParseUsesSnowflakeScheme() throws Exception {
        String url = PREFIX + HOST + "?user=u";
        UrlParser parser = new SnowFlakeParser(url);
        parser.parse();

        SnowFlakeParser snowflake = (SnowFlakeParser) parser;
        assertEquals(url, parser.getInitialUrl());
        assertEquals("snowflake", snowflake.getScheme());
        assertEquals(HOST, snowflake.getHost());
        assertEquals(-1, snowflake.getPort());
        assertEquals(Collections.singletonMap("user", "u"), parser.getProperties());
        assertTrue(parser.toString().startsWith(PREFIX + HOST + "/?"));
        assertRoundTrip(snowflake);
    }

    @Test
    public void testSecurityUtilReproFiltersWithoutDoublingPrefix() throws Exception {
        String filtered = SecurityUtil.filterJdbcConnectionSource(
                PREFIX + HOST + "/?user=u&password=p&fakeParam=evil");

        URI uri = new URI(filtered.substring("jdbc:".length()));
        assertEquals("snowflake", uri.getScheme());
        assertEquals(HOST, uri.getHost());
        assertEquals(-1, uri.getPort());
        assertEquals("/", uri.getPath());
        assertTrue(filtered.startsWith(PREFIX + HOST + "/?"));

        Map<String, String> expected = new HashMap<>();
        expected.put("user", "u");
        expected.put("password", "p");
        SnowFlakeParser parser = parse(filtered);
        assertEquals(expected, parser.getProperties());
        assertRoundTrip(parser);
    }

    @Test
    public void testSlashQueryAndPortVariants() throws Exception {
        String[] endings = {"", "/", "?", "/?", "?user=u", "/?user=u"};
        for (int port : new int[]{-1, 0, 443, 65535}) {
            String authority = HOST + (port < 0 ? "" : ":" + port);
            for (String ending : endings) {
                String url = PREFIX + authority + ending;
                SnowFlakeParser parser = parse(url);
                assertEquals(url, "snowflake", parser.getScheme());
                assertEquals(url, HOST, parser.getHost());
                assertEquals(url, port, parser.getPort());
                assertNull(parser.getDatabase());
                Map<String, String> expected = ending.endsWith("user=u")
                        ? Collections.singletonMap("user", "u") : Collections.emptyMap();
                assertEquals(url, expected, parser.getProperties());
                assertTrue(parser.toString().startsWith(PREFIX + authority + "/"));
                assertRoundTrip(parser);

                SnowFlakeParser filtered = parse(SecurityUtil.filterJdbcConnectionSource(url));
                assertEquals(HOST, filtered.getHost());
                assertEquals(port, filtered.getPort());
                assertEquals(expected, filtered.getProperties());
                assertRoundTrip(filtered);
            }
        }
    }

    @Test
    public void testMixedCasePrefixIsCanonicalized() throws Exception {
        String url = "JdBc:SnOwFlAkE://Account.snowflakecomputing.com/?USER=u&FakeParam=evil";
        SnowFlakeParser parser = parse(url);
        assertEquals("snowflake", parser.getScheme());
        assertEquals("Account.snowflakecomputing.com", parser.getHost());
        assertRoundTrip(parser);

        String filtered = SecurityUtil.filterJdbcConnectionSource(url);
        assertTrue(filtered.startsWith(PREFIX + "Account.snowflakecomputing.com/?"));
        parser = parse(filtered);
        assertEquals(Collections.singletonMap("USER", "u"), parser.getProperties());
        assertRoundTrip(parser);
    }

    @Test
    public void testUnderscoreAndIpv6Hosts() throws Exception {
        String[] hosts = {"account_name.snowflakecomputing.com", "[::1]", "[2001:db8::1]"};
        for (String host : hosts) {
            for (int port : new int[]{-1, 443}) {
                String authority = host + (port < 0 ? "" : ":" + port);
                String url = PREFIX + authority + "?allowUnderscoresInHost=true&fakeParam=evil";
                SnowFlakeParser parser = parse(url);
                assertEquals(host, parser.getHost());
                assertEquals(port, parser.getPort());
                assertRoundTrip(parser);

                String filtered = SecurityUtil.filterJdbcConnectionSource(url);
                assertTrue(filtered.startsWith(PREFIX + authority + "/?"));
                parser = parse(filtered);
                assertEquals(host, parser.getHost());
                assertEquals(port, parser.getPort());
                assertEquals(Collections.singletonMap("allowUnderscoresInHost", "true"), parser.getProperties());
                assertRoundTrip(parser);
            }
        }
    }

    @Test
    public void testUtf8DecodingFirstEqualsAndExistingValuePolicy() throws Exception {
        String url = PREFIX + HOST + "/?%75ser=用户&%70assword=p%26x=y%2Bz+space=="
                + "&role=分析&schema=%E5%88%86%E6%9E%90&application=client%2Dapp"
                + "&warehouse=wh%5F1&db=&fakeParam=evil&%E5%8F%82%E6%95%B0=%E5%80%BC&ignored";
        Map<String, String> allowed = new HashMap<>();
        allowed.put("user", "用户");
        allowed.put("password", "p&x=y+z space==");
        allowed.put("application", "client-app");
        allowed.put("warehouse", "wh_1");
        allowed.put("db", "");

        Map<String, String> parsed = new HashMap<>(allowed);
        parsed.put("role", "分析");
        parsed.put("schema", "分析");
        parsed.put("fakeParam", "evil");
        parsed.put("参数", "值");
        SnowFlakeParser parser = parse(url);
        assertEquals(parsed, parser.getProperties());
        assertRoundTrip(parser);

        // Credentials bypass value restrictions; other accepted keys still use the ASCII value policy.
        String filtered = SecurityUtil.filterJdbcConnectionSource(url);
        parser = parse(filtered);
        assertEquals(allowed, parser.getProperties());
        assertRoundTrip(parser);
        assertEquals(allowed, parse(SecurityUtil.filterJdbcConnectionSource(filtered)).getProperties());
    }

    @Test
    public void testIntendedNestedHttpAndHttpsSchemes() throws Exception {
        for (String scheme : new String[]{"http", "https"}) {
            for (int port : new int[]{-1, 8443}) {
                String authority = HOST + (port < 0 ? "" : ":" + port);
                String url = PREFIX + scheme + "://" + authority + "?user=u&fakeParam=evil";
                SnowFlakeParser parser = parse(url);
                assertEquals(scheme, parser.getScheme());
                assertEquals(HOST, parser.getHost());
                assertEquals(port, parser.getPort());
                assertTrue(parser.toString().startsWith(PREFIX + scheme + "://" + authority + "/?"));
                assertRoundTrip(parser);

                String filtered = SecurityUtil.filterJdbcConnectionSource(url);
                assertTrue(filtered.startsWith(PREFIX + scheme + "://" + authority + "/?"));
                parser = parse(filtered);
                assertEquals(scheme, parser.getScheme());
                assertEquals(HOST, parser.getHost());
                assertEquals(port, parser.getPort());
                assertEquals(Collections.singletonMap("user", "u"), parser.getProperties());
                assertRoundTrip(parser);
            }
        }
    }

    @Test
    public void testMalformedUrlsAndUnsupportedSchemesAreRejected() {
        String[] invalid = {
                null, "", "jdbc:snowflake:", "jdbc:snowflake:/" + HOST,
                "jdbc:snowflake2://" + HOST, "snowflake://" + HOST, "http://" + HOST,
                PREFIX, PREFIX + "/", PREFIX + "?user=u", PREFIX + "/?user=u",
                PREFIX + "host name", PREFIX + "user@" + HOST,
                PREFIX + "[::1", PREFIX + HOST + ":abc", PREFIX + HOST + ":-1",
                PREFIX + HOST + ":65536", PREFIX + HOST + ":999999999999",
                PREFIX + HOST + "/database", PREFIX + HOST + "//", PREFIX + HOST + "/%64atabase",
                PREFIX + "http://", PREFIX + "https://?user=u",
                PREFIX + "https://" + HOST + "/database",
                PREFIX + "ftp://" + HOST, PREFIX + "snowflake://" + HOST
        };
        for (String url : invalid) {
            assertThrows(String.valueOf(url), JdbcURLUnsafeException.class, () -> parse(url));
        }
    }

    @Test
    public void testMalformedQueryEncodingThrowsCheckedUnsafeException() {
        for (String query : new String[]{"user=%", "password=%2", "%GG=u", "user=%XY"}) {
            String url = PREFIX + HOST + "/?" + query;
            assertThrows(url, JdbcURLUnsafeException.class, () -> parse(url));
            assertThrows(url, JdbcURLUnsafeException.class, () -> SecurityUtil.filterJdbcConnectionSource(url));
        }
    }

    private static SnowFlakeParser parse(String url) throws JdbcURLException {
        SnowFlakeParser parser = new SnowFlakeParser(url);
        UrlParser urlParser = parser;
        urlParser.parse();
        return parser;
    }

    private static void assertRoundTrip(SnowFlakeParser parser) throws JdbcURLException {
        SnowFlakeParser reparsed = parse(parser.toString());
        assertEquals(parser.getScheme(), reparsed.getScheme());
        assertEquals(parser.getHost(), reparsed.getHost());
        assertEquals(parser.getPort(), reparsed.getPort());
        assertEquals(parser.getDatabase(), reparsed.getDatabase());
        assertEquals(parser.getProperties(), reparsed.getProperties());
    }
}
