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
package org.github.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.Logger;
import org.github.bean.CryptoRequestParam;
import org.github.bean.CryptoResponse;
import org.github.service.CryptoServerHandler;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangmingyang
 * @Date: 2020/5/6
 * @company Dingxuan
 */
public class CryptoClient {
    private static final Logger log = Logger.getLogger(CryptoServerHandler.class);
    private final String host;
    private final int port;
    /**
     * 客户端业务处理handler
     */
    private CryptoClientHandler clientHandler = new CryptoClientHandler();

    public CryptoClient(String host, int port) throws InterruptedException {
        this.host = host;
        this.port = port;
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            // 注册线程池
            b.group(group)
                    // 使用NioSocketChannel来作为连接用的channel类
                    .channel(NioSocketChannel.class)
                    // 绑定连接端口和host信息
                    .remoteAddress(new InetSocketAddress(this.host, this.port))
                    // 绑定连接初始化器
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            log.info("正在连接中...");
                            ch.pipeline().addLast(new StringEncoder(Charset.forName("UTF-8")));
                            ch.pipeline().addLast(new CryptoClientHandler());
                            ch.pipeline().addLast(new ByteArrayEncoder());
                            ch.pipeline().addLast(new ChunkedWriteHandler());

                        }
                    });
            // 异步连接服务器
            ChannelFuture cf = b.connect().sync();
            // 连接完成
            log.info("服务端连接成功...");
            // 异步等待关闭连接channel
            cf.channel().closeFuture().sync();
            // 关闭完成
            log.info("连接已关闭...");
        } finally {
            // 释放线程池资源
            group.shutdownGracefully().sync();
        }
    }

    public CryptoResponse send(String cryptoRequestParam) throws Exception {
        ChannelPromise promise = clientHandler.sendMessage(cryptoRequestParam);
        promise.await(3, TimeUnit.SECONDS);
        return clientHandler.getResponse();
    }

    public static void main(String[] args) throws Exception {
        // 连接127.0.0.1/65535，并启动
        CryptoRequestParam cryptoRequestParam = new CryptoRequestParam();
        cryptoRequestParam.setRequestId(001);
        cryptoRequestParam.setRequestType(100);
        cryptoRequestParam.setRequsetData("123");
        CryptoResponse aaa = new CryptoClient("127.0.0.1", 8888).send(JSON.toJSONString(cryptoRequestParam));
        System.out.println(aaa.getCode());
    }
}
