package com.example.mo15h.firebasechatapp;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class FirebaseChatApp extends Application {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /* Picasso */
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getUid() != null) {
            mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid());

            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (mAuth != null)
                        mDatabaseRef.child("online").setValue("Online");
                    mDatabaseRef.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }



}
