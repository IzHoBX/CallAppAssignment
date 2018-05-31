package com.izho.callappassignment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(!(accessToken != null && !accessToken.isExpired())) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

    }
}
