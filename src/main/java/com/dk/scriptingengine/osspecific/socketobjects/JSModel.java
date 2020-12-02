package com.dk.scriptingengine.osspecific.socketobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSModel
{
    public String name;
    public Object value;
    public String realValue;

    public JSModel(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }

    public JSModel(String realValue)
    {
        this.realValue = realValue;
    }

    @Override
    public String toString()
    {
        if(realValue != null)
            return realValue;

        String ret = "\"" + name + "\" : ";
        if(value == null)
            ret += "null";
        else /*if(value.getClass() == Integer.class
                || value.getClass() == Intege)
        {
            ret += value;
        }
        else if(value.getClass() == String.class)
        {
            ret += "\"" + value + "\"";
        }
        else if(value.getClass() == Boolean.class)
        {
            ret += (((Boolean)value) ? "true" : "false");
        }
        else if(value instanceof Map<?,?>
                || value instanceof List<?>)*/
        {
            ObjectMapper om = new ObjectMapper();
            try
            {
                ret += om.writeValueAsString(value);
            }
            catch (JsonProcessingException e)
            {
            }
        }
        /*else
            ret += value;*/
        return ret;
    }
}
