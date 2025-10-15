package com.alibaba.seckit;

import com.alibaba.seckit.jdbc.*;
import com.alibaba.seckit.xxe.XxeTool;
import com.alibaba.seckit.xxe.XxeToolImpl;
import com.alibaba.seckit.ssrf.SSRFAdaptor;
import com.alibaba.seckit.ssrf.SSRFChecker;
import com.alibaba.seckit.ssrf.SSRFCheckerImpl;
import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;

public class SecurityUtil {

    private static final JdbcTool jdbcTool = new JdbcToolImpl();
    private static final SSRFChecker ssrfChecker = new SSRFCheckerImpl();
    private static final SSRFAdaptor ssrfAdaptor = new SSRFAdaptor();
    private static final XxeTool xxeTool = new XxeToolImpl();

    /**
     * 过滤JDBC URL 连接串，删掉其中危险的参数，包括可能造成反序列化漏洞的参数、格式不合法的参数
     *
     * @param url 待过滤的连接串
     * @return 过滤后的连接串
     * @throws JdbcURLException 解析Url失败时抛出该异常，一般为url不合法
     */
    public static String filterJdbcConnectionSource(String url) throws JdbcURLException {
        return jdbcTool.filterConnectionSource(url);
    }

    public static FilterResult filterJdbcConnectionSourceWithResult(String url) throws JdbcURLException {
        return jdbcTool.filterConnectionSourceWithResult(url);
    }

    /**
     * 注册Filter，对于同一个schema, 后注册的会覆盖先注册的Filter
     *
     * @param filter 过滤类
     */
    public static void registerFilter(Filter filter) {
        jdbcTool.registerFilter(filter);
    }

    /**
     * 给XML解析器增加XXE防护
     * @param builder 支持以下类型的Builder(Factory)： <br>
     *                SAXReader <br>
     *                DocumentBuilderFactory <br>
     *                SAXParserFactory <br>
     *                XMLInputFactory <br>
     *                SAXBuilder <br>
     *                SchemeFactory <br>
     *                Validator <br>
     *                XMLReader <br>
     *                TransformerFactory <br>
     *                对于不受支持的builder类型，执行withXxeProtection()会抛出RuntimeException()异常，提示不受支持
     * @return 加了XXE保护的Factory或builder
     * @param <T> builder类型
     */
    public static <T> T withXxeProtection(T builder) {
        return xxeTool.withXxeProtection(builder);
    }

    /**
     * 获取实现SSRFChecker接口的单例类
     *
     * @return SSRFChecker接口实现的单例类
     */
    public static SSRFChecker getSSRFCheckerSingleton() {
        return ssrfChecker;
    }

    /**
     * 给要生成的HttpClient增加SSRF检查功能，生成的client请求前都会进行SSRF检查。<br>
     * 适用于 http-clients 4.x/5.x (包含HttpAsyncClient)版本、OkHttp2、OkHttp3 <br>
     * example: <br>
     * <pre>
     * // http client
     * HttpClientBuilder builder = SecurityUtil.withSSRFChecking(HttpClients.custom());
     * CloseableHttpClient client = builder.build();
     *
     * // http async client
     * HttpAsyncClientBuilder builder = SecurityUtil.withSSRFChecking(HttpAsyncClients.custom());
     * CloseableHttpAsyncClient client = builder.build();
     *
     * // Okhttp 2
     * OkHttpClient client = SecurityUtil.withSSRFChecking(new OkHttpClient());
     *
     * // Okhttp 3
     * OkHttpClient.Builder builder = SecurityUtil.withSSRFChecking(new OkHttpClient.Builder());
     * OkHttpClient client = builder.build();
     * </pre>
     * 加了withSSRFChecking之后生成的client<b>不能请求内网</b>，如果有请求内网URL的需求需要用其他client访问
     * @param <T> client或clientBuilder的类型
     * @param clientOrBuilder 要加入SSRF检查功能的 client 或 clientBuilder
     * @return 附带SSRF检查功能的client或clientBuilder
     */
    public static <T> T withSSRFChecking(T clientOrBuilder) {
        return ssrfAdaptor.withSSRFChecking(clientOrBuilder);
    }

    /**
     * 启动Hook检查，调用该函数后，在调用{@link #stopSSRFNetHookChecking()}函数前，
     * 该线程在所有tcp连接前会检查是否有ssrf攻击威胁。
     *
     * <p>
     * 检查启动后，当某个待连接的tcp连接被判断包含ssrf攻击威胁时，
     * 将抛出{@link SSRFUnsafeConnectionException}
     *
     * <p>
     * 同时，由于NetHook方式的原理是在TCP发起前拦截并直接抛出异常，如果使用的连接工具包含连接池，
     * 该方式不会默认关闭连接池资源，开发需要在{@link SSRFUnsafeConnectionException}异常处处理连接池问题。
     *
     * <p>请通过如下方式使用该接口：<blockquote><pre>
     *     try{
     *         SecurityUtil.startSSRFNetHookChecking();
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     } finally {
     *         SecurityUtil.stopSSRFNetHookChecking();
     *     }
     * </pre></blockquote>
     *
     */
    public static void startSSRFNetHookChecking() {
        ssrfChecker.startNetHookWithThreadLocal();
    }

    /**
     * 启动Hook检查，调用该函数后，在调用{@link #stopSSRFNetHookChecking()}函数前，
     * 该线程在所有tcp连接前会检查是否有ssrf攻击威胁。
     *
     * <p>
     * 检查启动后，当某个待连接的tcp连接被判断包含ssrf攻击威胁时，
     * 将抛出{@link SSRFUnsafeConnectionException}
     *
     * <p>
     * 同时，由于NetHook方式的原理是在TCP发起前拦截并直接抛出异常，如果使用的连接工具包含连接池，
     * 该方式不会默认关闭连接池资源，开发需要在{@link SSRFUnsafeConnectionException}异常处处理连接池问题。
     *
     * <p>请通过如下方式使用该接口：<blockquote><pre>
     *     try{
     *         SecurityUtil.startSSRFNetHookChecking(url);
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     } finally {
     *         SecurityUtil.stopSSRFNetHookChecking();
     *     }
     * </pre></blockquote>
     *
     * @param url 待访问的url
     */
    public static void startSSRFNetHookChecking(String url) {
        ssrfChecker.startNetHookWithThreadLocal(url);
    }

    /**
     * 停止SSRF Net Hook检查
     *
     */
    public static void stopSSRFNetHookChecking() {
        ssrfChecker.stopNetHookWithThreadLocal();
    }


}
