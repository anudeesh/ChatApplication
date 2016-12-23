package com.anudeesh.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class UserDetailsActivity extends AppCompatActivity {

    private ImageView img,send,back;
    TextView ufname,ulname,ugender;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        img = (ImageView) findViewById(R.id.imageViewUserDetImg);
        send = (ImageView) findViewById(R.id.imageViewSendMsg);
        back = (ImageView) findViewById(R.id.imageViewBack);
        ufname = (TextView) findViewById(R.id.textViewUserFNameVal);
        ulname = (TextView) findViewById(R.id.textViewUserLNameVal);
        ugender = (TextView) findViewById(R.id.textViewUserGenderVal);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        User user = (User) getIntent().getExtras().getSerializable("USER");
        final String current_uid = getIntent().getStringExtra("USERID");
        //String uname = mDatabase.child("users").child(current_uid).child("fname")

        Picasso.with(this)
                .load(user.getDp())
                .into(img);
        ufname.setText(user.getFname());
        ulname.setText(user.getLname());
        if(user.getGender().equals("")) {
            ugender.setText("Not specified");
        } else {
            ugender.setText(user.getGender());
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserDetailsActivity.this,InboxActivity.class);
                intent.putExtra("currentuser",current_uid);
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
