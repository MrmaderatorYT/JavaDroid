package com.ccs.javadroid.ui;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dialog construction shared across screens.
 *
 * <p>Five activities each carried an identical private {@code newRoundedDialog()}.
 * One place means one look, and one place to change it.</p>
 */
public final class Dialogs {

    private Dialogs() {}

    /** A Material dialog builder with the app's rounded styling. */
    public static MaterialAlertDialogBuilder rounded(Context context) {
        return new MaterialAlertDialogBuilder(context);
    }
}
