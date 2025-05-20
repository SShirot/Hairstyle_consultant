package com.example.hairstyle_consultant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairstyle_consultant.adapters.ProductAdapter;
import com.example.hairstyle_consultant.models.Product;
import com.example.hairstyle_consultant.services.ProductService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> products;
    private ProductService productService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        // Initialize back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageProductsActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });

        // Initialize views
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        FloatingActionButton addProductFab = findViewById(R.id.addProductFab);

        // Initialize services
        productService = new ProductService();

        // Initialize products list and adapter
        products = new ArrayList<>();
        productAdapter = new ProductAdapter(products, this::onEditProduct, this::onDeleteProduct);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsRecyclerView.setAdapter(productAdapter);

        // Set up add product button
        addProductFab.setOnClickListener(v -> {
            Intent intent = new Intent(ManageProductsActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        // Load products
        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // Reload products when returning to this activity
    }

    private void loadProducts() {
        productService.getAllProducts()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                products.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    product.setId(document.getId()); // Set the document ID
                    products.add(product);
                }
                productAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void onEditProduct(Product product) {
        Intent intent = new Intent(this, AddProductActivity.class);
        intent.putExtra("productId", product.getId());
        intent.putExtra("productName", product.getName());
        intent.putExtra("productDescription", product.getDescription());
        intent.putExtra("productPrice", product.getPrice());
        intent.putExtra("productStock", product.getStockAmount());
        intent.putExtra("productCategory", product.getCategory());
        intent.putExtra("productAvailable", product.isAvailable());
        startActivity(intent);
    }

    private void onDeleteProduct(Product product) {
        productService.deleteProduct(product.getId())
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                loadProducts(); // Reload the list
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error deleting product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
} 