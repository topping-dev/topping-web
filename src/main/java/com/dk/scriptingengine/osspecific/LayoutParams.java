package com.dk.scriptingengine.osspecific;

public class LayoutParams
{
    public static final int WRAP_CONTENT = -1;
    public static final int MATCH_PARENT = -2;
    public static final int FILL_PARENT = -2;

    public float weightSum = 1;
    public float weight = -1;
    public int gravity = 0;
    public int w = WRAP_CONTENT;
    public int h = WRAP_CONTENT;
    public int marginLeft;
    public int marginTop;
    public int marginRight;
    public int marginBottom;

    public LayoutParams()
    {

    }

    public LayoutParams(int w, int h)
    {
        this.w = w;
        this.h = h;
    }

    public void setMargins(int left, int top, int right, int bottom)
    {
        this.marginLeft = left;
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
    }
}
