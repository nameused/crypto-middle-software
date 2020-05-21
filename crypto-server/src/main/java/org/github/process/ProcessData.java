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

import org.github.algorithm.factor.SecurityDigest;
import org.github.algorithm.gm.SM2;
import org.github.algorithm.gm.SM3;
import org.github.algorithm.gm.SM4;
import org.github.bean.CryptoRequest;
import org.github.bean.CryptoResponse;
import org.github.common.exception.EncryptException;
import org.github.common.exception.HashException;
import org.github.common.exception.SignException;
import org.github.common.utils.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.github.config.CryptoConfigFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;


/**
 * 报文消息处理
 *
 * @author zhangmingyang
 * @Date: 2020/5/8
 * @company Dingxuan
 */
public class ProcessData {
    private static final Logger log = Logger.getLogger(ProcessData.class);
    private final static String APPKEY_REQUEST = "appKeyRequest";
    private final static String CRYPTO_REQUEST = "cryptoRequest";
    private final static String SM3_HASH = "sm3_hash";
    private final static String SM2_SIGN = "sm2_sign";
    private final static String SM2_KEYPAIR_GEN = "sm2_keypair_gen";
    private final static String SM2_VERIFY = "sm2_verify";
    private final static String SM4_KEY_GEN = "sm4_key_gen";
    private final static String SM4_ENCRYPT = "sm4_encrypt";
    private final static String SM4_DECRYPT = "sm4_decrypt";
    private final static String PUBLICKEY_REQUEST = "publicKeyRequest";


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
        CryptoResponse cryptoResponse = new CryptoResponse();
        Map<String,String> dataMap=new HashMap<>();
        SM2 sm2 = new SM2();
        SM3 sm3 = new SM3();
        SM4 sm4 = new SM4();
        CryptoRequest cryptoRequest = new CryptoRequest();
        SecurityDigest securityDigest = new SecurityDigest();
        //加密请求
        if (CRYPTO_REQUEST.equals(messageType)) {

            //获取appkey验证签名
            String requestHeader = jsonObject.getString("request_header");
            String appCode = JSON.parseObject(requestHeader).getString("app_code");
            String requestBody = jsonObject.getString("request_body");
            //加密后的数据
            String bodyEncryptData = JSON.parseObject(requestBody).getString("body_encrypt_data");
            if (!FileUtils.findFile(appCode)) {
                cryptoResponse.setCode(500);
                dataMap.put("error_msg","非法的数据请求!");
                cryptoResponse.setData(dataMap);
                throw new RuntimeException("非法的数据请求!");
            }

            byte[] appKey = Base64.decode(FileUtils.getAppKey(appCode));
            byte[] signFactor = Base64.decode(JSON.parseObject(requestHeader).getString("sign_factor"));
            byte[] hamcVaule = Base64.decode(JSON.parseObject(requestHeader).getString("hmac_value"));
            boolean verifyResult = false;
            try {
                verifyResult = securityDigest.verify(signFactor, appKey, Base64.decode(bodyEncryptData), hamcVaule);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            if (!verifyResult) {
                cryptoResponse.setCode(500);
                dataMap.put("error_msg","数据验证不通过,数据存在被篡改风险!");
                cryptoResponse.setData(dataMap);
                log.error("数据验证不通过,数据存在被篡改风险!");
                result = JSON.toJSONString(cryptoResponse);
                return result;
            }
            //通过appkey解析出新的requestBody内容
            byte[] requestBodyContent = null;
            try {
                requestBodyContent = sm4.decrypt("SM4/ECB/PKCS5Padding", appKey, null, Base64.decode(bodyEncryptData));
            } catch (EncryptException e) {
                cryptoResponse.setCode(500);
                dataMap.put("error_msg","数据解密错误!");
                cryptoResponse.setData(dataMap);
                log.error("数据解密错误!");
            }

            String requestBodyData = new String(requestBodyContent);
            Map requestBodyMap = JSON.parseObject(requestBodyData, Map.class);
            cryptoRequest.setRequestBody(requestBodyMap);
            String requestBodyJson = JSON.toJSONString(cryptoRequest);
            requestBody = JSON.parseObject(requestBodyJson).getString("request_body");
            String invokeType = JSON.parseObject(requestBody).getString("invoke_type");
            String data = JSON.parseObject(requestBody).getString("data");
            switch (invokeType) {
                case SM2_KEYPAIR_GEN:
                    KeyPair keyPair=null;
                    try {
                        keyPair =sm2.genKeyPair();
                    } catch ( SignException e) {
                        log.error(e.getMessage());
                    }
                    String sm2PrivateKey=Base64.toBase64String(keyPair.getPrivate().getEncoded());
                    String sm2PublicKey=Base64.toBase64String(keyPair.getPublic().getEncoded());
                    cryptoResponse.setCode(200);
                    dataMap.put("private_key",sm2PrivateKey);
                    dataMap.put("public_key",sm2PublicKey);
                    cryptoResponse.setData(dataMap);
                    result=JSON.toJSONString(cryptoResponse);
                    break;
                case SM2_SIGN:
                    String privateKey = JSON.parseObject(requestBody).getString("key");
                    byte[] sk = Base64.decode(privateKey);
                    byte[] signValue = null;
                    try {
                        signValue = sm2.sign(data.getBytes(), sk, "SM3WithSM2");
                    } catch (SignException e) {
                        cryptoResponse.setCode(500);
                        dataMap.put("error_msg",e.getMessage());
                        cryptoResponse.setData(dataMap);
                    }
                    log.info("签名值:" + Hex.toHexString(signValue));
                    String sign = Base64.toBase64String(signValue);
                    log.info("签名值的base64字符串:" + sign);
                    cryptoResponse.setCode(200);
                    dataMap.put("result",sign);
                    cryptoResponse.setData(dataMap);
                    result = JSON.toJSONString(cryptoResponse);
                    break;

                case SM2_VERIFY:
                    String publicKey = JSON.parseObject(requestBody).getString("key");
                    String signature = JSON.parseObject(requestBody).getString("sign_value");
                    byte[] pk = Base64.decode(publicKey);
                    boolean verify = false;
                    try {
                        verify = sm2.verify(data.getBytes(), pk, Base64.decode(signature), "SM3WithSM2");
                        log.info("验证结果:" + verify);
                    } catch (SignException e) {
                        cryptoResponse.setCode(500);
                        dataMap.put("error_msg",e.getMessage());
                        cryptoResponse.setData(dataMap);
                    }
                    log.info("验证结果:" + verify);
                    cryptoResponse.setCode(200);
                    dataMap.put("result",String.valueOf(verify));
                    cryptoResponse.setData(dataMap);
                    result = JSON.toJSONString(cryptoResponse);
                    break;

                case SM3_HASH:
                    byte[] hashValue = null;
                    try {
                        hashValue = sm3.hash(data.getBytes());
                    } catch (HashException e) {
                        cryptoResponse.setCode(500);
                        dataMap.put("error_msg",e.getMessage());
                        cryptoResponse.setData(dataMap);
                    }
                    String hashData = Base64.toBase64String(hashValue);
                    cryptoResponse.setCode(200);
                    dataMap.put("result",hashData);
                    cryptoResponse.setData(dataMap);
                    result = JSON.toJSONString(cryptoResponse);
                    break;

                case SM4_KEY_GEN:
                    byte[] genSM4key=null;
                    try {
                        genSM4key=sm4.genKey();
                    } catch (EncryptException e) {
                        log.error(e.getMessage());
                    }
                    String sm4keyData=Base64.toBase64String(genSM4key);
                    cryptoResponse.setCode(200);
                    dataMap.put("result",sm4keyData);
                    cryptoResponse.setData(dataMap);
                    result=JSON.toJSONString(cryptoResponse);
                    break;

                case SM4_ENCRYPT:
                    String sm4key = JSON.parseObject(requestBody).getString("key");
                    byte[] encryData = null;
                    try {
                        encryData = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode(sm4key), null, data.getBytes());
                    } catch (EncryptException e) {
                        log.error(e.getMessage());
                        cryptoResponse.setCode(500);
                        dataMap.put("error_msg",e.getMessage());
                        cryptoResponse.setData(dataMap);
                    }
                    cryptoResponse.setCode(200);
                    dataMap.put("result",Base64.toBase64String(encryData));
                    cryptoResponse.setData(dataMap);
                    result = JSON.toJSONString(cryptoResponse);
                    break;
                case SM4_DECRYPT:
                    String key = JSON.parseObject(requestBody).getString("key");
                    byte[] originalText = null;
                    try {
                        originalText = sm4.decrypt("SM4/ECB/PKCS5Padding", Base64.decode(key), null, Base64.decode(data));
                    } catch (EncryptException e) {
                        log.error(e.getMessage());
                        cryptoResponse.setCode(500);
                        dataMap.put("error_msg",e.getMessage());
                        cryptoResponse.setData(dataMap);
                    }
                    cryptoResponse.setCode(200);
                    dataMap.put("result",new String(originalText));
                    cryptoResponse.setData(dataMap);
                    result = JSON.toJSONString(cryptoResponse);
                    break;
                default:
                    log.error("不支持的消息类型!");
                    break;
            }
        }
        //客户端请求服务端私钥
        else if (PUBLICKEY_REQUEST.equals(messageType)) {
            String serverPublickey = CryptoConfigFactory.getCryptoConfig().getServer().get("publicKey");
            cryptoResponse.setCode(200);
            dataMap.put("result",serverPublickey);
            cryptoResponse.setData(dataMap);
            result = JSON.toJSONString(cryptoResponse);
        } else if (APPKEY_REQUEST.equals(messageType)) {
            String serverPrivateKey = CryptoConfigFactory.getCryptoConfig().getServer().get("privateKey");
            String appKeyPath = CryptoConfigFactory.getCryptoConfig().getClient().get("appKeyPath");
            String appCode = jsonObject.getString("app_code");
            String encryptAppkey = jsonObject.getString("app_key");
            byte[] appkey = sm2.decrypt(Base64.decode(encryptAppkey), Base64.decode(serverPrivateKey));
            if (!new File(appKeyPath).exists()) {
                try {
                    Files.createDirectories(Paths.get(appKeyPath));
                } catch (IOException e) {
                    log.error("创建文件夹失败!");
                }
            }
            try {
                FileUtils.genAppKeyFile(appKeyPath, appCode, appkey);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            cryptoResponse.setCode(200);
            dataMap.put("success_msg","appkey写入到服务端成功!");
            cryptoResponse.setData(dataMap);
            result = JSON.toJSONString(cryptoResponse);
        }
        return result;
    }


}
