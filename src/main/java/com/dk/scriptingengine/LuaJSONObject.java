package com.dk.scriptingengine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Class that handles JSON Object.
 */
@LuaClass(className = "LuaJSONObject")
public class LuaJSONObject implements LuaInterface
{
	public JSONObject jso;
	/**
	 * Creates LuaJSON from json string.
	 * @param str
	 * return LuaJSONObject
	 */
	@LuaFunction(manual = false, methodName = "CreateJSOFromString", self = LuaJSONObject.class, arguments = { String.class })
	public static LuaJSONObject CreateJSOFromString(String str)
	{
		LuaJSONObject lso = new LuaJSONObject();
		try
		{
			lso.jso = new JSONObject(str);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return lso;
	}
	
	/**
	 * Get object value at name.
	 * @param name Name value.
	 * @return LuaJSONObject
	 */
	@LuaFunction(manual = false, methodName = "GetJSONObject", arguments = { String.class })
	public LuaJSONObject GetJSONObject(String name)
	{
		LuaJSONObject lso = new LuaJSONObject();
		try
		{
			lso.jso = jso.getJSONObject(name);
		}
		catch(JSONException e)
		{
			lso.jso = new JSONObject();
		}
		return lso;
	}
	
	/**
	 * Get array value at name.
	 * @param name Name value.
	 * @return LuaJSONArray
	 */
	@LuaFunction(manual = false, methodName = "GetJSONArray", arguments = { String.class })
	public LuaJSONArray GetJSONArray(String name)
	{
		LuaJSONArray lsa = new LuaJSONArray();
		try
		{
			lsa.jsa = jso.getJSONArray(name);
		}
		catch(JSONException e)
		{
			lsa.jsa = new JSONArray();
		}
		return lsa;
	}
	
	/**
	 * Get string value at name.
	 * @param name Name value.
	 * @return String value.
	 */
	@LuaFunction(manual = false, methodName = "GetString", arguments = { String.class })
	public String GetString(String name)
	{
		try
		{
			return jso.getString(name);
		}
		catch(JSONException e)
		{
			return "error" + e.getMessage() != null ? e.getMessage() : "";
		}
	}
	
	/**
	 * Get int value at name.
	 * @param name Name value.
	 * @return int value.
	 */
	@LuaFunction(manual = false, methodName = "GetInt", arguments = { String.class })
	public Integer GetInt(String name)
	{
		try
		{
			return jso.getInt(name);
		}
		catch(JSONException e)
		{
			return -Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Get double value at name.
	 * @param name Name value.
	 * @return double value.
	 */
	@LuaFunction(manual = false, methodName = "GetDouble", arguments = { String.class })
	public Double GetDouble(String name)
	{
		try
		{
			return jso.getDouble(name);
		}
		catch(JSONException e)
		{
			return (double)-Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Get float value at name.
	 * @param name Name value.
	 * @return float value.
	 */
	@LuaFunction(manual = false, methodName = "GetFloat", arguments = { String.class })
	public Float GetFloat(String name)
	{
		try
		{
			return (float)jso.getDouble(name);
		}
		catch(JSONException e)
		{
			return (float)-Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Get boolean value at name.
	 * @param name Name value.
	 * @return boolean value.
	 */
	@LuaFunction(manual = false, methodName = "GetBool", arguments = { String.class })
	public Boolean GetBool(String name)
	{
		try
		{
			return jso.getBoolean(name);
		}
		catch(JSONException e)
		{
			return false;
		}
	}
	
	/**
	 * Frees the object.
	 */
	@LuaFunction(manual = false, methodName = "Free", arguments = { })
	public void Free()
	{
		
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
		return "LuaJSONObject";
	}

}
