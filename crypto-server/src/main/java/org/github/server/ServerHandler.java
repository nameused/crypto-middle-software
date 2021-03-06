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
package org.github.server;

import org.github.csp.ICsp;
import org.github.manage.CspManager;
import org.github.process.ProcessData;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * @author zhangmingyang
 * @Date: 2020/5/7
 * @company Dingxuan
 */
public class ServerHandler implements Runnable {
    private static final Logger log = Logger.getLogger(ServerHandler.class);

    private Socket socket;
    private  ICsp iCsp;

    public ServerHandler(Socket client) {
        this.socket = client;
        this.iCsp= CspManager.getDefaultCsp();
        new Thread(this).start();
    }

    public void run() {
        //读取客户端发送来的消息
        log.info("客户端数据已经连接");
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
            log.info("接收到客户端数据:" + strInputstream);
            socket.shutdownInput();
            baos.close();

            //处理数据并写入
            String result = new ProcessData(iCsp).process(strInputstream);
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
