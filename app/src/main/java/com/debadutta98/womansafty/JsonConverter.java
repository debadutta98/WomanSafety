package com.debadutta98.womansafty;


import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface JsonConverter {
@POST("/signup")
    Call<Void> register(@Body HashMap<String,String> map);
@POST("/login")
    Call<Void> checkUser(@Body HashMap<String,String> params);
@POST("/details")
Call<List<User>> details(@Body HashMap<String,String> map);
@POST("/location")
    Call<Void> locationUpdate(@Body HashMap<String,String> map);
@POST("/gpslocation")
    Call<List<UserLocation>> locatePeople();
@POST("/send")
    Call<Void> sendMassage(@Body HashMap<String,String> map);
@POST("/currentlocation")
    Call<List<UserLocation>> getCurrentLocation(@Body HashMap<String,String> map);
@POST("/password")
    Call<Void> change(@Body HashMap<String,String> map);
@POST("/upload")
    Call<Void>  uploadnewImage(@Body HashMap<String,String> map);
@POST("/delete")
    Call<Void> deleteAccount(@Body HashMap<String,String> map);
}
