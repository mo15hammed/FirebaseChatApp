package com.example.mo15h.firebasechatapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.FriendsViewHolder> {

    private static final String TAG = "FriendsRecyclerAdapter";
    private List<User> friendsList;
    private Context mContext;

    private DatabaseReference mUsersRef;

    public FriendsRecyclerAdapter(Context context, List<User> friendsList) {
        this.mContext = context;
        this.friendsList = friendsList;
        mUsersRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.users_item_layout, viewGroup, false);
        return new FriendsViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position) {

        final User user = friendsList.get(position);

        holder.mName.setText(user.getName());
        holder.mDate.setText(user.getDate());

        Picasso.get()
                .load(user.getThumbImage())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(holder.mImage);

        mUsersRef.child(user.getUid()).child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.d(TAG, "onDataChange: DataSnapshot = " + dataSnapshot);
                    Log.d(TAG, "onDataChange: DataSnapshot Value = " + dataSnapshot.getValue());
                    String online = dataSnapshot.getValue().toString();
                    if (online.equals("Online")) {
                        holder.mOnline.setVisibility(View.VISIBLE);
                    } else {
                        holder.mOnline.setVisibility(View.GONE);
                    }
                } else {
                    holder.mOnline.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.putExtra("userID", friendsList.get(holder.getAdapterPosition()).getUid());
                mContext.startActivity(profileIntent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView mImage, mOnline;
        private TextView mName, mDate;

        FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            mImage = mView.findViewById(R.id.img_user_profile);
            mOnline = mView.findViewById(R.id.img_online_status);
            mName = mView.findViewById(R.id.txt_user_name);
            mDate = mView.findViewById(R.id.txt_user_status);

        }
    }
}
