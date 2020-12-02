package com.luajava;

//----------------------------------------------------------------------------------------
//	Copyright  - 2011 Tangible Software Solutions Inc.
//	This class can be used by anyone provided that the copyright notice remains intact.
//
//	This class is used to simulate some .NET date properties in Java.
//----------------------------------------------------------------------------------------
public final class DotNetToJavaDateHelper
{
	public static int DatePart(int calendarDatePart, java.util.Date date)
	{
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(date);
		return c.get(calendarDatePart);
	}
}