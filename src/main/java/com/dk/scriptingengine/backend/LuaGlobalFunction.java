package com.dk.scriptingengine.backend;

import com.luajava.Lua.lua_State;

public interface LuaGlobalFunction
{
	/**
	 * (Ignore)
	 */
	public int Lua_Index(lua_State L);
	/**
	 * (Ignore)
	 */
	public int Lua_NewIndex(lua_State L);
	/**
	 * (Ignore)
	 */
	public int Lua_GC(lua_State L);
	/**
	 * (Ignore)
	 */
	public int Lua_ToString(lua_State L);
}