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
package org.github.algorithm.impl.gm;

import org.github.algorithm.ICsp;
import org.github.common.CryptoException;

import java.security.KeyPair;

/**
 * @author zhangmingyang
 * @Date: 2020/4/27
 * @company Dingxuan
 */
public class GmCsp implements ICsp {
    public byte[] keyGenSymmetrical() throws CryptoException {
        return new byte[0];
    }

    public KeyPair genKeyPair(int keySize) throws CryptoException {
        return null;
    }

    public byte[] hash(byte[] msg) throws CryptoException {
        return new byte[0];
    }

    public byte[] sign(byte[] privateKeyIndex, byte[] msg) throws CryptoException {
        return new byte[0];
    }

    public boolean verify(byte[] key, byte[] signature, byte[] msg) throws CryptoException {
        return false;
    }

    public byte[] encrypt(byte[] key, byte[] plaintext, int type) throws CryptoException {
        return new byte[0];
    }

    public byte[] decrypt(byte[] key, byte[] ciphertext, int type) throws CryptoException {
        return new byte[0];
    }

    public byte[] rng(int len) throws CryptoException {
        return new byte[0];
    }

    public String getProvider() {
        return null;
    }
}
