package com.anudeesh.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText emailField, pwdField, cpwdField, fnameField, lnameField;
    private Button signUpButton, cancelButton;
    private Spinner mGender;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ProgressDialog mProgressDialog;
    private ImageView imgUpload;
    final static int IMAGE_KEY = 0x03;
    private Uri galleryImage = Uri.EMPTY;
    private UploadTask uploadTask;
    //String email,pwd,cpwd,fname,lname;
    String gender;
    String dpURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailField = (EditText) findViewById(R.id.editTextEmailValSignup);
        pwdField = (EditText) findViewById(R.id.editTextPwdValSignup);
        cpwdField = (EditText) findViewById(R.id.editTextRepeatPwdValSignup);
        fnameField = (EditText) findViewById(R.id.editTextFNameValSignup);
        lnameField = (EditText) findViewById(R.id.editTextLNameValSignup);
        signUpButton = (Button) findViewById(R.id.buttonSignup);
        cancelButton = (Button) findViewById(R.id.buttonCancelSignup);
        mGender = (Spinner) findViewById(R.id.spinner);
        imgUpload = (ImageView) findViewById(R.id.imageViewUpload);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://chatroom-3c209.appspot.com");


        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter.createFromResource(this, R.array.gender,
                android.R.layout.simple_spinner_item);
        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGender.setAdapter(staticAdapter);

        mGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = (String)parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString();
                String pwd = pwdField.getText().toString();
                String cpwd = cpwdField.getText().toString();
                String fname = fnameField.getText().toString();
                String lname = lnameField.getText().toString();
                if(email.isEmpty() || pwd.isEmpty() || cpwd.isEmpty() || fname.isEmpty() || lname.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Enter all the fields",
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (pwd.equals(cpwd)) {
                        User user = new User(fname,lname,gender,dpURL,email);
                        createUserAccount(user,pwd);
                       // mDatabase.child("users").get

                    } else {
                        Toast.makeText(SignUpActivity.this, "Passwords mismatch", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imgUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imgIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imgIntent.setType("image/*");
                startActivityForResult(imgIntent,IMAGE_KEY);
            }
        });
    }

    public void createUserAccount(final User user, String password) {
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("demo", "createUserWithEmail:onComplete:" + task.isSuccessful());


                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Account not created. Choose a different email",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            String uid = getUid();
                            String key = mDatabase.child("users").push().getKey();
                            if(dpURL==null){
                                dpURL="";
                            }
                            User u = new User(user.getFname(),user.getLname(),user.getGender(),dpURL,user.getEmail());
                            //user.setDp(dpURL);
                            Map<String, Object> userValues = u.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/users/" + uid, userValues);

                            mDatabase.updateChildren(childUpdates);
                            Toast.makeText(SignUpActivity.this, "User has been created", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(AddExpensesActivity.this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                            //finish();
                        }
                        hideProgressDialog();
                        Intent intn = new Intent(SignUpActivity.this,MainActivity.class);
                        startActivity(intn);
                    }
                });
    }

    public void uploadImage() {
        StorageReference newRef = storageRef.child("images/"+galleryImage.getLastPathSegment());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        uploadTask = newRef.putFile(galleryImage,metadata);
        uploadTask.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SignUpActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                dpURL= String.valueOf(taskSnapshot.getDownloadUrl());
                Log.d("upload",dpURL);
            }
        });
        //return dpURL;
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_KEY)
        {
            if(resultCode == RESULT_OK) {
                //String uid = getUid();
                signUpButton.setEnabled(false);
                galleryImage = data.getData();
                Bitmap imgBitmap;
                try {
                    imgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),galleryImage);
                    imgUpload.setImageBitmap(imgBitmap);
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
                        Toast.makeText(SignUpActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SignUpActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                        dpURL= String.valueOf(taskSnapshot.getDownloadUrl());
                        Log.d("upload",dpURL);
                        signUpButton.setEnabled(true);
                    }
                });

            }
        }
    }
}
