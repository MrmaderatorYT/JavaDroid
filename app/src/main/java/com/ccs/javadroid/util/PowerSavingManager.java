package com.ccs.javadroid.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public final class PowerSavingManager {
    public static final int MODE_AUTO = 0;
    public static final int MODE_DISABLED = 1;
    public static final int MODE_ALWAYS_PERFORMANCE = 2;

    private static final int BATTERY_LOW_THRESHOLD = 30;

    private final AppPreferences prefs;
    private final Context context;

    public PowerSavingManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = new AppPreferences(context);
    }

    public int getMode() {
        return prefs.getPowerSavingMode();
    }

    public void setMode(int mode) {
        prefs.setPowerSavingMode(mode);
    }

    public boolean isPowerSavingActive() {
        int mode = getMode();
        switch (mode) {
            case MODE_DISABLED:
                return false;
            case MODE_ALWAYS_PERFORMANCE:
                return false;
            case MODE_AUTO:
            default:
                return getBatteryLevel() < BATTERY_LOW_THRESHOLD;
        }
    }

    public boolean isPerformanceMode() {
        int mode = getMode();
        if (mode == MODE_ALWAYS_PERFORMANCE) {
            return true;
        }
        if (mode == MODE_AUTO) {
            return getBatteryLevel() >= BATTERY_LOW_THRESHOLD;
        }
        return false;
    }

    public int getProblemsScanIntervalMs() {
        if (isPowerSavingActive()) {
            return 60_000;
        }
        if (isPerformanceMode()) {
            return 5_000;
        }
        return 10_000;
    }

    public boolean shouldAutoSave() {
        if (isPowerSavingActive()) {
            return false;
        }
        return true;
    }

    public boolean shouldAutoSearch() {
        if (isPowerSavingActive()) {
            return false;
        }
        return isPerformanceMode();
    }

    public boolean shouldFormatOnSave() {
        if (isPowerSavingActive()) {
            return false;
        }
        return true;
    }

    public boolean shouldLogVerbose() {
        if (isPowerSavingActive()) {
            return false;
        }
        return prefs.isVerboseLoggingEnabled();
    }

    public boolean shouldReduceAnimations() {
        return isPowerSavingActive();
    }

    private int getBatteryLevel() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) {
                return (int) ((level / (float) scale) * 100);
            }
        }
        return 100;
    }
}
