package com.heim.netty;

import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;

public class NettySSlHandler extends SslHandler {

    static Logger logger = LoggerFactory.getLogger(NettySSlHandler.class);


    public NettySSlHandler(SSLEngine sslEngine) {
        super(sslEngine);
        ;
    }

}
