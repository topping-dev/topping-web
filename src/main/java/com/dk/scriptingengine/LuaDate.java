package com.dk.scriptingengine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * LuaDate class.
 * LuaDate class is used to manipulate date values.
 */
@LuaClass(className = "LuaDate")
public class LuaDate implements LuaInterface
{
	Calendar dateStore = null;
	
	/**
	 * Returns the current LuaDate
	 * @return LuaDate
	 */
	@LuaFunction(manual = false, methodName = "Now", self = LuaDate.class)
	public static LuaDate Now()
	{
		LuaDate ret = new LuaDate();
		ret.dateStore = Calendar.getInstance(Locale.getDefault());
		return ret;
	}
	
	/**
	 * Creates LuaDate with given parameters
	 * @param day
	 * @param month
	 * @param year
	 * @return LuaDate
	 */
	@LuaFunction(manual = false, methodName = "CreateDate", self = LuaDate.class, arguments = { Integer.class, Integer.class, Integer.class })
	public static LuaDate CreateDate(int day, int month, int year)
	{
		LuaDate ret = new LuaDate();
		ret.dateStore = Calendar.getInstance(Locale.getDefault());
		ret.dateStore.set(year, month - 1, day, 0, 0, 0);
		return ret;
	}
	
	/**
	 * Creates LuaDate with given parameters
	 * @param day
	 * @param month
	 * @param year
	 * @param hour
	 * @param minute
	 * @param second
	 * @return LuaDate
	 */
	@LuaFunction(manual = false, methodName = "CreateDateWithTime", self = LuaDate.class, arguments = { Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
	public static LuaDate CreateDateWithTime(int day, int month, int year, int hour, int minute, int second)
	{
		LuaDate ret = new LuaDate();
		ret.dateStore = Calendar.getInstance(Locale.getDefault());
		ret.dateStore.set(year, month - 1, day, hour, minute, second);
		return ret;
	}
	
	/**
	 * Gets the day of month
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetDay")
	public Integer GetDay()
	{
		return dateStore.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * Sets the day of month
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetDay", arguments = { Integer.class })
	public void SetDay(Integer val)
	{
		dateStore.set(Calendar.DAY_OF_MONTH, val);
	}
	
	/**
	 * Gets the month
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetMonth")
	public Integer GetMonth()
	{
		return dateStore.get(Calendar.MONTH);
	}
	
	/**
	 * Sets the month
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetMonth", arguments = { Integer.class })
	public void SetMonth(Integer val)
	{
		dateStore.set(Calendar.MONTH, val);
	}
	
	/**
	 * Gets the year
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetYear")
	public Integer GetYear()
	{
		return dateStore.get(Calendar.YEAR);
	}
	
	/**
	 * Sets the year
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetYear", arguments = { Integer.class })
	public void SetYear(Integer val)
	{
		dateStore.set(Calendar.YEAR, val);
	}
	
	/**
	 * Gets the hour of the day (24)
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetHour")
	public Integer GetHour()
	{
		return dateStore.get(Calendar.HOUR_OF_DAY);
	}
	
	/**
	 * Sets the hour of the day (24)
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetHour", arguments = { Integer.class })
	public void SetHour(Integer val)
	{
		dateStore.set(Calendar.HOUR_OF_DAY, val);
	}	
	
	/**
	 * Gets the minute
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetMinute")
	public Integer GetMinute()
	{
		return dateStore.get(Calendar.MINUTE);
	}
	
	/**
	 * Set the minute
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetMinute", arguments = { Integer.class })
	public void SetMinute(Integer val)
	{
		dateStore.set(Calendar.MINUTE, val);
	}	
	
	/**
	 * Gets the Second
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetSecond")
	public Integer GetSecond()
	{
		return dateStore.get(Calendar.SECOND);
	}
	
	/**
	 * Sets the second
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetSecond", arguments = { Integer.class })
	public void SetSecond(Integer val)
	{
		dateStore.set(Calendar.SECOND, val);
	}
	
	/**
	 * Gets the millisecond
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetMilliSecond")
	public Integer GetMilliSecond()
	{
		return dateStore.get(Calendar.MILLISECOND);
	}
	
	/**
	 * Sets the millisecond
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetMilliSecond", arguments = { Integer.class })
	public void SetMilliSecond(Integer val)
	{
		dateStore.set(Calendar.MILLISECOND, val);
	}
	
	/**
	 * Gets the string representation of date
	 * 
	 * D 	day in year 	(Number) 	189
	 * E 	day of week 	(Text) 	Tuesday
	 * F 	day of week in month 	(Number) 	2 (2nd Wed in July)
	 * G 	era designator 	(Text) 	AD
	 * H 	hour in day (0-23) 	(Number) 	0
	 * K 	hour in am/pm (0-11) 	(Number) 	0
	 * L 	stand-alone month 	(Text/Number) 	July / 07
	 * M 	month in year 	(Text/Number) 	July / 07
	 * S 	fractional seconds 	(Number) 	978
	 * W 	week in month 	(Number) 	2
	 * Z 	time zone (RFC 822) 	(Timezone) 	-0800
	 * a 	am/pm marker 	(Text) 	PM
	 * c 	stand-alone day of week 	(Text/Number) 	Tuesday / 2
	 * d 	day in month 	(Number) 	10
	 * h 	hour in am/pm (1-12) 	(Number) 	12
	 * k 	hour in day (1-24) 	(Number) 	24
	 * m 	minute in hour 	(Number) 	30
	 * s 	second in minute 	(Number) 	55
	 * w 	week in year 	(Number) 	27
	 * y 	year 	(Number) 	2010
	 * z 	time zone 	(Timezone) 	Pacific Standard Time
	 * ' 	escape for text 	(Delimiter) 	'Date='
	 * '' 	single quote 	(Literal) 	'o''clock'
	 * @param frmt
	 * @return String
	 */
	public String ToString(String frmt)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(frmt);
		return sdf.format(dateStore.getTime());
	}
	
	/**
	 * Frees LuaDate.
	 */
	@LuaFunction(manual = false, methodName = "Free", arguments = {  })
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
		return "LuaDate";
	}
}
