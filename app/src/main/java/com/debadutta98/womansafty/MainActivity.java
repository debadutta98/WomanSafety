package com.debadutta98.womansafty;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.subsub.library.BeautyButton;

import org.imaginativeworld.whynotimagecarousel.CarouselItem;
import org.imaginativeworld.whynotimagecarousel.CarouselOnScrollListener;
import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageCarousel carousel;
    private static LocationManager locationManager;
    private static final int REQUEST_CHECK_SETTINGS = 111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       carousel = findViewById(R.id.carousel);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        imageSlider();

    }


    private void imageSlider() {
        List<CarouselItem> list = new ArrayList<>();
        list.add(
                new CarouselItem(
                        "https://i.ibb.co/VjgNB6M/animation-500-kimwassn.gif","Feel safe"
                )
        );
        list.add(
                new CarouselItem(
                        "https://i.ibb.co/khHYXF3/animation-500-kimwl0qy.gif","We are with you"
                )
        );
        list.add(
                new CarouselItem(
                         "https://i.ibb.co/3Sdm8CJ/animation-500-kimvuyit.gif","Click Random image"
                )
        );
        list.add(
                new CarouselItem(
                        "https://i.ibb.co/VLkjQr5/animation-500-kimwl46a.gif","SOS available for emergency"
                )
        );
        carousel.addData(list);
        carousel.setOnScrollListener(new CarouselOnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                // ...
            }

            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState, int position, @Nullable CarouselItem carouselItem) {
              if(position==1 || position==2 || position==0)
              {
                  Button b=(Button)findViewById(R.id.btn_next);
                  b.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          carousel.next();
                      }
                  });
              }
              else
              {
                  Button b=(Button)findViewById(R.id.btn_next);
                  b.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                         if(ContextCompat.checkSelfPermission(getApplicationContext(),
                                 Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                         &&
                                 ContextCompat.checkSelfPermission(getApplicationContext(),
                                         Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED
                         &&
                                 ContextCompat.checkSelfPermission(getApplicationContext(),
                                         Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
                        &&
                                 ContextCompat.checkSelfPermission(getApplicationContext(),
                                         Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                         &&
                                 ContextCompat.checkSelfPermission(getApplicationContext(),
                                         Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED
                         &&
                                 ContextCompat.checkSelfPermission(getApplicationContext(),
                                         Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
                         ) {
                             if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                 startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                 startLocationService();
                             }
                             else
                                 buildAlertMessageNoGps();
                          }
                         else
                         {
                             ActivityCompat.requestPermissions(MainActivity.this,
                                     new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA,
                                             Manifest.permission.READ_CONTACTS,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                     1);
                             Toast.makeText(MainActivity.this,"Please Grant All the permission",Toast.LENGTH_SHORT).show();
                         }
                      }
                  });
              }
            }
        });

        Intent intent1 = new Intent(MainActivity.this, ScreenOnOffService.class);
            startService(intent1);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA,
                        Manifest.permission.READ_CONTACTS,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                1);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //buildAlertMessageNoGps();
        switch (requestCode) {
            case 1:
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(MainActivity.this,"Plz allow all the request",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void buildAlertMessageNoGps() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true); //this displays dialog box like Google Maps with two buttons - OK and NO,THANKS

        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MainActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        buildAlertMessageNoGps();
                        break;
                    default:
                        break;
                }
                break;
        }
    }
    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                MainActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }
//may go to home
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.debadutta98.womansafty.LocationService".equals(service.service.getClassName())) {

                return true;
            }
        }
        return false;
    }
}