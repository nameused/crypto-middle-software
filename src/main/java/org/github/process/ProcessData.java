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
package org.github.process;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.github.algorithm.gm.hash.SM3;
import org.github.bean.CryptoResponse;
import org.github.common.exception.HashException;
import org.junit.Test;

/**
 * @author zhangmingyang
 * @Date: 2020/5/8
 * @company Dingxuan
 */
public class ProcessData {

    private final static String APPKEY_REQUEST = "appkeyRequst";
    private final static String CRYPTO_REQUEST = "cryptoRequest";

    /**
     * 根据接收到的消息类型进行解析处理
     *
     * @param strInputstream
     * @return
     */
    public static String process(String strInputstream) {
        String result = null;
        JSONObject jsonObject = JSON.parseObject(strInputstream);
        String messageType = jsonObject.getString("message_type");
        switch (messageType) {
            case CRYPTO_REQUEST:
                String requestBody = jsonObject.getString("request_body");
                String invokeType = JSON.parseObject(requestBody).getString("invoke_type");
                if ("sm3_hash".equals(invokeType)) {
                    String data = JSON.parseObject(requestBody).getString("data");
                    SM3 sm3 = new SM3();
                    byte[] hashValue = null;
                    try {
                        hashValue = sm3.hash(data.getBytes());
                    } catch (HashException e) {
                        e.printStackTrace();
                    }

                    String hashData = Base64.encode(hashValue);
                    CryptoResponse cryptoResponse=new CryptoResponse();
                    cryptoResponse.setCode(200);
                    cryptoResponse.setData(hashData);
                    result=JSON.toJSONString(cryptoResponse);
                } else if ("sm2_sign".equals(invokeType)) {
                }
                break;
            case APPKEY_REQUEST:
                break;
            default:
                break;
        }
        return result;
    }


    @Test
    public  void test() throws HashException {
        SM3 sm3=new SM3();
        sm3.hash("123".getBytes());
    }
}
