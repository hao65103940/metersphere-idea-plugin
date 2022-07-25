package org.metersphere.resolver;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fit2cloudzhao
 * @date 2022/7/15 10:59
 * @Description:
 */
public class CustomAnnotationHolderFactory {


    private static Logger logger = Logger.getInstance(CustomAnnotationHolderFactory.class);

    // 暂时存放必填的属性名。
    public static final List<String> GENERIC_LIST = new ArrayList<>();

    // 支持自定义拓展
    private static final Map<String, CustomAnnotationHolder> CUSTOM_ANNOTATION_MAP = new HashMap<>();


    static {
        addHelper("CommonAnnotation", new CommonAnnotationImpl());
        addHelper("CustNoAnnotation", new CustNoAnnotationImpl());
        addHelper("CertNoAnnotation", new CertNoAnnotationImpl());
        addHelper("CertTypeAnnotation", new CertTypeAnnotationImpl());
    }

    public static void addHelper(String custom, CustomAnnotationHolder customAnnotationHolder) {
        CUSTOM_ANNOTATION_MAP.put(custom, customAnnotationHolder);
    }

    public static void getAnnotation(PsiAnnotation psiAnnotation, JSONObject jsonObject, PsiField psiField) {
        String substringAfterLast = StringUtils.substringAfterLast(psiAnnotation.getQualifiedName(), ".");
        if (CUSTOM_ANNOTATION_MAP.keySet().stream().anyMatch(x -> x.equalsIgnoreCase(substringAfterLast))) {
            CustomAnnotationHolder customAnnotationHolder = CUSTOM_ANNOTATION_MAP.get(substringAfterLast);
            if (customAnnotationHolder != null) {
                customAnnotationHolder.buildJsonObject(psiAnnotation, jsonObject, psiField);
            }
        }

    }


    // 添加缓存数据
    public static void addGenericList(String filedName) {
        GENERIC_LIST.add(filedName);
    }

    // 获取缓存数据
    public static List<String> getGenericList() {
        return GENERIC_LIST.stream().distinct().collect(Collectors.toList());
    }

    // 清空缓存数据
    public static void removeGenericList() {
        GENERIC_LIST.clear();
    }


}
