package com.example.tieba.beans;

import android.annotation.SuppressLint;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author sheng
 * @date 2021/9/20 9:20
 */
public class User implements Serializable {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @SerializedName("name")
    private String name;

    @SerializedName("registration_date")
    private String registration_date;

    @SerializedName("introduction")
    private String introduction;
    @SerializedName("sex")
    private String sex;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("account")
    private String account;

    public String getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    //获取吧龄，有点难算
    @SuppressLint("DefaultLocale")
    public String getBaAge() {
        Date reg_date = null;
        try {
            reg_date = sdf.parse(registration_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert reg_date != null;
        //1000 * 60 * 60 * 24 * 365 = 31536000000,每次都要算不太好
        return String.format("%.1f年", (new Date().getTime() - reg_date.getTime()) / 31536000000.0);
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getSex() {
        return sex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegistration_date(String registration_date) {
        this.registration_date = registration_date;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
