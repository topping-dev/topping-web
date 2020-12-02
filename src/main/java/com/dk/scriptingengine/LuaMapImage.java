package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Class that is used to create images on map.
 */
@LuaClass(className = "LuaMapImage")
public class LuaMapImage implements LuaInterface
{
	public String image;
	public float bearing;
	public LuaPoint point;
	public float width;
	public float height;
	public float opacity;
	public boolean visible;
	public float zIndex;
	public MapImageBounds bounds;

	class MapImageBounds
	{
		public float north;
		public float south;
		public float east;
		public float west;
	}

	/**
	 * (Ignore)
	 */
	public LuaMapImage(String image)
	{
		this.image = image;
	}

	/**
	 * Sets the bearing of image
	 * @param bearing
	 */
	@LuaFunction(manual = false, methodName = "SetBearing", arguments = { Float.class })
	public void SetBearing(float bearing)
	{
		this.bearing = bearing;
	}

	/**
	 * Sets the dimension of image, height automatically calculated
	 * @param width
	 */
	@LuaFunction(manual = false, methodName = "SetDimensions", arguments = { Float.class })
	public void SetDimensions(float width)
	{
		this.width = width;
		this.height = width;
		updateBounds();
	}

	/**
	 * Sets the dimesion of image
	 * @param width
	 * @param height
	 */
	@LuaFunction(manual = false, methodName = "SetDimensionsEx", arguments = { Float.class, Float.class })
	public void SetDimensionsEx(float width, float height)
	{
		this.width = width;
		this.height = height;
		updateBounds();
	}

	/**
	 * Sets the position of image
	 * @param point
	 */
	@LuaFunction(manual = false, methodName = "SetPosition", arguments = { LuaPoint.class })
	public void SetPosition(LuaPoint point)
	{
		this.point = point;
		updateBounds();
	}

	/**
	 * Sets the position of the image
	 * @param x
	 * @param y
	 */
	@LuaFunction(manual = false, methodName = "SetPositionEx", arguments = { Float.class, Float.class })
	public void SetPositionEx(float x, float y)
	{
		this.point = LuaPoint.CreatePointPar(x, y);
		updateBounds();
	}

	/*public void SetPositionFromBound(LuaPoint point)
	{
	}*/

	/**
	 * Sets the opacity of the image
	 * @param transperency
	 */
	@LuaFunction(manual = false, methodName = "SetTransparency", arguments = { Float.class })
	public void SetTransparency(float transperency)
	{
		this.opacity = transperency;
	}

	/**
	 * Sets the visibility of the image
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "SetVisible", arguments = { Boolean.class })
	public void SetVisible(boolean value)
	{
		this.visible = value;
	}

	/**
	 * Sets the z-index of the image
	 * @param index
	 */
	@LuaFunction(manual = false, methodName = "SetZIndex", arguments = { Float.class })
	public void SetZIndex(float index)
	{
		this.zIndex = index;
	}

	public void updateBounds()
	{
		MapImageBounds mib = new MapImageBounds();

		float x = this.point.GetX();
		float y = this.point.GetY();

		float halfWidth = width / 2.0f;
		mib.west = x - halfWidth;
		mib.east = x + halfWidth;
		float halfHeight = height / 2.0f;
		mib.north = y - halfHeight;
		mib.south = y + halfHeight;
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
		return "LuaMapImage";
	}
	
}
