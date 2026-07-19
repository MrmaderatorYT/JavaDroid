package com.ccs.javadroid;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class JavaDroidApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Apply Material You Dynamic Colors across the app on Android 12+
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
