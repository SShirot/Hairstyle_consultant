package com.example.hairstyle_consultant;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        MaterialButton chatButton = findViewById(R.id.chatButton);
        MaterialButton addProductButton = findViewById(R.id.addProductButton);

        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        addProductButton.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }
} 