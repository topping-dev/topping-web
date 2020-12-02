package com.dk.scriptingengine.osspecific;

import com.luajava.Lua.CharPtr;

public abstract class AsyncResult
{
	public abstract void Call(CharPtr data, int result);
}
