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
package org.github.algorithm;

import org.github.common.CryptoException;

import java.security.KeyPair;

/**
 * 密码服务提供者接口
 *
 * @author zhangmingyang
 * @Date: 2020/4/27
 * @company Dingxuan
 */
public interface ICsp {
    /**
     * 对消息进行hash运算
     *
     * @param msg 将要进行hash运算的消息
     * @return hash值
     */

    /**
     * 对称密钥生成
     *
     * @return 对称密钥（索引）
     */
    byte[] keyGenSymmetrical() throws CryptoException;

    /**
     * 非对称密钥生成
     *
     * @return 非对称密钥对, 包括公钥、私钥（索引）
     */
    KeyPair genKeyPair(int keySize) throws CryptoException;



    byte[] hash(byte[] msg) throws CryptoException;

    /**
     * 使用输入的私钥（索引）对消息进行数字签名
     *
     * @param privateKeyIndex 私钥（索引）
     * @param msg             将要进行签名运算的消息
     * @return 数字签名值
     */
    byte[] sign(byte[] privateKeyIndex, byte[] msg) throws CryptoException;

    /**
     * 使用输入的公钥对签名值进行验证
     *
     * @param key       公钥
     * @param signature 签名值
     * @param msg       将要进行验签运算的消息
     * @return 验签结果，true/false
     */
    boolean verify(byte[] key, byte[] signature, byte[] msg) throws CryptoException;

    /**
     * 加密算法类型,使用密钥key将明文计算出密文
     *
     * @param key       密钥（对称密钥或公钥）
     * @param plaintext 明文
     * @param type      加密算法类型
     * @return 加密后密文
     */
    byte[] encrypt(byte[] key, byte[] plaintext, int type) throws CryptoException;

    /**
     * 加密算法类型,使用密钥key将密文计算出明文
     *
     * @param key        密钥（对称密钥或私钥）
     * @param ciphertext 密文
     * @param type       加密算法类型
     * @return 解密后明文
     */
    byte[] decrypt(byte[] key, byte[] ciphertext, int type) throws CryptoException;

    /**
     * 计算指定长度的随机数
     *
     * @param len 随机数长度
     * @return 随机数
     */
    byte[] rng(int len) throws CryptoException;

    /**
     * Csp提供厂商，可与gmcsp.yaml中defaultValue所要使用的值保持一致
     */
    String getProvider();


}
