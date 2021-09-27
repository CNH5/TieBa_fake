package com.example.tieba.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
 * @date 2021/9/25 16:10
 */
public class ReplyFloorDialog extends Dialog implements View.OnClickListener {
    private ImageTextButton1 good_bt;
    private ImageTextButton1 bad_bt;
    private RecyclerView reply_list;
    private Thread getDataThread;
    private List<Reply> list;
    private final Floor floor;
    private final String tie_poster_id;
    private final String account;

    public ReplyFloorDialog(@NonNull Context context, Floor floor, String tie_poster_id, String account) {
        super(context, R.style.MyTheme_Dialog);
        this.floor = floor;
        this.tie_poster_id = tie_poster_id;
        this.account = account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();

        setCanceledOnTouchOutside(true);
        setContentView(R.layout.reply_floor_view);

        initView();
        initList();
    }

    private void initList() {
        waitData();
        ReplyAdapter adapter = new ReplyAdapter(list, getContext(), account, tie_poster_id);

        adapter.setItemOnClickListener(this::inputViewVisible);

        reply_list.setAdapter(adapter);
    }

    private void inputViewVisible() {
        //TODO: 显示一个editText，悬浮在键盘上面，
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
                Toast.makeText(getContext(), "网络异常!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        getDataThread.start();
    }

    //等待获取数据的线程完成，好像也能弄加载动画？
    private void waitData() {
        try {
            getDataThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private void initView() {
        good_bt = findViewById(R.id.good_bt);
        bad_bt = findViewById(R.id.bad_bt);

        good_bt.setOnClickListener(this);
        bad_bt.setOnClickListener(this);

        reply_list = findViewById(R.id.reply_floor_list);
        reply_list.setLayoutManager(new LinearLayoutManager(getContext()));

        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.name).setOnClickListener(this);
        findViewById(R.id.reply_input).setOnClickListener(this);

        TextInImageView level_icon = findViewById(R.id.level_icon);
        Level level = new Level(floor.getPoster_exp());

        level_icon.setImageResource(level.getLevelIcon());
        level_icon.setText(level.getLevel());

        ((TextView) findViewById(R.id.name)).setText(floor.getPoster_name());
        ((TextView) findViewById(R.id.floor_num)).setText(String.format("%s楼的回复", floor.getFloor()));
        ((TextView) findViewById(R.id.reply_count)).setText(String.format("%d条回复", floor.getReply_count()));
        ((TextView) findViewById(R.id.date)).setText(String.format("第%s楼 | %s", floor.getFloor(), floor.getDate()));

        ImageView avatar = findViewById(R.id.avatar);
        avatar.setOnClickListener(this);
        if (floor.getPoster_avatar() == null) {
            avatar.setImageResource(R.mipmap.null_user_avatar);
        } else {
            Glide.with(getContext()).load(Constants.GET_IMAGE_PATH + floor.getPoster_avatar()).into(avatar);
        }

        if (floor.getReply_count() == 0) {
            ((TextView) findViewById(R.id.end_text)).setText("此楼还没有回复哦~");
        }

        TextView ident = findViewById(R.id.ident);
        if (tie_poster_id.equals(floor.getPoster_id())) {
            ident.setText("楼主");
            ident.setVisibility(View.VISIBLE);
        }

        //加载图片
        ImageView floor_image = findViewById(R.id.floor_image);
        if (floor.getImg() != null) {
            Glide.with(getContext()).load(Constants.GET_IMAGE_PATH + floor.getImg()).into(floor_image);
            floor_image.setVisibility(View.VISIBLE);
        }

        TextView floor_info = findViewById(R.id.floor_info);
        if (floor.getInfo() == null) {
            floor_info.setVisibility(View.GONE);
        } else {
            floor_info.setText(floor.getInfo());
        }

        setGoodBadButton();

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
            //退出dialog
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
            Intent intent = new Intent(getContext(), UserInfoActivity.class);
            //传递账号，到那边再查询
            intent.putExtra("target", floor.getPoster_id());
            intent.putExtra("account", account);
            getContext().startActivity(intent);

        } else if (vid == R.id.reply_input) {
            inputViewVisible();
        }
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {
        super.onProvideKeyboardShortcuts(data, menu, deviceId);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
