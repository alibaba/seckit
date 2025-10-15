package com.alibaba.seckit.ssrf;

import com.alibaba.seckit.util.ExpiringSet;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

/**
 * {@link HostCachePolicy}
 *
 * @author renyi.cry
 * @date 17/12/24 下午4:08
 */
public class HostCachePolicy {

    /**
     * The default expiration (ms)
     */
    public static final long DEFAULT_EXPIRATION_MILLS = 20 * 1000;

    public static final int DEFAULT_MAX_SIZE = 100 * 1000;

    public static final ExpirationPolicy DEFAULT_EXPIRATION_POLICY = ExpirationPolicy.ACCESSED;

    /**
     * Get a configured {@link ExpiringSet}.
     *
     * @return
     */
    public static ExpiringSet<String> expiringSet() {

        ExpiringMap<String, Object> mapBuilder = ExpiringMap.builder()
                .expiration(DEFAULT_EXPIRATION_MILLS, TimeUnit.MILLISECONDS)
                .expirationPolicy(DEFAULT_EXPIRATION_POLICY)
                .maxSize(DEFAULT_MAX_SIZE)
                .build();

        return ExpiringSet.create(mapBuilder);
    }
}
