package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaGlobalInt;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.DynamicByteBuf;
import com.dk.scriptingengine.osspecific.LayoutServer;

/**
 * Create toast messages with this class
 */
@LuaClass(className = "LuaToast")
@LuaGlobalInt(
		keys = { "TOAST_SHORT", "TOAST_LONG" },
		vals = { 1000, 5000 }
		)
public class LuaToast implements LuaInterface
{
	/**
	 * Show the toast
	 * @param context
	 * @param text text to show
	 * @param duration duration as milliseconds or TOAST_SHORT or TOAST_LONG
	 */
	@LuaFunction(manual = false, methodName = "Show", arguments = { LuaContext.class, String.class, Integer.class }, self = LuaToast.class)
	public static void Show(LuaContext context, String text, Integer duration)
	{
        DynamicByteBuf buf = DynamicByteBuf.create();
        buf.writeInt(LayoutServer.SMSG_TOAST);
        buf.writeInt(text.length());
        buf.writeString(text);
        buf.writeInt(duration);

        LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), buf);
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
		return "LuaToast";
	}
}
