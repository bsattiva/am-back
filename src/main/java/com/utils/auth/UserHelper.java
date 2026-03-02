package com.utils.auth;

import com.utils.AmdsHelper;
import com.utils.FileReader;
import com.utils.Helper;
import com.utils.TestHelper;
import com.utils.command.JsonHelper;
import com.utils.data.QueryHelper;
import io.cucumber.java.sl.In;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.management.monitor.GaugeMonitor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.utils.AmdsHelper.MESSAGE;
import static com.utils.FileReader.getServiceColumns;
import static com.utils.FileReader.schemaOk;

public class UserHelper {
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String EMAIL = "email";
    public static final String[] FIELDS = {FIRST_NAME, LAST_NAME, EMAIL};

    public static JSONObject createUsers(final String fileLocation) throws IOException {
        var statuses = Helper.getFailedObject();
        var status = true;
        var usersArray = FileReader.getValuesFromExcel(fileLocation, FIELDS);
        var statArray = new JSONArray();
        var failed = new StringBuilder("errors: \n");
        for (var i = 0; i < usersArray.length(); i++) {
            var obj = usersArray.getJSONObject(i);
            var firstName = obj.getString(FIRST_NAME);
            var lastName = obj.getString(LAST_NAME);
            var email = obj.getString(EMAIL);
            var password = String.format("user_%s", Helper.getRandomString(7));
            var name = String.format("%s_%s", firstName, lastName);
            var userObject = createUser(email, password, name);
            if (userObject.has("error")) {
                status = false;
                failed.append(String.format("email: %s, error: %s\n", email, userObject));
            }
        }
        statuses.put("status", status);
        statuses.put("created", statArray);
        statuses.put("failed", failed);

        return statuses;
    }

    public static List<String> getValidUsers() {
        var state = QueryHelper.getState("ignore");
        List<String> ignored = new ArrayList<>();
        List<String> validUsers = new ArrayList<>();
        if (state) {
            ignored = QueryHelper.getIgnored();
        }
        var users = JsonHelper.getListFromJsonArray(QueryHelper.getData("select distinct section from user.profile",
                QueryHelper.PULL_LIST).getJSONArray(MESSAGE));

        for (var user : users) {
            if (!ignored.contains(user)) {
                validUsers.add(user);
            }
        }
        return validUsers;
    }

    public static JSONObject createUser(final String email, final String password, final String userName) {
        var result = new JSONObject();
        var body = new JSONObject();
        body.put("email", email);
        body.put("password", password);
        var preUser = QueryHelper.getLastUser();
        result.put("create", QueryHelper.postData(Helper.getUrl("auth.url") + "/register", body));
        TestHelper.sleep(50);
        var afterUser = QueryHelper.getLastUser();
        if (Integer.parseInt(afterUser) > Integer.parseInt(preUser)) {

            result.put("rename", QueryHelper.updateName(userName, afterUser));
            result.put("unlock", QueryHelper.unlock(afterUser));
            QueryHelper.sendRegisterMail(email, userName, password);
        } else {
            result.put("user", afterUser);
            result.put("error", "true");
        }
        return result;
    }
}
