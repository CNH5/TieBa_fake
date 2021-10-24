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
import com.example.tieba.activity.UserInfoActivity;
import com.example.tieba.beans.Tie;
import com.example.tieba.views.ImageTextButton1;

import java.util.List;


/**
 * @author sheng
 * @date 2021/8/26 1:44
 */
public class TieAdapter extends RecyclerView.Adapter<TieAdapter.ViewHolder> implements View.OnClickListener {
    private final List<Tie> tieList;
    private final Context mContext;
    private final String account;


    public TieAdapter(Context context, List<Tie> data, String account) {
        this.mContext = context;
        this.tieList = data;
        this.account = account;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.tie_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tie tie = tieList.get(position);

        //TODO: 把这玩意变圆...
        //加载发帖用户的头像
        Glide.with(mContext).load(Constants.GET_IMAGE_PATH + tie.getPoster_avatar()).into(holder.poster_avatar);

        holder.poster_name.setText(tie.getPoster_name());
        holder.date.setText(tie.getDate());

        if (tie.getTitle() == null && tie.getInfo() == null) {
            holder.title.setVisibility(View.GONE);
        } else if (tie.getTitle() == null && tie.getInfo() != null) {
            holder.title.setText(tie.getInfo().substring(0, 39) + "...");
        } else {
            holder.title.setText(tie.getTitle());
        }

        //加载图片
        if (tie.getImg() != null) {
            Glide.with(mContext).load(Constants.GET_IMAGE_PATH + tie.getImg())
                    .into(holder.tie_image);
            holder.tie_image.setVisibility(View.VISIBLE);
        }

        holder.comment_bt.setImageResource(R.mipmap.comment);
        holder.share_bt.setImageResource(R.mipmap.share);

        holder.share_bt.setText("分享");  // 这个按钮没实现，不用管
        holder.comment_bt.setText((tie.getReply_count() > 0) ? String.valueOf(tie.getReply_count()) : "评论");

        setLike_Bad_bt(holder, tie);

        holder.setItemTag(position);
        holder.poster_name.setTag(position);
        holder.poster_avatar.setTag(position);
        holder.tie_image.setTag(position);
        holder.comment_bt.setTag(position);
        holder.good_bt.setTag(position);
        holder.bad_bt.setTag(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
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
    private void setLike_Bad_bt(ViewHolder holder, Tie tie) {
        holder.good_bt.setImageResource(tie.getLikeIcon());
        holder.bad_bt.setImageResource(tie.getBadIcon());

        holder.bad_bt.setText((tie.getBad() != 0) ? String.valueOf(tie.getBad()) : "踩");
        holder.good_bt.setText((tie.getGood() != 0) ? String.valueOf(tie.getGood()) : "赞");
    }


    @Override
    public int getItemCount() {
        return tieList.size();
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.share_bt) {
            Toast.makeText(mContext, "功能未实现！", Toast.LENGTH_SHORT).show();

        } else if (vid == R.id.avatar || vid == R.id.name) {
            // 跳转到其它用户的主页
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            //传递账号，到那边再查询
            intent.putExtra("target", tieList.get((int) v.getTag()).getPoster_id());
            intent.putExtra("account", account);
            ((Activity) mContext).startActivityForResult(intent, UserInfoActivity.CODE);

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

            } else {
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

            } else {
                int position = (int) v.getTag();
                Tie tie = tieList.get(position);
                tie.unlike();
                notifyItemChanged(position, "change_like_bt");
                BackstageInteractive.sendLike(account, tie.getId(), tie.getLiked(), Constants.TIE);
            }

        } else {
            int position = (int) v.getTag();
            Intent intent = new Intent(mContext, TieActivity.class);

            intent.putExtra("tie_id", tieList.get(position).getId());
            intent.putExtra("account", account);
            intent.putExtra("position", position);
            ((Activity) mContext).startActivityForResult(intent, TieActivity.CODE);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster_avatar;
        TextView poster_name;
        TextView date;
        TextView title;
        ImageView tie_image;
        ImageTextButton1 share_bt;
        ImageTextButton1 comment_bt;
        ImageTextButton1 bad_bt;
        ImageTextButton1 good_bt;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster_avatar = itemView.findViewById(R.id.avatar);
            poster_name = itemView.findViewById(R.id.name);
            date = itemView.findViewById(R.id.date);
            title = itemView.findViewById(R.id.title);
            tie_image = itemView.findViewById(R.id.image);
            share_bt = itemView.findViewById(R.id.share_bt);
            comment_bt = itemView.findViewById(R.id.comment_bt);
            bad_bt = itemView.findViewById(R.id.bad_bt);
            good_bt = itemView.findViewById(R.id.good_bt);

            itemView.setOnClickListener(TieAdapter.this);
            poster_avatar.setOnClickListener(TieAdapter.this);
            poster_name.setOnClickListener(TieAdapter.this);
            tie_image.setOnClickListener(TieAdapter.this);
            share_bt.setOnClickListener(TieAdapter.this);
            comment_bt.setOnClickListener(TieAdapter.this);
            good_bt.setOnClickListener(TieAdapter.this);
            bad_bt.setOnClickListener(TieAdapter.this);
        }

        public void setItemTag(int tag) {
            itemView.setTag(tag);
        }
    }

    public void changeItem(int position, Tie tie) {
        Tie t = tieList.get(position);
        t.setGood(tie.getGood());
        t.setBad(tie.getBad());
        t.setLiked(tie.getLiked());
        notifyItemChanged(position, "change_like_bt");
    }
}
