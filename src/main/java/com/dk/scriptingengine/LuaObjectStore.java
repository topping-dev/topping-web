package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Object store to store c objects sent and received from lua engine.
 */
@LuaClass(className = "LuaObjectStore")
public class LuaObjectStore implements LuaInterface
{
	/**
	 * Object that sent and received.
	 */
	public Object obj;

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
		return "LuaObjectStore";
	}
}
