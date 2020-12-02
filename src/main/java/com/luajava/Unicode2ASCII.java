package com.luajava;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Unicode2ASCII 
{
  public static String toHTML(String unicode) {
    String output = "";
    char[] charArray = unicode.toCharArray();
 
    for (int i = 0; i < charArray.length; ++i) {
      char a = charArray[i];
      if ((int) a > 255) {
        output += "&#" + (int) a + ";";
      } else {
        output += a;
      }
    }
    return output;
  }
  public static String toJAVA(String unicode) {
    String output = "";
    char[] charArray = unicode.toCharArray();
    for (int i = 0; i < charArray.length; ++i) {
      char a = charArray[i];
      if ((int) a > 255) {
        output += "\\u" + Integer.toHexString((int) a);
      } else {
        output += a;
      }
    }
    return output;
  }
  
  public static ByteBuffer toJAVASeq(CharSequence unicode)
  {
	ByteBuffer buf = ByteBuffer.allocate(unicode.length() * 7);
	for (int i = 0; i < unicode.length(); ++i) 
	{
		char a = unicode.charAt(i);
		if ((int) a > 255) 
		{
			String output = "\\u" + Integer.toHexString((int) a);
			try
			{
				buf.put(output.getBytes("ASCII"));
			}
			catch(UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		} 
		else
		{
			buf.putChar(a);
		}
	}
	return buf;
  }
}
