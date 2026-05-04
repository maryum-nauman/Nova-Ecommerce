package com.example.nova_ecommerce.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.user.adapters.ChatMessageAdapter;
import com.example.nova_ecommerce.user.models.ChatMessage;
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

public class UserChatFragment extends Fragment {
    private static final String ADMIN_UID  = "48ULkpPhYfVOAAfqcKbD7VtXOyt1";
    private static final String ADMIN_NAME = "Nova Support";

    private RecyclerView       recyclerChat;
    private ChatMessageAdapter adapter;
    private EditText           etMessage;
    private ImageButton        btnSend;
    private TextView           tvEmpty;

    private final List<ChatMessage> messageList = new ArrayList<>();

    private DatabaseReference chatRef;
    private String            userId;
    private String            userName;
    private String            userEmail;
    private String            chatKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);

        recyclerChat = view.findViewById(R.id.recyclerChat);
        etMessage    = view.findViewById(R.id.etChatMessage);
        btnSend      = view.findViewById(R.id.btnSendMessage);
        tvEmpty      = view.findViewById(R.id.tvChatEmpty);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please log in to chat", Toast.LENGTH_SHORT).show();
            return view;
        }

        userId  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatKey = userId + "_" + ADMIN_UID;

        chatRef = FirebaseDatabase.getInstance(
                "https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com"
        ).getReference("chats").child(chatKey);

        // Setup RecyclerView
        LinearLayoutManager llm =
                new LinearLayoutManager(getContext());
        llm.setStackFromEnd(true);
        recyclerChat.setLayoutManager(llm);
        adapter = new ChatMessageAdapter(
                getContext(), messageList, false);
        recyclerChat.setAdapter(adapter);

        loadUserInfo();

        btnSend.setOnClickListener(v -> sendMessage());
        return view;
    }

    private void loadUserInfo() {
        FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com").getReference("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        userName  = snap.child("name").getValue(String.class);
                        userEmail = snap.child("email").getValue(String.class);

                        if (userName  == null) userName  = "User";
                        if (userEmail == null) userEmail = "";

                        initChatMetadata();
                        loadMessages();
                        resetUnreadUser();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError e) {
                        userName  = "User";
                        userEmail = "";
                        initChatMetadata();
                        loadMessages();
                    }
                });
    }

    private void initChatMetadata() {
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.hasChild("userId")) {
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("userId",     userId);
                    meta.put("adminId",    ADMIN_UID);
                    meta.put("userName",   userName);
                    meta.put("userEmail",  userEmail);
                    meta.put("adminName",  ADMIN_NAME);
                    meta.put("unreadAdmin", 0);
                    meta.put("unreadUser",  0);
                    meta.put("lastMessage", "");
                    meta.put("lastTimestamp", 0);
                    chatRef.updateChildren(meta);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void loadMessages() {
        chatRef.child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ChatMessage msg = child.getValue(ChatMessage.class);
                            if (msg != null) {
                                msg.setMessageId(child.getKey());
                                messageList.add(msg);
                            }
                        }
                        adapter.sortByTimestamp();

                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            recyclerChat.scrollToPosition(messageList.size() - 1);
                        }
                        tvEmpty.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                        resetUnreadUser();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String msgId = chatRef.child("messages").push().getKey();
        long   now   = System.currentTimeMillis();

        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("senderId",   userId);
        msgMap.put("senderName", userName);
        msgMap.put("message",    text);
        msgMap.put("timestamp",  now);
        msgMap.put("isAdmin",    false);

        chatRef.child("messages").child(msgId).setValue(msgMap);

        Map<String, Object> meta = new HashMap<>();
        meta.put("lastMessage",   text);
        meta.put("lastTimestamp", now);
        chatRef.updateChildren(meta);

        chatRef.child("unreadAdmin")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot s) {
                                long cur = s.getValue(Long.class) != null ? s.getValue(Long.class) : 0;
                                chatRef.child("unreadAdmin").setValue(cur + 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError e) {}
                        });

        etMessage.setText("");
    }

    private void resetUnreadUser() {
        if (chatRef != null) {
            chatRef.child("unreadUser").setValue(0);
        }
    }
}