package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;

/**
 * SSRF检查器。
 *
 * @author renyi.cry
 */
public interface SSRFChecker {

    String[] ALLOWED_PROTOCOLS = {"http://", "https://"};

    /**
     * 启动Hook检查，调用该函数后，在调用{@link #stop()}函数前，
     * 该线程在所有tcp连接前会检查是否有ssrf攻击威胁。
     *
     * <p>
     * 检查启动后，当某个待连接的tcp连接被判断包含ssrf攻击威胁时，
     * 将抛出{@link SSRFUnsafeConnectionException}
     *
     * <p>请通过如下方式使用该接口：<blockquote><pre>
     *     try{
     *         ssrfChecker.start();
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     } finally {
     *         ssrfChecker.stop();
     *     }
     * </pre></blockquote>
     *
     * @see #startNetHookWithThreadLocal()
     * @deprecated since 1.2.15
     */
    @Deprecated
    void start();

    /**
     * 停止Hook检查，与{@link #start()}方法联合使用
     *
     * @see #stopNetHookWithThreadLocal()
     * @deprecated since 1.2.15
     */
    @Deprecated
    void stop();


    /**
     * 启动Hook检查，调用该函数后，在调用{@link #stopNetHookWithThreadLocal()}函数前，
     * 该线程在所有tcp连接前会检查是否有ssrf攻击威胁。
     *
     * <p>
     * 检查启动后，当某个待连接的tcp连接被判断包含ssrf攻击威胁时，
     * 将抛出{@link SSRFUnsafeConnectionException}
     *
     * <p>请通过如下方式使用该接口：<blockquote><pre>
     *     try{
     *         ssrfChecker.startNetHookWithThreadLocal();
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     } finally {
     *         ssrfChecker.stopNetHookWithThreadLocal();
     *     }
     * </pre></blockquote>
     *
     */
    void startNetHookWithThreadLocal();

    /**
     * 启动Hook检查，调用该函数后，在调用{@link #stopNetHookWithThreadLocal()}函数前，
     * 该线程在所有tcp连接前会检查是否有ssrf攻击威胁。
     *
     * <p>
     * 检查启动后，当某个待连接的tcp连接被判断包含ssrf攻击威胁时，
     * 将抛出{@link SSRFUnsafeConnectionException}
     *
     * <p>请通过如下方式使用该接口：<blockquote><pre>
     *     try{
     *         ssrfChecker.startNetHookWithThreadLocal(url);
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     } finally {
     *         ssrfChecker.stopNetHookWithThreadLocal();
     *     }
     * </pre></blockquote>
     * @param url 待访问的url
     */
    void startNetHookWithThreadLocal(String url);

    /**
     * 停止Hook检查，与{@link #startNetHookWithThreadLocal()} 或
     * {@link #startNetHookWithExpirationCache(String)}方法联合使用
     *
     */
    void stopNetHookWithThreadLocal();

    /**
     * 启动Hook检查，调用该函数后，在一定时间内，在所有tcp连接前检查该url是否有ssrf攻击威胁。
     *
     * <p>请通过如下方式使用该接口：<blockquote><pre>
     *     ssrfChecker.startNetHookWithExpirationCache(url);
     *     try{
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     }
     * </pre></blockquote>
     *
     * @param url 待访问的url
     */
    void startNetHookWithExpirationCache(String url);

    /**
     * 在不链接的情况下，判断url是否可以安全地被访问
     *
     * @param url         待访问的url
     * @return true 表示可以安全地被访问
     */
    boolean checkUrlWithoutConnection(String url);
}
