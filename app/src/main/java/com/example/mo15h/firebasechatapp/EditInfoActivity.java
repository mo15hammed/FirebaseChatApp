package com.example.mo15h.firebasechatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditInfoActivity extends AppCompatActivity {

    private static final String TAG = "EditInfoActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    private Toolbar mToolbar;
    private TextInputLayout mStatus, mName;
    private Button mBtnSaveChanges;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();

        mUserRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        mUserRef.keepSynced(true);

        mToolbar = findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = findViewById(R.id.edt_status);
        mStatus.getEditText().setText(getIntent().getStringExtra("status"));
        mName = findViewById(R.id.edt_namme);
        mName.getEditText().setText(getIntent().getStringExtra("name"));
        mBtnSaveChanges = findViewById(R.id.btn_save_changes);
        mProgressBar = findViewById(R.id.progressBar);

        mStatus.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                    mBtnSaveChanges.setEnabled(true);
            }
        });

        mName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(mName.getEditText().getText().toString())) {
                    mBtnSaveChanges.setEnabled(false);
                } else {
                    mBtnSaveChanges.setEnabled(true);
                }
            }
        });

        mBtnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressBar.setVisibility(View.VISIBLE);

                updateInfo();

            }
        });
    }

    private void updateInfo() {
        HashMap<String, Object> updatedInfo = new HashMap<>();
        updatedInfo.put("status", mStatus.getEditText().getText().toString());
        updatedInfo.put("name", mName.getEditText().getText().toString());

        mUserRef.updateChildren(updatedInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mProgressBar.setVisibility(View.GONE);
                    mBtnSaveChanges.setEnabled(false);
                    Toast.makeText(EditInfoActivity.this, "Changes have been saved !", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onComplete: Exception : " + task.getException().getMessage());
                    Toast.makeText(EditInfoActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                }
                mProgressBar.setVisibility(View.GONE);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            Intent startIntent = new Intent(this, StartActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startIntent);
        }
    }

}

