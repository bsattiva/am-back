package com.testshmestservice.testshmestservice.controller;

import com.enums.Area;
import com.utils.*;
import com.utils.auth.Auth;
import com.utils.command.CommandRunner;
import com.utils.data.QueryHelper;
import com.utils.command.JsonHelper;
import com.utils.excel.ExcelHelper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.bouncycastle.cert.ocsp.Req;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ContentDisposition;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.report.Step;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.testng.IResultMap;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@RestController
@EnableAutoConfiguration
@SpringBootApplication
@CrossOrigin
public class TestStartController {

    @Getter
    private static final String TEST_PROJECT = "TestCube";
    @Getter
    private static final String PROJECT = "project";
    public static final String ID = "id";
    public static final String LAYOUT = "layout";

    public static final String NAME = "name";
    public static final String USER_ID = "userId";
    private static final String TOKEN = "token";
    private static final String TEMPLATE = "template";
    private static final String SECTION_DATA = "section_data";
    private static final String  SEQUENCE = "sequence";
    public static final String USER_TOKEN = "userToken";
    public static final String FILE_ID = "fileId";
    private static final String RUN_ID = "runId";
    private static final String STEP_ID = "stepId";
    public static final String PULL_TABLE = "pull-table";
    public static final String PULL_MAP = "pull-map";
    private static final String MASK = "?";
    public static final String MESSAGE = "message";
    private static final String QUESTION_MASK = "\\?";
    private static final String DEFAULT_FEATURE = "/src/test/features/defaultFeature.feature";
    private static final String PAGES = "/src/test/resources/pages.json";
    private static final String CONFIG = "/src/test/resources/config.properties";
    private static final String PAGE_NAME = "pageName";
    private static final String HTML = "html";
    private static final Logger LOGGER = Logger.getLogger(TestStartController.class);
    private static final String CUBE_SECRET = "cubeSecret";
    private static final String SECRET = (Helper.isThing(System.getProperty(CUBE_SECRET)))
            ? System.getProperty(CUBE_SECRET) : "92jehdjasdg823jewgahuawaw wsau7a";


    @PostMapping("/record-step")
    JSONObject recordTestStep(final HttpServletRequest request, final HttpServletResponse response) {
        var object = RequestHelper.getRequestBody(request);
        var step = new Step();
        step.setName(object.getString("name"));
        step.start();
        step.complete(true);
        step.getHtml();
        return new JSONObject();
    }


    @GetMapping("/single-page")
    String getPage(final HttpServletRequest request, final HttpServletResponse response) {

        var project = getProject(request);
        var name = request.getParameter("id");

        return QueryHelper.getSinglePage(project,name).toString();

    }


    @GetMapping("/pages")
    String getPages(final HttpServletRequest request, final HttpServletResponse response) {
        var token = request.getParameter(TOKEN).replace(" ", "+");
        var project = getProject(request);
        var query = "select pagename,page,url from shmest.pages where projectid='?'".replace(MASK, project);
        var data = QueryHelper.getData(query, "pull-table");
        var escapedData = data.getJSONArray("message").toString()
                .replace(":\"{", ":{")
                .replace("}\",", "},")
                .replace("[\"{","[{")
                .replace("}\"]", "}]")
                .replace("\\", "")
                .replaceAll("\"\"([a-zA-Z0-9]*)\"\"", "\"\"$1\"\"")
                .replace("u00027", "'")
                .replace("\"[{", "[{")
                .replace("}]\"", "}]")
;
        System.out.println(escapedData);
        var array =  new JSONArray(escapedData);
        for (var i = 0; i < array.length(); i++) {
            var object = array.getJSONObject(i);
            var arr = new JSONArray();
            if (object.get("page") instanceof JSONObject) {
                var keys = object.getJSONObject("page").keys();
                while (keys.hasNext()) {
                    var key = keys.next();
                    var obj = object.getJSONObject("page").getJSONObject(key);
                    arr.put(obj);
                }

                object.remove("page");
                System.out.print(object.toString(5));
                object.put("page", arr);

            }


        }

        return array.toString();
    }


    @GetMapping("/auth")
    String getProject(final HttpServletRequest request){
        var head = request.getParameter("head");
        var getId = request.getParameter("getId");
        String token = (!Helper.isThing(head))
                ? request.getParameter(TOKEN).replace(" ", "+")
                : request.getHeader(TOKEN).replace(" ", "+");

        return (!Helper.isThing(getId)) ? QueryHelper.getProject(token) : QueryHelper.getIdByToken(token);
    }

    @GetMapping("/amds_get_section_data")
    String getSectionData(final HttpServletRequest request, final HttpServletResponse response) {
        final var auth = new Auth(request);
        if (auth.getIsAdmin()) {
            return AmdsHelper.getSectionData(Integer.parseInt(request.getParameter(ID))).toString();
        } else {
            response.setStatus(403);
            return Helper.getFailedObject().toString();
        }
    }
    @GetMapping("/amds_publish_site")
    String publishSite(final HttpServletRequest request, final HttpServletResponse response) {
        var auth = new Auth(request);
        var result = Helper.getFailedObject();
        if(auth.getIsAdmin()) {
            var created = HtmlHelper.publishHtml();
            var obj = new JSONObject();
            obj.put("created", created);
            var fromFile = FileHelper.getResourcePath("index.html", false);
            var toFile = Helper.getStringFromProperties("config.properties", "site.to");
            var published = FileHelper.moveFile(fromFile, toFile);
            obj.put("published", published);
            result = new JSONObject(){{
                put(MESSAGE, obj);
            }};
        } else {
            response.setStatus(403);
        }
        return result.toString();
    }
    @GetMapping("/parse")
    String getLocalHtml(final HttpServletRequest request) {
        var url = request.getParameter("url");
        var pageName = request.getParameter("name");
        String content = null;
        URLConnection connection = null;
        try {
            connection =  new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            content = scanner.next();
            scanner.close();
        }catch ( Exception ex ) {
            ex.printStackTrace();
        }
        var html = HtmlHelper.parseHtml(content);
        var htmlObject = new JSONObject();
        htmlObject.put(PAGE_NAME, pageName);
        htmlObject.put("elements", html);
        return htmlObject.toString();
    }

    @GetMapping("/user-profile")
    String getUserProfile(final HttpServletRequest request, final HttpServletResponse response) {

        final var token = request.getHeader(TOKEN);
        final var id = QueryHelper.getIdByToken(token);
        final var userId = request.getParameter("userId");

        var result = Helper.getFailedObject();
        if (Helper.isThing(id)) {
            if (QueryHelper.isAdmin(id)) {
                result = QueryHelper.loginAsUser(userId);
            } else {
                response.setStatus(403);
            }
        } else {
            response.setStatus(401);
        }
        return result.toString();
    }


    @GetMapping("/reset")
    String reset(final HttpServletRequest request, final HttpServletResponse response) {

        final var token = request.getHeader(TOKEN);
        final var id = QueryHelper.getIdByToken(token);
        final var userId = request.getParameter("userId");
        final var password = request.getParameter("password");
        var result = Helper.getFailedObject().toString();
        if (Helper.isThing(id)) {
            if (QueryHelper.isAdmin(id)) {
                result = QueryHelper.reset(userId, password);
            } else {
                response.setStatus(403);
            }
        } else {
            response.setStatus(401);
        }
        return result;
    }


    @GetMapping("/test-locked")
    String testLocked(final HttpServletRequest request, final HttpServletResponse response) {

        final var token = request.getHeader(TOKEN);
        final var id = QueryHelper.getIdByToken(token);
        final var userId = request.getParameter("userId");
        var result = Helper.getFailedObject();
        if (Helper.isThing(id)) {
            if (QueryHelper.isAdmin(id)) {
                result = new JSONObject();
                var status = (QueryHelper.testLocked(userId)) ? 1 : 0;
                result.put("message", status);
            } else {
                response.setStatus(403);
            }
        } else {
            response.setStatus(401);
        }
        return result.toString();
    }

    @GetMapping("/unlock")
    String unlock(final HttpServletRequest request, final HttpServletResponse response) {

        final var token = request.getHeader(TOKEN);
        final var id = QueryHelper.getIdByToken(token);
        var result = Helper.getFailedObject();
        final var userId = request.getParameter("userId");
        if (Helper.isThing(id)) {
            if (QueryHelper.isAdmin(id)) {
                result = new JSONObject();
                QueryHelper.unlock(userId);
                TestHelper.sleep(300);
                var status = (QueryHelper.testLocked(userId)) ? 1 : 0;
                result.put("message", status);
            } else {
                response.setStatus(403);
            }
        } else {
            response.setStatus(401);
        }
        return result.toString();
    }

    @GetMapping("/amds_use_name")
    String getUserName(final HttpServletRequest request, final HttpServletResponse response) {
        var result = "";

        if (Helper.isThing(QueryHelper.getIdByToken(request.getHeader(TOKEN)))) {
            var profile = QueryHelper.getProfile(request.getHeader(TOKEN));
            for (var i = 0; i < profile.length(); i++) {
                if (profile.getJSONObject(i).getString("element").equals("name")) {
                    result = profile.getJSONObject(i).getString("content");
                    break;
                }
            }

        } else {
            response.setStatus(401);
        }
        return result;
    }

    @PostMapping("/register")
    String register(final HttpServletRequest request, final HttpServletResponse response) {
        var obj = RequestHelper.getRequestBody(request);
        var token = request.getHeader(TOKEN);
        var id = QueryHelper.getIdByToken(token);
        var result = Helper.getFailedObject();
        if (Helper.isThing(id)) {
            if (QueryHelper.isAdmin(id)) {
                result = new JSONObject();
                var body = new JSONObject();
                body.put("email", obj.getString("email"));
                body.put("password", obj.getString("password"));
                var preUser = QueryHelper.getLastUser();
                result.put("create", QueryHelper.postData(Helper.getUrl("auth.url") + "/register", body));
                TestHelper.sleep(2000);
                var afterUser = QueryHelper.getLastUser();
                if (Integer.parseInt(afterUser) > Integer.parseInt(preUser)) {
                    var name = obj.getString("name");
                    result.put("rename", QueryHelper.updateName(name, afterUser));
                    result.put("unlock", QueryHelper.unlock(afterUser));
                } else {
                    result.put("user", afterUser);
                    response.setStatus(500);

                }

            } else {
                response.setStatus(403);

            }

        } else {
            response.setStatus(401);
        }


        return result.toString();
    }

    @PostMapping("/signin")
    String signIn(final HttpServletRequest request, final HttpServletResponse response) {
        var obj = RequestHelper.getRequestBody(request);
        var body = new JSONObject();
        body.put("email", obj.getString("email"));
        body.put("password", obj.getString("password"));
        return QueryHelper.postData(Helper.getUrl("auth.url") + "/login", body).toString();
    }

    @GetMapping("/single-test")
    String getSingleTest(final HttpServletRequest request, final HttpServletResponse response) {
        var token = request.getParameter("token").replace(" ", "+");
        var project = QueryHelper.getProject(token);
        var status = Helper.getFailedObject();
        if (Helper.isThing(project)) {
            var id = request.getParameter("id");
            status = QueryHelper
                    .getData("select name,test from shmest.tests where id='" + id + "'", "pull-table");

        } else {
            response.setStatus(401);
        }
        return status.toString();
    }

    @GetMapping("/handle-user")
    String delUser(final HttpServletRequest request, final HttpServletResponse response) {
        final var token = request.getHeader(TOKEN);
        final var userId = QueryHelper.getIdByToken(token);
        final var toGoUserId = QueryHelper.getIdByToken(request.getHeader(USER_TOKEN));
        var result = Helper.getFailedObject();
        if (Helper.isThing(toGoUserId)) {


            if(Helper.isThing(userId)) {
            if(QueryHelper.isAdmin(userId) && !QueryHelper.isAdmin(toGoUserId)) {
                    result = ProfileHelper.handleUseDeletion(Integer.parseInt(toGoUserId));

                } else {
                    response.setStatus(403);
                }
            } else {
                response.setStatus(401);
            }


        } else {
            response.setStatus(400);
        }
        return result.toString();
    }

    @PostMapping("/save-test")
    String saveTest(final HttpServletRequest request, final HttpServletResponse response) {
        var object = RequestHelper.getRequestBody(request);
        var token = object.getString("token");
        var status = Helper.getFailedObject();
        var project = QueryHelper.getProject(token);
        if (Helper.isThing(project)) {
            var tests = object.getString("test");
            var id = object.getString("id");
            var name = object.getString("name");
            var query = "insert into shmest.tests values('?','?','?','?','')"
                    .replaceFirst(QUESTION_MASK, id)
                    .replaceFirst(QUESTION_MASK, project)
                    .replaceFirst(QUESTION_MASK, name)
                    .replaceFirst(QUESTION_MASK, tests);
            var idQuery = "select id from shmest.tests where id='" + id + "' and project='" + project + "'";
            var savedId = QueryHelper.getData(idQuery, "pull-string");
            if (savedId != null
                    && savedId.has("message")
                    && Helper.isThing(savedId.getString("message"))) {
                query = "update shmest.tests set test='"
                        + tests + "' where id='" + id + "' and project='" + project + "'";
            }
            var stat = QueryHelper.getData(query, "execute");
            status.put("message", stat.getString("message"));
        } else {
            response.setStatus(401);

        }
        return status.toString();
    }

    @GetMapping("/delete-test")
    String deleteTest(final HttpServletRequest request, final HttpServletResponse response) {
        String token = request.getParameter(TOKEN).replace(" ", "+");
        var project = QueryHelper.getProject(token);
        var query = "delete from shmest.tests where project='"
                + project + "' and id='" + request.getParameter("id") + "'";
        var data = QueryHelper.getData(query, "execute");

        return data.getJSONArray("message").toString();
    }

    @GetMapping("/tests")
    String getTests(final HttpServletRequest request, final HttpServletResponse response) {
        String token = request.getParameter(TOKEN).replace(" ", "+");
        var project = QueryHelper.getProject(token);
        var query = "select id,name from shmest.tests where project='?'".replace(MASK, project);
        var data = QueryHelper.getData(query, "pull-table");

        return data.getJSONArray("message").toString();
    }

    @GetMapping("/defs")
    String getDefs() throws IOException {
        var defs = FileHelper.getResourceFile("defs.json", false);
        return FileUtils.readFileToString(defs, StandardCharsets.UTF_8);

    }


    @GetMapping("/amds_enabled_sheets")
    String getAmdsEnabledSheets(final HttpServletRequest request, final HttpServletResponse response) {
        var object = Helper.getFailedObject();
        var token = request.getHeader(TOKEN).replace(" ", "+");
        var id = QueryHelper.getIdByToken(token);
        var ids = QueryHelper.getEnabledAmdsSheets(id);

        object.put("message", JsonHelper.getArrayFromList(ids));
        return object.toString();
    }

    @GetMapping("/is-one")
    String isOnefinal(HttpServletRequest request, final HttpServletResponse response) {
        var token = request.getHeader(TOKEN);
        var id = QueryHelper.getIdByToken(token);
        return (QueryHelper.isAdmin(id)) ? "1" : "0";
    }



    @GetMapping("/amds-init-file")
    String generateUserFile(final HttpServletRequest request,
                            final HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        final var fileId = request.getParameter(FILE_ID);
        final var token = request.getHeader(TOKEN);
        final var id = QueryHelper.getIdByToken(token);
        final var userId = request.getParameter(USER_ID);
        var status = Helper.getFailedObject();
        if (Helper.isThing(id)) {
            if (QueryHelper.isAdmin(id)) {
                PdfHelper.saveUserPdf(userId, fileId);
                status = new JSONObject();
                status.put("message", "success");
            } else {
                response.setStatus(403);
            }
        } else response.setStatus(401);
        return status.toString();
    }

    @GetMapping("/download-pdf")
    ResponseEntity<Resource>  downloadPdf(final HttpServletRequest request,
                                       final HttpServletResponse response) throws MalformedURLException {
        var token = request.getHeader(TOKEN);
        var fileId = request.getParameter(FILE_ID);
        var id = QueryHelper.getIdByToken(token);

        var userId = request.getParameter(USER_ID);

        if (QueryHelper.isAdmin(id)) {




            Path fileDirectory = Paths
                    .get(TestHelper.getSameLevelProject("files") +  "/output_" + fileId + "_" + userId + ".pdf");
            Path filePath = fileDirectory
                    .resolve(TestHelper.getSameLevelProject("files") +  "/output_" + fileId + "_" + userId + ".pdf");
            var resource = new UrlResource(filePath.toUri());

            String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

            // Set content disposition header
            String disposition = "attachment; filename=" + resource.getFilename();

            // Create response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(Objects.requireNonNull(resource.getFilename()))
                    .build());


            return ResponseEntity.ok().headers(headers).body(resource);

        } else {
            response.setStatus(403);
        }

        return null;
    }

    @GetMapping("/amds-download-page")
    ResponseEntity<Resource> amdsDownloadPage(final HttpServletRequest request,
                                              final HttpServletResponse response) throws MalformedURLException {
        var token = request.getHeader(TOKEN);
        var managedToken = request.getHeader(USER_TOKEN);
        var sheetId = request.getParameter(ID);

        var managedId = TestHelper.getManagedUserId(token, managedToken);


        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            var query = AmdsHelper.getSheetQuery(sheetId);
            if (Helper.isThing(managedId)) {
                try {
                    query = AmdsHelper.getSheetQuery(sheetId, managedId);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            var data = QueryHelper.getData(query, PULL_TABLE);

            AmdsHelper.saveUserSheet(sheetId, data, QueryHelper.getIdByToken(token));
            Path fileDirectory = Paths.get(AmdsHelper.getAmdsFilesPath(AmdsHelper.getTableName(sheetId)));
            Path filePath = fileDirectory.resolve(AmdsHelper.getAmdsFilesPath(AmdsHelper.getTableName(sheetId)));
            var resource = new UrlResource(filePath.toUri());

        //    String disposition = "attachment; filename=" + resource.getFilename();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'wasm-unsafe-eval' 'inline-speculation-rules'");
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(Objects.requireNonNull(resource.getFilename()))
                    .build());


            return ResponseEntity.ok().headers(headers).body(resource);

        } else {
            response.setStatus(403);
        }

        return null;
    }




    @GetMapping("/download")
    ResponseEntity<Resource>  download(final HttpServletRequest request,
                                       final HttpServletResponse response) throws MalformedURLException {
        var token = request.getHeader(TOKEN);
        var id = QueryHelper.getIdByToken(token);
        var sheetId = request.getParameter(ID);
        var userId = request.getParameter(USER_ID);
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        var dataId = "";
        if (QueryHelper.isAdmin(id)) {
            var query = AmdsHelper.getSheetQuery(sheetId);
            if (Helper.isThing(userId)) {
                try {
                    dataId = (Helper.isThing(AmdsHelper.getUserIdByEmail(userId)))
                            ? AmdsHelper.getUserIdByEmail(userId) : "";
                    query = AmdsHelper.getSheetQuery(sheetId, dataId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            var data = QueryHelper.getData(query, PULL_TABLE);

            AmdsHelper.saveSheet(sheetId, data, dataId);
            Path fileDirectory = Paths.get(AmdsHelper.getAmdsFilesPath(AmdsHelper.getTableName(sheetId)));
            Path filePath = fileDirectory.resolve(AmdsHelper.getAmdsFilesPath(AmdsHelper.getTableName(sheetId)));
            var resource = new UrlResource(filePath.toUri());

            //String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

            // Set content disposition header
            String disposition = "attachment; filename=" + resource.getFilename();

            // Create response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'wasm-unsafe-eval' 'inline-speculation-rules'");
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(Objects.requireNonNull(resource.getFilename()))
                    .build());


            return ResponseEntity.ok().headers(headers).body(resource);

        } else {
            response.setStatus(403);
        }

        return null;
    }



    @GetMapping("/amds-download-all")
    ResponseEntity<Resource>  downloadAll(final HttpServletRequest request,
                                       final HttpServletResponse response) throws MalformedURLException {
        var token = request.getHeader(TOKEN);
        var id = QueryHelper.getIdByToken(token);
        final var path = Constants.MAIN_RESOURCES + "total.xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        var dataId = "";
        if (QueryHelper.isAdmin(id)) {
            ExcelHelper.saveMassiveExcel(path);
            Path fileDirectory = Paths.get(path);
            Path filePath = fileDirectory.resolve(path);
            var resource = new UrlResource(filePath.toUri());
            String disposition = "attachment; filename=" + resource.getFilename();

            // Create response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Security-Policy", "script-src 'self' 'unsafe-inline' 'wasm-unsafe-eval' 'inline-speculation-rules'");
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(Objects.requireNonNull(resource.getFilename()))
                    .build());

            stopWatch.stop();
            System.out.println("LOADING TOOK: " + stopWatch.getTime(TimeUnit.MINUTES));
            return ResponseEntity.ok().headers(headers).body(resource);

        } else {
            response.setStatus(403);
        }

        return null;
    }


    @GetMapping("/amds_mri_skills")
    String getAmdsMriSkillsTable(final HttpServletRequest request, final HttpServletResponse response) {
        var result = new JSONObject();
        var user = getProject(request);
        var query = "select row_name,phillips,siemens,ge,comments from amds.mri_skills where user_id='" + user + "'";
        return QueryHelper.getData(query, "pull-table").toString();
    }

    @GetMapping("/amds_sheet")
    String getAmdsSheet(final HttpServletRequest request, final HttpServletResponse response) {

        var token = request.getHeader(TOKEN);
        var userToken = request.getHeader(USER_TOKEN);
        var sheetId = request.getParameter(ID);
        var userId = QueryHelper.getIdByToken(token);
        if (QueryHelper.isAdmin(userId)) {
            if (Helper.isThing(userToken) && !userToken.equalsIgnoreCase("null")) {
                userId = QueryHelper.getIdByToken(userToken);
            }
        }
        var query = AmdsHelper.getSheetQuery(sheetId, userId);

        return QueryHelper.getData(query, PULL_TABLE).toString();
    }

    @GetMapping("/amds_table")
    String getAmdsTable(final HttpServletRequest request, final HttpServletResponse response) {

        var token = request.getHeader(TOKEN);

        var sheetId = request.getParameter(ID);
        var result = new JSONObject();
        var userId = request.getParameter("user_id");
        if (Helper.isThing(QueryHelper.getIdByToken(token))) {
            if (QueryHelper.isAdmin(QueryHelper.getIdByToken(token))) {
                var query = AmdsHelper.getSheetQuery(sheetId, userId);
                result = QueryHelper.getData(query, PULL_TABLE);
            } else {
                response.setStatus(403);
            }
        } else {
            response.setStatus(401);
        }

        return (result.has("message")) ? result.getJSONArray("message").toString() : result.toString();
    }

    @GetMapping("/amds_table_stats")
    String getTableStats(final HttpServletRequest request, final HttpServletResponse response) {
        var token = request.getHeader(TOKEN);
        var userId = QueryHelper.getIdByToken(token);
        var reverse = (Helper.isThing(request.getParameter("reverse"))
                && request.getParameter("reverse").equals("true"));
        var result = new JSONArray();
        if (Helper.isThing(userId)) {
            if (QueryHelper.isAdmin(userId)) {
               result = AmdsHelper.getTableStats(reverse);
            } else {
                response.setStatus(403);

            }

        } else {
            response.setStatus(401);

        }
        return result.toString();
    }

    @GetMapping("/amds_sheet_users")
    String getAmdsSheetUsers(final HttpServletRequest request, final HttpServletResponse response) {
        var token = request.getHeader(TOKEN);
        var userId = QueryHelper.getIdByToken(token);
        var sheet_id = request.getParameter("sheet_id");
        var reverse = request.getParameter("reverse");
        var result = new JSONArray();
        if (Helper.isThing(userId)) {
            if (QueryHelper.isAdmin(userId)) {
                if (Helper.isThing(reverse) && reverse.equals("true")) {
                    result = TestHelper.dedupe(QueryHelper.getSheetNotUsers(sheet_id));
                } else {
                    result = TestHelper.dedupe(QueryHelper.getSheetUsers(sheet_id));
                }

            } else {
                response.setStatus(403);

            }

        } else {
            response.setStatus(401);

        }

        return result.toString();
    }

    @GetMapping("/amds_user_sheets")
    String getAmdsUserSheets(final HttpServletRequest request, final HttpServletResponse response) {
        var token = request.getHeader(TOKEN);
        var userId = QueryHelper.getIdByToken(token);
        var id = (Helper.isThing(request.getParameter("userId"))) ? request.getParameter("userId") : userId;
        var reverse = request.getParameter("reverse");
        var all = request.getParameter("all");
        var result = new JSONArray();
        if (Helper.isThing(userId)) {
            if (QueryHelper.isAdmin(userId)) {
                if (Helper.isThing(all) && all.equals("true")) {
                    result = AmdsHelper.getAllTables();
                } else {
                    result = QueryHelper.getUserSheets(id, (Helper.isThing(reverse) && reverse.equals("true")));
                }


            } else {
                response.setStatus(403);

            }

        } else {
            response.setStatus(401);

        }
        return result.toString();
    }

    @GetMapping("/amds_set_ignore")
    String setIgnore(final HttpServletRequest request, final HttpServletResponse response) {

        if (request.getHeader("secret").equals("Kusaz9dkakwk45th_823haj-aashjgd")) {
            var state = Helper.isThing(request.getParameter("ignore"))
                    && request.getParameter("ignore").equals("true");
            QueryHelper.setState("ignore", state);
            return new JSONObject(String.format("{'%s':%b}", MESSAGE, QueryHelper.getState("ignore"))).toString();
        } else {
            response.setStatus(401);
        }
        return new JSONObject().toString();
    }


    @PostMapping("/amds_contact")
    String amdsContact(final HttpServletRequest request, final HttpServletResponse response) {
        var result = Helper.getFailedObject();
        var object = RequestHelper.getRequestBody(request);
        var name = object.getString("name");
        var email = object.getString("email");
        var subj = object.getString("subject");
        var message = object.getString("text");
        var body = new JSONObject();
        body.put("email", email);
        body.put("template", "<a></a>");
        body.put("subject", subj);
        body.put("name", name);
        body.put("text", message);
        body.put("secret", "qdb82qjd!^&shaagsa ashjjsag &^(");
        body.put("to", "mkarpoff@mail.ru");
        Map<String, String> headers = new HashMap<>(){{
           put("secret", "qdb82qjd!^&shaagsa ashjjsag &^(");
        }};
        result = QueryHelper.postData(Helper.getUrl("email.url") + "/contact", body, headers);

        return result.toString();
    }
    @PostMapping("/amds_set_layout")
    String setLayout(final HttpServletRequest request, final HttpServletResponse response) {
        var result = Helper.getFailedObject();
        var auth = new Auth(request);
        if(auth.getIsAdmin()) {

            var object = RequestHelper.getRequestBody(request);
            var id = (object.has(ID) && object.getInt(ID) > -1) ? object.getInt(ID) : AmdsHelper.getNewLayoutId();
            result = AmdsHelper.setLayot(id, object.getString("name"), object.getString(TEMPLATE));
        } else {
            response.setStatus(403);
        }
        return result.toString();
    }

    @PostMapping("/amds_delete_layout")
    String deleteLayout(final HttpServletRequest request, final HttpServletResponse response) {
        var auth = new Auth(request);
        if (auth.getIsAdmin()) {
            var object = RequestHelper.getRequestBody(request);
            return QueryHelper.deleteLayout(object.getInt(ID)).toString();
        } else {
            response.setStatus(403);
            return Helper.getFailedObject().toString();
        }
    }
    @PostMapping("/amds_set_template")
    String setTemplate(final HttpServletRequest request, final HttpServletResponse response) {
        var result = Helper.getFailedObject();
        var auth = new Auth(request);
        if(auth.getIsAdmin()) {
            var object = RequestHelper.getRequestBody(request);
            result = AmdsHelper.setTemplate(object.getInt(ID), object.getInt(LAYOUT), object.getString(TEMPLATE), object.getString(NAME));
        } else {
            response.setStatus(403);
        }
        return result.toString();
    }
    @PostMapping("/amds_set_section")
    String setSection(final HttpServletRequest request, final HttpServletResponse response) {
        var result = Helper.getFailedObject();
        var auth = new Auth(request);
        if(auth.getIsAdmin()) {
            var object = RequestHelper.getRequestBody(request);
            result = AmdsHelper
                    .setSection(object.getInt(ID),
                            object.getJSONArray(SEQUENCE),
                            AmdsHelper.enrichSection(object.getInt(ID),
                                    object.getJSONObject(SECTION_DATA)),
                            object.getJSONObject(SECTION_DATA));
        } else {
            response.setStatus(403);
        }
        return result.toString();
    }

    @GetMapping("/amds_get_section")
    String amdsGetSection(final HttpServletRequest request, final HttpServletResponse response) {
        var result = Helper.getFailedObject();
        var auth = new Auth(request);
        if(auth.getIsAdmin()) {
            result = AmdsHelper.getSection(Integer.parseInt(request.getParameter(ID)));
        } else {
            response.setStatus(403);
        }
        return result.toString();
    }

    @GetMapping("/amds_get_layout")
    String amdsGetLayout(final HttpServletRequest request, final HttpServletResponse response) {
        var result = Helper.getFailedObject();
        var auth = new Auth(request);
        if(auth.getIsAdmin()) {
            result = AmdsHelper.getLayout(Integer.parseInt(request.getParameter(ID)));
        } else {
            response.setStatus(403);
        }
        return result.toString();
    }
    @GetMapping("/amds_get_sections")
    String amdsGetSections(final HttpServletRequest request, final HttpServletResponse response) {
        return AmdsHelper.getSections().toString();
    }
    @GetMapping("/amds_get_templates")
    String getTemplates(final HttpServletRequest request, final HttpServletResponse response) {
        return AmdsHelper.getTemplates().toString();
    }

    @GetMapping("/amds_get_template")
    String getTemplate(final HttpServletRequest request, final HttpServletResponse response) {
        var auth = new Auth(request);
        if (auth.getIsAdmin()) {
            return AmdsHelper.getTemplate(Integer.parseInt(request.getParameter(ID))).toString();
        } else {
            response.setStatus(403);
            return Helper.getFailedObject().toString();
        }

    }
    @GetMapping("/amds_get_layouts")
    String amdsGetLayouts(final HttpServletRequest request, final HttpServletResponse response) {
        return AmdsHelper.getLayouts().toString();
    }
    @GetMapping("/amds_users")
    String getAmdsUsers(final HttpServletRequest request, final HttpServletResponse response) {
        var token = request.getHeader(TOKEN);
        var userId = QueryHelper.getIdByToken(token);
        var name = request.getParameter("name");
        var result = new JSONArray();
        if (Helper.isThing(userId)) {
            if (QueryHelper.isAdmin(userId)) {
                if (!Helper.isThing(name)) {
                    result = QueryHelper.getUsers();

                } else {
                    result = QueryHelper.getUsersByName(name);
                }


            } else {
                response.setStatus(403);

            }

        } else {
            response.setStatus(401);

        }
            return result.toString();
    }

    @GetMapping("/amds_columns")
    String getAmdsColumns(final HttpServletRequest request, final HttpServletResponse response) {

        var token = request.getHeader(TOKEN);
        var sheetId = request.getParameter(ID);
        var userId = QueryHelper.getIdByToken(token);
        if (Helper.isThing(userId)) {
            return AmdsHelper.getColumns(sheetId);
        } else {
            response.setStatus(401);
            return "";
        }
    }


    @GetMapping("/amds-model")
    public String get_table_model(final HttpServletRequest request, final HttpServletResponse response) {
        var result = TestHelper.getStatus("failure");
        var userId = QueryHelper.getIdByToken(request.getHeader(TOKEN));
        var id = request.getParameter(ID);
        if(Helper.isThing(userId)) {
            var isAdmin = QueryHelper.isAdmin(userId);

            if (Helper.isInteger(id)) {
                var model = QueryHelper.getRowsModel(Integer.parseInt(id)).getJSONArray(MESSAGE);
                var fields = QueryHelper.getFieldsModel(Integer.parseInt(id)).getJSONArray(MESSAGE);
                var col = AmdsHelper.getColumns(id);
                var userFields = fields.getJSONObject(0).getString("user_ui_fields");
                var tableName = fields.getJSONObject(0).getString("mask_name");
                var savedUserFields = fields.getJSONObject(0).getString("user");
                var uiFields = fields.getJSONObject(0).getString("ui_fields");
                var fieldsModel = fields.getJSONObject(0).getString("row_fields");
                var head = fields.getJSONObject(0).getString("head");
                var fieldModelObject = new JSONObject(fieldsModel);

                result = new JSONObject();
                result.put("model", model);
                result.put("fieldsModel", fieldModelObject);
                result.put("userFields", userFields);
                result.put("savedUserFields", savedUserFields);
                result.put("legacyFields", col);
                result.put("fields", uiFields);
                result.put("head", head);
                result.put("boss", isAdmin);
                result.put("table_name", tableName);
                System.out.println(fields.toString(5));
            } else {
                response.setStatus(400);
            }

        } else {
            response.setStatus(401);
        }

        return result.toString();
    }

    @PostMapping("/amds-upload")
    public String uploadFile(Model model, @RequestParam("file") MultipartFile file,
                             HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream in = file.getInputStream();
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        var sheetId = request.getParameter("id");
        var usToken = (Helper.isThing(request.getParameter("userToken")))
                ? request.getParameter("userToken").replace(" ", "+") : "";
            System.out.println("USER TOKEN AS PARAM: " + usToken);
        var id = QueryHelper.getIdByToken(request.getHeader("token"));
            System.out.println("MAIN ID: " + id);
            System.out.println("USER_TOKEN: " + request.getHeader("user_token"));
        var managedId = (Helper.isThing(usToken))
                ? QueryHelper.getIdByToken(usToken) : "";
            System.out.println("MANAGED_ID: " + managedId);
        var adminIntactOrIAdmin = true;

        var fileLocation = path.substring(0, path.length() - 1) + file.getOriginalFilename();

        FileOutputStream f = new FileOutputStream(fileLocation);
        int ch = 0;
        while ((ch = in.read()) != -1) {
            f.write(ch);
        }
        f.flush();
        f.close();
      //  var sheetId = QueryHelper.getSheetIdByExcelName(Objects.requireNonNull(file.getOriginalFilename()));

        model.addAttribute("message", "File: " + file.getOriginalFilename()
                + " has been uploaded successfully!");

        var array = FileReader.persistExcel(fileLocation, Integer.parseInt(sheetId), id).getJSONArray(MESSAGE);
        var savedFile = new File(fileLocation);
//        if (savedFile.exists())
//            savedFile.delete();
        if (!QueryHelper.isAdmin(id)) {
            adminIntactOrIAdmin = AmdsHelper.adminColumnsIntact(array, sheetId, id).isOk();

        }

        if (adminIntactOrIAdmin) {
            var userId = (Helper.isThing(managedId)) ? managedId : id;
            var delQuery = "delete from amds."
                    + AmdsHelper.getTableName(Integer.toString(Integer.parseInt(sheetId))) + " where user_id='" + userId + "'";
            QueryHelper.getData(delQuery, "execute");
            var query = AmdsHelper.createSheetPopulateQuery(sheetId, userId, array);
            System.out.println(query);
            var status = QueryHelper.getData(query, "execute").toString();
            System.out.println(status);
            return status;

        } else {
            response.setStatus(403);
            return new JSONObject(MESSAGE, "something went wrong").toString();
        }
    }

    @PostMapping("/amds_create_sheet")
    String createAmdsSheet(final HttpServletRequest request, final HttpServletResponse response) {
        var object = RequestHelper.getRequestBody(request);
        var statusObject = new JSONObject();
        statusObject.put("status", new JSONArray());
        var token = object.getString(TOKEN);
        var userToken = ((object.has(USER_TOKEN)) && object.get(USER_TOKEN) instanceof String) ? object.getString(USER_TOKEN) : "";

        var sheetId = "";
        if (object.get(ID) instanceof String) {
           sheetId = object.getString(ID);
        } else {
            sheetId = Integer.toString(object.getInt(ID));

        }
        var sheetName = AmdsHelper.getTableName(sheetId);

        var userId = QueryHelper.getIdByToken(token);
        var managedId = (Helper.isThing(userToken)) ? QueryHelper.getIdByToken(userToken) : userId;
        if (Helper.isThing(userId)) {
            var isAdmin = QueryHelper.isAdmin(userId);
            var adminCols = "row_name," + AmdsHelper.getAdminColumns(sheetId);
            var selQuery = "select " + adminCols + " from amds." + sheetName + " where user_id='" + managedId + "'";
            var obj = new JSONObject();
            var table = object.getJSONArray("table");
            var adminIntact = new UsefulBoolean();
            if (!isAdmin) {
                obj = QueryHelper.getData(selQuery, PULL_TABLE);
                var old = (obj.has(MESSAGE)) ? obj.getJSONArray(MESSAGE) : new JSONArray();
                adminIntact = AmdsHelper.adminColumnsIntact(old, table, adminCols, sheetId);
                System.out.println(adminIntact.getMessage());
            }
            if (adminIntact.isOk()) {
                var delQuery = "delete from amds." + sheetName + " where user_id='" + managedId + "'";
                QueryHelper.getData(delQuery, "execute");


                var inQuery = AmdsHelper.createSheetPopulateQuery(sheetId, managedId, table);



                QueryHelper.getData(inQuery, "execute");

            } else {
                response.setStatus(401);
            }


            return statusObject.toString();

            } else {
            response.setStatus(400);
            return "invalid operation";

        }

    }

    @PostMapping("/amds_mri_skills_create")
    String postAmdsMriSkillsTable(final HttpServletRequest request, final HttpServletResponse response) {
        var result = new JSONObject();
        var object = RequestHelper.getRequestBody(request);
        var user = QueryHelper.getIdByToken(object.getString(TOKEN));
        var table = object.getJSONArray("table");
            var delQuery = "delete from amds.mri_clinical_skills where user_id='" + user + "'";
        if (Helper.isThing(user)) {
            QueryHelper.getData(delQuery, "execute");
            for (var i  = 0; i < table.length(); i++) {
                var row = table.getJSONObject(i);
                if (i == 135) {
                    System.out.print("");

                }
                var rowName = row.getString("rowName")
                        .replace("'", "\\\\'").replace("?", "<q>");
                var phillips = row.getString("phillips");
                var siemens = row.getString("siemens");
                var ge = row.getString("ge");
                var comment = row.getString("comment").replace("'", "\\\\'");
                var query = "insert into amds.mri_clinical_skills values('?','?','?','?','?','?')"
                        .replaceFirst(QUESTION_MASK, rowName)
                        .replaceFirst(QUESTION_MASK, phillips)
                        .replaceFirst(QUESTION_MASK, siemens)
                        .replaceFirst(QUESTION_MASK, ge)
                        .replaceFirst(QUESTION_MASK, comment)
                        .replaceFirst(QUESTION_MASK, user);
                result.put(rowName, QueryHelper.getData(query, "execute"));
                System.out.print(i);
            }

        }


        return result.toString();
    }

    @PostMapping("/post-page")
    @ResponseBody
    String postPage(final HttpServletRequest request, final HttpServletResponse response) {
        var result = new JSONObject();
        var object = RequestHelper.getRequestBody(request);
        var token = object.getString(TOKEN);
        var project = QueryHelper.getProject(token);
        if (Helper.isThing(project)) {
            var page = object.getJSONObject("pageobject");
            page.put("project", project);
            var persisted = QueryHelper.persistSavedPage(page);
            if (persisted.getString("message").equals("success")) {
                result = QueryHelper.getSinglePage(project, page.getString("pagename"));
            }
        }

        return result.toString();
    }

    @PostMapping("/html")
    @ResponseBody
    String readHtml(final HttpServletRequest request, final HttpServletResponse response) {
        var result = new JSONObject();
        var object = RequestHelper.getRequestBody(request);
        var pageName = object.getString(PAGE_NAME);
        var html = object.getString(HTML);
        if (Helper.isThing(html)) {
            var pageObject = HtmlHelper.parseHtml(html);
            result.put(PAGE_NAME, pageName);
            result.put("elements", pageObject);
            result.put("project", object.getString("project"));
            result.put("url", object.getString("url"));
            System.out.print(result.toString(5));
            System.out.print(QueryHelper.persistPage(result));

        }

        return result.toString();
    }

    @PostMapping("/step")
    String saveStep(final HttpServletRequest request, final HttpServletResponse response) {
        var object = RequestHelper.getRequestBody(request);
        var secret = object.getString("secret");
        var result = new JSONObject("{\"message\":{\"status\":\"failure\"}}");
        System.out.println("SAVING STEP");
        if (secret.equals(SECRET)) {
            var statObject = new JSONObject();
            statObject.put("status",  QueryHelper.saveRunStep(object));
            result.put("message", statObject);
        } else {
            response.setStatus(401);
        }

        return result.toString();
    }

    @GetMapping("/log")
    String getLogs(final HttpServletRequest request, final HttpServletResponse response) {
        var project = getProject(request);
        var records = (Helper.isThing(request.getParameter("records")))
                ? Integer.parseInt(request.getParameter("records")) : 0;
        var result = new JSONObject();
        if (Helper.isThing(project)) {
            if (Helper.isThing(request.getParameter("area"))) {
                result = QueryHelper.getLogs(records, Area.valueOf(request.getParameter("area").toUpperCase()));
            } else {

                result = QueryHelper.getLogs(records);
            }

        } else {
            QueryHelper.logEntry("UNAUTHORIZED ACCESS TO LOGS ATTEMPTED", "", Area.QUERY_HELPER.label);
            response.setStatus(401);
        }
        return result.toString();
    }

    @GetMapping("/step-stat")
    String getStepStat(final HttpServletRequest request, final HttpServletResponse response) {
        var project = getProject(request);
        var result = new JSONObject();
        result.put("message", TestHelper.getStatus("pending"));
        if (Helper.isThing(project)) {
            return QueryHelper.getStepStatus(request.getParameter(RUN_ID), request.getParameter(STEP_ID), project);
        } else {
            response.setStatus(401);
        }
        return result.toString();
    }

    static class Runner extends Thread {
        @SneakyThrows
        public void run() {
            CommandRunner.runCommand();
        }

    }

    @PostMapping("/start")
    String startTest(final HttpServletRequest request, final HttpServletResponse response) throws java.net.MalformedURLException {
        var result = new JSONObject();

        var runId = Helper.getRandomString(8);
        var object = RequestHelper.getRequestBody(request);
        if(object.has(RUN_ID) && !object.getString(RUN_ID).equals("undefined")) {
            runId = object.getString(RUN_ID);
        }

        var scenarioId = (object.has("scenarioId"))
                ? object.getString("scenarioId") : Helper.getRandomString(10);
        var token = object.getString(TOKEN);
        var project = QueryHelper.getProject(token);
        var url = object.getString("baseUrl");
        QueryHelper.logEntry("PROJECT: " + project, project, Area.STARTER.label);

        if (Helper.isThing(project)) {
            QueryHelper.logEntry("PROJECT: " + project, project, Area.ATTENTION.label);
            object.put("project", project);
            object.put("projectId", project);
            object.put(RUN_ID, runId);
            object.put("scenarioId", scenarioId);
            var saved = TestHelper.saveTest(object.getJSONObject("tests"),
                    TestHelper.getSameLevelProject(TEST_PROJECT) + DEFAULT_FEATURE);
            result.put("statusSaved", saved);
            var pageSaved = TestHelper.savePages(object.getJSONObject("config"),
                    TestHelper.getSameLevelProject(TEST_PROJECT) + PAGES);
            result.put("pagesSaved", pageSaved);
            result.put("dashboardSaved", QueryHelper.saveDashboard(object));
            result.put("projectObject", object);
            List<String> map = new ArrayList<>();
            map.add("baseUrl=" + url);
            map.add("manager.url=http://localhost:8082/html");
            map.add("project=" + project);
            map.add("runid=" + runId);
            map.add("scenarioid=" + scenarioId);
            map.add("backend.url=http://localhost:8082/");
            map.add("hostname=109.228.57.213");
            map.add("port=4444");
            var configSaved = TestHelper.saveConfig(map, TestHelper.getSameLevelProject(TEST_PROJECT) + CONFIG);
            QueryHelper.logEntry("CONFIG SAVED: " + configSaved, project, "starter");
            result.put("configSaved", configSaved);

            var runner = new Runner();
            runner.start();

//                var output = CommandRunner.runCommand();
//                System.out.print("RAW OUTPUT: " + output);
//                TestHelper.sleep(2000);
            var sendableOut = "";
//                var sendableOut = output;
//                System.out.println();
//                System.out.println(output.indexOf("OpenJDK"));
//                System.out.println(output.indexOf("Skipped: "));
//
//                if (output.contains("Skipped: ") && output.contains("OpenJDK")) {
//
//                    sendableOut = output
//                            .substring(output.indexOf("OpenJDK"));
//                    sendableOut = sendableOut.substring(0, sendableOut.indexOf("Skipped: "));
//
//                } else {
//                    sendableOut = "CORRUPTED: " + output;
//
//                }
            result.put("output", sendableOut);
            QueryHelper.logEntry(sendableOut, project, Area.CUBE.label);
            LOGGER.info("output");

        } else {
            QueryHelper.logEntry("unauthorized request", project, Area.STARTER.label);
            response.setStatus(401);
        }


        return result.toString();
    }

}
