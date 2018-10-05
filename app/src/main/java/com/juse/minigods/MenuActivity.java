package com.juse.minigods;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.games.Games;

import java.net.MalformedURLException;
import java.net.URL;

public class MenuActivity extends AppCompatActivity {
    private final static int RC_SIGN_IN = 1337, RC_LEADERBOARD_UI = 1338;

    private AdView adView;
    private ConsentForm form;
    private ConsentInformation consentInformation;
    private boolean npa;

    private GoogleSignInClient signInClient;
    private GoogleSignInAccount account;
    private SignInButton signInButton;
    private Button signOutButton;

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

        setupGoogleGames();
    }

    private void setupGoogleGames() {
        signInButton = findViewById(R.id.googleSignIn);
        signOutButton = findViewById(R.id.signOut);

        signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        signInButton.setOnClickListener(v -> {
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, RC_SIGN_IN);
        });

        signOutButton.setOnClickListener(v ->
            signInClient.signOut().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    account = null;
                    updateLoginButtons();
                }
            })
        );

        findViewById(R.id.highscoreButton).setOnClickListener(view -> {
            if (account != null) {
                Games.getLeaderboardsClient(this, account)
                        .getLeaderboardIntent(getString(R.string.leaderboard_id))
                        .addOnSuccessListener(intent -> startActivityForResult(intent, RC_LEADERBOARD_UI));
            } else {
                new AlertDialog.Builder(this).setMessage(R.string.not_signed_in).create().show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ((account = GoogleSignIn.getLastSignedInAccount(this)) != null) {
            updateLoginButtons();
            return;
        }

        signInClient.silentSignIn().addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                account = result.getResult();
                updateLoginButtons();
            }
        });
    }

    private void updateLoginButtons() {
        if (account != null) {
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                account = result.getSignInAccount();
                updateLoginButtons();
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.sign_in_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    private void setupPrivacyPolicy() {
        npa = true;
        try {
            form = new ConsentForm.Builder(this, new URL(getString(R.string.privacy)))
                    .withListener(new ConsentFormListener() {
                        public void onConsentFormLoaded() {
                            super.onConsentFormLoaded();

                            if (form != null)
                                form.show();
                        }

                        public void onConsentFormError(String reason) {
                            super.onConsentFormError(reason);
                        }

                        public void onConsentFormOpened() {
                            super.onConsentFormOpened();
                        }

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

    protected void onStart() {
        super.onStart();

        Bundle extras = new Bundle();
        extras.putString("npa", npa ? "1" : "0");

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("D41BE5F1EE4B9029B95782426415A5D8")
                .addTestDevice("35F74627C48883E5E7AD4074C57CE6AA")
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        adView.loadAd(adRequest);
    }

    private void openView(Class activity) {
        startActivity(new Intent(this, activity));
    }
}
