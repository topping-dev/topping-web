package com.dk.scriptingengine.luagui;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaInterface;

@LuaClass(className = "LuaEventArgs")
public class LuaEventArgs implements LuaInterface
{
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
		return "LuaEventArgs"; 
	}

}
