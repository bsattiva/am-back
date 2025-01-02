package com.utils;


import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

public class Helper {
    private static final String HOME = System.getProperty("user.dir");
    private final static Logger LOGGER = Logger.getLogger(Helper.class);
    private static final String SEP = File.separator;
    private static final Random RANDOM = new Random();
    public static String getStringFromProperties(String file,String key) {
        Properties properties = new Properties();
        String result = "";
        try {
            properties.load(new FileInputStream(file));
            result = properties.getProperty(key);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
        return result;
    }


    public static String decrypt(String text, String sec) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            Key key = new SecretKeySpec(messageDigest.digest(sec.getBytes(StandardCharsets.UTF_8)), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(DECRYPT_MODE, key);

            byte[] decoded = Base64.getDecoder().decode(text.getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Unable to decrypt", e);
        }
    }


    public static String getUrl(String key){
        return Helper.getStringFromProperties("config.properties",key);
    }
    public static JSONObject getFailedObject(){
        JSONObject result = new JSONObject();
        result.put("message","failure");
        return result;
    }
    public static JSONObject getSuccessfulObject(){
        JSONObject result = new JSONObject();
        result.put("message","success");
        return result;
    }


    public static String getHomeDir(String[] tree){
        StringBuilder builder = new StringBuilder(HOME);
        Arrays.asList(tree).forEach(folder -> builder.append(SEP + folder));
        return builder.toString();
    }

    public static String encrypt(String text, String sec) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            Key key = new SecretKeySpec(messageDigest.digest(sec.getBytes(StandardCharsets.UTF_8)), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            byte[] encoded = Base64.getEncoder().encode(encrypted);
            return new String(encoded, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Unable to encrypt", e);
        }
    }

    public static String completeString(final String regex, final String body, final String[] arguments) {
        var parts = body.split(regex);
        var builder = new StringBuilder();
        var i = 0;
        for (var part : parts) {
            builder.append(part);
            if (arguments.length > i) {
                builder.append(arguments[i]);
            }
            i++;
        }

        return builder.toString();
    }


    public static String getRandomString(final int length) {
        final String raw = "abcdefghijklmnopqrstwuvxyzABCDEFGHIJKLMNOPQRSTWUVXYZ0123456789";
        var builder = new StringBuilder();
        IntStream.range(0, length).forEach(i -> {
            var num = RANDOM.nextInt(raw.length() -1);
            var nextChar = raw.substring(num, num + 1);
            builder.append(nextChar);
        });
        return builder.toString();
    }

    public static String getCurrentDir() {
        return System.getProperty("user.dir");
    }

    public static boolean isInteger(final String rawString) {
        return NumberUtils.isParsable(rawString);
    }

    public static boolean isThing(final String value) {
        return value != null && !value.isEmpty();
    }

    public static String parseStringWithRegex(final String regex, final String body, final int group) {
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(body);
        var result = "";
        while(matcher.find()) {
            result = matcher.group(group);
        }
        return result;
    }



}
