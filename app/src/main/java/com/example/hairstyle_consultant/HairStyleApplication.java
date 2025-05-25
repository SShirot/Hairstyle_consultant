package com.example.hairstyle_consultant;

import android.app.Application;
import android.util.Log;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.FirebaseApp;

public class HairStyleApplication extends Application {
    private static final String TAG = "HairStyleApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize Google Play Services
        initializeGooglePlayServices();
    }

    private void initializeGooglePlayServices() {
        try {
            // Update security provider
            ProviderInstaller.installIfNeeded(this);
            
            // Check Google Play Services availability
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int resultCode = googleAPI.isGooglePlayServicesAvailable(this);
            
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                Log.w(TAG, "Google Play Services not available. Result code: " + resultCode);
                if (googleAPI.isUserResolvableError(resultCode)) {
                    googleAPI.showErrorNotification(this, resultCode);
                }
            } else {
                Log.d(TAG, "Google Play Services is available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Google Play Services", e);
        }
    }
} 