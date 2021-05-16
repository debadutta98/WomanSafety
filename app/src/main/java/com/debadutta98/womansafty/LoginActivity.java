package com.debadutta98.womansafty;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
private TextView signup;
private Button login;
  private EditText username,passed;
  private TextView forget_password;
  private JsonConverter jsonConverter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signup=findViewById(R.id.tvSignUp);
        login=(Button)findViewById(R.id.loginbutton);
        forget_password=findViewById(R.id.tvIForgot);
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
       Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiKey.getHTTP())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonConverter=retrofit.create(JsonConverter.class);
        Paper.init(this);
        username=(EditText) findViewById(R.id.userphone);
        passed=(EditText)findViewById(R.id.etPassword);
        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String phone=username.getText().toString();
                String pass=passed.getText().toString();
Toast.makeText(LoginActivity.this,pass+phone,Toast.LENGTH_SHORT).show();
              if(checkLogin(phone,pass))
              {
                 validLogin(phone,pass);
              }
            }
        });
      signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.startActivity(new Intent(LoginActivity.this,Signup.class));
            }
        });
    }

    private void validLogin(String phone, String pass) {
        HashMap<String,String> map=new HashMap<>();
        map.put("phone",phone);
        map.put("password",pass);
        Call<Void> call=jsonConverter.checkUser(map);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response)
            {
                if(response.code()==200)
                {
                    Paper.book().write(Cache.userPhoneKey,phone);
                    Paper.book().write(Cache.userPasswordKey,pass);
                    startActivity(new Intent(LoginActivity.this,Home.class));
                }
                else if(response.code()==404)
                {
                    Toast.makeText(LoginActivity.this,"404",Toast.LENGTH_SHORT).show();
                }
                else if(response.code()==400)
                {
                    Toast.makeText(LoginActivity.this,"400",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setProgressAlert("Check your connection");
                Log.d("exception1is",String.valueOf(t));
                // startActivity(new Intent(LoginActivity.this,LoginActivity.class));
            }
        });
    }

    private boolean checkLogin(String phone, String pass)
    {
        if(TextUtils.isEmpty(phone))
        {
            username.setError("Enter valid phone",getDrawable(R.drawable.ic_error));
            return false;
        }
        if(TextUtils.isEmpty(pass))
        {
            passed.setError("Enter valid password",getDrawable(R.drawable.ic_error));
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
                .setTitle("Invalid Signin")
//set message
                .setMessage(s)
//set positive button
                .setPositiveButton("TryAgain", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .show();
    }
}