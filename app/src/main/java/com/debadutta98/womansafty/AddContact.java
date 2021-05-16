package com.debadutta98.womansafty;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import io.paperdb.Paper;

public class AddContact extends AppCompatActivity {
private Button addbutton,clearButton;
    private RecyclerView recyclerView;
   private ImageView back;
    static final int PICK_CONTACT=1;
    private String value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        back=findViewById(R.id.back_button);
    recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Paper.init(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            value = bundle.getString("send");
        }
        addbutton=findViewById(R.id.add_Contact_button);
        readContacts();
        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContacts();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(value.equals("0"))
                startActivity(new Intent(AddContact.this,Home.class));
                else
                    startActivity(new Intent(AddContact.this,Profile.class));
            }
        });
    }

    public void readContacts() {
        recyclerView.setAdapter(new Myadapter(Paper.book().read(Cache.contactsname),Paper.book().read(Cache.contactsnumber),this));
    }

    private void addContacts() {
        Uri uri = Uri.parse("content://contacts");
        Intent intent = new Intent(Intent.ACTION_PICK, uri);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri uri = intent.getData();
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

                Cursor cursor = getContentResolver().query(uri, projection,
                        null, null, null);
                cursor.moveToFirst();

                int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberColumnIndex);

                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = cursor.getString(nameColumnIndex);
                if(Paper.book().read(Cache.contactsname)==null)
                {

                    ArrayList<String> arrayList1=new ArrayList<>();
                    arrayList1.add(name);
                    Paper.book().write(Cache.contactsname,arrayList1);
                    ArrayList<String> arrayList2=new ArrayList<>();
                    arrayList2.add(number);
                    Paper.book().write(Cache.contactsnumber,arrayList2);

                }
                else
                {

                    ArrayList<String> arrayList1=Paper.book().read(Cache.contactsnumber);
                    ArrayList<String> arrayList2=Paper.book().read(Cache.contactsname);
                    arrayList1.add(number);
                    arrayList2.add(name);

                    Paper.book().write(Cache.contactsname,arrayList2);
                    Paper.book().write(Cache.contactsnumber,arrayList1);
                }
                readContacts();

            }
        }
    };
}