package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.osspecific.AssetManager;
import com.dk.scriptingengine.osspecific.Defines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Lua resource class.
 * This class is used to fetch resources from lua.
 */
@LuaClass(className = "LuaResource")
public class LuaResource implements LuaInterface
{
	/**
	 * This function gets resource from package, if can not it gets from other data location.
	 * @param path root path to search.
	 * @param resName resource name to search
	 * @return LuaStream of resource
	 */
	@LuaFunction(manual = false, methodName = "GetResourceAssetSd", self = LuaResource.class, arguments = { String.class, String.class })
	public static LuaStream GetResourceAssetSd(String path, String resName)
	{
		LuaStream ls = new LuaStream();
		ls.SetStream(Defines.GetResourceAssetSd(path, resName));
		return ls;
	}
	
	/**
	 * This function gets resource from other data location, if can not it gets from package.
	 * @param path root path to search.
	 * @param resName resource name to search
	 * @return LuaStream of resource
	 */
	@LuaFunction(manual = false, methodName = "GetResourceSdAsset", self = LuaResource.class, arguments = { String.class, String.class })
	public static LuaStream GetResourceSdAsset(String path, String resName)
	{
		LuaStream ls = new LuaStream();
		ls.SetStream(Defines.GetResourceSdAsset(path, resName));
		return ls;
	}
	
	/**
	 * This function gets resource from package.
	 * @param path root path to search.
	 * @param resName resource name to search
	 * @return LuaStream of resource
	 */
	@LuaFunction(manual = false, methodName = "GetResourceAsset", self = LuaResource.class, arguments = { String.class, String.class })
	public static LuaStream GetResourceAsset(String path, String resName)
	{
		LuaStream ls = new LuaStream();
		ls.SetStream(Defines.GetResourceAsset(path, resName));
		return ls;
	}
	
	/**
	 * This function gets resource from other data location.
	 * @param path root path to search.
	 * @param resName resource name to search
	 * @return LuaStream of resource
	 */
	@LuaFunction(manual = false, methodName = "GetResourceSd", self = LuaResource.class, arguments = { String.class, String.class })
	public static LuaStream GetResourceSd(String path, String resName)
	{
		LuaStream ls = new LuaStream();
		ls.SetStream(Defines.GetResourceSd(path, resName));
		return ls;
	}
	
	/**
	 * This function gets resource based on defines.lua config
	 * @param path root path to search.
	 * @param resName resource name to search
	 * @return LuaStream of resource
	 */
	public static LuaStream GetResource(String path, String resName)
	{
		//String scriptsRoot = LuaEngine.getInstance().GetScriptsRoot();
		int primaryLoad = LuaEngine.getInstance().GetPrimaryLoad();
		switch(primaryLoad)
		{
			case LuaEngine.EXTERNAL_DATA:
			{
				LuaStream ls = new LuaStream();
				ls.SetStream(Defines.GetResourceSdAsset(path + "/", resName.toString()));
				return ls;
			}
			case LuaEngine.INTERNAL_DATA:
			{
				LuaStream ls = new LuaStream();
				ls.SetStream(Defines.GetResourceInternalAsset(path + "/", resName.toString()));
				return ls;
			}
			case LuaEngine.RESOURCE_DATA:
			{
				LuaStream ls = new LuaStream();
				ls.SetStream(Defines.GetResourceAsset(path + "/", resName.toString()));
				return ls;
			}
			default:
			{
				LuaStream ls = new LuaStream();
				ls.SetStream(Defines.GetResourceAsset(path + "/", resName.toString()));
				return ls;
			}
		}
	}

    /**
     * (Ignore)
     */
	public static ArrayList<String> GetResourceDirectories(String startsWith)
	{
		int primaryLoad = LuaEngine.getInstance().GetPrimaryLoad();
		switch (primaryLoad)
		{
			case LuaEngine.EXTERNAL_DATA:
			case LuaEngine.INTERNAL_DATA:
			    return new ArrayList<>();
            case LuaEngine.RESOURCE_DATA:
            {
                try
                {
                    String[] fList = AssetManager.getInstance().list(LuaEngine.getInstance().GetUIRoot());
                    ArrayList<String> ret = new ArrayList<>();
                    for(String file : fList)
                    {
                        if(file.startsWith(startsWith))
                            ret.add(file);
                    }
                    return ret;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            default:
                return new ArrayList<>();
		}
	}

    /**
     * (Ignore)
     */
    public static ArrayList<String> GetResourceFiles(String folder)
    {
        int primaryLoad = LuaEngine.getInstance().GetPrimaryLoad();
            switch (primaryLoad)
            {
            case LuaEngine.EXTERNAL_DATA:
            case LuaEngine.INTERNAL_DATA:
                return new ArrayList<>();
            case LuaEngine.RESOURCE_DATA:
            {
                String[] fList = new String[0];
                try
                {
                    fList = AssetManager.getInstance().list(folder);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                return new ArrayList<>(Arrays.asList(fList));
            }
            default:
                return new ArrayList<>();
        }
    }

    /**
	 * (Ignore)
	 */
	@Override
	public void RegisterEventFunction(String var, LuaTranslator lt) 
	{
		
	}

	/**
	 * (Ignore)
	 */
	@Override
	public String GetId() 
	{
		return "LuaResource";
	}
}
