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
 * @date 2021/8/25 10:10
 */
public class Tie implements Serializable {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SerializedName("id")
    private String id;

    @SerializedName("date")
    private String date;

    @SerializedName("title")
    private String title;

    @SerializedName("info")
    private String info;

    @SerializedName("good")
    private int good;

    @SerializedName("bad")
    private int bad;

    @SerializedName("reply_count")
    private long reply_count;

    @SerializedName("poster_id")
    private String poster_id;

    @SerializedName("poster_name")
    private String poster_name;

    @SerializedName("poster_avatar")
    private String poster_avatar;

    @SerializedName("poster_exp")
    private String poster_exp;

    @SerializedName("liked")
    private String liked;

    @SerializedName("img")
    private String img;

    @SerializedName("ba_name")
    private String ba_name;

    public String getBa_name() {
        return ba_name;
    }

    public void setBa_name(String ba_name) {
        this.ba_name = ba_name;
    }

    public Date getDateValue() {
        Date d = null;

        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return d;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getLiked() {
        return liked;
    }

    public void setLiked(String liked) {
        this.liked = liked;
    }

    public String getPoster_id() {
        return poster_id;
    }

    public void setPoster_id(String poster_id) {
        this.poster_id = poster_id;
    }

    public void setPoster_exp(String poster_exp) {
        this.poster_exp = poster_exp;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        Date d = getDateValue();

        Date now = new Date();

        assert d != null;

        //时区不对，手动加8小时
        long t = now.getTime() - d.getTime() + 8 * 1000 * 60 * 60;
        if (t < (1000 * 60)) {
            int s = now.getSeconds() - d.getSeconds();
            return s > 0 ? s + "秒前" : "刚刚";

        } else if (t < (1000 * 60 * 60)) {
            return ((int) t / (1000 * 60)) + "分钟前";

        } else if (t < (1000 * 24 * 60 * 60)) {
            return ((int) t / (1000 * 60 * 60)) + "小时前";

        } else if (d.getMonth() < now.getMonth() && d.getYear() == now.getYear()) {
            return date.substring(5, 10);

        } else {
            return date.substring(0, 10);
        }
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getReply_count() {
        return reply_count;
    }

    public void setReply_count(long reply_count) {
        this.reply_count = reply_count;
    }

    //对这个帖子点赞,修改点赞点踩数和点赞状态
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

    //对这个帖子点踩，
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

    //获取点赞按钮的图标
    public int getLikeIcon() {
        return "true".equals(liked) ? R.mipmap.good_fill : R.mipmap.good;
    }

    //获取点踩按钮的图标
    public int getBadIcon() {
        return "false".equals(liked) ? R.mipmap.bad_fill : R.mipmap.bad;
    }

    @Override
    public String toString() {
        return "Tie{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", title='" + title + '\'' +
                ", info='" + info + '\'' +
                ", good=" + good +
                ", bad=" + bad +
                ", reply_count=" + reply_count +
                ", poster_id='" + poster_id + '\'' +
                ", poster_name='" + poster_name + '\'' +
                ", poster_avatar='" + poster_avatar + '\'' +
                ", poster_exp='" + poster_exp + '\'' +
                ", liked='" + liked + '\'' +
                ", img='" + img + '\'' +
                ", ba_name='" + ba_name + '\'' +
                '}';
    }
}
