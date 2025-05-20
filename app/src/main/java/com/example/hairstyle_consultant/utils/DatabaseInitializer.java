package com.example.hairstyle_consultant.utils;

import android.util.Log;

import com.example.hairstyle_consultant.models.Product;
import com.example.hairstyle_consultant.services.ProductService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";
    private final ProductService productService;

    public DatabaseInitializer() {
        this.productService = new ProductService();
    }

    public void initializeProducts() {
        // First check if products already exist
        productService.getAllProducts()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        addSampleProducts();
                    } else {
                        Log.d(TAG, "Products already exist in database");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking products", e);
                });
    }

    private void addSampleProducts() {
        List<Product> sampleProducts = new ArrayList<>();

        // Shampoos
        sampleProducts.add(new Product(
                null,
                "Moisture Boost Shampoo",
                "Hydrating shampoo for dry and damaged hair with argan oil and shea butter",
                19.99,
                50,
                "Shampoo",
                "HairCare Pro",
                "https://example.com/moisture-shampoo.jpg",
                true
        ));

        sampleProducts.add(new Product(
                null,
                "Volume Lift Shampoo",
                "Adds volume and body to fine hair with natural ingredients",
                24.99,
                35,
                "Shampoo",
                "HairCare Pro",
                "https://example.com/volume-shampoo.jpg",
                true
        ));

        sampleProducts.add(new Product(
                null,
                "Anti-Dandruff Shampoo",
                "Medicated shampoo for dandruff control with zinc pyrithione",
                16.99,
                45,
                "Shampoo",
                "ScalpCare",
                "https://example.com/anti-dandruff.jpg",
                true
        ));

        // Conditioners
        sampleProducts.add(new Product(
                null,
                "Deep Repair Conditioner",
                "Intensive repair for damaged hair with keratin and amino acids",
                22.99,
                40,
                "Conditioner",
                "HairCare Pro",
                "https://example.com/repair-conditioner.jpg",
                true
        ));

        sampleProducts.add(new Product(
                null,
                "Color Protect Conditioner",
                "Extends hair color vibrancy and prevents fading",
                21.99,
                30,
                "Conditioner",
                "ColorGuard",
                "https://example.com/color-protect.jpg",
                true
        ));

        // Styling Products
        sampleProducts.add(new Product(
                null,
                "Heat Protectant Spray",
                "Protects hair from heat damage up to 450°F with natural oils",
                18.99,
                60,
                "Styling",
                "StyleGuard",
                "https://example.com/heat-protectant.jpg",
                true
        ));

        sampleProducts.add(new Product(
                null,
                "Texturizing Sea Salt Spray",
                "Creates beachy waves and texture with natural sea salt",
                16.99,
                45,
                "Styling",
                "StyleGuard",
                "https://example.com/sea-salt-spray.jpg",
                true
        ));

        sampleProducts.add(new Product(
                null,
                "Strong Hold Hair Gel",
                "Maximum hold gel for all hair types",
                14.99,
                55,
                "Styling",
                "StyleGuard",
                "https://example.com/hair-gel.jpg",
                true
        ));

        // Hair Treatments
        sampleProducts.add(new Product(
                null,
                "Keratin Treatment Kit",
                "Professional-grade keratin treatment for home use",
                49.99,
                25,
                "Treatment",
                "HairCare Pro",
                "https://example.com/keratin-kit.jpg",
                true
        ));

        sampleProducts.add(new Product(
                null,
                "Hair Growth Serum",
                "Promotes hair growth with biotin and caffeine",
                29.99,
                40,
                "Treatment",
                "HairGrowth",
                "https://example.com/growth-serum.jpg",
                true
        ));

        // Hair Tools
        sampleProducts.add(new Product(
                null,
                "Professional Hair Dryer",
                "Ionic hair dryer with multiple heat settings",
                89.99,
                20,
                "Tools",
                "StyleTools",
                "https://example.com/hair-dryer.jpg",
                true
        ));

        sampleProducts.add(new Product(
                null,
                "Ceramic Flat Iron",
                "1-inch ceramic flat iron with adjustable temperature",
                59.99,
                30,
                "Tools",
                "StyleTools",
                "https://example.com/flat-iron.jpg",
                true
        ));

        // Hair Accessories
        sampleProducts.add(new Product(
                null,
                "Silk Hair Scarf",
                "100% silk hair scarf for protection and styling",
                24.99,
                50,
                "Accessories",
                "StyleGuard",
                "https://example.com/silk-scarf.jpg",
                true
        ));

        sampleProducts.add(new Product(
                null,
                "Professional Hair Clips",
                "Set of 12 durable hair clips",
                12.99,
                100,
                "Accessories",
                "StyleTools",
                "https://example.com/hair-clips.jpg",
                true
        ));

        // Add all products to Firestore
        for (Product product : sampleProducts) {
            productService.addProduct(product)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Sample product added: " + product.getName());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding sample product: " + product.getName(), e);
                    });
        }
    }
} 