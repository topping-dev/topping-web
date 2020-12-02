package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LGHorizontalScrollView")
public class LGHorizontalScrollView extends LGFrameLayout implements LuaInterface
{
	/**
	 * Creates LGHorizontalScrollView Object From Lua.
	 * @param lc
	 * @return LGHorizontalScrollView
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGHorizontalScrollView.class)
	public static LGHorizontalScrollView Create(LuaContext lc)
	{
		return new LGHorizontalScrollView(lc.GetContext());
	}
		
	/**
	 * (Ignore)
	 */
	public LGHorizontalScrollView(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGHorizontalScrollView(Context context, String luaId)
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
