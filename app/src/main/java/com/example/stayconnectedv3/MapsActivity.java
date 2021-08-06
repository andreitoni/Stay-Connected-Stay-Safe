package com.example.stayconnectedv3;
/*
@author Andrei Toni Niculae
 */
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    protected static GoogleMap mMap;
    protected static boolean mapActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
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

      /*  // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
      */
        // Takes the Lat and Long from all users and sets their markers on the map
        //  Log.d("map", DashboardActivity.allUsersList.getUsers().toString());
        for (User u : DashboardActivity.allUsersList.getUsers()) {
            u.setupMarker();
            //     Log.d("map", "Markers have been set up on the map");
        }

        mMap.setOnMarkerClickListener(marker -> {
            String markerTitle = marker.getTitle();
            Intent i = new Intent(MapsActivity.this, ShowUserDetails.class);
            i.putExtra("markerTitle",markerTitle);
            startActivity(i);
            return false;
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapActive = true;
    }

    protected static void updateMarkers() {
        if (mMap == null) {
            //    Log.d("UPDATE_MARKER", "mMap is null");
            return;
        }

        for (User u : DashboardActivity.allUsersList.getUsers()) {
            u.updateMarker();
            //    Log.d("map", "Marker updated on map for " + u.getEmail());
        }
    }


}



