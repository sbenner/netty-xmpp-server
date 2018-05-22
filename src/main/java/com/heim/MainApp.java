package com.heim;

import com.heim.utils.DefaultPropertiesApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A Camel Application
 */

@SpringBootApplication
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */


    public static void main(String... args) throws Exception {

        DefaultPropertiesApplication.run(MainApp.class, args);
//     //   Main main = new Main();
//       // main.addRouteBuilder(new MyRouteBuilder());
//        trustall();
//        EmbeddedXmppTestServer server = new EmbeddedXmppTestServer();
//        Thread.sleep(5000);
//        XmppEndpoint endpoint  = new XmppEndpoint();
//        endpoint.setHost("localhost");
//        endpoint.setPort(5222);
//        endpoint.setBinding(new XmppBinding());
//        endpoint.setServiceName("camel-test");
//        endpoint.setRoom("camel-test");
//        endpoint.setUser("camel_producer");
//
//        endpoint.setPassword("secret");
//        XmppProducer producer =
//                new XmppProducer(endpoint);
//
//        producer.start();

       //// server.startXmppEndpoint();
        //main.run(args);
    }

}

