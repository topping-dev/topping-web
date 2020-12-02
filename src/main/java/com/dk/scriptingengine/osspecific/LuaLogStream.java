package com.dk.scriptingengine.osspecific;

import java.io.OutputStream;

import com.luajava.Lua.CharPtr;

public class LuaLogStream extends OutputStream
{

	private int type = 0; //0 debug, 1 error
	
	public LuaLogStream()
	{
		
	}
	
	public LuaLogStream(int type)
	{
		this.type = type;
	}
	
	@Override
	public void write(int oneByte)
	{
		if(type == 0)
			Log.i("LuaLogStream", String.valueOf(oneByte));
		else
			Log.e("LuaLogStream", String.valueOf(oneByte));
	}

	@Override
	public void write(byte[] buffer)
	{
		try
		{
			CharPtr ptr = new CharPtr();
			ptr.setByteArray(buffer);
			if(type == 0)
				Log.i("LuaLogStream", ptr.toString());
			else
				Log.e("LuaLogStream", ptr.toString());
		}
		catch (Exception e) 
		{
			Log.d("LuaLogStream.java", e.getMessage());
		}
	}
	
	@Override
	public void write(byte[] buffer, int offset, int count)
	{
		try
		{
			CharPtr ptr = new CharPtr();
			ptr.setByteArray(buffer);
			if(type == 0)
				Log.i("LuaLogStream", ptr.toString());
			else
				Log.e("LuaLogStream", ptr.toString());
		}
		catch (Exception e) 
		{
			Log.d("LuaLogStream.java", e.getMessage());
		}
	}
}
