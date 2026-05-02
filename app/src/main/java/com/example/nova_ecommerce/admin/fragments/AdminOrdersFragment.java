package com.example.nova_ecommerce.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.admin.adapters.AdminOrdersAdapter;
import com.example.nova_ecommerce.admin.models.AdminOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrdersFragment extends Fragment {

    private RecyclerView         recyclerOrders;
    private AdminOrdersAdapter   adapter;
    private ProgressBar          progressBar;
    private TextView             tvEmpty, tvOrderCount;
    private Spinner              spinnerFilter;
    private DatabaseReference    usersRef;

    // Full list loaded once — filter applies on top
    private final List<AdminOrder> allOrders      = new ArrayList<>();
    private final List<AdminOrder> filteredOrders = new ArrayList<>();

    private int selectedDays = 7; // default filter

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_admin_orders, container, false);

        recyclerOrders = view.findViewById(R.id.recyclerAdminOrders);
        progressBar    = view.findViewById(R.id.progressBarAdminOrders);
        tvEmpty        = view.findViewById(R.id.tvEmptyAdminOrders);
        tvOrderCount   = view.findViewById(R.id.tvAdminOrderCount);
        spinnerFilter  = view.findViewById(R.id.spinnerOrderFilter);

        usersRef = FirebaseDatabase.getInstance(
                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("users");

        // ── RecyclerView setup ────────────────────────────────
        recyclerOrders.setLayoutManager(
                new LinearLayoutManager(getContext()));
        adapter = new AdminOrdersAdapter(
                getContext(), filteredOrders,
                order -> showOrderDetailDialog(order));
        recyclerOrders.setAdapter(adapter);

        // ── Spinner setup ─────────────────────────────────────
        setupFilterSpinner();

        loadAllOrders();
        return view;
    }

    // ── Spinner: Last 7 / 14 / 30 days / All time ────────────
    private void setupFilterSpinner() {
        String[] filters = {
                "Last 7 Days",
                "Last 14 Days",
                "Last 30 Days",
                "All Orders"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner,
                filters);
        spinnerAdapter.setDropDownViewResource(
                R.layout.item_spinner_dropdown);
        spinnerFilter.setAdapter(spinnerAdapter);

        spinnerFilter.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int pos,
                                               long id) {
                        switch (pos) {
                            case 0: selectedDays = 7;   break;
                            case 1: selectedDays = 14;  break;
                            case 2: selectedDays = 30;  break;
                            case 3: selectedDays = -1;  break; // All
                        }
                        applyFilter();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> p) {}
                });
    }

    // ── Load all orders from users/{userId}/orders ────────────
    private void loadAllOrders() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnap) {
                allOrders.clear();

                for (DataSnapshot userSnap : usersSnap.getChildren()) {
                    String uid = userSnap.getKey();

                    // Skip admin node
                    String role = userSnap.child("role")
                            .getValue(String.class);
                    if ("admin".equals(role)) continue;

                    DataSnapshot ordersSnap =
                            userSnap.child("orders");

                    for (DataSnapshot orderSnap
                            : ordersSnap.getChildren()) {
                        AdminOrder order = orderSnap.getValue(AdminOrder.class);
                        if (order != null) {
                            order.setOrderId(orderSnap.getKey());
                            // Preserve userId for detail dialog
                            if (order.getUserId() == null) {
                                order.setUserId(uid);
                            }
                            allOrders.add(order);
                        }
                    }
                }

                // Sort newest first
                allOrders.sort((a, b) -> {
                    if (a.getTimestamp() == null) return 1;
                    if (b.getTimestamp() == null) return -1;
                    return b.getTimestamp().compareTo(a.getTimestamp());
                });

                progressBar.setVisibility(View.GONE);
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Apply date filter ─────────────────────────────────────
    private void applyFilter() {
        filteredOrders.clear();

        if (selectedDays == -1) {
            // All orders
            filteredOrders.addAll(allOrders);
        } else {
            long now      = System.currentTimeMillis();
            long cutoffMs = (long) selectedDays * 24 * 60 * 60 * 1000;
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (AdminOrder order : allOrders) {
                try {
                    if (order.getTimestamp() == null) continue;
                    Date orderDate = sdf.parse(order.getTimestamp());
                    if (orderDate != null
                            && (now - orderDate.getTime()) <= cutoffMs) {
                        filteredOrders.add(order);
                    }
                } catch (ParseException e) {
                    // If timestamp can't be parsed, include it anyway
                    filteredOrders.add(order);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Update count label
        tvOrderCount.setText(filteredOrders.size() + " order(s)");

        tvEmpty.setVisibility(
                filteredOrders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ── Order detail + status update dialog ───────────────────
    private void showOrderDetailDialog(AdminOrder order) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_admin_order_detail, null);

        // ── Bind order info ───────────────────────────────────
        TextView tvId       = dialogView.findViewById(R.id.tvDialogOrderId);
        TextView tvName     = dialogView.findViewById(R.id.tvDialogName);
        TextView tvEmail    = dialogView.findViewById(R.id.tvDialogEmail);
        TextView tvPhone    = dialogView.findViewById(R.id.tvDialogPhone);
        TextView tvAddr     = dialogView.findViewById(R.id.tvDialogAddress);
        TextView tvPayment  = dialogView.findViewById(R.id.tvDialogPayment);
        TextView tvTotal    = dialogView.findViewById(R.id.tvDialogTotal);
        TextView tvItems    = dialogView.findViewById(R.id.tvDialogItems);
        TextView tvTime     = dialogView.findViewById(R.id.tvDialogTime);
        Spinner  spStatus   = dialogView.findViewById(
                R.id.spinnerOrderStatus);

        tvId.setText("Order: #" + order.getOrderId()
                .substring(Math.max(0,
                        order.getOrderId().length() - 8)));
        tvName.setText("👤 " + order.getFullName());
        tvEmail.setText("✉️ " + order.getEmail());
        tvPhone.setText("📞 " + order.getPhone());
        tvAddr.setText("📍 " + order.getAddress()
                + ", " + order.getCity()
                + " - " + order.getPostalCode());
        tvPayment.setText("💳 " + order.getPaymentMethod());
        tvTotal.setText("💰 Rs. " + String.format(
                "%,.0f", order.getTotalAmount()));
        tvTime.setText("🕐 " + order.getTimestamp());

        // Build items summary
        StringBuilder itemsSb = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String name = item.get("name") != null
                        ? item.get("name").toString() : "?";
                String qty = item.get("quantity") != null
                        ? item.get("quantity").toString() : "1";
                String price = item.get("price") != null
                        ? item.get("price").toString() : "0";
                itemsSb.append("• ").append(name)
                        .append(" x").append(qty)
                        .append("  Rs.").append(price)
                        .append("\n");
            }
        }
        tvItems.setText(itemsSb.toString().trim());

        // ── Status spinner ────────────────────────────────────
        String[] statuses = {
                "Pending", "Processing",
                "Shipped", "Delivered", "Cancelled"
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_spinner,
                statuses);
        statusAdapter.setDropDownViewResource(
                R.layout.item_spinner_dropdown);
        spStatus.setAdapter(statusAdapter);

        // Pre-select current status
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(order.getStatus())) {
                spStatus.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Order Details")
                .setView(dialogView)
                .setPositiveButton("Update Status", (dialog, which) -> {
                    String newStatus = spStatus
                            .getSelectedItem().toString();
                    updateOrderStatus(order, newStatus);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    // ── Write updated status to Firebase ─────────────────────
    private void updateOrderStatus(AdminOrder order, String newStatus) {
        usersRef.child(order.getUserId())
                .child("orders")
                .child(order.getOrderId())
                .child("status")
                .setValue(newStatus)
                .addOnSuccessListener(unused -> {
                    order.setStatus(newStatus);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(),
                            "Status updated to " + newStatus,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}