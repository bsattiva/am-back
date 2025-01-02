package com.utils.data;


import com.enums.Area;
import com.utils.*;

import com.utils.command.JsonHelper;
import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class QueryHelper {
    private static final String SECRET = "82hj 36j !!!4 %$ suyseuweyb ^^8hscvb";
    private static final String MAIL_SECRET = "qdb82qjd!^&shaagsa ashjjsag &^(";
    private static final String MESSAGE = "message";
    private static final String STATUS = "status";
    private static final String PULL_STRING = "pull-string";
    public static final String PULL_TABLE = "pull-table";
    public static final String PULL_LIST = "pull-list";
    private static final String EXECUTE = "execute";
    private static final String SUCCESS = "success";
    public static final String SEED = "sdk^sirhf87w8e6! djdsdghxz78 hdhdhd&&&&&&&& shd";
    private final static Logger LOGGER = Logger.getLogger(QueryHelper.class);
    private final static String QUESTION_MASK = "\\?";
    private static final String PROJECT_ID = "projectId";
    private static final String SCENARIO_ID = "scenarioId";
    private static final String RUN_ID = "runId";
    private static final String STEP_ID = "stepId";
    private static final int MAX_RECORDS = 20;
    private static final String MASK = "?";

    public static JSONObject getData(final String query, final String type){
        JSONObject object = new JSONObject("{}");
        object.put("query", query);
        object.put("type", type);
        object.put("secret", SECRET);
        String url = Helper.getStringFromProperties(Helper.getHomeDir(new String[]{"config.properties"}),"data.url");
        return HttpClient.sendHttpsPost(object, url);
    }

    public static JSONObject postData(final String url, final JSONObject object){

        return HttpClient.sendHttpsPost(object,url);
    }

    public static JSONObject postData(final String url, final JSONObject object, final Map<String, String> headers){

        return HttpClient.sendHttpsPost(object,url, headers);
    }

    public static JSONObject getLogs(final int records) {
        var query = "select time,message,area,id from shmest.log order by id desc limit " + records;
        return getData(query, PULL_TABLE);
    }

    public static JSONObject getLogs(final int records, Area area) {
        var query = "select time,message,area,id from shmest.log where area='"
                + area.label + "' order by id desc limit " + records;

    return getData(query, PULL_TABLE);
    }

    public static void selfLogEntry(final String message, final String project) {
        var query = "insert into shmest.log (time, project, area, message) values(now(),'?','?','?')";
        getData(Helper.completeString(QUESTION_MASK,
                query,new String[]{project, Area.LOGGING.label, message}), EXECUTE);

    }

    private static String getNonEmptyQueryBit(final String columns) {
        var cols = columns.split(",");
        var builder = new StringBuilder();
        var temp = "a.? is not null and a.?!=''";
        var i = 0;
        for (var col : cols) {
            builder.append(temp.replace(MASK, col));
            if (i < cols.length - 1) {
                builder.append(" or ");
            }
            i++;
        }
         return builder.toString();
    }

    public static List<String> getApplicableUsers(final String sheetId, final String sheetName) {
        var columns = AmdsHelper.getColumns(sheetId);

        var query = "select b.section from amds.? a join user.profile b on a.user_id=b.section where "
                .replace(MASK, sheetName)
                + getNonEmptyQueryBit(columns);

        var users = getData(query, PULL_LIST).getJSONArray(MESSAGE);
        List<String> userList = new ArrayList<>();
        for (var i = 0; i < users.length(); i++) {
            if (!userList.contains(users.getString(i))) {
                userList.add(users.getString(i));
            }
        }
        return userList;
    }



    public static int getSheetIdByExcelName(final String name) {
        var fileName = name.split(" for user")[0];
        return getSheetIdByName(fileName);
    }

    public static int getSheetIdByName(final String name) {
        final var query = "select id from amds.sheets where mask_name='" + name + "'";
        var idString = getData(query, PULL_STRING).getString(MESSAGE);
        return Integer.parseInt(idString);
    }


    public static JSONObject getRowsModel(final int sheetId) {
        var query = "select row_name,seq,info_row,sheet_id from amds.sheet_model where sheet_id=" + sheetId;
        return getData(query, PULL_TABLE);
    }

    public static JSONObject getFieldsModel(final int sheetId) {
        var query = "select row_fields,user,ui_fields,user_ui_fields,head,mask_name from amds.sheets where id=" + sheetId;
        return getData(query, PULL_TABLE);
    }




    public static List<String> getAmdsDisabledSheets(final String userId) {

        var query = "select sheet_id from amds.disabled_sheets where user_id=" + userId;
        var object = QueryHelper.getData(query, PULL_LIST);
        var idArray = (object.has(MESSAGE)) ? object.getJSONArray(MESSAGE) : new JSONArray();
        List<String> ids = new ArrayList<>();
        idArray.forEach(str -> ids.add((String) str));
        return ids;
    }

    public static JSONArray deleteUser(final int userId) {
        var statuses = new JSONArray();
        var userQuery = String.format("delete from user.user where name=%d", userId);
        var profileQuery = String.format("delete from user.profile where section=%d", userId);
        statuses.put(getData(userQuery, EXECUTE));
        statuses.put(getData(profileQuery, EXECUTE));
        return statuses;
    }


    public static List<String> getAllAmdsSheets() {
        var query = "select id from amds.sheets";
        return JsonHelper.getListFromJsonArray(getData(query, PULL_LIST).getJSONArray(MESSAGE));
    }



    public static List<String> getEnabledAmdsSheets(final String userId) {
        List<String> ids = new ArrayList<>();
        var disabledIds = getAmdsDisabledSheets(userId);
        var allIds = getAllAmdsSheets();
        return allIds.stream().filter(id -> !disabledIds.contains(id)).collect(Collectors.toList());
    }
    public static void logEntry(final String message, final String project, final String area) {
        var query = "insert into shmest.log (time, project, area, message) values(now(),'?','?','?')";
        System.out.println("TRYING TO SAVE WITH QUERY: " + Helper.completeString(QUESTION_MASK, query,new String[]{project, area, message}));
        var data = getData(Helper.completeString(QUESTION_MASK, query,new String[]{project, area, message}), EXECUTE);
        selfLogEntry(data.toString(5), project);
    }

    public static void logEntry(final String message, final String project, final String area, final StackTraceElement[] trace) {
        var query = "insert into shmest.log (time, project, area, message) values(now(),'?','?','?')";
        var error = message + " " + Stream.of(trace).map(StackTraceElement::toString).collect(Collectors.joining());
        getData(Helper.completeString(QUESTION_MASK, query,new String[]{project, area, error}), EXECUTE);
    }

    public static JSONArray getProfile(final String token){
        var id = getIdByToken(token);
        JSONObject result = Helper.getFailedObject();
        var query = "select content,element from user.profile where section=" + id;
        var profile = getData(query, PULL_TABLE);
        return profile.getJSONArray(MESSAGE);
    }



    public static String getEmailById(final String id) {
       final var query = "select email from user.user where name=" + id;
       return Helper.decrypt(getData(query, PULL_STRING).getString(MESSAGE), SEED);
    }



    public static String getPasswordlById(final String id) {
        final var query = "select pass from user.user where name=" + id;
        return Helper.decrypt(getData(query, PULL_STRING).getString(MESSAGE), SEED);
    }

//    public static void main(String[] args) {
//        System.out.print(loginAsUser("1341").toString(4));
//        System.out.println(testLocked("1341"));
//    }

    private static void lock(final String id, final String locked) {
        var query = "update user.user set locked='" + locked + "' where name=" + id;
        getData(query, EXECUTE);
    }


    public static JSONObject loginAsUser(final String id) {
        final var url = Helper.getUrl("auth.url") + "/login";
        var body = new JSONObject();
        var result = new JSONObject();
        body.put("email", getEmailById(id));
        body.put("password", getPasswordlById(id));
        try {
            var locked = unlock(id).getString("locked");
            result = HttpClient.sendHttpsPost(body, url);
            lock(id, locked);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }

    public static String getStepStatus(final String runId, final String stepId, final String projectId) {
        final String query = "select r.status from shmest.run r join shmest.dashboard d on r.runid=d.runid where r.runid='?' and r.stepid='?' and d.projectid='?'";
        var arguments = new String[] {runId, stepId, projectId};
//        System.out.println("CHECKING STATUS WITH QUERY");
//        System.out.println(Helper.completeString(QUESTION_MASK, query, arguments));
        var result = getData(Helper.completeString(QUESTION_MASK, query, arguments), PULL_STRING);
        return (result.has(MESSAGE))
                ? result.getString(MESSAGE) : "-1";

    }
    public static int getDashCount(final String projectId, final String scenarioId) {

        var query = "select count(*) from shmest.dashboard where projectid='?' and scenarioid='?'";
        var countString = getData(Helper.completeString(QUESTION_MASK,
                query,
                new String[] {projectId, scenarioId}), PULL_STRING).getString(MESSAGE);
        return (Helper.isInteger(countString)) ? Integer.parseInt(countString) : 0;
    }

    private static int getDashboardSec(final String projectId, final String scenarioId) {
        var seqQuery = "select max(seq) from shmest.dashboard where projectid='?' and scenarioid='?'";
        var sec = getData(Helper.completeString(QUESTION_MASK, seqQuery, new String[]{projectId, scenarioId}),
                PULL_STRING);
        var seqString = (sec.has(MESSAGE)) ? sec.getString(MESSAGE) : "";
        return (Helper.isInteger(seqString)) ? Integer.parseInt(seqString) : -1;
    }

    private static boolean cleanDashboard(final String projectId, final String scenarioId, final int sec) {
        var count = getDashCount(projectId, scenarioId);
        var status = true;
        if (count > MAX_RECORDS - 1) {
            var delSec= sec - MAX_RECORDS;
            var query = "delete from shmest.dashboard where projectid='?' and scenarioid='?' and sec=?";
            var arguments = new String[] {projectId, scenarioId, Integer.toString(delSec)};
            var message = getData(Helper.completeString(QUESTION_MASK, query, arguments), EXECUTE)
                    .getString(MESSAGE);
            if (!Helper.isThing(message) || !message.equals(SUCCESS)) {
                status = false;
            }
        }
        return status;
    }

    public static boolean saveDashboard(final JSONObject object) {
        var query = "insert into shmest.dashboard(projectid,scenarioid,runid,seq) values('?','?','?',?)";
        var projectId = object.getString(PROJECT_ID);
        var scenarioId = object.getString(SCENARIO_ID);
        var runId = object.getString(RUN_ID);
        var sec = getDashboardSec(projectId, scenarioId);
        var arguments = new String[] {projectId, scenarioId, runId, Integer.toString(sec + 1)};
        var saved = getData(Helper.completeString(QUESTION_MASK, query, arguments), EXECUTE);
        return saved.has(MESSAGE) && saved.getString(MESSAGE).equalsIgnoreCase("success")
                && cleanDashboard(projectId, scenarioId, sec);

    }

    private static int getRunStepSequence(final String runid) {
        var seqQuery = "select max(seq) from shmest.run where runid='" + runid + "'";
        var data = getData(seqQuery, PULL_STRING);
        return (data.length() > 0 && data.has(MESSAGE) && Helper.isInteger(data.getString(MESSAGE)))
                ? (Integer.parseInt(data.getString(MESSAGE)) + 1) : 0;
    }

    public static boolean saveRunStep(final JSONObject object) {
        var status = (object.getString(STATUS).equalsIgnoreCase("passed")) ? 1 : 0;
        var scenarioId = object.getString(SCENARIO_ID);
        var runid = object.getString(RUN_ID);
        var stepId = object.getString(STEP_ID);

            var query = "insert into shmest.run(runid,scenarioid,stepid,seq,status) values('?','?','?',?,?)";
            var saveQuery = Helper
                    .completeString("(\\?)",
                            query,
                            new String[] {runid,
                                    scenarioId,
                                    stepId,
                                    Integer.toString(getRunStepSequence(runid)),
                                    Integer.toString(status)});
            System.out.println(saveQuery);
            var saved = QueryHelper
                    .getData(saveQuery, EXECUTE);



            return saved.has(MESSAGE) && saved.getString(MESSAGE).equalsIgnoreCase("success");
    }

    public static JSONObject setIgnored(final int userId) {
        final var query = String.format("insert into amds.ignored(id) values(%d)", userId);
        return getData(query, EXECUTE);
    }

    public static boolean setState(final String item, final boolean state) {
        var status = state ? 1 : 0;
        final var delQuery = String.format("delete from amds.state where stat='%s'", item);
        final var query = String.format("insert into amds.state values('%s', %d)", item, status);
        getData(delQuery, EXECUTE);
        var resp = getData(query, EXECUTE);

        return resp.has(MESSAGE) && resp.getString(MESSAGE).equals(SUCCESS);
    }

    public static boolean getState(final String item) {
        var query = String.format("select is_set from amds.state where stat='%s'", item);
        var resp = getData(query, PULL_STRING);
        return resp.has(MESSAGE) && resp.getString(MESSAGE).equals("1");
    }

    public static List<String> getIgnored() {
        List<String> ignored = new ArrayList<>();
        if (getState("ignore")) {
            var query = "select id from amds.ignored";
            var list = getData(query, PULL_LIST).getJSONArray(MESSAGE);
            ignored = JsonHelper.getListFromJsonArray(list);
        }
        return ignored;
    }

    public static JSONObject persistSavedPage(final JSONObject page) {
        final String pageName = page.getString("pagename");
        final JSONArray pageObject = page.getJSONArray("page");
        var pageString = pageObject.toString()
//                .trim()
//                .replaceFirst("^\\[", "{")
//                .replaceFirst("\\]$","}").replace("'", "\\\\'")
                ;

        final String project = page.getString("project");
        final String url = page.getString("url");
        var status = new JSONObject();
        final String checkQuery = "select pagename from shmest.pages where pagename='"
                + pageName
                + "' and projectid='" + project + "'";
        if (getData(checkQuery, PULL_STRING).length() == 0) {
            final String query = "insert into shmest.pages(pagename,projectid,page,url) values('?','?','?','?')"
                    .replaceFirst(QUESTION_MASK, pageName)
                    .replaceFirst(QUESTION_MASK, project)
                    .replaceFirst(QUESTION_MASK, pageString.replace("'", "\\'"))
                    .replaceFirst(QUESTION_MASK, url);

            JSONObject object = new JSONObject("{}");
            object.put("query", query);
            object.put("type", EXECUTE);
            object.put("secret",SECRET);
            status = getData(query, EXECUTE);
        } else {
            var pageStringEscaped = pageString.replace("'", "\0027");
            final String updateQuery = "update shmest.pages set page='?',url='?' where projectid='?' and pagename='?'"
                    .replaceFirst(QUESTION_MASK, pageStringEscaped)
                    .replaceFirst(QUESTION_MASK, url)
                    .replaceFirst(QUESTION_MASK, project)
                    .replaceFirst(QUESTION_MASK, pageName);
            status = getData(updateQuery, EXECUTE);

        }
        return status;
    }

    public static JSONObject getSinglePage(final String project, final String name) {
        var query = "select page,url from shmest.pages where projectid='?' and pagename='?'"
                .replaceFirst(QUESTION_MASK, project)
                .replaceFirst(QUESTION_MASK, name);
        var data = QueryHelper.getData(query, "pull-table");

        if (data.length() > 0 && data.has("message") && data.getJSONArray("message").length() > 0) {
            var string = data.getJSONArray("message").getJSONObject(0).toString()
                    .replace(":\"{", ":{")
                    .replace("}\",", "},")
                    .replace("[\"{","[{")
                    .replace("}\"]", "}]")
                    .replace("\\", "")
                    .replace("\"[{", "[{")
                    .replace("}]\"", "}]")
                    .replace("u00027", "'")
                    .replaceAll("\"\"([a-zA-Z0-9]*)\"\"", "\"\"$1\"\"");

            var object = (data.has("message") && data.getJSONArray("message").length() > 0)
                    ? new JSONObject(string) : new JSONObject();
            if (object.has("page") && object.get("page") instanceof JSONObject) {
                var page = object.getJSONObject("page");
                var keys = page.keys();
                var arr = new JSONArray();
                while (keys.hasNext()) {
                    var key = keys.next();
                    var obj = page.getJSONObject(key);
                    arr.put(obj);
                }
                object.put("page", arr);
            }
            return object;

        } else {
            return new JSONObject();

        }


    }


    public static JSONObject saveUser(final String firstName,
                                      final String lastName,
                                      final String email,
                                      final String password) {
        var numQuery = "select max(name) from user.user";
        var preId = getData(numQuery, "pull-string").getString("message");
        final String url = "http://162.240.103.238:8088/register";
        //final String url = "http://localhost:8088/register";
        final var body = new JSONObject();
        body.put("email", email.trim());
        body.put("password", password.trim());
        var obj = HttpClient.sendHttpsPost(body, url);
        if (obj.has("message") && obj.getString("message").equals("success")) {
            TestHelper.sleep(1000);
            var id = getData(numQuery, "pull-string").getString("message");
            if (Integer.parseInt(id) > Integer.parseInt(preId)) {
                var query = "update user.profile set content='"
                        + firstName.trim() + " " + lastName.trim() + "' where section = '" + id + "'" ;
                var stat = getData(query, "execute");
                obj.put("profile", stat);

            } else {



                System.out.println("failed to save: " + email.trim());
            }


        } else {
            System.out.println("failue");
            System.out.println();
            System.out.print(obj.toString());

        }

        return obj;
    }



    public static JSONObject persistPage(final JSONObject page) {
        final String pageName = page.getString("pageName");
        final String pageObject = page.getJSONObject("elements").toString().replace("'", "\\\\'");
        final String project = page.getString("project");
        final String url = page.getString("url");
        var status = new JSONObject();
        final String checkQuery = "select pagename from shmest.pages where pagename='"
                + pageName
                + "' and projectid='" + project + "'";
        if (getData(checkQuery, PULL_STRING).length() == 0) {
            final String query = "insert into shmest.pages(pagename,projectid,page,url) values('?','?','?','?')"
                    .replaceFirst(QUESTION_MASK, pageName)
                    .replaceFirst(QUESTION_MASK, project)
                    .replaceFirst(QUESTION_MASK, pageObject)
                    .replaceFirst(QUESTION_MASK, url);

            JSONObject object = new JSONObject("{}");
            object.put("query", query);
            object.put("type", EXECUTE);
            object.put("secret",SECRET);
            status = getData(query, EXECUTE);
        } else {
            final String updateQuery = "update shmest.pages set page='?',url='?' where projectid='?' and pagename='?'"
                    .replaceFirst(QUESTION_MASK, pageObject)
                    .replaceFirst(QUESTION_MASK, url)
                    .replaceFirst(QUESTION_MASK, project)
                    .replaceFirst(QUESTION_MASK, pageName);
            status = getData(updateQuery, EXECUTE);

        }
        return status;
    }



    public static JSONObject saveAmdsSheetRow(final String sheetId, final String userId, final JSONObject row) {
        final var query = AmdsHelper.getCreateSheetQuery(sheetId, userId, row);
        return getData(query, EXECUTE);
    }

    public static String getLastUser() {
        var query = "select max(name) from user.user";
        return getData(query, "pull-string").getString(MESSAGE);
    }

    public static boolean testLocked(final String id) {
        final var query = "select locked from user.user where name=" + id;
        final var result = getData(query, PULL_STRING);
        return result.has(MESSAGE) && Helper.isThing(result.getString(MESSAGE));
    }

    public static String reset(final String id, final String password) {

        var pass = Helper.encrypt(password, SEED);
        var query = "update user.user set pass='" + pass + "' where name=" + id;
        return getData(query, EXECUTE).toString();
    }

    public static JSONObject updateName(final String name, final String id) {
        var query
                = "update user.profile set content='" + name + "' where element='name' and section='" + id + "'";
        return getData(query, EXECUTE);
    }

    public static JSONObject unlock(final String id) {
        var queryLoc = "select locked from user.user where name=" + id;
        var locked = getData(queryLoc, PULL_STRING).getString(MESSAGE);
        var query = "update user.user set locked='' where name=" + Integer.parseInt(id);
        var result = getData(query, EXECUTE);
        result.put("locked", locked);
        return result;
    }

    public static String getProject(final String token){
        String result = "";
    //    String url = Helper.getUrl("backend.url") + "middle/auth?token=" + token;
        String url = Helper.getUrl("auth.url") + "/auth?token=" + token;
        try {
            var res = HttpClient.sendHttpsPost(new JSONObject(), url).getJSONObject("profile");
            result = res.getJSONObject("profile").getJSONObject(MESSAGE).getString("name");
            System.out.print(res.getJSONObject("profile").toString(5));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }

    public static String getIdByToken(final String token) {
        return getIdByName(getProject(token));
    }

    public static boolean isAdmin(final String id) {
        var query = "select admin from amds.admintable where id='" + id + "'";
        var result = getData(query, "pull-string");
        return result.has("message") && result.getString("message").equals("1");
    }
    private static String vetName(final String name) {
        if (name.contains(" ")) {
            return "\"" + name + "\"";
        } else return name;
    }

    public static JSONArray getUsers() {
        var query = "select section,content from user.profile where element='name'";
        return getData(query, PULL_TABLE).getJSONArray(MESSAGE);
    }

    public static JSONArray getUsersByName(final String name) {
        var query
                = "select section,content from user.profile where element='name' and content like '%" + name + "%'";
        var arr = getData(query, PULL_TABLE).getJSONArray(MESSAGE);
        var ignored = QueryHelper.getIgnored();
        var list = JsonHelper
                .getListJsonObjects(arr)
                .stream().filter(obj -> !ignored.contains(obj.getString("section"))).collect(Collectors.toList());
        return JsonHelper.getArrayFromJsonObjectList(list);
        //return getData(query, PULL_TABLE).getJSONArray(MESSAGE);
    }

    public static JSONArray getSheetUsers(final String sheet_name) {
        var query = "select section,content from user.profile u where exists (select 1 from amds." +
                AmdsHelper.getTableName(sheet_name)
                + " where user_id = u.section)";
        return getData(query, PULL_TABLE).getJSONArray(MESSAGE);
    }

    public static JSONArray getSheetNotUsers(final String sheet_name) {
        var query = "select section,content from user.profile u where not exists (select 1 from amds." +
                AmdsHelper.getTableName(sheet_name)
                + " where user_id = u.section)";
        return getData(query, PULL_TABLE).getJSONArray(MESSAGE);
    }


    public static JSONArray getUserSheets(final String user_id, final boolean reverse) {
        var tables = AmdsHelper.getAllTables();
        var result = new JSONArray();
        for(var i = 0; i < tables.length(); i++) {
            var obj = tables.getJSONObject(i);
            var name = obj.getString("name");
            if (!reverse) {
                if (isTableCompleted(name, user_id)) {
                    result.put(obj);
                }
            } else {
                if (!isTableCompleted(name, user_id)) {
                    result.put(obj);
                }
            }
        }

        return result;
    }


    public static boolean isTableCompleted(final String table, final String user_id) {
        var query = "select row_name from amds." + table + " where user_id=" + user_id;
        return QueryHelper.getData(query, "pull-list").has("message");
    }

    public static String getIdByName(final String name){
        String result = "";

        String url = Helper.getUrl("user.url") + "/user-id?name=" + vetName(name);
        try {
            result = HttpClient.sendGet(url,new HashMap<>()).getString(MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    private static Map<String, String> names = new HashMap<>();

    public static String getNameById(final String id){
        String result = "";
        if (!names.containsKey(id)) {
             String url = Helper.getUrl("user.url") + "/username?id=" + id;
            try {
                result = HttpClient.sendGet(url,new HashMap<>()).getString(MESSAGE);
                names.put(id, result);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            result = names.get(id);
        }

        return result;
    }

    public static JSONObject sendMail(String template,String email, String subject,String replace){
        JSONObject result = Helper.getFailedObject();
        JSONObject data = new JSONObject();
        data.put("template",template);
        data.put("email",email);
        data.put("subject",subject);
        data.put("subject",replace);
        String url = Helper.getUrl("mail.url");
        result = HttpClient.sendHttpsPost(data,url);
        return result;
    }

}
