package com.juse.minigods.Legality;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.michaelflisar.gdprdialog.GDPR;
import com.michaelflisar.gdprdialog.GDPRConsent;
import com.michaelflisar.gdprdialog.GDPRConsentState;
import com.michaelflisar.gdprdialog.GDPRDefinitions;
import com.michaelflisar.gdprdialog.GDPRLocation;
import com.michaelflisar.gdprdialog.GDPRSetup;

import java.net.MalformedURLException;
import java.net.URL;

import io.fabric.sdk.android.Fabric;

/**
 * Created by LukasW on 2018-06-10.
 * Consent to manage ad and firebase consent, init and showing dialogs to let user choose.
 */

public class Consent {
    private static final String PUBLISHER_ID = "pub-3628038149266927", POLICY = "https://lwestling.github.io/minidogs_privacy_policy.html";
    private static boolean ConsentInitialized = false, IsUserInEEA = false, AdmobConsentFinished = false, FirebaseConsentFinished = false;

    private ConsentForm consentForm;
    private GDPRSetup gdprSetup;
    private GDPR gdpr;
    private boolean autoShowConsent;

    // this class would deserve a makeover

    public <T extends AppCompatActivity & GDPR.IGDPRCallback & ConsentInfoUpdateListener>
    void setupConsent(T activityAndListener, boolean showConsent) {
        this.autoShowConsent = showConsent;

        consentForm = null;
        gdprSetup = null;

        gdpr = GDPR.getInstance();
        gdpr.init(activityAndListener);
        if (!ConsentInitialized) {
            ConsentInformation consentInformation = getConsentInformation(activityAndListener);
            setupAdConsent(consentInformation, activityAndListener);
        }
    }

    public <T extends AppCompatActivity & GDPR.IGDPRCallback>
    void updateAdmobConsent(T activityAndListener, boolean unknownConsent) {
        if (isUserInEea(activityAndListener)) {
            IsUserInEEA = true;
            if (!AdmobConsentFinished) {
                if (unknownConsent) { // only show admob consent if the consent is unknown
                    if (autoShowConsent)
                        showAdConsentForm(activityAndListener);
                } else {
                    AdmobConsentFinished = true;
                }
            }
            if (!FirebaseConsentFinished) {
                setupFirebaseConsent(activityAndListener);
            }
        } else {
            // If not in EEA everything can be skipped
            AdmobConsentFinished = true;

            setFirebaseConsent(
                new GDPRConsentState(
                        activityAndListener, GDPRConsent.AUTOMATIC_PERSONAL_CONSENT, GDPRLocation.NOT_IN_EAA
                ), activityAndListener
            );
        }
    }

    private ConsentInformation getConsentInformation(Context context) {
        ConsentInformation consentInformation = ConsentInformation.getInstance(context);
        consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        return consentInformation;
    }

    private void setupAdConsent(ConsentInformation consentInformation, ConsentInfoUpdateListener listener) {
        String[] publisherIds = {PUBLISHER_ID};
        consentInformation.requestConsentInfoUpdate(publisherIds, listener);
    }

    public void showAdConsentForm(Context context) {
        ConsentInformation consentInformation = getConsentInformation(context);
        URL consentUrl = getConsentUrl();

        consentForm = new ConsentForm.Builder(context, consentUrl).withListener(
                new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        super.onConsentFormLoaded();
                        if (consentForm != null)
                            consentForm.show();
                    }

                    @Override
                    public void onConsentFormError(String reason) {
                        super.onConsentFormError(reason);
                        Logger.CrashlyticsLog(new RuntimeException(reason));
                    }

                    @Override
                    public void onConsentFormOpened() {
                        super.onConsentFormOpened();
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        super.onConsentFormClosed(consentStatus, userPrefersAdFree);
                        consentInformation.setConsentStatus(consentStatus);
                        AdmobConsentFinished = true;
                        if (FirebaseConsentFinished)
                            ConsentInitialized = true;
                    }
        })
        .withPersonalizedAdsOption()
        .withNonPersonalizedAdsOption()
        .build();

        consentForm.load();
    }

    private <T extends AppCompatActivity & GDPR.IGDPRCallback> void setupFirebaseConsent(T activityAndListener) {
        if (autoShowConsent) {
            setupDialog();
            gdpr.checkIfNeedsToBeShown(activityAndListener, gdprSetup);
        }
    }

    public <T extends AppCompatActivity & GDPR.IGDPRCallback> void showFirebaseConsent(T activityAndListener) {
        setupDialog(); // Can be used with SettingsActivity, but is only a extra new on first run
        gdpr.showDialog(activityAndListener, gdprSetup, GDPRLocation.IN_EAA_OR_UNKNOWN);
    }

    private void setupDialog() {
        gdprSetup = new GDPRSetup(
            GDPRDefinitions.FIREBASE_ANALYTICS,
            GDPRDefinitions.FIREBASE_CRASH,
            GDPRDefinitions.FIREBASE_CLOUD_MESSAGING)
            .withExplicitAgeConfirmation(true)
            .withPrivacyPolicy(POLICY)
            .withForceSelection(true)
            .withNoToolbarTheme(true);
    }

    private static Bundle getConsentAdExtra(Context context) {
        boolean nonPersonalizedAds =
                ConsentInformation.getInstance(context).getConsentStatus() == ConsentStatus.NON_PERSONALIZED;
        Bundle bundle = new Bundle();
        bundle.putString("npa", nonPersonalizedAds ? "1" : "0");
        return bundle;
    }

    public static AdRequest buildAdRequestWithConsent(Context context) {
        Bundle bundle = getConsentAdExtra(context);
        return new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, bundle)
                .addTestDevice("D41BE5F1EE4B9029B95782426415A5D8")
                .addTestDevice("CFA46828750FB3025F4F2074775E6EAB")
                .build();
    }

    public void setFirebaseConsent(GDPRConsentState gdprConsentState, Context context) {
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(context);
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();

        FirebaseConsentFinished = true;
        if (AdmobConsentFinished)
            ConsentInitialized = true;

        // if user turns off analytics during runtime it has to be manually disabled
        if (gdprConsentState.getConsent() == GDPRConsent.PERSONAL_CONSENT ||
                gdprConsentState.getConsent() == GDPRConsent.AUTOMATIC_PERSONAL_CONSENT ||
                gdprConsentState.getLocation() == GDPRLocation.NOT_IN_EAA) {
            analytics.setAnalyticsCollectionEnabled(true);
            messaging.setAutoInitEnabled(true);
            Fabric.with(context, new Crashlytics());
        } else {
            analytics.setAnalyticsCollectionEnabled(false);
            messaging.setAutoInitEnabled(false);
            disableCrashlytics(context);
        }
    }

    private void disableCrashlytics(Context context) {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder().disabled(true).build();
        Fabric.with(context, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    public static boolean IsConsentInitialized() {
        return ConsentInitialized;
    }

    // Cached bool
    public static boolean IsUserInEea() {
        return IsUserInEEA;
    }

    // Checks if user is in EEA
    private boolean isUserInEea(Context context) {
        return getConsentInformation(context).isRequestLocationInEeaOrUnknown();
    }

    private URL getConsentUrl() {
        URL consentUrl = null;
        try {
            consentUrl = new URL(POLICY);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return consentUrl;
    }
}
