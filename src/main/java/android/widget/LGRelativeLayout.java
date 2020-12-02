package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LGRelativeLayout")
public class LGRelativeLayout extends LGViewGroup implements LuaInterface
{
	/**
	 * Creates LGRelativeLayout Object From Lua.
	 * @param lc
	 * @return LGRelativeLayout
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGRelativeLayout.class)
	public static LGRelativeLayout Create(LuaContext lc)
	{
		return new LGRelativeLayout(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGRelativeLayout(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGRelativeLayout(Context context, String luaId)
	{
		super(context, luaId);
	}

    
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	//view = new RelativeLayout(context);
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
}
