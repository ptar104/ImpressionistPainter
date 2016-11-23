package com.capstone.petros.impressionistpainter;

import android.graphics.Paint;

/**
 * Created by peterkoutras on 11/17/16.
 */

public class BrushPoint {

    private float x,y, lastX, lastY;
    private BrushType brushType;
    private int color;

    public BrushPoint(float x, float y, float lastX, float lastY, BrushType brushType, int color){
        this.x = x;
        this.y = y;
        this.lastX = lastX;
        this.lastY = lastY;
        this.brushType = brushType;
        this.color = color;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getLastX() {
        return lastX;
    }

    public void setLastX(float lastX) {
        this.lastX = lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public void setLastY(float lastY) {
        this.lastY = lastY;
    }

    public BrushType getBrushType() {
        return brushType;
    }

    public void setBrushType(BrushType brushType) {
        this.brushType = brushType;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
