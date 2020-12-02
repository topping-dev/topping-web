package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.LuaEngine;

public class Resources
{
    public static final String LUA_LAYOUT_FOLDER = "layout";
    public static final String LUA_DRAWABLE_FOLDER = "drawable";
    public static final String LUA_VALUES_FOLDER = "values";
    public static final String LUA_ANIMATORS_FOLDER = "animator";
    public static final String LUA_ANIMS_FOLDER = "anim";

    private final String uiRoot;

    Resources()
    {
        uiRoot = LuaEngine.getInstance().GetUIRoot();
    }

    public int getIdentifier(String val, String color, String packageName)
    {
        return 0;
    }

    public String getColor(int identifier)
    {
        return null;
    }
}
