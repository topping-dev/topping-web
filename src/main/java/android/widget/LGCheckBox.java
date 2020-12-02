package android.widget;

import com.dk.helpers.ComboData;
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

@LuaClass(className = "LGCheckBox")
public class LGCheckBox extends LGCompoundButton implements LuaInterface
{
    private String label;
    private boolean checked;
    private LuaTranslator ltCBChecked;
    private String modelChecked;
    private String modelLabel;

    /**
	 * Creates LGCheckbox Object From Lua.
	 * @param lc
	 * @return LGCheckBox
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGCheckBox.class)
	public static LGCheckBox Create(LuaContext lc)
	{
		return new LGCheckBox(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGCheckBox(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGCheckBox(Context context, String luaId)
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
            modelLabel = luaId + "Label";

            html("<v-checkbox v-model=\"" + modelChecked + "\" id=\"" + luaId + "\" :label=\"" + modelLabel + "\"></v-checkbox>");
            jsModel.add(new JSModel(modelChecked, checked));
            jsModel.add(new JSModel(modelLabel, ""));
            watchModel.add(new WatchModel(luaId, "Checked"));
        }
        else
        {
            html("<v-checkbox></v-checkbox>");
        }
    }

	/**
	 * Sets the text
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetText", arguments = { String.class })
	public void SetText(String val)
	{
	    this.label = val;
	    if(!loaded)
        {
            if(luaId == null)
            {
                if (this.label.equals(""))
                    getRootNode().removeAttribute("label");
                else
                    getRootNode().setAttribute("label", val);
            }
            else
            {
                setJsValue(modelLabel, label);
            }
        }
        else
        {
            LayoutServer.getInstance().notifyDataChanged(lc.GetContext().getClient(), luaId, LayoutServer.TYPE_STRING, label);
        }
	}

	/**
	 * Gets the text
	 * @return String
	 */
	@LuaFunction(manual = false, methodName = "GetText")
	public String GetText()
	{
		return label;
	}

    /**
     * Sets the text
     * @param val
     */
    @LuaFunction(manual = false, methodName = "SetChecked", arguments = { Boolean.class })
    public void SetChecked(boolean val)
    {
        this.checked = val;
        if(!loaded)
        {
            if(luaId != null)
            {
                setJsValue(modelChecked, checked);
            }
        }
        else
        {
            LayoutServer.getInstance().notifyDataChanged(lc.GetContext().getClient(), luaId, LayoutServer.TYPE_BOOLEAN, checked);
        }
    }

    /**
     * Gets the text
     * @return String
     */
    @LuaFunction(manual = false, methodName = "GetChecked")
    public Boolean GetChecked()
    {
        return checked;
    }

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     * "CheckedChanged" happens when user checks checkbox
     * @param var Event
     * @param lt
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
		if(var.compareTo("CheckedChanged") == 0)
		{
            ltCBChecked = new LuaTranslator(lt.GetObject(), lt.GetFunction());

            if(luaId == null)
                return;

            String id = modelChecked;

            LayoutServer.getInstance().sendPacket(lc.GetContext().getClient(), Utilities.WatchAction(id, luaId, "CheckedChanged"));
		}
	}

    @Override
    public void CallEventValue(String id, String event, Object... vals)
    {
        if(event.equals(SET_VALUE))
        {
            if(vals == null)
                return;
            checked = (boolean)vals[0];
        }
        else if(event.equals("CheckedChanged"))
        {
            checked = (boolean)vals[0];

            if(ltCBChecked != null)
            {
                ltCBChecked.CallIn(lc, checked);
            }
        }
    }
}
