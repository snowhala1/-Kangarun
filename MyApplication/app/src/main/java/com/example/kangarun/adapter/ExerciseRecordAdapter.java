package com.example.kangarun.adapter;

import static com.example.kangarun.activity.LoginActivity.currentUser;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kangarun.R;
import com.example.kangarun.activity.ExerciseRecordDetailActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author Heng Sun u7611510,Bingnan Zhao u6508459
 */
public class ExerciseRecordAdapter extends BaseAdapter<ExerciseRecordAdapter.RecordViewHolder> {

    private List<DocumentSnapshot> recordsList;

    public ExerciseRecordAdapter(List<DocumentSnapshot> recordsList) {
        this.recordsList = recordsList;
    }

    @Override
    protected RecordViewHolder createView(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    protected void bindView(RecordViewHolder holder, int position) {
        DocumentSnapshot document = recordsList.get(position);
        String date = document.getString("date");

        double distance = document.getDouble("distance");

        DecimalFormat dfDistance = new DecimalFormat("#.##");
        String formattedDistance = dfDistance.format(distance);

        String duration = document.getString("duration");
        double calories = document.getDouble("calories");
        String formattedCalories = dfDistance.format(calories);

        Log.d("Adapter", position + " " + date + " " + distance + " " + duration + " " + calories);
        String path = "exerciseRecord/" + currentUser.getUserId() + date + "/mapSnapshot.png";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(path);

        holder.textViewDate.setText(date);
        holder.textViewDistance.setText("Distance: " + formattedDistance + " m");
        holder.textViewDuration.setText("Duration: " + duration);
        holder.textViewCalories.setText("Calories: " + formattedCalories + " kcal");

        holder.itemView.setOnClickListener(v -> {
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    Intent intent = new Intent(holder.itemView.getContext(), ExerciseRecordDetailActivity.class);
                    intent.putExtra("date", date);
                    intent.putExtra("distance", formattedDistance);
                    intent.putExtra("duration", duration);
                    intent.putExtra("calories", formattedCalories);
                    intent.putExtra("imagePath", uri.toString());
                    holder.itemView.getContext().startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("FirebaseStorage", "Error getting the image URL", e);
                    Toast.makeText(holder.itemView.getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected int getDataCount() {
        return recordsList.size();
    }

    @Override
    protected int getLayoutId(int viewType) {
        return R.layout.record_item; // 提供布局 ID
    }

    public void updateData(List<DocumentSnapshot> newData) {
        Log.d("Adapter", "Updating data with " + newData.size() + " records");
        recordsList.clear();
        recordsList.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public int getItemCount() {
        Log.d("Adapter", "size = " + recordsList.size());
        return recordsList.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDistance;
        TextView textViewDuration;
        TextView textViewCalories;
        TextView textViewDate;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDistance = itemView.findViewById(R.id.textViewDistance);
            textViewDuration = itemView.findViewById(R.id.textViewDuration);
            textViewCalories = itemView.findViewById(R.id.textViewCalories);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }
}

