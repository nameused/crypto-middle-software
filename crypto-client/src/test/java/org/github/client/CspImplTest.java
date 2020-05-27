package org.github.client;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.github.algorithm.gm.SM3;
import org.github.algorithm.gm.SM4;
import org.github.bean.AsymmetricKeyPair;
import org.github.common.exception.EncryptException;
import org.github.common.exception.HashException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author zhangmingyang
 * @Date: 2020/5/27
 * @company Dingxuan
 */
public class CspImplTest {
    private CryptoClient cryptoClient;
    private CspImpl csp;
    private final String TEST_APP_CODE = "test";
    private final String SERVER_PUBLICK_KEY = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEl4/Gl2rrNqIDEGOXGyf39t2s6Uq00GbKEMgQBJr4z+rqS3v7sLas8kjpUxnK3+0z/81VO1b5SZaZ0eFgeW/71g==";
    private final String TEST_APPKEY = "lL3OvhkIPOKh+Vn9Avlkxw==";
    private final String SM4_TEST_KEY = "+svJ7GvNGgrpLiOW3nWJ9A==";

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
    public void genAppKey() {
        System.out.println("appkey:" + csp.genAppKey("test"));
    }

    @Test
    public void sendAppKey() {
        csp.sendAppKey(SERVER_PUBLICK_KEY, TEST_APPKEY, TEST_APP_CODE);
    }

    @Test
    public void hash() throws HashException {
        String hash = csp.hash(TEST_APPKEY, TEST_APP_CODE, "123".getBytes());
        System.out.println("数据SM3的哈希值:" + hash);
        System.out.println("转换为字节数组后的16进制形式:" + Hex.toHexString(Base64.decode(hash)));
        SM3 sm3 = new SM3();
        System.out.println("计算hash：" + Hex.toHexString(sm3.hash("123".getBytes())));
    }

    @Test
    public void getSymmetryKey() {
        System.out.println("SM4密钥:" + csp.getSymmetryKey(TEST_APPKEY, TEST_APP_CODE));
    }

    @Test
    public void getAsymmetricKey() {
        AsymmetricKeyPair asymmetricKeyPair = csp.getAsymmetricKey(TEST_APPKEY, TEST_APP_CODE);
        System.out.println("公钥:" + asymmetricKeyPair.getPublicKey());
        System.out.println("私钥:" + asymmetricKeyPair.getPrivateKey());
    }

    @Test
    public void sign() {
        String sign = csp.sign(TEST_APPKEY, TEST_APP_CODE, "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgi6j55YWpGhPwb+qBOF8tyXsWSECShyigmaxL7UorGWygCgYIKoEcz1UBgi2hRANCAASlWNUQDaadijft9JJ7GVhB1h8sDcT/pxO1tAfAMiMfUKHpMtKD70HBr1Wbr5kJY0P6RcnsNXxs31h6oUOXLged", "123".getBytes());
        System.out.println("签名值:" + sign);
    }

    @Test
    public void verfiy() {
        String verfiy = csp.verfiy(TEST_APPKEY, TEST_APP_CODE, "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEpVjVEA2mnYo37fSSexlYQdYfLA3E/6cTtbQHwDIjH1Ch6TLSg+9Bwa9Vm6+ZCWND+kXJ7DV8bN9YeqFDly4HnQ==", "123".getBytes(), "MEUCIFmeIEpafQi4pq33NmQs2kNGvKUi+zdMZ5gdxykO/RMvAiEA8FntLcDG/T1P4YYy6aL8Fwv+w4pTTM+qztCpALFETWs=");
        System.out.println(verfiy);

    }

    @Test
    public void encrypt() throws EncryptException {
        String result = csp.encrypt(TEST_APPKEY, TEST_APP_CODE, SM4_TEST_KEY, "123".getBytes());
        System.out.println(result);
    }

    @Test
    public void decrypt() {
        String result = "/Cp+Sgj6H/paLqvvFMO4yA==";
        String plainText = csp.decrypt(TEST_APPKEY, TEST_APP_CODE, SM4_TEST_KEY, Base64.decode(result));
        System.out.println(plainText);
    }
}