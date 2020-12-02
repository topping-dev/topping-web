package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Object to store native objects that are not registered on lua engine.
 */
@LuaClass(className = "LuaNativeObject")
public class LuaNativeObject implements LuaInterface
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
		return "LuaNativeObject";
	}
}
