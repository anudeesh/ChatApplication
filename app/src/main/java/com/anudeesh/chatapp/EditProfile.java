package com.anudeesh.chatapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class EditProfile extends AppCompatActivity {

    private ImageView img,back;
    EditText ufname,ulname;
    Spinner gender;
    Button save;
    User user;
    String gen;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    final static int IMAGE_KEY = 0x03;
    private Uri galleryImage = Uri.EMPTY;
    private UploadTask uploadTask;
    String dpURL;
    public static String provider="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        img = (ImageView) findViewById(R.id.imageViewEditImg);
        back = (ImageView) findViewById(R.id.imageViewEditBack);
        ufname = (EditText) findViewById(R.id.editTextEditFname);
        ulname = (EditText) findViewById(R.id.editTextEditLname);
        gender = (Spinner) findViewById(R.id.spinnerGender);
        save = (Button) findViewById(R.id.buttonSaveDetails);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://chatroom-3c209.appspot.com");

        user = (User) getIntent().getExtras().getSerializable("EDIT");
        dpURL = user.getDp();
        gen = user.getGender();

        // Inflate your custom layout
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.user_actionbar,
                null);

        // Set up your ActionBar
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);



        // You customization
        final int actionBarColor = getResources().getColor(R.color.action_bar);
        actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));

        /*final Button actionBarTitle = (Button) findViewById(R.id.action_bar_inbox);
        actionBarTitle.setText("Inbox");
        actionBarTitle.setTextColor(Color.LTGRAY);*/

        final Button actionBarSent = (Button) findViewById(R.id.action_bar_users);
        actionBarSent.setText("Users");
        actionBarSent.setTextColor(Color.LTGRAY);

        final Button actionBarEdit = (Button) findViewById(R.id.action_bar_profile);
        actionBarEdit.setText("Profile");
        actionBarEdit.setTextColor(Color.WHITE);

        final ImageView logoutIcon = (ImageView) findViewById(R.id.action_bar_logout);

        Picasso.with(this)
                .load(user.getDp())
                .into(img);
        ufname.setText(user.getFname());
        ulname.setText(user.getLname());

        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter.createFromResource(this, R.array.gender,
                android.R.layout.simple_spinner_item);
        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(staticAdapter);

        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gen = (String)parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imgIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imgIntent.setType("image/*");
                startActivityForResult(imgIntent,IMAGE_KEY);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.setFname(ufname.getText().toString());
                user.setLname(ulname.getText().toString());
                user.setGender(gen);
                user.setDp(dpURL);
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                mDatabase.child("users").child(uid).setValue(user);
                Intent intent = new Intent(EditProfile.this,UserActivity.class);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("fb", FirebaseAuth.getInstance().getCurrentUser().getProviders().toString());
                provider = FirebaseAuth.getInstance().getCurrentUser().getProviders().toString();
                FirebaseAuth.getInstance().signOut();
                if (provider.contains("google")) {
                    provider="google";
                } else if(provider.contains("facebook")) {
                    provider="facebook";
                }
                Intent intent = new Intent(EditProfile.this, MainActivity.class);
                intent.putExtra("PROVIDER",provider);
                startActivity(intent);
            }
        });

        actionBarSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditProfile.this,UserActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_KEY)
        {
            if(resultCode == RESULT_OK) {
                //String uid = getUid();
                //signUpButton.setEnabled(false);
                galleryImage = data.getData();
                Bitmap imgBitmap;
                try {
                    imgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),galleryImage);
                    img.setImageBitmap(imgBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //showProgressDialog();
                StorageReference newRef = storageRef.child("images/"+galleryImage.getLastPathSegment());
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("image/jpg")
                        .build();
                uploadTask = newRef.putFile(galleryImage,metadata);
                uploadTask.addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(EditProfile.this, "Upload Success", Toast.LENGTH_SHORT).show();
                        dpURL= String.valueOf(taskSnapshot.getDownloadUrl());
                        Log.d("upload",dpURL);
                    }
                });

            }
        }
    }
}
