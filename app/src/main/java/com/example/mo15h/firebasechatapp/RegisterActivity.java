package com.example.mo15h.firebasechatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private TextInputLayout mName, mEmail, mPassword;
    private Button mBtnCreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.register_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = findViewById(R.id.progressBar);

        mName = findViewById(R.id.edt_reg_name);
        mEmail = findViewById(R.id.edt_reg_email);
        mPassword = findViewById(R.id.edt_reg_password);
        mBtnCreate = findViewById(R.id.btn_create_acc);


        mBtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    final User user = new User(name, email, password);

                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            user.setDeviceToken(instanceIdResult.getToken());
                            createAccount(user);
                        }
                    });

                } else {
                    Toast.makeText(RegisterActivity.this, "Fill missing fields !", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createAccount(final User user) {

        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String uid = currentUser.getUid();

                            Log.d(TAG, "onComplete: USER = " + user);

                            mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
                            mDatabaseRef.child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        mProgressBar.setVisibility(View.GONE);

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "mDatabaseRef.addOnCompleteListener:failure", task.getException());
                                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });

    }
}
