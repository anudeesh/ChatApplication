package com.anudeesh.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Anudeesh on 11/22/2016.
 */
public class UserListAdapter extends ArrayAdapter<User> {
    List<User> mData;
    Context mContext;
    int mResource;
    List<String> msgIDs;
    List<String> otherIDs;
    public UserListAdapter(Context context, int resource, List<User> objects, List<String> objects1,List<String> objects2) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mData = objects;
        this.msgIDs = objects1;
        this.otherIDs = objects2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource,parent,false);
        }
        User user = mData.get(position);

        TextView uname = (TextView) convertView.findViewById(R.id.textViewUserName);
        ImageView img = (ImageView) convertView.findViewById(R.id.imageViewUserImg);
        ImageView status = (ImageView) convertView.findViewById(R.id.imageViewUnread);

        uname.setText(user.getFname()+" "+user.getLname());
        Picasso.with(mContext)
                .load(user.getDp())
                .into(img);
        status.setVisibility(View.INVISIBLE);
        for(int i=0;i<msgIDs.size();i++) {
            if(msgIDs.get(i).equals(otherIDs.get(position))) {
                status.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }
}
