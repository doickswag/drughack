package ru.drughack.api.protection;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.apache.commons.codec.binary.Base32;
import ru.drughack.BuildConfig;
import ru.drughack.DrugHack;
import ru.drughack.utils.interfaces.Wrapper;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.Formatter;

@Getter
public class Protection implements Wrapper {

    //userinfo
    private String user = "idiot";
    private int uid = -1;
    public List<UserInfo> userInfos = new ArrayList<>();

    //other
    private final Gson gson = new Gson();
    private final String name = BuildConfig.NAME;
    private final String version = BuildConfig.VERSION;
    private final String buildTime = BuildConfig.BUILD_TIME;
    private final String token = BuildConfig.TOKEN;
    private final String hash = BuildConfig.HASH;
    private final String commit = BuildConfig.COMMIT;


    private void exit() {
        Runtime.getRuntime().halt(0);
    }

    public void error(String message) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(frame, message, name, JOptionPane.ERROR_MESSAGE);
    }

    private String getHwid() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(getDiskSerial().getBytes());
            StringBuilder hexString = new StringBuilder();
            try (Formatter formatter = new Formatter(hexString)) {
                for (byte b : hashBytes) formatter.format("%02x", b);
            }
            return hexString.toString();
        } catch (Exception e) {
            DrugHack.LOGGER.info("Error in getHwid: {}", e.getMessage());
            e.printStackTrace(System.err);
        }

        return "";
    }

    public String get(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setConnectTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    public String post(String url, String content) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = content.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    private void setUserInfo() {
        File file = new File(System.getProperty("user.home"), File.separator + "Drug" + File.separator + ".authtoken");
        if (!file.exists()) {
            error("Auth Token not found, please reinstall %s with installer!".formatted(name));
            exit();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String authToken = reader.readLine();
            if (authToken == null || authToken.isEmpty()) {
                error("Auth Token is empty!");
                exit();
            }

            String decryptedToken = decrypt(authToken, "d2h5IHlvdSBkb2luZz8=");

            for (UserInfo info : userInfos) {
                if (info.getHwid().equals(getHwid())) {
                    user = info.getUser();
                    uid = getUserIndex(info);
                    return;
                } else if (info.getHwid().equals("none")) {
                    user = info.getUser();
                    uid = getUserIndex(info);
                    changeHwidInfo(new UserInfo(user, info.password, getHwid()));
                    return;
                }
            }

            error("Your Hardware ID is invalid or user not found!");
            exit();
        } catch (Exception e) {
            error("Failed to read Auth Token!");
            exit();
        }
    }

    private int getUserIndex(UserInfo target) {
        int index = 1;
        for (UserInfo info : userInfos) {
            if (info.equals(target)) return index;
            index++;
        }
        return -1;
    }

    public void updateUserInfo() {
        try {
            String response = get("http://f1.rustix.me:41722/auth/users");
            if (response != null && !response.isEmpty()) userInfos = gson.fromJson(response, new TypeToken<List<UserInfo>>() {}.getType());
        } catch (Exception e) {
            DrugHack.LOGGER.info("Error in updateUserInfo: {}", e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public static String decrypt(String encryptData, String password) {
        try {
            byte[] combined = Base32.builder().get().decode(encryptData);
            byte[] salt = Arrays.copyOfRange(combined, 0, 32);
            byte[] iv = Arrays.copyOfRange(combined, 32, 32 + 16);
            byte[] encrypted = Arrays.copyOfRange(combined, 32 + 16, combined.length - 64);
            byte[] hmacDigest = Arrays.copyOfRange(combined, combined.length - 64, combined.length);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(secretKey.getEncoded(), "HmacSHA512"));
            hmac.update(iv);
            hmac.update(encrypted);
            byte[] calculatedHmac = hmac.doFinal();
            if (!MessageDigest.isEqual(hmacDigest, calculatedHmac)) throw new SecurityException("HMAC verification failed - token tampered");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            DrugHack.LOGGER.info("Error in decrypt: {}", e.getMessage());
            e.printStackTrace(System.err);
        }

        return "skull";
    }

    public void changeHwidInfo(UserInfo info) {
        try {
            String json = gson.toJson(info);
            String response = post("http://f1.rustix.me:41722/auth/change-hwid", json);

            if (response.isEmpty()) {
                DrugHack.LOGGER.info("Invites is empty");
                return;
            }

            updateUserInfo();
        } catch (Exception e) {
            DrugHack.LOGGER.info("Error in resetHwid: {}", e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    public static String getDiskSerial() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return getDiskSerialWin();
        } else if (os.contains("linux")) {
            return getDiskSerialLinux();
        } else if (os.contains("mac")) {
            return getDiskSerialMac();
        }

        return "SWAG";
    }

    private static String getDiskSerialWin() {
        try {
            Process process = Runtime.getRuntime().exec("wmic diskdrive get serialnumber");
            process.waitFor();
            String output = new String(process.getInputStream().readAllBytes(), "CP866");
            String[] lines = output.split("\n");
            if (lines.length >= 2) return lines[1].trim();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return "SWAG";
    }

    private static String getDiskSerialLinux() {
        try {
            Process process = Runtime.getRuntime().exec("hdparm -I /dev/sda | grep 'Serial Number'");
            process.waitFor();
            String output = new String(process.getInputStream().readAllBytes());
            if (!output.isEmpty()) return output.split(":")[1].trim();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return "SWAG";
    }

    private static String getDiskSerialMac() {
        try {
            Process process = Runtime.getRuntime().exec("diskutil info / | grep 'Volume UUID'");
            process.waitFor();
            String output = new String(process.getInputStream().readAllBytes());
            if (!output.isEmpty()) return output.split(":")[1].trim();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return "SWAG";
    }
}
