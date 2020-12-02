package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Class that is used to create markers on map.
 */
@LuaClass(className = "LuaMapMarker")
public class LuaMapMarker implements LuaInterface
{
	private LuaPoint position;
	public boolean clickable;
	public boolean draggable;
	public String icon;
	public String title;
	public float opacity;
	public boolean visible;

	/**
	 * (Ignore)
	 */
	public LuaMapMarker(LuaPoint position)
	{
		this.position = position;
	}

	/**
	 * Set marker draggable
	 * @param draggable
	 */
	@LuaFunction(manual = false, methodName="SetDraggable", arguments = { Boolean.class })
	public void SetDraggable(boolean draggable)
	{
		this.draggable = draggable;
	}

	/**
	 * Set marker position
	 * @param point
	 */
	@LuaFunction(manual = false, methodName = "SetPosition", arguments = { LuaPoint.class })
	public void SetPosition(LuaPoint point)
	{
		this.position = position;
	}

	/**
	 * Set marker position
	 * @param x
	 * @param y
	 */
	@LuaFunction(manual = false, methodName = "SetPositionEx", arguments = { Double.class, Double.class })
	public void SetPositionEx(double x, double y)
	{
		this.position = LuaPoint.CreatePointPar((float)x, (float)y);
	}

	/**
	 * Set marker snippet
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "SetSnippet", arguments = { String.class })
	public void SetSnippet(String value)
	{
		//marker.setSnippet(value);
		//TODO:not feasable
	}

	/**
	 * Set marker title
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "SetTitle", arguments = { String.class })
	public void SetTitle(String value)
	{
		this.title = value;
	}

	/**
	 * Set marker visibility
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "SetVisible", arguments = { Boolean.class })
	public void SetVisible(boolean value)
	{
		this.visible = visible;
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
		return "LuaMapMarker";
	}

}
