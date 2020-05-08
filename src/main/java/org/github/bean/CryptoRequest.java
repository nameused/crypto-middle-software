/**
 * Copyright DingXuan. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.github.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangmingyang
 * @Date: 2020/5/8
 * @company Dingxuan
 */
public class CryptoRequest {
    /**
     * 请求Id
     */
    @JSONField(name = "request_id")
    private String requestId;
    /**
     * 消息类型
     * cryptoRequest为加密请求
     */
    @JSONField(name = "message_type")
    private String messageType;

    /**
     * 数据请求提
     */
    @JSONField(name = "request_body")
    private Map<String, String> requestBody;
    /**
     * 数据请求头
     */
    @JSONField(name = "request_header")
    private Map<String, String> requestHeader;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Map<String, String> getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Map<String, String> requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(Map<String, String> requestHeader) {
        this.requestHeader = requestHeader;
    }

    public static void main(String[] args) {

        CryptoRequest cryptoRequest = new CryptoRequest();
        cryptoRequest.setRequestId("12323432");
        cryptoRequest.setMessageType("cryptoRequest");
        Map<String, String> headerMap = new HashMap<String, String>();
        Map<String, String> bodyMap = new HashMap<String, String>();
        headerMap.put("sign_factor", "sm2_sign");
        headerMap.put("signed_data", "23432445");

        bodyMap.put("invoke_type", "sm2_sign");
        bodyMap.put("key", "23432445");
        bodyMap.put("data", "fjikwer");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);

        String json = JSON.toJSONString(cryptoRequest);
        //获取请求体部分
        JSONObject requestbodyJson = JSONObject.parseObject(json);
        //对请求体进行签名,构造头部
        System.out.println(requestbodyJson.getString("request_body"));
        System.out.println(json);
    }
}
