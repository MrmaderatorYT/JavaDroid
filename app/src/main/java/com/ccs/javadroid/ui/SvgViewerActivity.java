package com.ccs.javadroid.ui;
import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppPreferences;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FullScreenHelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * SVG viewer: renders SVG files and allows zoom/pan.
 */
public class SvgViewerActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "file_path";

    private AppPreferences prefs;
    private AppTheme theme;
    private ImageView imageView;
    private TextView statusBar;

    public static void launch(Context context, File svgFile) {
        Intent i = new Intent(context, SvgViewerActivity.class);
        i.putExtra(EXTRA_FILE_PATH, svgFile.getAbsolutePath());
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new AppPreferences(this);
        theme = AppTheme.byId(prefs.getThemeId(), prefs);
        setTheme(theme.dark ? R.style.Theme_JavaDroid : R.style.Theme_JavaDroid_Light);

        super.onCreate(savedInstanceState);
        setContentView(buildRoot());
        FullScreenHelper.enable(this);

        String path = getIntent().getStringExtra(EXTRA_FILE_PATH);
        if (path == null) {
            Toast.makeText(this, "No file specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadSvg(new File(path));
    }

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme.bg);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(theme.toolbar);
        toolbar.setTitle("SVG Viewer");
        toolbar.setTitleTextColor(theme.text);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> finish());
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        statusBar = new TextView(this);
        statusBar.setBackgroundColor(theme.consoleBg);
        statusBar.setTextColor(theme.textDim);
        statusBar.setTextSize(11);
        statusBar.setPadding(dp(12), dp(6), dp(12), dp(6));
        root.addView(statusBar);

        FrameLayout container = new FrameLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        container.setBackgroundColor(theme.consoleBg);

        imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        container.addView(imageView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));

        root.addView(container);

        return root;
    }

    private void loadSvg(File file) {
        statusBar.setText("Loading: " + file.getName());

        try (InputStream is = new FileInputStream(file)) {
            SVG svg = SVG.getFromInputStream(is);

            float svgWidth = svg.getDocumentWidth();
            float svgHeight = svg.getDocumentHeight();

            int screenW = getResources().getDisplayMetrics().widthPixels;
            int screenH = getResources().getDisplayMetrics().heightPixels;

            if (svgWidth <= 0 || svgHeight <= 0) {
                svgWidth = screenW;
                svgHeight = screenH;
            }

            float scaleX = (float) screenW / svgWidth;
            float scaleY = (float) screenH / svgHeight;
            float scale = Math.max(scaleX, scaleY);

            int bitmapWidth = Math.max(1, (int) (svgWidth * scale));
            int bitmapHeight = Math.max(1, (int) (svgHeight * scale));

            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);

            float canvasScale = (float) bitmapWidth / svgWidth;
            canvas.scale(canvasScale, canvasScale);
            svg.renderToCanvas(canvas);

            imageView.setImageBitmap(bitmap);
            statusBar.setText(String.format("%s — %.0f x %.0f (x%.1f)",
                    file.getName(), svgWidth, svgHeight, scale));

        } catch (SVGParseException e) {
            statusBar.setText("Parse error: " + e.getMessage());
            Toast.makeText(this, "SVG parse error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            statusBar.setText("Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
