package com.example.mo15h.firebasechatapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    private ImageView mProfileImage;
    private TextView mName, mStatus;
    private Button mBtnImage, mBtnInfo;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("profile_images");

        mProfileImage = findViewById(R.id.img_profile);
        mName = findViewById(R.id.txt_name);
        mStatus = findViewById(R.id.txt_status);
        mBtnImage = findViewById(R.id.btn_image);
        mBtnInfo = findViewById(R.id.btn_info);
        mProgressBar = findViewById(R.id.progressBar);

        readUserData();

        mBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });

        mBtnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent infoIntent = new Intent(SettingsActivity.this, EditInfoActivity.class);
                infoIntent.putExtra("status", mStatus.getText().toString());
                infoIntent.putExtra("name", mName.getText().toString());
                startActivity(infoIntent);
            }
        });

    }

    private void readUserData() {

        String uid = mAuth.getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseRef.keepSynced(true);
        mDatabaseRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                final User currentUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, "currentUser = " + currentUser);

                mName.setText(currentUser.getName());
                mStatus.setText(currentUser.getStatus());

                Picasso.get()
                        .load(currentUser.getThumbImage())
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(mProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get()
                                        .load(currentUser.getThumbImage())
                                        .placeholder(R.drawable.default_avatar)
                                        .error(R.drawable.default_avatar)
                                        .into(mProfileImage);
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            mProgressBar.setVisibility(View.VISIBLE);
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri imageUri = result.getUri();

            final String uid = mAuth.getUid();

            File imageFile = new File(imageUri.getPath());
            Bitmap thumbImageBitmap = null;
            try {
                thumbImageBitmap = new Compressor(this)
                        .setMaxWidth(180)
                        .setMaxHeight(180)
                        .setQuality(75)
                        .compressToBitmap(imageFile);
            } catch (IOException e) {
                Log.d(TAG, "onActivityResult: IOException : " + e.getMessage());
                e.printStackTrace();
            }


            final StorageReference imageRef = mStorageRef.child(uid + ".jpg");
            final StorageReference thumbImageRef = mStorageRef.child("thumb_images").child(uid + ".jpg");


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumbImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] thumbImageBytes = baos.toByteArray();


            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content

                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri imageDownloadUrl) {
                                    thumbImageRef.putBytes(thumbImageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            thumbImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri thumbDownloadUrl) {

                                                    mDatabaseRef.child(uid).child("thumbImage").setValue(thumbDownloadUrl.toString());

                                                    mDatabaseRef.child(uid).child("image").setValue(imageDownloadUrl.toString());

                                                    Toast.makeText(SettingsActivity.this, "Image Uploaded !", Toast.LENGTH_SHORT).show();
                                                    mProgressBar.setVisibility(View.GONE);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Handle unsuccessful uploads
                                                    Log.d(TAG, "onFailure: Exception : " + exception.getMessage());
                                                    Toast.makeText(SettingsActivity.this, "Upload Failed !", Toast.LENGTH_SHORT).show();
                                                    mProgressBar.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                            Log.d(TAG, "onFailure: Exception : " + exception.getMessage());
                                            Toast.makeText(SettingsActivity.this, "Upload Failed !", Toast.LENGTH_SHORT).show();
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    Log.d(TAG, "onFailure: Exception : " + exception.getMessage());
                                    Toast.makeText(SettingsActivity.this, "Upload Failed !", Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Log.d(TAG, "onFailure: Exception : " + exception.getMessage());
                            Toast.makeText(SettingsActivity.this, "Upload Failed !", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });

        }

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
