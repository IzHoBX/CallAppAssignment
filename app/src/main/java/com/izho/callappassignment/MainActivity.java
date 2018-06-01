package com.izho.callappassignment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MainActivity extends AppCompatActivity {
    private AccessToken accessToken;
    private ArrayList<PhotoModel> photos;
    private ArrayList<AlbumModel> albums;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check log in status
        accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken == null || accessToken.isExpired()) {
            Log.i("login status: ", "not logged in, directing to log in activity");
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        } else {
            Log.i("login status: ", "logged in");
            //array list is used because recylerview uses random access frequently
            photos = new ArrayList<>();
            setupRecyclerView();
            //pullAllAlbums();
            pullAllPhotos();
        }

    }

    private void pullAllAlbums() {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.i("test", object.toString());
                Gson gson = new Gson();
                AlbumResponse response1 = gson.fromJson(object.toString(), AlbumResponse.class);
                for(AlbumModel a: response1.albumMeta.data) {
                    albums.add(a);
                }
                Log.i("Graph API call status", "completed");
            }
        });
        Bundle parameters = new Bundle();
        //to overcome 15-photos limit
        parameters.putString("fields", "albums{name}");
        parameters.putInt("limit", "albums{name}");
        request.setParameters(parameters);
        request.executeAsync();
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
        /*GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.i("test", object.toString());
                Gson gson = new Gson();
                Response response1 = gson.fromJson(object.toString(), Response.class);
                for(Photo p: response1.photos.data) {
                    //assumes the url at index 0 contains the best representable format of a photo
                    photos.add(new PhotoModel(p.picture, p.webp_images[0].source, p.name, p.album.name, p.created_time));
                }
                Log.i("Graph API call status", "completed");
                populateRecyclerView();
            }
        });
        Bundle parameters = new Bundle();
        //to overcome 15-photos limit
        parameters.putString("fields", "photos.limit(100){created_time,name,album,picture,webp_images}");
        request.setParameters(parameters);
        request.executeAsync();*/
    }
}

/**
 * This class contains only the information for needed to display a photo
 */
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

/**
 * This class contains only the information needed to display and pulling photos
 */
class AlbumModel {
    public String id;
    public String name;

    public AlbumModel(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

class AlbumResponse {
    public String id;
    public AlbumMeta albumMeta;

    public class AlbumMeta {
        public Paging paging;
        public AlbumModel[] data;

        public AlbumMeta(Paging paging, AlbumModel[] data) {
            this.paging = paging;
            this.data = data;
        }
    }

    public AlbumResponse(String id, AlbumMeta albumMeta) {
        this.id = id;
        this.albumMeta = albumMeta;
    }
}

class Paging {
    //not important in this app
    public Object cursors;
    public String next;

    public Paging(Object cursors, String next) {
        this.cursors = cursors;
        this.next = next;
    }
}

/**
 * Custom RecyclerView Adapter to drive photo cards
 */
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
