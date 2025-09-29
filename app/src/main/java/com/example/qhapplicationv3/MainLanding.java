package com.example.qhapplicationv3;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
public class MainLanding extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_landing_page);

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            Intent i = new Intent(this, Authentication.class);
            startActivity(i);
        });
    }
}

