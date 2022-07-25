package org.metersphere.resolver;

import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;

/**
 * 仅针对富国定制开发自定义注解.
 *
 * @author fit2cloudzhao
 * @date 2022/7/15 10:12
 * @Description:
 */
public interface CustomAnnotationHolder {


    /**
     * 构建解析自定义注释
     * @param psiAnnotation
     * @param jsonObject
     * @param psiField
     */
   void buildJsonObject(PsiAnnotation psiAnnotation, JSONObject jsonObject, PsiField psiField);


}
