package com.dk.scriptingengine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.widget.LGAbsListView;
import android.widget.LGAdapterView;
import android.widget.LGAutoCompleteTextView;
import android.widget.LGButton;
import android.widget.LGCheckBox;
import android.widget.LGComboBox;
import android.widget.LGCompoundButton;
import android.widget.LGDatePicker;
import android.widget.LGEditText;
import android.widget.LGFrameLayout;
import android.widget.LGLinearLayout;
import android.widget.LGListView;
import android.widget.LGMapView;
import android.widget.LGProgressBar;
import android.widget.LGRadioButton;
import android.widget.LGRadioGroup;
import android.widget.LGScrollView;
import android.widget.LGTextView;
import android.widget.LGView;

import com.dk.scriptingengine.osspecific.AssetManager;
import com.dk.scriptingengine.osspecific.Build;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.Log;
import com.dk.scriptingengine.osspecific.UrlHttpClient;
import com.luajava.Lua;
import com.luajava.Lua.CharPtr;
import com.luajava.Lua.lua_State;
import com.luajava.pBuffer;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.backend.LuaObject;
import com.dk.scriptingengine.backend.LuaTasker;
import com.dk.scriptingengine.backend.Lunar;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.luagui.LuaEventArgs;
import com.dk.scriptingengine.luagui.LuaViewInflator;
import com.dk.scriptingengine.osspecific.Defines;
import com.lordjoe.csharp.IDelegate;
import com.lordjoe.csharp.OneDelegate;

public class LuaEngine
{
	private Context androidContext;
	private static LuaEngine instance;
	private static List<Class<?>> plugins = new ArrayList<Class<?>>();
	
	public static void AddLuaPlugin(Class<?> plugin)
	{
		plugins.add(plugin);
	}
	
	public synchronized static LuaEngine getInstance()
	{
		if (instance == null)
		{
			// it's ok, we can call this constructor
			instance = new LuaEngine();
		}
		return instance;
	}

	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException(); 
		// that'll teach 'em
	}
	
	//reg type defines
	public final int REGTYPE_GUI = (1 << 0);
	
	/**
	 *	(Global)
	 */
	public enum GuiEvents
	{
		/**
		 * Zero initializer
		 */
	    GUI_EVENT_ZERO,
	    /**
	     * Fires when user interface is created
	     */
        GUI_EVENT_CREATE,
        /**
         * Fires when user interface resumed
         */
        GUI_EVENT_RESUME,
        /**
         * Fires when user interface paused
         */
        GUI_EVENT_PAUSE,
        /**
         * Fires when user interface destroyed
         */
        GUI_EVENT_DESTROY,
        /**
         * Fires when user interfaces updated
         */
	    GUI_EVENT_UPDATE,
	    /**
	     * Fires when user interface paint called
	     */
        GUI_EVENT_PAINT,
        /**
         * Fires when user interface tapped
         */
        GUI_EVENT_MOUSEDOWN,
        /**
         * Fires when user interface tap dropped
         */
        GUI_EVENT_MOUSEUP,
        /**
         * Fires when user touches and moves
         */
        GUI_EVENT_MOUSEMOVE,
        /**
         * Fires when adapter view needs view
         */
        GUI_EVENT_ADAPTERVIEW,
        /**
         * Internal use
         */
        GUI_EVENT_EVENT,
        /**
         * Fires when keystroke happened
         */
        GUI_EVENT_KEYDOWN,
        /**
         * Fires when keystoke dropped
         */
        GUI_EVENT_KEYUP,
        /**
         * Fires when nfc event happened
         */
        GUI_EVENT_NFC,
        /**
         * Count of the events
         */
	    GUI_EVENT_COUNT
	}
	
	protected LuaEngine()
	{
		super();
	}
	
	class LuaGuiBinding { HashMap<GuiEvents, String> Functions = new HashMap<GuiEvents, String>(); }
	
	HashMap<String, LuaGuiBinding> guiBinding = new HashMap<String, LuaEngine.LuaGuiBinding>();
	
	lua_State L;
	HashSet<lua_State> pendingThreads;
	
	String scriptsRoot = "";
	int primaryLoad = RESOURCE_DATA;
	double forceLoad = 0;
	String uiRoot;
	String mainUI;
	String mainForm;
	public static final int EXTERNAL_DATA = 1;
	public static final int INTERNAL_DATA = 2;
	public static final int RESOURCE_DATA = 3;
	public int webPort = 1723;
	public int webSocketPort = 1724;
	//TODO:
	//public ProgressDialog loadDialog;
	
	/* Custom values that stored */
	HashMap<String, UrlHttpClient> httpClientMap = new HashMap<>();
	LuaTasker<Class<?>> tasker = new LuaTasker<Class<?>>()
	{
		@Override
		public void DoJob(Class<?> val)
		{
			Lunar.methodMap.put(val, val.getMethods());
		}
	};
	Thread taskerThread;
	
	public void Startup(Context context)
	{
		androidContext = context;
		L = Lua.lua_open();
		pendingThreads = new HashSet<lua_State>();
		
		Lua.luaL_openlibs(L);
		/*try 
		{*/
			RegisterCoreFunctions();
			RegisterGlobals();
		/*} 
		catch (LuaException e) 
		{
			e.printStackTrace();
		}*/

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try
		{
			InputStream is = context.getAssets().open("defines.lua");
				
			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}

			buffer.flush();
			is.close();
		}
		catch (Exception e) 
		{
			Log.e("LuaEngine.java", e.getMessage());
		}

		if(Lua.luaL_loadstring(L, buffer.toString()) != 0)
		//if(Lua.luaL_loadbuffer(L, buffer.toString(), s) != 0)
		//if(Lua.LloadFile(s) != 0)
		{
			Report(L);
		}
		else
		{
			if(Lua.lua_pcall(L, 0, 0, 0) != 0)
			{
				Report(L);
			}
			else
			{
				Log.i("LuaEngine", "Script defines.lua loaded.");
			}
		}

		try
		{
			buffer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		scriptsRoot = "";
		Lua.lua_getglobal(L, "ScriptsRoot");
		if(Lua.lua_isstring(L, -1) == 0)
			Log.e("LuaEngine.java", "ScriptsRoot must be string");
		else
			scriptsRoot = Lua.lua_tostring(L, -1).toString();
		Lua.lua_pop(L, 1);
		
		primaryLoad = RESOURCE_DATA;
		Lua.lua_getglobal(L, "PrimaryLoad");
		if(Lua.lua_isnumber(L, -1) == 0)
			Log.e("LuaEngine.java", "PrimaryLoad must be number");
		else
			primaryLoad = Lua.lua_tointeger(L, -1);
		Lua.lua_pop(L, 1);
		
		forceLoad = 0;
		Lua.lua_getglobal(L, "ForceLoad");
		if(Lua.lua_isnumber(L, -1) == 0)
			Log.e("LuaEngine.java", "ForceLoad must be number");
		else
			forceLoad = Lua.lua_tointeger(L, -1);
		Lua.lua_pop(L, 1);
		
		uiRoot = "ui";
		Lua.lua_getglobal(L, "UIRoot");
		if(Lua.lua_isstring(L, -1) == 0)
			Log.e("LuaEngine.java", "UIRoot must be string");
		else
			uiRoot = Lua.lua_tostring(L, -1).toString();
		Lua.lua_pop(L, 1);
		
		mainUI = "main.xml";
		Lua.lua_getglobal(L, "MainUI");
		if(Lua.lua_isstring(L, -1) == 0)
			Log.e("LuaEngine.java", "MainUI must be string");
		else
			mainUI = Lua.lua_tostring(L, -1).toString();
		Lua.lua_pop(L, 1);
		
		mainForm = "";
		Lua.lua_getglobal(L, "MainForm");
		if(Lua.lua_isstring(L, -1) == 0)
			Log.e("LuaEngine.java", "MainForm must be string");
		else
			mainForm = Lua.lua_tostring(L, -1).toString();
		Lua.lua_pop(L, 1);

		Lua.lua_getglobal(L, "WebPort");
		if(Lua.lua_isnumber(L, -1) == 0)
			Log.e("LuaEngine.java", "MainForm must be number");
		else
			webPort = Lua.lua_tointeger(L, -1);
		Lua.lua_pop(L, 1);

		Lua.lua_getglobal(L, "WebSocketPort");
		if(Lua.lua_isnumber(L, -1) == 0)
			Log.e("LuaEngine.java", "MainForm must be number");
		else
			webSocketPort = Lua.lua_tointeger(L, -1);
		Lua.lua_pop(L, 1);
		
		Lua.lua_getglobal(L, "LuaDebug");
		if(Lua.lua_isnumber(L, -1) == 1 && Lua.lua_tointeger(L, -1) == 1)
		{
			//Create Socket libraries
			Lua.lua_pushcfunction(L, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return Lua.luaopen_socket_core((lua_State)arg);
				}
			});
			Lua.lua_pushstring(L, "socket");
			Lua.lua_call(L, 1, 0);
			
			Lua.lua_pushcfunction(L, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return Lua.luaopen_mime_core((lua_State)arg);
				}
			});
			Lua.lua_pushstring(L, "mime");
			Lua.lua_call(L, 1, 0);
			
			String scriptsRootStore = scriptsRoot;
			scriptsRoot = "";
			
			Lua.luaL_loadfile(L, "mime.lua");
			Lua.lua_pcall(L, 0, 0, 0);
			Lua.luaL_loadfile(L, "socket.lua");
			Lua.lua_pcall(L, 0, 0, 0);
			Lua.luaL_loadfile(L, "socket/url.lua");
			Lua.lua_pcall(L, 0, 0, 0);
			Lua.luaL_loadfile(L, "ltn12.lua");
			Lua.lua_pcall(L, 0, 0, 0);
			
			scriptsRoot = scriptsRootStore;
			
			Lua.lua_getglobal(L, "SocketBufferSize");
			if(Lua.lua_isnumber(L, -1) == 0)
				Log.i("LuaEngine.java", "SocketBufferSize not set using default");
			else
				Lua.STEPSIZE = Lua.lua_tointeger(L, -1);
			Lua.lua_pop(L, 1);
			
			Lua.lua_getglobal(L, "LuaBufferSize");
			if(Lua.lua_isnumber(L, -1) == 0)
				Log.i("LuaEngine.java", "LuaBufferSize not set using default");
			else
				Lua.LUAL_BUFFERSIZE = Lua.lua_tointeger(L, -1);
			Lua.lua_pop(L, 1);
			
			Lua.lua_getglobal(L, "PBufferSize");
			if(Lua.lua_isnumber(L, -1) == 0)
				Log.i("LuaEngine.java", "PBufferSize not set using default");
			else
				pBuffer.BUF_SIZE = Lua.lua_tointeger(L, -1);
			Lua.lua_pop(L, 1);
		}
		Lua.lua_pop(L, 1);
		
		//TODO
		//Na burda load etmeye başla
		StartupDefines();
	}
	
	public void DeleteFolder(File path)
	{
		File[] farr = path.listFiles();
		if(farr != null)
		{
			for(File f : farr)
			{
				if(f.isDirectory())
				{
					DeleteFolder(f);
				}
				else
					f.delete();
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void StartupDefines()
	{
		AssetManager assetManager = androidContext.getAssets();
		
		String[] rtn = null;
		try 
		{
			rtn = assetManager.list(scriptsRoot);
		} 
		catch (IOException e)
		{
			Log.e("LuaEngine", e.getMessage());
		}
		  
		int cnt_uncomp = 0;

		switch(primaryLoad)
		{
			case EXTERNAL_DATA: //External SD
			{
				String scriptsDir = Defines.GetExternalPathForResource(androidContext, scriptsRoot);
				File scripts = new File(scriptsDir);
				{
					if(!scripts.exists())
						scripts.mkdir();
					else
						DeleteFolder(scripts);
					for(String s : rtn)
					{
						try
						{
							InputStream is = assetManager.open(scriptsRoot + "/" + s);
							File scriptFile = new File(scriptsDir + "/" + s);
							if(!scriptFile.exists() || forceLoad > 0)
							{
								if(scriptFile.exists())
									scriptFile.delete();
								scriptFile.createNewFile();

								//Open the empty file as the output stream
						    	FileOutputStream myOutput = new FileOutputStream(scriptFile, false);

						    	//transfer bytes from the inputfile to the outputfile
						    	byte[] buffer = new byte[1024];
						    	int length;
						    	while ((length = is.read(buffer)) > 0)
						    	{
						    		myOutput.write(buffer, 0, length);
						    	}

						    	//Close the streams
						    	myOutput.flush();
						    	myOutput.close();
							}
							is.close();
						}
						catch (Exception e)
						{
							File scriptFile = new File(scriptsDir + "/" + s);
							if(scriptFile.exists())
								scriptFile.delete();
						}
					}
				}

				if(scripts == null)
				{
					Log.e("LuaEngine.java", "Cannot find sdcard to load binaries");
					return;
				}

				int count = 0;
				String[] files = scripts.list();
				for(String s : files)
				{
					try
					{
						if(Lua.luaL_loadfile(L, s) != 0)
						{
							Report(L);
						}
						else
						{
							if(Lua.lua_pcall(L, 0, 0, 0) != 0)
							{
								Report(L);
							}
							else
							{
								Log.i("LuaEngine", "Script " + s + " loaded.");
								count++;
								final int countF = count;
								final String[] filesF = files;
								//TODO
								/*((Activity)androidContext).runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										if(loadDialog != null)
											loadDialog.setProgress((loadDialog.getMax() * countF) / filesF.length);
									}
								});*/
							}
						}

						//buffer.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					cnt_uncomp++;
				}
			}break;
			case RESOURCE_DATA: //Internal Data
			{
				int count = 0;
				for(String s : rtn)
				{
					try
					{
						/*InputStream is = assetManager.open(scriptsRoot + "/" + s);
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();

						int nRead;
						byte[] data = new byte[16384];

						while ((nRead = is.read(data, 0, data.length)) != -1) {
						  buffer.write(data, 0, nRead);
						}

						buffer.flush();
						is.close();*/

						//if(Lua.luaL_loadstring(L, buffer.toString()) != 0)
						if(Lua.luaL_loadfile(L, s) != 0)
						{
							Report(L);
						}
						else
						{
							if(Lua.lua_pcall(L, 0, 0, 0) != 0)
							{
								Report(L);
							}
							else
							{
								Log.i("LuaEngine", "Script " + s + " loaded.");
								count++;
								final int countF = count;
								final String[] rtnF = rtn;
								//TODO:
								/*
								((Activity)androidContext).runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										loadDialog.setProgress((loadDialog.getMax() * countF) / rtnF.length);
									}
								});*/
							}
						}

						//buffer.close();
					}
					catch (Exception e)
					{
					}
					cnt_uncomp++;
				}
			}break;
			case INTERNAL_DATA: //Assets
			{
				String scriptsDir = androidContext.getFilesDir().getAbsolutePath();
				scriptsDir = scriptsDir + "/" + scriptsRoot;
				File scripts = new File(scriptsDir);
				{
					if(!scripts.exists())
						scripts.mkdir();
					else
						DeleteFolder(scripts);
					for(String s : rtn)
					{
						try
						{
							InputStream is = assetManager.open(scriptsRoot + "/" + s);
							File scriptFile = new File(scriptsDir + "/" + s);
							if(!scriptFile.exists() || forceLoad > 0)
							{
								if(scriptFile.exists())
									scriptFile.delete();
								scriptFile.createNewFile();

								//Open the empty file as the output stream
						    	FileOutputStream myOutput = new FileOutputStream(scriptFile, false);

						    	//transfer bytes from the inputfile to the outputfile
						    	byte[] buffer = new byte[1024];
						    	int length;
						    	while ((length = is.read(buffer)) > 0)
						    	{
						    		myOutput.write(buffer, 0, length);
						    	}

						    	//Close the streams
						    	myOutput.flush();
						    	myOutput.close();
							}
							is.close();
						}
						catch (Exception e)
						{
							File scriptFile = new File(scriptsDir + "/" + s);
							if(scriptFile.exists())
								scriptFile.delete();
						}
					}
				}

				if(scripts == null)
				{
					Log.e("LuaEngine.java", "Cannot find internal data to load binaries");
					return;
				}

				int count = 0;
				String[] files = scripts.list();
				for(String s : files)
				{
					try
					{
						/*FileInputStream is = new FileInputStream(scripts.getAbsolutePath() + "/" + s);
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();

						int nRead;
						byte[] data = new byte[16384];

						while ((nRead = is.read(data, 0, data.length)) != -1) {
						  buffer.write(data, 0, nRead);
						}

						buffer.flush();
						is.close();*/

						//if(Lua.luaL_loadstring(L, buffer.toString()) != 0)
						if(Lua.luaL_loadfile(L, s) != 0)
						{
							Report(L);
						}
						else
						{
							if(Lua.lua_pcall(L, 0, 0, 0) != 0)
							{
								Report(L);
							}
							else
							{
								Log.i("LuaEngine", "Script " + s + " loaded.");
								count++;
								final int countF = count;
								final String[] filesF = files;
								//TODO:
								/*((Activity)androidContext).runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										loadDialog.setProgress((loadDialog.getMax() * countF) / filesF.length);
									}
								});*/
							}
						}

						//buffer.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					cnt_uncomp++;
				}
			}break;
		}
		
		Log.i("LuaEngine", "Loaded " + cnt_uncomp + " Lua scripts.");
	}
	
	public void Startup(String root)
	{
		L = Lua.lua_open();
		pendingThreads = new HashSet<lua_State>();
		
		LoadScripts(root);
	}
	
	public void Startup(Context context, AssetManager assetManager, String root)
	{
		L = Lua.lua_open();
		pendingThreads = new HashSet<lua_State>();
		
		LoadScriptsFromAsset(context, assetManager, root);
	}
	
	void ScriptLoadDir(String root, HashSet<String> rtn)
	{
		File dir = new File(root);
		if(dir.isDirectory())
		{
			for(File file : dir.listFiles())
				ScriptLoadDir(file.getPath(), rtn);
		}
		else
		{
			String filename = dir.getName();
			String[] arr = filename.split("\\.");
			if(arr[arr.length - 1].compareTo("lua") == 0)
			{
				rtn.add(dir.getAbsolutePath());
			}
		}
	}
	
	boolean SetScriptFromStream(Context context, String name, InputStream is)
	{
		try
		{
			File filesDir = context.getFilesDir();
			String fileDir = filesDir.getAbsolutePath() + "/scripts/" + name;
			File file = new File(fileDir);
			if(!file.exists())
				return false;
			
			file.delete();
			file.createNewFile();
			
			//Open the empty db as the output stream
	    	FileOutputStream myOutput = new FileOutputStream(file, false);
	
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = is.read(buffer)) > 0)
	    	{
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	is.close();
		}
		catch (Exception e) 
		{
			return false;
		}
		return true;
	}
		
	void LoadScriptsFromAsset(Context context, AssetManager assetManager, String root)
	{
		String[] rtn = null;
		try 
		{
			rtn = assetManager.list(root);
		} 
		catch (IOException e)
		{
			Log.e("LuaEngine", e.getMessage());
		}
		  
		int cnt_uncomp=0;

		//File filesDir = context.getFilesDir();
		//String scriptsDir = filesDir.getAbsolutePath() + "/scripts";
		String scriptsDir = Defines.GetExternalPathForResource(context, root);
		File scripts = new File(scriptsDir);
		{
			if(!scripts.exists())
				scripts.mkdir();
			for(String s : rtn)
			{
				try 
				{
					InputStream is = assetManager.open(root + "/" + s);
					File scriptFile = new File(scriptsDir + "/" + s);
					if(!scriptFile.exists())
					{
						scriptFile.createNewFile();
					
						//Open the empty db as the output stream
				    	FileOutputStream myOutput = new FileOutputStream(scriptFile, false);
	
				 
				    	//transfer bytes from the inputfile to the outputfile
				    	byte[] buffer = new byte[1024];
				    	int length;
				    	while ((length = is.read(buffer)) > 0)
				    	{
				    		myOutput.write(buffer, 0, length);
				    	}
				 
				    	//Close the streams
				    	myOutput.flush();
				    	myOutput.close();
					}
					is.close();
				}
				catch (Exception e) 
				{
				}
			}
		}
		for(String s : scripts.list())
		{
			try 
			{
				//InputStream is = assetManager.open(root + "/" + s);
				FileInputStream is = new FileInputStream(scripts.getAbsolutePath() + "/" + s);
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				int nRead;
				byte[] data = new byte[16384];

				while ((nRead = is.read(data, 0, data.length)) != -1) {
				  buffer.write(data, 0, nRead);
				}

				buffer.flush();
				is.close();
				
				if(Lua.luaL_loadstring(L, buffer.toString()) != 0)
				//if(Lua.luaL_loadbuffer(L, buffer.toString(), s) != 0)
				//if(Lua.LloadFile(s) != 0)
				{
					Report(L);
				}
				else
				{
					if(Lua.lua_pcall(L, 0, 0, 0) != 0)
					{
						Report(L);
					}
					else
					{
						Log.i("LuaEngine", "Script " + s + " loaded.");
					}				
				}
				
				buffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			cnt_uncomp++;
		}
		Log.i("LuaEngine", "Loaded " + cnt_uncomp + " Lua scripts.");
	}
	
	void LoadScripts(String root)
	{
		HashSet<String> rtn = new HashSet<String>();
		ScriptLoadDir(root, rtn);
		
		int cnt_uncomp=0;

		Lua.luaL_openlibs(L);
		
		RegisterCoreFunctions();		
		
		for(String s : rtn)
		{
			if(Lua.luaL_loadfile(L, new CharPtr(s)) != 0)
			{
				Report(L);
			}
			else
			{
				if(Lua.lua_pcall(L, 0, 0, 0) != 0)
				{
					Report(L);
				}
				else
				{
					Log.i("LuaEngine", "Script " + s + " loaded.");
				}				
			}
			cnt_uncomp++;
		}
		Log.i("LuaEngine", "Loaded " + cnt_uncomp + " Lua scripts.");
	}
	
	void Report(lua_State L)
	{
		int count = 20;
		CharPtr msgP = Lua.lua_tostring(L, -1);
		if(msgP == null)
			return;
		
		String msg= msgP.toString();
		while(msg != null && count > 0)
		{
			Lua.lua_pop(L, -1);
			Log.e("LuaEngine", msg);
			msgP = Lua.lua_tostring(L, -1);
			if(msgP == null)
				return;
			msg = msgP.toString();
			count--;
		}
	}
	
	public boolean BeginCall(String func)
	{
		String sFuncName = new String(func);
		String copy = new String(func);
		StringTokenizer st = new StringTokenizer(copy, ".:");
		boolean colon = false;
		if(copy.indexOf(".:") == -1)
			Lua.lua_getglobal(L, func);
		else
		{
			Lua.lua_getglobal(L, "_G"); //start out with the global table.
			int top = 1;
			String token = st.nextToken();
			while(st.hasMoreTokens())
			{
				Lua.lua_getfield(L, -1, token); //get the (hopefully) table/func
				if(sFuncName.indexOf(token) != -1) //if it isn't the first token
				{
					if(sFuncName.charAt(sFuncName.indexOf(token)) == '.') //if it was a .
						colon = false;
					else if(sFuncName.charAt(sFuncName.indexOf(token)) == ':')
						colon = true;
				}
				else //if it IS the first token, we're OK to remove the "_G" from the stack
					colon = false;
				
				if(Lua.lua_isfunction(L, -1) && !Lua.lua_iscfunction(L, -1)) //if it's a Lua function
				{
					Lua.lua_replace(L, top);
					if(colon)
					{
						Lua.lua_pushvalue(L, -1); //make the table the first arg
						Lua.lua_replace(L, top + 1);
						Lua.lua_settop(L, top + 1);
					}
					else
						Lua.lua_settop(L, top);
					break;
				}
				else if(Lua.lua_istable(L, -1))
					token = st.nextToken();
			}

			
		}

		return colon;
	}
	
	public boolean ExecuteCall(int params, int res)
	{
		if(Lua.lua_pcall(L, params, res, 0) > 0)
		{
			Report(L);
			return false;
		}
		return true;
	}
	
	public void EndCall(int res)
	{
		for(int i = res; i > 0; i--)
		{
			if(!Lua.lua_isnone(L, res))
				Lua.lua_remove(L, res);
		}
	}
	
	public void CallFunction(String FuncName, int ref)
	{
		int top = Lua.lua_gettop(L);
		int args = 0;
		String sFuncName = new String(FuncName);
		String copy = new String(FuncName);
		StringTokenizer st = new StringTokenizer(copy, ".:");
		boolean colon = false;
		if(copy.indexOf(".:") == -1)
			Lua.lua_getglobal(L, FuncName);
		else
		{
			Lua.lua_getglobal(L, "_G"); //start out with the global table.
			String token = st.nextToken();
			while(st.hasMoreTokens())
			{
				Lua.lua_getfield(L, -1, token); //get the (hopefully) table/func
				if(sFuncName.indexOf(token) != -1) //if it isn't the first token
				{
					if(sFuncName.charAt(sFuncName.indexOf(token)) == '.') //if it was a .
						colon = false;
					else if(sFuncName.charAt(sFuncName.indexOf(token)) == ':')
						colon = true;
				}
				else //if it IS the first token, we're OK to remove the "_G" from the stack
					colon = false;
				
				if(Lua.lua_isfunction(L, -1) && !Lua.lua_iscfunction(L, -1)) //if it's a Lua function
				{
					if(colon)
					{
						Lua.lua_pushvalue(L, -2); //make the table the first arg
						Lua.lua_remove(L, -3);
						++args;
					}
					else
						Lua.lua_remove(L, -2);
					break;
				}
				else if(Lua.lua_istable(L, -1))
					token = st.nextToken();
			}			
		}
		Lua.lua_rawgeti(L, Lua.LUA_REGISTRYINDEX, ref);
		lua_State M = Lua.lua_tothread(L, -1);
		int thread = Lua.lua_gettop(L);
		int repeats = Lua.luaL_checkinteger(M, 1); //repeats, args
		int nargs = Lua.lua_gettop(M) - 1;
		if(nargs != 0) //if we HAVE args...
		{
			for(int i = 2; i <= nargs + 1; i++)
			{
				Lua.lua_pushvalue(M, i);
			}
			Lua.lua_xmove(M, L, nargs);
		}
		if(--repeats == 0) //free stuff, then
		{
			Lua.luaL_unref(L, Lua.LUA_REGISTRYINDEX, ref);
		}
		else
		{
			Lua.lua_remove(M, 1); //args
			Lua.lua_pushinteger(M, repeats); //args, repeats
			Lua.lua_insert(M, 1); //repeats, args
		}
		Lua.lua_remove(L, thread); //now we can remove the thread object
		int r = Lua.lua_pcall(L, nargs + args, 0, 0);
		if(r != 0)
			Report(L);
		
		Lua.lua_settop(L, top);
	}
	
	void FillVariable(Object val)
	{
		if(val == null)
		{
			PushNIL();
			return;
		}
		/*String name = val.getClass().getName();
	    if(name.compareTo("java.lang.Boolean") == 0)
	        PushBool((Boolean)val);
	    else if(name.compareTo("java.lang.Byte") == 0
	    	|| name.compareTo("java.lang.Short") == 0
	    	|| name.compareTo("java.lang.Integer") == 0)
	    	PushInt(Integer.parseInt(val.toString()));
	    else if(name.compareTo("java.lang.Long") == 0)
	    	PushLong(((Long)val).longValue());
	    else if(name.compareTo("java.lang.Float") == 0)
	    	PushFloat((Float)val);
	    else if(name.compareTo("java.lang.Double") == 0)
	    	PushDouble((Double)val);
	    else if(name.compareTo("java.lang.Char") == 0
	    	|| name.compareTo("java.lang.String") == 0)
	    	PushString(val.toString());
	    else if(name.compareTo("java.lang.Void") == 0)
	    	PushInt(0);
	    else if(name.compareTo("com.luajava.Lua.lua_State") == 0)
	    	return;
	    else if(name.compareTo("java.util.HashMap") == 0)
	    	PushTable((HashMap<Object, Object>) val);*/
		Class name = val.getClass();
	    if(name == java.lang.Boolean.class)
	        PushBool((Boolean)val);
	    else if(name == java.lang.Byte.class
	    	|| name == java.lang.Short.class
	    	|| name == java.lang.Integer.class)
	    	PushInt(Integer.parseInt(val.toString()));
	    else if(name == java.lang.Long.class)
	    	PushLong(((Long)val).longValue());
	    else if(name == java.lang.Float.class)
	    	PushFloat((Float)val);
	    else if(name == java.lang.Double.class)
	    	PushDouble((Double)val);
	    else if(name == java.lang.Character.class
	    	|| name == java.lang.String.class)
	    	PushString(val.toString());
	    else if(name == java.lang.Void.class)
	    	PushInt(0);
	    else if(name == com.luajava.Lua.lua_State.class)
	    	return;
	    else if(name == java.util.HashMap.class)
	    	PushTable((HashMap<Object, Object>) val);
	    else
	    	Lunar.push(L, val, true);
	}
	
	public Object OnGuiEvent(Object gui, String FunctionName, Object ... arguments)
	{
		if(FunctionName == null || FunctionName.compareTo("") == 0)
			return null;
		
		Lua.lua_pushstring(L, FunctionName);
		Lua.lua_gettable(L, Lua.LUA_GLOBALSINDEX);
		if(Lua.lua_isnil(L, -1))
		{
			Log.e("LuaEngine", "Tried to call invalid LUA function '" + FunctionName + "' from Player!\n");
			return null;
		}
		
		Lunar.push(L, gui, false);
		
		int j = 0;
		for(Object type : arguments)
		{
			FillVariable(type);
			j++;
		}
		
		int r = Lua.lua_pcall(L, j + 1, Lua.LUA_MULTRET, 0);
		if(r != 0)
		{
			Report(L);
			return null;
		}
		
		Object retVal = null;
		Lua.lua_TValue valTest = new Lua.lua_TValue(L.top);
		valTest.set_index(0);
		if(Lua.lua_TValue.OpLessThanOrEqual(L.top, valTest) || Lua.lua_isnoneornil(L, -1))
			retVal = null;
		else if(Lua.lua_isboolean(L, -1))
			retVal = (Lua.lua_toboolean(L, -1) == 1) ? true : false;
		else if(Lua.lua_isnumber(L, -1) > 0)
			retVal = Double.valueOf((Lua.lua_tonumber(L, -1)));
		else if(Lua.lua_isstring(L, -1) > 0)
			retVal = Lua.lua_tostring(L, -1).toString();
		else
		{
			//argList.add(Lua.luaL_checkudata(L, count, c.getName()));
    		Object o = Lua.lua_touserdata(L, -1);
    		if(o != null)
    		{
        		if(o.getClass() == com.dk.scriptingengine.backend.LuaObject.class)
                {
        			Object obj = ((LuaObject<?>)o).obj;
        			if(obj == null)
        			{
        				Log.e("Lunar Push", "Cannot get lua object property static thunk");
        				retVal = null;
        			}
        			retVal = obj;
                }
                else
                {
                	retVal = o;
                }
    		}
    		else
    		{
    			o = Lua.lua_topointer(L, -1);
    			Lua.Table ot = (Lua.Table)o;
    			HashMap<Object, Object> map = new HashMap<Object, Object>();
    			int size = Lua.sizenode(ot);
    			for(int i = 0; i < size; i++)
    			{
    				Lua.Node node = Lua.gnode(ot, i);
    				Lua.lua_TValue key = Lua.key2tval(node);
    				Object keyObject = null;
    				switch(Lua.ttype(key))
					{
						case Lua.LUA_TNIL:
							break;
						case Lua.LUA_TSTRING:
						{
							Lua.TString str = Lua.rawtsvalue(key);
							keyObject = str.toString();
						}break;
						case Lua.LUA_TNUMBER:
							keyObject = Lua.nvalue(key);
							break;
						default:
							break;
					}
    				
    				Lua.lua_TValue val = Lua.luaH_get(ot, key);
    				Object valObject = null;
    				switch(Lua.ttype(val))
    				{
    					case Lua.LUA_TNIL:
    						break;
    					case Lua.LUA_TSTRING:
    					{
    						Lua.TString str = Lua.rawtsvalue(val);
    						valObject = str.toString();
    					}break;
    					case Lua.LUA_TNUMBER:
    						valObject = Lua.nvalue(val);
    						break;
    					case Lua.LUA_TTABLE:
    						valObject = Lunar.ParseTable(val);
    						break;
    					case Lua.LUA_TUSERDATA:
    					{
    						valObject = (Lua.rawuvalue(val).user_data);
    						if(valObject != null)
    						{
        						if(valObject.getClass() == com.dk.scriptingengine.backend.LuaObject.class)
        	                    {
        	            			Object objA = ((LuaObject<?>)valObject).obj;
        	            			if(objA == null)
        	            			{
        	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
        	            				retVal = null;
        	            			}
        	
        	                        valObject = objA;
        	                    }
    						}
    					}break;
    					case Lua.LUA_TLIGHTUSERDATA:
    					{
    						valObject = Lua.pvalue(val);
    						if(valObject != null)
    						{
        						if(valObject.getClass() == com.dk.scriptingengine.backend.LuaObject.class)
        	                    {
        	            			Object objA = ((LuaObject<?>)valObject).obj;
        	            			if(objA == null)
        	            			{
        	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
        	            				retVal = null;
        	            			}
        	
        	                        valObject = objA;
        	                    }
    						}
    					}break;
    				}
    				map.put(keyObject, valObject);
    			}
    			retVal = map;
    		}
		}
		
		/*try
		{
			Lua.lua_pop(L, 1);
		}
		catch(Exception ex)
		{
			
		}*/
		return retVal;
	}
	
	public Object OnGuiEvent(Object gui, GuiEvents eventType, Object ... arguments)
	{
		LuaInterface li = (LuaInterface)gui;
		LuaGuiBinding binding = guiBinding.get(li.GetId());
		if(binding == null)
			return null;
		if(!binding.Functions.containsKey(eventType))
			return null;
		
		String FunctionName = binding.Functions.get(eventType);

		if(FunctionName == null || FunctionName.compareTo("") == 0)
			return null;

		//m_Lock.Acquire();
		Lua.lua_pushstring(L, FunctionName);
		Lua.lua_gettable(L, Lua.LUA_GLOBALSINDEX);
		if(Lua.lua_isnil(L, -1))
		{
			Log.e("LuaEngine", "Tried to call invalid LUA function '" + FunctionName + "' from Gui!");
			//m_Lock.Release();
			return null;
		}

		//Lunar<Player>::push(lu, pUnit);
		//Lua.pushJavaObject(gui);
		Lunar.push(L, gui, false);
		int j = 0;
		for(Object type : arguments)
		{
			FillVariable(type);
			j++;
		}
		
		int r = Lua.lua_pcall(L, j + 1, Lua.LUA_MULTRET, 0);
		if(r != 0)
			Report(L);
		
		Object retVal = null;
		Lua.lua_TValue valTest = new Lua.lua_TValue(L.top);
		valTest.set_index(0);
		if(Lua.lua_TValue.OpLessThanOrEqual(L.top, valTest) || Lua.lua_isnoneornil(L, -1))
			retVal = null;
		else if(Lua.lua_isboolean(L, -1))
			retVal = (Lua.lua_toboolean(L, -1) == 1) ? true : false;
		else if(Lua.lua_isnumber(L, -1) > 0)
			retVal = Double.valueOf((Lua.lua_tonumber(L, -1)));
		else if(Lua.lua_isstring(L, -1) > 0)
			retVal = Lua.lua_tostring(L, -1).toString();
		else
		{
			//argList.add(Lua.luaL_checkudata(L, count, c.getName()));
    		Object o = Lua.lua_touserdata(L, -1);
    		if(o != null)
    		{
        		if(o.getClass() == com.dk.scriptingengine.backend.LuaObject.class)
                {
        			Object obj = ((LuaObject<?>)o).obj;
        			if(obj == null)
        			{
        				Log.e("Lunar Push", "Cannot get lua object property static thunk");
        				retVal = null;
        			}
        			retVal = obj;
                }
                else
                {
                	retVal = o;
                }
    		}
    		else
    		{
    			o = Lua.lua_topointer(L, -1);
    			if(o.getClass() != Lua.Table.class)
    				retVal = o;
    			else
    			{
	    			Lua.Table ot = (Lua.Table)o;
	    			HashMap<Object, Object> map = new HashMap<Object, Object>();
	    			int size = Lua.sizenode(ot);
	    			for(int i = 0; i < size; i++)
	    			{
	    				Lua.Node node = Lua.gnode(ot, i);
	    				Lua.lua_TValue key = Lua.key2tval(node);
	    				Object keyObject = null;
	    				switch(Lua.ttype(key))
						{
							case Lua.LUA_TNIL:
								break;
							case Lua.LUA_TSTRING:
							{
								Lua.TString str = Lua.rawtsvalue(key);
								keyObject = str.toString();
							}break;
							case Lua.LUA_TNUMBER:
								keyObject = Lua.nvalue(key);
								break;
							default:
								break;
						}
	    				
	    				Lua.lua_TValue val = Lua.luaH_get(ot, key);
	    				Object valObject = null;
	    				switch(Lua.ttype(val))
	    				{
	    					case Lua.LUA_TNIL:
	    						break;
	    					case Lua.LUA_TSTRING:
	    					{
	    						Lua.TString str = Lua.rawtsvalue(val);
	    						valObject = str.toString();
	    					}break;
	    					case Lua.LUA_TNUMBER:
	    						valObject = Lua.nvalue(val);
	    						break;
	    					case Lua.LUA_TTABLE:
	    						valObject = Lunar.ParseTable(val);
	    						break;
	    					case Lua.LUA_TUSERDATA:
	    					{
	    						valObject = (Lua.rawuvalue(val).user_data);
	    						if(valObject != null)
	    						{
	        						if(valObject == com.dk.scriptingengine.backend.LuaObject.class)
	        	                    {
	        	            			Object objA = ((LuaObject<?>)valObject).obj;
	        	            			if(objA == null)
	        	            			{
	        	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
	        	            				retVal = null;
	        	            			}
	        	
	        	                        valObject = objA;
	        	                    }
	    						}
	    					}break;
	    					case Lua.LUA_TLIGHTUSERDATA:
	    					{
	    						valObject = Lua.pvalue(val);
	    						if(valObject != null)
	    						{
	        						if(valObject.getClass()== com.dk.scriptingengine.backend.LuaObject.class)
	        	                    {
	        	            			Object objA = ((LuaObject<?>)valObject).obj;
	        	            			if(objA == null)
	        	            			{
	        	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
	        	            				retVal = null;
	        	            			}
	        	
	        	                        valObject = objA;
	        	                    }
	    						}
	    					}break;
	    				}
	    				map.put(keyObject, valObject);
	    			}
	    			retVal = map;
    			}
    		}
		}
		
		if(!Lua.lua_TValue.OpLessThanOrEqual(L.top, valTest))
			Lua.lua_pop(L, 1);
		return retVal;
	}

	void RegisterCoreFunctions()
	{
		Lua.lua_register(L, "RegisterGuiEvent", new IDelegate() {
			
			@Override
			public Object invoke() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object invoke(Object arg1, Object arg2) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object invoke(Object arg) {
				return RegisterGuiEvent((lua_State)arg);
			}
			
			@Override
			public Object invoke(Object[] args) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		Lua.lua_register(L, "Log", new IDelegate() {
			
			@Override
			public Object invoke() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object invoke(Object arg1, Object arg2) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object invoke(Object arg) {
				int logType = Lua.luaL_checkinteger(L,1);
				String where = Lua.luaL_checkstring(L, 2).toString();
				String msg = Lua.luaL_checkstring(L, 3).toString();
				
				switch(logType)
				{
				case Log.VERBOSE: //2
				{
					Log.v(where, msg);
				}break;
				case Log.DEBUG: //3
				{
					Log.d(where, msg);
				}break;
				case Log.INFO: //4
				{
					Log.i(where, msg);
				}break;
				case Log.WARN: //5
				{
					Log.w(where, msg);
				}break;
				case Log.ERROR: //6
				{
					Log.e(where, msg);
				}break;
				case Log.ASSERT: //7
				{
					//Log.(where, msg);
				}break;
				default:
					break;
				};
				
				return 0;
			}
			
			@Override
			public Object invoke(Object[] args) {
				// TODO Auto-generated method stub
				return null;
			}
		});
				
		tasker.AddToQueue(LuaTranslator.class);
		
		tasker.AddToQueue(LuaContext.class);
		tasker.AddToQueue(LuaGraphics.class);
		
		tasker.AddToQueue(LuaEventArgs.class);
		tasker.AddToQueue(LuaViewInflator.class);
		tasker.AddToQueue(LGAbsListView.class);
		tasker.AddToQueue(LGAdapterView.class);
		tasker.AddToQueue(LGAutoCompleteTextView.class);
		tasker.AddToQueue(LGButton.class);
		tasker.AddToQueue(LGCheckBox.class);
		tasker.AddToQueue(LGComboBox.class);
		tasker.AddToQueue(LGCompoundButton.class);
		tasker.AddToQueue(LGDatePicker.class);
		tasker.AddToQueue(LGEditText.class);
		tasker.AddToQueue(LGFrameLayout.class);
		tasker.AddToQueue(LGLinearLayout.class);
		tasker.AddToQueue(LGListView.class);
		tasker.AddToQueue(LGMapView.class);
		tasker.AddToQueue(LGProgressBar.class);
		tasker.AddToQueue(LGRadioButton.class);
		tasker.AddToQueue(LGRadioGroup.class);
		//tasker.AddToQueue(LGRelativeLayout.class);
		tasker.AddToQueue(LGScrollView.class);
		//tasker.AddToQueue(LGTableLayout.class);
		//tasker.AddToQueue(LGTableRow.class);
		tasker.AddToQueue(LGTextView.class);
		tasker.AddToQueue(LGView.class);
		//tasker.AddToQueue(LGViewGroup.class);
		
		tasker.AddToQueue(LuaColor.class);
		tasker.AddToQueue(LuaDate.class);
		tasker.AddToQueue(LuaDefines.class);
		tasker.AddToQueue(LuaFragment.class);
		tasker.AddToQueue(LuaForm.class);
		tasker.AddToQueue(LuaMapCircle.class);
		tasker.AddToQueue(LuaMapImage.class);
		tasker.AddToQueue(LuaMapMarker.class);
		tasker.AddToQueue(LuaMapPolygon.class);
		tasker.AddToQueue(LuaMapPolyline.class);
		tasker.AddToQueue(LuaNativeObject.class);
		tasker.AddToQueue(LuaObjectStore.class);
		tasker.AddToQueue(LuaNativeCall.class);
		tasker.AddToQueue(LuaDatabase.class);
		tasker.AddToQueue(LuaHttpClient.class);
		tasker.AddToQueue(LuaJSONObject.class);
		tasker.AddToQueue(LuaJSONArray.class);
		tasker.AddToQueue(LuaDialog.class);
		tasker.AddToQueue(LuaPoint.class);
		tasker.AddToQueue(LuaRect.class);
		tasker.AddToQueue(LuaResource.class);
		tasker.AddToQueue(LuaTabForm.class);
		tasker.AddToQueue(LuaToast.class);
		tasker.AddToQueue(LuaStore.class);
		tasker.AddToQueue(LuaStream.class);
		
		for(Class<?> cls : plugins)
			tasker.AddToQueue(cls);
		
		if(taskerThread == null && Lunar.methodMap.size() == 0)
		{
			taskerThread = new Thread(tasker);
			taskerThread.start();
			
			while(tasker.HasJob())
			{
				try
				{
					Thread.sleep(10);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			tasker.Exit();
			try
			{
				taskerThread.join();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			taskerThread = null;
		}
		/*luaGlobalFunctions::Register(lu);*/
		
		Lunar.Register(L, LuaTranslator.class, false);
		
		Lunar.Register(L, LuaContext.class, false);
		Lunar.Register(L, LuaGraphics.class, false);
		
		Lunar.Register(L, LuaEventArgs.class, false);
		Lunar.Register(L, LuaViewInflator.class, false);
		Lunar.Register(L, LGAbsListView.class, false);
		Lunar.Register(L, LGAdapterView.class, false);
		Lunar.Register(L, LGAutoCompleteTextView.class, false);
		Lunar.Register(L, LGButton.class, false);
		Lunar.Register(L, LGCheckBox.class, false);
		Lunar.Register(L, LGComboBox.class, false);
		Lunar.Register(L, LGCompoundButton.class, false);
		Lunar.Register(L, LGDatePicker.class, false);
		Lunar.Register(L, LGEditText.class, false);
		Lunar.Register(L, LGFrameLayout.class, false);
		Lunar.Register(L, LGLinearLayout.class, false);
		Lunar.Register(L, LGListView.class, false);
		Lunar.Register(L, LGMapView.class, false);
		Lunar.Register(L, LGProgressBar.class, false);
		Lunar.Register(L, LGRadioButton.class, false);
		Lunar.Register(L, LGRadioGroup.class, false);
		//Lunar.Register(L, LGRelativeLayout.class, false);
		Lunar.Register(L, LGScrollView.class, false);
		//Lunar.Register(L, LGTableLayout.class, false);
		//Lunar.Register(L, LGTableRow.class, false);
		Lunar.Register(L, LGTextView.class, false);
		Lunar.Register(L, LGView.class, false);
		//Lunar.Register(L, LGViewGroup.class, false);
		
		Lunar.Register(L, LuaColor.class, false);
		Lunar.Register(L, LuaDate.class, false);
		Lunar.Register(L, LuaDefines.class, false);
		Lunar.Register(L, LuaFragment.class, false);
		Lunar.Register(L, LuaForm.class, false);
		Lunar.Register(L, LuaMapCircle.class, false);
		Lunar.Register(L, LuaMapImage.class, false);
		Lunar.Register(L, LuaMapMarker.class, false);
		Lunar.Register(L, LuaMapPolygon.class, false);
		Lunar.Register(L, LuaMapPolyline.class, false);
		Lunar.Register(L, LuaNativeObject.class, false);
		Lunar.Register(L, LuaObjectStore.class, false);
		Lunar.Register(L, LuaNativeCall.class, false);
		Lunar.Register(L, LuaDatabase.class, false);
		Lunar.Register(L, LuaHttpClient.class, false);
		Lunar.Register(L, LuaJSONObject.class, false);
		Lunar.Register(L, LuaJSONArray.class, false);
		Lunar.Register(L, LuaDialog.class, false);
		Lunar.Register(L, LuaPoint.class, false);
		Lunar.Register(L, LuaRect.class, false);
		Lunar.Register(L, LuaResource.class, false);
		Lunar.Register(L, LuaTabForm.class, false);
		Lunar.Register(L, LuaToast.class, false);
		Lunar.Register(L, LuaStore.class, false);
		Lunar.Register(L, LuaStream.class, false);
		
		for(Class<?> cls : plugins)
			Lunar.Register(L, cls, false);
		
		//set the suspendluathread a coroutine function
		/*lua_getglobal(lu,"coroutine");
		if(lua_istable(lu,-1) )
		{
			lua_pushcfunction(lu,SuspendLuaThread);
			lua_setfield(lu,-2,"wait");
			lua_pushcfunction(lu,SuspendLuaThread);
			lua_setfield(lu,-2,"WAIT");
		}
		lua_pop(lu,1);*/
	}
	
	void RegisterGlobals()
	{
		//GUIEvents
		int count = 0;
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_ZERO");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_CREATE");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_RESUME");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_PAUSE");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_DESTROY");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_UPDATE");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_PAINT");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_MOUSEDOWN");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_MOUSEUP");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_MOUSEMOVE");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_ADAPTERVIEW");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_EVENT");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_KEYDOWN");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_KEYUP");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_NFC");
		Lua.lua_pushinteger(L, count++);
		Lua.lua_setglobal(L, "GUIEVENT_COUNT");
		
		Lua.lua_pushstring(L, "Android");
		Lua.lua_setglobal(L, "OS_TYPE");
		
		Lua.lua_pushinteger(L, Build.VERSION.SDK_INT);
		Lua.lua_setglobal(L, "OS_VERSION");
		
		//TODO:this
		/*String s = GetContext().getResources().getString(R.string.deviceType);
		com.dk.scriptingengine.luagui.DisplayMetrics.isTablet = (s.compareTo("Tablet") == 0);*/
		Lua.lua_pushboolean(L, com.dk.scriptingengine.luagui.DisplayMetrics.isTablet);
		Lua.lua_setglobal(L, "IS_TABLET");
	}
	
	int RegisterGuiEvent(lua_State L)
	{
		String entry = Lua.luaL_checkstring(L, 1).toString();
		int ev = Lua.luaL_checkinteger(L, 2);
		String str = Lua.luaL_checkstring(L, 3).toString();
		
		if(entry.compareTo("") == 0 || ev == 0 || str == null || str.compareTo("") == 0)
			return 0;
		
		int top = Lua.lua_gettop(L);
		String sFuncName = new String(str);
		String copy = new String(str);
		StringTokenizer st = new StringTokenizer(copy, ".:");
		boolean colon = false;
		if(copy.indexOf(".:") == -1)
		{
			Lua.lua_getglobal(L, str);
			if(Lua.lua_isfunction(L, -1) && !Lua.lua_iscfunction(L, -1))
				RegisterEvent(REGTYPE_GUI, entry, ev, str);
			else
			{
				Log.e("LuaEngine", "RegisterPlayerEvent failed! " + str + " is not a valid Lua function.");
			}
		}
		else
		{
			Lua.lua_getglobal(L, "_G"); //start out with the global table.
			String token = st.nextToken();
			while(st.hasMoreTokens())
			{
				Lua.lua_getfield(L, -1, token);
				if(Lua.lua_isfunction(L, -1) && !Lua.lua_iscfunction(L, -1))
				{
					RegisterEvent(REGTYPE_GUI, entry, ev, str);
					break;
				}
				else if(Lua.lua_istable(L, -1))
				{
					token = st.nextToken();
					continue;
				}
				else
				{
					Log.e("LuaEngine", "RegisterGuiEvent failed! " + str + " is not a valid Lua function.");
					break;
				}
			}
		}
		Lua.lua_settop(L, top);
		return 0;
	}
	
	int SuspendLuaThread(lua_State L) 
	{
		lua_State thread = (Lua.lua_isthread(L, -1)) ? Lua.lua_tothread(L, 1) : null;
		if(thread == null)
		{
			return Log.e("LuaEngine", "SuspendLuaThread expected Lua coroutine, got NULLuaAPI.");
		}
		int waittime = Lua.luaL_checkinteger(L, 2);
		if(waittime <= 0)
		{
			return Log.e("LuaEngine","SuspendLuaThread expected timer > 0 instead got " + waittime);
		}
		
		Lua.lua_pushvalue(L, 1);
		int ref = Lua.luaL_ref(L, Lua.LUA_REGISTRYINDEX);
		/*
		 * #define LUA_NOREF       (-2)
		 * #define LUA_REFNIL      (-1)
		 */
		if(ref == -1 || ref == -2)
			return Log.e("LuaEngine", "Error in SuspendLuaThread! Failed to create a valid reference.");
		
		Lua.lua_remove(L, 1); // remove thread object
		Lua.lua_remove(L, 1); // remove timer.
		//All that remains now are the extra arguments passed to this function
		Lua.lua_xmove(L, thread, Lua.lua_gettop(L));
		pendingThreads.add(L);
		return Lua.lua_yield(L, Lua.lua_gettop(L));
	}
	
	void RegisterEvent(int regtype, String id, int evt, String func) 
	{
		if(func != null && func != "" && evt > 0) 
		{
			switch(regtype) 
			{
			case REGTYPE_GUI:
				{
					if(id != "" && evt < GuiEvents.GUI_EVENT_COUNT.ordinal()) 
					{
						if(!guiBinding.containsKey(id))
						{
							LuaGuiBinding bind = new LuaGuiBinding();
							bind.Functions.put(GuiEvents.class.getEnumConstants()[evt], func);
							guiBinding.put(id, bind);
						}
						else
						{
							LuaGuiBinding bind = guiBinding.get(id);
							GuiEvents evtid = GuiEvents.class.getEnumConstants()[evt];
							if(bind.Functions.containsKey(evtid))
								bind.Functions.remove(evtid);
							bind.Functions.put(evtid, new String(func));
						}
					}
				}break;
			default:
				break;
			};
		}
	}
	
	public void Unload()
	{
		Lua.lua_close(L);
		guiBinding.clear();
	}
	
	public void Restart(String root)
	{
		Unload();
		Startup(root);
	}
	
	public void Restart(Context context, AssetManager assetManager, String root)
	{
		Unload();
		Startup(context, assetManager, root);
	}
	
	public lua_State GetLuaState() { return L; }
	
	public void PushBool(boolean value) { Lua.lua_pushboolean(L, value); }
	public void PushNIL() { Lua.lua_pushnil(L); }
	public void PushInt(int val) { Lua.lua_pushinteger(L, val); }
	void PushLong(long val) { Lua.lua_pushnumber(L, (double)val); }
	public void PushFloat(float val) { Lua.lua_pushnumber(L, val); }
	public void PushDouble(double val) { Lua.lua_pushnumber(L, val); }
	public void PushString(String val) { Lua.lua_pushstring(L, val); }

	public void PushTable(HashMap<Object, Object> retVal)
	{
		Lua.lua_lock(L);
		boolean created = false;
		if(!created)
    	{
    		created = true;
    		Lua.lua_createtable(L, 0, retVal.size());
    	}
		for(Map.Entry<Object, Object> entry : retVal.entrySet())
		{
			Object retval = entry.getValue();
			String retName = retval.getClass().getName();
        	if(retName.compareTo("java.lang.Boolean") == 0)
        		((LuaEngine)LuaEngine.getInstance()).PushBool((Boolean)retval);
        	else if(retName.compareTo("java.lang.Byte") == 0
        		|| retName.compareTo("java.lang.Short") == 0
        		|| retName.compareTo("java.lang.Integer") == 0
        		|| retName.compareTo("java.lang.Long") == 0)
        		((LuaEngine)LuaEngine.getInstance()).PushInt((Integer)retval);
        	else if(retName.compareTo("java.lang.Float") == 0)
        		((LuaEngine)LuaEngine.getInstance()).PushFloat((Float)retval);
        	else if(retName.compareTo("java.lang.Double") == 0)
        		((LuaEngine)LuaEngine.getInstance()).PushDouble((Double)retval);
        	else if(retName.compareTo("java.lang.Char") == 0
        		|| retName.compareTo("java.lang.String") == 0)
        		((LuaEngine)LuaEngine.getInstance()).PushString((String)retval);
        	else if(retName.compareTo("java.lang.Void") == 0)
        		((LuaEngine)LuaEngine.getInstance()).PushInt(0);
        	else if(retName.compareTo("java.util.HashMap") == 0)
        	{
        		((LuaEngine)LuaEngine.getInstance()).PushTable((HashMap<Object, Object>)retval);
        	}
        	else
        	{
            	LuaEngine l = (LuaEngine)LuaEngine.getInstance();
            	Lunar.push(l.GetLuaState(), retval, false);
        	}
        	
			Lua.lua_setfield(L, -2, String.valueOf(entry.getKey()));
        	//Lua.lua_pushstring(L, String.valueOf(entry.getKey()));
        	 /*
             * To put values into the table, we first push the index, then the
             * value, and then call lua_rawset() with the index of the table in the
             * stack. Let's see why it's -3: In Lua, the value -1 always refers to
             * the top of the stack. When you create the table with lua_newtable(),
             * the table gets pushed into the top of the stack. When you push the
             * index and then the cell value, the stack looks like:
             *
             * <- [stack bottom] -- table, index, value [top]
             *
             * So the -1 will refer to the cell value, thus -3 is used to refer to
             * the table itself. Note that lua_rawset() pops the two last elements
             * of the stack, so that after it has been called, the table is at the
             * top of the stack.
             */
        	//Lua.lua_rawset(L, -3);
		}
		//Lua.sethvalue(L, obj, x)
		//Lua.luaH_new(L, narray, nhash)
		//Lua.setbvalue(L.top, (b != 0) ? 1 : 0); // ensure that true is 1
		//Lua.api_incr_top(L);
		Lua.lua_unlock(L);
		
	}
	
	/*Custom variables*/
	public UrlHttpClient GetHttpClient(String id)
	{
		if(httpClientMap.containsKey(id))
			return httpClientMap.get(id);
		else
		{
			UrlHttpClient client = new UrlHttpClient();
			httpClientMap.put(id, client);
			return client;
		}		
	}
	
	public void DestroyHttpClient(Integer id)
	{
		if(httpClientMap.containsKey(id))
		{
			@SuppressWarnings("unused")
			UrlHttpClient client = httpClientMap.get(id);
			httpClientMap.remove(id);
			client = null;
		}
	}
	
	public Context GetContext()
	{
		return androidContext;
	}
	
	public void SetContext(Context context)
	{
		androidContext = context;
	}
	
	public String GetScriptsRoot()
	{
		return scriptsRoot;
	}
	
	public int GetPrimaryLoad()
	{
		return primaryLoad;
	}
	
	public String GetUIRoot()
	{
		return uiRoot;
	}
	
	public String GetMainUI()
	{
		return mainUI;
	}
	
	public String GetMainForm()
	{
		return mainForm;
	}

	public boolean CatchExceptions()
	{
		return true;
	}
	
	public boolean ThrowExceptions()
	{
		return true;
	}
}
