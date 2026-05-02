package com.example.nova_ecommerce.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.adapters.ChatMessageAdapter;
import com.example.nova_ecommerce.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminChatFragment extends Fragment {

    private static final String ARG_CHAT_KEY  = "chatKey";
    private static final String ARG_USER_NAME = "userName";

    private RecyclerView       recyclerChat;
    private ChatMessageAdapter adapter;
    private EditText           etMessage;
    private ImageButton        btnSend, btnBack;
    private TextView           tvChatTitle;

    private final List<ChatMessage> messageList = new ArrayList<>();
    private DatabaseReference       chatRef;
    private String                  adminId;
    private String                  chatKey;

    public static AdminChatFragment newInstance(String chatKey,
                                                String userName) {
        AdminChatFragment f = new AdminChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_KEY,  chatKey);
        args.putString(ARG_USER_NAME, userName);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_admin_chat, container, false);

        recyclerChat = view.findViewById(R.id.recyclerChat);
        etMessage    = view.findViewById(R.id.etChatMessage);
        btnSend      = view.findViewById(R.id.btnSendMessage);
        btnBack      = view.findViewById(R.id.btnChatBack);
        tvChatTitle  = view.findViewById(R.id.tvChatTitle);

        adminId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        if (getArguments() != null) {
            chatKey = getArguments().getString(ARG_CHAT_KEY);
            tvChatTitle.setText(
                    getArguments().getString(ARG_USER_NAME,
                            "Customer"));
        }

        // Reference uses the full chatKey directly
        chatRef = FirebaseDatabase.getInstance(
                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("chats").child(chatKey);

        LinearLayoutManager llm =
                new LinearLayoutManager(getContext());
        llm.setStackFromEnd(true);
        recyclerChat.setLayoutManager(llm);
        adapter = new ChatMessageAdapter(
                getContext(), messageList, true); // true = admin view
        recyclerChat.setAdapter(adapter);

        btnBack.setOnClickListener(
                v -> getParentFragmentManager().popBackStack());
        btnSend.setOnClickListener(v -> sendReply());

        loadMessages();
        resetUnreadAdmin();
        return view;
    }

    private void loadMessages() {
        chatRef.child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot child
                                : snapshot.getChildren()) {
                            ChatMessage msg =
                                    child.getValue(ChatMessage.class);
                            if (msg != null) {
                                msg.setMessageId(child.getKey());
                                messageList.add(msg);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            recyclerChat.scrollToPosition(
                                    messageList.size() - 1);
                        }
                        resetUnreadAdmin();
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {}
                });
    }

    private void sendReply() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String msgId = chatRef.child("messages").push().getKey();
        long   now   = System.currentTimeMillis();

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("senderId",   adminId);
        msgMap.put("senderName", "Nova Support");
        msgMap.put("message",    text);
        msgMap.put("timestamp",  now);
        msgMap.put("isAdmin",    true);

        chatRef.child("messages").child(msgId).setValue(msgMap);

        // Update preview
        Map<String, Object> meta = new HashMap<>();
        meta.put("lastMessage",   text);
        meta.put("lastTimestamp", now);
        chatRef.updateChildren(meta);

        // Increment user's unread
        chatRef.child("unreadUser")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot s) {
                                long cur = s.getValue(Long.class) != null
                                        ? s.getValue(Long.class) : 0;
                                chatRef.child("unreadUser")
                                        .setValue(cur + 1);
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError e) {}
                        });

        etMessage.setText("");
    }

    private void resetUnreadAdmin() {
        if (chatRef != null) {
            chatRef.child("unreadAdmin").setValue(0);
        }
    }
}