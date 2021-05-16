package com.debadutta98.womansafty;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyBroadCastReciever extends BroadcastReceiver{
private static int i=0;
private static boolean bool=true;
private boolean t=false;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int SEND_MASSAGE = 2*10000;
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) || intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if(i==3)
            {
                Log.e("MY","ON");
                if(!t){
                    Paper.init(context);
                    if (!TextUtils.isEmpty(Paper.book().read(Cache.userPhoneKey)))
                        startClickPicture();
                    t=true;
                }
                else
                {
                    Log.e("MY","ON");
                    stopclickPicture();
                    t=false;

                }
                i=0;
            }
            else {
                i++;
            }
        }
    }
    private void stopclickPicture() {
        mHandler.removeCallbacks(mRunnable);
    }

    private void startClickPicture() {
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                if(!TextUtils.isEmpty(Paper.book().read(Cache.userPhoneKey)))
                    getProfileDetails(Paper.book().read(Cache.userPhoneKey));
                mHandler.postDelayed(mRunnable, SEND_MASSAGE);
            }
        }, SEND_MASSAGE);
    }

    private void getProfileDetails(String phone)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonConverter jsonConverter=retrofit.create(JsonConverter.class);
        HashMap<String,String> map=new HashMap<>();
        map.put("phone",phone);
        Call<List<User>> call=jsonConverter.details(map);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if(response.code()==200)
                {
                    List<User> res=response.body();
                    if(res!=null && res.size()>0)
                    {
                        getLocation(res.get(0).getID());
                    }
                }
                else if(response.code()==404)
                {
                    Log.e("erro","code:404");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }
    private void getLocation(String id)
    {
        HashMap<String,String> map=new HashMap<>();
        map.put("id",id);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonConverter jsonConverter=retrofit.create(JsonConverter.class);
        Call<List<UserLocation>> call=jsonConverter.getCurrentLocation(map);
        call.enqueue(new Callback<List<UserLocation>>() {
            @Override
            public void onResponse(Call<List<UserLocation>> call, Response<List<UserLocation>> response) {
                if(response.code()==200)
                {
                    List<UserLocation> res=response.body();
                    if(res!=null && res.size()>0)
                    {
                        sendMMS(res.get(0).getLAT(),res.get(0).getLOG(),res.get(0).getNAME());
                    }
                }
                else if(response.code()==404)
                {
                    Log.e("erro","code:401");
                }
            }

            @Override
            public void onFailure(Call<List<UserLocation>> call, Throwable t) {
            }
        });
    }
    private void sendMMS(String lat,String log,String name)
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonConverter jsonConverter=retrofit.create(JsonConverter.class);
        HashMap<String,String> map=new HashMap<String,String>();
        //map.put("url",requestId);
        ArrayList<String> numbers=Paper.book().read(Cache.contactsnumber);
        if(numbers!=null){
            for (int i = 0; i < numbers.size(); i++) {
                if(numbers.get(i).length()>10)
                {
                    numbers.set(i,numbers.get(i).substring(4, 9) + numbers.get(i).substring(10, 15));
                }
                else
                {
                    continue;
                }
            }
            map.put("numberlist",String.valueOf(numbers));
            String map_link="https://maps.google.com/?q="+lat+","+log;
            String msg="Hii my name is "+name+" can plz  help me by reach to this location "+map_link;
            map.put("msg",msg);
            Call<Void> call=jsonConverter.sendMassage(map);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(response.code()==200)
                    {
                        Log.e("Home","massage sent");
                    }
                    else if(response.code()==404)
                    {

                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                }
            });
        }
        else
        {
            Log.e("erro","code:402");
        }

    }


}