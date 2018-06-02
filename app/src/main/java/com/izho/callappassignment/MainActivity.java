package com.izho.callappassignment;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MainActivity extends AppCompatActivity {
    private AccessToken accessToken;
    private ArrayList<AlbumModel> albums;
    int currAlbum = 0;
    private RecyclerView list;
    private MyAdapter adapter;
    private View loading;

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
            //array list is used because recyler view uses random access frequently
            albums = new ArrayList<>();
            loading = findViewById(R.id.loading);
            setupRecyclerView();
            pullAllAlbums();
        }

    }

    private void pullAllAlbums() {
        GraphRequest.Callback graphCallback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                try {
                    JSONArray rawAlbumsData = response.getJSONObject().getJSONObject("albums").getJSONArray("data");
                    for(int i=0; i<rawAlbumsData.length();i++) {
                        albums.add(new AlbumModel(((JSONObject) rawAlbumsData.get(i)).get("id").toString(),
                                ((JSONObject) rawAlbumsData.get(i)).get("name").toString()));

                    }
                    Log.i("totalAlbum: ", albums.size() + "");
                    GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                    if(nextRequest != null){
                        nextRequest.setCallback(this);
                        nextRequest.executeAndWait();
                    } else {
                        populateRecyclerView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Bundle parameters = new Bundle();
        //assume the number of albums < limit of Graph API
        parameters.putString("fields", "albums{name}");

        new GraphRequest(accessToken, "me", parameters, HttpMethod.GET, graphCallback).executeAsync();
    }

    private void populateRecyclerView() {
        //requires user to have at least 1 album
        if(currAlbum < albums.size()) {
            pullAllPhotosFromAlbum(currAlbum);
            loading.setVisibility(View.VISIBLE);
            loading.bringToFront();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupRecyclerView() {
        list = (RecyclerView) findViewById(R.id.list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        list.setLayoutManager(layoutManager);
        list.hasFixedSize();

        list.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                //loads new photos if user reaches the bottom of list
                if(!list.canScrollVertically(1))
                    populateRecyclerView();
            }
        });

        adapter = new MyAdapter();
        list.setAdapter(adapter);
    }

    /**
     * Pull all photos of an album indicated by the index in the albums arraylist.
     */
    private void pullAllPhotosFromAlbum(final int albumIndex) {
        GraphRequest.Callback graphCallback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                try {
                    JSONArray rawPhotosData = response.getJSONObject().getJSONObject("photos").getJSONArray("data");
                    int oldNumPhotos = adapter.getItemCount();
                    for(int i=0; i<rawPhotosData.length();i++) {
                        String name = "";
                        try {
                            name = ((JSONObject) rawPhotosData.get(i)).get("name").toString();
                            if (name.length() > 30)
                                name = name.substring(0, 30);
                        } catch (JSONException e) {
                            //no name
                            name = "-";
                        }
                        //assume the link at webp_images[0] is always sufficient for displaying as large image
                        adapter.addItem(new PhotoModel(((JSONObject) rawPhotosData.get(i)).get("picture").toString(),
                                ((JSONObject) rawPhotosData.get(i)).getJSONArray("webp_images").getJSONObject(0).get("source").toString(),
                                name,
                                albums.get(albumIndex).name,
                                ((JSONObject) rawPhotosData.get(i)).get("created_time").toString()));
                    }
                    Log.i("totalPhotos: ", adapter.getItemCount() + "");
                    GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                    if(nextRequest != null){
                        nextRequest.setCallback(this);
                        nextRequest.executeAndWait();
                    } else {
                        currAlbum++;
                        adapter.notifyItemInserted(oldNumPhotos);
                        loading.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Loaded new photos!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    currAlbum++;
                    loading.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "No more photos!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Bundle parameters = new Bundle();
        parameters.putString("fields", "photos{name,created_time,picture,webp_images}");

        new GraphRequest(accessToken, albums.get(albumIndex).id, parameters, HttpMethod.GET, graphCallback).executeAsync();
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

    public MyAdapter() {
        this.dataset = new ArrayList<>();
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

    public void addItem(PhotoModel newPhoto) {
        dataset.add(newPhoto);
    }
}
