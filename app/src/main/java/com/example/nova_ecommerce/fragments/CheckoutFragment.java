package com.example.nova_ecommerce.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.CheckoutItemAdapter;
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

    private EditText     etFullName, etPhone, etEmail;
    private EditText     etAddress, etCity, etPostalCode;
    private RadioGroup   rgPayment;
    private RadioButton  rbCashOnDelivery, rbCreditCard, rbEasyPaisa;
    private View         cardFieldsLayout;
    private EditText     etCardNumber, etCardExpiry, etCardCVV;
    private TextView     tvOrderTotal, tvItemCount, tvSummaryArrow;
    private LinearLayout layoutOrderSummary;
    private RecyclerView rvOrderItems;
    private Button       btnPlaceOrder;

    private CartDatabaseHelper  cartDb;
    private String              userId;
    private DatabaseReference   userOrdersRef;
    private List<CartItem>      cartItems;
    private boolean             summaryExpanded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_checkout, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance()
                    .getCurrentUser().getUid();
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
        tvSummaryArrow   = view.findViewById(R.id.tvSummaryArrow);
        layoutOrderSummary = view.findViewById(R.id.layoutOrderSummary);
        rvOrderItems     = view.findViewById(R.id.rvOrderItems);
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

        // ── Payment toggle ────────────────────────────────────
        rgPayment.setOnCheckedChangeListener((group, checkedId) ->
                cardFieldsLayout.setVisibility(
                        checkedId == R.id.rbCreditCard
                                ? View.VISIBLE : View.GONE));

        // ── Order summary expand/collapse ─────────────────────
        View summaryHeader = view.findViewById(R.id.layoutSummaryHeader);
        summaryHeader.setOnClickListener(v -> toggleSummary());

        loadOrderSummary();
        btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());
        return view;
    }

    // ── Toggle order summary expand/collapse ──────────────────
    private void toggleSummary() {
        summaryExpanded = !summaryExpanded;
        layoutOrderSummary.setVisibility(
                summaryExpanded ? View.VISIBLE : View.GONE);
        tvSummaryArrow.setText(summaryExpanded ? "▲" : "▼");
    }

    // ── Load cart items into summary ──────────────────────────
    private void loadOrderSummary() {
        if (userId == null) return;
        cartItems = cartDb.getAllItems(userId);
        double total = cartDb.getTotal(userId);

        tvItemCount.setText(cartItems.size() + " item(s)");
        tvOrderTotal.setText("Rs. "
                + String.format("%,.0f", total));

        // Setup items recycler inside summary
        rvOrderItems.setLayoutManager(
                new LinearLayoutManager(getContext()));
        rvOrderItems.setAdapter(
                new CheckoutItemAdapter(getContext(), cartItems));
        rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void validateAndPlaceOrder() {
        String fullName   = etFullName.getText().toString().trim();
        String phone      = etPhone.getText().toString().trim();
        String email      = etEmail.getText().toString().trim();
        String address    = etAddress.getText().toString().trim();
        String city       = etCity.getText().toString().trim();
        String postalCode = etPostalCode.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Required"); etFullName.requestFocus(); return;
        }
        if (phone.isEmpty() || phone.length() < 10) {
            etPhone.setError("Enter valid phone"); etPhone.requestFocus(); return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError("Enter valid email"); etEmail.requestFocus(); return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Required"); etAddress.requestFocus(); return;
        }
        if (city.isEmpty()) {
            etCity.setError("Required"); etCity.requestFocus(); return;
        }

        if (rbCreditCard.isChecked()) {
            String cardNum = etCardNumber.getText().toString().trim();
            String expiry  = etCardExpiry.getText().toString().trim();
            String cvv     = etCardCVV.getText().toString().trim();
            if (cardNum.length() < 16) {
                etCardNumber.setError("16-digit card number required");
                etCardNumber.requestFocus(); return;
            }
            if (expiry.isEmpty()) {
                etCardExpiry.setError("Enter expiry");
                etCardExpiry.requestFocus(); return;
            }
            if (cvv.length() < 3) {
                etCardCVV.setError("Enter valid CVV");
                etCardCVV.requestFocus(); return;
            }

            // ── Simulate card verification ────────────────────
            simulateCardVerification(fullName, phone, email,
                    address, city, postalCode);
            return;
        }

        if (rbEasyPaisa.isChecked()) {
            simulateWalletVerification(fullName, phone, email,
                    address, city, postalCode);
            return;
        }

        // Cash on delivery — no verification needed
        placeOrder(fullName, phone, email,
                address, city, postalCode, "Cash on Delivery");
    }

    // ── Simulate card verification with progress dialog ───────
    private void simulateCardVerification(String fullName, String phone,
                                          String email, String address,
                                          String city, String postalCode) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_payment_processing, null);
        TextView  tvStatus    = dialogView.findViewById(R.id.tvPaymentStatus);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressPayment);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialog.show();

        // Step 1: Verifying card
        tvStatus.setText("Verifying card details...");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Step 2: Contacting bank
            tvStatus.setText("Contacting bank...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Step 3: Authorization
                tvStatus.setText("Authorizing payment...");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Step 4: Success (no money deducted)
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("✅  Payment Authorized!");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        dialog.dismiss();
                        placeOrder(fullName, phone, email,
                                address, city, postalCode,
                                "Credit Card");
                    }, 800);
                }, 1200);
            }, 1200);
        }, 1200);
    }

    // ── Simulate EasyPaisa/JazzCash verification ──────────────
    private void simulateWalletVerification(String fullName, String phone,
                                            String email, String address,
                                            String city, String postalCode) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_payment_processing, null);
        TextView   tvStatus  = dialogView.findViewById(R.id.tvPaymentStatus);
        ProgressBar progress = dialogView.findViewById(R.id.progressPayment);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialog.show();

        tvStatus.setText("Connecting to wallet...");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvStatus.setText("Verifying account...");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progress.setVisibility(View.GONE);
                tvStatus.setText("✅  Wallet Verified!");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    dialog.dismiss();
                    placeOrder(fullName, phone, email,
                            address, city, postalCode,
                            "EasyPaisa / JazzCash");
                }, 800);
            }, 1500);
        }, 1200);
    }

    // ── Write order to Firebase ───────────────────────────────
    private void placeOrder(String fullName, String phone, String email,
                            String address, String city, String postalCode,
                            String paymentMethod) {
        if (userId == null || userOrdersRef == null) {
            Toast.makeText(getContext(),
                    "Please log in to place an order",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Placing Order...");

        double total = cartDb.getTotal(userId);

        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId",  item.getProductId());
            itemMap.put("categoryId", item.getCategoryId());
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

        String orderId = userOrdersRef.push().getKey();
        userOrdersRef.child(orderId).setValue(order)
                .addOnSuccessListener(unused -> {
                    cartDb.clearCart(userId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,
                                    OrderSuccessFragment.newInstance(
                                            orderId, total,
                                            paymentMethod,
                                            fullName, address,
                                            city, itemsList))
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