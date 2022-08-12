package org.metersphere;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.metersphere.constants.HeadMappingConstants;
import org.metersphere.constants.MSApiConstants;
import org.metersphere.model.PostmanModel;
import org.metersphere.state.*;
import org.metersphere.utils.CodingUtil;
import org.metersphere.utils.HttpFutureUtils;
import org.metersphere.utils.MSApiUtil;
import org.metersphere.utils.ProgressUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * @author fit2cloudzhao
 * @date 2022/8/10 14:43
 * @description:
 */
public class ExportMsApi {




    public static void main(String[] args) {
//        String str="[  {\n" +
//                "      \"item\": [\n" +
//                "        {\n" +
//                "          \"request\": {\n" +
//                "            \"method\": \"POST\",\n" +
//                "            \"header\": [\n" +
//                "              {\n" +
//                "                \"type\": \"text\",\n" +
//                "                \"value\": \"application/json\",\n" +
//                "                \"key\": \"Content-Type\"\n" +
//                "              }\n" +
//                "            ],\n" +
//                "            \"body\": {\n" +
//                "              \"mode\": \"raw\",\n" +
//                "              \"jsonSchema\": \"{\\n\\t\\\"hidden\\\":true,\\n\\t\\\"$schema\\\":\\\"http://json-schema.org/draft-07/schema#\\\",\\n\\t\\\"type\\\":\\\"object\\\",\\n\\t\\\"title\\\":\\\"The Root Schema\\\",\\n\\t\\\"properties\\\":{\\n\\t\\t\\\"head\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The RequestHeaderDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"reqTxnCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnCode\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqRemark\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqRemark Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqRemark\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnTime\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnTime Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnTime\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"openId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The openId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/openId\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"ip\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The ip Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/ip\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlBrnNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlBrnNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlBrnNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"version\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The version Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/version\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"deviceId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The deviceId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/deviceId\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"mac\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The mac Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/mac\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"saveFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The saveFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/saveFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"browerType\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The browerType Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/browerType\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqJnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqJnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqJnlNo\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/RequestHeaderDTO\\\"\\n\\t\\t},\\n\\t\\t\\\"body\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"description\\\":\\\"开立投顾服务主账户Req\\\",\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The OpenIAAccoReqDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"custNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The custNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/custNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"agreementId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"开户客户依次同意的协议确认留痕返回编号列表\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The agreementId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"maxLength\\\":200,\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/agreementId\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"required\\\":[\\n\\t\\t\\t\\t\\\"custNo\\\"\\n\\t\\t\\t],\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/OpenIAAccoReqDTO\\\"\\n\\t\\t}\\n\\t},\\n\\t\\\"$id\\\":\\\"http://example.com/root.json\\\"\\n}\",\n" +
//                "              \"options\": {\n" +
//                "                \"raw\": {\n" +
//                "                  \"language\": \"json\"\n" +
//                "                }\n" +
//                "              },\n" +
//                "              \"raw\": \"{\\n\\t\\\"raw\\\":{\\n\\t\\t\\\"head\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The RequestHeaderDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"reqTxnCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnCode\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqRemark\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqRemark Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqRemark\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnTime\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnTime Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnTime\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"openId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The openId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/openId\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"ip\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The ip Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/ip\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlBrnNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlBrnNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlBrnNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"version\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The version Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/version\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"deviceId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The deviceId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/deviceId\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"mac\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The mac Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/mac\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"saveFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The saveFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/saveFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"browerType\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The browerType Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/browerType\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqJnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqJnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqJnlNo\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/RequestHeaderDTO\\\"\\n\\t\\t},\\n\\t\\t\\\"body\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"description\\\":\\\"开立投顾服务主账户Req\\\",\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The OpenIAAccoReqDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"custNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The custNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/custNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"agreementId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"开户客户依次同意的协议确认留痕返回编号列表\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The agreementId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"maxLength\\\":200,\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/agreementId\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"required\\\":[\\n\\t\\t\\t\\t\\\"custNo\\\"\\n\\t\\t\\t],\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/OpenIAAccoReqDTO\\\"\\n\\t\\t}\\n\\t}\\n}\",\n" +
//                "              \"formdata\": [\n" +
//                "                {\n" +
//                "                  \"description\": \"MODEL_KEY_PREFIX\",\n" +
//                "                  \"type\": \"text\",\n" +
//                "                  \"value\": \"\",\n" +
//                "                  \"key\": \"MODEL_KEY_PREFIX\"\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"description\": \"NESTED_PATH_SEPARATOR\",\n" +
//                "                  \"type\": \"text\",\n" +
//                "                  \"value\": \"\",\n" +
//                "                  \"key\": \"NESTED_PATH_SEPARATOR\"\n" +
//                "                }\n" +
//                "              ]\n" +
//                "            },\n" +
//                "            \"url\": {\n" +
//                "              \"path\": [\n" +
//                "                \"user\",\n" +
//                "                \"account\",\n" +
//                "                \"insert\",\n" +
//                "                \"v1\"\n" +
//                "              ],\n" +
//                "              \"query\": [\n" +
//                "                \n" +
//                "              ],\n" +
//                "              \"host\": \"{{fgia-front}}\",\n" +
//                "              \"raw\": \"/user/account/insert/v1\"\n" +
//                "            }\n" +
//                "          },\n" +
//                "          \"response\": [\n" +
//                "            {\n" +
//                "              \"originalRequest\": {\n" +
//                "                \"method\": \"POST\",\n" +
//                "                \"header\": [\n" +
//                "                  {\n" +
//                "                    \"type\": \"text\",\n" +
//                "                    \"value\": \"application/json\",\n" +
//                "                    \"key\": \"Content-Type\"\n" +
//                "                  }\n" +
//                "                ],\n" +
//                "                \"body\": {\n" +
//                "                  \"mode\": \"raw\",\n" +
//                "                  \"options\": {\n" +
//                "                    \"raw\": {\n" +
//                "                      \"language\": \"json\"\n" +
//                "                    }\n" +
//                "                  },\n" +
//                "                  \"raw\": \"{\\n\\t\\\"raw\\\":{\\n\\t\\t\\\"head\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The RequestHeaderDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"reqTxnCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnCode\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqRemark\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqRemark Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqRemark\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnTime\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnTime Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnTime\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"openId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The openId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/openId\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"ip\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The ip Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/ip\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlBrnNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlBrnNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlBrnNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"version\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The version Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/version\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"deviceId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The deviceId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/deviceId\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"mac\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The mac Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/mac\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"saveFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The saveFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/saveFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"browerType\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The browerType Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/browerType\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqJnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqJnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqJnlNo\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/RequestHeaderDTO\\\"\\n\\t\\t},\\n\\t\\t\\\"body\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"description\\\":\\\"开立投顾服务主账户Req\\\",\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The OpenIAAccoReqDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"custNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The custNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/custNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"agreementId\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"开户客户依次同意的协议确认留痕返回编号列表\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The agreementId Schema\\\",\\n\\t\\t\\t\\t\\t\\\"maxLength\\\":200,\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/agreementId\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"required\\\":[\\n\\t\\t\\t\\t\\\"custNo\\\"\\n\\t\\t\\t],\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/OpenIAAccoReqDTO\\\"\\n\\t\\t}\\n\\t}\\n}\"\n" +
//                "                },\n" +
//                "                \"url\": {\n" +
//                "                  \"path\": [\n" +
//                "                    \"user\",\n" +
//                "                    \"account\",\n" +
//                "                    \"insert\",\n" +
//                "                    \"v1\"\n" +
//                "                  ],\n" +
//                "                  \"query\": [\n" +
//                "                    \n" +
//                "                  ],\n" +
//                "                  \"host\": \"{{fgia-front}}\",\n" +
//                "                  \"raw\": \"/user/account/insert/v1\"\n" +
//                "                }\n" +
//                "              },\n" +
//                "              \"_postman_previewlanguage\": \"json\",\n" +
//                "              \"code\": 200,\n" +
//                "              \"jsonSchema\": \"{\\n\\t\\\"hidden\\\":true,\\n\\t\\\"$schema\\\":\\\"http://json-schema.org/draft-07/schema#\\\",\\n\\t\\\"type\\\":\\\"object\\\",\\n\\t\\\"title\\\":\\\"The Root Schema\\\",\\n\\t\\\"properties\\\":{\\n\\t\\t\\\"head\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The ResponseHeaderDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"jnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The jnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/jnlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnCode\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"rspRemark\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The rspRemark Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/rspRemark\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"mac\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The mac Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/mac\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"txnTime\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The txnTime Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/txnTime\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"resFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The resFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/resFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"saveFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The saveFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/saveFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"msgInfo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The msgInfo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/msgInfo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqJnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqJnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqJnlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"txnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The txnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/txnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"msgCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The msgCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/msgCode\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/ResponseHeaderDTO\\\"\\n\\t\\t},\\n\\t\\t\\\"body\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"description\\\":\\\"开立投顾服务主账户Res\\\",\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The OpenIAAccoResDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"iaAcco\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The iaAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/iaAcco\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/OpenIAAccoResDTO\\\"\\n\\t\\t}\\n\\t},\\n\\t\\\"$id\\\":\\\"http://example.com/root.json\\\"\\n}\",\n" +
//                "              \"responseTime\": 0,\n" +
//                "              \"name\": \"开立投顾服务主账户（风险揭示书同意！）-Example\",\n" +
//                "              \"header\": [\n" +
//                "                {\n" +
//                "                  \"name\": \"date\",\n" +
//                "                  \"description\": \"The date and time that the message was sent\",\n" +
//                "                  \"value\": \"Thu, 02 Dec 2021 06:26:59 GMT\",\n" +
//                "                  \"key\": \"date\"\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"name\": \"server\",\n" +
//                "                  \"description\": \"A name for the server\",\n" +
//                "                  \"value\": \"Apache-Coyote/1.1\",\n" +
//                "                  \"key\": \"server\"\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"name\": \"transfer-encoding\",\n" +
//                "                  \"description\": \"The form of encoding used to safely transfer the entity to the uidentity.\",\n" +
//                "                  \"value\": \"chunked\",\n" +
//                "                  \"key\": \"transfer-encoding\"\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"name\": \"content-type\",\n" +
//                "                  \"value\": \"application/json\",\n" +
//                "                  \"key\": \"content-type\"\n" +
//                "                }\n" +
//                "              ],\n" +
//                "              \"body\": \"{\\n\\t\\\"raw\\\":{\\n\\t\\t\\\"head\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The ResponseHeaderDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"jnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The jnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/jnlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnCode\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"rspRemark\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The rspRemark Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/rspRemark\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"mac\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The mac Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/mac\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"txnTime\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The txnTime Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/txnTime\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"resFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The resFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/resFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"saveFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The saveFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/saveFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"msgInfo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The msgInfo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/msgInfo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqJnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqJnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqJnlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"txnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The txnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/txnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"msgCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The msgCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/msgCode\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/ResponseHeaderDTO\\\"\\n\\t\\t},\\n\\t\\t\\\"body\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"description\\\":\\\"开立投顾服务主账户Res\\\",\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The OpenIAAccoResDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"iaAcco\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The iaAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/iaAcco\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/OpenIAAccoResDTO\\\"\\n\\t\\t}\\n\\t}\\n}\",\n" +
//                "              \"status\": \"OK\"\n" +
//                "            }\n" +
//                "          ],\n" +
//                "          \"name\": \"开立投顾服务主账户（风险揭示书同意！）\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"request\": {\n" +
//                "            \"method\": \"GET\",\n" +
//                "            \"header\": [\n" +
//                "              {\n" +
//                "                \"type\": \"text\",\n" +
//                "                \"value\": \"application/json\",\n" +
//                "                \"key\": \"Content-Type\"\n" +
//                "              }\n" +
//                "            ],\n" +
//                "            \"url\": {\n" +
//                "              \"path\": [\n" +
//                "                \"user\",\n" +
//                "                \"account\",\n" +
//                "                \"select\",\n" +
//                "                \"v1\",\n" +
//                "                \":custNo\"\n" +
//                "              ],\n" +
//                "              \"query\": [\n" +
//                "                \n" +
//                "              ],\n" +
//                "              \"host\": \"{{fgia-front}}\",\n" +
//                "              \"variable\": [\n" +
//                "                {\n" +
//                "                  \"description\": \".\",\n" +
//                "                  \"key\": \"custNo\"\n" +
//                "                }\n" +
//                "              ],\n" +
//                "              \"raw\": \"/user/account/select/v1/{custNo}\"\n" +
//                "            }\n" +
//                "          },\n" +
//                "          \"response\": [\n" +
//                "            {\n" +
//                "              \"originalRequest\": {\n" +
//                "                \"method\": \"GET\",\n" +
//                "                \"header\": [\n" +
//                "                  {\n" +
//                "                    \"type\": \"text\",\n" +
//                "                    \"value\": \"application/json\",\n" +
//                "                    \"key\": \"Content-Type\"\n" +
//                "                  }\n" +
//                "                ],\n" +
//                "                \"url\": {\n" +
//                "                  \"path\": [\n" +
//                "                    \"user\",\n" +
//                "                    \"account\",\n" +
//                "                    \"select\",\n" +
//                "                    \"v1\",\n" +
//                "                    \":custNo\"\n" +
//                "                  ],\n" +
//                "                  \"query\": [\n" +
//                "                    \n" +
//                "                  ],\n" +
//                "                  \"host\": \"{{fgia-front}}\",\n" +
//                "                  \"variable\": [\n" +
//                "                    {\n" +
//                "                      \"description\": \".\",\n" +
//                "                      \"key\": \"custNo\"\n" +
//                "                    }\n" +
//                "                  ],\n" +
//                "                  \"raw\": \"/user/account/select/v1/{custNo}\"\n" +
//                "                }\n" +
//                "              },\n" +
//                "              \"_postman_previewlanguage\": \"json\",\n" +
//                "              \"code\": 200,\n" +
//                "              \"jsonSchema\": \"{\\n\\t\\\"hidden\\\":true,\\n\\t\\\"$schema\\\":\\\"http://json-schema.org/draft-07/schema#\\\",\\n\\t\\\"type\\\":\\\"object\\\",\\n\\t\\\"title\\\":\\\"The Root Schema\\\",\\n\\t\\\"properties\\\":{\\n\\t\\t\\\"head\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The ResponseHeaderDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"jnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The jnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/jnlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnCode\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"rspRemark\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The rspRemark Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/rspRemark\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"mac\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The mac Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/mac\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"txnTime\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The txnTime Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/txnTime\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"resFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The resFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/resFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"saveFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The saveFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/saveFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"msgInfo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The msgInfo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/msgInfo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqJnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqJnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqJnlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"txnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The txnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/txnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"msgCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The msgCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/msgCode\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/ResponseHeaderDTO\\\"\\n\\t\\t},\\n\\t\\t\\\"body\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"description\\\":\\\"开立投顾服务主账户Res\\\",\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The QryAccoInfoResDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"rec\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"array\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The List Schema\\\",\\n\\t\\t\\t\\t\\t\\\"items\\\":[\\n\\t\\t\\t\\t\\t\\t{\\n\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The rec Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"innerFwTradeAcco\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The innerFwTradeAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/innerFwTradeAcco\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"bankCode\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The bankCode Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/bankCode\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"openCost\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The openCost Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/openCost\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"accumCost\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The accumCost Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/accumCost\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"strategyAcco\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The strategyAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/strategyAcco\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"strategyId\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The strategyId Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/strategyId\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"custStrategyName\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The custStrategyName Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/custStrategyName\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"outerFwTradeAcco\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The outerFwTradeAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/outerFwTradeAcco\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/rec\\\"\\n\\t\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\t],\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/rec\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"custNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The custNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/custNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"recNum\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"description\\\":\\\"子账户与策略\\\",\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"integer\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The recNum Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/recNum\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"iaAcco\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The iaAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/iaAcco\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/QryAccoInfoResDTO\\\"\\n\\t\\t}\\n\\t},\\n\\t\\\"$id\\\":\\\"http://example.com/root.json\\\"\\n}\",\n" +
//                "              \"responseTime\": 0,\n" +
//                "              \"name\": \"查询客户账户信息-Example\",\n" +
//                "              \"header\": [\n" +
//                "                {\n" +
//                "                  \"name\": \"date\",\n" +
//                "                  \"description\": \"The date and time that the message was sent\",\n" +
//                "                  \"value\": \"Thu, 02 Dec 2021 06:26:59 GMT\",\n" +
//                "                  \"key\": \"date\"\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"name\": \"server\",\n" +
//                "                  \"description\": \"A name for the server\",\n" +
//                "                  \"value\": \"Apache-Coyote/1.1\",\n" +
//                "                  \"key\": \"server\"\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"name\": \"transfer-encoding\",\n" +
//                "                  \"description\": \"The form of encoding used to safely transfer the entity to the user. Currently defined methods are: chunked, compress, deflate, gzip, identity.\",\n" +
//                "                  \"value\": \"chunked\",\n" +
//                "                  \"key\": \"transfer-encoding\"\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"name\": \"content-type\",\n" +
//                "                  \"value\": \"application/json\",\n" +
//                "                  \"key\": \"content-type\"\n" +
//                "                }\n" +
//                "              ],\n" +
//                "              \"body\": \"{\\n\\t\\\"raw\\\":{\\n\\t\\t\\\"head\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The ResponseHeaderDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"jnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The jnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/jnlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnCode\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqTxnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqTxnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqTxnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"rspRemark\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The rspRemark Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/rspRemark\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"mac\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The mac Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/mac\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"txnTime\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The txnTime Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/txnTime\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"resFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The resFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/resFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"saveFlag\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The saveFlag Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/saveFlag\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqChlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqChlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqChlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"msgInfo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The msgInfo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/msgInfo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"reqJnlNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The reqJnlNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/reqJnlNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"txnDate\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The txnDate Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/txnDate\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"msgCode\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The msgCode Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/msgCode\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/ResponseHeaderDTO\\\"\\n\\t\\t},\\n\\t\\t\\\"body\\\":{\\n\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\\"description\\\":\\\"开立投顾服务主账户Res\\\",\\n\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t},\\n\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\\"title\\\":\\\"The QryAccoInfoResDTO Schema\\\",\\n\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\\"rec\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"array\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The List Schema\\\",\\n\\t\\t\\t\\t\\t\\\"items\\\":[\\n\\t\\t\\t\\t\\t\\t{\\n\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"object\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The rec Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\\"properties\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"innerFwTradeAcco\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The innerFwTradeAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/innerFwTradeAcco\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"bankCode\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The bankCode Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/bankCode\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"openCost\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The openCost Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/openCost\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"accumCost\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The accumCost Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/accumCost\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"strategyAcco\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The strategyAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/strategyAcco\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"strategyId\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The strategyId Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/strategyId\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"custStrategyName\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The custStrategyName Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/custStrategyName\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\\"outerFwTradeAcco\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"title\\\":\\\"The outerFwTradeAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/outerFwTradeAcco\\\"\\n\\t\\t\\t\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/rec\\\"\\n\\t\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\t],\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/items/rec\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"custNo\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The custNo Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/custNo\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"recNum\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"description\\\":\\\"子账户与策略\\\",\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"integer\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The recNum Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/recNum\\\"\\n\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\\"iaAcco\\\":{\\n\\t\\t\\t\\t\\t\\\"hidden\\\":true,\\n\\t\\t\\t\\t\\t\\\"mock\\\":{\\n\\t\\t\\t\\t\\t\\t\\\"mock\\\":\\\"\\\"\\n\\t\\t\\t\\t\\t},\\n\\t\\t\\t\\t\\t\\\"type\\\":\\\"string\\\",\\n\\t\\t\\t\\t\\t\\\"title\\\":\\\"The iaAcco Schema\\\",\\n\\t\\t\\t\\t\\t\\\"$id\\\":\\\"#/properties/iaAcco\\\"\\n\\t\\t\\t\\t}\\n\\t\\t\\t},\\n\\t\\t\\t\\\"$id\\\":\\\"#/properties/QryAccoInfoResDTO\\\"\\n\\t\\t}\\n\\t}\\n}\",\n" +
//                "              \"status\": \"OK\"\n" +
//                "            }\n" +
//                "          ],\n" +
//                "          \"name\": \"查询客户账户信息\"\n" +
//                "        }\n" +
//                "      ],\n" +
//                "      \"name\": \"客户\",\n" +
//                "      \"description\": \"客户\"\n" +
//                "    }]";
//
//        List<PostmanModel> postmanModels = JSON.parseArray(str, PostmanModel.class);
//        AppSettingState appSettingState=new AppSettingState();
//        appSettingState.setMeterSphereAddress("http://10.1.12.13");
//        appSettingState.setMeterSphereAddress("http://democenter.fit2cloud.com:46666");
//        appSettingState.setAccesskey("ChuQpMcUVJArepe0");
//        appSettingState.setSecretkey("5WI0YpvHEi9FA3L9");
//        MSWorkSpace msWorkSpace = new MSWorkSpace();
//        msWorkSpace.setId("1aaa1eb6-fb6c-11ec-b882-0242ac1e0a06");
//        msWorkSpace.setId("b14b7f33-e53e-11ec-9f6f-c6f37abf3d82");
//        msWorkSpace.setName("默认工作空间");
//        appSettingState.setWorkSpace(msWorkSpace);
//
//        MSProject msProject = new MSProject();
//        msProject.setId("1aaa6774-fb6c-11ec-b882-0242ac1e0a06");
//        msProject.setId("b14b2b54-e53e-11ec-9f6f-c6f37abf3d82");
//        msProject.setName("默认项目");
//        msProject.setVersionEnable(true);
//        appSettingState.setProject(msProject);
//
//
//        MSModule msModule = new MSModule();
//        msModule.setId("473e46a4-9732-41e5-b0f9-012160f3609e");
//        msModule.setId("b14b7f33-e53e-11ec-9f6f-c6f37abf3d82");
//        msModule.setName("导出测试");
//        msModule.setName("fg");
//        appSettingState.setModule(msModule);
//
//        MSProjectVersion msProjectVersion = new MSProjectVersion();
//        msProjectVersion.setId("203c4c20-fb6c-11ec-b882-0242ac1e0a06");
//        msProjectVersion.setId("bfd2fef4-e53e-11ec-9f6f-c6f37abf3d82");
//        msProjectVersion.setName("v1.0.0");
//        msProjectVersion.setLatest(true);
//        appSettingState.setProjectVersion(msProjectVersion);
//        appSettingState.setUpdateVersion(msProjectVersion);
//
////        appSettingState.setExportModuleName("test");
//        appSettingState.setModeId("fullCoverage");
//        appSettingState.setDeepth(4);
//        appSettingState.setJavadoc(true);
//        appSettingState.setCoverModule(true);
//        try {
//            File temp = File.createTempFile(UUID.randomUUID().toString(), null);
//            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temp));
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("item", postmanModels);
//            JSONObject info = new JSONObject();
//            info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
//            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//            String exportName = "fuguo-annation-support";
//            info.put("name", exportName);
//            info.put("description", "exported at " + dateTime);
//            info.put("_postman_id", UUID.randomUUID().toString());
//            jsonObject.put("info", info);
//            bufferedWriter.write(new Gson().toJson(jsonObject));
//            bufferedWriter.flush();
//            bufferedWriter.close();
//            uploadToServer(appSettingState,temp);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println(HeadMappingConstants.getValueByKey("rspRemark"));

    }


    public static boolean uploadToServer(AppSettingState appSettingState, File file) {
        ProgressUtil.show((String.format("Start to sync to MeterSphere Server")));
        CloseableHttpClient httpclient = HttpFutureUtils.getOneHttpClient();

        String url = appSettingState.getMeterSphereAddress() + "/api/definition/import";
        HttpPost httpPost = new HttpPost(url);// 创建httpPost
        httpPost.setHeader("Accept", "application/json, text/plain, */*");
        httpPost.setHeader("accesskey", appSettingState.getAccesskey());
        httpPost.setHeader("signature", CodingUtil.aesEncrypt(appSettingState.getAccesskey()+"|"+System.currentTimeMillis(),appSettingState.getSecretkey(),appSettingState.getAccesskey()));
        CloseableHttpResponse response = null;
        JSONObject param = buildParam(appSettingState);
        HttpEntity formEntity = MultipartEntityBuilder.create().addBinaryBody("file", file, ContentType.APPLICATION_JSON, null)
                .addBinaryBody("request", param.toJSONString().getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON, null).build();

        httpPost.setEntity(formEntity);
        try {
            response = httpclient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static JSONObject buildParam(AppSettingState state) {
        JSONObject param = new JSONObject();
        param.put("modeId", MSApiUtil.getModeId(state.getModeId()));
        if (state.getModule() == null) {
            throw new RuntimeException("no module selected ! please check your rights");
        }
        param.put("moduleId", state.getModule().getId());
        param.put("platform", "Postman");
        param.put("model", "definition");
        param.put("projectId", state.getProject().getId());
        if (state.getProjectVersion() != null && state.isSupportVersion()) {
            param.put("versionId", state.getProjectVersion().getId());
        }
        if (MSApiUtil.getModeId(state.getModeId()).equalsIgnoreCase(MSApiConstants.MODE_FULLCOVERAGE)) {
            if (state.getUpdateVersion() != null && state.isSupportVersion()) {
                param.put("updateVersionId", state.getUpdateVersion().getId());
            }
            if (state.isCoverModule()) {
                param.put("coverModule", true);
            } else {
                param.put("coverModule", false);
            }
        }
        param.put("protocol", "HTTP");
        return param;
    }
}
