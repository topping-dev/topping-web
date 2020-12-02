package com.dk.scriptingengine;

import java.nio.charset.Charset;

import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;

public class LuaNFC implements LuaInterface
{
	LuaContext context;
	LuaTranslator ltTagRead;
	private final String HASH_TAG = "alng://";
	
	public void CreateMessage()
	{
		
	}
	
	public void CreateCommonRecord(int type, byte[] payload)
	{
		String val = HASH_TAG;
		
		val += type + "/";
		
		byte[] hash = val.getBytes(Charset.forName("UTF-16LE")); //UTF-16LE because windows phone sux
		byte[] combined = new byte[hash.length + payload.length];

		System.arraycopy(hash, 0, combined, 0, hash.length);
		System.arraycopy(payload, 0, combined, hash.length,payload.length);
	}
	
	@LuaFunction(manual = false, methodName = "IsAvailable")
	public boolean IsAvailable()
	{
		return false;
	}
	
	@Override
	public void RegisterEventFunction(String var, LuaTranslator lt)
	{
		if(var.compareTo("TagRead") == 0)
		{
			ltTagRead = lt;
		}
	}

	@Override
	public String GetId()
	{
		return "LuaNFC";
	}	
}
