package com.example.tieba.popupWindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.shinichi.library.ImagePreview;
import com.bumptech.glide.Glide;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import com.example.tieba.activity.LoginActivity;
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
import okhttp3.MultipartBody;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * @author sheng
 * @date 2021/9/27 16:47
 */
public class ReplyFloorPopupWindow extends PopupWindow implements View.OnClickListener, TextWatcher {
    private final Context mContext;
    private InputMethodManager inputManager;
    private final View v;
    private ImageTextButton1 good_bt;
    private ImageTextButton1 bad_bt;
    private RecyclerView reply_list;
    private TextView end_text;
    private EditText reply_text;
    private View call_reply_view_bt;
    private View reply_view;
    private Thread getDataThread;
    private List<Reply> list;
    private final Floor floor;
    private final String tie_poster_id;
    private final String account;
    private HeightProvider provider;

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
        //??????View
        setContentView(v);

        //???????????????
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        int screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        setHeight(Math.round(screenHeight * 0.89f));

        // ???????????????????????????
        setFocusable(true);

        // ????????????????????????????????????????????????????????????
        this.setAnimationStyle(R.style.popWindow_anim);

        setOutsideTouchable(true);

        //???????????????ColorDrawable???????????????(????????????0xb0000000)
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //??????SelectPicPopupWindow?????????????????????
        this.setBackgroundDrawable(dw);
    }

    private void initList() {
        getData();
        waitData();
        ReplyAdapter adapter = new ReplyAdapter(list, mContext, account, tie_poster_id);

        adapter.itemClick(() -> {
            reply_view.setVisibility(View.VISIBLE);
            call_reply_view_bt.setVisibility(View.GONE);
            return reply_text;
        });

        reply_list.setAdapter(adapter);
        initEndText();
    }

    private void getData() {
        getDataThread = new Thread(() -> {
            HttpUrl get_tie_list_url = Objects.requireNonNull(HttpUrl.parse(Constants.GET_REPLY_PATH)).newBuilder()
                    .addQueryParameter("account", account)
                    .addQueryParameter("floor", floor.getId())
                    .build();

            Type type_list = new TypeToken<List<Reply>>() {
            }.getType();  //?????????????????????
            try {
                list = new Gson().fromJson(
                        BackstageInteractive.get(get_tie_list_url),
                        type_list
                );
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(mContext, "????????????!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        getDataThread.start();

        end_text.setText("????????????...");
    }

    public void init() {
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
        inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        good_bt = v.findViewById(R.id.good_bt);
        bad_bt = v.findViewById(R.id.bad_bt);
        call_reply_view_bt = v.findViewById(R.id.call_reply_view_bt);

        call_reply_view_bt.setOnClickListener(this);

        good_bt.setOnClickListener(this);
        bad_bt.setOnClickListener(this);

        reply_list = v.findViewById(R.id.reply_floor_list);
        reply_list.setLayoutManager(new LinearLayoutManager(mContext));

        v.findViewById(R.id.close).setOnClickListener(this);
        v.findViewById(R.id.name).setOnClickListener(this);
        v.findViewById(R.id.send_reply_bt).setOnClickListener(this);
        v.findViewById(R.id.emoji).setOnClickListener(this);
        v.findViewById(R.id.microphone).setOnClickListener(this);
        v.findViewById(R.id.at).setOnClickListener(this);

        reply_text = v.findViewById(R.id.reply_text);
        reply_text.setFocusable(true);
        reply_text.setFocusableInTouchMode(true);
        reply_text.addTextChangedListener(this);
        reply_view = v.findViewById(R.id.reply_view);

        provider = new HeightProvider((Activity) mContext).setHeightListener(height -> {
            float h = height == 0 ? 0 : mContext.getResources().getDisplayMetrics().heightPixels * 0.08f;
            reply_view.setTranslationY(-height + h);
        });
        provider.init();

        v.findViewById(R.id.scroll_item).setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY != oldScrollY) {
                //TODO:???????????????????????????
                if (inputManager.isActive()) {
                    inputManager.hideSoftInputFromWindow(reply_text.getWindowToken(), 0);
                }

                reply_text.clearFocus();
                call_reply_view_bt.setVisibility(View.VISIBLE);
                reply_view.setVisibility(View.GONE);
            }
        });

        TextInImageView level_icon = v.findViewById(R.id.level_icon);
        Level level = new Level(floor.getPoster_exp());

        level_icon.setImageResource(level.getLevelIcon());
        level_icon.setText(level.getLevel());

        ((TextView) v.findViewById(R.id.name)).setText(floor.getPoster_name());
        ((TextView) v.findViewById(R.id.floor_num)).setText(String.format("%s????????????", floor.getFloor()));
        ((TextView) v.findViewById(R.id.reply_count)).setText(String.format("%d?????????", floor.getReply_count()));
        ((TextView) v.findViewById(R.id.date)).setText(String.format("???%s??? | %s", floor.getFloor(), floor.getDate()));

        ImageView avatar = v.findViewById(R.id.avatar);
        avatar.setOnClickListener(this);
        Glide.with(mContext).load(Constants.GET_IMAGE_PATH + floor.getPoster_avatar()).into(avatar);

        end_text = v.findViewById(R.id.end_text);
        end_text.setOnClickListener(this);


        TextView ident = v.findViewById(R.id.ident);
        if (tie_poster_id.equals(floor.getPoster_id())) {
            ident.setText("??????");
            ident.setVisibility(View.VISIBLE);
        }

        //????????????
        if (floor.getImg() != null) {
            ImageView floor_image = v.findViewById(R.id.floor_image);
            floor_image.setOnClickListener(this);
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
        bad_bt.setText((floor.getBad() != 0) ? String.valueOf(floor.getBad()) : "???");
        good_bt.setText((floor.getGood() != 0) ? String.valueOf(floor.getGood()) : "???");
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.close) {
            //??????
            dismiss();

        } else if (vid == R.id.emoji || vid == R.id.at || vid == R.id.microphone) {
            Toast.makeText(mContext, "????????????~", Toast.LENGTH_SHORT).show();

        } else if (vid == R.id.good_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);

            } else {
                floor.like();
                setGoodBadButton();
                BackstageInteractive.sendLike(account, floor.getId(), floor.getLiked(), Constants.FLOOR);
            }

        } else if (vid == R.id.bad_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);

            } else {
                floor.unlike();
                setGoodBadButton();
                BackstageInteractive.sendLike(account, floor.getId(), floor.getLiked(), Constants.FLOOR);
            }

        } else if (vid == R.id.floor_image) {
            ImagePreview.getInstance()
                    .setContext(mContext)  // ?????????????????????activity????????????????????????????????????????????????????????????
                    .setIndex(0)  // ???????????????????????????????????????0?????????
                    .setImage(Constants.GET_IMAGE_PATH + floor.getImg())
                    .start();

        } else if (vid == R.id.avatar || vid == R.id.name) {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            //?????????????????????????????????
            intent.putExtra("target", floor.getPoster_id());
            intent.putExtra("account", account);
            mContext.startActivity(intent);

        } else if (vid == R.id.send_reply_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);

            } else {
                postReply();
            }

        } else if (vid == R.id.end_text) {
            initList();

        } else if (vid == R.id.call_reply_view_bt) {
            call_reply_view_bt.setVisibility(View.GONE);
            reply_view.setVisibility(View.VISIBLE);
            reply_text.requestFocus();

            inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void initEndText() {
        end_text.setText((floor.getReply_count() == 0) ? "????????????????????????~" : "??????????????????");
    }

    @SuppressLint("DefaultLocale")
    private void postReply() {
        final String[] result = {null};

        Thread thread = new Thread(() -> {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("account", account)
                    .addFormDataPart("floor", floor.getId())
                    .addFormDataPart("info", reply_text.getText().toString());

            try {
                result[0] = BackstageInteractive.post(Constants.FLOOR_REPLY_PATH, builder.build());
            } catch (Exception e) {
                Log.d("err", e.toString());
            }
        });
        thread.start();

        try { //?????????????????????????????????,??????????????????????????????????????????
            thread.join();
        } catch (InterruptedException e) {
            Toast.makeText(mContext, "????????????!", Toast.LENGTH_SHORT).show();
        }

        if ("succeed".equals(result[0])) {
            initList();

            floor.setReply_count(list.size());
            ((TextView) v.findViewById(R.id.reply_count)).setText(String.format("%d?????????", floor.getReply_count()));
            reply_text.setText("");
            reply_text.clearFocus();

            if (inputManager.isActive()) {
                inputManager.hideSoftInputFromWindow(reply_text.getWindowToken(), 0);
            }
            reply_view.setVisibility(View.GONE);
            call_reply_view_bt.setVisibility(View.VISIBLE);

            Toast.makeText(mContext, "???????????????????????????", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(mContext, "????????????!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        TextView send_bt = v.findViewById(R.id.send_reply_bt);
        TextView text = v.findViewById(R.id.input_tips);

        if (!reply_text.getText().toString().equals("")) {
            send_bt.setTextColor(Color.parseColor("#4096FF"));
            send_bt.setEnabled(true);

            text.setText("[???????????????]");

        } else {
            send_bt.setTextColor(Color.parseColor("#909399"));
            send_bt.setEnabled(false);

            text.setText("??????????????????...");
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void dismiss() {
        super.dismiss();

        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.alpha = 1.0f;
        ((Activity) mContext).getWindow().setAttributes(lp);
        provider.dismiss();
    }
}
