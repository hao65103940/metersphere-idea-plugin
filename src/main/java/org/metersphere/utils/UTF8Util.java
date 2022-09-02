package org.metersphere.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class UTF8Util {
    public static String toUTF8String(String s) {
        if (StringUtils.isBlank(s)) {
            return "";
        }
        return new String(s.getBytes(StandardCharsets.UTF_8));
    }


    public static String subStringByStr(String str) {
        if (StringUtils.isNotBlank(str) && str.length() > 50) {
            // 防止接口报错，截取50个字符
            str = str.substring(0, 50);
        }
        return str;
    }
}
