package org.github.socket;

import com.alibaba.fastjson.JSON;
import org.github.bean.CryptoRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 报文发送测试类
 *
 * @author zhangmingyang
 * @Date: 2020/5/12
 * @company Dingxuan
 */
public class ClientTest {
    private Client client;
    private CryptoRequest cryptoRequest;
    private Map<String, String> headerMap;
    private Map<String, String> bodyMap;

    @Before
    public void init() throws IOException {
        client = new Client("localhost", 9998);
        cryptoRequest = new CryptoRequest();
        headerMap = new HashMap<String, String>();
        bodyMap = new HashMap<String, String>();
    }

    /**
     * SM3 模拟请求
     */
    @Test
    public void sm3RequestTest() {
        cryptoRequest.setRequestId("12323432");
        cryptoRequest.setMessageType("cryptoRequest");
        headerMap.put("sign_factor", "sm2_sign");
        headerMap.put("signed_data", "23432445");
        bodyMap.put("invoke_type", "sm3_hash");
        bodyMap.put("data", "fjikwer");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
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
        bodyMap.put("key", "0098a4dd5151a1630bfac2f8ed54bd6a18a5df68dbd4d9e591f4d872bd122c1bd0");
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
        bodyMap.put("key", "04ef882fcfaacf9d202f6f49c795dc54bc795234d11da590bfac331e5df525bd9ea98d2ee7d46b6718ee6dd2c739dda68a4c3be613576e674730e05e925c5e4e77");
        bodyMap.put("data", "123434");
        bodyMap.put("sign_value", "MEUCIQCIR4GpIUir8Yr2FRIynQAG++t0S8PAT2o2nRtXZ0wSLwIgA22MS6rlfS11Brs8Pl/Z7A8qrYyrcr6vsq5lV68lnEU=");
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

    /**
     * 向服务端发送公钥请求
     */
    @Test
    public void serverPublickeyRequestTest() {
        Map<String, String> map = new HashMap();
        map.put("message_type","publicKeyRequest");
        String publickeyJson=JSON.toJSONString(map);
        client.send(publickeyJson);
    }

}