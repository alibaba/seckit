package com.alibaba.seckit.deserialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ObjectInputFilter;

// Keep this implementation in sync with src/main/java/com/alibaba/seckit/deserialize/WhiteListObjectInputFilter.java.
public class WhiteListObjectInputFilter implements ObjectInputFilter {

    private static final Logger log = LoggerFactory.getLogger(WhiteListObjectInputFilter.class);
    private final ObjectInputFilter delegate;

    public WhiteListObjectInputFilter(ObjectInputFilter delegate) {
        this.delegate = delegate;
    }

    @Override
    public Status checkInput(FilterInfo filterInfo) {
        Status ret = delegate.checkInput(filterInfo);
        if (ret != Status.UNDECIDED) {
            if (ret == Status.REJECTED) {
                log.warn("deserialize class rejected: {}", filterInfo.serialClass());
            }
            return ret;
        }

        Class<?> clazz = filterInfo.serialClass();
        if (clazz != null) {
            if (clazz.isArray()) {
                do {
                    // Arrays are decided based on the component type
                    clazz = clazz.getComponentType();
                } while (clazz.isArray());
            }
            if (clazz.isPrimitive()) {
                return Status.ALLOWED;
            }

            log.warn("deserialize class rejected: {}", filterInfo.serialClass());
            return Status.REJECTED;
        }

        return Status.UNDECIDED;
    }
}
