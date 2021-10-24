package com.example.tieba.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tieba.BackstageInteractive;
import com.example.tieba.Constants;
import com.example.tieba.R;
import com.example.tieba.activity.LoginActivity;
import com.example.tieba.activity.UserInfoActivity;
import com.example.tieba.beans.Level;
import com.example.tieba.beans.Reply;
import com.example.tieba.views.ImageTextButton1;
import com.example.tieba.views.TextInImageView;

import java.util.List;

/**
 * @author sheng
 * @date 2021/9/25 17:00
 */
public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ViewHolder> implements View.OnClickListener {
    private final List<Reply> list;
    private final Context mContext;
    private final String account;
    private final String tie_poster_id;
    private OnItemClick click;

    public void itemClick(OnItemClick click) {
        this.click = click;
    }

    public interface OnItemClick {
        EditText getView();
    }

    public ReplyAdapter(List<Reply> list, Context mContext, String account, String tie_poster_id) {
        this.list = list;
        this.mContext = mContext;
        this.account = account;
        this.tie_poster_id = tie_poster_id;
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyAdapter.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (getItemViewType(position) == 0) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                Reply r = list.get(position);

                if ("change_like_bt".equals(payloads.get(0))) {
                    setLike_Bad_bt(holder, r);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.avatar || vid == R.id.name) {
            // 跳转到其它用户的主页
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            //传递账号，到那边再查询
            intent.putExtra("target", list.get((int) v.getTag()).getPoster_id());
            intent.putExtra("account", account);
            ((Activity) mContext).startActivityForResult(intent, UserInfoActivity.CODE);

        } else if (vid == R.id.floor_image) {
            // TODO: 点击图片后应当进入全屏浏览状态
            Log.d("tips", "show image");

        } else if (vid == R.id.good_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);

            } else {
                int position = (int) v.getTag();
                Reply r = list.get(position);
                r.like();
                notifyItemChanged(position, "change_like_bt");
                BackstageInteractive.sendLike(account, r.getId(), r.getLiked(), Constants.REPLY);
            }

        } else if (vid == R.id.bad_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);

            } else {
                int position = (int) v.getTag();
                Reply r = list.get(position);
                r.unlike();
                notifyItemChanged(position, "change_like_bt");
                BackstageInteractive.sendLike(account, r.getId(), r.getLiked(), Constants.REPLY);
            }

        } else {
            Reply r = list.get((int) v.getTag());
            click.getView().setText(String.format("回复 %s: ", r.getPoster_name()));
            click.getView().requestFocus();
            click.getView().setSelection(click.getView().getText().length());

            ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReplyAdapter.ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.reply_item, parent, false));
    }

    private void setLike_Bad_bt(ReplyAdapter.ViewHolder holder, Reply r) {
        holder.good_bt.setImageResource(r.getLikeIcon());
        holder.bad_bt.setImageResource(r.getBadIcon());

        holder.bad_bt.setText((r.getBad() != 0) ? String.valueOf(r.getBad()) : "踩");
        holder.good_bt.setText((r.getGood() != 0) ? String.valueOf(r.getGood()) : "赞");
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reply reply = list.get(position);

        holder.name.setText(reply.getPoster_name());
        holder.date.setText(reply.getDate());
        holder.floor_info.setText(reply.getInfo());
        Level level = new Level(reply.getPoster_exp());

        holder.level_icon.setImageResource(level.getLevelIcon());
        holder.level_icon.setText(level.getLevel());

        //TODO: 可能还有小吧主、吧主什么的标志,暂时先不管
        if (tie_poster_id.equals(reply.getPoster_id())) {
            holder.ident.setText("楼主");
            holder.ident.setVisibility(View.VISIBLE);
        }

        //TODO: 把这玩意变圆...
        //加载发帖用户的头像
        Glide.with(mContext).load(Constants.GET_IMAGE_PATH + reply.getPoster_avatar()).into(holder.avatar);

        holder.good_bt.setOnClickListener(this);
        holder.bad_bt.setOnClickListener(this);
        setLike_Bad_bt(holder, reply);

        holder.setItemTag(position);
        holder.name.setTag(position);
        holder.avatar.setTag(position);
        holder.good_bt.setTag(position);
        holder.bad_bt.setTag(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name;
        TextView date;
        TextView ident;
        TextView floor_info;
        TextInImageView level_icon;
        ImageTextButton1 good_bt;
        ImageTextButton1 bad_bt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            ident = itemView.findViewById(R.id.ident);
            floor_info = itemView.findViewById(R.id.reply_info);
            date = itemView.findViewById(R.id.date);
            level_icon = itemView.findViewById(R.id.level_icon);
            good_bt = itemView.findViewById(R.id.good_bt);
            bad_bt = itemView.findViewById(R.id.bad_bt);

            itemView.setOnClickListener(ReplyAdapter.this);
            avatar.setOnClickListener(ReplyAdapter.this);
            name.setOnClickListener(ReplyAdapter.this);
        }

        public void setItemTag(int tag) {
            itemView.setTag(tag);
        }
    }
}
