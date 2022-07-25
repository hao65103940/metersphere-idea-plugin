package org.metersphere.utils;

import com.intellij.openapi.diagnostic.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author fit2cloudzhao
 * @date 2022/7/19 17:06
 * @Description:
 */
public class ExceptionUtil {

    private static Logger logger = Logger.getInstance(ExceptionUtil.class);

    public static void logException(Throwable throwable) {
        logException(throwable, "");
    }

    public static void logException(Throwable throwable, String addition) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        String stackTrace = writer.getBuffer().toString();
        logger.error("插件运行失败, " + addition + "错误信息如下: " + stackTrace);
    }


    public static void handleSyntaxError(String code) throws RuntimeException {
        logger.error("您的代码可能存在语法错误, 无法为您生成代码, 参考信息: " + code);
    }
}
