package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.LuaPoint;

public class LatLng
{
    public float lat;
    public float lng;

    public LatLng(float lat, float lng)
    {
        this.lat = lat;
        this.lng = lng;
    }

    public LatLng(double x, double y)
    {
        this.lat = (float) x;
        this.lng = (float) y;
    }

    public LatLng(LuaPoint lp)
    {
        this.lat = lp.x;
        this.lng = lp.y;
    }
}
