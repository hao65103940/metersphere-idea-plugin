package org.metersphere.resolver;

import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import de.plushnikov.intellij.lombok.util.PsiAnnotationUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fit2cloudzhao
 * @date 2022/7/15 13:21
 * @Description:
 */
public class CustNoAnnotationImpl extends CustomAnnotationHolderFactory implements CustomAnnotationHolder {


    @Override
    public void buildJsonObject(PsiAnnotation psiAnnotation, JSONObject jsonObject, PsiField psiField) {

        String message = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "message", String.class);
        Boolean isNull = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "isNull", Boolean.class);
        // 描述
        if (StringUtils.isNotBlank(message)) {
            String description = jsonObject.getString("description");
            if (StringUtils.isNotBlank(description)) {
                jsonObject.put("description", String.join(";", description, message));
            } else {
                jsonObject.put("description", message);
            }
        }
        // 判断是否为空,true是，false必填
        if (!isNull) {
            addGenericList(psiField.getName());
        }
    }
}
