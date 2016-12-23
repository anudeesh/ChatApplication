package com.anudeesh.chatapp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anudeesh on 11/20/2016.
 */
public class User implements Serializable {
    String fname, lname, gender, dp, email;

    public User(String fname, String lname, String gender, String dp, String email) {
        this.fname = fname;
        this.lname = lname;
        this.gender = gender;
        this.dp = dp;
        this.email = email;
    }

    public User() {

    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Object> toMap() {
        HashMap<String,Object> result = new HashMap<>();
        result.put("fname",fname);
        result.put("lname",lname);
        result.put("gender",gender);
        result.put("dp",dp);
        result.put("email",email);

        return result;
    }
}
