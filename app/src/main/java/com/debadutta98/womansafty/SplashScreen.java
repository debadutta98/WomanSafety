package com.debadutta98.womansafty;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.HashMap;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashScreen extends AppCompatActivity {
    private static String TAG = SplashScreen.class.getName();
    private static long SLEEP_TIME = 2;    // Time in seconds to show the picture
private ImageView imageView;
private FragmentContainerView fragmentContainerView;
    private JsonConverter jsonConverter;
    private static LocationManager locationManager;

static int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar
        SplashScreen.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Removes notification bar
        setContentView(R.layout.activity_splash_screen);
        imageView=(ImageView)findViewById(R.id.imageView);
        Glide.with(SplashScreen.this)
                .load(R.drawable.woman)
                .into(imageView);
 //your layout with the picture
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonConverter=retrofit.create(JsonConverter.class);
        Paper.init(this);
        fragmentContainerView=findViewById(R.id.containerview404);
        // Start timer and launch main activity
        IntentLauncher launcher = new IntentLauncher();
        launcher.start();
    }

    private class IntentLauncher extends Thread {
        @Override
        /**
         * Sleep for some time and than start new activity.
         */
        public void run() {
            try {
                // Sleeping
                Thread.sleep(SLEEP_TIME*2000);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            if(connectivity())
            {
                String phone =Paper.book().read(Cache.userPhoneKey);
                String pass=Paper.book().read(Cache.userPasswordKey);
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
                )  {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        checkSaveCredentials(phone, pass);
                        Intent intent1 = new Intent(SplashScreen.this, ScreenOnOffService.class);
                        startService(intent1);
                        startLocationService();
                    } else {
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                        //buildAlertMessageNoGps();
                    }
                }
                else
                {
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                }
            }
            else
            {
                PageNotFound pageNotFound=PageNotFound.newInstance("0","1");
                FragmentManager fragmentManager=getSupportFragmentManager();
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left, R.anim.pop_enter, R.anim.pop_exit);
                fragmentTransaction.add(R.id.containerview404,pageNotFound,"FRAGMENT_PAGE_NOTFOUND").commit();
            }
        }
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                SplashScreen.this.startForegroundService(serviceIntent);
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
    private void checkSaveCredentials(String phone,String pass) {
        if(!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(pass)){
            HashMap<String, String> map = new HashMap<>();
            map.put("phone", phone);
            map.put("password", pass);
            Call<Void> call = jsonConverter.checkUser(map);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 200) {
                        // Paper.book().write(Cache.userPhoneKey,phone);
                        // Paper.book().write(Cache.userPasswordKey,pass);
                        startActivity(new Intent(SplashScreen.this, Home.class));
                    } else if (response.code() == 404 || response.code()==400) {
                        startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                   if(TextUtils.isEmpty(Paper.book().read(Cache.userPasswordKey)) || TextUtils.isEmpty(Paper.book().read(Cache.userPasswordKey)) ) {
                        Toast.makeText(SplashScreen.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        PageNotFound pageNotFound = PageNotFound.newInstance("0", "1");
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left, R.anim.pop_enter, R.anim.pop_exit);
                        fragmentTransaction.add(R.id.containerview404, pageNotFound, "FRAGMENT_PAGE_NOTFOUND").commit();
                    }
                   else
                   {
                       startActivity(new Intent(SplashScreen.this,LoginActivity.class));
                   }
                }
            });
        }
        else
        {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
        }
    }
    private boolean connectivity() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        return connected;
    }

}
