package com.dk.scriptingengine.backend;

import java.util.ArrayList;
import java.util.HashMap;

public class LuaHelper
{
	public static <T> ArrayList<T> ToArray(HashMap<Integer, T> values)
	{
		ArrayList<T> arr = new ArrayList<T>();
		for(int i = 0; i < values.size(); i++)
		{
			arr.add(values.get(i));
		}
		return arr;
	}
}
