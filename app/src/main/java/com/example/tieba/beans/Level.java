package com.example.tieba.beans;


import com.example.tieba.R;

/**
 * @author sheng
 * @date 2021/8/27 1:19
 */
public class Level {
    private int level;
    private final String exp;

    private static final String[] LEVEL_NAMES = new String[]{
            "稽静无声",
            "束手无稽",
            "稽关用尽",
            "无稽可施",
            "贻误军稽",
            "投稽取巧",
            "言听稽从",
            "随稽应变",
            "将稽就计",
            "鹤立稽群",
            "千谋百稽",
            "诡稽多端",
            "神稽妙算",
            "臻極滑稽",
            "稽世之才",
            "日理万稽",
            "一稽当关",
            "稽临天下"
    };

    private static final int[] LEVEL_UP_EXP = new int[]{
            5, 15, 30, 50, 100, 200, 500, 1000, 2000, 3000, 6000,
            10000, 18000, 30000, 60000, 100000, 300000, 1000000
    };

    public Level(String exp) {
        this.exp = exp;
        setLevel();
    }

    private void setLevel() {
        int e = exp == null ? 0 : Integer.parseInt(exp);
        for (int i = 0; i < LEVEL_UP_EXP.length; i++) {
            if (e < LEVEL_UP_EXP[i]) {
                level = i + 1;
                return;
            }
        }
    }

    public String getLevel() {
        return String.valueOf(level);
    }

    public int getLevelUpExp() {
        return LEVEL_UP_EXP[level - 1];
    }

    public String getLevelName(int level) {
        return LEVEL_NAMES[level];
    }

    public String getExp() {
        return exp;
    }

    public int getLevelIcon() {
        if (level < 4) {
            return R.mipmap.diamond_green;
        } else if (level < 10) {
            return R.mipmap.diamond_blue;
        } else if (level < 16) {
            return R.mipmap.diamond_yellow;
        } else {
            return R.mipmap.diamond_orange;
        }
    }

    @Override
    public String toString() {
        return "LV" + level + " " + getLevelName(level - 1);
    }
}
