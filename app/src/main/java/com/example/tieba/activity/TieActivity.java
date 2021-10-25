package com.example.tieba.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import cc.shinichi.library.ImagePreview;
import com.bumptech.glide.Glide;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import com.example.tieba.adapter.FloorAdapter;
import com.example.tieba.beans.Floor;
import com.example.tieba.beans.Level;
import com.example.tieba.beans.Tie;
import com.example.tieba.fragments.UndoneFragment;
import com.example.tieba.popupWindow.HeightProvider;
import com.example.tieba.views.ImageTextButton1;
import com.example.tieba.views.TextInImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class TieActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    public static final int CODE = 1;
    public static final int GET_IMAGE_EXIT_CODE = 6;
    private static final String[] TAB_NAMES = new String[]{"全部回复", "只看楼主"};
    private static final String[] ITEMS = {"热门排序", "正序排序", "倒序排序"};
    private static final String[] CONDITIONS = {"all", "only_tie_poster"};
    private HeightProvider provider;
    private String floor_order = "floor";  //楼层的排列顺序
    private int pos = 0;  //筛选条件，只看楼主和全部
    private String tie_id;
    private Tie tie;
    private String account;
    private Thread t;
    private Uri imgUri;
    private List<Floor> floorData;
    private ImageTextButton1 good_bt;
    private ImageTextButton1 bad_bt;
    private EditText reply_text;
    private View reply_view;
    private View reply_tie_bt;
    private InputMethodManager inputManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tie);
        initParams();
        getData();

        setItemOnClickListener();
        initTab();
        initView();
    }

    private void initParams() {
        account = getIntent().getStringExtra("account");
        tie_id = getIntent().getStringExtra("tie_id");

        reply_text = findViewById(R.id.reply_text);
        reply_text.addTextChangedListener(this);

        reply_view = findViewById(R.id.reply_view);

        reply_tie_bt = findViewById(R.id.reply_tie_bt);
        reply_tie_bt.setOnClickListener(this);

        provider = new HeightProvider(this).setHeightListener(height -> reply_view.setTranslationY(-height));
        provider.init();

        good_bt = findViewById(R.id.good_bt);
        bad_bt = findViewById(R.id.bad_bt);

        good_bt.setOnClickListener(this);
        bad_bt.setOnClickListener(this);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.blue);

        swipe.setOnRefreshListener(() -> {
            refreshData();
            swipe.setRefreshing(false);
        });

        findViewById(R.id.scrollView).setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
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

            if (scrollY != oldScrollY) {
                //TODO:滑动时窗口铺满屏幕
                if (inputManager.isActive()) {
                    inputManager.hideSoftInputFromWindow(reply_text.getWindowToken(), 0);
                }

                reply_text.clearFocus();
                reply_tie_bt.setVisibility(View.VISIBLE);
                reply_view.setVisibility(View.GONE);
            }
        });
    }

    private void setItemOnClickListener() {
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.more).setOnClickListener(this);
        findViewById(R.id.share_bt).setOnClickListener(this);
        findViewById(R.id.comment_bt).setOnClickListener(this);
        findViewById(R.id.floor_sort1).setOnClickListener(this);
        findViewById(R.id.floor_sort2).setOnClickListener(this);
        findViewById(R.id.avatar).setOnClickListener(this);
        findViewById(R.id.name).setOnClickListener(this);
        findViewById(R.id.subscription_bt).setOnClickListener(this);
        findViewById(R.id.end_info).setOnClickListener(this);
        findViewById(R.id.send_reply_bt).setOnClickListener(this);
        findViewById(R.id.emoji).setOnClickListener(this);
        findViewById(R.id.reply_image).setOnClickListener(this);
        findViewById(R.id.microphone).setOnClickListener(this);
        findViewById(R.id.at).setOnClickListener(this);
        findViewById(R.id.gift).setOnClickListener(this);
        findViewById(R.id.reply_more).setOnClickListener(this);
    }

    private void initTab() {
        TabLayout tab1 = findViewById(R.id.tab1);
        TabLayout tab2 = findViewById(R.id.tab2);
        //虚空绑定...
        ViewPager2 viewPager = new ViewPager2(this);

        for (String tab : TAB_NAMES) {
            tab1.addTab(tab1.newTab().setText(tab));
            tab2.addTab(tab2.newTab().setText(tab));
        }

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return new UndoneFragment();
            }

            @Override
            public int getItemCount() {
                return TAB_NAMES.length;
            }
        });

        new TabLayoutMediator(tab1, viewPager, (tab, position) -> tab.setText(TAB_NAMES[position])).attach();

        new TabLayoutMediator(tab2, viewPager, (tab, position) -> tab.setText(TAB_NAMES[position])).attach();

        //这只用绑定一个就够了，因为两个都是一起选中的，
        tab1.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pos = tab.getPosition();
                refreshData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        if (tie == null) {
            waitData();
        }

        ImageView avatar = findViewById(R.id.avatar);

        Glide.with(this).load(Constants.GET_IMAGE_PATH + tie.getPoster_avatar()).into(avatar);

        if (tie.getImg() != null) {
            ImageView tie_img = findViewById(R.id.tie_image);
            tie_img.setOnClickListener(this);
            Glide.with(this).load(Constants.GET_IMAGE_PATH + tie.getImg()).into(tie_img);
            tie_img.setVisibility(View.VISIBLE);
        }

        if (tie.getTitle() == null) {
            findViewById(R.id.tie_title).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.tie_title)).setText(tie.getTitle());
        }

        if (tie.getInfo() == null) {
            findViewById(R.id.tie_info).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.tie_info)).setText(tie.getInfo());
        }

        if (tie.getPoster_id().equals(account)) {
            TextView text = findViewById(R.id.subscription_bt);
            text.setText("xx阅读");
            text.setTextColor(Color.parseColor("#909399"));
            text.setBackgroundColor(Color.WHITE);
        }

        ((TextView) findViewById(R.id.name)).setText(tie.getPoster_name());
        ((TextView) findViewById(R.id.date)).setText(tie.getBa_name() + "吧 | " + tie.getDate());

        Level level = new Level(tie.getPoster_exp());
        TextInImageView level_icon = findViewById(R.id.level_icon);

        level_icon.setImageResource(level.getLevelIcon());
        level_icon.setText(level.getLevel());

        ImageTextButton1 share_bt = findViewById(R.id.share_bt);
        share_bt.setImageResource(R.mipmap.share);
        share_bt.setText("分享");

        ImageTextButton1 comment_bt = findViewById(R.id.comment_bt);
        comment_bt.setImageResource(R.mipmap.comment);
        comment_bt.setText((tie.getReply_count() > 0) ? String.valueOf(tie.getReply_count()) : "评论");

        setGoodBadButton();

        RecyclerView floor_list = findViewById(R.id.floor_list);
        floor_list.setLayoutManager(new LinearLayoutManager(this));

        //TODO:表情框还没弄

        FloorAdapter adapter = new FloorAdapter(floorData, this, account, tie.getPoster_id());
        floor_list.setAdapter(adapter);

    }

    private void getData() {
        t = new Thread(() -> {
            HttpUrl url1 = Objects.requireNonNull(HttpUrl.parse(Constants.GET_FLOOR_PATH)).newBuilder()
                    .addQueryParameter("tie", tie_id)
                    .addQueryParameter("account", account)
                    .addQueryParameter("condition", CONDITIONS[pos])
                    .addQueryParameter("order", floor_order)
                    .build();

            HttpUrl url2 = Objects.requireNonNull(HttpUrl.parse(Constants.GET_TIE_PATH)).newBuilder()
                    .addQueryParameter("id", tie_id)
                    .addQueryParameter("account", account)
                    .build();

            Type type_tie_list = new TypeToken<List<Floor>>() {
            }.getType();  //创建一个新类型

            Type tie_type = new TypeToken<Tie>() {
            }.getType();  //创建一个新类型

            try {
                floorData = new Gson().fromJson(BackstageInteractive.get(url1), type_tie_list);
                tie = new Gson().fromJson(BackstageInteractive.get(url2), tie_type);
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(this, "网络异常!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        t.start();
    }

    private void waitData() {
        try { //等待获取数据的线程完成
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.end_info)).setText(floorData.size() > 0 ? "暂无更多" : "偷偷告诉你，这还毛都没有T T");

        //设置返回值
        Intent intent = new Intent()
                .putExtra("tie", tie)
                .putExtra("position", getIntent().getIntExtra("position", -1))
                .putExtra("account", account);
        setResult(RESULT_OK, intent);
    }

    private void refreshData() {
        getData();
        waitData();

        initView();
    }

    private void setGoodBadButton() {
        good_bt.setImageResource(tie.getLikeIcon());
        bad_bt.setImageResource(tie.getBadIcon());
        good_bt.setText(String.valueOf(tie.getGood()));
        bad_bt.setText(String.valueOf(tie.getBad()));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.back) {
            finish();

        } else if (vid == R.id.share || vid == R.id.more || vid == R.id.subscription_bt) {
            Toast.makeText(this, "功能未实现!", Toast.LENGTH_SHORT).show();

        } else if (vid == R.id.good_bt) {
            if (account == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, LoginActivity.CODE);

            } else {
                tie.like();
                setGoodBadButton();
                BackstageInteractive.sendLike(account, tie.getId(), tie.getLiked(), Constants.TIE);
            }

        } else if (vid == R.id.bad_bt) {
            if (account == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, LoginActivity.CODE);

            } else {
                tie.unlike();
                setGoodBadButton();
                BackstageInteractive.sendLike(account, tie.getId(), tie.getLiked(), Constants.TIE);
            }

        } else if (vid == R.id.avatar || vid == R.id.name) {
            Intent intent = new Intent(this, UserInfoActivity.class);
            //传递账号，到那边再查询
            intent.putExtra("target", tie.getPoster_id());
            intent.putExtra("account", account);
            startActivity(intent);

        } else if (vid == R.id.tie_image) {
            ImagePreview.getInstance()
                    .setContext(this)  // 上下文，必须是activity，不需要担心内存泄漏，本框架已经处理好；
                    .setIndex(0)  // 设置从第几张开始看（索引从0开始）
                    .setImage(Constants.GET_IMAGE_PATH + tie.getImg())
                    .start();

        } else if (vid == R.id.reply_tie_bt || vid == R.id.comment_bt) {
            if (imgUri != null) {
                Intent intent;
                if (account == null) {
                    intent = new Intent(this, LoginActivity.class);
                    startActivityForResult(intent, LoginActivity.CODE);

                } else {
                    intent = new Intent(this, SendFloorActivity.class)
                            .putExtra("account", account)
                            .putExtra("tie", tie.getId())
                            .putExtra("info", reply_text.getText())
                            .putExtra("imgUri", imgUri.toString());

                    startActivityForResult(intent, SendFloorActivity.CODE);
                }
            } else {
                reply_tie_bt.setVisibility(View.GONE);
                //显示回复框
                reply_view.setVisibility(View.VISIBLE);
                //输入框获取焦点
                reply_text.requestFocus();
                //调起键盘
                inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }

        } else if (vid == R.id.floor_sort1 || vid == R.id.floor_sort2) {
            new AlertDialog.Builder(this).setItems(ITEMS, (dialog, which) -> {
                switch (which) {
                    case 0:
                        Toast.makeText(this, "暂未实现!", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        if (!"floor".equals(floor_order)) {
                            floor_order = "floor";
                            ((TextView) findViewById(R.id.floor_sort1)).setText("正序 v");
                            ((TextView) findViewById(R.id.floor_sort2)).setText("正序 v");
                            refreshData();
                        }
                        break;
                    case 2:
                        if (!"-floor".equals(floor_order)) {
                            floor_order = "-floor";
                            ((TextView) findViewById(R.id.floor_sort1)).setText("倒序 v");
                            ((TextView) findViewById(R.id.floor_sort2)).setText("倒序 v");
                            refreshData();
                        }
                        break;
                    default:
                }
            }).show();

        } else if (vid == R.id.end_info) {
            ((TextView) findViewById(R.id.end_info)).setText("加载中...");
            refreshData();

        } else if (vid == R.id.send_reply_bt) {
            if (account == null){
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, LoginActivity.CODE);

            } else {
                sendFloor();
            }

        } else if (vid == R.id.reply_image) {
            //跳转到获取图片的activity
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GET_IMAGE_EXIT_CODE);

        } else {
            Toast.makeText(this, "暂未完成!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendFloor() {
        String result = BackstageInteractive.sendFloor(tie.getId(), account, reply_text.getText().toString(), null);

        if ("succeed".equals(result)) {
            Toast.makeText(this, "发送成功，经验加三!", Toast.LENGTH_SHORT).show();
            reply_text.setText("");
            reply_view.setVisibility(View.GONE);
            reply_tie_bt.setVisibility(View.VISIBLE);

            if (inputManager.isActive()) {
                inputManager.hideSoftInputFromWindow(reply_text.getWindowToken(), 0);
            }

            refreshData();
        } else {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SendFloorActivity.CODE:
                if (resultCode == RESULT_OK) {
                    //刷新数据
                    refreshData();
                    //修改输入框
                    //TODO:最重要的经验加三需要在这里实现
                    Toast.makeText(this, "发送成功,经验加三", Toast.LENGTH_SHORT).show();
                    //清空输入框
                    imgUri = null;
                    reply_text.setText("");

                } else if (resultCode == SendFloorActivity.EXIT_CODE) {
                    assert data != null;
                    imgUri = Uri.parse(data.getStringExtra("imgUri"));
                    reply_text.setText(data.getStringExtra("info"));

                    reply_tie_bt.setVisibility(View.VISIBLE);
                    reply_view.setVisibility(View.GONE);
                }
                break;
            case LoginActivity.CODE:
            case UserInfoActivity.CODE:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    account = data.getStringExtra("account");
                    //刷新数据
                    refreshData();
                }
                break;
            case GET_IMAGE_EXIT_CODE:
                if (data != null) {
                    // 得到图片的全路径
                    imgUri = data.getData();
                    Intent intent = new Intent(this, SendFloorActivity.class)
                            .putExtra("imgUri", imgUri.toString())
                            .putExtra("info", reply_text.getText().toString());

                    startActivityForResult(intent, SendFloorActivity.CODE);
                }
                break;
            default:
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        TextView send_bt = findViewById(R.id.send_reply_bt);
        TextView text = findViewById(R.id.input_tips);

        if (!reply_text.getText().toString().equals("") || imgUri != null) {
            send_bt.setTextColor(Color.parseColor("#4096FF"));
            send_bt.setEnabled(true);

            text.setText("[草稿待发送]");

        } else {
            send_bt.setTextColor(Color.parseColor("#909399"));
            send_bt.setEnabled(false);

            text.setText("说说你的看法...");
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //防止内存泄露
        if (provider != null) {
            provider.dismiss();
        }
    }
}