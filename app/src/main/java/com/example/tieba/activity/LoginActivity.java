package com.example.tieba.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import okhttp3.HttpUrl;

import java.util.Objects;

import static com.example.tieba.Common.*;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    public static final int CODE = 3;
    private boolean password_visible = false;
    private EditText password_et;
    private EditText account_et;
    private Button login_bt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initParams();
        setItemOnClickListener();
        setAccount();
    }


    private void initParams() {
        account_et = findViewById(R.id.account_et);
        password_et = findViewById(R.id.password_et);
        login_bt = findViewById(R.id.login_bt);

        //
        account_et.addTextChangedListener(this);
        password_et.addTextChangedListener(this);

        account_et.setOnFocusChangeListener(mOnFocusHindHintListener);
        password_et.setOnFocusChangeListener(mOnFocusHindHintListener);
    }


    private void setItemOnClickListener() {
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.login_bt).setOnClickListener(this);
        findViewById(R.id.register_bt).setOnClickListener(this);
        findViewById(R.id.forget_password_bt).setOnClickListener(this);
        findViewById(R.id.password_visible_switch).setOnClickListener(this);
    }


    //????????????????????????
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //???????????????????????????????????????view?????????????????????????????????
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //???????????????????????????view
            View v = getCurrentFocus();
            //????????????????????????????????????
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v, (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
            }
        }
        //?????????activity???????????????????????????????????????????????????????????????????????????
        return super.dispatchTouchEvent(ev);
    }

    /**
     * ???????????????????????????
     */
    private void loginButtonClicked() {
        new Thread(() -> {
            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Constants.LOGIN_PATH)).newBuilder()
                    .addQueryParameter("account", account_et.getText().toString())
                    .addQueryParameter("password", password_et.getText().toString())
                    .build();

            try {
                String result = BackstageInteractive.get(url);

                if ("????????????".equals(result)) {
                    //????????????,?????????????????????,??????????????????,?????????????????????
                    rememberAccount();

                    Intent intent = new Intent();
                    intent.putExtra("account", account_et.getText().toString());

                    setResult(RESULT_OK, intent);

                    finish();
                }
                Looper.prepare();
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                Looper.loop();
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(this, "????????????!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    private void rememberAccount() {
        String spFileName = getResources().getString(R.string.shared_preferences_file_name);
        String accountKey = getResources().getString(R.string.account);
        String wasLoginKey = getResources().getString(R.string.was_login);

        SharedPreferences spFile = getSharedPreferences(spFileName, MODE_PRIVATE);

        SharedPreferences.Editor editor = spFile.edit();
        editor.putString(accountKey, account_et.getText().toString());
        editor.putBoolean(wasLoginKey, true);
        editor.apply();
    }

    private void setAccount() {
        String spFileName = getResources().getString(R.string.shared_preferences_file_name);
        String accountKey = getResources().getString(R.string.account);

        SharedPreferences spFile = getSharedPreferences(spFileName, MODE_PRIVATE);
        String account = spFile.getString(accountKey, null);

        if (account != null) {
            account_et.setText(account);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            //????????????????????????
            finish();
        } else if (v.getId() == R.id.login_bt) {
            //????????????
            loginButtonClicked();
        } else if (v.getId() == R.id.register_bt) {
            //????????????????????????????????????
            startActivity(new Intent(this, RegisterActivity.class));
        } else if (v.getId() == R.id.forget_password_bt) {
            //???????????????????????????????????????...
            Toast.makeText(this, "???????????????!", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.password_visible_switch) {
            //????????????????????????
            ImageView icon = findViewById(R.id.password_visible_switch);

            if (password_visible = !password_visible) {
                icon.setImageResource(R.mipmap.eye);
                password_et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                icon.setImageResource(R.mipmap.eye_close);
                password_et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
                password_et.setTypeface(Typeface.DEFAULT);
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //TODO: ??????????????????????????????????????????????????????EditText
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (accountPasswordAvailable()) {
            login_bt.setEnabled(true);
            login_bt.setBackground(getDrawable(R.drawable.shape_corner4));
        } else {
            login_bt.setEnabled(false);
            login_bt.setBackground(getDrawable(R.drawable.shape_corner3));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private boolean accountPasswordAvailable() {
        return ACCOUNT_PATTERN.matcher(account_et.getText()).matches()
                && PASSWORD_PATTERN.matcher(password_et.getText()).matches();
    }
}