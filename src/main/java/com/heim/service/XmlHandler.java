//package com.heim.service;
//
//import io.netty.channel.ChannelDuplexHandler;
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelPipeline;
//import io.netty.handler.codec.bytes.ByteArrayDecoder;
//import org.springframework.stereotype.Component;
//
///*
// * Регистрирует другие хэнлеры в pipeline netty
// * Т.к. мы можем добавить netty ChannelHandler только в encoder,
// * то мы используем его тоглько для регистрации других хэндлеров,
// * а его после регистрации удаляем.
// */
//@Component
//@ChannelHandler.Sharable
//public class XmlHandler extends ChannelDuplexHandler {
//
//    @Override
//    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//        ChannelPipeline pipeline = ctx.pipeline();
//        ChannelHandler channelHandler = pipeline.get("xml-decoder");
//        if (channelHandler == null) {
//            pipeline.addFirst("xml-decoder", new ByteArrayDecoder());
//            ctx.pipeline().fireChannelRegistered();
//        }
//
//        //  ctx.pipeline().remove(this);
//
//    }
//}