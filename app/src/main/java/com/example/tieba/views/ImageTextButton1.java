package com.example.tieba.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.tieba.R;

/**
 * @author sheng
 * @date 2021/8/29 4:06
 */
public class ImageTextButton1 extends RelativeLayout {

    private ImageView imgView;
    private TextView  textView;

    public ImageTextButton1(Context context) {
        super(context,null);
    }

    public ImageTextButton1(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        LayoutInflater.from(context).inflate(R.layout.img_text_bt1, this,true);

        this.imgView = findViewById(R.id.img_view);
        this.textView = findViewById(R.id.text_view);

        this.setClickable(true);
        this.setFocusable(true);
    }

    public void setImageResource(int resourceID) {
        this.imgView.setImageResource(resourceID);
    }

    public void setText(String text) {
        this.textView.setText(text);
    }

}
