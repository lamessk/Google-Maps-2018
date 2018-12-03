package com.codingwithmitch.googlemaps2018.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.codingwithmitch.googlemaps2018.R;
import com.craftman.cardform.Card;
import com.craftman.cardform.CardForm;
import com.craftman.cardform.OnPayBtnClickListner;
import com.google.firebase.auth.FirebaseAuth;


public class CreditCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card);
        getSupportActionBar().setTitle("Payment");

        CardForm cardFrom = (CardForm) findViewById(R.id.creditcardform);
        TextView txtDes = findViewById(R.id.payment_amount);
        Button btnPay = (Button) findViewById(R.id.btn_pay);

        txtDes.setText("$5");
        btnPay.setText(String.format("Payer %s",txtDes.getText()));


        cardFrom.setPayBtnClickListner(new OnPayBtnClickListner() {
            @Override
            public void onClick(Card card) {
                Toast.makeText(CreditCardActivity.this, "Card Added", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_home: {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            case R.id.action_sign_out: {
                signOut();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }

    }
}