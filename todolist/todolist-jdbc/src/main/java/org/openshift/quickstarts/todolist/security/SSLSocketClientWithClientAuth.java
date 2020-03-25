//package org.openshift.quickstarts.todolist.security;
//
//import java.net.*;
//import java.io.*;
//import javax.net.ssl.*;
//import javax.security.cert.X509Certificate;
//import java.security.KeyStore;
//import java.util.Optional;
//
///*
// * This example shows how to set up a key manager to do client
// * authentication if required by server.
// *
// * This program assumes that the client is not inside a firewall.
// * The application can be modified to connect to a server outside
// * the firewall by following SSLSocketClientWithTunneling.java.
// */
//public class SSLSocketClientWithClientAuth {
//
//    public static void send(String host, String path, int port) throws Exception {
//
//        try {
//
//            /*
//             * Set up a key manager for client authentication
//             * if asked by the server.  Use the implementation's
//             * default TrustStore and secureRandom routines.
//             */
//            SSLSocketFactory factory = null;
//            try {
//                SSLContext ctx;
//                KeyManagerFactory kmf;
//                KeyStore ks;
//                char[] passphrase = "passphrase".toCharArray();
//
//                ctx = SSLContext.getInstance("TLS");
//                kmf = KeyManagerFactory.getInstance("SunX509");
//                ks = KeyStore.getInstance("JKS");
//
//                ks.load(new FileInputStream("testkeys"), passphrase);
//
//                kmf.init(ks, passphrase);
//                ctx.init(kmf.getKeyManagers(), null, null);
//
//                factory = ctx.getSocketFactory();
//            } catch (Exception e) {
//                throw new IOException(e.getMessage());
//            }
//
//            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
//
//            /*
//             * send http request
//             *
//             * See SSLSocketClient.java for more information about why
//             * there is a forced handshake here when using PrintWriters.
//             */
//            sendMessage(path, socket);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public static void send(String host, String path, int port, KeyStore keyStore, Optional<String> keyPassword) throws Exception {
//
//        try {
//
//            /*
//             * Set up a key manager for client authentication
//             * if asked by the server.  Use the implementation's
//             * default TrustStore and secureRandom routines.
//             */
//            SSLSocketFactory factory = null;
//            try {
//                SSLContext ctx;
//                KeyManagerFactory kmf;
//                //KeyStore ks;
//                //char[] passphrase = "passphrase".toCharArray();
//
//                ctx = SSLContext.getInstance("TLS");
//                kmf = KeyManagerFactory.getInstance("SunX509");
//                //ks = KeyStore.getInstance("JKS");
//                //ks.load(new FileInputStream("testkeys"), passphrase);
//
//                kmf.init(keyStore, keyPassword.orElse("").toCharArray());
//                ctx.init(kmf.getKeyManagers(), null, null);
//
//                factory = ctx.getSocketFactory();
//            } catch (Exception e) {
//                throw new IOException(e.getMessage());
//            }
//
//            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
//
//            /*
//             * send http request
//             *
//             * See SSLSocketClient.java for more information about why
//             * there is a forced handshake here when using PrintWriters.
//             */
//            sendMessage(path, socket);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void sendMessage(String path, SSLSocket socket) throws IOException {
//        socket.startHandshake();
//
//        PrintWriter out = new PrintWriter(
//                new BufferedWriter(
//                        new OutputStreamWriter(
//                                socket.getOutputStream())));
//        out.println("GET " + path + " HTTP/1.0");
//        out.println();
//        out.flush();
//
//        /*
//         * Make sure there were no surprises
//         */
//        if (out.checkError())
//            System.out.println(
//                    "SSLSocketClient: java.io.PrintWriter error");
//
//        /* read response */
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(
//                        socket.getInputStream()));
//
//        String inputLine;
//
//        while ((inputLine = in.readLine()) != null)
//            System.out.println(inputLine);
//
//        in.close();
//        out.close();
//        socket.close();
//    }
//}
