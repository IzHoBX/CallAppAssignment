package com.izho.callappassignment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ShowPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);

        Intent i = getIntent();
        ImageView photo = findViewById(R.id.photo);
        
        Glide.with(getApplicationContext()).load(i.getStringExtra("url")).into(photo);
    }
}
