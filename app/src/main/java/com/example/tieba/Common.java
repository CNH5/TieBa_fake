package com.example.tieba;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.*;
import java.util.regex.Pattern;

/**
 * @author sheng
 * @date 2021/9/6 9:54
 */
public class Common {

    public static final Pattern ACCOUNT_PATTERN = Pattern.compile("([0-9]|[a-z]|[A-Z]|[_@#]){4,14}");
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("([0-9]|[a-z]|[A-Z]|[_@#]){4,14}");

    //输入框获取焦点时隐藏hint
    public static final View.OnFocusChangeListener mOnFocusHindHintListener = (v, hasFocus) -> {
        EditText textView = (EditText) v;
        String hint;
        if (hasFocus) {
            hint = textView.getHint().toString();
            textView.setTag(hint);
            textView.setHint("");
        } else {
            hint = textView.getTag().toString();
            textView.setHint(hint);
        }
    };

    //判断是否要收起键盘
    public static boolean isShouldHideKeyboard(View v, MotionEvent event) {
        //如果目前得到焦点的这个view是editText的话进行判断点击的位置
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            // 点击EditText的事件，忽略它。
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上
        return false;
    }

    //隐藏软键盘并让editText失去焦点
    public static void hideKeyboard(View v, InputMethodManager im) {
        IBinder token = v.getWindowToken();
        if (token != null && v instanceof EditText) {
            //让EditText失去焦点
            v.clearFocus();
            //这里先获取InputMethodManager再调用他的方法来关闭软键盘
            //InputMethodManager就是一个管理窗口输入的manager
            if (im != null) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static File getFile(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        File file = null;
        try {
            file = File.createTempFile("post_temp",".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            int x;
            byte[] b = new byte[1024 * 100];
            while ((x = is.read(b)) != -1) {
                fos.write(b, 0, x);
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
