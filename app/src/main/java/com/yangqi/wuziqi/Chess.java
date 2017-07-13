package com.yangqi.wuziqi;

/**
 * Created by YangQI on 2017/3/29.
 * 棋子类
 */
public class Chess {

    public enum Color {BLACK, WHITE, NONE}

    private Color color;

    public Chess(){
        this.color = Color.NONE;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
