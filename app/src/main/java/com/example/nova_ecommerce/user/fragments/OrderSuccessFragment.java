package com.example.nova_ecommerce.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.user.adapters.SuccessItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderSuccessFragment extends Fragment {

    private static final String ARG_ORDER_ID      = "orderId";
    private static final String ARG_TOTAL         = "total";
    private static final String ARG_PAYMENT       = "paymentMethod";
    private static final String ARG_NAME          = "fullName";
    private static final String ARG_ADDRESS       = "address";
    private static final String ARG_CITY          = "city";

    private static final String ARG_ITEM_NAMES    = "itemNames";
    private static final String ARG_ITEM_QTYS     = "itemQtys";
    private static final String ARG_ITEM_PRICES   = "itemPrices";
    private static final String ARG_ITEM_IMAGES   = "itemImages";

    public static OrderSuccessFragment newInstance(
            String orderId, double total, String paymentMethod,
            String fullName, String address, String city,
            List<Map<String, Object>> items) {

        OrderSuccessFragment f = new OrderSuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        args.putDouble(ARG_TOTAL,    total);
        args.putString(ARG_PAYMENT,  paymentMethod);
        args.putString(ARG_NAME,     fullName);
        args.putString(ARG_ADDRESS,  address);
        args.putString(ARG_CITY,     city);

        ArrayList<String> names  = new ArrayList<>();
        ArrayList<String> qtys   = new ArrayList<>();
        ArrayList<String> prices = new ArrayList<>();
        ArrayList<String> images = new ArrayList<>();

        for (Map<String, Object> item : items) {
            names.add(item.get("name")  != null
                    ? item.get("name").toString()  : "");
            qtys.add(item.get("quantity") != null
                    ? item.get("quantity").toString() : "1");
            prices.add(item.get("price") != null
                    ? item.get("price").toString() : "0");
            images.add(item.get("imageURL") != null
                    ? item.get("imageURL").toString() : "");
        }

        args.putStringArrayList(ARG_ITEM_NAMES,  names);
        args.putStringArrayList(ARG_ITEM_QTYS,   qtys);
        args.putStringArrayList(ARG_ITEM_PRICES, prices);
        args.putStringArrayList(ARG_ITEM_IMAGES, images);

        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_order_success, container, false);

        TextView    tvOrderId     = view.findViewById(R.id.tvOrderId);
        TextView    tvTotal       = view.findViewById(R.id.tvSuccessTotal);
        TextView    tvPayment     = view.findViewById(R.id.tvPaymentMethod);
        TextView    tvName        = view.findViewById(R.id.tvSuccessName);
        TextView    tvAddress     = view.findViewById(R.id.tvSuccessAddress);
        RecyclerView rvItems      = view.findViewById(R.id.rvSuccessItems);
        Button      btnContinue   = view.findViewById(
                R.id.btnContinueShopping);

        Bundle args = getArguments();
        if (args != null) {
            String fullId = args.getString(ARG_ORDER_ID, "");
            String shortId = fullId.length() > 8 ? fullId.substring(fullId.length() - 8) : fullId;
            tvOrderId.setText("Order #" + shortId.toUpperCase());

            tvTotal.setText("Rs. " + String.format("%,.0f", args.getDouble(ARG_TOTAL)));
            tvPayment.setText(args.getString(ARG_PAYMENT));
            tvName.setText(args.getString(ARG_NAME));
            tvAddress.setText(args.getString(ARG_ADDRESS) + ", " + args.getString(ARG_CITY));

            List<String> names  = args.getStringArrayList(ARG_ITEM_NAMES);
            List<String> qtys   = args.getStringArrayList(ARG_ITEM_QTYS);
            List<String> prices = args.getStringArrayList(ARG_ITEM_PRICES);
            List<String> images = args.getStringArrayList(ARG_ITEM_IMAGES);

            rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
            rvItems.setAdapter(new SuccessItemAdapter(getContext(), names, qtys, prices, images));
            rvItems.setNestedScrollingEnabled(false);
        }

        btnContinue.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ShopFragment())
                    .commit();
        });

        return view;
    }
}