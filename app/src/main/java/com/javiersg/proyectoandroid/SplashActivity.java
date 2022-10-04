package com.javiersg.proyectoandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private int DELAY_SPLASH = 5000; //estos son 10 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TimerTask carga = new TimerTask() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);

                startActivity(i);
                finish();
            }
        };

        Timer tiempo = new Timer();
        tiempo.schedule(carga, DELAY_SPLASH);

    }

}