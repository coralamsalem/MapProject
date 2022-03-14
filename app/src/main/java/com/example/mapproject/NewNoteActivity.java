package com.example.mapproject;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.Calendar;
import java.util.HashMap;

public class NewNoteActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionsGranted = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATIONS_PERMISSIONS_REQUEST = 5445;
    private Location currentLocation;

    private EditText noteTitle, note;
    private Button addNewNote,date, delete;
    private Uri uri = null;
    StorageReference storageR;
    private FirebaseFirestore firebaseFirestore;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    StorageTask uploadTask;
    FirebaseAuth fAuth;
    String userId;
    String noteId;
    String title, noteSt, dateSt, myUrl="";
    Double lon, lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        getLocationPermissions();
        getDeviceLocation();

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        noteSt = intent.getStringExtra("note");
        dateSt = intent.getStringExtra("date");
        noteId = intent.getStringExtra("noteId");

        firebaseFirestore = FirebaseFirestore.getInstance();

        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().toString();

        //newNoteImage = findViewById(R.id.image_note_update);
        note = findViewById(R.id.note);
        noteTitle = findViewById(R.id.note_title);
        date = findViewById(R.id.date);
        addNewNote = findViewById(R.id.add_btn);
        delete = findViewById(R.id.delete_btn);
        Toast.makeText(NewNoteActivity.this,lon +" " + lat, Toast.LENGTH_LONG).show();

        /* if (!title.isEmpty()){
            noteTitle.setText(title);
            note.setText(noteSt);
            date.setText(dateSt);
        } */

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        NewNoteActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                dateSt = day + "/" + month + "/" + year;
                date.setText(dateSt);
            }
        };



        addNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title =  noteTitle.getText().toString();
                noteSt = note.getText().toString();
                if (TextUtils.isEmpty(title))
                {
                    noteTitle.setError("Title is required");
                }
                if (TextUtils.isEmpty(noteSt))
                {
                    note.setError("Not description is required");
                }else {

                    uploadToFirebase();
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(noteId.isEmpty())
                {
                    startActivity(new Intent(NewNoteActivity.this, HomeActivity.class));
                }
                else{
                    FirebaseDatabase reference3 = FirebaseDatabase.getInstance();
                    reference3.getReference("Users").child(userId).child("Notes").child(noteId).removeValue();
                    startActivity(new Intent(NewNoteActivity.this, HomeActivity.class));
                }
            }
        });


    }

    private void uploadToFirebase() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

   userId = fAuth.getCurrentUser().getUid();
        DatabaseReference reference;
        if (noteId.isEmpty()) {

            reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Notes");

            String noteId = reference.push().getKey();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("noteId", noteId);
            hashMap.put("noteImage", myUrl);
            hashMap.put("title", title);
            hashMap.put("note", noteSt);
            hashMap.put("date", dateSt);
            hashMap.put("user", userId);
            hashMap.put("latitude", lat);
            hashMap.put("location", lon);
            reference.child(noteId).setValue(hashMap);

        }else{
            reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Notes").child(noteId);


            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("noteId", noteId);
            hashMap.put("noteImage", myUrl);
            hashMap.put("title", title);
            hashMap.put("note", noteSt);
            hashMap.put("date", dateSt);
            hashMap.put("user", userId);
            hashMap.put("latitude", lat);
            hashMap.put("location", lon);
            reference.setValue(hashMap);

        }
        progressDialog.dismiss();
        startActivity(new Intent(NewNoteActivity.this, HomeActivity.class));
        finish();


    }
    private void getLocationPermissions() {
        Log.d(TAG, "getLocationPermissions: getting location permissions");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(NewNoteActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (NewNoteActivity.this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(NewNoteActivity.this, permissions, LOCATIONS_PERMISSIONS_REQUEST);

        } else {
            mLocationPermissionsGranted = true;

        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device's current location");
        FusedLocationProviderClient mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: found location!");
                        currentLocation = (Location) task.getResult();
                        getLocation(currentLocation);
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(NewNoteActivity.this, "current location not found" , Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException");
        }
    }

    private void getLocation(Location currentLocation) {
        lon = currentLocation.getLatitude();
        lat = currentLocation.getLongitude();

    }

   /* @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            final Task<Location> location = mFusedLocationClient.getLastLocation();

            location.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location location = task.getResult();
                        if(location == null){
                            requestNewLocationData();
                        }
                        else {
                            try {
                                 lon = String.valueOf(location.getLongitude());
                                 lat = String.valueOf(location.getLatitude());


                            } catch (Exception e) {
                                Log.e("error: ", e.getMessage());
                            }
                        }
                    }
                }
            });

        } else {
            Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }
    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

        }
    };


*/


}

