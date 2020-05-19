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
        client.send(publickeyJson);
    }


    /**
     * 利用服务端公钥加密
     * 生成的appKey发送到服务端
     */

    @Test
    public void appKeyRequestTest() throws EncryptException {
        SecurityDigest securityDigest = new SecurityDigest();
        byte[] appkey = securityDigest.genAppkey("office2");
        byte[] sm2publicKey = Base64.decode("MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEl4/Gl2rrNqIDEGOXGyf39t2s6Uq00GbKEMgQBJr4z+rqS3v7sLas8kjpUxnK3+0z/81VO1b5SZaZ0eFgeW/71g==");
        System.out.println("appkey密钥：" + Base64.toBase64String(appkey));
        byte[] encryptAppkey = sm2.encrypt(appkey, sm2publicKey);
        System.out.println("公钥加密后的值：" + Base64.toBase64String(encryptAppkey));
        Map<String, String> map = new HashMap();
        map.put("message_type", "appKeyRequest");
        map.put("app_code", "office2");
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
        cryptoRequest.setRequestId("12323432");
        cryptoRequest.setMessageType("cryptoRequest");
        bodyMap.put("invoke_type", "sm3_hash");
        bodyMap.put("data", "fjikwer");
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

    /**
     * SM2 签名模拟请求
     */
    @Test
    public void sm2SignRequestTest() {
        cryptoRequest.setRequestId("34323434");
        cryptoRequest.setMessageType("cryptoRequest");
        headerMap.put("sign_factor", "sm2_sign");
        headerMap.put("signed_data", "23432445");
        bodyMap.put("invoke_type", "sm2_sign");
        bodyMap.put("key", "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgHF82vK5NK7UqTFy4wDawQJN4XczwWGq1KswbG+by94CgCgYIKoEcz1UBgi2hRANCAAQ0TGdM5zr2bUBFj6DrEBFWa4vjx8zMjdFYSiqAZ3NAYSXkE9N9kORAO5nohc6Cx2b7bSx7jDlMFonao5hwapFk");
        bodyMap.put("data", "123434");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));
    }

    /**
     * SM2 验证签名模拟请求
     */
    @Test
    public void sm2VerifyRequestTest() {
        cryptoRequest.setRequestId("34323434");
        cryptoRequest.setMessageType("cryptoRequest");
        headerMap.put("sign_factor", "sm2_verify");
        headerMap.put("signed_data", "23432445");
        bodyMap.put("invoke_type", "sm2_verify");
        bodyMap.put("key", "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAENExnTOc69m1ARY+g6xARVmuL48fMzI3RWEoqgGdzQGEl5BPTfZDkQDuZ6IXOgsdm+20se4w5TBaJ2qOYcGqRZA==");
        bodyMap.put("data", "123434");
        bodyMap.put("sign_value", "MEUCIQCtSTKTBX24JOi2WFnUqGCv2fHf/ewaD0YM5aZp4UCRoQIgAcWDx9ypDoUN1vp3JzqqfX7AgxhwyA5kZfohbbWsLvo=");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));
    }

    /**
     * SM4加密报文模拟请求
     */

    @Test
    public void sm4EncryptRequestTest() {
        cryptoRequest.setRequestId("34323434");
        cryptoRequest.setMessageType("cryptoRequest");
        headerMap.put("sign_factor", "998023432492304");
        headerMap.put("signed_data", "23432445");
        bodyMap.put("invoke_type", "sm4_encrypt");
        bodyMap.put("key", "f5503ad44220df1683a5b3da206eddbe");
        bodyMap.put("data", "123434");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));
    }

    /**
     * SM4数据解密请求
     */
    @Test
    public void sm4DecryptRequestTest() {
        cryptoRequest.setRequestId("34323434");
        cryptoRequest.setMessageType("cryptoRequest");
        headerMap.put("sign_factor", "998023432492304");
        headerMap.put("signed_data", "23432445");
        bodyMap.put("invoke_type", "sm4_decrypt");
        bodyMap.put("key", "f5503ad44220df1683a5b3da206eddbe");
        bodyMap.put("data", "ysDLZ6mMv5k05DW7Zz7TdA==");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        client.send(JSON.toJSONString(cryptoRequest));
    }
}