package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.luagui.LuaViewInflator;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.LayoutServer;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;

import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.Text;

@LuaClass(className = "LGTextView")
public class LGTextView extends LGView implements LuaInterface
{
	protected String text;
    private int textSize;
    private boolean singleLine;
    private String inputType;

    private String modelText;
    private String modelTextColor;
    private String modelFontSize;

    /**
	 * Creates LGTextView Object From Lua.
	 * @param lc
	 * @return LGTextView
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class, String.class }, self = LGTextView.class)
	public static LGTextView Create(LuaContext lc, String tag)
	{
		LGTextView tv = new LGTextView(lc.GetContext());
		tv.setTag(tag);
		return tv;		
	}
	
	/**
	 * (Ignore)
	 */
	public LGTextView(Context context)
	{
		super(context);		
	}
	
	/**
	 * (Ignore)
	 */
	public LGTextView(Context context, String luaId)
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
            modelTextColor = luaId + "TextColor";
            modelFontSize = luaId + "FontSize";
            html("<p v-bind:style=\"{ color: " + modelTextColor + ", fontSize: " + modelFontSize + " + 'px' }\">{{ " + modelText + " }}</p>");
            jsModel.add(new JSModel(modelText, ""));
            jsModel.add(new JSModel(modelTextColor, ""));
            jsModel.add(new JSModel(modelFontSize, ""));
        }
        else
            html("<p></p>");
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
     * Sets the text color
     * @param color
     */
    @LuaFunction(manual = false, methodName = "SetTextColor", arguments = { String.class })
    public void SetTextColor(String color)
	{
		//find a way to set text color
        if(!loaded)
        {
            if(luaId == null)
            {
                String s = "";
                if(getRootNode().getAttribute("style") != null)
                    s = getRootNode().getAttribute("style");
                s += "color:" + color + ";";
                getRootNode().setAttribute("style", s);
            }
            else
            {
                setJsValue(modelTextColor, color);
            }
        }
        else
        {
            if(luaId != null)
                LayoutServer.getInstance().notifyDataChanged(lc.GetContext().getClient(), modelTextColor, LayoutServer.TYPE_STRING, color);
        }

	}

    public void setTextSize(int textSize)
    {
        this.textSize = textSize;
        if(!loaded)
        {
            if(luaId == null)
            {
                String s = "";
                if(getRootNode().getAttribute("style") != null)
                    s = getRootNode().getAttribute("style");
                s += "fontSize:" + textSize + "px;";
                getRootNode().setAttribute("style", s);
            }
            else
            {
                setJsValue(modelFontSize, textSize);
            }
        }
        else
        {
            if(luaId != null)
                LayoutServer.getInstance().notifyDataChanged(lc.GetContext().getClient(), modelFontSize, LayoutServer.TYPE_STRING, textSize);
        }

    }

    public int getTextSize()
    {
        return textSize;
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
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
    	final Object self = this;
		super.RegisterEventFunction(var, lt);
		if(var.compareTo("TextChanged") == 0)
		{
			attr("@change", var);
		}
		else if(var.compareTo("BeforeTextChanged") == 0)
		{

		}
		else if(var.compareTo("AfterTextChanged") == 0)
		{

		}
	}

    public void setSingleLine(boolean singleLine)
    {
        this.singleLine = singleLine;
        if(!singleLine)
            getRootNode().setAttribute("multi-line");
    }

    public boolean getSingleLine()
    {
        return singleLine;
    }

    public void setInputType(String inputType)
    {
        this.inputType = inputType;
        getRootNode().setAttribute("type", inputType);
    }

    public String getInputType()
    {
        return inputType;
    }
}
