package com.alibaba.seckit.ssrf;

/**
 * SSRFResult
 *
 * @author jianbai
 */
public class SSRFResult {

    public static final String NO_DNS_IP = "-1";

    /**
     * 是否安全
     */
    private boolean safe;

    /**
     * Dns IP
     *
     * 如果解析了DNS, 但无DNS记录, 则返回NO_DNS_IP
     * @see #NO_DNS_IP
     */
    private String dnsIp;

    public SSRFResult() { }

    public static SSRFResult ofSafe() {
        return of(true, null);
    }

    public static SSRFResult ofNotSafe() {
        return of(false, null);
    }

    public static SSRFResult ofNotSafe(String dnsIp) {
        return of(false, dnsIp);
    }

    public static SSRFResult of(boolean safe, String dnsIp) {
        SSRFResult ssrfResult = new SSRFResult();
        ssrfResult.setSafe(safe);
        ssrfResult.setDnsIp(dnsIp);
        return ssrfResult;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public String getDnsIp() {
        return dnsIp;
    }

    public void setDnsIp(String dnsIp) {
        this.dnsIp = dnsIp;
    }
}
