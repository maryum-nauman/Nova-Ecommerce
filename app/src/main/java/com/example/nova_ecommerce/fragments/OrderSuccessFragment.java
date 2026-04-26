package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nova_ecommerce.R;

public class OrderSuccessFragment extends Fragment {

    private static final String ARG_ORDER_ID       = "orderId";
    private static final String ARG_TOTAL          = "total";
    private static final String ARG_PAYMENT_METHOD = "paymentMethod";

    public static OrderSuccessFragment newInstance(String orderId,
                                                   double total,
                                                   String paymentMethod) {
        OrderSuccessFragment fragment = new OrderSuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID,       orderId);
        args.putDouble(ARG_TOTAL,          total);
        args.putString(ARG_PAYMENT_METHOD, paymentMethod);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_success,
                container, false);

        TextView tvOrderId       = view.findViewById(R.id.tvOrderId);
        TextView tvSuccessTotal  = view.findViewById(R.id.tvSuccessTotal);
        TextView tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        Button   btnContinue     = view.findViewById(R.id.btnContinueShopping);

        if (getArguments() != null) {
            tvOrderId.setText("Order ID: "
                    + getArguments().getString(ARG_ORDER_ID));
            tvSuccessTotal.setText("Rs. " + String.format("%,.0f",
                    getArguments().getDouble(ARG_TOTAL)));
            tvPaymentMethod.setText("Payment: "
                    + getArguments().getString(ARG_PAYMENT_METHOD));
        }

        // Go back to Shop
        btnContinue.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ShopFragment())
                    .commit();
        });

        return view;
    }
}