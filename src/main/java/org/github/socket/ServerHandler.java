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
import com.alibaba.fastjson.JSONObject;
import org.github.bean.CryptoResponse;

import java.io.*;
import java.net.Socket;

/**
 * @author zhangmingyang
 * @Date: 2020/5/7
 * @company Dingxuan
 */
public class ServerHandler implements Runnable {
    private Socket socket;

    public ServerHandler(Socket client) {
        socket = client;
        new Thread(this).start();
    }

    public void run() {
        //读取客户端发送来的消息
        System.out.println("客户端数据已经连接");
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;
        String strInputstream = "";
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] by = new byte[2048];
            int n;
            while ((n = inputStream.read(by)) != -1) {
                baos.write(by, 0, n);
            }
            //获取客户端发送数据
            strInputstream = new String(baos.toByteArray());
            System.out.println(strInputstream);
            socket.shutdownInput();
            baos.close();

            //处理数据并写入

            JSONObject jsonObject = JSON.parseObject(strInputstream);
            int request_id = Integer.valueOf(jsonObject.getString("request_id"));
            String result = null;
            if (request_id == 1) {
                CryptoResponse cryptoResponse = new CryptoResponse();
                cryptoResponse.setCode(200);
                cryptoResponse.setData("result");
                result = JSON.toJSONString(cryptoResponse);
            }

            outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            outputStream.writeUTF(result);
            outputStream.flush();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    socket = null;
                    System.out.println("服务端 finally 异常:" + e.getMessage());
                }
            }
        }
    }
}
