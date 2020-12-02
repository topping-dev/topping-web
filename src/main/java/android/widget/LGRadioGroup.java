package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.DynamicByteBuf;
import com.dk.scriptingengine.osspecific.LayoutServer;
import com.dk.scriptingengine.osspecific.Utilities;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;
import com.dk.scriptingengine.osspecific.socketobjects.WatchModel;

@LuaClass(className = "LGRadioGroup")
public class LGRadioGroup extends LGLinearLayout implements LuaInterface
{
    private String checked;
    private LuaTranslator ltRBChecked;

	private String modelChecked;

	/**
	 * Creates LGRadioGroup Object From Lua.
	 * @param lc
	 * @return LGRadioGroup
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGRadioGroup.class)
	public static LGRadioGroup Create(LuaContext lc)
	{
		return new LGRadioGroup(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGRadioGroup(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGRadioGroup(Context context, String luaId)
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
            modelChecked = luaId + "Checked";
            html("<v-radio-group v-model=\"" + modelChecked + "\" id=\"" + luaId + "\">¨¨~~</v-radio-group>");
            jsModel.add(new JSModel(modelChecked, checked));
            watchModel.add(new WatchModel(luaId, "Checked"));
        }
        else
    	    html("<v-radio-group></v-radio-group>");
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
            ltRBChecked = new LuaTranslator(lt.GetObject(), lt.GetFunction());
		}
	}

    @Override
    public void CallEventValue(String id, String event, Object... vals)
    {
        if(event.equals(SET_VALUE))
        {
            if(vals == null)
                return;
            checked = (String) vals[0];

            if(ltRBChecked != null)
            {
                ltRBChecked.CallIn(lc, checked);
            }
        }
    }
}
