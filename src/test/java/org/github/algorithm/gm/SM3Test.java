package org.github.algorithm.gm;

import org.bouncycastle.util.encoders.Hex;
import org.github.common.exception.HashException;
import org.junit.Test;



/**
 * @author zhangmingyang
 * @Date: 2020/5/13
 * @company Dingxuan
 */
public class SM3Test {

    @Test
    public void hash() throws HashException {
        SM3 sm3=new SM3();
        byte[] hash = sm3.hash("123".getBytes());
        System.out.println(Hex.toHexString(hash));
    }
}