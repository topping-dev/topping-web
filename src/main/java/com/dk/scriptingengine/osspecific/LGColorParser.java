package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.LuaColor;

import java.util.HashMap;
import java.util.Map;

import javax.print.DocFlavor;

public class LGColorParser
{
    HashMap<String, HashMap<String, String>> colorMap;

    public static LGColorParser GetInstance()
    {
        return LGParser.GetInstance().pColor;
    }

    public void ParseXML(String filename)
    {
        if(colorMap == null)
        {
            colorMap = new HashMap<>();
            HashMap<String, String> transparent = new HashMap<>();
            transparent.put(LGParser.ORIENTATION_PORTRAIT_S, "#00000000");
            transparent.put(LGParser.ORIENTATION_LANDSCAPE_S, "#00000000");
            colorMap.put("transparent", transparent);
        }
    }

    public void ParseXML(int orientation, HashMap<String, String> attrs, String value)
    {
        if(colorMap == null)
        {
            colorMap = new HashMap<>();
            HashMap<String, String> transparent = new HashMap<>();
            transparent.put(LGParser.ORIENTATION_PORTRAIT_S, "#00000000");
            transparent.put(LGParser.ORIENTATION_LANDSCAPE_S, "#00000000");
            colorMap.put("transparent", transparent);
        }

        for(Map.Entry<String, String> entry : attrs.entrySet())
        {
            if(entry.getKey().equals("name"))
            {
                HashMap<String, String> oldValue = colorMap.get(entry.getValue());
                HashMap<String, String> valueDict = new HashMap<>();

                if ((orientation & LGParser.ORIENTATION_PORTRAIT) > 0)
                    valueDict.put(LGParser.ORIENTATION_PORTRAIT_S, ParseColor(value));
                else
                {
                    Object val;
                    if(oldValue != null && ((val = oldValue.get(LGParser.ORIENTATION_PORTRAIT_S)) != null))
                        valueDict.put(LGParser.ORIENTATION_PORTRAIT_S, (String) val);
                }

                if ((orientation & LGParser.ORIENTATION_LANDSCAPE) > 0)
                    valueDict.put(LGParser.ORIENTATION_LANDSCAPE_S, ParseColor(value));
                else
                {
                    Object val;
                    if(oldValue != null && ((val = oldValue.get(LGParser.ORIENTATION_LANDSCAPE_S)) != null))
                        valueDict.put(LGParser.ORIENTATION_LANDSCAPE_S, (String) val);
                }

                colorMap.put(entry.getKey(), valueDict);
            }
        }
    }

    public String ParseColor(String color)
    {
        String retVal = null;
        int orientation = LGParser.ORIENTATION_PORTRAIT;

        if(color.contains("color/"))
        {
            String[] arr = color.split("/");
            HashMap<String, String> val = colorMap.get(arr[arr.length - 1]);
            if(val != null)
            {
                if(orientation == LGParser.ORIENTATION_PORTRAIT)
                    retVal = val.get(LGParser.ORIENTATION_PORTRAIT_S);
                else
                    retVal = val.get(LGParser.ORIENTATION_LANDSCAPE_S);
            }
        }
        if(retVal == null)
            retVal = ParseColorInternal(color);
        return retVal;
    }

    private static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length()-2, hex.length());
    }

    public String ParseColorInternal(String color)
    {
        //Its alread #xxxxxx or #xxxxxxxx format
        return color;
    }
}
