package com.example.kangarun.activity;

import static com.example.kangarun.activity.LoginActivity.currentUser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kangarun.Message;
import com.example.kangarun.R;
import com.example.kangarun.User;
import com.example.kangarun.adapter.ChatAdapter;
import com.example.kangarun.databinding.ActivityChatBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author Runyao Wang u6812566,Qiutong Zeng u7724723,Heng Sun u7611510
 */
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "messages";
    private ActivityChatBinding binding;
    private User receiver;
    private List<Message> messageList;
    private ChatAdapter adapter;
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = messageList.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Message m = new Message();
                    m.senderId = documentChange.getDocument().getString("senderId");
                    m.receiverId = documentChange.getDocument().getString("receiverId");
                    m.messageContent = documentChange.getDocument().getString("message");
                    m.datetime = getDateTime(documentChange.getDocument().getDate("time"));
                    m.dateObj = documentChange.getDocument().getDate("time");
                    messageList.add(m);
                }
            }
            messageList.sort(Comparator.comparing(m -> m.dateObj));
            if (count == 0) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(messageList.size(), messageList.size());
                binding.chatRecycleView.smoothScrollToPosition(messageList.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
    };
    private List<String> currentUserBlockList;
    private List<String> receiverBlockList;
    private boolean isCurrentUserBlockListLoaded = false;
    private boolean isReceiverBlockListLoaded = false;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Back button
        binding.imageBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        // Send button
        binding.layoutSend.setOnClickListener(v -> attemptSendMessage());

        receiver = getIntent().getSerializableExtra("user", com.example.kangarun.User.class);
        assert receiver != null;
        binding.textName.setText(receiver.getUsername());

        init();
        listenMessage();

        ImageView profileButton = findViewById(R.id.imageInfo);
        profileButton.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "User Profile", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), FriendProfileActivity.class);
            intent.putExtra("user", receiver);
            //TODO Show current user profile
            startActivity(intent);
        });
    }

    /**
     * Initializes components and loads user block lists.
     */
    private void init() {
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(
                receiver.getUserId(),
                messageList,
                currentUser.getUserId()
        );
        binding.chatRecycleView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        // Load current user's block list
        db.collection("user").document(currentUser.getUserId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentUserBlockList = (List<String>) documentSnapshot.get("blockList");
                isCurrentUserBlockListLoaded = true;
            } else {
                Log.e(TAG, "Current user's block list not found.");
            }
        });

        // Load receiver's block list
        db.collection("user").document(receiver.getUserId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                receiverBlockList = (List<String>) documentSnapshot.get("blockList");
                isReceiverBlockListLoaded = true;
            } else {
                Log.e(TAG, "Receiver's block list not found.");
            }
        });
    }

    /**
     * Attempts to send a message after loading block lists.
     * Checks if users have blocked each other before sending a message.
     */
    private void attemptSendMessage() {
        loadBlockLists();
        if (!isCurrentUserBlockListLoaded || !isReceiverBlockListLoaded) {
            // Ensure both block lists are loaded
            return;
        }
        String senderId = currentUser.getUserId();
        String receiverId = receiver.getUserId();

        if (receiverBlockList != null && receiverBlockList.contains(senderId)) {
            Log.d(TAG, "Sender is blocked by the receiver. Message not sent.");
            Toast.makeText(ChatActivity.this, "You are blocked by this user.", Toast.LENGTH_SHORT).show();
        } else if (currentUserBlockList != null && currentUserBlockList.contains(receiverId)) {
            Log.d(TAG, "Receiver is blocked by the sender. Message not sent.");
            Toast.makeText(ChatActivity.this, "You have blocked this user.", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Neither user is blocked. Sending message...");
            sendMessage();
        }
    }

    /**
     * Loads block lists for both the current user and the receiver from Firestore.
     */
    private void loadBlockLists() {
        // Load current user's block list
        db.collection("user").document(currentUser.getUserId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentUserBlockList = (List<String>) documentSnapshot.get("blockList");
                isCurrentUserBlockListLoaded = true;
            } else {
                Log.e(TAG, "Current user's block list not found.");
            }
        });

        // Load receiver's block list
        db.collection("user").document(receiver.getUserId()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                receiverBlockList = (List<String>) documentSnapshot.get("blockList");
                isReceiverBlockListLoaded = true;
            } else {
                Log.e(TAG, "Receiver's block list not found.");
            }
        });
    }

    /**
     * Sends a chat message to Firestore.
     * Adds a new document to the 'collection_chat' collection with the message details.
     */
    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("senderId", currentUser.getUserId());
        message.put("receiverId", receiver.getUserId());
        message.put("message", binding.inputMessage.getText().toString());
        message.put("time", new Date());
        db.collection("collection_chat")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message written with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                });
        binding.inputMessage.setText(null);
    }

    /**
     * Listens for new messages between the current user and the receiver.
     */
    private void listenMessage() {
        db.collection("collection_chat")
                .whereEqualTo("senderId", currentUser.getUserId())
                .whereEqualTo("receiverId", receiver.getUserId())
                .addSnapshotListener(eventListener);
        db.collection("collection_chat")
                .whereEqualTo("senderId", receiver.getUserId())
                .whereEqualTo("receiverId", currentUser.getUserId())
                .addSnapshotListener(eventListener);
    }
    /**
     * Converts a Date object to a formatted string.
     * @param date the Date object to format
     * @return a formatted string representing the date and time
     */
    private String getDateTime(Date date) {
        return new SimpleDateFormat("yyyy MM-dd - hh:mm a", Locale.getDefault()).format(date);
    }
}
