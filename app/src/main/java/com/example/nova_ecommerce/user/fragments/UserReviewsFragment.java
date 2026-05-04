package com.example.nova_ecommerce.user.fragments;

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
import com.example.nova_ecommerce.user.adapters.ReviewAdapter;
import com.example.nova_ecommerce.user.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserReviewsFragment extends Fragment {

    private RecyclerView rvAllUserReviews;
    private ReviewAdapter adapter;
    private final List<Review> reviewList = new ArrayList<>();
    private TextView tvNoReviews;
    private ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_reviews, container, false);

        rvAllUserReviews = view.findViewById(R.id.rvAllUserReviews);
        tvNoReviews = view.findViewById(R.id.tvNoReviewsAll);
        btnBack = view.findViewById(R.id.btnBackReviews);

        rvAllUserReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReviewAdapter(getContext(), reviewList);
        adapter.setShowProductName(true);
        
        adapter.setOnReviewClickListener(review -> {
            if (review.getProductId() != null && review.getCategoryId() != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, ProductDetailFragment.newInstance(review.getProductId(), review.getCategoryId()))
                        .addToBackStack(null)
                        .commit();
            }
        });

        rvAllUserReviews.setAdapter(adapter);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        loadAllUserReviews();

        return view;
    }

    private void loadAllUserReviews() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com").getReference("users").child(uid).child("reviews");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Review review = ds.getValue(Review.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }
                Collections.reverse(reviewList);
                adapter.notifyDataSetChanged();
                tvNoReviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
