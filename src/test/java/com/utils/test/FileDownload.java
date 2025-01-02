package com.utils.test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDownload {
    public static final String TOKEN = "token";
    public static final String USER_TOKEN = "userToken";
    public static boolean downloadFile(final String endpoint,
                                       final String downloadPath,
                                       final String token,
                                       final String userToken) throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header(TOKEN, token)
                .header(USER_TOKEN, userToken)
                .build();

         HttpResponse<Path> response = httpClient
                 .send(request, HttpResponse.BodyHandlers.ofFile(Paths.get(downloadPath)));

         if (response.statusCode() == 200) {
            return true;
        } else {
            System.out.println("Failed to download the file. HTTP Status Code: " + response.statusCode());
        }
         return false;
    }
}