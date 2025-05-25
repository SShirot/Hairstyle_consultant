package com.example.hairstyle_consultant;

import android.os.Bundle;
import android.text.TextUtils;
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

        authManager = new AuthenticationManager(this);
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHairInfo();
            }
        });
    }

    private void saveHairInfo() {
        String hairStyle = hairStyleInput.getText().toString().trim();
        String hairQuality = hairQualityInput.getText().toString().trim();
        String hairLength = hairLengthInput.getText().toString().trim();
        String hairColor = hairColorInput.getText().toString().trim();
        String hairTexture = hairTextureInput.getText().toString().trim();
        String hairConcerns = hairConcernsInput.getText().toString().trim();

        userRef.child("hairStyle").setValue(hairStyle);
        userRef.child("hairQuality").setValue(hairQuality);
        userRef.child("hairLength").setValue(hairLength);
        userRef.child("hairColor").setValue(hairColor);
        userRef.child("hairTexture").setValue(hairTexture);
        userRef.child("hairConcerns").setValue(hairConcerns);

        Toast.makeText(this, "Hair information saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
} 