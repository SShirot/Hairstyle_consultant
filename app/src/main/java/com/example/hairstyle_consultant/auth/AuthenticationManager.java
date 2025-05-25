package com.example.hairstyle_consultant.auth;

import android.app.Activity;
import android.content.Context;
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
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Context context;
    private boolean isInitialized = false;

    public AuthenticationManager(Context context) {
        this.context = context;
        initialize();
    }

    private void initialize() {
        try {
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            
            // Enable persistence BEFORE getting database instance
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            // Initialize Firebase Database with the correct region URL
            FirebaseDatabase database = FirebaseDatabase.getInstance(DATABASE_URL);
            mDatabase = database.getReference();
            mDatabase.keepSynced(true);
            
            // Initialize security provider in background
            new Thread(() -> {
                try {
                    ProviderInstaller.installIfNeeded(context);
                    Log.d(TAG, "Security provider installed successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Error installing security provider", e);
                }
            }).start();
            
            isInitialized = true;
            Log.d(TAG, "AuthenticationManager initialized");
            Log.d(TAG, "Database URL: " + DATABASE_URL);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AuthenticationManager", e);
            isInitialized = false;
        }
    }

    public FirebaseUser getCurrentUser() {
        if (!isInitialized) {
            Log.w(TAG, "AuthenticationManager not initialized");
            return null;
        }
        try {
            return mAuth.getCurrentUser();
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user", e);
            return null;
        }
    }

    public void registerUser(String email, String password, String fullName, String phoneNumber, OnAuthCompleteListener listener) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot register user: AuthenticationManager not initialized");
            listener.onFailure("Authentication system not initialized");
            return;
        }

        Log.d(TAG, "Starting user registration process");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Full Name: " + fullName);
        Log.d(TAG, "Phone: " + phoneNumber);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Firebase Auth: User creation successful");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
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
            DatabaseReference userRef = mDatabase.child("users").child(user.getUserId());
            Log.d(TAG, "Database path: " + userRef.toString());

            userRef.setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        Log.i(TAG, "Database: User data saved successfully");
                        listener.onSuccess(mAuth.getCurrentUser());
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

    public void loginUser(String email, String password, OnAuthCompleteListener listener) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot login: AuthenticationManager not initialized");
            listener.onFailure("Authentication system not initialized");
            return;
        }

        Log.d(TAG, "Starting login process");
        Log.d(TAG, "Email: " + email);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Login successful");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "User ID: " + user.getUid());
                            listener.onSuccess(user);
                        } else {
                            Log.e(TAG, "User is null after successful login");
                            listener.onFailure("Login failed: User object is null");
                        }
                    } else {
                        String errorMessage = "Login failed";
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Invalid email or password";
                        } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            errorMessage = "No account found with this email";
                        } else if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Log.e(TAG, "Login failed - " + errorMessage);
                        listener.onFailure(errorMessage);
                    }
                });
    }

    public void signOut() {
        if (!isInitialized) {
            Log.e(TAG, "Cannot sign out: AuthenticationManager not initialized");
            return;
        }
        Log.d(TAG, "Signing out user");
        mAuth.signOut();
    }

    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }
} 