package com.example.barcodereader;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.SellerViewHolder> {

    private List<Seller> sellerList;

    public SellerAdapter(List<Seller> sellerList) {
        this.sellerList = sellerList;
    }

    @NonNull
    @Override
    public SellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller, parent, false);
        return new SellerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerViewHolder holder, int position) {
        Seller seller = sellerList.get(position);

        holder.tvSellerName.setText(seller.getName());
        holder.tvPrice.setText(String.valueOf(seller.getPrice()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = seller.getLink();

                if (url != null && !url.isEmpty()) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        v.getContext().startActivity(browserIntent);
                    } catch (Exception e) {
                        Toast.makeText(v.getContext(), "Link açılamadı", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(v.getContext(), "Satıcı linki bulunamadı", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }

    public static class SellerViewHolder extends RecyclerView.ViewHolder {
        TextView tvSellerName, tvPrice;

        public SellerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSellerName = itemView.findViewById(R.id.seller_name);
            tvPrice = itemView.findViewById(R.id.seller_price);
        }
    }
}