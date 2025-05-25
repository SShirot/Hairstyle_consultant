package com.example.hairstyle_consultant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hairstyle_consultant.auth.AuthenticationManager;
import com.example.hairstyle_consultant.models.User;
import com.example.hairstyle_consultant.utils.DatabaseInitializer;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AuthenticationManager authManager;
    private TextView welcomeText;
    private Button chatButton;
    private Button productsButton;
    private Button profileButton;
    private Button logoutButton;
    private Button hairInfoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize AuthenticationManager
        authManager = AuthenticationManager.getInstance();
        authManager.initialize(this);

        // Check if user is logged in
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            // If not logged in, redirect to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        setupClickListeners();
        loadUserData(currentUser);

        // Initialize the product database with sample data
        DatabaseInitializer databaseInitializer = new DatabaseInitializer();
        databaseInitializer.initializeProducts();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        chatButton = findViewById(R.id.chatButton);
        productsButton = findViewById(R.id.productsButton);
        profileButton = findViewById(R.id.profileButton);
        logoutButton = findViewById(R.id.logoutButton);
        hairInfoButton = findViewById(R.id.hairInfoButton);
    }

    private void loadUserData(FirebaseUser currentUser) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        welcomeText.setText("Welcome, " + user.getFullName());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error loading user data", databaseError.toException());
                Toast.makeText(MainActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        chatButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ChatActivity.class));
        });

        productsButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ManageProductsActivity.class));
        });

        profileButton.setOnClickListener(v -> {
            // TODO: Implement profile activity
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        hairInfoButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HairInfoActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            authManager.logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // TODO: Implement settings
            Toast.makeText(this, "Settings feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}