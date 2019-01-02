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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private TextInputLayout mEmail, mPassword;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        mToolbar = findViewById(R.id.login_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = findViewById(R.id.progressBar);

        mEmail = findViewById(R.id.edt_reg_email);
        mPassword = findViewById(R.id.edt_reg_password);
        mLogin = findViewById(R.id.btn_login);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    User user = new User();
                    user.setEmail(email);
                    user.setPassword(password);
                    loginAccount(user);

                } else {
                    Toast.makeText(LoginActivity.this, "Fill missing fields !", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void loginAccount(User user) {
        mAuth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");


                            final FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String deviceToken = instanceIdResult.getToken();

                                    mDatabaseRef.child(user.getUid()).child("deviceToken").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgressBar.setVisibility(View.GONE);
                                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    });

                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);

                        }

                        // ...
                    }
                });
    }
}
