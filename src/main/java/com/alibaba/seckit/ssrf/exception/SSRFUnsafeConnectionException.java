package com.alibaba.seckit.ssrf.exception;

import lombok.Getter;

import java.net.InetAddress;

/**
 * Throws {@link SSRFUnsafeConnectionException} when going to
 * connect a url that is vulnerable by SSRF Attack. Referring to Http
 * org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor, only
 * IOException and SecurityException could be handled by the exception
 * handler, and that is why this class extends the SecurityException.
 *
 * @author renyi.cry
  */
@Getter
public class SSRFUnsafeConnectionException extends SecurityException {

    private InetAddress blockedAddress;

    public SSRFUnsafeConnectionException(String message) {
        super(message);
    }

    public SSRFUnsafeConnectionException(String message, InetAddress blockedAddress) {
        super(message);
        this.blockedAddress = blockedAddress;
    }

}
