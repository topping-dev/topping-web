package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Class that is used in buffer operations
 */
@LuaClass(className = "LuaBuffer")
public class LuaBuffer implements LuaInterface
{
	public byte[] buffer;
	
	/**
	 * Creates a buffer
	 * @param capacity
	 * @return LuaBuffer
	 */
	public static LuaBuffer Create(int capacity)
	{
		LuaBuffer lb = new LuaBuffer();
		lb.buffer = new byte[capacity];
		return lb;
	}
	
	/**
	 * Gets byte from index
	 * @param index
	 * @return int
	 */
	@LuaFunction(manual = false, methodName = "GetByte", arguments = { Integer.class })
	public Integer GetByte(Integer index)
	{
		return (int) buffer[index];
	}
	
	/**
	 * Set Byte at index
	 * @param index
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "SetByte", arguments = { Integer.class, Integer.class })
	public void SetByte(Integer index, Integer value)
	{
		buffer[index] = (byte)value.intValue();
	}
	
	/**
	 * Frees LuaBuffer.
	 */
	@LuaFunction(manual = false, methodName = "Free", arguments = {  })
	public void Free()
	{
		
	}
	
	/**
	 * (Ignore)
	 */
	public byte[] GetBuffer() { return buffer; }
	
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
		return "LuaBuffer";
	}
}
