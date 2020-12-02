package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

import java.util.Map;

@LuaClass(className = "LGLinearLayout")
public class LGLinearLayout extends LGViewGroup implements LuaInterface
{
	private int orientation = 1;

	/**
	 * Creates LGLinearLayout Object From Lua.
	 * @param lc
	 * @return LGLinearLayout
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGLinearLayout.class)
	public static LGLinearLayout Create(LuaContext lc)
	{
		return new LGLinearLayout(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGLinearLayout(Context context)
	{
		super(context);	
	}
	
	/**
	 * (Ignore)
	 */
	public LGLinearLayout(Context context, String luaId)
	{
		super(context, luaId);	
	}
	
	/**
	 * (Ignore)
	 */
	public void Setup(Context context)
    {
		html("<v-layout>¨¨~~</v-layout>");
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

	public void setOrientation(int orientation)
	{
		this.orientation = orientation;
    	if(orientation == 0)
            getRootNode().setAttribute("row");
    	else
    		getRootNode().setAttribute("column");
	}

	public int getOrientation()
	{
		return orientation;
	}
}
