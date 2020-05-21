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
package org.github.algorithm.gm;

import org.github.common.exception.EncryptException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

/**
 * @author zhangmingyang
 * @Date: 2020/5/13
 * @company Dingxuan
 */
public class SM4 extends GmBase{
    private static final Logger log = Logger.getLogger(SM4.class);
    private static final String KEY_ALGORITHM = "SM4";

    public byte[] genKey() throws EncryptException {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM,BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            log.error(e.getMessage());
            throw new EncryptException(e.getMessage(), e);
        }
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    public byte[] encrypt(String cipherAlgorithm, byte[] key, byte[] iv, byte[] originalText) throws EncryptException {
        byte[] encryptData = null;
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm,BouncyCastleProvider.PROVIDER_NAME);
            Key secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
            if (ArrayUtils.isEmpty(iv)) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } else {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            }
            encryptData = cipher.doFinal(originalText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            log.error(e.getMessage());
            throw new EncryptException(e.getMessage(), e);
        }
        return encryptData;
    }


    public byte[] decrypt(String cipherAlgorithm, byte[] key, byte[] iv, byte[] encryptText) throws EncryptException {
        byte[] encryptData = null;
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm,BouncyCastleProvider.PROVIDER_NAME);
            Key secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
            if (ArrayUtils.isEmpty(iv)) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            } else {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            }
            encryptData = cipher.doFinal(encryptText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            log.error(e.getMessage());
            throw new EncryptException(e.getMessage(), e);
        }
        return encryptData;
    }
}
