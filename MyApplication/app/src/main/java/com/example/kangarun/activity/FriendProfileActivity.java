package com.example.kangarun.activity;

import static com.example.kangarun.activity.LoginActivity.currentUser;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kangarun.R;
import com.example.kangarun.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

/**
 * @author Runyao Wang u6812566,Qiutong Zeng u7724723,Yan Jin u7779907
 */
public class FriendProfileActivity extends AppCompatActivity {
    TextView useremail, username, usergender, userweight, userheight;
    Button blockUserButton, addFriendButton;
    ImageView profile_image_view;
    StorageReference storageReference;
    String profileId, currentId;
    private FirebaseFirestore firebaseFirestore;

    public FriendProfileActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        User user = (User) getIntent().getSerializableExtra("user", User.class);

        username = findViewById(R.id.username);
        useremail = findViewById(R.id.useremail);
        usergender = findViewById(R.id.usergender);
        userweight = findViewById(R.id.userweight);
        userheight = findViewById(R.id.userheight);
        profile_image_view = findViewById(R.id.profile_image_view);
        blockUserButton = findViewById(R.id.blockUserButton);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Back button
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        StorageReference profileRef = storageReference.child("user/" + user.getUserId() + "/profile.jpg");
        DocumentReference profileDocRef = firebaseFirestore.collection("user").document(user.getUserId());

        profileDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                profileId = documentSnapshot.getString("uid");
            } else {
                System.out.println("No such document!");
            }
        }).addOnFailureListener(e -> {
            System.err.println("Error fetching document: " + e.getMessage());
        });

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profile_image_view);
            }
        });
        currentId = currentUser.getUserId();
        profileDocRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                username.setText("Username: " + value.getString("username"));
                useremail.setText("Email: " + value.getString("email"));
                usergender.setText("Gender: " + value.getString("gender"));
                userweight.setText("Weight: " + String.valueOf(value.getDouble("weight")) + "kg");
                userheight.setText("Height: " + String.valueOf(value.getDouble("height")) + "cm");
            }
        });

        // Add/Delete friend button
        addFriendButton = findViewById(R.id.addFriendButton);
        profileDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot != null && snapshot.exists()) {
                    List<String> friendList = (List<String>) snapshot.get("friendList");
                    if (friendList != null && friendList.contains(currentId)) {
                        // Change button text to "Delete Friend"
                        addFriendButton.setText("Delete Friend");
                        setupDeleteFriendButton(profileDocRef);
                    } else {
                        // Change button text to "Add Friend"
                        addFriendButton.setText("Add Friend");
                        setupAddFriendButton(profileDocRef);
                    }
                }
            }
        });
        blockUserButton.setOnClickListener(v -> {
            if (blockUserButton.getText().equals("Block")) {
                blockUser();
            } else {
                unblockUser();
            }
        });

    }

    /**
     * Configures the delete friend button to remove the current user from the friend's friend list.
     * @param profileDocRef The document reference of the profile user.
     */
    private void setupDeleteFriendButton(DocumentReference profileDocRef) {
        DocumentReference currentDocRef = firebaseFirestore.collection("user").document(currentId);
        addFriendButton.setOnClickListener(v -> {
            profileDocRef.update("friendList", FieldValue.arrayRemove(currentId))
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Friend", "Friend removed successfully!");
                    })
                    .addOnFailureListener(e -> Log.e("Friend", "Error removing friend", e));
            currentDocRef.update("friendList", FieldValue.arrayRemove(profileId))
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Friend", "Friend removed successfully!");
                    })
                    .addOnFailureListener(e -> Log.e("Friend", "Error removing friend", e));
            addFriendButton.setText("Add Friend");
            setupAddFriendButton(profileDocRef); // Switch functionality to add
        });

    }

    /**
     * Configures the add friend button to add the current user to the friend's friend list.
     * @param profileDocRef The document reference of the profile user's Firestore document.
     */
    private void setupAddFriendButton(DocumentReference profileDocRef) {
        DocumentReference currentDocRef = firebaseFirestore.collection("user").document(currentId);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add friendUserId to the 'friends' array of the current user
                profileDocRef.update("friendList", FieldValue.arrayUnion(currentId))
                        .addOnSuccessListener(aVoid -> {
                            Log.d("AddFriend", "Friend added successfully!");
                            // Handle successful addition here
                        })
                        .addOnFailureListener(e -> {
                            Log.w("AddFriend", "Error adding friend", e);
                            if (e.getMessage().contains("No document to update")) {
                                // Document does not exist, so create the 'friends' field and set its initial value
                                profileDocRef.set(
                                        Collections.singletonMap("friends", Collections.singletonList(currentId))
                                ).addOnSuccessListener(aVoid -> Log.d("AddFriend", "Document created and friend added!"));
                            }
                        });
                currentDocRef.update("friendList", FieldValue.arrayUnion(profileId))
                        .addOnSuccessListener(aVoid -> {
                            Log.d("AddFriend", "Friend added successfully!");
                            // Handle successful addition here
                        })
                        .addOnFailureListener(e -> {
                            Log.w("AddFriend", "Error adding friend", e);
                            if (e.getMessage().contains("No document to update")) {
                                // Document does not exist, so create the 'friends' field and set its initial value
                                currentDocRef.set(
                                        Collections.singletonMap("friends", Collections.singletonList(profileId))
                                ).addOnSuccessListener(aVoid -> Log.d("AddFriend", "Document created and friend added!"));
                            }
                        });
                addFriendButton.setText("Delete Friend");
                setupDeleteFriendButton(profileDocRef);
            }
        });
    }

    /**
     * Configures the block user functionality to remove the friend from both users' friend lists and add to the block list.
     */
    private void blockUser() {
        DocumentReference currentDocRef = firebaseFirestore.collection("user").document(currentId);
        DocumentReference profileDocRef = firebaseFirestore.collection("user").document(profileId);
        profileDocRef.update("friendList", FieldValue.arrayRemove(currentId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Friend", "Friend removed successfully!");
                })
                .addOnFailureListener(e -> Log.e("Friend", "Error removing friend", e));
        currentDocRef.update("friendList", FieldValue.arrayRemove(profileId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Friend", "Friend removed successfully!");
                })
                .addOnFailureListener(e -> Log.e("Friend", "Error removing friend", e));
        addFriendButton.setText("Add Friend");
        setupAddFriendButton(profileDocRef);
        currentDocRef.update(
                        "blockList", FieldValue.arrayUnion(profileId)
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d("BlockUser", "User blocked successfully!");
                    blockUserButton.setText("Unblock");
                    Toast.makeText(FriendProfileActivity.this, "User blocked", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("BlockUser", "Error blocking user", e));
    }

    /**
     * Configures the unblock user functionality to remove the user from the current user's block list.
     */
    private void unblockUser() {
        DocumentReference currentDocRef = firebaseFirestore.collection("user").document(currentId);
        currentDocRef.update("blockList", FieldValue.arrayRemove(profileId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("UnblockUser", "User unblocked successfully!");
                    blockUserButton.setText("Block");
                    Toast.makeText(FriendProfileActivity.this, "User unblocked", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("UnblockUser", "Error unblocking user", e));
    }

}