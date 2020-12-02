package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LGTableRow")
public class LGTableRow extends LGLinearLayout implements LuaInterface
{
	/**
	 * Creates LGTableRow Object From Lua.
	 * @param lc
	 * @return LGTableRow
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGTableRow.class)
	public static LGTableRow Create(LuaContext lc)
	{
		return new LGTableRow(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGTableRow(Context context)
	{
		super(context);		
	}
	
	/**
	 * (Ignore)
	 */
	public LGTableRow(Context context, String luaId)
	{
		super(context, luaId);		
	}
	
	/**
	 * (Ignore)
	 */
	public void Setup(Context context)
    {
		html("<tr></tr>");
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
