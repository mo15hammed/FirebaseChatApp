package com.example.mo15h.firebasechatapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private static final String TAG = "FriendsFragment";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mFriendsRef;

    private ValueEventListener FriendsEventListener;

    private FriendsRecyclerAdapter adapter;
    private RecyclerView mRecyclerFriends;
    private List<User> usersList;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_friends, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseRef.keepSynced(true);

        usersList = new ArrayList<>();


        mRecyclerFriends = mView.findViewById(R.id.recyclerFriends);
        mRecyclerFriends.setHasFixedSize(true);

        mRecyclerFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        setupRecyclerView();
        adapter = new FriendsRecyclerAdapter(getContext(), usersList);
        mRecyclerFriends.setAdapter(adapter);

        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFriendsRef.removeEventListener(FriendsEventListener);
    }

    private void setupRecyclerView() {

        mFriendsRef = FirebaseDatabase.getInstance().getReference("Friend_Requests").child(mAuth.getUid());
        mFriendsRef.keepSynced(true);

        FriendsEventListener = mFriendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    final Friend friend = ds.getValue(Friend.class);
                    Log.d(TAG, "onDataChange: Friend = " + friend);
                    Log.d(TAG, "onDataChange: Key = " + ds.getKey());

                    if (friend.getRequest_type() == 3) {
                        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot usersSnapShot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: USER KEY = " + usersSnapShot.getKey());

                                    if (usersSnapShot.getKey().equals(ds.getKey())) {
                                        User user = usersSnapShot.getValue(User.class);
                                        user.setUid(ds.getKey());
                                        user.setDate(friend.getFriend_date());
                                        user.setOnline(dataSnapshot.child(usersSnapShot.getKey()).child("online").getValue().toString());

                                        if (!usersList.contains(user))
                                            usersList.add(user);

                                        Log.d(TAG, "onDataChange: FRIENDS = " + usersList);
                                    }

                                }

                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }



}
