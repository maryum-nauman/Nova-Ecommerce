package com.example.nova_ecommerce.utils;

import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OtpManager {

    private static final String DB_URL        =
            "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com";
    private static final long   OTP_EXPIRY_MS = 5 * 60 * 1000;

    private static final String SERVICE_ID  = "service_enfbpgo";
    private static final String TEMPLATE_ID = "template_4k8jk2l";
    private static final String PUBLIC_KEY  = "WDEoxFZ-96C-2aU-a";

    // ── Generate OTP + save to Firebase ──────────────────────
    public static String generateAndSave(String userId) {
        String otp    = String.format("%06d",
                new Random().nextInt(999999));
        long   expiry = System.currentTimeMillis() + OTP_EXPIRY_MS;

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("code",     otp);
        otpData.put("expiry",   expiry);
        otpData.put("verified", false);

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("otps")
                .child(userId)
                .setValue(otpData);

        return otp;
    }

    // ── Send OTP email via EmailJS ────────────────────────────
    public static void sendOtpEmail(String toEmail,
                                    String userName,
                                    String otp,
                                    OtpEmailCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(
                        "https://api.emailjs.com/api/v1.0/email/send");
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty(
                        "Content-Type", "application/json");
                conn.setRequestProperty(
                        "origin", "http://localhost");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                String safeUserName = sanitize(userName);
                String body = "{"
                        + "\"service_id\":\"" + SERVICE_ID + "\","
                        + "\"template_id\":\"" + TEMPLATE_ID + "\","
                        + "\"user_id\":\"" + PUBLIC_KEY + "\","
                        + "\"template_params\":{"
                        + "\"to_email\":\"" + toEmail + "\","
                        + "\"to_name\":\"" + safeUserName + "\","
                        + "\"passcode\":\"" + otp + "\","
                        + "\"otp_code\":\"" + otp + "\","
                        + "\"time\":\"5 minutes\","
                        + "\"expiry_minutes\":\"5\""
                        + "}}";

                Log.d("EMAILJS", "Body: " + body);

                byte[] input = body.getBytes(
                        java.nio.charset.StandardCharsets.UTF_8);
                conn.getOutputStream().write(input);

                int responseCode = conn.getResponseCode();

                // ── Read response compatible with all API levels
                String response = readStream(
                        responseCode == 200
                                ? conn.getInputStream()
                                : conn.getErrorStream());

                Log.d("EMAILJS",
                        "Response " + responseCode + ": " + response);

                if (responseCode == 200) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(
                            "Code " + responseCode + " — " + response);
                }

            } catch (Exception e) {
                Log.e("EMAILJS", "Exception: " + e.getMessage());
                callback.onFailure(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Unknown error");
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    // ── Read InputStream into String (works all API levels) ───
    private static String readStream(InputStream stream) {
        if (stream == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            Log.e("EMAILJS", "readStream error: " + e.getMessage());
        }
        return sb.toString();
    }

    // ── Prevent JSON injection ────────────────────────────────
    private static String sanitize(String input) {
        if (input == null) return "Customer";
        return input.replace("\"", "'")
                .replace("\\", "")
                .replace("\n", " ")
                .replace("\r", "");
    }

    public interface OtpEmailCallback {
        void onSuccess();
        void onFailure(String error);
    }
}