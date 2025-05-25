package com.example.hairstyle_consultant.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.hairstyle_consultant.models.User;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseError;
import com.google.android.gms.security.ProviderInstaller;

public class AuthenticationManager {
    private static final String TAG = "AuthenticationManager";
    private static final String DATABASE_URL = "https://hairstyleconsultant-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static AuthenticationManager instance;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private boolean isInitialized = false;

    private AuthenticationManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (isInitialized) {
            return;
        }

        try {
            // Initialize Firebase Auth first
            auth = FirebaseAuth.getInstance();
            
            // Enable offline persistence for Firebase Database
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            // Initialize Firebase Database with the correct URL
            database = FirebaseDatabase.getInstance(DATABASE_URL);
            
            // Set up security provider in background
            new Thread(() -> {
                try {
                    ProviderInstaller.installIfNeededAsync(context, new ProviderInstaller.ProviderInstallListener() {
                        @Override
                        public void onProviderInstalled() {
                            Log.d("AuthManager", "Security provider installed successfully");
                        }

                        @Override
                        public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
                            Log.e("AuthManager", "Security provider installation failed: " + errorCode);
                        }
                    });
                } catch (Exception e) {
                    Log.e("AuthManager", "Error installing security provider: " + e.getMessage());
                }
            }).start();

            isInitialized = true;
            Log.d("AuthManager", "AuthenticationManager initialized successfully");
        } catch (Exception e) {
            Log.e("AuthManager", "Error initializing AuthenticationManager: " + e.getMessage());
            throw new RuntimeException("Failed to initialize AuthenticationManager", e);
        }
    }

    public void logout() {
        if (!isInitialized) {
            Log.e("AuthManager", "Cannot logout: AuthenticationManager not initialized");
            return;
        }
        
        try {
            if (auth != null) {
                auth.signOut();
                Log.d("AuthManager", "User logged out successfully");
            } else {
                Log.e("AuthManager", "Cannot logout: FirebaseAuth is null");
            }
        } catch (Exception e) {
            Log.e("AuthManager", "Error during logout: " + e.getMessage());
        }
    }

    public FirebaseUser getCurrentUser() {
        if (!isInitialized) {
            Log.e("AuthManager", "Cannot get current user: AuthenticationManager not initialized");
            return null;
        }
        
        try {
            return auth != null ? auth.getCurrentUser() : null;
        } catch (Exception e) {
            Log.e("AuthManager", "Error getting current user: " + e.getMessage());
            return null;
        }
    }

    public void loginUser(String email, String password, OnAuthResultListener listener) {
        if (!isInitialized) {
            Log.e("AuthManager", "Cannot login: AuthenticationManager not initialized");
            if (listener != null) {
                listener.onFailure(new Exception("Authentication not initialized"));
            }
            return;
        }

        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d("AuthManager", "User logged in successfully: " + user.getUid());
                            if (listener != null) {
                                listener.onSuccess(user);
                            }
                        } else {
                            Log.e("AuthManager", "Login successful but user is null");
                            if (listener != null) {
                                listener.onFailure(new Exception("User is null after successful login"));
                            }
                        }
                    } else {
                        Log.e("AuthManager", "Login failed: " + task.getException().getMessage());
                        if (listener != null) {
                            listener.onFailure(task.getException());
                        }
                    }
                });
        } catch (Exception e) {
            Log.e("AuthManager", "Error during login: " + e.getMessage());
            if (listener != null) {
                listener.onFailure(e);
            }
        }
    }

    public void registerUser(Context context, String email, String password, String fullName, String phoneNumber, OnAuthCompleteListener listener) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot register user: AuthenticationManager not initialized");
            listener.onFailure("Authentication system not initialized");
            return;
        }

        Log.d(TAG, "Starting user registration process");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Full Name: " + fullName);
        Log.d(TAG, "Phone: " + phoneNumber);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Firebase Auth: User creation successful");
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "Firebase Auth: User ID: " + firebaseUser.getUid());
                            User user = new User(firebaseUser.getUid(), email, fullName, phoneNumber);
                            saveUserToDatabase(user, listener);
                        } else {
                            Log.e(TAG, "Firebase Auth: User is null after successful creation");
                            listener.onFailure("User creation failed: User object is null");
                        }
                    } else {
                        String errorMessage = "Registration failed";
                        if (task.getException() instanceof FirebaseAuthException) {
                            errorMessage = task.getException().getMessage();
                        }
                        Log.e(TAG, "Firebase Auth: Registration failed - " + errorMessage);
                        listener.onFailure(errorMessage);
                    }
                });
    }

    private void saveUserToDatabase(User user, OnAuthCompleteListener listener) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot save user: AuthenticationManager not initialized");
            listener.onFailure("Authentication system not initialized");
            return;
        }

        Log.d(TAG, "Saving user data to database");
        Log.d(TAG, "User ID: " + user.getUserId());
        Log.d(TAG, "Email: " + user.getEmail());
        Log.d(TAG, "Full Name: " + user.getFullName());
        Log.d(TAG, "Phone: " + user.getPhoneNumber());

        try {
            DatabaseReference userRef = database.getReference().child("users").child(user.getUserId());
            Log.d(TAG, "Database path: " + userRef.toString());

            userRef.setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        Log.i(TAG, "Database: User data saved successfully");
                        listener.onSuccess(auth.getCurrentUser());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Database: Failed to save user data", e);
                        Log.e(TAG, "Error message: " + e.getMessage());
                        Log.e(TAG, "Error cause: " + (e.getCause() != null ? e.getCause().getMessage() : "No cause"));
                        listener.onFailure("Failed to save user data: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception while setting up database operation", e);
            listener.onFailure("Database error: " + e.getMessage());
        }
    }

    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public interface OnAuthResultListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
} 