package com.dk.scriptingengine;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaHelper;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Call native functions from lua
 */
@LuaClass(className = "LuaNativeCall")
public class LuaNativeCall implements LuaInterface
{
	/**
	 * Call native function on underlying system
	 * @param self
	 * @param func
	 * @param params
	 * @return LuaObjectStore
	 */
	@LuaFunction(manual = false, methodName = "Call", arguments = { Object.class, String.class, HashMap.class })
	public static LuaObjectStore Call(Object self, String func, HashMap<Integer, Object> params)
	{
		Method[] methods = self.getClass().getMethods();
		LuaObjectStore los = new LuaObjectStore();
		ArrayList<Object> arr = LuaHelper.ToArray(params);
		for(Method m : methods)
		{
			if(m.getName().compareTo(func) == 0)
			{
				try
				{
					los.obj = m.invoke(self, arr.toArray());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return los;
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
		return "LuaNativeCall";
	}

}
