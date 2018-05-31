package com.izho.callappassignment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    private AccessToken accessToken;
    private LinkedList<PhotoModel> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photos = new LinkedList<>();

        ensureLoggedIn();
        pullAllPhotos();
        setupRecyclerView();
        populateRecyclerView();

        ImageView x = (ImageView) findViewById(R.id.imageView);
        Glide.with(getApplicationContext()).load("https://scontent.xx.fbcdn.net/v/t31.0-8/20369018_1418013628283283_5829614423638478225_o.jpg.webp?_nc_cat=0&oh=5aa458b52d6f18cbd74b683da24db52e&oe=5B7FC178").into(x);

        //RecyclerView photosView = (RecyclerView) findViewById(R.id.photos_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        //photosView.setLayoutManager(layoutManager);


    }

    private void populateRecyclerView() {
    }

    private void setupRecyclerView() {
    }

    private void pullAllPhotos() {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.i("test", object.toString());
                Gson gson = new Gson();
                Response response1 = gson.fromJson(object.toString(), Response.class);
                for(Photo p: response1.photos.data) {
                    photos.add(new PhotoModel(p.picture, p.webp_images[0].source, p.name, p.album.name, p.created_time));
                }
                Log.i("test", photos.size() + "");
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "photos{created_time,name,album,picture,webp_images}");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void ensureLoggedIn() {
        accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken == null || accessToken.isExpired()) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
    }
}

class PhotoModel {
    public String thumbnailLink;
    public String photoLink;
    public String title;
    public String albumTitle;
    public String creationTime;

    public PhotoModel(String thumbnailLink, String photoLink, String title, String albumTitle, String creationTime) {
        this.thumbnailLink = thumbnailLink;
        this.photoLink = photoLink;
        this.title = title;
        this.albumTitle = albumTitle;
        this.creationTime = creationTime;
    }
}
class Album {
    public String name;
    public String id;
    public String created_time;

    public Album(String name, String id, String created_time) {
        this.name = name;
        this.id = id;
        this.created_time = created_time;
    }
}

class Image {
    public String height;
    public String source;
    public String width;

    public Image(String height, String source, String width) {
        this.height = height;
        this.source = source;
        this.width = width;
    }
}

class Photo {
    public String created_time;
    public String name;
    public Album album;
    public String picture;
    public Image[] webp_images;

    public Photo(String created_time, String name, Album album, String picture, Image[] webp_images) {
        this.created_time = created_time;
        this.name = name;
        this.album = album;
        this.picture = picture;
        this.webp_images = webp_images;
    }
}

class PhotoMeta {
    public Photo[] data;
    public Object paging;

    public PhotoMeta(Photo[] data, Object paging) {
        this.data = data;
        this.paging = paging;
    }
}

class Response {
    public PhotoMeta photos;
    public String id;

    public Response(PhotoMeta photos, String id) {
        this.photos = photos;
        this.id = id;
    }
}
