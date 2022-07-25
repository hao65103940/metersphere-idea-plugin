package org.metersphere.resolver;

import com.alibaba.fastjson.JSONObject;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import de.plushnikov.intellij.lombok.util.PsiAnnotationUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fit2cloudzhao
 * @date 2022/7/15 11:33
 * @Description:
 */
public class CommonAnnotationImpl extends CustomAnnotationHolderFactory implements CustomAnnotationHolder {


    @Override
    public void buildJsonObject(PsiAnnotation psiAnnotation, JSONObject jsonObject, PsiField psiField) {

        String fieldName = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "fieldName", String.class);
        Integer length = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "length", Integer.class);
        String message = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "message", String.class);
        Boolean isNull = PsiAnnotationUtil.getAnnotationValue(psiAnnotation, "isNull", Boolean.class);
        // 字段描述
        if (StringUtils.isNotBlank(fieldName)) {
            jsonObject.put("mock", new JSONObject() {{
                put("mock", fieldName);
            }});
        }
        // 描述
        if (StringUtils.isNotBlank(message)) {
            String description = jsonObject.getString("description");
            if (StringUtils.isNotBlank(description)) {
                jsonObject.put("description", String.join(";", description, message));
            } else {
                jsonObject.put("description", message);
            }
        }
        // 最大长度
        if (length > 0) {
            jsonObject.put("maxLength", length);
        }
        // 判断是否为空,true是，false必填
        if (!isNull) {
            addGenericList(psiField.getName());
        }
    }
}
