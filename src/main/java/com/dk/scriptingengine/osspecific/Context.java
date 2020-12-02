package com.dk.scriptingengine.osspecific;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;

public class Context
{
    private static Context instance;
    private final String client;
    private Resources resources;
    private String packageName;

    public Context(String client)
    {
        this.client = client;
    }

    public AssetManager getAssets()
    {
        return AssetManager.getInstance();
    }

    public Resources getResources()
    {
        if(resources == null)
            new Resources();
        return resources;
    }

    public File getExternalFilesDir(String path)
    {
        return null;
    }

    public File getFilesDir()
    {
        return null;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public String getClient()
    {
        return client;
    }
}
