package com.example.tieba.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
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

public class TieActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int CODE = 1;
    private static final String[] TAB_NAMES = new String[]{"全部回复", "只看楼主"};
    private static final String[] ITEMS = {"热门排序", "正序排序", "倒序排序"};
    private static final String[] CONDITIONS = {"all", "only_tie_poster"};
    private String floor_order = "floor";  //楼层的排列顺序
    private int pos = 0;  //筛选条件，只看楼主和全部
    private Tie tie;
    private String account;
    private Thread t;
    private List<Floor> floorData;
    private RecyclerView floor_list;
    private ImageTextButton1 good_bt;
    private ImageTextButton1 bad_bt;

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
        tie = (Tie) getIntent().getSerializableExtra("tie");
        account = getIntent().getStringExtra("account");

        Intent intent = new Intent()
                .putExtra("tie", tie)
                .putExtra("position", getIntent().getIntExtra("position", -1))
                .putExtra("account", account);
        setResult(RESULT_OK, intent);

        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.blue);

        swipe.setOnRefreshListener(() -> {
            refreshData();
            swipe.setRefreshing(false);
        });

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

    private void setItemOnClickListener() {
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.more).setOnClickListener(this);
        findViewById(R.id.share_bt).setOnClickListener(this);
        findViewById(R.id.comment_bt).setOnClickListener(this);
        findViewById(R.id.reply_tie_bt).setOnClickListener(this);
        findViewById(R.id.floor_sort1).setOnClickListener(this);
        findViewById(R.id.floor_sort2).setOnClickListener(this);
        findViewById(R.id.avatar).setOnClickListener(this);
        findViewById(R.id.name).setOnClickListener(this);
        findViewById(R.id.subscription_bt).setOnClickListener(this);

        good_bt = findViewById(R.id.good_bt);
        bad_bt = findViewById(R.id.bad_bt);

        good_bt.setOnClickListener(this);
        bad_bt.setOnClickListener(this);
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
        ImageView avatar = findViewById(R.id.avatar);

        if (tie.getPoster_avatar() == null) {
            avatar.setImageResource(R.mipmap.null_user_avatar);
        } else {
            Glide.with(this).load(Constants.GET_IMAGE_PATH + tie.getPoster_avatar()).into(avatar);
        }

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

        floor_list = findViewById(R.id.floor_list);
        floor_list.setLayoutManager(new LinearLayoutManager(this));

        //TODO:表情框还没弄

        waitData();

        FloorAdapter adapter = new FloorAdapter(floorData, this, account, tie.getPoster_id());
        floor_list.setAdapter(adapter);

        setBottomText();
    }

    private void setBottomText() {
        ((TextView) findViewById(R.id.end_info)).setText(floorData.size() > 0 ? "暂无更多" : "偷偷告诉你，这还毛都没有T T");
    }

    private void getData() {
        t = new Thread(() -> {
            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Constants.GET_FLOOR_PATH)).newBuilder()
                    .addQueryParameter("tie", tie.getId())
                    .addQueryParameter("account", account)
                    .addQueryParameter("condition", CONDITIONS[pos])
                    .addQueryParameter("order", floor_order)
                    .build();

            Type type_tie_list = new TypeToken<List<Floor>>() {
            }.getType();  //创建一个新类型

            try {
                floorData = new Gson().fromJson(
                        BackstageInteractive.get(url),
                        type_tie_list
                );
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
    }

    private void refreshData() {
        getData();
        waitData();
        FloorAdapter adapter = new FloorAdapter(floorData, this, account, tie.getPoster_id());
        floor_list.setAdapter(adapter);

        setBottomText();
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
            Intent intent;
            if (account == null) {
                intent = new Intent(this, LoginActivity.class);

            } else {
                intent = new Intent(this, SendFloorActivity.class);
                intent.putExtra("account", account);
                intent.putExtra("tie", tie.getId());
            }

            startActivityForResult(intent, SendFloorActivity.CODE);

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
                    //TODO:最重要的经验加三需要在这里实现
                }
                break;
            case LoginActivity.CODE:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    account = data.getStringExtra("account");
                    //刷新数据
                    refreshData();
                    //TODO:最重要的经验加三需要在这里实现
                }
                break;
            default:
        }
    }
}