package com.ccs.javadroid;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiChatActivity extends AppCompatActivity {

    private static final String TAG = "AiChat";
    private static final String EXTRA_CODE = "code";
    private static final String EXTRA_FILE_NAME = "file_name";
    private static final String EXTRA_PROJECT_ROOT = "project_root";

    private LinearLayout messagesContainer;
    private EditText etInput;
    private ScrollView scrollChat;
    private TextView tvModelLabel;
    private String codeContext = "";
    private String fileName = "";

    private int accentColor = 0xFF4A86C8;
    private int bgColor = 0xFF1E1E1E;
    private int toolbarColor = 0xFF3C3F41;
    private int textColor = 0xFFBBBBBB;
    private int dimColor = 0xFF808080;
    private int greenColor = 0xFF499C54;
    private int errorColor = 0xFFFF6B6B;
    private static final int COLOR_BOLD = 0xFFFFFFFF;
    private static final int COLOR_HEADER = 0xFF4A86C8;

    public static void launch(Context context, String code, String fileName, String projectRoot) {
        Intent i = new Intent(context, AiChatActivity.class);
        i.putExtra(EXTRA_CODE, code != null ? code : "");
        i.putExtra(EXTRA_FILE_NAME, fileName != null ? fileName : "");
        i.putExtra(EXTRA_PROJECT_ROOT, projectRoot != null ? projectRoot : "");
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FullScreenHelper.enable(this);
        applyColors();
        Log.d(TAG, "onCreate START");

        codeContext = getIntent().getStringExtra(EXTRA_CODE);
        if (codeContext == null) codeContext = "";
        fileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        if (fileName == null) fileName = "";

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor);

        // ── Toolbar ──
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setBackgroundColor(toolbarColor);
        toolbar.setPadding(dp(8), dp(8), dp(12), dp(8));
        toolbar.setGravity(Gravity.CENTER_VERTICAL);

        TextView btnBack = new TextView(this);
        btnBack.setText("\u2190");
        btnBack.setTextColor(textColor);
        btnBack.setTextSize(18);
        btnBack.setPadding(dp(8), dp(4), dp(8), dp(4));
        btnBack.setOnClickListener(v -> finish());
        toolbar.addView(btnBack);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("AI Assistant");
        tvTitle.setTextColor(textColor);
        tvTitle.setTextSize(16);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tvTitle.setLayoutParams(titleLp);
        toolbar.addView(tvTitle);

        tvModelLabel = new TextView(this);
        tvModelLabel.setText(getModelDisplayName());
        tvModelLabel.setTextColor(dimColor);
        tvModelLabel.setTextSize(11);
        tvModelLabel.setPadding(dp(8), dp(4), dp(8), dp(4));
        tvModelLabel.setOnClickListener(v -> showModelDialog());
        toolbar.addView(tvModelLabel);

        TextView btnApiKey = new TextView(this);
        btnApiKey.setText("Key");
        btnApiKey.setTextColor(accentColor);
        btnApiKey.setTextSize(11);
        btnApiKey.setPadding(dp(8), dp(4), dp(8), dp(4));
        btnApiKey.setOnClickListener(v -> showApiKeyDialog());
        toolbar.addView(btnApiKey);

        root.addView(toolbar);

        // ── Quick actions ──
        LinearLayout quickRow = new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);
        quickRow.setBackgroundColor(toolbarColor);
        quickRow.setPadding(dp(6), dp(4), dp(6), dp(4));

        String[] actions = {"Explain", "Find bugs", "Refactor", "Optimize", "Document", "Test"};
        for (String action : actions) {
            TextView chip = new TextView(this);
            chip.setText(action);
            chip.setTextColor(accentColor);
            chip.setTextSize(11);
            chip.setPadding(dp(10), dp(4), dp(10), dp(4));
            chip.setClickable(true);
            chip.setFocusable(true);
            LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            chipLp.setMarginEnd(dp(4));
            chip.setLayoutParams(chipLp);
            chip.setOnClickListener(v -> performQuickAction(action));
            quickRow.addView(chip);
        }
        root.addView(quickRow);

        // ── Chat area ──
        scrollChat = new ScrollView(this);
        scrollChat.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        scrollChat.setBackgroundColor(bgColor);

        messagesContainer = new LinearLayout(this);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        messagesContainer.setPadding(dp(8), dp(8), dp(8), dp(8));
        messagesContainer.setLayoutParams(new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        scrollChat.addView(messagesContainer);
        root.addView(scrollChat);

        // ── Input ──
        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setBackgroundColor(toolbarColor);
        inputRow.setPadding(dp(8), dp(4), dp(8), dp(4));

        etInput = new EditText(this);
        etInput.setHint("Ask about your code...");
        etInput.setHintTextColor(dimColor);
        etInput.setTextColor(textColor);
        etInput.setBackgroundColor(0xFF3A3F47);
        etInput.setTextSize(14);
        etInput.setMaxLines(5);
        etInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        etInput.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView btnSend = new TextView(this);
        btnSend.setText(" Send ");
        btnSend.setTextColor(accentColor);
        btnSend.setTextSize(14);
        btnSend.setPadding(dp(12), dp(8), dp(12), dp(8));
        btnSend.setGravity(Gravity.CENTER);

        inputRow.addView(etInput);
        inputRow.addView(btnSend);
        root.addView(inputRow);

        setContentView(root);
        Log.d(TAG, "onCreate: setContentView DONE");

        // Welcome
        if (!codeContext.isEmpty()) {
            addText("System", "Loaded " + fileName + " (" + codeContext.length() + " chars). Ask anything about this code!");
        } else {
            addText("System", "AI Assistant ready. Ask me about Java, explain code, find bugs, refactor, optimize, or generate tests.");
        }

        btnSend.setOnClickListener(v -> sendMessage());

        if (!GeminiService.hasApiKey(this)) {
            addText("System", "API key not set. Tap 'Key' to add your Gemini key from aistudio.google.com");
            showApiKeyDialog();
        }

        Log.d(TAG, "onCreate DONE, container children=" + messagesContainer.getChildCount());
    }

    // ── Text ──────────────────────────────────────────────────

    private void addText(String sender, String text) {
        boolean isSystem = "System".equals(sender);
        boolean isAI = "AI".equals(sender);

        // Заголовок
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setPadding(dp(8), dp(6), dp(8), dp(2));

        TextView tvLabel = new TextView(this);
        tvLabel.setText((isSystem ? "--- " : isAI ? ">>> " : "<<< ") + sender);
        tvLabel.setTextColor(isSystem ? dimColor : isAI ? greenColor : accentColor);
        tvLabel.setTextSize(12);
        tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.BOLD);
        headerRow.addView(tvLabel);
        messagesContainer.addView(headerRow);

        // Контент
        if (isAI && text.contains("```")) {
            // AI-відповідь з кодовими блоками — парсимо на сегменти
            parseAndAddMarkdown(text);
        } else {
            // Простий текст
            TextView tvBody = new TextView(this);
            tvBody.setText(text);
            tvBody.setTextColor(textColor);
            tvBody.setTextSize(14);
            tvBody.setPadding(dp(8), dp(2), dp(8), dp(6));
            tvBody.setLineSpacing(0, 1.3f);
            LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            bodyLp.bottomMargin = dp(4);
            tvBody.setLayoutParams(bodyLp);
            messagesContainer.addView(tvBody);
        }

        scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
    }

    private void removeLastLine() {
        int count = messagesContainer.getChildCount();
        if (count >= 2) {
            messagesContainer.removeViewAt(count - 1); // body
            messagesContainer.removeViewAt(count - 2); // header
        }
    }

    // ── Markdown parser з CodeBlockView ───────────────────────

    private static final Pattern P_CODE_BLOCK = Pattern.compile("```(\\w*)\\n(.*?)```", Pattern.DOTALL);
    private static final Pattern P_BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern P_HEADER = Pattern.compile("^#{1,3}\\s+(.+)$", Pattern.MULTILINE);

    private void parseAndAddMarkdown(String text) {
        Matcher m = P_CODE_BLOCK.matcher(text);
        int lastEnd = 0;

        while (m.find()) {
            // Текст перед кодовим блоком
            if (m.start() > lastEnd) {
                String before = text.substring(lastEnd, m.start());
                addFormattedTextView(before);
            }

            // Кодовий блок з обводкою та кнопкою Copy
            String lang = m.group(1);
            String code = m.group(2);
            if (code != null && code.endsWith("\n")) code = code.substring(0, code.length() - 1);

            CodeBlockView codeView = new CodeBlockView(this, code, lang);

            // Підсвітка
            SpannableStringBuilder highlighted = ChatFormatter.format("```" + (lang != null ? lang : "") + "\n" + code + "\n```", textColor);
            codeView.setHighlighted(highlighted);

            LinearLayout.LayoutParams codeLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            codeLp.setMargins(dp(8), dp(4), dp(8), dp(4));
            codeView.setLayoutParams(codeLp);
            messagesContainer.addView(codeView);

            lastEnd = m.end();
        }

        // Текст після останнього кодового блоку
        if (lastEnd < text.length()) {
            addFormattedTextView(text.substring(lastEnd));
        }
    }

    private void addFormattedTextView(String text) {
        if (text.trim().isEmpty()) return;

        // Headers
        Matcher hm = P_HEADER.matcher(text);
        int last = 0;
        while (hm.find()) {
            if (hm.start() > last) addPlainTextView(text.substring(last, hm.start()));
            TextView tv = new TextView(this);
            tv.setText(hm.group(1));
            tv.setTextColor(COLOR_HEADER);
            tv.setTextSize(15);
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            tv.setPadding(dp(8), dp(6), dp(8), dp(2));
            messagesContainer.addView(tv);
            last = hm.end();
        }
        if (last < text.length()) {
            addPlainTextView(text.substring(last));
        }
    }

    private void addPlainTextView(String text) {
        if (text.trim().isEmpty()) return;

        // Bold
        Matcher bm = P_BOLD.matcher(text);
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int last = 0;
        while (bm.find()) {
            if (bm.start() > last) sb.append(text, last, bm.start());
            int s = sb.length();
            sb.append(bm.group(1));
            sb.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), s, sb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new ForegroundColorSpan(COLOR_BOLD), s, sb.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            last = bm.end();
        }
        if (last < text.length()) sb.append(text, last, text.length());

        TextView tv = new TextView(this);
        tv.setText(sb);
        tv.setTextColor(textColor);
        tv.setTextSize(14);
        tv.setPadding(dp(8), dp(2), dp(8), dp(4));
        tv.setLineSpacing(0, 1.3f);
        messagesContainer.addView(tv);
    }

    // ── Send ──────────────────────────────────────────────────

    private void sendMessage() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) return;
        if (!GeminiService.hasApiKey(this)) {
            addText("System", "Set your API key first (tap 'Key').");
            showApiKeyDialog();
            return;
        }

        etInput.setText("");
        addText("You", input);
        addText("AI", "Thinking...");

        StringBuilder prompt = new StringBuilder();
        if (!codeContext.isEmpty()) {
            prompt.append("File: ").append(fileName).append("\n```java\n")
                    .append(codeContext).append("\n```\n\n");
        }
        prompt.append(input);

        GeminiService.chat(this, buildSystemPrompt(), prompt.toString(), null,
                new GeminiService.ResponseCallback() {
                    @Override public void onSuccess(String response) {
                        removeLastLine();
                        addText("AI", response);
                    }
                    @Override public void onError(String error) {
                        removeLastLine();
                        addText("AI", "Error: " + error);
                    }
                });
    }

    // ── Quick actions ─────────────────────────────────────────

    private void performQuickAction(String action) {
        if (codeContext.isEmpty()) {
            addText("System", "Open a file first for code context.");
            return;
        }
        if (!GeminiService.hasApiKey(this)) {
            showApiKeyDialog();
            return;
        }

        String prompt;
        switch (action.toLowerCase(Locale.ROOT)) {
            case "explain":
                prompt = "Explain this code in detail. What does it do? How does it work?";
                break;
            case "find bugs":
                prompt = "Find bugs, potential issues, and code smells. For each issue explain the problem and provide a fix.";
                break;
            case "refactor":
                prompt = "Refactor this code to be cleaner and follow best practices. Show the improved version.";
                break;
            case "optimize":
                prompt = "Optimize this code for better performance. Show the optimized version.";
                break;
            case "document":
                prompt = "Add JavaDoc documentation to this code. Include class, method, parameter, and return docs.";
                break;
            case "test":
                prompt = "Write JUnit tests for this code. Cover edge cases and normal cases.";
                break;
            default:
                prompt = action;
        }

        addText("You", "[" + action + "] " + prompt);
        addText("AI", "Thinking...");

        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append("File: ").append(fileName).append("\n```java\n")
                .append(codeContext).append("\n```\n\n");
        fullPrompt.append(prompt);

        GeminiService.chat(this, buildSystemPrompt(), fullPrompt.toString(), null,
                new GeminiService.ResponseCallback() {
                    @Override public void onSuccess(String response) {
                        removeLastLine();
                        addText("AI", response);
                    }
                    @Override public void onError(String error) {
                        removeLastLine();
                        addText("AI", "Error: " + error);
                    }
                });
    }

    // ── Helpers ───────────────────────────────────────────────

    private String buildSystemPrompt() {
        return "You are an expert Java programming assistant inside JavaDroid Android IDE. "
                + "Help the user with their code. Be concise and practical. "
                + "When showing code, use java code blocks. "
                + "When finding bugs, explain the issue and provide the fix. "
                + "When refactoring, show the improved code. "
                + "Keep responses focused and actionable.";
    }

    private void showModelDialog() {
        String current = GeminiService.getSelectedModel(this);
        int checked = 0;
        for (int i = 0; i < GeminiService.AVAILABLE_MODELS.length; i++) {
            if (GeminiService.AVAILABLE_MODELS[i].equals(current)) { checked = i; break; }
        }
        new AlertDialog.Builder(this)
                .setTitle("Select AI Model")
                .setSingleChoiceItems(GeminiService.MODEL_DISPLAY_NAMES, checked, (d, w) -> {
                    GeminiService.setSelectedModel(this, GeminiService.AVAILABLE_MODELS[w]);
                    tvModelLabel.setText(GeminiService.MODEL_DISPLAY_NAMES[w]);
                    addText("System", "Model changed to: " + GeminiService.MODEL_DISPLAY_NAMES[w]);
                    d.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showApiKeyDialog() {
        EditText input = new EditText(this);
        input.setHint("Paste your API key from aistudio.google.com");
        input.setHintTextColor(dimColor);
        input.setTextColor(textColor);
        input.setPadding(48, 24, 48, 24);
        input.setSingleLine(true);
        String saved = GeminiService.getApiKey(this);
        if (saved != null && !saved.isEmpty()) input.setText(saved);

        new AlertDialog.Builder(this)
                .setTitle("Gemini API Key")
                .setMessage("Get your free key at aistudio.google.com/apikey")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String key = input.getText().toString().trim();
                    if (!key.isEmpty()) {
                        GeminiService.setApiKey(this, key);
                        tvModelLabel.setText(getModelDisplayName());
                        addText("System", "API key saved. Model: " + getModelDisplayName());
                        Toast.makeText(this, "Key saved", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getModelDisplayName() {
        String model = GeminiService.getSelectedModel(this);
        for (int i = 0; i < GeminiService.AVAILABLE_MODELS.length; i++) {
            if (GeminiService.AVAILABLE_MODELS[i].equals(model)) {
                return GeminiService.MODEL_DISPLAY_NAMES[i];
            }
        }
        return model;
    }

    private void applyColors() {
        try {
            AppPreferences prefs = new AppPreferences(this);
            AppTheme theme = AppTheme.byId(prefs.getThemeId(), prefs);
            accentColor = theme.accent;
            bgColor = theme.bg;
            toolbarColor = theme.toolbar;
            textColor = theme.text;
            dimColor = theme.textDim;
            greenColor = theme.successText;
            errorColor = theme.errorText;
        } catch (Throwable ignored) {}
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
