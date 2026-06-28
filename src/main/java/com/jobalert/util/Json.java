package com.jobalert.util;

import java.util.List;
import java.util.Map;

/**
 * Minimal navigation over parsed JSON (Map/List), independent of the Jackson version
 * Spring uses under the hood (works the same on Jackson 2 or 3).
 */
public final class Json {

    private Json() {}

    public static Object get(Object node, String key) {
        return node instanceof Map<?, ?> m ? m.get(key) : null;
    }

    /** Navigate nested object keys, e.g. str(j, "company", "display_name"). */
    public static String str(Object node, String... keys) {
        Object cur = node;
        for (String k : keys) {
            cur = get(cur, k);
        }
        return cur == null ? "" : cur.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> arr(Object node, String key) {
        Object v = get(node, key);
        return v instanceof List<?> l ? (List<Object>) l : List.of();
    }

    public static boolean bool(Object node, String key) {
        Object v = get(node, key);
        return v instanceof Boolean b ? b : false;
    }
}
