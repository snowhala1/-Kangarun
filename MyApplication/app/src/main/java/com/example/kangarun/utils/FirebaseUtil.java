package com.example.kangarun.utils;

import android.util.Log;

import com.example.kangarun.activity.MainActivity;
import com.example.kangarun.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * @author Runyao Wang u6812566
 */
public class FirebaseUtil {
    public static void loadUsersIntoAVL(UserAVLTree tree_arg) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Access the 'user' collection and retrieve all documents
        db.collection("user").get().addOnCompleteListener(t -> {
            if (t.isSuccessful() && t.getResult() != null) {
                // Iterate over each document in the result set
                for (QueryDocumentSnapshot queryDocumentSnapshot : t.getResult()) {
                    // Extract the 'uid' field as a String and insert it into the AVLTree
                    User user = new User();

                    user.setUsername(queryDocumentSnapshot.getString("username"));
                    user.setEmail(queryDocumentSnapshot.getString("email"));
                    user.setUserId(queryDocumentSnapshot.getString("uid"));
                    user.setGender(queryDocumentSnapshot.getString("gender"));
                    tree_arg.insert(user);
                }
                Log.d("treeComplete", MainActivity.tree.display());
            } else {
                // Handle the case where the Firestore query fails
                if (t.getException() != null) {
                    System.err.println("Error loading users from Firestore: " + t.getException().getMessage());
                }
            }
        });
    }

}
