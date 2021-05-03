package com.digitechsolz.totorickshaw;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import java.util.HashMap;

public class LaunchActivity extends Activity {

    private static int SPLASH_TIME_OUT = 3000;
    //Session Manager
    LoginSessionManager loginSessionManager;
    //Session Manager
    LocationSessionManager locationSessionManager;

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

        setContentView(R.layout.activity_launch);

        locationSessionManager = new LocationSessionManager(getApplicationContext());

        loginSessionManager = new LoginSessionManager(getApplicationContext());

        prepareJson();
    }

    public void prepareJson()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loginSessionManager.isLoginSession()) {
                    if (locationSessionManager.isLocationSession()) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), LocationSetupActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);

    }
}
