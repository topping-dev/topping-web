package com.dk.scriptingengine;

import com.luajava.Lua;
import com.luajava.Lua.lua_State;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaGlobalManual;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * General store for storing string and numbers in dictionary
 * Keys must be string value
 */
@LuaClass(className = "LuaStore")
@LuaGlobalManual(name = "STORE")
public class LuaStore implements LuaInterface
{
	private static final String PREFS_NAME = "LUA_STORE_FILE_RUED";
	
	/**
	 * Sets the string value to store
	 * @param key 
	 * @param value
	 */
	public static void SetString(String key, String value)
	{
		/*Context ctx = LuaForm.GetActiveForm().GetContext().GetContext();
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(key, value);
	    editor.commit();*/
	}
	
	/**
	 * Sets the number value to store
	 * @param key
	 * @param value
	 */
	public static void SetNumber(String key, double value)
	{
		/*Context ctx = LuaForm.GetActiveForm().GetContext().GetContext();
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putFloat(key, (float) value);
	    editor.commit();*/
	}
	
	/**
	 * Gets value stored at key
	 * @param key
	 * @return
	 */
	public static Object Get(String key)
	{
		/*Context ctx = LuaForm.GetActiveForm().GetContext().GetContext();
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
		try
		{
			return settings.getString(key, null);
		}
		catch (Exception e) 
		{
			return Double.valueOf(settings.getFloat(key, -1.0f));
		}*/
		return null;
	}
	
	/**
	 * Gets string value stored at key
	 * @param key
	 * @return String
	 */
	public static String GetString(String key)
	{
		/*Context ctx = LuaForm.GetActiveForm().GetContext().GetContext();
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
		try
		{
			return settings.getString(key, null);
		}
		catch (Exception e) 
		{
			return null;
		}*/
		return "";
	}
	
	/**
	 * Gets number value stored at key
	 * @param key
	 * @return double
	 */
	public static Double GetNumber(String key)
	{
		/*Context ctx = LuaForm.GetActiveForm().GetContext().GetContext();
		SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
		return Double.valueOf(settings.getFloat(key, -1));*/
		return 0.0;
	}
	
	/**
	 * (Ignore)
	 */
	public static int Lua_Index(lua_State L)
	{
		String key = Lua.lua_tostring(L, 2).toString();
		Object val = LuaStore.Get(key);
		if(val == null)
			LuaEngine.getInstance().PushNIL();
		else if(val instanceof String)
			LuaEngine.getInstance().PushString((String) val);
		else
			LuaEngine.getInstance().PushDouble((Double)val);
		return 1;
	}

	/**
	 * (Ignore)
	 */
	public static int Lua_NewIndex(lua_State L)
	{
		String key = Lua.lua_tostring(L, 2).toString();
		if(Lua.lua_isstring(L, 3) != 0)
		{
			String val = Lua.lua_tostring(L, 3).toString();
			LuaStore.SetString(key, val);
		}
		else if(Lua.lua_isnumber(L, 3) != 0)
		{
			Double val = Lua.lua_tonumber(L, 3);
			LuaStore.SetNumber(key, val);
		}
		return 1;
	}

	/**
	 * (Ignore)
	 */
	public static int Lua_GC(lua_State L)
	{
		/*int ptr = 0;
	    Object obj = check(L, 1);
	    if(obj == null)
		    return 0;
	    Lua.lua_getfield(L, Lua.LUA_REGISTRYINDEX, "DO NOT TRASH");
	    if(Lua.lua_istable(L, -1))
	    {
	    	String name = RemoveChar(obj.getClass().getName(), '.');
            Lua.lua_getfield(L, -1, name);
		    if(Lua.lua_isnil(L,-1))
		    {
			    obj = null;
		    }
	    }*/
	    //Lua.lua_pop(L, 3);
	    return 0;
	}

	/**
	 * (Ignore)
	 */
	public static int Lua_ToString(lua_State L)
	{
		Lua.lua_pushstring(L, "STORE");
		return 0;
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
		return "LuaStore";
	}
}
