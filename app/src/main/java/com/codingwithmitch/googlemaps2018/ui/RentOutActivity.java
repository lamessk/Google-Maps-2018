package com.codingwithmitch.googlemaps2018.ui;

import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.codingwithmitch.googlemaps2018.R;
import com.codingwithmitch.googlemaps2018.UserClient;
import com.codingwithmitch.googlemaps2018.models.ChatMessage;
import com.codingwithmitch.googlemaps2018.models.ParkingSpot;
import com.codingwithmitch.googlemaps2018.models.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RentOutActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "RentOutActivity";

    private EditText mAddress, mDescription, mPrice;
    private Button regParkingSpotButton;
    private ParkingSpot pSpot;

    private FirebaseFirestore mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_out);

        mAddress = findViewById(R.id.rentAddressField);
        mPrice = findViewById(R.id.rentPrice);
        mDescription = findViewById(R.id.rentDescription);

        findViewById(R.id.rentButton).setOnClickListener(this);
        mDb = FirebaseFirestore.getInstance();
    }

    public void insertNewParkingSpot(){
        Address address = geoLocate();
        String description = mDescription.getText().toString();
        String price = mPrice.getText().toString();

        if(!address.equals("")){

            DocumentReference newParkingDoc = mDb
                    .collection("Parking Spots")
                    .document();

            ParkingSpot parkingSpot = new ParkingSpot();
            parkingSpot.setpAvailability(description);
            parkingSpot.setpAddress(mAddress.getText().toString());
            parkingSpot.setpPrice(price);
            parkingSpot.setGeo_point(new GeoPoint(address.getLatitude(), address.getLongitude()));

            User user = ((UserClient)(getApplicationContext())).getUser();
            Log.d(TAG, "insertNewMessage: retrieved user client: " + user.toString());
            parkingSpot.setpUserID(user);

            newParkingDoc.set(parkingSpot).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        clearForm();
                    }else{
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private Address geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mAddress.getText().toString();

        Geocoder geocoder = new Geocoder(getApplicationContext());
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }
        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());

            return address;
        }
        return null;
    }

    public void clearForm(){
        mDescription.setText("");
        mPrice.setText("");
        mAddress.setText("");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rentButton:{
                insertNewParkingSpot();
            }
        }
    }

}
