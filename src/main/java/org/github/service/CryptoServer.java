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
package org.github.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;

/**
 * @author zhangmingyang
 * @Date: 2020/5/6
 * @company Dingxuan
 */
public class CryptoServer {
    private static final Logger log = Logger.getLogger(CryptoServer.class);
    private final int port;

    public CryptoServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.option(ChannelOption.SO_BACKLOG, 1024);
            // 绑定线程池
            sb.group(group, bossGroup)
                    // 指定使用的channel
                    .channel(NioServerSocketChannel.class)
                    // 绑定监听端口
                    .localAddress(this.port)
                    // 绑定客户端连接时候触发操作
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            log.info("报告");
                            log.info("信息：有一客户端链接到本服务端");
                            log.info("IP:" + ch.localAddress().getAddress());
                            log.info("Port:" + ch.localAddress().getPort());
                            log.info("报告完毕");
                            ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                            //客户端触发操作
                            ch.pipeline().addLast(new CryptoServerHandler());
                            ch.pipeline().addLast(new ByteArrayEncoder());
                        }
                    });
            // 服务器异步创建绑定
            ChannelFuture cf = sb.bind().sync();
            System.out.println(CryptoServer.class + " 启动正在监听： " + cf.channel().localAddress());
            // 关闭服务器通道
            cf.channel().closeFuture().sync();
        } finally {
            // 释放线程池资源
            group.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        // 启动
        new CryptoServer(8888).start();
    }
}
