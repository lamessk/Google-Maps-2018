package com.codingwithmitch.googlemaps2018.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.codingwithmitch.googlemaps2018.R;

import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    private Button rentOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rentOutButton = (Button) findViewById(R.id.rentOutButton);

        rentOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, RentOutActivity.class));
            }
        });
    }
}
