package com.example.kangarun.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kangarun.Message;
import com.example.kangarun.databinding.ReceivedMessageBinding;
import com.example.kangarun.databinding.SentMessageBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * ChatAdapter is responsible for displaying chat messages in a RecyclerView.
 * It handles both sent and received messages, showing different layouts for each type.
 * @author Runyao Wang u6812566
 */
public class ChatAdapter extends BaseAdapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    private final String receiverId;
    private final List<Message> messageList;
    private final String senderId;

    public ChatAdapter(String receiverId, List<Message> messageList, String senderId) {
        this.receiverId = receiverId;
        this.messageList = messageList;
        this.senderId = senderId;
    }

    /**
     * Creates a new view holder based on the view type.
     * @param parent The parent view group.
     * @param viewType The type of the view.
     * @return A new instance of the view holder.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder createView(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    SentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()), parent, false)
            );
        }

    }

    /**
     * Binds data to the view holder.
     *
     * @param holder The view holder.
     * @param position The position of the item in the data set.
     */
    @Override
    public void bindView(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setMessage(messageList.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setMessage(messageList.get(position), this.receiverId);
        }
    }

    /**
     * Gets the number of items in the data set.
     * @return The number of items in the data set.
     */
    @Override
    public int getDataCount() {
        return messageList.size();
    }

    /**
     * Returns the view type of the item at the specified position.
     * @param position The position of the item within the adapter's data set.
     * @return The view type of the item at the specified position.
     */
    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final SentMessageBinding binding;

        SentMessageViewHolder(SentMessageBinding sentMessageBinding) {
            super(sentMessageBinding.getRoot());
            binding = sentMessageBinding;
        }

        void setMessage(Message m) {
            binding.textMessage.setText(m.messageContent);
            binding.textTime.setText(m.datetime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ReceivedMessageBinding receivedMessageBinding) {
            super(receivedMessageBinding.getRoot());
            binding = receivedMessageBinding;
        }

        /**
         * Binds a message to the view holder and loads the receiver's profile image.
         * @param m The message to bind.
         * @param receiverId The ID of the receiver.
         */
        void setMessage(Message m, String receiverId) {
            binding.textMessage.setText(m.messageContent);
            binding.textTime.setText(m.datetime);
            StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                    .child("user/" + receiverId + "/profile.jpg");
            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(binding.imageProfile);
                }
            });
        }
    }
}
