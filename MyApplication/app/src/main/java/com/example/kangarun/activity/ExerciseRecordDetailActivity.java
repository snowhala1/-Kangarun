package com.example.kangarun.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kangarun.R;
import com.squareup.picasso.Picasso;

/**
 * @author Heng Sun u7611510
 */
public class ExerciseRecordDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_record_detail);

        TextView textViewDate = findViewById(R.id.textViewDate);
        TextView textViewDistance = findViewById(R.id.textViewDistance);
        TextView textViewDuration = findViewById(R.id.textViewDuration);
        TextView textViewCalories = findViewById(R.id.textViewCalories);
        ImageView imageViewMapSnapshot = findViewById(R.id.imageViewMapSnapshot);

        //
        String date = getIntent().getStringExtra("date");
        String distance = getIntent().getStringExtra("distance");
        String duration = getIntent().getStringExtra("duration");
        String calories = getIntent().getStringExtra("calories");
        String imagePath = getIntent().getStringExtra("imagePath");

        textViewDate.setText("Date " + date);
        textViewDistance.setText("Distance: " + distance + " m");
        textViewDuration.setText("Duration: " + duration);
        textViewCalories.setText("Calories: " + calories + " kcal");

        // Load images from Firebase Storage
        Picasso.get().load(imagePath).into(imageViewMapSnapshot);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

    }
}