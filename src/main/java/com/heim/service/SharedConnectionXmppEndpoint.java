//package com.heim.service;
//
//import org.apache.camel.component.xmpp.XmppComponent;
//import org.apache.camel.component.xmpp.XmppEndpoint;
//import org.jivesoftware.smack.*;
//import org.jivesoftware.smack.sasl.SASLMechanism;
//import org.jivesoftware.smack.tcp.XMPPTCPConnection;
//import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
//import org.jivesoftware.smack.util.ByteUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.security.auth.callback.CallbackHandler;
//import java.io.IOException;
//
//public class SharedConnectionXmppEndpoint extends XmppEndpoint {
//    Logger logger = LoggerFactory.getLogger(SharedConnectionXmppEndpoint.class);
//    ConnectionListener connectionListener = new ConnectionListener() {
//        @Override
//        public void connected(XMPPConnection xmppConnection) {
//            logger.info("xmpp", "connected");
//            try {
//                SASLAuthentication.registerSASLMechanism(new SASLMechanism() {
//                    @Override
//                    protected void authenticateInternal(CallbackHandler callbackHandler) throws SmackException {
//                        logger.info("test");
//                    }
//
//                    @Override
//                    protected byte[] getAuthenticationText() throws SmackException {
//                        byte[] authcid = toBytes(this.authenticationId);
//                        byte[] passw = toBytes(":" + this.password);
//                        return ByteUtils.concact(authcid, passw);
//                    }
//
//                    @Override
//                    public String getName() {
//                        return "PLAIN";
//                    }
//
//                    @Override
//                    public int getPriority() {
//                        return 410;
//                    }
//
//                    @Override
//                    public void checkIfSuccessfulOrThrow() throws SmackException {
//                        logger.info("test");
//                    }
//
//                    @Override
//                    protected SASLMechanism newInstance() {
//                        return this;
//                    }
//                });
//
//            } catch (Exception e) {
//                e.printStackTrace();
////                runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        Toast.makeText(NewClientActivity.this, "Incorrect username or password", Toast.LENGTH_LONG).show();
////                    }
////                });
//
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void authenticated(XMPPConnection connection, boolean resumed) {
//            logger.info("authenticated");
//        }
//
//        @Override
//        public void connectionClosed() {
//            logger.info("closed");
//        }
//
//        @Override
//        public void connectionClosedOnError(Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//
//        @Override
//        public void reconnectionSuccessful() {
//
//        }
//
//        @Override
//        public void reconnectingIn(int seconds) {
//
//        }
//
//        @Override
//        public void reconnectionFailed(Exception e) {
//
//        }
//    };
//    private XMPPTCPConnection xmppConnection;
//
//    public SharedConnectionXmppEndpoint(String uri, XmppComponent component
//    ) throws IOException, InterruptedException, XMPPException, SmackException {
//        super(uri, component);
//        XMPPTCPConnectionConfiguration configuration =
//                XMPPTCPConnectionConfiguration.builder()
//                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
//                        .setXmppDomain("localhost")
//                        .setDebuggerEnabled(true)
//                        .setUsernameAndPassword("sergey", "secret")
//                        .setHost("localhost")
//                        .setPort(5222)
//                        .build();
//        this.xmppConnection = new XMPPTCPConnection(configuration);
//        this.xmppConnection.addConnectionListener(connectionListener);
//        this.xmppConnection.setPacketReplyTimeout(10000);
//        this.xmppConnection.connect();
////        this.xmppConnection.login();
//
//    }
//
//    @Override
//    public XMPPTCPConnection createConnection() throws XMPPException, InterruptedException, IOException, SmackException {
//        if (xmppConnection != null) {
//            return xmppConnection;
//        }
//        return super.createConnection();
//    }
//}