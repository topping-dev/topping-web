package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LGScrollView")
public class LGScrollView extends LGFrameLayout implements LuaInterface
{
	/**
	 * Creates LGScrollView Object From Lua.
	 * @param lc
	 * @return LGScrollView
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGScrollView.class)
	public static LGScrollView Create(LuaContext lc)
	{
		return new LGScrollView(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */	
	public LGScrollView(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGScrollView(Context context, String luaId)
	{
		super(context, luaId);
	}
	
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	html("<div>¨¨~~</div>");
    }

	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
	}
}
