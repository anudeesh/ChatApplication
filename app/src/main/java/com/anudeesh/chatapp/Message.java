package com.anudeesh.chatapp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anudeesh on 11/22/2016.
 */
public class Message implements Serializable {
    String msgtext, date, sender, receiver, read, imgurl;

    public Message(String msgtext, String date, String sender, String receiver, String read) {
        this.msgtext = msgtext;
        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
        this.read = read;
    }

    public Message() {

    }

    public String getMsgtext() {
        return msgtext;
    }

    public void setMsgtext(String msgtext) {
        this.msgtext = msgtext;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public Map<String, Object> toMap() {
        HashMap<String,Object> result = new HashMap<>();
        result.put("msgtext",msgtext);
        result.put("date",date);
        result.put("sender",sender);
        result.put("receiver",receiver);
        result.put("read",read);
        result.put("imgurl",imgurl);

        return result;
    }
}
