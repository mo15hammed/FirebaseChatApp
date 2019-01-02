package com.example.mo15h.firebasechatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button mBtnRegister, mBtnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mBtnRegister = findViewById(R.id.btn_register);
        mBtnLogin = findViewById(R.id.btn_login);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(regIntent);
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent (StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

    }
}
