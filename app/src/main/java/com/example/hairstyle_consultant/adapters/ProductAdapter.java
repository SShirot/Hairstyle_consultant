package com.example.hairstyle_consultant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hairstyle_consultant.R;
import com.example.hairstyle_consultant.models.Product;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> products;
    private OnProductActionListener editListener;
    private OnProductActionListener deleteListener;

    public interface OnProductActionListener {
        void onAction(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductActionListener editListener, OnProductActionListener deleteListener) {
        this.products = products;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView priceText;
        private TextView descriptionText;
        private TextView categoryText;
        private TextView stockText;
        private MaterialButton editButton;
        private MaterialButton deleteButton;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.productName);
            priceText = itemView.findViewById(R.id.productPrice);
            descriptionText = itemView.findViewById(R.id.productDescription);
            categoryText = itemView.findViewById(R.id.productCategory);
            stockText = itemView.findViewById(R.id.productStock);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(Product product) {
            nameText.setText(product.getName());
            priceText.setText(String.format("$%.2f", product.getPrice()));
            descriptionText.setText(product.getDescription());
            categoryText.setText(product.getCategory());
            stockText.setText(String.format("Stock: %d", product.getStockAmount()));

            editButton.setOnClickListener(v -> editListener.onAction(product));
            deleteButton.setOnClickListener(v -> deleteListener.onAction(product));
        }
    }
} 