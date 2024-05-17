package com.example.kangarun.activity;

import static com.example.kangarun.activity.LoginActivity.currentUser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kangarun.R;
import com.example.kangarun.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/**
 * @author Qiutong Zeng u7724723,Bingnan Zhao u6508459,Yan Jin u7779907, Runyao Wang u6812566
 */
public class UserProfileActivity extends AppCompatActivity {
    TextView useremail, username, usergender, userweight, userheight;
    Button updateInfoButton, blacklistButton;
    ImageView profile_image_view;
    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    String currentId;

    public UserProfileActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        username = findViewById(R.id.username);
        useremail = findViewById(R.id.useremail);
        usergender = findViewById(R.id.usergender);
        userweight = findViewById(R.id.userweight);
        userheight = findViewById(R.id.userheight);
        profile_image_view = findViewById(R.id.profile_image_view);
        updateInfoButton = findViewById(R.id.uploadInfoButton);
        blacklistButton = findViewById(R.id.blacklistButton);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Fetching current user ID
        currentId = currentUser != null ? currentUser.getUserId() : null;
        StorageReference profileRef = storageReference.child("user/" + User.getCurrentUserId() + "/profile.jpg");
        // Get user's avatar
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profile_image_view);
            }
        });

        // Back button
        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection("user").document(User.getCurrentUserId());
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value == null) {
                    Log.e("NULL USER", "value is null, skip it");
                    return;
                }
                username.setText("Username: " + value.getString("username"));
                useremail.setText("Email: " + value.getString("email"));
                usergender.setText("Gender: " + value.getString("gender"));
                userweight.setText("Weight: " + String.valueOf(value.getDouble("weight")) + "kg");
                userheight.setText("Height: " + String.valueOf(value.getDouble("height")) + "cm");
            }
        });

        Log.i("User in userprofile", currentId);

        loadUserProfile(currentId);


        updateInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        });

        blacklistButton.setOnClickListener(v -> {
            if (blacklistButton.getText().equals("Blacklist")) {
                if (currentId != null) {
                    blacklistUser();
                } else {
                    Toast.makeText(UserProfileActivity.this, "User IDs are not set", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Load and display the user's profile data from Firestore.
     */
    private void loadUserProfile(String userId) {
        StorageReference profileRef = storageReference.child("user/" + userId + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profile_image_view))
                .addOnFailureListener(e -> Log.e("FirebaseStorage", "Error fetching profile image", e));

        DocumentReference documentReference = firebaseFirestore.collection("user").document(userId);
        documentReference.addSnapshotListener(this, (value, error) -> {
            if (value != null && value.exists()) {
                username.setText("Username: " + value.getString("username"));
                useremail.setText("Email: " + value.getString("email"));
                usergender.setText("Gender: " + value.getString("gender"));
                userweight.setText("Weight: " + value.getDouble("weight") + "kg");
                userheight.setText("Height: " + value.getDouble("height") + "cm");
            }
        });
    }

    /**
     * Method to handle the user blacklisting operation.
     */
    private void blacklistUser() {
        Log.d("BlacklistUser", "User blacklisted successfully!");
        Toast.makeText(UserProfileActivity.this, "User blacklisted", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), BlacklistActivity.class);
        startActivity(intent);
    }
}