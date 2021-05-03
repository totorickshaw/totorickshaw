package com.digitechsolz.totorickshaw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;

public class LocationSetupActivity extends AppCompatActivity {

    //Session Manager
    LocationSessionManager locationSessionManager;
    //Session Manager
    LoginSessionManager loginSessionManager;
    private GpsTracker gpsTracker;
    Button setup_btn;
    //Coordinatelayout for Snackbar
    ConstraintLayout constraintLayout;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        setContentView(R.layout.activity_location_setup);
        init();
    }

    private void init() {
        locationSessionManager = new LocationSessionManager(getApplicationContext());

        loginSessionManager = new LoginSessionManager(getApplicationContext());
        HashMap<String, String> loginSessionDetails = loginSessionManager.getLoginSessionDetails();
        user_id = loginSessionDetails.get(LoginSessionManager.KEY_USERID_SES);

        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        setup_btn = findViewById(R.id.setup_btn);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        setup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(LocationSetupActivity.this);
                if(gpsTracker.canGetLocation()){
                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();

                    HashMap<String, String> loginIndata = new HashMap<>();
                    loginIndata.put("user_id", user_id);

                    locationSessionManager.createLocationSession(loginIndata);

//                    Toast.makeText(getApplicationContext(), "Latitude : " + latitude + "& Longitude : " + longitude, Toast.LENGTH_LONG).show();

                    if (Double.toString(latitude) != null && Double.toString(longitude) != null) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("latitude", Double.toString(latitude));
                        intent.putExtra("longitude", Double.toString(longitude));
                        startActivity(intent);
                        finish();
                    }

                }else{
                    gpsTracker.showSettingsAlert();
                }
            }
        });
    }
}