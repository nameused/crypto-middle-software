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
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.github.algorithm.gm.SM2;
import org.github.algorithm.gm.SM3;
import org.github.algorithm.gm.SM4;
import org.github.bean.CryptoResponse;
import org.github.common.exception.EncryptException;
import org.github.common.exception.HashException;
import org.github.common.exception.SignException;
import org.github.common.utils.FileUtils;
import org.github.config.CryptoConfig;
import org.github.config.CryptoConfigFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.github.common.utils.GmUtil.byteArrayToPrivateKey;
import static org.github.common.utils.GmUtil.byteArrayToPublickey;

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
    private final static String SM2_VERIFY = "sm2_verify";
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
        SM2 sm2 = new SM2();
        SM4 sm4 = new SM4();
        //加密请求
        if (CRYPTO_REQUEST.equals(messageType)) {
            String requestBody = jsonObject.getString("request_body");
            String invokeType = JSON.parseObject(requestBody).getString("invoke_type");
            String data = JSON.parseObject(requestBody).getString("data");
            switch (invokeType) {
                case SM2_SIGN:
                    String privateKey = JSON.parseObject(requestBody).getString("key");
                    byte[] sk = Base64.decode(privateKey);
                    byte[] signValue = null;
                    try {
                        signValue = sm2.sign(data.getBytes(), sk, "SM3WithSM2");
                    } catch (SignException e) {
                        cryptoResponse.setCode(500);
                        cryptoResponse.setData(e.getMessage());
                    }
                    log.info("签名值:" + Hex.toHexString(signValue));
                    String sign = Base64.toBase64String(signValue);
                    log.info("签名值的base64字符串:" + sign);
                    cryptoResponse.setCode(200);
                    cryptoResponse.setData(sign);
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
                        cryptoResponse.setData(e.getMessage());
                    }
                    log.info("验证结果:" + verify);
                    cryptoResponse.setCode(200);
                    cryptoResponse.setData(String.valueOf(verify));
                    result = JSON.toJSONString(cryptoResponse);
                    break;
                case SM3_HASH:
                    SM3 sm3 = new SM3();
                    byte[] hashValue = null;
                    try {
                        hashValue = sm3.hash(data.getBytes());
                    } catch (HashException e) {
                        cryptoResponse.setCode(500);
                        cryptoResponse.setData(e.getMessage());
                    }
                    String hashData = Base64.toBase64String(hashValue);
                    cryptoResponse.setCode(200);
                    cryptoResponse.setData(hashData);
                    result = JSON.toJSONString(cryptoResponse);
                    break;
                case SM4_ENCRYPT:
                    String sm4key = JSON.parseObject(requestBody).getString("key");
                    byte[] encryData = null;
                    try {
                        encryData = sm4.encrypt("SM4/ECB/PKCS5Padding", Hex.decode(sm4key), null, data.getBytes());
                    } catch (EncryptException e) {
                        log.error(e.getMessage());
                        cryptoResponse.setCode(500);
                        cryptoResponse.setData(e.getMessage());
                    }
                    cryptoResponse.setCode(200);
                    cryptoResponse.setData(Base64.toBase64String(encryData));
                    result = JSON.toJSONString(cryptoResponse);
                    break;
                case SM4_DECRYPT:
                    String key = JSON.parseObject(requestBody).getString("key");
                    byte[] originalText = null;
                    try {
                        originalText = sm4.decrypt("SM4/ECB/PKCS5Padding", Hex.decode(key), null, Base64.decode(data));
                    } catch (EncryptException e) {
                        log.error(e.getMessage());
                        cryptoResponse.setCode(500);
                        cryptoResponse.setData(e.getMessage());
                    }
                    cryptoResponse.setCode(200);
                    cryptoResponse.setData(new String(originalText));
                    result = JSON.toJSONString(cryptoResponse);
                    break;
                default:
                    log.error("不支持该消息类型!");
                    break;
            }

        }
        //客户端请求服务端私钥
        else if (PUBLICKEY_REQUEST.equals(messageType)) {
            String serverPublickey = CryptoConfigFactory.getCryptoConfig().getServer().get("publicKey");
            cryptoResponse.setCode(200);
            cryptoResponse.setData(serverPublickey);
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
            cryptoResponse.setData("appkey写入成功!");
            result = JSON.toJSONString(cryptoResponse);
        }
        return result;
    }


    @Test
    public void test() throws HashException, EncryptException {
        SM4 sm4 = new SM4();
        System.out.println(Hex.toHexString(Hex.decode("b4e6b84eebb69cfad6e4e306cc371dad")));
        System.out.println("密钥长度：" + Hex.decode("7ea62732069e1c03").length);
        byte[] encryData = sm4.encrypt("SM4/ECB/PKCS5Padding", "7ea62732069e1c03".getBytes(), null, "12344".getBytes());
        log.info("sm4加密结果:" + Hex.toHexString(encryData));
    }
}
