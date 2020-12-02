-- Root folder of scripts
ScriptsRoot = "scripts";

-- 1 External SD
-- 2 Internal
-- 3 Assets
PrimaryLoad = 3;

-- Force load of external or internal scripts
ForceLoad = 1;

-- Root folder of user interface files
UIRoot = "ui";

-- Startup XML 
-- Leaving this "", will create empty view for tab system
-- if you want to use tab system, overload the create event of the
-- MainForm(LuaForm) and add tab using LuaTabForm.
-- MainUI = "main.xml"
MainUI = "testbed.xml";

-- Startup Form
MainForm = "Main";

-- Debug
LuaDebug = 0;

--if LuaDebug is 0 lines after this line will not work

--Debugger configuration 
--Default 8192
--SocketBufferSize = 524288

--Default 1024
--LuaBufferSize = 1024

--Default 8192
--PBufferSize = 65536

--initconnection = require "debugger"
--initconnection("192.168.56.1", "10000", "luaidekey")
--initconnection("192.168.1.25", "10000", "luaidekey")

--web lua port
WebPort = 1723

--web socket port
WebSocketPort = 1724

