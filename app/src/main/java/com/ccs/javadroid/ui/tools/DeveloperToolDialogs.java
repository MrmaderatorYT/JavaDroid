package com.ccs.javadroid.ui.tools;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccs.javadroid.R;
import com.ccs.javadroid.ui.Dialogs;
import com.ccs.javadroid.util.AppTheme;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Standalone helper dialogs: a regex tester and a Base64/URL converter.
 *
 * <p>They were inline in {@code MainActivity} — a hundred and sixty lines that
 * touched none of the editor's state and existed only because there was
 * nowhere else to put them. Both now build their own views and read their text
 * from resources instead of the English literals they carried before.</p>
 */
public final class DeveloperToolDialogs {

    private DeveloperToolDialogs() {}

    /**
     * Live regex tester: shows every match as the pattern or subject is typed,
     * and reports a syntax error rather than swallowing it.
     */
    public static void showRegexTester(Activity activity, AppTheme theme) {
        LinearLayout layout = column(activity);

        EditText pattern = field(activity, theme, activity.getString(R.string.tool_regex_hint), 1);
        EditText subject = field(activity, theme, activity.getString(R.string.tool_regex_text_hint), 3);
        TextView result = output(activity, theme);

        layout.addView(pattern);
        layout.addView(subject);
        layout.addView(result);

        TextWatcher watcher = new SimpleTextWatcher(() ->
                result.setText(describeMatches(activity, pattern.getText().toString(),
                        subject.getText().toString())));
        pattern.addTextChangedListener(watcher);
        subject.addTextChangedListener(watcher);

        Dialogs.rounded(activity)
                .setTitle(R.string.tool_regex_title)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /** Runs the pattern and formats the outcome, including the failure cases. */
    private static CharSequence describeMatches(Activity activity, String regex, String text) {
        if (regex.isEmpty()) return "";
        try {
            Matcher matcher = Pattern.compile(regex).matcher(text);
            StringBuilder out = new StringBuilder();
            int count = 0;
            while (matcher.find()) {
                count++;
                out.append(activity.getString(R.string.tool_regex_match, count, matcher.group()))
                        .append('\n');
                // A pattern that can match empty advances no further on its own.
                if (matcher.end() == matcher.start() && matcher.end() >= text.length()) break;
                if (count > 500) {
                    out.append(activity.getString(R.string.tool_regex_truncated));
                    break;
                }
            }
            return count == 0 ? activity.getString(R.string.tool_regex_no_match) : out.toString();
        } catch (PatternSyntaxException e) {
            return activity.getString(R.string.tool_regex_invalid, e.getDescription());
        } catch (Exception e) {
            return activity.getString(R.string.tool_regex_invalid, String.valueOf(e.getMessage()));
        }
    }

    /** Base64 and percent-encoding, in both directions. */
    public static void showEncoder(Activity activity, AppTheme theme) {
        LinearLayout layout = column(activity);

        EditText input = field(activity, theme, activity.getString(R.string.tool_encoder_input), 3);
        TextView output = output(activity, theme);
        output.setTextIsSelectable(true);

        layout.addView(input);
        layout.addView(buttonRow(activity,
                button(activity, activity.getString(R.string.tool_encoder_encode), () ->
                        output.setText(run(activity, () -> Base64.encodeToString(
                                input.getText().toString().getBytes(StandardCharsets.UTF_8),
                                Base64.NO_WRAP)))),
                button(activity, activity.getString(R.string.tool_encoder_decode), () ->
                        output.setText(run(activity, () -> new String(
                                Base64.decode(input.getText().toString(), Base64.DEFAULT),
                                StandardCharsets.UTF_8))))));
        layout.addView(buttonRow(activity,
                button(activity, activity.getString(R.string.tool_encoder_url_encode), () ->
                        output.setText(run(activity, () ->
                                URLEncoder.encode(input.getText().toString(), "UTF-8")))),
                button(activity, activity.getString(R.string.tool_encoder_url_decode), () ->
                        output.setText(run(activity, () ->
                                URLDecoder.decode(input.getText().toString(), "UTF-8"))))));
        layout.addView(output);

        Dialogs.rounded(activity)
                .setTitle(R.string.tool_encoder_title)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /** A conversion that may throw; failures become a readable line, not a crash. */
    private interface Conversion {
        String apply() throws UnsupportedEncodingException, IllegalArgumentException;
    }

    private static String run(Activity activity, Conversion conversion) {
        try {
            return conversion.apply();
        } catch (IllegalArgumentException e) {
            return activity.getString(R.string.tool_encoder_invalid, String.valueOf(e.getMessage()));
        } catch (Exception e) {
            return activity.getString(R.string.tool_encoder_failed, String.valueOf(e.getMessage()));
        }
    }

    // ── view helpers ─────────────────────────────────────────────────────────

    private static LinearLayout column(Activity activity) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(activity, 16);
        layout.setPadding(pad, pad, pad, pad);
        return layout;
    }

    private static EditText field(Activity activity, AppTheme theme, String hint, int minLines) {
        EditText field = new EditText(activity);
        field.setHint(hint);
        field.setTextColor(theme.text);
        field.setHintTextColor(theme.textDim);
        if (minLines > 1) {
            field.setMinLines(minLines);
            field.setGravity(Gravity.TOP | Gravity.START);
        }
        return field;
    }

    private static TextView output(Activity activity, AppTheme theme) {
        TextView view = new TextView(activity);
        view.setTextColor(theme.accent);
        view.setPadding(0, dp(activity, 8), 0, dp(activity, 8));
        return view;
    }

    private static Button button(Activity activity, String text, Runnable action) {
        Button button = new Button(activity);
        button.setText(text);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private static LinearLayout buttonRow(Activity activity, Button... buttons) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (Button button : buttons) {
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(button);
        }
        return row;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    /** {@link TextWatcher} boilerplate reduced to the one callback we use. */
    private static final class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
        @Override public void onTextChanged(CharSequence s, int a, int b, int c) { onChange.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
