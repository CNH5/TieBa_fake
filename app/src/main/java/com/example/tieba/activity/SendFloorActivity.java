package com.example.tieba.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.example.tieba.R;

public class SendFloorActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    public static final int CODE = 5;
    public static final int GET_IMAGE_EXIT_CODE = 6;
    public static final int EXIT_CODE = 8;
    private EditText info_et;
    private TextView send_bt;
    private ImageView image;
    private ImageView unselect_img_bt;
    private String account;
    private String tie_id;
    private Uri uri;
    private InputMethodManager inputManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_floor);

        initParams();

        initView();
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
        uri = Uri.parse(getIntent().getStringExtra("imgUri"));
        info_et.setText(getIntent().getStringExtra("info"));

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initView() {
        info_et.requestFocus();
        inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        image.setImageURI(uri);

        unselect_img_bt.setVisibility(View.VISIBLE);
        image.setBackgroundColor(Color.WHITE);
    }

    private void setResults() {
        Intent intent = new Intent()
                .putExtra("info", info_et.getText().toString())
                .putExtra("imgUri", uri.toString());
        setResult(EXIT_CODE, intent);
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
            setResults();
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
            uri = null;
            v.setVisibility(View.GONE);
            setSendButton();
        }
    }

    private void postFloor() {
        String result = BackstageInteractive.sendFloor(tie_id, account, info_et.getText().toString(), image);

        if ("succeed".equals(result)) {
            Toast.makeText(this, "发送成功!", Toast.LENGTH_SHORT).show();

            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IMAGE_EXIT_CODE) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                uri = data.getData();

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

    @Override
    public void onBackPressed() {
        setResults();
        super.onBackPressed();
    }
}