package com.example.hairstyle_consultant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hairstyle_consultant.auth.AuthenticationManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    private TextInputEditText emailInput, passwordInput, confirmPasswordInput, fullNameInput, phoneNumberInput;
    private TextInputLayout emailLayout, passwordLayout, confirmPasswordLayout, fullNameLayout, phoneNumberLayout;
    private MaterialButton registerButton;
    private AuthenticationManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting registration activity");
        try {
            setContentView(R.layout.activity_register);

            // Initialize back button
            ImageButton backButton = findViewById(R.id.backButton);
            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked");
                finish();
            });

            // Initialize AuthenticationManager
            authManager = new AuthenticationManager(this);
            Log.d(TAG, "AuthenticationManager initialized");

            // Initialize views
            initializeViews();
            setupRegisterButton();
            Log.d(TAG, "Views initialized and setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing registration: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            emailInput = findViewById(R.id.emailEditText);
            passwordInput = findViewById(R.id.passwordEditText);
            confirmPasswordInput = findViewById(R.id.confirmPasswordEditText);
            fullNameInput = findViewById(R.id.fullNameEditText);
            phoneNumberInput = findViewById(R.id.phoneNumberEditText);
            registerButton = findViewById(R.id.registerButton);

            emailLayout = findViewById(R.id.emailLayout);
            passwordLayout = findViewById(R.id.passwordLayout);
            confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
            fullNameLayout = findViewById(R.id.fullNameLayout);
            phoneNumberLayout = findViewById(R.id.phoneNumberLayout);
            Log.d(TAG, "All views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }

    private void setupRegisterButton() {
        registerButton.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            try {
                clearErrors();
                if (validateInputs()) {
                    Log.d(TAG, "Input validation successful, proceeding with registration");
                    registerButton.setEnabled(false);
                    registerUser();
                } else {
                    Log.d(TAG, "Input validation failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in register button click: " + e.getMessage(), e);
                registerButton.setEnabled(true);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        fullNameLayout.setError(null);
        phoneNumberLayout.setError(null);
    }

    private boolean validateInputs() {
        Log.d(TAG, "Starting input validation");
        boolean isValid = true;
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String fullName = fullNameInput.getText().toString().trim();
        String phoneNumber = phoneNumberInput.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            Log.d(TAG, "Email validation failed: empty email");
            emailLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d(TAG, "Email validation failed: invalid email format");
            emailLayout.setError("Please enter a valid email address");
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            Log.d(TAG, "Password validation failed: empty password");
            passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            Log.d(TAG, "Password validation failed: too short");
            passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*")) {
            Log.d(TAG, "Password validation failed: no uppercase");
            passwordLayout.setError("Password must contain at least one uppercase letter");
            isValid = false;
        } else if (!password.matches(".*[a-z].*")) {
            Log.d(TAG, "Password validation failed: no lowercase");
            passwordLayout.setError("Password must contain at least one lowercase letter");
            isValid = false;
        } else if (!password.matches(".*\\d.*")) {
            Log.d(TAG, "Password validation failed: no number");
            passwordLayout.setError("Password must contain at least one number");
            isValid = false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            Log.d(TAG, "Confirm password validation failed: empty");
            confirmPasswordLayout.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            Log.d(TAG, "Confirm password validation failed: passwords don't match");
            confirmPasswordLayout.setError("Passwords do not match");
            isValid = false;
        }

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            Log.d(TAG, "Full name validation failed: empty");
            fullNameLayout.setError("Full name is required");
            isValid = false;
        } else if (fullName.length() < 2) {
            Log.d(TAG, "Full name validation failed: too short");
            fullNameLayout.setError("Full name must be at least 2 characters");
            isValid = false;
        }

        // Validate phone number
        if (TextUtils.isEmpty(phoneNumber)) {
            Log.d(TAG, "Phone number validation failed: empty");
            phoneNumberLayout.setError("Phone number is required");
            isValid = false;
        } else if (!phoneNumber.matches("^[+]?[0-9]{10,13}$")) {
            Log.d(TAG, "Phone number validation failed: invalid format");
            phoneNumberLayout.setError("Please enter a valid phone number");
            isValid = false;
        }

        Log.d(TAG, "Input validation completed. Valid: " + isValid);
        return isValid;
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String fullName = fullNameInput.getText().toString().trim();
        String phoneNumber = phoneNumberInput.getText().toString().trim();

        Log.d(TAG, "Starting user registration process");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Full Name: " + fullName);
        Log.d(TAG, "Phone: " + phoneNumber);

        try {
            authManager.registerUser(email, password, fullName, phoneNumber, new AuthenticationManager.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Log.i(TAG, "User registration successful");
                    Log.i(TAG, "User UID: " + user.getUid());
                    Log.i(TAG, "User Email: " + user.getEmail());
                    
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    
                    try {
                        // Navigate to main activity
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Log.d(TAG, "Navigation to MainActivity successful");
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error navigating to MainActivity: " + e.getMessage(), e);
                        Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Registration failed: " + errorMessage);
                    registerButton.setEnabled(true);
                    
                    if (errorMessage.contains("email address is already in use")) {
                        Log.d(TAG, "Email already in use error");
                        emailLayout.setError("This email is already registered");
                    } else if (errorMessage.contains("badly formatted")) {
                        Log.d(TAG, "Invalid email format error");
                        emailLayout.setError("Invalid email format");
                    } else if (errorMessage.contains("password")) {
                        Log.d(TAG, "Invalid password error");
                        passwordLayout.setError("Invalid password format");
                    } else {
                        Log.e(TAG, "Unknown registration error: " + errorMessage);
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in registerUser: " + e.getMessage(), e);
            registerButton.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RegisterActivity destroyed");
    }
} 