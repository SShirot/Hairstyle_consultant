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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

public class AuthenticationManager {
    private static final String TAG = "AuthenticationManager";
    private static final String DATABASE_URL = "https://hairstyleconsultant-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static AuthenticationManager instance;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private boolean isInitialized = false;
    private User currentUserData; // Store current user data

    public AuthenticationManager() {
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
                            // Get user data from database
                            getUserData(user.getUid(), new OnUserDataListener() {
                                @Override
                                public void onSuccess(User userData) {
                                    currentUserData = userData;
                                    if (listener != null) {
                                        listener.onSuccess(user);
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e("AuthManager", "Failed to get user data: " + error);
                                    if (listener != null) {
                                        listener.onFailure(new Exception("Failed to get user data: " + error));
                                    }
                                }
                            });
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

    public void getUserData(String userId, OnUserDataListener listener) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot get user data: AuthenticationManager not initialized");
            listener.onFailure("Authentication system not initialized");
            return;
        }

        try {
            DatabaseReference userRef = database.getReference().child("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            listener.onSuccess(user);
                        } else {
                            listener.onFailure("Failed to parse user data");
                        }
                    } else {
                        listener.onFailure("User data not found");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error getting user data: " + databaseError.getMessage());
                    listener.onFailure("Database error: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception while getting user data: " + e.getMessage());
            listener.onFailure("Error: " + e.getMessage());
        }
    }

    public User getCurrentUserData() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        // TODO: Implement caching mechanism for user data
        return null;
    }

    public void logout() {
        if (!isInitialized) {
            Log.e("AuthManager", "Cannot logout: AuthenticationManager not initialized");
            return;
        }
        
        try {
            if (auth != null) {
                auth.signOut();
                currentUserData = null; // Clear user data on logout
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
                            user.setRole("user");
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
        Log.d(TAG, "Role: " + user.getRole());

        try {
            DatabaseReference userRef = database.getReference().child("users").child(user.getUserId());
            Log.d(TAG, "Database path: " + userRef.toString());

            // Create a map with all user data including null hair information
            java.util.Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());
            userData.put("phoneNumber", user.getPhoneNumber());
            userData.put("role", user.getRole());
            userData.put("hairStyle", null);
            userData.put("hairQuality", null);
            userData.put("hairLength", null);
            userData.put("hairColor", null);
            userData.put("hairTexture", null);
            userData.put("hairConcerns", null);

            userRef.setValue(userData)
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

    public interface OnUserDataListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public interface OnAuthResultListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }

    public void updateUserData(User updatedUser, OnUserDataListener listener) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot update user data: AuthenticationManager not initialized");
            listener.onFailure("Authentication system not initialized");
            return;
        }

        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot update user data: No user logged in");
            listener.onFailure("No user logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Updating user data for ID: " + userId);

        // Create a map with all user data
        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("userId", updatedUser.getUserId());
        userData.put("email", updatedUser.getEmail());
        userData.put("fullName", updatedUser.getFullName());
        userData.put("phoneNumber", updatedUser.getPhoneNumber());
        userData.put("role", updatedUser.getRole());
        userData.put("hairStyle", updatedUser.getHairStyle());
        userData.put("hairQuality", updatedUser.getHairQuality());
        userData.put("hairLength", updatedUser.getHairLength());
        userData.put("hairColor", updatedUser.getHairColor());
        userData.put("hairTexture", updatedUser.getHairTexture());
        userData.put("hairConcerns", updatedUser.getHairConcerns());

        DatabaseReference userRef = database.getReference("users").child(userId);
        userRef.updateChildren(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data updated successfully");
                    // Update cached data
                    currentUserData = updatedUser;
                    listener.onSuccess(updatedUser);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating user data: " + e.getMessage());
                    listener.onFailure("Failed to update user data: " + e.getMessage());
                });
    }

    public void updateHairInfo(String hairStyle, String hairQuality, String hairLength, 
                             String hairColor, String hairTexture, String hairConcerns, 
                             OnUserDataListener listener) {
        if (!isInitialized || auth.getCurrentUser() == null) {
            listener.onFailure("Authentication system not initialized or no user logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Updating hair info for user ID: " + userId);

        // Create a map with hair information
        java.util.Map<String, Object> hairData = new java.util.HashMap<>();
        hairData.put("hairStyle", hairStyle);
        hairData.put("hairQuality", hairQuality);
        hairData.put("hairLength", hairLength);
        hairData.put("hairColor", hairColor);
        hairData.put("hairTexture", hairTexture);
        hairData.put("hairConcerns", hairConcerns);

        DatabaseReference userRef = database.getReference("users").child(userId);
        userRef.updateChildren(hairData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Hair info updated successfully");
                    // Update cached data
                    if (currentUserData != null) {
                        currentUserData.setHairStyle(hairStyle);
                        currentUserData.setHairQuality(hairQuality);
                        currentUserData.setHairLength(hairLength);
                        currentUserData.setHairColor(hairColor);
                        currentUserData.setHairTexture(hairTexture);
                        currentUserData.setHairConcerns(hairConcerns);
                    }
                    listener.onSuccess(currentUserData);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating hair info: " + e.getMessage());
                    listener.onFailure("Failed to update hair info: " + e.getMessage());
                });
    }
} 