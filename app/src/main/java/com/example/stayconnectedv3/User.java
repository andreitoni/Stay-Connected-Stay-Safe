package com.example.stayconnectedv3;
/*
@author Andrei Toni Niculae
 */

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;


import static com.example.stayconnectedv3.MapsActivity.mMap;

public class User {

    private String uID;
    private String email;
    private String firstName;
    private double latitude;
    private double longitude;
    private double speed;
    public Marker marker;

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    private int batteryLevel;

    public ArrayList<String> getLocationHistory() {
        return locationHistory;
    }

    public void setLocationHistory(ArrayList<String> locationHistory) {
        this.locationHistory = locationHistory;
    }

    private ArrayList<String> locationHistory = new ArrayList<>();


    private String address;

    public static User userFromDoc(DocumentSnapshot doc) {

        User user = new User();
        user.setuID("" + doc.get("uID"));
        user.setEmail("" + doc.get("email"));
        user.setFirstName("" + doc.get("name"));

        if (doc.get("batteryLevel") != null) {
            user.setBatteryLevel(Integer.parseInt("" + doc.get("batteryLevel")));
        }
        if (doc.get("speed") != null) {
            user.setSpeed(Double.parseDouble("" + doc.get("speed")));
        }
        if (doc.get("latitude") != null) {
            user.setLatitude(Double.parseDouble("" + doc.get("latitude")));
        }
        if (doc.get("longitude") != null) {
            user.setLongitude(Double.parseDouble("" + doc.get("longitude")));
        }
        user.setAddress("" + doc.get("address"));
        user.setLocationHistory((ArrayList<String>) doc.get("location_history"));
        return user;
    }

    private void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public User() {
    }


    public User(String uID) {
        this.uID = uID;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setLocation(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    // creates the marker
    public void setupMarker() {
        MarkerOptions marker = new MarkerOptions();
        LatLng latLng = new LatLng(this.getLatitude(), this.getLongitude());
        marker.position(latLng);
        marker.title("" + this.getFirstName());

        //marker.

        //marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.andreimoji));


        this.setMarker(mMap.addMarker(marker));
        LatLng pos = new LatLng(latitude, longitude);
        //when the user open the map, his marker is focused
          mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18.0f));
        //   Log.d("SETUP_MARKER", "Marker setup for " + this.getEmail());

    }

    public void updateMarker() {

        if (this.marker == null) {
            //    Log.d("update_marker", "Marker is null");
            return;
        }
        //Log.d("UPDATE_MARKER", "User marker updated");
        //  this.marker.setTitle("Address: " + this.getAddress());
        this.marker.setPosition(new LatLng(this.getLatitude(), this.getLongitude()));

    }


    // Update the user list locally
    public void update(User other) {
        this.latitude = other.getLatitude();
        this.longitude = other.getLongitude();
        this.address = other.getAddress();
        this.locationHistory = other.getLocationHistory();
        this.speed = other.getSpeed();
        this.firstName = other.getFirstName();
        this.batteryLevel = other.getBatteryLevel();
        //  Log.d("999", toString());
    }

    public String toString() {
        //return this.getuID() + " " + this.getFirstName() + " " + this.getLastName() + " || Location: " + this.getLatitude() + " " + this.getLongitude();
        return "Email: " + this.getEmail() + " Latitude: " + this.getLatitude() + " Longitude " + this.getLongitude() + " Current Address " + this.getAddress() + " Location history " + this.getLocationHistory();
        // return "UserID = " + this.getuID() + " " + this.getEmail() + " ------ Location: " + this.getLatitude() + " " + this.getLongitude();
    }

    public boolean equals(Object other) {
        if (other instanceof User) {
            User that = (User) other;
            return this.getuID().equals(that.getuID());
        }

        return false;
    }
}
