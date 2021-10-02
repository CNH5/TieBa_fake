package com.example.tieba;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;
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

    public static String sendFloor(String tie_id, String account, String info, ImageView image) {
        final String[] result = {null};

        Thread thread = new Thread(() -> {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("tie", tie_id)
                    .addFormDataPart("account", account)
                    .addFormDataPart("info", info);

            if (image != null && image.getDrawable() != null) {
                //直接获取imageview中的图片
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                //转文件
                RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), Common.getFile(bitmap));
                builder.addFormDataPart("img", "f.jpg", fileBody);
            }

            try {
                result[0] = BackstageInteractive.post(Constants.REPLY_TIE_PATH, builder.build());
            } catch (IOException e) {
                e.printStackTrace();
                result[0] = "网络异常";
            }
        });
        thread.start();

        try { //等待获取数据的线程完成,好像也可以在这里设置加载动画
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }
}
