package com.example.hairstyle_consultant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hairstyle_consultant.models.Product;
import com.example.hairstyle_consultant.services.ProductService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class AddProductActivity extends AppCompatActivity {
    private TextInputEditText nameInput, descriptionInput, priceInput, stockInput;
    private AutoCompleteTextView categoryInput;
    private SwitchMaterial availableSwitch;
    private MaterialButton saveButton;
    private ProductService productService;
    private String productId;

    private final String[] categories = {
        "Shampoo",
        "Conditioner",
        "Styling",
        "Treatment",
        "Tools",
        "Accessories"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        // Initialize ProductService
        productService = new ProductService();

        // Initialize views
        initializeViews();
        setupCategoryDropdown();
        setupSaveButton();

        // Check if we're editing an existing product
        productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            loadProductData();
        }
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.productNameInput);
        descriptionInput = findViewById(R.id.productDescriptionInput);
        priceInput = findViewById(R.id.productPriceInput);
        stockInput = findViewById(R.id.productStockInput);
        categoryInput = findViewById(R.id.productCategoryInput);
        availableSwitch = findViewById(R.id.productAvailableSwitch);
        saveButton = findViewById(R.id.saveProductButton);
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categories
        );
        categoryInput.setAdapter(adapter);
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> saveProduct());
    }

    private void loadProductData() {
        productService.getProductById(productId)
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Product product = documentSnapshot.toObject(Product.class);
                    if (product != null) {
                        // Populate the form with product data
                        nameInput.setText(product.getName());
                        descriptionInput.setText(product.getDescription());
                        priceInput.setText(String.valueOf(product.getPrice()));
                        stockInput.setText(String.valueOf(product.getStockAmount()));
                        categoryInput.setText(product.getCategory(), false);
                        availableSwitch.setChecked(product.isAvailable());
                    }
                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void saveProduct() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Create product object
        Product product = new Product(
            productId, // Will be null for new products
            nameInput.getText().toString().trim(),
            descriptionInput.getText().toString().trim(),
            Double.parseDouble(priceInput.getText().toString().trim()),
            Integer.parseInt(stockInput.getText().toString().trim()),
            categoryInput.getText().toString().trim(),
            "", // Empty brand
            "", // Empty image URL
            availableSwitch.isChecked()
        );

        if (productId != null) {
            // Update existing product
            productService.updateProduct(productId, product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            // Add new product
            productService.addProduct(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate name
        if (TextUtils.isEmpty(nameInput.getText())) {
            nameInput.setError("Name is required");
            isValid = false;
        }

        // Validate description
        if (TextUtils.isEmpty(descriptionInput.getText())) {
            descriptionInput.setError("Description is required");
            isValid = false;
        }

        // Validate price
        if (TextUtils.isEmpty(priceInput.getText())) {
            priceInput.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceInput.getText().toString());
                if (price <= 0) {
                    priceInput.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                priceInput.setError("Invalid price format");
                isValid = false;
            }
        }

        // Validate stock
        if (TextUtils.isEmpty(stockInput.getText())) {
            stockInput.setError("Stock quantity is required");
            isValid = false;
        } else {
            try {
                int stock = Integer.parseInt(stockInput.getText().toString());
                if (stock < 0) {
                    stockInput.setError("Stock cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                stockInput.setError("Invalid stock format");
                isValid = false;
            }
        }

        // Validate category
        if (TextUtils.isEmpty(categoryInput.getText())) {
            categoryInput.setError("Category is required");
            isValid = false;
        }

        return isValid;
    }
} 