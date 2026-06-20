package com.ccs.javadroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Built-in HTTP Client (Postman-like).
 * Parses .http files and sends requests via OkHttp.
 * Displays formatted JSON/XML response.
 */
public class HttpApiClientActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "file_path";

    private AppPreferences prefs;
    private AppTheme theme;

    private EditText methodSpinner;
    private EditText urlInput;
    private EditText headersInput;
    private EditText bodyInput;
    private TextView responseStatus;
    private TextView responseHeaders;
    private LinearLayout responseBody;
    private TextView timingText;

    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler ui = new Handler(Looper.getMainLooper());

    public static void launch(Context context, File httpFile) {
        Intent i = new Intent(context, HttpApiClientActivity.class);
        i.putExtra(EXTRA_FILE_PATH, httpFile.getAbsolutePath());
        context.startActivity(i);
    }

    public static void launch(Context context) {
        Intent i = new Intent(context, HttpApiClientActivity.class);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new AppPreferences(this);
        theme = AppTheme.byId(prefs.getThemeId(), prefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        try {
            setContentView(buildRoot());
        } catch (Exception e) {
            android.util.Log.e("HTTP", "buildRoot failed", e);
            LinearLayout fallback = new LinearLayout(this);
            fallback.setOrientation(LinearLayout.VERTICAL);
            fallback.setBackgroundColor(0xFFFFFFFF);
            TextView errTv = new TextView(this);
            errTv.setText("Error: " + e.getMessage());
            errTv.setTextColor(0xFFFF0000);
            errTv.setPadding(16, 16, 16, 16);
            fallback.addView(errTv);
            setContentView(fallback);
            return;
        }
        FullScreenHelper.enable(this);

        String path = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (path != null) {
            loadHttpFile(new File(path));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme.bg);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(theme.toolbar);
        toolbar.setTitle("HTTP Client");
        toolbar.setTitleTextColor(theme.text);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());
        root.addView(toolbar);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(8), dp(12), dp(8));

        // Method + URL row
        LinearLayout urlRow = new LinearLayout(this);
        urlRow.setOrientation(LinearLayout.HORIZONTAL);

        methodSpinner = new EditText(this);
        methodSpinner.setText("GET");
        methodSpinner.setTextColor(theme.successText);
        methodSpinner.setTextSize(13);
        methodSpinner.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        methodSpinner.setBackgroundColor(theme.consoleBg);
        methodSpinner.setPadding(dp(8), dp(8), dp(8), dp(8));
        methodSpinner.setSingleLine(true);
        methodSpinner.setSelectAllOnFocus(true);
        LinearLayout.LayoutParams methodLp = new LinearLayout.LayoutParams(dp(90), ViewGroup.LayoutParams.WRAP_CONTENT);
        methodSpinner.setLayoutParams(methodLp);
        urlRow.addView(methodSpinner);

        urlInput = new EditText(this);
        urlInput.setHint("https://api.example.com/endpoint");
        urlInput.setHintTextColor(theme.textDim);
        urlInput.setTextColor(theme.text);
        urlInput.setTextSize(12);
        urlInput.setTypeface(Typeface.MONOSPACE);
        urlInput.setBackgroundColor(theme.consoleBg);
        urlInput.setPadding(dp(8), dp(8), dp(8), dp(8));
        urlInput.setSingleLine(true);
        urlInput.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        urlRow.addView(urlInput);

        TextView sendBtn = createBtn("▶ Send", theme.successText);
        sendBtn.setOnClickListener(v -> sendRequest());
        urlRow.addView(sendBtn);

        content.addView(urlRow);

        // Headers
        TextView headersLabel = createLabel("Headers (Key: Value per line)");
        content.addView(headersLabel);

        headersInput = new EditText(this);
        headersInput.setHint("Content-Type: application/json\nAccept: */*");
        headersInput.setHintTextColor(theme.textDim);
        headersInput.setTextColor(theme.text);
        headersInput.setTextSize(11);
        headersInput.setTypeface(Typeface.MONOSPACE);
        headersInput.setBackgroundColor(theme.consoleBg);
        headersInput.setPadding(dp(8), dp(6), dp(8), dp(6));
        headersInput.setMinLines(2);
        headersInput.setMaxLines(4);
        content.addView(headersInput);

        // Body
        TextView bodyLabel = createLabel("Body (POST/PUT)");
        content.addView(bodyLabel);

        bodyInput = new EditText(this);
        bodyInput.setHint("{ \"key\": \"value\" }");
        bodyInput.setHintTextColor(theme.textDim);
        bodyInput.setTextColor(theme.text);
        bodyInput.setTextSize(11);
        bodyInput.setTypeface(Typeface.MONOSPACE);
        bodyInput.setBackgroundColor(theme.consoleBg);
        bodyInput.setPadding(dp(8), dp(6), dp(8), dp(6));
        bodyInput.setMinLines(3);
        bodyInput.setMaxLines(8);
        content.addView(bodyInput);

        // Response section
        View divider = new View(this);
        divider.setBackgroundColor(theme.separator);
        content.addView(divider, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // Response status + timing
        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setPadding(0, dp(8), 0, dp(4));

        responseStatus = new TextView(this);
        responseStatus.setTextColor(theme.textDim);
        responseStatus.setTextSize(12);
        responseStatus.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        statusRow.addView(responseStatus);

        timingText = new TextView(this);
        timingText.setTextColor(theme.textDim);
        timingText.setTextSize(11);
        timingText.setTypeface(Typeface.MONOSPACE);
        timingText.setPadding(dp(12), 0, 0, 0);
        statusRow.addView(timingText);

        content.addView(statusRow);

        // Response headers
        responseHeaders = new TextView(this);
        responseHeaders.setTextColor(theme.textDim);
        responseHeaders.setTextSize(10);
        responseHeaders.setTypeface(Typeface.MONOSPACE);
        responseHeaders.setPadding(0, dp(2), 0, dp(6));
        content.addView(responseHeaders);

        // Response body
        responseBody = new LinearLayout(this);
        responseBody.setOrientation(LinearLayout.VERTICAL);
        responseBody.setBackgroundColor(theme.consoleBg);
        responseBody.setPadding(dp(8), dp(8), dp(8), dp(8));
        content.addView(responseBody, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        scroll.addView(content);

        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        return root;
    }

    // ══════════════════════════════════════════════════════════
    //  .http file parser
    // ══════════════════════════════════════════════════════════

    private void loadHttpFile(File file) {
        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            HttpFile parsed = parseHttpFile(content);

            methodSpinner.setText(parsed.method);
            urlInput.setText(parsed.url);

            StringBuilder headers = new StringBuilder();
            for (Map.Entry<String, String> e : parsed.headers.entrySet()) {
                headers.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
            headersInput.setText(headers.toString().trim());

            bodyInput.setText(parsed.body != null ? parsed.body : "");
            bodyInput.setEnabled(!"GET".equalsIgnoreCase(parsed.method)
                    && !"HEAD".equalsIgnoreCase(parsed.method));

            Toast.makeText(this, "Loaded: " + file.getName(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading .http file: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private static class HttpFile {
        String method = "GET";
        String url = "";
        java.util.LinkedHashMap<String, String> headers = new java.util.LinkedHashMap<>();
        String body = null;
    }

    /**
     * Parse IntelliJ .http file format:
     * <pre>
     * ### comment
     * GET https://api.example.com/users
     * Content-Type: application/json
     *
     * { "name": "John" }
     * </pre>
     */
    private HttpFile parseHttpFile(String content) {
        HttpFile result = new HttpFile();
        String[] lines = content.split("\n");

        int state = 0; // 0=method, 1=headers, 2=body
        StringBuilder bodyBuilder = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();

            // Skip comments and separators
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("###")) {
                if (state == 1) state = 2; // empty line after headers = body starts
                continue;
            }

            if (state == 0) {
                // Parse "METHOD URL" line
                String[] parts = line.split("\\s+", 2);
                if (parts.length == 2) {
                    result.method = parts[0].toUpperCase(Locale.ROOT);
                    result.url = parts[1];
                    state = 1;
                }
            } else if (state == 1) {
                // Parse header "Key: Value"
                int colon = line.indexOf(':');
                if (colon > 0) {
                    String key = line.substring(0, colon).trim();
                    String value = line.substring(colon + 1).trim();
                    result.headers.put(key, value);
                } else {
                    // Not a header — might be body without blank line
                    state = 2;
                    bodyBuilder.append(line).append("\n");
                }
            } else {
                bodyBuilder.append(line).append("\n");
            }
        }

        if (bodyBuilder.length() > 0) {
            result.body = bodyBuilder.toString().trim();
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════
    //  HTTP execution
    // ══════════════════════════════════════════════════════════

    private void sendRequest() {
        String method = methodSpinner.getText().toString().trim().toUpperCase(Locale.ROOT);
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Enter a URL", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
            urlInput.setText(url);
        }

        // Build request
        Request.Builder rb = new Request.Builder().url(url);

        // Headers
        String headersText = headersInput.getText().toString().trim();
        if (!headersText.isEmpty()) {
            for (String line : headersText.split("\n")) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    rb.addHeader(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
                }
            }
        }

        // Body
        String bodyText = bodyInput.getText().toString().trim();
        if (!bodyText.isEmpty() && !method.equals("GET") && !method.equals("HEAD")) {
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            rb.method(method, RequestBody.create(bodyText, mediaType));
        } else {
            rb.method(method, null);
        }

        // UI: show loading
        responseBody.removeAllViews();
        responseStatus.setText("Sending...");
        responseStatus.setTextColor(theme.textDim);
        responseHeaders.setText("");
        timingText.setText("");

        long startTime = System.currentTimeMillis();

        client.newCall(rb.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ui.post(() -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    responseStatus.setText("FAILED");
                    responseStatus.setTextColor(theme.errorText);
                    timingText.setText(elapsed + " ms");
                    responseBody.removeAllViews();
                    addResponseBody("Error: " + e.getMessage(), theme.errorText);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                long elapsed = System.currentTimeMillis() - startTime;
                int code = response.code();
                String body = response.body() != null ? response.body().string() : "";
                Headers respHeaders = response.headers();

                ui.post(() -> {
                    // Status
                    String statusText = code + " " + response.message();
                    responseStatus.setText(statusText);
                    responseStatus.setTextColor(code >= 200 && code < 300
                            ? theme.successText : theme.errorText);
                    timingText.setText(body.length() / 1024 + " KB — " + elapsed + " ms");

                    // Headers
                    StringBuilder hb = new StringBuilder();
                    for (int i = 0; i < respHeaders.size(); i++) {
                        hb.append(respHeaders.name(i)).append(": ").append(respHeaders.value(i));
                        if (i < respHeaders.size() - 1) hb.append("\n");
                    }
                    responseHeaders.setText(hb.toString());

                    // Body
                    responseBody.removeAllViews();
                    renderResponseBody(body);
                });
            }
        });
    }

    // ══════════════════════════════════════════════════════════
    //  Response rendering
    // ══════════════════════════════════════════════════════════

    private void renderResponseBody(String body) {
        if (body.isEmpty()) {
            addResponseBody("(empty response)", theme.textDim);
            return;
        }

        // Try JSON — get pre-highlighted SpannableStringBuilder
        if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
            SpannableStringBuilder formatted = JsonXmlFormatter.formatJson(body);
            TextView tv = new TextView(this);
            tv.setText(formatted);
            tv.setTextColor(theme.consoleText);
            tv.setTextSize(11);
            tv.setTypeface(Typeface.MONOSPACE);
            tv.setPadding(0, 0, 0, dp(4));
            responseBody.addView(tv);
            return;
        }

        // Try XML
        if (body.trim().startsWith("<")) {
            SpannableStringBuilder formatted = JsonXmlFormatter.formatXml(body);
            TextView tv = new TextView(this);
            tv.setText(formatted);
            tv.setTextColor(theme.consoleText);
            tv.setTextSize(11);
            tv.setTypeface(Typeface.MONOSPACE);
            tv.setPadding(0, 0, 0, dp(4));
            responseBody.addView(tv);
            return;
        }

        // Plain text
        addResponseBody(body, theme.consoleText);
    }

    private void addResponseBody(String text, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(11);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(0, 0, 0, dp(4));
        responseBody.addView(tv);
    }

    // ══════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════

    private TextView createLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(theme.textDim);
        label.setTextSize(11);
        label.setPadding(0, dp(8), 0, dp(2));
        return label;
    }

    private TextView createBtn(String text, int color) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(color);
        btn.setTextSize(13);
        btn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        btn.setPadding(dp(12), dp(8), dp(12), dp(8));
        btn.setBackgroundResource(android.R.drawable.list_selector_background);
        btn.setGravity(Gravity.CENTER);
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) btn.getLayoutParams();
        lp.setMarginStart(dp(6));
        return btn;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
