package com.ccs.javadroid;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.ccs.javadroid.util.AppPreferences;

public class JavaDroidApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String language = new AppPreferences(this).getAppLanguage();
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language));
        // Apply Material You Dynamic Colors across the app on Android 12+
        DynamicColors.applyToActivitiesIfAvailable(this);
        if (BuildConfig.DEBUG) {
            com.ccs.javadroid.util.StartupTrace.install(this);
        }
    }
}
