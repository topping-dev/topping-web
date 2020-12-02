package com.dk.scriptingengine.backend;

import com.dk.scriptingengine.LuaTranslator;

public interface LuaInterface
{
	/**
	 * (Ignore)
	 */
    void RegisterEventFunction(String var, LuaTranslator lt);
    /**
	 * (Ignore)
	 */
    String GetId();
}
