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
package org.github.csp;

import org.github.common.exception.CspException;

import java.security.KeyPair;

/**
 * 密码服务接口
 *
 * @author zhangmingyang
 * @Date: 2020/6/8
 * @company Dingxuan
 */
public interface ICsp {

    /**
     * 密钥对生成
     *
     * @return
     */
    KeyPair genKeyPair() throws CspException;


    /**
     * 生产随机数
     *
     * @param length
     * @return
     * @throws CspException
     */
    String genRandom(int length) throws CspException;


    /**
     * 数据哈希
     *
     * @param data
     * @return
     * @throws CspException
     */
    byte[] hash(byte[] data) throws CspException;

    /**
     * 数据签名
     *
     * @param privateKey
     * @param data
     * @return
     * @throws CspException
     */

    byte[] sign(byte[] privateKey, byte[] data) throws CspException;

    /**
     * 验证签名
     *
     * @param publicKey
     * @param data
     * @param signature
     * @return
     * @throws CspException
     */
    boolean verify(byte[] publicKey, byte[] data, byte[] signature) throws CspException;

    /**
     * 非对称加密
     *
     * @param publickey
     * @param data
     * @return
     * @throws CspException
     */
    byte[] asyEncrypt(byte[] publickey, byte[] data) throws CspException;

    /**
     * 非对称解密
     *
     * @param privateKey
     * @param data
     * @return
     * @throws CspException
     */
    byte[] asyDecrypt(byte[] privateKey, byte[] data) throws CspException;

    /**
     * 对称加密
     *
     * @param key
     * @param data
     * @param iv
     * @param algorithmType
     * @return
     * @throws CspException
     */
    byte[] sysEncrypt(byte[] key, byte[] data, byte[] iv, String algorithmType) throws CspException;

    /**
     * 对称解密
     *
     * @param key
     * @param data
     * @param iv
     * @param algorithmType
     * @return
     * @throws CspException
     */
    byte[] sysDecrypt(byte[] key, byte[] data, byte[] iv, String algorithmType) throws CspException;
}
