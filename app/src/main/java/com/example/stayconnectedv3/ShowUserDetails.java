package com.example.stayconnectedv3;
/*
@author Andrei Toni Niculae

This class displays detailed information about a specific user such as: their profile picture,
Name, moving speed, battery level, current address and a location history list.

When a marker is pressed on the map, this class is called.
 */

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ShowUserDetails extends AppCompatActivity {

    TextView tv_name, tv_speed_edit,tv_battery,currentAddress;
    ListView listView_userInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_details);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        listView_userInfo = findViewById(R.id.listview);
        tv_name = findViewById(R.id.tv_name_edit);
        tv_speed_edit = findViewById(R.id.tv_speed_edit);
        tv_battery= findViewById(R.id.insert_battery);
        currentAddress = findViewById(R.id.currentAddress);
        ImageView profilePicture = findViewById(R.id.profilePicture);
        String name = getIntent().getStringExtra("markerTitle");

        List<User> userListGood = DashboardActivity.allUsersList.getUsers();

        for (User u : userListGood) {
            if (u.getFirstName().equals(name)) {
                List<String> locationHistoryList = u.getLocationHistory();
                String battery = Integer.toString((int)u.getBatteryLevel());
                tv_battery.setText(battery+"%");
                tv_name.setText(name);
                String speed = Integer.toString((int) u.getSpeed());
                tv_speed_edit.setText(speed+" km/h");
                String current_address = u.getAddress();
                currentAddress.setText(current_address);
                try{
                listView_userInfo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationHistoryList));}
                catch (NullPointerException e){  Toast.makeText(getApplicationContext(), "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();}
                try {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + name);

                    Log.d("69", storageReference + "");

                    Glide.with(this)
                            .load(storageReference)
                            .into(profilePicture);
                }catch (Exception e){}
            }
        }

    }
}