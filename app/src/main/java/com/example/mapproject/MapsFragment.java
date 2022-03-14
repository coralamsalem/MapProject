package com.example.mapproject;

import static com.example.mapproject.Utils.Constants.DEFAULT_ZOOM;
import static com.example.mapproject.Utils.GeoNotesUtils.showMarkerInfo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.mapproject.Utils.Constants;
import com.example.mapproject.Utils.GeoNotesUtils;
import com.example.mapproject.Utils.GooglePlayServicesHelper;
import com.example.mapproject.Utils.MarkerClusterRenderer;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MapsFragment.class.getSimpleName();

    private String finalLocation;

    private GoogleMap mMap;
    private Location currentLocation;
    private List<Note> noteList = new ArrayList<>();
    private boolean mLocationPermissionsGranted = false;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATIONS_PERMISSIONS_REQUEST = 5445;

    private AutocompleteSupportFragment autocompleteFragment;

    private Geocoder geocoder;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    String userId = fAuth.getCurrentUser().getUid();
    DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Notes");


    private List<Note> allNotesFromDb = new ArrayList<>();

    public MapsFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLocationPermissionsGranted) {
            if (GooglePlayServicesHelper.isGooglePlayServicesAvailable(this.getContext())) {
                getLocationPermissions();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    private void getLocationPermissions() {
        Log.d(TAG, "getLocationPermissions: getting location permissions");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATIONS_PERMISSIONS_REQUEST);

        } else {
            mLocationPermissionsGranted = true;
            initMap();
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map fragment");
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), "AIzaSyAg6KS4cU2gwhVG7lvkB0_5Rewn2dkObIE");
        }

        // Initialize the AutocompleteSupportFragment.
       autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        if (autocompleteFragment != null) {
            autocompleteFragment.setHint("Search Location");
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        this.mMap = googleMap;

        if (mLocationPermissionsGranted) {
            Log.d(TAG, "onMapReady: Getting current location of the device and moving the camera");

            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false); // to get rid of the default location button

            init();
        }
    }

    private void init() {
        Log.d(TAG, "init: initializing");
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }

        });


        getAllNotes();



        mMap.setOnMarkerClickListener(marker -> {
            displayMarkerWithNotes(marker);
            return true;
        });
    }



    // Getting the base location of the user
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device's current location");
        FusedLocationProviderClient mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        try {
            if (mLocationPermissionsGranted) {
                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: found location!");
                        currentLocation = (Location) task.getResult();
                        Double lon, lat;
                        lon = currentLocation.getLongitude();
                        lat = currentLocation.getLatitude();

                        SharedPreferences pref = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putFloat("lon", lon.floatValue());
                        editor.putFloat("lat", lat.floatValue());
                        editor.commit();

                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                "My Location");
                    } else {
                        Log.d(TAG, "onComplete: current location is null");
                        moveCamera(new LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE),"my location");
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException");
        }
    }

    private void getAllNotes() {
        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //noteList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    String noteId = snapshot1.child("noteId").getValue().toString();
                    String title = snapshot1.child("title").getValue().toString();
                    String note = snapshot1.child("note").getValue().toString();
                    String date = snapshot1.child("date").getValue().toString();
                    String user = snapshot1.child("user").getValue().toString();
                    String latitude = snapshot1.child("latitude").getValue().toString();
                    String longitute = snapshot1.child("longitute").getValue().toString();
                    String location = "";
                    Note note1 = new Note(title, note, user, date, location, latitude, longitute, noteId);
                    noteList.add(note1);
                }
                if( noteList.size() > 0 ) {
                    showClusterMapView(noteList, mMap);
                }else{
                    Toast.makeText(getContext(), "There is no notes", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void displayMarkerWithNotes(Marker marker) {
        Toast.makeText(this.getContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public String getLocationDetails(LatLng latLng) {
        geocoder = new Geocoder(this.getActivity());

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    latLng.latitude,
                    latLng.longitude,
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            finalLocation = "service not available";
            Log.e(TAG, finalLocation, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            finalLocation = "invalid lat long used";
            Log.e(TAG, finalLocation + ". " +
                    "Latitude = " + latLng.latitude +
                    ", Longitude = " +
                    latLng.longitude, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (finalLocation.isEmpty()) {
                finalLocation = "no address found";
                Log.e(TAG, finalLocation);
            }
        } else {
            Address address = addresses.get(0);
            Log.i(TAG, "address found");
            finalLocation = address.getAddressLine(0);
        }
        return finalLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: called");
        mLocationPermissionsGranted = false;
        if (requestCode == LOCATIONS_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRequestPermissionsResult: permission failed");
                        return;
                    }
                }
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
                mLocationPermissionsGranted = true;
                // initialize our map
                initMap();
            }
        }
    }

    private void moveCamera(LatLng latLng, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(GeoNotesUtils.bitmapDescriptorFromVector(getContext(), R.drawable.ic_marker_cluster));
            mMap.addMarker(options);
        }

        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
    }

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(DEFAULT_ZOOM).build();
    }

    private void showClusterMapView(List<Note> noteList, GoogleMap mMap) {

        // moveCamera(new LatLng(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE), Constants.DEFAULT_TITLE);

        ClusterManager<Note> mClusterManager = new ClusterManager<>(getContext(), mMap);
        mClusterManager.setRenderer(new MarkerClusterRenderer(getContext(), mMap, mClusterManager, fAuth.getCurrentUser().getEmail()));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(marker -> {
            String title = "More Details of " + "\"" + marker.getTitle() + "\"";
            String description = marker.getSnippet();
            showMarkerInfo((AppCompatActivity) getContext(), title, description);

        });
        addNoteItemsWhichHasLatLng(mClusterManager, noteList);
        mClusterManager.cluster();
    }

    private void addNoteItemsWhichHasLatLng(ClusterManager<Note> mClusterManager, List<Note> noteList) {
        for (Note note : noteList) {
            if (note.getLocation() != null) {
                mClusterManager.addItem(note);
            }
        }
    }
}







        /*implements OnMapReadyCallback {
    GoogleMap googleMap;
    MapView mapView;
    View view;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient client;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        client = LocationServices.getFusedLocationProviderClient(getContext());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = (MapView) view.findViewById(R.id.maps_view);

        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
            getCurrentLocation();
        }
        /*SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }*/
/*    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        this.googleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        getCurrentLocation();
       /* googleMap.addMarker(new MarkerOptions().position(new LatLng(-34, 151)).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-34, 151)));

*/
 /*   }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                MarkerOptions options = new MarkerOptions().position(latLng);
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                                googleMap.addMarker(options);
                            }
                        });
                    }
                }

            });
        }else{
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    44);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 44){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }
}
*/