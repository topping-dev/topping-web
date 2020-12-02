package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;
import com.dk.scriptingengine.osspecific.socketobjects.MethodModel;

@LuaClass(className = "LGEditText")
public class LGEditText extends LGTextView implements LuaInterface
{
	private String modelText;

	/**
	 * Creates LGEditText Object From Lua.
	 * @param lc
	 * @return LGEditText
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGEditText.class)
	public static LGEditText Create(LuaContext lc)
	{
		return new LGEditText(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGEditText(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGEditText(Context context, String luaId)
	{
		super(context, luaId);
	}

    /**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	super.Setup(context);
		if(luaId != null)
		{
			modelText = luaId + "Text";
			html("<v-text-field style=\"padding-top:0px !important;\" v-model=\"" + modelText + "\"></v-text-field>");
			jsModel.add(new JSModel(modelText, ""));
		}
		else
			html("<v-text-field></v-text-field>");
    }

	/**
	 * (Ignore)
	 */
	public void setHintTextColor(String color)
	{

	}
    
    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     * "TextChanged" happens when text is changed
     * "BeforeTextChanged" happens before text is changed
     * "AfterTextChanged" happens after text is changed
     * @param var Event
     * @param lt
     */
	@Override
	public void RegisterEventFunction(String var, LuaTranslator lt)
	{
		super.RegisterEventFunction(var, lt);
	}
}
