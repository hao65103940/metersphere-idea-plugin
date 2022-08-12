package org.metersphere.constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author fit2cloudzhao
 * @date 2022/8/11 13:57
 * @description:
 */
public class HeadMappingConstants {


    public static Map<String, String> headMapping = new HashMap<>() {{
        // public
        put("reqChlNo", "请求渠道编号");
        put("reqChlBrnNo", "请求分支渠道编号");
        put("reqTxnDate", "请求渠道日期");
        put("reqTxnTime", "请求渠道时间");
        put("reqJnlNo", "请求渠道流水号");
        put("reqTxnCode", "请求渠道交易码");
        put("mac", "mac地址");
        put("saveFlag", "报文安全标识");

        // request
        put("openId", "微信OPENID");
        put("ip", "ip地址");
        put("reqRemark", "保留字段");
        put("version", "报文版本号");
        put("browerType", "浏览器类型");
        put("deviceId", "设备编号");


        // response
        put("rspRemark", "保留字段");
        put("txnDate", "交易日期");
        put("txnTime", "交易时间");
        put("jnlNo", "流水号");
        put("resFlag", "响应类型");
        put("msgCode", "消息码");
        put("msgInfo", "消息内容");

    }};


    public static String getValueByKey(String key) {
        return headMapping.entrySet().stream().map(x ->
        {
            if (x.getKey().equalsIgnoreCase(key)) {
                return x.getValue();
            }
            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
    }


}
