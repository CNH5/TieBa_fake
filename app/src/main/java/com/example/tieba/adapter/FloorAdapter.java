package com.example.tieba.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cc.shinichi.library.ImagePreview;
import com.bumptech.glide.Glide;
import com.example.tieba.*;
import com.example.tieba.activity.LoginActivity;
import com.example.tieba.activity.UserInfoActivity;
import com.example.tieba.beans.Floor;
import com.example.tieba.beans.Level;
import com.example.tieba.popupWindow.ReplyFloorPopupWindow;
import com.example.tieba.views.ImageTextButton1;
import com.example.tieba.views.TextInImageView;

import java.util.List;

/**
 * @author sheng
 * @date 2021/9/4 1:48
 */
public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.ViewHolder> implements View.OnClickListener {
    private final List<Floor> floorList;
    private final Context mContext;
    private final String account;
    private final String tie_poster_id;

    public FloorAdapter(List<Floor> floorList, Context mContext, String account, String tie_poster_id) {
        this.floorList = floorList;
        this.mContext = mContext;
        this.account = account;
        this.tie_poster_id = tie_poster_id;
    }


    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.avatar || vid == R.id.name) {
            // 跳转到其它用户的主页
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            //传递账号，到那边再查询
            intent.putExtra("target", floorList.get((int) v.getTag()).getPoster_id());
            intent.putExtra("account", account);
            ((Activity) mContext).startActivityForResult(intent, UserInfoActivity.CODE);

        } else if (vid == R.id.floor_image) {
            // TODO: 点击图片后应当进入全屏浏览状态
            Floor f = floorList.get((int) v.getTag());

            ImagePreview.getInstance()
                    .setContext(mContext)  // 上下文，必须是activity，不需要担心内存泄漏，本框架已经处理好；
                    .setIndex(0)  // 设置从第几张开始看（索引从0开始）
                    .setImage(Constants.GET_IMAGE_PATH + f.getImg())
                    .start();

        } else if (v.getId() == R.id.good_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);
            } else {
                int position = (int) v.getTag();
                Floor f = floorList.get(position);
                f.like();
                notifyItemChanged(position, "change_like_bt");
                BackstageInteractive.sendLike(account, f.getId(), f.getLiked(), Constants.FLOOR);
            }


        } else if (v.getId() == R.id.bad_bt) {
            if (account == null) {
                Intent intent = new Intent(mContext, LoginActivity.class);
                ((Activity) mContext).startActivityForResult(intent, LoginActivity.CODE);

            } else {
                int position = (int) v.getTag();
                Floor f = floorList.get(position);
                f.unlike();
                notifyItemChanged(position, "change_like_bt");
                BackstageInteractive.sendLike(account, f.getId(), f.getLiked(), Constants.FLOOR);
            }


        } else {
            //TODO: 从页面下方拉出抽屉框，显示回复
            int position = (int) v.getTag();
            Floor f = floorList.get(position);
            Context context = v.getContext();
            //使用dialog：
//            Dialog dialog = new ReplyFloorDialog(v.getContext(), f, tie_poster_id, account);
//            dialog.setOnDismissListener(dialog1 -> notifyItemChanged(position, "change_like_bt"));
//            dialog.show();
            //修改背景颜色

            ReplyFloorPopupWindow window = new ReplyFloorPopupWindow(context, f, tie_poster_id, account);
            window.showAtLocation(
                    ((Activity) context).getWindow().getDecorView(),
                    Gravity.BOTTOM,
                    0, 0
            );

            window.setOnDismissListener(() -> {
                notifyItemChanged(position, "change_all");
                System.out.println(f.getReply_count());
            });
            window.init();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.floor_item, parent, false));
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Floor floor = floorList.get(position);

        holder.name.setText(floor.getPoster_name());
        holder.date.setText(String.format("第%s楼 | %s", floor.getFloor(), floor.getDate()));
        Level level = new Level(floor.getPoster_exp());

        holder.level_icon.setImageResource(level.getLevelIcon());
        holder.level_icon.setText(level.getLevel());

        //TODO: 可能还有小吧主、吧主什么的标志,暂时先不管
        if (tie_poster_id.equals(floor.getPoster_id())) {
            holder.ident.setText("楼主");
            holder.ident.setVisibility(View.VISIBLE);
        }

        //加载图片
        if (floor.getImg() != null) {
            Glide.with(mContext).load(Constants.GET_IMAGE_PATH + floor.getImg()).into(holder.img);
            holder.img.setVisibility(View.VISIBLE);
        }

        if (floor.getInfo() == null) {
            holder.floor_info.setVisibility(View.GONE);
        } else {
            holder.floor_info.setText(floor.getInfo());
        }

        //TODO: 把这玩意变圆...
        //加载发帖用户的头像
        Glide.with(mContext).load(Constants.GET_IMAGE_PATH + floor.getPoster_avatar()).into(holder.avatar);

        setLike_Bad_bt(holder, floor);
        set_reply_num(holder, floor);

        holder.good_bt.setTag(position);
        holder.bad_bt.setTag(position);
        holder.setItemTag(position);
        holder.name.setTag(position);
        holder.avatar.setTag(position);
        holder.img.setTag(position);
        holder.floor_reply_bt.setTag(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (getItemViewType(position) == 0) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);

            } else {
                Floor f = floorList.get(position);

                if ("change_like_bt".equals(payloads.get(0))) {
                    setLike_Bad_bt(holder, f);
                } else if ("change_all".equals(payloads.get(0))) {
                    setLike_Bad_bt(holder, f);
                    set_reply_num(holder, f);
                }
            }
        }
    }

    //设置回复楼层按钮是否可见
    @SuppressLint("DefaultLocale")
    private void set_reply_num(FloorAdapter.ViewHolder holder, Floor f) {
        if (f.getReply_count() > 0) {
            holder.floor_reply_bt.setText(String.format("查看全部%d条评论 >", f.getReply_count()));
            holder.floor_reply_bt.setVisibility(View.VISIBLE);

        } else {
            holder.floor_reply_bt.setVisibility(View.GONE);

        }
    }

    private void setLike_Bad_bt(FloorAdapter.ViewHolder holder, Floor f) {
        holder.good_bt.setImageResource(f.getLikeIcon());
        holder.bad_bt.setImageResource(f.getBadIcon());

        holder.bad_bt.setText((f.getBad() != 0) ? String.valueOf(f.getBad()) : "踩");
        holder.good_bt.setText((f.getGood() != 0) ? String.valueOf(f.getGood()) : "赞");
    }

    @Override
    public int getItemCount() {
        return floorList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name;
        TextView date;
        TextView ident;
        TextView floor_info;
        TextInImageView level_icon;
        ImageView img;
        ImageTextButton1 good_bt;
        ImageTextButton1 bad_bt;
        TextView floor_reply_bt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            ident = itemView.findViewById(R.id.ident);
            floor_info = itemView.findViewById(R.id.floor_info);
            date = itemView.findViewById(R.id.date);
            level_icon = itemView.findViewById(R.id.level_icon);
            good_bt = itemView.findViewById(R.id.good_bt);
            bad_bt = itemView.findViewById(R.id.bad_bt);
            img = itemView.findViewById(R.id.floor_image);
            floor_reply_bt = itemView.findViewById(R.id.floor_reply_bt);

            itemView.setOnClickListener(FloorAdapter.this);
            avatar.setOnClickListener(FloorAdapter.this);
            name.setOnClickListener(FloorAdapter.this);
            floor_reply_bt.setOnClickListener(FloorAdapter.this);
            img.setOnClickListener(FloorAdapter.this);
            good_bt.setOnClickListener(FloorAdapter.this);
            bad_bt.setOnClickListener(FloorAdapter.this);
        }

        public void setItemTag(int tag) {
            itemView.setTag(tag);
        }
    }
}
