package org.github.client;

import com.alibaba.fastjson.JSON;
import org.bouncycastle.util.encoders.Base64;
import org.github.algorithm.factor.SecurityDigest;
import org.github.algorithm.gm.SM2;
import org.github.algorithm.gm.SM3;
import org.github.algorithm.gm.SM4;
import org.github.bean.CryptoRequest;
import org.github.common.exception.EncryptException;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @author zhangmingyang
 * @Date: 2020/5/18
 * @company Dingxuan
 */
public class CryptoClientTest {
    private CryptoClient client;
    private CryptoRequest cryptoRequest;
    private Map<String, String> headerMap;
    private Map<String, String> bodyMap;
    private SM2 sm2;
    private SM3 sm3;
    private SM4 sm4;
    private SecurityDigest securityDigest;

    @Before
    public void init() throws IOException {
        client = new CryptoClient("localhost", 9998);
        cryptoRequest = new CryptoRequest();
        headerMap = new HashMap<String, String>();
        bodyMap = new HashMap<String, String>();
        sm2 = new SM2();
        sm3 = new SM3();
        sm4 = new SM4();
        securityDigest = new SecurityDigest();
    }

    /**
     * 向服务端发送公钥请求
     */
    @Test
    public void serverPublickeyRequestTest() {
        Map<String, String> map = new HashMap();
        map.put("message_type", "publicKeyRequest");
        String publickeyJson = JSON.toJSONString(map);
        String result = client.send(publickeyJson);
        System.out.println(result);
    }


    /**
     * 利用服务端公钥加密
     * 生成的appKey发送到服务端
     */

    @Test
    public void appKeyRequestTest() throws EncryptException {
        SecurityDigest securityDigest = new SecurityDigest();
        //生成appkey
        byte[] appkey = securityDigest.genAppkey("test");
        //公钥转换
        byte[] sm2publicKey = Base64.decode("MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEl4/Gl2rrNqIDEGOXGyf39t2s6Uq00GbKEMgQBJr4z+rqS3v7sLas8kjpUxnK3+0z/81VO1b5SZaZ0eFgeW/71g==");
        //保存生成的appkey密钥
        System.out.println("appkey密钥：" + Base64.toBase64String(appkey));
        //服务公钥加密appkey
        byte[] encryptAppkey = sm2.encrypt(appkey, sm2publicKey);
        System.out.println("公钥加密后的值：" + Base64.toBase64String(encryptAppkey));
        //组装appkey请求数据
        Map<String, String> map = new HashMap();
        map.put("message_type", "appKeyRequest");
        map.put("app_code", "test");
        map.put("app_key", Base64.toBase64String(encryptAppkey));
        String appKeyRequestData = JSON.toJSONString(map);
        System.out.println("发送的数据为：" + appKeyRequestData);
        client.send(appKeyRequestData);
    }


    /**
     * SM3 模拟请求
     */
    @Test
    public void sm3RequestTest() throws Exception {
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm3_hash");
        bodyMap.put("data", Base64.toBase64String("234324".getBytes()));
        String bodyJson = JSON.toJSONString(bodyMap);
        //先使用appkey加密数据
        byte[] bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="), null, bodyJson.getBytes());
        bodyMap.clear();
        bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
        System.out.println(JSON.toJSONString(bodyMap));
        //然后利用appkey进行sm3的hmac计算
        byte[] signFactorByte = securityDigest.genSignFactor();
        String signFactor = Base64.toBase64String(signFactorByte);
        byte[] keyS = securityDigest.genKeyS(signFactorByte, Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="));
        byte[] hmac = securityDigest.hamc(keyS, bodyByte);
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", "office2");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        System.out.println("客户端发送的数据:" + JSON.toJSONString(cryptoRequest));
        client.send(JSON.toJSONString(cryptoRequest));
    }



    @Test
    public void sm2KeyPariGenRequestTest() throws Exception {

        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm2_keypair_gen");
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="), null, bodyJson.getBytes());
        bodyMap.clear();
        bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));

        byte[] signFactorByte = securityDigest.genSignFactor();
        String signFactor = Base64.toBase64String(signFactorByte);
        byte[] keyS = securityDigest.genKeyS(signFactorByte, Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="));
        byte[] hmac = securityDigest.hamc(keyS, bodyByte);
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", "office2");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));

    }


    /**
     * SM2 签名模拟请求
     */
    @Test
    public void sm2SignRequestTest() throws Exception {
        String privateKey="MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQg7M7Xoq75JNKf+45zWuk8YqdwkrNuKWtNHOKoweek2DugCgYIKoEcz1UBgi2hRANCAAShjQPBf/ipCnRn+e3Q1Z/aYPF1uPri+GELFWgKNgmap7BayScZMee5oKwZ3Hvj9fZGIMkr6PPBSIlSfm9ivs5z";
        String data="123434";
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm2_sign");
        bodyMap.put("key", privateKey);
        bodyMap.put("data", data);
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="), null, bodyJson.getBytes());
        bodyMap.clear();
        bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));

        byte[] signFactorByte = securityDigest.genSignFactor();
        String signFactor = Base64.toBase64String(signFactorByte);
        byte[] keyS = securityDigest.genKeyS(signFactorByte, Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="));
        byte[] hmac = securityDigest.hamc(keyS, bodyByte);
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", "office2");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));

    }

    /**
     * SM2 验证签名模拟请求
     */
    @Test
    public void sm2VerifyRequestTest() throws Exception {
        String publicKey="MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEoY0DwX/4qQp0Z/nt0NWf2mDxdbj64vhhCxVoCjYJmqewWsknGTHnuaCsGdx74/X2RiDJK+jzwUiJUn5vYr7Ocw==";
        String data="123434";
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm2_verify");
        bodyMap.put("sign_value","MEYCIQCVhIOoUArABEkUMb4LOObHd+nAa/nBpoK2vyVsJCOsZQIhALbWDMIknIKzJWOh73EcZEAOm/1jWRlrvH7fjiP5tx7a");
        bodyMap.put("key", publicKey);
        bodyMap.put("data", data);
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="), null, bodyJson.getBytes());
        bodyMap.clear();
        bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));

        byte[] signFactorByte = securityDigest.genSignFactor();
        String signFactor = Base64.toBase64String(signFactorByte);
        byte[] keyS = securityDigest.genKeyS(signFactorByte, Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="));
        byte[] hmac = securityDigest.hamc(keyS, bodyByte);
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", "office2");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));


    }

    /**
     * 构造sm4密钥生成请求,并发送
     */
    @Test
    public void genSM4KeyRequestTest() throws Exception {
        cryptoRequest.setRequestId("123456");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm4_key_gen");
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="), null, bodyJson.getBytes());
        bodyMap.clear();
        bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
        byte[] signFactorByte = securityDigest.genSignFactor();
        String signFactor = Base64.toBase64String(signFactorByte);
        byte[] keyS = securityDigest.genKeyS(signFactorByte, Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="));
        byte[] hmac = securityDigest.hamc(keyS, bodyByte);
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", "office2");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));
    }


    /**
     * SM4加密报文模拟请求
     */

    @Test
    public void sm4EncryptRequestTest() throws Exception {
        String sm4Key="NWCep2qxC86wDVEYgiVI3A==";
        String data="123434";
        cryptoRequest.setRequestId("34323434");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm4_encrypt");
        bodyMap.put("key", sm4Key);
        bodyMap.put("data", data);
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="), null, bodyJson.getBytes());
        bodyMap.clear();

        bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
        byte[] signFactorByte = securityDigest.genSignFactor();
        String signFactor = Base64.toBase64String(signFactorByte);
        byte[] keyS = securityDigest.genKeyS(signFactorByte, Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="));
        byte[] hmac = securityDigest.hamc(keyS, bodyByte);
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", "office2");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));
    }

    /**
     * SM4数据解密请求
     */
    @Test
    public void sm4DecryptRequestTest() throws Exception {
        String sm4Key="NWCep2qxC86wDVEYgiVI3A==";
        String encryptData="5Gm/rNNqonptx/bP0GNQhQ==";
        cryptoRequest.setRequestId("34323434");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm4_decrypt");
        bodyMap.put("key", sm4Key);
        bodyMap.put("data", encryptData);
        String bodyJson = JSON.toJSONString(bodyMap);
        byte[] bodyByte = sm4.encrypt("SM4/ECB/PKCS5Padding", Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="), null, bodyJson.getBytes());
        bodyMap.clear();

        bodyMap.put("body_encrypt_data", Base64.toBase64String(bodyByte));
        byte[] signFactorByte = securityDigest.genSignFactor();
        String signFactor = Base64.toBase64String(signFactorByte);
        byte[] keyS = securityDigest.genKeyS(signFactorByte, Base64.decode("joW9/ON9n+Mc3rv4b3yS0Q=="));
        byte[] hmac = securityDigest.hamc(keyS, bodyByte);
        headerMap.put("sign_factor", signFactor);
        headerMap.put("hmac_value", Base64.toBase64String(hmac));
        headerMap.put("app_code", "office2");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));

    }
}