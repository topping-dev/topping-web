package com.dk.scriptingengine.backend;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.osspecific.Context;

public abstract class LuaLoadHandler implements Runnable
{
	private final Context ctx;
	private Thread t;

	public LuaLoadHandler(Context applicationContext)
	{
		this.ctx = applicationContext;
		t = new Thread(this);
	}

	public void start()
	{
		t.start();
	}

	public void interrupt()
	{
		t.interrupt();
	}

	@Override
	public void run()
	{
		LuaEngine.getInstance().Startup(ctx);
		OnFinished();
	}
	
	public abstract void OnFinished();
}
