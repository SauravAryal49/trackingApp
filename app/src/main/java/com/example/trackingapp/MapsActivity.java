package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.trackingapp.Model.MyLocation;
import com.example.trackingapp.Utils.Common;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ValueEventListener {

    private GoogleMap mMap;

    DatabaseReference trackingUserLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerEventRealtime();
    }

    private void registerEventRealtime() {
        trackingUserLocation= FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION)
                .child(Common.trackingUser.getUid());

        trackingUserLocation.addValueEventListener(this);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Enable zoom ui
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //Set skin for the map
        boolean success=googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.my_uber_style));

    }

    @Override
    protected void onResume() {
        super.onResume();
        trackingUserLocation.addValueEventListener(this);
    }

    @Override
    protected void onStop() {
        trackingUserLocation.removeEventListener(this);
        super.onStop();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        MyLocation location=dataSnapshot.getValue(MyLocation.class);

        //Add Marker
        if(location!=null) {
            LatLng userMarker = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(userMarker)
                    .title(Common.trackingUser.getEmail())
                    .snippet(Common.getDataFormatted(Common.convertTimeStampToDate(location.getTime()))));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker,16f));
        }else {
            Toast.makeText(this, "error in parsing location", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "turn on the location if it has not been ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Toast.makeText(this, "error!"+databaseError, Toast.LENGTH_SHORT).show();
    }
}
