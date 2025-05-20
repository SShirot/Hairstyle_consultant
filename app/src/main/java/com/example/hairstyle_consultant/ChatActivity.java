package com.example.hairstyle_consultant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairstyle_consultant.models.Product;
import com.example.hairstyle_consultant.services.ProductService;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private GenerativeModelFutures model;
    private ProductService productService;
    private String allProductsInfo;
    private static final String SYSTEM_PROMPT = "You are an expert hairstyle consultant AI assistant. Your role is to provide personalized hairstyle recommendations and advice.";
    private static final String QUERY_ANALYSIS_PROMPT = "Analyze if the following user query is asking about products or services. " +
            "Respond with only 'YES' if the query is about products/services, or 'NO' if it's about hairstyle advice or general questions. " +
            "Query: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Initialize services
        productService = new ProductService();
        GenerativeModel generativeModel = new GenerativeModel("gemini-1.5-flash", "AIzaSyAbYhs_o8XzFvf1TfnCUtnxIS-x11BblpI");
        model = GenerativeModelFutures.from(generativeModel);

        // Initialize chat
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Load all products first
        loadAllProducts();
    }

    private void loadAllProducts() {
        productService.getAllProducts()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                StringBuilder productInfo = new StringBuilder("Available Products:\n\n");
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    productInfo.append("Name: ").append(product.getName())
                            .append("\nBrand: ").append(product.getBrand())
                            .append("\nDescription: ").append(product.getDescription())
                            .append("\nPrice: $").append(String.format("%.2f", product.getPrice()))
                            .append("\nStock Amount: ").append(product.getStockAmount())
                            .append("\nCategory: ").append(product.getCategory())
                            .append("\nAvailable: ").append(product.isAvailable() ? "Yes" : "No")
                            .append("\nImage URL: ").append(product.getImageUrl())
                            .append("\n\n");
                }
                allProductsInfo = productInfo.toString();
                
                // Add welcome message with product availability
                String welcomeMessage = "Hello! I'm your AI hairstyle consultant. I can help you find the perfect hairstyle based on your face shape, hair type, and preferences. " +
                        "I also have information about our products and can help you find the right one for your needs. What would you like to know?";
                messages.add(new ChatMessage(welcomeMessage, false));
                chatAdapter.notifyDataSetChanged();

                // Set up send button click listener
                sendButton.setOnClickListener(v -> {
                    String message = messageInput.getText().toString().trim();
                    if (!message.isEmpty()) {
                        sendMessage(message);
                        messageInput.setText("");
                    }
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Still show welcome message even if products fail to load
                messages.add(new ChatMessage("Hello! I'm your AI hairstyle consultant. I can help you find the perfect hairstyle based on your face shape, hair type, and preferences. What would you like to know?", false));
                chatAdapter.notifyDataSetChanged();
            });
    }

    private void sendMessage(String message) {
        // Add user message to chat
        messages.add(new ChatMessage(message, true));
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.smoothScrollToPosition(messages.size() - 1);

        // Analyze if the query is about products
        analyzeQuery(message);
    }

    private void analyzeQuery(String query) {
        Content content = new Content.Builder()
            .addText(QUERY_ANALYSIS_PROMPT + query)
            .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String analysis = result.getText().trim();
                if (analysis.equalsIgnoreCase("YES")) {
                    getAIResponse(query, allProductsInfo);
                } else {
                    getAIResponse(query, null);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Error analyzing query: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    getAIResponse(query, null);
                });
            }
        }, Executors.newSingleThreadExecutor());
    }

    private void getAIResponse(String userMessage, String productInfo) {
        StringBuilder prompt = new StringBuilder(SYSTEM_PROMPT);
        if (productInfo != null) {
            prompt.append("\n\nProduct Information:\n").append(productInfo);
        }
        prompt.append("\n\nUser: ").append(userMessage);

        Content content = new Content.Builder()
            .addText(prompt.toString())
            .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiResponse = result.getText();
                runOnUiThread(() -> {
                    messages.add(new ChatMessage(aiResponse, false));
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }, Executors.newSingleThreadExecutor());
    }
} 