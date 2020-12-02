package com.dk.scriptingengine.osspecific;

public class Rect
{
    protected int left;
    protected int top;
    protected int right;
    protected int bottom;

    public void set(int left, int top, int right, int bottom)
    {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public int height()
    {
        return bottom - top;
    }

    public int width()
    {
        return right - left;
    }
}
