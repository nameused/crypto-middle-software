package org.github.algorithm.gm;

import org.bouncycastle.util.encoders.Hex;
import org.github.common.exception.EncryptException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author zhangmingyang
 * @Date: 2020/5/13
 * @company Dingxuan
 */
public class SM4Test {


    @Test
    public void encryptTest() throws EncryptException {
        SM4 sm4 = new SM4();
        byte[] encryptData = sm4.encrypt("SM4/ECB/PKCS5Padding", Hex.decode("75e2ed6910aee1317761d8e7b1c623d3"), null, "123".getBytes());
        System.out.println(Hex.toHexString(encryptData));
    }
}