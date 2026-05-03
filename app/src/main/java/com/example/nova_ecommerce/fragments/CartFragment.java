package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.CartAdapter;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.example.nova_ecommerce.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.*;

public class CartFragment extends Fragment {

    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;
    private final List<CartItem> cartList = new ArrayList<>();
    private TextView tvTotal, tvEmptyCart;
    private Button btnCheckout;
    private CartDatabaseHelper cartDb;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerCart = view.findViewById(R.id.recyclerCart);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return view;
        }

        currentUserId = user.getUid();
        cartDb = CartDatabaseHelper.getInstance(getContext());

        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartAdapter = new CartAdapter(getContext(), cartList, this::refreshCart);
        recyclerCart.setAdapter(cartAdapter);

        cartAdapter = new CartAdapter(getContext(), cartList, this::refreshCart);
        recyclerCart.setAdapter(cartAdapter);

        cartAdapter.setOnItemClickListener(item -> {
            String productId = item.getProductId();
            String categoryId = item.getCategoryId();

            if (productId == null || categoryId == null || categoryId.isEmpty()) {
                Toast.makeText(getContext(), "Product details unavailable", Toast.LENGTH_SHORT).show();
                return;
            }

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ProductDetailFragment.newInstance(productId, categoryId))
                    .addToBackStack(null)
                    .commit();
        });
        refreshCart();

        btnCheckout.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CheckoutFragment())
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
        cartList.addAll(cartDb.getAllItems(currentUserId));
        cartAdapter.notifyDataSetChanged();
        tvEmptyCart.setVisibility(cartList.isEmpty() ? View.VISIBLE : View.GONE);
        tvTotal.setText("Rs. " + String.format("%,.0f", cartDb.getTotal(currentUserId)));
    }
}