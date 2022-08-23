package org.metersphere.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import org.metersphere.constants.PluginConstants;

/**
 * @author fit2cloudzhao
 */
public class BaseTypeUtil {


    /**
     * 根据类型和全限定名判断是否为两种基础类型或 Object
     *
     * @param psiType 类型和全限定名
     * @return 是否为两种基础类型或 Object
     */
    public static boolean isBaseTypeOrObject(PsiType psiType) {
        if (psiType == null) {
            return false;
        }
        String qName = psiType.getCanonicalText(); // 全限定名 java.lang.String
        String typeName = psiType.getPresentableText();  // 类型全名  String
        return isBaseType(typeName) || typeIsObject(typeName);
    }

    /**
     * 根据类型和全限定名判断是否为两种基础类型或 Object
     *
     * @param psiClass 类型和全限定名
     * @return 是否为两种基础类型或 Object
     */
    public static boolean isBaseTypeOrObject(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        String typeName = psiClass.getName();
//        String qName = psiClass.getQualifiedName();
        return isBaseType(typeName) || typeIsObject(typeName);
    }

    /**
     * 根据类型判断是否为Java基本类型或 Object
     *
     * @param typeName 类型
     * @return 是否为Java基本类型或 Object
     */
    public static boolean isJavaBaseTypeOrObject(String typeName) {
        return isJavaBaseType(typeName) || typeIsObject(typeName);
    }

    /**
     * 根据类型判断是否为基本类型
     *
     * @param typeName 类型
     * @return
     */
    public static boolean isBaseType(String typeName) {
        return isJavaBaseType(typeName);
    }

    /**
     * 根据类型判断是否为Java基本类型
     *
     * @param typeName 类型
     * @return 是否为Java基本类型
     */
    public static boolean isJavaBaseType(String typeName) {
        if (typeName == null) {
            return false;
        }
        return PluginConstants.simpleJavaTypeValue.containsKey(typeName);
    }

    /**
     * 根据全限定名判断是否为其他基本类型
     *
     * @param qName 全限定名
     * @return 是否为其他基本类型
     */
//    public static boolean isOtherBaseType(String qName) {
//        if (qName == null) {
//            return false;
//        }
//        return OTHER_BASE_TYPE_MAP.containsKey(qName);
//    }

    /**
     * 获取Java 基本类型的默认值
     *
     * @param typeName 类型
     * @return 默认值
     */
//    public static String getJavaBaseTypeDefaultValStr(String typeName) {
//        if (typeName == null) {
//            return null;
//        }
//        TypeInfo typeInfo = JAVA_BASE_TYPE_MAP.get(typeName.toLowerCase());
//        if (typeInfo == null) {
//            return null;
//        }
//        return typeInfo.getDefaultValStr();
//    }

    /**
     * 根据全限定名获取默认值字符串
     *
     * @param qName 全限定名
     * @return 默认值字符串
     */
//    public static String getDefaultValStrByQname(String qName) {
//        TypeInfo typeInfo = getTypeInfoByQname(qName);
//        if (typeInfo == null) {
//            return null;
//        }
//        return typeInfo.getDefaultValStr();
//    }

    /**
     * 根据全限定名获取默认值对应的导入包信息
     *
     * @param qName 全限定名
     * @return 导入包信息
     */
//    public static String getDefaultValImportByQname(String qName) {
//        TypeInfo typeInfo = getTypeInfoByQname(qName);
//        if (typeInfo == null) {
//            return null;
//        }
//        return typeInfo.getImportStr();
//    }

    /**
     * 获取两种 base 类型的默认值
     *
     * @param psiType     psiType
     * @param commentInfo 参数
     * @return 默认值
     */
//    public static Object getBaseDefaultVal(PsiType psiType, CommentInfo commentInfo) {
//        if (psiType == null) {
//            return null;
//        }
//        String typeName = psiType.getPresentableText();
//        String qName = psiType.getCanonicalText();
//        TypeInfo typeInfo = JAVA_BASE_TYPE_MAP.get(typeName.toLowerCase());
//        if (typeInfo == null) {
//            typeInfo = OTHER_BASE_TYPE_MAP.get(qName);
//        }
//        if (typeInfo != null) {
//            return typeInfo.getDefaultValGetFn().apply(commentInfo);
//        }
//        return null;
//    }



//    private static TypeInfo getTypeInfoByQname(String qName) {
//        TypeInfo typeInfo = OTHER_BASE_TYPE_MAP.get(qName);
//        if (typeInfo == null) {
//            typeInfo = OTHER_INTERFACE_MAP.get(qName);
//        }
//        return typeInfo;
//    }

    private static boolean typeIsObject(String typeName) {
        return "Object".equals(typeName);
    }

//    private static boolean notUsingRandom() {
//        return PluginSettingHelper.getConfigItem(PluginSettingEnum.DEFAULT_NOT_RANDOM, false);
//    }

//    private static String randomDate(CommentInfo commentInfo) {
//        Date now = new Date();
//        String pattern = commentInfo.getSingleStr(MoreCommentTagEnum.JSON_FORMAT.getTag(), "yyyy-MM-dd'T'HH:mm:ss.SSS+0000");
//        pattern = commentInfo.getSingleStr(MoreCommentTagEnum.DATE_FORMAT.getTag(), pattern);
//        now.setTime(System.currentTimeMillis() + RandomUtils.nextLong(0, 86400000));
//        if (notUsingRandom()) {
//            now.setTime(1338182040520L);
//        }
//        return DateFormatUtils.format(now, pattern);
//    }
//
//    private static String randomString(CommentInfo commentInfo) {
//        String fieldDesc = commentInfo.getValue("");
//        Boolean random = commentInfo.getSingleBool(MoreCommentTagEnum.EXAMPLE_RANDOM.getTag(), false);
//        Boolean guid = commentInfo.getSingleBool(MoreCommentTagEnum.EXAMPLE_GUID.getTag(), false);
//        if (guid) {
//            if (notUsingRandom()) {
//                return "98100F81-C8D8-45F8-9658-F31F5DC693C2";
//            }
//            return UUID.randomUUID().toString().toUpperCase();
//        }
//        // 强指定随机或字段无任何描述时, 随机生成
//        //   否则使用描述加随机数字后缀组成示例值
//        if (random || StringUtils.isBlank(fieldDesc)) {
//            if (notUsingRandom()) {
//                return "HelloWorld";
//            }
//            int length = RandomUtils.nextInt(5, 20);
//            StringBuilder stringBuilder = new StringBuilder();
//            for (int i = 0; i < length; i++) {
//                stringBuilder.append(randomChar(false));
//            }
//            return stringBuilder.toString();
//        } else {
//            if (fieldDesc.contains(CommonConst.BREAK_LINE)) {
//                fieldDesc = fieldDesc.substring(0, fieldDesc.indexOf(CommonConst.BREAK_LINE));
//            }
//            if (notUsingRandom()) {
//                return fieldDesc;
//            }
//            return fieldDesc + RandomUtils.nextInt(1, 128);
//        }
//    }
//
//    private static long randomLong() {
//        if (notUsingRandom()) {
//            return 10L;
//        }
//        return RandomUtils.nextLong(10, 1000);
//    }
//
//    private static float randomFloat() {
//        if (notUsingRandom()) {
//            return 10f;
//        }
//        return RandomUtils.nextFloat(10, 100);
//    }
//
//    private static int randomInt() {
//        if (notUsingRandom()) {
//            return 1;
//        }
//        return RandomUtils.nextInt(1, 1024);
//    }
//
//    private static short randomShort() {
//        if (notUsingRandom()) {
//            return (short) 1;
//        }
//        return (short) RandomUtils.nextInt(1, 100);
//    }
//
//    private static byte randomByte() {
//        if (notUsingRandom()) {
//            return (byte) 1;
//        }
//        return RandomUtils.nextBytes(1)[0];
//    }
//
//    private static boolean randomBoolean() {
//        if (notUsingRandom()) {
//            return false;
//        }
//        return RandomUtils.nextBoolean();
//    }
//
//    private static double randomDouble() {
//        if (notUsingRandom()) {
//            return 50d;
//        }
//        return RandomUtils.nextDouble(50, 1000);
//    }
//
//    private static char randomChar() {
//        if (notUsingRandom()) {
//            return 'Q';
//        }
//        boolean en = RandomUtils.nextBoolean();
//        return randomChar(en);
//    }
//
//    private static char randomChar(boolean en) {
//        if (en) {
//            boolean upper = RandomUtils.nextBoolean();
//            if (upper) {
//                return (char) RandomUtils.nextInt(65, 90);
//            } else {
//                return (char) RandomUtils.nextInt(97, 122);
//            }
//        } else {
//            String str = "";
//            int highCode;
//            int lowCode;
//
//            Random random = new Random();
//
//            //B0 + 0~39(16~55) 一级汉字所占区
//            highCode = (176 + Math.abs(random.nextInt(39)));
//            //A1 + 0~93 每区有94个汉字
//            lowCode = (161 + Math.abs(random.nextInt(93)));
//
//            byte[] b = new byte[2];
//            b[0] = (Integer.valueOf(highCode)).byteValue();
//            b[1] = (Integer.valueOf(lowCode)).byteValue();
//
//            try {
//                str = new String(b, "GBK");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            return str.charAt(0);
//        }
//    }
//
//    @Data
//    public static class TypeInfo {
//
//        private String importStr;
//        private String defaultValStr;
//        private Function<CommentInfo, Object> defaultValGetFn;
//
//        public TypeInfo(String importStr, String defaultValStr, Function<CommentInfo, Object> defaultValGetFn) {
//            this.importStr = importStr;
//            this.defaultValStr = defaultValStr;
//            this.defaultValGetFn = defaultValGetFn;
//        }
//
//        public static TypeInfo of(String importStr, String defaultValStr, Object defaultVal) {
//            return of(importStr, defaultValStr, (commentInfo) -> defaultVal);
//        }
//
//        public static TypeInfo of(String defaultValStr, Function<CommentInfo, Object> defaultValGetFn) {
//            return of(null, defaultValStr, defaultValGetFn);
//        }
//
//        public static TypeInfo of(String importStr, String defaultValStr, Function<CommentInfo, Object> defaultValGetFn) {
//            return new TypeInfo(importStr, defaultValStr, defaultValGetFn);
//        }
//    }

}
