package com.example.kangarun.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kangarun.User;
import com.example.kangarun.UserListener;
import com.example.kangarun.databinding.ItemContainerBlacklistUserBinding;

import java.util.List;

/**
 * @author Yan Jin u7779907, Bingnan Zhao u6508459
 */
public class BlacklistUserAdapter extends BaseAdapter<BlacklistUserAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public BlacklistUserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder createView(@NonNull ViewGroup parent, int viewType) {
        ItemContainerBlacklistUserBinding binding = ItemContainerBlacklistUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void bindView(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.setUserData(user);

        holder.binding.buttonUnblock.setOnClickListener(v -> {
            if (userListener != null) {
                userListener.onUserUnblocked(user);
            }
        });
    }

    @Override
    public int getDataCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerBlacklistUserBinding binding;

        UserViewHolder(ItemContainerBlacklistUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.getUsername());
            binding.textEmail.setText(user.getEmail());// Set default image
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }
}
