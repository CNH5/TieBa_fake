package com.example.tieba.beans;

import android.annotation.SuppressLint;
import com.example.tieba.R;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author sheng
 * @date 2021/9/25 16:56
 */
public class Reply implements Serializable {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SerializedName("id")
    private String id;

    @SerializedName("date")
    private String date;

    @SerializedName("poster_id")
    private String poster_id;

    @SerializedName("poster_name")
    private String poster_name;

    @SerializedName("poster_avatar")
    private String poster_avatar;

    @SerializedName("poster_exp")
    private String poster_exp;

    @SerializedName("info")
    private String info;

    @SerializedName("good")
    private int good;

    @SerializedName("bad")
    private int bad;

    @SerializedName("liked")
    private String liked;

    //对这层楼点赞,修改点赞点踩数和点赞状态
    public void like() {
        if (liked == null) {
            good = good + 1;
            liked = "true";
        } else if (liked.equals("true")) {
            good = good - 1;
            liked = null;
        } else if (liked.equals("false")) {
            bad = bad - 1;
            good = good + 1;
            liked = "true";
        }
    }

    //对这层楼点踩，
    public void unlike() {
        if (liked == null) {
            bad = bad + 1;
            liked = "false";
        } else if (liked.equals("false")) {
            bad = bad - 1;
            liked = null;
        } else if (liked.equals("true")) {
            bad = bad + 1;
            good = good - 1;
            liked = "false";
        }
    }

    public String getDate() {
        Date d = null;

        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assert d != null;
        Date now = new Date();
        if (d.getYear() < now.getYear()) {
            return date.substring(0, 10);

        } else if (d.getDate() < now.getDate() || d.getMonth() < now.getMonth()) {
            return date.substring(5, 10);

        } else if (d.getHours() < now.getHours()) {
            return (now.getHours() - d.getHours()) + "小时前";

        } else if (d.getMinutes() < now.getMinutes()) {
            return (now.getMinutes() - d.getMinutes()) + "分钟前";

        } else {
            int s = now.getSeconds() - d.getSeconds();
            return (s > 0) ? s + "秒前" : "刚刚";

        }
    }

    //获取点赞按钮的图标
    public int getLikeIcon() {
        return "true".equals(liked) ? R.mipmap.good_fill : R.mipmap.good;
    }

    //获取点踩按钮的图标
    public int getBadIcon() {
        return "false".equals(liked) ? R.mipmap.bad_fill : R.mipmap.bad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPoster_id() {
        return poster_id;
    }

    public void setPoster_id(String poster_id) {
        this.poster_id = poster_id;
    }

    public String getPoster_name() {
        return poster_name;
    }

    public void setPoster_name(String poster_name) {
        this.poster_name = poster_name;
    }

    public String getPoster_avatar() {
        return poster_avatar;
    }

    public void setPoster_avatar(String poster_avatar) {
        this.poster_avatar = poster_avatar;
    }

    public String getPoster_exp() {
        return poster_exp;
    }

    public void setPoster_exp(String poster_exp) {
        this.poster_exp = poster_exp;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getGood() {
        return good;
    }

    public void setGood(int good) {
        this.good = good;
    }

    public int getBad() {
        return bad;
    }

    public void setBad(int bad) {
        this.bad = bad;
    }

    public String getLiked() {
        return liked;
    }

    public void setLiked(String liked) {
        this.liked = liked;
    }
}
