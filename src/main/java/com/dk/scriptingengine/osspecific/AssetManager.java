package com.dk.scriptingengine.osspecific;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.print.DocFlavor;

public class AssetManager
{
    private static AssetManager instance;
    public static AssetManager getInstance()
    {
        if(instance == null)
            instance = new AssetManager();
        return instance;
    }

    public InputStream open(String file) throws IOException
    {
        String path = "assets/" + file;
        FileInputStream fs = new FileInputStream(path);
        return fs;
    }

    public String[] list(String root) throws IOException
    {
        String path = "assets/" + root;
        File f = new File(path);
        return f.list();
    }
}
