package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaResource;
import com.dk.scriptingengine.LuaStream;

import java.util.ArrayList;
import java.util.Comparator;

public class LGLayoutParser
{
    private ArrayList<LGParser.DynamicResource> clearedDirectoryList;

    public static LGLayoutParser GetInstance()
    {
        return LGParser.GetInstance().pLayout;
    }

    public void Initialize()
    {
        ArrayList<String> directoryList = LuaResource.GetResourceDirectories(Resources.LUA_LAYOUT_FOLDER);
        clearedDirectoryList = LGParser.GetInstance().Tester(directoryList, Resources.LUA_LAYOUT_FOLDER);
        clearedDirectoryList.sort(new Comparator<LGParser.DynamicResource>()
        {
            @Override
            public int compare(LGParser.DynamicResource obj1, LGParser.DynamicResource obj2)
            {
                String aData = (String) obj1.data;
                String bData = (String) obj2.data;
                if(aData.equals(bData))
                    return 0;
                else if(aData.length() > bData.length())
                    return -1;
                else
                    return 1;

            }
        });
    }

    public LuaStream GetLayout(String name)
    {
        LuaStream ls = null;
        for(LGParser.DynamicResource dr : clearedDirectoryList)
        {
            String path = LuaEngine.getInstance().GetUIRoot() + "/" + dr.data;
            ls = LuaResource.GetResource(path, name);
            if(ls.HasStream())
                break;
        }
        return ls;
    }
}
