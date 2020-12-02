package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.luagui.DisplayMetrics;

import java.util.HashMap;
import java.util.Map;

public class LGDimensionParser
{
    HashMap<String, HashMap<String, Integer>> dimensionMap;

    public static LGDimensionParser GetInstance()
    {
        return LGParser.GetInstance().pDimen;
    }

    public void ParseXML(int orientation, HashMap<String, String> attrs, String value)
    {
        if(dimensionMap == null)
        {
            dimensionMap = new HashMap<>();
        }

        for(Map.Entry<String, String> entry : attrs.entrySet())
        {
            if(entry.getKey().equals("name"))
            {
                HashMap<String, Integer> oldValue = dimensionMap.get(entry.getValue());
                HashMap<String, Integer> valueDict = new HashMap<>();

                if ((orientation & LGParser.ORIENTATION_PORTRAIT) > 0)
                    valueDict.put(LGParser.ORIENTATION_PORTRAIT_S, DisplayMetrics.readSize(value));
                else
                {
                    Object val;
                    if(oldValue != null && ((val = oldValue.get(LGParser.ORIENTATION_PORTRAIT_S)) != null))
                        valueDict.put(LGParser.ORIENTATION_PORTRAIT_S, (Integer) val);
                }

                if ((orientation & LGParser.ORIENTATION_LANDSCAPE) > 0)
                    valueDict.put(LGParser.ORIENTATION_LANDSCAPE_S, DisplayMetrics.readSize(value));
                else
                {
                    Object val;
                    if(oldValue != null && ((val = oldValue.get(LGParser.ORIENTATION_LANDSCAPE_S)) != null))
                        valueDict.put(LGParser.ORIENTATION_LANDSCAPE_S, (Integer) val);
                }

                dimensionMap.put(entry.getKey(), valueDict);
            }
        }
    }

    public int GetDimension(String key)
    {
        int orientation = LGParser.ORIENTATION_PORTRAIT;

        if(key.startsWith("@dimen/"))
        {
            String[] arr = key.split("/");
            HashMap<String, Integer> val = dimensionMap.get(arr[arr.length - 1]);
            if(val != null)
            {
                if(orientation == LGParser.ORIENTATION_PORTRAIT)
                    return val.get(LGParser.ORIENTATION_PORTRAIT_S);
                else
                    return val.get(LGParser.ORIENTATION_LANDSCAPE_S);
            }
        }
        return DisplayMetrics.readSize(key);
    }
}
