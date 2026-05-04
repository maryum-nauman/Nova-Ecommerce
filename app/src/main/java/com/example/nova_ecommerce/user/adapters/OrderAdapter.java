package com.example.nova_ecommerce.user.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.user.models.CartItem;
import com.example.nova_ecommerce.user.models.Order;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText(String.format("ORDER #%s", order.getOrderId()));
        holder.tvOrderDate.setText(order.getTimestamp());
        holder.tvOrderNetTotal.setText(String.format(Locale.getDefault(), "Rs. %,.0f", order.getTotalAmount()));
        holder.tvOrderStatus.setText(order.getStatus());

        int itemCount = (order.getItems() != null) ? order.getItems().size() : 0;
        holder.tvOrderItemCount.setText(String.format(Locale.getDefault(), "%d %s", itemCount, itemCount == 1 ? "Item" : "Items"));

        setStatusStyle(holder.tvOrderStatus, order.getStatus());

        holder.itemView.setOnClickListener(v -> showOrderDetailsModal(order));
    }

    private void setStatusStyle(TextView tv, String status) {
        if (status == null) return;
        switch (status.toLowerCase()) {
            case "pending":
                tv.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "shipped":
                tv.setBackgroundResource(R.drawable.bg_status_shipped);
                break;
            case "delivered":
                tv.setBackgroundResource(R.drawable.bg_status_delivered);
                break;
            case "cancelled":
                tv.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
        }
    }

    private void showOrderDetailsModal(Order order) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_order_details, null);
        dialog.setContentView(view);

        TextView tvId = view.findViewById(R.id.tvDetailOrderId);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvNetTotal = view.findViewById(R.id.tvDetailNetTotal);
        TextView tvPayment = view.findViewById(R.id.tvDetailPaymentMethod);
        TextView tvAddress = view.findViewById(R.id.tvDetailAddress);
        LinearLayout containerItems = view.findViewById(R.id.containerItems);
        Button btnCancel = view.findViewById(R.id.btnCancelOrder);

        tvId.setText(String.format("ORDER #%s", order.getOrderId()));
        tvStatus.setText(order.getStatus());
        setStatusStyle(tvStatus, order.getStatus());
        tvDate.setText(order.getTimestamp());
        tvNetTotal.setText(String.format(Locale.getDefault(), "Rs. %,.0f", order.getTotalAmount()));
        tvPayment.setText(String.format("Payment: %s", order.getPaymentMethod()));
        
        String fullAddress = String.format("%s, %s", order.getAddress(), order.getCity());
        tvAddress.setText(String.format("Delivery to: %s", fullAddress));

        if (order.getItems() != null) {
            containerItems.removeAllViews();
            for (CartItem item : order.getItems()) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_order_detail_product, null);
                ImageView img = itemView.findViewById(R.id.imgItem);
                TextView name = itemView.findViewById(R.id.tvItemName);
                TextView qtyPrice = itemView.findViewById(R.id.tvItemQtyPrice);
                TextView total = itemView.findViewById(R.id.tvItemTotal);

                name.setText(item.getName());
                qtyPrice.setText(String.format(Locale.getDefault(), "%d x Rs. %,.0f", item.getQuantity(), item.getPrice()));
                total.setText(String.format(Locale.getDefault(), "Rs. %,.0f", item.getPrice() * item.getQuantity()));
                Glide.with(context).load(item.getImageUrl()).into(img);
                containerItems.addView(itemView);
            }
        }

        if ("pending".equalsIgnoreCase(order.getStatus())) {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(v -> {
                String uid = FirebaseAuth.getInstance().getUid();
                if (uid != null) {
                    FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com")
                            .getReference("users").child(uid).child("orders").child(order.getOrderId())
                            .child("status").setValue("cancelled")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Order Cancelled", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                }
            });
        }

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderDate, tvOrderNetTotal, tvOrderItemCount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderNetTotal = itemView.findViewById(R.id.tvOrderNetTotal);
            tvOrderItemCount = itemView.findViewById(R.id.tvOrderItemCount);
        }
    }
}
