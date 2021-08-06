package com.example.stayconnectedv3;
/*
@author Andrei Toni Niculae

This class allows the signed in user to set a name and a profile picture for thei account.
The picture will be stored in the Firebase Storage.
 */
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class SetNameAndPictureActivity extends AppCompatActivity {

    private ImageView profilePic;
    public Uri imageUri;
    private StorageReference storageReference;
    FirebaseFirestore db;
    FirebaseAuth mUser;
    EditText edt_name;


    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_name_and_picture_layout);

        db = FirebaseFirestore.getInstance();

        mUser = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(mUser.getCurrentUser()).getUid();
        edt_name = findViewById(R.id.inputName);

        final Button nextButton = findViewById(R.id.nextButton);
        profilePic = findViewById(R.id.profilePicture);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        profilePic.setOnClickListener(v -> {
            if(edt_name.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(), "Please enter your name first", Toast.LENGTH_SHORT).show();
            }
            else{choosePicture();}
        });
        nextButton.setOnClickListener(v -> {

            String name = edt_name.getText().toString();
            Log.d("1",""+name);
            if (name.equals("")) {
                Toast.makeText(getApplicationContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
            } else {


                // ADD INFO IN FIRESTORE
                Map<String, Object> data = new HashMap<>();
                data.put("name", name);


                db.collection("users").document(userID)
                        .set(data, SetOptions.merge());

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name).build();
                Log.d("display name", name);
                assert user != null;
                user.updateProfile(profileUpdates);

                Intent i = new Intent(SetNameAndPictureActivity.this, DashboardActivity.class);
                startActivity(i);
            }
        });

    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
            uploadPictureInFirebase();
        }
    }

    private void uploadPictureInFirebase() {
        String name = edt_name.getText().toString();
        StorageReference myReference = storageReference.child("images/" + name);

        myReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> Snackbar.make(findViewById(android.R.id.content),
                        "Image Uploaded.", Snackbar.LENGTH_LONG).show()).
                addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed To Upload",
                        Toast.LENGTH_SHORT).show());
    }
}
