package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.ssrf.exception.SSRFUnsafeConnectionException;

/**
 * SSRF checker.
 *
 * @author renyi.cry
 */
public interface SSRFChecker {

    String[] ALLOWED_PROTOCOLS = {"http://", "https://"};

    /**
     * Enable SSRF checks before TCP connections on this thread until {@link #stop()} is called.
     *
     * <p>
     * Unsafe TCP connections are rejected with {@link SSRFUnsafeConnectionException}.
     *
     * <p>Usage:<blockquote><pre>
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
     * Stop hook checks enabled by {@link #start()}.
     *
     * @see #stopNetHookWithThreadLocal()
     * @deprecated since 1.2.15
     */
    @Deprecated
    void stop();


    /**
     * Enable SSRF checks before TCP connections on this thread until {@link #stopNetHookWithThreadLocal()} is called.
     *
     * <p>
     * Unsafe TCP connections are rejected with {@link SSRFUnsafeConnectionException}.
     *
     * <p>Usage:<blockquote><pre>
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
     * Enable SSRF checks before TCP connections on this thread until {@link #stopNetHookWithThreadLocal()} is called.
     *
     * <p>
     * Unsafe TCP connections are rejected with {@link SSRFUnsafeConnectionException}.
     *
     * <p>Usage:<blockquote><pre>
     *     try{
     *         ssrfChecker.startNetHookWithThreadLocal(url);
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     } finally {
     *         ssrfChecker.stopNetHookWithThreadLocal();
     *     }
     * </pre></blockquote>
     * @param url URL to access
     */
    void startNetHookWithThreadLocal(String url);

    /**
     * Stop hook checks enabled by {@link #startNetHookWithThreadLocal()} or {@link #startNetHookWithExpirationCache(String)}.
     *
     */
    void stopNetHookWithThreadLocal();

    /**
     * Enable time-limited SSRF checks for this URL before TCP connections.
     *
     * <p>Usage:<blockquote><pre>
     *     ssrfChecker.startNetHookWithExpirationCache(url);
     *     try{
     *         // Do connect ...
     *     } catch (SSRFUnsafeConnectionException e) {
     *         // unsafe connection
     *     }
     * </pre></blockquote>
     *
     * @param url URL to access
     */
    void startNetHookWithExpirationCache(String url);

    /**
     * Check whether a URL is safe to access without opening a connection.
     *
     * @param url         URL to access
     * @return true if the URL is safe to access
     */
    boolean checkUrlWithoutConnection(String url);
}
