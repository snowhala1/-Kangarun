package com.example.kangarun.utils;

import com.example.kangarun.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Runyao Wang u6812566
 */
public class UserAVLTree {
    private UserNode root;

    private static class UserNode {
        User value;
        UserNode left;
        UserNode right;
        int height;

        UserNode(User value) {
            this.value = value;
            this.height = 1;
        }
    }

    public void insert(User user) {
        root = insert(root, user);
    }

    private UserNode insert(UserNode node, User user) {
        if (node == null) {
            return new UserNode(user);
        }

        int cmp = user.compareTo(node.value);
        if (cmp < 0) {
            node.left = insert(node.left, user);
        } else if (cmp > 0) {
            node.right = insert(node.right, user);
        } else {
            return node;
        }

        return rebalance(node);
    }

    private UserNode rebalance(UserNode node) {
        updateHeight(node);
        int balance = getBalance(node);
        if (balance > 1) {
            if (getBalance(node.left) < 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if (balance < -1) {
            if (getBalance(node.right) > 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    private UserNode rotateRight(UserNode y) {
        UserNode x = y.left;
        UserNode z = x.right;
        x.right = y;
        y.left = z;
        updateHeight(y);
        updateHeight(x);
        return x;
    }

    private UserNode rotateLeft(UserNode x) {
        UserNode y = x.right;
        UserNode z = y.left;
        y.left = x;
        x.right = z;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    private void updateHeight(UserNode n) {
        n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    private int height(UserNode n) {
        return n == null ? 0 : n.height;
    }

    private int getBalance(UserNode n) {
        return (n == null) ? 0 : height(n.left) - height(n.right);
    }

    // Normal search for username
    public List<User> searchPartial(String query) {
        List<User> results = new ArrayList<>();
        searchPartialHelper(root, query.toLowerCase(), results);
        return results;
    }

    private void searchPartialHelper(UserNode node, String query, List<User> results) {
        if (node == null) return;
        if (node.value.getUsername().toLowerCase().contains(query)) {
            results.add(node.value);
        }
        searchPartialHelper(node.left, query, results);
        searchPartialHelper(node.right, query, results);
    }

    // Token search by query
    public List<User> searchToken(Map<String, String> query) {
        List<User> results = new ArrayList<>();
        searchTokenHelper(root, query, results);
        return results;
    }
    private void searchTokenHelper(UserNode node, Map<String, String> query, List<User> results) {
        if (node == null) return;

        // Ensure there is a user object and it matches all provided query fields.
        if (node.value != null) {
            boolean match = true;

            // Check if the username is in the query and matches the current node's username
            if (query.containsKey("username") && query.get("username") != null) {
                String queryUsername = query.get("username").toLowerCase();
                match &= node.value.getUsername().toLowerCase().contains(queryUsername);
            }

            // Check if the email is in the query and matches the current node's email
            if (query.containsKey("email") && query.get("email") != null) {
                String queryEmail = query.get("email").toLowerCase();
                match &= node.value.getEmail().toLowerCase().contains(queryEmail);
            }

            // Check if the gender is in the query and matches the current node's gender
            if (query.containsKey("gender") && query.get("gender") != null) {
                String queryGender = query.get("gender").toLowerCase();
                String gender = node.value.getGender();
                if (gender == null){
                    gender = "o";
                } else if(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("m")){
                    gender = "m";
                } else if (gender.equalsIgnoreCase("female") || gender.equalsIgnoreCase("f")) {
                    gender = "f";
                } else {
                    gender = "o";
                }
                match &= gender.equals(queryGender);
            }

            // If all applicable checks are true, add the user to the results
            if (match) {
                results.add(node.value);
            }
        }

        // Recursively search in the left and right subtrees
        searchTokenHelper(node.left, query, results);
        searchTokenHelper(node.right, query, results);
    }

    // Display the tree structure
    public String display() {
        if (root == null) return "The tree is empty";
        return display(root, 0);
    }

    private String display(UserNode node, int tabs) {
        if (node == null) {
            return "\t".repeat(tabs) + "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(node.value.toString());
        sb.append("\n");

        if (node.left != null || node.right != null) {
            sb.append("\t".repeat(tabs)).append("├─").append(display(node.left, tabs + 1));
            sb.append("\n").append("\t".repeat(tabs)).append("├─").append(display(node.right, tabs + 1));
        }

        return sb.toString();
    }
}
