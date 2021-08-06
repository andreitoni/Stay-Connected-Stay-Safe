package com.example.stayconnectedv3;
/*
@author Andrei Toni Niculae

This class is responsible for the login/sign up of the users using Firebase Authentication.
 */

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore firestoreDatabase;
    private FirebaseAuth mAuth;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firestoreDatabase = FirebaseFirestore.getInstance();

        final Button signUpSignInButton = findViewById(R.id.signUpSignInButton);
        final EditText edt_email = findViewById(R.id.inputMail);
        final EditText edt_password = findViewById(R.id.inputPassword);
        final Button loginButton = findViewById(R.id.login_button);

        signUpSignInButton.setOnClickListener(v -> {

            String email = edt_email.getText().toString().trim();
            String password = edt_password.getText().toString().trim();
            // Create an account in Firebase
            if (signUpSignInButton.getText().toString().equals(getString(R.string.sign_up))) {
                if (email.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "User created", Toast.LENGTH_SHORT).show();

                                    userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                    // Create a user document with the user id and add the field "email".
                                    DocumentReference documentReference = firestoreDatabase.collection("users").document(userID);
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("email", email);
                                    userMap.put("uID", userID);
                                    documentReference.set(userMap).addOnSuccessListener(aVoid -> Log.d("123", "User profile is created for: " + userID));

                                    // Start the SetNameAndPictureActivity activity
                                    Intent i = new Intent(MainActivity.this, SetNameAndPictureActivity.class);
                                    startActivity(i);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(getApplicationContext(), "User creation failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                if (email.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(getApplicationContext(), "Sign in successful", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(MainActivity.this, DashboardActivity.class);
                                    startActivity(i);

                                } else {
                                    Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        //when this button is pressed, it's text changes as well as for the sign in/sign up button
        loginButton.setOnClickListener(v -> {
            if (loginButton.getText().toString().equals(getString(R.string.already_have_an_account_log_in))) {
                signUpSignInButton.setText(R.string.sign_in);
                loginButton.setText(R.string.no_account_create_one);
            } else {
                signUpSignInButton.setText(R.string.sign_up);
                loginButton.setText(R.string.already_have_an_account_log_in);
            }
        });

        //  <----------------------------END OF onCreate---------------------------------------->
    }

    @Override
    public void onStart() {
        super.onStart();
        // IF THE USER IS ALREADY LOGGED IN, SEND HIM TO MAIN PAGE.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent i = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(i);
        }
    }
}