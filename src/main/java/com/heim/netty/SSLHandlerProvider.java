
package com.heim.netty;

import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

@ChannelHandler.Sharable
public class SSLHandlerProvider {
    private static final Logger logger = LoggerFactory.getLogger(SSLHandlerProvider.class);

    private static final String PROTOCOL = "TLS";
    private static final String ALGORITHM_SUN_X509 = "SunX509";
    private static final String ALGORITHM = "ssl.KeyManagerFactory.algorithm";
    private static final String KEYSTORE = "mysslstore.jks";
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEYSTORE_PASSWORD = "123456";
    private static final String CERT_PASSWORD = "123456";
    private static SSLContext serverSSLContext = null;
    public static SSLEngine sslEngine = null;

    public static SSLContext getSslContext() {
        if (serverSSLContext != null) return serverSSLContext;
        return null;
    }


    public static SslContext getContext() {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();

//            SSLEngine engine = context.createSSLEngine();
//            engine.setUseClientMode(false);
//            engine.setEnabledProtocols(engine.getSupportedProtocols());
//            engine.setEnabledCipherSuites(engine.getSupportedCipherSuites());
//            engine.setEnableSessionCreation(true);

            SslContext sslCtx = SslContextBuilder.
                    forServer(ssc.certificate(), ssc.privateKey())

                    //.sslProvider(SslProvider.JDK)
                    //.protocols("TLSv1.2", "SSLv3", "TLSv1", "TLSv1.1")
                    //.startTls(true)
                    .build();


            return sslCtx;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//
//    public static SSLEngine initSSLContext() {
//        if (sslEngine != null)
//            return sslEngine;
//
//        logger.info("Initiating SSL context");
//        String algorithm = Security.getProperty(KeyManagerFactory.getDefaultAlgorithm());
//        if (algorithm == null) {
//            algorithm = ALGORITHM_SUN_X509;
//        }
//        KeyStore ks = null;
//        InputStream inputStream = null;
//        try {
//            inputStream = new FileInputStream(SSLHandlerProvider.class.getClassLoader().
//                    getResource(KEYSTORE).getFile());
//            ks = KeyStore.getInstance(KEYSTORE_TYPE);
//            ks.load(inputStream, KEYSTORE_PASSWORD.toCharArray());
//        } catch (IOException e) {
//            logger.error("Cannot load the keystore file", e);
//        } catch (CertificateException e) {
//            logger.error("Cannot get the certificate", e);
//        } catch (NoSuchAlgorithmException e) {
//            logger.error("Somthing wrong with the SSL algorithm", e);
//        } catch (KeyStoreException e) {
//            logger.error("Cannot initialize keystore", e);
//        } finally {
//            try {
//                if (inputStream != null)
//                    inputStream.close();
//            } catch (IOException e) {
//                logger.error("Cannot close keystore file stream ", e);
//            }
//        }
//        try {
//
//            // Set up key manager factory to use our key store
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
//            kmf.init(ks, CERT_PASSWORD.toCharArray());
//            KeyManager[] keyManagers = kmf.getKeyManagers();
//            TrustManager[] trustManagers = null;
//
//            serverSSLContext = SSLContext.getInstance(PROTOCOL);
//            serverSSLContext.
//                    init(keyManagers, trustManagers, null);
//
////
//            sslEngine = serverSSLContext.createSSLEngine();
//            sslEngine.setEnabledProtocols(sslEngine.getSupportedProtocols());
//            sslEngine.setEnabledCipherSuites(sslEngine.getSupportedCipherSuites());
//            sslEngine.setUseClientMode(false);
//            sslEngine.setNeedClientAuth(false);
//            sslEngine.setEnableSessionCreation(true);
//
//            return sslEngine;
//        } catch (Exception e) {
//            logger.error("Failed to initialize the server-side SSLContext", e);
//        }
//
//        return null;
//
//    }


}