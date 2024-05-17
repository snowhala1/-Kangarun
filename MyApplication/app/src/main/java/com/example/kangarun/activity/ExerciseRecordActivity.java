package com.example.kangarun.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kangarun.LoginState;
import com.example.kangarun.R;
import com.example.kangarun.adapter.ExerciseRecordAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Bingnan Zhao u6508459
 */
public class ExerciseRecordActivity extends AppCompatActivity {

    public List<DocumentSnapshot> list;
    public List<DocumentSnapshot> dateDeslist;
    public List<DocumentSnapshot> dateAsclist;
    public List<DocumentSnapshot> distanceDeslist;
    public List<DocumentSnapshot> distanceAsclist;
    public List<DocumentSnapshot> durationDeslist;
    public List<DocumentSnapshot> durationAsclist;
    FirebaseFirestore db;
    private boolean dateDescending;
    private boolean distanceDescending;
    private boolean durationDescending;
    private List<DocumentSnapshot> allRecords;
    private ExerciseRecordAdapter adapter;
    private Button sortByDateButton;
    private Button sortByDistanceButton;
    private Button sortByDurationButton;
    private ImageView imageBack;
    private LineChart chart;

    /**
     * Initializes the activity with necessary views, adapters, and database connections.
     * Sets up the UI components such as RecyclerView, Chart, and Buttons, and initializes Firebase instances.
     * Sets listeners for system window insets, handles Firestore data fetching, and initializes sort functionalities.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<>();

        dateDescending = true;
        distanceDescending = false;
        durationDescending = false;

        setContentView(R.layout.activity_exercise_record);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.exercise_record_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.exerciseRecordView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseRecordAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        chart = findViewById(R.id.exerciseChart);

        // Back button
        imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        CollectionReference records = db.collection("exerciseRecord");
        allRecords = new ArrayList<>();
        LoginState currentUser = LoginState.getInstance();
        String uid = currentUser.getUserId();
        if (uid != null) {
            records.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    allRecords.clear();
                    for (DocumentSnapshot document : task.getResult()) {
                        allRecords.add(document);
                        loadUserRecords();
                        updateChart(dateDeslist);
                    }
                    Log.d("Firestore", "Total documents fetched: " + allRecords.size());
                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                }
            });

        }
        sortByDateButton = findViewById(R.id.SortByDateButton);
        sortByDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Sort By Date", Toast.LENGTH_SHORT).show();
                if (!dateAsclist.isEmpty() && !dateDeslist.isEmpty())
                    adapter.updateData(dateDescending ? dateAsclist : dateDeslist);
                dateDescending = !dateDescending;
                toggleSortDirection(dateDescending, sortByDateButton);
            }
        });
        sortByDistanceButton = findViewById(R.id.SortByDistanceButton);
        sortByDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Sort By Distance", Toast.LENGTH_SHORT).show();
                if (!distanceAsclist.isEmpty() && !distanceDeslist.isEmpty())
                    adapter.updateData(distanceDescending ? distanceAsclist : distanceDeslist);
                distanceDescending = !distanceDescending;
                toggleSortDirection(distanceDescending, sortByDistanceButton);
            }
        });
        sortByDurationButton = findViewById(R.id.SortByDurationButton);
        sortByDurationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Sort By Duration", Toast.LENGTH_SHORT).show();
                if (!durationAsclist.isEmpty() && !durationDeslist.isEmpty())
                    adapter.updateData(durationDescending ? durationAsclist : durationDeslist);
                durationDescending = !durationDescending;
                toggleSortDirection(durationDescending, sortByDurationButton);
            }
        });

        EdgeToEdge.enable(this);
    }

    /**
     * Updates and styles the chart based on a list of Firestore document snapshots.
     * This method processes the data into chart entries, sets the visual aspects of the chart,
     * and refreshes the display.
     *
     * @param recordsList List of DocumentSnapshot objects to be transformed into chart data.
     */
    private void updateChart(List<DocumentSnapshot> recordsList) {
        List<Entry> entries = extractEntriesFromRecords(recordsList);
        LineDataSet dataSet = new LineDataSet(entries, "Daily Distance");
        LineData lineData = new LineData(dataSet);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawLabels(false);
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(3f);
        Description description = new Description();
        description.setEnabled(false);
        chart.setDescription(description);
        chart.setData(lineData);
        chart.invalidate();
    }

    /**
     * Converts a list of DocumentSnapshot into chart entries based on daily distances.
     * This method aggregates distances per day for the past week and formats them into
     * chart entries。
     *
     * @param recordsList The list of Firestore DocumentSnapshot to be processed.
     * @return A list of Entry objects, each representing a day's total distance over the past week.
     */
    private List<Entry> extractEntriesFromRecords(List<DocumentSnapshot> recordsList) {
        Map<Integer, Float> dailyDistances = new LinkedHashMap<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_YEAR);
        for (int i = 0; i < 7; i++) {
            dailyDistances.put(i + 1, 0.0f);
        }

        for (DocumentSnapshot doc : recordsList) {
            try {
                Date date = inputFormat.parse(doc.getString("date"));
                cal.setTime(date);
                int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                int daysAgo = today - dayOfYear;

                if (daysAgo >= 0 && daysAgo < 7) {
                    Double distance = doc.getDouble("distance");
                    if (distance != null && distance < 100000000) {
                        float floatDistance = distance.floatValue();
                        dailyDistances.put(7 - daysAgo, dailyDistances.get(7 - daysAgo) + floatDistance);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            entries.add(new Entry(i, dailyDistances.get(i)));
        }
        return entries;
    }

    /**
     * The method retrieves the current user's ID, filters the documents from {@code allRecords}
     * to find those that belong to the user, and updates the UI components accordingly. If the
     * user ID is not found, no action is taken.
     */
    public void loadUserRecords() {
        LoginState currentUser = LoginState.getInstance();
        String uid = currentUser.getUserId();
        Log.d("ExerciseRecord", "uid:" + uid);
        if (uid != null) {
            list.clear();
            for (DocumentSnapshot document : allRecords) {
                if (uid.equals(document.getString("uid"))) {
                    list.add(document);
                }
            }
            Log.d("Firestore", "Total user documents fetched: " + list.size());
            implementSortLists();
            adapter.updateData(dateDescending ? dateDeslist : dateAsclist);
        }
    }

    /**
     * Sorts records by date, distance, and duration in both ascending and descending orders.
     * Initializes sorted lists from a base list and applies custom comparators for each category.
     */
    public void implementSortLists() {
        dateDeslist = new ArrayList<>(list);
        distanceDeslist = new ArrayList<>(list);
        durationDeslist = new ArrayList<>(list);
        dateAsclist = new ArrayList<>(list);
        distanceAsclist = new ArrayList<>(list);
        durationAsclist = new ArrayList<>(list);
        Collections.sort(dateAsclist, new Comparator<DocumentSnapshot>() {
            @Override
            public int compare(DocumentSnapshot doc1, DocumentSnapshot doc2) {
                String date1 = doc1.contains("date") ? doc1.getString("date") : "0";
                String date2 = doc2.contains("date") ? doc2.getString("date") : "0";
                return date1.compareTo(date2);
            }
        });
        dateDeslist = new ArrayList<>(dateAsclist);
        Collections.reverse(dateDeslist);
        Collections.sort(distanceAsclist, new Comparator<DocumentSnapshot>() {
            @Override
            public int compare(DocumentSnapshot doc1, DocumentSnapshot doc2) {
                double distance1 = doc1.contains("distance") ? doc1.getDouble("distance") : 0;
                double distance2 = doc2.contains("distance") ? doc2.getDouble("distance") : 0;
                return Double.compare(distance1, distance2);
            }
        });
        distanceDeslist = new ArrayList<>(distanceAsclist);
        Collections.reverse(distanceDeslist);
        Collections.sort(durationAsclist, new Comparator<DocumentSnapshot>() {
            @Override
            public int compare(DocumentSnapshot doc1, DocumentSnapshot doc2) {
                String duration1 = doc1.contains("duration") ? doc1.getString("duration") : "0";
                String duration2 = doc2.contains("duration") ? doc2.getString("duration") : "0";
                return duration1.compareTo(duration2);
            }
        });
        durationDeslist = new ArrayList<>(durationAsclist);
        Collections.reverse(durationDeslist);
    }

    /**
     * Toggles the sort direction indicator on buttons related to sorting.
     * This method updates the visual indicator for sorting direction (ascending or descending) on a specified button.
     *
     * @param descending Indicates the sort direction; true for descending, false for ascending.
     * @param button The button on which to display the sort direction indicator.
     */
    private void toggleSortDirection(boolean descending, Button button) {
        sortByDateButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        sortByDistanceButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        sortByDurationButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (descending) {
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_sort_descending, 0);
        } else {
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_sort_ascending, 0);
        }
    }
}