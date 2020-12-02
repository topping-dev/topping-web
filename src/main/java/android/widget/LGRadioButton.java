package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.LayoutServer;
import com.dk.scriptingengine.osspecific.Utilities;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;

@LuaClass(className = "LGRadioButton")
public class LGRadioButton extends LGCompoundButton implements LuaInterface
{
    private String modelKey;
	private String modelValue;

	private String key = "";
    private String value = "";

    LuaTranslator ltCheckedChanged;

    /**
	 * Creates LGRadioButton Object From Lua.
	 * @param lc
	 * @return LGRadioButton
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGRadioButton.class)
	public static LGRadioButton Create(LuaContext lc)
	{
		return new LGRadioButton(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGRadioButton(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGRadioButton(Context context, String luaId)
	{
		super(context, luaId);
	}
    
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
		if(luaId != null)
		{
			modelText = luaId + "Text";
			modelKey = luaId + "Key";
			modelValue = luaId + "Value";

			key = luaId;
			value = luaId;

			html("<v-radio :label=\"" + modelText + "\" :key=\"" + modelKey + "\" :value=\"" + modelValue + "\" id=\"" + luaId + "\"></v-radio>");
            jsModel.add(new JSModel(modelText, text));
            jsModel.add(new JSModel(modelKey, key));
            jsModel.add(new JSModel(modelValue, value));
		}
		else
			html("<v-radio></v-radio>");
    }

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     * "CheckedChanged" happens when radio button is changed
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
		if(var.compareTo("CheckedChanged") == 0)
		{
			ltCheckedChanged = new LuaTranslator(lt.GetObject(), lt.GetFunction());

			if(luaId == null)
				return;

			LayoutServer.getInstance().sendPacket(lc.GetContext().getClient(), Utilities.MethodAction(luaId, "CheckedChanged"));
		}
	}

	@Override
	public void CallEventValue(String id, String event, Object... vals)
	{
		if(event.equals("CheckedChanged"))
		{
			if(ltCheckedChanged != null)
			{
				ltCheckedChanged.CallIn(lc);
			}
		}
	}
}
