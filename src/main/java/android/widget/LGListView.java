package android.widget;

import com.dk.scriptingengine.LuaColor;
import com.dk.scriptingengine.LuaFragment;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.LayoutServer;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;
import com.dk.scriptingengine.osspecific.socketobjects.MethodModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@LuaClass(className = "LGListView")
public class LGListView extends LGAbsListView implements LuaInterface
{
	private boolean useTabletModeIfNecessary = true;
	private int selectionColor;
	private LGListViewFragment llvf;
	public LuaFragment lf;
	private LGAdapterView adapter;
	private HashMap<Integer, LGView> layoutMap = new HashMap<>();
    private String modelRef;
    private String modelList;
    private String modelTombstone;
    private String modelSize;
    private String modelOffset;
    private String modelLoadMore;
    private String modelSpinner;
    private String modelNoMore;
    private String modelILoaded;
    private String modelAdapter;

    @LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGListView.class)
	public static LGListView Create(LuaContext lc)
	{
		return new LGListView(lc.GetContext());
	}
	
	/**
	 * (Ignore)
	 */
	public LGListView(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGListView(Context context, String luaId)
	{
		super(context, luaId);
	}
    
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	selectionColor = LuaColor.HexToColor(LuaColor.TRANSPARENT);
    	/*FragmentTracker ft = FragmentTracker.GetInstance(LuaForm.GetActiveForm().getSupportFragmentManager());
		int rotation = DisplayMetrics.GetRotation(getContext());
		int id = Defines.generateViewId();
		int idD = Defines.generateViewId();
		if(DisplayMetrics.isTablet && (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270))
		{
			LinearLayout general = new LinearLayout(context);
			general.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			
			FrameLayout fll = new FrameLayout(context);
			fll.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.3f));
			fll.setId(id);
			general.addView(fll);
			
			FrameLayout fld = new FrameLayout(context);
			fld.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.7f));
			fld.setId(idD);
			general.addView(fld);
			
			llvf = new LGListViewFragment();
			lf = new LuaFragment();
			ft.ReplaceFragment(id, llvf, false);
			ft.ReplaceFragment(idD, lf, false);
			view = general;
		}
		else
			view = new ListView(context);//ft.ReplaceFragment(R.id.generalDataListViewLinearLayout, gdlf, false);*/
        if(luaId != null)
        {
            modelRef = luaId + "ListRef";
            modelList = luaId + "List";
            modelTombstone = luaId + "Tombstone";
            modelSize = luaId + "Size";
            modelOffset = luaId + "Offset";
            modelLoadMore = luaId + "LoadMore";
            modelSpinner = luaId + "Spinner";
            modelNoMore = luaId + "NoMore";
            modelILoaded = luaId + "ILoaded";
            modelAdapter = luaId + "Adapter";

            html("<vue-recyclist " +
                    "ref = \"" + modelRef + "\"" +
                    ":list = \"" + modelList + "\"" +
                    ":tombstone = \"" + modelTombstone + "\"" +
                    ":size = \"" + modelSize + "\"" +
                    ":offset = \"" + modelOffset + "\"" +
                    ":loadmore = \"" + modelLoadMore + "\"" +
                    ":spinner = \"" + modelSpinner + "\"" +
                    ":nomore = \"" + modelNoMore + "\"" +
                    ":iloaded = \"" + modelILoaded + "\">" +
                    "<template slot=\"item\" scope=\"props\">" +
                    "<" + luaId.toLowerCase(Locale.US) + "-adapter-item id=\"" + modelAdapter + "\" :index=\"props.index\"></" + luaId.toLowerCase(Locale.US) + "-adapter-item>" +
                    "</template>" +
                    "<div slot=\"spinner\"></div>" +
                    "<div slot=\"nomore\"></div>" +
                    "</vue-recyclist>");

            jsCoreModel = "var " + luaId + "AdapterItem = {" +
                    "data: function() { return {} }," +
                    "template: '<component :is=\"`" + luaId + "adapteritem${index}`\" :id=\"`" + luaId + "_${index}`\" :data=\"data\" :index=\"index\"></component>'," +
                    "props: ['index', 'id', 'data']" +
                    "};" +
                    "Vue.component(\"" + luaId.toLowerCase(Locale.US) + "-adapter-item\", " + luaId + "AdapterItem);" +
                    "for(var i = 0; i < 11; i++)" +
                    "{" +
                        "eval(\"var " + luaId + "adapteritem\" + i + \" = function (resolve, reject) { listResolveMap['" + luaId + "adapteritem\" + i + \"'] = resolve; var id = '" + luaId + "'; var itemName = '" + luaId + "adapteritem\" + i + \"'; var index = \" + i + \"; var dataToSend = new ByteBuffer(); dataToSend.writeInt(CMSG_VIEW_FOR_INDEX); dataToSend.writeInt(id.length); dataToSend.writeString(id); dataToSend.writeInt(itemName.length); dataToSend.writeString(itemName); dataToSend.writeInt(index); dataToSend.flip(); webSocket.send(dataToSend.toBuffer()); };\"); " +
                        "Vue.component(\"" + luaId + "adapteritem\" + i, eval(\"" + luaId + "adapteritem\" + i));" +
                    "}";
            ArrayList<String> vals = new ArrayList<>();
            vals.add("aaaa");
            vals.add("aaaa");
            vals.add("aaaa");
            vals.add("aaaa");
            vals.add("aaaa");
            jsModel.add(new JSModel(modelList, vals));
            jsModel.add(new JSModel(modelTombstone, true));
            jsModel.add(new JSModel(modelSize, 10));
            jsModel.add(new JSModel(modelOffset, 0));
            jsModel.add(new JSModel(modelSpinner, false));
            jsModel.add(new JSModel(modelNoMore, false));

            methodModel.add(new MethodModel(luaId, "LoadMore"));
            methodModel.add(new MethodModel(luaId, "ILoaded"));
            //methodModel.add(new MethodModel(luaId, "ItemSelected"));
        }
        else
    	    html("<div>¨¨~~</div>");
    }

    /**
     * Gets the LGAdapterView of listview
     * @return LGAdapterView
     */
    @LuaFunction(manual = false, methodName = "GetAdapter", arguments = { })
    public LGAdapterView GetAdapter()
    {
    	return adapter;
    }

    /**
     * Sets the LGAdapterView of listview
     * @param adapter
     */
    @LuaFunction(manual = false, methodName = "SetAdapter", arguments = { LGAdapterView.class })
    public void SetAdapter(LGAdapterView adapter)
    {
    	this.adapter = adapter;
    	this.adapter.setParent(this);
        if(luaId != null)
        {
            LayoutServer.getInstance().notifyDataChanged(lc.GetContext().getClient(), new String[]{ modelSize, modelList}, new Integer[]{ LayoutServer.TYPE_INTEGER, LayoutServer.TYPE_OBJECT }, new Object[]{ this.adapter.getCount(), this.adapter.getItems() });
            LayoutServer.getInstance().notifyListRefresh(lc.GetContext().getClient(), luaId);
        }
    }
    
    /**
     * Gets the tablet mode status
     * @return boolean
     */
    @LuaFunction(manual = false, methodName = "IsUseTabletModeIfNecessaryEnabled", arguments = { })
    public boolean IsUseTabletModeIfNecessaryEnabled()
    {
    	return useTabletModeIfNecessary == true;
    }
    
    /**
     * Sets the tablet mode if tablet is present. 
     * If this value is set false, classic mode will be used.
     * @param tabletMode
     */
    @LuaFunction(manual = false, methodName = "SetUseTabletModeIfNecessary", arguments = { Boolean.class })
    public void SetUseTabletModeIfNecessary(boolean tabletMode)
    {
    	useTabletModeIfNecessary = tabletMode;
    }
    
    /**
     * Returns the selected cell color
     * @return int
     */
    @LuaFunction(manual = false, methodName = "GetSelectedCellColor")
    public Integer GetSelectedCellColor()
    {
    	return selectionColor;
    }
    
    /**
     * Sets the selected cell color.
     * @param color
     */
    @LuaFunction(manual = false, methodName = "SetSelectedCellColor", arguments = { Integer.class })
    public void SetSelectedCellColor(int color)
    {
    	selectionColor = color;
    }
    
    /**
     * (Ignore)
     */
    public LuaFragment GetDetailFragment()
    {
    	return lf;
    }
    
    /**
     * Registers Event for,
     * "Click" happens when user clicks on view
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
	}

    @Override
    public LGView viewForIndex(int index, String itemName)
    {
        LGView convertView = null;
        if(layoutMap.containsKey(index))
            convertView = layoutMap.get(index);
        if(GetAdapter() == null)
            return convertView;
        if(index >= GetAdapter().getCount())
            return convertView;
        LGView viewInflated = GetAdapter().getView(index, convertView, null/*TODO:*/);
        viewInflated.html("<div onclick='" + luaId + "ItemSelected(" + index + ")'>" + viewInflated.html() + "</div>");
        viewInflated.addMethodModel(new MethodModel(viewInflated.GetId(), "ItemSelected"));
        layoutMap.put(index, viewInflated);
        return viewInflated;
    }

    @Override
    public void CallEventValue(String id, String event, Object... vals)
    {
        if(event.equals("ItemSelected"))
        {
            adapter.getItemSelected().CallIn(this, GetDetailFragment(), (Integer) vals[0], adapter.getItem((Integer) vals[0]));
        }
    }
}
