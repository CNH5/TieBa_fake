package com.example.tieba.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.shinichi.library.ImagePreview;
import com.bumptech.glide.Glide;
import com.example.tieba.*;
import com.example.tieba.activity.LoginActivity;
import com.example.tieba.activity.TieActivity;
import com.example.tieba.beans.Tie;
import com.example.tieba.views.ImageTextButton1;

import java.util.List;

/**
 * @author sheng
 * @date 2021/9/19 10:21
 */
public class HistoryTieAdapter extends RecyclerView.Adapter<HistoryTieAdapter.ViewHolder> implements View.OnClickListener {
    private static final String[] numbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    private final List<Tie> tieList;
    private final Context mContext;
    private final String account;

    public HistoryTieAdapter(Context context, List<Tie> data, String account) {
        this.mContext = context;
        this.tieList = data;
        this.account = account;
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.share_bt) {
            Toast.makeText(mContext, "功能未实现！", Toast.LENGTH_SHORT).show();

        } else if (vid == R.id.image) {
            // TODO: 点击图片后应当进入全屏浏览状态
            Tie tie = tieList.get((int) v.getTag());

            ImagePreview.getInstance()
                    .setContext(mContext)  // 上下文，必须是activity，不需要担心内存泄漏，本框架已经处理好；
                    .setIndex(0)  // 设置从第几张开始看（索引从0开始）
                    .setImage(Constants.GET_IMAGE_PATH + tie.getImg())
                    .start();

        } else if (v.getId() == R.id.good_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);

            } else  {
                int position = (int) v.getTag();
                Tie tie = tieList.get(position);
                tie.like();
                notifyItemChanged(position, "change_like_bt");
                BackstageInteractive.sendLike(account, tie.getId(), tie.getLiked(), Constants.TIE);
            }

        } else if (v.getId() == R.id.bad_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);

            }else {
                int position = (int) v.getTag();
                Tie tie = tieList.get(position);
                tie.unlike();
                notifyItemChanged(position, "change_like_bt");
                BackstageInteractive.sendLike(account, tie.getId(), tie.getLiked(), Constants.TIE);
            }


        } else {
            Intent intent = new Intent(mContext, TieActivity.class);
            intent.putExtra("tie_id", tieList.get((int) v.getTag()).getId());
            intent.putExtra("account", account);
            intent.putExtra("position", (int) v.getTag());
            ((Activity) mContext).startActivityForResult(intent, TieActivity.CODE);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryTieAdapter.ViewHolder(
                LayoutInflater.from(mContext)
                        .inflate(R.layout.history_tie_item, parent, false));
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tie tie = tieList.get(position);
        holder.ba_name.setText(tie.getBa_name() + "吧");

        if (tie.getTitle() == null && tie.getInfo() == null) {
            holder.title.setVisibility(View.GONE);
        } else if (tie.getTitle() == null && tie.getInfo() != null) {
            holder.title.setText(tie.getInfo().substring(0, 39) + "...");
        } else {
            holder.title.setText(tie.getTitle());
        }

        //加载图片
        if (tie.getImg() != null) {
            Glide.with(mContext).load(Constants.GET_IMAGE_PATH + tie.getImg()).into(holder.image);
            holder.image.setVisibility(View.VISIBLE);
        }

        holder.dd.setText(String.format("%02d",tie.getDateValue().getDate()));
        holder.mm.setText(numbers[tie.getDateValue().getMonth()] + "月");

        holder.comment_bt.setImageResource(R.mipmap.comment);
        holder.share_bt.setImageResource(R.mipmap.share);

        holder.share_bt.setText("分享");  // 这个按钮没实现，不用管
        holder.comment_bt.setText((tie.getReply_count() > 0) ? String.valueOf(tie.getReply_count()) : "评论");

        setLike_Bad_bt(holder, tie);
        holder.setItemTag(position);
        holder.comment_bt.setTag(position);
        holder.image.setTag(position);
        holder.good_bt.setTag(position);
        holder.bad_bt.setTag(position);
    }

    @Override
    public int getItemCount() {
        return tieList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryTieAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (getItemViewType(position) == 0) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                Tie tie = tieList.get(position);

                if ("change_like_bt".equals(payloads.get(0))) {
                    setLike_Bad_bt(holder, tie);
                }
            }
        }
    }

    //设置点赞和点踩按钮的状态
    private void setLike_Bad_bt(HistoryTieAdapter.ViewHolder holder, Tie tie) {
        holder.good_bt.setImageResource(tie.getLikeIcon());
        holder.bad_bt.setImageResource(tie.getBadIcon());

        holder.bad_bt.setText((tie.getBad() != 0) ? String.valueOf(tie.getBad()) : "踩");
        holder.good_bt.setText((tie.getGood() != 0) ? String.valueOf(tie.getGood()) : "赞");
    }

    public void changeItem(int position, Tie tie) {
        Tie t = tieList.get(position);
        t.setGood(tie.getGood());
        t.setBad(tie.getBad());
        t.setLiked(tie.getLiked());
        notifyItemChanged(position, "change_like_bt");
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        TextView dd;
        TextView mm;
        TextView ba_name;
        TextView title;
        ImageView image;
        ImageTextButton1 share_bt;
        ImageTextButton1 comment_bt;
        ImageTextButton1 bad_bt;
        ImageTextButton1 good_bt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dd = itemView.findViewById(R.id.dd);
            mm = itemView.findViewById(R.id.mm);
            ba_name = itemView.findViewById(R.id.ba_name);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
            share_bt = itemView.findViewById(R.id.share_bt);
            comment_bt = itemView.findViewById(R.id.comment_bt);
            bad_bt = itemView.findViewById(R.id.bad_bt);
            good_bt = itemView.findViewById(R.id.good_bt);

            itemView.setOnClickListener(HistoryTieAdapter.this);
            image.setOnClickListener(HistoryTieAdapter.this);
            share_bt.setOnClickListener(HistoryTieAdapter.this);
            comment_bt.setOnClickListener(HistoryTieAdapter.this);
            good_bt.setOnClickListener(HistoryTieAdapter.this);
            bad_bt.setOnClickListener(HistoryTieAdapter.this);
        }

        public void setItemTag(int tag) {
            itemView.setTag(tag);
        }
    }
}
