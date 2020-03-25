package org.openshift.quickstarts.todolist.security;

import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;


public class SSLSocketClientWithClientAuthTest {
    String CERTFILE = "/Users/robinjohnhopkins/workspace/openshift-quickstarts/sr-tomtest.apps.ca-central-1.starter.openshift-online.com.cert";
    String KEYFILE = "/Users/robinjohnhopkins/workspace/openshift-quickstarts/sr-tomtest.apps.ca-central-1.starter.openshift-online.com.key";

    // Note to self - the above self signed certs which I got from an online site, work with
    // SecurityHelper code (bouncycastle) but not PemReader (throws an exception)
    // create keystore:
    //  pwd
    //  /Users/robinjohnhopkins/workspace/openshift-quickstarts/openshift-quickstarts/todolist
    //  openssl pkcs12 -export -in ../../sr-tomtest.apps.ca-central-1.starter.openshift-online.com.cert -inkey ../../sr-tomtest.apps.ca-central-1.starter.openshift-online.com.key -certfile ../../sr-tomtest.apps.ca-central-1.starter.openshift-online.com.cert -out keystore.p12
    //
    //  pw=password
    //  keytool -importkeystore -srckeystore keystore.p12 -srcstoretype pkcs12 -destkeystore keystore.jks -deststoretype JKS

    //@Test
    //    public void test() throws IOException, GeneralSecurityException {
    //        Assert.assertEquals(1,1);
    //        File certificateChainFile = new File(CERTFILE);
    //
    //        File privateKeyFile = new File(KEYFILE);
    //
    //        KeyStore ks = PemReader.loadKeyStore(certificateChainFile, privateKeyFile, Optional.empty());
    //
    //        Assert.assertNotNull(ks);
    //    }

    @Test
    public void securityHelperTest() {
        final String caCertificateFileName = CERTFILE;
        final String clientCertificateFileName = CERTFILE;
        final String clientKeyFileName = KEYFILE;

        SSLSocketFactory sslSocketFactory = null;
        try {
            sslSocketFactory = SecurityHelper.createSocketFactory(caCertificateFileName, clientCertificateFileName, clientKeyFileName);
            Assert.assertNotNull(sslSocketFactory);
            System.out.println("createSocketFactory complete");

            SSLSocket sslSocket = (SSLSocket)sslSocketFactory.createSocket("localhost", 32000);
            Assert.assertNotNull(sslSocket);
            // sslSocket.setUseClientMode(false);
            sslSocket.startHandshake();
            InputStream sslIS = sslSocket.getInputStream();
            OutputStream sslOS = sslSocket.getOutputStream();

            sslOS.write("GET /README.md HTTP/1.1\r\n\r\n".getBytes());
            sslOS.flush();
            // sslIS.read();

            // use input stream to read server's response
            BufferedReader in = new BufferedReader(new InputStreamReader(sslIS));
            StringBuilder sb = new StringBuilder();

            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
                System.out.println(str);

            }

            sslSocket.close();
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in test - only designed to run if server listening and pems are in place. so...leave it");
        }
    }

}
