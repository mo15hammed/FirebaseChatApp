package com.example.mo15h.firebasechatapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private final int GALLERY_REQUEST = 111;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef, mUserRef, mChatRef, mMessagesRef;
    private StorageReference mImageStorageRef;

    private  User user;
    private String mCurrentUserId;

    private Toolbar mToolbar;
    private TextView mTitleView, mLastSeenView, mPlaceHolder;
    private RecyclerView mRecyclerMessages;
    private ImageView mProfileImage;
    private ImageButton mAddImage, mSendMessage;
    private EditText mEdtMessage;

    private MessagesRecyclerAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private final List<Message> messageList = new ArrayList<>();

    private boolean mLoading = true;
    public static int mPreviousTotal = 0;

    private final int PAGE_START = 1;
    private final int PAGE_SIZE = 15;
    private int currentPage = PAGE_START;
    private int currentPageLength = currentPage * PAGE_SIZE;


    private ValueEventListener userOnlineEventListener;
    private ChildEventListener newMessageChildEventListener, messagesEventListener;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference("Users");
        mChatRef = FirebaseDatabase.getInstance().getReference("Chat");
        mMessagesRef = FirebaseDatabase.getInstance().getReference("Messages");
        mUserRef.keepSynced(true);
        mChatRef.keepSynced(true);
        mMessagesRef.keepSynced(true);

        mImageStorageRef = FirebaseStorage.getInstance().getReference("message_images");

        mCurrentUserId = mAuth.getUid();

        user = (User) getIntent().getSerializableExtra("user");

        Log.d(TAG, "onCreate: USER = " + user);

        // Set up the toolbar
        mToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        // Set up the recycler view
        mRecyclerMessages = findViewById(R.id.recyclerMessages);
        mRecyclerMessages.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerMessages.setLayoutManager(mLinearLayoutManager);
        mAdapter = new MessagesRecyclerAdapter(this, messageList, user);
        mRecyclerMessages.setAdapter(mAdapter);


        // Set up other widgets
        mAddImage = findViewById(R.id.img_add);
        mSendMessage = findViewById(R.id.img_send);
        mEdtMessage = findViewById(R.id.edt_message);
        mPlaceHolder = findViewById(R.id.txt_place_holder);

        mTitleView = findViewById(R.id.txt_user_name);
        mTitleView.setText(user.getName());

        mProfileImage = findViewById(R.id.img_user_profile);
        Picasso.get()
                .load(user.getThumbImage())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(mProfileImage);

        mLastSeenView = findViewById(R.id.txt_last_seen);

        userOnlineEventListener = mUserRef.child(user.getUid()).child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.getValue().toString();

                if (online.equals("Online"))
                    mLastSeenView.setText(online);
                else {

                    Long lastOnlineInMillis = Long.parseLong(online);
                    String lastSeenInTimeAgo = GetTimeAgo.getTimeAgo(lastOnlineInMillis);

                    mLastSeenView.setText(lastSeenInTimeAgo);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final HashMap<String, Object> chatAddMap = new HashMap<>();
        chatAddMap.put("seen", true);

        newMessageChildEventListener = mMessagesRef.child(mCurrentUserId).child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mChatRef.child(mCurrentUserId).child(user.getUid()).updateChildren(chatAddMap);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadMessages();
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                mEdtMessage.setText("");

                Log.d(TAG, "onClick: SCROLL TO POSITION = " + (mRecyclerMessages.getAdapter().getItemCount() - 1));
                Log.d(TAG, "onClick: First Visible Item Position = " + mLinearLayoutManager.findFirstVisibleItemPosition());
                Log.d(TAG, "onClick: Last Visible Item Position = " + mLinearLayoutManager.findLastVisibleItemPosition());


            }
        });

        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT PIC"), GALLERY_REQUEST);
            }
        });


    }
    private void loadMessages() {

//        final Query pageMessageRef = mMessagesRef.child(mCurrentUserId).child(user.getUid());

        mMessagesRef.child(mCurrentUserId).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final long count = dataSnapshot.getChildrenCount();

                messagesEventListener = mMessagesRef.child(mCurrentUserId).child(user.getUid()).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Message msg = dataSnapshot.getValue(Message.class);

                        mPlaceHolder.setVisibility(View.GONE);
                        messageList.add(msg);
                        mAdapter.notifyDataSetChanged();

                        if (messageList.size() > count) {

                            if (!msg.getFrom().equals(mCurrentUserId) && mRecyclerMessages.getAdapter().getItemCount() - 1 - mLinearLayoutManager.findLastVisibleItemPosition() > 2) {

                                Toast.makeText(ChatActivity.this, "New Message", Toast.LENGTH_SHORT).show();
                            } else {
                                mRecyclerMessages.scrollToPosition(mRecyclerMessages.getAdapter().getItemCount()-1);
                            }

                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendMessage() {

        String message = mEdtMessage.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = mCurrentUserId + "/" + user.getUid();
            String chat_user_ref = user.getUid() + "/" + mCurrentUserId;

            DatabaseReference user_messaage_push = mMessagesRef.child(mCurrentUserId).child(user.getUid()).push();
            String push = user_messaage_push.getKey();

            HashMap<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("type", "text");
            messageMap.put("seen", false);
            messageMap.put("timestamp", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            HashMap<String, Object> chatMap = new HashMap<>();
            chatMap.put("timestamp", ServerValue.TIMESTAMP);
            chatMap.put("seen", false);

            HashMap<String, Object> userMessageMap = new HashMap<>();
            userMessageMap.put("Messages/" + current_user_ref + "/" + push, messageMap);
            userMessageMap.put("Messages/" + chat_user_ref + "/" + push, messageMap);
            userMessageMap.put("Chat/" + current_user_ref, chatMap);
            userMessageMap.put("Chat/" + chat_user_ref, chatMap);

            mRootRef.updateChildren(userMessageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: Successful");
                    } else {
                        Log.d(TAG, "onComplete: Exception : " + task.getException().getMessage());
                    }
                }
            });


        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            Intent startIntent = new Intent(ChatActivity.this, StartActivity.class);
            startActivity(startIntent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMessagesRef.child(mCurrentUserId).child(user.getUid()).removeEventListener(newMessageChildEventListener);
        mUserRef.child(user.getUid()).child("online").removeEventListener(userOnlineEventListener);
        mMessagesRef.child(mCurrentUserId).child(user.getUid()).removeEventListener(messagesEventListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            if (data != null) {
                Uri imageUri = data.getData();

                final String current_user_ref = mCurrentUserId + "/" + user.getUid();
                final String chat_user_ref = user.getUid() + "/" + mCurrentUserId;

                DatabaseReference user_messaage_push = mMessagesRef.child(mCurrentUserId).child(user.getUid()).push();
                final String push = user_messaage_push.getKey();

                final StorageReference imagePath = mImageStorageRef.child(push + ".jpg");
                imagePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_uri = uri.toString();

                                HashMap<String, Object> messageMap = new HashMap<>();
                                messageMap.put("message", download_uri);
                                messageMap.put("type", "image");
                                messageMap.put("seen", false);
                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                messageMap.put("from", mCurrentUserId);

                                HashMap<String, Object> userMessageMap = new HashMap<>();
                                userMessageMap.put(current_user_ref + "/" + push, messageMap);
                                userMessageMap.put(chat_user_ref + "/" + push, messageMap);

                                mMessagesRef.updateChildren(userMessageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(ChatActivity.this, "Image Sent", Toast.LENGTH_SHORT).show();
                                        
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ChatActivity.this, "Upload Failed !", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "onFailure: Exception = " + e.getMessage());
                                    }
                                });

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Upload Failed !", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: Exception : " + e.getMessage());
                    }
                });



            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
}
