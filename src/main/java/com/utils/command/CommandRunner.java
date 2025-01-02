package com.utils.command;

import com.utils.Helper;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRunner {

private static final Logger LOGGER = Logger.getLogger(CommandRunner.class);


    public static String runCommand() throws IOException {

        var builder = new ProcessBuilder(CommandHelper.getCommand(Helper.getCurrentDir()));
        var result = "";
        builder.redirectErrorStream(true);
        try (var writer = new StringWriter()) {
            var process = builder.start();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            var responseBuilder = new StringBuilder();
            while ((reader.readLine()) != null) {
                responseBuilder
                        .append(reader.lines().collect(Collectors.joining()));
            }
            process.waitFor();
            IOUtils
                    .copy(new ByteArrayInputStream(responseBuilder.toString().getBytes()),
                            writer, StandardCharsets.UTF_8);
            result = "SUCCESS: " + String.join(" ", List.of(CommandHelper.getCommand(Helper.getCurrentDir()))) + writer.toString();

        } catch (InterruptedException | IOException e) {
            LOGGER.error(e);
            result = "ERROR: " + e.getMessage();
            Thread.currentThread().interrupt();
        }
        return result;
    }
}
