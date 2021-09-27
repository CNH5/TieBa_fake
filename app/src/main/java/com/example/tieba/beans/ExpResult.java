package com.example.tieba.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author sheng
 * @date 2021/9/21 10:37
 */
public class ExpResult {

    @SerializedName("exp")
    private String exp;

    @SerializedName("sign")
    private boolean sign;


    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public boolean isSign() {
        return sign;
    }

    public void setSign(boolean sign) {
        this.sign = sign;
    }
}
