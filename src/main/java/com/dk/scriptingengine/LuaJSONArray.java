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
@LuaClass(className = "LuaJSONArray")
public class LuaJSONArray implements LuaInterface
{
	public JSONArray jsa;

	/**
	 * Get array count.
	 * @return int count of array.
	 */
	@LuaFunction(manual = false, methodName = "Count", arguments = { })
	public int Count()
	{
		return jsa.length();
	}
	
	/**
	 * Get object value at index.
	 * @param index value.
	 * @return LuaJSONObject
	 */
	@LuaFunction(manual = false, methodName = "GetJSONObject", arguments = { Integer.class })
	public LuaJSONObject GetJSONObject(Integer index)
	{
		LuaJSONObject lso = new LuaJSONObject();
		try
		{
			lso.jso = jsa.getJSONObject(index);
		}
		catch(JSONException e)
		{
			lso.jso = new JSONObject();
		}
		return lso;
	}
	
	/**
	 * Get array value at index.
	 * @param index value.
	 * @return LuaJSONArray
	 */
	@LuaFunction(manual = false, methodName = "GetJSONArray", arguments = { Integer.class })
	public LuaJSONArray GetJSONArray(Integer index)
	{
		LuaJSONArray lsa = new LuaJSONArray();
		try
		{
			lsa.jsa = jsa.getJSONArray(index);
		}
		catch(JSONException e)
		{
			lsa.jsa = new JSONArray();
		}
		return lsa;
	}
	
	/**
	 * Get string value at name.
	 * @param index value.
	 * @return String value.
	 */
	@LuaFunction(manual = false, methodName = "GetString", arguments = { Integer.class })
	public String GetString(Integer index)
	{
		try
		{
			return jsa.getString(index);
		}
		catch(JSONException e)
		{
			return "error" + e.getMessage() != null ? e.getMessage() : "";
		}
	}
	
	/**
	 * Get int value at name.
	 * @param index value.
	 * @return int value.
	 */
	@LuaFunction(manual = false, methodName = "GetInt", arguments = { Integer.class })
	public Integer GetInt(Integer index)
	{
		try
		{
			return jsa.getInt(index);
		}
		catch(JSONException e)
		{
			return -Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Get double value at name.
	 * @param index value.
	 * @return double value.
	 */
	@LuaFunction(manual = false, methodName = "GetDouble", arguments = { Integer.class })
	public Double GetDouble(Integer index)
	{
		try
		{
			return jsa.getDouble(index);
		}
		catch(JSONException e)
		{
			return (double)-Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Get float value at name.
	 * @param index value.
	 * @return float value.
	 */
	@LuaFunction(manual = false, methodName = "GetFloat", arguments = { Integer.class })
	public Float GetFloat(Integer index)
	{
		try
		{
			return (float)jsa.getDouble(index);
		}
		catch(JSONException e)
		{
			return (float)-Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Get boolean value at name.
	 * @param index value.
	 * @return boolean value.
	 */
	@LuaFunction(manual = false, methodName = "GetBool", arguments = { Integer.class })
	public Boolean GetBool(Integer index)
	{
		try
		{
			return jsa.getBoolean(index);
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
		return "LuaJSONArray";
	}

}
