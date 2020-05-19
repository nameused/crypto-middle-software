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


import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author zhangmingyang
 * @Date: 2020/5/7
 * @company Dingxuan
 */
public class CryptoServer {
    private static final Logger log = Logger.getLogger(CryptoServer.class);
    private int port;

    public CryptoServer(int port) {
        this.port = port;
    }

    public void init() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                // 一旦有堵塞, 则表示服务器与客户端获得了连接
                Socket client = serverSocket.accept();
                // 处理这次连接
                new ServerHandler(client);
            }
        } catch (Exception e) {
            log.error("服务器异常: " + e.getMessage());
        }
    }


    public static void main(String[] args) throws Exception {
        CryptoServer server = new CryptoServer(9998);
        log.info("密码服务启动中.....");
        server.init();
    }
}
