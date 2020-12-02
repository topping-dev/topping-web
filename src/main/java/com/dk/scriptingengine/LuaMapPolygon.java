package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.osspecific.LatLng;

import java.util.ArrayList;

/**
 * Class that is used to create polygons on map.
 */
@LuaClass(className = "LuaMapPolygon")
public class LuaMapPolygon implements LuaInterface
{
	public boolean draggable;
	public boolean editable;
	public ArrayList<LatLng> path;
	public ArrayList<ArrayList<LatLng>> paths;
	public PolygonOptions options = new PolygonOptions();

	public class PolygonOptions
	{
		public String strokeColor = "";
		public float strokeWeight;
		public String fillColor = "";
		public float zIndex;
		public boolean visible;
	}

	/**
	 * (Ignore)
	 */
	public LuaMapPolygon()
	{
	}

	/**
	 * Sets the fill color
	 * @param color
	 */
	@LuaFunction(manual = false, methodName = "SetFillColor", arguments = { LuaColor.class })
	public void SetFillColor(LuaColor color)
	{
		options.fillColor = LuaColor.ColorToHex(color.GetColorValue());
	}

	/**
	 * Sets the stroke color
	 * @param color
	 */
	@LuaFunction(manual = false, methodName = "SetStrokeColor", arguments = { LuaColor.class })
	public void SetStrokeColor(LuaColor color)
	{
		options.strokeColor = LuaColor.ColorToHex(color.GetColorValue());
	}

	/**
	 * Sets the stroke width
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "SetStrokeWidth", arguments = { Float.class })
	public void SetStrokeWidth(float value)
	{
		options.strokeWeight = value;
	}

	/**
	 * Sets the visibility
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "SetVisible", arguments = { Boolean.class })
	public void SetVisible(boolean value)
	{
		options.visible = value;
	}

	/**
	 * Sets the z-index
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "SetZIndex", arguments = { Float.class })
	public void SetZIndex(float value)
	{
		options.zIndex = value;
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
		return "LuaMapPolygon";
	}

}
