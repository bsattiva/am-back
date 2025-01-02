package com.utils;

import com.utils.data.QueryHelper;
import org.json.JSONObject;

public class ProfileHelper {

    public static String getUserName(final int userId) {
        final var query = String.format("select content from user.profile where section=%d", userId);
        var nameObject = QueryHelper.getData(query, "pull-string");
        if (nameObject.has("message")) {
            return nameObject.getString("message");
        }
        return null;
    }

    public static JSONObject updateProfile(final int newUser, final int oldUser) {
        final var userName = getUserName(oldUser);
        var query = String
                .format("insert into user.profile(view,section,element,content) values ('profile',%d,'name','%s')",
                        newUser,
                        userName);
        return QueryHelper.getData(query, "execute");
    }

    public static int createNewUser(final int oldUser) {
        final var preUser = QueryHelper.getLastUser();
        var result = new JSONObject();
        final var email = Helper.getRandomString(5) + "_user";
        final var password = Helper.getRandomString(10);
        var body = new JSONObject();
        body.put("email", email);
        body.put("password", password);

        result.put("create", QueryHelper.postData(Helper.getUrl("auth.url") + "/register", body));
        TestHelper.sleep(2000);
        final var afterUser = Integer.parseInt(QueryHelper.getLastUser());
        if(afterUser > Integer.parseInt(preUser)) {
            updateProfile(afterUser, oldUser);
        } else {
            return -1;
        }
        return afterUser;
    }


    public static JSONObject handleUseDeletion(final int userName) {
        final var newUser = createNewUser(userName);
        if(newUser > 0) {
            var tableStatuses = AmdsHelper.reassignTablesToNewUser(newUser, userName);
            var deleteStatus = QueryHelper.deleteUser(userName);
            var result = new JSONObject();
            result.put("reassignStatus", tableStatuses);
            result.put("deleteStatus", deleteStatus);
            result.put("ignored", QueryHelper.setIgnored(newUser));
            return result;

        }
        return Helper.getFailedObject();
    }

    public static void main(String[] args) {

    }

}
