package com.example.nova_ecommerce.user.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.user.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends
        RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_SENT     = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context           context;
    private final List<ChatMessage> messages;
    private final boolean           isAdminView;

    public ChatMessageAdapter(Context context,
                              List<ChatMessage> messages,
                              boolean isAdminView) {
        this.context     = context;
        this.messages    = messages;
        this.isAdminView = isAdminView;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        boolean isSent = isAdminView ? msg.isAdmin() : !msg.isAdmin();
        return isSent ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {
        int layout = viewType == VIEW_TYPE_SENT
                ? R.layout.item_chat_sent
                : R.layout.item_chat_received;
        View view = LayoutInflater.from(context)
                .inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder,
                                 int position) {
        ChatMessage msg = messages.get(position);
        holder.tvMessage.setText(msg.getMessage());
        holder.tvTime.setText(formatTime(msg.getTimestamp()));
    }

    public void sortByTimestamp() {
        Collections.sort(messages,
                (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(timestamp));
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvChatMessage);
            tvTime    = itemView.findViewById(R.id.tvChatTime);
        }
    }
}