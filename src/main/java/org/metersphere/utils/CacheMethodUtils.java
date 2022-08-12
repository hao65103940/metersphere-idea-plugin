package org.metersphere.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fit2cloudzhao
 * @date 2022/8/12 10:38
 * @description:
 */
public class CacheMethodUtils {

    private static final String METHOD_KEY = "method_key";

    private static final ConcurrentHashMap<String, Object> CONTEXT_DATA = new ConcurrentHashMap<>(16);

    public static <T> void addData(T data) {
        CONTEXT_DATA.put(METHOD_KEY, data);
    }

    public static void removeAll() {
        CONTEXT_DATA.clear();
    }

    public static <T> T getData() {
        return (T) CONTEXT_DATA.get(METHOD_KEY);
    }


}
