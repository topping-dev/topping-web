package com.dk.scriptingengine.osspecific;

public class RectF
{
    protected float left;
    protected float top;
    protected float right;
    protected float bottom;

    protected void set(float left, float top, float right, float bottom)
    {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
}
