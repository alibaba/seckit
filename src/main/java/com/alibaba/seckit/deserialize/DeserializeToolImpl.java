package com.alibaba.seckit.deserialize;

import sun.misc.ObjectInputFilter;
import sun.misc.SharedSecrets;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

// Keep this implementation in sync with src/main/java9/com/alibaba/seckit/deserialize/DeserializeToolImpl.java.
public class DeserializeToolImpl implements DeserializeTool {

    private static final ObjectInputFilter DEFAULT_FILTER = createFilter(null, null);

    @Override
    public ObjectInputStream newObjectInputStream(InputStream in, String[] allowedPatterns, Class<?>[] allowedClasses) throws IOException {
        ObjectInputFilter filter = newObjectInputFilter(allowedPatterns, allowedClasses);
        ObjectInputStream ois = new ObjectInputStream(in);
        SharedSecrets.getJavaOISAccess().setObjectInputFilter(ois, filter);
        return ois;
    }

    public ObjectInputFilter newObjectInputFilter(String[] allowedPatterns, Class<?>[] allowedClasses) {
        if ((allowedPatterns == null || allowedPatterns.length == 0)
                && (allowedClasses == null || allowedClasses.length == 0)) {
            return DEFAULT_FILTER;
        }
        return createFilter(allowedPatterns, allowedClasses);
    }

    private static ObjectInputFilter createFilter(String[] allowedPatterns, Class<?>[] allowedClasses) {
        return new WhiteListObjectInputFilter(ObjectInputFilter.Config.createFilter(
                DeserializeUtils.generateFilterPattern(allowedPatterns, allowedClasses)
        ));
    }
}
