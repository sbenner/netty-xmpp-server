//package com.heim.config;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.handler.codec.xml.XmlDecoder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ServerCommandHandler extends ChannelInboundHandlerAdapter {
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ctx.pipeline().addFirst("decoder", new XmlDecoder());
//        ctx.fireChannelRead(msg);
//    }
//}
