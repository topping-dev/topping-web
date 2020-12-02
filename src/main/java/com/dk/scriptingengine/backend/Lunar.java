package com.dk.scriptingengine.backend;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import com.dk.scriptingengine.osspecific.Log;
import com.luajava.Lua;
import com.luajava.Lua.LuaException;
import com.luajava.Tools;
import com.luajava.Lua.lua_State;
import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaJavaFunction;
import com.dk.scriptingengine.LuaNativeObject;
import com.lordjoe.csharp.IDelegate;

public class Lunar
{
    private Lunar()
    {
        
    }
    
    public static String RemoveChar(String from, char w)
    {
        char[] arr = from.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : arr)
        {
            if (c != w)
                sb.append(c);
        }
        return sb.toString();
    }
    
    public static byte[] sizeof(Object obj)
    {
    	try
    	{
    	    ByteArrayOutputStream byteObject = new ByteArrayOutputStream();
    	    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteObject);
    	    objectOutputStream.writeObject(obj);
    	    objectOutputStream.flush();
    	    objectOutputStream.close();
    	    byteObject.close();
    	
    	    return byteObject.toByteArray();
    	}
    	catch(Exception e)
    	{
    		return new byte[0];
    	}

    }

    static IDelegate tostring_T = Lua.lua_CFunction.build(Lunar.class, "tostring_T");
    static IDelegate gc_T = Lua.lua_CFunction.build(Lunar.class, "gc_T");
    static IDelegate thunk = Lua.lua_CFunction.build(Lunar.class, "thunk");
    static IDelegate Sthunk = Lua.lua_CFunction.build(Lunar.class, "Sthunk");
    public static HashMap<Class<?>, Method[]> methodMap = new HashMap<Class<?>, Method[]>();
    public static void Register(lua_State L, Class<?> cls, boolean loadAll)
    {
        //String name = RemoveChar(cls.getName(), '.');
    	
    	LuaClass lc = cls.getAnnotation(LuaClass.class);
    	String name = "";
    	if(lc == null)
    	{
    		Log.e("LuaEngine", "No class annotation on " + cls.getName());
    		String[] arr = cls.getName().split("\\.");
    		name = arr[arr.length - 1];
    		//return;
    	}
    	else
    		name = lc.className();

	    Lua.lua_newtable(L);
	    int methods = Lua.lua_gettop(L);

        Lua.luaL_newmetatable(L, name);
	    int metatable = Lua.lua_gettop(L);
		
	    Lua.luaL_newmetatable(L, "DO NOT TRASH");
	    Lua.lua_pop(L, 1);

	    // store method table in globals so that
	    // scripts can add functions written in Lua.
	    Lua.lua_pushvalue(L, methods);
        Lua.lua_setfield(L, Lua.LUA_GLOBALSINDEX, name);

	    // hide metatable from Lua getmetatable()
	    Lua.lua_pushvalue(L, methods);
	    Lua.lua_setfield(L, metatable, "__metatable");

	    Lua.lua_pushvalue(L, methods);
	    Lua.lua_setfield(L, metatable, "__index");

	    Lua.lua_pushcfunction(L, tostring_T);
	    Lua.lua_setfield(L, metatable, "__tostring");

	    Lua.lua_pushcfunction(L, gc_T);
	    Lua.lua_setfield(L, metatable, "__gc");

	    Lua.lua_newtable(L);                // mt for method table
	    Lua.lua_setmetatable(L, methods);

	    //Method [] methodInfos = cls.getMethods();
		Method[] methodInfos = methodMap.get(cls);
	    for(Method m : methodInfos)
	    {
	    	if(loadAll)
	    	{
                Lua.lua_pushstring(L, m.getName());
                //Lua.lua_pushlightuserdata(L, mi);
                //((LuaFunction)a).SetMethodInfo(mi);
                LuaJavaFunction lf = new LuaJavaFunction(false, cls, m.getName(), m.getParameterTypes(), m);
                Lua.lua_pushlightuserdata(L, lf);
                //Lua.lua_pushlightuserdata(L, (void*)l);
                if((m.getModifiers() & Modifier.STATIC) > 0)
                	Lua.lua_pushcclosure(L, Sthunk, 1);
                else
                	Lua.lua_pushcclosure(L, thunk, 1);
                Lua.lua_settable(L, methods);
	    	}
	    	else
	    	{
		    	LuaFunction lf = m.getAnnotation(LuaFunction.class);
		    	if(lf != null)
		    	{
	                Lua.lua_pushstring(L, m.getName());
	                //Lua.lua_pushlightuserdata(L, mi);
	                //((LuaFunction)a).SetMethodInfo(mi);
	                //Lua.lua_pushlightuserdata(L, lf); old one
	                Lua.lua_pushlightuserdata(L, new LuaJavaFunction(lf.manual(), lf.self(), lf.methodName(), lf.arguments(), m));
	                //Lua.lua_pushlightuserdata(L, (void*)l);
	                if((m.getModifiers() & Modifier.STATIC) > 0)
	                	Lua.lua_pushcclosure(L, Sthunk, 1);
	                else
	                	Lua.lua_pushcclosure(L, thunk, 1);
	                Lua.lua_settable(L, methods);
		    	}
	    	}
	    }
	    
	   LuaGlobalString lgs = cls.getAnnotation(LuaGlobalString.class);
	   if(lgs != null)
	   {
		   String[] keys = lgs.keys();
		   String[] vals = lgs.vals();
		   for(int i = 0; i < keys.length; i++)
		   {
			   Lua.lua_pushliteral(L, vals[i]);
			   Lua.lua_setglobal(L, keys[i]);
		   }
	   }
	   
	   LuaGlobalInt lgi = cls.getAnnotation(LuaGlobalInt.class);
	   if(lgi != null)
	   {
		   String[] keys = lgi.keys();
		   int[] vals = lgi.vals();
		   for(int i = 0; i < keys.length; i++)
		   {
			   Lua.lua_pushinteger(L, vals[i]);
			   Lua.lua_setglobal(L, keys[i]);
		   }
	   }
	   
	   LuaGlobalNumber lgn = cls.getAnnotation(LuaGlobalNumber.class);
	   if(lgn != null)
	   {
		   String[] keys = lgn.keys();
		   double[] vals = lgn.vals();
		   for(int i = 0; i < keys.length; i++)
		   {
			   Lua.lua_pushnumber(L, vals[i]);
			   Lua.lua_setglobal(L, keys[i]);
		   }
	   }
	   
	   Lua.lua_pop(L, 2);  // drop metatable and method table
	   
	   LuaGlobalManual lgm = cls.getAnnotation(LuaGlobalManual.class);
	   if(lgm != null)
	   {
		   String namea = lgm.name();
		   Lua.lua_newtable(L);
		   int methodsa = Lua.lua_gettop(L);

	       Lua.luaL_newmetatable(L, namea);
		   int metatablea = Lua.lua_gettop(L);
			
		   Lua.luaL_newmetatable(L, "DO NOT TRASH");
		   Lua.lua_pop(L, 1);
			
		   // store method table in globals so that
		   // scripts can add functions written in Lua.
		   Lua.lua_pushvalue(L, methodsa);
		   Lua.lua_setfield(L, Lua.LUA_GLOBALSINDEX, namea);
		   
		   // hide metatable from Lua getmetatable()
		   Lua.lua_pushvalue(L, methodsa);
		   Lua.lua_setfield(L, metatablea, "__metatable");
		   
		   Lua.lua_pushvalue(L, methodsa);
		   Lua.lua_setfield(L, metatablea, "__index");
		   
		   Lua.lua_pushcfunction(L, Lua.lua_CFunction.build(cls, "Lua_ToString"));
		   Lua.lua_setfield(L, metatablea, "__tostring");
		   
		   Lua.lua_pushcfunction(L, Lua.lua_CFunction.build(cls, "Lua_GC"));
		   Lua.lua_setfield(L, metatablea, "__gc");
		   		   
		   Lua.lua_newtable(L);                // mt for method table
		   int mt = Lua.lua_gettop(L);
		   
		   Lua.lua_pushcfunction(L, Lua.lua_CFunction.build(cls, "Lua_Index"));
		   Lua.lua_setfield(L, mt, "__index");
		   
		   Lua.lua_pushcfunction(L, Lua.lua_CFunction.build(cls, "Lua_NewIndex"));
		   Lua.lua_setfield(L, mt, "__newindex");
		   
		   Lua.lua_setmetatable(L, methodsa);
		   Lua.lua_pop(L, 2);
	   }
    }

// push onto the Lua stack a userdata containing a pointer to T object
    @SuppressWarnings("unchecked")
	public static int push(lua_State L, Object obj, boolean gc)
    {
        if (obj == null)
        {
            Lua.lua_pushnil(L);
            return Lua.lua_gettop(L);
        }

        //String name = RemoveChar(obj.getClass().getName(), '.');
        Class<?> cls = obj.getClass();
    	LuaClass lc = cls.getAnnotation(LuaClass.class);
    	String name = "";
    	Object objectToAdd = obj;
    	if(lc == null)
    	{
    		Log.e("Lunar", "No LuaClass defined for " + cls.getName());
    		/*String[] arr = cls.getName().split("\\.");
    		name = arr[arr.length - 1];*/
        	LuaNativeObject lno = new LuaNativeObject();
        	lno.obj = obj;
        	objectToAdd = lno;
        	cls = objectToAdd.getClass();
        	lc = cls.getAnnotation(LuaClass.class);
        	name = lc.className();
    		//return Lua.lua_gettop(L);
    	}
    	else
    		name = lc.className();

        Lua.luaL_getmetatable(L, name);  // lookup metatable in Lua registry
        if (Lua.lua_isnil(L, -1))
            Lua.luaL_error(L, name + " missing metatable");

        int mt = Lua.lua_gettop(L);
        //Lua.lua_pushlightuserdata(L, obj);
        //Object ptr = Lua.lua_newuserdata(L, obj, sizeof(obj).length);
        Object ptr = Lua.lua_newuserdata(L, LuaObject.class);
        //((LuaObject)ptr).PushObject(obj);
        ((LuaObject)ptr).PushObject(objectToAdd);
        int ud = Lua.lua_gettop(L);
        {
            Lua.lua_pushvalue(L, mt);
            Lua.lua_setmetatable(L, -2);
            Lua.lua_getfield(L, Lua.LUA_REGISTRYINDEX, "DO NOT TRASH");
            if (Lua.lua_isnil(L, -1))
            {
                Lua.luaL_newmetatable(L, "DO NOT TRASH");
                Lua.lua_pop(L, 1);
            }
            Lua.lua_getfield(L, Lua.LUA_REGISTRYINDEX, "DO NOT TRASH");
            if (gc == false)
            {
                Lua.lua_pushboolean(L, true);
                Lua.lua_setfield(L, -2, name);
            }
            Lua.lua_pop(L, 1);
        }

        Lua.lua_settop(L, ud);
        Lua.lua_replace(L, mt);
        Lua.lua_settop(L, mt);
        return mt;  // index of userdata containing pointer to T object
    }

    // get userdata from Lua stack and return pointer to T object
    private static Object check(lua_State L, int narg) 
    {
        Object obj = Lua.lua_touserdata(L, narg);
        //T obj = (T)Lua.lua_touserdata(L, narg);
        if (obj == null)
		    return null;
	    return obj;
    }
    
    public static Object ParseTable(Lua.lua_TValue valP)
    {
    	HashMap<Object, Object> map = new HashMap<Object, Object>();
    	Lua.Table ot = Lua.hvalue(valP);
    	int size = Lua.sizenode(ot);
		for(int i = 0; i < size; i++)
		{
			Lua.Node node = Lua.gnode(ot, i);
			Lua.lua_TValue key = Lua.key2tval(node);
			Object keyObject = null;
			switch(Lua.ttype(key))
			{
				case Lua.LUA_TNIL:
					break;
				case Lua.LUA_TSTRING:
				{
					Lua.TString str = Lua.rawtsvalue(key);
					keyObject = str.toString();
				}break;
				case Lua.LUA_TNUMBER:
					keyObject = Lua.nvalue(key);
					break;
				default:
					break;
			}
			
			Lua.lua_TValue val = Lua.luaH_get(ot, key);
			Object valObject = null;
			switch(Lua.ttype(val))
			{
				case Lua.LUA_TNIL:
					break;
				case Lua.LUA_TSTRING:
				{
					Lua.TString str = Lua.rawtsvalue(val);
					valObject = str.toString();
				}break;
				case Lua.LUA_TNUMBER:
					valObject = Lua.nvalue(val);
					break;
				case Lua.LUA_TTABLE:
					valObject = ParseTable(val);
					break;
				case Lua.LUA_TUSERDATA:
				{
					valObject = (Lua.rawuvalue(val).user_data);
					if(valObject != null)
					{
						if(valObject.getClass().getName().startsWith("com.dk.scriptingengine.LuaObject"))
	                    {
	            			Object objA = ((LuaObject<?>)valObject).obj;
	            			if(objA == null)
	            			{
	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
	                            return 0;
	            			}
	
	                        valObject = objA;
	                    }
					}
				}break;
				case Lua.LUA_TLIGHTUSERDATA:
				{
					valObject = Lua.pvalue(val);
					if(valObject != null)
					{
						if(valObject.getClass().getName().startsWith("com.dk.scriptingengine.LuaObject"))
	                    {
	            			Object objA = ((LuaObject<?>)valObject).obj;
	            			if(objA == null)
	            			{
	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
	                            return 0;
	            			}
	
	                        valObject = objA;
	                    }
					}
				}break;					
					
			}
			map.put(keyObject, valObject);
		}
		return map;
    }

    // member function dispatcher
    @SuppressWarnings("rawtypes")
	public static int thunk(lua_State L)
    {
    	try
    	{
	    	// stack has userdata, followed by method args
		    Object lobj = check(L, 1);  // get 'self', or if you prefer, 'this'
		    if(lobj == null)
		    	throw new LuaException(L, null);
		    Object obj = ((LuaObject)(lobj)).obj;
		    Lua.lua_remove(L, 1);  // remove self so member function args start at index 1
		    // get member function from upvalue
	        //MethodInfo mi = (MethodInfo)Lua.lua_touserdata(L, Lua.lua_upvalueindex(1));
		    LuaJavaFunction la = (LuaJavaFunction)Lua.lua_touserdata(L, Lua.lua_upvalueindex(1));
		    
		    /*Method m = null; old one
			try 
			{
				m = obj.getClass().getMethod(la.methodName(), la.arguments());
			} 
			catch (SecurityException e2) 
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} 
			catch (NoSuchMethodException e2) 
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}*/
		    Method m = la.method();
		    
	        if (la.manual())
	        {
				try 
				{
					m.invoke(obj, new Object[] { L });
				} 
				catch (IllegalArgumentException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (IllegalAccessException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				catch (InvocationTargetException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
			else
	        {
	            int count = 1;
	            ArrayList<Object> argList = new ArrayList<Object>();
	            Class<?>[] argTypeList = la.arguments();
	            for (Class<?> c : argTypeList)
	            {
	            	String name = c.getName();
	            	if(name.compareTo("java.lang.Boolean") == 0)
	            		argList.add((Lua.luaL_checkinteger(L, count)) != 0);
	            	else if(name.compareTo("java.lang.Byte") == 0)
	            		argList.add(Lua.luaL_checkinteger(L, count));
	            	else if(name.compareTo("java.lang.Short") == 0)
	            		argList.add((short)(Lua.luaL_checkinteger(L, count)));
	            	else if(name.compareTo("java.lang.Integer") == 0)
	            		argList.add(Lua.luaL_checkinteger(L, count));
	            	else if(name.compareTo("java.lang.Long") == 0)
	            		argList.add((Long)Lua.luaL_checklong(L, count));
	            	else if(name.compareTo("java.lang.Float") == 0)
	            		argList.add(new Float((Lua.luaL_checknumber(L, count))));
	            	else if(name.compareTo("java.lang.Double") == 0)
	            		argList.add((Double)Lua.luaL_checknumber(L, count));
	            	else if(name.compareTo("java.lang.Char") == 0)
	            	{
	            		String val = Lua.luaL_checkstring(L, count).toString();
	            		argList.add(val.charAt(0));
	            	}	            		
	            	else if(name.compareTo("java.lang.String") == 0)
	            		argList.add(Lua.luaL_checkstring(L, count).toString());
	            	else if(name.compareTo("com.KopiLua.lua_State") == 0)
	            		argList.add(L);
	            	else
	            	{
	            		Object o = Lua.lua_touserdata(L, count);
	            		if(o != null)
	            		{
		            		if(o.getClass() == com.dk.scriptingengine.backend.LuaObject.class)
		                    {
		            			Object objA = ((LuaObject<?>)o).obj;
		            			if(objA == null)
		            			{
		            				Log.e("Lunar Push", "Cannot get lua object property thunk");
		                            return 0;
		            			}
		
		                        argList.add(objA);
		                    }
		            		else if(o.getClass() == com.dk.scriptingengine.LuaNativeObject.class)
		            		{
		            			Object objNative = ((LuaNativeObject)o).obj;
		            			if(objNative == null)
		            			{
		            				Log.e("Lunar Push", "Cannot get lua native object property thunk");
		            				return 0;
		            			}
		            			
		            			argList.add(objNative);
		            		}
		                    else
		                        argList.add(o);
	            		}
	            		else
	            		{
	            			o = Lua.lua_topointer(L, count);
	            			if(o == null)
	            			{
	            				if(Lua.lua_isboolean(L, count))
	            					argList.add(Lua.lua_toboolean(L, count));
	            				else if(Lua.lua_isnumber(L, count) > 0)
	            					argList.add(Lua.lua_tonumber(L, count));
	            				else if(Lua.lua_isstring(L, count) > 0)
	            					argList.add(Lua.lua_tostring(L, count).toString());
	            				else if(Lua.lua_isnoneornil(L, count))
	            					argList.add(null);
	            			}
	            			else
	            			{
		            			Lua.Table ot = (Lua.Table)o;
		            			HashMap<Object, Object> map = new HashMap<Object, Object>();
		            			boolean useArray = ot.sizearray > 0;
		            			int size = 0;
		            			if(useArray)
		            				size = ot.sizearray;
		            			else
		            				size = Lua.sizenode(ot);
		            			for(int i = 0; i < size; i++)
		            			{
		            				Object keyObject = null;
		            				Lua.lua_TValue key = null;
		            				if(!useArray)
		            				{
			            				Lua.Node node = Lua.gnode(ot, i);
			            				key = Lua.key2tval(node);
			            				switch(Lua.ttype(key))
										{
											case Lua.LUA_TNIL:
												break;
											case Lua.LUA_TSTRING:
											{
												Lua.TString str = Lua.rawtsvalue(key);
												keyObject = str.toString();
											}break;
											case Lua.LUA_TNUMBER:
												keyObject = Lua.nvalue(key);
												break;
											default:
												break;
										}
		            				}
		            				else
		            				{
		            					keyObject = i;
		            				}
		            				
		            				Lua.lua_TValue val = null;
	            					Object valObject = null;
		            				if(!useArray)
		            					val = Lua.luaH_get(ot, key);
		            				else
		            					val = ot.array[i];
		            				switch(Lua.ttype(val))
		            				{
		            					case Lua.LUA_TNIL:
		            						break;
		            					case Lua.LUA_TSTRING:
		            					{
		            						Lua.TString str = Lua.rawtsvalue(val);
		            						valObject = str.toString();
		            					}break;
		            					case Lua.LUA_TNUMBER:
		            						valObject = Lua.nvalue(val);
		            						break;
		            					case Lua.LUA_TTABLE:
		            						valObject = ParseTable(val);
		            						break;
		            					case Lua.LUA_TUSERDATA:
		            					{
		            						valObject = (Lua.rawuvalue(val).user_data);
		            						if(valObject != null)
		            						{
			            						if(valObject.getClass().getName().startsWith("com.dk.scriptingengine.LuaObject"))
			            	                    {
			            	            			Object objA = ((LuaObject<?>)valObject).obj;
			            	            			if(objA == null)
			            	            			{
			            	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
			            	                            return 0;
			            	            			}
			            	
			            	                        valObject = objA;
			            	                    }
		            						}
		            					}break;
		            					case Lua.LUA_TLIGHTUSERDATA:
		            					{
		            						valObject = Lua.pvalue(val);
		            						if(valObject != null)
		            						{
			            						if(valObject.getClass().getName().startsWith("com.dk.scriptingengine.LuaObject"))
			            	                    {
			            	            			Object objA = ((LuaObject<?>)valObject).obj;
			            	            			if(objA == null)
			            	            			{
			            	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
			            	                            return 0;
			            	            			}
			            	
			            	                        valObject = objA;
			            	                    }
		            						}
		            					}break;
		            				}
		            				if(keyObject == null && valObject == null)
		            				{
		            					for(int j = 0; j < ot.array.length; j++)
		            					{
		            						Lua.lua_TValue valO = ot.array[j];
		    	            				switch(Lua.ttype(valO))
		    	            				{
		    	            					case Lua.LUA_TNIL:
		    	            						break;
		    	            					case Lua.LUA_TSTRING:
		    	            					{
		    	            						Lua.TString str = Lua.rawtsvalue(valO);
		    	            						valObject = str.toString();
		    	            					}break;
		    	            					case Lua.LUA_TNUMBER:
		    	            						valObject = Lua.nvalue(valO);
		    	            						break;
		    	            					case Lua.LUA_TTABLE:
		    	            						valObject = ParseTable(valO);
		    	            						break;
		    	            					case Lua.LUA_TUSERDATA:
		    	            					{
		    	            						valObject = (Lua.rawuvalue(valO).user_data);
		    	            						if(valObject != null)
		    	            						{
		    		            						if(valObject.getClass().getName().startsWith("com.dk.scriptingengine.LuaObject"))
		    		            	                    {
		    		            	            			Object objA = ((LuaObject<?>)valObject).obj;
		    		            	            			if(objA == null)
		    		            	            			{
		    		            	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
		    		            	                            return 0;
		    		            	            			}
		    		            	
		    		            	                        valObject = objA;
		    		            	                    }
		    	            						}
		    	            					}break;
		    	            					case Lua.LUA_TLIGHTUSERDATA:
		    	            					{
		    	            						valObject = Lua.pvalue(valO);
		    	            						if(valObject != null)
		    	            						{
		    		            						if(valObject.getClass().getName().startsWith("com.dk.scriptingengine.LuaObject"))
		    		            	                    {
		    		            	            			Object objA = ((LuaObject<?>)valObject).obj;
		    		            	            			if(objA == null)
		    		            	            			{
		    		            	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
		    		            	                            return 0;
		    		            	            			}
		    		            	
		    		            	                        valObject = objA;
		    		            	                    }
		    	            						}
		    	            					}break;
		    	            				}
		            						map.put(j, valObject);
		            					}	            					
		            				}
		            				else
		            					map.put(keyObject, valObject);
		            			}
		            			argList.add(map);
	            			}
	            		}
	            	}
	            
	                
	                ++count;
	            }
	
	            Object retval = null;
				try 
				{
					retval = m.invoke(obj, argList.toArray());
				} catch (IllegalArgumentException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (IllegalAccessException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (InvocationTargetException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	            if (retval == null)
	                ((LuaEngine)LuaEngine.getInstance()).PushNIL();
	            else 
	            {
	            	String retName = retval.getClass().getName();
	            	if(retName.compareTo("java.lang.Boolean") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushBool((Boolean)retval);
	            	else if(retName.compareTo("java.lang.Byte") == 0
	            		|| retName.compareTo("java.lang.Short") == 0
	            		|| retName.compareTo("java.lang.Integer") == 0
	            		|| retName.compareTo("java.lang.Long") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushInt((Integer)retval);
	            	else if(retName.compareTo("java.lang.Float") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushFloat((Float)retval);
	            	else if(retName.compareTo("java.lang.Double") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushDouble((Double)retval);
	            	else if(retName.compareTo("java.lang.Char") == 0
	            		|| retName.compareTo("java.lang.String") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushString((String)retval);
	            	else if(retName.compareTo("java.lang.Void") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushInt(0);
	            	else if(retName.compareTo("java.util.HashMap") == 0)
	            	{
	            		((LuaEngine)LuaEngine.getInstance()).PushTable((HashMap<Object, Object>)retval);
	            	}
	            	else
	            	{
	                	LuaEngine l = (LuaEngine)LuaEngine.getInstance();
	                	Lunar.push(l.GetLuaState(), retval, false);
	            	}
	            }
	        }
	        
	        
		    /*RegType *l = static_cast<RegType*>(lua_touserdata(L, lua_upvalueindex(1)));
		    //return (obj->*(l->mfunc))(L);  // call member function
		    return l->mfunc(L,obj);*/
	        return 1;
	    }
		catch (LuaException e) 
		{
			String s = Lua.lua_tostring(L, -1).toString();
			if(s == null || e.c == null)
				Log.e("Lunar", "Exception on thunk, possible solution use : for object variables");
			else
				Log.e("Lunar", "Exception on thunk, " + s);
			Log.e("Lunar", e.getMessage());
			Tools.LogException("Lunar", e);
			return 1;
		}
    }
    
    public static int Sthunk(lua_State L)
    {
    	try
    	{
	    	// stack has userdata, followed by method args
		    /*Object lobj = check(L, 1);  // get 'self', or if you prefer, 'this'
		    Object obj = ((LuaObject)(lobj)).obj;*/
		    //Lua.lua_remove(L, 1);  // remove self so member function args start at index 1
		    // get member function from upvalue
	        //MethodInfo mi = (MethodInfo)Lua.lua_touserdata(L, Lua.lua_upvalueindex(1));
		    LuaJavaFunction la = (LuaJavaFunction)Lua.lua_touserdata(L, Lua.lua_upvalueindex(1));
		    Class<?> selfClass = la.self();
		    
		    /*Method m = null; oldone
			try {
				m = selfClass.getMethod(la.methodName(), la.arguments());
			} catch (SecurityException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (NoSuchMethodException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}*/
		    Method m = la.method();
		    
	        if (la.manual())
	        {
				try {
					m.invoke(null, new Object[] { L });
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
			else
	        {
	            int count = 1;
	            ArrayList<Object> argList = new ArrayList<Object>();
	            Class<?>[] argTypeList = la.arguments();
	            //if(argTypeList.length == Lua.luaL_ar)
	            for (Class<?> c : argTypeList)
	            {
	            	String name = c.getName();
	            	if(name.compareTo("java.lang.Boolean") == 0)
	            		argList.add((Lua.luaL_checkinteger(L, count)) != 0);
	            	else if(name.compareTo("java.lang.Byte") == 0)
	            		argList.add(Lua.luaL_checkinteger(L, count));
	            	else if(name.compareTo("java.lang.Short") == 0)
	            		argList.add((short)(Lua.luaL_checkinteger(L, count)));
	            	else if(name.compareTo("java.lang.Integer") == 0)
	            		argList.add(Lua.luaL_checkinteger(L, count));
	            	else if(name.compareTo("java.lang.Long") == 0)
	            		argList.add((Long)Lua.luaL_checklong(L, count));
	            	else if(name.compareTo("java.lang.Float") == 0)
	            		argList.add(new Float((Lua.luaL_checknumber(L, count))));
	            	else if(name.compareTo("java.lang.Double") == 0)
	            		argList.add((Double)Lua.luaL_checknumber(L, count));
	            	else if(name.compareTo("java.lang.Character") == 0)
	            	{
	            		String val = Lua.luaL_checkstring(L, count).toString();
	            		argList.add(val.charAt(0));
	            	}	            		
	            	else if(name.compareTo("java.lang.String") == 0)
	            		argList.add(Lua.luaL_checkstring(L, count).toString());
	            	else if(name.compareTo("com.KopiLua.lua_State") == 0)
	            		argList.add(L);
	            	else
	            	{
	            		//argList.add(Lua.luaL_checkudata(L, count, c.getName()));
	            		Object o = Lua.lua_touserdata(L, count);
	            		if(o != null)
	            		{
		            		if(o.getClass() == com.dk.scriptingengine.backend.LuaObject.class)
		                    {
		            			Object obj = ((LuaObject<?>)o).obj;
		            			if(obj == null)
		            			{
		            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
		                            return 0;
		            			}
		
		                        argList.add(obj);
		                    }
		            		else if(o.getClass() == com.dk.scriptingengine.LuaNativeObject.class)
		            		{
		            			Object objNative = ((LuaNativeObject)o).obj;
		            			if(objNative == null)
		            			{
		            				Log.e("Lunar Push", "Cannot get lua native object property static thunk");
		            				return 0;
		            			}
		            			
		            			argList.add(objNative);
		            		}
		                    else
		                    	argList.add(o);
	            		}
	            		else
	            		{
	            			o = Lua.lua_topointer(L, count);
	            			if(o == null)
	            			{
	            				if(Lua.lua_isboolean(L, count))
	            					argList.add(Lua.lua_toboolean(L, count));
	            				else if(Lua.lua_isnumber(L, count) > 0)
	            					argList.add(Lua.lua_tonumber(L, count));
	            				else if(Lua.lua_isstring(L, count) > 0)
	            					argList.add(Lua.lua_tostring(L, count).toString());
	            				else if(Lua.lua_isnoneornil(L, count))
	            					argList.add(null);
	            			}
	            			else
	            			{
		            			Lua.Table ot = (Lua.Table)o;
		            			HashMap<Object, Object> map = new HashMap<Object, Object>();
		            			boolean useArray = ot.sizearray > 0;
		            			int size = 0;
		            			if(useArray)
		            				size = ot.sizearray;
		            			else
		            				size = Lua.sizenode(ot);
		            			for(int i = 0; i < size; i++)
		            			{
		            				Object keyObject = null;
		            				Lua.lua_TValue key = null;
		            				if(!useArray)
		            				{
			            				Lua.Node node = Lua.gnode(ot, i);
			            				key = Lua.key2tval(node);
			            				switch(Lua.ttype(key))
										{
											case Lua.LUA_TNIL:
												break;
											case Lua.LUA_TSTRING:
											{
												Lua.TString str = Lua.rawtsvalue(key);
												keyObject = str.toString();
											}break;
											case Lua.LUA_TNUMBER:
												keyObject = Lua.nvalue(key);
												break;
											default:
												break;
										}
		            				}
		            				else
		            				{
		            					keyObject = i;
		            				}
		            				
		            				Lua.lua_TValue val = null;
	            					Object valObject = null;
		            				if(!useArray)
		            					val = Lua.luaH_get(ot, key);
		            				else
		            					val = ot.array[i];
		            				switch(Lua.ttype(val))
		            				{
		            					case Lua.LUA_TNIL:
		            						break;
		            					case Lua.LUA_TSTRING:
		            					{
		            						Lua.TString str = Lua.rawtsvalue(val);
		            						valObject = str.toString();
		            					}break;
		            					case Lua.LUA_TNUMBER:
		            						valObject = Lua.nvalue(val);
		            						break;
		            					case Lua.LUA_TTABLE:
		            						valObject = ParseTable(val);
		            						break;
		            					case Lua.LUA_TUSERDATA:
		            					{
		            						valObject = (Lua.rawuvalue(val).user_data);
		            						if(valObject != null)
		            						{
			            						if(valObject.getClass().getName().startsWith("com.dk.scriptingengine.LuaObject"))
			            	                    {
			            	            			Object objA = ((LuaObject<?>)valObject).obj;
			            	            			if(objA == null)
			            	            			{
			            	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
			            	                            return 0;
			            	            			}
			            	
			            	                        valObject = objA;
			            	                    }
		            						}
		            					}break;
		            					case Lua.LUA_TLIGHTUSERDATA:
		            					{
		            						valObject = Lua.pvalue(val);
		            						if(valObject != null)
		            						{
			            						if(valObject.getClass().getName().startsWith("com.dk.scriptingengine.LuaObject"))
			            	                    {
			            	            			Object objA = ((LuaObject<?>)valObject).obj;
			            	            			if(objA == null)
			            	            			{
			            	            				Log.e("Lunar Push", "Cannot get lua object property static thunk");
			            	                            return 0;
			            	            			}
			            	
			            	                        valObject = objA;
			            	                    }
		            						}
		            					}break;
		            				}
		            				map.put(keyObject, valObject);
		            			}
		            			argList.add(map);
	            			}
	            		}
	            	}            		
	                
	                ++count;
	            }
	            
	            Object retval = null;
				try 
				{
					retval = m.invoke(null, argList.toArray());
				} 
				catch (Exception e)
				{
					Log.e("Lunar", "Exception occured at static thunk, " + e.getMessage() + "\n" + m.getName());
				}
	            
	
	            if (retval == null)
	                ((LuaEngine)LuaEngine.getInstance()).PushNIL();
	            else
	            {
	            	String retName = retval.getClass().getName();
	            	if(retName.compareTo("java.lang.Boolean") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushBool((Boolean)retval);
	            	else if(retName.compareTo("java.lang.Byte") == 0
	            		|| retName.compareTo("java.lang.Short") == 0
	            		|| retName.compareTo("java.lang.Integer") == 0
	            		|| retName.compareTo("java.lang.Long") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushInt((Integer)retval);
	            	else if(retName.compareTo("java.lang.Float") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushFloat((Float)retval);
	            	else if(retName.compareTo("java.lang.Double") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushDouble((Double)retval);
	            	else if(retName.compareTo("java.lang.Char") == 0
	            		|| retName.compareTo("java.lang.String") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushString((String)retval);
	            	else if(retName.compareTo("java.lang.Void") == 0)
	            		((LuaEngine)LuaEngine.getInstance()).PushInt(0);
	            	else if(retName.compareTo("java.util.HashMap") == 0)
	            	{
	            		((LuaEngine)LuaEngine.getInstance()).PushTable((HashMap<Object, Object>)retval);
	            	}            	
	            	else
	            	{
	                	LuaEngine l = (LuaEngine)LuaEngine.getInstance();
	                	Lunar.push(l.GetLuaState(), retval, false);
	            	}
	            }
	        }
	        
	        
		    /*RegType *l = static_cast<RegType*>(lua_touserdata(L, lua_upvalueindex(1)));
		    //return (obj->*(l->mfunc))(L);  // call member function
		    return l->mfunc(L,obj);*/
	        return 1;
    	}
    	catch (LuaException e) 
		{
    		String s = Lua.lua_tostring(L, -1).toString();
    		if(s == null)
    			Log.e("Lunar", "Exception on sThunk, possible solution use . for static variables");
    		else
    			Log.e("Lunar", "Exception on sThunk, " + s);
			Log.e("Lunar", e.getMessage());
			Tools.LogException("Lunar", e);
			return 1;
		}
    }    

    // garbage collection metamethod	
	public static int gc_T(lua_State L)
	{
        int ptr = 0;
	    Object obj = check(L, 1);
	    if(obj == null)
		    return 0;
	    Lua.lua_getfield(L, Lua.LUA_REGISTRYINDEX, "DO NOT TRASH");
	    if(Lua.lua_istable(L, -1))
	    {
	    	String name = RemoveChar(obj.getClass().getName(), '.');
            Lua.lua_getfield(L, -1, name);
		    if(Lua.lua_isnil(L,-1))
		    {
			    obj = null;
		    }
	    }
	    Lua.lua_pop(L, 3);
	    return 0;
	}

	public static int tostring_T(lua_State L)
	{
		Object ptrHold = (Lua.lua_touserdata(L, 1));
		if(ptrHold == null)
			return 0;
		if(ptrHold.getClass() == LuaObject.class)
			ptrHold = ((LuaObject<?>)ptrHold).obj;
		if(ptrHold == null)
			return 0;
		String name = ptrHold.getClass().getSimpleName();
		Lua.lua_pushstring(L, name);
		return 1;
	}
}
