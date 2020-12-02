package com.dk.scriptingengine.luagui;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LuaContext")
public class LuaContext implements LuaInterface
{
	private Context context;
	
	/**
	 * Creates LuaContext Object From Lua.
	 * @param context
	 * @return LuaContext
	 */
	@LuaFunction(manual = false, methodName = "CreateLuaContext", self = LuaContext.class)
	public static LuaContext CreateLuaContext(Context context)
	{
		LuaContext lc = new LuaContext();
		lc.SetContext(context);
		return lc;
	}
	
	/**
	 * Gets context
	 * @return Context value
	 */
	@LuaFunction(manual = false, methodName = "GetContext")
	public Context GetContext() { return context; }
	
	/**
	 * (Ignore)
	 */
	public void SetContext(Context val) { context = val; }
	
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
		return "LuaContext";
	}	
}
