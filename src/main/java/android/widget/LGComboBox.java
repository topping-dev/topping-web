package android.widget;

import com.dk.helpers.ComboData;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.ArrayAdapter;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.DynamicByteBuf;
import com.dk.scriptingengine.osspecific.LayoutServer;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;
import com.dk.scriptingengine.osspecific.socketobjects.WatchModel;

@LuaClass(className = "LGComboBox")
public class LGComboBox extends LGView implements LuaInterface
{
	LuaTranslator ltCBChanged;
	ArrayAdapter<ComboData> mAdapter;
	private String mCustom = "";
	private String mDelete = "";
    ComboData selectedData = null;
    private String modelSeleted;
    private String modelItems;

    /**
	 * Creates LGComboBox Object From Lua.
	 * @param lc
	 * @return LGComboBox
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGComboBox.class)
	public static LGComboBox Create(LuaContext lc)
	{
		return new LGComboBox(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGComboBox(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGComboBox(Context context, String luaId)
	{
		super(context, luaId);
	}
    
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	html("<v-select item-text=\"name\" item-value=\"tag\" " + (luaId == null ? "" : "v-model=\"" + luaId + "Selected\" id=\"" + luaId + "\" :items=\"" + luaId + "Items\" ") + " single-line></v-select>");
    	if(luaId != null)
        {
            modelSeleted = luaId + "Selected";
            modelItems = luaId + "Items";
            jsModel.add(new JSModel(modelSeleted, null));
            jsModel.add(new JSModel("\"" + luaId + "Items\" : []"));
            watchModel.add(new WatchModel(luaId, "Selected"));
        }
    }
    
    /**
	 * (Ignore)
	 */
    @Override
    public void AfterSetup(Context context)
    {
    	super.AfterSetup(context);
		mAdapter = new ArrayAdapter<>(lc.GetContext(), luaId + "Items");
    }

	/**
	 * Sets the text
	 * @param val
	 */
	@LuaFunction(manual = false, methodName = "SetText", arguments = { String.class })
	public void SetText(String val)
	{
		get(0).setAttribute("label", "val");
	}

	/**
	 * Gets the text
	 * @return String
	 */
	@LuaFunction(manual = false, methodName = "GetText")
	public String GetText()
	{
		return get(0).getAttribute("label");
	}
    
    /**
     * Add combo item to combobox
     * @param id of combobox
     * @param tag
     */
    @LuaFunction(manual = false, methodName = "AddComboItem", arguments = { String.class, Object.class })
    public void AddComboItem(String id, Object tag)
    {
    	ComboData cd = new ComboData();
    	cd.name = id;
    	cd.tag = tag;
		mAdapter.add(cd);
		mAdapter.notifyDataSetChanged();
    }
    
    /**
     * Show custom button
     * @param value
     */
    @LuaFunction(manual = false, methodName = "ShowCustom", arguments = { Integer.class })
    public void ShowCustom(Integer value)
    {
		/*if(value.intValue() == 1)
		{
			ComboData cd = new ComboData();
			cd.name = mCustom;
			cd.tag = null;
			cd.type = -1;
			mAdapter.insert(cd, 0);
            Defaults.RunOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mAdapter.notifyDataSetChanged();
                }
            });
		}
		else
		{
			ComboData cdToRemove = null;
			for(int i = 0; i < mAdapter.getCount(); i++)
			{
				ComboData cd = mAdapter.getItem(i);
				if(cd.type == -1)
				{
					cdToRemove = cd;
					break;
				}
			}

			if(cdToRemove != null)
            {
                mAdapter.remove(cdToRemove);
                Defaults.RunOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
		}*/
    }
    
    /**
     * Show delete button
     * @param value
     */
    @LuaFunction(manual = false, methodName = "ShowDelete", arguments = { Integer.class })
    public void ShowDelete(Integer value)
    {
		/*if(value.intValue() == 1)
		{
			ComboData cd = new ComboData();
			cd.name = mDelete;
			cd.tag = null;
			cd.type = -2;
			mAdapter.insert(cd, 0);
            Defaults.RunOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mAdapter.notifyDataSetChanged();
                }
            });
		}
		else
		{
			ComboData cdToRemove = null;
			for(int i = 0; i < mAdapter.getCount(); i++)
			{
				ComboData cd = mAdapter.getItem(i);
				if(cd.type == -2)
				{
					cdToRemove = cd;
					break;
				}
			}

			if(cdToRemove != null)
            {
                mAdapter.remove(cdToRemove);
                Defaults.RunOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
		}*/
    }
    
    /**
     * Set combobox editable
     * @param value (1 or 0)
     */
    /*@LuaFunction(manual = false, methodName = "SetEditable", arguments = { Integer.class })
    public void SetEditable(Integer value)
    {
    	((ComboBox)view).editable = value.intValue() == 1 ? true : false;
    	if(value == 0)
    		((ComboBox)view).clearFocus();
    }*/
    
    /**
     * Sets the selected value
     * @return name value
     */
    @LuaFunction(manual = false, methodName = "SetSelected", arguments = { Integer.class })
    public void SetSelected(int index)
    {
		//((Spinner)view).setSelection(index);
    }
    
    /**
     * Gets the selected name
     * @return name value
     */
    @LuaFunction(manual = false, methodName = "GetSelectedName")
    public String GetSelectedName()
    {
    	if(selectedData != null)
    		return selectedData.name;
    	return null;
    }
    
    /**
     * Gets the selected tag
     * @return tag value
     */
    @LuaFunction(manual = false, methodName = "GetSelectedTag")
    public Object GetSelectedTag()
    {
        if(selectedData != null)
    	    return selectedData.tag;

        return null;
    }

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     * "Changed" happens when user changes the selection
     * @param var Event
     * @param lt
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
		if(var.compareTo("Changed") == 0)
		{
			ltCBChanged = new LuaTranslator(lt.GetObject(), lt.GetFunction());

			if(luaId == null)
			    return;

			String id = luaId + "Selected";
            DynamicByteBuf buf = DynamicByteBuf.create();
            buf.writeInt(LayoutServer.SMSG_REGISTER_EVENT);
            buf.writeInt(LayoutServer.EVENT_TYPE_WATCH);
            buf.writeInt(luaId.length());
            buf.writeString(luaId);
            buf.writeInt(id.length());
            buf.writeString(id);
            buf.writeInt("Changed".length());
            buf.writeString("Changed");

            LayoutServer.getInstance().sendPacket(lc.GetContext().getClient(), buf);
		}
	}

	@Override
	public void CallEventValue(String id, String event, Object... vals)
	{
	    if(event.equals(SET_VALUE))
        {
            if(vals == null)
                return;
            for(ComboData cd : mAdapter.getAll())
            {
                if(cd.tag instanceof String)
                {
                    if(cd.tag.equals(vals[0]))
                    {
                        selectedData = cd;
                        break;
                    }
                }
                else
                {
                    if(Compare((Number)cd.tag, (Number)vals[0]) == 0)
                    {
                        selectedData = cd;
                        break;
                    }
                }
            }
        }
		else if(event.equals("Changed"))
		{
            for(ComboData cd : mAdapter.getAll())
            {
                if(cd.tag instanceof String)
                {
                    if(cd.tag.equals(vals[0]))
                    {
                        selectedData = cd;
                        break;
                    }
                }
                else
                {
                    if(Compare((Number)cd.tag, (Number)vals[0]) == 0)
                    {
                        selectedData = cd;
                        break;
                    }
                }
            }

			if(ltCBChanged != null)
            {
                ltCBChanged.CallIn(lc, selectedData.name, selectedData.tag);
            }
		}
	}

    public static int Compare(Number n1, Number n2)
    {
        long l1 = n1.longValue();
        long l2 = n2.longValue();
        if (l1 != l2)
            return (l1 < l2 ? -1 : 1);
        return Double.compare(n1.doubleValue(), n2.doubleValue());
    }
}
