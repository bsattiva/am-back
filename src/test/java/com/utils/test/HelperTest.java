package com.utils.test;

import com.utils.data.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class HelperTest {


    public static String getTokenTest(final String email, final String pass) throws JSONException {
        final String url = com.utils.Helper
                .getStringFromProperties(com.utils.Helper.getHomeDir(new String[]{"config.properties"}),
                        "this.url") + "/signin";
        var body = new JSONObject() {{
            put("email", email);
            put("password", pass);
        }};
        var resp = HttpClient.sendHttpsPost(body, url);
        System.out.println(resp);
        return resp.toString();
    }
}
