package com.utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


class TestHelperTest {
    private static final Logger LOGGER = Logger.getLogger(TestHelperTest.class);
    @Test
    void testGetTests() {
        var object = new JSONObject();

        try {
            object = new JSONObject(FileUtils
                    .readFileToString(FileHelper.getResourceFile("test.json", true),
                            StandardCharsets.UTF_8));
            Assert.assertNotNull(object);
            var tests = TestHelper.getTests(object);
            Assert.assertNotNull(tests);
            Assert.assertEquals(tests.size(), 9);

        } catch (JSONException | IOException e) {
            LOGGER.error(e.getMessage());
            throw new AssertionError(e.getMessage());
        }

    }

    @Test
    void saveTest() {
        try {
            var object = new JSONObject(FileUtils
                     .readFileToString(FileHelper.getResourceFile("test.json", true),
                             StandardCharsets.UTF_8));
            Assert.assertNotNull(object);
            var path = TestHelper.getSameLevelProject("temp") + "/src/test/features/defaultFeature.feature";
            TestHelper.saveTest(object, path);

            var fileLines = FileUtils.readLines(new File(path), StandardCharsets.UTF_8);
            Assert.assertEquals(fileLines.size(), 9);

        } catch (JSONException | IOException e) {
            LOGGER.error(e.getMessage());
            throw new AssertionError(e.getMessage());
        }
    }
}