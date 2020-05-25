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
package org.github.algorithm.factor;

import org.github.algorithm.gm.GmBase;
import org.github.algorithm.gm.SM3;
import org.github.algorithm.gm.SM4;
import org.github.common.exception.EncryptException;
import org.github.common.exception.HashException;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * 安全摘要实现
 *
 * @author zhangmingyang
 * @Date: 2020/5/13
 * @company Dingxuan
 */
public class SecurityDigest extends GmBase {
    private static final Logger log = Logger.getLogger(SecurityDigest.class);
    /**
     * 生成8字节的签名因子
     *
     * @return
     */
    public byte[] genSignFactor() throws EncryptException {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("SM4",BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error(e.getMessage());
            throw new EncryptException(e.getMessage(), e);
        }
        keyGenerator.init(64);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 根据appCode生成appKey
     *
     * @return
     */
    public byte[] genAppkey(String appCode) throws EncryptException {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("SM4", new BouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            throw new EncryptException(e.getMessage(), e);
        }
        keyGenerator.init(128, new SecureRandom(appCode.getBytes()));
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 通过签名因子生成Ks
     *
     * @param signFactor
     * @param appKey
     * @return
     */
    public byte[] genKeyS(byte[] signFactor, byte[] appKey) throws EncryptException {
        byte[] paddingFactor = new byte[0];
        try {
            paddingFactor = paddingFactor(signFactor);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        SM4 sm4 = new SM4();
        byte[] keyS = sm4.encrypt("SM4/ECB/PKCS5Padding", appKey, null, paddingFactor);
        return keyS;
    }

    /**
     * 通过keyS与原数据进行
     * sm3的hash计算得出摘要值
     *
     * @param message
     * @param keyS
     * @return
     */
    public byte[] hamc(byte[] keyS, byte[] message) throws HashException {
        byte[] finalMessage = toByteArray(keyS, message);
        SM3 sm3 = new SM3();
        byte[] hmac = sm3.hash(finalMessage);
        return hmac;
    }

    /**
     * 验证有效性
     *
     * @return
     */
    public boolean verify(byte[] signFactor, byte[] appKey, byte[] message, byte[] signValue) throws Exception {
        byte[] hamc = hamc(genKeyS(signFactor, appKey), message);
        if (Arrays.equals(hamc, signValue)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 计算Ks :=SM4(APPKEY)[Y||(Y⊕(‘FF’||‘FF’||‘FF’||‘FF’||‘FF’||‘FF’||‘FF’||‘FF’))]
     * 获取SM4实例时，字符串拼接模式有：PKCS5Padding和NOPadding
     * SM4初始化时1是加密，0是解密
     *
     * @param signFactor
     * @return 计算得到[Y||(Y⊕(‘FF’||‘FF’||‘FF’||‘FF’||‘FF’||‘FF’||‘FF’||‘FF’))]的值
     */
    public byte[] paddingFactor(byte[] signFactor) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] byteXor;
        try {
            byteXor = xor(signFactor, Hex.decode("FFFFFFFFFFFFFFFF"));
            baos.write(byteXor);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return baos.toByteArray();
    }

    /**
     * 异或运算
     *
     * @param signFactor
     * @param paddingByte
     * @return
     */
    private static byte[] xor(byte[] signFactor, byte[] paddingByte) {
        int byteLen = signFactor.length;
        byte[] result = new byte[byteLen];

        for (int i = 0; i < byteLen; i++) {
            result[i] = (byte) (signFactor[i] ^ paddingByte[i]);
        }
        return result;
    }

    /**
     * 数组拼接
     *
     * @param b1
     * @param b2
     * @return
     */
    private static byte[] toByteArray(byte[] b1, byte[] b2) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(b1);
            baos.write(b2);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return baos.toByteArray();
    }

}
