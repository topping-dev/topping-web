package com.dk.scriptingengine.backend;

import com.luajava.Lua.lua_State;

public class LunarRegister
{
	public lua_State L;
	public Class<?> cls;
	public boolean loadAll;
	
	public LunarRegister(lua_State L, Class<?> cls, boolean loadAll)
	{
		this.L = L;
		this.cls = cls;
		this.loadAll = loadAll;
	}
}