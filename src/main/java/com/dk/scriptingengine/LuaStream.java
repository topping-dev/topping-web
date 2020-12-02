package com.dk.scriptingengine;

import java.io.InputStream;
import java.io.OutputStream;

import com.dk.scriptingengine.osspecific.Log;
import com.luajava.Tools;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;

/**
 * Lua stream interface.
 * This class is used to manupulate streams.
 */
@LuaClass(className = "LuaStream")
public class LuaStream implements LuaInterface
{
	private final static int NOSTREAM = -1;
	private final static int INPUTSTREAM = 0;
	private final static int OUTPUTSTREAM = 1;
	
	private int type = NOSTREAM;
	private InputStream is = null;
	private OutputStream os = null;
	
	/*@LuaFunction(manual = false, methodName = "Create", self = LuaStream.class, arguments = { Integer.class })
    public static Object Create(Integer type)
    {
		LuaStream ls = new LuaStream();
		if(type == LuaStream.INPUTSTREAM)
			OutputStream os = new FileOutputStream()
        LuaDatabase db = new LuaDatabase();
        db.db = new DatabaseHelper(context.GetContext());
        return db;
    }*/

	public boolean HasStream() { return type != NOSTREAM; }

	/**
	 * Get stream.
	 * @return LuaObjectStore InputStream or OutputStream value. 
	 */
	@LuaFunction(manual = false, methodName = "GetStream")
	public LuaObjectStore GetStream()
	{
		if(type == -1)
		{
			Log.e("LuaStream.java", "Stream not set");
			return null;
		}
		
		LuaObjectStore los = new LuaObjectStore();
		if(type == INPUTSTREAM)
			los.obj = is;
		else
			los.obj = os;
		
		return los;
	}
	
	/**
	 * (Ignore)
	 */
	public Object GetStreamInternal()
	{
		if(type == -1)
		{
			Log.e("LuaStream.java", "Stream not set");
			return null;
		}
		
		if(type == INPUTSTREAM)
			return is;
		else
			return os;
	}
	
	/**
	 * Set stream.
	 * @param LuaObjectStore InputStream or OutputStream value.
	 */
	@LuaFunction(manual = false, methodName = "SetStream", arguments = { LuaObjectStore.class })
	public void SetStream(LuaObjectStore stream)
	{
		Object nativeStream = stream.obj;
		if(nativeStream instanceof InputStream)
		{
			is = (InputStream)nativeStream;
			type = INPUTSTREAM;
		}
		else
		{
			os = (OutputStream)nativeStream;
			type = OUTPUTSTREAM;
		}
	}
	
	/**
	 * (Ignore)
	 */
	public void SetStream(Object stream)
	{
	    if (stream == null)
	        return;
		if(stream instanceof InputStream)
		{
			is = (InputStream)stream;
			type = INPUTSTREAM;
		}
		else
		{
			os = (OutputStream)stream;
			type = OUTPUTSTREAM;
		}
	}
	
	/**
	 * Reads a single byte from this stream and returns it as an integer in the range from 0 to 255. Returns -1 if the end of the stream has been reached. Blocks until one byte has been read, the end of the source stream is detected or an exception is thrown.
	 * @return int value of byte.
	 */
	@LuaFunction(manual = false, methodName = "ReadOne")
	public Integer ReadOne()
	{
		if(type == OUTPUTSTREAM)
		{
			Log.e("LuaStream.java", "Tried to read output stream.");
			return -1;
		}
		
		try
		{
			return is.read();
		}
		catch (Exception e) 
		{
			Tools.LogException("LuaStream.java", e);
			return -1;
		}
	}
	
	/**
	 * Reads at most length bytes from this stream and stores them in the byte array b starting at offset.
	 * @param bufferO buffer object.
	 * @param offset offset to start.
	 * @param length length to read.
	 */
	@LuaFunction(manual = false, methodName = "Read", arguments = { Object.class, Integer.class, Integer.class })
	public void Read(LuaBuffer bufferO, Integer offset, Integer length)
	{
		if(type == OUTPUTSTREAM)
		{
			Log.e("LuaStream.java", "Tried to read output stream.");
			return;
		}
		
		byte[] buffer = bufferO.GetBuffer();
		
		try
		{
			is.read(buffer, offset, length);
		}
		catch (Exception e) 
		{
			Tools.LogException("LuaStream.java", e);
		}
	}
	
	/**
	 * Writes a single byte to this stream. Only the least significant byte of the integer oneByte is written to the stream.
	 * @param oneByte byte value.
	 */
	@LuaFunction(manual = false, methodName = "WriteOne", arguments = { Integer.class })
	public void WriteOne(Integer oneByte)
	{
		if(type == INPUTSTREAM)
		{
			Log.e("LuaStream.java", "Tried to write input stream.");
			return;
		}
		
		try
		{
			os.write(oneByte);
		}
		catch (Exception e) 
		{
			Tools.LogException("LuaStream.java", e);
		}
	}
	
	/**
	 * Writes count bytes from the byte array buffer starting at position offset to this stream.
	 * @param bufferO buffer object.
	 * @param offset offset to start.
	 * @param length length to write.
	 */
	@LuaFunction(manual = false, methodName = "Write", arguments = { Object.class, Integer.class, Integer.class })
	public void Write(LuaBuffer bufferO, Integer offset, Integer length)
	{
		if(type == INPUTSTREAM)
		{
			Log.e("LuaStream.java", "Tried to write input stream.");
			return;
		}
		
		byte[] buffer = bufferO.GetBuffer();
		
		try
		{
			os.write(buffer, offset, length);
		}
		catch (Exception e) 
		{
			Tools.LogException("LuaStream.java", e);
		}
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
		return "LuaStream";
	}
}
