package com.example.tieba.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.tieba.R;

/**
 * @author sheng
 * @date 2021/9/11 14:11
 */
public class TextInImageView extends ConstraintLayout {
    private ImageView imgView;
    private TextView textView;

    public TextInImageView(Context context) {
        super(context);
    }

    public TextInImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.text_in_img_view, this,true);

        this.imgView = findViewById(R.id.image);
        this.textView = findViewById(R.id.text);

        this.setClickable(true);
        this.setFocusable(true);
    }

    public TextInImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageResource(int resourceID) {
        this.imgView.setImageResource(resourceID);
    }

    public void setText(String text) {
        this.textView.setText(text);
    }
}
