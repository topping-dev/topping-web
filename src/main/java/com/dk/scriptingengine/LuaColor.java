package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaGlobalInt;
import com.dk.scriptingengine.backend.LuaGlobalString;
import com.dk.scriptingengine.backend.LuaInterface;

import java.util.HashMap;
import java.util.Locale;

@LuaClass(className = "LuaDate")
@LuaGlobalInt(
		keys = { 
					"LuaColor_BLACK",
					"LuaColor_BLUE",
					"LuaColor_CYAN",
					"LuaColor_DKGRAY",
					"LuaColor_GRAY",
					"LuaColor_GREEN",
					"LuaColor_LTGRAY",
					"LuaColor_MAGENTA",
					"LuaColor_RED",
					"LuaColor_TRANSPARENT",
					"LuaColor_WHITE",
			    	"LuaColor_YELLOW"
			   },
		vals = {
                    0xFF000000,
                    0xFF0000FF,
                    0xFF00FFFF,
                    0xFF444444,
                    0xFF888888,
                    0xFF00FF00,
                    0xFFCCCCCC,
                    0xFFFF00FF,
                    0xFFFF0000,
                    0x00000000,
                    0xFFFFFFFF,
                    0xFFFFFF00
			   }
		)
public class LuaColor implements LuaInterface
{
	private String colorValue;
	private int colorValueInt;

    public static final String BLACK = "#000000";
    public static final String BLUE = "#0000ff";
    public static final String CYAN = "#00ffff";
    public static final String DKGRAY = "#444444";
    public static final String GRAY = "#888888";
    public static final String GREEN = "#00ff00";
    public static final String LTGRAY = "#cccccc";
    public static final String MAGENTA = "#ff00ff";
    public static final String RED = "#ff0000";
    public static final String TRANSPARENT = "#00000000";
    public static final String WHITE = "#ffffff";
    public static final String YELLOW = "#ffff00";
    private static HashMap<String, Integer> sColorNameMap;
    static
    {
        sColorNameMap = new HashMap<>();
        sColorNameMap.put("black", HexToColor(BLACK));
        sColorNameMap.put("blue", HexToColor(BLUE));
        sColorNameMap.put("cyan", HexToColor(CYAN));
        sColorNameMap.put("dkgray", HexToColor(DKGRAY));
        sColorNameMap.put("gray", HexToColor(GRAY));
        sColorNameMap.put("green", HexToColor(GREEN));
        sColorNameMap.put("ltgray", HexToColor(LTGRAY));
        sColorNameMap.put("magenta", HexToColor(MAGENTA));
        sColorNameMap.put("red", HexToColor(RED));
        sColorNameMap.put("transparent", HexToColor(TRANSPARENT));
        sColorNameMap.put("white", HexToColor(WHITE));
        sColorNameMap.put("yellow", HexToColor(YELLOW));
    }
	
	@SuppressWarnings("unused")
	/**
	 * (Global)
	 */
	private enum ColorInternal
	{
		LuaColor_BLACK("#000000"),
		LuaColor_BLUE("#0000ff"),
		LuaColor_CYAN("#00ffff"),
		LuaColor_DKGRAY("#444444"),
		LuaColor_GRAY("#888888"),
		LuaColor_GREEN("#00ff00"),
		LuaColor_LTGRAY("#cccccc"),
		LuaColor_MAGENTA("#ff00ff"),
		LuaColor_RED("#ff0000"),
		LuaColor_TRANSPARENT("#00000000"),
		LuaColor_WHITE("#ffffff"),
		LuaColor_YELLOW("#ffff00");

		private final String val;

		ColorInternal(String val)
		{
			this.val = val;
		}

		public String value()
		{
			return this.val;
		}
	}

	/**
	 * Returns LuaColor from string value.
	 * Example #ffffffff or #ffffff
	 * @param colorStr
	 * @return LuaColor
	 */
	@LuaFunction(manual = false, methodName = "FromString", self = LuaColor.class, arguments = { String.class })
	public static LuaColor FromString(String colorStr)
	{
		LuaColor color = new LuaColor();
		color.colorValue = colorStr;
		color.colorValueInt = HexToColor(colorStr);
		return color;
	}
	
	/**
	 * Returns LuaColor from argb.
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 * @return LuaColor
	 */
	@LuaFunction(manual = false, methodName = "CreateFromARGB", self = LuaColor.class, arguments = { Integer.class, Integer.class, Integer.class, Integer.class })
	public static LuaColor CreateFromARGB(int alpha, int red, int green, int blue)
	{
	    StringBuilder sb = new StringBuilder();
	    sb.append("#");
	    sb.append(Integer.toHexString(red));
	    sb.append(Integer.toHexString(green));
	    sb.append(Integer.toHexString(blue));
		sb.append(Integer.toHexString(alpha));
		LuaColor color = new LuaColor();
		color.colorValue = sb.toString();
		color.colorValueInt = HexToColor(color.colorValue);
		return color;
	}
	
	/**
	 * Returns LuaColor from rgb.
	 * @param red
	 * @param green
	 * @param blue
	 * @return LuaColor
	 */
	@LuaFunction(manual = false, methodName = "CreateFromRGB", self = LuaColor.class, arguments = { Integer.class, Integer.class, Integer.class })
	public static LuaColor CreateFromRGB(int red, int green, int blue)
	{
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        sb.append(Integer.toHexString(red));
        sb.append(Integer.toHexString(green));
        sb.append(Integer.toHexString(blue));
        LuaColor color = new LuaColor();
        color.colorValue = sb.toString();
        color.colorValueInt = HexToColor(color.colorValue);
        return color;
	}
	
	/**
	 * Returns the integer color value
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetColorValue")
	public int GetColorValue()
	{
		return colorValueInt;
	}

    /**
     * (Ignore)
     */
	public static int alpha(int color) {
		return color >>> 24;
	}

    /**
     * (Ignore)
     */
	public static int red(int color) {
		return (color >> 16) & 0xFF;
	}

    /**
     * (Ignore)
     */
	public static int green(int color) {
		return (color >> 8) & 0xFF;
	}

    /**
     * (Ignore)
     */
	public static int blue(int color) {
		return color & 0xFF;
	}

	/**
	 * (Ignore)
	 */
	public static int rgb(
			int red,
			int green,
			int blue) {
		return 0xff000000 | (red << 16) | (green << 8) | blue;
	}

	/**
	 * (Ignore)
	 */
	public static int rgb(float red, float green, float blue) {
		return 0xff000000 |
				((int) (red   * 255.0f + 0.5f) << 16) |
				((int) (green * 255.0f + 0.5f) <<  8) |
				(int) (blue  * 255.0f + 0.5f);
	}

	/**
	 * (Ignore)
	 */
	public static int argb(
			int alpha,
			int red,
			int green,
			int blue) {
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	/**
	 * (Ignore)
	 */
	public static int argb(float alpha, float red, float green, float blue) {
		return ((int) (alpha * 255.0f + 0.5f) << 24) |
				((int) (red   * 255.0f + 0.5f) << 16) |
				((int) (green * 255.0f + 0.5f) <<  8) |
				(int) (blue  * 255.0f + 0.5f);
	}

	public static int HexToColor(String colorString)
    {
        if (colorString.charAt(0) == '#') {
        	String colorToWork = colorString;
        	String alpha = "";
        	if(colorString.length() == 9)
			{
				colorToWork = colorToWork.substring(0, 7);
				alpha = colorString.substring(7, 9);
			}
			else if(colorString.length() == 7)
			{

			}
			else
			{
				throw new IllegalArgumentException("Unknown color");
			}
            // Use a long to avoid rollovers on #ffXXXXXX
            long color = Long.parseLong(colorToWork.substring(1), 16);
        	if(!alpha.equals(""))
			{
 				long alphaI = Long.parseLong(alpha, 16);
				color = color | (alphaI << 24);
			}
			else
				color |= 0x00000000ff000000;
            return (int)color;
        } else {
            Integer color = sColorNameMap.get(colorString.toLowerCase(Locale.ROOT));
            if (color != null) {
                return color;
            }
        }
        return HexToColor(WHITE);
    }

    public static String ColorToHex(int color)
    {
        int alpha = alpha(color);
        int blue = blue(color);
        int green = green(color);
        int red = red(color);

        String alphaHex = To00Hex(alpha);
        String blueHex = To00Hex(blue);
        String greenHex = To00Hex(green);
        String redHex = To00Hex(red);

        // hexBinary value: aabbggrr
        StringBuilder str = new StringBuilder("#");
        str.append(blueHex);
        str.append(greenHex);
        str.append(redHex );
		str.append(alphaHex);

        return str.toString();
    }

    private static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length()-2, hex.length());
    }
	
	/**
	 * Frees LuaColor.
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
		return "LuaColor";
	}
}
