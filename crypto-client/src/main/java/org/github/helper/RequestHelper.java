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
package org.github.helper;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.github.algorithm.factor.SecurityDigest;
import org.github.algorithm.gm.SM2;
import org.github.algorithm.gm.SM4;
import org.github.bean.CryptoRequest;
import org.github.common.exception.EncryptException;
import org.github.common.exception.HashException;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据请求辅助类
 *
 * @author zhangmingyang
 * @Date: 2020/5/20
 * @company Dingxuan
 */
public class RequestHelper {
    private static final Logger log = Logger.getLogger(RequestHelper.class);
    private SM2 sm2;
    private SM4 sm4;
    private SecurityDigest securityDigest;
    /**
     * 消息类型
     */
    public static final String MESSAGE_TYPE = "message_type";


    public RequestHelper() {
        this.securityDigest = new SecurityDigest();
        this.sm2 = new SM2();
        sm4 = new SM4();
    }

    /**
     * 构建服务端公钥请求消息
     * ex:
     *
     * @return
     */
    public static String buildServerPublicKeyRequestMessage() {
        Map<String, String> map = new HashMap();
        map.put(MESSAGE_TYPE, "publicKeyRequest");
        return JSON.toJSONString(map);
    }

    /**
     * 本地生产appkey,构建appkey请求消息
     *
     * @param serverPublickey 服务端公钥
     * @param appCode         本地应用编码代号appcode
     * @return
     */
    public String buildAppkeyRequestMessage(String serverPublickey, String appCode) {
        SecurityDigest securityDigest = new SecurityDigest();
        //生成appkey
        byte[] appkey = new byte[0];
        try {
            appkey = securityDigest.genAppkey(appCode);
        } catch (EncryptException e) {
            e.printStackTrace();
        }
        //公钥转换
        byte[] sm2publicKey = Base64.decode(serverPublickey);
        //本地保存生成的appkey密钥

        //利用服务端公钥加密appkey
        byte[] encryptAppkey = sm2.encrypt(appkey, sm2publicKey);
        log.info("公钥加密后的值：" + Base64.toBase64String(encryptAppkey));
        //组装appkey请求数据
        Map<String, String> map = new HashMap();
        map.put("message_type", "appKeyRequest");
        map.put("app_code", appCode);
        map.put("app_key", Base64.toBase64String(encryptAppkey));
        return JSON.toJSONString(map);
    }

    /**
     * 构建哈希请求消息
     *
     * @param appKey
     * @return
     */
    public String buildHashRequestMessage(String appKey, String appCode, byte[] data) {
        CryptoRequest cryptoRequest = new CryptoRequest();
        Map headerMap = new HashMap<String, String>();
        Map bodyMap = new HashMap<String, String>();
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm3_hash");
        bodyMap.put("data", Base64.toBase64String(data));
        String bodyJson = JSON.toJSONString(bodyMap);
        //先使用appkey加密数据
        byte[] bodyByte = new byte[0];
        try {
            bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode(appKey), null, bodyJson.getBytes());
        } catch (EncryptException e) {
            e.printStackTrace();
        }
        bodyMap.clear();
        bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
        //然后利用appkey进行sm3的hmac计算
        byte[] signFactorByte = new byte[0];
        byte[] keyS = null;
        byte[] hmac = null;
        try {
            signFactorByte = securityDigest.genSignFactor();
            keyS = securityDigest.genKeyS(signFactorByte, Base64.decode(appKey));
            hmac = securityDigest.hamc(keyS, bodyByte);
        } catch (EncryptException | HashException e) {
            log.error(e.getMessage());
        }
        String signFactor = Base64.toBase64String(signFactorByte);
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", appCode);
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        return JSON.toJSONString(cryptoRequest);
    }

    /**
     * 构建对称密钥请求消息
     *
     * @param appKey
     * @param appCode
     * @return
     */
    public String buildSymmetryKeyRequestMessage(String appKey, String appCode) {
        CryptoRequest cryptoRequest = new CryptoRequest();
        Map headerMap = new HashMap<String, String>();
        Map bodyMap = new HashMap<String, String>();
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm4_key_gen");
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = new byte[0];
        byte[] signFactorByte = new byte[0];
        byte[] keyS = null;
        byte[] hmac = null;
        String signFactor = null;
        try {
            bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode(appKey), null, bodyJson.getBytes());
            bodyMap.clear();
            bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
            signFactorByte = securityDigest.genSignFactor();
            signFactor = Base64.toBase64String(signFactorByte);
            keyS = securityDigest.genKeyS(signFactorByte, Base64.decode(appKey));
            hmac = securityDigest.hamc(keyS, bodyByte);
        } catch (EncryptException | HashException e) {
            e.printStackTrace();
        }
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", appCode);
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        return JSON.toJSONString(cryptoRequest);
    }

    /**
     * 构建非对称密钥请求消息
     *
     * @param appKey
     * @param appCode
     * @return
     */

    public String buildAsymmetricKeyRequestMessage(String appKey, String appCode) {
        CryptoRequest cryptoRequest = new CryptoRequest();
        Map headerMap = new HashMap<String, String>();
        Map bodyMap = new HashMap<String, String>();
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm2_keypair_gen");
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = new byte[0];
        byte[] signFactorByte = new byte[0];
        byte[] keyS = null;
        byte[] hmac = null;
        String signFactor = null;
        try {
            bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode(appKey), null, bodyJson.getBytes());
            bodyMap.clear();
            bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
            signFactorByte = securityDigest.genSignFactor();
            signFactor = Base64.toBase64String(signFactorByte);
            keyS = securityDigest.genKeyS(signFactorByte, Base64.decode(appKey));
            hmac = securityDigest.hamc(keyS, bodyByte);
        } catch (EncryptException | HashException e) {
            e.printStackTrace();
        }
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", appCode);
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        return JSON.toJSONString(cryptoRequest);
    }


    /**
     * 构建签名请求消息
     *
     * @param appKey
     * @param appCode
     * @param privateKey
     * @param data
     * @return
     */
    public String buidSignRequestMessage(String appKey, String appCode, String privateKey, byte[] data) {
        CryptoRequest cryptoRequest = new CryptoRequest();
        Map headerMap = new HashMap<String, String>();
        Map bodyMap = new HashMap<String, String>();
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm2_sign");
        bodyMap.put("key", privateKey);
        bodyMap.put("data", Base64.toBase64String(data));
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = new byte[0];
        byte[] signFactorByte = null;
        byte[] keyS = null;
        byte[] hmac = null;
        String signFactor = null;
        try {
            bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode(appKey), null, bodyJson.getBytes());
            bodyMap.clear();
            bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
            signFactorByte = securityDigest.genSignFactor();
            signFactor = Base64.toBase64String(signFactorByte);
            keyS = securityDigest.genKeyS(signFactorByte, Base64.decode(appKey));
            hmac = securityDigest.hamc(keyS, bodyByte);
        } catch (EncryptException | HashException e) {
            e.printStackTrace();
        }
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", appCode);
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        return JSON.toJSONString(cryptoRequest);
    }

    /**
     * 构建签名验证请求消息
     *
     * @param appKey
     * @param appCode
     * @param publicKey
     * @param data
     * @param signValue
     * @return
     */

    public String buildVerifyRequestMessage(String appKey, String appCode, String publicKey, byte[] data, String signValue) {
        CryptoRequest cryptoRequest = new CryptoRequest();
        Map headerMap = new HashMap<String, String>();
        Map bodyMap = new HashMap<String, String>();
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm2_verify");
        bodyMap.put("sign_value", signValue);
        bodyMap.put("key", publicKey);
        bodyMap.put("data", data);
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = new byte[0];
        byte[] signFactorByte = null;
        byte[] keyS = null;
        byte[] hmac = null;
        String signFactor = null;
        try {
            bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode(appKey), null, bodyJson.getBytes());
            bodyMap.clear();
            bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
            signFactorByte = securityDigest.genSignFactor();
            signFactor = Base64.toBase64String(signFactorByte);
            keyS = securityDigest.genKeyS(signFactorByte, Base64.decode(appCode));
            hmac = securityDigest.hamc(keyS, bodyByte);
        } catch (EncryptException | HashException e) {
            e.printStackTrace();
        }
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", appCode);
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        return JSON.toJSONString(cryptoRequest);
    }

    /**
     * 构建加密请求消息
     *
     * @param appKey
     * @param appCode
     * @param sm4Key
     * @param data
     * @return
     */
    public String buildEncryptRequestMessage(String appKey, String appCode, String sm4Key, byte[] data) {
        CryptoRequest cryptoRequest = new CryptoRequest();
        Map headerMap = new HashMap<String, String>();
        Map bodyMap = new HashMap<String, String>();
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm4_encrypt");
        bodyMap.put("key", sm4Key);
        bodyMap.put("data", data);
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = new byte[0];
        byte[] signFactorByte = null;
        byte[] keyS = null;
        byte[] hmac = null;
        String signFactor = null;
        try {
            bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode(appKey), null, bodyJson.getBytes());
            bodyMap.clear();
            bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
            signFactorByte = securityDigest.genSignFactor();
            signFactor = Base64.toBase64String(signFactorByte);
            keyS = securityDigest.genKeyS(signFactorByte, Base64.decode(appCode));
            hmac = securityDigest.hamc(keyS, bodyByte);
        } catch (EncryptException | HashException e) {
            e.printStackTrace();
        }
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", appCode);
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        return JSON.toJSONString(cryptoRequest);
    }

    /**
     * 构建解密请求消息
     *
     * @param appKey
     * @param appCode
     * @param sm4Key
     * @param encryptData
     * @return
     */
    public String buildDecryptRequestMessage(String appKey, String appCode, String sm4Key, byte[] encryptData) {
        CryptoRequest cryptoRequest = new CryptoRequest();
        Map headerMap = new HashMap<String, String>();
        Map bodyMap = new HashMap<String, String>();
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm4_decrypt");
        bodyMap.put("key", sm4Key);
        bodyMap.put("data", Base64.toBase64String(encryptData));
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = new byte[0];
        byte[] signFactorByte = null;
        byte[] keyS = null;
        byte[] hmac = null;
        String signFactor = null;
        try {
            bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode(appKey), null, bodyJson.getBytes());
            bodyMap.clear();
            bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
            signFactorByte = securityDigest.genSignFactor();
            signFactor = Base64.toBase64String(signFactorByte);
            keyS = securityDigest.genKeyS(signFactorByte, Base64.decode(appCode));
            hmac = securityDigest.hamc(keyS, bodyByte);
        } catch (EncryptException | HashException e) {
            e.printStackTrace();
        }
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", appCode);
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        return JSON.toJSONString(cryptoRequest);
    }

}
