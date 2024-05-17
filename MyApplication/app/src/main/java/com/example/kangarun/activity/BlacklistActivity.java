package com.example.kangarun.activity;

import static com.example.kangarun.activity.LoginActivity.currentUser;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kangarun.R;
import com.example.kangarun.User;
import com.example.kangarun.UserListener;
import com.example.kangarun.adapter.BlacklistUserAdapter;
import com.example.kangarun.databinding.ActivityBlacklistBinding;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * BlacklistActivity manages the display and interaction with the user's list of blocked users.
 * @author Yan Jin u7779907
 */
public class BlacklistActivity extends AppCompatActivity implements UserListener {

    private ActivityBlacklistBinding binding;
    private List<User> blockedUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlacklistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup the back button to close the activity
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        // Fetch blocked users if the current user is valid
        if (currentUser != null && currentUser.getUserId() != null) {
            getBlockedUsers();
            setListeners();
        } else {
            binding.textErrorMessage.setText("User ID not set");
            binding.textErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    // Set listener for the back button in the app's UI
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    /**
    Retrieve the list of user IDs that the current user has blocked
    */
    private void getBlockedUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(currentUser.getUserId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                blockedUsers.clear();
                List<String> blockList = (List<String>) task.getResult().get("blockList");
                if (blockList != null && !blockList.isEmpty()) {
                    fetchBlockedUsersDetails(blockList);
                } else {
                    updateUI();
                }
            } else {
                binding.textErrorMessage.setText("Failed to load blocked users");
                binding.textErrorMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Fetch details of blocked users from Firestore
     * @param blockList user's blocklist from firestore
     */
    private void fetchBlockedUsersDetails(List<String> blockList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").get().addOnCompleteListener(t -> {
            if (t.isSuccessful() && t.getResult() != null) {
                List<User> users = new ArrayList<>();

                for (QueryDocumentSnapshot queryDocumentSnapshot : t.getResult()) {
                    String userId = queryDocumentSnapshot.getString("uid");
                    if (blockList.contains(userId)) {

                        User user = new User();
                        user.setUsername(queryDocumentSnapshot.getString("username"));
                        user.setEmail(queryDocumentSnapshot.getString("email"));
                        user.setUserId(userId);
                        blockedUsers.add(user);
                    }
                }
                updateUI();
            }
        });
    }

    /**
     * Update the user interface to display the list of blocked users or a message if no users are blocked
     */
    private void updateUI() {
        if (!blockedUsers.isEmpty()) {
            BlacklistUserAdapter adapter = new BlacklistUserAdapter(blockedUsers, this);
            binding.userRecyclerView.setAdapter(adapter);
            binding.userRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.userRecyclerView.setVisibility(View.VISIBLE);
            binding.textErrorMessage.setVisibility(View.GONE);
        } else {
            binding.userRecyclerView.setVisibility(View.GONE);
            binding.textErrorMessage.setText("No blocked users");
            binding.textErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        // Can be used for future user interactions in the blacklist
    }

    /**
     *  Unblocks a user when they are clicked in the UI
     * @param user user in friend list position
     */
    @Override
    public void onUserUnblocked(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(currentUser.getUserId())
                .update("blockList", FieldValue.arrayRemove(user.getUserId()))
                .addOnSuccessListener(aVoid -> {
                    blockedUsers.remove(user);
                    updateUI();
                    Toast.makeText(this, "User unblocked", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to unblock user", Toast.LENGTH_SHORT).show();
                    Log.e("BlacklistActivity", "Error unblocking user", e);
                });
    }
}
