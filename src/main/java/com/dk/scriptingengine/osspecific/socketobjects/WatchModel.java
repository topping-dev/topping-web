package com.dk.scriptingengine.osspecific.socketobjects;

public class WatchModel
{
    private String output;

    public WatchModel(String id, String suffix)
    {
        output = "{\"id\":\"" + id + "\", \"idWatch\":\"" + id + suffix + "\"}";
    }

    @Override
    public String toString()
    {
        return output;
    }
}
