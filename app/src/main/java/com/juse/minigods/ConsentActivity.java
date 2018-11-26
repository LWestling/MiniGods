package com.juse.minigods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentStatus;
import com.juse.minigods.Legality.Consent;
import com.juse.minigods.Legality.Logger;
import com.michaelflisar.gdprdialog.GDPR;
import com.michaelflisar.gdprdialog.GDPRConsentState;
import com.michaelflisar.gdprdialog.helper.GDPRPreperationData;

/*
    Abstract class that is extended by activities to always check consent
    @author Lukas Westling
 */
public abstract class ConsentActivity
        extends AppCompatActivity implements GDPR.IGDPRCallback, ConsentInfoUpdateListener {
    private Consent consent;
    private boolean showConsent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showConsent = true;
    }

    protected void onResume() {
        super.onResume();

        setupConsent();
    }

    private void setupConsent() {
        consent = new Consent();
        consent.setupConsent(this, showConsent);
    }

    /* General Consent Callbacks */
    public void onConsentNeedsToBeRequested(GDPRPreperationData gdprPreperationData) {
        consent.showFirebaseConsent(this);
    }

    public void onConsentInfoUpdate(GDPRConsentState gdprConsentState, boolean bool) {
        consent.setFirebaseConsent(gdprConsentState, this);
    }

    /* Admob Consent Callbacks */
    public void onConsentInfoUpdated(ConsentStatus consentStatus) {
        consent.updateAdmobConsent(this, consentStatus == ConsentStatus.UNKNOWN);
    }

    public void onFailedToUpdateConsentInfo(String errorDescription) {
        Logger.CrashlyticsLog(errorDescription);
    }

    public void setShowConsent(boolean showConsent) {
        this.showConsent = showConsent;
    }

    public Consent getConsent() {
        return consent;
    }
}
