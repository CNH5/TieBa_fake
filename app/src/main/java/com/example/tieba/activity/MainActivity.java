package com.example.tieba.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import com.example.tieba.beans.Ba;
import com.example.tieba.beans.Tie;
import com.example.tieba.fragments.TieListFragment;
import com.example.tieba.fragments.UndoneFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String[] TAB_NAMES = new String[]{"全部", "精华", "动态"};
    public static final String ACCOUNT_VALUE = "MainActivity.ACCOUNT";
    private final String ba_id = "1";
    private String account;
    private boolean was_login;
    private Thread getDataThread;
    private Ba ba;
    //暂时没有数据，吧编号固定为1
    private TieListFragment tie_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置按键监听
        setItemOnClickListener();

        initParams();
        //设置吧头像、吧名和显示经验
        initBaInfo();

        //设置tab
        initTab();
    }


    private void initParams() {
        String spFileName = getResources().getString(R.string.shared_preferences_file_name);
        String wasLoginKey = getResources().getString(R.string.was_login);
        String accountKey = getResources().getString(R.string.account);
        SharedPreferences spFile = getSharedPreferences(spFileName, MODE_PRIVATE);

        was_login = spFile.getBoolean(wasLoginKey, false);
        was_login = false;  //注释掉每次打开就不用重新登录，但是没法退出登录
        account = was_login ? spFile.getString(accountKey, null) : null;

        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.blue);

        swipe.setOnRefreshListener(() -> {
            tie_list.refreshData();
            initBaInfo();
            swipe.setRefreshing(false);
        });

        findViewById(R.id.level_name).setOnClickListener(this);
        findViewById(R.id.exp_progress).setOnClickListener(this);

        NestedScrollView scroll_item = findViewById(R.id.scrollView);
        scroll_item.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            swipe.setEnabled(scrollY == 0);

            ConstraintLayout tab_view2 = findViewById(R.id.tab_view2);
            ConstraintLayout tab_view1 = findViewById(R.id.tab_view1);
            if (scrollY >= tab_view1.getY()) {
                //悬浮的切换框显示
                tab_view2.setVisibility(View.VISIBLE);
                tab_view1.setVisibility(View.GONE);
                findViewById(R.id.top_bar).setEnabled(true);

            } else {
                //悬浮的切换框不显示
                findViewById(R.id.top_bar).setEnabled(false);
                tab_view2.setVisibility(View.GONE);
                tab_view1.setVisibility(View.VISIBLE);
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void initBaInfo() {
        getData();

        TextView sign_in_bt = findViewById(R.id.sign_in_bt);
        sign_in_bt.setOnClickListener(this);

        waitData();

        Glide.with(this).load(Constants.GET_IMAGE_PATH + ba.getAvatar())
                .into((ImageView) findViewById(R.id.ba_avatar));
        ((TextView) findViewById(R.id.ba_name)).setText(ba.getName() + "吧");
        sign_in_bt.setVisibility(View.VISIBLE);
        //获取经验
        ProgressBar exp_progress = findViewById(R.id.exp_progress);

        if (was_login && ba.isSubscription()) {
            if (ba.isSigned()) {
                sign_in_bt.setText("已签到");
                sign_in_bt.setTextColor(Color.parseColor("#909399"));
                sign_in_bt.setBackgroundColor(Color.WHITE);
                sign_in_bt.setEnabled(false);

            } else {
                sign_in_bt.setText("签到");
                sign_in_bt.setTextColor(Color.parseColor("#FFFFFF"));
                sign_in_bt.setBackgroundResource(R.drawable.shape_corner5);
                sign_in_bt.setEnabled(true);
            }

            ((TextView) findViewById(R.id.level_name)).setText(ba.getLevel());
            exp_progress.setVisibility(View.VISIBLE);
            exp_progress.setMax(ba.levelUpExp());
            exp_progress.setProgress(ba.getExp() != null ? Integer.parseInt(ba.getExp()) : 0);

        } else {
            ((TextView) findViewById(R.id.level_name)).setText("关注 119.3W\t\t\t帖子 1.9KW");
            sign_in_bt.setText("+ 关注");
            exp_progress.setVisibility(View.GONE);
        }
    }


    private void initTab() {
        TabLayout tab1 = findViewById(R.id.tab1);
        TabLayout tab2 = findViewById(R.id.tab2);
        ViewPager2 viewPager = findViewById(R.id.viewpager);
        List<Fragment> tabFragmentList = new ArrayList<>();

        for (String tab : TAB_NAMES) {
            tab1.addTab(tab1.newTab().setText(tab));
            tab2.addTab(tab2.newTab().setText(tab));
        }

        tie_list = TieListFragment.newInstance();
        tie_list.setAccount(account);

        tabFragmentList.add(tie_list);
        tabFragmentList.add(UndoneFragment.newInstance());
        tabFragmentList.add(UndoneFragment.newInstance());

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return tabFragmentList.get(position);
            }

            @Override
            public int getItemCount() {
                return tabFragmentList.size();
            }
        });

        new TabLayoutMediator(tab1, viewPager, (tab, position) -> tab.setText(TAB_NAMES[position])).attach();

        new TabLayoutMediator(tab2, viewPager, (tab, position) -> tab.setText(TAB_NAMES[position])).attach();
    }

    private void waitData() {
        try { //等待获取数据的线程完成,好像也可以在这里设置加载动画
            getDataThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO:还有加载失败的界面
    }

    private void getData() {
        getDataThread = new Thread(() -> {
            try {
                HttpUrl get_exp_url = Objects.requireNonNull(HttpUrl.parse(Constants.GET_BA_PATH)).newBuilder()
                        .addQueryParameter("account", account)
                        .addQueryParameter("ba", ba_id)
                        .build();

                ba = new Gson().fromJson(BackstageInteractive.get(get_exp_url), Ba.class);

            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(this, "网络异常!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        getDataThread.start();
    }

    private void setItemOnClickListener() {
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.search).setOnClickListener(this);
        findViewById(R.id.bell).setOnClickListener(this);
        findViewById(R.id.send_tie_bt).setOnClickListener(this);
        findViewById(R.id.sign_in_bt).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.back) {
            Toast.makeText(this, "首页未实现！", Toast.LENGTH_SHORT).show();

        } else if (vid == R.id.search || vid == R.id.bell) {
            Toast.makeText(this, "功能未实现!", Toast.LENGTH_SHORT).show();

        } else if (vid == R.id.send_tie_bt) {
            //如果没登录就跳转到登录界面，如果登录了了就到发帖页面
            Intent intent = new Intent(this, was_login ? SendTieActivity.class : LoginActivity.class);
            intent.putExtra(ACCOUNT_VALUE, account);
            startActivityForResult(intent, was_login ? SendTieActivity.CODE : LoginActivity.CODE);

        } else if (vid == R.id.sign_in_bt) {
            if (!was_login) {
                //没有登录
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(ACCOUNT_VALUE, account);
                startActivityForResult(intent, LoginActivity.CODE);

            } else if (ba.isSubscription()) {
                //已经关注吧，则签到
                signIn();

            } else {
                //没有关注吧，则应当先关注，再更新吧信息
                subscriptionBa();
                initBaInfo();
            }
        } else if (vid == R.id.level_name || vid == R.id.exp_progress) {
            //显示经验进度
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(
                    "经验值: " + ba.getExp() + "/" + ba.levelUpExp() + "\n" +
                            "超级会员经验加速6倍"
            );//提示消息
            //积极的选择
            alertDialog.setPositiveButton("立即开通", (dialog, which) ->
                    Toast.makeText(MainActivity.this, "暂未实现！", Toast.LENGTH_SHORT).show()
            );
            //中立的选择
            alertDialog.setNeutralButton("取消", (dialog, which) -> {
            });
            //显示
            alertDialog.show();
        }
    }

    private void subscriptionBa() {
        final String[] result = {null};

        Thread thread = new Thread(() -> {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("account", account)
                    .addFormDataPart("ba", ba_id);

            try {
                result[0] = BackstageInteractive.post(Constants.SUBSCRIPTION_BA_PATH, builder.build());
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(this, "网络异常!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        thread.start();

        try { //等待上传数据的线程完成,好像也可以在这里设置加载动画
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, result[0], Toast.LENGTH_SHORT).show();
        initBaInfo();
    }

    //签到
    private void signIn() {
        final String[] result = {null};
        Thread thread = new Thread(() -> {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("account", account)
                    .addFormDataPart("ba", ba_id);

            try {
                result[0] = BackstageInteractive.post(Constants.SIGN_IN_PATH, builder.build());
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(this, "网络异常!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if ("invalid".equals(result[0])) {
            Toast.makeText(this, "签到失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "签到成功，经验+8\n你是今日第" + result[0] + "个签到", Toast.LENGTH_SHORT).show();
            initBaInfo();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SendTieActivity.CODE: //点击发帖后返回，应该根据情况添加一条帖子,或者说刷新页面？
                if (resultCode == RESULT_OK) {
                    tie_list.refreshData();
                }
                break;
            case LoginActivity.CODE: //从登录界面退出来，刷新顶部的经验条什么的,顺带刷新帖子列表
                if (resultCode == RESULT_OK) {
                    assert data != null;

                    account = data.getStringExtra("account");
                    Toast.makeText(this, "登录成功!", Toast.LENGTH_SHORT).show();
                    was_login = true;

                    initBaInfo();

                    tie_list.setAccount(account);
                    tie_list.refreshData();
                }
                break;
            default: {
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    if (account == null && data.getStringExtra("account") != null) {
                        was_login = true;
                        account = data.getStringExtra("account");
                        initBaInfo();

                        tie_list.setAccount(account);
                        tie_list.refreshData();

                    } else if (requestCode == TieActivity.CODE) {
                        //点击帖子后返回,应当更新在里面的点赞状态
                        Tie tie = (Tie) data.getSerializableExtra("tie");
                        int position = data.getIntExtra("position", -1);
                        tie_list.changeListItem(position, tie);
                    }
                }
            }
        }
    }
}