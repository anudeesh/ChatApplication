package com.anudeesh.chatapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Anudeesh on 11/22/2016.
 */
public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.SavedViewHolder> implements RecyclerView.OnItemTouchListener {
    private ArrayList<User> list = new ArrayList<User>();
    //private ArrayList<String> uids = new ArrayList<String>();
    //String uid;
    Context mContext;
    private AdapterView.OnItemClickListener mListener;

    public UsersRecyclerAdapter(ArrayList<User> list, /*ArrayList<String> uids, String uid,*/ Context mContext) {
        this.list = list;
        //this.uids = uids;
        //this.uid = uid;
        this.mContext = mContext;
    }

    @Override
    public SavedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user_layout,parent,false);
        SavedViewHolder savedViewHolder = new SavedViewHolder(view);
        return savedViewHolder;
    }

    @Override
    public void onBindViewHolder(SavedViewHolder holder, int position) {
        User user = list.get(position);
        //String cid = uids.get(position);
        //if(!uid.equals(cid)) {
            holder.uname.setText(user.getFname()+" "+user.getLname());
            Picasso.with(mContext)
                    .load(user.getDp())
                    .into(holder.img);
        //}
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public static class SavedViewHolder extends RecyclerView.ViewHolder {

        TextView uname;
        ImageView img;
        LinearLayout container;
        public SavedViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.imageViewUserImg);
            uname = (TextView) itemView.findViewById(R.id.textViewUserName);
            container = (LinearLayout) itemView.findViewById(R.id.cont);

        }
    }
}
