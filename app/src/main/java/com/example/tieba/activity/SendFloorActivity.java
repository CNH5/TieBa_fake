package com.example.tieba.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class SendFloorActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    public static final int CODE = 5;
    public static final int GET_IMAGE_EXIT_CODE = 6;
    private EditText info_et;
    private TextView send_bt;
    private ImageView image;
    private ImageView unselect_img_bt;
    private String account;
    private String tie_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_floor);

        initParams();
    }

    private void initParams() {
        info_et = findViewById(R.id.info_et);
        send_bt = findViewById(R.id.send_floor_bt);
        image = findViewById(R.id.image);
        unselect_img_bt = findViewById(R.id.unselect_img_bt);

        info_et.addTextChangedListener(this);
        send_bt.setOnClickListener(this);
        image.setOnClickListener(this);
        unselect_img_bt.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);

        account = getIntent().getStringExtra("account");
        tie_id = getIntent().getStringExtra("tie");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        setSendButton();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();

        } else if (v.getId() == R.id.send_floor_bt) {
            postFloor();

        } else if (v.getId() == R.id.image) {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, GET_IMAGE_EXIT_CODE);

        } else if (v.getId() == R.id.unselect_img_bt) {
            image.setBackgroundResource(R.mipmap.add_border);
            image.setImageURI(null);
            v.setVisibility(View.GONE);
            setSendButton();
        }
    }

    private void postFloor() {
        final String[] result = {null};

        Thread thread = new Thread(() -> {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("tie", tie_id)
                    .addFormDataPart("account", account)
                    .addFormDataPart("info", info_et.getText().toString());

            if (image.getDrawable() != null) {
                //直接获取imageview中的图片
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                //转文件
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), Common.getFile(bitmap));
                builder.addFormDataPart("img", "f.jpg", fileBody);
            }

            try {
                result[0] = BackstageInteractive.post(Constants.REPLY_TIE_PATH, builder.build());
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

            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "发送失败!", Toast.LENGTH_SHORT).show();
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
                //TODO: 需要把图片path保存到editor中，下次打开时再加载进来
                image.setImageURI(uri);
                unselect_img_bt.setVisibility(View.VISIBLE);
                image.setBackgroundColor(Color.WHITE);

                setSendButton();
            }
        }
    }

    private void setSendButton() {
        //文字和图片至少要有一个
        if (image.getDrawable() != null || !info_et.getText().toString().equals("")) {
            send_bt.setTextColor(Color.parseColor("#4096FF"));
            send_bt.setEnabled(true);
        } else {
            send_bt.setTextColor(Color.parseColor("#909399"));
            send_bt.setEnabled(false);
        }
    }
}