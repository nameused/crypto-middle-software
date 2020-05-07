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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;
import org.github.bean.CryptoRequestParam;
import org.github.bean.CryptoResponse;
import org.github.service.CryptoServerHandler;

import java.awt.*;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangmingyang
 * @Date: 2020/5/6
 * @company Dingxuan
 */
public class CryptoClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(CryptoServerHandler.class);
    private ChannelHandlerContext ctx;
    private ChannelPromise promise;
    private CryptoResponse response;


    /**
     * 向服务端发送数据
     */
//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        log.info("客户端与服务端通道-开启：" + ctx.channel().localAddress() + "channelActive");
//        super.channelActive(ctx);
//        this.ctx = ctx;
//        String aaa="12";
//        ctx.writeAndFlush(aaa);
//    }

    /**
     * channelInactive
     * channel 通道 Inactive 不活跃的
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端与服务端通道-关闭：" + ctx.channel().localAddress() + "channelInactive");
    }

    //    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
//        System.out.println("读取客户端通道信息..");
//        ByteBuf buf = msg.readBytes(msg.readableBytes());
//        System.out.println("客户端接收到的服务端信息:" + ByteBufUtil.hexDump(buf) + "; 数据包为:" + buf.toString(Charset.forName("utf-8")));
//    }

    /**
     * 读取客户端通道信息,接收服务端发送的返回
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CryptoResponse message = (CryptoResponse) msg;
        if (message != null) {
            response = message;
            promise.setSuccess();
        } else {
            ctx.fireChannelRead(msg);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        System.out.println("异常退出:" + cause.getMessage());
    }


    public synchronized ChannelPromise sendMessage(Object message) {
        while (ctx == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error("等待ChannelHandlerContext实例化过程中出错", e);
            }
        }
        promise = ctx.newPromise();
        ctx.writeAndFlush(message);
        return promise;
    }

    public CryptoResponse getResponse() {
        return response;
    }

}
