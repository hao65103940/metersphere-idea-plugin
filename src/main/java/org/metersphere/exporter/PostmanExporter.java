package org.metersphere.exporter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.intellij.icons.AllIcons;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiMethodUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import de.plushnikov.intellij.lombok.util.PsiAnnotationUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.metersphere.AppSettingService;
import org.metersphere.constants.CommentTagEnum;
import org.metersphere.constants.HeadMappingConstants;
import org.metersphere.constants.PluginConstants;
import org.metersphere.constants.SpringMappingConstants;
import org.metersphere.model.PostmanModel;
import org.metersphere.model.PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean;
import org.metersphere.resolver.CustomAnnotationHolderFactory;
import org.metersphere.state.AppSettingState;
import org.metersphere.utils.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.metersphere.constants.PluginConstants.PACKAGETYPESMAP;


public class PostmanExporter implements IExporter {
    private final AppSettingService appSettingService = AppSettingService.getInstance();

    private static final Pattern RequestBodyPattern = Pattern.compile("RequestBody");
    private static final Pattern RequestPathPattern = Pattern.compile("PathVariable");
    private static final Pattern FormDataPattern = Pattern.compile("RequestParam");
    private static final Pattern MultiPartFormDataPattern = Pattern.compile("RequestPart");
    private static final List<String> FormDataAnnoPath = Lists.newArrayList("org.springframework.web.bind.annotation.RequestPart", "org.springframework.web.bind.annotation.RequestParam");

    private static final Pattern RequestAnyPattern = Pattern.compile("RequestBody|RequestParam|RequestPart");

    @Override
    public boolean export(PsiElement psiElement) {
        try {
            List<PsiJavaFile> files = new LinkedList<>();
            getFile(psiElement, files);
            files = files.stream().filter(f ->
                    f instanceof PsiJavaFile
            ).collect(Collectors.toList());
            if (files.size() == 0) {
                Messages.showInfoMessage("No java file detected! please change your search root", infoTitle());
                return false;
            }
            List<PostmanModel> postmanModels = transform(files, true, false, appSettingService.getState());
            if (postmanModels.size() == 0) {
                Messages.showInfoMessage("No java api was found! please change your search root", infoTitle());
                return false;
            }
            FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            fileChooserDescriptor.setDescription("Choose the location you want to export");
            FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(fileChooserDescriptor, null, null);
            VirtualFile file[] = fileChooserDialog.choose(psiElement.getProject(), new VirtualFile[]{});
            if (file.length == 0) {
                Messages.showInfoMessage("No directory selected", infoTitle());
                return false;
            } else {
                Messages.showInfoMessage(String.format("will be exported to %s", file[0].getCanonicalPath() + "/postman.json"), infoTitle());
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file[0].getCanonicalPath() + "/postman.json"));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("item", postmanModels);
            JSONObject info = new JSONObject();
            info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            info.put("name", psiElement.getProject().getName());
            info.put("description", "exported at " + dateTime);
            jsonObject.put("info", info);
            bufferedWriter.write(new Gson().toJson(jsonObject));
            bufferedWriter.flush();
            bufferedWriter.close();
            return true;
        } catch (Exception e) {
            logger.error("MeterSphere plugin export to postman error start......");
            logger.error(e);
            logger.error("MeterSphere plugin export to postman error end......");
            return false;
        }
    }

    @NotNull
    public String infoTitle() {
        return PluginConstants.MessageTitle.Info.name();
    }

    public List<PsiJavaFile> getFile(PsiElement psiElement, List<PsiJavaFile> files) {
        if (psiElement instanceof PsiDirectory) {
            Arrays.stream(psiElement.getChildren()).forEach(p -> {
                if (p instanceof PsiJavaFile) {
                    ProgressUtil.show(("Found controller: " + ((PsiJavaFile) p).getName()));
                    files.add((PsiJavaFile) p);
                } else if (p instanceof PsiDirectory) {
                    getFile(p, files);
                }
            });
        } else {
            if (psiElement.getContainingFile() instanceof PsiJavaFile) {
                ProgressUtil.show(("Found controller: " + (psiElement.getContainingFile()).getName()));
                files.add((PsiJavaFile) psiElement.getContainingFile());
            }
        }
        return files;
    }

    Logger logger = Logger.getInstance(PostmanExporter.class);

    public List<PostmanModel> transform(List<PsiJavaFile> files, boolean withBasePath, boolean withJsonSchema, AppSettingState state) {
        List<PostmanModel> models = new LinkedList<>();
        files.forEach(f -> {
            logger.info(f.getText() + "...........");
            PsiClass controllerClass = PsiTreeUtil.findChildOfType(f, PsiClass.class);
            if (controllerClass != null) {
                PostmanModel model = new PostmanModel();
                if (!f.getName().endsWith(".java")) return;
                PsiClass[] classes = f.getClasses();
                if (classes.length == 0)
                    return;
                model.setName(getJavaDocName(f.getClasses()[0], state));
                model.setDescription(model.getName());
                List<PostmanModel.ItemBean> itemBeans = new LinkedList<>();
                boolean isRequest = false;
                boolean restController = false;
                String basePath = "";

                //从注解里面找 RestController 和 RequestMapping 来确定请求头和 basepath
                PsiModifierList controllerModi = PsiTreeUtil.findChildOfType(controllerClass, PsiModifierList.class);
                if (controllerModi != null) {
                    Collection<PsiAnnotation> annotations = PsiTreeUtil.findChildrenOfType(controllerModi, PsiAnnotation.class);
                    if (annotations.size() > 0) {
                        Map<String, Boolean> r = containsAnnotation(annotations);
                        if (r.get("rest") || r.get("general")) {
                            isRequest = true;
                        }
                        if (r.get("rest")) {
                            restController = true;
                        }
                    }
                }

                if (isRequest) {
                    List<PsiAnnotation> annotations = PsiTreeUtil.findChildrenOfType(controllerModi, PsiAnnotation.class).stream().filter(a -> a.getQualifiedName().contains("RequestMapping")).collect(Collectors.toList());
                    PsiAnnotation requestMappingA = annotations.size() > 0 ? annotations.get(0) : null;
                    if (requestMappingA != null) {
                        basePath = PsiAnnotationUtil.getAnnotationValue(requestMappingA, String.class);
                        if (StringUtils.isNotBlank(basePath)) {
                            if (basePath.startsWith("/"))
                                basePath = basePath.replaceFirst("/", "");
                        } else {
                            basePath = "";
                        }
                    }
                    if (StringUtils.isNotBlank(state.getContextPath())) {
                        if (StringUtils.isNotBlank(basePath))
                            basePath = state.getContextPath().replaceFirst("/", "") + "/" + basePath;
                        else
                            basePath = state.getContextPath().replaceFirst("/", "");
                    }

                    Collection<PsiMethod> methodCollection = PsiTreeUtil.findChildrenOfType(controllerClass, PsiMethod.class);
                    Iterator<PsiMethod> methodIterator = methodCollection.iterator();
                    while (methodIterator.hasNext()) {
                        PsiMethod e1 = methodIterator.next();
                        //注解
                        Optional<PsiAnnotation> mapO = findMappingAnn(e1, PsiAnnotation.class);


                        if (mapO.isPresent()) {
                            PostmanModel.ItemBean itemBean = new PostmanModel.ItemBean();
                            //方法名称
                            itemBean.setName(getJavaDocName(e1, state));
                            PostmanModel.ItemBean.RequestBean requestBean = new PostmanModel.ItemBean.RequestBean();
                            //请求类型
                            requestBean.setMethod(getMethod(mapO.get()));
                            if (requestBean.getMethod().equalsIgnoreCase("Unknown Method")) {
                                //MessageMapping 等不是 rest 接口
                                isRequest = false;
                                continue;
                            }

                            Map<String, String> paramJavaDoc = getParamMap(e1, state);
                            //url
                            PostmanModel.ItemBean.RequestBean.UrlBean urlBean = new PostmanModel.ItemBean.RequestBean.UrlBean();

                            urlBean.setHost("{{" + e1.getProject().getName() + "}}");
                            String urlStr = Optional.ofNullable(getUrlFromAnnotation(e1)).orElse("");
                            // 把方法上的url缓存起来
                            CacheMethodUtils.addData(urlStr);

                            urlBean.setPath(getPath(urlStr, basePath));
                            urlBean.setQuery(getQuery(e1, requestBean, paramJavaDoc));
                            urlBean.setVariable(getVariable(urlBean.getPath(), paramJavaDoc));

                            String rawPre = (StringUtils.isNotBlank(basePath) ? "/" + basePath : "");
                            if (withBasePath) {
                                String cp = StringUtils.isNotBlank(state.getContextPath()) ? "{{" + e1.getProject().getName() + "}}" + "/" + state.getContextPath() : "{{" + e1.getProject().getName() + "}}";
                                urlBean.setRaw(cp + rawPre + (urlStr.startsWith("/") ? urlStr : "/" + urlStr));
                            } else {
                                urlBean.setRaw(rawPre + (urlStr.startsWith("/") ? urlStr : "/" + urlStr));
                            }
                            requestBean.setUrl(urlBean);
                            ProgressUtil.show((String.format("Found controller: %s api: %s", f.getName(), urlBean.getRaw())));
                            //header
                            List<PostmanModel.ItemBean.RequestBean.HeaderBean> headerBeans = new ArrayList<>();
                            if (restController) {
                                addRestHeader(headerBeans);
                            } else {
                                addFormHeader(headerBeans);
                            }
                            PsiElement headAn = findModifierInList(e1.getModifierList(), "headers");
                            PostmanModel.ItemBean.RequestBean.HeaderBean headerBean = new PostmanModel.ItemBean.RequestBean.HeaderBean();
                            if (headAn != null) {
                                String headerStr = PsiAnnotationUtil.getAnnotationValue((PsiAnnotation) headAn, "headers", String.class);
                                if (StringUtils.isNotBlank(headerStr)) {
                                    headerBean.setKey(headerStr.split("=")[0]);
                                    headerBean.setValue(headerStr.split("=")[1]);
                                    headerBean.setType("text");
                                    headerBeans.add(headerBean);
                                } else {
                                    Collection<PsiNameValuePair> heaerNVP = PsiTreeUtil.findChildrenOfType(headAn, PsiNameValuePair.class);
                                    Iterator<PsiNameValuePair> psiNameValuePairIterator = heaerNVP.iterator();
                                    while (psiNameValuePairIterator.hasNext()) {
                                        PsiNameValuePair ep1 = psiNameValuePairIterator.next();
                                        if (ep1.getText().contains("headers")) {
                                            Collection<PsiLiteralExpression> pleC = PsiTreeUtil.findChildrenOfType(headAn, PsiLiteralExpression.class);
                                            Iterator<PsiLiteralExpression> expressionIterator = pleC.iterator();
                                            while (expressionIterator.hasNext()) {

                                                PsiLiteralExpression ple = expressionIterator.next();
                                                String heaerItem = ple.getValue().toString();
                                                if (heaerItem.contains("=")) {
                                                    headerBean = new PostmanModel.ItemBean.RequestBean.HeaderBean();
                                                    headerBean.setKey(heaerItem.split("=")[0]);
                                                    headerBean.setValue(heaerItem.split("=")[1]);
                                                    headerBean.setType("text");
                                                    headerBeans.add(headerBean);
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                            requestBean.setHeader(removeDuplicate(headerBeans));
                            //body
                            PsiParameterList parameterList = e1.getParameterList();// 获取方法的参数集合
                            PostmanModel.ItemBean.RequestBean.BodyBean bodyBean = new PostmanModel.ItemBean.RequestBean.BodyBean();
                            for (PsiParameter pe : parameterList.getParameters()) {// 循环每个参数
                                PsiAnnotation[] pAt = pe.getAnnotations();// 拿到方法参数上的注解
                                if (ArrayUtils.isNotEmpty(pAt)
                                        // 必须包含MVC注解, 防止@Valid等注解影响判断
                                        && CollectionUtils.isNotEmpty(PsiAnnotationUtil.findAnnotations(pe, RequestAnyPattern))) { // RequestBody|RequestParam|RequestPart
                                    if (CollectionUtils.isNotEmpty(PsiAnnotationUtil.findAnnotations(pe, RequestBodyPattern))) {// RequestBody  暂且好像不支持 RequestParam
                                        bodyBean.setMode("raw");
                                        Map<String, String> rawMap = getRaw(pe.getName(), pe.getType(), pe.getProject());
                                        bodyBean.setRaw(rawMap.get("raw"));
                                        if (withJsonSchema) {
                                            bodyBean.setJsonSchema(rawMap.get("schema"));
                                        }
                                        PostmanModel.ItemBean.RequestBean.BodyBean.OptionsBean optionsBean = new PostmanModel.ItemBean.RequestBean.BodyBean.OptionsBean();
                                        PostmanModel.ItemBean.RequestBean.BodyBean.OptionsBean.RawBean rawBean = new PostmanModel.ItemBean.RequestBean.BodyBean.OptionsBean.RawBean();
                                        rawBean.setLanguage("json");
                                        optionsBean.setRaw(rawBean);
                                        bodyBean.setOptions(optionsBean);
                                        requestBean.setBody(bodyBean);
                                        //隐式
                                        addRestHeader(headerBeans);
                                    }
                                    if (CollectionUtils.isNotEmpty(PsiAnnotationUtil.findAnnotations(pe, MultiPartFormDataPattern))) {// RequestPart
                                        bodyBean.setMode("formdata");
                                        bodyBean.setFormdata(getFromdata(bodyBean.getFormdata(), pe, e1));
                                        requestBean.setBody(bodyBean);
                                        //隐式
                                        addMultipartHeader(headerBeans);
                                    }
                                } else {
                                    String javaType = pe.getType().getCanonicalText();
                                    if (!PluginConstants.simpleJavaType.contains(javaType) && !skipJavaTypes.contains(javaType)) {
                                        if (!StringUtils.equalsIgnoreCase(bodyBean.getMode(), "raw")) {
                                            //json 优先
                                            bodyBean.setMode("formdata");
                                            addFormHeader(headerBeans);
                                        }
                                        bodyBean.setFormdata(getFromdata(bodyBean.getFormdata(), pe, e1));
                                        requestBean.setBody(bodyBean);
                                    }
                                }
                            }
                            itemBean.setRequest(requestBean);
                            itemBean.setResponse(getResponseBean(itemBean, e1, withJsonSchema));
                            itemBeans.add(itemBean);
                        }
                    }
                    model.setItem(itemBeans);
                    if (isRequest)
                        models.add(model);
                }
            }
        });
        return models;
    }

    private List<PostmanModel.ItemBean.ResponseBean> getResponseBean(PostmanModel.ItemBean itemBean, PsiMethod e1, boolean withJsonSchema) {
        PostmanModel.ItemBean.ResponseBean responseBean = new PostmanModel.ItemBean.ResponseBean();
        responseBean.setName(itemBean.getName() + "-Example");
        responseBean.setStatus("OK");
        responseBean.setCode(200);
        responseBean.setHeader(getResponseHeader(itemBean));
        responseBean.set_postman_previewlanguage("json");
        responseBean.setOriginalRequest(JSONObject.parseObject(JSONObject.toJSONString(itemBean.getRequest()), PostmanModel.ItemBean.ResponseBean.OriginalRequestBean.class));
        Map<String, String> rawMap = getResponseBody(e1);
        responseBean.setBody(rawMap.get("raw"));
        if (withJsonSchema) {
            responseBean.setJsonSchema(rawMap.get("schema"));
        }
        return new ArrayList<>() {{
            add(responseBean);
        }};
    }

    private Map getResponseBody(PsiMethod e1) {
        String returnType = e1.getReturnType().getPresentableText();
        if (!"void".equalsIgnoreCase(returnType)) {
            return getRaw(returnType, e1.getReturnType(), e1.getProject());
        }
        return new HashMap();
    }

    private List<PostmanModel.ItemBean.ResponseBean.HeaderBeanXX> getResponseHeader(PostmanModel.ItemBean itemBean) {
        List<PostmanModel.ItemBean.ResponseBean.HeaderBeanXX> headers = new ArrayList<>();
        PostmanModel.ItemBean.ResponseBean.HeaderBeanXX h1 = new PostmanModel.ItemBean.ResponseBean.HeaderBeanXX();
        h1.setKey("date");
        h1.setName("date");
        h1.setValue("Thu, 02 Dec 2021 06:26:59 GMT");
        h1.setDescription("The date and time that the message was sent");
        headers.add(h1);

        PostmanModel.ItemBean.ResponseBean.HeaderBeanXX h2 = new PostmanModel.ItemBean.ResponseBean.HeaderBeanXX();
        h2.setKey("server");
        h2.setName("server");
        h2.setValue("Apache-Coyote/1.1");
        h2.setDescription("A name for the server");
        headers.add(h2);

        PostmanModel.ItemBean.ResponseBean.HeaderBeanXX h3 = new PostmanModel.ItemBean.ResponseBean.HeaderBeanXX();
        h3.setKey("transfer-encoding");
        h3.setName("transfer-encoding");
        h3.setValue("chunked");
        h3.setDescription("The form of encoding used to safely transfer the entity to the user. Currently defined methods are: chunked, compress, deflate, gzip, identity.");
        headers.add(h3);


        if (itemBean.getRequest().getHeader() != null && itemBean.getRequest().getHeader().stream().filter(s -> s.getKey().equalsIgnoreCase("Content-Type")).count() > 0) {
            PostmanModel.ItemBean.ResponseBean.HeaderBeanXX h4 = new PostmanModel.ItemBean.ResponseBean.HeaderBeanXX();
            h4.setKey("content-type");
            h4.setName("content-type");
            h4.setValue(itemBean.getRequest().getHeader().stream().filter(s -> s.getKey().equalsIgnoreCase("Content-Type")).findFirst().orElse(new PostmanModel.ItemBean.RequestBean.HeaderBean()).getValue());
            headers.add(h4);
        }
        return headers;

    }

    private Map<String, String> getParamMap(PsiMethod e1, AppSettingState state) {
        if (e1 == null)
            return new HashMap<>();
        if (!state.isJavadoc()) {
            return new HashMap<>();
        }
        Map<String, String> r = new HashMap<>();
        Collection<PsiDocToken> tokens = PsiTreeUtil.findChildrenOfType(e1.getDocComment(), PsiDocToken.class);
        if (tokens.size() > 0) {
            Iterator<PsiDocToken> iterator = tokens.iterator();
            while (iterator.hasNext()) {
                PsiDocToken token = iterator.next();
                if (token.getTokenType().toString().equalsIgnoreCase("DOC_TAG_NAME") && token.getText().equalsIgnoreCase("@param")) {
                    PsiDocToken paramEn = iterator.next();
                    PsiDocToken paramZh = iterator.next();
                    if (StringUtils.isNoneBlank(paramEn.getText(), paramZh.getText())) {
                        r.put(UTF8Util.toUTF8String(paramEn.getText()), UTF8Util.toUTF8String(paramZh.getText()));
                    }
                }
            }
        }
        return r;
    }

    private List<?> getVariable(List<String> path, Map<String, String> paramJavaDoc) {
        JSONArray variables = new JSONArray();
        for (String s : path) {
            if (s.startsWith(":")) {
                JSONObject var = new JSONObject();
                var.put("key", s.substring(1));
                var.put("description", paramJavaDoc.get(s.substring(1)));
                variables.add(var);
            }
        }
        if (variables.size() > 0)
            return variables;
        return null;
    }

    /**
     * 优先 javadoc，如果没有就方法名称
     *
     * @param e1
     * @return
     */
    private String getJavaDocName(PsiDocCommentOwner e1, AppSettingState state) {

        if (e1 == null)
            return "unknown module";
        List<String> resultList = new ArrayList<>();
        String apiName = e1.getName();
        resultList.add(apiName);
        if (!state.isJavadoc()) {
            return resultList.toString();
        }
        PsiElement[] children = e1.getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiComment) {
                Map<String, CommentTagEnum> commentTagMap = CommentTagEnum.allTagMap();
                PsiComment psiComment = (PsiComment) child;
                String text = psiComment.getText();
                if (text.startsWith("/**") && text.endsWith("*/")) {
                    String[] lines = text.replaceAll("\r", "").split("\n");
                    for (String line : lines) {
                        if (line.contains("/**") || line.contains("*/")) {
                            continue;
                        }
                        line = line.replaceAll("\\*", "").trim();
                        if (StringUtils.isBlank(line)) {
                            continue;
                        }
                        if (line.contains("@")) {
                            String[] tagValArray = line.split(" ");
                            String tag = "";
                            String tagVal = null;
                            if (tagValArray.length > 0) {
                                tag = tagValArray[0].trim();
                            }
                            if (tagValArray.length > 1) {
                                tagVal = line.substring(tag.length()).trim();
                            }
                            tag = tag.substring(1).split(":")[0];
                            //
                            if (commentTagMap.containsKey(tag.toLowerCase()) && StringUtils.isNotBlank(tagVal)) {
                                resultList.add(tagVal);
                            }
                        } else {
                            resultList.add(line);
                        }
                    }
                }
                // 如果存在JAVADOC，移除第一个
                if (resultList.size() > 1) {
                    resultList.remove(0);
                }
            }
        }
        return resultList.stream().collect(Collectors.joining(";"));
    }

    private List<PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean> getFromdata(List<FormDataBean> formdata, PsiParameter pe, PsiMethod psiMethod) {
        PsiAnnotation[] reqAnns = pe.getAnnotations();
        String value = Arrays.stream(reqAnns).filter(p -> FormDataAnnoPath.contains(p.getQualifiedName())).collect(Collectors.toList()).stream().findFirst().map(reqAnn -> PsiAnnotationUtil.getAnnotationValue(reqAnn, String.class)).orElse(pe.getName());
        if (formdata == null) {
            formdata = new ArrayList<>();
        }

        String type = getPeFormType(pe);
        if (type.equalsIgnoreCase("file")) {
            formdata.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(value, type, null, null));
        } else {
            List<PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean> dataBeans = getFormDataBeans(pe, psiMethod);
            formdata.addAll(dataBeans);
        }
        return formdata;
    }

    private List<PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean> getFormDataBeans(PsiParameter pe, PsiMethod psiMethod) {
        AppSettingState state = ApplicationManager.getApplication().getService(AppSettingService.class).getState();
        int maxDeepth = state.getDeepth();
        int curDeepth;
        PsiClass psiClass = JavaPsiFacade.getInstance(pe.getProject()).findClass(pe.getType().getCanonicalText(), GlobalSearchScope.allScope(pe.getProject()));
        List<PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean> param = new LinkedList<>();
        if (psiClass != null) {

            if (PluginConstants.simpleJavaType.contains(psiClass.getName())) {
                // 写在方法上的注释(直接在方法写java原生参数的情况)
                HashMap<String, String> methodParamDocMap = new HashMap<>();
                if (Objects.nonNull(psiMethod.getDocComment())) {
                    for (PsiDocTag tag : psiMethod.getDocComment().getTags()) {
                        PsiElement[] dataElements = tag.getDataElements();
                        if (dataElements.length >= 2) {
                            // 只处理标准Javadoc
                            methodParamDocMap.put(dataElements[0].getText(), dataElements[1].getText());
                        } else if (dataElements.length == 1) {
                            // 只写 xx参数, 没有注释的情况
                            methodParamDocMap.put(dataElements[0].getText(), "");
                        }
                    }
                    // 如果是简单类型, 则直接返回
                    param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(pe.getName(), "text", PluginConstants.simpleJavaTypeValue.get(psiClass.getQualifiedName()), methodParamDocMap.get(pe.getName())));
                    return param;
                }
            }

            PsiField[] fields = psiClass.getAllFields();
            curDeepth = 1;
            for (PsiField field : fields) {
                if (PluginConstants.simpleJavaType.contains(field.getType().getCanonicalText()))
                    param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(field.getName(), "text", PluginConstants.simpleJavaTypeValue.get(field.getType().getCanonicalText()), getJavaDocName(field, state)));
                    //这个判断对多层集合嵌套的数据类型
                else if (PsiTypeUtil.isCollection(field.getType())) {
                    getFormDataBeansCollection(param, field, field.getName() + "[0]", curDeepth, maxDeepth);
                } else if (field.getType().getCanonicalText().contains("[]")) {
                    getFormDataBeansArray(param, field, field.getName() + "[0]", curDeepth, maxDeepth);
                } else if (PsiTypeUtil.isMap(field.getType())) {
                    param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(field.getName() + ".key", "text", null, getJavaDocName(field, state)));
                } else {
                    getFormDataBeansPojo(param, field, field.getName(), curDeepth, maxDeepth);
                }
            }
        }

        return param;
    }

    private void getFormDataBeansMap(List<PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean> param, PsiField field, String prefixField, int curDeepth, int maxDeepth) {
        if (curDeepth == maxDeepth)
            return;
        prefixField = StringUtils.isNotBlank(prefixField) ? prefixField : "";
        param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(prefixField + "." + field.getName() + ".key", "text", null, null));
    }

    private void getFormDataBeansPojo(List<PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean> param, PsiField fatherField, String prefixField, int curDeepth, int maxDeepth) {
        if (curDeepth == maxDeepth)
            return;
        AppSettingState state = ApplicationManager.getApplication().getService(AppSettingService.class).getState();
        PsiClass psiClass = PsiTypeUtil.getPsiClass(fatherField.getType(), fatherField.getProject(), "pojo");
        prefixField = StringUtils.isNotBlank(prefixField) ? prefixField : "";
        if (psiClass != null) {
            if (PluginConstants.simpleJavaType.contains(psiClass.getName())) {
                param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(prefixField, "text", PluginConstants.simpleJavaTypeValue.get(psiClass.getName()), getJavaDocName(psiClass, state)));
            } else {
                //复杂对象类型遍历属性
                PsiField[] fields = psiClass.getAllFields();
                for (PsiField field : fields) {
                    if (skipJavaTypes.contains(field.getName().toLowerCase()))
                        continue;
                    if (PluginConstants.simpleJavaType.contains(field.getType().getCanonicalText()))//普通类型
                        param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(prefixField + "." + field.getName(), "text", PluginConstants.simpleJavaTypeValue.get(field.getType().getCanonicalText()), getJavaDocName(psiClass, state)));
                    else {
                        //容器
                        String pf = prefixField + "." + field.getName() + "[0]";
                        if (PsiTypeUtil.isCollection(field.getType())) {
                            getFormDataBeansCollection(param, field, pf, curDeepth + 1, maxDeepth);
                        } else if (field.getType().getCanonicalText().contains("[]")) {
                            //数组
                            getFormDataBeansArray(param, field, pf, curDeepth + 1, maxDeepth);
                        } else if (PsiTypeUtil.isMap(field.getType())) {
                            getFormDataBeansMap(param, field, field.getName(), curDeepth + 1, maxDeepth);
                        } else
                            getFormDataBeansPojo(param, field, pf, curDeepth + 1, maxDeepth);
                    }
                }
            }
        }
    }

    private void getFormDataBeansArray(List<PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean> param, PsiField fatherField, String prefixField, int curDeepth, int maxDeepth) {
        if (curDeepth == maxDeepth)
            return;
        AppSettingState state = ApplicationManager.getApplication().getService(AppSettingService.class).getState();
        PsiClass psiClass = PsiTypeUtil.getPsiClass(fatherField.getType(), fatherField.getProject(), "array");
        prefixField = StringUtils.isNotBlank(prefixField) ? prefixField : "";
        if (psiClass != null) {
            if (PluginConstants.simpleJavaType.contains(psiClass.getName())) {
                param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(prefixField, "text", PluginConstants.simpleJavaTypeValue.get(psiClass.getName()), getJavaDocName(psiClass, state)));
            } else {
                //复杂对象类型遍历属性
                PsiField[] fields = psiClass.getAllFields();
                for (PsiField field : fields) {
                    if (skipJavaTypes.contains(field.getName().toLowerCase()))
                        continue;
                    if (PluginConstants.simpleJavaType.contains(field.getType().getCanonicalText()))//普通类型
                        param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(prefixField + "." + field.getName(), "text", PluginConstants.simpleJavaTypeValue.get(field.getType().getCanonicalText()), getJavaDocName(field, state)));
                    else {
                        //容器
                        String pf = prefixField + "." + field.getName() + "[0]";
                        if (PsiTypeUtil.isCollection(field.getType())) {
                            getFormDataBeansCollection(param, field, pf, curDeepth + 1, maxDeepth);
                        } else if (field.getType().getCanonicalText().contains("[]")) {
                            //数组
                            getFormDataBeansArray(param, field, pf, curDeepth + 1, maxDeepth);
                        } else if (PsiTypeUtil.isMap(field.getType())) {
                            getFormDataBeansMap(param, field, field.getName(), curDeepth + 1, maxDeepth);
                        } else
                            getFormDataBeansPojo(param, field, pf, curDeepth + 1, maxDeepth);
                    }
                }
            }
        }
    }

    private void getFormDataBeansCollection(List<PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean> param, PsiField fatherField, String prefixField, int curDeepth, int maxDeepth) {
        if (curDeepth == maxDeepth)
            return;
        AppSettingState state = ApplicationManager.getApplication().getService(AppSettingService.class).getState();
        PsiClass psiClass = PsiTypeUtil.getPsiClass(fatherField, "collection");
        prefixField = StringUtils.isNotBlank(prefixField) ? prefixField : "";
        if (psiClass != null) {
            if (PluginConstants.simpleJavaType.contains(psiClass.getName())) {
                param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(prefixField, "text", PluginConstants.simpleJavaTypeValue.get(psiClass.getName()), getJavaDocName(psiClass, state)));
            } else {
                //复杂对象类型遍历属性
                PsiField[] fields = psiClass.getAllFields();
                for (PsiField field : fields) {
                    if (skipJavaTypes.contains(field.getName().toLowerCase()))
                        continue;
                    if (PluginConstants.simpleJavaType.contains(field.getType().getCanonicalText()))//普通类型
                        param.add(new PostmanModel.ItemBean.RequestBean.BodyBean.FormDataBean(prefixField + "." + field.getName(), "text", PluginConstants.simpleJavaTypeValue.get(field.getType().getCanonicalText()), getJavaDocName(psiClass, state)));
                    else {
                        //容器
                        String pf = prefixField + "." + field.getName() + "[0]";
                        if (PsiTypeUtil.isCollection(field.getType())) {
                            getFormDataBeansCollection(param, field, pf, curDeepth + 1, maxDeepth);
                        } else if (field.getType().getCanonicalText().contains("[]")) {
                            //数组
                            getFormDataBeansArray(param, field, pf, curDeepth + 1, maxDeepth);
                        } else if (PsiTypeUtil.isMap(field.getType())) {
                            getFormDataBeansMap(param, field, field.getName(), curDeepth + 1, maxDeepth);
                        } else
                            getFormDataBeansPojo(param, field, pf, curDeepth + 1, maxDeepth);
                    }
                }
            }
        } else {
            logger.error(fatherField.getContainingFile().getName() + ":" + fatherField.getName() + " cannot find psiclass");
        }
    }

    /**
     * 获取 @RequestPart 类型 form
     *
     * @param pe
     * @return
     */
    private String getPeFormType(PsiParameter pe) {
        if (pe.getType().getCanonicalText().contains("File")) {
            return "file";
        }
        return pe.getType().getCanonicalText();
    }

    public Optional<PsiAnnotation> findMappingAnn(PsiMethod e1, Class<PsiAnnotation> psiAnnotationClass) {
        Collection<PsiAnnotation> annotations = PsiTreeUtil.findChildrenOfType(e1, PsiAnnotation.class);
        return annotations.stream().filter(a -> a.getQualifiedName().contains("Mapping")).findFirst();
    }

    public List<PostmanModel.ItemBean.RequestBean.HeaderBean> removeDuplicate
            (List<PostmanModel.ItemBean.RequestBean.HeaderBean> headerBeans) {
        if (headerBeans != null && headerBeans.size() > 1) {
            headerBeans = headerBeans.stream().distinct().collect(Collectors.toList());
        }
        return headerBeans;
    }

    public List<String> getPath(String urlStr, String basePath) {
        String[] urls = urlStr.split("/");
        if (StringUtils.isNotBlank(basePath))
            urls = (basePath + "/" + urlStr).split("/");
        Pattern p = Pattern.compile("\\{(\\w+)\\}");
        return Arrays.stream(urls).map(s -> {
            Matcher m = p.matcher(s);
            while (m.find()) {
                s = ":" + m.group(1);
            }
            return s;
        }).filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.toList());
    }

    public void addFormHeader(List<PostmanModel.ItemBean.RequestBean.HeaderBean> headerBeans) {
        addHeader(headerBeans, "application/x-www-form-urlencoded");
    }

    public void addHeader(List<PostmanModel.ItemBean.RequestBean.HeaderBean> headerBeans, String contentType) {
        for (PostmanModel.ItemBean.RequestBean.HeaderBean headerBean : headerBeans) {
            if (headerBean.getKey().equalsIgnoreCase("Content-Type")) {
                headerBean.setKey("Content-Type");
                headerBean.setValue(contentType);
                headerBean.setType("text");
                return;
            }
        }
        PostmanModel.ItemBean.RequestBean.HeaderBean headerBean = new PostmanModel.ItemBean.RequestBean.HeaderBean();
        headerBean.setKey("Content-Type");
        headerBean.setValue(contentType);
        headerBean.setType("text");
        headerBeans.add(headerBean);
    }

    public void addRestHeader(List<PostmanModel.ItemBean.RequestBean.HeaderBean> headerBeans) {
        addHeader(headerBeans, "application/json");
    }

    public void addMultipartHeader(List<PostmanModel.ItemBean.RequestBean.HeaderBean> headerBeans) {
        addHeader(headerBeans, "multipart/form-data");
    }

    public List<?> getQuery(PsiMethod e1, PostmanModel.ItemBean.RequestBean requestBean, Map<String, String> paramJavaDoc) {
        List<JSONObject> r = new ArrayList<>();
        PsiParameterList parametersList = e1.getParameterList();
        PsiParameter[] parameter = parametersList.getParameters();
        if (requestBean.getMethod().equalsIgnoreCase("REQUEST") && parameter.length == 0) {
            requestBean.setMethod("GET");
        }
        for (PsiParameter psiParameter : parameter) {
            PsiAnnotation[] pAt = psiParameter.getAnnotations();
            if (ArrayUtils.isNotEmpty(pAt)) {
                //requestParam
                if (CollectionUtils.isNotEmpty(PsiAnnotationUtil.findAnnotations(psiParameter, FormDataPattern))) {
                    String javaType = psiParameter.getType().getCanonicalText();
                    if (PluginConstants.simpleJavaType.contains(javaType)) {
                        JSONObject stringParam = new JSONObject();
                        stringParam.put("key", getAnnotationName("RequestParam", "value", psiParameter));
                        stringParam.put("value", "");
                        stringParam.put("equals", true);
                        stringParam.put("description", paramJavaDoc.get(psiParameter.getName()));
                        r.add(stringParam);
                    } else {
                        /**
                         * todo 复杂的 requestParam 类型 /foo?id=1,2
                         * class foo{
                         *     private long[] id;
                         * }
                         */
                        if ("REQUEST".equalsIgnoreCase(requestBean.getMethod()))
                            requestBean.setMethod("POST");
                    }
                }
            } else {
                String javaType = psiParameter.getType().getCanonicalText();
                if (PluginConstants.simpleJavaType.contains(javaType)) {
                    JSONObject stringParam = new JSONObject();
                    stringParam.put("key", psiParameter.getName());
                    stringParam.put("value", "");
                    stringParam.put("equals", true);
                    stringParam.put("description", paramJavaDoc.get(psiParameter.getName()));
                    r.add(stringParam);
                } else {
                    if ("REQUEST".equalsIgnoreCase(requestBean.getMethod()))
                        requestBean.setMethod("POST");
                }
            }
        }
        return r;
    }

    /**
     * 获取 注解里面的 desc 比如 RequestParam("页数") int page
     *
     * @param annotationName
     * @param attributeName
     * @param psiParameter
     * @return
     */
    private String getAnnotationName(String annotationName, String attributeName, PsiParameter psiParameter) {
        PsiAnnotation annotations[] = psiParameter.getAnnotations();
        if (annotations != null && annotations.length > 0) {
            for (PsiAnnotation an : annotations) {
                if (an.getQualifiedName().contains(annotationName)) {
                    for (JvmAnnotationAttribute valuePair : an.getAttributes()) {
                        if (valuePair instanceof PsiNameValuePair) {
                            PsiNameValuePair valuePair1 = (PsiNameValuePair) valuePair;
                            if (valuePair1.getAttributeName().equalsIgnoreCase(attributeName)) {
                                return valuePair1.getLiteralValue();
                            }
                        }
                    }
                }
            }
        }
        return psiParameter.getName();
    }

    public String getMethod(PsiAnnotation mapAnn) {
        String method = PsiAnnotationUtil.getAnnotationValue(mapAnn, "method", String.class);
        if (StringUtils.isNotBlank(method)) {
            return method;
        }
        for (String s : SpringMappingConstants.mapList) {
            if (mapAnn.getQualifiedName().equalsIgnoreCase(s)) {
                method = s.replace("org.springframework.web.bind.annotation.", "").replace("Mapping", "").toUpperCase();
                if ("Request".equalsIgnoreCase(method)) {
                    return "GET";
                }
                return method;
            }
        }

        return "Unknown Method";
    }

    public static PsiElement findModifierInList(@NotNull PsiModifierList modifierList, String modifier) {
        PsiElement[] children = modifierList.getChildren();
        for (PsiElement child : children) {
            if (child.getText().contains(modifier)) return child;
        }
        return null;
    }

    public String getUrlFromAnnotation(PsiMethod method) {
        Collection<PsiAnnotation> mappingAn = PsiTreeUtil.findChildrenOfType(method, PsiAnnotation.class);
        Iterator<PsiAnnotation> mi = mappingAn.iterator();
        while (mi.hasNext()) {
            PsiAnnotation annotation = mi.next();
            if (annotation.getQualifiedName().contains("Mapping")) {
                Collection<String> mapUrls = PsiAnnotationUtil.getAnnotationValues(annotation, "value", String.class);
                if (CollectionUtils.isEmpty(mapUrls)) {
                    mapUrls = PsiAnnotationUtil.getAnnotationValues(annotation, "path", String.class);
                }
                if (mapUrls.size() > 0) {
                    return mapUrls.iterator().next();
                }
            }
        }
        return null;
    }

    public Map<String, Boolean> containsAnnotation(Collection<PsiAnnotation> annotations) {
        Map r = new HashMap();
        r.put("rest", false);
        r.put("general", false);
        Iterator<PsiAnnotation> it = annotations.iterator();
        while (it.hasNext()) {
            PsiAnnotation next = it.next();
            if (next.getQualifiedName().equalsIgnoreCase("org.springframework.web.bind.annotation.RestController"))
                r.put("rest", true);
            if (next.getQualifiedName().equalsIgnoreCase("org.springframework.stereotype.Controller"))
                r.put("general", true);
        }
        return r;
    }

    List<String> skipJavaTypes = new ArrayList<>() {{
        add("serialVersionUID".toLowerCase());
        add("optimisticLockVersion".toLowerCase());
        add("javax.servlet.http.HttpServletResponse");
        add("javax.servlet.http.HttpServletRequest");
    }};

    //PsiType:RequestDTO<UserInfnEntity>  当前这个应该不支持多个请求参数. 比如：private void test(User user, Test test){}
    public Map getRaw(String paramName, PsiType pe, Project project) {
        Map<String, Object> resultMap = new HashMap<>();
        String javaType = pe.getCanonicalText(); // 全限定名
        // 只要是泛型嵌套都为null
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(pe.getCanonicalText(), GlobalSearchScope.allScope(project)); // 根据全限定名找到类
        LinkedHashMap<String, Object> param = new LinkedHashMap<>(); // 总raw
        AppSettingState state = AppSettingService.getInstance().getState();
        int maxDeepth = state.getDeepth();
        int curDeepth = 1;
        JSONObject jsonSchema = new JSONObject();  // 总schema
        String schemaType = PsiTypeUtil.isCollection(pe) ? "array" : "object";
        jsonSchema.put("type", schemaType);
        jsonSchema.put("$id", "http://example.com/root.json");
        jsonSchema.put("title", "The Root Schema");
        jsonSchema.put("hidden", true);
        jsonSchema.put("$schema", "http://json-schema.org/draft-07/schema#");
        JSONObject properties = new JSONObject();
        JSONArray items = new JSONArray();
        String basePath = "#/properties";
        String baseItemsPath = "#/items";

        this.resolveByPsiType(paramName, pe, project, properties, resultMap, basePath, baseItemsPath, items, curDeepth, maxDeepth, param);

        if ("object".equalsIgnoreCase(schemaType)) {
            jsonSchema.put("properties", properties);
        } else {
            jsonSchema.put("items", items);
        }
        logger.info("jsonSchema:====>" + JSON.toJSONString(jsonSchema));
        resultMap.put("schema", JSONObject.toJSONString(jsonSchema, SerializerFeature.PrettyFormat));
        if (Optional.ofNullable(resultMap.get("raw")).isEmpty()) {
            resultMap.put("raw", JSONObject.toJSONString(param, SerializerFeature.PrettyFormat));
        }
        logger.info("resultMap===>" + JSON.toJSONString(resultMap));
        // 动作执行完之后之后清除缓存
        PsiTypeUtilExt.clearGeneric();

        return resultMap;
    }


    /**
     * @param paramName
     * @param psiFieldType
     * @param project
     * @param properties
     * @param resultMap
     * @param basePath
     * @param baseItemsPath
     * @param items
     * @param curDeepth
     * @param maxDeepth
     * @param param         raw
     */
    // RequestDTO<UserInfnEntity>  最外层
    private void resolveByPsiType(String paramName, PsiType psiFieldType, Project project, JSONObject properties, Map<String, Object> resultMap, String basePath, String baseItemsPath, JSONArray items, int curDeepth, int maxDeepth, LinkedHashMap<String, Object> param) {

        String peCanonicalText = psiFieldType.getCanonicalText(); // 全限定名
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(peCanonicalText, GlobalSearchScope.allScope(project));//  根据全限定明获取类,泛型都为空

        // 判断是否是基本类型  String,Integer .....
        if (PluginConstants.simpleJavaType.contains(peCanonicalText)) {
            resultMap.put("raw", PluginConstants.simpleJavaTypeValue.get(peCanonicalText));
            properties.put(paramName, createProperty(PluginConstants.simpleJavaTypeJsonSchemaMap.get(peCanonicalText), psiClass, null, basePath + "/" + paramName));
        }
        // 判断是否是数组 String[],Integer[]
        boolean isArrayType = psiFieldType instanceof PsiArrayType;
        if (isArrayType) {
            PsiArrayType psiArrayType = (PsiArrayType) psiFieldType;
            PsiType componentType = psiArrayType.getComponentType();
            resultMap.put("raw", getJSONArray(componentType, curDeepth, maxDeepth, items, baseItemsPath, project).toJSONString());
        }

        // map
        if (PsiTypeUtil.isMap(psiFieldType)) {
            getRawMap(param, psiFieldType, properties, basePath);
        }


        // 判断是否是引用类型(枚举/对象/List/Map)
        boolean isReferenceType = psiFieldType instanceof PsiClassReferenceType;
        if (isReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiFieldType;
            PsiClass resolveClass = psiClassReferenceType.resolve(); // RequestDTO
            if (resolveClass == null) {
                ExceptionUtil.handleSyntaxError(psiClassReferenceType.getCanonicalText());
            }
            // 枚举
            if (resolveClass.isEnum()) {
                // 暂且先不考虑
            }

            // 普通对象
//            if (psiClass != null) {
//                // 解析字段
//                Object generic = getGeneric(param, psiFieldType, paramName, properties, basePath, project, curDeepth, maxDeepth, items);
//                System.out.println(generic);
//                param.put("raw", generic);
//            }

            // List
            if (PsiTypeUtilExt.isPsiTypeFromList(psiFieldType, project) && ((PsiClassReferenceType) psiFieldType).getParameters().length == 1) {
                PsiType[] parameters = ((PsiClassReferenceType) psiFieldType).getParameters();
                PsiClass psiClass1 = PsiTypesUtil.getPsiClass(parameters[0]);
                String qualifiedName = psiClass1.getQualifiedName();
                if (PluginConstants.simpleJavaType.contains(qualifiedName)) {// String
                    JSONObject item = createProperty(PluginConstants.simpleJavaTypeJsonSchemaMap.get(qualifiedName), psiClass1, null, baseItemsPath + "/" + psiClass1.getName());
                    items.add(item);
                } else {
                    LinkedHashMap<String, Object> paramTemp = new LinkedHashMap<>();
                    Object generic = this.getGeneric(paramTemp, parameters[0], psiClass1.getName(), new JSONObject(), "#/items", project, curDeepth + 1, maxDeepth, new JSONArray());
                    JSONObject item = createProperty("object", psiClass1, null, baseItemsPath + "/" + psiClass1.getName());
                    item.put("properties", generic);
                    items.add(item);
                }
                param.put("raw", items);
                return;
            }
//
//            // Set
//            if (PsiTypeUtil.isPsiTypeFromSet(psiFieldType, project)) {
//                return getStructureAndCommentInfoByCollection(fieldName, commentInfo, typeNameFormat, fieldPrefix, level,
//                        structureAndCommentInfo, parameters, "Set<%s>", FieldType.SET);
//            }
//
//            // Collection 放在后面判断, 优先级低一些
//            if (PsiTypeUtil.isPsiTypeFromCollection(psiFieldType, project)) {
//                return getStructureAndCommentInfoByCollection(fieldName, commentInfo, typeNameFormat, fieldPrefix, level,
//                        structureAndCommentInfo, parameters, "Collection<%s>", FieldType.COLLECTION);
//            }
//
//            // 判断是否为 File
//            if (PsiTypeUtil.isPsiTypeFromXxx(psiFieldType, project, AnnotationHolder.QNAME_OF_MULTIPART_FILE)) {
//                structureAndCommentInfo.setFieldTypeCode(FieldType.FILE.getType());
//                structureAndCommentInfo.setOriginalFieldTypeCode(FieldType.FILE.getType());
//            }
//
//            // Map
            if (PsiTypeUtil.isMap(psiFieldType)) {
                getRawMap(param, psiFieldType, properties, basePath);
            }

            // 对象/泛型
            if ((psiClass != null || !PsiTypeUtilExt.isPsiTypeFromParameter(psiFieldType)) && !PsiTypeUtilExt.isPsiTypeFromList(psiFieldType, project)) { //
                Object generic = this.getGeneric(param, psiFieldType, paramName, properties, basePath, project, curDeepth, maxDeepth, items);
                param.put("raw", generic);
            }


        }

    }

    private JSONObject createProperty(String type, PsiClass pe, JSONArray items, String id) {
        JSONObject pro = new JSONObject();
        pro.put("type", type);
        String description = getJavaDocName(pe, ApplicationManager.getApplication().getService(AppSettingService.class).getState());
        if (StringUtils.isNotBlank(description) && !PluginConstants.simpleJavaType.contains(pe.getName()) && !StringUtils.equalsIgnoreCase(description, pe.getName())) {
            pro.put("description", description);
        }
        if (items != null) {
            pro.put("items", items);
        }
        pro.put("title", "The " + pe.getName() + " Schema");
        pro.put("$id", id);
        pro.put("hidden", true);
        setMockObj(pro);
        return pro;
    }

    private JSONObject createProperty(String type, PsiParameter pe, JSONArray items, String id) {
        JSONObject pro = new JSONObject();
        pro.put("type", type);
        String description = getJavaDocName(PsiTypesUtil.getPsiClass(pe.getType()), ApplicationManager.getApplication().getService(AppSettingService.class).getState());
        if (StringUtils.isNotBlank(description) && !PluginConstants.simpleJavaType.contains(pe.getName()) && !StringUtils.equalsIgnoreCase(description, pe.getName())) {
            pro.put("description", description);
        }
        if (items != null) {
            pro.put("items", items);
        }
        pro.put("title", "The " + pe.getName() + " Schema");
        pro.put("$id", id);
        pro.put("hidden", true);
        setMockObj(pro);
        return pro;
    }

    private void setMockObj(JSONObject pro) {
        JSONObject mock = new JSONObject();
        mock.put("mock", "");
        pro.put("mock", mock);
    }

    private void setMockObj(JSONObject pro, String value) {
        JSONObject mock = new JSONObject();
        mock.put("mock", value);
        pro.put("mock", mock);
    }

    /**
     * 构建类中属性的对象
     *
     * @param type
     * @param pe
     * @param items
     * @param id
     * @return
     */
    private JSONObject createProperty(String type, PsiField pe, JSONArray items, String id) {
        JSONObject pro = new JSONObject();
        pro.put("type", type);
        String description = getJavaDocName(pe, ApplicationManager.getApplication().getService(AppSettingService.class).getState());
        if (StringUtils.isNotBlank(description) && !PluginConstants.simpleJavaType.contains(pe.getName()) && !StringUtils.equalsIgnoreCase(description, pe.getName())) {
            pro.put("description", description);
        }


        if (items != null) {
            pro.put("items", items);
        }
        pro.put("title", "The " + pe.getName() + " Schema");
        pro.put("$id", id);
        pro.put("hidden", true);
        setMockObj(pro);

        // -----start 获取自定义类注解的值，目前只支持定义的几种注解-----
        getCustomAnnotationValue(pe, pro);
        // -----end-----

        // -----start 添加head属性值-----
        setHeadDescription(pe, pro);
        // -----end-----


        return pro;
    }


    /**
     * 获取自定义注解的字段值
     *
     * @param pe
     * @param pro
     */
    private void getCustomAnnotationValue(PsiField pe, JSONObject pro) {
        // 获取字段上所有注解
        PsiAnnotation[] annotations = pe.getAnnotations();
        for (PsiAnnotation psa : annotations) {
            CustomAnnotationHolderFactory.getAnnotation(psa, pro, pe);
        }
    }


    /**
     * 获取JSONField注解name
     *
     * @param psiField
     * @return
     */
    private String getNameValueByJsonField(PsiField psiField) {
        PsiAnnotation[] annotations = psiField.getAnnotations();
        for (PsiAnnotation psa : annotations) {
            if (StringUtils.contains(psa.getQualifiedName(), "com.alibaba.fastjson.annotation.JSONField")) {
                return PsiAnnotationUtil.getAnnotationValue(psa, "name", String.class);
            }
        }
        return psiField.getName();
    }


    /**
     * 设置head 属性值
     *
     * @param pe
     * @param jsonObject
     */
    private void setHeadDescription(PsiField pe, JSONObject jsonObject) {
        String valueByKey = HeadMappingConstants.getValueByKey(pe.getName());
        String description = jsonObject.getString("description");
        if (StringUtils.isNotBlank(valueByKey) && StringUtils.isBlank(description)) {
            jsonObject.put("description", valueByKey);
        }

        // 默认赋值为方法的url
        if ("reqTxnCode".equalsIgnoreCase(pe.getName())) {
            String methodUrl = CacheMethodUtils.getData();
            setMockObj(jsonObject, methodUrl);
        }

    }

    private void setRawMap(LinkedHashMap param, PsiField field) {
        LinkedHashMap fieldMap = new LinkedHashMap();
        getRawMap(fieldMap, field.getType(), null, null);
        param.put(field.getName(), fieldMap);
    }

    private void getRawMap(LinkedHashMap param, PsiType type, JSONObject properties, String parentPath) {
        PsiType[] types = ((PsiClassReferenceType) type).getParameters();
        if (types.length != 2) {
            param.put(new JSONObject(), new JSONObject());
            return;
        }
        String keyJavaType = ((PsiClassReferenceType) type).getParameters()[0].getPresentableText();
        String valueType = ((PsiClassReferenceType) type).getParameters()[1].getPresentableText();
        if (PluginConstants.simpleJavaType.contains(keyJavaType)) {
            if (PluginConstants.simpleJavaType.contains(valueType)) {
                param.put(PluginConstants.simpleJavaTypeValue.get(keyJavaType), PluginConstants.simpleJavaTypeValue.get(valueType));
            } else {
                param.put(PluginConstants.simpleJavaTypeValue.get(keyJavaType), new JSONObject());
            }
        } else {
            if (PluginConstants.simpleJavaType.contains(valueType)) {
                param.put(new JSONObject(), PluginConstants.simpleJavaTypeValue.get(valueType));
            } else {
                param.put(new JSONObject(), new JSONObject());
            }
        }
//        properties.put(field.getText(), createProperty(PluginConstants.simpleJavaTypeJsonSchemaMap.get(valueType), field, null, parentPath + "/" + field.getName()));
    }

    private PsiType getGenericClass(PsiType type, PsiClass psiClass) {
        PsiTypeParameter[] parameters = PsiTypesUtil.getPsiClass(type).getTypeParameters();
        PsiType[] allTypes = ((PsiClassReferenceType) type).getParameters();
        int index = 0;
        for (PsiTypeParameter p : parameters) {
            if (p.getName().equalsIgnoreCase(psiClass.getName())) {
                return allTypes[index];
            }
            index++;
        }
        return null;
    }

    /**
     * @param param
     * @param type       PsiType:RequestDTO<UserInfnEntity>
     * @param paramName
     * @param properties
     * @param parentPath
     * @param project
     */
    private Object getGeneric(LinkedHashMap<String, Object> param, PsiType type, String paramName, JSONObject properties, String parentPath, Project project, int curDeepth, int maxDeepth, JSONArray items) {

        // 有几个类型参数
        int genericCount = ((PsiClassReferenceType) type).getParameterCount();
        PsiClass outerClass = PsiTypesUtil.getPsiClass(type); // RequestDTO
        if (outerClass == null) {
            return null;
        }
        // 判断嵌套层级
        if (curDeepth == maxDeepth) {
            return new JSONObject();
        }

        // 解析类的泛型类的信息
        PsiTypeUtilExt.resolvePsiClassParameter((PsiClassType) type);
        // 获取类中所有属性包含继承的属性
        PsiField[] fields = PsiClassUtil.getAllFieldsByPsiClass(outerClass);
        for (PsiField f : fields) {
            PsiClass filedClass = PsiTypesUtil.getPsiClass(f.getType());
            if (filedClass == null && !f.getType().getCanonicalText().contains("[]")) {
                filedClass = JavaPsiFacade.getInstance(project).findClass(PACKAGETYPESMAP.get(f.getType().getCanonicalText()), GlobalSearchScope.allScope(project));
            }
            if (filedClass != null) { // java.lang.String
                if (PluginConstants.simpleJavaType.contains(filedClass.getQualifiedName())) {
                    param.put(f.getName(), PluginConstants.simpleJavaTypeValue.get(filedClass.getQualifiedName()));
                    JSONObject item = createProperty(PluginConstants.simpleJavaTypeJsonSchemaMap.get(filedClass.getQualifiedName()), f, null, parentPath + "/" + f.getName());
                    // 判断注解JSONField
                    String nameValueByJsonField = this.getNameValueByJsonField(f);
                    properties.put(nameValueByJsonField, item);
                } else {
                    // 其他类型

                    // 引用(对象/List/枚举/泛型)
                    boolean isReferenceType = f.getType() instanceof PsiClassReferenceType;
                    if (isReferenceType) {
                        // List<?>
                        if (PsiTypeUtilExt.isPsiTypeFromList(f.getType(), project) && ((PsiClassReferenceType) f.getType()).getParameters().length == 1) {
                            PsiType[] parameters = ((PsiClassReferenceType) f.getType()).getParameters();
                            PsiClass psiClass1 = PsiTypesUtil.getPsiClass(parameters[0]);// List<String>
                            JSONArray jsonArray = new JSONArray();
                            JSONObject array = createProperty("array", filedClass, jsonArray, "#/items/" + f.getName());
                            String qualifiedName = psiClass1.getQualifiedName();
                            if (PluginConstants.simpleJavaType.contains(qualifiedName)) {// String
                                JSONObject item = createProperty(PluginConstants.simpleJavaTypeJsonSchemaMap.get(qualifiedName), f, null, parentPath + "/" + f.getName());
                                jsonArray.add(item);
                                String nameValueByJsonField = this.getNameValueByJsonField(f);
                                properties.put(nameValueByJsonField, array);
                            } else {
                                LinkedHashMap<String, Object> paramTemp = new LinkedHashMap<>();
                                Object generic = this.getGeneric(paramTemp, parameters[0], psiClass1.getName(), new JSONObject(), "#/items", project, curDeepth + 1, maxDeepth, new JSONArray());
                                JSONObject item = createProperty("object", f, null, parentPath + "/" + f.getName());
                                item.put("properties", generic);
                                jsonArray.add(item);
                                String nameValueByJsonField = this.getNameValueByJsonField(f);
                                properties.put(nameValueByJsonField, array);
                                param.put("raw", jsonArray);
                            }
                        }

                        //  字段中 判断是否是泛型（包含T，E等的）
                        if (PsiTypeUtilExt.isPsiTypeFromParameter(f.getType())) {  //
                            // 获取泛型真实类型
                            PsiType realPsiType = PsiTypeUtilExt.getRealPsiType(f.getType(), project, null);
                            PsiClass bodyClass = PsiTypesUtil.getPsiClass(realPsiType);
                            // 也要判断类型
                            Object generic = this.getGeneric(new LinkedHashMap<>(), realPsiType, bodyClass.getName(), new JSONObject(), parentPath, project, curDeepth + 1, maxDeepth, new JSONArray());
                            JSONObject item = createProperty("object", bodyClass, null, parentPath + "/" + bodyClass.getName());
                            item.put("properties", generic);
                            String nameValueByJsonField = this.getNameValueByJsonField(f);
                            properties.put(nameValueByJsonField, item);
//                            param.put(f.getName(), getFields(bodyClass, 1, 2, pros, parentPath + "/" + paramName + "/" + f.getName()));
                            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(CustomAnnotationHolderFactory.getGenericList())) {
                                // 添加必填字段
                                item.put("required", CustomAnnotationHolderFactory.getGenericList());
                                // 添加完清除缓存
                                CustomAnnotationHolderFactory.removeGenericList();
                            }
                        }

                        // 对象
                        if (!PsiTypeUtilExt.isPsiTypeFromParameter(f.getType()) && !PsiTypeUtilExt.isPsiTypeFromList(f.getType(), project)) {
                            // 解析对象类型的 eg: User user
                            JSONObject item = createProperty("object", filedClass, null, "#/properties" + "/" + filedClass.getName());
                            JSONObject pros = new JSONObject();
                            Object generic = this.getGeneric(new LinkedHashMap<>(), f.getType(), filedClass.getName(), pros, parentPath, project, curDeepth + 1, maxDeepth, new JSONArray());
                            item.put("properties", generic);
                            String nameValueByJsonField = this.getNameValueByJsonField(f);
                            properties.put(nameValueByJsonField, item);
                        }
                    }
                }
            } else {

                // 判断是否是数组 String[],Integer[]
                boolean isArrayType = f.getType() instanceof PsiArrayType;
                if (isArrayType && f.getType().getCanonicalText().contains("[]")) {
                    PsiArrayType psiArrayType = (PsiArrayType) f.getType();
                    PsiType componentType = psiArrayType.getComponentType();
                    JSONArray jsonArray = new JSONArray();
                    JSONObject array = createProperty("array", f, jsonArray, "#/items/" + componentType);
                    String canonicalText = componentType.getCanonicalText();
                    if (PluginConstants.simpleJavaType.contains(canonicalText)) {
                        jsonArray.add(createProperty(PluginConstants.simpleJavaTypeJsonSchemaMap.get(canonicalText), f, null, parentPath + "/" + paramName));
                        String nameValueByJsonField = this.getNameValueByJsonField(f);
                        properties.put(nameValueByJsonField, array);
                    } else {
                        LinkedHashMap<String, Object> paramTemp = new LinkedHashMap<>();
                        PsiClass psiClass = PsiTypesUtil.getPsiClass(componentType);
                        Object generic = this.getGeneric(paramTemp, componentType, psiClass.getName(), new JSONObject(), "#/items", project, curDeepth + 1, maxDeepth, new JSONArray());
                        JSONObject item = createProperty("object", f, null, parentPath + "/" + f.getName());
                        item.put("properties", generic);
                        jsonArray.add(item);
                        String nameValueByJsonField = this.getNameValueByJsonField(f);
                        properties.put(nameValueByJsonField, array);
                        param.put("raw", jsonArray);
                    }
                }

                // map
                if (PsiTypeUtil.isMap(f.getType())) {
                    getRawMap(param, f.getType(), properties, parentPath);
                }
            }
        }
        return properties;
    }


    /**
     * 简单对象数组和复杂对象数组的
     *
     * @param field
     * @param items
     * @return
     */
    private JSONArray getJSONArray(PsiType field, int curDeepth, int maxDeepth, JSONArray items, String parentPath, Project project) {
        JSONArray r = new JSONArray();
        JSONObject item = new JSONObject();
        String qualifiedName = field.getDeepComponentType().getCanonicalText(); // String[]
        if (PluginConstants.simpleJavaType.contains(qualifiedName)) {
            r.add(PluginConstants.simpleJavaTypeValue.get(qualifiedName));
            PsiClass psiClass = PsiTypeUtil.getPsiClass(field.getDeepComponentType(), project, "");
            if (psiClass == null) {
                psiClass = JavaPsiFacade.getInstance(project).findClass(PACKAGETYPESMAP.get(field.getDeepComponentType().getCanonicalText()), GlobalSearchScope.allScope(project));
            }
            items.add(createProperty("array", psiClass, null, parentPath + "/"));
        } else {
            if (curDeepth == maxDeepth) {
                return new JSONArray();
            }
            PsiClass psiClass = PsiTypeUtil.getPsiClass(field, project, "array");
            if (psiClass != null) {
                item = createProperty("object", psiClass, null, parentPath + "/properties");
                items.add(item);
                r.add(getFields(psiClass, curDeepth + 1, maxDeepth, item, parentPath + "/properties"));
            } else {
                PsiType[] types = field instanceof PsiClassType ? ((PsiClassType) field).getParameters() : field instanceof PsiArrayType ? new PsiType[]{field} : new PsiType[]{};
                if (types.length == 1) {
                    PsiClass subClass = PsiTypeUtil.getPsiClass(types[0], project, "");
                    if (subClass == null) {
                        return r;
                    }
                    item = createProperty(Optional.ofNullable(PluginConstants.simpleJavaTypeJsonSchemaMap.get(subClass.getQualifiedName())).orElse("object"), subClass, null, parentPath + "/properties");
                    JSONObject pros = new JSONObject();
                    item.put("properties", pros);
                    items.add(item);
                    r.add(getFields(subClass, curDeepth + 1, maxDeepth, pros, parentPath + "/properties"));
                }
            }
        }

        return r;
    }

    public Object getFields(PsiClass context, int curDeepth, int maxDeepth, JSONObject properties, String basePath) {
        if (context == null)
            return "";
        if (PluginConstants.simpleJavaType.contains(context.getName())) {
            properties.put(context.getName(), createProperty(PluginConstants.simpleJavaTypeJsonSchemaMap.get(context.getName()), context, null, basePath + "/" + context.getName()));
            return PluginConstants.simpleJavaTypeValue.get(context.getName());
        }
        //复杂对象类型遍历属性
        PsiField[] fields = context.getAllFields();
        if (fields == null)
            return "";
        LinkedHashMap param = new LinkedHashMap();
        for (PsiField field : fields) {
            if (skipJavaTypes.contains(field.getName().toLowerCase()))
                continue;
            if (PluginConstants.simpleJavaType.contains(field.getType().getCanonicalText())) {
                param.put(field.getName(), PluginConstants.simpleJavaTypeValue.get(field.getType().getCanonicalText()));
                properties.put(field.getName(), createProperty(PluginConstants.simpleJavaTypeJsonSchemaMap.get(field.getType().getCanonicalText()), field, null, basePath + "/" + field.getName()));
            } else {
                //容器
                if (curDeepth == maxDeepth) {
                    properties.put(field.getName(), createProperty("object", field, null, basePath + "/" + field.getName()));
                    return new JSONObject();
                }
                if (PsiTypeUtil.isCollection(field.getType())) {
                    JSONArray items = new JSONArray();
                    properties.put("items", items);
                    //集合类型都是列表
                    getJSONArray(field.getType(), curDeepth + 1, maxDeepth, items, basePath + "/" + field.getName(), field.getProject());
                    param.put(field.getName(), getFields(PsiTypeUtil.getPsiClass(field, "collection"), curDeepth + 1, maxDeepth, new JSONObject(), basePath + "/" + field.getName()));
                } else if (field.getType().getCanonicalText().contains("[]")) {
                    //数组
                    JSONArray items = new JSONArray();
                    properties.put("items", items);
                    param.put(field.getName(), getJSONArray(field.getType(), curDeepth + 1, maxDeepth, items, basePath + "/" + field.getName(), field.getProject()));
                } else if (PsiTypeUtil.isMap(field.getType())) {
                    getRawMap(param, field.getType(), properties, basePath);
                } else {
                    param.put(field.getName(), getFields(PsiTypeUtil.getPsiClass(field, "pojo"), curDeepth + 1, maxDeepth, properties, basePath + "/" + field.getName()));
                }
            }
        }
        return param;
    }
}


