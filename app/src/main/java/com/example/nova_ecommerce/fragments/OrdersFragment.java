package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.OrderAdapter;
import com.example.nova_ecommerce.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdersFragment extends Fragment {

    private static final String ARG_FILTER_STATUS = "filterStatus";

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();
    private TextView tvNoOrders, tvTitle;
    private ImageButton btnBack;
    private String filterStatus = "all";

    public static OrdersFragment newInstance(String status) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filterStatus = getArguments().getString(ARG_FILTER_STATUS, "all");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        tvNoOrders = view.findViewById(R.id.tvNoOrders);
        tvTitle = view.findViewById(R.id.tvOrdersTitle); // Assuming ID in layout
        btnBack = view.findViewById(R.id.btnBackOrders);

        // Update Title based on filter
        if (tvTitle != null) {
            if ("all".equalsIgnoreCase(filterStatus)) {
                tvTitle.setText("My Orders");
            } else {
                tvTitle.setText(filterStatus.substring(0, 1).toUpperCase() + filterStatus.substring(1).toLowerCase() + " Orders");
            }
        }

        adapter = new OrderAdapter(getContext(), orderList);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        loadUserOrders();

        return view;
    }

    private void loadUserOrders() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com")
                .getReference("users").child(uid).child("orders");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Order order = ds.getValue(Order.class);
                    if (order != null) {
                        order.setOrderId(ds.getKey());
                        
                        // Apply Filter
                        if ("all".equalsIgnoreCase(filterStatus) || 
                            (order.getStatus() != null && order.getStatus().equalsIgnoreCase(filterStatus))) {
                            orderList.add(order);
                        }
                    }
                }
                // Show newest orders first
                Collections.reverse(orderList);
                adapter.notifyDataSetChanged();

                tvNoOrders.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
