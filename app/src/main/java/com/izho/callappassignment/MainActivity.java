package com.izho.callappassignment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MainActivity extends AppCompatActivity {
    private AccessToken accessToken;
    private ArrayList<PhotoModel> photos;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photos = new ArrayList<>();

        ensureLoggedIn();
        pullAllPhotos();
        setupRecyclerView();

    }

    private void populateRecyclerView() {
        list.setAdapter(new MyAdapter(photos));
    }

    private void setupRecyclerView() {
        list = (RecyclerView) findViewById(R.id.list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        list.setLayoutManager(layoutManager);
        list.hasFixedSize();
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
                populateRecyclerView();
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "photos.limit(100){created_time,name,album,picture,webp_images}");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void ensureLoggedIn() {
        accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken == null || accessToken.isExpired()) {
            Log.i("test", "attempting to start login activity");
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


class MyAdapter extends RecyclerView.Adapter<MyAdapter.PhotoViewHolder> {
    private ArrayList<PhotoModel> dataset;

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView albumTitle;
        public TextView creationTime;
        public ImageView thumbnail;

        public PhotoViewHolder(View view) {
            super(view);
            this.title = view.findViewById(R.id.title);
            this.albumTitle = view.findViewById(R.id.albumTitle);
            this.creationTime = view.findViewById(R.id.creationTime);
            this.thumbnail = view.findViewById(R.id.thumbnail);
        }
    }

    public MyAdapter(ArrayList<PhotoModel> dataset) {
        this.dataset = dataset;
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, final int position) {
        holder.title.setText("Title: " + dataset.get(position).title);
        holder.albumTitle.setText("Album: " + dataset.get(position).albumTitle);
        holder.creationTime.setText("Time created: " + dataset.get(position).creationTime);
        Glide.with(getApplicationContext()).load(dataset.get(position).thumbnailLink).into(holder.thumbnail);
        Log.i("test", "onbindviewHolder");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShowPhotoActivity.class);
                intent.putExtra("url", dataset.get(position).photoLink);
                getApplicationContext().startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_card, parent, false);
        return new PhotoViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }


}
