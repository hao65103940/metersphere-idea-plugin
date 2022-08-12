package org.metersphere;

import org.apache.commons.lang3.StringUtils;
import org.metersphere.constants.HeadMappingConstants;

/**
 * @author fit2cloudzhao
 * @date 2022/8/11 15:10
 * @description:
 */
public class TestApi {

    public static void main(String[] args) {
        System.out.println(HeadMappingConstants.getValueByKey("txnTime"));
        if (StringUtils.isBlank(HeadMappingConstants.getValueByKey("txnTime"))) {
            System.out.println(true);
        }
    }
}
