package com.dk.scriptingengine.osspecific.socketobjects;

public class MethodModel
{
    private String output;

    public MethodModel(String id, String suffix)
    {
        output = "{\"id\":\"" + id + "\", \"idWatch\":\"" + id + suffix + "\"}";
    }

    @Override
    public String toString()
    {
        return output;
    }
}
