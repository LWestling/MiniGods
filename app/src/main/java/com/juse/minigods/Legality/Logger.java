package com.juse.minigods.Legality;

// Logger, and only used with consent

import android.content.Context;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

public class Logger {
    private static boolean disabled = false;

    public static void CrashlyticsLog(String message) {
        if (Consent.IsConsentInitialized() && !disabled) {
            Crashlytics.log(message);
        }
    }

    public static void CrashlyticsLog(Exception e) {
        if (Consent.IsConsentInitialized() && !disabled) {
            Crashlytics.logException(e);
        }
    }

    public static void AnalyticsLog(Context context, String name, Bundle bundle) {
        if (Consent.IsConsentInitialized() && !disabled) {
            FirebaseAnalytics.getInstance(context).logEvent(name, bundle);
        }
    }

    public static void Disable() {
        disabled = true;
    }

    public static void AnalyticsProperty(Context context, String property, String value) {
        FirebaseAnalytics.getInstance(context).setUserProperty(property, value);
    }
}
