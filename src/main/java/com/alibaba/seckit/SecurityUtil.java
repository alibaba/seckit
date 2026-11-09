package com.alibaba.seckit;

import com.alibaba.seckit.cli.CLITool;
import com.alibaba.seckit.cli.CLIToolImpl;
import com.alibaba.seckit.deserialize.DeserializeTool;
import com.alibaba.seckit.deserialize.DeserializeToolImpl;
import com.alibaba.seckit.jdbc.*;
import com.alibaba.seckit.sql.SqlTool;
import com.alibaba.seckit.sql.SqlToolImpl;
import com.alibaba.seckit.url.UrlTool;
import com.alibaba.seckit.url.UrlToolImpl;
import com.alibaba.seckit.xss.XssTool;
import com.alibaba.seckit.xss.XssToolImpl;
import com.alibaba.seckit.xxe.XxeTool;
import com.alibaba.seckit.xxe.XxeToolImpl;
import com.alibaba.seckit.ssrf.SSRFAdaptor;
import com.alibaba.seckit.ssrf.SSRFChecker;
import com.alibaba.seckit.ssrf.SSRFCheckerImpl;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

@Slf4j
public class SecurityUtil {

    private static final JdbcTool jdbcTool = new JdbcToolImpl();
    private static final SSRFChecker ssrfChecker = new SSRFCheckerImpl();
    private static final SSRFAdaptor ssrfAdaptor = new SSRFAdaptor();
    private static final XxeTool xxeTool = new XxeToolImpl();
    private static final SqlTool sqlTool = new SqlToolImpl();
    private static final CLITool cliTool = new CLIToolImpl();
    private static final UrlTool urlTool = new UrlToolImpl();
    private static final XssTool xssTool = new XssToolImpl();

    // Isolate JDK-specific deserialization linkage so an unapplied multi-release layer cannot break other tools.
    private static class DeserializeToolHolder {
        static final DeserializeTool TOOL = new DeserializeToolImpl();
    }

    /**
     * Must be called once during application startup before using SSRF protection or making guarded connections.
     * On JDK 16+, add these JVM options: <code>--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED</code>.
     */
    public static void initialize() {
        try {
            Class.forName("com.alibaba.seckit.ssrf.SecurityNetHooksProvider");
        } catch (Throwable e) {
            log.warn("ssrf initialize failed", e);
        }
    }

    /**
     * Filters a JDBC connection string, removing dangerous parameters, including parameters
     * that may lead to deserialization vulnerabilities and malformed parameters.
     *
     * @param url the connection string to filter
     * @return the filtered connection string
     * @throws JdbcURLException if the url cannot be parsed, usually because it is malformed
     */
    public static String filterJdbcConnectionSource(String url) throws JdbcURLException {
        return jdbcTool.filterConnectionSource(url);
    }

    public static FilterResult filterJdbcConnectionSourceWithResult(String url) throws JdbcURLException {
        return jdbcTool.filterConnectionSourceWithResult(url);
    }

    /**
     * Registers a filter. For the same scheme, a filter registered later overrides the earlier one.
     *
     * @param filter the filter to register
     */
    public static void registerFilter(Filter filter) {
        jdbcTool.registerFilter(filter);
    }

    /**
     * Adds XXE protection to an XML parser.
     *
     * @param builder supports the following builder (factory) types: <br>
     *                SAXReader <br>
     *                DocumentBuilderFactory <br>
     *                SAXParserFactory <br>
     *                XMLInputFactory <br>
     *                SAXBuilder <br>
     *                SchemaFactory <br>
     *                Validator <br>
     *                XMLReader <br>
     *                TransformerFactory <br>
     *                For unsupported builder types a RuntimeException is thrown.
     * @param <T>     the builder type
     * @return the factory or builder with XXE protection
     */
    public static <T> T withXxeProtection(T builder) {
        return xxeTool.withXxeProtection(builder);
    }

    /**
     * Returns the singleton {@link SSRFChecker}.
     * Call {@link #initialize()} once during application startup before using SSRF protection.
     * On JDK 16+, add these JVM options: <code>--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED</code>.
     *
     * @return the singleton {@link SSRFChecker}
     */
    public static SSRFChecker getSSRFCheckerSingleton() {
        return ssrfChecker;
    }

    /**
     * Adds SSRF checking to an HTTP client; every request issued by the resulting client is checked first.<br>
     * Supports http-clients 4.x/5.x (including HttpAsyncClient), OkHttp2 and OkHttp3. <br>
     * Call {@link #initialize()} once during application startup before using SSRF protection.
     * On JDK 16+, add these JVM options: <code>--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED</code>.
     * Example: <br>
     * <pre>
     * // http client
     * HttpClientBuilder builder = SecurityUtil.withSSRFChecking(HttpClients.custom());
     * CloseableHttpClient client = builder.build();
     *
     * // http async client
     * HttpAsyncClientBuilder builder = SecurityUtil.withSSRFChecking(HttpAsyncClients.custom());
     * CloseableHttpAsyncClient client = builder.build();
     *
     * // OkHttp 2
     * OkHttpClient client = SecurityUtil.withSSRFChecking(new OkHttpClient());
     *
     * // OkHttp 3
     * OkHttpClient.Builder builder = SecurityUtil.withSSRFChecking(new OkHttpClient.Builder());
     * OkHttpClient client = builder.build();
     * </pre>
     * A client created this way <b>cannot access internal networks</b>; use a different client
     * for requests that legitimately target internal addresses.
     *
     * @param <T>             the type of the client or client builder
     * @param clientOrBuilder the client or client builder to add SSRF checking to
     * @return the client or client builder with SSRF checking
     */
    public static <T> T withSSRFChecking(T clientOrBuilder) {
        return ssrfAdaptor.withSSRFChecking(clientOrBuilder);
    }

    /**
     * Starts hook-based checking. Until {@link #stopSSRFNetHookChecking()} is called, every TCP connection
     * made by the current thread is checked for SSRF.
     * Call {@link #initialize()} once during application startup before using SSRF protection.
     * On JDK 16+, add these JVM options: <code>--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED</code>.
     *
     * <p>
     * When a connection is considered unsafe, {@link SSRFUnsafeConnectionException} is thrown.
     *
     * <p>
     * Because the hook throws right before the TCP connection is made, connection pools used by the client
     * are not closed automatically; handle them where {@link SSRFUnsafeConnectionException} is caught.
     *
     * <p>Usage:<blockquote><pre>
     *     try {
     *         SecurityUtil.startSSRFNetHookChecking();
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     } finally {
     *         SecurityUtil.stopSSRFNetHookChecking();
     *     }
     * </pre></blockquote>
     */
    public static void startSSRFNetHookChecking() {
        ssrfChecker.startNetHookWithThreadLocal();
    }

    /**
     * Starts hook-based checking. Until {@link #stopSSRFNetHookChecking()} is called, every TCP connection
     * made by the current thread is checked for SSRF.
     * Call {@link #initialize()} once during application startup before using SSRF protection.
     * On JDK 16+, add these JVM options: <code>--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED</code>.
     *
     * <p>
     * When a connection is considered unsafe, {@link SSRFUnsafeConnectionException} is thrown.
     *
     * <p>
     * Because the hook throws right before the TCP connection is made, connection pools used by the client
     * are not closed automatically; handle them where {@link SSRFUnsafeConnectionException} is caught.
     *
     * <p>Usage:<blockquote><pre>
     *     try {
     *         SecurityUtil.startSSRFNetHookChecking(url);
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     } finally {
     *         SecurityUtil.stopSSRFNetHookChecking();
     *     }
     * </pre></blockquote>
     *
     * @param url the url to be accessed
     */
    public static void startSSRFNetHookChecking(String url) {
        ssrfChecker.startNetHookWithThreadLocal(url);
    }

    /**
     * Stops SSRF net hook checking for the current thread.
     * Call {@link #initialize()} once during application startup before using SSRF protection.
     * On JDK 16+, add these JVM options: <code>--add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED</code>.
     */
    public static void stopSSRFNetHookChecking() {
        ssrfChecker.stopNetHookWithThreadLocal();
    }

    /**
     * Escapes user input for concatenation into a SQL string literal.
     *
     * @param sql the user input
     * @return the escaped input
     */
    public static String escapeSql(String sql) {
        return sqlTool.mysqlRealEscapeString(sql);
    }

    /**
     * Sanitizes user input used as a query condition or a column name such as in <code>ORDER BY</code>.
     * <p>
     * Only <code>a-zA-Z0-9_-.</code> and Chinese characters are kept; all other characters are removed.
     * Not suitable for expressions.
     *
     * @param sql the SQL fragment to sanitize
     * @return the sanitized fragment
     */
    public static String trimSql(String sql) {
        return sqlTool.trimSql(sql);
    }

    /**
     * Sanitizes user input used as column names in <code>ORDER BY</code>.
     * <p>
     * Only <code>a-zA-Z0-9_-. ,</code> and Chinese characters are kept; all other characters are removed.
     * Not suitable for expressions.
     *
     * @param sql the column names to sanitize
     * @return the sanitized column names
     */
    public static String trimSql4OrderBy(String sql) {
        return sqlTool.trimSql4OrderBy(sql);
    }

    /**
     * Keeps ASCII letters/digits, underscore, hyphen, comma, dot, and Chinese characters U+4E00-U+9FBB.
     * This filters an individual argument, not a full shell command; command names and options need separate validation.
     *
     * @param cmd the input argument
     * @return the filtered argument, or null if the input is null
     */
    public static String trimCmd(String cmd) {
        return cliTool.filter(cmd);
    }

    /**
     * Creates an ObjectInputStream that only allows basic JDK classes such as String and Map.
     */
    public static ObjectInputStream newObjectInputStream(InputStream in) throws IOException {
        return DeserializeToolHolder.TOOL.newObjectInputStream(in, null, null);
    }

    /**
     * Creates an ObjectInputStream with an allowlist given as patterns.
     * Basic JDK classes such as String and Map are allowed by default.
     * Pattern rules:
     * <ul>
     *     <li><code>com.package.TestClass</code> allows only com.package.TestClass (not its fields' types or super classes)</li>
     *     <li><code>com.package.*</code> allows classes in package com.package</li>
     *     <li><code>com.package.**</code> allows classes in package com.package and all of its sub-packages</li>
     * </ul>
     */
    public static ObjectInputStream newObjectInputStream(InputStream in, String... allowedPatterns) throws IOException {
        return DeserializeToolHolder.TOOL.newObjectInputStream(in, allowedPatterns, null);
    }

    /**
     * Creates an ObjectInputStream with an allowlist given as classes.
     * Basic JDK classes such as String and Map are allowed by default.
     * <p>
     * Allowing <code>com.package.TestClass</code> also allows its super classes and the declared types
     * of non-static, non-transient instance fields. Custom serialization may require explicit extra classes.
     * Note that subclasses of a field's declared type are NOT allowed. For example:
     * <pre>
     * public class TestClass extends SuperCls implements Serializable {
     *     private Map&lt;String, String&gt; map;
     *     private CustomCls customCls;
     * }
     * </pre>
     * allows TestClass, SuperCls, Map and CustomCls, but not HashMap or subclasses of CustomCls.
     */
    public static ObjectInputStream newObjectInputStream(InputStream in, Class<?>... allowedClasses) throws IOException {
        return DeserializeToolHolder.TOOL.newObjectInputStream(in, null, allowedClasses);
    }

    /**
     * Creates an ObjectInputStream with an allowlist given as both patterns and classes.
     *
     * @see #newObjectInputStream(InputStream, String...)
     * @see #newObjectInputStream(InputStream, Class...)
     */
    public static ObjectInputStream newObjectInputStream(InputStream in, String[] allowedPatterns, Class<?>[] allowedClasses) throws IOException {
        return DeserializeToolHolder.TOOL.newObjectInputStream(in, allowedPatterns, allowedClasses);
    }

    /**
     * Allows a well-formed rooted relative path, or an HTTP(S) URL whose host matches allowedDomains.
     * Network-path references such as <code>//example.com/path</code> also require a matching host.
     * Malformed URLs, backslashes, unencoded whitespace/control characters, and userinfo are rejected.
     * Matching rules (case-insensitive):
     * <ul>
     *     <li><code>example.com</code> matches example.com exactly</li>
     *     <li><code>*.example.com</code> matches example.com and all of its sub-domains, e.g. a.example.com</li>
     * </ul>
     */
    public static boolean isRedirectUrlAllowed(String url, String... allowedDomains) {
        return urlTool.isRedirectUrlAllowed(url, allowedDomains, null);
    }

    /**
     * Checks whether a redirect target is safe: the url is relative, or its host matches allowedDomains
     * and does not match excludeDomains.
     *
     * @see #isRedirectUrlAllowed(String, String...)
     */
    public static boolean isRedirectUrlAllowed(String url, String[] allowedDomains, String[] excludeDomains) {
        return urlTool.isRedirectUrlAllowed(url, allowedDomains, excludeDomains);
    }

    /**
     * <p>Escapes text for an HTML context, converting the following characters into HTML entities:</p>
     *
     * <blockquote><pre>
     * " -&gt; &amp;quot;
     * &amp; -&gt; &amp;amp;
     * ' -&gt; &amp;#39;
     * &lt; -&gt; &amp;lt;
     * &gt; -&gt; &amp;gt;
     * </pre></blockquote>
     */
    public static String escapeHtml(String text) {
        return xssTool.escapeHtml(text);
    }

    /**
     * Escapes text for a JavaScript context by encoding every non-alphanumeric character
     * as a JavaScript Unicode escape, preventing user data from breaking out of a JS string.
     */
    public static String escapeJavascript(String text) {
        return xssTool.escapeJavaScript(text);
    }
}
