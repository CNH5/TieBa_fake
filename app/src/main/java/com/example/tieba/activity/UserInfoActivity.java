package com.example.tieba.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import cc.shinichi.library.ImagePreview;
import com.bumptech.glide.Glide;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import com.example.tieba.adapter.HistoryTieAdapter;
import com.example.tieba.beans.Tie;
import com.example.tieba.beans.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int CODE = 10;
    private String account;
    private String target;
    private List<Tie> historyTieList;
    private HistoryTieAdapter adapter;
    private User user;
    private RecyclerView historyList;
    private TextView bt1;
    private Thread t1;
    private Thread t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initParams();
        refreshData();
    }

    @SuppressLint("SetTextI18n")
    private void intiView() {
        ((TextView) findViewById(R.id.name)).setText(user.getName());
        ((TextView) findViewById(R.id.ba_age)).setText("吧龄: " + user.getBaAge());
        ((TextView) findViewById(R.id.Introduction)).setText("简介: " + user.getIntroduction());

        ImageView more_bt = findViewById(R.id.more);
        more_bt.setOnClickListener(this);

        ImageView community_bt = findViewById(R.id.community_bt);
        community_bt.setOnClickListener(this);


        findViewById(R.id.back).setOnClickListener(this);

        if (account != null && account.equals(target)) {
            ((TextView) findViewById(R.id.topic)).setText("我的主页");
            bt1.setText("编辑资料");
            community_bt.setVisibility(View.GONE);
            more_bt.setVisibility(View.GONE);

        } else {
            ((TextView) findViewById(R.id.topic)).setText("TA的主页");
            bt1.setText("+ 关注");
        }

        ((TextView) findViewById(R.id.sex)).setText(user.getSex());
        ((TextView) findViewById(R.id.end_text)).setText("暂无更多");

        ImageView avatar = findViewById(R.id.avatar);
        avatar.setOnClickListener(this);

        Glide.with(this).load(Constants.GET_IMAGE_PATH + user.getAvatar()).into(avatar);
    }

    private void initParams() {
        account = getIntent().getStringExtra("account");
        target = getIntent().getStringExtra("target");
        if (target == null) {
            finish();
        } else {
            getUser();
        }

        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.blue);

        swipe.setOnRefreshListener(() -> {
            refreshData();
            swipe.setRefreshing(false);
        });

        NestedScrollView scroll_item = findViewById(R.id.scrollView);
        scroll_item.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            swipe.setEnabled(scrollY == 0);
            //TODO: 还可以在这里设置悬浮框...
        });

        historyList = findViewById(R.id.history_tie_list);
        historyList.setLayoutManager(new LinearLayoutManager(this));

        bt1 = findViewById(R.id.bt1);
        bt1.setOnClickListener(this);
    }

    private void refreshData() {
        getUser();
        getHistoryTie();

        waitUserData();
        waitListData();
        intiView();

        adapter = new HistoryTieAdapter(this, historyTieList, account);
        historyList.setAdapter(adapter);
    }

    private void getUser() {
        t1 = new Thread(() -> {
            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Constants.GET_USER_INFO_PATH)).newBuilder()
                    .addQueryParameter("target", target)
                    .build();

            Type user_type = new TypeToken<User>() {
            }.getType();  //创建一个新类型

            try {
                user = new Gson().fromJson(
                        BackstageInteractive.get(url),
                        user_type
                );
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(this, "网络异常!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        t1.start();
    }

    private void getHistoryTie() {
        t2 = new Thread(() -> {
            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Constants.GET_USER_TIE_PATH)).newBuilder()
                    .addQueryParameter("account", account)
                    .addQueryParameter("target", target)
                    .build();
            Type type_tie_list = new TypeToken<List<Tie>>() {
            }.getType();  //创建一个新类型

            try {
                historyTieList = new Gson().fromJson(
                        BackstageInteractive.get(url),
                        type_tie_list
                );
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(this, "网络异常!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        t2.start();
    }

    private void waitUserData() {
        try { //等待获取数据的线程完成
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitListData() {
        try { //等待获取数据的线程完成
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            assert data != null;
            if (data.getStringExtra("account") != null && account == null) {
                account = data.getStringExtra("account");

                Intent intent = new Intent().putExtra("account", account);
                setResult(RESULT_OK, intent);
            }
            switch (requestCode) {
                case TieActivity.CODE:
                    Tie tie = (Tie) data.getSerializableExtra("tie");
                    int position = data.getIntExtra("position", -1);
                    adapter.changeItem(position, tie);

                    break;
                case LoginActivity.CODE:
                    Toast.makeText(this, "登录成功!", Toast.LENGTH_SHORT).show();
                    refreshData();

                    break;
                case ChangeUserInfoActivity.CODE:
                    refreshData();

                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.back) {
            finish();

        } else if (vid == R.id.community_bt || vid == R.id.more) {
            Toast.makeText(this, "暂未实现!", Toast.LENGTH_SHORT).show();

        } else if (vid == R.id.avatar) {
            ImagePreview.getInstance()
                    .setContext(this)  // 上下文，必须是activity，不需要担心内存泄漏，本框架已经处理好；
                    .setIndex(0)  // 设置从第几张开始看（索引从0开始）
                    .setImage(Constants.GET_IMAGE_PATH + user.getAvatar())
                    .start();

        } else if (vid == R.id.bt1) {
            if (account != null && account.equals(target)) {
                Intent intent = new Intent(this, ChangeUserInfoActivity.class);
                intent.putExtra("user", user);
                startActivityForResult(intent, ChangeUserInfoActivity.CODE);

            } else {
                Toast.makeText(this, "暂未实现!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}