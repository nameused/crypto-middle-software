package org.github.algorithm.gm;

import org.bouncycastle.util.encoders.Base64;
import org.github.common.exception.SignException;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.*;

/**
 * @author zhangmingyang
 * @Date: 2020/5/14
 * @company Dingxuan
 */
public class SM2Test {
    private SM2 sm2;

    @Before
    public void init() {
    sm2=new SM2();
    }

    @Test
    public void genKeyPair() throws SignException {
        KeyPair keyPair = sm2.genKeyPair();
        System.out.println("privateKey:" + Base64.toBase64String(keyPair.getPrivate().getEncoded()));
        System.out.println("publicKey:" + Base64.toBase64String(keyPair.getPublic().getEncoded()));
    }
}
