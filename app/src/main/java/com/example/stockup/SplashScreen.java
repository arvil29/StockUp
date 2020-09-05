package com.example.stockup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


//creates intro splash screen which lasts for 1 sec
public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent splashIntent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(splashIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);


    }
}
