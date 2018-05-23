package com.heim.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class XmppMessageHandler
        implements Processor {


    @Override
    public void process(Exchange exchange) throws Exception {
        //Exchange ex = new XmppEndpoint().createExchange(exchange);
        System.out.println(exchange.getIn().getBody().toString());
    }
}
