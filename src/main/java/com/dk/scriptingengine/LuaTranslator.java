package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Translates Lua Functions to native functions
 */
@LuaClass(className = "LuaTranslator")
public class LuaTranslator implements LuaInterface
{
    Object obj = "";
    String function = "";

    /**
	 * Creates LuaTranslator Object From Lua.
     * @param obj
     * @param function
     * @return LuaTranslator
     */
    @LuaFunction(manual = false, methodName = "Register", self = LuaTranslator.class, arguments = { Object.class, String.class })
    public static LuaTranslator Register(Object obj, String function)
    {
        return new LuaTranslator(obj, function);
    }

    /**
	 * (Ignore)
	 */
    public LuaTranslator(Object objP, String functionP)
    {
        function = functionP;
        obj = objP;
    }

    /**
	 * (Ignore)
	 */
    public void CallIn(Object ... args)
    {
        LuaEngine.getInstance().OnGuiEvent(obj, function, args);
    }

    /**
	 * (Ignore)
	 */
    public void Call(Object a, Object b)
    {
        CallIn(a, b);
    }
    
    /**
	 * (Ignore)
	 */
    public Object GetObject()
    {
    	return obj;
    }
    
    /**
	 * (Ignore)
	 */
    public String GetFunction()
    {
    	return function;
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
		return "LuaTranslator";
	}
}
