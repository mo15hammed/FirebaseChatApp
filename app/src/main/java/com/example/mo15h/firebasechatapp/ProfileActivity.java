package com.example.mo15h.firebasechatapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";


    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef, mFriendRequestsRef, mNotificationsRef;
    private FirebaseUser mCurrentUser;
    private int requestStatus = 0;
    private User user;

    private String userID;

    private int totalFriends = 0;

    private ImageView mProfileImage;
    private TextView mUserName, mUserStatus, mTotalFriends;
    private Button mHandleRequest, mDeclineRequest, mSendMessage;

    private ValueEventListener userEventListener, friendRequestsEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mUsersRef = FirebaseDatabase.getInstance().getReference("Users");
        mFriendRequestsRef = FirebaseDatabase.getInstance().getReference("Friend_Requests");
        mNotificationsRef = FirebaseDatabase.getInstance().getReference("Notifications");
        mUsersRef.keepSynced(true);
        mFriendRequestsRef.keepSynced(true);
        mNotificationsRef.keepSynced(true);

        mCurrentUser = mAuth.getCurrentUser();

        mProfileImage = findViewById(R.id.img_user_profile);
        mUserName = findViewById(R.id.txt_user_name);
        mUserStatus = findViewById(R.id.txt_user_status);
        mTotalFriends = findViewById(R.id.txt_total_friends);
        mHandleRequest = findViewById(R.id.btn_handle_request);
        mDeclineRequest = findViewById(R.id.btn_decline_request);
        mSendMessage = findViewById(R.id.btn_send_message);

        userID = getIntent().getStringExtra("userID");

        readRequestTypeAndSetupButtons(userID);

        userEventListener = mUsersRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                user = dataSnapshot.getValue(User.class);
                Picasso.get()
                        .load(user.getImage())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(mProfileImage);

                mUserName.setText(user.getName());
                mUserStatus.setText(user.getStatus());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFriendRequestsRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Friend friend = ds.getValue(Friend.class);
                    if (friend.getRequest_type() == 3)
                        totalFriends++;

                }
                mTotalFriends.setText("Total Friends : " + totalFriends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mHandleRequest.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {

                Map<String, Object> map = new HashMap<>();
                switch (requestStatus) {

                    case 0: // No Friend Request (Send Friend Request)

                        map.clear();
                        map.put(mCurrentUser.getUid() + "/" + userID + "/request_type", 1); // request sent
                        map.put(userID + "/" + mCurrentUser.getUid() + "/request_type", 2); // request received
                        mFriendRequestsRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                HashMap<String, String> notificationMap = new HashMap<>();
                                notificationMap.put("from", mCurrentUser.getUid());
                                notificationMap.put("notification_type", "request");

                                mNotificationsRef.child(userID).push().setValue(notificationMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: ");

                                    }
                                });

                            }
                        });

                        break;
                    case 1: // Friend Request Sent (Cancel Friend Request)

                        map.clear();
                        map.put(mCurrentUser.getUid() + "/" + userID , null); // no friend request
                        map.put(userID + "/" + mCurrentUser.getUid() , null); // no friend request

                        mFriendRequestsRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: ");
                            }
                        });


                        break;
                    case 2: // Friend Request Received (Accept Friend Request)

                        map.clear();
                        map.put(mCurrentUser.getUid() + "/" + userID + "/request_type", 3); // friends
                        map.put(userID + "/" + mCurrentUser.getUid() + "/request_type", 3); // friends

                        String date = new SimpleDateFormat("dd MMM, yyyy h:mm:ss a", Locale.US).format(new Date());

                        map.put(mCurrentUser.getUid() + "/" + userID + "/friend_date", date); // friend date
                        map.put(userID + "/" + mCurrentUser.getUid() + "/friend_date", date); // friend date

                        mFriendRequestsRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: ");

                            }
                        });


                        break;
                    case 3: // Friends (Un-Friend)

                        map.clear();
                        map.put(mCurrentUser.getUid() + "/" + userID , null); // no friend request
                        map.put(userID + "/" + mCurrentUser.getUid() , null); // no friend request

                        mFriendRequestsRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: ");

                            }
                        });

                        break;
                }
            }
        });

        mDeclineRequest.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if (requestStatus == 2) { // Friend Request Received (Decline Friend Request)

                    Map<String, Object> map = new HashMap<>();
                    map.put(mCurrentUser.getUid() + "/" + userID , null); // no friend request
                    map.put(userID + "/" + mCurrentUser.getUid() , null); // no friend request

                    mFriendRequestsRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
//                            requestStatus = 0;
                            Log.d(TAG, "onSuccess: ");

                        }
                    });
                }
            }
        });

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(ProfileActivity.this, ChatActivity.class);
                user.setUid(userID);
                chatIntent.putExtra("user", user);
                startActivity(chatIntent);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent startIntent = new Intent(this, StartActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startIntent);
        }
    }


    private void readRequestTypeAndSetupButtons(String userID) {
        friendRequestsEventListener = mFriendRequestsRef.child(mCurrentUser.getUid()).child(userID).child("request_type").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                        requestStatus = dataSnapshot.getValue(Integer.class);
                } else
                    requestStatus = 0;
                Log.d(TAG, "onDataChange: TEST = " + requestStatus);
                switch (requestStatus) {
                    case 0: // No Friend Request (Send Friend Request)
                        mHandleRequest.setText("Send Friend Request");
                        mHandleRequest.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        mDeclineRequest.setVisibility(View.GONE);
                        mSendMessage.setVisibility(View.GONE);
                        break;
                    case 1: // Friend Request Sent (Cancel Friend Request)
                        mHandleRequest.setText("Cancel Friend Request");
                        mHandleRequest.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        mDeclineRequest.setVisibility(View.GONE);
                        mSendMessage.setVisibility(View.GONE);
                        break;
                    case 2: // Friend Request Received (Accept Friend Request)
                        mHandleRequest.setText("Accept Friend Request");
                        mHandleRequest.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        mDeclineRequest.setVisibility(View.VISIBLE);
                        mSendMessage.setVisibility(View.GONE);
                        break;
                    case 3: // Friends (Un-Friend)
                        mHandleRequest.setText("Un-Friend");
                        mHandleRequest.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        mDeclineRequest.setVisibility(View.GONE);
                        mSendMessage.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUsersRef.child(userID).removeEventListener(userEventListener);
        mFriendRequestsRef.child(mCurrentUser.getUid()).child(userID).child("request_type").removeEventListener(friendRequestsEventListener);
    }
}
