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
package org.github.client;

import org.bouncycastle.util.encoders.Base64;
import org.github.algorithm.factor.SecurityDigest;
import org.github.bean.AsymmetricKeyPair;
import org.github.common.exception.EncryptException;
import org.github.common.utils.MessageUtil;
import org.github.helper.RequestHelper;


/**
 * 密码服务调用统一入口
 *
 * @author zhangmingyang
 * @Date: 2020/5/25
 * @company Dingxuan
 */
public class CspImpl {
    /**
     * 密码服务客户端
     */
    private CryptoClient cryptoClient;
    /**
     * 请求辅助类
     */
    private RequestHelper requestHelper;

    public CspImpl(CryptoClient cryptoClient) {
        this.cryptoClient = cryptoClient;
        this.requestHelper = new RequestHelper();
    }

    /**
     * 获取服务端公钥信息
     *
     * @return
     */
    public String getServerPublicKey() {
        String serverPublicKeyRequestMessage = RequestHelper.buildServerPublicKeyRequestMessage();
        String result = cryptoClient.send(serverPublicKeyRequestMessage);
        return MessageUtil.parseCommonResult(result);
    }

    /**
     * 生成本地的APPKEY
     *
     * @param appCode
     * @return
     */
    public String genAppKey(String appCode) {
        SecurityDigest securityDigest = new SecurityDigest();
        //生成appkey
        byte[] appkey = new byte[0];
        try {
            appkey = securityDigest.genAppkey(appCode);
        } catch (EncryptException e) {
            e.printStackTrace();
        }
        return Base64.toBase64String(appkey);
    }


    /**
     * 生成appkey 并发送给
     * 服务端
     *
     * @param serverPublicKey 服务端公钥
     * @param appKey          本地应用密钥
     * @param appCode         本地应用代码
     * @return
     */
    public String sendAppKey(String serverPublicKey, String appKey, String appCode) {
        String appkeyRequestMessage = requestHelper.buildAppkeyRequestMessage(serverPublicKey, appKey, appCode);
        return cryptoClient.send(appkeyRequestMessage);
    }

    /**
     * 数据hash计算
     *
     * @param appKey
     * @param appCode
     * @param data
     * @return
     */
    public String hash(String appKey, String appCode, byte[] data) {
        String hashRequestMessage = requestHelper.buildHashRequestMessage(appKey, appCode, data);
        String result = cryptoClient.send(hashRequestMessage);
        return MessageUtil.parseCommonResult(result);
    }

    /**
     * 获取对称密钥
     *
     * @param appKey
     * @param appCode
     * @return
     */
    public String getSymmetryKey(String appKey, String appCode) {
        String symmetryKeyRequestMessage = requestHelper.buildSymmetryKeyRequestMessage(appKey, appCode);
        String result = cryptoClient.send(symmetryKeyRequestMessage);
        return MessageUtil.parseCommonResult(result);
    }

    /**
     * 获取非对称密钥对
     *
     * @param appKey
     * @param appCode
     * @return
     */
    public AsymmetricKeyPair getAsymmetricKey(String appKey, String appCode) {
        String asymmetricKeyRequestMessage = requestHelper.buildAsymmetricKeyRequestMessage(appKey, appCode);
        String message = cryptoClient.send(asymmetricKeyRequestMessage);
        return MessageUtil.parseAsymmetricKeyPairResult(message);
    }

    /**
     * 签名实现
     *
     * @param appKey
     * @param appCode
     * @param privateKey
     * @param data
     * @return
     */
    public String sign(String appKey, String appCode, String privateKey, byte[] data) {
        String signRequestMessage = requestHelper.buidSignRequestMessage(appKey, appCode, privateKey, data);
        String result = cryptoClient.send(signRequestMessage);
        return MessageUtil.parseCommonResult(result);
    }

    /**
     * 验证签名
     *
     * @param appKey
     * @param appCode
     * @param publicKey
     * @param data
     * @param signValue
     * @return
     */

    public String verfiy(String appKey, String appCode, String publicKey, byte[] data, String signValue) {
        String verifyRequestMessage = requestHelper.buildVerifyRequestMessage(appKey, appCode, publicKey, data, signValue);
        String result = cryptoClient.send(verifyRequestMessage);
        return MessageUtil.parseCommonResult(result);
    }

    /**
     * sm4加密
     *
     * @param appkey
     * @param appCode
     * @param sm4key
     * @param data
     * @return
     */
    public String encrypt(String appkey, String appCode, String sm4key, byte[] data) {
        String encryptRequestMessage = requestHelper.buildEncryptRequestMessage(appkey, appCode, sm4key, data);
        String result = cryptoClient.send(encryptRequestMessage);
        return MessageUtil.parseCommonResult(result);
    }

    /**
     * sm4解密
     *
     * @param appkey
     * @param appCode
     * @param sm4key
     * @param encryptData
     * @return
     */
    public String decrypt(String appkey, String appCode, String sm4key, byte[] encryptData) {
        String decryptRequestMessage = requestHelper.buildDecryptRequestMessage(appkey, appCode, sm4key, encryptData);
        String result = cryptoClient.send(decryptRequestMessage);
        return MessageUtil.parseCommonResult(result);
    }

}
