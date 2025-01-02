package com.utils.data;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class HttpClient {


    private final static CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final Logger LOGGER = Logger.getLogger(HttpClient.class);


    private void close() throws IOException {
        httpClient.close();
    }

    public static JSONObject sendRemoteGet(String url, Map<String,String> head) throws Exception {
        String result = "";

        JSONObject obj = new JSONObject();
        try {
            URL r = new URL(url);
            InputStream trustStream = new FileInputStream("./ssl-server.jks");
            char[] trustPassword = "Ks327!dfj0".toCharArray();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(trustStream, trustPassword);

            TrustManagerFactory trustFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(trustStore);
            TrustManager[] trustManagers = trustFactory.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, null);
            SSLContext.setDefault(sslContext);

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8098));

            Authenticator.setDefault(new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("data-server1", "Ks327!dfj0".toCharArray());
                }
            });



            HttpsURLConnection conn = (HttpsURLConnection)r.openConnection();
            conn.setAllowUserInteraction(true);
            //  conn.setDoOutput(true);
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestMethod("GET");
            head.forEach((key,value) -> conn.setRequestProperty(key,value));
//            OutputStream os = conn.getOutputStream();
//            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
//            osw.write(body.toString());
//
//
//            osw.flush();
//            osw.close();
//            os.close();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder response = new StringBuilder();
            String line;
            while((line = br.readLine())!=null){
                response.append(line);
            }
            obj = new JSONObject(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }



    public static JSONObject sendGet(String url, Map<String,String> head) throws Exception {
        String result = "";

        JSONObject obj = new JSONObject();
        try {
            URL r = new URL(url);


            HttpURLConnection conn = (!url.contains("https"))
                    ? (HttpURLConnection)r.openConnection() : (HttpsURLConnection)r.openConnection();


            conn.setAllowUserInteraction(true);
            //  conn.setDoOutput(true);
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestMethod("GET");
            head.forEach((key,value) -> conn.setRequestProperty(key,value));
//            OutputStream os = conn.getOutputStream();
//            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
//            osw.write(body.toString());
//
//
//            osw.flush();
//            osw.close();
//            os.close();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder response = new StringBuilder();
            String line;
            while((line = br.readLine())!=null){
                response.append(line);
            }
            obj = new JSONObject(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static String getString(String url) throws Exception {

        HttpGet request = new HttpGet(url);
        String result = "";

        try (CloseableHttpResponse response = httpClient.execute(request)) {


            System.out.println(response.getStatusLine().toString());

            org.apache.http.HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {

                result = EntityUtils.toString(entity);

            }

        }

        return result;
    }


    public static JSONObject sendGet(String url) throws Exception {

        HttpGet request = new HttpGet(url);
        String result = "";

        try (CloseableHttpResponse response = httpClient.execute(request)) {


            System.out.println(response.getStatusLine().toString());

            org.apache.http.HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            if (entity != null) {

                result = EntityUtils.toString(entity);

            }

        }

        return new JSONObject(result.replaceAll("\n","").replaceAll("\r",""));
    }

    public static JSONObject sendRemotePost(JSONObject body, String url){
        JSONObject obj = new JSONObject();
        try {
            URL r = new URL(url);
            InputStream trustStream = new FileInputStream("./ssl-server.jks");
            char[] trustPassword = "Ks327!dfj0".toCharArray();
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(trustStream, trustPassword);

            TrustManagerFactory trustFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(trustStore);
            TrustManager[] trustManagers = trustFactory.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, null);
            SSLContext.setDefault(sslContext);

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8098));

            Authenticator.setDefault(new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("data-server1", "Ks327!dfj0".toCharArray());
                }
            });



            HttpsURLConnection conn = (HttpsURLConnection)r.openConnection();
            conn.setAllowUserInteraction(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(body.toString());


            osw.flush();
            osw.close();
            os.close();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder response = new StringBuilder();
            String line;
            while((line = br.readLine())!=null){
                response.append(line);
            }
            obj = new JSONObject(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static String sendHttpMultipartPost(final LinkedHashMap<String, String> body,
                                               final String url,
                                               final InputStream blob) {

        String text = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
//        headers.forEach(post :: addHeader);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
       // builder.setContentType(ContentType.MULTIPART_FORM_DATA);
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//        builder.setBoundary("------WebKitFormBoundary91QUBfu8SgCXCGL6");
        for (String key : body.keySet()) {
            if (!key.equals("image")) {

                if (body.get(key) != null) {
                    String value = body.get(key);
                    builder.addTextBody(key, value, ContentType.TEXT_PLAIN);

                }

            } else {

                try {

                    builder.addBinaryBody(
                            "productTileImages.image",
                            blob,
                            ContentType.IMAGE_PNG,
                            body.get("fileName")
                    );

                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }

            }

        }

        org.apache.http.HttpEntity multipart = builder.build();


        post.setEntity(multipart);
//        InputStream stream = null;
//        ByteArrayOutputStream ous = null;
//        var content = "";
//        try {
//           ous = new ByteArrayOutputStream();
//            post.getEntity().writeTo(ous);
//            content = ous.toString();
//            System.out.print(content);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        CloseableHttpResponse response = null;
        try {

            response = httpClient.execute(post);

            org.apache.http.HttpEntity responseEntity = response.getEntity();

            text = new BufferedReader(
                    new InputStreamReader(responseEntity.getContent(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }



        return text;
    }

    public static JSONObject sendHttpsPost(JSONObject body, String url, Map<String, String> headers){
        JSONObject obj = new JSONObject();
        try {
            URL r = new URL(url);

            HttpURLConnection conn = (HttpURLConnection)r.openConnection();
            conn.setAllowUserInteraction(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Content-Type","application/json");
            headers.forEach((key, value) -> {
                conn.setRequestProperty(key, headers.get(key));
            });
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(body.toString());


            osw.flush();
            osw.close();
            os.close();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder response = new StringBuilder();
            String line;
            while((line = br.readLine())!=null){
                response.append(line);
            }
            obj = new JSONObject(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject sendHttpsPost(JSONObject body, String url){
        JSONObject obj = new JSONObject();
        try {
            URL r = new URL(url);

            HttpURLConnection conn = (HttpURLConnection)r.openConnection();
            conn.setAllowUserInteraction(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept","application/json");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(body.toString());


            osw.flush();
            osw.close();
            os.close();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder response = new StringBuilder();
            String line;
            while((line = br.readLine())!=null){
                response.append(line);
            }
            obj = new JSONObject(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static JSONObject sendPost(JSONObject body ,String url) throws Exception {

        RestTemplate template = new RestTemplate();
        HttpEntity<JSONObject> entity = new HttpEntity(body);
        ResponseEntity<JSONObject> response = template.exchange(url, HttpMethod.POST,entity,JSONObject.class);
        JSONObject re = response.getBody();

        //      HttpPost post = new HttpPost(url);
        JSONObject result = new JSONObject("{}");
//        post.setEntity(new UrlEncodedFormEntity(urlParameters));
//
//        try (CloseableHttpClient httpClient = HttpClients.createDefault();
//             CloseableHttpResponse response = httpClient.execute(post)) {
//            result.put("message",response.getEntity());
//            System.out.println(EntityUtils.toString(response.getEntity()));
//        }
        return result;
    }




}