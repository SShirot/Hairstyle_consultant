package com.example.hairstyle_consultant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairstyle_consultant.auth.AuthenticationManager;
import com.example.hairstyle_consultant.models.Product;
import com.example.hairstyle_consultant.models.User;
import com.example.hairstyle_consultant.services.ProductService;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private GenerativeModelFutures model;
    private ProductService productService;
    private String allProductsInfo;
    private AuthenticationManager authManager;
    private DatabaseReference userRef;
    private User currentUser;

    private static final String SYSTEM_PROMPT = "Bạn là một chuyên gia tư vấn tóc thân thiện và chuyên nghiệp. " +
            "Hãy trả lời ngắn gọn, súc tích bằng tiếng Việt. " +
            "Khi tư vấn, hãy cân nhắc các yếu tố sau:\n\n" +
            "1. Thông tin tóc của người dùng:\n%s\n\n" +
            "2. Danh sách sản phẩm có sẵn:\n%s\n\n" +
            "3. Nguyên tắc tư vấn:\n" +
            "- Luôn đề cập đến tên người dùng khi trả lời\n" +
            "- Đưa ra lời khuyên dựa trên tình trạng tóc hiện tại\n" +
            "- Chỉ đề xuất sản phẩm có trong danh sách\n" +
            "- Giải thích lý do tại sao sản phẩm phù hợp với tóc của họ\n" +
            "- Nếu không có sản phẩm phù hợp, hãy nói rõ và đề xuất giải pháp thay thế\n" +
            "- Luôn thân thiện và chuyên nghiệp trong cách trả lời";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

        // Initialize AuthenticationManager
        authManager = AuthenticationManager.getInstance();
        authManager.initialize(this);

        // Initialize chat
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Get current user and initialize database reference
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance("https://hairstyleconsultant-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users")
                .child(currentUser.getUid());

        // Load all products first
        loadAllProducts();
    }

    private void loadAllProducts() {
        productService.getAllProducts()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                StringBuilder productInfo = new StringBuilder();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    productInfo.append("- ").append(product.getName())
                            .append(" (").append(product.getBrand()).append(")\n")
                            .append("  Mô tả: ").append(product.getDescription()).append("\n")
                            .append("  Giá: ").append(String.format("%.0f", product.getPrice())).append(" VNĐ\n")
                            .append("  Danh mục: ").append(product.getCategory()).append("\n")
                            .append("  Tình trạng: ").append(product.isAvailable() ? "Còn hàng" : "Hết hàng").append("\n\n");
                }
                allProductsInfo = productInfo.toString();
                
                // Load user data from Realtime Database
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            currentUser = dataSnapshot.getValue(User.class);
                            if (currentUser != null) {
                                String userInfo = String.format(
                                    "Tên: %s\n" +
                                    "Kiểu tóc: %s\n" +
                                    "Chất lượng tóc: %s\n" +
                                    "Độ dài tóc: %s\n" +
                                    "Màu tóc: %s\n" +
                                    "Kết cấu tóc: %s\n" +
                                    "Vấn đề tóc: %s",
                                    currentUser.getFullName(),
                                    currentUser.getHairStyle(),
                                    currentUser.getHairQuality(),
                                    currentUser.getHairLength(),
                                    currentUser.getHairColor(),
                                    currentUser.getHairTexture(),
                                    currentUser.getHairConcerns()
                                );
                                
                                // Add personalized welcome message
                                String welcomeMessage = String.format(
                                    "Xin chào %s! 👋\n\n" +
                                    "Tôi là trợ lý tư vấn tóc AI của bạn. Dựa trên thông tin tóc của bạn:\n" +
                                    "- Kiểu tóc: %s\n" +
                                    "- Chất lượng: %s\n" +
                                    "- Độ dài: %s\n" +
                                    "- Màu sắc: %s\n" +
                                    "- Kết cấu: %s\n" +
                                    "- Vấn đề: %s\n\n" +
                                    "Tôi có thể giúp bạn:\n" +
                                    "1. Tư vấn kiểu tóc phù hợp\n" +
                                    "2. Đề xuất sản phẩm chăm sóc tóc\n" +
                                    "3. Giải đáp thắc mắc về tóc\n\n" +
                                    "Bạn muốn được tư vấn về vấn đề gì?",
                                    currentUser.getFullName(),
                                    currentUser.getHairStyle(),
                                    currentUser.getHairQuality(),
                                    currentUser.getHairLength(),
                                    currentUser.getHairColor(),
                                    currentUser.getHairTexture(),
                                    currentUser.getHairConcerns()
                                );
                                
                                messages.add(new ChatMessage(welcomeMessage, false));
                                chatAdapter.notifyDataSetChanged();

                                // Set up send button click listener
                                sendButton.setOnClickListener(v -> {
                                    String message = messageInput.getText().toString().trim();
                                    if (!message.isEmpty()) {
                                        sendMessage(message, userInfo);
                                        messageInput.setText("");
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error loading user data: " + databaseError.getMessage());
                        messages.add(new ChatMessage("Xin chào! Tôi là trợ lý tư vấn tóc AI của bạn. Tôi có thể giúp bạn tìm kiếm kiểu tóc phù hợp. Bạn muốn biết thêm thông tin gì?", false));
                        chatAdapter.notifyDataSetChanged();
                    }
                });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(ChatActivity.this, "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                messages.add(new ChatMessage("Xin chào! Tôi là trợ lý tư vấn tóc AI của bạn. Tôi có thể giúp bạn tìm kiếm kiểu tóc phù hợp. Bạn muốn biết thêm thông tin gì?", false));
                chatAdapter.notifyDataSetChanged();
            });
    }

    private void sendMessage(String message, String userInfo) {
        // Add user message to chat
        messages.add(new ChatMessage(message, true));
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.smoothScrollToPosition(messages.size() - 1);

        // Get AI response with personalized context
        getAIResponse(message, userInfo);
    }

    private void getAIResponse(String userMessage, String userInfo) {
        StringBuilder prompt = new StringBuilder(String.format(SYSTEM_PROMPT, 
            userInfo,
            allProductsInfo
        ));
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