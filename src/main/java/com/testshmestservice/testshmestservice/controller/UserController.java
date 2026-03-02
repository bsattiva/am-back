package com.testshmestservice.testshmestservice.controller;
import com.utils.*;
import com.utils.auth.UserHelper;
import com.utils.data.QueryHelper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.springframework.web.multipart.MultipartFile;


@RestController
@EnableAutoConfiguration
@SpringBootApplication
@CrossOrigin
public class UserController {
    public static final String TOKEN = "token";


    @GetMapping("/get_email")
    String getEmail(final HttpServletRequest request, final HttpServletResponse response) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return QueryHelper.getEmailById(request.getParameter("id"));
        } else {
            response.setStatus(500);
            return "NaN";
        }

    }

    @PostMapping("/amds_bulk")
    String bulkUpload(@RequestParam("file") MultipartFile file,
                      @RequestParam("token") String token, HttpServletResponse response) throws IOException {
        var result = Helper.getFailedObject();

        if(QueryHelper.isAdmin(QueryHelper.getIdByToken(token))) {
            InputStream in = file.getInputStream();
            File currDir = new File(".");
            String path = currDir.getAbsolutePath();
            var fileLocation = path.substring(0, path.length() - 1) + file.getOriginalFilename();

            FileOutputStream f = new FileOutputStream(fileLocation);
            int ch = 0;
            while ((ch = in.read()) != -1) {
                f.write(ch);
            }
            f.flush();
            f.close();
            var status = UserHelper.createUsers(fileLocation);
            if (status.getBoolean("status")) {
                result.put("message", "File: " + file.getOriginalFilename()
                        + " has been uploaded successfully!");
            } else {
                result.put("message", status.getString("failed"));
            }


        } else {
            response.setStatus(403);
        }

        return result.toString();
    }

}
