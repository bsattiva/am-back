package com.utils.test;

import org.apache.hc.client5.http.classic.methods.HttpPost;

import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

public class FileUpload {

    public static String sendFile(final String endpoint,
                                  final String filePath,
                                  final Map<String, String> headers) throws IOException, GeneralSecurityException {


        File file = new File(filePath);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
            HttpPost httpPost = new HttpPost(endpoint);
            headers.forEach(httpPost :: setHeader);
            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addPart("file", fileBody);
            HttpEntity requestEntity = multipartEntityBuilder.build();
            httpPost.setEntity(requestEntity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                return  EntityUtils.toString(response.getEntity());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}