package com.debadutta98.womansafty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kusu.loadingbutton.LoadingButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Signup extends AppCompatActivity {
private LoadingButton buttonLoading;
private Uri filePath;
private String link;
StorageReference storageReference;
private EditText name,phone,address,password;
private   FirebaseStorage storage;
private ImageView circleImageView;
private JsonConverter jsonConverter;
    private final int PICK_IMAGE_REQUEST = 71;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        setContentView(R.layout.activity_signup);
      //  Paper.init(this);
        name=findViewById(R.id.name_edit);
        phone=findViewById(R.id.phone_edit);
        password=findViewById(R.id.password_edit);
        address=findViewById(R.id.address_edit);
        circleImageView=findViewById(R.id.profile_image);
        buttonLoading=findViewById(R.id.loadingButton);
      Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonConverter=retrofit.create(JsonConverter.class);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        buttonLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String user_phone=phone.getText().toString();
                String user_name=name.getText().toString();
                String user_address=address.getText().toString();
                String user_password=password.getText().toString();
                if(createAccount(user_phone,user_name,user_address,user_password))
                {
                    uploadImage(user_phone);
                    buttonLoading.showLoading();
                    validValues(user_phone,user_name,user_address,user_password);
                }
                else
                {
                    buttonLoading.hideLoading();
                }
            }
        });

    }
    private void validValues(String user_phone, String user_name, String user_address, String user_password) {
        HashMap<String,String> map=new HashMap<>();
        String id=UUID.randomUUID().toString();
        map.put("id",id);
        map.put("gender","female");
        map.put("url",link);
        map.put("phone",user_phone);
        map.put("address",user_address);
        map.put("password",user_password);
        map.put("name",user_name);
        Call<Void> call=jsonConverter.register(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code()==200)
                {
                    //startActivity(new Intent(AccountActivity.this,LoginActivity.class));
                    Toast.makeText(Signup.this,"Successful",Toast.LENGTH_SHORT).show();
                }
                else if(response.code()==404)
                {
                   setProgressAlert("phone number already exit");
                }
                startActivity(new Intent(Signup.this,LoginActivity.class));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setProgressAlert("Check your connection");
                Toast.makeText(Signup.this,String.valueOf(t),Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Signup.this,LoginActivity.class));
            }
        });
    }

    private void chooseImage() {

        AlertDialog alertDialog = new AlertDialog.Builder(Signup.this)
//set icon
                .setIcon(R.drawable.camera)
//set title
                .setTitle("Choose Picture")
//set message
//set positive button
                .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
                    }
                })
                .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(ContextCompat.checkSelfPermission(Signup.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(Signup.this,new String[]{Manifest.permission.CAMERA},101);
                        }
                        else
                        {
                            openCamera();
                        }
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,102);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==101)
        {
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                openCamera();
            }
            else
            {

            }
        }
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
                circleImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode==102 && resultCode == RESULT_OK){
            filePath = data.getData();
            Bitmap bitmap =(Bitmap)data.getExtras().get("data");
            circleImageView.setImageBitmap(bitmap);
            filePath = getImageUri(getApplicationContext(), bitmap);
        }
    }

    private void uploadImage(String user_phone) {
        if(filePath != null)
        {
            link=UUID.randomUUID().toString();
            StorageReference ref = storageReference.child("profileimage/"+ link);
           // Toast.makeText(Signup.this, link,Toast.LENGTH_LONG).show();
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            buttonLoading.hideLoading();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            buttonLoading.hideLoading();
                            Toast.makeText(Signup.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            buttonLoading.hideLoading();
                        }
                    });
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private boolean createAccount(String user_phone, String user_name, String user_address, String user_password)
    {
if(user_phone.length()<10)
{
    int colorInt = getResources().getColor(R.color.red);
    ColorStateList csl = ColorStateList.valueOf(colorInt);

    phone.setBackgroundTintList(csl);
    phone.setError("Enter valid Phone number", getResources().getDrawable(R.drawable.ic_error));
    return false;
}
if(TextUtils.isEmpty(user_name))
{
    int colorInt = getResources().getColor(R.color.red);
    ColorStateList csl = ColorStateList.valueOf(colorInt);
    name.setBackgroundTintList(csl);
    name.setError("Enter valid Phone number", getResources().getDrawable(R.drawable.ic_error));
    return false;
}
if(TextUtils.isEmpty(user_address))
{
    int colorInt = getResources().getColor(R.color.red);
    ColorStateList csl = ColorStateList.valueOf(colorInt);
    address.setBackgroundTintList(csl);
    address.setError("Enter valid Phone number", getResources().getDrawable(R.drawable.ic_error));
    return false;
}
if(TextUtils.isEmpty(user_password))
{
    int colorInt = getResources().getColor(R.color.red);
    ColorStateList csl = ColorStateList.valueOf(colorInt);
    password.setBackgroundTintList(csl);
    password.setError("Enter valid Phone number", getResources().getDrawable(R.drawable.ic_error));
    return false;
}
return true;
    }
    public void setProgressAlert (String s)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
//set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
//set title
                .setTitle("Invalid SignUp")
//set message
                .setMessage(s)
//set positive button
                .setPositiveButton("TryAgain", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        startActivity(new Intent(Signup.this, LoginActivity.class));
                        finish();
                    }
                })
                .show();
    }
}