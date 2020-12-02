package android.widget;

import com.dk.scriptingengine.LuaMapMarker;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;

import java.util.ArrayList;

@LuaClass(className = "LGAutoCompleteTextView")
public class LGAutoCompleteTextView extends LGEditText implements LuaInterface
{

	private String modelText;
	private String modelItems;

	private ArrayList<String> items = new ArrayList<>();

	/**
	 * Creates LGAutoCompleteTextView Object From Lua.
	 * @param lc
	 * @return LGAutoCompleteTextView
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGAutoCompleteTextView.class)
	public static LGAutoCompleteTextView Create(LuaContext lc)
	{
		return new LGAutoCompleteTextView(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGAutoCompleteTextView(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGAutoCompleteTextView(Context context, String luaId)
	{
		super(context, luaId);
	}

    /**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	//super.Setup(context);
		if(luaId != null)
		{
			modelText = luaId + "Text";
			modelItems = luaId + "Items";
			html("<v-autocomplete style=\"padding-top:0px !important;\" v-model=\"" + modelText + "\"></v-autocomplete>");
			jsModel.add(new JSModel(modelText, ""));
			jsModel.add(new JSModel(modelItems, items));
		}
		else
			html("<v-autocomplete></v-autocomplete>");
    }
    
    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     */
	@Override
	public void RegisterEventFunction(String var, LuaTranslator lt)
	{
		super.RegisterEventFunction(var, lt);
	}
}
