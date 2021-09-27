package com.example.tieba;

import android.util.Log;
import okhttp3.*;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * @author sheng
 * @date 2021/9/4 2:11
 */
public class BackstageInteractive {

    public static String get(HttpUrl url) throws Exception {
        Request request = new Request.Builder().url(url).get().build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {

                assert response.body() != null;
                String result = response.body().string();
                Log.d("get", result);

                return result;
            }
        } catch (Exception e) {
            Log.d("fail", "请求出错！");
            throw e;
        }
        return null;
    }

    public static String post(String url, RequestBody body) throws IOException {
        try {
            Request request = new Request.Builder().url(url).post(body).build();
            try (Response response = new OkHttpClient().newCall(request).execute()) {

                assert response.body() != null;
                String result = response.body().string();

                Log.d("get", result);
                return result;
            }
        } catch (Exception e) {
            Log.d(TAG, "post: post请求失败:" + e);
            throw e;
        }
    }

    //发送点赞信息
    public static void sendLike(String account, String id, String type, String target_type) {
        new Thread(() -> {
            RequestBody body = new FormBody.Builder()
                    .add("poster", account)
                    .add("id", id)
                    .add("type", type == null ? "" : type)
                    .add("target_type", target_type)
                    .build();
            try {
                BackstageInteractive.post(Constants.LIKE_PATH, body);
                //点赞成功只有动画，不会搞
            } catch (Exception ignored) {
                //贴吧断网点赞都没效果,不管了
            }
        }).start();
    }
}
