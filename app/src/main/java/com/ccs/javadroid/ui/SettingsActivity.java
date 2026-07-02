package com.ccs.javadroid.ui;
import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppPreferences;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FullScreenHelper;
import com.ccs.javadroid.tools.compilers.NdkManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;
import java.io.File;

/**
 * Розгорнутий екран налаштувань: тема, шрифт, поведінка редактора та компілятор.
 * UI будується програмно, щоб теми застосовувались миттєво без перезапуску.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String EXTRA_CHANGED = "changed";

    private AppPreferences prefs;
    private AppTheme theme;

    private LinearLayout customColorsSection;
    private TextView themeNameLabel;

    private View bgSwatch, fgSwatch, accentSwatch, toolbarSwatch,
            consoleBgSwatch, keywordSwatch, stringSwatch, commentSwatch;

    private String initialThemeId;
    private int initialCustomBg, initialCustomToolbar, initialCustomFg, initialCustomAccent,
            initialCustomConsoleBg, initialCustomKeyword, initialCustomString, initialCustomComment;
    private int initialFontSize, initialFontFamily, initialTabSize;
    private float initialLineSpacing;
    private boolean initialLineNumbers, initialWordWrap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new AppPreferences(this);
        theme = AppTheme.byId(prefs.getThemeId(), prefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        initialThemeId = prefs.getThemeId();
        initialCustomBg = prefs.getCustomBg();
        initialCustomToolbar = prefs.getCustomToolbar();
        initialCustomFg = prefs.getCustomFg();
        initialCustomAccent = prefs.getCustomAccent();
        initialCustomConsoleBg = prefs.getCustomConsoleBg();
        initialCustomKeyword = prefs.getCustomKeyword();
        initialCustomString = prefs.getCustomString();
        initialCustomComment = prefs.getCustomComment();
        initialFontSize = prefs.getFontSize();
        initialFontFamily = prefs.getFontFamily();
        initialTabSize = prefs.getTabSize();
        initialLineSpacing = prefs.getLineSpacing();
        initialLineNumbers = prefs.isLineNumbers();
        initialWordWrap = prefs.isWordWrap();

        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(0);
        setContentView(buildRoot());
        FullScreenHelper.enable(this);
    }

    @Override
    public void onBackPressed() {
        boolean changed = !initialThemeId.equals(prefs.getThemeId())
                || initialCustomBg != prefs.getCustomBg()
                || initialCustomToolbar != prefs.getCustomToolbar()
                || initialCustomFg != prefs.getCustomFg()
                || initialCustomAccent != prefs.getCustomAccent()
                || initialCustomConsoleBg != prefs.getCustomConsoleBg()
                || initialCustomKeyword != prefs.getCustomKeyword()
                || initialCustomString != prefs.getCustomString()
                || initialCustomComment != prefs.getCustomComment()
                || initialFontSize != prefs.getFontSize()
                || initialFontFamily != prefs.getFontFamily()
                || initialTabSize != prefs.getTabSize()
                || initialLineSpacing != prefs.getLineSpacing()
                || initialLineNumbers != prefs.isLineNumbers()
                || initialWordWrap != prefs.isWordWrap();
        setResult(Activity.RESULT_OK, getIntent().putExtra(EXTRA_CHANGED, changed));
        super.onBackPressed();
    }

    // ══════════════════════════════════════════════════════════
    //  UI
    // ══════════════════════════════════════════════════════════

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme.bg);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(theme.toolbar);
        toolbar.setTitle(getString(R.string.menu_settings));
        toolbar.setTitleTextColor(theme.text);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        root.addView(toolbar);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(scroll);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(8), dp(16), dp(24));
        scroll.addView(content);

        content.addView(buildAppearanceSection());
        content.addView(buildCustomColorsSection());
        content.addView(buildEditorSection());
        content.addView(buildCompilerSection());
        content.addView(buildPowerSavingSection());
        content.addView(buildResetButton());

        return root;
    }

    // ── Appearance / Theme ────────────────────────────────────

    private View buildAppearanceSection() {
        LinearLayout section = newSection(getString(R.string.settings_section_theme));

        themeNameLabel = subtitle(AppTheme.displayName(prefs.getThemeId()));
        section.addView(themeNameLabel);

        HorizontalScrollView hscroll = new HorizontalScrollView(this);
        hscroll.setHorizontalScrollBarEnabled(false);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, dp(8));

        for (AppTheme preset : AppTheme.presets()) {
            row.addView(buildThemeCard(preset));
        }
        // Кастомний пресет
        row.addView(buildCustomThemeCard());

        hscroll.addView(row);
        section.addView(hscroll);
        return section;
    }

    private View buildThemeCard(AppTheme preset) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(110), dp(96));
        lp.setMargins(0, 0, dp(10), 0);
        card.setLayoutParams(lp);

        boolean selected = preset.id.equals(prefs.getThemeId());
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(preset.bg);
        bg.setStroke(selected ? dp(2) : dp(1),
                selected ? preset.accent : theme.separator);
        bg.setCornerRadius(dp(8));
        card.setBackground(bg);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));

        // Mini-preview rows
        View r1 = swatchRow(preset.editorKeyword, preset.editorString);
        View r2 = swatchRow(preset.editorComment, preset.text);
        card.addView(r1);
        card.addView(spacer(dp(4)));
        card.addView(r2);

        TextView name = new TextView(this);
        name.setText(AppTheme.displayName(preset.id));
        name.setTextColor(preset.text);
        name.setTextSize(11);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = dp(8);
        name.setLayoutParams(nlp);
        name.setGravity(Gravity.CENTER);
        card.addView(name);

        card.setContentDescription(getString(R.string.a11y_settings_theme_card, AppTheme.displayName(preset.id)));
        card.setOnClickListener(v -> {
            prefs.setThemeId(preset.id);
            theme = AppTheme.byId(preset.id, prefs);
            smoothRecreate();
        });
        return card;
    }

    private View buildCustomThemeCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(110), dp(96));
        lp.setMargins(0, 0, dp(10), 0);
        card.setLayoutParams(lp);

        boolean selected = AppTheme.ID_CUSTOM.equals(prefs.getThemeId());
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(prefs.getCustomBg());
        bg.setStroke(selected ? dp(2) : dp(1),
                selected ? prefs.getCustomAccent() : theme.separator);
        bg.setCornerRadius(dp(8));
        card.setBackground(bg);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));

        View r1 = swatchRow(prefs.getCustomKeyword(), prefs.getCustomString());
        View r2 = swatchRow(prefs.getCustomComment(), prefs.getCustomFg());
        card.addView(r1);
        card.addView(spacer(dp(4)));
        card.addView(r2);

        TextView name = new TextView(this);
        name.setText(getString(R.string.settings_theme_custom));
        name.setTextColor(prefs.getCustomFg());
        name.setTextSize(11);
        name.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = dp(8);
        name.setLayoutParams(nlp);
        card.addView(name);

        card.setContentDescription(getString(R.string.a11y_settings_custom_theme_card));
        card.setOnClickListener(v -> {
            prefs.setThemeId(AppTheme.ID_CUSTOM);
            theme = AppTheme.byId(AppTheme.ID_CUSTOM, prefs);
            smoothRecreate();
        });
        return card;
    }

    private View swatchRow(int c1, int c2) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        View a = new View(this);
        a.setBackgroundColor(c1);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(0, dp(10), 1f);
        ap.setMargins(0, 0, dp(2), 0);
        a.setLayoutParams(ap);

        View b = new View(this);
        b.setBackgroundColor(c2);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(10), 2f);
        b.setLayoutParams(bp);

        row.addView(a);
        row.addView(b);
        return row;
    }

    // ── Custom Colors ─────────────────────────────────────────

    private View buildCustomColorsSection() {
        customColorsSection = newSection(getString(R.string.settings_section_custom_colors));
        boolean isCustom = AppTheme.ID_CUSTOM.equals(prefs.getThemeId());
        customColorsSection.setVisibility(isCustom ? View.VISIBLE : View.GONE);

        bgSwatch       = colorPickerRow(getString(R.string.settings_color_bg),         prefs.getCustomBg(),        c -> { prefs.setCustomBg(c);        refreshCustomTheme(); });
        toolbarSwatch  = colorPickerRow(getString(R.string.settings_color_toolbar),    prefs.getCustomToolbar(),   c -> { prefs.setCustomToolbar(c);   refreshCustomTheme(); });
        fgSwatch       = colorPickerRow(getString(R.string.settings_color_fg),         prefs.getCustomFg(),        c -> { prefs.setCustomFg(c);        refreshCustomTheme(); });
        accentSwatch   = colorPickerRow(getString(R.string.settings_color_accent),     prefs.getCustomAccent(),    c -> { prefs.setCustomAccent(c);    refreshCustomTheme(); });
        consoleBgSwatch= colorPickerRow(getString(R.string.settings_color_console_bg), prefs.getCustomConsoleBg(), c -> { prefs.setCustomConsoleBg(c); refreshCustomTheme(); });
        keywordSwatch  = colorPickerRow(getString(R.string.settings_color_keyword),    prefs.getCustomKeyword(),   c -> { prefs.setCustomKeyword(c);   refreshCustomTheme(); });
        stringSwatch   = colorPickerRow(getString(R.string.settings_color_string),     prefs.getCustomString(),    c -> { prefs.setCustomString(c);    refreshCustomTheme(); });
        commentSwatch  = colorPickerRow(getString(R.string.settings_color_comment),    prefs.getCustomComment(),   c -> { prefs.setCustomComment(c);   refreshCustomTheme(); });

        return customColorsSection;
    }

    private void refreshCustomTheme() {
        if (AppTheme.ID_CUSTOM.equals(prefs.getThemeId())) {
            theme = AppTheme.byId(AppTheme.ID_CUSTOM, prefs);
        }
        smoothRecreate();
    }

    /**
     * Перезапускає Activity без миготіння (flash/blink).
     * Вимикає анімацію переходу і одразу відновлює з новою темою.
     */
    private void smoothRecreate() {
        overridePendingTransition(0, 0);
        recreate();
        overridePendingTransition(0, 0);
    }

    private View colorPickerRow(String label, int currentColor, ColorChosen onChosen) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));
        android.util.TypedValue tv = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
        if (tv.resourceId != 0) row.setBackgroundResource(tv.resourceId);
        row.setClickable(true);
        row.setFocusable(true);

        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(theme.text);
        t.setTextSize(13);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        t.setLayoutParams(tlp);

        View swatch = new View(this);
        GradientDrawable d = new GradientDrawable();
        d.setColor(currentColor);
        d.setCornerRadius(dp(4));
        d.setStroke(dp(1), theme.separator);
        swatch.setBackground(d);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(40), dp(24));
        slp.setMargins(dp(8), 0, dp(8), 0);
        swatch.setLayoutParams(slp);

        TextView hex = new TextView(this);
        hex.setText(toHex(currentColor));
        hex.setTextColor(theme.textDim);
        hex.setTextSize(11);
        hex.setTypeface(new AppPreferences(this).resolveTypeface());

        row.addView(t);
        row.addView(swatch);
        row.addView(hex);

        row.setContentDescription(getString(R.string.a11y_settings_color_picker, label));
        row.setOnClickListener(v -> showColorDialog(label, currentColor, c -> {
            d.setColor(c);
            hex.setText(toHex(c));
            onChosen.onColor(c);
        }));

        customColorsSection.addView(row);
        return swatch;
    }

    private interface ColorChosen { void onColor(int color); }

    private void showColorDialog(String title, int initial, ColorChosen cb) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        box.setPadding(p, p, p, p);

        // Кольорове прев'ю
        View preview = new View(this);
        GradientDrawable pd = new GradientDrawable();
        pd.setColor(initial);
        pd.setCornerRadius(dp(6));
        pd.setStroke(dp(1), theme.separator);
        preview.setBackground(pd);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(40));
        plp.bottomMargin = dp(8);
        preview.setLayoutParams(plp);
        box.addView(preview);

        final int[] rgb = { Color.red(initial), Color.green(initial), Color.blue(initial) };

        EditText hexInput = new EditText(this);
        hexInput.setHint("#RRGGBB");
        hexInput.setText(toHex(initial));
        hexInput.setTypeface(new AppPreferences(this).resolveTypeface());
        hexInput.setTextColor(theme.text);
        hexInput.setHintTextColor(theme.textDim);
        hexInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(7) });
        hexInput.setInputType(InputType.TYPE_CLASS_TEXT);
        hexInput.setContentDescription(getString(R.string.a11y_settings_hex_input));
        box.addView(hexInput);

        SeekBar rs = newColorSeek(rgb[0]);
        rs.setContentDescription(getString(R.string.a11y_settings_color_r));
        SeekBar gs = newColorSeek(rgb[1]);
        gs.setContentDescription(getString(R.string.a11y_settings_color_g));
        SeekBar bs = newColorSeek(rgb[2]);
        bs.setContentDescription(getString(R.string.a11y_settings_color_b));
        TextView rl = colorSeekLabel("R", rgb[0]);
        TextView gl = colorSeekLabel("G", rgb[1]);
        TextView bl = colorSeekLabel("B", rgb[2]);
        box.addView(rl); box.addView(rs);
        box.addView(gl); box.addView(gs);
        box.addView(bl); box.addView(bs);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (s == rs) { rgb[0] = progress; rl.setText("R: " + progress); }
                if (s == gs) { rgb[1] = progress; gl.setText("G: " + progress); }
                if (s == bs) { rgb[2] = progress; bl.setText("B: " + progress); }
                int c = Color.rgb(rgb[0], rgb[1], rgb[2]);
                pd.setColor(c);
                hexInput.setText(toHex(c));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        rs.setOnSeekBarChangeListener(listener);
        gs.setOnSeekBarChangeListener(listener);
        bs.setOnSeekBarChangeListener(listener);

        hexInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) tryParseHex(hexInput, rs, gs, bs, pd);
        });

        // Палітра пресетних кольорів
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(8);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        glp.topMargin = dp(8);
        grid.setLayoutParams(glp);
        for (int c : PRESET_PALETTE) {
            View dot = new View(this);
            GradientDrawable dd = new GradientDrawable();
            dd.setColor(c);
            dd.setCornerRadius(dp(3));
            dd.setStroke(dp(1), theme.separator);
            dot.setBackground(dd);
            GridLayout.LayoutParams gp = new GridLayout.LayoutParams();
            gp.width = dp(28);
            gp.height = dp(28);
            gp.setMargins(dp(2), dp(2), dp(2), dp(2));
            dot.setLayoutParams(gp);
            dot.setOnClickListener(view -> {
                rs.setProgress(Color.red(c));
                gs.setProgress(Color.green(c));
                bs.setProgress(Color.blue(c));
            });
            grid.addView(dot);
        }
        box.addView(grid);

        newRoundedDialog()
                .setTitle(title)
                .setView(box)
                .setPositiveButton(R.string.dialog_apply, (d, w) -> {
                    tryParseHex(hexInput, rs, gs, bs, pd);
                    cb.onColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private static final int[] PRESET_PALETTE = {
            0xFF000000, 0xFF1E1E1E, 0xFF2B2B2B, 0xFF3C3F41,
            0xFF808080, 0xFFBBBBBB, 0xFFEEEEEE, 0xFFFFFFFF,
            0xFFFF6B6B, 0xFFE74C3C, 0xFFD13438, 0xFFBF616A,
            0xFFFFA500, 0xFFE6A23C, 0xFFCC7832, 0xFFCCC77A,
            0xFF6A8759, 0xFF499C54, 0xFF50FA7B, 0xFFA6E22E,
            0xFF4A86C8, 0xFF1F6FEB, 0xFF268BD2, 0xFF88C0D0,
            0xFF6897BB, 0xFFAE81FF, 0xFFBD93F9, 0xFFB48EAD,
            0xFFFF79C6, 0xFFE6DB74, 0xFFF1FA8C, 0xFF859900
    };

    private SeekBar newColorSeek(int v) {
        SeekBar s = new SeekBar(this);
        s.setMax(255);
        s.setProgress(v);
        return s;
    }

    private TextView colorSeekLabel(String comp, int v) {
        TextView t = new TextView(this);
        t.setText(comp + ": " + v);
        t.setTextColor(theme.text);
        t.setTextSize(11);
        return t;
    }

    private void tryParseHex(EditText input, SeekBar r, SeekBar g, SeekBar b, GradientDrawable preview) {
        try {
            String s = input.getText().toString().trim();
            if (!s.startsWith("#")) s = "#" + s;
            int c = Color.parseColor(s);
            r.setProgress(Color.red(c));
            g.setProgress(Color.green(c));
            b.setProgress(Color.blue(c));
            preview.setColor(c);
        } catch (Exception ignored) {
        }
    }

    // ── Editor ────────────────────────────────────────────────

    private View buildEditorSection() {
        LinearLayout section = newSection(getString(R.string.settings_section_editor));

        // Font family
        section.addView(label(getString(R.string.settings_font_family)));
        Spinner fontSpinner = newSpinner(new String[] {
                getString(R.string.font_monospace),
                getString(R.string.font_sans),
                getString(R.string.font_serif),
                getString(R.string.font_default),
                getString(R.string.font_jetbrains),
                getString(R.string.font_fira_code),
                getString(R.string.font_source_code),
                getString(R.string.font_dejavu_mono),
                getString(R.string.font_roboto_mono)
        });
        fontSpinner.setContentDescription(getString(R.string.a11y_settings_font_family));
        fontSpinner.setSelection(prefs.getFontFamily());
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setFontFamily(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        section.addView(fontSpinner);

        // Font size
        TextView sizeLabel = label(getString(R.string.settings_font_size_n, prefs.getFontSize()));
        section.addView(sizeLabel);
        SeekBar sizeBar = new SeekBar(this);
        sizeBar.setContentDescription(getString(R.string.a11y_settings_font_size));
        sizeBar.setMax(24); // 8..32
        sizeBar.setProgress(prefs.getFontSize() - 8);
        sizeBar.setOnSeekBarChangeListener(simpleSeek(progress -> {
            int sz = progress + 8;
            sizeLabel.setText(getString(R.string.settings_font_size_n, sz));
            prefs.setFontSize(sz);
        }));
        section.addView(sizeBar);

        // Tab size
        TextView tabLabel = label(getString(R.string.settings_tab_size_n, prefs.getTabSize()));
        section.addView(tabLabel);
        SeekBar tabBar = new SeekBar(this);
        tabBar.setContentDescription(getString(R.string.a11y_settings_tab_size));
        tabBar.setMax(7); // 1..8
        tabBar.setProgress(prefs.getTabSize() - 1);
        tabBar.setOnSeekBarChangeListener(simpleSeek(progress -> {
            int t = progress + 1;
            tabLabel.setText(getString(R.string.settings_tab_size_n, t));
            prefs.setTabSize(t);
        }));
        section.addView(tabBar);

        // Line spacing
        TextView lsLabel = label(getString(R.string.settings_line_spacing_n,
                String.format(Locale.US, "%.1f", prefs.getLineSpacing())));
        section.addView(lsLabel);
        SeekBar lsBar = new SeekBar(this);
        lsBar.setContentDescription(getString(R.string.a11y_settings_line_spacing));
        lsBar.setMax(20); // 1.0..3.0 step 0.1
        lsBar.setProgress((int) ((prefs.getLineSpacing() - 1f) * 10));
        lsBar.setOnSeekBarChangeListener(simpleSeek(progress -> {
            float ls = 1f + progress / 10f;
            lsLabel.setText(getString(R.string.settings_line_spacing_n,
                    String.format(Locale.US, "%.1f", ls)));
            prefs.setLineSpacing(ls);
        }));
        section.addView(lsBar);

        section.addView(buildSwitch(getString(R.string.settings_line_numbers),
                prefs.isLineNumbers(), prefs::setLineNumbers));
        section.addView(buildSwitch(getString(R.string.settings_word_wrap),
                prefs.isWordWrap(), prefs::setWordWrap));
        section.addView(buildSwitch(getString(R.string.settings_auto_save),
                prefs.isAutoSave(), prefs::setAutoSave));
        section.addView(buildSwitch(getString(R.string.settings_format_on_save),
                prefs.isFormatOnSave(), prefs::setFormatOnSave));
        section.addView(buildSwitch(getString(R.string.settings_minimap),
                prefs.isMinimap(), prefs::setMinimap));

        return section;
    }

    // ── Compiler ──────────────────────────────────────────────

    private View buildCompilerSection() {
        LinearLayout section = newSection(getString(R.string.settings_section_compiler));

        section.addView(label(getString(R.string.settings_java_target)));
        final String[] codes = { AppPreferences.JAVA_8 };
        String[] labels = {
                "Java 8"
        };
        Spinner sp = newSpinner(labels);
        sp.setContentDescription(getString(R.string.a11y_settings_java_target));
        int sel = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(prefs.getJavaTarget())) { sel = i; break; }
        }
        sp.setSelection(sel);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setJavaTarget(codes[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        section.addView(sp);

        TextView hint = new TextView(this);
        hint.setText(getString(R.string.settings_java_hint));
        hint.setTextColor(theme.textDim);
        hint.setTextSize(11);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(4);
        hint.setLayoutParams(lp);
        section.addView(hint);

        // NDK Installation Button
        TextView ndkBtn = new TextView(this);
        boolean ndkInstalled = NdkManager.isNdkInstalled(this);
        ndkBtn.setText(ndkInstalled ? R.string.settings_ndk_installed : R.string.settings_ndk_install);
        ndkBtn.setTextColor(ndkInstalled ? theme.errorText : theme.accent);
        ndkBtn.setTextSize(14);
        ndkBtn.setPadding(dp(8), dp(16), dp(8), dp(8));
        ndkBtn.setContentDescription(getString(R.string.a11y_settings_ndk));
        ndkBtn.setOnClickListener(v -> {
            if (NdkManager.isNdkInstalled(this)) {
                newRoundedDialog()
                        .setTitle(R.string.settings_ndk_uninstall_title)
                        .setMessage(R.string.settings_ndk_uninstall_message)
                        .setPositiveButton(R.string.settings_ndk_remove, (di, w) -> {
                            deleteRecursive(NdkManager.getNdkDir(this).getParentFile());
                            ndkBtn.setText(R.string.settings_ndk_install);
                            ndkBtn.setTextColor(theme.accent);
                            Toast.makeText(this, getString(R.string.settings_ndk_removed), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                newRoundedDialog()
                        .setTitle(R.string.settings_ndk_install_title)
                        .setMessage(R.string.settings_ndk_install_message)
                        .setPositiveButton(R.string.settings_ndk_download, (di, w) -> {
                            android.app.ProgressDialog pd = new android.app.ProgressDialog(this);
                            pd.setTitle(R.string.settings_ndk_installing);
                            pd.setMessage(getString(R.string.settings_ndk_connecting));
                            pd.setCancelable(false);
                            pd.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
                            pd.setMax(100);
                            pd.show();

                            NdkManager.downloadAndInstallNdk(this, new NdkManager.NdkInstallCallback() {
                                @Override
                                public void onProgress(String message, int percent) {
                                    pd.setMessage(message);
                                    pd.setProgress(percent);
                                }
                                @Override
                                public void onSuccess() {
                                    pd.dismiss();
                                    ndkBtn.setText(R.string.settings_ndk_installed);
                                    ndkBtn.setTextColor(theme.errorText);
                                    Toast.makeText(SettingsActivity.this, getString(R.string.settings_ndk_success), Toast.LENGTH_LONG).show();
                                }
                                @Override
                                public void onError(String error) {
                                    pd.dismiss();
                                    new AlertDialog.Builder(SettingsActivity.this)
                                            .setTitle(R.string.settings_ndk_error_title)
                                            .setMessage(getString(R.string.settings_ndk_error_message, error))
                                            .setPositiveButton(R.string.settings_ndk_ok, null)
                                            .show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        section.addView(ndkBtn);

        section.addView(buildSwitch(getString(R.string.settings_verbose_logging),
                prefs.isVerboseLoggingEnabled(), prefs::setVerboseLoggingEnabled));

        return section;
    }

    // ── Power Saving ───────────────────────────────────────

    private View buildPowerSavingSection() {
        LinearLayout section = newSection(getString(R.string.settings_section_power_saving));

        TextView desc = new TextView(this);
        desc.setText(getString(R.string.settings_power_saving_desc));
        desc.setTextColor(theme.textDim);
        desc.setTextSize(11);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.bottomMargin = dp(8);
        desc.setLayoutParams(dlp);
        section.addView(desc);

        String[] modes = {
                getString(R.string.settings_power_saving_auto),
                getString(R.string.settings_power_saving_disabled),
                getString(R.string.settings_power_saving_performance)
        };
        Spinner sp = newSpinner(modes);
        sp.setContentDescription(getString(R.string.a11y_settings_power_saving));
        sp.setSelection(prefs.getPowerSavingMode());
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.setPowerSavingMode(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        section.addView(sp);

        return section;
    }

    private View buildResetButton() {
        TextView btn = new TextView(this);
        btn.setText(getString(R.string.settings_reset_defaults));
        btn.setTextColor(theme.errorText);
        btn.setTextSize(13);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(16), dp(14), dp(16), dp(14));
        btn.setContentDescription(getString(R.string.a11y_settings_reset));
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x00000000);
        d.setStroke(dp(1), theme.errorText);
        d.setCornerRadius(dp(6));
        btn.setBackground(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(24);
        btn.setLayoutParams(lp);
        btn.setOnClickListener(v -> newRoundedDialog()
                .setTitle(R.string.settings_reset_defaults)
                .setMessage(R.string.settings_reset_confirm)
                .setPositiveButton(R.string.dialog_apply, (di, w) -> resetDefaults())
                .setNegativeButton(R.string.dialog_cancel, null)
                .show());
        return btn;
    }

    private void resetDefaults() {
        // Залишаємо project_root, скидаємо все інше.
        String root = prefs.getProjectRoot();
        prefs.raw().edit().clear().apply();
        if (root != null) prefs.setProjectRoot(root);
        Toast.makeText(this, R.string.settings_reset_done, Toast.LENGTH_SHORT).show();
        smoothRecreate();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    // ══════════════════════════════════════════════════════════
    //  UI helpers
    // ══════════════════════════════════════════════════════════

    private LinearLayout newSection(String title) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(16);
        box.setLayoutParams(lp);

        TextView header = new TextView(this);
        header.setText(title);
        header.setTextColor(theme.accent);
        header.setTextSize(11);
        header.setLetterSpacing(0.08f);
        header.setAllCaps(true);
        header.setPadding(0, 0, 0, dp(8));
        box.addView(header);

        View sep = new View(this);
        sep.setBackgroundColor(theme.separator);
        sep.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        box.addView(sep);

        return box;
    }

    private TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(theme.text);
        t.setTextSize(13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        t.setLayoutParams(lp);
        return t;
    }

    private TextView subtitle(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(theme.textDim);
        t.setTextSize(12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(4);
        t.setLayoutParams(lp);
        return t;
    }

    private Spinner newSpinner(String[] items) {
        Spinner sp = new Spinner(this);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTextColor(theme.text);
                return v;
            }
        };
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(ad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(4);
        sp.setLayoutParams(lp);
        return sp;
    }

    private View buildSwitch(String title, boolean initial, BoolSetter setter) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = dp(8);
        row.setLayoutParams(rlp);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(theme.text);
        t.setTextSize(13);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        t.setLayoutParams(tlp);

        Switch sw = new Switch(this);
        sw.setContentDescription(title);
        sw.setChecked(initial);
        sw.setOnCheckedChangeListener((CompoundButton b, boolean v) -> setter.set(v));

        row.addView(t);
        row.addView(sw);
        return row;
    }

    private interface BoolSetter { void set(boolean v); }

    private SeekBar.OnSeekBarChangeListener simpleSeek(IntConsumer cb) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) { cb.accept(progress); }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
    }

    private interface IntConsumer { void accept(int v); }

    private View spacer(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
        return v;
    }

    private static String toHex(int color) {
        return String.format(Locale.US, "#%06X", 0xFFFFFF & color);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private com.google.android.material.dialog.MaterialAlertDialogBuilder newRoundedDialog() {
        return new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
    }

    public static void launch(Activity host, int requestCode) {
        host.startActivityForResult(new android.content.Intent(host, SettingsActivity.class), requestCode);
    }
}
