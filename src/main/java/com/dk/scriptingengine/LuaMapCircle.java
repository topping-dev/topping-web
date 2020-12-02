package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.osspecific.LatLng;

/**
 * Class that is used to create circles on map.
 */
@LuaClass(className = "LuaMapCircle")
public class LuaMapCircle implements LuaInterface
{
	public LatLng center;
	public float radius;
	public boolean draggable;
	public boolean editable;
	public CircleOptions options = new CircleOptions();

	class CircleOptions
	{
		public String strokeColor = "";
		public float strokeWeight;
		public String fillColor = "";
		public float zIndex;
	}

	/**
	 * (Ignore)
	 */
	public LuaMapCircle()
	{
	}

	/**
	 * Set circle center
	 * @param center
	 */
	@LuaFunction(manual = false, methodName = "SetCenter", arguments = { LuaPoint.class })
	public void SetCenter(LuaPoint center)
	{
		this.center = new LatLng(center);
	}

	/**
	 * Set circle center
	 * @param x
	 * @param y
	 */
	@LuaFunction(manual = false, methodName = "SetCenterEx", arguments = { Double.class, Double.class })
	public void SetCenterEx(double x, double y)
	{
		this.center = new LatLng(x, y);
	}

	/**
	 * Set circle radius
	 * @param radius
	 */
	@LuaFunction(manual = false, methodName = "SetRadius", arguments = { Double.class })
	public void SetRadius(double radius)
	{
		this.radius = (float) radius;
	}

	/**
	 * Set circle stroke color
	 * @param color
	 */
	@LuaFunction(manual = false, methodName = "SetStrokeColor", arguments = { LuaColor.class })
	public void SetStrokeColor(LuaColor color)
	{
		options.strokeColor = LuaColor.ColorToHex(color.GetColorValue());
	}

	/**
	 * Set circle stroke color with integer
	 * @param color
	 */
	@LuaFunction(manual = false, methodName = "SetStrokeColorEx", arguments = { Integer.class })
	public void SetStrokeColorEx(int color)
	{
		options.strokeColor = LuaColor.ColorToHex(color);
	}

	/**
	 * Set circle stroke width
	 * @param width
	 */
	@LuaFunction(manual = false, methodName = "SetStrokeWidth", arguments = { Double.class })
	public void SetStrokeWidth(double width)
	{
		options.strokeWeight = (float)width;
	}

	/**
	 * Set circle fill color
	 * @param color
	 */
	@LuaFunction(manual = false, methodName = "SetFillColor", arguments = { LuaColor.class })
	public void SetFillColor(LuaColor color)
	{
		options.fillColor = LuaColor.ColorToHex(color.GetColorValue());
	}

	/**
	 * Set circle fill color with integer
	 * @param color
	 */
	@LuaFunction(manual = false, methodName = "SetFillColorEx", arguments = { Integer.class})
	public void SetFillColorEx(int color)
	{
		options.fillColor = LuaColor.ColorToHex(color);
	}

	/**
	 * Set z-index of circle
	 * @param index
	 */
	@LuaFunction(manual = false, methodName = "SetZIndex", arguments = { Double.class })
	public void SetZIndex(double index)
	{
		options.zIndex = (float) index;
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
		return "LuaMapCircle";
	}
}
