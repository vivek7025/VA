package com.va.flashapitry1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class landingPage extends AppCompatActivity {

    private String[] PERMISSIONS;
    ProgressDialog progressDialog;
    ArrayList mobileArray;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    CardView cvPermi,cvContinue;
    TextView t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        cvPermi = findViewById(R.id.cvpermi);
        cvContinue = findViewById(R.id.cvContinue);
        t1 = findViewById(R.id.cvText);

        cvContinue.setVisibility(View.INVISIBLE);
        progressDialog = new ProgressDialog(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {

        }else{
            Intent intent = new Intent(getApplicationContext(),getStarted.class);
            startActivity(intent);
        }

        PERMISSIONS = new String[] {

                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };


        cvPermi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasPermissions(landingPage.this,PERMISSIONS)) {

                    ActivityCompat.requestPermissions(landingPage.this,PERMISSIONS,1);
//                    Intent intent = new Intent(getApplicationContext(), permissionGet.class);
//                    startActivity(intent);
                }
                cvPermi.setVisibility(View.INVISIBLE);
                cvContinue.setVisibility(View.VISIBLE);
            }
        });

        cvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Creating your custom model for contact name");
                progressDialog.setTitle("Creating");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                progressDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), getStarted.class);
                startActivity(intent);
            }
        });

    }
    private boolean hasPermissions(Context context, String... PERMISSIONS) {

        if (context != null && PERMISSIONS != null) {

            for (String permission: PERMISSIONS){

                if (ActivityCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Calling Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Calling Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "SMS Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "SMS Permission is denied", Toast.LENGTH_SHORT).show();
            }

            if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Read contact Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Read contact Permission is denied", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[4] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Read Sms Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Read Sms Permission is denied", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[5] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Read Call Log Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Read Call log Permission is denied", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[6] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Record Audio Permission is granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Record Audio Permission is denied", Toast.LENGTH_SHORT).show();
            }


            cvPermi.setVisibility(View.INVISIBLE);
            cvContinue.setVisibility(View.VISIBLE);

        }
    }
    @SuppressLint("Range")
    private ArrayList getAllContacts() {
        ArrayList<String> nameList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur!= null ? cur.getCount() : 0) > 0) {
            while (cur!= null && cur.moveToNext()) {
                @SuppressLint("Range") String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                nameList.add(name);
                if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    pCur.close();
                }
            }
        }
        if (cur!= null) {
            cur.close();
        }
        return nameList;
    }
    private void sendDataToServer(String json) {
        String url = "http://192.168.1.22:5000/getjs";
        new MyHttpRequestTask().execute(url,json);
    }

    private class MyHttpRequestTask extends AsyncTask<String,Integer,String> {
        @Override
        protected String doInBackground(String... params) {
            String my_url = params[0];
            String json = params[1];
//            String my_data = params[1];
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(my_url)
                        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json))
                        .build();
                try (Response response = okHttpClient.newCall(request).execute()) {
                    String resp = response.body().string();
                    progressDialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), getStarted.class);
                    startActivity(intent);
//                    JSONObject Jobject = new JSONObject(resp);
//                    String value = Jobject.getString("callName");
//
//                    String result = value.replaceAll("^\"|\"$", "");
//                    textView.setText(result);

                }



            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }
}