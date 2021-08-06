package com.example.stayconnectedv3;
/*
@author Andrei Toni Niculae
 */

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class DashboardActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    public static UserList allUsersList = new UserList();

    FirebaseFirestore db;
    String userID;
    FirebaseAuth mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_layout);

        findViewById(R.id.buttonStartLocationUpdates).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        DashboardActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION_PERMISSION
                );
            } else {
                startLocationService();
            }
        });

        findViewById(R.id.buttonStopLocationUpdates).setOnClickListener(v -> stopLocationService());


        //LOG OUT BUTTON
        {
            final Button logoutButton = findViewById(R.id.logout_button);
            //LOG OUT BUTTON
            // Log out the current user and send it to the sign in/sign up screen
            logoutButton.setOnClickListener(v -> {
                stopLocationService();
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            });
        }

        // SHOW MAP BUTTON
        final Button showMapButton = findViewById(R.id.buttonShowMap);

        showMapButton.setOnClickListener(v -> {
            if (isLocationServiceRunning()) {
                Intent i = new Intent(DashboardActivity.this, MapsActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(getApplicationContext(), "Start the location service first!", Toast.LENGTH_SHORT).show();
            }
        });

        final Button settingsButton = findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, SetNameAndPictureActivity.class);
            startActivity(i);
        });


        final Button panicButton = findViewById(R.id.panic_button);

        panicButton.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, PanicActivity.class);
            startActivity(i);
        });


        db = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(mUser.getCurrentUser()).getUid();

        getBatteryLevel();


        //-------------------------END OF ONCREATE METHOD-------------------------------------
    }

    protected void getBatteryLevel() {

        BatteryManager bm = (BatteryManager) this.getSystemService(BATTERY_SERVICE);
        int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        Log.d("battery", "" + batteryLevel);

        // ADD INFO IN FIRESTORE
        Map<String, Object> data = new HashMap<>();
        data.put("batteryLevel", batteryLevel);

        db.collection("users").document(userID)
                .set(data, SetOptions.merge());

    }


    protected static void getAllUsersFromDB() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Log.d("1234", document.getId() + " => " + document.getData());
                            User user = User.userFromDoc(document);

                            if (!user.getuID().equals("none")) {
                                if (allUsersList.contains(user)) {
                                    allUsersList.update(user);
                                } else {
                                    allUsersList.add(user);
                                }
                            }
                            //   Log.d("GET_OTHER_USERS", "list updated: " + allUsersList);
                            //  Log.d("GET_OTHER_USERS", "get user success");
                            //for(User u: allUsersList.users){
                            //Log.d("1234", u.getEmail()+ " " + u.getAddress());}
                            //Log.d("1234","-------------------------------------------------");
                        }
                    } else {
                        Log.w("GET_OTHER_USERS", "Error getting other users.", task.getException());
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {

                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
        }
    }
}