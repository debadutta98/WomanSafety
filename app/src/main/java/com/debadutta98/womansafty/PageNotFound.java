package com.debadutta98.womansafty;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PageNotFound#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PageNotFound extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Runnable mRunnable;
    JsonConverter jsonConverter;
    private static final int NETWORK_CONNECTIVITY_CHECK = 1000;
    private Handler mHandler;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PageNotFound() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PageNotFound.
     */
    // TODO: Rename and change types and number of parameters
    public static PageNotFound newInstance(String param1, String param2) {
        PageNotFound fragment = new PageNotFound();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Paper.init(getContext());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonConverter=retrofit.create(JsonConverter.class);
        mHandler = new Handler();
    }

    private void startUserConnectivityRunnable(){
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
if(connectivity())
{
stopUserConnectivity();
String phone=Paper.book().read(Cache.userPhoneKey);
String pass=Paper.book().read(Cache.userPasswordKey);
if(!TextUtils.isEmpty(pass) && !TextUtils.isEmpty(phone))
{
    checkSaveCredentials(phone ,pass);
}
}
mHandler.postDelayed(mRunnable,NETWORK_CONNECTIVITY_CHECK);
            }
        }, NETWORK_CONNECTIVITY_CHECK);
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
                        startActivity(new Intent(getContext(), Home.class));
                    } else if (response.code() == 404 || response.code()==400) {
                        startActivity(new Intent(getContext(), LoginActivity.class));
                    }
                    stopUserConnectivity();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    // startActivity(new Intent(LoginActivity.this,LoginActivity.class));
                }
            });
        }
        else
        {
            startActivity(new Intent(getContext(), MainActivity.class));
        }
    }
    private boolean connectivity() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        return connected;
    }
    private void stopUserConnectivity(){
        mHandler.removeCallbacks(mRunnable);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view= inflater.inflate(R.layout.fragment_page_not_found, container, false);
        ImageView pagenotfound=(ImageView)view.findViewById(R.id.page_not_found);
        Glide.with(this).load(R.drawable.pagenotfound).into(pagenotfound);
       return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startUserConnectivityRunnable();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUserConnectivity();
    }
}