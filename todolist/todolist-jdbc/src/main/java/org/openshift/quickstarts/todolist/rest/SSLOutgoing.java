package org.openshift.quickstarts.todolist.rest;

import org.junit.Assert;
import org.openshift.quickstarts.todolist.security.SecurityHelper;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.*;


@Path("ssl")
@Produces(MediaType.APPLICATION_JSON)
public class SSLOutgoing {

    @Path("/call")
    @GET
    public JsonObject makeOutgoingSSLCall(
            @QueryParam("host") String host,
            @QueryParam("port") String port,
            @QueryParam("file") String file){
        String CACERTFILENAME = System.getenv("CACERTFILENAME");
        String CLIENTCERTFILENAME = System.getenv("CLIENTCERTFILENAME");
        String CLIENTKEYFILENAME = System.getenv("CLIENTKEYFILENAME");
        StringBuilder sb = new StringBuilder();
        boolean error = false;
        int portNumeric = 32000;
        if (port != null && !port.isEmpty()){
            portNumeric = Integer.getInteger(port);
        }
        if (file == null || file.isEmpty()){
            file = "README.md";
        }
        SSLSocket sslSocket = null;
        try {
            if (CACERTFILENAME == null || CACERTFILENAME.isEmpty()){
                sb.append("Error CACERTFILENAME empty");
                error = true;
            }
            if (CLIENTCERTFILENAME == null || CLIENTCERTFILENAME.isEmpty()){
                sb.append("Error CLIENTCERTFILENAME empty");
                error = true;
            }
            if (CLIENTKEYFILENAME == null || CLIENTKEYFILENAME.isEmpty()){
                sb.append("Error CLIENTKEYFILENAME empty");
                error = true;
            }
            if (portNumeric < 2000 || portNumeric > 65000){
                sb.append("Error port out of range "+port);
                error = true;
            }
            if (host == null || host.isEmpty()){
                sb.append("Error host empty");
                error = true;
            }
            if (!error){
                SSLSocketFactory sslSocketFactory = SecurityHelper.getCreateSocketFactory(
                        CACERTFILENAME,CLIENTCERTFILENAME, CLIENTKEYFILENAME);
                sslSocket = (SSLSocket)sslSocketFactory.createSocket(host, portNumeric);

                Assert.assertNotNull(sslSocket);
                // sslSocket.setUseClientMode(false);
                sslSocket.startHandshake();
                InputStream sslIS = sslSocket.getInputStream();
                OutputStream sslOS = sslSocket.getOutputStream();

                sslOS.write(("GET /" + file + " HTTP/1.1\r\n\r\n").getBytes());
                sslOS.flush();
                // sslIS.read();

                // use input stream to read server's response
                BufferedReader in = new BufferedReader(new InputStreamReader(sslIS));

                String str;
                while ((str = in.readLine()) != null) {
                    sb.append(str);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception in test - only designed to run if server listening and pems are in place. so...leave it");
        } finally {
            if (sslSocket != null){
                try {
                    sslSocket.close();
                } catch (IOException e) {
                }
            }
        }
        String var = "result";
        if (error){
            var = "error";
        }
        JsonObject object = Json.createObjectBuilder()
                .add(var,sb.toString())
                .build();
        return object;
    }
}