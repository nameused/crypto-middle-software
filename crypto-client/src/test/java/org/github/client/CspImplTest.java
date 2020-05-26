package org.github.client;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author zhangmingyang
 * @Date: 2020/5/25
 * @company Dingxuan
 */
public class CspImplTest {
    private CryptoClient cryptoClient;
    private CspImpl csp;

    @Before
    public void init() throws IOException {
        cryptoClient = new CryptoClient("localhost", 9998);
        csp = new CspImpl(cryptoClient);
    }

    @Test
    public void getServerPublicKey() {
        System.out.println("服务端公钥:" + csp.getServerPublicKey());
    }

    @Test
    public void getAppKey() {
    }

    @Test
    public void hash() {
    }
}