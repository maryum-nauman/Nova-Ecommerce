package com.example.nova_ecommerce.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.models.Review;

import java.util.List;

public class ReviewAdapter extends
        RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context       context;
    private final List<Review>  reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context    = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder,
                                 int position) {
        Review review = reviewList.get(position);

        holder.tvUserName.setText(review.getUserName());
        holder.tvComment.setText(review.getComment());
        holder.tvTimestamp.setText(review.getTimestamp());
        holder.ratingBar.setRating((float) review.getRating());

        // Show initials avatar
        String name = review.getUserName();
        if (name != null && !name.isEmpty()) {
            holder.tvAvatar.setText(
                    String.valueOf(name.charAt(0)).toUpperCase());
        }
    }

    @Override
    public int getItemCount() { return reviewList.size(); }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView   tvUserName, tvComment, tvTimestamp, tvAvatar;
        RatingBar  ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar    = itemView.findViewById(R.id.tvReviewAvatar);
            tvUserName  = itemView.findViewById(R.id.tvReviewUserName);
            tvComment   = itemView.findViewById(R.id.tvReviewComment);
            tvTimestamp = itemView.findViewById(R.id.tvReviewTimestamp);
            ratingBar   = itemView.findViewById(R.id.ratingBarReview);
        }
    }
}