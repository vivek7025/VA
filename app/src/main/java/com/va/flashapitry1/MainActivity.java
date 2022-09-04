package com.va.flashapitry1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OVERLAY_PERMISSION = 1;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    EditText et;
    TextView tx;
    Button btn,btn2,btnOpen;
    private TextToSpeech mTTS;
    SpeechRecognizer speechRecognizer;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String[] appDetails = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et = findViewById(R.id.et);
        tx = findViewById(R.id.tx);
        btn = findViewById(R.id.btn);
        btn2 = findViewById(R.id.btn2);
        btnOpen = findViewById(R.id.btnOpen);


        if(!Settings.canDrawOverlays(this)){
            // ask for setting
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            Intent intent = new Intent(getApplicationContext(),getStarted.class);
            startActivity(intent);
        }



        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);



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

        OkHttpClient okHttpClient = new OkHttpClient();
//        Request request = new Request.Builder().url("http://192.168.0.10:5000/").build();
//        bot/How are you

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                speechRecognizer.startListening(speechRecognizerIntent);
//                String input = et.getText().toString();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this,FloatingWindow.class));
            }
        });

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), processBar.class);
                startActivity(intent);
            }
        });


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

                et.setText("Listening...");

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                //editText.setText(data.get(0));
                String input = data.get(0);
                et.setText(input);

                Toast.makeText(MainActivity.this, input, Toast.LENGTH_SHORT).show();
                Request request = new Request.Builder().url("http://192.168.0.10:5000/bot/"+input).build();

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        tx.setText("failed to connect");
//                        Toast.makeText(MainActivity.this, "Network not found", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String resp = response.body().string();
//                        String result = resp.substring(1, resp.length() - 1);
                        String result = resp.replaceAll("^\"|\"$", "");
//                        tx.setText(result);
                        mTTS.speak(result, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission ", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission ", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                // permission granted...
            } else {
                // permission not granted...
            }
        }
    }
}