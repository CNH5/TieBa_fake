package com.example.tieba.beans;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author sheng
 * @date 2021/9/28 16:36
 */
public class Ba implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("introduction")
    private String introduction;

    @SerializedName("exp")
    private String exp;

    @SerializedName("signed")
    private boolean signed;

    @SerializedName("subscription")
    private boolean subscription;

    private Level level;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public boolean isSubscription() {
        return subscription;
    }

    public void setSubscription(boolean subscription) {
        this.subscription = subscription;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public String getLevel() {
        if (level == null) {
            level = new Level(exp);
        }
        return level.toString();
    }

    public int levelUpExp(){
        if (level == null) {
            level = new Level(exp);
        }
        return level.getLevelUpExp();
    }
}
