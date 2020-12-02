package android.widget;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LGCompoundButton")
public class LGCompoundButton extends LGButton implements LuaInterface
{
	/**
	 * Creates LGCompoundButton Object From Lua.
	 * @param lc
	 * @return LGCompoundButton
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGCompoundButton.class)
	public static LGCompoundButton Create(LuaContext lc)
	{
		return new LGCompoundButton(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGCompoundButton(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGCompoundButton(Context context, String luaId)
	{
		super(context, luaId);
	}
    
    /**
	 * (Ignore)
	 */
    /*public void Setup(Context context)
    {
		lc = LuaContext.CreateLuaContext(context);
		if (!loaded)
        {
            LuaEngine.getInstance().OnGuiEvent(this, LuaEngine.GuiEvents.GUI_EVENT_CREATE, lc);
            loaded = true;
        }
    }*/
    
    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     * @param var Event
     * @param lt
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
	}
}
