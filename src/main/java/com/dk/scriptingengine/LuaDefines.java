package com.dk.scriptingengine;

import java.util.Calendar;
import java.util.List;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LuaDefines")
public class LuaDefines implements LuaInterface
{
	public static String TAG = "LuaDefines";
	
	//TODO:Add lua date
	@LuaFunction(manual = false, methodName = "GetHumanReadableDate", self = LuaDefines.class, arguments = { Integer.class })
	public static String GetHumanReadableDate(Integer value)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		return day + "." + month + "." + year /*+ " " + hour + ":" + month + ":" + second*/;
	}

	@LuaFunction(manual = false, methodName = "RegisterAndConnectWifi", self = LuaDefines.class, arguments = { LuaContext.class, String.class, String.class })
	public static void RegisterAndConnectWifi(LuaContext lc, String ssid, String password)
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
		return "LuaDefines";
	}
}
