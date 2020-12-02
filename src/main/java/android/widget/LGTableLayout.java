package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LGTableLayout")
public class LGTableLayout extends LGLinearLayout implements LuaInterface
{
	/**
	 * Creates LGTableLayout Object From Lua.
	 * @param lc
	 * @return LGTableLayout
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGTableLayout.class)
	public static LGTableLayout Create(LuaContext lc)
	{
		return new LGTableLayout(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */	
	public LGTableLayout(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGTableLayout(Context context, String luaId)
	{
		super(context, luaId);
	}

	/**
	 * (Ignore)
	 */
	public void Setup(Context context)
    {
		html("<table></table>");
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
