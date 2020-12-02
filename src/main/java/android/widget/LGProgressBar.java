package android.widget;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;
import com.dk.scriptingengine.osspecific.socketobjects.WatchModel;

@LuaClass(className = "LGProgressBar")
public class LGProgressBar extends LGView implements LuaInterface
{
    private boolean indeterminate;
    private int max;
    private Style style;
    private int progress;
    private String modelProgress;

    public enum Style
    {
        Horizontal,
        Small,
        Large,
        Inverse,
        SmallInverse,
        LargeInverse
    }

    /**
	 * Creates LGProgressBar Object From Lua.
	 * @param lc
	 * @return LGProgressBar
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGProgressBar.class)
	public static LGProgressBar Create(LuaContext lc)
	{
		return new LGProgressBar(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGProgressBar(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGProgressBar(Context context, String luaId)
	{
		super(context, luaId);
	}
    
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	html("<v-progress-circular " + (luaId == null ? "" : "v-model=\"" + luaId + "Progress\" id=\"" + luaId + "\"") + "></v-progress-circular>");
    	if(luaId != null)
        {
            modelProgress = luaId + "Progress";
            jsModel.add(new JSModel(modelProgress, 0));
            watchModel.add(new WatchModel(luaId, "Progress"));
        }
    }

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
	}

    public void setIndeterminate(boolean indeterminate)
    {
        this.indeterminate = indeterminate;
        if(indeterminate)
            getRootNode().setAttribute("indeterminate");
        else
            getRootNode().removeAttribute("indeterminate");
    }

    public boolean getIndeterminate()
    {
        return indeterminate;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public int getMax()
    {
        return max;
    }

    public void setStyle(Style style)
    {
        this.style = style;
        if(style == Style.Horizontal)
        {
            if(getRootNode().getNodeName().contains("v-progress-circular"))
            {
                String html = html();
                html = html.replace("v-progress-circular", "v-progress-linear");
                html(html);
            }
        }
        else
        {
            if(getRootNode().getNodeName().contains("v-progress-linear"))
            {
                String html = html();
                html = html.replace("v-progress-linear", "v-progress-circular");
                html(html);
            }

            if(style == Style.Large || style == Style.LargeInverse)
            {
                getRootNode().removeAttribute(":size");
                getRootNode().setAttribute(":size", "50");
            }
        }
    }

    public Style getStyle()
    {
        return style;
    }
}
