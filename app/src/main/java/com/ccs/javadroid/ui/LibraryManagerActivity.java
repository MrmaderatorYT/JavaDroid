package com.ccs.javadroid.ui;
import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppPreferences;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FullScreenHelper;
import com.ccs.javadroid.maven.MavenPaths;
import com.ccs.javadroid.maven.PomWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LibraryManagerActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_PATH = "project_path";

    private AppPreferences prefs;
    private AppTheme theme;
    private File projectRoot;

    private EditText etSearch;
    private TextView btnSearch;
    private TextView tvStatus;
    private RecyclerView rvResults;
    private LinearLayout placeholderLayout;
    private ResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new AppPreferences(this);
        theme = AppTheme.byId(prefs.getThemeId(), prefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);

        String path = getIntent().getStringExtra(EXTRA_PROJECT_PATH);
        if (path != null) {
            projectRoot = new File(path);
        }

        setContentView(buildRootLayout());
        FullScreenHelper.enable(this);        showEmptyState(false);
    }

    private View buildRootLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme.bg);

        // Toolbar
        Toolbar toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(theme.toolbar);
        toolbar.setTitle(getString(R.string.lib_title));
        toolbar.setTitleTextColor(theme.text);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        Drawable nav = toolbar.getNavigationIcon();
        if (nav != null) nav.setColorFilter(theme.text, PorterDuff.Mode.SRC_IN);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        root.addView(toolbar);

        // Search Bar container
        LinearLayout searchBar = new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(dp(16), dp(16), dp(16), dp(8));
        searchBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Search EditText
        etSearch = new EditText(this);
        etSearch.setHint(getString(R.string.lib_search_hint));
        etSearch.setHintTextColor(theme.textDim);
        etSearch.setTextColor(theme.text);
        etSearch.setInputType(InputType.TYPE_CLASS_TEXT);
        etSearch.setSingleLine(true);
        etSearch.setTextSize(14);
        etSearch.setPadding(dp(12), dp(10), dp(12), dp(10));
        
        try {
            Drawable searchIcon = getResources().getDrawable(R.drawable.ic_search).mutate();
            searchIcon.setColorFilter(theme.textDim, PorterDuff.Mode.SRC_IN);
            etSearch.setCompoundDrawablesWithIntrinsicBounds(searchIcon, null, null, null);
            etSearch.setCompoundDrawablePadding(dp(8));
        } catch (Exception ignored) {}

        GradientDrawable editBg = new GradientDrawable();
        editBg.setColor(blend(theme.toolbar, theme.bg, 0.4f));
        editBg.setCornerRadius(dp(20)); // rounded pill style input
        editBg.setStroke(dp(1), theme.separator);
        etSearch.setBackground(editBg);

        LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        editLp.rightMargin = dp(10);
        etSearch.setLayoutParams(editLp);
        searchBar.addView(etSearch);

        // Search Button
        btnSearch = new TextView(this);
        btnSearch.setText(getString(R.string.lib_search));
        btnSearch.setTextColor(theme.text);
        btnSearch.setTextSize(14);
        btnSearch.setTypeface(Typeface.DEFAULT_BOLD);
        btnSearch.setGravity(Gravity.CENTER);
        btnSearch.setPadding(dp(20), dp(10), dp(20), dp(10));
        
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(theme.accent);
        btnBg.setCornerRadius(dp(20)); // pill button
        btnSearch.setBackground(btnBg);
        
        btnSearch.setClickable(true);
        btnSearch.setFocusable(true);
        btnSearch.setOnClickListener(v -> performSearch());
        searchBar.addView(btnSearch);

        root.addView(searchBar);

        // Status Label (e.g. Loading)
        tvStatus = new TextView(this);
        tvStatus.setTextColor(theme.textDim);
        tvStatus.setTextSize(13);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(dp(16), dp(8), dp(16), dp(8));
        tvStatus.setVisibility(View.GONE);
        root.addView(tvStatus);

        // Content Area (RecyclerView or Placeholder)
        FrameLayout contentArea = new FrameLayout(this);
        contentArea.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // RecyclerView for results
        rvResults = new RecyclerView(this);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setPadding(0, dp(4), 0, dp(12));
        rvResults.setClipToPadding(false);
        rvResults.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        adapter = new ResultsAdapter();
        rvResults.setAdapter(adapter);
        contentArea.addView(rvResults);

        // Placeholder Empty State Layout
        contentArea.addView(buildPlaceholderLayout());

        root.addView(contentArea);

        return root;
    }

    private View buildPlaceholderLayout() {
        placeholderLayout = new LinearLayout(this);
        placeholderLayout.setOrientation(LinearLayout.VERTICAL);
        placeholderLayout.setGravity(Gravity.CENTER);
        placeholderLayout.setPadding(dp(32), dp(48), dp(32), dp(48));
        placeholderLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView iv = new ImageView(this);
        try {
            Drawable d = getResources().getDrawable(R.drawable.ic_search).mutate();
            d.setColorFilter(blend(theme.textDim, theme.bg, 0.6f), PorterDuff.Mode.SRC_IN);
            iv.setImageDrawable(d);
        } catch (Exception ignored) {}
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(72), dp(72));
        ivLp.bottomMargin = dp(16);
        iv.setLayoutParams(ivLp);
        placeholderLayout.addView(iv);

        TextView title = new TextView(this);
        title.setText(getString(R.string.lib_title));
        title.setTextColor(theme.text);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        placeholderLayout.addView(title);

        TextView sub = new TextView(this);
        sub.setText(getString(R.string.lib_search_hint));
        sub.setTextColor(theme.textDim);
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(8);
        sub.setLayoutParams(subLp);
        placeholderLayout.addView(sub);

        return placeholderLayout;
    }

    private void showEmptyState(boolean noResults) {
        rvResults.setVisibility(View.GONE);
        placeholderLayout.setVisibility(View.VISIBLE);
        
        if (placeholderLayout.getChildCount() >= 3) {
            TextView title = (TextView) placeholderLayout.getChildAt(1);
            TextView sub = (TextView) placeholderLayout.getChildAt(2);
            if (noResults) {
                title.setText(getString(R.string.lib_no_results));
                sub.setText(getString(R.string.lib_search_hint));
            } else {
                title.setText(getString(R.string.lib_title));
                sub.setText(getString(R.string.lib_search_hint));
            }
        }
    }

    private void showResultsState() {
        placeholderLayout.setVisibility(View.GONE);
        rvResults.setVisibility(View.VISIBLE);
    }

    private void performSearch() {
        final String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) return;

        btnSearch.setEnabled(false);
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(getString(R.string.lib_searching));
        adapter.clear();

        new Thread(() -> {
            try {
                String encoded = URLEncoder.encode(query, "UTF-8");
                String urlStr = "https://search.maven.org/solrsearch/select?q=" + encoded + "&rows=30&wt=json";
                
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("GET");
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream in = conn.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    String json = out.toString("UTF-8");
                    conn.disconnect();

                    JSONObject rootObj = new JSONObject(json);
                    JSONObject responseObj = rootObj.optJSONObject("response");
                    final List<LibItem> items = new ArrayList<>();
                    if (responseObj != null) {
                        JSONArray docs = responseObj.optJSONArray("docs");
                        if (docs != null) {
                            for (int i = 0; i < docs.length(); i++) {
                                JSONObject doc = docs.getJSONObject(i);
                                LibItem item = new LibItem();
                                item.groupId = doc.optString("g");
                                item.artifactId = doc.optString("a");
                                item.version = doc.optString("latestVersion");
                                items.add(item);
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        btnSearch.setEnabled(true);
                        tvStatus.setVisibility(View.GONE);
                        if (items.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showResultsState();
                            adapter.setItems(items);
                        }
                    });

                } else {
                    conn.disconnect();
                    throw new IOException("HTTP code " + responseCode);
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSearch.setEnabled(true);
                    tvStatus.setText(e.getMessage());
                });
            }
        }).start();
    }

    private void addLibrary(LibItem item) {
        if (projectRoot == null) return;
        File pomFile = MavenPaths.pomFile(projectRoot);
        if (!pomFile.exists()) {
            Toast.makeText(this, R.string.toast_not_maven, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog loadingDlg = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.lib_loading_versions))
                .setCancelable(false)
                .show();

        new Thread(() -> {
            try {
                String qStr = "g:\"" + item.groupId + "\" AND a:\"" + item.artifactId + "\"";
                String encoded = URLEncoder.encode(qStr, "UTF-8");
                String urlStr = "https://search.maven.org/solrsearch/select?q=" + encoded + "&core=gav&rows=100&wt=json";

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    InputStream in = conn.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    String json = out.toString("UTF-8");
                    conn.disconnect();

                    JSONObject rootObj = new JSONObject(json);
                    JSONObject responseObj = rootObj.optJSONObject("response");
                    final List<String> versions = new ArrayList<>();
                    if (responseObj != null) {
                        JSONArray docs = responseObj.optJSONArray("docs");
                        if (docs != null) {
                            for (int i = 0; i < docs.length(); i++) {
                                JSONObject doc = docs.getJSONObject(i);
                                String v = doc.optString("v");
                                if (!v.isEmpty() && !versions.contains(v)) {
                                    versions.add(v);
                                }
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        loadingDlg.dismiss();
                        if (versions.isEmpty()) {
                            versions.add(item.version);
                        }
                        showVersionPickerDialog(item, versions, pomFile);
                    });

                } else {
                    conn.disconnect();
                    throw new IOException("HTTP code " + responseCode);
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingDlg.dismiss();
                    List<String> versions = new ArrayList<>();
                    versions.add(item.version);
                    showVersionPickerDialog(item, versions, pomFile);
                });
            }
        }).start();
    }

    private void showVersionPickerDialog(LibItem item, List<String> versions, File pomFile) {
        final String[] versionArray = versions.toArray(new String[0]);
        final int[] selected = {0};

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.lib_select_version))
                .setSingleChoiceItems(versionArray, 0, (dialog, which) -> selected[0] = which)
                .setPositiveButton(R.string.dialog_apply, (dialog, which) -> {
                    String chosenVersion = versionArray[selected[0]];
                    performAddDependency(item.groupId, item.artifactId, chosenVersion, pomFile);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void performAddDependency(String groupId, String artifactId, String version, File pomFile) {
        try {
            byte[] bytes = Files.readAllBytes(pomFile.toPath());
            String content = new String(bytes, StandardCharsets.UTF_8);
            String updated = PomWriter.addDependency(content, groupId, artifactId, version);
            Files.write(pomFile.toPath(), updated.getBytes(StandardCharsets.UTF_8));
            
            Toast.makeText(this, getString(R.string.lib_added, artifactId), Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    public static void launch(Activity host, File projectPath, int requestCode) {
        Intent intent = new Intent(host, LibraryManagerActivity.class);
        intent.putExtra(EXTRA_PROJECT_PATH, projectPath.getAbsolutePath());
        host.startActivityForResult(intent, requestCode);
    }

    // ── FrameLayout custom subclass helper for matching package layout ────
    private static class FrameLayout extends android.widget.FrameLayout {
        public FrameLayout(Context context) { super(context); }
    }

    // ── Adapter & ViewHolders ─────────────────────────────────

    private static class LibItem {
        String groupId;
        String artifactId;
        String version;
    }

    private class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
        private final List<LibItem> items = new ArrayList<>();

        public void setItems(List<LibItem> items) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
        }

        public void clear() {
            this.items.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout card = new LinearLayout(parent.getContext());
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(12), dp(12), dp(12), dp(12));
            
            RecyclerView.LayoutParams cardLp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(dp(16), dp(6), dp(16), dp(6));
            card.setLayoutParams(cardLp);

            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(blend(theme.toolbar, theme.bg, 0.5f));
            cardBg.setCornerRadius(dp(8));
            card.setBackground(cardBg);

            TypedValue tv = new TypedValue();
            parent.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
            if (tv.resourceId != 0) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    card.setForeground(parent.getContext().getResources().getDrawable(tv.resourceId, parent.getContext().getTheme()));
                }
            }

            // Left: Avatar with first letter of artifactId
            TextView avatar = new TextView(parent.getContext());
            avatar.setGravity(Gravity.CENTER);
            avatar.setTextSize(15);
            avatar.setTypeface(Typeface.DEFAULT_BOLD);
            avatar.setTextColor(Color.WHITE);
            
            GradientDrawable avatarBg = new GradientDrawable();
            avatarBg.setShape(GradientDrawable.OVAL);
            avatarBg.setColor(theme.accent);
            avatar.setBackground(avatarBg);

            LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(dp(36), dp(36));
            avatarLp.rightMargin = dp(12);
            avatar.setLayoutParams(avatarLp);
            card.addView(avatar);

            // Center: Info texts (Artifact ID, Group ID)
            LinearLayout info = new LinearLayout(parent.getContext());
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            info.setLayoutParams(infoLp);

            TextView name = new TextView(parent.getContext());
            name.setTextSize(14);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setTextColor(theme.text);
            info.addView(name);

            TextView group = new TextView(parent.getContext());
            group.setTextSize(11);
            group.setTextColor(theme.textDim);
            LinearLayout.LayoutParams groupLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            groupLp.topMargin = dp(2);
            group.setLayoutParams(groupLp);
            info.addView(group);

            card.addView(info);

            // Right: Version badge pill
            TextView badge = new TextView(parent.getContext());
            badge.setPadding(dp(8), dp(4), dp(8), dp(4));
            badge.setTextSize(11);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setTextColor(theme.successText);
            
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(blend(theme.successText, theme.bg, 0.85f));
            badgeBg.setCornerRadius(dp(12));
            badge.setBackground(badgeBg);

            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            badgeLp.leftMargin = dp(8);
            badge.setLayoutParams(badgeLp);
            card.addView(badge);

            return new ViewHolder(card, avatar, name, group, badge);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LibItem item = items.get(position);
            holder.tvName.setText(item.artifactId);
            holder.tvGroup.setText(item.groupId);
            
            String firstLetter = item.artifactId != null && !item.artifactId.isEmpty() 
                    ? item.artifactId.substring(0, 1).toUpperCase() 
                    : "L";
            holder.tvAvatar.setText(firstLetter);
            
            // Set random/different colors for avatar backgrounds based on artifact ID hash to look amazing!
            int hash = item.artifactId != null ? Math.abs(item.artifactId.hashCode()) : 0;
            int color;
            switch (hash % 5) {
                case 0: color = theme.accent; break;
                case 1: color = theme.successText; break;
                case 2: color = 0xFFE6A23C; break; // Yellow/orange
                case 3: color = 0xFFE74C3C; break; // Red/orange
                case 4: 
                default: color = 0xFF9B59B6; break; // Purple
            }
            GradientDrawable avatarBg = (GradientDrawable) holder.tvAvatar.getBackground();
            avatarBg.setColor(color);
            
            holder.tvBadge.setText(item.version);
            holder.itemView.setOnClickListener(v -> addLibrary(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView tvAvatar;
            final TextView tvName;
            final TextView tvGroup;
            final TextView tvBadge;

            ViewHolder(View itemView, TextView avatar, TextView name, TextView group, TextView badge) {
                super(itemView);
                this.tvAvatar = avatar;
                this.tvName = name;
                this.tvGroup = group;
                this.tvBadge = badge;
            }
        }
    }
}
