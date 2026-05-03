package com.example.nova_ecommerce.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.nova_ecommerce.utils.OtpManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private CartDatabaseHelper cartDb;
    private String             userId;
    private String             userEmail;
    private String             userName;
    private DatabaseReference  userOrdersRef;
    private List<CartItem>     cartItems;
    private boolean            summaryExpanded = false;

    private String pendingFullName, pendingPhone, pendingEmail, pendingAddress, pendingCity, pendingPostalCode, pendingPaymentMethod, pendingOtp;

    private static final String DB_URL = "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId    = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            userOrdersRef = FirebaseDatabase.getInstance(DB_URL).getReference("users").child(userId).child("orders");
        }

        cartDb = CartDatabaseHelper.getInstance(getContext());
        if (userId != null) {
            FirebaseDatabase.getInstance(DB_URL).getReference("users").child(userId).child("name")
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot s) {
                                    userName = s.getValue(String.class);
                                    if (userName == null) userName = "Customer";
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError e) {
                                    userName = "Customer";
                                }
                            });
        }

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

        if (userEmail != null) etEmail.setText(userEmail);
        rgPayment.setOnCheckedChangeListener((group, checkedId) ->
                cardFieldsLayout.setVisibility(checkedId == R.id.rbCreditCard ? View.VISIBLE : View.GONE));
        view.findViewById(R.id.layoutSummaryHeader).setOnClickListener(v -> toggleSummary());

        loadOrderSummary();
        btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());
        return view;
    }

    private void toggleSummary() {
        summaryExpanded = !summaryExpanded;
        layoutOrderSummary.setVisibility(summaryExpanded ? View.VISIBLE : View.GONE);
        tvSummaryArrow.setText(summaryExpanded ? "▲" : "▼");
    }

    private void loadOrderSummary() {
        if (userId == null) return;
        cartItems = cartDb.getAllItems(userId);
        double total = cartDb.getTotal(userId);
        tvItemCount.setText(cartItems.size() + " item(s)");
        tvOrderTotal.setText("Rs. " + String.format("%,.0f", total));
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setAdapter(new CheckoutItemAdapter(getContext(), cartItems));
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
            etFullName.setError("Required");
            etFullName.requestFocus(); return;
        }
        if (phone.isEmpty() || phone.length() < 10) {
            etPhone.setError("Enter valid phone");
            etPhone.requestFocus(); return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            etEmail.setError("Enter valid email");
            etEmail.requestFocus(); return;
        }
        if (address.isEmpty()) {
            etAddress.setError("Required");
            etAddress.requestFocus(); return;
        }
        if (city.isEmpty()) {
            etCity.setError("Required");
            etCity.requestFocus(); return;
        }

        pendingFullName    = fullName;
        pendingPhone       = phone;
        pendingEmail       = email;
        pendingAddress     = address;
        pendingCity        = city;
        pendingPostalCode  = postalCode;

        if (rbCreditCard.isChecked()) {
            String cardNum = etCardNumber.getText().toString().trim();
            String expiry  = etCardExpiry.getText().toString().trim();
            String cvv     = etCardCVV.getText().toString().trim();

            if (cardNum.length() < 16) {
                etCardNumber.setError("16-digit number required");
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
            pendingPaymentMethod = "Credit Card";
            sendOtpAndShowDialog();

        } else if (rbEasyPaisa.isChecked()) {
            pendingPaymentMethod = "EasyPaisa / JazzCash";
            sendOtpAndShowDialog();

        } else {
            placeOrder("Cash on Delivery");
        }
    }

    private void sendOtpAndShowDialog() {
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Sending OTP...");
        pendingOtp = OtpManager.generateAndSave(userId);
        OtpManager.sendOtpEmail(userEmail, userName != null ? userName : "Customer", pendingOtp,
                new OtpManager.OtpEmailCallback() {
                    @Override
                    public void onSuccess() {
                        new Handler(Looper.getMainLooper()).post(() -> {btnPlaceOrder.setEnabled(true);
                            btnPlaceOrder.setText("Place Order");
                            showOtpDialog();
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            btnPlaceOrder.setEnabled(true);
                            btnPlaceOrder.setText("Place Order");
                            Toast.makeText(getContext(), "Email error, but you can still verify", Toast.LENGTH_SHORT).show();
                            showOtpDialog();
                        });
                    }
                });
    }

    private void showOtpDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_otp_verify, null);

        TextView  tvOtpEmail   = dialogView.findViewById(R.id.tvOtpEmail);
        EditText  etOtp        = dialogView.findViewById(R.id.etOtpCode);
        TextView  tvCountdown  = dialogView.findViewById(R.id.tvOtpCountdown);
        TextView  tvResend     = dialogView.findViewById(R.id.tvResendOtp);
        Button    btnVerify    = dialogView.findViewById(R.id.btnVerifyOtp);
        ProgressBar pbVerify   = dialogView.findViewById(R.id.pbOtpVerify);
        String maskedEmail = maskEmail(userEmail);
        tvOtpEmail.setText("OTP sent to " + maskedEmail);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.show();
        CountDownTimer[] timer = {null};
        timer[0] = new CountDownTimer(5 * 60 * 1000, 1000) {
            @Override
            public void onTick(long ms) {
                long minutes = ms / 60000;
                long seconds = (ms % 60000) / 1000;
                tvCountdown.setText(String.format(Locale.getDefault(), "Expires in %02d:%02d", minutes, seconds));
                tvCountdown.setTextColor(ms < 60000 ? 0xFFFF5722 : 0xFF888888);
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("OTP expired");
                tvCountdown.setTextColor(0xFFFF5722);
                btnVerify.setEnabled(false);
            }
        }.start();

        dialog.setOnDismissListener(d -> {
            if (timer[0] != null) timer[0].cancel();
        });

        etOtp.requestFocus();
        etOtp.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (s.length() == 6) {
                    btnVerify.performClick();
                }
            }
        });

        tvResend.setOnClickListener(v -> {
            if (timer[0] != null) timer[0].cancel();
            pendingOtp = OtpManager.generateAndSave(userId);
            tvResend.setEnabled(false);
            tvResend.setTextColor(0xFFAAAAAA);
            tvResend.setText("Resending...");

            OtpManager.sendOtpEmail(userEmail, userName, pendingOtp, new OtpManager.OtpEmailCallback() {
                        @Override
                        public void onSuccess() {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                tvResend.setText("Resend OTP");
                                tvResend.setEnabled(true);
                                tvResend.setTextColor(0xFF009688);
                                Toast.makeText(getContext(), "New OTP sent!", Toast.LENGTH_SHORT).show();
                                timer[0] = new CountDownTimer(5 * 60 * 1000, 1000) {
                                    @Override
                                    public void onTick(long ms) {
                                        long min = ms / 60000;
                                        long sec = (ms % 60000) / 1000;
                                        tvCountdown.setText(String.format(Locale.getDefault(), "Expires in %02d:%02d", min, sec));
                                    }
                                    @Override
                                    public void onFinish() {
                                        tvCountdown.setText("OTP expired");
                                        btnVerify.setEnabled(false);
                                    }
                                }.start();
                                btnVerify.setEnabled(true);
                            });
                        }
                        @Override
                        public void onFailure(String error) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                tvResend.setText("Resend OTP");
                                tvResend.setEnabled(true);
                                Toast.makeText(getContext(), "Resend failed: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });

        btnVerify.setOnClickListener(v -> {
            String entered = etOtp.getText().toString().trim();

            if (entered.length() != 6) {
                etOtp.setError("Enter 6-digit OTP");
                return;
            }

            pbVerify.setVisibility(View.VISIBLE);
            btnVerify.setEnabled(false);

            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("otps").child(userId)
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    pbVerify.setVisibility(View.GONE);
                                    String savedOtp = snap.child("code").getValue(String.class);
                                    Long expiry = snap.child("expiry").getValue(Long.class);
                                    boolean verified = Boolean.TRUE.equals(snap.child("verified").getValue(Boolean.class));

                                    if (verified) {
                                        showOtpError(etOtp, btnVerify, "OTP already used");
                                        return;
                                    }

                                    if (expiry != null && System.currentTimeMillis() > expiry) {
                                        showOtpError(etOtp, btnVerify, "OTP expired. Please resend.");
                                        return;
                                    }

                                    if (entered.equals(savedOtp)) {
                                        FirebaseDatabase.getInstance(DB_URL)
                                                .getReference("otps")
                                                .child(userId)
                                                .child("verified")
                                                .setValue(true);

                                        if (timer[0] != null)
                                            timer[0].cancel();
                                        dialog.dismiss();
                                        placeOrder(pendingPaymentMethod);

                                    } else {
                                        showOtpError(etOtp, btnVerify, "Incorrect OTP. Try again.");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    pbVerify.setVisibility(View.GONE);
                                    btnVerify.setEnabled(true);
                                    Toast.makeText(getContext(), "Verification failed", Toast.LENGTH_SHORT).show();
                                }
                            });
        });
    }

    private void showOtpError(EditText etOtp, Button btnVerify, String msg) {
        etOtp.setError(msg);
        etOtp.setText("");
        etOtp.requestFocus();
        btnVerify.setEnabled(true);
    }

    private String maskEmail(String email) {
        if (email == null) return "your email";
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    private void placeOrder(String paymentMethod) {
        if (userId == null || userOrdersRef == null) return;
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
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> order = new HashMap<>();
        order.put("fullName",      pendingFullName);
        order.put("phone",         pendingPhone);
        order.put("email",         pendingEmail);
        order.put("address",       pendingAddress);
        order.put("city",          pendingCity);
        order.put("postalCode",    pendingPostalCode);
        order.put("paymentMethod", paymentMethod);
        order.put("items",         itemsList);
        order.put("totalAmount",   total);
        order.put("status",        "Pending");
        order.put("timestamp",     timestamp);

        String orderId = userOrdersRef.push().getKey();
        userOrdersRef.child(orderId).setValue(order)
                .addOnSuccessListener(unused -> {
                    FirebaseDatabase.getInstance(DB_URL).getReference("otps").child(userId).removeValue();

                    cartDb.clearCart(userId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, OrderSuccessFragment.newInstance(orderId, total, paymentMethod, pendingFullName, pendingAddress, pendingCity, itemsList))
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Place Order");
                    Toast.makeText(getContext(), "Order failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}