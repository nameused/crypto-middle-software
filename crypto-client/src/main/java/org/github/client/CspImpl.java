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
        return cryptoClient.send(serverPublicKeyRequestMessage);
    }

    /**
     * 生成appkey 并发送给
     * 服务端
     *
     * @param serverPublicKey 服务端公钥
     * @param appCode         本地应用代码
     * @return
     */
    public String getAppKey(String serverPublicKey, String appCode) {
        String appkeyRequestMessage = requestHelper.buildAppkeyRequestMessage(serverPublicKey, appCode);
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
        return cryptoClient.send(hashRequestMessage);
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
        return cryptoClient.send(symmetryKeyRequestMessage);
    }

    /**
     * 获取非对称密钥对
     *
     * @param appKey
     * @param appCode
     * @return
     */
    public String getAsymmetricKey(String appKey, String appCode) {
        String asymmetricKeyRequestMessage = requestHelper.buildAsymmetricKeyRequestMessage(appKey, appCode);
        return cryptoClient.send(asymmetricKeyRequestMessage);
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
        return cryptoClient.send(signRequestMessage);
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
        return cryptoClient.send(verifyRequestMessage);
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
        return cryptoClient.send(encryptRequestMessage);
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
        return cryptoClient.send(decryptRequestMessage);
    }

}
