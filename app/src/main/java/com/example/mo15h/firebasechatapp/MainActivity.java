package com.example.mo15h.firebasechatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private Toolbar mToolbar;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabsLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseRef.keepSynced(true);

        mToolbar = findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Awesome Chat");

        mTabsLayout = findViewById(R.id.main_tabs);
        mViewPager = findViewById(R.id.view_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabsLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            goToStart();
        } else {
            mDatabaseRef.child(mAuth.getUid()).child("online").setValue("Online");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.item_logout:
                mDatabaseRef.child(mAuth.getUid()).child("online").setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mAuth.signOut();
                        goToStart();
                    }
                });
                return true;

            case R.id.item_acc_settings:
                Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;
            case R.id.item_all_users:
                Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(usersIntent);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    private void goToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

}
