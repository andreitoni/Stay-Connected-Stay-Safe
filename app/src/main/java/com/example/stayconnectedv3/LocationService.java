package com.example.stayconnectedv3;
/*
@author Andrei Toni Niculae
 */
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LocationService extends Service {


    FirebaseFirestore db;
    FirebaseAuth mUser;
    String userID;

    private String oldAddress = "";
    private String address = "";
    private int timer = 0;
    private static ArrayList<String> favouriteAddressesList = new ArrayList<String>();


    @Override
    public void onCreate() {
        super.onCreate();

        db = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(mUser.getCurrentUser()).getUid();

    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            address = addresses.get(0).getAddressLine(0);
            //      Log.d("000", address);w
        } catch (Exception exception) {
            //     Log.d("000", "unable to get street address");
        }
        return address;
    }


    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NotNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            locationResult.getLastLocation();

            double latitude = locationResult.getLastLocation().getLatitude();
            double longitude = locationResult.getLastLocation().getLongitude();


            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("EEE d MMM yyyy, HH:mm:ss");
            String date = df.format(Calendar.getInstance().getTime());
            //Log.d("888", "" + date);

            timer = timer + 2;

            //   Log.d("888", "" + timer);
            //add favorite location after 10 minutes(600 sec, increment by 2 = 1200s)
            if (timer > 120) {
                String currentAddress = getAddress(latitude, longitude);
                timer = 0;
                if (oldAddress.equals(currentAddress)) {

                    if (favouriteAddressesList.size() > 0) {
                        //if the first 25 characters in the previous address are NOT the same as
                        // the first 25 characters in the current address, add the current address to the favourite addresses list.
                        if (!favouriteAddressesList.get(favouriteAddressesList.size() - 1).regionMatches(false, 0, currentAddress, 0, 25)) {
                            //  if (!favouriteAddresses.get(favouriteAddresses.size() - 1).equals(currentAddress)) {
                            favouriteAddressesList.add(currentAddress + " at " + date);
                        }
                    } else {
                        //add the first address at first iteration
                        favouriteAddressesList.add(currentAddress + " at " + date);
                    }
                }
                oldAddress = address;
            }

            // ADD INFO IN FIRESTORE
            Map<String, Object> data = new HashMap<>();
            data.put("latitude", latitude);
            data.put("longitude", longitude);
            data.put("address", getAddress(latitude, longitude));
            data.put("location_history", favouriteAddressesList);
            if (locationResult.getLastLocation().hasSpeed()) {
                double speed = locationResult.getLastLocation().getSpeed();
                data.put("speed", speed);
            }


            db.collection("users").document(userID)
                    .set(data, SetOptions.merge());

            // Log.d("map", DashboardActivity.allUsersList.getUsers().toString());

            DashboardActivity.getAllUsersFromDB();
            if (MapsActivity.mapActive) {

                MapsActivity.updateMarkers();

            } else {
                Log.d("MARKER", "mMap is inactive in service");
            }

        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("MissingPermission")
    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());


    }


    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
