package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.CartAdapter;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.example.nova_ecommerce.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView         recyclerCart;
    private CartAdapter          cartAdapter;
    private final List<CartItem> cartList = new ArrayList<>();
    private TextView             tvTotal, tvEmptyCart;
    private Button               btnCheckout;
    private CartDatabaseHelper   cartDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_cart, container, false);

        recyclerCart = view.findViewById(R.id.recyclerCart);
        tvTotal      = view.findViewById(R.id.tvTotal);
        tvEmptyCart  = view.findViewById(R.id.tvEmptyCart);
        btnCheckout  = view.findViewById(R.id.btnCheckout);

        cartDb = CartDatabaseHelper.getInstance(getContext());

        recyclerCart.setLayoutManager(
                new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(
                getContext(), cartList, this::refreshCart);
        recyclerCart.setAdapter(cartAdapter);

        refreshCart();

        btnCheckout.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(),
                        "Your cart is empty!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            new CheckoutFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCart();
    }

    private void refreshCart() {
        cartList.clear();
        cartList.addAll(cartDb.getAllItems());
        if (cartAdapter != null) cartAdapter.notifyDataSetChanged();
        updateTotal();
        if (tvEmptyCart != null) {
            tvEmptyCart.setVisibility(
                    cartList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void updateTotal() {
        if (tvTotal != null) {
            tvTotal.setText("Rs. "
                    + String.format("%,.0f", cartDb.getTotal()));
        }
    }
}