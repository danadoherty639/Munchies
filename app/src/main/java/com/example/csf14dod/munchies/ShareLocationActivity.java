package com.example.csf14dod.munchies;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ShareLocationActivity extends AppCompatActivity  {

    final int SEND_SMS_PERMISSION_REQUEST_CODE=1;
    Button bt_send;
    EditText message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_location);

        bt_send = (Button)findViewById(R.id.bt_send);
        bt_send.setEnabled(false);
        message = (EditText) findViewById(R.id.editTextMsg);

        if (checkPermission(android.Manifest.permission.SEND_SMS)) {
            bt_send.setEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.SEND_SMS},
                    SEND_SMS_PERMISSION_REQUEST_CODE);
        }

        Bundle bundle = getIntent().getParcelableExtra("bundle");
        LatLng mylatlng = bundle.getParcelable("position");

        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses = null;
        String add ="";
        try {
            addresses = geoCoder.getFromLocation(mylatlng.latitude, mylatlng.longitude, 1);
            android.location.Address address = addresses.get(0);

            if(addresses.size() >0){
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++)
                    add += address.getAddressLine(i) + "\n";

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        message.setText(add);
    }

    private boolean checkPermission(String permission) {
        int permissionCheck =
                ContextCompat.checkSelfPermission(this,permission);
        return (permissionCheck ==
                PackageManager.PERMISSION_GRANTED);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    bt_send.setEnabled(true);
                }
                return;
            }
        }
    }
    public void send(View view) {
        String phoneNumber = ((EditText)findViewById(
                R.id.editTextNumber)).getText().toString();
        String msg = ((EditText)findViewById(
                R.id.editTextMsg)).getText().toString();
        if (phoneNumber==null || phoneNumber.
                length()==0 || msg==null || msg.length()==0 ) {
            return;
        }
        if (checkPermission(android.Manifest.permission.SEND_SMS)) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, msg,
                    null, null);
        } else {
            Toast.makeText(ShareLocationActivity.this, "No Permission",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
