package com.example.tieba.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Common;
import com.example.tieba.Constants;
import com.example.tieba.R;
import com.example.tieba.beans.User;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.example.tieba.Common.hideKeyboard;
import static com.example.tieba.Common.isShouldHideKeyboard;

public class ChangeUserInfoActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    public static final int CODE = 7;
    public static final int GET_IMAGE_EXIT_CODE = 8;
    private EditText name_et;
    private EditText intro_et;
    private TextView save_bt;
    private ImageView avatar;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_info);

        setUser();
        initView();
    }

    //时间分发方法重写
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //如果是点击事件，获取点击的view，并判断是否要收起键盘
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //获取目前得到焦点的view
            View v = getCurrentFocus();
            //判断是否要收起并进行处理
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v, (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
            }
        }
        //这个是activity的事件分发，一定要有，不然就不会有任何的点击事件了
        return super.dispatchTouchEvent(ev);
    }

    private void setUser() {
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            finish();
        }
    }

    private void initView() {
        name_et = findViewById(R.id.name);
        intro_et = findViewById(R.id.introduction);

        name_et.setText(user.getName());
        intro_et.setText(user.getIntroduction());

        name_et.addTextChangedListener(this);
        intro_et.addTextChangedListener(this);

        save_bt = findViewById(R.id.save);
        save_bt.setOnClickListener(this);

        avatar = findViewById(R.id.avatar);
        avatar.setOnClickListener(this);

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.xx).setOnClickListener(this);
        findViewById(R.id.yy).setOnClickListener(this);
        findViewById(R.id.sex_bt).setOnClickListener(this);

        ((TextView) findViewById(R.id.sex)).setText(user.getSex());
        ((TextView) findViewById(R.id.account)).setText(user.getAccount());

        ImageView avatar = findViewById(R.id.avatar);

        Glide.with(this).load(Constants.GET_IMAGE_PATH + user.getAvatar()).into(avatar);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //TODO: 显示字数和字数上限
        user.setName(String.valueOf(name_et.getText()));
        user.setIntroduction(String.valueOf(intro_et.getText()));

        save_bt.setEnabled(!"".equals(String.valueOf(name_et.getText())));
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();

        } else if (v.getId() == R.id.save) {

            final String[] result = {null};

            Thread thread = new Thread(() -> {
                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("account", user.getAccount())
                        .addFormDataPart("name", user.getName())
                        .addFormDataPart("sex", user.getSex())
                        .addFormDataPart("introduction", user.getIntroduction());

                try {
                    result[0] = BackstageInteractive.post(Constants.CHANGE_USER_INFO_PATH, builder.build());
                } catch (Exception e) {
                    Looper.prepare();
                    Toast.makeText(this, "网络异常!", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            });
            thread.start();

            try { //等待获取数据的线程完成,好像也可以在这里设置加载动画
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if ("succeed".equals(result[0])) {
                Toast.makeText(this, "修改成功!", Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "修改失败!", Toast.LENGTH_SHORT).show();
            }

        } else if (v.getId() == R.id.sex_bt) {
            final String[] items = {"男", "女", "未知"};
            new AlertDialog.Builder(this)
                    .setTitle("选择性别")
                    .setItems(items, (dialog, which) -> {
                        user.setSex(items[which]);
                        ((TextView) findViewById(R.id.sex)).setText(items[which]);
                    })
                    .show();

        } else if (v.getId() == R.id.yy || v.getId() == R.id.xx || v.getId() == R.id.avatar) {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GET_IMAGE_EXIT_CODE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_IMAGE_EXIT_CODE) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                avatar.setImageURI(uri);
                //直接上传头像

                final String[] result = {null};
                Thread thread = new Thread(() -> {
                    MultipartBody.Builder builder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("account", user.getAccount());

                    if (avatar.getDrawable() != null) {
                        //直接获取imageview中的图片
                        Bitmap bitmap = ((BitmapDrawable) avatar.getDrawable()).getBitmap();
                        //转文件
                        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), Common.getFile(bitmap));
                        builder.addFormDataPart("img", "a.jpg", fileBody);
                    }

                    try {
                        result[0] = BackstageInteractive.post(Constants.CHANGE_AVATAR_PATH, builder.build());
                    } catch (Exception e) {
                        Looper.prepare();
                        Toast.makeText(this, "网络异常!", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });
                thread.start();

                try { //等待获取数据的线程完成,好像也可以在这里设置加载动画
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if ("succeed".equals(result[0])) {
                    Toast.makeText(this, "修改成功!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "修改失败!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}