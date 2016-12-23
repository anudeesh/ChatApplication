package com.anudeesh.chatapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Anudeesh on 11/22/2016.
 */
public class MessageListAdapter extends ArrayAdapter<Message> {
    List<Message> mData;
    Context mContext;
    int mResource;
    private DatabaseReference mDatabase;
    ArrayList<Message> mList;


    public MessageListAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
        }

        Message msg = mData.get(position);
        TextView msgtext = (TextView) convertView.findViewById(R.id.textViewMsgText);
        ImageView msgImg = (ImageView) convertView.findViewById(R.id.imageViewMsgPic);
        //final TextView msgsender = (TextView) convertView.findViewById(R.id.textViewMsgSender);
        TextView msgTime = (TextView) convertView.findViewById(R.id.textViewMsgTime);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        String cuid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200,200);

        if(msg.getImgurl().equals("")) {
            msgImg.setVisibility(View.GONE);
            msgtext.setText(msg.getMsgtext());
        } else {
            msgtext.setVisibility(View.GONE);
            Picasso.with(mContext).load(msg.getImgurl()).into(msgImg);
        }
        /*mDatabase.child("users").child(msg.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                msgsender.setText(dataSnapshot.child("fname").getValue().toString() + " " + dataSnapshot.child("lname").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/



        PrettyTime prettyTime = new PrettyTime();
        String d = prettyTime.format(new Date(msg.getDate()));
        msgTime.setText(d);
        if(cuid.equals(msg.getSender())) {
            msgTime.setGravity(Gravity.RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            msgtext.setGravity(Gravity.RIGHT);

        } else {
            msgTime.setGravity(Gravity.LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            msgtext.setGravity(Gravity.LEFT);
        }
        msgImg.setLayoutParams(params);
        return convertView;
    }
}
