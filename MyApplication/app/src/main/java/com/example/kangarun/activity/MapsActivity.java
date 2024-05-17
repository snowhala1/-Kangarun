package com.example.kangarun.activity;

import static com.example.kangarun.activity.LoginActivity.currentUser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kangarun.R;
import com.example.kangarun.databinding.ActivityMapsBinding;
import com.example.kangarun.utils.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Heng Sun u7611510, Bingnan Zhao u6508459
 */
public class MapsActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean permissionDenied = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private UiSettings mUiSettings;
    private ActivityMapsBinding binding;
    private LatLng mCurrentLocation; //records current location after clicking start exercise button
    private List<LatLng> mPathPoints = new ArrayList<>(); // store all coordinates in path
    private Polyline mPolyline; // used to draw poly line
    private Timer mPathTimer = null; // timer used for draw path
    private Timer mDurationTimer = null; //timer used for calculate duration
    private long mStartTimeMillis = 0; //record time stamp when start exercise
    private double distance = 0;//record the distance of exercise
    private String duration;
    private String exerciseDate;
    private double calories;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        String uid = currentUser.getUserId();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button startExerciseButton = findViewById(R.id.start_button);
        startExerciseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startExerciseButton.getText().equals("Start")) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
                    Date date = new Date(System.currentTimeMillis());
                    exerciseDate = simpleDateFormat.format(date);//get Current time
                    startExerciseButton.setText(getString(R.string.stop));
                    Toast.makeText(getApplicationContext(), "Start Exercise!", Toast.LENGTH_SHORT).show();
                    startDrawingPath();
                    startTiming();
                } else if (startExerciseButton.getText().equals("Stop")) {
                    startExerciseButton.setText(getString(R.string.save));
                    Toast.makeText(getApplicationContext(), "Stop Exercise", Toast.LENGTH_SHORT).show();
                    stopDrawingPath();
                    stopTiming();
                    captureMapSnapshot();
                } else if (startExerciseButton.getText().equals("Save Exercise record")) {
                    Toast.makeText(getApplicationContext(), "Exercise Record Saved", Toast.LENGTH_SHORT).show();
                    //Collect" uid dateStr,timeDisplay,calories,distance" and store
                    Map<String, Object> exerciseRecord = new HashMap<>();

                    exerciseRecord.put("uid", uid);
                    exerciseRecord.put("date", exerciseDate);
                    exerciseRecord.put("distance", distance);
                    exerciseRecord.put("duration", duration);
                    exerciseRecord.put("calories", calories);
                    db.collection("exerciseRecord").document(uid + exerciseDate)
                            .set(exerciseRecord)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                    captureMapSnapshot();
                    Intent goToMain = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(goToMain);
                }
            }
        });
        // Add the back button functionality
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

    }


    /**
     * drawing path on map
     */
    private void startDrawingPath() {
        mPathTimer = new Timer();
        mPathTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mMap != null) {
                    getCurrentLocation();
                    if (mCurrentLocation != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updatePath();
                            }
                        });
                    }
                }
            }
        }, 0, 1000);
    }

    /**
     * stop drawing path on map
     */
    private void stopDrawingPath() {
        if (mPathTimer != null) {
            mPathTimer.cancel();
            mPathTimer = null;
        }
    }

    /**
     * start calculating exercise duration
     */
    private void startTiming() {
        // Start counting from 00:00
        mStartTimeMillis = System.currentTimeMillis();
        mDurationTimer = new Timer();
        mDurationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long elapsedTimeMillis = System.currentTimeMillis() - mStartTimeMillis;
                long elapsedSeconds = elapsedTimeMillis / 1000;
                long secondsDisplay = elapsedSeconds % 60;
                long elapsedMinutes = elapsedSeconds / 60;
                duration = String.format("%02d:%02d", elapsedMinutes, secondsDisplay);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDurationTextView(duration);
                    }
                });
            }
        }, 0, 1000);
    }

    /**
     * stop calculating exercise duration
     */
    private void stopTiming() {
        if (mDurationTimer != null) {
            mDurationTimer.cancel();
            mDurationTimer = null;
        }
    }

    /**
     * Calculate distance between two points in latitude and longitude.
     *
     * @param start LatLng of start point
     * @param end   LatLng of end point
     * @return distance in meters
     */
    private float calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }

    /**
     * get current location
     */
    private void getCurrentLocation() {
        if (mFusedLocationClient != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }
            });
        }
    }

    /**
     * update path e.g. polyline
     */
    private void updatePath() {
        if (mCurrentLocation != null) {
            if (!mPathPoints.isEmpty()) {
                LatLng lastPoint = mPathPoints.get(mPathPoints.size() - 1);
                distance += calculateDistance(lastPoint, mCurrentLocation);
            }
            mPathPoints.add(mCurrentLocation); // add the current position to path list points.
            if (mPathPoints.size() > 1) {
                if (mPolyline != null) {
                    mPolyline.remove(); //remove last poly line
                }

                // generate poly line
                PolylineOptions polylineOptions = new PolylineOptions()
                        .color(Color.BLUE)
                        .width(20);

                for (LatLng point : mPathPoints) {
                    polylineOptions.add(point);
                    System.out.println(point);
                }

                mPolyline = mMap.addPolyline(polylineOptions);
            }
            updateDistanceTextView(distance);
        }
    }

    /**
     * updating text in duration textview
     *
     * @param timeDisplay the duration of time need to be display on textview
     */
    private void updateDurationTextView(String timeDisplay) {
        TextView durationTextView = findViewById(R.id.duration_text);
        durationTextView.setText(timeDisplay);
    }

    /**
     * update distance display TextView
     *
     * @param distance meters
     */
    private void updateDistanceTextView(double distance) {
        TextView distanceTextView = findViewById(R.id.distance);
        distanceTextView.setText(String.format("%.2f km", distance / 1000));
        calories = calculateCalories(distance, System.currentTimeMillis() - mStartTimeMillis, 70); //
        updateCaloriesTextView(calories);
    }

    /**
     * Calculate calories burned based on weight, time, and speed.
     *
     * @param distanceMeters Distance in meters
     * @param timeMillis     Time in milliseconds
     * @param weightKg       Weight in kilograms
     * @return Calories burned
     */
    private double calculateCalories(double distanceMeters, long timeMillis, double weightKg) {
        double timeHours = timeMillis / 3600000.0; // convert time to hours
        double speedMinPer400m = (timeMillis / 60000.0) / (distanceMeters / 400.0); // calculate speed（minutes per 400 meters）

        double K = 30 / speedMinPer400m; // gauge index k
        return weightKg * timeHours * K; // return calories
    }

    /**
     * Update the text in the calories TextView
     *
     * @param calories Number of calories burned
     */
    private void updateCaloriesTextView(double calories) {
        TextView caloriesTextView = findViewById(R.id.calories_text);
        caloriesTextView.setText(String.format("%.0f cal", calories));
    }

    private void captureMapSnapshot() {
        if (mMap == null) {
            Log.d("MapSnapshot", "Map not initialized");
            return;  // make sure the map is loaded
        }
        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                uploadMapSnapshotToFirebase(byteArray);
            }
        });
    }

    private void uploadMapSnapshotToFirebase(byte[] imageBytes) {
        String filePath = "exerciseRecord/" + currentUser.getUserId() + exerciseDate + "/mapSnapshot.png";
        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(filePath);

        // upload to Firebase Storage
        fileRef.putBytes(imageBytes)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("FirebaseStorage", "Snapshot uploaded successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FirebaseStorage", "Snapshot upload failed", e);
                    }
                });
    }


    /**
     * Returns whether the checkbox with the given id is checked.
     */
    private boolean isChecked(int id) {
        return ((CheckBox) findViewById(id)).isChecked();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(isChecked(R.id.zoom_buttons_toggle));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                }
            });
        } else {
            enableMyLocation();
        }
    }

    /**
     * Checks if the map is ready (which depends on whether the Google Play services APK is
     * available. This should be called prior to calling any methods on GoogleMap.
     */
    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the zoom controls (+/- buttons in the bottom-right of the map for LTR
        // locale or bottom-left for RTL locale).
        mUiSettings.setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG)
                .show();
        System.out.println(location);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
                .show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}