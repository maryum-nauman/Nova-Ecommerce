package com.example.nova_ecommerce.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.user.adapters.DealAdapter;
import com.example.nova_ecommerce.user.adapters.ProductAdapter;
import com.example.nova_ecommerce.user.models.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {

    private static final String ADMIN_UID = "48ULkpPhYfVOAAfqcKbD7VtXOyt1";

    private RecyclerView    rvDeals, rvRecommended;
    private DealAdapter     dealsAdapter;
    private ProductAdapter  recommendedAdapter;
    private TextView        tvEmpty;

    private final List<Product> dealsList        = new ArrayList<>();
    private final List<Product> recommendedList  = new ArrayList<>();
    private final List<Product> fullList         = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        rvDeals       = view.findViewById(R.id.recyclerDeals);
        rvRecommended = view.findViewById(R.id.recyclerRecommended);
        tvEmpty       = view.findViewById(R.id.tvEmpty);

        setupRecyclerViews();
        loadData();
        return view;
    }

    private void setupRecyclerViews() {
        rvDeals.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dealsAdapter = new DealAdapter(getContext(), dealsList);
        rvDeals.setAdapter(dealsAdapter);
        dealsAdapter.setOnDealClickListener(product -> navigateToDetail(product));

        rvRecommended.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recommendedAdapter = new ProductAdapter(getContext(), recommendedList);
        rvRecommended.setAdapter(recommendedAdapter);
        recommendedAdapter.setOnProductClickListener(product -> navigateToDetail(product));
    }

    private void navigateToDetail(Product product) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product.getId(), product.getCategoryId()))
                .addToBackStack(null)
                .commit();
    }

    private void loadData() {
        DatabaseReference adminRef = FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("products").child(ADMIN_UID);

        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot adminSnap) {
                dealsList.clear();
                recommendedList.clear();
                fullList.clear();

                for (DataSnapshot catSnap : adminSnap.getChildren()) {
                    String catId   = catSnap.getKey();
                    String catName = catSnap.child("name").getValue(String.class);

                    for (DataSnapshot productSnap : catSnap.child("items").getChildren()) {
                        Product product = productSnap.getValue(Product.class);
                        if (product != null) {
                            product.setId(productSnap.getKey());
                            product.setCategoryId(catId);
                            product.setCategoryName(catName);
                            fullList.add(product);

                            if (product.isFeatured()) {
                                if (dealsList.size() < 4)
                                    dealsList.add(product);
                            } else {
                                recommendedList.add(product);
                            }
                        }
                    }
                }

                dealsAdapter.notifyDataSetChanged();
                recommendedAdapter.notifyDataSetChanged();
                tvEmpty.setVisibility(recommendedList.isEmpty() && dealsList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void filterProducts(String query) {
        if (recommendedAdapter == null || fullList.isEmpty()) return;
        recommendedList.clear();

        if (query.isEmpty()) {
            rvDeals.setVisibility(View.VISIBLE);
            for (Product p : fullList) {
                if (!p.isFeatured()) recommendedList.add(p);
            }
            tvEmpty.setVisibility(View.GONE);
        } else {
            rvDeals.setVisibility(View.GONE);
            String lower = query.toLowerCase().trim();
            for (Product p : fullList) {
                boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(lower);
                boolean matchCat  = p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(lower);
                if (matchName || matchCat) recommendedList.add(p);
            }
            tvEmpty.setVisibility(recommendedList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        recommendedAdapter.notifyDataSetChanged();
    }
}