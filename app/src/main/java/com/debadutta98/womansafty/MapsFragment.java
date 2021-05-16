package com.debadutta98.womansafty;


import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;

import android.content.Context;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;

import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private JsonConverter jsonConverter;
    private GoogleMap mgoogleMap;
    private LatLngBounds latLngBounds;
    private LocationRequest locationRequest;
 private Marker userLocationMarker;
   private View mCustomMarkerView;
   private ImageView  mMarkerImageView;
    private Handler mHandler = new Handler();
    private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mgoogleMap = googleMap;
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);
            googleMap.getUiSettings().setScrollGesturesEnabled(true);
            googleMap.getUiSettings().setTiltGesturesEnabled(true);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        Paper.init(getActivity());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonConverter = retrofit.create(JsonConverter.class);
        getLastKnowLocation();
        mCustomMarkerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_costum_marker, null);
        mMarkerImageView=(ImageView)mCustomMarkerView.findViewById(R.id.profile_imag_urle);
//        locationRequest = LocationRequest.create();
//        locationRequest.setInterval(500);
//        locationRequest.setFastestInterval(500);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLastKnowLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    Double lat = latLng.latitude;
                    Double log = latLng.longitude;

                        updateLocation(latLng);

                }
            }
        });
    }

    private void updateLocation(LatLng latLng) {

        HashMap<String, String> map = new HashMap<>();
        String phone = Paper.book().read(Cache.userPhoneKey);
        map.put("phone", phone);
        Call<List<User>> call = jsonConverter.details(map);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                List<User> res = response.body();
                if (response.code() == 200) {
                    updateCodinate(res.get(0).getID(), latLng);
                    //addCustomMarkerFromURL(res.get(0).getURL(),latLng);
                    setMarkers(res.get(0).getID());

                } else if (response.code() == 404) {
                    Toast.makeText(getActivity(), "404", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }



    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if(mgoogleMap!=null)
            {
                    setUserLocationMarker(locationResult.getLastLocation());
            }
        }
    };
private void setUserLocationMarker(Location location) {
    LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
if(userLocationMarker==null)
{
    updateLocation(latLng);
    MarkerOptions markerOptions=new MarkerOptions();
    markerOptions.position(latLng);
   userLocationMarker= mgoogleMap.addMarker(markerOptions);
    //setCameraView(latLng);
    mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
}
else
{
    updateLocation(latLng);
userLocationMarker.setPosition(latLng);
    mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
}
}
    private void startLocationupdate() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationupdate() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
}

    @Override
    public void onStart() {
        super.onStart();
        startLocationupdate();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationupdate();
    }

    private void updateCodinate(String id, LatLng latLng) {
        HashMap<String,String> map=new HashMap<>();
        map.put("id",id);
        map.put("lat",String.valueOf(latLng.latitude));
        map.put("log",String.valueOf(latLng.longitude));
        Call<Void> call=jsonConverter.locationUpdate(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code()==200)
                {
                    Toast.makeText(getActivity(),"200",Toast.LENGTH_SHORT).show();
                }
                else if(response.code()==404)
                {
                    Toast.makeText(getActivity(),"404",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

        mMarkerImageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;

    }
    private Bitmap getMarkerBitmapFromView(View view, @DrawableRes int resId)
    {
        mMarkerImageView.setImageResource(resId);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }
    private void addCustomMarkerFromURL(String ImageUrl,LatLng latLng,int a,String id,String name)
    {
        if (mgoogleMap == null) {
            return;
        }
        // adding a marker with image from URL using glide image loading library
if(!TextUtils.isEmpty(ImageUrl)) {
    FirebaseStorage storage= FirebaseStorage.getInstance();
    StorageReference stef = storage.getReference().child("profileimage").child(ImageUrl);
    stef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
        @Override
        public void onSuccess(Uri uri) {
            if (uri != null)
            {
                Glide.with(getActivity())
                        .asBitmap()
                        .placeholder(R.drawable.avatar)
                        .load(uri.toString()).fitCenter()
                        .into(new CustomTarget<Bitmap>()
                        {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                // Toast.makeText(getActivity(), "activity", Toast.LENGTH_SHORT).show();
                                if(a==1){
                                    Marker m=mgoogleMap.addMarker(new MarkerOptions().position(latLng)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, resource))));
                                    m.setTag(id);
                                    m.setTitle(name);
                                    mMarkerArray.add(m);
                                    mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f));
                                }
                                else
                                {
                                    Marker m=mgoogleMap.addMarker(new MarkerOptions().position(latLng)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, resource))));
                                    m.setTag(id);
                                    m.setTitle(name);
                                    mMarkerArray.add(m);
                                    mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f));
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                Toast.makeText(getActivity(), "clear", Toast.LENGTH_SHORT).show();
                            }

                        });
            }
        }
    });

}
else
{
    if(a==1){

       Marker m= mgoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, R.drawable.avatar))));
       m.setTitle(name);
       m.setTag(id);
       mMarkerArray.add(m);
        mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f));
    }
    else
    {
        Marker m= mgoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, R.drawable.avatar))));
        m.setTitle(name);
        m.setTag(id);
        mMarkerArray.add(m);
        mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f));
    }
}
    }

    private void setMarkers(String id)
    {
        Call<List<UserLocation>> call=jsonConverter.locatePeople();
        call.enqueue(new Callback<List<UserLocation>>() {
            @Override
            public void onResponse(Call<List<UserLocation>> call, Response<List<UserLocation>> response) {
                List<UserLocation> res=response.body();
                if(response.code()==200 && res.size()!=0)
                {
                        for(UserLocation userLocation : res)
                        {
                                   if (userLocation.getID().equals(id)) {
                                       addCustomMarkerFromURL(userLocation.getURL(), new LatLng(Double.parseDouble(userLocation.getLAT()), Double.parseDouble(userLocation.getLOG())), 1, userLocation.getID(), userLocation.getNAME());
                                   } else {
                                       addCustomMarkerFromURL(userLocation.getURL(), new LatLng(Double.parseDouble(userLocation.getLAT()), Double.parseDouble(userLocation.getLOG())), 0, userLocation.getID(), userLocation.getNAME());
                                   }
                        }
                }
                else if(response.code()==404)
                {

                }
            }

            @Override
            public void onFailure(Call<List<UserLocation>> call, Throwable t) {

            }
        });
    }
    private void startUserLocationsRunnable(){
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void retrieveUserLocations() {
        HashMap<String, String> map = new HashMap<>();
        String phone = Paper.book().read(Cache.userPhoneKey);
        map.put("phone", phone);
        Call<List<User>> call = jsonConverter.details(map);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
             //   Toast.makeText(getActivity(),"retrive",Toast.LENGTH_SHORT).show();
                List<User> res = response.body();
                if (response.code() == 200) {
                    updateCodinate(res.get(0).getID());
                    //addCustomMarkerFromURL(res.get(0).getURL(),latLng);
                    //setMarkers(res.get(0).getID());

                } else if (response.code() == 404) {
                    Toast.makeText(getActivity(), "404", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }

    private void updateCodinate(String id) {
        Call<List<UserLocation>> call=jsonConverter.locatePeople();
        call.enqueue(new Callback<List<UserLocation>>() {
            @Override
            public void onResponse(Call<List<UserLocation>> call, Response<List<UserLocation>> response) {
                List<UserLocation> res=response.body();
                if(response.code()==200 && res.size()!=0 && res!=null)
                {
                    ArrayList<Marker> markers=mMarkerArray;
                   // mMarkerArray.clear();
                    for(UserLocation userLocation : res)
                    {
                        for(int i=0;i<markers.size();i++) {
                            if(markers.get(i).getTag().toString().equals(userLocation.getID()))
                            {
                                markers.get(i).setPosition(new LatLng(Double.parseDouble(userLocation.getLAT()),Double.parseDouble(userLocation.getLOG())));
                                markers.set(i,markers.get(i));
                            }
                            else
                            {
                                continue;
                            }

                        }
                    }
                }
                else if(response.code()==404)
                {

                }
            }

            @Override
            public void onFailure(Call<List<UserLocation>> call, Throwable t) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        startUserLocationsRunnable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

}