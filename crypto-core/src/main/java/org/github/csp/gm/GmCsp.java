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
package org.github.csp.gm;

import org.github.algorithm.gm.SM2;
import org.github.algorithm.gm.SM3;
import org.github.algorithm.gm.SM4;
import org.github.common.exception.CspException;
import org.github.common.exception.EncryptException;
import org.github.common.exception.HashException;
import org.github.common.exception.SignException;
import org.github.csp.ICsp;

import java.security.KeyPair;

/**
 * 国密算法软实现
 *
 * @author zhangmingyang
 * @Date: 2020/6/9
 * @company Dingxuan
 */
public class GmCsp implements ICsp {
    @Override
    public KeyPair genKeyPair() throws CspException {
        try {
            return new SM2().genKeyPair();
        } catch (SignException e) {
            throw new CspException(e.getMessage());
        }
    }

    @Override
    public String genRandom(int length) throws CspException {
        return null;
    }

    @Override
    public byte[] hash(byte[] data) throws CspException {
        try {
            return new SM3().hash(data);
        } catch (HashException e) {
            throw new CspException(e.getMessage());
        }
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws CspException {
        try {
            return new SM2().sign(data, privateKey, "SM3WithSM2");
        } catch (SignException e) {
            throw new CspException(e.getMessage());
        }
    }

    @Override
    public boolean verify(byte[] publicKey, byte[] data, byte[] signature) throws CspException {
        try {
            return new SM2().verify(data, publicKey, signature, "SM3WithSM2");
        } catch (SignException e) {
            throw new CspException(e.getMessage());
        }
    }

    @Override
    public byte[] asyEncrypt(byte[] publickey, byte[] data) throws CspException {
        return new SM2().encrypt(data, publickey);
    }

    @Override
    public byte[] asyDecrypt(byte[] privateKey, byte[] data) throws CspException {
        return new SM2().decrypt(data, privateKey);
    }

    @Override
    public byte[] sysEncrypt(byte[] key, byte[] data, byte[] iv, String algorithmType) throws CspException {
        try {
            return new SM4().encrypt(algorithmType, key, iv, data);
        } catch (EncryptException e) {
            throw new CspException(e.getMessage());
        }
    }

    @Override
    public byte[] sysDecrypt(byte[] key, byte[] data, byte[] iv, String algorithmType) throws CspException {
        try {
            return new SM4().decrypt(algorithmType, key, iv, data);
        } catch (EncryptException e) {
            throw new CspException(e.getMessage());
        }
    }
}
