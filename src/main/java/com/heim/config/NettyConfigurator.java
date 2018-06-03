package com.heim.config;

//import com.heim.service.XmlHandler;


import com.heim.service.XmppMessageHandler;
import org.apache.camel.component.xmpp.XmppComponent;
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

//    @Autowired
//    XmlHandler xmlHandler;

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
//        CamelContext context = getContext();
//
//        XmppEndpoint endpoint =
//                getContext().getEndpoint
//                        ("xmpp://sergey@localhost:5222?password=secret",
//                                XmppEndpoint.class);
        XmppComponent xmppComponent = getContext().getComponent("xmpp", XmppComponent.class);

//        SharedConnectionXmppEndpoint
//                endpoint2 = new SharedConnectionXmppEndpoint(
//                "xmpp://sergey@localhost:5222/test?password=secret",
//                xmppComponent);
//
//        from(endpoint2).
//                setBody(constant("I will win!\n Your Superman.")).
//                to(endpoint2);


    }


}