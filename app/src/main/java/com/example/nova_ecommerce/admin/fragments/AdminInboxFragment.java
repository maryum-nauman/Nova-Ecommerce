package com.example.nova_ecommerce.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nova_ecommerce.R;
import com.example.nova_ecommerce.admin.adapters.AdminInboxAdapter;
import com.example.nova_ecommerce.user.models.ChatPreview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminInboxFragment extends Fragment {

    private RecyclerView      recyclerInbox;
    private AdminInboxAdapter adapter;
    private ProgressBar       progressBar;
    private TextView          tvEmpty;

    private final List<ChatPreview> chatList = new ArrayList<>();
    private DatabaseReference       chatsRef;
    private String                  adminId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_inbox, container, false);

        recyclerInbox = view.findViewById(R.id.recyclerAdminInbox);
        progressBar   = view.findViewById(R.id.progressBarInbox);
        tvEmpty       = view.findViewById(R.id.tvEmptyInbox);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        chatsRef = FirebaseDatabase.getInstance("https://nova-ecommerce-cb3bf-default-rtdb.firebaseio.com").getReference("chats");

        recyclerInbox.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminInboxAdapter(getContext(), chatList, chat -> openChat(chat));
        recyclerInbox.setAdapter(adapter);

        loadInbox();
        return view;
    }

    private void loadInbox() {
        progressBar.setVisibility(View.VISIBLE);

        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String chatAdminId = child.child("adminId").getValue(String.class);
                    if (chatAdminId == null || !chatAdminId.equals(adminId)) continue;

                    ChatPreview preview = new ChatPreview();
                    preview.setChatKey(child.getKey());
                    preview.setUserId(child.child("userId").getValue(String.class));
                    preview.setUserName(child.child("userName").getValue(String.class));
                    preview.setUserEmail(child.child("userEmail").getValue(String.class));
                    preview.setLastMessage(child.child("lastMessage").getValue(String.class));

                    Long ts = child.child("lastTimestamp").getValue(Long.class);
                    preview.setLastTimestamp(ts != null ? ts : 0);
                    Long unread = child.child("unreadAdmin").getValue(Long.class);
                    preview.setUnreadAdmin(unread != null ? unread.intValue() : 0);

                    if (preview.getUserName() != null) {
                        chatList.add(preview);
                    }
                }

                chatList.sort((a, b) -> Long.compare(b.getLastTimestamp(), a.getLastTimestamp()));

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat(ChatPreview chat) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, AdminChatFragment.newInstance(chat.getChatKey(), chat.getUserName()))
                .addToBackStack(null)
                .commit();
    }
}