package com.example.mo15h.firebasechatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class UsersActivity extends AppCompatActivity {

    private static final String TAG = "UsersActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseRef.keepSynced(true);

        mToolbar = findViewById(R.id.users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mRecyclerView = findViewById(R.id.recyclerUsers);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            setupFirebaseRecyclerView();
            firebaseRecyclerAdapter.startListening();

        } else {
            Intent startIntent = new Intent(this, StartActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }

    private void setupFirebaseRecyclerView() {
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>().setQuery(mDatabaseRef, User.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull final User model) {
                Log.d(TAG, "onBindViewHolder: Triggered !!");
                final String userID = getRef(position).getKey();

                if (mAuth.getUid().equals(userID)) {
                    holder.mName.setText(model.getName() + " [You]");
                } else {
                    holder.mName.setText(model.getName());
                }
                holder.mStatus.setText(model.getStatus());
                Picasso.get()
                        .load(model.getThumbImage())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(holder.mImage);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: User id = " + userID);
                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("userID", userID);
                        startActivity(profileIntent);
                    }
                });


            }
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = getLayoutInflater().inflate(R.layout.users_item_layout, viewGroup, false);

                return new UsersViewHolder(view);
            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    static class UsersViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView mName, mStatus;
        private ImageView mImage;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;

            mName = mView.findViewById(R.id.txt_user_name);
            mStatus = mView.findViewById(R.id.txt_user_status);
            mImage = mView.findViewById(R.id.img_user_profile);

        }
    }
}
