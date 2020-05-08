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
package org.github.socket;

import com.alibaba.fastjson.JSON;
import org.github.bean.CryptoRequest;
import org.github.bean.CryptoRequestParam;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangmingyang
 * @Date: 2020/5/7
 * @company Dingxuan
 */
public class Client {

    private Socket socket;
    private String ip;
    private int port;

    public Client(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        this.socket = new Socket(ip, port);
    }

    public void send(String jsonObject) {
        while (true) {
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = null;
                dataOutputStream = new DataOutputStream(outputStream);
                //向服务器端发送一条消息
                dataOutputStream.write(jsonObject.getBytes());
                dataOutputStream.flush();
                System.out.println("传输数据完毕");
                socket.shutdownOutput();
                //读取服务器返回的消息
                DataInputStream dataInputStream = null;
                String strInputstream = "";
                dataInputStream = new DataInputStream(new BufferedInputStream(inputStream));
                strInputstream = dataInputStream.readUTF();
                System.out.println("获取数据：" + strInputstream);
                if (strInputstream != null) {
                    System.out.println("客户端关闭连接");
                    Thread.sleep(500);
                    break;
                }
            } catch (Exception e) {
                System.out.println("客户端异常:" + e.getMessage());
                break;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        socket = null;
                        System.out.println("客户端 finally 异常:" + e.getMessage());
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 9998);
        CryptoRequest cryptoRequest = new CryptoRequest();
        cryptoRequest.setRequestId("12323432");
        cryptoRequest.setMessageType("cryptoRequest");
        Map<String, String> headerMap = new HashMap<String, String>();
        Map<String, String> bodyMap = new HashMap<String, String>();
        headerMap.put("sign_factor", "sm2_sign");
        headerMap.put("signed_data", "23432445");
        bodyMap.put("invoke_type", "sm3_hash");
        bodyMap.put("data", "fjikwer");
        cryptoRequest.setRequestHeader(headerMap);
        cryptoRequest.setRequestBody(bodyMap);
        String json = JSON.toJSONString(cryptoRequest);
        client.send(JSON.toJSONString(cryptoRequest));
    }


}
