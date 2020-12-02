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
import com.dk.scriptingengine.osspecific.socketobjects.MethodModel;

import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.Text;

@LuaClass(className = "LGButton")
public class LGButton extends LGTextView implements LuaInterface
{
	private LuaTranslator ltBClick;
    protected String modelText;

    /**
	 * Creates LGButton Object From Lua.
	 * @param lc
	 * @return LGButton
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGButton.class)
	public static LGButton Create(LuaContext lc)
	{
		return new LGButton(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGButton(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGButton(Context context, String luaId)
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
            html("<v-btn @click.native=\"" + luaId + "Click\">{{ " + modelText + " }}</v-btn>");
            jsModel.add(new JSModel(modelText, ""));
            methodModel.add(new MethodModel(luaId, "Click"));
        }
        else
            html("<v-btn></v-btn>");
    }

	/**
	 * Sets the text
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetText", arguments = { String.class })
	public void SetText(String val)
	{
        this.text = val;
        if(!loaded)
        {
            if(luaId == null)
            {
                if (getRootNode().getChild(0) == null)
                {
                    Node n = new Text(getRootNode().getOwnerDocument(), val);
                    getRootNode().addChild(n);
                }
                else
                    getRootNode().getChild(0).setNodeValue(val);
            }
            else
            {
                setJsValue(modelText, text);
            }
        }
        else
        {
            if(luaId != null)
                LayoutServer.getInstance().notifyDataChanged(lc.GetContext().getClient(), modelText, LayoutServer.TYPE_STRING, text);
        }
	}

	/**
	 * Gets the text
	 * @return String
	 */
	@LuaFunction(manual = false, methodName = "GetText")
	public String GetText()
	{
		return text;
	}

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     * @param var Event
     * @param lt
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		if(var.compareTo("Click") == 0)
		{
			ltBClick = new LuaTranslator(lt.GetObject(), lt.GetFunction());

			if(luaId == null)
				return;

			LayoutServer.getInstance().sendPacket(lc.GetContext().getClient(), Utilities.MethodAction(luaId, "Click"));
		}
	}

	@Override
	public void CallEventValue(String id, String event, Object... vals)
	{
		if(event.equals("Click"))
		{
			if(ltBClick != null)
			{
				ltBClick.CallIn(lc);
			}
		}
	}
}
