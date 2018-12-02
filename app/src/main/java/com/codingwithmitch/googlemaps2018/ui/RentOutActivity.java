package com.codingwithmitch.googlemaps2018.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.codingwithmitch.googlemaps2018.R;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class RentOutActivity extends AppCompatActivity {

    private EditText pAddress, pDescription;
    private Button regParkingSpotButton;

    private FirebaseFirestore pDb;


    public void registerNewParkingLocation(final String parkingAddress, final String parkingDescription){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_out);


    }
}
