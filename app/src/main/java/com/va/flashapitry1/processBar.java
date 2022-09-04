package com.va.flashapitry1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class processBar extends AppCompatActivity {

    private TextToSpeech mTTS;
    SpeechRecognizer speechRecognizer;

    EditText intent,input1,input2,output1,slot1;
    TextView textView;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_bar);

        intent = findViewById(R.id.intent);
        input1 = findViewById(R.id.input1);
        input2 = findViewById(R.id.input2);
        output1 = findViewById(R.id.output1);
        slot1 = findViewById(R.id.slot1);
        textView = findViewById(R.id.textView);
        btnSend = findViewById(R.id.btnSend);

        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    float speed;
                    speed = 1.0f;
                    mTTS.setLanguage(Locale.UK);
                    mTTS.setSpeechRate(speed);
                }
            }
        });

//        OkHttpClient okHttpClient = new OkHttpClient();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToServer();
            }
        });

    }

    private String formatDataAsJSON(){
        final JSONObject root = new JSONObject();
        try{
            root.put("intent",intent.getText().toString());
            root.put("input1",input1.getText().toString());
            root.put("input2",input2.getText().toString());
            root.put("output1",output1.getText().toString());
            root.put("slot1",slot1.getText().toString());
            return root.toString(1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendDataToServer() {
        String json = formatDataAsJSON();
        String url = "http://192.168.0.3:5000/getjs";
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
                    JSONObject Jobject = new JSONObject(resp);
                    String value = Jobject.getString("callName");

                    String result = value.replaceAll("^\"|\"$", "");
//                    textView.setText(result);
                    mTTS.speak(result, TextToSpeech.QUEUE_FLUSH, null);

                }



            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }
}