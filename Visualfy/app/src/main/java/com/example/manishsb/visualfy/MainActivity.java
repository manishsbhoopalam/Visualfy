package com.example.manishsb.visualfy;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // ask for permission

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);


        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // ask for permission

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);


        }

    }
    public void vision(View view){
        Intent intent = new Intent(getApplicationContext(),Vision.class);
        intent.putExtra("Text","Welcome to Visualfy . Tell Open camera to start your journey in visualfy and Close to end it.");
        Toast.makeText(this, "Opening", Toast.LENGTH_SHORT).show();
        startActivity(intent);

    }
    public void rec(View view){
        Intent intent = new Intent(getApplicationContext(),SpeechRec.class);
        startActivity(intent);

    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                Toast.makeText(this, "permission is required", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==2) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);
                Toast.makeText(this, "permission is required", Toast.LENGTH_SHORT).show();

            }
        }

    }


}
