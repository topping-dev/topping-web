package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.luagui.DisplayMetrics;

import java.util.HashMap;
import java.util.Map;

public class LGStringParser
{
    HashMap<String, HashMap<String, String>> stringMap;

    public static LGStringParser GetInstance()
    {
        return LGParser.GetInstance().pString;
    }

    public void ParseXML(int orientation, HashMap<String, String> attrs, String value)
    {
        if (stringMap == null)
        {
            stringMap = new HashMap<>();
        }

        for(Map.Entry<String, String> entry : attrs.entrySet())
        {
            if(entry.getKey().equals("name"))
            {
                HashMap<String, String> oldValue = stringMap.get(entry.getValue());
                HashMap<String, String> valueDict = new HashMap<>();

                if ((orientation & LGParser.ORIENTATION_PORTRAIT) > 0)
                    valueDict.put(LGParser.ORIENTATION_PORTRAIT_S, value);
                else
                {
                    Object val;
                    if(oldValue != null && ((val = oldValue.get(LGParser.ORIENTATION_PORTRAIT_S)) != null))
                        valueDict.put(LGParser.ORIENTATION_PORTRAIT_S, (String) val);
                }

                if ((orientation & LGParser.ORIENTATION_LANDSCAPE) > 0)
                    valueDict.put(LGParser.ORIENTATION_LANDSCAPE_S, value);
                else
                {
                    Object val;
                    if(oldValue != null && ((val = oldValue.get(LGParser.ORIENTATION_LANDSCAPE_S)) != null))
                        valueDict.put(LGParser.ORIENTATION_LANDSCAPE_S, (String) val);
                }

                stringMap.put(entry.getKey(), valueDict);
            }
        }
    }

    public String GetString(String key)
    {
        int orientation = LGParser.ORIENTATION_PORTRAIT;

        if(key.startsWith("@string/"))
        {
            String[] arr = key.split("/");
            HashMap<String, String> val = stringMap.get(arr[arr.length - 1]);
            if(val != null)
            {
                if(orientation == LGParser.ORIENTATION_PORTRAIT)
                    return val.get(LGParser.ORIENTATION_PORTRAIT_S);
                else
                    return val.get(LGParser.ORIENTATION_LANDSCAPE_S);
            }
        }
        return key;
    }
}
