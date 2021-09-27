package com.example.tieba.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import com.example.tieba.activity.UserInfoActivity;
import com.example.tieba.adapter.ReplyAdapter;
import com.example.tieba.beans.Floor;
import com.example.tieba.beans.Level;
import com.example.tieba.beans.Reply;
import com.example.tieba.views.ImageTextButton1;
import com.example.tieba.views.TextInImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * @author sheng
 * @date 2021/9/27 16:47
 */
public class ReplyFloorPopupWindow extends PopupWindow implements View.OnClickListener {
    private final Context mContext;
    private final View v;
    private ImageTextButton1 good_bt;
    private ImageTextButton1 bad_bt;
    private RecyclerView reply_list;
    private Thread getDataThread;
    private List<Reply> list;
    private final Floor floor;
    private final String tie_poster_id;
    private final String account;

    @SuppressLint("InflateParams")
    public ReplyFloorPopupWindow(@NonNull Context context, Floor floor, String tie_poster_id, String account) {
        super(context);
        this.floor = floor;
        this.tie_poster_id = tie_poster_id;
        this.account = account;
        this.mContext = context;
        v = LayoutInflater.from(mContext).inflate(R.layout.reply_floor_view, null);

        initWindow();
    }

    private void initWindow() {
        //设置View
        setContentView(v);

        //设置宽与高
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        int screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        setHeight(Math.round(screenHeight * 0.89f));

        // 设置弹出窗体可点击
        setFocusable(true);

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.popWindow_anim);

        setOutsideTouchable(true);

        //实例化一个ColorDrawable颜色为透明(半透明是0xb0000000)
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
    }

    private void initList() {
        waitData();
        ReplyAdapter adapter = new ReplyAdapter(list, mContext, account, tie_poster_id);

        adapter.setItemOnClickListener(this::inputViewVisible);

        reply_list.setAdapter(adapter);
    }

    private void inputViewVisible() {

    }

    private void getData() {
        getDataThread = new Thread(() -> {
            HttpUrl get_tie_list_url = Objects.requireNonNull(HttpUrl.parse(Constants.GET_REPLY_PATH)).newBuilder()
                    .addQueryParameter("account", account)
                    .addQueryParameter("floor", floor.getId())
                    .build();

            Type type_list = new TypeToken<List<Reply>>() {
            }.getType();  //创建一个新类型
            try {
                list = new Gson().fromJson(
                        BackstageInteractive.get(get_tie_list_url),
                        type_list
                );
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(mContext, "网络异常!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        getDataThread.start();
    }

    public void init() {
        getData();
        initView();
        initList();

        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.alpha = 0.6f;
        ((Activity) mContext).getWindow().setAttributes(lp);
    }

    private void waitData() {
        try {
            getDataThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private void initView() {
        good_bt = v.findViewById(R.id.good_bt);
        bad_bt = v.findViewById(R.id.bad_bt);

        good_bt.setOnClickListener(this);
        bad_bt.setOnClickListener(this);

        reply_list = v.findViewById(R.id.reply_floor_list);
        reply_list.setLayoutManager(new LinearLayoutManager(mContext));

        v.findViewById(R.id.close).setOnClickListener(this);
        v.findViewById(R.id.name).setOnClickListener(this);
        v.findViewById(R.id.reply_input).setOnClickListener(this);

        TextInImageView level_icon = v.findViewById(R.id.level_icon);
        Level level = new Level(floor.getPoster_exp());

        level_icon.setImageResource(level.getLevelIcon());
        level_icon.setText(level.getLevel());

        ((TextView) v.findViewById(R.id.name)).setText(floor.getPoster_name());
        ((TextView) v.findViewById(R.id.floor_num)).setText(String.format("%s楼的回复", floor.getFloor()));
        ((TextView) v.findViewById(R.id.reply_count)).setText(String.format("%d条回复", floor.getReply_count()));
        ((TextView) v.findViewById(R.id.date)).setText(String.format("第%s楼 | %s", floor.getFloor(), floor.getDate()));

        ImageView avatar = v.findViewById(R.id.avatar);
        avatar.setOnClickListener(this);
        if (floor.getPoster_avatar() == null) {
            avatar.setImageResource(R.mipmap.null_user_avatar);
        } else {
            Glide.with(mContext).load(Constants.GET_IMAGE_PATH + floor.getPoster_avatar()).into(avatar);
        }

        if (floor.getReply_count() == 0) {
            ((TextView) v.findViewById(R.id.end_text)).setText("此楼还没有回复哦~");
        }

        TextView ident = v.findViewById(R.id.ident);
        if (tie_poster_id.equals(floor.getPoster_id())) {
            ident.setText("楼主");
            ident.setVisibility(View.VISIBLE);
        }

        //加载图片
        ImageView floor_image = v.findViewById(R.id.floor_image);
        if (floor.getImg() != null) {
            Glide.with(mContext).load(Constants.GET_IMAGE_PATH + floor.getImg()).into(floor_image);
            floor_image.setVisibility(View.VISIBLE);
        }

        TextView floor_info = v.findViewById(R.id.floor_info);
        if (floor.getInfo() == null) {
            floor_info.setVisibility(View.GONE);
        } else {
            floor_info.setText(floor.getInfo());
        }

        setGoodBadButton();
    }

    private void setGoodBadButton() {
        good_bt.setImageResource(floor.getLikeIcon());
        bad_bt.setImageResource(floor.getBadIcon());
        bad_bt.setText((floor.getBad() != 0) ? String.valueOf(floor.getBad()) : "踩");
        good_bt.setText((floor.getGood() != 0) ? String.valueOf(floor.getGood()) : "赞");
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.close) {
            //退出
            dismiss();

        } else if (vid == R.id.good_bt) {
            floor.like();
            setGoodBadButton();
            BackstageInteractive.sendLike(account, floor.getId(), floor.getLiked(), Constants.FLOOR);

        } else if (vid == R.id.bad_bt) {
            floor.unlike();
            setGoodBadButton();
            BackstageInteractive.sendLike(account, floor.getId(), floor.getLiked(), Constants.FLOOR);

        } else if (vid == R.id.avatar || vid == R.id.name) {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            //传递账号，到那边再查询
            intent.putExtra("target", floor.getPoster_id());
            intent.putExtra("account", account);
            mContext.startActivity(intent);

        } else if (vid == R.id.reply_input) {
            inputViewVisible();
        }
    }
}
