package com.juse.minigods;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.net.MalformedURLException;
import java.net.URL;

public class MenuActivity extends AppCompatActivity {
    private AdView adView;
    private ConsentForm form;
    private ConsentInformation consentInformation;
    private boolean npa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        consentInformation = ConsentInformation.getInstance(this);
        String[] publisherIds = {"pub-3628038149266927"};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                if (consentStatus == ConsentStatus.UNKNOWN) {
                    if (consentInformation.isRequestLocationInEeaOrUnknown()) {
                        setupPrivacyPolicy();
                    } else {
                        npa = false;
                    }
                } else {
                    npa = consentStatus == ConsentStatus.NON_PERSONALIZED;
                }
            }

            public void onFailedToUpdateConsentInfo(String reason) { }
        });

        findViewById(R.id.playButton).setOnClickListener(v -> openView(MainActivity.class));
        findViewById(R.id.aboutButton).setOnClickListener(v -> openView(AboutActivity.class));
        adView = findViewById(R.id.adView);
    }

    private void setupPrivacyPolicy() {
        npa = true;
        try {
            form = new ConsentForm.Builder(this, new URL(getString(R.string.privacy)))
                    .withListener(new ConsentFormListener() {
                        @Override
                        public void onConsentFormLoaded() {
                            super.onConsentFormLoaded();

                            if (form != null)
                                form.show();
                        }

                        @Override
                        public void onConsentFormError(String reason) {
                            super.onConsentFormError(reason);
                        }

                        @Override
                        public void onConsentFormOpened() {
                            super.onConsentFormOpened();
                        }

                        @Override
                        public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                            super.onConsentFormClosed(consentStatus, userPrefersAdFree);

                            if (consentStatus == ConsentStatus.NON_PERSONALIZED)
                                npa = true;
                            else if (consentStatus == ConsentStatus.PERSONALIZED)
                                npa = false;
                        }
                    })
                    .withPersonalizedAdsOption()
                    .withNonPersonalizedAdsOption()
                    .build();
            form.load();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = new Bundle();
        extras.putString("npa", npa ? "1" : "0");

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("D41BE5F1EE4B9029B95782426415A5D8")
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        adView.loadAd(adRequest);
    }

    private void openView(Class activity) {
        startActivity(new Intent(this, activity));
    }
}
