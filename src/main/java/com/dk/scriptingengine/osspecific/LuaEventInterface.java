package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.backend.LuaInterface;

public interface LuaEventInterface extends LuaInterface
{
    /**
     * (Ignore)
     */
    void CallEventFunction(String id, String event, Object... vals);
}
