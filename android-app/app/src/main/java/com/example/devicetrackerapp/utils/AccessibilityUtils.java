package com.example.devicetrackerapp.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class AccessibilityUtils {

    private AccessibilityUtils() {
    }

    public static boolean isEnabled(Context context) {

        ComponentName componentName =
                new ComponentName(
                        context,
                        "com.example.devicetrackerapp.service.RemoteAccessibilityService"
                );

        String enabledServices =
                Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                );

        if (enabledServices == null) {

            return false;

        }

        return enabledServices.contains(
                componentName.flattenToString()
        );

    }

    public static void openAccessibilitySettings(Context context) {

        Intent intent =
                new Intent(
                        Settings.ACTION_ACCESSIBILITY_SETTINGS
                );

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);

    }

}