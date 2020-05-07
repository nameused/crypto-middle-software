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

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import org.github.bean.CryptoRequestParam;
import org.github.bean.CryptoResponse;

import java.io.UnsupportedEncodingException;

/**
 * @author zhangmingyang
 * @Date: 2020/5/6
 * @company Dingxuan
 */
public class CryptoServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(CryptoServerHandler.class);

    /**
     * channel 通道 action 活跃的
     * 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
     * channelAction
     *
     * @param ctx
     * @throws Exception
     */
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info(ctx.channel().localAddress().toString() + " 通道已激活！");
    }

    /**
     * channelInactive
     * channel 通道 Inactive 不活跃的
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
     *
     * @param ctx
     * @throws Exception
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(ctx.channel().localAddress().toString() + " 通道不活跃！");
        // 关闭流
    }

    /**
     * @param buf
     * @return
     * @author Taowd
     * TODO  此处用来处理收到的数据中含有中文的时出现乱码的问题
     */
    private String getMessage(ByteBuf buf) {
        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        try {
            return new String(con, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 功能：读取服务器发送过来的信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 第一种：接收字符串时的处理
//        ByteBuf buf = (ByteBuf) msg;
//        String rev = getMessage(buf);
//        log.info("服务端收到客户端的数据:" + rev);
        CryptoRequestParam cryptoRequestParam = (CryptoRequestParam) msg;
        String requestParam = JSON.toJSONString(cryptoRequestParam);
        log.info("服务端接收到的请求参数:" + requestParam);
        CryptoResponse cryptoResponse = new CryptoResponse();
        if (requestParam != null) {
            cryptoResponse.setCode(200);
            cryptoResponse.setData("resutlt");
        }
        ctx.writeAndFlush(cryptoResponse).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 功能：服务端发生异常的操作
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.error("异常信息：\r\n" + cause.getMessage());
    }
}
