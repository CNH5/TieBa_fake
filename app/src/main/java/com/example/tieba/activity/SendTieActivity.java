package com.example.tieba.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Common;
import com.example.tieba.Constants;
import com.example.tieba.R;
import okhttp3.*;
import okhttp3.MultipartBody.Builder;


public class SendTieActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    public static final int CODE = 2;
    public static final int GET_IMAGE_EXIT_CODE = 4;
    private EditText title_et;
    private EditText info_et;
    private TextView send_bt;
    private ImageView image;
    private ImageView unselect_img_bt;
    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_tie);

        getAccount();
        initView();
        loadDraft();
    }

    /**
     * 加载草稿
     */
    private void loadDraft() {
        String spFileName = getResources().getString(R.string.tie_draft_filename);
        String draftTitleKey = getResources().getString(R.string.tie_draft_title);
        String draftInfoKey = getResources().getString(R.string.tie_draft_info);

        SharedPreferences spFile = getSharedPreferences(spFileName, MODE_PRIVATE);
        title_et.setText(spFile.getString(draftTitleKey, null));
        info_et.setText(spFile.getString(draftInfoKey, null));
    }


    private void saveDraft(String title, String info) {
        //保存草稿
        String spFileName = getResources().getString(R.string.tie_draft_filename);
        String draftTitleKey = getResources().getString(R.string.tie_draft_title);
        String draftInfoKey = getResources().getString(R.string.tie_draft_info);

        SharedPreferences spFile = getSharedPreferences(spFileName, MODE_PRIVATE);

        SharedPreferences.Editor editor = spFile.edit();

        editor.putString(draftTitleKey, title);
        editor.putString(draftInfoKey, info);

        editor.apply();
    }

    private void resetDraft() {
        title_et.setText("");
        info_et.setText("");
    }


    /**
     * 获取发帖的账号
     */
    private void getAccount() {
        Intent intent = getIntent();
        account = intent.getStringExtra(MainActivity.ACCOUNT_VALUE);

        //界面被错误调用
        if (account == null) {
            finish();
        }
    }

    private void initView() {
        ((TextView) findViewById(R.id.send_to_ba_tv)).setText("发布到滑稽吧");

        send_bt = findViewById(R.id.send_tie_bt);
        title_et = findViewById(R.id.title_et);
        info_et = findViewById(R.id.info_et);

        title_et.addTextChangedListener(this);
        info_et.addTextChangedListener(this);

        //正文editText获取焦点
        info_et.requestFocus();
        //调起键盘
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.send_tie_bt).setOnClickListener(this);

        //设置删除图片按钮
        unselect_img_bt = findViewById(R.id.unselect_img_bt);
        unselect_img_bt.setOnClickListener(this);
        image = findViewById(R.id.image);
        image.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();

        } else if (v.getId() == R.id.send_tie_bt) {
            postTie();

        } else if (v.getId() == R.id.image) {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GET_IMAGE_EXIT_CODE);

        } else if (v.getId() == R.id.unselect_img_bt) {
            image.setBackgroundResource(R.mipmap.add_border);
            image.setImageURI(null);
            v.setVisibility(View.GONE);
            //标题和图片至少要有一个
            if (title_et.getText().toString().equals("") && info_et.getText().toString().equals("")) {
                send_bt.setTextColor(Color.parseColor("#909399"));
                send_bt.setEnabled(false);
            }

        }
    }

    private void postTie() {
        final String[] result = {null};

        Thread thread = new Thread(() -> {
            Builder builder = new Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("account", account)
                    .addFormDataPart("ba", "1")
                    .addFormDataPart("title", title_et.getText().toString())
                    .addFormDataPart("info", info_et.getText().toString());

            if (image.getDrawable() != null) {
                //直接获取imageview中的图片
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                //转文件
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), Common.getFile(bitmap));
                builder.addFormDataPart("img", "t.jpg", fileBody);
            }

            try {
                result[0] = BackstageInteractive.post(Constants.APPEND_TIE_PATH, builder.build());
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
            Toast.makeText(this, "发送成功!", Toast.LENGTH_SHORT).show();
            resetDraft();

            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "发送失败!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!title_et.getText().toString().equals("")
                || image.getDrawable() != null
                || !info_et.getText().toString().equals("")) {

            send_bt.setTextColor(Color.parseColor("#4096FF"));
            send_bt.setEnabled(true);
        } else {
            send_bt.setTextColor(Color.parseColor("#909399"));
            send_bt.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //保存草稿
        saveDraft(title_et.getText().toString(), info_et.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_IMAGE_EXIT_CODE) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                //TODO: 需要把图片path保存到editor中，下次打开时再加载进来
                image.setImageURI(uri);
                unselect_img_bt.setVisibility(View.VISIBLE);
                image.setBackgroundColor(Color.WHITE);

                send_bt.setTextColor(Color.parseColor("#4096FF"));
                send_bt.setEnabled(true);
            }
        }
    }
}