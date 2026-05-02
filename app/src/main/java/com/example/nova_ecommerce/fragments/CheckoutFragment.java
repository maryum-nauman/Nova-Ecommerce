package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.database.CartDatabaseHelper;
import com.example.nova_ecommerce.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutFragment extends Fragment {

    private EditText  etFullName, etPhone, etEmail;
    private EditText  etAddress, etCity, etPostalCode;
    private RadioGroup   rgPayment;
    private RadioButton  rbCashOnDelivery, rbCreditCard, rbEasyPaisa;
    private View         cardFieldsLayout;
    private EditText     etCardNumber, etCardExpiry, etCardCVV;
    private TextView     tvOrderTotal, tvItemCount;
    private Button       btnPlaceOrder;

    private CartDatabaseHelper cartDb;
    private String             userId;

    // ── NEW path: users/{userId}/orders ───────────────────────
    private DatabaseReference userOrdersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_checkout, container, false);

        // ── Firebase ──────────────────────────────────────────
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance()
                    .getCurrentUser().getUid();

            // Orders now live under users/{userId}/orders
            userOrdersRef = FirebaseDatabase.getInstance(
                    "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
            ).getReference("users").child(userId).child("orders");
        }

        cartDb = CartDatabaseHelper.getInstance(getContext());

        // ── Bind views ────────────────────────────────────────
        etFullName       = view.findViewById(R.id.etFullName);
        etPhone          = view.findViewById(R.id.etPhone);
        etEmail          = view.findViewById(R.id.etEmail);
        etAddress        = view.findViewById(R.id.etAddress);
        etCity           = view.findViewById(R.id.etCity);
        etPostalCode     = view.findViewById(R.id.etPostalCode);
        tvOrderTotal     = view.findViewById(R.id.tvOrderTotal);
        tvItemCount      = view.findViewById(R.id.tvItemCount);
        btnPlaceOrder    = view.findViewById(R.id.btnPlaceOrder);
        rgPayment        = view.findViewById(R.id.rgPayment);
        rbCashOnDelivery = view.findViewById(R.id.rbCashOnDelivery);
        rbCreditCard     = view.findViewById(R.id.rbCreditCard);
        rbEasyPaisa      = view.findViewById(R.id.rbEasyPaisa);
        cardFieldsLayout = view.findViewById(R.id.cardFieldsLayout);
        etCardNumber     = view.findViewById(R.id.etCardNumber);
        etCardExpiry     = view.findViewById(R.id.etCardExpiry);
        etCardCVV        = view.findViewById(R.id.etCardCVV);

        // Pre-fill email
        if (FirebaseAuth.getInstance().getCurrentUser() != null
                && FirebaseAuth.getInstance()
                .getCurrentUser().getEmail() != null) {
            etEmail.setText(FirebaseAuth.getInstance()
                    .getCurrentUser().getEmail());
        }

        // Show/hide card fields
        rgPayment.setOnCheckedChangeListener((group, checkedId) ->
                cardFieldsLayout.setVisibility(
                        checkedId == R.id.rbCreditCard
                                ? View.VISIBLE : View.GONE));

        loadOrderSummary();
        btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());

        return view;
    }

    private void loadOrderSummary() {
        List<CartItem> items = cartDb.getAllItems();
        double total = cartDb.getTotal();
        tvItemCount.setText(items.size() + " item(s) in cart");
        tvOrderTotal.setText("Rs. " + String.format("%,.0f", total));
    }

    private void validateAndPlaceOrder() {
        String fullName   = etFullName.getText().toString().trim();
        String phone      = etPhone.getText().toString().trim();
        String email      = etEmail.getText().toString().trim();
        String address    = etAddress.getText().toString().trim();
        String city       = etCity.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus(); return;
        }
        if (phone.isEmpty() || phone.length() < 10) {
            etPhone.setError("Enter a valid phone number");
            etPhone.requestFocus(); return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus(); return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus(); return;
        }
        if (city.isEmpty()) {
            etCity.setError("City is required");
            etCity.requestFocus(); return;
        }

        if (rbCreditCard.isChecked()) {
            String cardNum = etCardNumber.getText().toString().trim();
            String expiry  = etCardExpiry.getText().toString().trim();
            String cvv     = etCardCVV.getText().toString().trim();
            if (cardNum.length() < 16) {
                etCardNumber.setError("Enter valid 16-digit card number");
                etCardNumber.requestFocus(); return;
            }
            if (expiry.isEmpty()) {
                etCardExpiry.setError("Enter card expiry");
                etCardExpiry.requestFocus(); return;
            }
            if (cvv.length() < 3) {
                etCardCVV.setError("Enter valid CVV");
                etCardCVV.requestFocus(); return;
            }
        }

        String paymentMethod = "Cash on Delivery";
        int selectedId = rgPayment.getCheckedRadioButtonId();
        if (selectedId == R.id.rbCreditCard)
            paymentMethod = "Credit Card";
        else if (selectedId == R.id.rbEasyPaisa)
            paymentMethod = "EasyPaisa";

        placeOrder(fullName, phone, email,
                address, city, postalCode, paymentMethod);
    }

    private void placeOrder(String fullName, String phone, String email,
                            String address, String city, String postalCode,
                            String paymentMethod) {

        if (userOrdersRef == null) {
            Toast.makeText(getContext(),
                    "Please log in to place an order",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Placing Order...");

        List<CartItem> cartItems = cartDb.getAllItems();
        double total = cartDb.getTotal();

        // Build items list — now includes categoryId
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId",  item.getProductId());
            itemMap.put("categoryId", item.getCategoryId()); // ← new field
            itemMap.put("name",       item.getName());
            itemMap.put("price",      item.getPrice());
            itemMap.put("quantity",   item.getQuantity());
            itemMap.put("imageURL",   item.getImageUrl());
            itemsList.add(itemMap);
        }

        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> order = new HashMap<>();
        order.put("fullName",      fullName);
        order.put("phone",         phone);
        order.put("email",         email);
        order.put("address",       address);
        order.put("city",          city);
        order.put("postalCode",    postalCode);
        order.put("paymentMethod", paymentMethod);
        order.put("items",         itemsList);
        order.put("totalAmount",   total);
        order.put("status",        "Pending");
        order.put("timestamp",     timestamp);
        // userId not needed inside the order since it's
        // already scoped under users/{userId}/orders

        // ── Write to users/{userId}/orders/{orderId} ──────────
        String orderId = userOrdersRef.push().getKey();
        userOrdersRef.child(orderId).setValue(order)
                .addOnSuccessListener(unused -> {
                    cartDb.clearCart();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,
                                    OrderSuccessFragment.newInstance(
                                            orderId, total, paymentMethod))
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Place Order");
                    Toast.makeText(getContext(),
                            "Order failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}