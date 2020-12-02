package com.dk.scriptingengine.osspecific;

import com.dk.helpers.ComboData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

public class ArrayAdapter<T> implements IAdapter
{
    private final Context ctx;
    private final String id;
    ArrayList<T> data = new ArrayList<>();

    public ArrayAdapter(Context ctx, String id)
    {
        this.ctx = ctx;
        this.id = id;
    }

    public void add(T val)
    {
        data.add(val);
    }

    public void remove(T val)
    {
        data.remove(val);
    }

    @Override
    public void notifyDataSetChanged()
    {
        LayoutServer.getInstance().notifyDataChanged(ctx.getClient(), id, LayoutServer.TYPE_OBJECT, data);
    }

    public ArrayList<T> getAll()
    {
        return new ArrayList<>(data);
    }
}
