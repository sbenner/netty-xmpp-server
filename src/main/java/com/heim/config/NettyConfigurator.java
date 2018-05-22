package com.heim.config;

import com.heim.service.XmlHandler;
import com.heim.service.XmppMessageHandler;
import org.apache.camel.CamelContext;
import org.apache.camel.component.netty4.NettyComponent;
import org.apache.camel.component.netty4.NettyConfiguration;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 3/21/17
 * Time: 7:10 PM
 */
@Configuration
public class NettyConfigurator extends SpringRouteBuilder {

    @Autowired
    XmppMessageHandler messageHandler;

    @Autowired
    XmlHandler xmlHandler;

//    @PostConstruct
//    public void init(){
//        CamelContext context = getContext();
//        NettyConfiguration configuration = new NettyConfiguration();
//        configuration.setDecoder(xmlHandler);
//        ((NettyComponent)context.
//                getComponent("netty4")).setConfiguration(configuration);
//    }

    @Override
    public void configure() throws Exception {
        CamelContext context = getContext();
        NettyConfiguration configuration = new NettyConfiguration();
        configuration.setDecoder(xmlHandler);
        ((NettyComponent) context.
                getComponent("netty4")).setConfiguration(configuration);
        from("netty4:tcp://localhost:5222")
                .process(messageHandler);


    }


}