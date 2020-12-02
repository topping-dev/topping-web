package com.dk.scriptingengine.osspecific;

import android.widget.LGAbsListView;
import android.widget.LGListView;
import android.widget.LGView;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaForm;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.luagui.LuaViewInflator;
import com.dk.scriptingengine.osspecific.socketobjects.CMSGClientData;
import com.dk.scriptingengine.osspecific.socketobjects.MethodModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LayoutServer extends WebSocketServer
{
    public final static int SMSG_SERVER_WELCOME = 0;
    public final static int CMSG_CLIENT_DATA = 1;
    public final static int SMSG_SESSION = 2;
    public final static int CMSG_SESSION_ACCEPTED = 3;
    public final static int SMSG_DATA_CHANGED = 4;
    public final static int SMSG_REGISTER_EVENT = 5;
    public final static int CMSG_GUI_EVENT = 6;
    public final static int CMSG_VALUE_CHANGE = 7;
    public final static int SMSG_TOAST = 8;
    public final static int SMSG_VIEW_FOR_INDEX = 9;
    public final static int CMSG_VIEW_FOR_INDEX = 10;
    public final static int SMSG_CHANGE_PAGE = 11;
    public final static int SMSG_DIALOG = 12;
    public final static int SMSG_LIST_REFRESH = 13;

    public final static int TYPE_NULL = 0;
    public final static int TYPE_INTEGER = 1;
    public final static int TYPE_FLOAT = 2;
    public final static int TYPE_STRING = 3;
    public final static int TYPE_BOOLEAN = 4;
    public final static int TYPE_OBJECT = 5;

    public final static int EVENT_TYPE_METHOD = 0;
    public final static int EVENT_TYPE_WATCH = 1;

    private ObjectMapper objectMapper;

    private HashMap<String, CMSGClientData> connectedClients = new HashMap<>();
    private HashMap<String, LuaForm> activeWindows = new HashMap<>();
    private HashMap<String, WebSocket> activeConnections = new HashMap<>();
    private HashMap<String, LuaEventInterface> activeGlobals = new HashMap<>();

    private static LayoutServer sInstance;
    public static LayoutServer getInstance()
    {
        return sInstance;
    }

    public LayoutServer(InetSocketAddress inetSocketAddress)
    {
        super(inetSocketAddress);
        sInstance = this;
        objectMapper = new ObjectMapper();
        LGParser.GetInstance().Initialize();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        /*String uniqueID = UUID.randomUUID().toString();
        connectedClients.put(uniqueID, null);*/

        String uniqueID = conn.getRemoteSocketAddress().toString();
        connectedClients.put(uniqueID, null);

        activeConnections.put(conn.getRemoteSocketAddress().toString(), conn);

        DynamicByteBuf dbb = DynamicByteBuf.create();
        dbb.writeInt(SMSG_SERVER_WELCOME);
        dbb.writeInt(uniqueID.length());
        dbb.writeString(uniqueID);
        conn.send(dbb.toArray());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        connectedClients.remove(conn.getRemoteSocketAddress().toString());
        activeWindows.remove(conn.getRemoteSocketAddress().toString());
        activeConnections.remove(conn.getRemoteSocketAddress().toString());
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        Log.e("aaa", "aaa");
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message)
    {
        super.onMessage(conn, message);

        CMSGClientData data = connectedClients.get(conn.getRemoteSocketAddress().toString());

        if(!message.hasRemaining())
            return;

        int opcode = message.getInt();
        switch (opcode)
        {
            case CMSG_CLIENT_DATA:
            {
                try
                {
                    int payloadLength = message.getInt();
                    byte[] buffer = new byte[payloadLength];
                    message.get(buffer, 0, payloadLength);
                    String payload = new String(buffer);
                    CMSGClientData cmsgClientData = objectMapper.readValue(payload, CMSGClientData.class);
                    Log.d("LayoutServer", cmsgClientData.getName());

                    connectedClients.put(conn.getRemoteSocketAddress().toString(), cmsgClientData);

                    String initPage = getPageFromUri(cmsgClientData.getHref());
                    String luaId = LuaEngine.getInstance().GetMainForm();
                    String initUI = initPage;
                    if(initPage.compareTo("") == 0)
                    {
                        initUI = LuaEngine.getInstance().GetMainUI();
                    }
                    else
                        initUI = initPage + ".xml";

                    Context ctx = new Context(conn.getRemoteSocketAddress().toString());
                    LuaViewInflator inflater = new LuaViewInflator(LuaContext.CreateLuaContext(ctx));
                    LGView v = inflater.ParseFile(initUI, null);
                    LuaForm lf = new LuaForm(ctx, luaId);
                    LuaForm.SetActiveForm(lf);
                    lf.SetView(v);

                    String s = lf.toWeb();
                    Log.d("html", s);

                    String jsCore = lf.toJsCoreModel();
                    Log.d("jsCore", jsCore);

                    String js = lf.toJs();
                    js = "{" + js + "}";
                    Log.d("js", js);

                    String watch = lf.toWatch();
                    watch = "{\"watch\": [" + watch + "]}";
                    Log.d("watch", watch);

                    String method = lf.toMethod();
                    method = "{\"method\": [" + method + "]}";
                    Log.d("method", method);

                    activeWindows.put(conn.getRemoteSocketAddress().toString(), lf);

                    DynamicByteBuf dbb = DynamicByteBuf.create();
                    dbb.writeInt(SMSG_SESSION);
                    dbb.writeInt(s.length());
                    dbb.writeString(s);
                    dbb.writeInt(jsCore.length());
                    dbb.writeString(jsCore);
                    dbb.writeInt(js.length());
                    dbb.writeString(js);
                    dbb.writeInt(watch.length());
                    dbb.writeString(watch);
                    dbb.writeInt(method.length());
                    dbb.writeString(method);
                    conn.send(dbb.toArray());
                }
                catch (IOException e)
                {
                    Log.e("LayoutServer.java", e.toString());
                }
            }break;
            case CMSG_SESSION_ACCEPTED:
            {
                LuaForm lf = activeWindows.get(conn.getRemoteSocketAddress().toString());
                lf.AfterSetup();
            }break;
            case CMSG_GUI_EVENT:
            {
                int idLength = message.getInt();
                String id = readString(message, idLength);
                int idWatchLength = message.getInt();
                String idWatch = readString(message, idWatchLength);
                int eventLength = message.getInt();
                String event = readString(message, eventLength);

                ArrayList<Object> vals = new ArrayList<>();
                Object val = readValue(message);
                while(val != null)
                {
                    vals.add(val);
                    val = readValue(message);
                }

                Object[] valArr = null;
                if(vals.size() > 0)
                {
                    valArr = new Object[vals.size()];
                    valArr = vals.toArray(valArr);
                }

                LuaForm lf = activeWindows.get(conn.getRemoteSocketAddress().toString());
                LGView v = null;
                if(id != null && !id.equals(""))
                    v = lf.GetViewById(id);
                if(v != null)
                {
                    v.CallEventValue(idWatch, event, valArr);
                }
                else
                {
                    for (Map.Entry<String, LuaEventInterface> entry : activeGlobals.entrySet())
                    {
                        entry.getValue().CallEventFunction(idWatch, event, valArr);
                    }
                }
            }break;
            case CMSG_VALUE_CHANGE:
            {
                int idLength = message.getInt();
                String id = readString(message, idLength);
                int idWatchLength = message.getInt();
                String idWatch = readString(message, idWatchLength);

                ArrayList<Object> vals = new ArrayList<>();
                Object val = readValue(message);
                while(val != null)
                {
                    vals.add(val);
                    val = readValue(message);
                }

                Object[] valArr = null;
                if(vals.size() > 0)
                {
                    valArr = new Object[vals.size()];
                    valArr = vals.toArray(valArr);
                }

                LuaForm lf = activeWindows.get(conn.getRemoteSocketAddress().toString());
                LGView v = lf.GetViewById(id);
                if(v != null)
                {
                    v.CallEventValue(idWatch, LGView.SET_VALUE, valArr);
                }
                else
                {
                    for (Map.Entry<String, LuaEventInterface> entry : activeGlobals.entrySet())
                    {
                        entry.getValue().CallEventFunction(idWatch, LGView.SET_VALUE, valArr);
                    }
                }
            }break;
            case CMSG_VIEW_FOR_INDEX:
            {
                int idLength = message.getInt();
                String id = readString(message, idLength);
                int itemNameLength = message.getInt();
                String itemName = readString(message, itemNameLength);
                int index = message.getInt();

                LuaForm lf = activeWindows.get(conn.getRemoteSocketAddress().toString());
                LGView v = lf.GetViewById(id);
                if(v != null && v instanceof LGAbsListView)
                {
                    LGView vItem = ((LGAbsListView)v).viewForIndex(index, itemName);
                    if(vItem == null)
                        return;

                    String s = vItem.toWeb();
                    Log.d("html", s);

                    String jsCore = vItem.toJsCoreModel();
                    Log.d("jsCore", jsCore);

                    String js = vItem.toJs();
                    js = "{" + js + "}";
                    Log.d("js", js);

                    String watch = vItem.toWatch();
                    watch = "{\"watch\": [" + watch + "]}";
                    Log.d("watch", watch);

                    String method = vItem.toMethod();
                    method = "{\"method\": [" + method + "]}";
                    Log.d("method", method);

                    DynamicByteBuf dbb = DynamicByteBuf.create();
                    dbb.writeInt(SMSG_VIEW_FOR_INDEX);
                    dbb.writeInt(idLength);
                    dbb.writeString(id);
                    dbb.writeInt(itemNameLength);
                    dbb.writeString(itemName);
                    dbb.writeInt(index);
                    dbb.writeInt(s.length());
                    dbb.writeString(s);
                    dbb.writeInt(jsCore.length());
                    dbb.writeString(jsCore);
                    dbb.writeInt(js.length());
                    dbb.writeString(js);
                    dbb.writeInt(watch.length());
                    dbb.writeString(watch);
                    dbb.writeInt(method.length());
                    dbb.writeString(method);
                    conn.send(dbb.toArray());
                }
            }break;
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        ex.printStackTrace();
        connectedClients.remove(conn.getRemoteSocketAddress().toString());
        activeWindows.remove(conn.getRemoteSocketAddress().toString());
        activeConnections.remove(conn.getRemoteSocketAddress().toString());
    }

    @Override
    public void onStart()
    {

    }

    public static Object readValue (ByteBuffer buf)
    {
        if(!buf.hasRemaining())
            return null;
        int valType = buf.getInt();
        if(valType == TYPE_NULL)
            return null;
        if(valType == TYPE_INTEGER)
            return buf.getInt();
        else if(valType == TYPE_FLOAT)
            return buf.getFloat();
        else if(valType == TYPE_STRING)
        {
            int len = buf.getInt();
            return readString(buf, len);
        }
        else if(valType == TYPE_BOOLEAN)
            return buf.getInt() == 1;
        else if(valType == TYPE_OBJECT)
        {
            int len = buf.getInt();
            return readString(buf, len);
        }

        return null;
    }

    public static void writeValue(DynamicByteBuf buf, int valType, Object val)
    {
        buf.writeInt(valType);
        if(valType == TYPE_NULL)
            return;
        if(valType == TYPE_INTEGER)
            buf.writeInt((int) val);
        else if(valType == TYPE_FLOAT)
            buf.writeFloat((float) val);
        else if(valType == TYPE_STRING)
        {
            String valS = (String) val;

            buf.writeInt(valS.length());
            buf.writeString(valS);
        }
        else if(valType == TYPE_BOOLEAN)
        {
            buf.writeInt(((boolean)val) ? 1 : 0);
        }
        else if(valType == TYPE_OBJECT)
        {
            ObjectMapper om = new ObjectMapper();
            String valS = "";
            try
            {
                valS = om.writeValueAsString(val);
            }
            catch (JsonProcessingException e)
            {
                e.printStackTrace();
            }

            buf.writeInt(valS.length());
            buf.writeString(valS);
        }
    }

    public static String readString(ByteBuffer buf, int len)
    {
        byte[] buffer = new byte[len];
        buf.get(buffer, 0, len);
        return new String(buffer);
    }

    public void sendPacket(String client, DynamicByteBuf buf)
    {
        WebSocket conn = activeConnections.get(client);
        if(conn != null)
            conn.send(buf.toArray());
        else
            Log.e("LayoutServer.java", "Cannot send message to client:" + client);
    }

    private String getPageFromUri(String uriString)
    {
        try
        {
            URI uri = new URI(uriString);
            Log.d("LayoutServer.java", uri.getQuery());

            String page = "";
            String[] split = uri.getQuery().split("&");
            for(String s : split)
            {
                String[] splitIn = s.split("=");
                if(splitIn[0].equals("page"))
                    return splitIn[1];
            }
        }
        catch (Exception e)
        {
        }
        return "";
    }

    public void notifyDataChanged(String client, String id, int type, Object val)
    {
        DynamicByteBuf buf = DynamicByteBuf.create();
        buf.writeInt(LayoutServer.SMSG_DATA_CHANGED);

        buf.writeInt(1);
        buf.writeInt(id.length());
        buf.writeString(id);
        LayoutServer.writeValue(buf, type, val);

        LayoutServer.getInstance().sendPacket(client, buf);
    }

    public void notifyDataChanged(String client, ArrayList<String> ids, ArrayList<Integer> types, ArrayList<Object> vals)
    {
        notifyDataChanged(client, ids.toArray(new String[ids.size()]), types.toArray(new Integer[types.size()]), vals.toArray(new Object[vals.size()]));
    }

    public void notifyDataChanged(String client, String[] ids, Integer[] types, Object[] vals)
    {
        DynamicByteBuf buf = DynamicByteBuf.create();
        buf.writeInt(LayoutServer.SMSG_DATA_CHANGED);

        buf.writeInt(ids.length);

        for(int i = 0; i < ids.length; i++)
        {
            buf.writeInt(ids[i].length());
            buf.writeString(ids[i]);
            LayoutServer.writeValue(buf, types[i], vals[i]);
        }

        LayoutServer.getInstance().sendPacket(client, buf);
    }

    public void notifyListRefresh(String client, String id)
    {
        DynamicByteBuf buf = DynamicByteBuf.create();
        buf.writeInt(LayoutServer.SMSG_LIST_REFRESH);

        buf.writeInt(id.length());
        buf.writeString(id);

        LayoutServer.getInstance().sendPacket(client, buf);
    }

    public void changePage(String client, String id, String ui)
    {
        DynamicByteBuf buf = DynamicByteBuf.create();
        buf.writeInt(LayoutServer.SMSG_CHANGE_PAGE);

        writeValue(buf, TYPE_STRING, id);
        if(ui != null)
            writeValue(buf, TYPE_STRING, ui);
        LayoutServer.getInstance().sendPacket(client, buf);
    }

    public void addActiveGlobal(LuaEventInterface li)
    {
        activeGlobals.put(li.GetId(), li);
    }

    public void removeActiveGlobal(LuaEventInterface li)
    {
        activeGlobals.remove(li.GetId());
    }

    public void removeActiveGlobal(String id)
    {
        activeGlobals.remove(id);
    }
}
