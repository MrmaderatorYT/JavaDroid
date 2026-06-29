package com.ccs.javadroid.ui;
import com.ccs.javadroid.R;
import com.ccs.javadroid.util.AppPreferences;
import com.ccs.javadroid.util.AppTheme;
import com.ccs.javadroid.util.FullScreenHelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;

/**
 * Simple image viewer: displays bitmap files (jpg, png, gif, webp).
 */
public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "file_path";

    private AppPreferences prefs;
    private AppTheme theme;
    private ImageView imageView;
    private TextView statusBar;

    public static void launch(Context context, File imageFile) {
        Intent i = new Intent(context, ImageViewerActivity.class);
        i.putExtra(EXTRA_FILE_PATH, imageFile.getAbsolutePath());
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

        loadImage(new File(path));
    }

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme.bg);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(theme.toolbar);
        toolbar.setTitle("Image Viewer");
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
                0, 0, 1));
        container.setBackgroundColor(theme.consoleBg);

        imageView = new ImageView(this);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setContentDescription(getString(R.string.a11y_image_viewer));
        container.addView(imageView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER));

        root.addView(container);

        return root;
    }

    private void loadImage(File file) {
        statusBar.setText("Loading: " + file.getName());

        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

            int width = opts.outWidth;
            int height = opts.outHeight;
            int inSampleSize = 1;

            while (width / inSampleSize > 2048 || height / inSampleSize > 2048) {
                inSampleSize *= 2;
            }

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = inSampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                statusBar.setText(String.format("%s — %d x %d — %d KB",
                        file.getName(), opts.outWidth, opts.outHeight,
                        file.length() / 1024));
            } else {
                statusBar.setText("Failed to decode image");
                Toast.makeText(this, "Failed to decode image",
                        Toast.LENGTH_LONG).show();
            }
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
