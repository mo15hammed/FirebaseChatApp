package com.example.mo15h.firebasechatapp;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";

    private FirebaseAuth mAuth;
    private DatabaseReference mConvRef;
    private DatabaseReference mMessagesRef;
    private DatabaseReference mUsersRef;

    private RecyclerView mChatsRecycler;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private String mCurrentUserId;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();

        mChatsRecycler = mView.findViewById(R.id.recyclerChats);
        mCurrentUserId = mAuth.getUid();

        mConvRef = FirebaseDatabase.getInstance().getReference("Chat").child(mCurrentUserId);
        mConvRef.keepSynced(true);
        mUsersRef = FirebaseDatabase.getInstance().getReference("Users");
        mUsersRef.keepSynced(true);
        mMessagesRef = FirebaseDatabase.getInstance().getReference("Messages").child(mCurrentUserId);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        mChatsRecycler.setHasFixedSize(true);
        mChatsRecycler.setLayoutManager(linearLayoutManager);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvRef.orderByChild("timestamp");


        setupFirebaseRecyclerView(conversationQuery);

        firebaseRecyclerAdapter.startListening();


    }

    @Override
    public void onStop() {
        super.onStop();

        firebaseRecyclerAdapter.stopListening();
    }

    private void setupFirebaseRecyclerView(Query query) {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Conv>().setQuery(query, Conv.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conv, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull final Conv model) {
                Log.d(TAG, "onBindViewHolder: Triggered !!");

                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessagesRef.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String data = dataSnapshot.child("message").getValue().toString();

                        boolean isFromMe = false;
                        if (mCurrentUserId.equals(dataSnapshot.child("from").getValue().toString()))
                            isFromMe = true;

                        holder.setmLastMessage(data, isFromMe, model.isSeen());

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

                mUsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);
                        user.setUid(list_user_id);
                        user.setOnline(dataSnapshot.child("online").getValue().toString());


                        final User chatUser = user;

                        String name = user.getName();
                        String imageUri = user.getThumbImage();
                        String online = user.getOnline();

                        holder.mName.setText(name);

                        Picasso.get()
                                .load(imageUri)
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .into(holder.mImage);

                        holder.setmOnline(online);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user", chatUser);
                                startActivity(chatIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = getLayoutInflater().inflate(R.layout.users_item_layout, viewGroup, false);

                return new ChatsViewHolder(view);
            }
        };
        mChatsRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    static class ChatsViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView mName, mLastMessage;
        private ImageView mImage, mOnline;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            mName = mView.findViewById(R.id.txt_user_name);
            mLastMessage = mView.findViewById(R.id.txt_user_status);
            mImage = mView.findViewById(R.id.img_user_profile);
            mOnline = mView.findViewById(R.id.img_online_status);

        }

        public void setmLastMessage(String data, boolean isFromMe, boolean seen) {

            if (isFromMe)
                data = "You: " + data;

            mLastMessage.setText(data);
            if (!seen) {
                mLastMessage.setTypeface(mLastMessage.getTypeface(), Typeface.BOLD);
            } else {
                mLastMessage.setTypeface(mLastMessage.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setmOnline(String online) {
            if (online != null) {
                if (online.equals("Online")) {
                    mOnline.setVisibility(View.VISIBLE);
                } else {
                    mOnline.setVisibility(View.GONE);
                }
            } else {
                mOnline.setVisibility(View.GONE);
            }
        }

    }
}
