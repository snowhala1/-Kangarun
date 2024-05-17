package com.example.kangarun.activity;

import static com.example.kangarun.activity.LoginActivity.currentUser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kangarun.User;
import com.example.kangarun.UserListener;
import com.example.kangarun.adapter.UserAdapter;
import com.example.kangarun.databinding.ActivityFriendListBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wang u6812566,Yan Jin u7779907
 */
public class FriendListActivity extends AppCompatActivity implements UserListener {

    private ActivityFriendListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getUsers();
        setListeners();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Fetches and displays the list of friends from Firestore, excluding any blocked users.
     */
    private void getUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUid = currentUser.getUserId();

        // Get the current user's document to retrieve their friendList and blockList
        DocumentReference currentUserRef = db.collection("user").document(currentUid);
        currentUserRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> currentUserFriendList = (List<String>) documentSnapshot.get("friendList");
                List<String> currentUserBlockList = (List<String>) documentSnapshot.get("blockList");

                if (currentUserFriendList != null && currentUserBlockList != null) {
                    // Query all users
                    db.collection("user").get().addOnCompleteListener(t -> {
                        if (t.isSuccessful() && t.getResult() != null) {
                            List<User> users = new ArrayList<>();

                            for (QueryDocumentSnapshot queryDocumentSnapshot : t.getResult()) {
                                String userId = queryDocumentSnapshot.getString("uid");
                                if (!currentUid.equals(userId) &&
                                        currentUserFriendList.contains(userId) &&
                                        !currentUserBlockList.contains(userId)) {

                                    User user = new User();
                                    user.setUsername(queryDocumentSnapshot.getString("username"));
                                    user.setEmail(queryDocumentSnapshot.getString("email"));
                                    user.setUserId(userId);
                                    users.add(user);
                                }
                            }

                            if (!users.isEmpty()) {
                                UserAdapter adapter = new UserAdapter(users, this);
                                binding.userRecyclerView.setAdapter(adapter);
                                binding.userRecyclerView.setVisibility(View.VISIBLE);
                            } else {
                                // Handle empty state
                                binding.userRecyclerView.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e("getUsers", "Error getting documents: ", t.getException());
                        }
                    });
                }
            } else {
                Log.e("getUser", "Current user document does not exist");
            }
        }).addOnFailureListener(e -> Log.e("getUser", "Error fetching current user data", e));
    }

    /**
     * Placeholder method for unblocking users. Currently does not perform any operations.
     * @param user The user to potentially unblock.
     */
    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), FriendProfileActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    @Override
    public void onUserUnblocked(User user) {

    }


}

