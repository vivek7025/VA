package com.va.flashapitry1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.va.flashapitry1.cloudConnect;


public class FloatingWindow extends Service {

    private WindowManager wm;
    private LinearLayout ll;
    String insideLoop = "no";
    JSONObject JobjectGobal ;
    private Button srt,close;
    cloudConnect clConnect = new cloudConnect();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String[] appDetails = new String[2];
    ArrayList nameList;
    String ip;

    private TextToSpeech mTTS;
    SpeechRecognizer speechRecognizer;
    private ViewGroup floatView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("IP");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String val = String.valueOf(snapshot.getValue());
                ip =val;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);

        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        floatView = (ViewGroup) inflater.inflate(R.layout.floatwindow, null);
        srt = floatView.findViewById(R.id.btnStr);
        close = floatView.findViewById(R.id.btnClose);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(400,570,LAYOUT_FLAG , WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        parameters.x = 0;
        parameters.y = 0;
        parameters.gravity = Gravity.CENTER | Gravity.CENTER;
//
//        ll.addView(srt);
//        ll.addView(close);
        wm.addView(floatView, parameters);

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

        floatView.setOnTouchListener(new View.OnTouchListener() {
            private WindowManager.LayoutParams updatedParameters = parameters;
            int x,y;
            float touchedX, touchedY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        touchedX = event.getRawX();
                        touchedY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        updatedParameters.x = (int) (x + (event.getRawX() - touchedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - touchedY));

                        wm.updateViewLayout(floatView, updatedParameters);

                    default:
                        break;
                }

                return false;
            }
        });

        srt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.startListening(speechRecognizerIntent);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(floatView);
                stopSelf();
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                Toast.makeText(FloatingWindow.this, "Listening...", Toast.LENGTH_SHORT).show();

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
                String json = "";
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                //editText.setText(data.get(0));
                String input = data.get(0);

                Toast.makeText(FloatingWindow.this, input, Toast.LENGTH_SHORT).show();
//                Request request = new Request.Builder().url("http://192.168.0.10:5000/bot/"+input).build();
//                String url = "http://192.168.0.5:5000/getInput";
//                String url = "http://192.168.1.47:5000/getInput";
                String url = "http://172.20.10.2:5000/getInput";

                if(insideLoop.equalsIgnoreCase("no")){
                    json = formatDataAsJSON(input);
                }else if(insideLoop.equalsIgnoreCase("yes")){
                    Toast.makeText(FloatingWindow.this, "inside running", Toast.LENGTH_SHORT).show();
                    insideLoop = "no";
                    try {
                        String contentNumber = JobjectGobal.getString("s_askForSlot");
                        json =formatDataAsJSONSpecific(JobjectGobal,input,contentNumber);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                new MyHttpRequestTask().execute(url,json);

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

    }

    private class MyHttpRequestTask extends AsyncTask<String,Integer,String> {
        String value = "";
        String s_ask = "";

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
                    String s_type = Jobject.getString("s_type");
                    if(s_type.equalsIgnoreCase("nomralConvo")){
                        value = Jobject.getString("s_output");
                        String result = value.replaceAll("^\"|\"$", "");
                        mTTS.speak(result, TextToSpeech.QUEUE_FLUSH, null);
                    }
                    else if(s_type.equalsIgnoreCase("specificConvo")){
                        s_ask = Jobject.getString("s_ask");
                        if(s_ask.equalsIgnoreCase("no")){
                            String s_action = Jobject.getString("s_action");
                            if(s_action.equalsIgnoreCase("call")){
                                callPhone(Jobject);
                            }
                            else if(s_action.equalsIgnoreCase("remind")){
                                setReminder(Jobject);
//                                String s_output = Jobject.getString("s_output");
//                                mTTS.speak(s_output, TextToSpeech.QUEUE_FLUSH, null);
                            }
                            else if(s_action.equalsIgnoreCase("callrecent")){
                                getRecentCall();
                            }
                            else if(s_action.equalsIgnoreCase("message")){
                                sendMessage(Jobject);
                            }
                            else if(s_action.equalsIgnoreCase("askmessagerecent")){
                                getRecentMessage(Jobject);
                            }
                            else if(s_action.equalsIgnoreCase("readmessageperson")){
                                getRecentMessagePerson(Jobject);
                            }
                            else if(s_action.equalsIgnoreCase("readmessagecheck")){
                                getRecentMessageCheck(Jobject);
                            }
                            else if(s_action.equalsIgnoreCase("readmessagecheckperson")){
                                getRecentMessageCheckPerson(Jobject);
                            }



                        }
                        else if(s_ask.equalsIgnoreCase("qa")){

                        }
                        else{
                            s_ask = Jobject.getString("s_ask");
                            insideLoop = "yes";
                            JobjectGobal = Jobject;
                            value = s_ask;
                            String result = value.replaceAll("^\"|\"$", "");
                            mTTS.speak(result, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }

                }



            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    private void setReminder(JSONObject jobject) {
        try {
            String content = jobject.getString("s_slot1");
            String time = jobject.getString("s_slot2");

            String[] appDetails = clConnect.getConfigFile();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.putExtra("action","insert");
            intent.putExtra("content",content);
            intent.putExtra("time",time);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName(
                    appDetails[0],appDetails[1]));
            startActivity(intent);

            String s_output = jobject.getString("s_output");
            mTTS.speak(s_output, TextToSpeech.QUEUE_FLUSH, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRecentMessageCheckPerson(JSONObject jobject) {
        try {
            String nameToCheck = jobject.getString("s_slot1");
            String content = "not getting", address, person, date, adr, rd;
            int counter = 0;
            String outpt = "";
            String spk;
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    int index_Address = cursor.getColumnIndex("address");
                    int index_Person = cursor.getColumnIndex("person");
                    int index_Body = cursor.getColumnIndex("body");
                    int index_Date = cursor.getColumnIndex("date");
                    int index_Type = cursor.getColumnIndex("type");
                    int index_Read = cursor.getColumnIndex("read");
                    adr = cursor.getString(index_Address);
                    String nm = getNameFromNumber(adr);
                    if (nm.equalsIgnoreCase(nameToCheck)) {
                        address = cursor.getString(index_Address);
                        content = cursor.getString(index_Body);
                        person = cursor.getString(index_Person);
                        date = cursor.getString(index_Date);
                        rd = cursor.getString(index_Read);
                        if (rd.equalsIgnoreCase("0")) {
                            outpt = "y";
                        }
                        break;
                    }
                } while (cursor.moveToNext());

            }

            if (outpt.equalsIgnoreCase("y")) {
                outpt = "";
                spk = "You have a unread message from  " + nameToCheck;
            } else {
                spk = "You don't have any unread message from  " + nameToCheck;
            }
            mTTS.speak(spk, TextToSpeech.QUEUE_FLUSH, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getRecentMessageCheck(JSONObject jobject) {
        String result;
        String content,address,person,date,rdVl;
        int counter = 0;
        Cursor cursor=getContentResolver().query(Uri.parse("content://sms"),null,null,null,null);
        if (cursor.moveToFirst()) {
            int index_Read = cursor.getColumnIndex("read");
            do {
                rdVl = cursor.getString(index_Read);
                if(rdVl.equalsIgnoreCase("0")){
                    counter++;
                }
            } while (cursor.moveToNext());
            if (!cursor.isClosed()) {
                cursor.close();
                cursor = null;
            }
        } else {
            result = "no result!";
        } // end if
        String cnt = String.valueOf(counter);
        String spk = "You have "+cnt +"  unread messages";
        mTTS.speak(spk, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void getRecentMessagePerson(JSONObject jobject) {
        try {
            String nameToCheck = jobject.getString("s_slot1");
            String content = "not getting", address, person, date, adr;
            int counter = 0;
            Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    int index_Address = cursor.getColumnIndex("address");
                    int index_Person = cursor.getColumnIndex("person");
                    int index_Body = cursor.getColumnIndex("body");
                    int index_Date = cursor.getColumnIndex("date");
                    int index_Type = cursor.getColumnIndex("type");
                    int index_Read = cursor.getColumnIndex("read");
                    adr = cursor.getString(index_Address);
                    String nm = getNameFromNumber(adr);
//                Toast.makeText(this, nm, Toast.LENGTH_SHORT).show();
//                Toast.makeText(this, numberToCheck, Toast.LENGTH_SHORT).show();
                    if (nm.equalsIgnoreCase(nameToCheck)) {
                        address = cursor.getString(index_Address);
                        content = cursor.getString(index_Body);
                        person = cursor.getString(index_Person);
                        date = cursor.getString(index_Date);
//                    Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
                        break;
                    }
                } while (cursor.moveToNext());

            }

            String spk = "Your have recent messages from  " + nameToCheck + "      "+nameToCheck +" says" + content;
            mTTS.speak(spk, TextToSpeech.QUEUE_FLUSH, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRecentMessage(JSONObject jobject) {
        //for reading last sms
        try{
        Cursor cursor=getContentResolver().query(Uri.parse("content://sms"),null,null,null,null);
        cursor.moveToFirst();
        int index_Address = cursor.getColumnIndex("address");
//        int index_Person = cursor.getColumnIndex("person");
        int index_Body = cursor.getColumnIndex("body");
        int index_Date = cursor.getColumnIndex("date");
        int index_Type = cursor.getColumnIndex("type");
        int index_Read = cursor.getColumnIndex("read");
        String address = cursor.getString(index_Address);
        String content = cursor.getString(index_Body);

        String rdmName = getNameFromNumber(address);

        String sphv = "Your last message was from " +rdmName +"        " +rdmName +" says " +content;
            mTTS.speak(sphv, TextToSpeech.QUEUE_FLUSH, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(JSONObject jobject) {
        String personName = "";
        String content = "";
        try {
            personName = jobject.getString("s_slot1");
            content = jobject.getString("s_slot2");
//            mTTS.speak(content, TextToSpeech.QUEUE_FLUSH, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE},
                    "DISPLAY_NAME= '" + personName + "'", null, null);
            cursor.moveToFirst();
            String NumberContact = cursor.getString(0);

            String sendTo = NumberContact.trim();
            //sending msg to the contact
            try{
                SmsManager smmanage = SmsManager.getDefault();
                smmanage.sendTextMessage(sendTo,null,content,null,null);
                String spk = "Your message to " +personName +" saying " +content +" is sent";
                mTTS.speak(spk, TextToSpeech.QUEUE_FLUSH, null);
            }
            catch (Exception e){
                String spk = "Your message to " +personName +" is failed to sent ";
                mTTS.speak(spk, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        catch(Exception en)
        {
            en.printStackTrace();
        }


    }

    private void getRecentCall() {
        try {
            Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null, null);
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            managedCursor.moveToFirst();

            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);

            Date callDayTime = new Date(Long.valueOf(callDate));
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm");
            String dateString = formatter.format(callDayTime);
            String callDuration = managedCursor.getString(duration);

            String dir = null;

            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            String contactName = getNameFromNumber(phNumber);
            if(dir.equalsIgnoreCase("INCOMING")){
                String spk = "Your most recent call was an " +dir +" call from " +contactName;
                mTTS.speak(spk, TextToSpeech.QUEUE_FLUSH, null);
            }else if(dir.equalsIgnoreCase("OUTGOING")){
                String spk = "Your most recent call was an " +dir +" call to " +contactName;
                mTTS.speak(spk, TextToSpeech.QUEUE_FLUSH, null);
            }else if(dir.equalsIgnoreCase("MISSED")){
                String spk = "You got a " +dir +" call from " +contactName;
                mTTS.speak(spk, TextToSpeech.QUEUE_FLUSH, null);
            }


            managedCursor.close();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void callPhone(JSONObject jobject) {
        String s_slot1 = "";
        try {
            s_slot1 = jobject.getString("s_slot1");
            String resp = checkNumber(s_slot1);
            if(resp.equalsIgnoreCase("got")){
                String num = getNumber(s_slot1);
                Intent callContactIntent = new Intent(Intent.ACTION_CALL);
                callContactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callContactIntent.setData(Uri.parse("tel:"+num));

                //sending msg to the contact

                startActivity(callContactIntent);
            }else{
                mTTS.speak("Sorry couldn't find the contact ", TextToSpeech.QUEUE_FLUSH, null);
            }


//            mTTS.speak(num, TextToSpeech.QUEUE_FLUSH, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String checkNumber(String name) {
        String rtv = "nul";
        nameList = getAllContacts();
        if (Arrays.asList(nameList).contains(name)) {
            // true
            rtv = "got";
        }else{
            rtv = "not";
        }
        return rtv;
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

    private String getNumber(String name){
        String sendTo = "nul";
        //for getting respective number of contact name
        try {
            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE},
                    "DISPLAY_NAME= '" + name + "'", null, null);
            cursor.moveToFirst();
            String NumberContact = cursor.getString(0);

            sendTo = NumberContact.trim();
            return sendTo;
            //sending msg to the contact
        }
        catch(Exception en)
        {
            en.printStackTrace();
        }
        return sendTo;
    }

    private String formatDataAsJSON(String input){
        final JSONObject root = new JSONObject();
        try{
            root.put("s_content",input);
            root.put("s_status","new");
            return root.toString(1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String formatDataAsJSONSpecific(JSONObject js,String content_value,String contentNumber){
        final JSONObject root = new JSONObject();
        String content2 = "";
        String content3 = "";
        try{
        if(contentNumber.equalsIgnoreCase("s_content2")){
            root.put("s_content2",content_value);
            root.put("s_content3","");
        }else if(contentNumber.equalsIgnoreCase("s_content3")){
            if((js.optString("s_content2")).equalsIgnoreCase("")){
                root.put("s_content2",js.optString("s_content2"));
                root.put("s_content3",content_value);
            }else{
                root.put("s_content2",js.getString("s_content2"));
                root.put("s_content3",content_value);
            }
        }
            root.put("s_type",js.getString("s_type"));
            root.put("s_content",js.getString("s_content"));
            root.put("s_intent",js.getString("s_intent"));
            root.put("s_slotNumber",js.getString("s_slotNumber"));
            root.put("s_slot1",js.getString("s_slot1"));
            root.put("s_slot2",js.getString("s_slot2"));
            root.put("s_slot3",js.getString("s_slot3"));
            root.put("s_slot4",js.getString("s_slot4"));
            root.put("s_ask",js.getString("s_ask"));
            root.put("s_askForSlot",js.getString("s_askForSlot"));
            root.put("s_action",js.getString("s_action"));
            root.put("s_output",js.getString("s_output"));
            root.put("s_status","running");
            return root.toString(1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getNameFromNumber(String phNumber) {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor= getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }
}


