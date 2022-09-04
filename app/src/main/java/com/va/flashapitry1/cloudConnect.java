package com.va.flashapitry1;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class cloudConnect {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String appClass,appPackage;
    String[] appDetails = new String[2];


public String[] getConfigFile(){
//A_TO_DO
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("allowedApps").child("A_TO_DO").child("classpackage");
    reference.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String val = String.valueOf(snapshot.getValue());
            appDetails[0] =val;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference().child("allowedApps").child("A_TO_DO").child("mainclass");
    reference2.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String val = String.valueOf(snapshot.getValue());
            appDetails[1] =val;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return appDetails;
}
}
