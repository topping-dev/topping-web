package com.luajava;

import java.io.FileOutputStream;
import java.io.IOException;

import com.dk.scriptingengine.osspecific.Log;
import com.luajava.Lua.CharPtr;
import com.luajava.Lua.lua_Debug;
import com.dk.scriptingengine.LuaEngine;

//FUCK TODO TASK: There is no preprocessor in Java:
///#region Usings
//FUCK TODO TASK: There is no preprocessor in Java:
///#endregion

public final class Tools
{
	public static void LogException(String tag, Exception e)
	{
		lua_Debug ld = new lua_Debug();
		try
		{
			Lua.lua_getinfo(LuaEngine.getInstance().GetLuaState(), new CharPtr("S"), ld);
		}
		catch (Exception ex) 
		{
			ld.source = new CharPtr("");
			Log.e("Tools.java", "Cannot parse lua debug");
		}
		StringBuilder exStr = new StringBuilder();
		if(e.getMessage() == null)
			exStr.append(e.toString());
		else
			exStr.append(e.getMessage());
		exStr.append("\n");
		StackTraceElement[] arr = new Throwable().getStackTrace();
		if(arr != null)
		{
			for(int i = 0; i < arr.length; i++)
			{
				exStr.append(arr[i] + "\n");
			}
		}
		exStr.append("Lua: ").append(ld.source.toString()).append(" ").append(ld.currentline).append("\n");
		Log.e(tag, exStr.toString());
	}
	
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region Public Methods
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region IsNumericType
    /** 
     Determines whether the specified value is of numeric type.
     
     @param o The object to check.
     @return 
     	<c>true</c> if o is a numeric type; otherwise, <c>false</c>.
     
    */
    public static boolean IsNumericType(Object o)
    {
        return (o instanceof Byte || o instanceof Byte || o instanceof Short || o instanceof Short || o instanceof Integer || o instanceof Integer || o instanceof Long || o instanceof Long || o instanceof Float || o instanceof Double || o instanceof java.math.BigDecimal);
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region IsPositive
    /** 
     Determines whether the specified value is positive.
     
     @param Value The value.
     @param ZeroIsPositive if set to <c>true</c> treats 0 as positive.
     @return 
     	<c>true</c> if the specified value is positive; otherwise, <c>false</c>.
     
    */
    public static boolean IsPositive(Object Value, boolean ZeroIsPositive)
    {
    	Class<?> c = Value.getClass();
    	String name = c.getName();
    	if(name == "Byte")
    		return (ZeroIsPositive ? ((Byte)Value).byteValue() >= 0 : ((Byte)Value).byteValue() > 0);
    	else if(name == "Short")
    		return (ZeroIsPositive ? ((Short)Value).shortValue() >= 0 : ((Short)Value).shortValue() > 0);
    	else if(name == "Integer")
    		return (ZeroIsPositive ? ((Integer)Value).intValue() >= 0 : ((Integer)Value).intValue() > 0);
    	else if(name == "Long")
    		return (ZeroIsPositive ? ((Long)Value).longValue() >= 0 : ((Long)Value).longValue() > 0);
    	else if(name == "Character")
    		return (ZeroIsPositive ? true : ((Character)Value).charValue() != '\0');
    	else if(name == "Float")
    		return (ZeroIsPositive ? ((Float)Value).intValue() >= 0 : ((Float)Value).intValue() > 0);
    	else if(name == "Double")
    		return (ZeroIsPositive ? ((Double)Value).intValue() >= 0 : ((Double)Value).intValue() > 0);
    	else 
    		return false;
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region ToUnsigned
    /** 
     Converts the specified values boxed type to its correpsonding unsigned
     type.
     
     @param Value The value.
     @return A boxed numeric object whos type is unsigned.
    */
    public static Object ToUnsigned(Object Value)
    {
    	return Value;
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region ToInteger
    /** 
     Converts the specified values boxed type to its correpsonding integer
     type.
     
     @param Value The value.
     @return A boxed numeric object whos type is an integer type.
    */
    public static Object ToInteger(Object Value, boolean Round)
    {
    	Class<?> c = Value.getClass();
    	String name = c.getName();
    	if(name == "Byte"
    		|| name == "Short"
    		|| name == "Integer"
    		|| name == "Long")
    		return Value;
    	else if(name == "Float")
    		return (Round ? (int)Math.round(((Float)Value).floatValue()) : (int)(((Float)Value).floatValue()));
    	else if(name == "Double")
    		return (Round ? (long)Math.round(((Double)Value).doubleValue()) : (long)(((Double)Value).doubleValue()));
    	else 
    		return null;
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region UnboxToLong
    public static long UnboxToLong(Object Value, boolean Round)
    {
    	Class<?> c = Value.getClass();
    	String name = c.getName();
    	if(name == "Byte")
    		return (long)(((Byte)Value).byteValue());
    	else if(name == "Short")
    		return (long)(((Short)Value).shortValue());
    	else if(name == "Integer")
    		return (long)(((Integer)Value).intValue());
    	else if(name == "Long")
    		return ((Long)Value).longValue();
    	else if(name == "Float")
    		return (Round ? (long)Math.round(((Float)Value).floatValue()) : (long)(((Float)Value).floatValue()));
    	else if(name == "Double")
    		return (Round ? (long)Math.round(((Double)Value).doubleValue()) : (long)(((Double)Value).doubleValue()));
    	else 
    		return 0;    	
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region ReplaceMetaChars
    /** 
     Replaces the string representations of meta chars with their corresponding
     character values.
     
     @param input The input.
     @return A string with all string meta chars are replaced
    */
    public static String ReplaceMetaChars(String input)
    {
        //return Regex.Replace(input, "(\\\\)(\\d{3}|[^\\d])?", new Matcher(ReplaceMetaCharsMatch));
    	return input;
    }
    /*private static String ReplaceMetaCharsMatch(Match m)
    {
        // convert octal quotes (like \040)
        if (m.Groups[2].getLength() == 3)
        {
            return Convert.ToChar(Byte.parseByte(String.format(8, m.Groups[2].Value))).toString();
        }
        else
        {
            // convert all other special meta characters
            //TODO: \xhhh hex and possible dec !!
//FUCK NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
//            switch (m.Groups[2].Value)
//ORIGINAL LINE: case "0":
            if (m.Groups[2].Value.equals("0")) // null
            {
                    return "\0";
            }
//ORIGINAL LINE: case "a":
            else if (m.Groups[2].Value.equals("a")) // alert (beep)
            {
                    return "\a";
            }
//ORIGINAL LINE: case "b":
            else if (m.Groups[2].Value.equals("b")) // BS
            {
                    return "\b";
            }
//ORIGINAL LINE: case "f":
            else if (m.Groups[2].Value.equals("f")) // FF
            {
                    return "\f";
            }
//ORIGINAL LINE: case "v":
            else if (m.Groups[2].Value.equals("v")) // vertical tab
            {
                    return "\v";
            }
//ORIGINAL LINE: case "r":
            else if (m.Groups[2].Value.equals("r")) // CR
            {
                    return "\r";
            }
//ORIGINAL LINE: case "n":
            else if (m.Groups[2].Value.equals("n")) // LF
            {
                    return "\n";
            }
//ORIGINAL LINE: case "t":
            else if (m.Groups[2].Value.equals("t")) // Tab
            {
                    return "\t";
            }
            else
            {
                    // if neither an octal quote nor a special meta character
                    // so just remove the backslash
                    return m.Groups[2].Value;
            }
        }
    }*/
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region printf
    public static void printf(String Format, Object... Parameters)
    {
    	Log.i("LuaEngine", Tools.sprintf(Format, Parameters));
        System.out.print(Tools.sprintf(Format, Parameters));
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region fprintf
    public static void fprintf(FileOutputStream Destination, String Format, Object... Parameters)
    {
        try {
			Destination.write(Tools.sprintf(Format, Parameters).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    //public static Regex r = new Regex("\\%(\\d*\\$)?([\\'\\#\\-\\+ ]*)(\\d*)(?:\\.(\\d+))?([hl])?([dioxXucsfeEgGpn%])");
    
   public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);  
   }

   public static String padLeft(String s, int n) {
       return String.format("%1$#" + n + "s", s);  
   }
    

//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region sprintf
    public static String sprintf(String Format, Object... Parameters)
    {
    	return String.format(Format, Parameters);
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion

//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region Private Methods
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region FormatOCT
    private static String FormatOct(String NativeFormat, boolean Alternate, int FieldLength, int FieldPrecision, boolean Left2Right, char Padding, Object Value)
    {
        String w = "";
        String lengthFormat = "{0" + (FieldLength != Integer.MIN_VALUE ? "," + (Left2Right ? "-" : "") + Integer.toString(FieldLength) : "") + "}";

        if (IsNumericType(Value))
        {
            w = String.valueOf(Long.toString(UnboxToLong(Value, true)));

            if (Left2Right || Padding == ' ')
            {
                if (Alternate && !w.equals("0"))
                {
                    w = "0" + w;
                }
                w = String.format(lengthFormat, w);
            }
            else
            {
                if (FieldLength != Integer.MIN_VALUE)
                {
                    w = padLeft(w, FieldLength - (Alternate && !w.equals("0") ? 1 : 0)/*, Padding*/);
                }
                if (Alternate && !w.equals("0"))
                {
                    w = "0" + w;
                }
            }
        }

        return w;
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region FormatHEX
    private static String FormatHex(String NativeFormat, boolean Alternate, int FieldLength, int FieldPrecision, boolean Left2Right, char Padding, Object Value)
    {
        String w = "";
        String lengthFormat = "{0" + (FieldLength != Integer.MIN_VALUE ? "," + (Left2Right ? "-" : "") + Integer.toString(FieldLength) : "") + "}";
        String numberFormat = "{0:" + NativeFormat + (FieldPrecision != Integer.MIN_VALUE ? Integer.toString(FieldPrecision) : "") + "}";

        if (IsNumericType(Value))
        {
            w = String.format(numberFormat, Value);

            if (Left2Right || Padding == ' ')
            {
                if (Alternate)
                {
                    w = (NativeFormat.equals("x") ? "0x" : "0X") + w;
                }
                w = String.format(lengthFormat, w);
            }
            else
            {
                if (FieldLength != Integer.MIN_VALUE)
                {
                    w = padLeft(w, FieldLength - (Alternate ? 2 : 0)/*, Padding*/);
                }
                if (Alternate)
                {
                    w = (NativeFormat.equals("x") ? "0x" : "0X") + w;
                }
            }
        }

        return w;
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#region FormatNumber
    private static String FormatNumber(String NativeFormat, boolean Alternate, int FieldLength, int FieldPrecision, boolean Left2Right, boolean PositiveSign, boolean PositiveSpace, char Padding, Object Value)
    {
        String w = "";
        String lengthFormat = "{0" + (FieldLength != Integer.MIN_VALUE ? "," + (Left2Right ? "-" : "") + Integer.toString(FieldLength) : "") + "}";
        String numberFormat = "{0:" + NativeFormat + (FieldPrecision != Integer.MIN_VALUE ? Integer.toString(FieldLength) : "0") + "}";

        if (IsNumericType(Value))
        {
            w = String.format(numberFormat, Value);

            if (Left2Right || Padding == ' ')
            {
                if (IsPositive(Value, true))
                {
                    w = (PositiveSign ? "+" : (PositiveSpace ? " " : "")) + w;
                }
                w = String.format(lengthFormat, w);
            }
            else
            {
                if (w.startsWith("-"))
                {
                    w = w.substring(1);
                }
                if (FieldLength != Integer.MIN_VALUE)
                {
                    w = padLeft(w, FieldLength - 1/*, Padding*/);
                }
                if (IsPositive(Value, true))
                {
                    w = (PositiveSign ? "+" : (PositiveSpace ? " " : (FieldLength != Integer.MIN_VALUE ? Padding : ""))) + w;
                }
                else
                {
                    w = "-" + w;
                }
            }
        }

        return w;
    }
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
//FUCK TODO TASK: There is no preprocessor in Java:
        ///#endregion
}