package com.example.nova_ecommerce.admin.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.admin.models.AdminOrder;

import java.util.List;
import java.util.Map;

public class AdminOrdersAdapter extends
        RecyclerView.Adapter<AdminOrdersAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(AdminOrder order);
    }

    private final Context             context;
    private final List<AdminOrder>         orderList;
    private final OnOrderClickListener listener;

    public AdminOrdersAdapter(Context context,
                              List<AdminOrder> orderList,
                              OnOrderClickListener listener) {
        this.context   = context;
        this.orderList = orderList;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder,
                                 int position) {
        AdminOrder order = orderList.get(position);

        holder.tvOrderId.setText("#" + order.getOrderId()
                .substring(Math.max(0,
                        order.getOrderId().length() - 8)));
        holder.tvCustomerName.setText(order.getFullName());
        holder.tvTimestamp.setText(order.getTimestamp());
        holder.tvTotal.setText("Rs. " + String.format(
                "%,.0f", order.getTotalAmount()));
        holder.tvPayment.setText(order.getPaymentMethod());

        // Item count
        int itemCount = order.getItems() != null
                ? order.getItems().size() : 0;
        holder.tvItemCount.setText(itemCount
                + (itemCount == 1 ? " item" : " items"));

        // Status badge color
        holder.tvStatus.setText(order.getStatus());
        holder.tvStatus.setBackgroundColor(
                getStatusColor(order.getStatus()));

        holder.cardOrder.setOnClickListener(
                v -> listener.onOrderClick(order));
    }

    private int getStatusColor(String status) {
        if (status == null) return Color.parseColor("#9E9E9E");
        switch (status) {
            case "Pending":    return Color.parseColor("#FF9800");
            case "Processing": return Color.parseColor("#2196F3");
            case "Shipped":    return Color.parseColor("#9C27B0");
            case "Delivered":  return Color.parseColor("#4CAF50");
            case "Cancelled":  return Color.parseColor("#F44336");
            default:           return Color.parseColor("#9E9E9E");
        }
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        CardView cardOrder;
        TextView tvOrderId, tvCustomerName, tvTimestamp,
                tvTotal, tvStatus, tvItemCount, tvPayment;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder       = itemView.findViewById(R.id.cardAdminOrder);
            tvOrderId       = itemView.findViewById(R.id.tvAdminOrderId);
            tvCustomerName  = itemView.findViewById(R.id.tvAdminOrderCustomer);
            tvTimestamp     = itemView.findViewById(R.id.tvAdminOrderTime);
            tvTotal         = itemView.findViewById(R.id.tvAdminOrderTotal);
            tvStatus        = itemView.findViewById(R.id.tvAdminOrderStatus);
            tvItemCount     = itemView.findViewById(R.id.tvAdminOrderItemCount);
            tvPayment       = itemView.findViewById(R.id.tvAdminOrderPayment);
        }
    }
}