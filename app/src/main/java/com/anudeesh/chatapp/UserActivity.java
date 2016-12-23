package com.anudeesh.chatapp;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    ArrayList<User> registeredUsers;
    ArrayList<String> userIDs;
    ArrayList<User> otherUsers;
    ArrayList<String> otherIDs;
    private DatabaseReference mDatabase;
    public static String provider="";
    /*RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;*/
    String userid="";
    ListView myView;
    User currentUser;
    ArrayList<String> userMsgs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        myView = (ListView) findViewById(R.id.listViewUsers);

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
        actionBarTitle.setText("Inbox");*/

        final Button actionBarSent = (Button) findViewById(R.id.action_bar_users);
        actionBarSent.setText("Users");

        final Button actionBarEdit = (Button) findViewById(R.id.action_bar_profile);
        actionBarEdit.setText("Profile");

        final ImageView logoutIcon = (ImageView) findViewById(R.id.action_bar_logout);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        userIDs = new ArrayList<String>();
        otherIDs = new ArrayList<String>();
        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabase.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userMsgs = new ArrayList<String>();
                for(DataSnapshot value : dataSnapshot.getChildren()) {
                    Message msg = value.getValue(Message.class);
                    if(userid.equals(msg.getReceiver()) && msg.getRead().equals("false")) {
                        userMsgs.add(msg.getSender());
                    }
                }

                mDatabase.child("users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        registeredUsers = new ArrayList<User>();
                        otherUsers = new ArrayList<User>();
                        Log.e("Count " ,""+dataSnapshot.getChildrenCount());
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            User user = postSnapshot.getValue(User.class);
                            String uid = postSnapshot.getKey();

                            registeredUsers.add(user);
                            userIDs.add(uid);

                            if(!userid.equals(uid)) {
                                otherUsers.add(user);
                                otherIDs.add(uid);
                            } else {
                                currentUser = user;
                            }
                        }

                        myView.setVisibility(View.VISIBLE);
                        UserListAdapter adapter = new UserListAdapter(UserActivity.this,R.layout.row_user_layout,otherUsers,userMsgs,otherIDs);
                        myView.setAdapter(adapter);

                        myView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent intent = new Intent(UserActivity.this,UserDetailsActivity.class);
                                intent.putExtra("USER",otherUsers.get(i));
                                intent.putExtra("USERID",otherIDs.get(i));
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                Intent intent = new Intent(UserActivity.this,MainActivity.class);
                intent.putExtra("PROVIDER",provider);
                startActivity(intent);
            }
        });

        actionBarEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserActivity.this,EditProfile.class);
                intent.putExtra("EDIT",currentUser);
                startActivity(intent);
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.logout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FirebaseAuth.getInstance().signOut();
        Intent intn = new Intent(UserActivity.this,MainActivity.class);
        startActivity(intn);
        return super.onOptionsItemSelected(item);
    }*/
}
