package com.ccs.javadroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Сервіс для роботи з Google Gemini API (AI Studio).
 * Підтримує gemini-2.5-flash, gemini-2.5-flash-lite, gemini-2.0-flash.
 */
public final class GeminiService {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String PREFS_NAME = "gemini_settings";
    private static final String KEY_API_KEY = "gemini_api_key";
    private static final String KEY_MODEL = "gemini_model";

    public static final String MODEL_35_FLASH = "gemini-3.5-flash";
    public static final String MODEL_31_FLASH_LITE = "gemini-3.1-flash-lite";
    public static final String MODEL_25_FLASH = "gemini-2.5-flash";

    public static final String[] AVAILABLE_MODELS = {
            MODEL_35_FLASH,
            MODEL_31_FLASH_LITE,
            MODEL_25_FLASH
    };

    public static final String[] MODEL_DISPLAY_NAMES = {
            "Gemini 3.5 Flash",
            "Gemini 3.1 Flash Lite",
            "Gemini 2.5 Flash"
    };

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private GeminiService() {}

    // ── API Key Management ─────────────────────────────────────

    public static void setApiKey(Context ctx, String apiKey) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_API_KEY, apiKey).apply();
    }

    public static String getApiKey(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_API_KEY, "");
    }

    public static boolean hasApiKey(Context ctx) {
        String key = getApiKey(ctx);
        return key != null && !key.trim().isEmpty();
    }

    public static void setSelectedModel(Context ctx, String model) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_MODEL, model).apply();
    }

    public static String getSelectedModel(Context ctx) {
        String model = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_MODEL, MODEL_35_FLASH);
        for (String m : AVAILABLE_MODELS) {
            if (m.equals(model)) return model;
        }
        return MODEL_35_FLASH;
    }

    // ── Chat (conversation with history) ───────────────────────

    public interface ResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    /**
     * Надсилає повідомлення з контекстом коду (system prompt + chat history).
     */
    public static void chat(Context ctx, String systemPrompt, String userMessage,
                             java.util.List<ChatMessage> history, ResponseCallback callback) {
        if (!hasApiKey(ctx)) {
            mainHandler.post(() -> callback.onError("No API key set. Go to Settings → AI."));
            return;
        }

        String apiKey = getApiKey(ctx);
        String model = getSelectedModel(ctx);

        executor.execute(() -> {
            try {
                JSONObject body = buildRequestBody(systemPrompt, userMessage, history);
                String urlString = BASE_URL + model + ":generateContent?key=" + apiKey;

                android.util.Log.d("GeminiService", "Request URL: " + BASE_URL + model + ":generateContent");
                android.util.Log.d("GeminiService", "Body: " + body.toString().substring(0, Math.min(500, body.toString().length())));

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);

                byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);
                OutputStream os = conn.getOutputStream();
                os.write(bodyBytes);
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                android.util.Log.d("GeminiService", "Response code: " + responseCode);

                if (responseCode != 200) {
                    String errBody = readStream(conn.getErrorStream());
                    android.util.Log.e("GeminiService", "Error body: " + errBody);
                    String hint = "";
                    if (responseCode == 400) hint = "\nCheck your API key and model name.";
                    else if (responseCode == 403) hint = "\nAPI key may not have access to this model.";
                    else if (responseCode == 404) hint = "\nModel not found. Try gemini-2.0-flash or gemini-1.5-flash.";
                    else if (responseCode == 429) hint = "\nRate limited. Wait a moment and try again.";
                    final String errorMsg = "API " + responseCode + ": " + errBody + hint;
                    mainHandler.post(() -> callback.onError(errorMsg));
                    return;
                }

                String responseBody = readStream(conn.getInputStream());
                android.util.Log.d("GeminiService", "Response length: " + responseBody.length());
                android.util.Log.d("GeminiService", "Response preview: " + responseBody.substring(0, Math.min(300, responseBody.length())));
                String text = parseResponse(responseBody);
                android.util.Log.d("GeminiService", "Parsed text length: " + (text != null ? text.length() : "null"));
                android.util.Log.d("GeminiService", "Parsed text preview: " + (text != null ? text.substring(0, Math.min(200, text.length())) : "null"));
                mainHandler.post(() -> {
                    android.util.Log.d("GeminiService", "Delivering to callback, text=" + (text != null ? text.length() : "null"));
                    callback.onSuccess(text);
                });

            } catch (Exception e) {
                android.util.Log.e("GeminiService", "Exception", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        });
    }

    /**
     * Швидкий одноразовий запит без історії (для scan/explain).
     */
    public static void quickPrompt(Context ctx, String prompt, ResponseCallback callback) {
        chat(ctx, "", prompt, null, callback);
    }

    // ── Request Building ───────────────────────────────────────

    private static JSONObject buildRequestBody(String systemPrompt, String userMessage,
                                                java.util.List<ChatMessage> history) throws JSONException {
        JSONObject body = new JSONObject();

        JSONArray contents = new JSONArray();

        // System instruction (як user/system повідомлення на початку)
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "user");
            sysMsg.put("parts", new JSONArray().put(new JSONObject().put("text", systemPrompt)));
            contents.put(sysMsg);

            JSONObject sysResp = new JSONObject();
            sysResp.put("role", "model");
            sysResp.put("parts", new JSONArray().put(new JSONObject().put("text", "Understood. I will follow these instructions.")));
            contents.put(sysResp);
        }

        // History
        if (history != null) {
            for (ChatMessage msg : history) {
                JSONObject m = new JSONObject();
                m.put("role", msg.isFromUser() ? "user" : "model");
                m.put("parts", new JSONArray().put(new JSONObject().put("text", msg.getText())));
                contents.put(m);
            }
        }

        // Current user message
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("parts", new JSONArray().put(new JSONObject().put("text", userMessage)));
        contents.put(userMsg);

        body.put("contents", contents);

        // Generation config
        JSONObject genConfig = new JSONObject();
        genConfig.put("temperature", 0.7);
        genConfig.put("maxOutputTokens", 8192);
        body.put("generationConfig", genConfig);

        return body;
    }

    private static String parseResponse(String responseBody) throws JSONException {
        JSONObject json = new JSONObject(responseBody);
        JSONArray candidates = json.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            return "No response from AI.";
        }
        JSONObject candidate = candidates.getJSONObject(0);
        JSONObject content = candidate.optJSONObject("content");
        if (content == null) return "Empty response.";

        JSONArray parts = content.optJSONArray("parts");
        if (parts == null || parts.length() == 0) return "No content.";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            String text = parts.getJSONObject(i).optString("text", "");
            if (!text.isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(text);
            }
        }
        return sb.toString();
    }

    private static String readStream(java.io.InputStream is) throws java.io.IOException {
        if (is == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString().trim();
    }

    // ── Chat Message Model ─────────────────────────────────────

    public static class ChatMessage {
        private final String text;
        private final boolean fromUser;

        public ChatMessage(String text, boolean fromUser) {
            this.text = text;
            this.fromUser = fromUser;
        }

        public String getText() { return text; }
        public boolean isFromUser() { return fromUser; }
    }
}
