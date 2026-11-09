package com.alibaba.seckit.jdbc.parser.oracle;

import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

import java.util.ArrayList;
import java.util.List;

/** Parses one complete (KEY=atom) or (KEY=(child)...(child)) descriptor. */
public final class NVFactory {
    private static final int MAX_INPUT_LENGTH = 65536;
    private static final int MAX_DEPTH = 64;

    public NVPair createNVPair(String descriptor) throws JdbcURLUnsafeException {
        if (descriptor == null || descriptor.length() > MAX_INPUT_LENGTH) {
            throw new JdbcURLUnsafeException("TNS descriptor is null or exceeds the length limit");
        }
        NVTokens cursor = new NVTokens(descriptor);
        NVPair root = readPair(cursor, 1);
        cursor.skipWhitespace();
        if (!cursor.atEnd()) {
            throw cursor.error("Trailing descriptor content");
        }
        return root;
    }

    private NVPair readPair(NVTokens cursor, int depth) throws JdbcURLUnsafeException {
        if (depth > MAX_DEPTH) {
            throw cursor.error("Descriptor nesting exceeds " + MAX_DEPTH);
        }
        cursor.expect('(');
        String name = cursor.readName();
        cursor.expect('=');
        cursor.skipWhitespace();

        NVPair pair;
        if (cursor.at('(')) {
            List<NVPair> children = new ArrayList<>();
            do {
                children.add(readPair(cursor, depth + 1));
                cursor.skipWhitespace();
            } while (cursor.at('('));
            pair = new NVPair(name, children);
        } else {
            pair = cursor.readAtom(name);
        }
        // Also rejects atom/child mixtures and adjacent or partially quoted atoms.
        cursor.expect(')');
        return pair;
    }
}
