package com.ccs.javadroid.ui;

import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppPreferences;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FullScreenHelper;
import com.ccs.javadroid.util.MarkdownRenderer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Full-screen markdown preview with scrollable rendered content.
 */
public class MarkdownPreviewActivity extends AppCompatActivity {

    private static final String EXTRA_CONTENT = "content";
    private static final String EXTRA_TITLE = "title";

    private AppTheme theme;

    public static void launch(Context context, String title, String content) {
        Intent i = new Intent(context, MarkdownPreviewActivity.class);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_CONTENT, content);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppPreferences prefs = new AppPreferences(this);
        theme = AppTheme.byId(prefs.getThemeId(), prefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        setContentView(buildRoot());
        FullScreenHelper.enable(this);

        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        if (content == null || content.trim().isEmpty()) {
            Toast.makeText(this, R.string.file_is_empty, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        boolean dark = theme != null && theme.dark;
        TextView preview = new TextView(this);
        preview.setText(MarkdownRenderer.render(content, dark, 16, prefs.resolveTypeface()));
        preview.setTextSize(16);
        preview.setPadding(dp(24), dp(16), dp(24), dp(32));
        preview.setLineSpacing(0, 1.4f);
        preview.setBackgroundColor(dark ? 0xFF1E1E20 : 0xFFFFFFFF);
        preview.setTextColor(dark ? 0xFFDFE1E5 : 0xFF3C3F41);
        preview.setTextIsSelectable(true);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        scroll.setBackgroundColor(dark ? 0xFF1E1E20 : 0xFFFFFFFF);
        scroll.addView(preview);

        LinearLayout root = (LinearLayout) findViewById(R.id.md_root);
        root.addView(scroll);
    }

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setId(R.id.md_root);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme.bg);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(theme.toolbar);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        toolbar.setTitle(title != null ? title : getString(R.string.menu_md_preview));
        toolbar.setTitleTextColor(theme.text);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        root.addView(toolbar);

        return root;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
