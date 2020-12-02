package android.widget;

import java.io.InputStream;

import com.dk.scriptingengine.LuaStream;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.LayoutServer;

import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.Text;

public class LGImageView extends LGView implements LuaInterface
{
	private String imagePath;

	/**
	 * Creates LGImageView Object From Lua.
	 * @param lc
	 * @param tag String tag
	 * @return LGImageView
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class, String.class }, self = LGImageView.class)
	public static LGImageView Create(LuaContext lc, String tag)
	{
		LGImageView iv = new LGImageView(lc.GetContext());
		iv.setTag(tag);
		return iv;		
	}
	
	/**
	 * (Ignore)
	 */
	public LGImageView(Context context)
	{
		super(context);		
	}
	
	/**
	 * (Ignore)
	 */
	public LGImageView(Context context, String luaId)
	{
		super(context, luaId);		
	}
    
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	html("<img style=\"object-fit:contain;\"></img>");
    }
    
    /**
     * Sets the image view from LuaStream stream
     * @param stream
     */
    @LuaFunction(manual = false, methodName = "SetImage", arguments = { LuaStream.class })
    void SetImage(LuaStream stream)
    {
    	InputStream is = (InputStream)stream.GetStreamInternal();

    	//TODO:
		/*
    	BitmapDrawable bd = new BitmapDrawable(is);
    	((ImageView)view).setImageDrawable(bd);*/
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

	public void setImagePath(String imagePath)
	{
		this.imagePath = imagePath;
        /*if(!loaded)
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
                jsModel = "\"" + luaId + "Text\" : \"" + this.text + "\"";
            }
        }
        else
        {
            if(luaId != null)
                LayoutServer.getInstance().notifyDataChanged(lc.GetContext().getClient(), luaId + "ImageSource", LayoutServer.TYPE_STRING, text);
        }*/
	}

	public String getImagePath()
	{
		return imagePath;
	}
}
