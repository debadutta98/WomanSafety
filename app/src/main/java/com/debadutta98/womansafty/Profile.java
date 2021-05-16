package com.debadutta98.womansafty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kusu.loadingbutton.LoadingButton;
import com.polyak.iconswitch.IconSwitch;
import com.skydoves.powermenu.CircularEffect;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Profile extends AppCompatActivity {
private LoadingButton logout;
private ImageView menu_profile;
private ImageView add_number;
private IconSwitch gotomap;
private PowerMenu powerMenu;
private TextView name_text,address_text;
private Uri filePath;
private final int PICK_IMAGE_REQUEST = 71;
private CircleImageView profile_image,circle_image;
private FragmentContainerView fragmentContainerView;
private CardView laws,tips,selfdefence,video;
private JsonConverter jsonConverter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        logout=(LoadingButton) findViewById(R.id.logoutloadingButton);
        menu_profile=(ImageView)findViewById(R.id.menu_profile);
        add_number=(ImageView)findViewById(R.id.add_contacts);
        gotomap=findViewById(R.id.icon_switch_profile);
        fragmentContainerView=findViewById(R.id.fragment_container);
        profile_image=(CircleImageView)findViewById(R.id.profile_image);
        circle_image=(CircleImageView)findViewById(R.id.user_circleImageView);
        name_text=findViewById(R.id.user_name);
        address_text=findViewById(R.id.address_user);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonConverter=retrofit.create(JsonConverter.class);
        Paper.init(this);
        //
        laws=(CardView)findViewById(R.id.laws);
        tips=(CardView)findViewById(R.id.tips);
        selfdefence=(CardView)findViewById(R.id.self_defence);
        video=(CardView)findViewById(R.id.self_defence_video);
        //
        String phone=Paper.book().read(Cache.userPhoneKey);
        renewPic(phone);
        gotomap.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                MapsFragment mapsFragment = new MapsFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_bottom,  R.anim.slide_out_bottom);

                if(current==IconSwitch.Checked.RIGHT) {
                    fragmentTransaction.add(R.id.fragment_container, mapsFragment, "FRAGMENT_MAPVIEW_PROFILE").commit();
                }
                else
                {
                    fragmentTransaction.remove(fragmentManager.findFragmentById(R.id.fragment_container)).commit();
                }
            }
        });

        laws.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Toast.makeText(Profile.this,"laws",Toast.LENGTH_SHORT).show();
               LawsFragment lawsFragment=LawsFragment.newInstance("0","1");
                FragmentManager fragmentManager=getSupportFragmentManager();
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
               fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left, R.anim.pop_enter, R.anim.pop_exit);
                fragmentTransaction.add(R.id.fragment_container,lawsFragment,"FRAGMENT_LAWS").commit();
            }
        });
        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Profile.this,"tips",Toast.LENGTH_SHORT).show();
                TipsFragment tipsFragment=TipsFragment.newInstance("0","1");
                FragmentManager fragmentManager=getSupportFragmentManager();
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left, R.anim.pop_enter, R.anim.pop_exit);
                fragmentTransaction.add(R.id.fragment_container,tipsFragment,"FRAGMENT_TIPS").commit();
            }
        });
        selfdefence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Profile.this,"selfdefence",Toast.LENGTH_SHORT).show();
                Selfdefence selfdefence=Selfdefence.newInstance("0","1");
                FragmentManager fragmentManager=getSupportFragmentManager();
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left, R.anim.pop_enter, R.anim.pop_exit);
                fragmentTransaction.add(R.id.fragment_container,selfdefence,"FRAGMENT_SELF_DEFENCE").commit();
            }
        });
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Profile.this,"video",Toast.LENGTH_SHORT).show();
                VideoFragment selfdefence=VideoFragment.newInstance("0","1");
                FragmentManager fragmentManager=getSupportFragmentManager();
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left, R.anim.pop_enter, R.anim.pop_exit);
                fragmentTransaction.add(R.id.fragment_container,selfdefence,"FRAGMENT_VIDEO").commit();
            }
        });
        add_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this,AddContact.class).putExtra("send","1"));
            }
        });
        menu_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                 powerMenu = new PowerMenu.Builder(Profile.this)
                       // .addItemList(list) // list has "Novel", "Poerty", "Art"
                        .addItem(new PowerMenuItem("Reset PassWord", false)) // add an item.
                        .addItem(new PowerMenuItem("Change Phone", false))
                        .addItem(new PowerMenuItem("Delete Account",false))// aad an item list.
                        .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT).
                        .setMenuRadius(10f) // sets the corner radius.
                        .setMenuShadow(10f) // sets the shadow.
                        .setTextColor(ContextCompat.getColor(Profile.this,R.color.black))
                        .setTextGravity(Gravity.CENTER)
                        .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                        .setSelectedTextColor(Color.WHITE)
                        .setMenuColor(Color.WHITE)
                        .setSelectedMenuColor(ContextCompat.getColor(Profile.this, R.color.appcolor))
                         .setCircularEffect(CircularEffect.BODY)
                        .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                            @Override
                            public void onItemClick(int position, PowerMenuItem item)
                            {

                              if(item.getTitle().equals("Reset PassWord"))
                              {
                                  AlertDialog.Builder alertadd = new AlertDialog.Builder(new ContextThemeWrapper(Profile.this, R.style.AlertDialogCustom));
                                  LayoutInflater factory = LayoutInflater.from(Profile.this);
                                 // AlertDialog alertDialog = alertadd.create();

                                 // alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                  final View view = factory.inflate(R.layout.change_password, null);
                                  alertadd.setView(view);
                                  alertadd.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dlg, int sumthin) {
                                          EditText prev_password=view.findViewById(R.id.prev_password);
                                          EditText new_password=view.findViewById(R.id.change_password);
                                          HashMap<String,String> map=new HashMap<String,String>();
                                          String prev=prev_password.getText().toString().trim();
                                          String new_pass=new_password.getText().toString().trim();
                                          String phone=(String)Paper.book().read(Cache.userPhoneKey);
                                          if(!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(prev) && !TextUtils.isEmpty(new_pass))
                                          {
                                              String sql="Update users SET PASSWORD="+"'"+new_pass+"'"+"WHERE PASSWORD="+"'"+prev+"'"+"AND PHONE="+"'"+phone+"'";
                                              map.put("sql",sql);
                                              Call<Void> call=jsonConverter.change(map);
                                              call.enqueue(new Callback<Void>() {
                                                  @Override
                                                  public void onResponse(Call<Void> call, Response<Void> response) {
                                                      if(response.code()==200)
                                                      {
                                                          Paper.book(Cache.userPasswordKey).destroy();
                                                          startActivity(new Intent(Profile.this,LoginActivity.class));
                                                          Toast.makeText(Profile.this,"Password Change Successfully",Toast.LENGTH_SHORT).show();
                                                      }
                                                      else
                                                      {
                                                          Toast.makeText(Profile.this,"Code:404",Toast.LENGTH_SHORT).show();
                                                      }
                                                  }

                                                  @Override
                                                  public void onFailure(Call<Void> call, Throwable t) {
                                                      Toast.makeText(Profile.this,"Check your Internet",Toast.LENGTH_SHORT).show();
                                                  }
                                              });
                                          }
                                      }
                                  });
                                  alertadd.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which){

                                      }
                                  });

                                  alertadd.show();
                              }
                              else if(item.getTitle().equals("Change Phone"))
                              {
                                  AlertDialog.Builder alertadd = new AlertDialog.Builder(new ContextThemeWrapper(Profile.this, R.style.AlertDialogCustom));
                                  LayoutInflater factory = LayoutInflater.from(Profile.this);
                                 // AlertDialog alertDialog = alertadd.create();

                                 // alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                  final View view = factory.inflate(R.layout.change_phone, null);
                                  alertadd.setView(view);
                                  alertadd.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dlg, int sumthin) {
                                          EditText user_phone=view.findViewById(R.id.user_phone);
                                          EditText password=view.findViewById(R.id.user_password);
                                          EditText new_phone=view.findViewById(R.id.new_phone);
                                          HashMap<String,String> map=new HashMap<String, String>();
                                          String up=user_phone.getText().toString().trim();
                                          String pass=password.getText().toString().trim();
                                          String newphone=new_phone.getText().toString().trim();
                                          if(!TextUtils.isEmpty(up) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(newphone))
                                          {
                                              String sql="Update users SET PHONE="+"'"+newphone+"'"+"WHERE PASSWORD="+"'"+pass+"'"+"AND PHONE="+"'"+up+"'";
                                              map.put("sql",sql);
                                              Call<Void> call=jsonConverter.change(map);
                                              call.enqueue(new Callback<Void>() {
                                                  @Override
                                                  public void onResponse(Call<Void> call, Response<Void> response) {
                                                      if(response.code()==200)
                                                      {
                                                          Paper.book(Cache.userPhoneKey).destroy();
                                                          startActivity(new Intent(Profile.this,LoginActivity.class));
                                                          Toast.makeText(Profile.this,"Phone Number Change Successfully",Toast.LENGTH_SHORT).show();
                                                      }
                                                      else
                                                      {
                                                          Toast.makeText(Profile.this,"Code:404",Toast.LENGTH_SHORT).show();
                                                      }
                                                  }

                                                  @Override
                                                  public void onFailure(Call<Void> call, Throwable t) {
                                                      Toast.makeText(Profile.this,"Check your Internet",Toast.LENGTH_SHORT).show();
                                                  }
                                              });
                                          }
                                         // Toast.makeText(Profile.this,prev_password.getText().toString(),Toast.LENGTH_SHORT).show();
                                      }
                                  });
                                  alertadd.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which){
                                         // alertDialog.dismiss();
                                      }
                                  });

                                 alertadd.show();
                              }
                              else if(item.getTitle().equals("Delete Account"))
                              {
                                  AlertDialog.Builder alertadd = new AlertDialog.Builder(new ContextThemeWrapper(Profile.this, R.style.AlertDialogCustom));
                                  alertadd.setTitle("Delete Account");
                                  alertadd.setMessage("Are You Sure ?");
                                  alertadd.setIcon(R.drawable.ic_error);
                                  //AlertDialog alertDialog=alertadd.create();
                                  alertadd.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {
                                          String phone=Paper.book().read(Cache.userPhoneKey);
                                          if(!TextUtils.isEmpty(phone))
                                          {
                                              getUID(phone);
                                          }
                                      }
                                  });
                                  alertadd.setPositiveButton("No", new DialogInterface.OnClickListener() {
                                      @Override
                                      public void onClick(DialogInterface dialog, int which) {

                                      }
                                  });
                                  alertadd.show();
                              }
                                powerMenu.dismiss();
                            }
                        })
                        .build();
                powerMenu.showAsDropDown(menu_profile);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Paper.book(Cache.userPasswordKey).destroy();
                Paper.book(Cache.userPhoneKey).destroy();
                startActivity(new Intent(Profile.this,LoginActivity.class));
                //logout.showLoading();
            }
        });
    }

    private void getUID(String phone) {
        HashMap<String, String> map = new HashMap<>();
      //  String phone = Paper.book().read(Cache.userPhoneKey);
        map.put("phone", phone);
        Call<List<User>> call = jsonConverter.details(map);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                //Toast.makeText(Profile.this,"retrive",Toast.LENGTH_SHORT).show();
                List<User> res = response.body();
                if (response.code() == 200) {
                    delete(res.get(0).getID());
                    //addCustomMarkerFromURL(res.get(0).getURL(),latLng);
                    //setMarkers(res.get(0).getID());

                } else if (response.code() == 404) {
                    Toast.makeText(Profile.this, "404", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }

    private void delete(String id) {
        HashMap<String,String> map=new HashMap<>();
        map.put("id",id);
        Call<Void> call=jsonConverter.deleteAccount(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code()==200)
                {
                 Paper.book().destroy();
                 startActivity(new Intent(Profile.this,LoginActivity.class));
                }
                else if(response.code()==404)
                {
                    Toast.makeText(Profile.this,"User Not exit",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Profile.this,"Check your Connection",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCase(String i) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        ImageView image = new ImageView(this);
        Glide.with(Profile.this).load(i).placeholder(R.drawable.avatar).into(image);
        alertadd.setView(image);
        alertadd.setNeutralButton("upload", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
            }
        });
        alertadd.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertadd.show();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(Signup.this, String.valueOf(data.getData()), Toast.LENGTH_LONG).show();
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                circle_image.setImageBitmap(bitmap);
                profile_image.setImageBitmap(bitmap);
                uploadAndUpdate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadAndUpdate() {
        String phone=Paper.book().read(Cache.userPhoneKey);
        if(!TextUtils.isEmpty(phone))
        {
            HashMap<String, String> map = new HashMap<>();
            //  String phone = Paper.book().read(Cache.userPhoneKey);
            map.put("phone", phone);
            Call<List<User>> call = jsonConverter.details(map);
            call.enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    //Toast.makeText(Profile.this,"retrive",Toast.LENGTH_SHORT).show();
                    List<User> res = response.body();
                    if (response.code() == 200) {
                        uploadPicture(res.get(0).getID(),res.get(0).getURL());

                    } else if (response.code() == 404) {
                        Toast.makeText(Profile.this, "404", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {

                }
            });
        }
    }
    private void uploadPicture(String id,String url) {
if(!TextUtils.isEmpty(id))
{
    final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileimage/"+ url);
    storageReference.getDownloadUrl()
            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                   deleteProfileImage(uri.toString(),id);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    int errorCode = ((StorageException) exception).getErrorCode();
                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        StorageReference storageReference2 = FirebaseStorage.getInstance().getReference().child("Photos").child("photo_1.png");
                        createUserProfile(id);
                    }

                }
            });

}
else
{

}
    }

    private void createUserProfile(String id) {
        Toast.makeText(Profile.this,filePath.toString(),Toast.LENGTH_SHORT).show();
        if(filePath != null)
        {
            StorageReference storageReference;
            FirebaseStorage storage;
           String link= UUID.randomUUID().toString();
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();
            StorageReference ref = storageReference.child("profileimage/"+ link);
           // Toast.makeText(Signup.this, link,Toast.LENGTH_LONG).show();
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            updateUserDatabase(id,link);
                          //  buttonLoading.hideLoading();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        }

    }

    private void updateUserDatabase(String id, String link) {
        HashMap<String,String> map=new HashMap<>();
        map.put("id",id);
        map.put("url",link);
        Call<Void> call=jsonConverter.uploadnewImage(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code()==200)
                {
                    Toast.makeText(Profile.this,"Sucessfully Update Profile Image",Toast.LENGTH_SHORT).show();
                }
                else if(response.code()==404)
                {
                    Toast.makeText(Profile.this,"Code:404",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    private void deleteProfileImage(String toString,String id) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(toString);
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
              createUserProfile(id);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
              //  Log.d(TAG, "onFailure: did not delete file");
            }
        });

    }

    private void renewPic(String phone) {
        HashMap<String,String> map=new HashMap<>();
        map.put("phone",phone);
        Call<List<User>> call=jsonConverter.details(map);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if(response.code()==200)
                {
                    List<User> res=response.body();
                    name_text.setText(res.get(0).getName());
                    address_text.setText(res.get(0).getADDRESS());
                    FirebaseStorage storage= FirebaseStorage.getInstance();
                    String url=res.get(0).getURL();
                    if(!TextUtils.isEmpty(url)) {
                        StorageReference stef = storage.getReference().child("profileimage").child(url);
                        stef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    Glide.with(Profile.this)
                                            .load(uri.toString()) // image url
                                            .placeholder(R.drawable.avatar) // any placeholder to load at start
                                            .into(circle_image);
                                    circle_image.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            showCase(uri.toString());
                                        }
                                    });
                                    Glide.with(Profile.this).load(uri.toString()).placeholder(R.drawable.avatar).into(profile_image);
                                }
                            }
                        });
                    }
                    else
                    {
                        circle_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showCase(null);
                            }
                        });
                    }
                }
                else if(response.code()==404)
                {
                    Toast.makeText(Profile.this,"404",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }


}