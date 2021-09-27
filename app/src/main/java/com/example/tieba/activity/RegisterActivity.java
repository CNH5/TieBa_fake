package com.example.tieba.activity;

import android.content.Context;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import static com.example.tieba.Common.*;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private EditText account_et;
    private EditText password_et;
    private Button register_bt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initParams();
    }


    private void initParams() {
        account_et = findViewById(R.id.account_et);
        password_et = findViewById(R.id.password_et);
        register_bt = findViewById(R.id.register_bt);

        account_et.setOnFocusChangeListener(mOnFocusHindHintListener);
        password_et.setOnFocusChangeListener(mOnFocusHindHintListener);

        account_et.addTextChangedListener(this);
        password_et.addTextChangedListener(this);

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.register_bt).setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        } else if (v.getId() == R.id.register_bt) {
            registerButtonClicked();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (accountPasswordAvailable()) {
            register_bt.setEnabled(true);
            register_bt.setBackground(getDrawable(R.drawable.shape_corner4));
        } else {
            register_bt.setEnabled(false);
            register_bt.setBackground(getDrawable(R.drawable.shape_corner3));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        //TODO: 在这里添加提示，提示用户应该修改哪些EditText
    }

    //判断账号密码是否可用
    private boolean accountPasswordAvailable() {
        return ACCOUNT_PATTERN.matcher(account_et.getText()).matches()
                && PASSWORD_PATTERN.matcher(password_et.getText()).matches();
    }

    private void registerButtonClicked() {
        new Thread(() -> {
            RequestBody body = new FormBody.Builder()
                    .add("account", account_et.getText().toString())
                    .add("password", password_et.getText().toString())
                    .build();

            try {
                String result = BackstageInteractive.post(Constants.REGISTER_PATH, body);

                Looper.prepare();
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                Looper.loop();

                if ("注册成功".equals(result)) {
                    //TODO: 应该用intent传输账号给LoginActivity，在那边再输一次密码
                    finish();
                }
            } catch (Exception e) {
                Looper.prepare();
                // TODO: 还可以在顶部toolbar下面加一个黄色的通知栏，提示网络异常,登录页面那边同理
                Toast.makeText(this, "网络异常", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

}