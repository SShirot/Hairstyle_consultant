package com.example.hairstyle_consultant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hairstyle_consultant.auth.AuthenticationManager;
import com.example.hairstyle_consultant.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final String DATABASE_URL = "https://hairstyleconsultant-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private AuthenticationManager authManager;
    private TextView nameText, emailText, phoneText, roleText;
    private TextView hairStyleText, hairQualityText, hairLengthText, hairColorText, hairTextureText, hairConcernsText;
    private MaterialButton editHairInfoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize AuthenticationManager
        authManager = AuthenticationManager.getInstance();
        authManager.initialize(this);

        // Initialize views
        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadUserData();
    }

    private void initializeViews() {
        // Initialize toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize TextViews
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        phoneText = findViewById(R.id.phoneText);
        roleText = findViewById(R.id.roleText);
        hairStyleText = findViewById(R.id.hairStyleText);
        hairQualityText = findViewById(R.id.hairQualityText);
        hairLengthText = findViewById(R.id.hairLengthText);
        hairColorText = findViewById(R.id.hairColorText);
        hairTextureText = findViewById(R.id.hairTextureText);
        hairConcernsText = findViewById(R.id.hairConcernsText);

        // Initialize button
        editHairInfoButton = findViewById(R.id.editHairInfoButton);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        editHairInfoButton.setOnClickListener(v -> {
            // Launch HairInfoActivity for editing
            startActivity(new Intent(ProfileActivity.this, HairInfoActivity.class));
        });
    }

    private void loadUserData() {
        if (authManager.getCurrentUser() == null) {
            Log.e(TAG, "Current user is null");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get user data from AuthenticationManager
        User userData = authManager.getCurrentUserData();
        if (userData != null) {
            Log.d(TAG, "Using cached user data");
            updateUI(userData);
        } else {
            Log.d(TAG, "No cached data, loading from database");
            String userId = authManager.getCurrentUser().getUid();
            authManager.getUserData(userId, new AuthenticationManager.OnUserDataListener() {
                @Override
                public void onSuccess(User user) {
                    updateUI(user);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "Error loading user data: " + error);
                    Toast.makeText(ProfileActivity.this, "Error loading user data: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateUI(User user) {
        try {
            // Update personal information
            nameText.setText("Name: " + (user.getFullName() != null ? user.getFullName() : "Not set"));
            emailText.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "Not set"));
            phoneText.setText("Phone: " + (user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not set"));
            roleText.setText("Role: " + (user.getRole() != null ? user.getRole() : "Not set"));

            // Update hair information
            hairStyleText.setText("Hair Style: " + (user.getHairStyle() != null ? user.getHairStyle() : "Not set"));
            hairQualityText.setText("Hair Quality: " + (user.getHairQuality() != null ? user.getHairQuality() : "Not set"));
            hairLengthText.setText("Hair Length: " + (user.getHairLength() != null ? user.getHairLength() : "Not set"));
            hairColorText.setText("Hair Color: " + (user.getHairColor() != null ? user.getHairColor() : "Not set"));
            hairTextureText.setText("Hair Texture: " + (user.getHairTexture() != null ? user.getHairTexture() : "Not set"));
            hairConcernsText.setText("Hair Concerns: " + (user.getHairConcerns() != null ? user.getHairConcerns() : "Not set"));

            Log.d(TAG, "UI updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage());
            Toast.makeText(this, "Error displaying user data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when returning from HairInfoActivity
        loadUserData();
    }
} 