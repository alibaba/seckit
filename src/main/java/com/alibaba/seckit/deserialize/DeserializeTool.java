package com.alibaba.seckit.deserialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public interface DeserializeTool {
    ObjectInputStream newObjectInputStream(InputStream in, String[] allowedPatterns, Class<?>[] allowedClasses) throws IOException;
}
