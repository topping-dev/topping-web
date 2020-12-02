package com.dk.scriptingengine;

import java.net.InetSocketAddress;
import java.util.HashMap;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.luagui.LuaViewInflator;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.LayoutServer;

import android.widget.LGView;

/**
 * User interface form
 */
@LuaClass(className = "LuaForm")
public class LuaForm extends LGView implements LuaInterface
{
	private static HashMap<String, LuaForm> activeForms = new HashMap<>();
	protected String luaId = "LuaForm";
	protected String ui = "";
	protected LGView view;

    public LuaForm(Context context)
	{
		super(context);
	}

	public LuaForm(Context context, String luaId)
	{
		super(context, luaId);
	}

	/**
	 * Creates LuaForm Object From Lua.
	 * Form that created will be sent on GUI_EVENT_CREATE event.
	 * @param lc
	 * @param luaId
	 */
	@LuaFunction(manual = false, methodName = "Create", self = LuaForm.class, arguments = { LuaContext.class, String.class })
	public static void Create(LuaContext lc, String luaId)
	{
		LayoutServer.getInstance().changePage(lc.GetContext().getClient(), luaId, null);
	}
	
	/**
	 * Creates LuaForm Object From Lua with ui.
	 * Form that created will be sent on GUI_EVENT_CREATE event.
	 * @param lc
	 * @param luaId
	 * @param ui
	 */
	@LuaFunction(manual = false, methodName = "CreateWithUI", self = LuaForm.class, arguments = { LuaContext.class, String.class, String.class })
	public static void CreateWithUI(LuaContext lc, String luaId, String ui)
	{
		String[] arr = ui.split("\\.");
		LayoutServer.getInstance().changePage(lc.GetContext().getClient(), luaId, arr[0]);
	}

	//TODO:Documentation
	/**
	 * Creates LuaForm Object From Lua for tabs.
	 * @param lc
	 * @param luaId
	 * @return NativeObject
	 */
	@LuaFunction(manual = false, methodName = "CreateForTab", self = LuaForm.class, arguments = { LuaContext.class, String.class })
	public static Object CreateForTab(LuaContext lc, String luaId)
	{
		/*Intent intent = new Intent(lc.GetContext(), LuaForm.class);
		intent.putExtra("LUA_ID_RUED", luaId);
		return intent;*/
		return new Object();
	}
	
	/**
	 * Gets Active LuaForm
	 * @return LuaForm
	 */
	@LuaFunction(manual = false, methodName = "GetActiveForm", self = LuaForm.class, arguments = { LuaContext.class })
	public static LuaForm GetActiveForm(LuaContext lc)
	{
		return LuaForm.activeForms.get(lc.GetContext().getClient());
	}
	
	public static void SetActiveForm(LuaForm form)
	{
		LuaForm.activeForms.put(form.GetContext().GetContext().getClient(), form);
	}
	
	/**
	 * Gets LuaContext value of form
	 * @return LuaContext
	 */
	@LuaFunction(manual = false, methodName = "GetContext")
	public LuaContext GetContext()
	{
		return lc;
	}
	
	/**
	 * Gets the view of fragment.
	 * @return LGView
	 */
	@LuaFunction(manual = false, methodName = "GetViewById", arguments = { String.class })
	public LGView GetViewById(String lId)
	{
		return this.view.GetViewById(lId);
	}
	
	/**
	 * Gets the view.
	 * @return LGView
	 */
	@LuaFunction(manual = false, methodName = "GetView")
	public LGView GetView()
	{
		return view;
	}
	
	/**
	 * Sets the view to render.
	 * @param v
	 */
	@LuaFunction(manual = false, methodName = "SetView", arguments = { LGView.class })
	public void SetView(LGView v)
	{
		view = v;
		//setContentView(v);
	}
	
	/**
	 * Sets the xml file of the view to render.
	 * @param xml
	 */
	@LuaFunction(manual = false, methodName = "SetViewXML", arguments = { String.class })
	public void SetViewXML(String xml)
	{
		LuaViewInflator inflater = new LuaViewInflator(lc);
		view = inflater.ParseFile(xml, null);
		//setContentView(view.view);
	}
	
	/**
	 * Sets the title of the screen.
	 * @param str
	 */
	@LuaFunction(manual = false, methodName = "SetTitle", arguments = { String.class })
	public void SetTitle(String str)
	{
		//setTitle(str);
	}
	
	/**
	 * Closes the form
	 */
	@LuaFunction(manual = false, methodName = "Close")
	public void Close()
	{
		//finishActivity(-1);
	}
	
	/**
	 * Frees the created object.
	 */
	@LuaFunction(manual = false, methodName = "Free")
	public void Free()
	{
	}
	
	/**
	 * (Ignore)
	 */
	/*public void onCreateOverload(Bundle savedInstanceState)
	{
        Common.pack = getApplicationContext().getPackageName();
        Common.scale = getResources().getDisplayMetrics().density;
        
		super.onCreate(savedInstanceState);
	}*/
	
	/**
	 * (Ignore)
	 */
	/*@Override
	protected void onCreate(Bundle savedInstanceState)
	{			
		super.onCreate(savedInstanceState);
        
        Common.pack = getApplicationContext().getPackageName();
        Common.scale = getResources().getDisplayMetrics().density;
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		final PackageManager pm = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

        for (ResolveInfo temp : appList) 
        {
            Log.d("Lua Engine", "Launcher package and activity name = "
                    + temp.activityInfo.packageName + "    "
                    + temp.activityInfo.name);
            if(getClass().getSimpleName().compareTo(temp.activityInfo.name) == 0)
            {
            	return;
            }
        }
		LuaForm.activeForm = this;
		String luaId = "LuaForm";
		Bundle extras = null;
		if (savedInstanceState == null) 
		{
		    extras = getIntent().getExtras();
		    if(extras == null) 
		    {
		    	luaId = "LuaForm";
		    	ui = "";
		    } 
		    else 
		    {
		    	luaId= extras.getString("LUA_ID_RUED", "LuaForm");
		    	ui = extras.getString("LUA_UI_RUED", "");
		    }
		} 
		else 
		{
			luaId= savedInstanceState.getString("LUA_ID_RUED", "LuaForm");
			ui = savedInstanceState.getString("LUA_UI_RUED", "");
		}
		
		this.luaId = luaId;
		luaContext = LuaContext.CreateLuaContext(this);
		if(ui == null || ui.compareTo("") == 0)
			LuaEngine.getInstance().OnGuiEvent(this, GuiEvents.GUI_EVENT_CREATE, luaContext, this);
		else
		{
			LuaViewInflator inflater = new LuaViewInflator(luaContext);
			this.view = inflater.ParseFile(ui, null);
			setContentView(view.view);
		}
	}*/
	
	/**
	 * (Ignore)
	 */
	/*@Override
	protected void onResume()
	{
		super.onResume();
		LuaForm.activeForm = this;
		LuaEngine.getInstance().OnGuiEvent(this, GuiEvents.GUI_EVENT_RESUME, luaContext);
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1
				&& Defines.CheckPermission(this, android.Manifest.permission.NFC))
		{
			if(mNfcAdapter != null)
				mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
		}
		if(view != null)
		{
			view.onResume();
		}
	}*/
	
	/**
	 * (Ignore)
	 */
	/*@Override
	protected void onPause()
	{
		super.onPause();
		LuaEngine.getInstance().OnGuiEvent(this, GuiEvents.GUI_EVENT_PAUSE, luaContext);
		if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1
				&& Defines.CheckPermission(this, android.Manifest.permission.NFC))
		{		
			if(mNfcAdapter != null)
				mNfcAdapter.disableForegroundDispatch(this);
		}
		if(view != null)
		{
			view.onPause();
		}
	}*/
	
	/**
	 * (Ignore)
	 */
	/*@Override
	protected void onDestroy()
	{
		super.onDestroy();
		LuaEngine.getInstance().OnGuiEvent(this, GuiEvents.GUI_EVENT_DESTROY, luaContext);
		//Move destroy event to subviews
		if(view != null)
		{
			view.onDestroy();
		}
	}*/
	
	/**
	 * (Ignore)
	 */
	/*@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		luaId= savedInstanceState.getString("LUA_ID_RUED");
    	ui = savedInstanceState.getString("LUA_UI_RUED");
	}*/
	
	/**
	 * (Ignore)
	 */
	/*@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString("LUA_ID_RUED", luaId);
		outState.putString("LUA_UI_RUED", ui);
	}*/

	/**
	 * (Ignore)
	 */
	public String toWeb()
	{
		return view.toWeb();
	}

	/**
	 * (Ignore)
	 */
	public String toJsCoreModel()
	{
		return view.toJsCoreModel();
	}

	/**
	 * (Ignore)
	 */
	public String toJs()
	{
		return view.toJs();
	}

    /**
     * (Ignore)
     */
    public String toWatch()
    {
        return view.toWatch();
    }

	/**
	 * (Ignore)
	 */
	public String toMethod()
	{
		return view.toMethod();
	}

    /**
     * (Ignore)
     */
    public void AfterSetup()
    {
        this.view.AfterSetup(lc.GetContext());
    }

    /**
	 * (Ignore)
	 */
	@Override
	public void RegisterEventFunction(String var, LuaTranslator lt) 
	{
		
	}

	/**
	 * (Ignore)
	 */
	@Override
	public String GetId() 
	{
		if(luaId != null)
			return luaId;
		return "LuaForm";
	}
}
