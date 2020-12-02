package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LGFrameLayout")
public class LGFrameLayout extends LGViewGroup implements LuaInterface
{	
	/**
	 * Creates LGFrameLayout Object From Lua.
	 * @param lc
	 * @return LGFrameLayout
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGFrameLayout.class)
	public static LGFrameLayout Create(LuaContext lc)
	{
		return new LGFrameLayout(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGFrameLayout(Context context)
	{
		super(context);		
	}
	
	/**
	 * (Ignore)
	 */
	public LGFrameLayout(Context context, String luaId)
	{
		super(context, luaId);		
	}
    
    /**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
		html("<div style=\"display:block;position:relative;\">¨¨~~</div>");
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
