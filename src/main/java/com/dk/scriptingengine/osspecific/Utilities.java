package com.dk.scriptingengine.osspecific;

public class Utilities
{
    public static DynamicByteBuf MethodAction(String luaId, String action)
    {
        String id = luaId + action;

        return Utilities.MethodAction(id, luaId, action);
    }

    public static DynamicByteBuf MethodAction(String id, String luaId, String action)
    {
        DynamicByteBuf buf = DynamicByteBuf.create();
        buf.writeInt(LayoutServer.SMSG_REGISTER_EVENT);
        buf.writeInt(LayoutServer.EVENT_TYPE_METHOD);
        buf.writeInt(luaId.length());
        buf.writeString(luaId);
        buf.writeInt(id.length());
        buf.writeString(id);
        buf.writeInt(action.length());
        buf.writeString(action);

        return buf;
    }

    public static DynamicByteBuf WatchAction(String luaId, String action)
    {
        String id = luaId + action;

        return Utilities.MethodAction(id, luaId, action);
    }

    public static DynamicByteBuf WatchAction(String id, String luaId, String action)
    {
        DynamicByteBuf buf = DynamicByteBuf.create();
        buf.writeInt(LayoutServer.SMSG_REGISTER_EVENT);
        buf.writeInt(LayoutServer.EVENT_TYPE_WATCH);
        buf.writeInt(luaId.length());
        buf.writeString(luaId);
        buf.writeInt(id.length());
        buf.writeString(id);
        buf.writeInt(action.length());
        buf.writeString(action);

        return buf;
    }
}
