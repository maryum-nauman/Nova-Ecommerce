package com.example.nova_ecommerce.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.models.ChatPreview;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminInboxAdapter extends
        RecyclerView.Adapter<AdminInboxAdapter.InboxViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatPreview chat);
    }

    private final Context             context;
    private final List<ChatPreview>   chatList;
    private final OnChatClickListener listener;

    public AdminInboxAdapter(Context context,
                             List<ChatPreview> chatList,
                             OnChatClickListener listener) {
        this.context  = context;
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_inbox, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder,
                                 int position) {
        ChatPreview chat = chatList.get(position);

        // Initials avatar
        String name = chat.getUserName() != null
                ? chat.getUserName() : "?";
        holder.tvAvatar.setText(
                String.valueOf(name.charAt(0)).toUpperCase());

        holder.tvUserName.setText(name);
        holder.tvLastMessage.setText(chat.getLastMessage());
        holder.tvTime.setText(formatTime(chat.getLastTimestamp()));

        // Unread badge
        if (chat.getUnreadAdmin() > 0) {
            holder.tvUnread.setVisibility(View.VISIBLE);
            holder.tvUnread.setText(
                    String.valueOf(chat.getUnreadAdmin()));
        } else {
            holder.tvUnread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(
                v -> listener.onChatClick(chat));
    }

    private String formatTime(long timestamp) {
        if (timestamp == 0) return "";
        return new SimpleDateFormat("MMM dd", Locale.getDefault())
                .format(new Date(timestamp));
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    static class InboxViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvUserName, tvLastMessage,
                tvTime, tvUnread;

        InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar      = itemView.findViewById(R.id.tvInboxAvatar);
            tvUserName    = itemView.findViewById(R.id.tvInboxUserName);
            tvLastMessage = itemView.findViewById(R.id.tvInboxLastMsg);
            tvTime        = itemView.findViewById(R.id.tvInboxTime);
            tvUnread      = itemView.findViewById(R.id.tvInboxUnread);
        }
    }
}