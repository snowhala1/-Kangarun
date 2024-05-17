package com.example.kangarun.activity;

import static com.example.kangarun.activity.LoginActivity.currentUser;
import static com.example.kangarun.utils.FirebaseUtil.loadUsersIntoAVL;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kangarun.R;
import com.example.kangarun.utils.UserAVLTree;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;
import com.hitomi.cmlibrary.OnMenuStatusChangeListener;
import com.squareup.picasso.Picasso;

/**
 * @author Heng Sun u7611510, Qiutong Zeng u7724723,Runyao Wang u6812566
 */
public class MainActivity extends AppCompatActivity {
    public static UserAVLTree tree;
    ImageView profileButton;
    CircleMenu circleMenu;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                currentUser.setUserId(""); // Clear the current user ID
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();
        profileButton = findViewById(R.id.main_profile_image_view);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "User Profile", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                startActivity(intent);
            }
        });

        tree = new UserAVLTree();
        loadUsersIntoAVL(tree); // Load user data into AVL tree

        circleMenu = (CircleMenu) findViewById(R.id.circle_menu);
        // Configure circle menu with main and sub-menu options
        circleMenu.setMainMenu(Color.parseColor("#CDCDCD"), R.drawable.icon_menu, R.drawable.exit)
                .addSubMenu(Color.parseColor("#30A400"), R.drawable.search)
                .addSubMenu(Color.parseColor("#FF4B32"), R.drawable.chat)
                .addSubMenu(Color.parseColor("#258CFF"), R.drawable.sport)
                .addSubMenu(Color.parseColor("#6650a5"), R.drawable.record)
                .addSubMenu(Color.parseColor("#F7AD19"), R.drawable.profile)
                .setOnMenuSelectedListener(new OnMenuSelectedListener() {

                    /**
                     * Handles navigation based on circle menu selection.
                     * @param index The index of the selected menu item.
                     */
                    @Override
                    public void onMenuSelected(int index) {
                        // Handle menu selection to start different activities
                        switch (index) {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                                startActivity(intent);
                                break;
                            case 1:
                                Intent intent1 = new Intent(getApplicationContext(), FriendListActivity.class);
                                startActivity(intent1);
                                break;
                            case 2:
                                Intent intent2 = new Intent(getApplicationContext(), MapsActivity.class);
                                startActivity(intent2);
                                break;
                            case 3:
                                Intent intent3 = new Intent(getApplicationContext(), ExerciseRecordActivity.class);
                                startActivity(intent3);
                                break;
                            case 4:
                                Intent intent4 = new Intent(getApplicationContext(), UserProfileActivity.class);
                                startActivity(intent4);
                                break;

                        }
                    }

                }).setOnMenuStatusChangeListener(new OnMenuStatusChangeListener() {

                    @Override
                    public void onMenuOpened() {
                    }

                    @Override
                    public void onMenuClosed() {
                    }

                });


    }


    /**
     * Ban return button
     */
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the profile image from Firebase storage
        setProfileImage();
    }

    /**
     * Set user's profile image from Firebase storage
     */
    private void setProfileImage() {
        // Locate the user's profile image from Firebase storage
        StorageReference profileRef = storageReference.child("user/" + currentUser.getUserId() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load and set the user's profile image using Picasso
                Picasso.get().load(uri).into(profileButton);
            }
        });
    }
}