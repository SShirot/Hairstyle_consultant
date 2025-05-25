package com.example.hairstyle_consultant;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hairstyle_consultant.auth.AuthenticationManager;
import com.example.hairstyle_consultant.models.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.button.MaterialButton;

public class HairInfoActivity extends AppCompatActivity {
    private static final String TAG = "HairInfoActivity";
    private EditText hairStyleInput, hairQualityInput, hairLengthInput, hairColorInput, hairTextureInput, hairConcernsInput;
    private MaterialButton saveButton;
    private AuthenticationManager authManager;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hair_info);

        hairStyleInput = findViewById(R.id.hairStyleInput);
        hairQualityInput = findViewById(R.id.hairQualityInput);
        hairLengthInput = findViewById(R.id.hairLengthInput);
        hairColorInput = findViewById(R.id.hairColorInput);
        hairTextureInput = findViewById(R.id.hairTextureInput);
        hairConcernsInput = findViewById(R.id.hairConcernsInput);
        saveButton = findViewById(R.id.saveHairInfoButton);

        authManager = AuthenticationManager.getInstance();
        authManager.initialize(this);

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userRef = FirebaseDatabase.getInstance("https://hairstyleconsultant-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users")
                .child(currentUser.getUid());

        loadExistingHairInfo();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveHairInfo();
                }
            }
        });
    }

    private void loadExistingHairInfo() {
        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        hairStyleInput.setText(user.getHairStyle());
                        hairQualityInput.setText(user.getHairQuality());
                        hairLengthInput.setText(user.getHairLength());
                        hairColorInput.setText(user.getHairColor());
                        hairTextureInput.setText(user.getHairTexture());
                        hairConcernsInput.setText(user.getHairConcerns());
                    }
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                Log.e(TAG, "Error loading hair info: " + databaseError.getMessage());
                Toast.makeText(HairInfoActivity.this, "Error loading hair information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String errorMessage = "";

        String hairStyle = hairStyleInput.getText().toString().trim();
        if (TextUtils.isEmpty(hairStyle)) {
            hairStyleInput.setError("Hair style is required");
            isValid = false;
        } else if (hairStyle.length() < 2) {
            hairStyleInput.setError("Hair style must be at least 2 characters");
            isValid = false;
        }

        String hairQuality = hairQualityInput.getText().toString().trim();
        if (TextUtils.isEmpty(hairQuality)) {
            hairQualityInput.setError("Hair quality is required");
            isValid = false;
        }

        String hairLength = hairLengthInput.getText().toString().trim();
        if (TextUtils.isEmpty(hairLength)) {
            hairLengthInput.setError("Hair length is required");
            isValid = false;
        }

        String hairColor = hairColorInput.getText().toString().trim();
        if (TextUtils.isEmpty(hairColor)) {
            hairColorInput.setError("Hair color is required");
            isValid = false;
        }

        String hairTexture = hairTextureInput.getText().toString().trim();
        if (TextUtils.isEmpty(hairTexture)) {
            hairTextureInput.setError("Hair texture is required");
            isValid = false;
        }

        String hairConcerns = hairConcernsInput.getText().toString().trim();
        if (!TextUtils.isEmpty(hairConcerns) && hairConcerns.length() < 3) {
            hairConcernsInput.setError("If provided, concerns should be at least 3 characters");
            isValid = false;
        }

        if (!isValid) {
            Toast.makeText(this, "Please fill in all required fields correctly", Toast.LENGTH_LONG).show();
        }

        return isValid;
    }

    private void saveHairInfo() {
        if (!validateInputs()) {
            return;
        }

        String hairStyle = hairStyleInput.getText().toString().trim();
        String hairQuality = hairQualityInput.getText().toString().trim();
        String hairLength = hairLengthInput.getText().toString().trim();
        String hairColor = hairColorInput.getText().toString().trim();
        String hairTexture = hairTextureInput.getText().toString().trim();
        String hairConcerns = hairConcernsInput.getText().toString().trim();

        // Show loading indicator
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        authManager.updateHairInfo(hairStyle, hairQuality, hairLength, hairColor, hairTexture, hairConcerns,
                new AuthenticationManager.OnUserDataListener() {
                    @Override
                    public void onSuccess(User user) {
                        runOnUiThread(() -> {
                            saveButton.setEnabled(true);
                            saveButton.setText("Save");
                            Toast.makeText(HairInfoActivity.this, "Hair information saved successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            saveButton.setEnabled(true);
                            saveButton.setText("Save");
                            Toast.makeText(HairInfoActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }
} 