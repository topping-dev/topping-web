package android.widget;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;

@LuaClass(className = "LGViewGroup")
public class LGViewGroup extends LGView implements LuaInterface
{
	/**
	 * Creates LGViewGroup Object From Lua.
	 * Do not create this class directly.
	 * @param lc
	 * @return LGViewGroup
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGViewGroup.class)
	public static LGViewGroup Create(LuaContext lc)
	{
		return new LGViewGroup(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGViewGroup(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGViewGroup(Context context, String luaId)
	{
		super(context, luaId);
	}
	
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	lc = LuaContext.CreateLuaContext(context);
    }

	@Override
	public void AfterSetup(Context context)
	{
		for(LGView v : subviews)
			v.AfterSetup(context);

		super.AfterSetup(context);
	}

	/**
	 * (Ignore)
	 */
	@Override
	public void onLayoutCreated()
	{
		float weightSum = 0;
		for(LGView v : subviews)
			weightSum += v.getLayoutParams().weight;

		for(LGView v : subviews)
		{
			if(v.getLayoutParams().w == 0)
			{
				int widthPercent = (int) ((v.getLayoutParams().weight * 100) / weightSum);
				v.addStyle("width:" + widthPercent + "% !important;");
			}

			if(v.getLayoutParams().h == 0)
			{
				int heightPercent = (int) ((v.getLayoutParams().weight * 100) / weightSum);
				v.addStyle("height:" + heightPercent + "% !important;");
			}
		}

		super.onLayoutCreated();
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

	public void addView(LGView v)
	{
		v.parent = this;
		this.subviews.add(v);
	}
}
