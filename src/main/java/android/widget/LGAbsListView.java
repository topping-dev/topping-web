package android.widget;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

//Arada adapterview var
@LuaClass(className = "LGAbsListView")
public abstract class LGAbsListView extends LGView implements LuaInterface
{
	private boolean loaded = false;
	
	/**
	 * Creates LGAbsListView Object From Lua.
	 * Do not use this class directly
	 * @param lc
	 * @return LGAbsListView
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGAbsListView.class)
	public static LGAbsListView Create(LuaContext lc)
	{
		/*return new LGAbsListView(lc.GetContext());*/
		return null;
	}
	
	/**
	 * (Ignore)
	 */
	public LGAbsListView(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGAbsListView(Context context, String luaId)
	{
		super(context, luaId);
	}

    /**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	lc = LuaContext.CreateLuaContext(context);
		if (!loaded)
        {
            LuaEngine.getInstance().OnGuiEvent(this, LuaEngine.GuiEvents.GUI_EVENT_CREATE, lc);
            loaded = true;
        }	
    }

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
	}

	public abstract LGView viewForIndex(int index, String itemName);
}
