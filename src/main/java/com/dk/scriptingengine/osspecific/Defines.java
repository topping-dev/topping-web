package com.dk.scriptingengine.osspecific;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import com.dk.scriptingengine.LuaEngine;

public class Defines
{
	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	/**
	 * Generate a value suitable for use in {@link #setId(int)}.
	 * This value will not collide with ID values generated at build time by aapt for R.id.
	 *
	 * @return a generated ID value
	 */
	public static int generateViewId() {
	    for (;;) {
	        final int result = sNextGeneratedId.get();
	        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
	        int newValue = result + 1;
	        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
	        if (sNextGeneratedId.compareAndSet(result, newValue)) {
	            return result;
	        }
	    }
	}
	
	public static InputStream GetResourceAssetSd(String path, String resName)
    {
		Context context = LuaEngine.getInstance().GetContext();
    	try
    	{
    		try
    		{
    			AssetManager assetManager = context.getAssets();

    	        InputStream istr = assetManager.open(path + resName);
    	        return istr;
    		}
    		catch (Exception e) 
    		{
    			/*int resId = context.getResources().getIdentifier("app_name", "string", Common.pack);
    			String appName = context.getResources().getString(resId);
        		File tempDir= Environment.getExternalStorageDirectory();
        		//File tempDir = getCacheDir();
        	    tempDir=new File(tempDir.getAbsolutePath() + "/" +  appName + "/");
        	    if(!tempDir.exists())
        	    {
        	        tempDir.mkdir();
        	    }
        	    
        	    tempDir = new File(tempDir.getAbsolutePath() + "/data/");
        	    if(!tempDir.exists())
        	    {
        	        tempDir.mkdir();
        	    }
        	    
        	    tempDir = new File(tempDir.getAbsolutePath() + "/" + path + resName);*/
    			
    			File tempDir = context.getExternalFilesDir(path);
    			tempDir = new File(tempDir.getAbsolutePath() + "/" + resName);
        	    
        	    FileInputStream fsi = new FileInputStream(tempDir);
        	    return fsi;
			}
    	}
    	catch (Exception e) 
    	{
    		return null;
		}
    }
	
	public static String GetPathAssetSd(String path)
    {
		Context context = LuaEngine.getInstance().GetContext();
    	try
    	{
    		try
    		{
    			//AssetManager assetManager = context.getAssets();
    			
    			if(path.compareTo("/") == 0)
    				path = "";

    	        return path;
    		}
    		catch (Exception e) 
    		{
    			/*int resId = context.getResources().getIdentifier("app_name", "string", Common.pack);
    			String appName = context.getResources().getString(resId);
        		File tempDir= Environment.getExternalStorageDirectory();
        		//File tempDir = getCacheDir();
        	    tempDir=new File(tempDir.getAbsolutePath() + "/" +  appName + "/");
        	    if(!tempDir.exists())
        	    {
        	        tempDir.mkdir();
        	    }
        	    
        	    tempDir = new File(tempDir.getAbsolutePath() + "/data/");
        	    if(!tempDir.exists())
        	    {
        	        tempDir.mkdir();
        	    }
        	    
        	    String retVal = tempDir.getAbsolutePath() + "/" + path;
        	    return retVal;*/
    			
    			return context.getExternalFilesDir(path).getAbsolutePath();
			}
    	}
    	catch (Exception e) 
    	{
    		return "";
		}
    }
	
	public static InputStream GetResourceSdAsset(String path, String resName)
	{
		Context context = LuaEngine.getInstance().GetContext();
    	try
    	{
    		try
    		{
    			/*int resId = context.getResources().getIdentifier("app_name", "string", Common.pack);
    			String appName = context.getResources().getString(resId);
        		File tempDir= Environment.getExternalStorageDirectory();
        		//File tempDir = getCacheDir();
        	    tempDir=new File(tempDir.getAbsolutePath() + "/" +  appName + "/");
        	    if(!tempDir.exists())
        	    {
        	        tempDir.mkdir();
        	    }
        	    
        	    tempDir = new File(tempDir.getAbsolutePath() + "/data/");
        	    if(!tempDir.exists())
        	    {
        	        tempDir.mkdir();
        	    }
        	    tempDir = new File(tempDir.getAbsolutePath() + "/" + path + resName);
        	    */
    			
    			File tempDir = context.getExternalFilesDir(path);
    			tempDir = new File(tempDir.getAbsolutePath() + "/" + resName);
        	    if(!tempDir.exists())
        	    	throw new Exception();
        	    
        	    FileInputStream fsi = new FileInputStream(tempDir);
        	    return fsi;
    		}
    		catch (Exception e) 
    		{
    			AssetManager assetManager = context.getAssets();
    			
    			if(path.compareTo("/") == 0)
    				path = "";

    	        InputStream istr = assetManager.open(path + resName);
    	        return istr;
			}
    	}
    	catch (Exception e) 
    	{
    		return null;
		}
	}
	
	public static String GetPathSdAsset(String path)
	{
		Context context = LuaEngine.getInstance().GetContext();
    	try
    	{
    		try
    		{
    			/*int resId = context.getResources().getIdentifier("app_name", "string", Common.pack);
    			String appName = context.getResources().getString(resId);
        		File tempDir= Environment.getExternalStorageDirectory();
        		//File tempDir = getCacheDir();
        	    tempDir=new File(tempDir.getAbsolutePath() + "/" +  appName + "/");
        	    if(!tempDir.exists())
        	    {
        	        tempDir.mkdir();
        	    }
        	    
        	    tempDir = new File(tempDir.getAbsolutePath() + "/data/");
        	    if(!tempDir.exists())
        	    {
        	        tempDir.mkdir();
        	    }
        	    
        	    String retVal = tempDir.getAbsolutePath() + "/" + path;
        	    return retVal;*/
    			
    			return context.getExternalFilesDir(path).getAbsolutePath();
    		}
    		catch (Exception e) 
    		{
    			if(path.compareTo("/") == 0)
    				path = "";
    			
    			return path;
			}
    	}
    	catch (Exception e) 
    	{
    		return null;
		}
	}
	
	public static InputStream GetResourceSd(String path, String resName)
	{
		Context context = LuaEngine.getInstance().GetContext();
		try
		{
			/*int resId = context.getResources().getIdentifier("app_name", "string", Common.pack);
			String appName = context.getResources().getString(resId);
    		File tempDir= Environment.getExternalStorageDirectory();
    		//File tempDir = getCacheDir();
    	    tempDir=new File(tempDir.getAbsolutePath() + "/" +  appName + "/");
    	    if(!tempDir.exists())
    	    {
    	        tempDir.mkdir();
    	    }
    	    
    	    tempDir = new File(tempDir.getAbsolutePath() + "/data/");
    	    if(!tempDir.exists())
    	    {
    	        tempDir.mkdir();
    	    }
    	    
    	    tempDir = new File(tempDir.getAbsolutePath() + "/" + path + resName);*/
			File tempDir = context.getExternalFilesDir(path);
			tempDir = new File(tempDir.getAbsolutePath() + "/" + resName);
    	    if(!tempDir.exists())
    	    	throw new Exception();
    	    
    	    FileInputStream fsi = new FileInputStream(tempDir);
    	    return fsi;
		}
		catch (Exception e) 
    	{
    		return null;
		}
	}
	
	public static String GetPathSd(String path)
	{
		Context context = LuaEngine.getInstance().GetContext();
		try
		{
			/*int resId = context.getResources().getIdentifier("app_name", "string", Common.pack);
			String appName = context.getResources().getString(resId);
    		File tempDir= Environment.getExternalStorageDirectory();
    		//File tempDir = getCacheDir();
    	    tempDir=new File(tempDir.getAbsolutePath() + "/" +  appName + "/");
    	    if(!tempDir.exists())
    	    {
    	        tempDir.mkdir();
    	    }
    	    
    	    tempDir = new File(tempDir.getAbsolutePath() + "/data/");
    	    if(!tempDir.exists())
    	    {
    	        tempDir.mkdir();
    	    }
    	    
    	    String retVal = tempDir.getAbsolutePath() + "/" + path;
    	    return retVal;*/
			
			return context.getExternalFilesDir(path).getAbsolutePath();
		}
		catch (Exception e) 
    	{
    		return "";
		}
	}
	
	public static InputStream GetResourceAsset(String path, String resName)
    {
		Context context = LuaEngine.getInstance().GetContext();
		try
		{
			AssetManager assetManager = context.getAssets();

			if(path.compareTo("/") == 0)
				path = "";
			
	        InputStream istr = assetManager.open(path + resName);
	        return istr;
		}
		catch (Exception e) 
		{
			return null;
		}
    }
	
	public static String GetPathAsset(String path)
    {
		//Context context = LuaEngine.getInstance().GetContext();
		try
		{
			if(path.compareTo("/") == 0)
				path = "";
			
			return path;
		}
		catch (Exception e) 
		{
			return null;
		}
    }
	
	public static InputStream GetResourceInternalAsset(String path,	String resName)
	{
		Context context = LuaEngine.getInstance().GetContext();
		try
		{
			try
			{
				String scriptsDir = context.getFilesDir().getAbsolutePath();
				scriptsDir = scriptsDir + "/" + path;
				File scripts = new File(scriptsDir);
				{
					if(!scripts.exists())
						scripts.mkdir();
					
					FileInputStream fsi = new FileInputStream(scriptsDir + resName);
		    	    return fsi;
				}
			}
			catch (Exception e) 
			{
				AssetManager assetManager = context.getAssets();
				
				if(path.compareTo("/") == 0)
					path = "";
	
		        InputStream istr = assetManager.open(path + resName);
		        return istr;
			}
		}
		catch (Exception e) 
		{
			return null;
		}
	}
	
	public static String GetPathInternalAsset(String path)
	{
		Context context = LuaEngine.getInstance().GetContext();
		try
		{
			try
			{
				String scriptsDir = context.getFilesDir().getAbsolutePath();
				scriptsDir = scriptsDir + "/" + path;
				return scriptsDir;
			}
			catch (Exception e) 
			{
				if(path.compareTo("/") == 0)
					path = "";
				return path;
			}
		}
		catch (Exception e) 
		{
			return null;
		}
	}
	
	public static String GetPathForLua()
	{
		String scriptsRoot = LuaEngine.getInstance().GetScriptsRoot();
		int primaryLoad = LuaEngine.getInstance().GetPrimaryLoad();
		switch(primaryLoad)
		{
			case LuaEngine.EXTERNAL_DATA:
			{
				return Defines.GetPathSdAsset(scriptsRoot + "/");
				
			}
			case LuaEngine.INTERNAL_DATA:
			{
				return Defines.GetPathInternalAsset(scriptsRoot + "/");
			}
			case LuaEngine.RESOURCE_DATA:
			{
				return Defines.GetPathAsset(scriptsRoot + "/");
			}
			default:
			{
				return Defines.GetPathAsset(scriptsRoot + "/");
			}
		}
	}
	
	public static String GetExternalPathForResource(Context context, String type)
    {
		return System.getProperty("user.dir") + "/external";
    }
}
