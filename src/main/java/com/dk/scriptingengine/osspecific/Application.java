package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.backend.LuaLoadHandler;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.luagui.LuaViewInflator;

import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletResponse;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.DOMBuilder;
import jodd.lagarto.dom.LagartoDOMBuilder;

import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.ipAddress;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

public class Application
{
    public static final int LAYOUT_DIRECTION_RTL = 0;
    private static LuaContext applicationLuaContext;
    private static Context applicationContext;
    public static final LagartoDOMBuilder DOM_BUILDER = new LagartoDOMBuilder();
    /*static
    {
        DOM_BUILDER.getConfig().setCaseSensitive(true);
    }*/

    public static void main(String[] args)
    {
        final LuaEngine luaEngine = LuaEngine.getInstance();

        applicationContext = new Context("");
        applicationLuaContext = LuaContext.CreateLuaContext(applicationContext);
        LuaLoadHandler handler = new LuaLoadHandler(applicationContext)
        {
            @Override
            public void OnFinished()
            {
                port(LuaEngine.getInstance().webPort);
                staticFiles.externalLocation(System.getProperty("user.dir") + "/assets");
                get("/:file", (req, res) ->
                {
                    byte[] bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/web/" + req.params(":file")));
                    HttpServletResponse raw = res.raw();

                    raw.getOutputStream().write(bytes);
                    raw.getOutputStream().flush();
                    raw.getOutputStream().close();

                    return res.raw();
                });
                //get("/hello", (req, res) -> "Hello World");

                String host = "0.0.0.0";
                int port = LuaEngine.getInstance().webSocketPort;
                WebSocketServer server = new LayoutServer(new InetSocketAddress(host, port));
                server.run();
            }
        };
        handler.start();
    }

    public static LuaContext GetApplicationContext()
    {
        return applicationLuaContext;
    }
}
