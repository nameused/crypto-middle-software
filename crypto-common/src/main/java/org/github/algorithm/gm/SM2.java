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

import org.github.common.exception.SignException;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;


import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author zhangmingyang
 * @Date: 2020/5/13
 * @company Dingxuan
 */
public class SM2 extends GmBase {
    private static final Logger log = Logger.getLogger(SM2.class);
    private static final String KEY_ALGORITHM = "EC";
    private static final String PROVIDER = "BC";
    private static final String KEY_GEN_PARAMETER = "sm2p256v1";
    private static X9ECParameters x9ECParameters = GMNamedCurves.getByName(KEY_GEN_PARAMETER);
    private static ECDomainParameters ecDomainParameters = new ECDomainParameters(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());

    public KeyPair genKeyPair() throws SignException {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(KEY_ALGORITHM, PROVIDER);
            generator.initialize(new ECNamedCurveGenParameterSpec(KEY_GEN_PARAMETER));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            log.error(e.getMessage());
            throw new SignException(e.getMessage(), e);
        }
        return generator.genKeyPair();
    }


    public byte[] sign(byte[] data, byte[] privateKey, String signatureAlgorithm) throws SignException {
        Signature signature;
        byte[] signValue;
        try {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(priKey);
            signature.update(data);
            signValue = signature.sign();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            log.error(e.getMessage());
            throw new SignException(e.getMessage(), e);
        }
        return signValue;
    }


    public boolean verify(byte[] data, byte[] publicKey, byte[] sign, String signatureAlgorithm) throws SignException {
        boolean verify;
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initVerify(pubKey);
            signature.update(data);
            verify = signature.verify(sign);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            log.error(e.getMessage());
            throw new SignException(e.getMessage(), e);
        }
        return verify;
    }

    /**
     * BC的实现按照老的c1||c2||c3的方式
     * 进行拼接
     *
     * @param data
     * @param publicKey
     * @return
     */
    public static byte[] sm2EncryptOld(byte[] data, byte[] publicKey) {

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = null;
        PublicKey pubKey = null;
        try {
            keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            pubKey = keyFactory.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        BCECPublicKey localECPublicKey = (BCECPublicKey) pubKey;
        ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(localECPublicKey.getQ(), ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(true, new ParametersWithRandom(ecPublicKeyParameters));
        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 旧版sm2私钥解密
     * c1||c2||c3
     *
     * @param data
     * @param key
     * @return
     */
    public static byte[] sm2DecryptOld(byte[] data, byte[] key) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = null;
        PrivateKey priKey=null;
        try {
            keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            priKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        BCECPrivateKey localECPrivateKey = (BCECPrivateKey) priKey;
        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(localECPrivateKey.getD(), ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, ecPrivateKeyParameters);
        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将c1||c3||c2 转化为c1||c2||c3 再进行解密
     *
     * @param c1c3c2
     * @return
     */
    public static byte[] changeC1C3C2ToC1C2C3(byte[] c1c3c2) {
        //sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c1Len = (x9ECParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32;
        byte[] result = new byte[c1c3c2.length];
        //c1: 0->65
        System.arraycopy(c1c3c2, 0, result, 0, c1Len);
        //c2
        System.arraycopy(c1c3c2, c1Len + c3Len, result, c1Len, c1c3c2.length - c1Len - c3Len);
        //c3
        System.arraycopy(c1c3c2, c1Len, result, c1c3c2.length - c3Len, c3Len);
        return result;
    }


    /**
     * bc加解密使用旧标c1||c2||c3，此方法在加密后调用，将结果转化为c1||c3||c2
     *
     * @param c1c2c3
     * @return
     */
    public static byte[] changeC1C2C3ToC1C3C2(byte[] c1c2c3) {
        //sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c1Len = (x9ECParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1;
        final int c3Len = 32;
        byte[] result = new byte[c1c2c3.length];
        //c1
        System.arraycopy(c1c2c3, 0, result, 0, c1Len);
        //c3
        System.arraycopy(c1c2c3, c1c2c3.length - c3Len, result, c1Len, c3Len);
        //c2
        System.arraycopy(c1c2c3, c1Len, result, c1Len + c3Len, c1c2c3.length - c1Len - c3Len);
        return result;
    }

    /**
     * c1||c3||c2 格式解密
     *
     * @param data
     * @param key
     * @return
     */
    public byte[] decrypt(byte[] data, byte[] key) {
        return sm2DecryptOld(changeC1C3C2ToC1C2C3(data), key);
    }

    /**
     * c1||c3||c2 格式加密
     *
     * @param data
     * @param key
     * @return
     */

    public byte[] encrypt(byte[] data, byte[] key) {
        return changeC1C2C3ToC1C3C2(sm2EncryptOld(data, key));
    }

    /**
     * DER编码C1C2C3密文（根据《SM2密码算法使用规范》 GM/T 0009-2012）
     *
     * @param cipher
     * @return
     * @throws IOException
     */
    public static byte[] encodeSM2CipherToDER(byte[] cipher) throws IOException {
        int curveLength = getCurveLength(ecDomainParameters);
        return encodeSM2CipherToDER(curveLength, 32, cipher);
    }

    /**
     * DER编码C1C3C2密文（根据《SM2密码算法使用规范》 GM/T 0009-2012）
     *
     * @param curveLength
     * @param digestLength
     * @param cipher
     * @return
     * @throws IOException
     */
    public static byte[] encodeSM2CipherToDER(int curveLength, int digestLength, byte[] cipher)
            throws IOException {
        int startPos = 1;
        byte[] c1x = new byte[curveLength];
        System.arraycopy(cipher, startPos, c1x, 0, c1x.length);
        startPos += c1x.length;
        byte[] c1y = new byte[curveLength];
        System.arraycopy(cipher, startPos, c1y, 0, c1y.length);
        startPos += c1y.length;
        byte[] c2 = new byte[cipher.length - c1x.length - c1y.length - 1 - digestLength];
        System.arraycopy(cipher, startPos, c2, 0, c2.length);
        startPos += c2.length;
        byte[] c3 = new byte[digestLength];
        System.arraycopy(cipher, startPos, c3, 0, c3.length);
        ASN1Encodable[] arr = new ASN1Encodable[4];
        arr[0] = new ASN1Integer(c1x);
        arr[1] = new ASN1Integer(c1y);
        arr[2] = new DEROctetString(c3);
        arr[3] = new DEROctetString(c2);
        DERSequence ds = new DERSequence(arr);
        return ds.getEncoded(ASN1Encoding.DER);
    }


    public static int getCurveLength(ECDomainParameters domainParams) {
        return (domainParams.getCurve().getFieldSize() + 7) / 8;
    }
}
