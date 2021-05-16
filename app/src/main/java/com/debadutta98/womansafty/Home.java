package com.debadutta98.womansafty;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polyak.iconswitch.IconSwitch;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.paperdb.Paper;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Home extends AppCompatActivity implements PictureCapturingListener, ActivityCompat.OnRequestPermissionsResultCallback {
private ImageView imageView;
private ImageView bodygard;
private PulsatorLayout pulsator;
private static int i=0;
private FragmentContainerView fragmentContainerView;
private IconSwitch iconSwitch;
private APictureCapturingService pictureService;
private ImageView addcontact;
private JsonConverter jsonConverter;
private boolean init=false;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int SEND_MASSAGE = 2*10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        try {
            if (!init) {
                MediaManager.init(Home.this);
                init = true;
            }
        }
        catch (Exception e)
        {
            init=true;
        }
        iconSwitch=findViewById(R.id.icon_switch_home);
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        addcontact=findViewById(R.id.add);
        imageView=findViewById(R.id.profile_image);
        bodygard=findViewById(R.id.bodygard);
        fragmentContainerView=(FragmentContainerView)findViewById(R.id.map_view) ;
        Paper.init(this);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonConverter=retrofit.create(JsonConverter.class);
        pictureService = PictureCapturingServiceImpl.getInstance(this);
        addcontact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment();
            }
        });
       iconSwitch.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
           @Override
           public void onCheckChanged(IconSwitch.Checked current) {
               MapsFragment mapsFragment = new MapsFragment();
               FragmentManager fragmentManager = getSupportFragmentManager();
               FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
               fragmentTransaction.addToBackStack(null);
               fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_bottom,  R.anim.slide_out_bottom);
               if(current==IconSwitch.Checked.RIGHT) {
                   //Toast.makeText(Home.this, "Toggle", Toast.LENGTH_SHORT).show();

                   fragmentTransaction.add(R.id.map_view, mapsFragment, "FRAGMENT_MAPVIEW").commit();
               }
               else
               {
                   fragmentTransaction.remove(fragmentManager.findFragmentById(R.id.map_view)).commit();
               }
               }
       });
        String phone=Paper.book().read(Cache.userPhoneKey);
        showProfile(phone);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this,Profile.class));
            }
        });
        bodygard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startClickPicture();
                pulsator.start();
            }
        });
        bodygard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stopclickPicture();
                pulsator.stop();
                return true;
            }
        });
    }

    public void stopclickPicture() {
        mHandler.removeCallbacks(mRunnable);
    }

    public void startClickPicture() {
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                pictureService.startCapturing(Home.this);
                mHandler.postDelayed(mRunnable, SEND_MASSAGE);
            }
        }, SEND_MASSAGE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String phone=Paper.book().read(Cache.userPhoneKey);
        if(!TextUtils.isEmpty(phone))
        showProfile(phone);
    }

    private void showProfile(String phone) {
    HashMap<String,String> map=new HashMap<>();
    map.put("phone",phone);
        Call<List<User>> call=jsonConverter.details(map);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if(response.code()==200)
                {
                    List<User> res=response.body();
                    FirebaseStorage storage= FirebaseStorage.getInstance();
                    String url=res.get(0).getURL();
                   if(!TextUtils.isEmpty(url)) {
                        StorageReference stef = storage.getReference().child("profileimage").child(url);
                        stef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    Glide.with(Home.this)
                                            .load(uri.toString()) // image url
                                            .placeholder(R.drawable.avatar) // any placeholder to load at start
                                            .into(imageView);
                                }
                            }
                        });
                    }
                }
                else if(response.code()==404)
                {
                    Toast.makeText(Home.this,"404",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }

    private void startFragment() {
        startActivity(new Intent(Home.this,AddContact.class).putExtra("send","0"));
    }

    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (pictureData != null && pictureUrl != null) {
            runOnUiThread(() -> {
                //convert byte array 'pictureData' to a bitmap (no need to read the file from the external storage)
                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                //scale image to avoid POTENTIAL "Bitmap too large to be uploaded into a texture" when displaying into an ImageView
                final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                //do whatever you want with the bitmap or the scaled one...
            });
          uploadtoCloudnary(pictureUrl);
           // showToast("Picture saved to " + pictureUrl);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            picturesTaken.forEach((pictureUrl, pictureData) -> {
                //convert the byte array 'pictureData' to a bitmap (no need to read the file from the external storage) but in case you
                //You can also use 'pictureUrl' which stores the picture's location on the device
                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
            });
            //showToast("Done capturing all photos!");
            return;
        }
        //showToast("No camera detected!");
    }
    private void showToast(final String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show()
        );
    }


   public void uploadtoCloudnary(String filePath) {

        String requestId = MediaManager.get().upload(filePath).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                // your code here
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                // example code starts here
                Double progress = (double) bytes / totalBytes;
                // post progress to app UI (e.g. progress bar, notification)
                // example code ends here
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
             // Toast.makeText(Home.this, String.valueOf(resultData), Toast.LENGTH_LONG).show();
               // Log.e("Home",String.valueOf(resultData));
                JSONObject movieObject = new JSONObject(resultData);
                String url = null;
                try {
                    url = movieObject.getString("secure_url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getProfileDetails(Paper.book().read(Cache.userPhoneKey),url);
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                // your code here
                Toast.makeText(Home.this, "unable to Uploaded on cloudnary", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                // your code here
            }
        })
                .dispatch();
    }
private void getProfileDetails(String phone,String url)
{
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
                   getLocation(res.get(0).getID(),url);
               }
            }
            else if(response.code()==404)
            {
                Toast.makeText(Home.this, "code 404", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<List<User>> call, Throwable t) {

        }
    });
}
    private void getLocation(String id,String url)
    {
        HashMap<String,String> map=new HashMap<>();
        map.put("id",id);
        Call<List<UserLocation>> call=jsonConverter.getCurrentLocation(map);
        call.enqueue(new Callback<List<UserLocation>>() {
            @Override
            public void onResponse(Call<List<UserLocation>> call, Response<List<UserLocation>> response) {
                if(response.code()==200)
                {
                    List<UserLocation> res=response.body();
                    if(res!=null && res.size()>0)
                    {
                        sendMMS(url,res.get(0).getLAT(),res.get(0).getLOG(),res.get(0).getNAME());
                    }
                }
                else if(response.code()==404)
                {
                    Toast.makeText(Home.this, " code :404", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserLocation>> call, Throwable t) {
            }
        });
    }
    private void sendMMS(String requestId,String lat,String log,String name)
    {
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
            String msg="Hii my name is "+name+" can plz  help me by reach to this location "+map_link+" Here is the images which are click randomly from the location: "+requestId;
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
            Toast.makeText(this,"Please add contacts",Toast.LENGTH_SHORT).show();
        }

    }


}