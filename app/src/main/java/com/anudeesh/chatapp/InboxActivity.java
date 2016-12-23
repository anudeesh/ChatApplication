package com.anudeesh.chatapp;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InboxActivity extends AppCompatActivity {

    ImageView userimg,attach,send;
    EditText msgbody;
    TextView rname;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    final static int IMAGE_KEY = 0x03;
    private Uri galleryImage = Uri.EMPTY;
    private UploadTask uploadTask;
    ArrayList<Message> userMsgs;
    private ListView listView;
    String msgImg;
    String uid, rid;
    String mkey;
    String del;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl("gs://chatroom-3c209.appspot.com");

        userimg = (ImageView) findViewById(R.id.imageViewReceiver);
        attach = (ImageView) findViewById(R.id.imageViewAttach);
        send = (ImageView) findViewById(R.id.imageViewSend);
        msgbody = (EditText) findViewById(R.id.editTextMsgBody);
        rname = (TextView) findViewById(R.id.textViewReceiverName);
        rid = getIntent().getStringExtra("currentuser");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listView = (ListView) findViewById(R.id.listViewMessages);


        if(!rid.equals("")) {
            mDatabase.child("users").child(rid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    rname.setText(dataSnapshot.child("fname").getValue().toString() + " " + dataSnapshot.child("lname").getValue().toString());
                    Picasso.with(getApplicationContext()).load(dataSnapshot.child("dp").getValue().toString()).into(userimg);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mDatabase.child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userMsgs = new ArrayList<Message>();
                for(DataSnapshot value : dataSnapshot.getChildren()) {
                    Message msg = value.getValue(Message.class);
                    String msgkey = uid+" "+rid;
                    if(msgkey.contains(msg.getReceiver()) && msgkey.contains(msg.getSender())) {
                        if(msg.getRead().equals("false")) {
                            msg.setRead("true");
                            mDatabase.child("messages").child(value.getKey()).child("read").setValue("true");
                        }
                        userMsgs.add(msg);
                    }
                }
                if(userMsgs.size()>0) {
                    populateMessages();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imgIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imgIntent.setType("image/*");
                startActivityForResult(imgIntent,IMAGE_KEY);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = msgbody.getText().toString();
                if(!text.isEmpty()) {
                    Date date = new Date();
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
                    String newDate = sd.format(date);
                    Message msg = new Message();
                    msg.setSender(uid);
                    msg.setReceiver(rid);
                    msg.setDate(newDate);
                    msg.setImgurl("");
                    msg.setMsgtext(text);
                    msg.setRead("false");
                    final String key = mDatabase.child("messages").push().getKey();
                    Map<String, Object> msgValues = msg.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/messages/" + key, msgValues);

                    mDatabase.updateChildren(childUpdates);
                    userMsgs.add(msg);
                    MessageListAdapter adapter = new MessageListAdapter(InboxActivity.this,R.layout.row_message_layout,userMsgs);
                    listView.setAdapter(adapter);
                    msgbody.setText("");
                }
            }
        });
    }

    public void populateMessages() {
        MessageListAdapter adapter = new MessageListAdapter(InboxActivity.this,R.layout.row_message_layout,userMsgs);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                mDatabase.child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot value : dataSnapshot.getChildren()) {
                            Message msg = value.getValue(Message.class);
                            if(msg.getDate().equals(userMsgs.get(i).getDate())) {
                                mDatabase.child("messages").child(value.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getApplicationContext(),"Text deleted", Toast.LENGTH_SHORT).show();
                                            userMsgs.remove(i);
                                        }else{
                                            Toast.makeText(getApplicationContext(),"Error deleting text", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                            }
                        }
                        MessageListAdapter adapter = new MessageListAdapter(InboxActivity.this,R.layout.row_message_layout,userMsgs);
                        listView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_KEY)
        {
            if(resultCode == RESULT_OK) {
                galleryImage = data.getData();
                StorageReference newRef = storageRef.child("images/"+galleryImage.getLastPathSegment());
                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("image/jpg")
                        .build();
                uploadTask = newRef.putFile(galleryImage,metadata);
                uploadTask.addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(InboxActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(InboxActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                        msgImg= String.valueOf(taskSnapshot.getDownloadUrl());
                        Log.d("upload",msgImg);

                        Date date = new Date();
                        SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
                        String newDate = sd.format(date);
                        Message msg = new Message();
                        msg.setSender(uid);
                        msg.setReceiver(rid);
                        msg.setDate(newDate);
                        msg.setImgurl(msgImg);
                        msg.setMsgtext("");
                        msg.setRead("false");

                        final String key = mDatabase.child("messages").push().getKey();
                        Map<String, Object> msgValues = msg.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/messages/" + key, msgValues);
                        mDatabase.updateChildren(childUpdates);

                        userMsgs.add(msg);
                        MessageListAdapter adapter = new MessageListAdapter(InboxActivity.this,R.layout.row_message_layout,userMsgs);
                        listView.setAdapter(adapter);

                        /*listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                                //Expense ex = userExpenses.get(i);
                                mDatabase.child("messages").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot value : dataSnapshot.getChildren()) {
                                            Message msg = value.getValue(Message.class);
                                            if(msg.getDate().equals(userMsgs.get(i).getDate())) {
                                                del = value.getKey();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                userMsgs.remove(i);
                                mDatabase.child("messages").child(del).removeValue();
                                MessageListAdapter adapter = new MessageListAdapter(InboxActivity.this,R.layout.row_message_layout,userMsgs);
                                listView.setAdapter(adapter);
                                return false;
                            }
                        });*/
                    }
                });

            }
        }
    }

    /*public void getUserMessages() {
        mDatabase.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userMsgs = new ArrayList<Message>();
                for(DataSnapshot value : dataSnapshot.getChildren()) {
                    Message msg = value.getValue(Message.class);
                    String msgkey = uid+" "+rid;
                    if(msgkey.contains(msg.getReceiver()) && msgkey.contains(msg.getSender())) {
                        userMsgs.add(msg);
                    }
                }
                if(userMsgs.size()>0) {
                    populateMessages();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/
}
