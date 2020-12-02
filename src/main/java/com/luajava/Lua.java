package com.luajava;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Calendar;
import java.util.HashMap;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.backend.Lunar;
import com.dk.scriptingengine.osspecific.Base64;
import com.dk.scriptingengine.osspecific.Defines;
import com.dk.scriptingengine.osspecific.Log;
import com.dk.scriptingengine.osspecific.LuaLogStream;
import com.lordjoe.csharp.Delegator;
import com.lordjoe.csharp.IDelegate;
import com.lordjoe.csharp.OneDelegate;
import com.lordjoe.csharp.TwoDelegate;

public class Lua {
	public static final String LUA_VERSION = "Lua 5.1";
	public static final String LUA_RELEASE = "Lua 5.1.4";
	public static final int LUA_VERSION_NUM = 501;
	public static final String LUA_COPYRIGHT = "Copyright (C) 1994-2008 Lua.org, PUC-Rio";
	public static final String LUA_AUTHORS = "R. Ierusalimschy, L. H. de Figueiredo & W. Celes";

	/* mark for precompiled code (`<esc>Lua') */
	public static final String LUA_SIGNATURE = "\u001bLua";

	/* option for multiple returns in `lua_pcall' and `lua_call' */
	public static final int LUA_MULTRET = (-1);

	/*
	 * * pseudo-indices
	 */
	public static final int LUA_REGISTRYINDEX = (-10000);
	public static final int LUA_ENVIRONINDEX = (-10001);
	public static final int LUA_GLOBALSINDEX = (-10002);

	public static int lua_upvalueindex(int i) {
		return LUA_GLOBALSINDEX - i;
	}

	/* thread status; 0 is OK */
	public static final int LUA_YIELD = 1;
	public static final int LUA_ERRRUN = 2;
	public static final int LUA_ERRSYNTAX = 3;
	public static final int LUA_ERRMEM = 4;
	public static final int LUA_ERRERR = 5;

	public static final int LUA_GCSTOP = 0;
	public static final int LUA_GCRESTART = 1;
	public static final int LUA_GCCOLLECT = 2;
	public static final int LUA_GCCOUNT = 3;
	public static final int LUA_GCCOUNTB = 4;
	public static final int LUA_GCSTEP = 5;
	public static final int LUA_GCSETPAUSE = 6;
	public static final int LUA_GCSETSTEPMUL = 7;

	/* Key to file-handle type */
	public static final String LUA_FILEHANDLE = "FILE*";

	public static final String LUA_COLIBNAME = "coroutine";
	public static final String LUA_TABLIBNAME = "table";
	public static final String LUA_IOLIBNAME = "io";
	public static final String LUA_OSLIBNAME = "os";
	public static final String LUA_STRLIBNAME = "string";
	public static final String LUA_MATHLIBNAME = "math";
	public static final String LUA_DBLIBNAME = "debug";
	public static final String LUA_LOADLIBNAME = "package";

	/* for header of binary files -- this is Lua 5.1 */
	public static final int LUAC_VERSION = 0x51;

	/* for header of binary files -- this is the official format */
	public static final int LUAC_FORMAT = 0;

	/* size of header of binary files */
	public static final int LUAC_HEADERSIZE = 12;

	public static final String lua_ident = "$Lua: " + LUA_RELEASE + " "
			+ LUA_COPYRIGHT + " $\n" + "$Authors: " + LUA_AUTHORS + " $\n"
			+ "$URL: www.lua.org $\n";

	/*
	 * * basic types
	 */
	public static final int LUA_TNONE = -1;

	public static final int LUA_TNIL = 0;
	public static final int LUA_TBOOLEAN = 1;
	public static final int LUA_TLIGHTUSERDATA = 2;
	public static final int LUA_TNUMBER = 3;
	public static final int LUA_TSTRING = 4;
	public static final int LUA_TTABLE = 5;
	public static final int LUA_TFUNCTION = 6;
	public static final int LUA_TUSERDATA = 7;
	public static final int LUA_TTHREAD = 8;

	/* minimum Lua stack available to a C function */
	public static final int LUA_MINSTACK = 20;

	public static final String LUA_INTFRMLEN = "";

	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate int lua_CFunction(lua_State L);
	public static Delegator lua_CFunction = new Delegator(
			new Class[] { lua_State.class }, Integer.TYPE);

	/*
	 * * functions that read/write blocks when loading/dumping Lua chunks
	 */
	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate CharPtr lua_Reader(lua_State L, object ud, out uint sz);
	public static Delegator lua_Reader = new Delegator(new Class[] {
			lua_State.class, Object.class, RefObject.class }, CharPtr.class);

	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate int lua_Writer(lua_State L, CharPtr p, uint sz, object
	// ud);
	public static Delegator lua_Writer = new Delegator(new Class[] {
			lua_State.class, Object.class, int.class, Object.class },
			Integer.TYPE);

	/*
	 * * prototype for memory-allocation functions
	 */
	// public delegate object lua_Alloc(object ud, object ptr, uint osize, uint
	// nsize);
	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate object lua_Alloc(Type t);
	public static Delegator lua_Alloc = new Delegator(
			new Class[] { Class.class }, Object.class);

	/* type of protected functions, to be ran by `runprotected' */
	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate void Pfunc(lua_State L, object ud);
	public static Delegator Pfunc = new Delegator(new Class[] {
			lua_State.class, Object.class }, void.class);

	/* Functions to be called by the debuger in specific events */
	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate void lua_Hook(lua_State L, lua_Debug ar);
	public static Delegator lua_Hook = new Delegator(new Class[] {
			lua_State.class, lua_Debug.class }, Void.TYPE);

	public static void api_checknelems(lua_State L, int n) {
		api_check(L, n <= Lua.lua_TValue.OpSubtraction(L.top, L.base_));
	}

	public static void api_checkvalidindex(lua_State L, lua_TValue i) {
		api_check(L, i != luaO_nilobject);
	}

	public static void api_incr_top(lua_State L) {
		api_check(L, Lua.lua_TValue.OpLessThan(L.top, L.ci.top));
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.inc(tempRef_top);
		L.top = tempRef_top.argvalue;
	}

	public static lua_TValue index2adr(lua_State L, int idx) {
		if (idx > 0) {
			lua_TValue o = Lua.lua_TValue.OpAddition(L.base_, (idx - 1));
			api_check(L, idx <= Lua.lua_TValue.OpSubtraction(L.ci.top, L.base_));
			if (lua_TValue.OpGreaterThanOrEqual(o, L.top))// if (o >= L.top)
			{
				return luaO_nilobject;
			} else {
				return o;
			}
		} else if (idx > LUA_REGISTRYINDEX) {
			api_check(
					L,
					idx != 0
							&& -idx <= Lua.lua_TValue.OpSubtraction(L.top,
									L.base_));
			return Lua.lua_TValue.OpAddition(L.top, idx);
		} else // pseudo-indices
		{
			switch (idx) {
			case LUA_REGISTRYINDEX:
				return registry(L);
			case LUA_ENVIRONINDEX: {
				Closure func = curr_func(L);
				sethvalue(L, L.env, func.c.getenv());
				return L.env;
			}
			case LUA_GLOBALSINDEX:
				return gt(L);
			default: {
				Closure func = curr_func(L);
				idx = LUA_GLOBALSINDEX - idx;
				return (idx <= func.c.getnupvalues()) ? func.c.upvalue[idx - 1]
						: (lua_TValue) luaO_nilobject;
			}
			}
		}
	}

	public static Table getcurrenv(lua_State L) {
		if (L.ci == L.base_ci[0]) // no enclosing function?
		{
			return hvalue(gt(L)); // use global table as environment
		} else {
			Closure func = curr_func(L);
			return func.c.getenv();
		}
	}

	public static void luaA_pushobject(lua_State L, lua_TValue o) {
		setobj2s(L, L.top, o);
		api_incr_top(L);
	}

	public static int lua_checkstack(lua_State L, int size) {
		int res = 1;
		lua_lock(L);
		if (size > LUAI_MAXCSTACK
				|| (Lua.lua_TValue.OpSubtraction(L.top, L.base_) + size) > LUAI_MAXCSTACK) {
			res = 0; // stack overflow
		} else if (size > 0) {
			luaD_checkstack(L, size);
			if (Lua.lua_TValue.OpLessThan(L.ci.top,
					Lua.lua_TValue.OpAddition(L.top, size))) {
				L.ci.top = Lua.lua_TValue.OpAddition(L.top, size);
			}
		}
		lua_unlock(L);
		return res;
	}

	public static void lua_xmove(lua_State from, lua_State to, int n) {
		int i;
		if (from == to) {
			return;
		}
		lua_lock(to);
		api_checknelems(from, n);
		api_check(from, G(from) == G(to));
		api_check(from, Lua.lua_TValue.OpSubtraction(to.ci.top, to.top) >= n);
		from.top = lua_TValue.OpSubtraction(from.top, n); // from.top -= n;
		for (i = 0; i < n; i++) {
			RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
					to.top);
			setobj2s(to, lua_TValue.inc(tempRef_top),
					Lua.lua_TValue.OpAddition(from.top, i));
			to.top = tempRef_top.argvalue;
		}
		lua_unlock(to);
	}

	public static void lua_setlevel(lua_State from, lua_State to) {
		to.nCcalls = from.nCcalls;
	}

	// public static lua_CFunction lua_atpanic(lua_State L, lua_CFunction
	// panicf)
	public static IDelegate lua_atpanic(lua_State L, IDelegate panicf) {
		IDelegate old;// lua_CFunction old;
		lua_lock(L);
		old = G(L).panic;
		G(L).panic = panicf;
		lua_unlock(L);
		return old;
	}

	public static lua_State lua_newthread(lua_State L) {
		lua_State L1;
		lua_lock(L);
		luaC_checkGC(L);
		L1 = luaE_newthread(L);
		setthvalue(L, L.top, L1);
		api_incr_top(L);
		lua_unlock(L);
		luai_userstatethread(L, L1);
		return L1;
	}

	/*
	 * * basic stack manipulation
	 */

	public static int lua_gettop(lua_State L) {
		return cast_int(Lua.lua_TValue.OpSubtraction(L.top, L.base_));
	}

	public static void lua_settop(lua_State L, int idx) {
		lua_lock(L);
		if (idx >= 0) {
			api_check(L,
					idx <= Lua.lua_TValue.OpSubtraction(L.stack_last, L.base_));
			while (Lua.lua_TValue.OpLessThan(L.top,
					Lua.lua_TValue.OpAddition(L.base_, idx))) {
				RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
						L.top);
				setnilvalue(lua_TValue.inc(tempRef_top));
				L.top = tempRef_top.argvalue;
			}
			L.top = Lua.lua_TValue.OpAddition(L.base_, idx);
		} else {
			api_check(
					L,
					-(idx + 1) <= (Lua.lua_TValue.OpSubtraction(L.top, L.base_)));
			L.top = lua_TValue.OpAddition(L.top, idx + 1);// L.top += idx+1; //
															// `subtract' index
															// (index is
															// negative)
		}
		lua_unlock(L);
	}

	public static void lua_remove(lua_State L, int idx) {
		lua_TValue p;
		lua_lock(L);
		p = index2adr(L, idx);
		api_checkvalidindex(L, p);
		while (lua_TValue.OpLessThan((p = p.getItem(1)), L.top))// while
																// ((p=p[1]) <
																// L.top)
		{
			setobj2s(L, lua_TValue.OpSubtraction(p, 1), p);// setobjs2s(L, p-1,
															// p);
		}
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top);
		L.top = tempRef_top.argvalue;
		lua_unlock(L);
	}

	public static void lua_insert(lua_State L, int idx) {
		lua_TValue p = new lua_TValue();
		lua_TValue q = new lua_TValue();
		lua_lock(L);
		p = index2adr(L, idx);
		api_checkvalidindex(L, p);
		RefObject<lua_TValue> val = new RefObject<Lua.lua_TValue>(q);
		for (val.argvalue = L.top; lua_TValue.OpGreaterThan(val.argvalue, p); lua_TValue
				.dec(val))// for (q = L.top; q>p; lua_TValue.dec(q))
		{
			setobjs2s(L, val.argvalue,
					lua_TValue.OpSubtraction(val.argvalue, 1));// setobjs2s(L,
																// q, q-1);
		}
		q = val.argvalue;
		setobjs2s(L, p, L.top);
		lua_unlock(L);
	}

	public static void lua_replace(lua_State L, int idx) {
		lua_TValue o;
		lua_lock(L);
		/* explicit test for incompatible code */
		if (idx == LUA_ENVIRONINDEX && L.ci == L.base_ci[0]) {
			luaG_runerror(L, new CharPtr("no calling environment"));
		}
		api_checknelems(L, 1);
		o = index2adr(L, idx);
		api_checkvalidindex(L, o);
		if (idx == LUA_ENVIRONINDEX) {
			Closure func = curr_func(L);
			api_check(L, ttistable(Lua.lua_TValue.OpSubtraction(L.top, 1)));
			func.c.setenv(hvalue(Lua.lua_TValue.OpSubtraction(L.top, 1)));
			luaC_barrier(L, func, Lua.lua_TValue.OpSubtraction(L.top, 1));
		} else {
			setobj(L, o, Lua.lua_TValue.OpSubtraction(L.top, 1));
			if (idx < LUA_GLOBALSINDEX) // function upvalue?
			{
				luaC_barrier(L, curr_func(L),
						Lua.lua_TValue.OpSubtraction(L.top, 1));
			}
		}
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top);
		L.top = tempRef_top.argvalue;
		lua_unlock(L);
	}

	public static void lua_pushvalue(lua_State L, int idx) {
		lua_lock(L);
		setobj2s(L, L.top, index2adr(L, idx));
		api_incr_top(L);
		lua_unlock(L);
	}

	/*
	 * * access functions (stack . C)
	 */

	public static int lua_type(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		return (o == luaO_nilobject) ? LUA_TNONE : ttype(o);
	}

	public static CharPtr lua_typename(lua_State L, int t) {
		// UNUSED(L);
		return (t == LUA_TNONE) ? new CharPtr("no value") : luaT_typenames[t];
	}

	public static boolean lua_iscfunction(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		return iscfunction(o);
	}

	public static int lua_isnumber(lua_State L, int idx) {
		lua_TValue n = new lua_TValue();
		lua_TValue o = index2adr(L, idx);
		RefObject<Lua.lua_TValue> tempRef_o = new RefObject<Lua.lua_TValue>(o);
		int tempVar = tonumber(tempRef_o, n);
		o = tempRef_o.argvalue;
		return tempVar;
	}

	public static int lua_isstring(lua_State L, int idx) {
		int t = lua_type(L, idx);
		return (t == LUA_TSTRING || t == LUA_TNUMBER) ? 1 : 0;
	}

	public static int lua_isuserdata(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		return (ttisuserdata(o) || ttislightuserdata(o)) ? 1 : 0;
	}

	public static int lua_rawequal(lua_State L, int index1, int index2) {
		lua_TValue o1 = index2adr(L, index1);
		lua_TValue o2 = index2adr(L, index2);
		return (o1 == luaO_nilobject || o2 == luaO_nilobject) ? 0
				: luaO_rawequalObj(o1, o2);
	}

	public static int lua_equal(lua_State L, int index1, int index2) {
		lua_TValue o1, o2;
		int i;
		lua_lock(L); // may call tag method
		o1 = index2adr(L, index1);
		o2 = index2adr(L, index2);
		i = (o1 == luaO_nilobject || o2 == luaO_nilobject) ? 0 : equalobj(L,
				o1, o2);
		lua_unlock(L);
		return i;
	}

	public static int lua_lessthan(lua_State L, int index1, int index2) {
		lua_TValue o1, o2;
		int i;
		lua_lock(L); // may call tag method
		o1 = index2adr(L, index1);
		o2 = index2adr(L, index2);
		i = (o1 == luaO_nilobject || o2 == luaO_nilobject) ? 0 : luaV_lessthan(
				L, o1, o2);
		lua_unlock(L);
		return i;
	}

	public static double lua_tonumber(lua_State L, int idx) {
		lua_TValue n = new lua_TValue();
		lua_TValue o = index2adr(L, idx);
		RefObject<Lua.lua_TValue> tempRef_o = new RefObject<Lua.lua_TValue>(o);
		boolean tempVar = tonumber(tempRef_o, n) != 0;
		o = tempRef_o.argvalue;
		if (tempVar) {
			return nvalue(o);
		} else {
			return 0;
		}
	}

	public static int lua_tointeger(lua_State L, int idx) {
		lua_TValue n = new lua_TValue();
		lua_TValue o = index2adr(L, idx);
		RefObject<Lua.lua_TValue> tempRef_o = new RefObject<Lua.lua_TValue>(o);
		boolean tempVar = tonumber(tempRef_o, n) != 0;
		o = tempRef_o.argvalue;
		if (tempVar) {
			Integer res = null;
			double num = nvalue(o);
			RefObject<Integer> tempRef_res = new RefObject<Integer>(res);
			lua_number2integer(tempRef_res, num);
			res = tempRef_res.argvalue;
			return res;
		} else {
			return 0;
		}
	}

	public static int lua_toboolean(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		return (l_isfalse(o) == 0) ? 1 : 0;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static CharPtr lua_tolstring (lua_State L, int idx,
	// out uint len)
	public static CharPtr lua_tolstring(lua_State L, int idx,
			RefObject<Integer> len) {
		lua_TValue o = index2adr(L, idx);
		if (!ttisstring(o)) {
			lua_lock(L); // `luaV_tostring' may create a new string
			if (luaV_tostring(L, o) == 0) // conversion failed?
			{
				len.argvalue = 0;
				lua_unlock(L);
				return null;
			}
			luaC_checkGC(L);
			o = index2adr(L, idx); // previous call may reallocate the stack
			lua_unlock(L);
		}
		len.argvalue = tsvalue(o).len;
		return svalue(o);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static uint lua_objlen (lua_State L, int idx)
	public static int lua_objlen(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		switch (ttype(o)) {
		case LUA_TSTRING:
			return tsvalue(o).len;
		case LUA_TUSERDATA:
			return uvalue(o).len;
		case LUA_TTABLE:
			return (int) luaH_getn(hvalue(o));
		case LUA_TNUMBER: {
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint l;
			int l;
			lua_lock(L); // `luaV_tostring' may create a new string
			l = (luaV_tostring(L, o) != 0 ? tsvalue(o).len : 0);
			lua_unlock(L);
			return l;
		}
		default:
			return 0;
		}
	}

	public static IDelegate lua_tocfunction(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		return (!iscfunction(o)) ? null : clvalue(o).c.f;
	}

	public static Object lua_touserdata(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		switch (ttype(o)) {
		case LUA_TUSERDATA:
			return (rawuvalue(o).user_data);
		case LUA_TLIGHTUSERDATA:
			return pvalue(o);
		default:
			return null;
		}
	}

	public static lua_State lua_tothread(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		return (!ttisthread(o)) ? null : thvalue(o);
	}

	public static Object lua_topointer(lua_State L, int idx) {
		lua_TValue o = index2adr(L, idx);
		switch (ttype(o)) {
		case LUA_TTABLE:
			return hvalue(o);
		case LUA_TFUNCTION:
			return clvalue(o);
		case LUA_TTHREAD:
			return thvalue(o);
		case LUA_TUSERDATA:
		case LUA_TLIGHTUSERDATA:
			return lua_touserdata(L, idx);
		default:
			return null;
		}
	}

	/*
	 * * push functions (C . stack)
	 */

	public static void lua_pushnil(lua_State L) {
		lua_lock(L);
		setnilvalue(L.top);
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_pushnumber(lua_State L, double n) {
		lua_lock(L);
		setnvalue(L.top, n);
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_pushinteger(lua_State L, int n) {
		lua_lock(L);
		setnvalue(L.top, cast_num(n));
		api_incr_top(L);
		lua_unlock(L);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static void lua_pushlstring (lua_State L, CharPtr
	// s, uint len)
	public static void lua_pushlstring(lua_State L, CharPtr s, int len) {
		lua_lock(L);
		luaC_checkGC(L);
		setsvalue2s(L, L.top, luaS_newlstr(L, s, len));
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_pushstring(lua_State L, CharPtr s) {
		if (s == null) {
			lua_pushnil(L);
		} else {
			lua_pushlstring(L, s, (int) strlen(s));
		}
	}

	public static void lua_pushstring(lua_State L, String sS) {
		CharPtr s = new CharPtr(sS);
		if (s == null) {
			lua_pushnil(L);
		} else {
			lua_pushlstring(L, s, (int) strlen(s));
		}
	}

	public static CharPtr lua_pushvfstring(lua_State L, CharPtr fmt,
			Object[] argp) {
		CharPtr ret;
		lua_lock(L);
		luaC_checkGC(L);
		ret = luaO_pushvfstring(L, fmt, argp);
		lua_unlock(L);
		return ret;
	}

	public static CharPtr lua_pushfstring(lua_State L, CharPtr fmt) {
		CharPtr ret;
		lua_lock(L);
		luaC_checkGC(L);
		ret = luaO_pushvfstring(L, fmt, null);
		lua_unlock(L);
		return ret;
	}

	public static CharPtr lua_pushfstring(lua_State L, String fmt) {
		CharPtr ret;
		lua_lock(L);
		luaC_checkGC(L);
		ret = luaO_pushvfstring(L, new CharPtr(fmt), null);
		lua_unlock(L);
		return ret;
	}

	public static CharPtr lua_pushfstring(lua_State L, CharPtr fmt, Object... p) {
		CharPtr ret;
		lua_lock(L);
		luaC_checkGC(L);
		ret = luaO_pushvfstring(L, fmt, p);
		lua_unlock(L);
		return ret;
	}

	public static CharPtr lua_pushfstring(lua_State L, String fmt, Object... p) {
		CharPtr ret;
		lua_lock(L);
		luaC_checkGC(L);
		ret = luaO_pushvfstring(L, new CharPtr(fmt), p);
		lua_unlock(L);
		return ret;
	}

	public static void lua_pushcclosure(lua_State L, IDelegate fn, int n) {
		Closure cl;
		lua_lock(L);
		luaC_checkGC(L);
		api_checknelems(L, n);
		cl = luaF_newCclosure(L, n, getcurrenv(L));
		cl.c.f = fn;
		L.top = lua_TValue.OpSubtraction(L.top, n);// L.top -= n;
		while (n-- != 0) {
			setobj2n(L, cl.c.upvalue[n], Lua.lua_TValue.OpAddition(L.top, n));
		}
		setclvalue(L, L.top, cl);
		lua_assert(iswhite(obj2gco(cl)));
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_pushboolean(lua_State L, int b) {
		lua_lock(L);
		setbvalue(L.top, (b != 0) ? 1 : 0); // ensure that true is 1
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_pushboolean(lua_State L, boolean b) {
		lua_lock(L);
		setbvalue(L.top, (b != false) ? 1 : 0); // ensure that true is 1
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_pushlightuserdata(lua_State L, Object p) {
		lua_lock(L);
		setpvalue(L.top, p);
		api_incr_top(L);
		lua_unlock(L);
	}

	public static int lua_pushthread(lua_State L) {
		lua_lock(L);
		setthvalue(L, L.top, L);
		api_incr_top(L);
		lua_unlock(L);
		return (G(L).mainthread == L) ? 1 : 0;
	}

	/*
	 * * get functions (Lua . stack)
	 */

	public static void lua_gettable(lua_State L, int idx) {
		lua_TValue t;
		lua_lock(L);
		t = index2adr(L, idx);
		api_checkvalidindex(L, t);
		luaV_gettable(L, t, Lua.lua_TValue.OpSubtraction(L.top, 1),
				Lua.lua_TValue.OpSubtraction(L.top, 1));
		lua_unlock(L);
	}

	public static void lua_getfield(lua_State L, int idx, CharPtr k) {
		lua_TValue t;
		lua_TValue key = new lua_TValue();
		lua_lock(L);
		t = index2adr(L, idx);
		api_checkvalidindex(L, t);
		setsvalue(L, key, luaS_new(L, k));
		luaV_gettable(L, t, key, L.top);
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_getfield(lua_State L, int idx, String kS) {
		CharPtr k = new CharPtr(kS);
		lua_TValue t;
		lua_TValue key = new lua_TValue();
		lua_lock(L);
		t = index2adr(L, idx);
		api_checkvalidindex(L, t);
		setsvalue(L, key, luaS_new(L, k));
		luaV_gettable(L, t, key, L.top);
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_rawget(lua_State L, int idx) {
		lua_TValue t;
		lua_lock(L);
		t = index2adr(L, idx);
		api_check(L, ttistable(t));
		setobj2s(L, Lua.lua_TValue.OpSubtraction(L.top, 1),
				luaH_get(hvalue(t), Lua.lua_TValue.OpSubtraction(L.top, 1)));
		lua_unlock(L);
	}

	public static void lua_rawgeti(lua_State L, int idx, int n) {
		lua_TValue o;
		lua_lock(L);
		o = index2adr(L, idx);
		api_check(L, ttistable(o));
		setobj2s(L, L.top, luaH_getnum(hvalue(o), n));
		api_incr_top(L);
		lua_unlock(L);
	}

	public static void lua_createtable(lua_State L, int narray, int nrec) {
		lua_lock(L);
		luaC_checkGC(L);
		sethvalue(L, L.top, luaH_new(L, narray, nrec));
		api_incr_top(L);
		lua_unlock(L);
	}

	public static int lua_getmetatable(lua_State L, int objindex) {
		lua_TValue obj;
		Table mt = null;
		int res;
		lua_lock(L);
		obj = index2adr(L, objindex);
		switch (ttype(obj)) {
		case LUA_TTABLE:
			mt = hvalue(obj).metatable;
			break;
		case LUA_TUSERDATA:
			mt = uvalue(obj).metatable;
			break;
		default:
			mt = G(L).mt[ttype(obj)];
			break;
		}
		if (mt == null) {
			res = 0;
		} else {
			sethvalue(L, L.top, mt);
			api_incr_top(L);
			res = 1;
		}
		lua_unlock(L);
		return res;
	}

	public static void lua_getfenv(lua_State L, int idx) {
		lua_TValue o;
		lua_lock(L);
		o = index2adr(L, idx);
		api_checkvalidindex(L, o);
		switch (ttype(o)) {
		case LUA_TFUNCTION:
			sethvalue(L, L.top, clvalue(o).c.getenv());
			break;
		case LUA_TUSERDATA:
			sethvalue(L, L.top, uvalue(o).env);
			break;
		case LUA_TTHREAD:
			setobj2s(L, L.top, gt(thvalue(o)));
			break;
		default:
			setnilvalue(L.top);
			break;
		}
		api_incr_top(L);
		lua_unlock(L);
	}

	/*
	 * * set functions (stack . Lua)
	 */

	public static void lua_settable(lua_State L, int idx) {
		lua_TValue t;
		lua_lock(L);
		api_checknelems(L, 2);
		t = index2adr(L, idx);
		api_checkvalidindex(L, t);
		luaV_settable(L, t, Lua.lua_TValue.OpSubtraction(L.top, 2),
				Lua.lua_TValue.OpSubtraction(L.top, 1));
		L.top = lua_TValue.OpSubtraction(L.top, 2);// L.top -= 2; // pop index
													// and value
		lua_unlock(L);
	}

	public static void lua_setfield(lua_State L, int idx, CharPtr k) {
		lua_TValue t;
		lua_TValue key = new lua_TValue();
		lua_lock(L);
		api_checknelems(L, 1);
		t = index2adr(L, idx);
		api_checkvalidindex(L, t);
		setsvalue(L, key, luaS_new(L, k));
		luaV_settable(L, t, key, Lua.lua_TValue.OpSubtraction(L.top, 1));
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top); // pop value
		L.top = tempRef_top.argvalue;
		lua_unlock(L);
	}

	public static void lua_setfield(lua_State L, int idx, String kS) {
		CharPtr k = new CharPtr(kS);
		lua_TValue t;
		lua_TValue key = new lua_TValue();
		lua_lock(L);
		api_checknelems(L, 1);
		t = index2adr(L, idx);
		api_checkvalidindex(L, t);
		setsvalue(L, key, luaS_new(L, k));
		luaV_settable(L, t, key, Lua.lua_TValue.OpSubtraction(L.top, 1));
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top); // pop value
		L.top = tempRef_top.argvalue;
		lua_unlock(L);
	}

	public static void lua_rawset(lua_State L, int idx) {
		lua_TValue t;
		lua_lock(L);
		api_checknelems(L, 2);
		t = index2adr(L, idx);
		api_check(L, ttistable(t));
		setobj2t(L,
				luaH_set(L, hvalue(t), Lua.lua_TValue.OpSubtraction(L.top, 2)),
				Lua.lua_TValue.OpSubtraction(L.top, 1));
		luaC_barriert(L, hvalue(t), Lua.lua_TValue.OpSubtraction(L.top, 1));
		L.top = lua_TValue.OpSubtraction(L.top, 2);// L.top -= 2;
		lua_unlock(L);
	}

	public static void lua_rawseti(lua_State L, int idx, int n) {
		lua_TValue o;
		lua_lock(L);
		api_checknelems(L, 1);
		o = index2adr(L, idx);
		api_check(L, ttistable(o));
		setobj2t(L, luaH_setnum(L, hvalue(o), n),
				Lua.lua_TValue.OpSubtraction(L.top, 1));
		luaC_barriert(L, hvalue(o), Lua.lua_TValue.OpSubtraction(L.top, 1));
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top);
		L.top = tempRef_top.argvalue;
		lua_unlock(L);
	}

	public static int lua_setmetatable(lua_State L, int objindex) {
		lua_TValue obj;
		Table mt;
		lua_lock(L);
		api_checknelems(L, 1);
		obj = index2adr(L, objindex);
		api_checkvalidindex(L, obj);
		if (ttisnil(Lua.lua_TValue.OpSubtraction(L.top, 1))) {
			mt = null;
		} else {
			api_check(L, ttistable(Lua.lua_TValue.OpSubtraction(L.top, 1)));
			mt = hvalue(Lua.lua_TValue.OpSubtraction(L.top, 1));
		}
		switch (ttype(obj)) {
		case LUA_TTABLE: {
			hvalue(obj).metatable = mt;
			if (mt != null) {
				luaC_objbarriert(L, hvalue(obj), mt);
			}
			break;
		}
		case LUA_TUSERDATA: {
			uvalue(obj).metatable = mt;
			if (mt != null) {
				luaC_objbarrier(L, rawuvalue(obj), mt);
			}
			break;
		}
		default: {
			G(L).mt[ttype(obj)] = mt;
			break;
		}
		}
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top);
		L.top = tempRef_top.argvalue;
		lua_unlock(L);
		return 1;
	}

	public static int lua_setfenv(lua_State L, int idx) {
		lua_TValue o;
		int res = 1;
		lua_lock(L);
		api_checknelems(L, 1);
		o = index2adr(L, idx);
		api_checkvalidindex(L, o);
		api_check(L, ttistable(Lua.lua_TValue.OpSubtraction(L.top, 1)));
		switch (ttype(o)) {
		case LUA_TFUNCTION:
			clvalue(o).c.setenv(hvalue(Lua.lua_TValue.OpSubtraction(L.top, 1)));
			break;
		case LUA_TUSERDATA:
			uvalue(o).env = hvalue(Lua.lua_TValue.OpSubtraction(L.top, 1));
			break;
		case LUA_TTHREAD:
			sethvalue(L, gt(thvalue(o)),
					hvalue(Lua.lua_TValue.OpSubtraction(L.top, 1)));
			break;
		default:
			res = 0;
			break;
		}
		if (res != 0) {
			luaC_objbarrier(L, gcvalue(o),
					hvalue(Lua.lua_TValue.OpSubtraction(L.top, 1)));
		}
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top);
		L.top = tempRef_top.argvalue;
		lua_unlock(L);
		return res;
	}

	/*
	 * * `load' and `call' functions (run Lua code)
	 */

	public static void adjustresults(lua_State L, int nres) {
		if (nres == LUA_MULTRET
				&& Lua.lua_TValue.OpGreaterThanOrEqual(L.top, L.ci.top)) {
			L.ci.top = L.top;
		}
	}

	public static void checkresults(lua_State L, int na, int nr) {
		api_check(
				L,
				(nr) == LUA_MULTRET
						|| (Lua.lua_TValue.OpSubtraction(L.ci.top, L.top) >= (nr)
								- (na)));
	}

	public static void lua_call(lua_State L, int nargs, int nresults) {
		lua_TValue func;
		lua_lock(L);
		api_checknelems(L, nargs + 1);
		checkresults(L, nargs, nresults);
		func = Lua.lua_TValue.OpSubtraction(L.top, (nargs + 1));
		luaD_call(L, func, nresults);
		adjustresults(L, nresults);
		lua_unlock(L);
	}

	/*
	 * * Execute a protected call.
	 */
	public static class CallS // data to `f_call'
	{
		public lua_TValue func;
		public int nresults;
	}

	public static void f_call(lua_State L, Object ud) {
		CallS c = (CallS) ((ud instanceof CallS) ? ud : null);
		luaD_call(L, c.func, c.nresults);
	}

	// static IDelegate f_call = Pfunc.build(Lua.class, "f_call");
	static IDelegate f_call = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			f_call((lua_State) arg1, arg2);
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int lua_pcall(lua_State L, int nargs, int nresults,
			int errfunc) {
		CallS c = new CallS();
		int status;
		int func;
		lua_lock(L);
		api_checknelems(L, nargs + 1);
		checkresults(L, nargs, nresults);
		if (errfunc == 0) {
			func = 0;
		} else {
			lua_TValue o = index2adr(L, errfunc);
			api_checkvalidindex(L, o);
			func = savestack(L, o);
		}
		c.func = Lua.lua_TValue.OpSubtraction(L.top, (nargs + 1)); // function
																	// to be
																	// called
		c.nresults = nresults;
		status = luaD_pcall(L, f_call, c, savestack(L, c.func), func);
		adjustresults(L, nresults);
		lua_unlock(L);
		return status;
	}

	/*
	 * * Execute a protected C call.
	 */
	public static class CCallS // data to `f_Ccall'
	{
		public IDelegate func;// public lua_CFunction func;
		public Object ud;
	}

	public static void f_Ccall(lua_State L, Object ud) {
		CCallS c = (CCallS) ((ud instanceof CCallS) ? ud : null);
		Closure cl;
		cl = luaF_newCclosure(L, 0, getcurrenv(L));
		cl.c.f = c.func;
		setclvalue(L, L.top, cl); // push function
		api_incr_top(L);
		setpvalue(L.top, c.ud); // push only argument
		api_incr_top(L);
		luaD_call(L, Lua.lua_TValue.OpSubtraction(L.top, 2), 0);
	}

	// static IDelegate f_Ccall = Pfunc.build(Lua.class, "f_Ccall");
	static IDelegate f_Ccall = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			f_Ccall((lua_State) arg1, arg2);
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int lua_cpcall(lua_State L, IDelegate func, Object ud) {
		CCallS c = new CCallS();
		int status;
		lua_lock(L);
		c.func = func;
		c.ud = ud;
		status = luaD_pcall(L, f_Ccall, c, savestack(L, L.top), 0);
		lua_unlock(L);
		return status;
	}

	public static int lua_load(lua_State L, IDelegate reader, Object data,
			CharPtr chunkname) {
		Zio z = new Zio();
		int status;
		lua_lock(L);
		if (chunkname == null) {
			chunkname = new CharPtr("?");
		}
		luaZ_init(L, z, reader, data);
		status = luaD_protectedparser(L, z, chunkname);
		lua_unlock(L);
		return status;
	}

	public static int lua_dump(lua_State L, IDelegate writer, Object data) {
		int status;
		lua_TValue o;
		lua_lock(L);
		api_checknelems(L, 1);
		o = Lua.lua_TValue.OpSubtraction(L.top, 1);
		if (isLfunction(o)) {
			status = luaU_dump(L, clvalue(o).l.p, writer, data, 0);
		} else {
			status = 1;
		}
		lua_unlock(L);
		return status;
	}

	public static int lua_status(lua_State L) {
		return L.status;
	}

	/*
	 * * Garbage-collection function
	 */

	public static int lua_gc(lua_State L, int what, int data) {
		int res = 0;
		global_State g;
		lua_lock(L);
		g = G(L);
		switch (what) {
		case LUA_GCSTOP: {
			g.GCthreshold = MAX_LUMEM;
			break;
		}
		case LUA_GCRESTART: {
			g.GCthreshold = g.totalbytes;
			break;
		}
		case LUA_GCCOLLECT: {
			luaC_fullgc(L);
			break;
		}
		case LUA_GCCOUNT: {
			/* GC values are expressed in Kbytes: #bytes/2^10 */
			res = cast_int(g.totalbytes >> 10);
			break;
		}
		case LUA_GCCOUNTB: {
			res = cast_int(g.totalbytes & 0x3ff);
			break;
		}
		case LUA_GCSTEP: {
			int a = ((int) data << 10);
			if (a <= g.totalbytes) {
				g.GCthreshold = (int) (g.totalbytes - a);
			} else {
				g.GCthreshold = 0;
			}
			while (g.GCthreshold <= g.totalbytes) {
				luaC_step(L);
				if (g.gcstate == GCSpause) // end of cycle?
				{
					res = 1; // signal it
					break;
				}
			}
			break;
		}
		case LUA_GCSETPAUSE: {
			res = g.gcpause;
			g.gcpause = data;
			break;
		}
		case LUA_GCSETSTEPMUL: {
			res = g.gcstepmul;
			g.gcstepmul = data;
			break;
		}
		default:
			res = -1; // invalid option
			break;
		}
		lua_unlock(L);
		return res;
	}

	/*
	 * * miscellaneous functions
	 */

	public static int lua_error(lua_State L) {
		lua_lock(L);
		api_checknelems(L, 1);
		luaG_errormsg(L);
		lua_unlock(L);
		return 0; // to avoid warnings
	}

	public static int lua_next(lua_State L, int idx) {
		lua_TValue t;
		int more;
		lua_lock(L);
		t = index2adr(L, idx);
		api_check(L, ttistable(t));
		more = luaH_next(L, hvalue(t), Lua.lua_TValue.OpSubtraction(L.top, 1));
		if (more != 0) {
			api_incr_top(L);
		} else // no more elements
		{
			RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
					L.top);
			lua_TValue.dec(tempRef_top); // remove key
			L.top = tempRef_top.argvalue;
		}
		lua_unlock(L);
		return more;
	}

	public static void lua_concat(lua_State L, int n) {
		lua_lock(L);
		api_checknelems(L, n);
		if (n >= 2) {
			luaC_checkGC(L);
			luaV_concat(L, n,
					cast_int(Lua.lua_TValue.OpSubtraction(L.top, L.base_)) - 1);
			L.top = lua_TValue.OpSubtraction(L.top, (n - 1));// L.top -= (n-1);
		} else if (n == 0) // push empty string
		{
			setsvalue2s(L, L.top, luaS_newlstr(L, new CharPtr(""), 0));
			api_incr_top(L);
		}
		/* else n == 1; nothing to do */
		lua_unlock(L);
	}

	public static IDelegate lua_getallocf(lua_State L, RefObject<Object> ud) {
		IDelegate f;// lua_Alloc f;
		lua_lock(L);
		if (ud.argvalue != null) {
			ud.argvalue = G(L).ud;
		}
		f = G(L).frealloc;
		lua_unlock(L);
		return f;
	}

	public static void lua_setallocf(lua_State L, IDelegate f, Object ud) {
		lua_lock(L);
		G(L).ud = ud;
		G(L).frealloc = f;
		lua_unlock(L);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static object lua_newuserdata(lua_State L, uint
	// size)
	public static Object lua_newuserdata(lua_State L, int size) {
		Udata u;
		lua_lock(L);
		luaC_checkGC(L);
		u = luaS_newudata(L, size, getcurrenv(L));
		setuvalue(L, L.top, u);
		api_incr_top(L);
		lua_unlock(L);
		return u.user_data;
	}

	// this one is used internally only
	public static Object lua_newuserdata(lua_State L, java.lang.Class t) {
		Udata u;
		lua_lock(L);
		luaC_checkGC(L);
		u = luaS_newudata(L, t, getcurrenv(L));
		setuvalue(L, L.top, u);
		api_incr_top(L);
		lua_unlock(L);
		return u.user_data;
	}

	public static CharPtr aux_upvalue(lua_TValue fi, int n,
			RefObject<lua_TValue> val) {
		Closure f;
		if (!ttisfunction(fi)) {
			return null;
		}
		f = clvalue(fi);
		if (f.c.getisC() != 0) {
			if (!(1 <= n && n <= f.c.getnupvalues())) {
				return null;
			}
			val.argvalue = f.c.upvalue[n - 1];
			return new CharPtr("");
		} else {
			Proto p = f.l.p;
			if (!(1 <= n && n <= p.sizeupvalues)) {
				return null;
			}
			val.argvalue = f.l.upvals[n - 1].v;
			return getstr(p.upvalues[n - 1]);
		}
	}

	public static CharPtr lua_getupvalue(lua_State L, int funcindex, int n) {
		CharPtr name;
		lua_TValue val = new lua_TValue();
		lua_lock(L);
		RefObject<Lua.lua_TValue> tempRef_val = new RefObject<Lua.lua_TValue>(
				val);
		name = aux_upvalue(index2adr(L, funcindex), n, tempRef_val);
		val = tempRef_val.argvalue;
		if (name != null) {
			setobj2s(L, L.top, val);
			api_incr_top(L);
		}
		lua_unlock(L);
		return name;
	}

	public static CharPtr lua_setupvalue(lua_State L, int funcindex, int n) {
		CharPtr name;
		lua_TValue val = new lua_TValue();
		lua_TValue fi;
		lua_lock(L);
		fi = index2adr(L, funcindex);
		api_checknelems(L, 1);
		RefObject<Lua.lua_TValue> tempRef_val = new RefObject<Lua.lua_TValue>(
				val);
		name = aux_upvalue(fi, n, tempRef_val);
		val = tempRef_val.argvalue;
		if (name != null) {
			RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
					L.top);
			lua_TValue.dec(tempRef_top);
			L.top = tempRef_top.argvalue;
			setobj(L, val, L.top);
			luaC_barrier(L, clvalue(fi), L.top);
		}
		lua_unlock(L);
		return name;
	}

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_COMPAT_GETN
	/*
	 * public static int luaL_getn(lua_State L, int t); public static void
	 * luaL_setn(lua_State L, int t, int n);
	 */
	// #else
	public static int luaL_getn(lua_State L, int i) {
		return (int) lua_objlen(L, i);
	}

	public static void luaL_setn(lua_State L, int i, int j) // no op!
	{
	}

	// #endif

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_COMPAT_OPENLIB
	// /#define luaI_openlib luaL_openlib
	// #endif

	/* extra error code for `luaL_load' */
	public static final int LUA_ERRFILE = (LUA_ERRERR + 1);

	public static class luaL_Reg {

		private CharPtr name;
		private IDelegate func;
		private String funcName;

		public luaL_Reg(String strName, String javaFunction) {
			if (strName != null) {
				name = new CharPtr(strName);
				func = lua_CFunction.build(Lua.class, javaFunction);
			} else
				name = null;
			funcName = javaFunction;
		}
		
		public luaL_Reg(String strName, IDelegate javaFunction) {
			if (strName != null) {
				name = new CharPtr(strName);
				func = javaFunction;
			} else
				name = null;
			funcName = strName;
		}

		public final String GetFunctionName() {
			return this.funcName;
		}

		public final IDelegate GetJavaFunction() {
			return this.func;
		}
	}

	/*
	 * * ===============================================================* some
	 * useful macros*
	 * ===============================================================
	 */

	public static void luaL_argcheck(lua_State L, boolean cond, int numarg,
			String extramsg) {
		if (!cond) {
			luaL_argerror(L, numarg, new CharPtr(extramsg));
		}
	}

	public static CharPtr luaL_checkstring(lua_State L, int n) {
		return luaL_checklstring(L, n);
	}

	public static CharPtr luaL_optstring(lua_State L, int n, CharPtr d) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint len;
		int len = 0;
		RefObject<Integer> tempRef_len = new RefObject<Integer>(len);
		CharPtr tempVar = luaL_optlstring(L, n, d, tempRef_len);
		len = tempRef_len.argvalue;
		return tempVar;
	}

	public static CharPtr luaL_optstring(lua_State L, int n, String dS) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint len;
		int len = 0;
		CharPtr d = new CharPtr(dS);
		RefObject<Integer> tempRef_len = new RefObject<Integer>(len);
		CharPtr tempVar = luaL_optlstring(L, n, d, tempRef_len);
		len = tempRef_len.argvalue;
		return tempVar;
	}

	public static int luaL_checkint(lua_State L, int n) {
		return (int) luaL_checkinteger(L, n);
	}

	public static int luaL_optint(lua_State L, int n, int d) {
		return (int) luaL_optinteger(L, n, d);
	}

	public static long luaL_checklong(lua_State L, int n) {
		return luaL_checkinteger(L, n);
	}

	public static long luaL_optlong(lua_State L, int n, int d) {
		return luaL_optinteger(L, n, d);
	}

	public static CharPtr luaL_typename(lua_State L, int i) {
		return lua_typename(L, lua_type(L, i));
	}

	// /#define luaL_dofile(L, fn) \
	// (luaL_loadfile(L, fn) || lua_pcall(L, 0, LUA_MULTRET, 0))

	// /#define luaL_dostring(L, s) \
	// (luaL_loadstring(L, s) || lua_pcall(L, 0, LUA_MULTRET, 0))

	public static void luaL_getmetatable(lua_State L, CharPtr n) {
		lua_getfield(L, LUA_REGISTRYINDEX, n);
	}

	public static void luaL_getmetatable(lua_State L, String n) {
		lua_getfield(L, LUA_REGISTRYINDEX, n);
	}

	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate double luaL_opt_delegate (lua_State L, int narg);
	public static final Delegator luaL_opt_delegate = new Delegator(
			new Class[] { lua_State.class, int.class }, Double.TYPE);

	// public static double luaL_opt(lua_State L, luaL_opt_delegate f, int n,
	// double d)
	public static double luaL_opt(lua_State L, IDelegate f, int n, double d) {
		// return lua_isnoneornil(L, (n != 0) ? d : f(L, n)) ? 1 : 0;
		/*return lua_isnoneornil(L, (n != 0) ? d : (Double) f.invoke(L, n)) ? 1
				: 0;*/
		return lua_isnoneornil(L, n) ? d : (Double) f.invoke(L, n);
	}

	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate int luaL_opt_delegate_integer(lua_State L, int narg);
	public final static Delegator luaL_opt_delegate_integer = new Delegator(
			new Class[] { lua_State.class, int.class }, Integer.TYPE);

	// public static int luaL_opt_integer(lua_State L, luaL_opt_delegate_integer
	// f, int n, double d)
	public static int luaL_opt_integer(lua_State L, IDelegate f, int n, double d) {
		return (int) (lua_isnoneornil(L, n) ? d : ((Integer) f.invoke(L, (n))));
	}

	/*
	 * * {======================================================* Generic Buffer
	 * manipulation* =======================================================
	 */

	public static class luaL_Buffer {
		public int p; // current position in buffer
		public int lvl; // number of strings in the stack (level)
		public lua_State L;
		public CharPtr buffer = new CharPtr(new char[LUAL_BUFFERSIZE]);
	}

	public static void luaL_addchar(luaL_Buffer B, char c) {
		if (B.p >= LUAL_BUFFERSIZE) {
			luaL_prepbuffer(B);
		}
		B.buffer.setItem(B.p++, c);
	}

	/*** compatibility only */
	public static void luaL_putchar(luaL_Buffer B, char c) {
		luaL_addchar(B, c);
	}

	public static void luaL_addsize(luaL_Buffer B, int n) {
		B.p += n;
	}

	/* }====================================================== */

	/* compatibility with ref system */

	/* pre-defined references */
	public static final int LUA_NOREF = (-2);
	public static final int LUA_REFNIL = (-1);

	// /#define lua_ref(L,lock) ((lock) ? luaL_ref(L, LUA_REGISTRYINDEX) : \
	// (lua_pushstring(L, "unlocked references are obsolete"), lua_error(L), 0))

	// /#define lua_unref(L,ref) luaL_unref(L, LUA_REGISTRYINDEX, (ref))

	// /#define lua_getref(L,ref) lua_rawgeti(L, LUA_REGISTRYINDEX, (ref))

	// /#define luaL_reg luaL_Reg

	/*
	 * This file uses only the official API of Lua.* Any function declared here
	 * could be written as an application function.
	 */

	// /#define lauxlib_c
	// /#define LUA_LIB

	public static final int FREELIST_REF = 0; // free list of references

	/* convert a stack index to positive */
	public static int abs_index(lua_State L, int i) {
		return ((i) > 0 || (i) <= LUA_REGISTRYINDEX ? (i) : lua_gettop(L) + (i)
				+ 1);
	}

	/*
	 * * {======================================================* Error-report
	 * functions* =======================================================
	 */

	public static int luaL_argerror(lua_State L, int narg, CharPtr extramsg) {
		lua_Debug ar = new lua_Debug();
		if (lua_getstack(L, 0, ar) == 0) // no stack frame?
		{
			return luaL_error(L, new CharPtr("bad argument #%d (%s)"), narg,
					extramsg);
		}
		lua_getinfo(L, new CharPtr("n"), ar);
		if (strcmp(ar.namewhat, new CharPtr("method")) == 0) {
			narg--; // do not count `self'
			if (narg == 0) // error is in the self argument itself?
			{
				return luaL_error(L, new CharPtr("calling "
						+ getLUA_QS().toString() + " on bad self ({1})"),
						ar.name, extramsg);
			}
		}
		if (ar.name == null) {
			ar.name = new CharPtr("?");
		}
		return luaL_error(L, new CharPtr("bad argument #%d to "
				+ getLUA_QS().toString() + " (%s)"), narg, ar.name, extramsg);
	}

	public static int luaL_argerror(lua_State L, int narg, String extramsgS) {
		CharPtr extramsg = new CharPtr(extramsgS);
		lua_Debug ar = new lua_Debug();
		if (lua_getstack(L, 0, ar) == 0) // no stack frame?
		{
			return luaL_error(L, new CharPtr("bad argument #%d (%s)"), narg,
					extramsg);
		}
		lua_getinfo(L, new CharPtr("n"), ar);
		if (strcmp(ar.namewhat, new CharPtr("method")) == 0) {
			narg--; // do not count `self'
			if (narg == 0) // error is in the self argument itself?
			{
				return luaL_error(L, new CharPtr("calling "
						+ getLUA_QS().toString() + " on bad self ({1})"),
						ar.name, extramsg);
			}
		}
		if (ar.name == null) {
			ar.name = new CharPtr("?");
		}
		return luaL_error(L, new CharPtr("bad argument #%d to "
				+ getLUA_QS().toString() + " (%s)"), narg, ar.name, extramsg);
	}

	public static int luaL_typerror(lua_State L, int narg, CharPtr tname) {
		CharPtr msg = lua_pushfstring(L, new CharPtr("%s expected, got %s"),
				tname, luaL_typename(L, narg));
		return luaL_argerror(L, narg, msg);
	}

	public static void tag_error(lua_State L, int narg, int tag) {
		luaL_typerror(L, narg, lua_typename(L, tag));
	}

	public static void luaL_where(lua_State L, int level) {
		lua_Debug ar = new lua_Debug();
		if (lua_getstack(L, level, ar) != 0) // check function at level
		{
			lua_getinfo(L, new CharPtr("Sl"), ar); // get info about it
			if (ar.currentline > 0) // is there info?
			{
				lua_pushfstring(L, new CharPtr("%s:%d: "), ar.short_src,
						ar.currentline);
				return;
			}
		}
		lua_pushliteral(L, new CharPtr("")); // else, no information
												// available...
	}

	public static int luaL_error(lua_State L, CharPtr fmt, Object... p) {
		luaL_where(L, 1);
		lua_pushvfstring(L, fmt, p);
		lua_concat(L, 2);
		return lua_error(L);
	}

	public static int luaL_error(lua_State L, String fmtS, Object... p) {
		CharPtr fmt = new CharPtr(fmtS);
		luaL_where(L, 1);
		lua_pushvfstring(L, fmt, p);
		lua_concat(L, 2);
		return lua_error(L);
	}

	/* }====================================================== */

	public static int luaL_checkoption(lua_State L, int narg, CharPtr def,
			CharPtr[] lst) {
		CharPtr name = (def != null) ? luaL_optstring(L, narg, def)
				: luaL_checkstring(L, narg);
		int i;
		for (i = 0; i < lst.length; i++) {
			if (strcmp(lst[i], name) == 0) {
				return i;
			}
		}
		return luaL_argerror(
				L,
				narg,
				lua_pushfstring(L, new CharPtr("invalid option "
						+ getLUA_QS().toString()), name));
	}

	public static int luaL_checkoption(lua_State L, int narg, String defS,
			CharPtr[] lst) {
		CharPtr def = new CharPtr(defS);
		CharPtr name = (def != null) ? luaL_optstring(L, narg, def)
				: luaL_checkstring(L, narg);
		int i;
		for (i = 0; i < lst.length; i++) {
			if (strcmp(lst[i], name) == 0) {
				return i;
			}
		}
		return luaL_argerror(
				L,
				narg,
				lua_pushfstring(L, new CharPtr("invalid option "
						+ getLUA_QS().toString()), name));
	}

	public static int luaL_newmetatable(lua_State L, CharPtr tname) {
		lua_getfield(L, LUA_REGISTRYINDEX, tname); // get registry.name
		if (!lua_isnil(L, -1)) // name already in use?
		{
			return 0; // leave previous value on top, but return 0
		}
		lua_pop(L, 1);
		lua_newtable(L); // create metatable
		lua_pushvalue(L, -1);
		lua_setfield(L, LUA_REGISTRYINDEX, tname); // registry.name = metatable
		return 1;
	}

	public static int luaL_newmetatable(lua_State L, String tname) {
		lua_getfield(L, LUA_REGISTRYINDEX, tname); // get registry.name
		if (!lua_isnil(L, -1)) // name already in use?
		{
			return 0; // leave previous value on top, but return 0
		}
		lua_pop(L, 1);
		lua_newtable(L); // create metatable
		lua_pushvalue(L, -1);
		lua_setfield(L, LUA_REGISTRYINDEX, tname); // registry.name = metatable
		return 1;
	}

	public static Object luaL_checkudata(lua_State L, int ud, CharPtr tname) {
		Object p = lua_touserdata(L, ud);
		if (p != null) // value is a userdata?
		{
			if (lua_getmetatable(L, ud) != 0) // does it have a metatable?
			{
				lua_getfield(L, LUA_REGISTRYINDEX, tname); // get correct
															// metatable
				if (lua_rawequal(L, -1, -2) != 0) // does it have the correct
													// mt?
				{
					lua_pop(L, 2); // remove both metatables
					return p;
				}
			}
		}
		luaL_typerror(L, ud, tname); // else error
		return null; // to avoid warnings
	}

	public static Object luaL_checkudata(lua_State L, int ud, String tnameS) {
		CharPtr tname = new CharPtr(tnameS);
		Object p = lua_touserdata(L, ud);
		if (p != null) // value is a userdata?
		{
			if (lua_getmetatable(L, ud) != 0) // does it have a metatable?
			{
				lua_getfield(L, LUA_REGISTRYINDEX, tname); // get correct
															// metatable
				if (lua_rawequal(L, -1, -2) != 0) // does it have the correct
													// mt?
				{
					lua_pop(L, 2); // remove both metatables
					return p;
				}
			}
		}
		luaL_typerror(L, ud, tname); // else error
		return null; // to avoid warnings
	}

	public static void luaL_checkstack(lua_State L, int space, CharPtr mes) {
		if (lua_checkstack(L, space) == 0) {
			luaL_error(L, new CharPtr("stack overflow (%s)"), mes);
		}
	}

	public static void luaL_checkstack(lua_State L, int space, String mes) {
		if (lua_checkstack(L, space) == 0) {
			luaL_error(L, new CharPtr("stack overflow (%s)"), new CharPtr(mes));
		}
	}

	public static void luaL_checktype(lua_State L, int narg, int t) {
		if (lua_type(L, narg) != t) {
			tag_error(L, narg, t);
		}
	}

	public static void luaL_checkany(lua_State L, int narg) {
		if (lua_type(L, narg) == LUA_TNONE) {
			luaL_argerror(L, narg, new CharPtr("value expected"));
		}
	}

	public static CharPtr luaL_checklstring(lua_State L, int narg) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint len;
		int len = 0;
		RefObject<Integer> tempRef_len = new RefObject<Integer>(len);
		CharPtr tempVar = luaL_checklstring(L, narg, tempRef_len);
		len = tempRef_len.argvalue;
		return tempVar;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static CharPtr luaL_checklstring (lua_State L, int
	// narg, out uint len)
	public static CharPtr luaL_checklstring(lua_State L, int narg,
			RefObject<Integer> len) {
		CharPtr s = lua_tolstring(L, narg, len);
		if (s == null) {
			tag_error(L, narg, LUA_TSTRING);
		}
		return s;
	}

	public static CharPtr luaL_optlstring(lua_State L, int narg, CharPtr def) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint len;
		int len = 0;
		RefObject<Integer> tempRef_len = new RefObject<Integer>(len);
		CharPtr tempVar = luaL_optlstring(L, narg, def, tempRef_len);
		len = tempRef_len.argvalue;
		return tempVar;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static CharPtr luaL_optlstring (lua_State L, int
	// narg, CharPtr def, out uint len)
	public static CharPtr luaL_optlstring(lua_State L, int narg, CharPtr def,
			RefObject<Integer> len) {
		if (lua_isnoneornil(L, narg)) {
			len.argvalue = (int) ((def != null) ? strlen(def) : 0);
			return def;
		} else {
			return luaL_checklstring(L, narg, len);
		}
	}

	public static CharPtr luaL_optlstring(lua_State L, int narg, String defS,
			RefObject<Integer> len) {
		CharPtr def = new CharPtr(defS);
		if (lua_isnoneornil(L, narg)) {
			len.argvalue = (int) ((def != null) ? strlen(def) : 0);
			return def;
		} else {
			return luaL_checklstring(L, narg, len);
		}
	}

	public static double luaL_checknumber(lua_State L, int narg) {
		double d = lua_tonumber(L, narg);
		if ((d == 0) && (lua_isnumber(L, narg) == 0)) // avoid extra test when d
														// is not 0
		{
			tag_error(L, narg, LUA_TNUMBER);
		}
		return d;
	}

	// static IDelegate luaL_checknumber = luaL_opt_delegate.build(Lua.class,
	// "luaL_checknumber");
	static IDelegate luaL_checknumber = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luaL_checknumber((lua_State) arg1, (Integer) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static double luaL_optnumber(lua_State L, int narg, double def) {
		return luaL_opt(L, luaL_checknumber, narg, def);
	}

	public static int luaL_checkinteger(lua_State L, int narg) {
		int d = lua_tointeger(L, narg);
		if (d == 0 && lua_isnumber(L, narg) == 0) // avoid extra test when d is
													// not 0
		{
			tag_error(L, narg, LUA_TNUMBER);
		}
		return d;
	}

	// static IDelegate luaL_checkinteger =
	// luaL_opt_delegate_integer.build(Lua.class, "luaL_checkinteger");
	static IDelegate luaL_checkinteger = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luaL_checkinteger((lua_State) arg1, (Integer) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int luaL_optinteger(lua_State L, int narg, int def) {
		return luaL_opt_integer(L, luaL_checkinteger, narg, def);
	}

	public static int luaL_getmetafield(lua_State L, int obj, CharPtr event_) {
		if (lua_getmetatable(L, obj) == 0) // no metatable?
		{
			return 0;
		}
		lua_pushstring(L, event_);
		lua_rawget(L, -2);
		if (lua_isnil(L, -1)) {
			lua_pop(L, 2); // remove metatable and metafield
			return 0;
		} else {
			lua_remove(L, -2); // remove only metatable
			return 1;
		}
	}

	public static int luaL_callmeta(lua_State L, int obj, CharPtr event_) {
		obj = abs_index(L, obj);
		if (luaL_getmetafield(L, obj, event_) == 0) // no metafield?
		{
			return 0;
		}
		lua_pushvalue(L, obj);
		lua_call(L, 1, 1);
		return 1;
	}

	public static void luaL_register(lua_State L, CharPtr libname, luaL_Reg[] l) {
		luaI_openlib(L, libname, l, 0);
	}

	public static void luaL_register(lua_State L, String libname, luaL_Reg[] l) {
		luaI_openlib(L, new CharPtr(libname), l, 0);
	}

	// we could just take the .Length member here, but let's try
	// to keep it as close to the C implementation as possible.
	public static int libsize(luaL_Reg[] l) {
		int size = 0;
		for (; l[size].name != null; size++) {
			;
		}
		return size;
	}

	public static void luaI_openlib(lua_State L, CharPtr libname, luaL_Reg[] l,
			int nup) {
		if (libname != null) {
			int size = libsize(l);
			/* check whether lib already exists */
			luaL_findtable(L, LUA_REGISTRYINDEX, new CharPtr("_LOADED"), 1);
			lua_getfield(L, -1, libname); // get _LOADED[libname]
			if (!lua_istable(L, -1)) // not found?
			{
				lua_pop(L, 1); // remove previous result
				/* try global variable (and create one if it does not exist) */
				if (luaL_findtable(L, LUA_GLOBALSINDEX, libname, size) != null) {
					luaL_error(L, new CharPtr("name conflict for module "
							+ getLUA_QS().toString()), libname);
				}
				lua_pushvalue(L, -1);
				lua_setfield(L, -3, libname); // _LOADED[libname] = new table
			}
			lua_remove(L, -2); // remove _LOADED table
			lua_insert(L, -(nup + 1)); // move library table to below upvalues
		}
		int reg_num = 0;
		for (; l[reg_num].name != null; reg_num++) {
			int i;
			for (i = 0; i < nup; i++) // copy upvalues to the top
			{
				lua_pushvalue(L, -nup);
			}
			lua_pushcclosure(L, l[reg_num].func, nup);
			lua_setfield(L, -(nup + 2), l[reg_num].name);
		}
		lua_pop(L, nup); // remove upvalues
	}

	/*
	 * * {======================================================* getn-setn:
	 * size for arrays* =======================================================
	 */

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_COMPAT_GETN

	/*
	 * public static int checkint(lua_State L, int topop) { int n = (lua_type(L,
	 * -1) == LUA_TNUMBER) ? lua_tointeger(L, -1) : -1; lua_pop(L, topop);
	 * return n; }
	 * 
	 * 
	 * public static void getsizes(lua_State L) { lua_getfield(L,
	 * LUA_REGISTRYINDEX, "LUA_SIZES"); if (lua_isnil(L, -1)) // no `size'
	 * table? { lua_pop(L, 1); // remove nil lua_newtable(L); // create it
	 * lua_pushvalue(L, -1); // `size' will be its own metatable
	 * lua_setmetatable(L, -2); lua_pushliteral(L, "kv"); lua_setfield(L, -2,
	 * "__mode"); // metatable(N).__mode = "kv" lua_pushvalue(L, -1);
	 * lua_setfield(L, LUA_REGISTRYINDEX, "LUA_SIZES"); // store in register } }
	 * 
	 * 
	 * public static void luaL_setn(lua_State L, int t, int n) { t =
	 * abs_index(L, t); lua_pushliteral(L, "n"); lua_rawget(L, t); if
	 * (checkint(L, 1) >= 0) // is there a numeric field `n'? {
	 * lua_pushliteral(L, "n"); // use it lua_pushinteger(L, n); lua_rawset(L,
	 * t); } else // use `sizes' { getsizes(L); lua_pushvalue(L, t);
	 * lua_pushinteger(L, n); lua_rawset(L, -3); // sizes[t] = n lua_pop(L, 1);
	 * // remove `sizes' } }
	 * 
	 * 
	 * public static int luaL_getn(lua_State L, int t) { int n; t = abs_index(L,
	 * t); lua_pushliteral(L, "n"); // try t.n lua_rawget(L, t); if ((n =
	 * checkint(L, 1)) >= 0) { return n; } getsizes(L); // else try sizes[t]
	 * lua_pushvalue(L, t); lua_rawget(L, -2); if ((n = checkint(L, 2)) >= 0) {
	 * return n; } return (int)lua_objlen(L, t); }
	 */

	// #endif

	/* }====================================================== */

	// public static CharPtr luaL_gsub(lua_State L, CharPtr s, CharPtr p,
	// CharPtr r)
	public static CharPtr luaL_gsub(lua_State L, Object sS, Object pS, Object rS) {
		CharPtr s = null;
		CharPtr p = null;
		CharPtr r = null;
		if (sS instanceof String)
			s = new CharPtr((String) sS);
		else
			s = (CharPtr) sS;
		if (pS instanceof String)
			p = new CharPtr((String) pS);
		else
			p = (CharPtr) pS;
		if (rS instanceof String)
			r = new CharPtr((String) rS);
		else
			r = (CharPtr) rS;
		CharPtr wild;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l = (uint)strlen(p);
		int l = (int) strlen(p);
		luaL_Buffer b = new luaL_Buffer();
		luaL_buffinit(L, b);
		while ((wild = strstr(s, p)) != null) {
			luaL_addlstring(b, s, (int) CharPtr.OpSubtraction(wild, s));// luaL_addlstring(b,
																		// s,
																		// (wild
																		// -
																		// s));
																		// //
																		// push
																		// prefix
			luaL_addstring(b, r); // push replacement in place of pattern
			// s = wild + l; // continue after `p'
			s = CharPtr.OpAddition(wild, 1);
		}
		luaL_addstring(b, s); // push last suffix
		luaL_pushresult(b);
		return lua_tostring(L, -1);
	}

	public static CharPtr luaL_findtable(lua_State L, int idx, CharPtr fname,
			int szhint) {
		CharPtr e;
		lua_pushvalue(L, idx);
		do {
			e = strchr(fname, '.');
			if (e == null) {
				e = CharPtr.OpAddition(fname, strlen(fname));// e = fname +
																// strlen(fname);
			}
			lua_pushlstring(L, fname, (int) CharPtr.OpSubtraction(e, fname));
			lua_rawget(L, -2);
			if (lua_isnil(L, -1)) // no such field?
			{
				lua_pop(L, 1); // remove this nil
				lua_createtable(L, 0, (CharPtr.OpEquality(e, '.') ? 1 : szhint)); // new
																					// table
																					// for
																					// field
				lua_pushlstring(L, fname, (int) CharPtr.OpSubtraction(e, fname));// (e
																					// -
																					// fname));
				lua_pushvalue(L, -2);
				lua_settable(L, -4); // set new table into field
			} else if (!lua_istable(L, -1)) // field has a non-table value?
			{
				lua_pop(L, 2); // remove table and value
				return fname; // return problematic part of the name
			}
			lua_remove(L, -2); // remove previous table
			fname = CharPtr.OpAddition(e, 1);// fname = e + 1;
		} while (CharPtr.OpEquality(e, '.'));// } while (e == '.');
		return null;
	}

	public static CharPtr luaL_findtable(lua_State L, int idx, String fnameS,
			int szhint) {
		CharPtr fname = new CharPtr(fnameS);
		CharPtr e;
		lua_pushvalue(L, idx);
		do {
			e = strchr(fname, '.');
			if (e == null) {
				e = CharPtr.OpAddition(fname, strlen(fname));// e = fname +
																// strlen(fname);
			}
			lua_pushlstring(L, fname, (int) CharPtr.OpSubtraction(e, fname));
			lua_rawget(L, -2);
			if (lua_isnil(L, -1)) // no such field?
			{
				lua_pop(L, 1); // remove this nil
				lua_createtable(L, 0, (CharPtr.OpEquality(e, '.') ? 1 : szhint)); // new
																					// table
																					// for
																					// field
				lua_pushlstring(L, fname, (int) CharPtr.OpSubtraction(e, fname));// (e
																					// -
																					// fname));
				lua_pushvalue(L, -2);
				lua_settable(L, -4); // set new table into field
			} else if (!lua_istable(L, -1)) // field has a non-table value?
			{
				lua_pop(L, 2); // remove table and value
				return fname; // return problematic part of the name
			}
			lua_remove(L, -2); // remove previous table
			fname = CharPtr.OpAddition(e, 1);// fname = e + 1;
		} while (CharPtr.OpEquality(e, '.'));// } while (e == '.');
		return null;
	}

	/*
	 * * {======================================================* Generic Buffer
	 * manipulation* =======================================================
	 */

	public static int bufflen(luaL_Buffer B) {
		return B.p;
	}

	public static int bufffree(luaL_Buffer B) {
		return LUAL_BUFFERSIZE - bufflen(B);
	}

	public static final int LIMIT = LUA_MINSTACK / 2;

	public static int emptybuffer(luaL_Buffer B) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l = (uint)bufflen(B);
		int l = (int) bufflen(B);
		if (l == 0) // put nothing on stack
		{
			return 0;
		} else {
			lua_pushlstring(B.L, B.buffer, l);
			B.p = 0;
			B.lvl++;
			return 1;
		}
	}

	public static void adjuststack(luaL_Buffer B) {
		if (B.lvl > 1) {
			lua_State L = B.L;
			int toget = 1; // number of levels to concat
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint toplen = lua_strlen(L, -1);
			int toplen = lua_strlen(L, -1);
			do {
				// FUCK WARNING: Unsigned integer types have no direct
				// equivalent in Java:
				// ORIGINAL LINE: uint l = lua_strlen(L, -(toget+1));
				int l = lua_strlen(L, -(toget + 1));
				if (B.lvl - toget + 1 >= LIMIT || toplen > l) {
					toplen += l;
					toget++;
				} else {
					break;
				}
			} while (toget < B.lvl);
			lua_concat(L, toget);
			B.lvl = B.lvl - toget + 1;
		}
	}

	public static CharPtr luaL_prepbuffer(luaL_Buffer B) {
		if (emptybuffer(B) != 0) {
			adjuststack(B);
		}
		return new CharPtr(B.buffer, B.p);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static void luaL_addlstring (luaL_Buffer B, CharPtr
	// s, uint l)
	public static void luaL_addlstring(luaL_Buffer B, CharPtr s, int l) {
		while (l-- != 0) {
			char c = s.getItem(0);
			s = s.next();
			luaL_addchar(B, c);
		}
	}

	public static void luaL_addlstring(luaL_Buffer B, String sS, int l) {
		CharPtr s = new CharPtr(sS);
		while (l-- != 0) {
			char c = s.getItem(0);
			s = s.next();
			luaL_addchar(B, c);
		}
	}

	public static void luaL_addstring(luaL_Buffer B, CharPtr s) {
		luaL_addlstring(B, s, (int) strlen(s));
	}

	public static void luaL_pushresult(luaL_Buffer B) {
		emptybuffer(B);
		lua_concat(B.L, B.lvl);
		B.lvl = 1;
	}

	public static void luaL_addvalue(luaL_Buffer B) {
		lua_State L = B.L;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint vl;
		int vl = 0;
		RefObject<Integer> tempRef_vl = new RefObject<Integer>(vl);
		CharPtr s = lua_tolstring(L, -1, tempRef_vl);
		vl = tempRef_vl.argvalue;
		if (vl <= bufffree(B)) // fit into buffer?
		{
			CharPtr dst = new CharPtr(B.buffer.chars, B.buffer.index + B.p);
			CharPtr src = new CharPtr(s.chars, s.index);
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: for (uint i = 0; i < vl; i++)
			for (int i = 0; i < vl; i++) {
				dst.setItem(i, src.getItem(i));
			}
			B.p += (int) vl;
			lua_pop(L, 1); // remove from stack
		} else {
			if (emptybuffer(B) != 0) {
				lua_insert(L, -2); // put buffer before new value
			}
			B.lvl++; // add new value into B stack
			adjuststack(B);
		}
	}

	public static void luaL_buffinit(lua_State L, luaL_Buffer B) {
		B.L = L;
		B.p = 0; // B.buffer
		B.lvl = 0;
	}

	/* }====================================================== */

	public static int luaL_ref(lua_State L, int t) {
		int ref_;
		t = abs_index(L, t);
		if (lua_isnil(L, -1)) {
			lua_pop(L, 1); // remove from stack
			return LUA_REFNIL; // `nil' has a unique fixed reference
		}
		lua_rawgeti(L, t, FREELIST_REF); // get first free element
		ref_ = (int) lua_tointeger(L, -1); // ref = t[FREELIST_REF]
		lua_pop(L, 1); // remove it from stack
		if (ref_ != 0) // any free element?
		{
			lua_rawgeti(L, t, ref_); // remove it from list
			lua_rawseti(L, t, FREELIST_REF); // (t[FREELIST_REF] = t[ref])
		} else // no free elements
		{
			ref_ = (int) lua_objlen(L, t);
			ref_++; // create new reference
		}
		lua_rawseti(L, t, ref_);
		return ref_;
	}

	public static void luaL_unref(lua_State L, int t, int ref_) {
		if (ref_ >= 0) {
			t = abs_index(L, t);
			lua_rawgeti(L, t, FREELIST_REF);
			lua_rawseti(L, t, ref_); // t[ref] = t[FREELIST_REF]
			lua_pushinteger(L, ref_);
			lua_rawseti(L, t, FREELIST_REF); // t[FREELIST_REF] = ref
		}
	}

	/*
	 * * {======================================================* Load functions
	 * * =======================================================
	 */

	public static class LoadF {
		public int extraline;
		public InputStream f;
		public CharPtr buff = new CharPtr(new char[LUAL_BUFFERSIZE]);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static CharPtr getF (lua_State L, object ud, out
	// uint size)
	public static CharPtr getF(lua_State L, Object ud, RefObject<Integer> size) {
		size.argvalue = 0;
		LoadF lf = (LoadF) ud;
		// (void)L;
		if (lf.extraline != 0) {
			lf.extraline = 0;
			size.argvalue = 1;
			return new CharPtr("\n");
		}
		if (feof(lf.f) != 0) {
			return null;
		}
		size.argvalue = (int) fread(lf.buff, 1, lf.buff.chars.length, lf.f);
		return (size.argvalue > 0) ? new CharPtr(lf.buff) : null;
	}

	public static int errfile(lua_State L, CharPtr what, int fnameindex) {
		CharPtr serr = strerror(errno());
		CharPtr filename = CharPtr.OpAddition(lua_tostring(L, fnameindex), 1);// CharPtr
																				// filename
																				// =
																				// lua_tostring(L,
																				// fnameindex)
																				// +
																				// 1;
		lua_pushfstring(L, new CharPtr("cannot %s %s: %s"), what, filename,
				serr);
		lua_remove(L, fnameindex);
		return LUA_ERRFILE;
	}

	// static IDelegate getF = lua_Reader.build(Lua.class, "getF");
	static IDelegate getF = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			return getF((lua_State) args[0], args[1],
					(RefObject<Integer>) args[2]);
		}
	};
	
	public static int luaL_loadfile(lua_State L, String filename)
	{
		return luaL_loadfile(L, new CharPtr(filename));
	}

	public static int luaL_loadfile(lua_State L, CharPtr filename) {
		LoadF lf = new LoadF();
		int status, readstatus;
		int c;
		int fnameindex = lua_gettop(L) + 1; // index of filename on the stack
		lf.extraline = 0;
		if (filename == null) {
			lua_pushliteral(L, new CharPtr("=stdin"));
			lf.f = stdin;
		} else {
			lua_pushfstring(L, new CharPtr("@%s"), filename);
			lf.f = (InputStream) fopen(filename, new CharPtr("r"));
			if (lf.f == null) {
				return errfile(L, new CharPtr("open"), fnameindex);
			}
		}
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if PocketPC
		// fseek(lf.f, 3, 0);
		// #endif
		c = getc(lf.f);
		if (c == '#') // Unix exec. file?
		{
			lf.extraline = 1;
			while ((c = getc(lf.f)) != EOF && c != '\n')
				; // skip first line
			if (c == '\n') {
				c = getc(lf.f);
			}
		}
		if (c == LUA_SIGNATURE.charAt(0) && (filename != null)) // binary file?
		{
			lf.f = freopen(filename, new CharPtr("rb"), lf.f); // reopen in
																// binary mode
			if (lf.f == null) {
				return errfile(L, new CharPtr("reopen"), fnameindex);
			}
			/* skip eventual `#!...' */
			while ((c = getc(lf.f)) != EOF && c != LUA_SIGNATURE.charAt(0))
				;
			lf.extraline = 0;
		}
		ungetc(c, lf.f);
		status = lua_load(L, getF, lf, lua_tostring(L, -1));
		readstatus = ferror(lf.f);
		if (filename != null) // close file (even in case of errors)
		{
			fclose(lf.f);
		}
		if (readstatus != 0) {
			lua_settop(L, fnameindex); // ignore results from `lua_load'
			return errfile(L, new CharPtr("read"), fnameindex);
		}
		lua_remove(L, fnameindex);
		return status;
	}

	public static class LoadS {
		public CharPtr s;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public uint size;
		public int size;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: static CharPtr getS (lua_State L, object ud, out uint
	// size)
	public static CharPtr getS(lua_State L, Object ud, RefObject<Integer> size) {
		LoadS ls = (LoadS) ud;
		// (void)L;
		// if (ls.size == 0) return null;
		size.argvalue = ls.size;
		ls.size = 0;
		return ls.s;
	}

	// static IDelegate getS = lua_Reader.build(Lua.class, "getS");
	static IDelegate getS = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			return getS((lua_State) args[0], args[1],
					(RefObject<Integer>) args[2]);
		}
	};

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int luaL_loadbuffer(lua_State L, CharPtr
	// buff, uint size, CharPtr name)
	public static int luaL_loadbuffer(lua_State L, CharPtr buff, int size,
			CharPtr name) {
		LoadS ls = new LoadS();
		ls.s = new CharPtr(buff);
		ls.size = size;
		return lua_load(L, getS, ls, name);
	}

	public static int luaL_loadstring(lua_State L, CharPtr s) {
		return luaL_loadbuffer(L, s, (int) strlen(s), s);
	}

	public static int luaL_loadstring(lua_State L, String sS) {
		CharPtr s = new CharPtr(sS);
		return luaL_loadbuffer(L, s, (int) strlen(s), s);
	}

	/* }====================================================== */

	public static Object l_alloc(java.lang.Class t) {
		// return System.Activator.CreateInstance(t);
		try {
			return t.newInstance();
		} catch (IllegalAccessException e) {
			Tools.LogException("Lua.java", e);
		} catch (InstantiationException e) {
			Tools.LogException("Lua.java", e);
		}
		return null;
	}

	public static int panic(lua_State L) {
		// (void)L; /* to avoid warnings */
		// fprintf(stderr, new
		// CharPtr("PANIC: unprotected error in call to Lua API (%s)\n"),
		// lua_tostring(L, -1));
		return 0;
	}

	// static IDelegate panic = lua_CFunction.build(Lua.class, "panic");
	static IDelegate panic = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return panic((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	// static IDelegate l_alloc = lua_Alloc.build(Lua.class, "l_alloc");
	static IDelegate l_alloc = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return l_alloc((Class) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static lua_State luaL_newstate() {
		lua_State L = lua_newstate(l_alloc, null);
		if (L != null) {
			lua_atpanic(L, panic);
		}
		return L;
	}

	/*
	 * * If your system does not support `stdout', you can just remove this
	 * function.* If you need, you can define your own `print' function,
	 * following this* model but changing `fputs' to put the strings at a proper
	 * place* (a console window or a log file, for instance).
	 */
	public static int luaB_print(lua_State L) {
		int n = lua_gettop(L); // number of arguments
		int i;
		lua_getglobal(L, new CharPtr("tostring"));
		for (i = 1; i <= n; i++) {
			CharPtr s;
			lua_pushvalue(L, -1); // function to be called
			lua_pushvalue(L, i); // value to print
			lua_call(L, 1, 1);
			s = lua_tostring(L, -1); // get result
			if (s == null) {
				return luaL_error(L, new CharPtr(LUA_QL("tostring")
						+ " must return a string to " + LUA_QL("print")));
			}
			if (i > 1) {
				fputs(new CharPtr("\t"), stdout);
			}
			fputs(s, stdout);
			lua_pop(L, 1); // pop result
		}
		System.out.printf("\n", stdout);
		return 0;
	}

	public static int luaB_tonumber(lua_State L) {
		int base_ = luaL_optint(L, 2, 10);
		if (base_ == 10) // standard conversion
		{
			luaL_checkany(L, 1);
			if (lua_isnumber(L, 1) != 0) {
				lua_pushnumber(L, lua_tonumber(L, 1));
				return 1;
			}
		} else {
			CharPtr s1 = luaL_checkstring(L, 1);
			CharPtr s2 = null;
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: ulong n;
			long n;
			luaL_argcheck(L, 2 <= base_ && base_ <= 36, 2, "base out of range");
			RefObject<CharPtr> tempRef_s2 = new RefObject<CharPtr>(s2);
			n = strtoul(s1, tempRef_s2, base_);
			s2 = tempRef_s2.argvalue;
			if (s1 != s2) // at least one valid digit?
			{
				while (isspace((byte) (s2.getItem(0)))) // skip trailing spaces
				{
					s2 = s2.next();
				}
				if (s2.getItem(0) == '\0') // no invalid trailing characters?
				{
					lua_pushnumber(L, (double) n);
					return 1;
				}
			}
		}
		lua_pushnil(L); // else not a number
		return 1;
	}

	public static int luaB_error(lua_State L) {
		int level = luaL_optint(L, 2, 1);
		lua_settop(L, 1);
		if ((lua_isstring(L, 1) != 0) && (level > 0)) // add extra information?
		{
			luaL_where(L, level);
			lua_pushvalue(L, 1);
			lua_concat(L, 2);
		}
		return lua_error(L);
	}

	public static int luaB_getmetatable(lua_State L) {
		luaL_checkany(L, 1);
		if (lua_getmetatable(L, 1) == 0) {
			lua_pushnil(L);
			return 1; // no metatable
		}
		luaL_getmetafield(L, 1, new CharPtr("__metatable"));
		return 1; // returns either __metatable field (if present) or metatable
	}

	public static int luaB_setmetatable(lua_State L) {
		int t = lua_type(L, 2);
		luaL_checktype(L, 1, LUA_TTABLE);
		luaL_argcheck(L, t == LUA_TNIL || t == LUA_TTABLE, 2,
				"nil or table expected");
		if (luaL_getmetafield(L, 1, new CharPtr("__metatable")) != 0) {
			luaL_error(L, new CharPtr("cannot change a protected metatable"));
		}
		lua_settop(L, 2);
		lua_setmetatable(L, 1);
		return 1;
	}

	public static void getfunc(lua_State L, int opt) {
		if (lua_isfunction(L, 1)) {
			lua_pushvalue(L, 1);
		} else {
			lua_Debug ar = new lua_Debug();
			int level = (opt != 0) ? luaL_optint(L, 1, 1) : luaL_checkint(L, 1);
			luaL_argcheck(L, level >= 0, 1, "level must be non-negative");
			if (lua_getstack(L, level, ar) == 0) {
				luaL_argerror(L, 1, new CharPtr("invalid level"));
			}
			lua_getinfo(L, new CharPtr("f"), ar);
			if (lua_isnil(L, -1)) {
				luaL_error(L, new CharPtr(
						"no function environment for tail call at level %d"),
						level);
			}
		}
	}

	public static int luaB_getfenv(lua_State L) {
		getfunc(L, 1);
		if (lua_iscfunction(L, -1)) // is a C function?
		{
			lua_pushvalue(L, LUA_GLOBALSINDEX); // return the thread's global
												// env.
		} else {
			lua_getfenv(L, -1);
		}
		return 1;
	}

	public static int luaB_setfenv(lua_State L) {
		luaL_checktype(L, 2, LUA_TTABLE);
		getfunc(L, 0);
		lua_pushvalue(L, 2);
		if ((lua_isnumber(L, 1) != 0) && (lua_tonumber(L, 1) == 0)) {
			/* change environment of current thread */
			lua_pushthread(L);
			lua_insert(L, -2);
			lua_setfenv(L, -2);
			return 0;
		} else if (lua_iscfunction(L, -2) || lua_setfenv(L, -2) == 0) {
			luaL_error(L, new CharPtr(LUA_QL("setfenv")
					+ " cannot change environment of given object"));
		}
		return 1;
	}

	public static int luaB_rawequal(lua_State L) {
		luaL_checkany(L, 1);
		luaL_checkany(L, 2);
		lua_pushboolean(L, lua_rawequal(L, 1, 2));
		return 1;
	}

	public static int luaB_rawget(lua_State L) {
		luaL_checktype(L, 1, LUA_TTABLE);
		luaL_checkany(L, 2);
		lua_settop(L, 2);
		lua_rawget(L, 1);
		return 1;
	}

	public static int luaB_rawset(lua_State L) {
		luaL_checktype(L, 1, LUA_TTABLE);
		luaL_checkany(L, 2);
		luaL_checkany(L, 3);
		lua_settop(L, 3);
		lua_rawset(L, 1);
		return 1;
	}

	public static int luaB_gcinfo(lua_State L) {
		lua_pushinteger(L, lua_getgccount(L));
		return 1;
	}

	public static final CharPtr[] opts = new CharPtr[] { new CharPtr("stop"),
			new CharPtr("restart"), new CharPtr("collect"),
			new CharPtr("count"), new CharPtr("step"), new CharPtr("setpause"),
			new CharPtr("setstepmul"), null };
	public final static int[] optsnum = { LUA_GCSTOP, LUA_GCRESTART,
			LUA_GCCOLLECT, LUA_GCCOUNT, LUA_GCSTEP, LUA_GCSETPAUSE,
			LUA_GCSETSTEPMUL };

	public static int luaB_collectgarbage(lua_State L) {
		int o = luaL_checkoption(L, 1, new CharPtr("collect"), opts);
		int ex = luaL_optint(L, 2, 0);
		int res = lua_gc(L, optsnum[o], ex);
		switch (optsnum[o]) {
		case LUA_GCCOUNT: {
			int b = lua_gc(L, LUA_GCCOUNTB, 0);
			lua_pushnumber(L, res + ((double) b / 1024));
			return 1;
		}
		case LUA_GCSTEP: {
			lua_pushboolean(L, res);
			return 1;
		}
		default: {
			lua_pushnumber(L, res);
			return 1;
		}
		}
	}

	public static int luaB_type(lua_State L) {
		luaL_checkany(L, 1);
		lua_pushstring(L, luaL_typename(L, 1));
		return 1;
	}

	public static int luaB_next(lua_State L) {
		luaL_checktype(L, 1, LUA_TTABLE);
		lua_settop(L, 2); // create a 2nd argument if there isn't one
		if (lua_next(L, 1) != 0) {
			return 2;
		} else {
			lua_pushnil(L);
			return 1;
		}
	}

	public static int luaB_pairs(lua_State L) {
		luaL_checktype(L, 1, LUA_TTABLE);
		lua_pushvalue(L, lua_upvalueindex(1)); // return generator,
		lua_pushvalue(L, 1); // state,
		lua_pushnil(L); // and initial value
		return 3;
	}

	public static int ipairsaux(lua_State L) {
		int i = luaL_checkint(L, 2);
		luaL_checktype(L, 1, LUA_TTABLE);
		i++; // next value
		lua_pushinteger(L, i);
		lua_rawgeti(L, 1, i);
		return (lua_isnil(L, -1)) ? 0 : 2;
	}

	public static int luaB_ipairs(lua_State L) {
		luaL_checktype(L, 1, LUA_TTABLE);
		lua_pushvalue(L, lua_upvalueindex(1)); // return generator,
		lua_pushvalue(L, 1); // state,
		lua_pushinteger(L, 0); // and initial value
		return 3;
	}

	public static int load_aux(lua_State L, int status) {
		if (status == 0) // OK?
		{
			return 1;
		} else {
			lua_pushnil(L);
			lua_insert(L, -2); // put before error message
			return 2; // return nil plus error message
		}
	}

	public static int luaB_loadstring(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		CharPtr chunkname = luaL_optstring(L, 2, s);
		return load_aux(L, luaL_loadbuffer(L, s, l, chunkname));
	}

	public static int luaB_loadfile(lua_State L) {
		CharPtr fname = luaL_optstring(L, 1, (CharPtr) null);
		return load_aux(L, luaL_loadfile(L, fname));
	}

	/*
	 * * Reader for generic `load' function: `lua_load' uses the* stack for
	 * internal stuff, so the reader cannot change the* stack top. Instead, it
	 * keeps its resulting string in a* reserved slot inside the stack.
	 */
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static CharPtr generic_reader (lua_State L, object
	// ud, out uint size)
	public static CharPtr generic_reader(lua_State L, Object ud,
			RefObject<Integer> size) {
		// (void)ud; /* to avoid warnings */
		luaL_checkstack(L, 2, new CharPtr("too many nested functions"));
		lua_pushvalue(L, 1); // get function
		lua_call(L, 0, 1); // call it
		if (lua_isnil(L, -1)) {
			size.argvalue = 0;
			return null;
		} else if (lua_isstring(L, -1) != 0) {
			lua_replace(L, 3); // save string in a reserved stack slot
			return lua_tolstring(L, 3, size);
		} else {
			size.argvalue = 0;
			luaL_error(L, new CharPtr("reader function must return a string"));
		}
		return null; // to avoid warnings
	}

	// static IDelegate generic_reader = lua_Reader.build(Lua.class,
	// "generic_reader");
	static IDelegate generic_reader = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			return generic_reader((lua_State) args[0], args[1],
					(RefObject<Integer>) args[2]);
		}
	};

	public static int luaB_load(lua_State L) {
		int status;
		CharPtr cname = luaL_optstring(L, 2, new CharPtr("=(load)"));
		luaL_checktype(L, 1, LUA_TFUNCTION);
		lua_settop(L, 3); // function, eventual name, plus one reserved slot
		status = lua_load(L, generic_reader, null, cname);
		return load_aux(L, status);
	}

	public static int luaB_dofile(lua_State L) {
		CharPtr fname = luaL_optstring(L, 1, (CharPtr) null);
		int n = lua_gettop(L);
		if (luaL_loadfile(L, fname) != 0) {
			lua_error(L);
		}
		lua_call(L, 0, LUA_MULTRET);
		return lua_gettop(L) - n;
	}

	public static int luaB_assert(lua_State L) {
		luaL_checkany(L, 1);
		if (lua_toboolean(L, 1) == 0) {
			return luaL_error(L, new CharPtr("%s"),
					luaL_optstring(L, 2, new CharPtr("assertion failed!")));
		}
		return lua_gettop(L);
	}

	// static IDelegate luaL_checkint =
	// luaL_opt_delegate_integer.build(Lua.class, "luaL_checkint");
	static IDelegate luaL_checkint = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luaL_checkint((lua_State) arg1, (Integer) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int luaB_unpack(lua_State L) {
		int i, e, n;
		luaL_checktype(L, 1, LUA_TTABLE);
		i = luaL_optint(L, 2, 1);
		e = luaL_opt_integer(L, luaL_checkint, 3, luaL_getn(L, 1));
		if (i > e) // empty range
		{
			return 0;
		}
		n = e - i + 1; // number of elements
		if (n <= 0 || (lua_checkstack(L, n) == 0)) // n <= 0 means arith.
													// overflow
		{
			return luaL_error(L, new CharPtr("too many results to unpack"));
		}
		lua_rawgeti(L, 1, i); // push arg[i] (avoiding overflow problems)
		while (i++ < e) // push arg[i + 1...e]
		{
			lua_rawgeti(L, 1, i);
		}
		return n;
	}

	public static int luaB_select(lua_State L) {
		int n = lua_gettop(L);
		if (lua_type(L, 1) == LUA_TSTRING
				&& lua_tostring(L, 1).getItem(0) == '#') {
			lua_pushinteger(L, n - 1);
			return 1;
		} else {
			int i = luaL_checkint(L, 1);
			if (i < 0) {
				i = n + i;
			} else if (i > n) {
				i = n;
			}
			luaL_argcheck(L, 1 <= i, 1, "index out of range");
			return n - i;
		}
	}

	public static int luaB_pcall(lua_State L) {
		int status;
		luaL_checkany(L, 1);
		status = lua_pcall(L, lua_gettop(L) - 1, LUA_MULTRET, 0);
		lua_pushboolean(L, (status == 0) ? 1 : 0);
		lua_insert(L, 1);
		return lua_gettop(L); // return status + all results
	}

	public static int luaB_xpcall(lua_State L) {
		int status;
		luaL_checkany(L, 2);
		lua_settop(L, 2);
		lua_insert(L, 1); // put error function under function to be called
		status = lua_pcall(L, 0, LUA_MULTRET, 1);
		lua_pushboolean(L, (status == 0) ? 1 : 0);
		lua_replace(L, 1);
		return lua_gettop(L); // return status + all results
	}

	public static int luaB_tostring(lua_State L) {
		luaL_checkany(L, 1);
		if (luaL_callmeta(L, 1, new CharPtr("__tostring")) != 0) // is there a
																	// metafield?
		{
			return 1; // use its value
		}
		switch (lua_type(L, 1)) {
		case LUA_TNUMBER:
			lua_pushstring(L, lua_tostring(L, 1));
			break;
		case LUA_TSTRING:
			lua_pushvalue(L, 1);
			break;
		case LUA_TBOOLEAN:
			lua_pushstring(L, (lua_toboolean(L, 1) != 0 ? new CharPtr("true")
					: new CharPtr("false")));
			break;
		case LUA_TNIL:
			lua_pushliteral(L, new CharPtr("nil"));
			break;
		default:
			lua_pushfstring(L, new CharPtr("%s: %p"), luaL_typename(L, 1),
					lua_topointer(L, 1));
			break;
		}
		return 1;
	}

	public static int luaB_newproxy(lua_State L) {
		lua_settop(L, 1);
		lua_newuserdata(L, 0); // create proxy
		if (lua_toboolean(L, 1) == 0) {
			return 1; // no metatable
		} else if (lua_isboolean(L, 1)) {
			lua_newtable(L); // create a new metatable `m'...
			lua_pushvalue(L, -1); // ... and mark `m' as a valid metatable
			lua_pushboolean(L, 1);
			lua_rawset(L, lua_upvalueindex(1)); // weaktable[m] = true
		} else {
			int validproxy = 0; // to check if weaktable[metatable(u)] == true
			if (lua_getmetatable(L, 1) != 0) {
				lua_rawget(L, lua_upvalueindex(1));
				validproxy = lua_toboolean(L, -1);
				lua_pop(L, 1); // remove value
			}
			luaL_argcheck(L, validproxy != 0, 1, "boolean or proxy expected");
			lua_getmetatable(L, 1); // metatable is valid; get it
		}
		lua_setmetatable(L, 2);
		return 1;
	}

	private final static luaL_Reg[] base_funcs = {
			new luaL_Reg("assert", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_assert((lua_State)arg);
				}
			}),
			new luaL_Reg("collectgarbage", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_collectgarbage((lua_State)arg);
				}
			}),
			new luaL_Reg("dofile", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_dofile((lua_State)arg);
				}
			}),
			new luaL_Reg("error", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_error((lua_State)arg);
				}
			}),
			new luaL_Reg("gcinfo", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_gcinfo((lua_State)arg);
				}
			}),
			new luaL_Reg("getfenv", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_getfenv((lua_State)arg);
				}
			}),
			new luaL_Reg("getmetatable", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_load((lua_State)arg);
				}
			}),
			new luaL_Reg("loadfile", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_load((lua_State)arg);
				}
			}),
			new luaL_Reg("load", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_load((lua_State)arg);
				}
			}),
			new luaL_Reg("loadstring", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_loadstring((lua_State)arg);
				}
			}),
			new luaL_Reg("next", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_next((lua_State)arg);
				}
			}),
			new luaL_Reg("pcall", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_pcall((lua_State)arg);
				}
			}),
			new luaL_Reg("print", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_print((lua_State)arg);
				}
			}),
			new luaL_Reg("rawequal", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_rawequal((lua_State)arg);
				}
			}),
			new luaL_Reg("rawget", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_rawget((lua_State)arg);
				}
			}),
			new luaL_Reg("rawset", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_rawset((lua_State)arg);
				}
			}),
			new luaL_Reg("select", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_select((lua_State)arg);
				}
			}),
			new luaL_Reg("setfenv", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_setfenv((lua_State)arg);
				}
			}),
			new luaL_Reg("setmetatable", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_setmetatable((lua_State)arg);
				}
			}),
			new luaL_Reg("tonumber", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_tonumber((lua_State)arg);
				}
			}),
			new luaL_Reg("tostring", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_tostring((lua_State)arg);
				}
			}),
			new luaL_Reg("type", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_type((lua_State)arg);
				}
			}),
			new luaL_Reg("unpack", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_unpack((lua_State)arg);
				}
			}),
			new luaL_Reg("xpcall", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_xpcall((lua_State)arg);
				}
			}), new luaL_Reg((String)null, (String)null) };

	/*
	 * * {======================================================* Coroutine
	 * library* =======================================================
	 */

	public static final int CO_RUN = 0; // running
	public static final int CO_SUS = 1; // suspended
	public static final int CO_NOR = 2; // 'normal' (it resumed another
										// coroutine)
	public static final int CO_DEAD = 3;

	public static final String[] statnames = { "running", "suspended",
			"normal", "dead" };

	public static int costatus(lua_State L, lua_State co) {
		if (L == co) {
			return CO_RUN;
		}
		switch (lua_status(co)) {
		case LUA_YIELD:
			return CO_SUS;
		case 0: {
			lua_Debug ar = new lua_Debug();
			if (lua_getstack(co, 0, ar) > 0) // does it have frames?
			{
				return CO_NOR; // it is running
			} else if (lua_gettop(co) == 0) {
				return CO_DEAD;
			} else {
				return CO_SUS; // initial state
			}
		}
		default: // some error occured
			return CO_DEAD;
		}
	}

	public static int luaB_costatus(lua_State L) {
		lua_State co = lua_tothread(L, 1);
		luaL_argcheck(L, co != null, 1, "coroutine expected");
		lua_pushstring(L, new CharPtr(statnames[costatus(L, co)]));
		return 1;
	}

	public static int auxresume(lua_State L, lua_State co, int narg) {
		int status = costatus(L, co);
		if (lua_checkstack(co, narg) == 0) {
			luaL_error(L, new CharPtr("too many arguments to resume"));
		}
		if (status != CO_SUS) {
			lua_pushfstring(L, new CharPtr("cannot resume %s coroutine"),
					new CharPtr(statnames[status]));
			return -1; // error flag
		}
		lua_xmove(L, co, narg);
		lua_setlevel(L, co);
		status = lua_resume(co, narg);
		if (status == 0 || status == LUA_YIELD) {
			int nres = lua_gettop(co);
			if (lua_checkstack(L, nres + 1) == 0) {
				luaL_error(L, new CharPtr("too many results to resume"));
			}
			lua_xmove(co, L, nres); // move yielded values
			return nres;
		} else {
			lua_xmove(co, L, 1); // move error message
			return -1; // error flag
		}
	}

	public static int luaB_coresume(lua_State L) {
		lua_State co = lua_tothread(L, 1);
		int r;
		luaL_argcheck(L, co != null, 1, "coroutine expected");
		r = auxresume(L, co, lua_gettop(L) - 1);
		if (r < 0) {
			lua_pushboolean(L, 0);
			lua_insert(L, -2);
			return 2; // return false + error message
		} else {
			lua_pushboolean(L, 1);
			lua_insert(L, -(r + 1));
			return r + 1; // return true + `resume' returns
		}
	}

	public static int luaB_auxwrap(lua_State L) {
		lua_State co = lua_tothread(L, lua_upvalueindex(1));
		int r = auxresume(L, co, lua_gettop(L));
		if (r < 0) {
			if (lua_isstring(L, -1) != 0) // error object is a string?
			{
				luaL_where(L, 1); // add extra info
				lua_insert(L, -2);
				lua_concat(L, 2);
			}
			lua_error(L); // propagate error
		}
		return r;
	}

	public static int luaB_cocreate(lua_State L) {
		lua_State NL = lua_newthread(L);
		luaL_argcheck(L, lua_isfunction(L, 1) && !lua_iscfunction(L, 1), 1,
				"Lua function expected");
		lua_pushvalue(L, 1); // move function to top
		lua_xmove(L, NL, 1); // move function from L to NL
		return 1;
	}

	// static IDelegate luaB_auxwrap = lua_CFunction.build(Lua.class,
	// "luaB_auxwrap");
	static IDelegate luaB_auxwrap = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return luaB_auxwrap((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int luaB_cowrap(lua_State L) {
		luaB_cocreate(L);
		lua_pushcclosure(L, luaB_auxwrap, 1);
		return 1;
	}

	public static int luaB_yield(lua_State L) {
		return lua_yield(L, lua_gettop(L));
	}

	public static int luaB_corunning(lua_State L) {
		if (lua_pushthread(L) != 0) {
			lua_pushnil(L); // main thread is not a coroutine
		}
		return 1;
	}

	private final static luaL_Reg[] co_funcs = {
			new luaL_Reg("create", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_cocreate((lua_State)arg);
				}
			}),
			new luaL_Reg("resume", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_coresume((lua_State)arg);
				}
			}),
			new luaL_Reg("running", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_corunning((lua_State)arg);
				}
			}),
			new luaL_Reg("status", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_costatus((lua_State)arg);
				}
			}),
			new luaL_Reg("wrap", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_cowrap((lua_State)arg);
				}
			}),
			new luaL_Reg("yield", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaB_yield((lua_State)arg);
				}
			}), new luaL_Reg((String)null, (String)null) };

	/* }====================================================== */

	// public static void auxopen(lua_State L, CharPtr name, lua_CFunction f,
	// lua_CFunction u)
	public static void auxopen(lua_State L, CharPtr name, IDelegate f,
			IDelegate u) {
		lua_pushcfunction(L, u);
		lua_pushcclosure(L, f, 1);
		lua_setfield(L, -2, name);
	}

	// static IDelegate luaB_ipairs = lua_CFunction.build(Lua.class,
	// "luaB_ipairs");
	static IDelegate luaB_ipairs = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return luaB_ipairs((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	// static IDelegate ipairsaux = lua_CFunction.build(Lua.class, "ipairsaux");
	static IDelegate ipairsaux = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return ipairsaux((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	// static IDelegate luaB_pairs = lua_CFunction.build(Lua.class,
	// "luaB_pairs");
	static IDelegate luaB_pairs = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return luaB_pairs((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	// static IDelegate luaB_next = lua_CFunction.build(Lua.class, "luaB_next");
	static IDelegate luaB_next = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return luaB_next((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	// static IDelegate luaB_newproxy = lua_CFunction.build(Lua.class,
	// "luaB_newproxy");
	static IDelegate luaB_newproxy = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return luaB_newproxy((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static void base_open(lua_State L) {
		/* set global _G */
		lua_pushvalue(L, LUA_GLOBALSINDEX);
		lua_setglobal(L, new CharPtr("_G"));
		/* open lib into global table */
		luaL_register(L, new CharPtr("_G"), base_funcs);
		lua_pushliteral(L, new CharPtr(LUA_VERSION));
		lua_setglobal(L, new CharPtr("_VERSION")); // set global _VERSION
		/* `ipairs' and `pairs' need auxliliary functions as upvalues */
		auxopen(L, new CharPtr("ipairs"), luaB_ipairs, ipairsaux);
		auxopen(L, new CharPtr("pairs"), luaB_pairs, luaB_next);
		/* `newproxy' needs a weaktable as upvalue */
		lua_createtable(L, 0, 1); // new table `w'
		lua_pushvalue(L, -1); // `w' will be its own metatable
		lua_setmetatable(L, -2);
		lua_pushliteral(L, new CharPtr("kv"));
		lua_setfield(L, -2, new CharPtr("__mode")); // metatable(w).__mode =
													// "kv"
		lua_pushcclosure(L, luaB_newproxy, 1);
		lua_setglobal(L, new CharPtr("newproxy")); // set global `newproxy'
	}

	public static int luaopen_base(lua_State L) {
		base_open(L);
		luaL_register(L, new CharPtr(LUA_COLIBNAME), co_funcs);
		return 2;
	}

	/*
	 * * Marks the end of a patch list. It is an invalid value both as an
	 * absolute* address, and as a list link (would link an element to itself).
	 */
	public static final int NO_JUMP = (-1);

	/*
	 * * grep "ORDER OPR" if you change these enums
	 */
	public enum BinOpr {
		OPR_ADD, OPR_SUB, OPR_MUL, OPR_DIV, OPR_MOD, OPR_POW, OPR_CONCAT, OPR_NE, OPR_EQ, OPR_LT, OPR_LE, OPR_GT, OPR_GE, OPR_AND, OPR_OR, OPR_NOBINOPR;

		public int getValue() {
			return this.ordinal();
		}

		public static BinOpr forValue(int value) {
			return values()[value];
		}
	}

	public enum UnOpr {
		OPR_MINUS, OPR_NOT, OPR_LEN, OPR_NOUNOPR;

		public int getValue() {
			return this.ordinal();
		}

		public static UnOpr forValue(int value) {
			return values()[value];
		}
	}

	public static InstructionPtr getcode(FuncState fs, expdesc e) {
		return new InstructionPtr(fs.f.code, e.u.s.info);
	}

	public static int luaK_codeAsBx(FuncState fs, OpCode o, int A, int sBx) {
		return luaK_codeABx(fs, o, A, sBx + MAXARG_sBx);
	}

	public static void luaK_setmultret(FuncState fs, expdesc e) {
		luaK_setreturns(fs, e, LUA_MULTRET);
	}

	public static boolean hasjumps(expdesc e) {
		return e.t != e.f;
	}

	public static int isnumeral(expdesc e) {
		return (e.k == expkind.VKNUM && e.t == NO_JUMP && e.f == NO_JUMP) ? 1
				: 0;
	}

	public static void luaK_nil(FuncState fs, int from, int n) {
		InstructionPtr previous;
		if (fs.pc > fs.lasttarget) // no jumps to current position?
		{
			if (fs.pc == 0) // function start?
			{
				if (from >= fs.nactvar) {
					return; // positions are already clean
				}
			} else {
				previous = new InstructionPtr(fs.f.code, fs.pc - 1);
				if (GET_OPCODE(previous) == OpCode.OP_LOADNIL) {
					int pfrom = GETARG_A(previous);
					int pto = GETARG_B(previous);
					if (pfrom <= from && from <= pto + 1) // can connect both?
					{
						if (from + n - 1 > pto) {
							SETARG_B(previous, from + n - 1);
						}
						return;
					}
				}
			}
		}
		luaK_codeABC(fs, OpCode.OP_LOADNIL, from, from + n - 1, 0); // else no
																	// optimization
	}

	public static int luaK_jump(FuncState fs) {
		int jpc = fs.jpc; // save list of jumps to here
		int j;
		fs.jpc = NO_JUMP;
		j = luaK_codeAsBx(fs, OpCode.OP_JMP, 0, NO_JUMP);
		RefObject<Integer> tempRef_j = new RefObject<Integer>(j);
		luaK_concat(fs, tempRef_j, jpc); // keep them on hold
		j = tempRef_j.argvalue;
		return j;
	}

	public static void luaK_ret(FuncState fs, int first, int nret) {
		luaK_codeABC(fs, OpCode.OP_RETURN, first, nret + 1, 0);
	}

	public static int condjump(FuncState fs, OpCode op, int A, int B, int C) {
		luaK_codeABC(fs, op, A, B, C);
		return luaK_jump(fs);
	}

	public static void fixjump(FuncState fs, int pc, int dest) {
		InstructionPtr jmp = new InstructionPtr(fs.f.code, pc);
		int offset = dest - (pc + 1);
		lua_assert(dest != NO_JUMP);
		if (Math.abs(offset) > MAXARG_sBx) {
			luaX_syntaxerror(fs.ls, new CharPtr("control structure too long"));
		}
		SETARG_sBx(jmp, offset);
	}

	/*
	 * * returns current `pc' and marks it as a jump target (to avoid wrong*
	 * optimizations with consecutive instructions not in the same basic block).
	 */
	public static int luaK_getlabel(FuncState fs) {
		fs.lasttarget = fs.pc;
		return fs.pc;
	}

	public static int getjump(FuncState fs, int pc) {
		int offset = GETARG_sBx(fs.f.code[pc]);
		if (offset == NO_JUMP) // point to itself represents end of list
		{
			return NO_JUMP; // end of list
		} else {
			return (pc + 1) + offset; // turn offset into absolute position
		}
	}

	public static InstructionPtr getjumpcontrol(FuncState fs, int pc) {
		InstructionPtr pi = new InstructionPtr(fs.f.code, pc);
		if (pc >= 1 && (testTMode(GET_OPCODE(pi.getItem(-1))) != 0)) {
			return new InstructionPtr(pi.codes, pi.pc - 1);
		} else {
			return new InstructionPtr(pi.codes, pi.pc);
		}
	}

	/*
	 * * check whether list has any jump that do not produce a value* (or
	 * produce an inverted value)
	 */
	public static int need_value(FuncState fs, int list) {
		for (; list != NO_JUMP; list = getjump(fs, list)) {
			InstructionPtr i = getjumpcontrol(fs, list);
			if (GET_OPCODE(i.getItem(0)) != OpCode.OP_TESTSET) {
				return 1;
			}
		}
		return 0; // not found
	}

	public static int patchtestreg(FuncState fs, int node, int reg) {
		InstructionPtr i = getjumpcontrol(fs, node);
		if (GET_OPCODE(i.getItem(0)) != OpCode.OP_TESTSET) {
			return 0; // cannot patch other instructions
		}
		if (reg != NO_REG && reg != GETARG_B(i.getItem(0))) {
			SETARG_A(i, reg);
		} else // no register to put value or register already has the value
		{
			i.setItem(
					0,
					CREATE_ABC(OpCode.OP_TEST, GETARG_B(i.getItem(0)), 0,
							GETARG_C(i.getItem(0))));
		}

		return 1;
	}

	public static void removevalues(FuncState fs, int list) {
		for (; list != NO_JUMP; list = getjump(fs, list)) {
			patchtestreg(fs, list, NO_REG);
		}
	}

	public static void patchlistaux(FuncState fs, int list, int vtarget,
			int reg, int dtarget) {
		while (list != NO_JUMP) {
			int next = getjump(fs, list);
			if (patchtestreg(fs, list, reg) != 0) {
				fixjump(fs, list, vtarget);
			} else {
				fixjump(fs, list, dtarget); // jump to default target
			}
			list = next;
		}
	}

	public static void dischargejpc(FuncState fs) {
		patchlistaux(fs, fs.jpc, fs.pc, NO_REG, fs.pc);
		fs.jpc = NO_JUMP;
	}

	public static void luaK_patchlist(FuncState fs, int list, int target) {
		if (target == fs.pc) {
			luaK_patchtohere(fs, list);
		} else {
			lua_assert(target < fs.pc);
			patchlistaux(fs, list, target, NO_REG, target);
		}
	}

	public static void luaK_patchtohere(FuncState fs, int list) {
		luaK_getlabel(fs);
		RefObject<Integer> tempRef_jpc = new RefObject<Integer>(fs.jpc);
		luaK_concat(fs, tempRef_jpc, list);
		fs.jpc = tempRef_jpc.argvalue;
	}

	public static void luaK_concat(FuncState fs, RefObject<Integer> l1, int l2) {
		if (l2 == NO_JUMP) {
			return;
		} else if (l1.argvalue == NO_JUMP) {
			l1.argvalue = l2;
		} else {
			int list = l1.argvalue;
			int next;
			while ((next = getjump(fs, list)) != NO_JUMP) // find last element
			{
				list = next;
			}
			fixjump(fs, list, l2);
		}
	}

	public static void luaK_checkstack(FuncState fs, int n) {
		int newstack = fs.freereg + n;
		if (newstack > fs.f.maxstacksize) {
			if (newstack >= MAXSTACK) {
				luaX_syntaxerror(fs.ls, new CharPtr(
						"function or expression too complex"));
			}
			fs.f.maxstacksize = cast_byte(newstack);
		}
	}

	public static void luaK_reserveregs(FuncState fs, int n) {
		luaK_checkstack(fs, n);
		fs.freereg += n;
	}

	public static void freereg(FuncState fs, int reg) {
		if ((ISK(reg) == 0) && reg >= fs.nactvar) {
			fs.freereg--;
			lua_assert(reg == fs.freereg);
		}
	}

	public static void freeexp(FuncState fs, expdesc e) {
		if (e.k == expkind.VNONRELOC) {
			freereg(fs, e.u.s.info);
		}
	}

	public static int addk(FuncState fs, lua_TValue k, lua_TValue v) {
		lua_State L = fs.L;
		lua_TValue idx = luaH_set(L, fs.h, k);
		Proto f = fs.f;
		int oldsize = f.sizek;
		if (ttisnumber(idx)) {
			lua_assert(luaO_rawequalObj(fs.f.k[cast_int(nvalue(idx))], v));
			return cast_int(nvalue(idx));
		} else // constant not found; create a new entry
		{
			setnvalue(idx, cast_num(fs.nk));
			RefObject<lua_TValue[]> tempRef_k = new RefObject<lua_TValue[]>(f.k);
			RefObject<Integer> tempRef_sizek = new RefObject<Integer>(f.sizek);
			luaM_growvector(L, tempRef_k, fs.nk, tempRef_sizek, MAXARG_Bx,
					new CharPtr("constant table overflow"), lua_TValue.class);
			f.k = tempRef_k.argvalue;
			f.sizek = tempRef_sizek.argvalue;
			while (oldsize < f.sizek) {
				setnilvalue(f.k[oldsize++]);
			}
			setobj(L, f.k[fs.nk], v);
			luaC_barrier(L, f, v);
			return fs.nk++;
		}
	}

	public static int luaK_stringK(FuncState fs, TString s) {
		lua_TValue o = new lua_TValue();
		setsvalue(fs.L, o, s);
		return addk(fs, o, o);
	}

	public static int luaK_numberK(FuncState fs, double r) {
		lua_TValue o = new lua_TValue();
		setnvalue(o, r);
		return addk(fs, o, o);
	}

	public static int boolK(FuncState fs, int b) {
		lua_TValue o = new lua_TValue();
		setbvalue(o, b);
		return addk(fs, o, o);
	}

	public static int nilK(FuncState fs) {
		lua_TValue k = new lua_TValue(), v = new lua_TValue();
		setnilvalue(v);
		/* cannot use nil as key; instead use table itself to represent nil */
		sethvalue(fs.L, k, fs.h);
		return addk(fs, k, v);
	}

	public static void luaK_setreturns(FuncState fs, expdesc e, int nresults) {
		if (e.k == expkind.VCALL) // expression is an open function call?
		{
			SETARG_C(getcode(fs, e), nresults + 1);
		} else if (e.k == expkind.VVARARG) {
			SETARG_B(getcode(fs, e), nresults + 1);
			SETARG_A(getcode(fs, e), fs.freereg);
			luaK_reserveregs(fs, 1);
		}
	}

	public static void luaK_setoneret(FuncState fs, expdesc e) {
		if (e.k == expkind.VCALL) // expression is an open function call?
		{
			e.k = expkind.VNONRELOC;
			e.u.s.info = GETARG_A(getcode(fs, e));
		} else if (e.k == expkind.VVARARG) {
			SETARG_B(getcode(fs, e), 2);
			e.k = expkind.VRELOCABLE; // can relocate its simple result
		}
	}

	public static void luaK_dischargevars(FuncState fs, expdesc e) {
		switch (e.k) {
		case VLOCAL: {
			e.k = expkind.VNONRELOC;
			break;
		}
		case VUPVAL: {
			e.u.s.info = luaK_codeABC(fs, OpCode.OP_GETUPVAL, 0, e.u.s.info, 0);
			e.k = expkind.VRELOCABLE;
			break;
		}
		case VGLOBAL: {
			e.u.s.info = luaK_codeABx(fs, OpCode.OP_GETGLOBAL, 0, e.u.s.info);
			e.k = expkind.VRELOCABLE;
			break;
		}
		case VINDEXED: {
			freereg(fs, e.u.s.aux);
			freereg(fs, e.u.s.info);
			e.u.s.info = luaK_codeABC(fs, OpCode.OP_GETTABLE, 0, e.u.s.info,
					e.u.s.aux);
			e.k = expkind.VRELOCABLE;
			break;
		}
		case VVARARG:
		case VCALL: {
			luaK_setoneret(fs, e);
			break;
		}
		default: // there is one value available (somewhere)
			break;
		}
	}

	public static int code_label(FuncState fs, int A, int b, int jump) {
		luaK_getlabel(fs); // those instructions may be jump targets
		return luaK_codeABC(fs, OpCode.OP_LOADBOOL, A, b, jump);
	}

	public static void discharge2reg(FuncState fs, expdesc e, int reg) {
		luaK_dischargevars(fs, e);
		switch (e.k) {
		case VNIL: {
			luaK_nil(fs, reg, 1);
			break;
		}
		case VFALSE:
		case VTRUE: {
			luaK_codeABC(fs, OpCode.OP_LOADBOOL, reg,
					(e.k == expkind.VTRUE) ? 1 : 0, 0);
			break;
		}
		case VK: {
			luaK_codeABx(fs, OpCode.OP_LOADK, reg, e.u.s.info);
			break;
		}
		case VKNUM: {
			luaK_codeABx(fs, OpCode.OP_LOADK, reg, luaK_numberK(fs, e.u.nval));
			break;
		}
		case VRELOCABLE: {
			InstructionPtr pc = getcode(fs, e);
			SETARG_A(pc, reg);
			break;
		}
		case VNONRELOC: {
			if (reg != e.u.s.info) {
				luaK_codeABC(fs, OpCode.OP_MOVE, reg, e.u.s.info, 0);
			}
			break;
		}
		default: {
			lua_assert(e.k == expkind.VVOID || e.k == expkind.VJMP);
			return; // nothing to do...
		}
		}
		e.u.s.info = reg;
		e.k = expkind.VNONRELOC;
	}

	public static void discharge2anyreg(FuncState fs, expdesc e) {
		if (e.k != expkind.VNONRELOC) {
			luaK_reserveregs(fs, 1);
			discharge2reg(fs, e, fs.freereg - 1);
		}
	}

	public static void exp2reg(FuncState fs, expdesc e, int reg) {
		discharge2reg(fs, e, reg);
		RefObject<Integer> tempRef_t = new RefObject<Integer>(e.t);
		if (e.k == expkind.VJMP) {
			luaK_concat(fs, tempRef_t, e.u.s.info); // put this jump in `t' list
		}
		e.t = tempRef_t.argvalue;
		if (hasjumps(e)) {
			int finalV; // position after whole expression
			int p_f = NO_JUMP; // position of an eventual LOAD false
			int p_t = NO_JUMP; // position of an eventual LOAD true
			if (need_value(fs, e.t) != 0 || need_value(fs, e.f) != 0) {
				int fj = (e.k == expkind.VJMP) ? NO_JUMP : luaK_jump(fs);
				p_f = code_label(fs, reg, 0, 1);
				p_t = code_label(fs, reg, 1, 0);
				luaK_patchtohere(fs, fj);
			}
			finalV = luaK_getlabel(fs);
			patchlistaux(fs, e.f, finalV, reg, p_f);
			patchlistaux(fs, e.t, finalV, reg, p_t);
		}
		e.f = e.t = NO_JUMP;
		e.u.s.info = reg;
		e.k = expkind.VNONRELOC;
	}

	public static void luaK_exp2nextreg(FuncState fs, expdesc e) {
		luaK_dischargevars(fs, e);
		freeexp(fs, e);
		luaK_reserveregs(fs, 1);
		exp2reg(fs, e, fs.freereg - 1);
	}

	public static int luaK_exp2anyreg(FuncState fs, expdesc e) {
		luaK_dischargevars(fs, e);
		if (e.k == expkind.VNONRELOC) {
			if (!hasjumps(e)) // exp is already in a register
			{
				return e.u.s.info;
			}
			if (e.u.s.info >= fs.nactvar) // reg. is not a local?
			{
				exp2reg(fs, e, e.u.s.info); // put value on it
				return e.u.s.info;
			}
		}
		luaK_exp2nextreg(fs, e); // default
		return e.u.s.info;
	}

	public static void luaK_exp2val(FuncState fs, expdesc e) {
		if (hasjumps(e)) {
			luaK_exp2anyreg(fs, e);
		} else {
			luaK_dischargevars(fs, e);
		}
	}

	public static int luaK_exp2RK(FuncState fs, expdesc e) {
		luaK_exp2val(fs, e);
		switch (e.k) {
		case VKNUM:
		case VTRUE:
		case VFALSE:
		case VNIL: {
			if (fs.nk <= MAXINDEXRK) // constant fit in RK operand?
			{
				e.u.s.info = (e.k == expkind.VNIL) ? nilK(fs)
						: (e.k == expkind.VKNUM) ? luaK_numberK(fs, e.u.nval)
								: boolK(fs, (e.k == expkind.VTRUE) ? 1 : 0);
				e.k = expkind.VK;
				return RKASK(e.u.s.info);
			} else {
				break;
			}
		}
		case VK: {
			if (e.u.s.info <= MAXINDEXRK) // constant fit in argC?
			{
				return RKASK(e.u.s.info);
			} else {
				break;
			}
		}
		default:
			break;
		}
		/* not a constant in the right range: put it in a register */
		return luaK_exp2anyreg(fs, e);
	}

	public static void luaK_storevar(FuncState fs, expdesc var, expdesc ex) {
		switch (var.k) {
		case VLOCAL: {
			freeexp(fs, ex);
			exp2reg(fs, ex, var.u.s.info);
			return;
		}
		case VUPVAL: {
			int e = luaK_exp2anyreg(fs, ex);
			luaK_codeABC(fs, OpCode.OP_SETUPVAL, e, var.u.s.info, 0);
			break;
		}
		case VGLOBAL: {
			int e = luaK_exp2anyreg(fs, ex);
			luaK_codeABx(fs, OpCode.OP_SETGLOBAL, e, var.u.s.info);
			break;
		}
		case VINDEXED: {
			int e = luaK_exp2RK(fs, ex);
			luaK_codeABC(fs, OpCode.OP_SETTABLE, var.u.s.info, var.u.s.aux, e);
			break;
		}
		default: {
			lua_assert(0); // invalid var kind to store
			break;
		}
		}
		freeexp(fs, ex);
	}

	public static void luaK_self(FuncState fs, expdesc e, expdesc key) {
		int func;
		luaK_exp2anyreg(fs, e);
		freeexp(fs, e);
		func = fs.freereg;
		luaK_reserveregs(fs, 2);
		luaK_codeABC(fs, OpCode.OP_SELF, func, e.u.s.info, luaK_exp2RK(fs, key));
		freeexp(fs, key);
		e.u.s.info = func;
		e.k = expkind.VNONRELOC;
	}

	public static void invertjump(FuncState fs, expdesc e) {
		InstructionPtr pc = getjumpcontrol(fs, e.u.s.info);
		lua_assert(testTMode(GET_OPCODE(pc.getItem(0))) != 0
				&& GET_OPCODE(pc.getItem(0)) != OpCode.OP_TESTSET
				&& GET_OPCODE(pc.getItem(0)) != OpCode.OP_TEST);
		SETARG_A(pc, (GETARG_A(pc.getItem(0)) == 0) ? 1 : 0);
	}

	public static int jumponcond(FuncState fs, expdesc e, int cond) {
		if (e.k == expkind.VRELOCABLE) {
			InstructionPtr ie = getcode(fs, e);
			if (GET_OPCODE(ie) == OpCode.OP_NOT) {
				fs.pc--; // remove previous OpCode.OP_NOT
				return condjump(fs, OpCode.OP_TEST, GETARG_B(ie), 0,
						(cond == 0) ? 1 : 0);
			}
			/* else go through */
		}
		discharge2anyreg(fs, e);
		freeexp(fs, e);
		return condjump(fs, OpCode.OP_TESTSET, NO_REG, e.u.s.info, cond);
	}

	public static void luaK_goiftrue(FuncState fs, expdesc e) {
		int pc; // pc of last jump
		luaK_dischargevars(fs, e);
		switch (e.k) {
		case VK:
		case VKNUM:
		case VTRUE: {
			pc = NO_JUMP; // always true; do nothing
			break;
		}
		case VFALSE: {
			pc = luaK_jump(fs); // always jump
			break;
		}
		case VJMP: {
			invertjump(fs, e);
			pc = e.u.s.info;
			break;
		}
		default: {
			pc = jumponcond(fs, e, 0);
			break;
		}
		}
		RefObject<Integer> tempRef_f = new RefObject<Integer>(e.f);
		luaK_concat(fs, tempRef_f, pc); // insert last jump in `f' list
		e.f = tempRef_f.argvalue;
		luaK_patchtohere(fs, e.t);
		e.t = NO_JUMP;
	}

	public static void luaK_goiffalse(FuncState fs, expdesc e) {
		int pc; // pc of last jump
		luaK_dischargevars(fs, e);
		switch (e.k) {
		case VNIL:
		case VFALSE: {
			pc = NO_JUMP; // always false; do nothing
			break;
		}
		case VTRUE: {
			pc = luaK_jump(fs); // always jump
			break;
		}
		case VJMP: {
			pc = e.u.s.info;
			break;
		}
		default: {
			pc = jumponcond(fs, e, 1);
			break;
		}
		}
		RefObject<Integer> tempRef_t = new RefObject<Integer>(e.t);
		luaK_concat(fs, tempRef_t, pc); // insert last jump in `t' list
		e.t = tempRef_t.argvalue;
		luaK_patchtohere(fs, e.f);
		e.f = NO_JUMP;
	}

	public static void codenot(FuncState fs, expdesc e) {
		luaK_dischargevars(fs, e);
		switch (e.k) {
		case VNIL:
		case VFALSE: {
			e.k = expkind.VTRUE;
			break;
		}
		case VK:
		case VKNUM:
		case VTRUE: {
			e.k = expkind.VFALSE;
			break;
		}
		case VJMP: {
			invertjump(fs, e);
			break;
		}
		case VRELOCABLE:
		case VNONRELOC: {
			discharge2anyreg(fs, e);
			freeexp(fs, e);
			e.u.s.info = luaK_codeABC(fs, OpCode.OP_NOT, 0, e.u.s.info, 0);
			e.k = expkind.VRELOCABLE;
			break;
		}
		default: {
			lua_assert(0); // cannot happen
			break;
		}
		}
		/* interchange true and false lists */
		{
			int temp = e.f;
			e.f = e.t;
			e.t = temp;
		}
		removevalues(fs, e.f);
		removevalues(fs, e.t);
	}

	public static void luaK_indexed(FuncState fs, expdesc t, expdesc k) {
		t.u.s.aux = luaK_exp2RK(fs, k);
		t.k = expkind.VINDEXED;
	}

	public static int constfolding(OpCode op, expdesc e1, expdesc e2) {
		double v1, v2, r;
		if ((isnumeral(e1) == 0) || (isnumeral(e2) == 0)) {
			return 0;
		}
		v1 = e1.u.nval;
		v2 = e2.u.nval;
		switch (op) {
		case OP_ADD:
			r = luai_numadd(v1, v2);
			break;
		case OP_SUB:
			r = luai_numsub(v1, v2);
			break;
		case OP_MUL:
			r = luai_nummul(v1, v2);
			break;
		case OP_DIV:
			if (v2 == 0) // do not attempt to divide by 0
			{
				return 0;
			}
			r = luai_numdiv(v1, v2);
			break;
		case OP_MOD:
			if (v2 == 0) // do not attempt to divide by 0
			{
				return 0;
			}
			r = luai_nummod(v1, v2);
			break;
		case OP_POW:
			r = luai_numpow(v1, v2);
			break;
		case OP_UNM:
			r = luai_numunm(v1);
			break;
		case OP_LEN: // no constant folding for 'len'
			return 0;
		default:
			lua_assert(0);
			r = 0;
			break;
		}
		if (luai_numisnan(r)) // do not attempt to produce NaN
		{
			return 0;
		}
		e1.u.nval = r;
		return 1;
	}

	public static void codearith(FuncState fs, OpCode op, expdesc e1, expdesc e2) {
		if (constfolding(op, e1, e2) != 0) {
			return;
		} else {
			int o2 = (op != OpCode.OP_UNM && op != OpCode.OP_LEN) ? luaK_exp2RK(
					fs, e2) : 0;
			int o1 = luaK_exp2RK(fs, e1);
			if (o1 > o2) {
				freeexp(fs, e1);
				freeexp(fs, e2);
			} else {
				freeexp(fs, e2);
				freeexp(fs, e1);
			}
			e1.u.s.info = luaK_codeABC(fs, op, 0, o1, o2);
			e1.k = expkind.VRELOCABLE;
		}
	}

	public static void codecomp(FuncState fs, OpCode op, int cond, expdesc e1,
			expdesc e2) {
		int o1 = luaK_exp2RK(fs, e1);
		int o2 = luaK_exp2RK(fs, e2);
		freeexp(fs, e2);
		freeexp(fs, e1);
		if (cond == 0 && op != OpCode.OP_EQ) {
			int temp; // exchange args to replace by `<' or `<='
			temp = o1; // o1 <==> o2
			o1 = o2;
			o2 = temp;
			cond = 1;
		}
		e1.u.s.info = condjump(fs, op, cond, o1, o2);
		e1.k = expkind.VJMP;
	}

	public static void luaK_prefix(FuncState fs, UnOpr op, expdesc e) {
		expdesc e2 = new expdesc();
		e2.t = e2.f = NO_JUMP;
		e2.k = expkind.VKNUM;
		e2.u.nval = 0;
		switch (op) {
		case OPR_MINUS: {
			if (isnumeral(e) == 0) {
				luaK_exp2anyreg(fs, e); // cannot operate on non-numeric
										// constants
			}
			codearith(fs, OpCode.OP_UNM, e, e2);
			break;
		}
		case OPR_NOT:
			codenot(fs, e);
			break;
		case OPR_LEN: {
			luaK_exp2anyreg(fs, e); // cannot operate on constants
			codearith(fs, OpCode.OP_LEN, e, e2);
			break;
		}
		default:
			lua_assert(0);
			break;
		}
	}

	public static void luaK_infix(FuncState fs, BinOpr op, expdesc v) {
		switch (op) {
		case OPR_AND: {
			luaK_goiftrue(fs, v);
			break;
		}
		case OPR_OR: {
			luaK_goiffalse(fs, v);
			break;
		}
		case OPR_CONCAT: {
			luaK_exp2nextreg(fs, v); // operand must be on the `stack'
			break;
		}
		case OPR_ADD:
		case OPR_SUB:
		case OPR_MUL:
		case OPR_DIV:
		case OPR_MOD:
		case OPR_POW: {
			if ((isnumeral(v) == 0)) {
				luaK_exp2RK(fs, v);
			}
			break;
		}
		default: {
			luaK_exp2RK(fs, v);
			break;
		}
		}
	}

	public static void luaK_posfix(FuncState fs, BinOpr op, expdesc e1,
			expdesc e2) {
		switch (op) {
		case OPR_AND: {
			lua_assert(e1.t == NO_JUMP); // list must be closed
			luaK_dischargevars(fs, e2);
			RefObject<Integer> tempRef_f = new RefObject<Integer>(e2.f);
			luaK_concat(fs, tempRef_f, e1.f);
			e2.f = tempRef_f.argvalue;
			e1.Copy(e2);
			break;
		}
		case OPR_OR: {
			lua_assert(e1.f == NO_JUMP); // list must be closed
			luaK_dischargevars(fs, e2);
			RefObject<Integer> tempRef_t = new RefObject<Integer>(e2.t);
			luaK_concat(fs, tempRef_t, e1.t);
			e2.t = tempRef_t.argvalue;
			e1.Copy(e2);
			break;
		}
		case OPR_CONCAT: {
			luaK_exp2val(fs, e2);
			if (e2.k == expkind.VRELOCABLE
					&& GET_OPCODE(getcode(fs, e2)) == OpCode.OP_CONCAT) {
				lua_assert(e1.u.s.info == GETARG_B(getcode(fs, e2)) - 1);
				freeexp(fs, e1);
				SETARG_B(getcode(fs, e2), e1.u.s.info);
				e1.k = expkind.VRELOCABLE;
				e1.u.s.info = e2.u.s.info;
			} else {
				luaK_exp2nextreg(fs, e2); // operand must be on the 'stack'
				codearith(fs, OpCode.OP_CONCAT, e1, e2);
			}
			break;
		}
		case OPR_ADD:
			codearith(fs, OpCode.OP_ADD, e1, e2);
			break;
		case OPR_SUB:
			codearith(fs, OpCode.OP_SUB, e1, e2);
			break;
		case OPR_MUL:
			codearith(fs, OpCode.OP_MUL, e1, e2);
			break;
		case OPR_DIV:
			codearith(fs, OpCode.OP_DIV, e1, e2);
			break;
		case OPR_MOD:
			codearith(fs, OpCode.OP_MOD, e1, e2);
			break;
		case OPR_POW:
			codearith(fs, OpCode.OP_POW, e1, e2);
			break;
		case OPR_EQ:
			codecomp(fs, OpCode.OP_EQ, 1, e1, e2);
			break;
		case OPR_NE:
			codecomp(fs, OpCode.OP_EQ, 0, e1, e2);
			break;
		case OPR_LT:
			codecomp(fs, OpCode.OP_LT, 1, e1, e2);
			break;
		case OPR_LE:
			codecomp(fs, OpCode.OP_LE, 1, e1, e2);
			break;
		case OPR_GT:
			codecomp(fs, OpCode.OP_LT, 0, e1, e2);
			break;
		case OPR_GE:
			codecomp(fs, OpCode.OP_LE, 0, e1, e2);
			break;
		default:
			lua_assert(0);
			break;
		}
	}

	public static void luaK_fixline(FuncState fs, int line) {
		fs.f.lineinfo[fs.pc - 1] = line;
	}

	public static int luaK_code(FuncState fs, long i, int line) {
		Proto f = fs.f;
		dischargejpc(fs); // `pc' will change
		/* put new int in code array */
		RefObject<Long[]> tempRef_code = new RefObject<Long[]>(f.code);
		RefObject<Integer> tempRef_sizecode = new RefObject<Integer>(f.sizecode);
		Lua.<Long> luaM_growvector(fs.L, tempRef_code, fs.pc,
				tempRef_sizecode, MAX_INT, new CharPtr("code size overflow"),
				Long.class);
		f.code = tempRef_code.argvalue;
		f.sizecode = tempRef_sizecode.argvalue;
		f.code[fs.pc] = i;
		/* save corresponding line information */
		RefObject<Integer[]> tempRef_lineinfo = new RefObject<Integer[]>(
				f.lineinfo);
		RefObject<Integer> tempRef_sizelineinfo = new RefObject<Integer>(
				f.sizelineinfo);
		Lua.<Integer> luaM_growvector(fs.L, tempRef_lineinfo, fs.pc,
				tempRef_sizelineinfo, MAX_INT,
				new CharPtr("code size overflow"), Integer.class);
		f.lineinfo = tempRef_lineinfo.argvalue;
		f.sizelineinfo = tempRef_sizelineinfo.argvalue;
		f.lineinfo[fs.pc] = line;
		return fs.pc++;
	}

	public static int luaK_codeABC(FuncState fs, OpCode o, int a, int b, int c) {
		lua_assert(getOpMode(o) == OpMode.iABC);
		lua_assert(getBMode(o) != OpArgMask.OpArgN || b == 0);
		lua_assert(getCMode(o) != OpArgMask.OpArgN || c == 0);
		return luaK_code(fs, CREATE_ABC(o, a, b, c), fs.ls.lastline);
	}

	public static int luaK_codeABx(FuncState fs, OpCode o, int a, int bc) {
		lua_assert(getOpMode(o) == OpMode.iABx || getOpMode(o) == OpMode.iAsBx);
		lua_assert(getCMode(o) == OpArgMask.OpArgN);
		return luaK_code(fs, CREATE_ABx(o, a, bc), fs.ls.lastline);
	}

	public static void luaK_setlist(FuncState fs, int base_, int nelems,
			int tostore) {
		int c = (nelems - 1) / LFIELDS_PER_FLUSH + 1;
		int b = (tostore == LUA_MULTRET) ? 0 : tostore;
		lua_assert(tostore != 0);
		if (c <= MAXARG_C) {
			luaK_codeABC(fs, OpCode.OP_SETLIST, base_, b, c);
		} else {
			luaK_codeABC(fs, OpCode.OP_SETLIST, base_, b, 0);
			luaK_code(fs, c, fs.ls.lastline);
		}
		fs.freereg = base_ + 1; // free registers with list values
	}

	public static int db_getregistry(lua_State L) {
		lua_pushvalue(L, LUA_REGISTRYINDEX);
		return 1;
	}

	public static int db_getmetatable(lua_State L) {
		luaL_checkany(L, 1);
		if (lua_getmetatable(L, 1) == 0) {
			lua_pushnil(L); // no metatable
		}
		return 1;
	}

	public static int db_setmetatable(lua_State L) {
		int t = lua_type(L, 2);
		luaL_argcheck(L, t == LUA_TNIL || t == LUA_TTABLE, 2,
				"nil or table expected");
		lua_settop(L, 2);
		lua_pushboolean(L, lua_setmetatable(L, 1));
		return 1;
	}

	public static int db_getfenv(lua_State L) {
		lua_getfenv(L, 1);
		return 1;
	}

	public static int db_setfenv(lua_State L) {
		luaL_checktype(L, 2, LUA_TTABLE);
		lua_settop(L, 2);
		if (lua_setfenv(L, 1) == 0) {
			luaL_error(L, new CharPtr(LUA_QL("setfenv")
					+ " cannot change environment of given object"));
		}
		return 1;
	}

	public static void settabss(lua_State L, CharPtr i, CharPtr v) {
		lua_pushstring(L, v);
		lua_setfield(L, -2, i);
	}

	public static void settabsi(lua_State L, CharPtr i, int v) {
		lua_pushinteger(L, v);
		lua_setfield(L, -2, i);
	}

	public static lua_State getthread(lua_State L, RefObject<Integer> arg) {
		if (lua_isthread(L, 1)) {
			arg.argvalue = 1;
			return lua_tothread(L, 1);
		} else {
			arg.argvalue = 0;
			return L;
		}
	}

	public static void treatstackoption(lua_State L, lua_State L1, CharPtr fname) {
		if (L == L1) {
			lua_pushvalue(L, -2);
			lua_remove(L, -3);
		} else {
			lua_xmove(L1, L, 1);
		}
		lua_setfield(L, -2, fname);
	}

	public static int db_getinfo(lua_State L) {
		lua_Debug ar = new lua_Debug();
		int arg = 0;
		RefObject<Integer> tempRef_arg = new RefObject<Integer>(arg);
		lua_State L1 = getthread(L, tempRef_arg);
		arg = tempRef_arg.argvalue;
		CharPtr options = luaL_optstring(L, arg + 2, new CharPtr("flnSu"));
		if (lua_isnumber(L, arg + 1) != 0) {
			if (lua_getstack(L1, (int) lua_tointeger(L, arg + 1), ar) == 0) {
				lua_pushnil(L); // level out of range
				return 1;
			}
		} else if (lua_isfunction(L, arg + 1)) {
			lua_pushfstring(L, new CharPtr(">%s"), options);
			options = lua_tostring(L, -1);
			lua_pushvalue(L, arg + 1);
			lua_xmove(L, L1, 1);
		} else {
			return luaL_argerror(L, arg + 1, new CharPtr(
					"function or level expected"));
		}
		if (lua_getinfo(L1, options, ar) == 0) {
			return luaL_argerror(L, arg + 2, new CharPtr("invalid option"));
		}
		lua_createtable(L, 0, 2);
		if (strchr(options, 'S') != null) {
			settabss(L, new CharPtr("source"), ar.source);
			settabss(L, new CharPtr("short_src"), ar.short_src);
			settabsi(L, new CharPtr("linedefined"), ar.linedefined);
			settabsi(L, new CharPtr("lastlinedefined"), ar.lastlinedefined);
			settabss(L, new CharPtr("what"), ar.what);
		}
		if (strchr(options, 'l') != null) {
			settabsi(L, new CharPtr("currentline"), ar.currentline);
		}
		if (strchr(options, 'u') != null) {
			settabsi(L, new CharPtr("nups"), ar.nups);
		}
		if (strchr(options, 'n') != null) {
			settabss(L, new CharPtr("name"), ar.name);
			settabss(L, new CharPtr("namewhat"), ar.namewhat);
		}
		if (strchr(options, 'L') != null) {
			treatstackoption(L, L1, new CharPtr("activelines"));
		}
		if (strchr(options, 'f') != null) {
			treatstackoption(L, L1, new CharPtr("func"));
		}
		return 1; // return table
	}

	public static int db_getlocal(lua_State L) {
		int arg = 0;
		RefObject<Integer> tempRef_arg = new RefObject<Integer>(arg);
		lua_State L1 = getthread(L, tempRef_arg);
		arg = tempRef_arg.argvalue;
		lua_Debug ar = new lua_Debug();
		CharPtr name;
		if (lua_getstack(L1, luaL_checkint(L, arg + 1), ar) == 0) // out of
																	// range?
		{
			return luaL_argerror(L, arg + 1, new CharPtr("level out of range"));
		}
		name = lua_getlocal(L1, ar, luaL_checkint(L, arg + 2));
		if (name != null) {
			lua_xmove(L1, L, 1);
			lua_pushstring(L, name);
			lua_pushvalue(L, -2);
			return 2;
		} else {
			lua_pushnil(L);
			return 1;
		}
	}

	public static int db_setlocal(lua_State L) {
		int arg = 0;
		RefObject<Integer> tempRef_arg = new RefObject<Integer>(arg);
		lua_State L1 = getthread(L, tempRef_arg);
		arg = tempRef_arg.argvalue;
		lua_Debug ar = new lua_Debug();
		if (lua_getstack(L1, luaL_checkint(L, arg + 1), ar) == 0) // out of
																	// range?
		{
			return luaL_argerror(L, arg + 1, new CharPtr("level out of range"));
		}
		luaL_checkany(L, arg + 3);
		lua_settop(L, arg + 3);
		lua_xmove(L, L1, 1);
		lua_pushstring(L, lua_setlocal(L1, ar, luaL_checkint(L, arg + 2)));
		return 1;
	}

	public static int auxupvalue(lua_State L, int get) {
		CharPtr name;
		int n = luaL_checkint(L, 2);
		luaL_checktype(L, 1, LUA_TFUNCTION);
		if (lua_iscfunction(L, 1)) // cannot touch C upvalues from Lua
		{
			return 0;
		}
		name = (get != 0) ? lua_getupvalue(L, 1, n) : lua_setupvalue(L, 1, n);
		if (name == null) {
			return 0;
		}
		lua_pushstring(L, name);
		lua_insert(L, -(get + 1));
		return get + 1;
	}

	public static int db_getupvalue(lua_State L) {
		return auxupvalue(L, 1);
	}

	public static int db_setupvalue(lua_State L) {
		luaL_checkany(L, 3);
		return auxupvalue(L, 0);
	}

	public static final String KEY_HOOK = "h";

	public static final String[] hooknames = { "call", "return", "line",
			"count", "tail return" };

	public static void hookf(lua_State L, lua_Debug ar) {
		lua_pushlightuserdata(L, KEY_HOOK);
		lua_rawget(L, LUA_REGISTRYINDEX);
		lua_pushlightuserdata(L, L);
		lua_rawget(L, -2);
		if (lua_isfunction(L, -1)) {
			lua_pushstring(L, new CharPtr(hooknames[(int) ar.event_]));
			if (ar.currentline >= 0) {
				lua_pushinteger(L, ar.currentline);
			} else {
				lua_pushnil(L);
			}
			lua_assert(lua_getinfo(L, new CharPtr("lS"), ar));
			lua_call(L, 2, 0);
		}
	}

	public static int makemask(CharPtr smask, int count) {
		int mask = 0;
		if (strchr(smask, 'c') != null) {
			mask |= LUA_MASKCALL;
		}
		if (strchr(smask, 'r') != null) {
			mask |= LUA_MASKRET;
		}
		if (strchr(smask, 'l') != null) {
			mask |= LUA_MASKLINE;
		}
		if (count > 0) {
			mask |= LUA_MASKCOUNT;
		}
		return mask;
	}

	public static CharPtr unmakemask(int mask, CharPtr smask) {
		int i = 0;
		if ((mask & LUA_MASKCALL) != 0) {
			smask.setItem(i++, 'c');
		}
		if ((mask & LUA_MASKRET) != 0) {
			smask.setItem(i++, 'r');
		}
		if ((mask & LUA_MASKLINE) != 0) {
			smask.setItem(i++, 'l');
		}
		smask.setItem(i, '\0');
		return smask;
	}

	public static void gethooktable(lua_State L) {
		lua_pushlightuserdata(L, KEY_HOOK);
		lua_rawget(L, LUA_REGISTRYINDEX);
		if (!lua_istable(L, -1)) {
			lua_pop(L, 1);
			lua_createtable(L, 0, 1);
			lua_pushlightuserdata(L, KEY_HOOK);
			lua_pushvalue(L, -2);
			lua_rawset(L, LUA_REGISTRYINDEX);
		}
	}

	// static IDelegate hookf = lua_Hook.build(Lua.class, "hookf");
	static IDelegate hookf = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			hookf((lua_State) arg1, (lua_Debug) arg2);
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int db_sethook(lua_State L) {
		int arg = 0, mask, count = 0;
		IDelegate func;// lua_Hook func;
		RefObject<Integer> tempRef_arg = new RefObject<Integer>(arg);
		lua_State L1 = getthread(L, tempRef_arg);
		arg = tempRef_arg.argvalue;
		if (lua_isnoneornil(L, arg + 1)) {
			lua_settop(L, arg + 1);
			func = null; // turn off hooks
			mask = 0;
			count = 0;
		} else {
			CharPtr smask = luaL_checkstring(L, arg + 2);
			luaL_checktype(L, arg + 1, LUA_TFUNCTION);
			count = luaL_optint(L, arg + 3, 0);
			func = hookf;
			mask = makemask(smask, count);
		}
		gethooktable(L);
		lua_pushlightuserdata(L, L1);
		lua_pushvalue(L, arg + 1);
		lua_rawset(L, -3); // set new hook
		lua_pop(L, 1); // remove hook table
		lua_sethook(L1, func, mask, count); // set hooks
		return 0;
	}

	public static int db_gethook(lua_State L) {
		int arg = 0;
		RefObject<Integer> tempRef_arg = new RefObject<Integer>(arg);
		lua_State L1 = getthread(L, tempRef_arg);
		arg = tempRef_arg.argvalue;
		CharPtr buff = new CharPtr(new char[5]);
		int mask = lua_gethookmask(L1);
		IDelegate hook = lua_gethook(L1);// lua_Hook hook = lua_gethook(L1);
		if (hook != null && hook != hookf) // external hook?
		{
			lua_pushliteral(L, new CharPtr("external hook"));
		} else {
			gethooktable(L);
			lua_pushlightuserdata(L, L1);
			lua_rawget(L, -2); // get hook
			lua_remove(L, -2); // remove hook table
		}
		lua_pushstring(L, unmakemask(mask, buff));
		lua_pushinteger(L, lua_gethookcount(L1));
		return 3;
	}

	public static int db_debug(lua_State L) {
		for (;;) {
			CharPtr buffer = new CharPtr(new char[250]);
			fputs(new CharPtr("lua_debug> "), stderr);
			if (fgets(buffer, stdin) == null
					|| strcmp(buffer, new CharPtr("cont\n")) == 0) {
				return 0;
			}
			if (luaL_loadbuffer(L, buffer, (int) strlen(buffer), new CharPtr(
					"=(debug command)")) != 0
					|| lua_pcall(L, 0, 0, 0) != 0) {
				fputs(lua_tostring(L, -1), stderr);
				fputs(new CharPtr("\n"), stderr);
			}
			lua_settop(L, 0); // remove eventual returns
		}
	}

	public static final int LEVELS1 = 12; // size of the first part of the stack
	public static final int LEVELS2 = 10; // size of the second part of the
											// stack

	public static int db_errorfb(lua_State L) {
		int level;
		boolean firstpart = true; // still before eventual `...'
		int arg = 0;
		RefObject<Integer> tempRef_arg = new RefObject<Integer>(arg);
		lua_State L1 = getthread(L, tempRef_arg);
		arg = tempRef_arg.argvalue;
		lua_Debug ar = new lua_Debug();
		if (lua_isnumber(L, arg + 2) != 0) {
			level = (int) lua_tointeger(L, arg + 2);
			lua_pop(L, 1);
		} else {
			level = (L == L1) ? 1 : 0; // level 0 may be this own function
		}
		if (lua_gettop(L) == arg) {
			lua_pushliteral(L, new CharPtr(""));
		} else if (lua_isstring(L, arg + 1) == 0) // message is not a string
		{
			return 1;
		} else {
			lua_pushliteral(L, new CharPtr("\n"));
		}
		lua_pushliteral(L, new CharPtr("stack traceback:"));
		while (lua_getstack(L1, level++, ar) != 0) {
			if (level > LEVELS1 && firstpart) {
				/* no more than `LEVELS2' more levels? */
				if (lua_getstack(L1, level + LEVELS2, ar) == 0) {
					level--; // keep going
				} else {
					lua_pushliteral(L, new CharPtr("\n\t...")); // too many
																// levels
					while (lua_getstack(L1, level + LEVELS2, ar) != 0) // find
																		// last
																		// levels
					{
						level++;
					}
				}
				firstpart = false;
				continue;
			}
			lua_pushliteral(L, new CharPtr("\n\t"));
			lua_getinfo(L1, new CharPtr("Snl"), ar);
			lua_pushfstring(L, new CharPtr("%s:"), ar.short_src);
			if (ar.currentline > 0) {
				lua_pushfstring(L, "%d:", ar.currentline);
			}
			if (!CharPtr.OpEquality(ar.namewhat, '\0'))// if (ar.namewhat !=
														// '\0') // is there a
														// name?
			{
				lua_pushfstring(L, " in function " + getLUA_QS(), ar.name);
			} else {
				if (CharPtr.OpEquality(ar.what, 'm'))// if (ar.what == 'm') //
														// main?
				{
					lua_pushfstring(L, " in main chunk");
				} else if (CharPtr.OpEquality(ar.what, 'C')
						|| CharPtr.OpEquality(ar.what, 't'))// else if (ar.what
															// == 'C' || ar.what
															// == 't')
				{
					lua_pushliteral(L, " ?"); // C function or tail call
				} else {
					lua_pushfstring(L, " in function <%s:%d>", ar.short_src,
							ar.linedefined);
				}
			}
			lua_concat(L, lua_gettop(L) - arg);
		}
		lua_concat(L, lua_gettop(L) - arg);
		return 1;
	}

	private final static luaL_Reg[] dblib = {
			new luaL_Reg("debug", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_debug((lua_State)arg);
				}
			}),
			new luaL_Reg("getfenv", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_getfenv((lua_State)arg);
				}
			}),
			new luaL_Reg("gethook", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_gethook((lua_State)arg);
				}
			}),
			new luaL_Reg("getinfo", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_getinfo((lua_State)arg);
				}
			}),
			new luaL_Reg("getlocal", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_getlocal((lua_State)arg);
				}
			}),
			new luaL_Reg("getregistry", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_getregistry((lua_State)arg);
				}
			}),
			new luaL_Reg("getmetatable", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_getmetatable((lua_State)arg);
				}
			}),
			new luaL_Reg("getupvalue", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_getupvalue((lua_State)arg);
				}
			}),
			new luaL_Reg("setfenv", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_setfenv((lua_State)arg);
				}
			}),
			new luaL_Reg("sethook", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_sethook((lua_State)arg);
				}
			}),
			new luaL_Reg("setlocal", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_setlocal((lua_State)arg);
				}
			}),
			new luaL_Reg("setmetatable", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_setmetatable((lua_State)arg);
				}
			}),
			new luaL_Reg("setupvalue", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_setupvalue((lua_State)arg);
				}
			}),
			new luaL_Reg("traceback", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return db_errorfb((lua_State)arg);
				}
			}), new luaL_Reg((String)null, (String)null) };

	public static int luaopen_debug(lua_State L) {
		luaL_register(L, LUA_DBLIBNAME, dblib);
		return 1;
	}

	public static int pcRel(InstructionPtr pc, Proto p) {
		assert pc.codes == p.code;
		return pc.pc - 1;
	}

	public static int getline(Proto f, int pc) {
		return (f.lineinfo != null && pc < f.lineinfo.length) ? f.lineinfo[pc] : 0;
	}

	public static void resethookcount(lua_State L) {
		L.hookcount = L.basehookcount;
	}

	public static int currentpc(lua_State L, CallInfo ci) {
		if (!isLua(ci)) // function is not a Lua function?
		{
			return -1;
		}
		if (ci == L.ci) {
			ci.savedpc = InstructionPtr.Assign(L.savedpc);
		}
		return pcRel(ci.savedpc, ci_func(ci).l.p);
	}

	public static int currentline(lua_State L, CallInfo ci) {
		int pc = currentpc(L, ci);
		if (pc < 0) {
			return -1; // only active lua functions have current-line
						// information
		} else {
			return getline(ci_func(ci).l.p, pc);
		}
	}

	/*
	 * * this function can be called asynchronous (e.g. during a signal)
	 */
	// public static int lua_sethook(lua_State L, lua_Hook func, int mask, int
	// count)
	public static int lua_sethook(lua_State L, IDelegate func, int mask,
			int count) {
		if (func == null || mask == 0) // turn off hooks?
		{
			mask = 0;
			func = null;
		}
		L.hook = func;
		L.basehookcount = count;
		resethookcount(L);
		L.hookmask = cast_byte(mask);
		return 1;
	}

	// public static lua_Hook lua_gethook(lua_State L)
	public static IDelegate lua_gethook(lua_State L) {
		return L.hook;
	}

	public static int lua_gethookmask(lua_State L) {
		return L.hookmask;
	}

	public static int lua_gethookcount(lua_State L) {
		return L.basehookcount;
	}

	public static int lua_getstack(lua_State L, int level, lua_Debug ar) {
		int status;
		CallInfo ci = new CallInfo();
		lua_lock(L);
		RefObject<CallInfo> reftype = new RefObject<Lua.CallInfo>(ci);
		for (reftype.argvalue = L.ci; level > 0
				&& CallInfo.OpGreaterThan(reftype.argvalue, L.base_ci[0]); CallInfo
				.dec(reftype)) {
			level--;
			if (f_isLua(reftype.argvalue)) // Lua function?
			{
				level -= reftype.argvalue.tailcalls; // skip lost tail calls
			}
		}
		ci = reftype.argvalue;
		if (level == 0 && CallInfo.OpGreaterThan(ci, L.base_ci[0]))// ci >
																	// L.base_ci[0])
																	// // level
																	// found?
		{
			status = 1;
			ar.i_ci = CallInfo.OpSubtraction(ci, L.base_ci[0]);// ar.i_ci = ci -
																// L.base_ci[0];
		} else if (level < 0) // level is of a lost tail call?
		{
			status = 1;
			ar.i_ci = 0;
		} else // no such level
		{
			status = 0;
		}
		lua_unlock(L);
		return status;
	}

	public static Proto getluaproto(CallInfo ci) {
		return (isLua(ci) ? ci_func(ci).l.p : null);
	}

	public static CharPtr findlocal(lua_State L, CallInfo ci, int n) {
		CharPtr name;
		Proto fp = getluaproto(ci);
		if ((fp != null)
				&& (name = luaF_getlocalname(fp, n, currentpc(L, ci))) != null) {
			return name; // is a local variable in a Lua function
		} else {
			lua_TValue limit = (ci == L.ci) ? L.top : CallInfo
					.OpAddition(ci, 1).func;// lua_TValue limit = (ci == L.ci) ?
											// L.top : (ci+1).func;
			if (lua_TValue.OpSubtraction(limit, ci.base_) >= n && n > 0) // is
																			// 'n'
																			// inside
																			// 'ci'
																			// stack?
			{
				return new CharPtr("(*temporary)");
			} else {
				return null;
			}
		}
	}

	public static CharPtr lua_getlocal(lua_State L, lua_Debug ar, int n) {
		CallInfo ci = L.base_ci[ar.i_ci];
		CharPtr name = findlocal(L, ci, n);
		lua_lock(L);
		if (name != null) {
			luaA_pushobject(L, ci.base_.getItem(n - 1));
		}
		lua_unlock(L);
		return name;
	}

	public static CharPtr lua_setlocal(lua_State L, lua_Debug ar, int n) {
		CallInfo ci = L.base_ci[ar.i_ci];
		CharPtr name = findlocal(L, ci, n);
		lua_lock(L);
		if (name != null) {
			setobjs2s(L, ci.base_.getItem(n - 1),
					Lua.lua_TValue.OpSubtraction(L.top, 1));
		}
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top); // pop value
		L.top = tempRef_top.argvalue;
		lua_unlock(L);
		return name;
	}

	public static void funcinfo(lua_Debug ar, Closure cl) {
		if (cl.c.getisC() != 0) {
			ar.source = new CharPtr("=[C]");
			ar.linedefined = -1;
			ar.lastlinedefined = -1;
			ar.what = new CharPtr("C");
		}
		else if(cl.l.p == null)
		{
			ar.source = new CharPtr("=[P]");
			ar.linedefined = -1;
			ar.lastlinedefined = -1;
			ar.what = new CharPtr("P");
		} else {
			ar.source = getstr(cl.l.p.source);
			ar.linedefined = cl.l.p.linedefined;
			ar.lastlinedefined = cl.l.p.lastlinedefined;
			ar.what = (ar.linedefined == 0) ? new CharPtr("main")
					: new CharPtr("Lua");
		}
		luaO_chunkid(ar.short_src, ar.source, LUA_IDSIZE);
	}

	public static void info_tailcall(lua_Debug ar) {
		ar.name = ar.namewhat = new CharPtr("");
		ar.what = new CharPtr("tail");
		ar.lastlinedefined = ar.linedefined = ar.currentline = -1;
		ar.source = new CharPtr("=(tail call)");
		luaO_chunkid(ar.short_src, ar.source, LUA_IDSIZE);
		ar.nups = 0;
	}

	public static void collectvalidlines(lua_State L, Closure f) {
		if (f == null || (f.c.getisC() != 0)) {
			setnilvalue(L.top);
		} else {
			Table t = luaH_new(L, 0, 0);
			Integer[] lineinfo = f.l.p.lineinfo;
			int i;
			for (i = 0; i < f.l.p.sizelineinfo; i++) {
				setbvalue(luaH_setnum(L, t, lineinfo[i]), 1);
			}
			sethvalue(L, L.top, t);
		}
		incr_top(L);
	}

	public static int auxgetinfo(lua_State L, CharPtr what, lua_Debug ar,
			Closure f, CallInfo ci) {
		int status = 1;
		if (f == null) {
			info_tailcall(ar);
			return status;
		}
		for (; what.getItem(0) != 0; what = what.next()) {
			switch (what.getItem(0)) {
			case 'S': {
				funcinfo(ar, f);
				break;
			}
			case 'l': {
				ar.currentline = (ci != null) && (ci.index != -1) ? currentline(L, ci) : -1;
				break;
			}
			case 'u': {
				ar.nups = f.c.getnupvalues();
				break;
			}
			case 'n': {
				RefObject<CharPtr> tempRef_name = new RefObject<CharPtr>(
						ar.name);
				ar.namewhat = ((ci != null) && (ci.index != -1)) ? getfuncname(L, ci, tempRef_name)
						: null;
				ar.name = tempRef_name.argvalue;
				if (ar.namewhat == null) {
					ar.namewhat = new CharPtr(""); // not found
					ar.name = null;
				}
				break;
			}
			case 'L':
			case 'f': // handled by lua_getinfo
				break;
			default: // invalid option
				status = 0;
				break;
			}
		}
		return status;
	}

	public static int lua_getinfo(lua_State L, CharPtr what, lua_Debug ar) {
		int status;
		Closure f = new Closure();
		CallInfo ci = new CallInfo();
		lua_lock(L);
		if (CharPtr.OpEquality(what, '>'))// if (what == '>')
		{
			lua_TValue func = Lua.lua_TValue.OpSubtraction(L.top, 1);
			luai_apicheck(L, ttisfunction(func));
			what = what.next(); // skip the '>'
			f = clvalue(func);
			RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
					L.top);
			lua_TValue.dec(tempRef_top); // pop function
			L.top = tempRef_top.argvalue;
		} else if (ar.i_ci != 0) // no tail call?
		{
			ci = L.base_ci[ar.i_ci];
			lua_assert(ttisfunction(ci.func));
			f = clvalue(ci.func);
		}
		status = auxgetinfo(L, what, ar, f, ci);
		if (strchr(what, 'f') != null) {
			if (f == null) {
				setnilvalue(L.top);
			} else {
				setclvalue(L, L.top, f);
			}
			incr_top(L);
		}
		if (strchr(what, 'L') != null) {
			collectvalidlines(L, f);
		}
		lua_unlock(L);
		return status;
	}

	public static int lua_getinfo(lua_State L, String whatS, lua_Debug ar) {
		CharPtr what = new CharPtr(whatS);
		int status;
		Closure f = null;
		CallInfo ci = null;
		lua_lock(L);
		if (CharPtr.OpEquality(what, '>'))// if (what == '>')
		{
			lua_TValue func = Lua.lua_TValue.OpSubtraction(L.top, 1);
			luai_apicheck(L, ttisfunction(func));
			what = what.next(); // skip the '>'
			f = clvalue(func);
			RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
					L.top);
			lua_TValue.dec(tempRef_top); // pop function
			L.top = tempRef_top.argvalue;
		} else if (ar.i_ci != 0) // no tail call?
		{
			ci = L.base_ci[ar.i_ci];
			lua_assert(ttisfunction(ci.func));
			f = clvalue(ci.func);
		}
		status = auxgetinfo(L, what, ar, f, ci);
		if (strchr(what, 'f') != null) {
			if (f == null) {
				setnilvalue(L.top);
			} else {
				setclvalue(L, L.top, f);
			}
			incr_top(L);
		}
		if (strchr(what, 'L') != null) {
			collectvalidlines(L, f);
		}
		lua_unlock(L);
		return status;
	}

	/*
	 * * {======================================================* Symbolic
	 * Execution and code checker*
	 * =======================================================
	 */

	public static int checkjump(Proto pt, int pc) {
		if (!(0 <= pc && pc < pt.sizecode)) {
			return 0;
		}
		return 1;
	}

	public static int checkreg(Proto pt, int reg) {
		if (!((reg) < (pt).maxstacksize)) {
			return 0;
		}
		return 1;
	}

	public static int precheck(Proto pt) {
		if (!(pt.maxstacksize <= MAXSTACK)) {
			return 0;
		}
		if (!(pt.numparams + (pt.is_vararg & VARARG_HASARG) <= pt.maxstacksize)) {
			return 0;
		}
		if (!(((pt.is_vararg & VARARG_NEEDSARG) == 0) || ((pt.is_vararg & VARARG_HASARG) != 0))) {
			return 0;
		}
		if (!(pt.sizeupvalues <= pt.nups)) {
			return 0;
		}
		if (!(pt.sizelineinfo == pt.sizecode || pt.sizelineinfo == 0)) {
			return 0;
		}
		if (!(pt.sizecode > 0 && GET_OPCODE(pt.code[pt.sizecode - 1]) == OpCode.OP_RETURN)) {
			return 0;
		}
		return 1;
	}

	public static int checkopenop(Proto pt, int pc) {
		return luaG_checkopenop(pt.code[pc + 1]);
	}

	public static int luaG_checkopenop(long i) {
		switch (GET_OPCODE(i)) {
		case OP_CALL:
		case OP_TAILCALL:
		case OP_RETURN:
		case OP_SETLIST: {
			if (!(GETARG_B(i) == 0)) {
				return 0;
			}
			return 1;
		}
		default: // invalid int after an open call
			return 0;
		}
	}

	public static int checkArgMode(Proto pt, int r, OpArgMask mode) {
		switch (mode) {
		case OpArgN:
			if (r != 0) {
				return 0;
			}
			break;
		case OpArgU:
			break;
		case OpArgR:
			checkreg(pt, r);
			break;
		case OpArgK:
			if (!((ISK(r) != 0) ? INDEXK(r) < pt.sizek : r < pt.maxstacksize)) {
				return 0;
			}
			break;
		}
		return 1;
	}

	public static long symbexec(Proto pt, int lastpc, int reg) {
		int pc;
		int last; // stores position of last int that changed `reg'
		int dest;
		last = pt.sizecode - 1; // points to final return (a `neutral' int)
		if (precheck(pt) == 0) {
			return 0;
		}
		for (pc = 0; pc < lastpc; pc++) {
			long i = pt.code[pc];
			OpCode op = GET_OPCODE(i);
			int a = GETARG_A(i);
			int b = 0;
			int c = 0;
			if (!(op.getValue() < NUM_OPCODES)) {
				return 0;
			}
			checkreg(pt, a);
			switch (getOpMode(op)) {
			case iABC: {
				b = GETARG_B(i);
				c = GETARG_C(i);
				if (checkArgMode(pt, b, getBMode(op)) == 0) {
					return 0;
				}
				if (checkArgMode(pt, c, getCMode(op)) == 0) {
					return 0;
				}
				break;
			}
			case iABx: {
				b = GETARG_Bx(i);
				if (getBMode(op) == OpArgMask.OpArgK) {
					if (!(b < pt.sizek)) {
						return 0;
					}
				}
				break;
			}
			case iAsBx: {
				b = GETARG_sBx(i);
				if (getBMode(op) == OpArgMask.OpArgR) {
					dest = pc + 1 + b;
					if (!((0 <= dest && dest < pt.sizecode))) {
						return 0;
					}
					if (dest > 0) {
						int j;
						/*
						 * check that it does not jump to a setlist count; this
						 * is tricky, because the count from a previous setlist
						 * may have the same value of an invalid setlist; so, we
						 * must go all the way back to the first of them (if
						 * any)
						 */
						for (j = 0; j < dest; j++) {
							long d = pt.code[dest - 1 - j];
							if (!(GET_OPCODE(d) == OpCode.OP_SETLIST && GETARG_C(d) == 0)) {
								break;
							}
						}
						/*
						 * if 'j' is even, previous value is not a setlist (even
						 * if it looks like one)
						 */
						if ((j & 1) != 0) {
							return 0;
						}
					}
				}
				break;
			}
			}
			if (testAMode(op) != 0) {
				if (a == reg) // change register `a'
				{
					last = pc;
				}
			}
			if (testTMode(op) != 0) {
				if (!(pc + 2 < pt.sizecode)) // check skip
				{
					return 0;
				}
				if (!(GET_OPCODE(pt.code[pc + 1]) == OpCode.OP_JMP)) {
					return 0;
				}
			}
			switch (op) {
			case OP_LOADBOOL: {
				if (c == 1) // does it jump?
				{
					if (!(pc + 2 < pt.sizecode)) // check its jump
					{
						return 0;
					}
					if (!(GET_OPCODE(pt.code[pc + 1]) != OpCode.OP_SETLIST || GETARG_C(pt.code[pc + 1]) != 0)) {
						return 0;
					}
				}
				break;
			}
			case OP_LOADNIL: {
				if (a <= reg && reg <= b) {
					last = pc; // set registers from `a' to `b'
				}
				break;
			}
			case OP_GETUPVAL:
			case OP_SETUPVAL: {
				if (!(b < pt.nups)) {
					return 0;
				}
				break;
			}
			case OP_GETGLOBAL:
			case OP_SETGLOBAL: {
				if (!(ttisstring(pt.k[b]))) {
					return 0;
				}
				break;
			}
			case OP_SELF: {
				checkreg(pt, a + 1);
				if (reg == a + 1) {
					last = pc;
				}
				break;
			}
			case OP_CONCAT: {
				if (!(b < c)) // at least two operands
				{
					return 0;
				}
				break;
			}
			case OP_TFORLOOP: {
				if (!(c >= 1)) // at least one result (control variable)
				{
					return 0;
				}
				checkreg(pt, a + 2 + c); // space for results
				if (reg >= a + 2) // affect all regs above its base
				{
					last = pc;
				}
				break;
			}
			case OP_FORLOOP:
			case OP_FORPREP:
				checkreg(pt, a + 3);
				/*
				 * go through ...no, on second thoughts don't, because this is
				 * C#
				 */
				dest = pc + 1 + b;
				/* not full check and jump is forward and do not skip `lastpc'? */
				if (reg != NO_REG && pc < dest && dest <= lastpc) {
					pc += b; // do the jump
				}
				break;

			case OP_JMP: {
				dest = pc + 1 + b;
				/* not full check and jump is forward and do not skip `lastpc'? */
				if (reg != NO_REG && pc < dest && dest <= lastpc) {
					pc += b; // do the jump
				}
				break;
			}
			case OP_CALL:
			case OP_TAILCALL: {
				if (b != 0) {
					checkreg(pt, a + b - 1);
				}
				c--; // c = num. returns
				if (c == LUA_MULTRET) {
					if (checkopenop(pt, pc) == 0) {
						return 0;
					}
				} else if (c != 0) {
					checkreg(pt, a + c - 1);
				}
				if (reg >= a) // affect all registers above base
				{
					last = pc;
				}
				break;
			}
			case OP_RETURN: {
				b--; // b = num. returns
				if (b > 0) {
					checkreg(pt, a + b - 1);
				}
				break;
			}
			case OP_SETLIST: {
				if (b > 0) {
					checkreg(pt, a + b);
				}
				if (c == 0) {
					pc++;
					if (!(pc < pt.sizecode - 1)) {
						return 0;
					}
				}
				break;
			}
			case OP_CLOSURE: {
				int nup, j;
				if (!(b < pt.sizep)) {
					return 0;
				}
				nup = pt.p[b].nups;
				if (!(pc + nup < pt.sizecode)) {
					return 0;
				}
				for (j = 1; j <= nup; j++) {
					OpCode op1 = GET_OPCODE(pt.code[pc + j]);
					if (!(op1 == OpCode.OP_GETUPVAL || op1 == OpCode.OP_MOVE)) {
						return 0;
					}
				}
				if (reg != NO_REG) // tracing?
				{
					pc += nup; // do not 'execute' these pseudo-instructions
				}
				break;
			}
			case OP_VARARG: {
				if (!((pt.is_vararg & VARARG_ISVARARG) != 0 && (pt.is_vararg & VARARG_NEEDSARG) == 0)) {
					return 0;
				}
				b--;
				if (b == LUA_MULTRET) {
					if (checkopenop(pt, pc) == 0) {
						return 0;
					}
				}
				checkreg(pt, a + b - 1);
				break;
			}
			default:
				break;
			}
		}
		return pt.code[last];
	}

	// /#undef check
	// /#undef checkjump
	// /#undef checkreg

	/* }====================================================== */

	public static int luaG_checkcode(Proto pt) {
		return (symbexec(pt, pt.sizecode, NO_REG) != 0) ? 1 : 0;
	}

	public static CharPtr kname(Proto p, int c) {
		if (ISK(c) != 0 && ttisstring(p.k[INDEXK(c)])) {
			return svalue(p.k[INDEXK(c)]);
		} else {
			return new CharPtr("?");
		}
	}

	public static CharPtr getobjname(lua_State L, CallInfo ci, int stackpos,
			RefObject<CharPtr> name) {
		if (isLua(ci)) // a Lua function?
		{
			Proto p = ci_func(ci).l.p;
			int pc = currentpc(L, ci);
			long i;
			name.argvalue = luaF_getlocalname(p, stackpos + 1, pc);
			if (name.argvalue != null) // is a local?
			{
				return new CharPtr("local");
			}
			i = symbexec(p, pc, stackpos); // try symbolic execution
			lua_assert(pc != -1);
			switch (GET_OPCODE(i)) {
			case OP_GETGLOBAL: {
				int g = GETARG_Bx(i); // global index
				lua_assert(ttisstring(p.k[g]));
				name.argvalue = svalue(p.k[g]);
				return new CharPtr("global");
			}
			case OP_MOVE: {
				int a = GETARG_A(i);
				int b = GETARG_B(i); // move from `b' to `a'
				if (b < a) {
					return getobjname(L, ci, b, name); // get name for `b'
				}
				break;
			}
			case OP_GETTABLE: {
				int k = GETARG_C(i); // key index
				name.argvalue = kname(p, k);
				return new CharPtr("field");
			}
			case OP_GETUPVAL: {
				int u = GETARG_B(i); // upvalue index
				name.argvalue = (CharPtr) ((p.upvalues != null) ? getstr(p.upvalues[u])
						: new CharPtr("?"));
				return new CharPtr("upvalue");
			}
			case OP_SELF: {
				int k = GETARG_C(i); // key index
				name.argvalue = kname(p, k);
				return new CharPtr("method");
			}
			default:
				break;
			}
		}
		return null; // no useful name found
	}

	public static CharPtr getfuncname(lua_State L, CallInfo ci,
			RefObject<CharPtr> name) {
		long i;
		if ((isLua(ci) && ci.tailcalls > 0)
				|| !isLua(CallInfo.OpSubtraction(ci, 1)))// if ((isLua(ci) &&
															// ci.tailcalls > 0)
															// || !isLua(ci -
															// 1))
		{
			return null; // calling function is not Lua (or is unknown)
		}
		RefObject<CallInfo> tempRef_ci = new RefObject<CallInfo>(ci);
		CallInfo.dec(tempRef_ci); // calling function
		ci = tempRef_ci.argvalue;
		int valCurPc = currentpc(L, ci);
		if(valCurPc < 0)
			return null;
		i = ci_func(ci).l.p.code[valCurPc];
		if (GET_OPCODE(i) == OpCode.OP_CALL
				|| GET_OPCODE(i) == OpCode.OP_TAILCALL
				|| GET_OPCODE(i) == OpCode.OP_TFORLOOP) {
			return getobjname(L, ci, GETARG_A(i), name);
		} else {
			return null; // no useful name can be found
		}
	}

	/* only ANSI way to check whether a pointer points to an array */
	public static int isinstack(CallInfo ci, lua_TValue o) {
		lua_TValue p = null;
		RefObject<lua_TValue> refArg = new RefObject<Lua.lua_TValue>(p);
		for (refArg.argvalue = ci.base_; lua_TValue.OpLessThan(refArg.argvalue,
				ci.top); lua_TValue.inc(refArg))// for (p = ci.base_; p <
												// ci.top; lua_TValue.inc(p))
		{
			if (o == refArg.argvalue) {
				return 1;
			}
		}
		p = refArg.argvalue;
		return 0;
	}

	public static void luaG_typeerror(lua_State L, lua_TValue o, CharPtr op) {
		CharPtr name = null;
		CharPtr t = luaT_typenames[ttype(o)];
		RefObject<CharPtr> tempRef_name = new RefObject<CharPtr>(name);
		CharPtr kind = (isinstack(L.ci, o)) != 0 ? getobjname(L, L.ci,
				cast_int(lua_TValue.OpSubtraction(o, L.base_)), tempRef_name)
				: null;// CharPtr kind = (isinstack(L.ci, o)) != 0 ?
						// getobjname(L, L.ci, cast_int(o - L.base_),
						// tempRef_name) : null;
		name = tempRef_name.argvalue;
		if (kind != null) {
			luaG_runerror(L, "attempt to %s %s " + getLUA_QS()
					+ " (a %s value)", op, kind, name, t);
		} else {
			luaG_runerror(L, "attempt to %s a %s value", op, t);
		}
	}

	public static void luaG_typeerror(lua_State L, lua_TValue o, String opS) {
		CharPtr op = new CharPtr(opS);
		CharPtr name = null;
		CharPtr t = luaT_typenames[ttype(o)];
		RefObject<CharPtr> tempRef_name = new RefObject<CharPtr>(name);
		CharPtr kind = (isinstack(L.ci, o)) != 0 ? getobjname(L, L.ci,
				cast_int(lua_TValue.OpSubtraction(o, L.base_)), tempRef_name)
				: null;// CharPtr kind = (isinstack(L.ci, o)) != 0 ?
						// getobjname(L, L.ci, cast_int(o - L.base_),
						// tempRef_name) : null;
		name = tempRef_name.argvalue;
		if (kind != null) {
			luaG_runerror(L, "attempt to %s %s " + getLUA_QS()
					+ " (a %s value)", op, kind, name, t);
		} else {
			luaG_runerror(L, "attempt to %s a %s value", op, t);
		}
	}

	public static void luaG_concaterror(lua_State L, lua_TValue p1,
			lua_TValue p2) {
		if (ttisstring(p1) || ttisnumber(p1)) {
			p1 = p2;
		}
		lua_assert(!ttisstring(p1) && !ttisnumber(p1));
		luaG_typeerror(L, p1, "concatenate");
	}

	public static void luaG_aritherror(lua_State L, lua_TValue p1, lua_TValue p2) {
		lua_TValue temp = new lua_TValue();
		if (luaV_tonumber(p1, temp) == null) {
			p2 = p1; // first operand is wrong
		}
		luaG_typeerror(L, p2, "perform arithmetic on");
	}

	public static int luaG_ordererror(lua_State L, lua_TValue p1, lua_TValue p2) {
		CharPtr t1 = luaT_typenames[ttype(p1)];
		CharPtr t2 = luaT_typenames[ttype(p2)];
		if (t1.getItem(2) == t2.getItem(2)) {
			luaG_runerror(L, "attempt to compare two %s values", t1);
		} else {
			luaG_runerror(L, "attempt to compare %s with %s", t1, t2);
		}
		return 0;
	}

	public static void addinfo(lua_State L, CharPtr msg) {
		CallInfo ci = L.ci;
		if (isLua(ci)) // is Lua code?
		{
			CharPtr buff = new CharPtr(new char[LUA_IDSIZE]); // add file:line
																// information
			int line = currentline(L, ci);
			luaO_chunkid(buff, getstr(getluaproto(ci).source), LUA_IDSIZE);
			luaO_pushfstring(L, "%s:%d: %s", buff, line, msg);
		}
	}

	public static void luaG_errormsg(lua_State L) {
		if (L.errfunc != 0) // is there an error handling function?
		{
			lua_TValue errfunc = restorestack(L, L.errfunc);
			if (!ttisfunction(errfunc)) {
				luaD_throw(L, LUA_ERRERR);
			}
			setobjs2s(L, L.top, Lua.lua_TValue.OpSubtraction(L.top, 1)); // move
																			// argument
			setobjs2s(L, Lua.lua_TValue.OpSubtraction(L.top, 1), errfunc); // push
																			// function
			incr_top(L);
			luaD_call(L, Lua.lua_TValue.OpSubtraction(L.top, 2), 1); // call it
		}
		luaD_throw(L, LUA_ERRRUN);
	}

	public static void luaG_runerror(lua_State L, CharPtr fmt, Object... argp) {
		addinfo(L, luaO_pushvfstring(L, fmt, argp));
		luaG_errormsg(L);
	}

	public static void luaG_runerror(lua_State L, String fmt, Object... argp) {
		addinfo(L, luaO_pushvfstring(L, new CharPtr(fmt), argp));
		luaG_errormsg(L);
	}

	public static void luaD_checkstack(lua_State L, int n) {
		if ((Lua.lua_TValue.OpSubtraction(L.stack_last, L.top)) <= n) {
			luaD_growstack(L, n);
		} else {
			// FUCK TODO TASK: There is no preprocessor in Java:
			// #if HARDSTACKTESTS
			//luaD_reallocstack(L, L.stacksize - EXTRA_STACK - 1);
			// #endif
		}
	}

	public static void incr_top(lua_State L) {
		luaD_checkstack(L, 1);
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.inc(tempRef_top);
		L.top = tempRef_top.argvalue;
	}

	// in the original C code these values save and restore the stack by number
	// of bytes. marshalling sizeof
	// isn't that straightforward in managed languages, so i implement these by
	// index instead.
	public static int savestack(lua_State L, lua_TValue p) {
		return p.index;
	}

	public static lua_TValue restorestack(lua_State L, int n) {
		return L.stack[n];
	}

	public static int saveci(lua_State L, CallInfo p) {
		return CallInfo.OpSubtraction(p, L.base_ci);// return p - L.base_ci;
	}

	public static CallInfo restoreci(lua_State L, int n) {
		return L.base_ci[n];
	}

	/* results from luaD_precall */
	public static final int PCRLUA = 0; // initiated a call to a Lua function
	public static final int PCRC = 1; // did a call to a C function
	public static final int PCRYIELD = 2; // C funtion yielded

	/*
	 * * {======================================================* Error-recovery
	 * functions* =======================================================
	 */

	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate void luai_jmpbuf(int b);
	public Delegator luai_jmpbuf = new Delegator(new Class[] { int.class },
			void.class);

	/* chain list of long jump buffers */
	public static class lua_longjmp {
		public lua_longjmp previous;
		public Delegator b;// public luai_jmpbuf b;
		public volatile int status; // error code
	}

	public static void luaD_seterrorobj(lua_State L, int errcode,
			lua_TValue oldtop) {
		switch (errcode) {
		case LUA_ERRMEM: {
			setsvalue2s(L, oldtop, luaS_newliteral(L, MEMERRMSG));
			break;
		}
		case LUA_ERRERR: {
			setsvalue2s(L, oldtop,
					luaS_newliteral(L, "error in error handling"));
			break;
		}
		case LUA_ERRSYNTAX:
		case LUA_ERRRUN: {
			setobjs2s(L, oldtop, Lua.lua_TValue.OpSubtraction(L.top, 1)); // error
																			// message
																			// on
																			// current
																			// top
			break;
		}
		}
		L.top = lua_TValue.OpAddition(oldtop, 1);// L.top = oldtop + 1;
	}

	public static void restore_stack_limit(lua_State L) {
		lua_assert(L.stack_last.index == L.stacksize - EXTRA_STACK - 1);
		if (L.size_ci > LUAI_MAXCALLS) // there was an overflow?
		{
			int inuse = CallInfo.OpSubtraction(L.ci, L.base_ci);// int inuse =
																// L.ci -
																// L.base_ci;
			if (inuse + 1 < LUAI_MAXCALLS) // can `undo' overflow?
			{
				luaD_reallocCI(L, LUAI_MAXCALLS);
			}
		}
	}

	public static void resetstack(lua_State L, int status) {
		L.ci = L.base_ci[0];
		L.base_ = L.ci.base_;
		luaF_close(L, L.base_); // close eventual pending closures
		luaD_seterrorobj(L, status, L.base_);
		L.nCcalls = L.baseCcalls;
		L.allowhook = 1;
		restore_stack_limit(L);
		L.errfunc = 0;
		L.errorJmp = null;
	}

	public static void luaD_throw(lua_State L, int errcode) {
		if (L.errorJmp != null) {
			L.errorJmp.status = errcode;
			LUAI_THROW(L, L.errorJmp);
		} else {
			L.status = cast_byte(errcode);
			if (G(L).panic != null) {
				resetstack(L, errcode);
				lua_unlock(L);
				G(L).panic.invoke(L);
			}
			// FUCK TODO TASK: There is no preprocessor in Java:
			// #if XBOX
			// throw new ApplicationException();
			// #elif PocketPC
			// throw new AndroidException();
			// #else
			// System.exit(EXIT_FAILURE);
			// #endif
		}
	}

	public static int luaD_rawrunprotected(lua_State L, IDelegate f, Object ud) {
		lua_longjmp lj = new lua_longjmp();
		lj.status = 0;
		lj.previous = L.errorJmp; // chain new error handler
		L.errorJmp = lj;
		/*
		 * LUAI_TRY(L, lj, f(L, ud) );
		 */
		if(LuaEngine.getInstance().CatchExceptions())
		{
			try
			{
				f.invoke(L, ud);// f(L, ud);
			}
			catch (java.lang.Exception e) {
				Log.e("Lua Error", "Exception ", e);
				int count = 20;
				CharPtr msgP = Lua.lua_tostring(L, -1);
				if(msgP != null)
				{
					String lastMsg = "";
					String msg= msgP.toString();
					while(msg != null && count > 0)
					{
						Lua.lua_pop(L, -1);
						Log.e("LuaEngine", msg);
						msgP = Lua.lua_tostring(L, -1);
						if(msgP == null)
							break;
						msg = msgP.toString();
						if(msg.compareTo(lastMsg) == 0)
							break;
						lastMsg = msg;
						count--;
					}
				}
			    lua_Debug entry = new lua_Debug();
			    int depth = 0; 

			    while (lua_getstack(L, depth, entry) != 0)
				{
			        int status = lua_getinfo(L, "Sln", entry);
			        if(status == 0)
			        	break;
			        try
			        {
			        	StringBuilder sb = new StringBuilder();
			        	if(entry.short_src != null)
			        		sb.append(entry.short_src.toString());
			        	else
			        		sb.append("?");
			        	sb.append("(").append(entry.currentline).append("): ");
			        	if(entry.name != null)
			        		sb.append(entry.name.toString());
			        	else
			        		sb.append("?");
			        	Log.e("LuaEngineStack", sb.toString());
			        }
			        catch(Exception ex)
			        {
			        	
			        }
			        depth++;
			    }
				if (lj.status == 0) {
					lj.status = -1;
				}
			}
		}
		else
			f.invoke(L, ud);
		L.errorJmp = lj.previous; // restore old error handler
		return lj.status;
	}

	/* }====================================================== */

	public static void correctstack(lua_State L, lua_TValue[] oldstack) {
		/*
		 * don't need to do this CallInfo ci; GCObject up; L.top = L.stack[L.top
		 * - oldstack]; for (up = L.openupval; up != null; up = up.gch.next)
		 * gco2uv(up).v = L.stack[gco2uv(up).v - oldstack]; for (ci =
		 * L.base_ci[0]; ci <= L.ci; CallInfo.inc(ref ci)) { ci.top =
		 * L.stack[ci.top - oldstack]; ci.base_ = L.stack[ci.base_ - oldstack];
		 * ci.func = L.stack[ci.func - oldstack]; } L.base_ = L.stack[L.base_ -
		 * oldstack];
		 */
	}

	public static void luaD_reallocstack(lua_State L, int newsize) {
		lua_TValue[] oldstack = L.stack;
		int realsize = newsize + 1 + EXTRA_STACK;
		lua_assert(L.stack_last.index == L.stacksize - EXTRA_STACK - 1);
		RefObject<lua_TValue[]> tempRef_stack = new RefObject<lua_TValue[]>(
				L.stack);
		luaM_reallocvector(L, tempRef_stack, L.stacksize, realsize,
				lua_TValue.class); // , lua_TValue
		L.stack = tempRef_stack.argvalue;
		L.stacksize = realsize;
		L.stack_last = L.stack[newsize];
		correctstack(L, oldstack);
	}

	public static void luaD_reallocCI(lua_State L, int newsize) {
		CallInfo oldci = L.base_ci[0];
		RefObject<CallInfo[]> tempRef_base_ci = new RefObject<CallInfo[]>(
				L.base_ci);
		luaM_reallocvector(L, tempRef_base_ci, L.size_ci, newsize,
				CallInfo.class); // , CallInfo
		L.base_ci = tempRef_base_ci.argvalue;
		L.size_ci = newsize;
		L.ci = L.base_ci[CallInfo.OpSubtraction(L.ci, oldci)];// L.ci =
																// L.base_ci[L.ci
																// - oldci];
		L.end_ci = L.base_ci[L.size_ci - 1];
	}

	public static void luaD_growstack(lua_State L, int n) {
		if (n <= L.stacksize) // double size is enough?
		{
			luaD_reallocstack(L, 2 * L.stacksize);
		} else {
			luaD_reallocstack(L, L.stacksize + n);
		}
	}

	public static CallInfo growCI(lua_State L) {
		if (L.size_ci > LUAI_MAXCALLS) // overflow while handling overflow?
		{
			luaD_throw(L, LUA_ERRERR);
		} else {
			luaD_reallocCI(L, 2 * L.size_ci);
			if (L.size_ci > LUAI_MAXCALLS) {
				luaG_runerror(L, "stack overflow");
			}
		}
		RefObject<CallInfo> tempRef_ci = new RefObject<CallInfo>(L.ci);
		CallInfo.inc(tempRef_ci);
		L.ci = tempRef_ci.argvalue;
		return L.ci;
	}

	public static void luaD_callhook(lua_State L, int event_, int line) {
		IDelegate hook = L.hook;// lua_Hook hook = L.hook;
		if ((hook != null) && (L.allowhook != 0)) {
			int top = savestack(L, L.top);
			int ci_top = savestack(L, L.ci.top);
			lua_Debug ar = new lua_Debug();
			ar.event_ = event_;
			ar.currentline = line;
			if (event_ == LUA_HOOKTAILRET) {
				ar.i_ci = 0; // tail call; no debug information about it
			} else {
				ar.i_ci = CallInfo.OpSubtraction(L.ci, L.base_ci);// ar.i_ci =
																	// L.ci -
																	// L.base_ci;
			}
			luaD_checkstack(L, LUA_MINSTACK); // ensure minimum stack size
			L.ci.top = Lua.lua_TValue.OpAddition(L.top, LUA_MINSTACK);
			lua_assert(Lua.lua_TValue.OpLessThanOrEqual(L.ci.top, L.stack_last));
			L.allowhook = 0; // cannot call hooks inside a hook
			lua_unlock(L);
			hook.invoke(L, ar);// hook(L, ar);
			lua_lock(L);
			lua_assert(L.allowhook == 0);
			L.allowhook = 1;
			L.ci.top = restorestack(L, ci_top);
			L.top = restorestack(L, top);
		}
	}

	public static lua_TValue adjust_varargs(lua_State L, Proto p, int actual) {
		int i;
		int nfixargs = p.numparams;
		Table htab = null;
		lua_TValue base_, fixed_;
		for (; actual < nfixargs; ++actual) {
			RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
					L.top);
			setnilvalue(lua_TValue.inc(tempRef_top));
			L.top = tempRef_top.argvalue;
		}
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if LUA_COMPAT_VARARG
		if ((p.is_vararg & VARARG_NEEDSARG) != 0) // compat. with old-style
													// vararg?
		{
			int nvar = actual - nfixargs; // number of extra arguments
			lua_assert(p.is_vararg & VARARG_HASARG);
			luaC_checkGC(L);
			htab = luaH_new(L, nvar, 1); // create `arg' table
			for (i = 0; i < nvar; i++) // put extra arguments into `arg' table
			{
				setobj2n(
						L,
						luaH_setnum(L, htab, i + 1),
						lua_TValue.OpAddition(
								Lua.lua_TValue.OpSubtraction(L.top, nvar), i));// setobj2n(L,
																				// luaH_setnum(L,
																				// htab,
																				// i+1),
																				// Lua.lua_TValue.OpSubtraction(L.top,
																				// nvar)
																				// +
																				// i);
			}
			/* store counter in field `n' */
			setnvalue(luaH_setstr(L, htab, luaS_newliteral(L, "n")),
					cast_num(nvar));
		}
		// #endif
		/* move fixed parameters to final position */
		fixed_ = Lua.lua_TValue.OpSubtraction(L.top, actual); // first fixed
																// argument
		base_ = L.top; // final position of first argument
		for (i = 0; i < nfixargs; i++) {
			RefObject<Lua.lua_TValue> tempRef_top2 = new RefObject<Lua.lua_TValue>(
					L.top);
			setobjs2s(L, lua_TValue.inc(tempRef_top2),
					lua_TValue.OpAddition(fixed_, i));// setobjs2s(L,
														// lua_TValue.inc(tempRef_top2),
														// fixed_ + i);
			L.top = tempRef_top2.argvalue;
			setnilvalue(lua_TValue.OpAddition(fixed_, i));// setnilvalue(fixed_
															// + i);
		}
		/* add `arg' parameter */
		if (htab != null) {
			lua_TValue top = L.top;
			RefObject<Lua.lua_TValue> tempRef_top3 = new RefObject<Lua.lua_TValue>(
					L.top);
			lua_TValue.inc(tempRef_top3);
			L.top = tempRef_top3.argvalue;
			sethvalue(L, top, htab);
			lua_assert(iswhite(obj2gco(htab)));
		}
		return base_;
	}

	public static lua_TValue tryfuncTM(lua_State L, lua_TValue func) {
		/* const */
		lua_TValue tm = luaT_gettmbyobj(L, func, TMS.TM_CALL);
		lua_TValue p = null;
		int funcr = savestack(L, func);
		if (!ttisfunction(tm)) {
			luaG_typeerror(L, func, "call");
		}
		/* Open a hole inside the stack at `func' */
		RefObject<lua_TValue> refArgs = new RefObject<Lua.lua_TValue>(p);
		for (refArgs.argvalue = L.top; lua_TValue.OpGreaterThan(p, func); lua_TValue
				.dec(refArgs))// for (p = L.top; p > func; lua_TValue.dec(p))
		{
			setobjs2s(L, refArgs.argvalue,
					lua_TValue.OpSubtraction(refArgs.argvalue, 1));// setobjs2s(L,
																	// p, p -
																	// 1);
		}
		p = refArgs.argvalue;
		incr_top(L);
		func = restorestack(L, funcr); // previous call may change stack
		setobj2s(L, func, tm); // tag method is the new function to be called
		return func;
	}

	public static CallInfo inc_ci(lua_State L) {
		if (L.ci == L.end_ci) {
			return growCI(L);
		}
		// (condhardstacktests(luaD_reallocCI(L, L.size_ci)), ++L.ci))
		RefObject<CallInfo> tempRef_ci = new RefObject<CallInfo>(L.ci);
		CallInfo.inc(tempRef_ci);
		L.ci = tempRef_ci.argvalue;
		return L.ci;
	}

	public static int luaD_precall(lua_State L, lua_TValue func, int nresults) {
		LClosure cl;
		int funcr;
		if (!ttisfunction(func)) // `func' is not a function?
		{
			func = tryfuncTM(L, func); // check the `function' tag method
		}
		funcr = savestack(L, func);
		cl = clvalue(func).l;
		L.ci.savedpc = InstructionPtr.Assign(L.savedpc);
		if (cl.getisC() == 0) // Lua function? prepare its call
		{
			CallInfo ci;
			lua_TValue st = null, base_;
			Proto p = cl.p;
			luaD_checkstack(L, p.maxstacksize);
			func = restorestack(L, funcr);
			if (p.is_vararg == 0) // no varargs?
			{
				base_ = L.stack[lua_TValue.OpAddition(func, 1).index];// base_ =
																		// L.stack[func
																		// + 1];
				if (Lua.lua_TValue.OpGreaterThan(L.top,
						lua_TValue.OpAddition(base_, p.numparams)))// if
																	// (Lua.lua_TValue.OpGreaterThan(L.top,
																	// base_ +
																	// p.numparams))
				{
					L.top = lua_TValue.OpAddition(base_, p.numparams);// L.top =
																		// base_
																		// +
																		// p.numparams;
				}
			} else // vararg function
			{
				int nargs = Lua.lua_TValue.OpSubtraction(L.top, func) - 1;
				base_ = adjust_varargs(L, p, nargs);
				func = restorestack(L, funcr); // previous call may change the
												// stack
			}
			ci = inc_ci(L); // now `enter' new function
			ci.func = func;
			L.base_ = ci.base_ = base_;
			ci.top = Lua.lua_TValue.OpAddition(L.base_, p.maxstacksize);
			lua_assert(Lua.lua_TValue.OpLessThanOrEqual(ci.top, L.stack_last));
			L.savedpc = new InstructionPtr(p.code, 0); // starting point
			ci.tailcalls = 0;
			ci.nresults = nresults;
			RefObject<lua_TValue> refargs = new RefObject<Lua.lua_TValue>(st);
			for (refargs.argvalue = L.top; lua_TValue.OpLessThan(
					refargs.argvalue, ci.top); lua_TValue.inc(refargs))// for
																		// (st =
																		// L.top;
																		// st <
																		// ci.top;
																		// lua_TValue.inc(st))
			{
				setnilvalue(refargs.argvalue);
			}
			st = refargs.argvalue;
			L.top = ci.top;
			if ((L.hookmask & LUA_MASKCALL) != 0) {
				RefObject<InstructionPtr> tempRef_savedpc = new RefObject<InstructionPtr>(
						L.savedpc);
				InstructionPtr.inc(tempRef_savedpc); // hooks assume 'pc' is
														// already incremented
				L.savedpc = tempRef_savedpc.argvalue;
				luaD_callhook(L, LUA_HOOKCALL, -1);
				RefObject<InstructionPtr> tempRef_savedpc2 = new RefObject<InstructionPtr>(
						L.savedpc);
				InstructionPtr.dec(tempRef_savedpc2); // correct 'pc'
				L.savedpc = tempRef_savedpc2.argvalue;
			}
			return PCRLUA;
		} else // if is a C function, call it
		{
			CallInfo ci;
			int n;
			luaD_checkstack(L, LUA_MINSTACK); // ensure minimum stack size
			ci = inc_ci(L); // now `enter' new function
			ci.func = restorestack(L, funcr);
			L.base_ = ci.base_ = Lua.lua_TValue.OpAddition(ci.func, 1);
			ci.top = Lua.lua_TValue.OpAddition(L.top, LUA_MINSTACK);
			lua_assert(Lua.lua_TValue.OpLessThanOrEqual(ci.top, L.stack_last));
			ci.nresults = nresults;
			if ((L.hookmask & LUA_MASKCALL) != 0) {
				luaD_callhook(L, LUA_HOOKCALL, -1);
			}
			lua_unlock(L);
			n = (Integer) curr_func(L).c.f.invoke(L); // do the actual call
			lua_lock(L);
			if (n < 0) // yielding?
			{
				return PCRYIELD;
			} else {
				luaD_poscall(L, Lua.lua_TValue.OpSubtraction(L.top, n));
				return PCRC;
			}
		}
	}

	public static lua_TValue callrethooks(lua_State L, lua_TValue firstResult) {
		int fr = savestack(L, firstResult); // next call may change stack
		luaD_callhook(L, LUA_HOOKRET, -1);
		if (f_isLua(L.ci)) // Lua function?
		{
			while (((L.hookmask & LUA_MASKRET) != 0) && (L.ci.tailcalls-- != 0)) // tail
																					// calls
			{
				luaD_callhook(L, LUA_HOOKTAILRET, -1);
			}
		}
		return restorestack(L, fr);
	}

	public static int luaD_poscall(lua_State L, lua_TValue firstResult) {
		lua_TValue res;
		int wanted, i;
		CallInfo ci;
		if ((L.hookmask & LUA_MASKRET) != 0) {
			firstResult = callrethooks(L, firstResult);
		}
		RefObject<CallInfo> tempRef_ci = new RefObject<CallInfo>(L.ci);
		ci = CallInfo.dec(tempRef_ci);
		L.ci = tempRef_ci.argvalue;
		res = ci.func; // res == final position of 1st result
		wanted = ci.nresults;
		L.base_ = CallInfo.OpSubtraction(ci, 1).base_;// L.base_ = (ci -
														// 1).base_; // restore
														// base
		L.savedpc = InstructionPtr
				.Assign(CallInfo.OpSubtraction(ci, 1).savedpc); // L.savedpc =
																// InstructionPtr.Assign((ci
																// -
																// 1).savedpc);
																// // restore
																// savedpc
		/* move results to correct place */
		for (i = wanted; i != 0 && lua_TValue.OpLessThan(firstResult, L.top); i--)// for
																					// (i
																					// =
																					// wanted;
																					// i
																					// !=
																					// 0
																					// &&
																					// firstResult
																					// <
																					// L.top;
																					// i--)
		{
			setobjs2s(L, res, firstResult);
			res = lua_TValue.OpAddition(res, 1);// res = res + 1;
			firstResult = lua_TValue.OpAddition(firstResult, 1);// firstResult =
																// firstResult +
																// 1;
		}
		while (i-- > 0) {
			RefObject<lua_TValue> tempRef_res = new RefObject<lua_TValue>(res);
			setnilvalue(lua_TValue.inc(tempRef_res));
			res = tempRef_res.argvalue;
		}
		L.top = res;
		return (wanted - LUA_MULTRET); // 0 iff wanted == LUA_MULTRET
	}

	/*
	 * * Call a function (C or Lua). The function to be called is at *func.* The
	 * arguments are on the stack, right after the function.* When returns, all
	 * the results are on the stack, starting at the original* function
	 * position.
	 */
	public static void luaD_call(lua_State L, lua_TValue func, int nResults) {
		if (++L.nCcalls >= LUAI_MAXCCALLS) {
			if (L.nCcalls == LUAI_MAXCCALLS) {
				luaG_runerror(L, "C stack overflow");
			} else if (L.nCcalls >= (LUAI_MAXCCALLS + (LUAI_MAXCCALLS >> 3))) {
				luaD_throw(L, LUA_ERRERR); // error while handing stack error
			}
		}
		if (luaD_precall(L, func, nResults) == PCRLUA) // is a Lua function?
		{
			luaV_execute(L, 1); // call it
		}
		L.nCcalls--;
		luaC_checkGC(L);
	}

	public static void resume(lua_State L, Object ud) {
		lua_TValue firstArg = (lua_TValue) ud;
		CallInfo ci = L.ci;
		if (L.status == 0) // start coroutine?
		{
			lua_assert(ci == L.base_ci[0]
					&& lua_TValue.OpGreaterThan(firstArg, L.base_));// lua_assert(ci
																	// ==
																	// L.base_ci[0]
																	// &&
																	// firstArg
																	// >
																	// L.base_);
			if (luaD_precall(L, lua_TValue.OpSubtraction(firstArg, 1),
					LUA_MULTRET) != PCRLUA)// if (luaD_precall(L, firstArg - 1,
											// LUA_MULTRET) != PCRLUA)
			{
				return;
			}
		} else // resuming from previous yield
		{
			lua_assert(L.status == LUA_YIELD);
			L.status = 0;
			if (!f_isLua(ci)) // `common' yield?
			{
				/* finish interrupted execution of `OP_CALL' */
				CallInfo cSub = CallInfo.OpSubtraction(ci, 1);
				lua_assert(GET_OPCODE(cSub.savedpc.getItem(-1)) == OpCode.OP_CALL
						|| GET_OPCODE(cSub.savedpc.getItem(-1)) == OpCode.OP_TAILCALL);// lua_assert(GET_OPCODE((ci-1).savedpc[-1])
																						// ==
																						// OpCode.OP_CALL
																						// ||
																						// GET_OPCODE((ci-1).savedpc[-1])
																						// ==
																						// OpCode.OP_TAILCALL);
				if (luaD_poscall(L, firstArg) != 0) // complete it...
				{
					L.top = L.ci.top; // and correct top if not multiple results
				}
			} else // yielded inside a hook: just continue its execution
			{
				L.base_ = L.ci.base_;
			}
		}
		luaV_execute(L, CallInfo.OpSubtraction(L.ci, L.base_ci));// luaV_execute(L,
																	// L.ci -
																	// L.base_ci);
	}

	public static int resume_error(lua_State L, CharPtr msg) {
		L.top = L.ci.base_;
		setsvalue2s(L, L.top, luaS_new(L, msg));
		incr_top(L);
		lua_unlock(L);
		return LUA_ERRRUN;
	}

	public static int resume_error(lua_State L, String msg) {
		L.top = L.ci.base_;
		setsvalue2s(L, L.top, luaS_new(L, new CharPtr(msg)));
		incr_top(L);
		lua_unlock(L);
		return LUA_ERRRUN;
	}

	// static IDelegate resume = Pfunc.build(Lua.class, "resume");
	static IDelegate resume = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			resume((lua_State) arg1, arg2);
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int lua_resume(lua_State L, int nargs) {
		int status;
		lua_lock(L);
		if (L.status != LUA_YIELD && (L.status != 0 || (L.ci != L.base_ci[0]))) {
			return resume_error(L, "cannot resume non-suspended coroutine");
		}
		if (L.nCcalls >= LUAI_MAXCCALLS) {
			return resume_error(L, "C stack overflow");
		}
		luai_userstateresume(L, nargs);
		lua_assert(L.errfunc == 0);
		L.baseCcalls = ++L.nCcalls;
		status = luaD_rawrunprotected(L, resume,
				Lua.lua_TValue.OpSubtraction(L.top, nargs));
		if (status != 0) // error?
		{
			L.status = cast_byte(status); // mark thread as `dead'
			luaD_seterrorobj(L, status, L.top);
			L.ci.top = L.top;
		} else {
			lua_assert(L.nCcalls == L.baseCcalls);
			status = L.status;
		}
		--L.nCcalls;
		lua_unlock(L);
		return status;
	}

	public static int lua_yield(lua_State L, int nresults) {
		luai_userstateyield(L, nresults);
		lua_lock(L);
		if (L.nCcalls > L.baseCcalls) {
			luaG_runerror(L,
					"attempt to yield across metamethod/C-call boundary");
		}
		L.base_ = Lua.lua_TValue.OpSubtraction(L.top, nresults); // protect
																	// stack
																	// slots
																	// below
		L.status = LUA_YIELD;
		lua_unlock(L);
		return -1;
	}

	public static int luaD_pcall(lua_State L, IDelegate func, Object u,
			int old_top, int ef) {
		int status;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: ushort oldnCcalls = L.nCcalls;
		short oldnCcalls = L.nCcalls;
		int old_ci = saveci(L, L.ci);
		byte old_allowhooks = L.allowhook;
		int old_errfunc = L.errfunc;
		L.errfunc = ef;
		status = luaD_rawrunprotected(L, func, u);
		if (status != 0) // an error occurred?
		{
			lua_TValue oldtop = restorestack(L, old_top);
			luaF_close(L, oldtop); // close eventual pending closures
			luaD_seterrorobj(L, status, oldtop);
			L.nCcalls = oldnCcalls;
			L.ci = restoreci(L, old_ci);
			L.base_ = L.ci.base_;
			L.savedpc = InstructionPtr.Assign(L.ci.savedpc);
			L.allowhook = old_allowhooks;
			restore_stack_limit(L);
		}
		L.errfunc = old_errfunc;
		return status;
	}

	/*
	 * * Execute a protected parser.
	 */
	public static class SParser // data to `f_parser'
	{
		public Zio z;
		public Mbuffer buff = new Mbuffer(); // buffer to be used by the scanner
		public CharPtr name;
	}

	public static void f_parser(lua_State L, Object ud) {
		int i;
		Proto tf;
		Closure cl;
		SParser p = (SParser) ud;
		int c = luaZ_lookahead(p.z);
		luaC_checkGC(L);
		tf = (c == LUA_SIGNATURE.charAt(0)) ? luaU_undump(L, p.z, p.buff,
				p.name) : luaY_parser(L, p.z, p.buff, p.name);
		cl = luaF_newLclosure(L, tf.nups, hvalue(gt(L)));
		cl.l.p = tf;
		for (i = 0; i < tf.nups; i++) // initialize eventual upvalues
		{
			cl.l.upvals[i] = luaF_newupval(L);
		}
		setclvalue(L, L.top, cl);
		incr_top(L);
	}

	// static IDelegate f_parser = Pfunc.build(Lua.class, "f_parser");
	static IDelegate f_parser = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			f_parser((lua_State) arg1, arg2);
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int luaD_protectedparser(lua_State L, Zio z, CharPtr name) {
		SParser p = new SParser();
		int status;
		p.z = z;
		p.name = new CharPtr(name);
		luaZ_initbuffer(L, p.buff);
		status = luaD_pcall(L, f_parser, p, savestack(L, L.top), L.errfunc);
		luaZ_freebuffer(L, p.buff);
		return status;
	}

	public static class DumpState {
		public lua_State L;
		public IDelegate writer;// public lua_Writer writer;
		public Object data;
		public int strip;
		public int status;
	}

	public static void DumpMem(Object b, DumpState D) {
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if XBOX
		// todo: implement this - mjf
		assert false;
		// #else
		/*
		 * int size = Marshal.SizeOf(b); IntPtr ptr =
		 * Marshal.AllocHGlobal(size); Marshal.StructureToPtr(b, ptr, false);
		 * byte[] bytes = new byte[size]; Marshal.Copy(ptr, bytes, 0, size);
		 * char[] ch = new char[bytes.length]; for (int i = 0; i < bytes.length;
		 * i++) { ch[i] = (char)bytes[i]; } CharPtr str = ch; DumpBlock(str,
		 * (int)str.chars.length, D); Marshal.Release(ptr);
		 */
		// #endif
	}

	public static void DumpMem(Object b, int n, DumpState D) {
		Array array = (Array) ((b instanceof Array) ? b : null);
		assert array.getLength(b) == n;
		for (int i = 0; i < n; i++) {
			DumpMem(array.get(b, i), D);
		}
	}

	public static void DumpVar(Object x, DumpState D) {
		DumpMem(x, D);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static void DumpBlock(CharPtr b, uint size,
	// DumpState D)
	public static void DumpBlock(CharPtr b, int size, DumpState D) {
		if (D.status == 0) {
			lua_unlock(D.L);
			D.status = (Integer) D.writer.invoke(new Object[] { D.L, b, size,
					D.data });// D.status=D.writer(D.L,b,size,D.data);
			lua_lock(D.L);
		}
	}

	public static void DumpChar(int y, DumpState D) {
		char x = (char) y;
		DumpVar(x, D);
	}

	public static void DumpInt(int x, DumpState D) {
		DumpVar(x, D);
	}

	public static void DumpNumber(double x, DumpState D) {
		DumpVar(x, D);
	}

	public static void DumpVector(Object b, int n, DumpState D) {
		DumpInt(n, D);
		DumpMem(b, n, D);
	}

	public static void DumpString(TString s, DumpState D) {
		if (s == null || getstr(s) == null) {
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint size=0;
			int size = 0;
			DumpVar(size, D);
		} else {
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint size=s.tsv.len+1;
			int size = s.gettsv().len + 1; // include trailing '\0'
			DumpVar(size, D);
			DumpBlock(getstr(s), size, D);
		}
	}

	public static void DumpCode(Proto f, DumpState D) {
		DumpVector(f.code, f.sizecode, D);
	}

	public static void DumpConstants(Proto f, DumpState D) {
		int i, n = f.sizek;
		DumpInt(n, D);
		for (i = 0; i < n; i++) {
			/* const */
			lua_TValue o = f.k[i];
			DumpChar(ttype(o), D);
			switch (ttype(o)) {
			case LUA_TNIL:
				break;
			case LUA_TBOOLEAN:
				DumpChar(bvalue(o), D);
				break;
			case LUA_TNUMBER:
				DumpNumber(nvalue(o), D);
				break;
			case LUA_TSTRING:
				DumpString(rawtsvalue(o), D);
				break;
			default:
				lua_assert(0); // cannot happen
				break;
			}
		}
		n = f.sizep;
		DumpInt(n, D);
		for (i = 0; i < n; i++) {
			DumpFunction(f.p[i], f.source, D);
		}
	}

	public static void DumpDebug(Proto f, DumpState D) {
		int i, n;
		n = (D.strip != 0) ? 0 : f.sizelineinfo;
		DumpVector(f.lineinfo, n, D);
		n = (D.strip != 0) ? 0 : f.sizelocvars;
		DumpInt(n, D);
		for (i = 0; i < n; i++) {
			DumpString(f.locvars[i].varname, D);
			DumpInt(f.locvars[i].startpc, D);
			DumpInt(f.locvars[i].endpc, D);
		}
		n = (D.strip != 0) ? 0 : f.sizeupvalues;
		DumpInt(n, D);
		for (i = 0; i < n; i++) {
			DumpString(f.upvalues[i], D);
		}
	}

	public static void DumpFunction(Proto f, TString p, DumpState D) {
		DumpString(((f.source == p) || (D.strip != 0)) ? null : f.source, D);
		DumpInt(f.linedefined, D);
		DumpInt(f.lastlinedefined, D);
		DumpChar(f.nups, D);
		DumpChar(f.numparams, D);
		DumpChar(f.is_vararg, D);
		DumpChar(f.maxstacksize, D);
		DumpCode(f, D);
		DumpConstants(f, D);
		DumpDebug(f, D);
	}

	public static void DumpHeader(DumpState D) {
		CharPtr h = new CharPtr(new char[LUAC_HEADERSIZE]);
		luaU_header(h);
		DumpBlock(h, LUAC_HEADERSIZE, D);
	}

	/*
	 * * dump Lua function as precompiled chunk
	 */
	public static int luaU_dump(lua_State L, Proto f, IDelegate w, Object data,
			int strip) {
		DumpState D = new DumpState();
		D.L = L;
		D.writer = w;
		D.data = data;
		D.strip = strip;
		D.status = 0;
		DumpHeader(D);
		DumpFunction(f, null, D);
		return D.status;
	}

	public static int sizeCclosure(int n) {
		return GetUnmanagedSize(CClosure.class)
				+ GetUnmanagedSize(lua_TValue.class) * (n - 1);
	}

	public static int sizeLclosure(int n) {
		return GetUnmanagedSize(LClosure.class)
				+ GetUnmanagedSize(lua_TValue.class) * (n - 1);
	}

	public static Closure luaF_newCclosure(lua_State L, int nelems, Table e) {
		// Closure c = (Closure)luaM_malloc(L, sizeCclosure(nelems));
		Closure c = Lua.<Closure> luaM_new(L, Closure.class);
		AddTotalBytes(L, sizeCclosure(nelems));
		luaC_link(L, obj2gco(c), Integer.valueOf(LUA_TFUNCTION).byteValue());
		c.c.setisC(Integer.valueOf(1).byteValue());
		c.c.setenv(e);
		c.c.setnupvalues(cast_byte(nelems));
		c.c.upvalue = new lua_TValue[nelems];
		for (int i = 0; i < nelems; i++) {
			c.c.upvalue[i] = new lua_TValue();
		}
		return c;
	}

	public static Closure luaF_newLclosure(lua_State L, int nelems, Table e) {
		// Closure c = (Closure)luaM_malloc(L, sizeLclosure(nelems));
		Closure c = Lua.<Closure> luaM_new(L, Closure.class);
		AddTotalBytes(L, sizeLclosure(nelems));
		luaC_link(L, obj2gco(c), Integer.valueOf(LUA_TFUNCTION).byteValue());
		c.l.setisC(Integer.valueOf(0).byteValue());
		c.l.setenv(e);
		c.l.setnupvalues(cast_byte(nelems));
		c.l.upvals = new UpVal[nelems];
		for (int i = 0; i < nelems; i++) {
			c.l.upvals[i] = new UpVal();
		}
		while (nelems-- > 0) {
			c.l.upvals[nelems] = null;
		}
		return c;
	}

	public static UpVal luaF_newupval(lua_State L) {
		UpVal uv = Lua.<UpVal> luaM_new(L, UpVal.class);
		luaC_link(L, obj2gco(uv), Integer.valueOf(LUA_TUPVAL).byteValue());
		uv.v = uv.u.value;
		setnilvalue(uv.v);
		return uv;
	}

	public static UpVal luaF_findupval(lua_State L, lua_TValue level) {
		global_State g = G(L);
		GCObjectRef pp = new OpenValRef(L);
		UpVal p;
		UpVal uv;
		while (pp.get() != null
				&& lua_TValue.OpGreaterThanOrEqual((p = ngcotouv(pp.get())).v,
						level))// while (pp.get() != null && (p =
								// ngcotouv(pp.get())).v >= level)
		{
			lua_assert(p.v != p.u.value);
			if (p.v == level) // found a corresponding upvalue?
			{
				if (isdead(g, obj2gco(p))) // is it dead?
				{
					changewhite(obj2gco(p)); // ressurect it
				}
				return p;
			}
			pp = new NextRef(p);
		}
		uv = Lua.<UpVal> luaM_new(L, UpVal.class); // not found: create a new
													// one
		uv.tt = LUA_TUPVAL;
		uv.marked = luaC_white(g);
		uv.v = level; // current value lives in the stack
		uv.next = pp.get(); // chain it in the proper position
		pp.set(obj2gco(uv));
		uv.u.l.prev = g.uvhead; // double link it in `uvhead' list
		uv.u.l.next = g.uvhead.u.l.next;
		uv.u.l.next.u.l.prev = uv;
		g.uvhead.u.l.next = uv;
		lua_assert(uv.u.l.next.u.l.prev == uv && uv.u.l.prev.u.l.next == uv);
		return uv;
	}

	public static void unlinkupval(UpVal uv) {
		lua_assert(uv.u.l.next.u.l.prev == uv && uv.u.l.prev.u.l.next == uv);
		uv.u.l.next.u.l.prev = uv.u.l.prev; // remove from `uvhead' list
		uv.u.l.prev.u.l.next = uv.u.l.next;
	}

	public static void luaF_freeupval(lua_State L, UpVal uv) {
		if (uv.v != uv.u.value) // is it open?
		{
			unlinkupval(uv); // remove from open list
		}
		luaM_free(L, uv); // free upvalue
	}

	public static void luaF_close(lua_State L, lua_TValue level) {
		UpVal uv;
		global_State g = G(L);
		while (L.openupval != null
				&& lua_TValue.OpGreaterThanOrEqual(
						(uv = ngcotouv(L.openupval)).v, level))// while
																// (L.openupval
																// != null &&
																// (uv =
																// ngcotouv(L.openupval)).v
																// >= level)
		{
			GCObject o = obj2gco(uv);
			lua_assert(!isblack(o) && uv.v != uv.u.value);
			L.openupval = uv.next; // remove from `open' list
			if (isdead(g, o)) {
				luaF_freeupval(L, uv); // free upvalue
			} else {
				unlinkupval(uv);
				setobj(L, uv.u.value, uv.v);
				uv.v = uv.u.value; // now current value lives here
				luaC_linkupval(L, uv); // link upvalue into `gcroot' list
			}
		}
	}

	public static Proto luaF_newproto(lua_State L) {
		Proto f = Lua.<Proto> luaM_new(L, Proto.class);
		luaC_link(L, obj2gco(f), Integer.valueOf(LUA_TPROTO).byteValue());
		f.k = null;
		f.sizek = 0;
		f.p = null;
		f.sizep = 0;
		f.code = null;
		f.sizecode = 0;
		f.sizelineinfo = 0;
		f.sizeupvalues = 0;
		f.nups = 0;
		f.upvalues = null;
		f.numparams = 0;
		f.is_vararg = 0;
		f.maxstacksize = 0;
		f.lineinfo = null;
		f.sizelocvars = 0;
		f.locvars = null;
		f.linedefined = 0;
		f.lastlinedefined = 0;
		f.source = null;
		return f;
	}

	public static void luaF_freeproto(lua_State L, Proto f) {
		f = null;// Nullify reference to collect object :D
		/*
		 * Integer[] it = new Integer[f.code.length]; for(int i = 0; i <
		 * f.code.length; i++) it[i] = Integer.valueOf(f.code[i]);
		 * Lua.<Integer>luaM_freearray(L, it, Integer.class);
		 * Lua.<Proto>luaM_freearray(L, f.p, Proto.class);
		 * Lua.<lua_TValue>luaM_freearray(L, f.k, lua_TValue.class); Integer[]
		 * itl = new Integer[f.lineinfo.length]; for(int i = 0; i <
		 * f.lineinfo.length; i++) itl[i] = Integer.valueOf(f.lineinfo[i]);
		 * Lua.<Integer>luaM_freearray(L, itl, Integer.class);
		 * Lua.<LocVar>luaM_freearray(L, f.locvars, LocVar.class);
		 * Lua.<TString>luaM_freearray(L, f.upvalues, TString.class);
		 * luaM_free(L, f);
		 */
	}

	// we have a gc, so nothing to do
	public static void luaF_freeclosure(lua_State L, Closure c) {
		int size = (c.c.getisC() != 0) ? sizeCclosure(c.c.getnupvalues())
				: sizeLclosure(c.l.getnupvalues());
		// luaM_freemem(L, c, size);
		SubtractTotalBytes(L, size);
	}

	/*
	 * * Look for n-th local variable at line `line' in function `func'.*
	 * Returns null if not found.
	 */
	public static CharPtr luaF_getlocalname(Proto f, int local_number, int pc) {
		int i;
		for (i = 0; i < f.sizelocvars && f.locvars[i].startpc <= pc; i++) {
			if (pc < f.locvars[i].endpc) // is variable active?
			{
				local_number--;
				if (local_number == 0) {
					return getstr(f.locvars[i].varname);
				}
			}
		}
		return null; // not found
	}

	/*
	 * * Possible states of the Garbage Collector
	 */
	public static final int GCSpause = 0;
	public static final int GCSpropagate = 1;
	public static final int GCSsweepstring = 2;
	public static final int GCSsweep = 3;
	public static final int GCSfinalize = 4;

	/*
	 * * some userful bit tricks
	 */
	public static int resetbits(RefObject<Byte> x, int m) {
		x.argvalue = (byte) (x.argvalue & (byte)(~m));
		return x.argvalue;
	}

	public static int setbits(RefObject<Byte> x, int m) {
		x.argvalue = (byte) (x.argvalue | (byte)(m));
		return x.argvalue;
	}

	public static boolean testbits(byte x, int m) {
		return (x & (byte) m) != 0;
	}

	public static int bitmask(int b) {
		return 1 << b;
	}

	public static int bit2mask(int b1, int b2) {
		return (bitmask(b1) | bitmask(b2));
	}

	public static int l_setbit(RefObject<Byte> x, int b) {
		return setbits(x, bitmask(b));
	}

	public static int resetbit(RefObject<Byte> x, int b) {
		return resetbits(x, bitmask(b));
	}

	public static boolean testbit(byte x, int b) {
		return testbits(x, bitmask(b));
	}

	public static int set2bits(RefObject<Byte> x, int b1, int b2) {
		return setbits(x, (bit2mask(b1, b2)));
	}

	public static int reset2bits(RefObject<Byte> x, int b1, int b2) {
		return resetbits(x, (bit2mask(b1, b2)));
	}

	public static boolean test2bits(byte x, int b1, int b2) {
		return testbits(x, (bit2mask(b1, b2)));
	}

	/*
	 * * Layout for bit use in `marked' field:* bit 0 - object is white (type 0)
	 * * bit 1 - object is white (type 1)* bit 2 - object is black* bit 3 - for
	 * userdata: has been finalized* bit 3 - for tables: has weak keys* bit 4 -
	 * for tables: has weak values* bit 5 - object is fixed (should not be
	 * collected)* bit 6 - object is "super" fixed (only the main thread)
	 */

	public static final int WHITE0BIT = 0;
	public static final int WHITE1BIT = 1;
	public static final int BLACKBIT = 2;
	public static final int FINALIZEDBIT = 3;
	public static final int KEYWEAKBIT = 3;
	public static final int VALUEWEAKBIT = 4;
	public static final int FIXEDBIT = 5;
	public static final int SFIXEDBIT = 6;
	public final static int WHITEBITS = bit2mask(WHITE0BIT, WHITE1BIT);

	public static boolean iswhite(GCObject x) {
		return test2bits(x.getgch().marked, WHITE0BIT, WHITE1BIT);
	}

	public static boolean isblack(GCObject x) {
		return testbit(x.getgch().marked, BLACKBIT);
	}

	public static boolean isgray(GCObject x) {
		return (!isblack(x) && !iswhite(x));
	}

	public static int otherwhite(global_State g) {
		return g.currentwhite ^ WHITEBITS;
	}

	public static boolean isdead(global_State g, GCObject v) {
		return (v.getgch().marked & otherwhite(g) & WHITEBITS) != 0;
	}

	public static void changewhite(GCObject x) {
		x.getgch().SetMarked((byte) (x.getgch().marked ^ (byte) WHITEBITS));
	}

	public static void gray2black(GCObject x) {
		RefObject<Byte> tempRef_marked = new RefObject<Byte>(x.getgch().marked);
		l_setbit(tempRef_marked, BLACKBIT);
		x.getgch().SetMarked(tempRef_marked.argvalue);
	}

	public static boolean valiswhite(lua_TValue x) {
		return (iscollectable(x) && iswhite(gcvalue(x)));
	}

	public static byte luaC_white(global_State g) {
		return (byte) (g.currentwhite & WHITEBITS);
	}

	public static void luaC_checkGC(lua_State L) {
		// condhardstacktests(luaD_reallocstack(L, L.stacksize - EXTRA_STACK -
		// 1));
		// luaD_reallocstack(L, L.stacksize - EXTRA_STACK - 1);
		if (G(L).totalbytes >= G(L).GCthreshold) {
			luaC_step(L);
		}
	}

	public static void luaC_barrier(lua_State L, Object p, lua_TValue v) {
		if (valiswhite(v) && isblack(obj2gco(p))) {
			luaC_barrierf(L, obj2gco(p), gcvalue(v));
		}
	}

	public static void luaC_barriert(lua_State L, Table t, lua_TValue v) {
		if (valiswhite(v) && isblack(obj2gco(t))) {
			luaC_barrierback(L, t);
		}
	}

	public static void luaC_objbarrier(lua_State L, Object p, Object o) {
		if (iswhite(obj2gco(o)) && isblack(obj2gco(p))) {
			luaC_barrierf(L, obj2gco(p), obj2gco(o));
		}
	}

	public static void luaC_objbarriert(lua_State L, Table t, Object o) {
		if (iswhite(obj2gco(o)) && isblack(obj2gco(t))) {
			luaC_barrierback(L, t);
		}
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public const uint GCSTEPSIZE = 1024;
	public static final int GCSTEPSIZE = 1024;
	public static final int GCSWEEPMAX = 40;
	public static final int GCSWEEPCOST = 10;
	public static final int GCFINALIZECOST = 100;

	public static byte maskmarks = (byte) (~(bitmask(BLACKBIT) | WHITEBITS));

	public static void makewhite(global_State g, GCObject x) {
		x.getgch().SetMarked(
				(byte) (x.getgch().marked & maskmarks | luaC_white(g)));
	}

	public static void white2gray(GCObject x) {
		RefObject<Byte> tempRef_marked = new RefObject<Byte>(x.getgch().marked);
		reset2bits(tempRef_marked, WHITE0BIT, WHITE1BIT);
		x.getgch().SetMarked(tempRef_marked.argvalue);
	}

	public static void black2gray(GCObject x) {
		RefObject<Byte> tempRef_marked = new RefObject<Byte>(x.getgch().marked);
		resetbit(tempRef_marked, BLACKBIT);
		x.getgch().SetMarked(tempRef_marked.argvalue);
	}

	public static void stringmark(TString s) {
		RefObject<Byte> tempRef_marked = new RefObject<Byte>(s.gettsv().marked);
		reset2bits(tempRef_marked, WHITE0BIT, WHITE1BIT);
		s.gettsv().SetMarked(tempRef_marked.argvalue);
	}

	public static boolean isfinalized(Udata_uv u) {
		return testbit(u.marked, FINALIZEDBIT);
	}

	public static void markfinalized(Udata_uv u) {
		byte marked = u.marked; // can't pass properties in as ref
		RefObject<Byte> tempRef_marked = new RefObject<Byte>(marked);
		l_setbit(tempRef_marked, FINALIZEDBIT);
		marked = tempRef_marked.argvalue;
		u.marked = marked;
	}

	public static int KEYWEAK = bitmask(KEYWEAKBIT);
	public static int VALUEWEAK = bitmask(VALUEWEAKBIT);

	public static void markvalue(global_State g, lua_TValue o) {
		checkconsistency(o);
		if (iscollectable(o) && iswhite(gcvalue(o))) {
			reallymarkobject(g, gcvalue(o));
		}
	}

	public static void markobject(global_State g, Object t) {
		if (iswhite(obj2gco(t))) {
			reallymarkobject(g, obj2gco(t));
		}
	}

	public static void setthreshold(global_State g) {
		g.GCthreshold = (int) ((g.estimate / 100) * g.gcpause);
	}

	public static void removeentry(Node n) {
		lua_assert(ttisnil(gval(n)));
		if (iscollectable(gkey(n))) {
			setttype(gkey(n), LUA_TDEADKEY); // dead key; remove it
		}
	}

	public static void reallymarkobject(global_State g, GCObject o) {
		lua_assert(iswhite(o) && !isdead(g, o));
		white2gray(o);
		switch (o.getgch().tt) {
		case LUA_TSTRING: {
			return;
		}
		case LUA_TUSERDATA: {
			Table mt = gco2u(o).metatable;
			gray2black(o); // udata are never gray
			if (mt != null) {
				markobject(g, mt);
			}
			markobject(g, gco2u(o).env);
			return;
		}
		case LUA_TUPVAL: {
			UpVal uv = gco2uv(o);
			markvalue(g, uv.v);
			if (uv.v == uv.u.value) // closed?
			{
				gray2black(o); // open upvalues are never black
			}
			return;
		}
		case LUA_TFUNCTION: {
			gco2cl(o).c.setgclist(g.gray);
			g.gray = o;
			break;
		}
		case LUA_TTABLE: {
			gco2h(o).gclist = g.gray;
			g.gray = o;
			break;
		}
		case LUA_TTHREAD: {
			gco2th(o).gclist = g.gray;
			g.gray = o;
			break;
		}
		case LUA_TPROTO: {
			gco2p(o).gclist = g.gray;
			g.gray = o;
			break;
		}
		default:
			lua_assert(0);
			break;
		}
	}

	public static void marktmu(global_State g) {
		GCObject u = g.tmudata;
		if (u != null) {
			do {
				u = u.getgch().next;
				makewhite(g, u); // may be marked, if left from previous GC
				reallymarkobject(g, u);
			} while (u != g.tmudata);
		}
	}

	/* move `dead' udata that need finalization to list `tmudata' */
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static uint luaC_separateudata (lua_State L, int
	// all)
	public static int luaC_separateudata(lua_State L, int all) {
		global_State g = G(L);
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint deadmem = 0;
		int deadmem = 0;
		GCObjectRef p = new NextRef(g.mainthread);
		GCObject curr;
		while ((curr = p.get()) != null) {
			if (!(iswhite(curr) || (all != 0)) || isfinalized(gco2u(curr))) {
				p = new NextRef(curr.getgch()); // don't bother with them
			} else if (fasttm(L, gco2u(curr).metatable, TMS.TM_GC) == null) {
				markfinalized(gco2u(curr)); // don't need finalization
				p = new NextRef(curr.getgch());
			} else // must call its gc method
			{
				deadmem += (int) sizeudata(gco2u(curr));
				markfinalized(gco2u(curr));
				p.set(curr.getgch().next);
				/* link `curr' at the end of `tmudata' list */
				if (g.tmudata == null) // list is empty?
				{
					g.tmudata = curr.getgch().SetNext(curr); // creates a
																// circular list
				} else {
					curr.getgch().SetNext(g.tmudata.getgch().next);
					g.tmudata.getgch().SetNext(curr);
					g.tmudata = curr;
				}
			}
		}
		return deadmem;
	}

	public static int traversetable(global_State g, Table h) {
		int i;
		int weakkey = 0;
		int weakvalue = 0;
		/* const */
		lua_TValue mode;
		if (h.metatable != null) {
			markobject(g, h.metatable);
		}
		mode = gfasttm(g, h.metatable, TMS.TM_MODE);
		if ((mode != null) && ttisstring(mode)) // is there a weak mode?
		{
			weakkey = (strchr(svalue(mode), 'k') != null) ? 1 : 0;
			weakvalue = (strchr(svalue(mode), 'v') != null) ? 1 : 0;
			if ((weakkey != 0) || (weakvalue != 0)) // is really weak?
			{
				h.marked &= (byte) ~(KEYWEAK | VALUEWEAK); // clear bits
				h.marked |= cast_byte((weakkey << KEYWEAKBIT)
						| (weakvalue << VALUEWEAKBIT));
				h.gclist = g.weak; // must be cleared after GC,...
				g.weak = obj2gco(h); // ... so put in the appropriate list
			}
		}
		if ((weakkey != 0) && (weakvalue != 0)) {
			return 1;
		}
		if (weakvalue == 0) {
			i = h.sizearray;
			while ((i--) != 0) {
				markvalue(g, h.array[i]);
			}
		}
		i = sizenode(h);
		while ((i--) != 0) {
			Node n = gnode(h, i);
			lua_assert(ttype(gkey(n)) != LUA_TDEADKEY || ttisnil(gval(n)));
			if (ttisnil(gval(n))) {
				removeentry(n); // remove empty entries
			} else {
				lua_assert(ttisnil(gkey(n)));
				if (weakkey == 0) {
					markvalue(g, gkey(n));
				}
				if (weakvalue == 0) {
					markvalue(g, gval(n));
				}
			}
		}
		return ((weakkey != 0) || (weakvalue != 0)) ? 1 : 0;
	}

	/*
	 * * All marks are conditional because a GC may happen while the* prototype
	 * is still being created
	 */
	public static void traverseproto(global_State g, Proto f) {
		int i;
		if (f.source != null) {
			stringmark(f.source);
		}
		for (i = 0; i < f.sizek; i++) // mark literals
		{
			markvalue(g, f.k[i]);
		}
		for (i = 0; i < f.sizeupvalues; i++) // mark upvalue names
		{
			if (f.upvalues[i] != null) {
				stringmark(f.upvalues[i]);
			}
		}
		for (i = 0; i < f.sizep; i++) // mark nested protos
		{
			if (f.p[i] != null) {
				markobject(g, f.p[i]);
			}
		}
		for (i = 0; i < f.sizelocvars; i++) // mark local-variable names
		{
			if (f.locvars[i].varname != null) {
				stringmark(f.locvars[i].varname);
			}
		}
	}

	public static void traverseclosure(global_State g, Closure cl) {
		markobject(g, cl.c.getenv());
		if (cl.c.getisC() != 0) {
			int i;
			for (i = 0; i < cl.c.getnupvalues(); i++) // mark its upvalues
			{
				markvalue(g, cl.c.upvalue[i]);
			}
		} else {
			int i;
			lua_assert(cl.l.getnupvalues() == cl.l.p.nups);
			markobject(g, cl.l.p);
			for (i = 0; i < cl.l.getnupvalues(); i++) // mark its upvalues
			{
				markobject(g, cl.l.upvals[i]);
			}
		}
	}

	public static void checkstacksizes(lua_State L, lua_TValue max) {
		int ci_used = cast_int(CallInfo.OpSubtraction(L.ci, L.base_ci[0]));// int
																			// ci_used
																			// =
																			// cast_int(L.ci
																			// -
																			// L.base_ci[0]);
																			// //
																			// number
																			// of
																			// `ci'
																			// in
																			// use
		int s_used = cast_int(lua_TValue.OpSubtraction(max, L.stack));// int
																		// s_used
																		// =
																		// cast_int(max
																		// -
																		// L.stack);
																		// //
																		// part
																		// of
																		// stack
																		// in
																		// use
		if (L.size_ci > LUAI_MAXCALLS) // handling overflow?
		{
			return; // do not touch the stacks
		}
		if (4 * ci_used < L.size_ci && 2 * BASIC_CI_SIZE < L.size_ci) {
			luaD_reallocCI(L, L.size_ci / 2); // still big enough...
		}
		// condhardstacktests(luaD_reallocCI(L, ci_used + 1));
		if (4 * s_used < L.stacksize
				&& 2 * (BASIC_STACK_SIZE + EXTRA_STACK) < L.stacksize) {
			luaD_reallocstack(L, L.stacksize / 2); // still big enough...
		}
		// condhardstacktests(luaD_reallocstack(L, s_used));
	}

	public static void traversestack(global_State g, lua_State l) {
		lua_TValue o = new lua_TValue(), lim;
		CallInfo ci = new CallInfo();
		markvalue(g, gt(l));
		lim = l.top;
		RefObject<CallInfo> refarg = new RefObject<Lua.CallInfo>(ci);
		for (refarg.argvalue = l.base_ci[0]; CallInfo.OpLessThanOrEqual(
				refarg.argvalue, l.ci); CallInfo.inc(refarg))// for (ci =
																// l.base_ci[0];
																// ci <= l.ci;
																// CallInfo.inc(ci))
		{
			lua_assert(Lua.lua_TValue.OpLessThanOrEqual(refarg.argvalue.top,
					l.stack_last));
			if (lua_TValue.OpLessThan(lim, refarg.argvalue.top))// if (lim <
																// ci.top)
			{
				lim = refarg.argvalue.top;
			}
		}
		ci = refarg.argvalue;
		RefObject<lua_TValue> refargt = new RefObject<Lua.lua_TValue>(o);
		for (refargt.argvalue = l.stack[0]; lua_TValue.OpLessThan(
				refargt.argvalue, l.top); lua_TValue.inc(refargt))// for (o =
																	// l.stack[0];
																	// o <
																	// l.top;
																	// lua_TValue.inc(o))
		{
			markvalue(g, refargt.argvalue);
		}
		o = refargt.argvalue;
		for (; lua_TValue.OpLessThanOrEqual(refargt.argvalue, lim); lua_TValue
				.inc(refargt))// for (; o <= lim; lua_TValue.inc(o))
		{
			setnilvalue(refargt.argvalue);
		}
		o = refargt.argvalue;
		checkstacksizes(l, lim);
	}

	/*
	 * * traverse one gray object, turning it to black.* Returns `quantity'
	 * traversed.
	 */
	public static int propagatemark(global_State g) {
		GCObject o = g.gray;
		lua_assert(isgray(o));
		gray2black(o);
		switch (o.getgch().tt) {
		case LUA_TTABLE: {
			Table h = gco2h(o);
			g.gray = h.gclist;
			if (traversetable(g, h) != 0) // table is weak?
			{
				black2gray(o); // keep it gray
			}
			return GetUnmanagedSize(Table.class)
					+ GetUnmanagedSize(lua_TValue.class) * h.sizearray
					+ GetUnmanagedSize(Node.class) * sizenode(h);
		}
		case LUA_TFUNCTION: {
			Closure cl = gco2cl(o);
			g.gray = cl.c.getgclist();
			traverseclosure(g, cl);
			return (cl.c.getisC() != 0) ? sizeCclosure(cl.c.getnupvalues())
					: sizeLclosure(cl.l.getnupvalues());
		}
		case LUA_TTHREAD: {
			lua_State th = gco2th(o);
			g.gray = th.gclist;
			th.gclist = g.grayagain;
			g.grayagain = o;
			black2gray(o);
			traversestack(g, th);
			return GetUnmanagedSize(lua_State.class)
					+ GetUnmanagedSize(lua_TValue.class) * th.stacksize
					+ GetUnmanagedSize(CallInfo.class) * th.size_ci;
		}
		case LUA_TPROTO: {
			Proto p = gco2p(o);
			g.gray = p.gclist;
			traverseproto(g, p);
			return GetUnmanagedSize(Proto.class) + GetUnmanagedSize(int.class)
					* p.sizecode + GetUnmanagedSize(Proto.class) * p.sizep
					+ GetUnmanagedSize(lua_TValue.class) * p.sizek
					+ GetUnmanagedSize(Integer.class) * p.sizelineinfo
					+ GetUnmanagedSize(LocVar.class) * p.sizelocvars
					+ GetUnmanagedSize(TString.class) * p.sizeupvalues;
		}
		default:
			lua_assert(0);
			return 0;
		}
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static uint propagateall (global_State g)
	public static int propagateall(global_State g) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint m = 0;
		int m = 0;
		while (g.gray != null) {
			m += (int) propagatemark(g);
		}
		return m;
	}

	/*
	 * * The next function tells whether a key or value can be cleared from* a
	 * weak table. Non-collectable objects are never removed from weak* tables.
	 * Strings behave as `values', so are never removed too. for* other objects:
	 * if really collected, cannot keep them; for userdata* being finalized,
	 * keep them in keys, but not in values
	 */
	public static boolean iscleared(lua_TValue o, boolean iskey) {
		if (!iscollectable(o)) {
			return false;
		}
		if (ttisstring(o)) {
			stringmark(rawtsvalue(o)); // strings are `values', so are never
										// weak
			return false;
		}
		return iswhite(gcvalue(o))
				|| (ttisuserdata(o) && (!iskey && isfinalized(uvalue(o))));
	}

	/*
	 * * clear collected entries from weaktables
	 */
	public static void cleartable(GCObject l) {
		while (l != null) {
			Table h = gco2h(l);
			int i = h.sizearray;
			lua_assert(testbit(h.marked, VALUEWEAKBIT)
					|| testbit(h.marked, KEYWEAKBIT));
			if (testbit(h.marked, VALUEWEAKBIT)) {
				while (i-- != 0) {
					lua_TValue o = h.array[i];
					if (iscleared(o, false)) // value was collected?
					{
						setnilvalue(o); // remove value
					}
				}
			}
			i = sizenode(h);
			while (i-- != 0) {
				Node n = gnode(h, i);
				if (!ttisnil(gval(n))
						&& (iscleared(key2tval(n), true) || iscleared(gval(n),
								false))) // non-empty entry?
				{
					setnilvalue(gval(n)); // remove value...
					removeentry(n); // remove entry from Table
				}
			}
			l = h.gclist;
		}
	}

	public static void freeobj(lua_State L, GCObject o) {
		switch (o.getgch().tt) {
		case LUA_TPROTO:
			luaF_freeproto(L, gco2p(o));
			break;
		case LUA_TFUNCTION:
			luaF_freeclosure(L, gco2cl(o));
			break;
		case LUA_TUPVAL:
			luaF_freeupval(L, gco2uv(o));
			break;
		case LUA_TTABLE:
			luaH_free(L, gco2h(o));
			break;
		case LUA_TTHREAD: {
			lua_assert(gco2th(o) != L && gco2th(o) != G(L).mainthread);
			luaE_freethread(L, gco2th(o));
			break;
		}
		case LUA_TSTRING: {
			G(L).strt.nuse--;
			SubtractTotalBytes(L, sizestring(gco2ts(o)));
			luaM_freemem(L, gco2ts(o));
			break;
		}
		case LUA_TUSERDATA: {
			SubtractTotalBytes(L, sizeudata(gco2u(o)));
			luaM_freemem(L, gco2u(o));
			break;
		}
		default:
			lua_assert(0);
			break;
		}
	}

	public static void sweepwholelist(lua_State L, GCObjectRef p) {
		sweeplist(L, p, MAX_LUMEM);
	}

	public static GCObjectRef sweeplist(lua_State L, GCObjectRef p, int count) {
		GCObject curr;
		global_State g = G(L);
		int deadmask = otherwhite(g);
		while ((curr = p.get()) != null && count-- > 0) {
			if (curr.getgch().tt == LUA_TTHREAD) // sweep open upvalues of each
													// thread
			{
				sweepwholelist(L, new OpenValRef(gco2th(curr)));
			}
			if (((curr.getgch().marked ^ WHITEBITS) & deadmask) != 0) // not
																		// dead?
			{
				lua_assert(isdead(g, curr)
						|| testbit(curr.getgch().marked, FIXEDBIT));
				makewhite(g, curr); // make it white (for next cycle)
				p = new NextRef(curr.getgch());
			} else // must erase `curr'
			{
				lua_assert(isdead(g, curr) || deadmask == bitmask(SFIXEDBIT));
				p.set(curr.getgch().next);
				if (curr == g.rootgc) // is the first element of the list?
				{
					g.rootgc = curr.getgch().next; // adjust first
				}
				freeobj(L, curr);
			}
		}
		return p;
	}

	public static void checkSizes(lua_State L) {
		global_State g = G(L);
		/* check size of string hash */
		if (g.strt.nuse < (int) (g.strt.size / 4)
				&& g.strt.size > MINSTRTABSIZE * 2) {
			luaS_resize(L, g.strt.size / 2); // table is too big
		}
		/* check size of buffer */
		if (luaZ_sizebuffer(g.buff) > LUA_MINBUFFER * 2) // buffer too big?
		{
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint newsize = luaZ_sizebuffer(g.buff) / 2;
			int newsize = luaZ_sizebuffer(g.buff) / 2;
			luaZ_resizebuffer(L, g.buff, (int) newsize);
		}
	}

	public static void GCTM(lua_State L) {
		global_State g = G(L);
		GCObject o = g.tmudata.getgch().next; // get first element
		Udata udata = rawgco2u(o);
		lua_TValue tm;
		/* remove udata from `tmudata' */
		if (o == g.tmudata) // last element?
		{
			g.tmudata = null;
		} else {
			g.tmudata.getgch().SetNext(udata.uv.next);
		}
		udata.uv.next = g.mainthread.next; // return it to `root' list
		g.mainthread.next = o;
		makewhite(g, o);
		tm = fasttm(L, udata.uv.metatable, TMS.TM_GC);
		if (tm != null) {
			byte oldah = L.allowhook;
			int oldt = (int) g.GCthreshold;
			L.allowhook = 0; // stop debug hooks during GC tag method
			g.GCthreshold = 2 * g.totalbytes; // avoid GC steps
			setobj2s(L, L.top, tm);
			setuvalue(L, Lua.lua_TValue.OpAddition(L.top, 1), udata);
			L.top = lua_TValue.OpAddition(L.top, 2);// L.top += 2;
			luaD_call(L, Lua.lua_TValue.OpSubtraction(L.top, 2), 0);
			L.allowhook = oldah; // restore hooks
			g.GCthreshold = (int) oldt; // restore threshold
		}
	}

	/*
	 * * Call all GC tag methods
	 */
	public static void luaC_callGCTM(lua_State L) {
		while (G(L).tmudata != null) {
			GCTM(L);
		}
	}

	public static void luaC_freeall(lua_State L) {
		global_State g = G(L);
		int i;
		g.currentwhite = (byte) (WHITEBITS | bitmask(SFIXEDBIT)); // mask to
																	// collect
																	// all
																	// elements
		sweepwholelist(L, new RootGCRef(g));
		for (i = 0; i < g.strt.size; i++) // free all string lists
		{
			sweepwholelist(L, new ArrayRef(g.strt.hash, i));
		}
	}

	public static void markmt(global_State g) {
		int i;
		for (i = 0; i < NUM_TAGS; i++) {
			if (g.mt[i] != null) {
				markobject(g, g.mt[i]);
			}
		}
	}

	/* mark root set */
	public static void markroot(lua_State L) {
		global_State g = G(L);
		g.gray = null;
		g.grayagain = null;
		g.weak = null;
		markobject(g, g.mainthread);
		/* make global table be traversed before main stack */
		markvalue(g, gt(g.mainthread));
		markvalue(g, registry(L));
		markmt(g);
		g.gcstate = GCSpropagate;
	}

	public static void remarkupvals(global_State g) {
		UpVal uv;
		for (uv = g.uvhead.u.l.next; uv != g.uvhead; uv = uv.u.l.next) {
			lua_assert(uv.u.l.next.u.l.prev == uv && uv.u.l.prev.u.l.next == uv);
			if (isgray(obj2gco(uv))) {
				markvalue(g, uv.v);
			}
		}
	}

	public static void atomic(lua_State L) {
		global_State g = G(L);
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint udsize;
		int udsize; // total size of userdata to be finalized
		/* remark occasional upvalues of (maybe) dead threads */
		remarkupvals(g);
		/* traverse objects cautch by write barrier and by 'remarkupvals' */
		propagateall(g);
		/* remark weak tables */
		g.gray = g.weak;
		g.weak = null;
		lua_assert(!iswhite(obj2gco(g.mainthread)));
		markobject(g, L); // mark running thread
		markmt(g); // mark basic metatables (again)
		propagateall(g);
		/* remark gray again */
		g.gray = g.grayagain;
		g.grayagain = null;
		propagateall(g);
		udsize = luaC_separateudata(L, 0); // separate userdata to be finalized
		marktmu(g); // mark `preserved' userdata
		udsize += propagateall(g); // remark, to propagate `preserveness'
		cleartable(g.weak); // remove collected objects from weak tables
		/* flip current white */
		g.currentwhite = cast_byte(otherwhite(g));
		g.sweepstrgc = 0;
		g.sweepgc = new RootGCRef(g);
		g.gcstate = GCSsweepstring;
		g.estimate = g.totalbytes - udsize; // first estimate
	}

	public static int singlestep(lua_State L) {
		global_State g = G(L);
		/* lua_checkmemory(L); */
		switch (g.gcstate) {
		case GCSpause: {
			markroot(L); // start a new collection
			return 0;
		}
		case GCSpropagate: {
			if (g.gray != null) {
				return propagatemark(g);
			} else // no more `gray' objects
			{
				atomic(L); // finish mark phase
				return 0;
			}
		}
		case GCSsweepstring: {
			int old = (int) g.totalbytes;
			sweepwholelist(L, new ArrayRef(g.strt.hash, g.sweepstrgc++));
			if (g.sweepstrgc >= g.strt.size) // nothing more to sweep?
			{
				g.gcstate = GCSsweep; // end sweep-string phase
			}
			lua_assert(old >= g.totalbytes);
			g.estimate -= (int) (old - g.totalbytes);
			return GCSWEEPCOST;
		}
		case GCSsweep: {
			int old = (int) g.totalbytes;
			g.sweepgc = sweeplist(L, g.sweepgc, GCSWEEPMAX);
			if (g.sweepgc.get() == null) // nothing more to sweep?
			{
				checkSizes(L);
				g.gcstate = GCSfinalize; // end sweep phase
			}
			lua_assert(old >= g.totalbytes);
			g.estimate -= (int) (old - g.totalbytes);
			return GCSWEEPMAX * GCSWEEPCOST;
		}
		case GCSfinalize: {
			if (g.tmudata != null) {
				GCTM(L);
				if (g.estimate > GCFINALIZECOST) {
					g.estimate -= GCFINALIZECOST;
				}
				return GCFINALIZECOST;
			} else {
				g.gcstate = GCSpause; // end collection
				g.gcdept = 0;
				return 0;
			}
		}
		default:
			lua_assert(0);
			return 0;
		}
	}

	public static void luaC_step(lua_State L) {
		global_State g = G(L);
		int lim = (int) ((GCSTEPSIZE / 100) * g.gcstepmul);
		if (lim == 0) {
			lim = (int) ((MAX_LUMEM - 1) / 2); // no limit
		}
		g.gcdept += g.totalbytes - g.GCthreshold;
		do {
			lim -= singlestep(L);
			if (g.gcstate == GCSpause) {
				break;
			}
		} while (lim > 0);
		if (g.gcstate != GCSpause) {
			if (g.gcdept < GCSTEPSIZE) {
				g.GCthreshold = g.totalbytes + GCSTEPSIZE; // - lim/g.gcstepmul;
			} else {
				g.gcdept -= GCSTEPSIZE;
				g.GCthreshold = g.totalbytes;
			}
		} else {
			lua_assert(g.totalbytes >= g.estimate);
			setthreshold(g);
		}
	}

	public static void luaC_fullgc(lua_State L) {
		global_State g = G(L);
		if (g.gcstate <= GCSpropagate) {
			/* reset sweep marks to sweep all elements (returning them to white) */
			g.sweepstrgc = 0;
			g.sweepgc = new RootGCRef(g);
			/* reset other collector lists */
			g.gray = null;
			g.grayagain = null;
			g.weak = null;
			g.gcstate = GCSsweepstring;
		}
		lua_assert(g.gcstate != GCSpause && g.gcstate != GCSpropagate);
		/* finish any pending sweep phase */
		while (g.gcstate != GCSfinalize) {
			lua_assert(g.gcstate == GCSsweepstring || g.gcstate == GCSsweep);
			singlestep(L);
		}
		markroot(L);
		while (g.gcstate != GCSpause) {
			singlestep(L);
		}
		setthreshold(g);
	}

	public static void luaC_barrierf(lua_State L, GCObject o, GCObject v) {
		global_State g = G(L);
		lua_assert(isblack(o) && iswhite(v) && !isdead(g, v) && !isdead(g, o));
		lua_assert(g.gcstate != GCSfinalize && g.gcstate != GCSpause);
		lua_assert(ttype(o.getgch()) != LUA_TTABLE);
		/* must keep invariant? */
		if (g.gcstate == GCSpropagate) {
			reallymarkobject(g, v); // restore invariant
		} else // don't mind
		{
			makewhite(g, o); // mark as white just to avoid other barriers
		}
	}

	public static void luaC_barrierback(lua_State L, Table t) {
		global_State g = G(L);
		GCObject o = obj2gco(t);
		lua_assert(isblack(o) && !isdead(g, o));
		lua_assert(g.gcstate != GCSfinalize && g.gcstate != GCSpause);
		black2gray(o); // make table gray (again)
		t.gclist = g.grayagain;
		g.grayagain = o;
	}

	public static void luaC_link(lua_State L, GCObject o, byte tt) {
		global_State g = G(L);
		o.getgch().SetNext(g.rootgc);
		g.rootgc = o;
		o.getgch().SetMarked(luaC_white(g));
		o.getgch().SetTT(tt);
	}

	public static void luaC_linkupval(lua_State L, UpVal uv) {
		global_State g = G(L);
		GCObject o = obj2gco(uv);
		o.getgch().SetNext(g.rootgc); // link upvalue into `rootgc' list
		g.rootgc = o;
		if (isgray(o)) {
			if (g.gcstate == GCSpropagate) {
				gray2black(o); // closed upvalues need barrier
				luaC_barrier(L, uv, uv.v);
			} else // sweep phase: sweep it (turning it into white)
			{
				makewhite(g, o);
				lua_assert(g.gcstate != GCSfinalize && g.gcstate != GCSpause);
			}
		}
	}

	private final static luaL_Reg[] lualibs = {
			new luaL_Reg("", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_base((lua_State)arg);
				}
			}),
			new luaL_Reg(LUA_LOADLIBNAME, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_package((lua_State)arg);
				}
			}),
			new luaL_Reg(LUA_TABLIBNAME, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_table((lua_State)arg);
				}
			}),
			new luaL_Reg(LUA_IOLIBNAME, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_io((lua_State)arg);
				}
			}),
			new luaL_Reg(LUA_OSLIBNAME, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_os((lua_State)arg);
				}
			}),
			new luaL_Reg(LUA_STRLIBNAME, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_string((lua_State)arg);
				}
			}),
			new luaL_Reg(LUA_MATHLIBNAME, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_math((lua_State)arg);
				}
			}),
			new luaL_Reg(LUA_DBLIBNAME, new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_debug((lua_State)arg);
				}
			}),
			/*new luaL_Reg("socket", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_socket_core((lua_State)arg);
				}
			}),
			new luaL_Reg("mime", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return luaopen_mime_core((lua_State)arg);
				}
			}),*/
			new luaL_Reg((String)null, (String)null) };

	public static void luaL_openlibs(lua_State L) {
		for (int i = 0; i < lualibs.length - 1; i++) {
			luaL_Reg lib = lualibs[i];
			lua_pushcfunction(L, lib.func);
			lua_pushstring(L, lib.name);
			lua_call(L, 1, 0);
		}	
	  /*luaL_loadfile(L, "mime.lua");
	  lua_pcall(L, 0, 0, 0);
	  luaL_loadfile(L, "socket.lua");
	  lua_pcall(L, 0, 0, 0);*/
	  /*luaL_loadfile(L, "socket/ftp.lua");
	  lua_pcall(L, 0, 0, 0);
	  luaL_loadfile(L, "socket/http.lua");
	  lua_pcall(L, 0, 0, 0);
	  luaL_loadfile(L, "socket/smtp.lua");
	  lua_pcall(L, 0, 0, 0);
	  luaL_loadfile(L, "socket/tp.lua");
	  lua_pcall(L, 0, 0, 0);*/
	  /*luaL_loadfile(L, "socket/url.lua");
	  lua_pcall(L, 0, 0, 0);
	  luaL_loadfile(L, "ltn12.lua");
	  lua_pcall(L, 0, 0, 0);*/
	}

	public static final int IO_INPUT = 1;
	public static final int IO_OUTPUT = 2;

	public static final String[] fnames = { "input", "output" };

	public static int pushresult(lua_State L, int i, CharPtr filename) {
		int en = errno(); // calls to Lua API may change this value
		if (i != 0) {
			lua_pushboolean(L, 1);
			return 1;
		} else {
			lua_pushnil(L);
			if (filename != null) {
				lua_pushfstring(L, "%s: %s", filename, strerror(en));
			} else {
				lua_pushfstring(L, "%s", strerror(en));
			}
			lua_pushinteger(L, en);
			return 3;
		}
	}

	public static void fileerror(lua_State L, int arg, CharPtr filename) {
		lua_pushfstring(L, "%s: %s", filename, strerror(errno()));
		luaL_argerror(L, arg, lua_tostring(L, -1));
	}

	public static FilePtr tofilep(lua_State L) {
		return (FilePtr) luaL_checkudata(L, 1, LUA_FILEHANDLE);
	}

	public static int io_type(lua_State L) {
		Object ud;
		luaL_checkany(L, 1);
		ud = lua_touserdata(L, 1);
		lua_getfield(L, LUA_REGISTRYINDEX, LUA_FILEHANDLE);
		if (ud == null || (lua_getmetatable(L, 1) == 0)
				|| (lua_rawequal(L, -2, -1) == 0)) {
			lua_pushnil(L); // not a file
		} else if (((FilePtr) ((ud instanceof FilePtr) ? ud : null)).file == null) {
			lua_pushliteral(L, "closed file");
		} else {
			lua_pushliteral(L, "file");
		}
		return 1;
	}

	public static FilePtr tofile(lua_State L) {
		FilePtr f = tofilep(L);
		if (f.file == null && f.fileOut == null) {
			luaL_error(L, "attempt to use a closed file");
		}
		return f;
	}

	/*
	 * * When creating file files, always creates a `closed' file file* before
	 * opening the actual file; so, if there is a memory error, the* file is not
	 * left opened.
	 */
	public static FilePtr newfile(lua_State L) {

		FilePtr pf = (FilePtr) lua_newuserdata(L, FilePtr.class);
		pf.file = null; // file file is currently `closed'
		luaL_getmetatable(L, LUA_FILEHANDLE);
		lua_setmetatable(L, -2);
		return pf;
	}

	/*
	 * * function to (not) close the standard files stdin, stdout, and stderr
	 */
	public static int io_noclose(lua_State L) {
		lua_pushnil(L);
		lua_pushliteral(L, "cannot close standard file");
		return 2;
	}

	/*
	 * * function to close 'popen' files
	 */
	public static int io_pclose(lua_State L) {
		FilePtr p = tofilep(L);
		int ok = (lua_pclose(L, p.file) == 0) ? 1 : 0;
		p.file = null;
		return pushresult(L, ok, null);
	}

	/*
	 * * function to close regular files
	 */
	public static int io_fclose(lua_State L) {
		FilePtr p = tofilep(L);
		int ok = (fclose(p.file) == 0) ? 1 : 0;
		p.file = null;
		return pushresult(L, ok, null);
	}

	public static int aux_close(lua_State L) {
		lua_getfenv(L, 1);
		lua_getfield(L, -1, "__close");
		return (Integer) (lua_tocfunction(L, -1)).invoke(L);
	}

	public static int io_close(lua_State L) {
		if (lua_isnone(L, 1)) {
			lua_rawgeti(L, LUA_ENVIRONINDEX, IO_OUTPUT);
		}
		tofile(L); // make sure argument is a file
		return aux_close(L);
	}

	public static int io_gc(lua_State L) {
		InputStream f = tofilep(L).file;
		/* ignore closed files */
		if (f != null) {
			aux_close(L);
		}
		return 0;
	}

	public static int io_tostring(lua_State L) {
		InputStream f = tofilep(L).file;
		if (f == null) {
			lua_pushliteral(L, "file (closed)");
		} else {
			lua_pushfstring(L, "file (%p)", f);
		}
		return 1;
	}

	public static int io_open(lua_State L) {
		CharPtr filename = luaL_checkstring(L, 1);
		CharPtr mode = luaL_optstring(L, 2, "r");
		FilePtr pf = newfile(L);
		pf.file = (InputStream) fopen(filename, mode);
		return (pf.file == null) ? pushresult(L, 0, filename) : 1;
	}

	/*
	 * * this function has a separated environment, which defines the* correct
	 * __close for 'popen' files
	 */
	public static int io_popen(lua_State L) {
		CharPtr filename = luaL_checkstring(L, 1);
		CharPtr mode = luaL_optstring(L, 2, "r");
		FilePtr pf = newfile(L);
		pf.file = (InputStream) lua_popen(L, filename, mode);
		return (pf.file == null) ? pushresult(L, 0, filename) : 1;
	}

	public static int io_tmpfile(lua_State L) {
		FilePtr pf = newfile(L);
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if XBOX
		// luaL_error(L, "io_tmpfile not supported on Xbox360");
		luaL_error(L, "not supported");
		// #else
		// pf.file = tmpfile();
		// #endif
		return (pf.file == null) ? pushresult(L, 0, null) : 1;
	}

	public static FilePtr getiofile(lua_State L, int findex) {
		if(findex == IO_INPUT)
		{
			InputStream f;
			lua_rawgeti(L, LUA_ENVIRONINDEX, findex);
			Object tempVar = lua_touserdata(L, -1);
			f = ((FilePtr) ((tempVar instanceof FilePtr) ? tempVar : null)).file;
			if (f == null) {
				luaL_error(L, "standard %s file is closed", fnames[findex - 1]);
			}
			return ((FilePtr) ((tempVar instanceof FilePtr) ? tempVar : null));
		}
		else
		{
			OutputStream f;
			lua_rawgeti(L, LUA_ENVIRONINDEX, findex);
			Object tempVar = lua_touserdata(L, -1);
			f = ((FilePtr) ((tempVar instanceof FilePtr) ? tempVar : null)).fileOut;
			if (f == null) {
				luaL_error(L, "standard %s file is closed", fnames[findex - 1]);
			}
			return ((FilePtr) ((tempVar instanceof FilePtr) ? tempVar : null));
		}
	}

	public static int g_iofile(lua_State L, int f, CharPtr mode) {
		if (!lua_isnoneornil(L, 1)) {
			CharPtr filename = lua_tostring(L, 1);
			if (filename != null) {
				FilePtr pf = newfile(L);
				pf.file = (InputStream) fopen(filename, mode);
				if (pf.file == null) {
					fileerror(L, 1, filename);
				}
			} else {
				tofile(L); // check that it's a valid file file
				lua_pushvalue(L, 1);
			}
			lua_rawseti(L, LUA_ENVIRONINDEX, f);
		}
		/* return current value */
		lua_rawgeti(L, LUA_ENVIRONINDEX, f);
		return 1;
	}

	public static int g_iofile(lua_State L, int f, String modeS) {
		CharPtr mode = new CharPtr(modeS);
		if (!lua_isnoneornil(L, 1)) {
			CharPtr filename = lua_tostring(L, 1);
			if (filename != null) {
				FilePtr pf = newfile(L);
				pf.file = (InputStream) fopen(filename, mode);
				if (pf.file == null) {
					fileerror(L, 1, filename);
				}
			} else {
				tofile(L); // check that it's a valid file file
				lua_pushvalue(L, 1);
			}
			lua_rawseti(L, LUA_ENVIRONINDEX, f);
		}
		/* return current value */
		lua_rawgeti(L, LUA_ENVIRONINDEX, f);
		return 1;
	}

	public static int io_input(lua_State L) {
		return g_iofile(L, IO_INPUT, "r");
	}

	public static int io_output(lua_State L) {
		return g_iofile(L, IO_OUTPUT, "w");
	}

	// static IDelegate io_readline = lua_CFunction.build(Lua.class,
	// "io_readline");
	static IDelegate io_readline = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return io_readline((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static void aux_lines(lua_State L, int idx, int toclose) {
		lua_pushvalue(L, idx);
		lua_pushboolean(L, toclose); // close/not close file when finished
		lua_pushcclosure(L, io_readline, 2);
	}

	public static int f_lines(lua_State L) {
		tofile(L); // check that it's a valid file file
		aux_lines(L, 1, 0);
		return 1;
	}

	public static int io_lines(lua_State L) {
		if (lua_isnoneornil(L, 1)) // no arguments?
		{
			/* will iterate over default input */
			lua_rawgeti(L, LUA_ENVIRONINDEX, IO_INPUT);
			return f_lines(L);
		} else {
			CharPtr filename = luaL_checkstring(L, 1);
			FilePtr pf = newfile(L);
			pf.file = (InputStream) fopen(filename, "r");
			if (pf.file == null) {
				fileerror(L, 1, filename);
			}
			aux_lines(L, lua_gettop(L), 1);
			return 1;
		}
	}

	/*
	 * * {======================================================* READ*
	 * =======================================================
	 */

	public static int read_number(lua_State L, InputStream f) {
		// double d;
		Object[] parms = { (Object) (double) 0.0 };
		if (fscanf(f, LUA_NUMBER_SCAN, parms) == 1) {
			lua_pushnumber(L, ((Double) parms[0]).doubleValue());
			return 1;
		} else // read fails
		{
			return 0;
		}
	}

	public static int test_eof(lua_State L, InputStream f) {
		int c = getc(f);
		ungetc(c, f);
		lua_pushlstring(L, null, 0);
		return (c != EOF) ? 1 : 0;
	}

	public static int read_line(lua_State L, InputStream f) {
		luaL_Buffer b = new luaL_Buffer();
		luaL_buffinit(L, b);
		for (;;) {
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint l;
			int l;
			CharPtr p = luaL_prepbuffer(b);
			if (fgets(p, f) == null) // eof?
			{
				luaL_pushresult(b); // close buffer
				return (lua_objlen(L, -1) > 0) ? 1 : 0; // check whether read
														// something
			}
			l = (int) strlen(p);
			if (l == 0 || p.getItem(l - 1) != '\n') {
				luaL_addsize(b, (int) l);
			} else {
				luaL_addsize(b, (int) (l - 1)); // do not include `eol'
				luaL_pushresult(b); // close buffer
				return 1; // read at least an `eol'
			}
		}
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int read_chars (lua_State L, Stream f, uint
	// n)
	public static int read_chars(lua_State L, InputStream f, int n) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint rlen;
		int rlen; // how much to read
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint nr;
		int nr; // number of chars actually read
		luaL_Buffer b = new luaL_Buffer();
		luaL_buffinit(L, b);
		rlen = LUAL_BUFFERSIZE; // try to read that much each time
		do {
			CharPtr p = luaL_prepbuffer(b);
			if (rlen > n) // cannot read more than asked
			{
				rlen = n;
			}
			nr = (int) fread(p, GetUnmanagedSize(Character.class), (int) rlen,
					f);
			luaL_addsize(b, (int) nr);
			n -= nr; // still have to read `n' chars
		} while (n > 0 && nr == rlen); // until end of count or eof
		luaL_pushresult(b); // close buffer
		return (n == 0 || lua_objlen(L, -1) > 0) ? 1 : 0;
	}

	public static int g_read(lua_State L, InputStream f, int first) {
		int nargs = lua_gettop(L) - 1;
		int success;
		int n;
		clearerr(f);
		if (nargs == 0) // no arguments?
		{
			success = read_line(L, f);
			n = first + 1; // to return 1 result
		} else // ensure stack space for all results and for auxlib's buffer
		{
			luaL_checkstack(L, nargs + LUA_MINSTACK, "too many arguments");
			success = 1;
			for (n = first; (nargs-- != 0) && (success != 0); n++) {
				if (lua_type(L, n) == LUA_TNUMBER) {
					// FUCK WARNING: Unsigned integer types have no direct
					// equivalent in Java:
					// ORIGINAL LINE: uint l = (uint)lua_tointeger(L, n);
					int l = (int) lua_tointeger(L, n);
					success = (l == 0) ? test_eof(L, f) : read_chars(L, f, l);
				} else {
					CharPtr p = lua_tostring(L, n);
					luaL_argcheck(L, (p != null) && (p.getItem(0) == '*'), n,
							"invalid option");
					switch (p.getItem(1)) {
					case 'n': // number
						success = read_number(L, f);
						break;
					case 'l': // line
						success = read_line(L, f);
						break;
					case 'a': // file
						read_chars(L, f, ~((int) 0)); // read MAX_uint chars
						success = 1; // always success
						break;
					default:
						return luaL_argerror(L, n, "invalid format");
					}
				}
			}
		}
		if (ferror(f) != 0) {
			return pushresult(L, 0, null);
		}
		if (success == 0) {
			lua_pop(L, 1); // remove last result
			lua_pushnil(L); // push nil instead
		}
		return n - first;
	}

	public static int io_read(lua_State L) {
		return g_read(L, getiofile(L, IO_INPUT).file, 1);
	}

	public static int f_read(lua_State L) {
		return g_read(L, tofile(L).file, 2);
	}

	public static int io_readline(lua_State L) {
		Object tempVar = lua_touserdata(L, lua_upvalueindex(1));
		InputStream f = ((FilePtr) ((tempVar instanceof FilePtr) ? tempVar
				: null)).file;
		int sucess;
		if (f == null) // file is already closed?
		{
			luaL_error(L, "file is already closed");
		}
		sucess = read_line(L, f);
		if (ferror(f) != 0) {
			return luaL_error(L, "%s", strerror(errno()));
		}
		if (sucess != 0) {
			return 1;
		} else // EOF
		{
			if (lua_toboolean(L, lua_upvalueindex(2)) != 0) // generator created
															// file?
			{
				lua_settop(L, 0);
				lua_pushvalue(L, lua_upvalueindex(1));
				aux_close(L); // close it
			}
			return 0;
		}
	}

	/* }====================================================== */

	public static int g_write(lua_State L, OutputStream f, int arg) {
		int nargs = lua_gettop(L) - 1;
		int status = 1;
		for (; (nargs--) != 0; arg++) {
			if (lua_type(L, arg) == LUA_TNUMBER) {
				/* optimization: could be done exactly as for strings */
				status = ((status != 0) && (fprintf(f, LUA_NUMBER_FMT,
						lua_tonumber(L, arg)) > 0)) ? 1 : 0;
			} else {
				// FUCK WARNING: Unsigned integer types have no direct
				// equivalent in Java:
				// ORIGINAL LINE: uint l;
				int l = 0;
				RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
				CharPtr s = luaL_checklstring(L, arg, tempRef_l);
				l = tempRef_l.argvalue;
				status = ((status != 0) && (fwrite(s,
						GetUnmanagedSize(Character.class), (int) l, f) == l)) ? 1
						: 0;
			}
		}
		return pushresult(L, status, null);
	}

	public static int io_write(lua_State L) {
		return g_write(L, getiofile(L, IO_OUTPUT).fileOut, 1);
	}

	public static int f_write(lua_State L) {
		return g_write(L, tofile(L).fileOut, 2);
	}

	public static int f_seek(lua_State L) {
		int[] mode = { SEEK_SET, SEEK_CUR, SEEK_END };
		CharPtr[] modenames = { new CharPtr("set"), new CharPtr("cur"),
				new CharPtr("end"), null };
		InputStream f = tofile(L).file;
		int op = luaL_checkoption(L, 2, "cur", modenames);
		long offset = luaL_optlong(L, 3, 0);
		op = fseek(f, offset, mode[op]);
		if (op != 0) {
			return pushresult(L, 0, null); // error
		} else {
			lua_pushinteger(L, ftell(f));
			return 1;
		}
	}

	public static int f_setvbuf(lua_State L) {
		CharPtr[] modenames = { new CharPtr("no"), new CharPtr("full"),
				new CharPtr("line"), null };
		int[] mode = { _IONBF, _IOFBF, _IOLBF };
		InputStream f = tofile(L).file;
		int op = luaL_checkoption(L, 2, (CharPtr) null, modenames);
		int sz = luaL_optinteger(L, 3, LUAL_BUFFERSIZE);
		int res = setvbuf(f, (CharPtr) null, mode[op], (int) sz);
		return pushresult(L, (res == 0) ? 1 : 0, null);
	}

	public static int io_flush(lua_State L) {
		int result = 1;
		/*
		 * try { getiofile(L, IO_OUTPUT).Flush(); } catch (java.lang.Exception
		 * e) { result = 0; }
		 */
		return pushresult(L, result, null);
	}

	public static int f_flush(lua_State L) {
		int result = 1;
		/*
		 * try { tofile(L).Flush(); } catch (java.lang.Exception e) { result =
		 * 0; }
		 */
		return pushresult(L, result, null);
	}

	private final static luaL_Reg[] iolib = {
			new luaL_Reg("close", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_close((lua_State)arg);
				}
			}),
			new luaL_Reg("flush", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_flush((lua_State)arg);
				}
			}),
			new luaL_Reg("input", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_input((lua_State)arg);
				}
			}),
			new luaL_Reg("lines", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_lines((lua_State)arg);
				}
			}), 
			new luaL_Reg("open", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_open((lua_State)arg);
				}
			}),
			new luaL_Reg("output", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_output((lua_State)arg);
				}
			}),
			new luaL_Reg("popen", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_popen((lua_State)arg);
				}
			}), 
			new luaL_Reg("read", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_read((lua_State)arg);
				}
			}),
			new luaL_Reg("tmpfile", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_tmpfile((lua_State)arg);
				}
			}),
			new luaL_Reg("type", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_type((lua_State)arg);
				}
			}), 
			new luaL_Reg("write", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_write((lua_State)arg);
				}
			}),
			new luaL_Reg((String)null, (String)null) };

	private final static luaL_Reg[] flib = { 
			new luaL_Reg("close", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_close((lua_State)arg);
				}
			}),
			new luaL_Reg("flush", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return f_flush((lua_State)arg);
				}
			}), 
			new luaL_Reg("lines", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return f_lines((lua_State)arg);
				}
			}),
			new luaL_Reg("read", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return f_read((lua_State)arg);
				}
			}), 
			new luaL_Reg("seek", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return f_seek((lua_State)arg);
				}
			}),
			new luaL_Reg("setvbuf", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return f_setvbuf((lua_State)arg);
				}
			}),
			new luaL_Reg("write", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return f_write((lua_State)arg);
				}
			}), 
			new luaL_Reg("__gc", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_gc((lua_State)arg);
				}
			}),
			new luaL_Reg("__tostring", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return io_tostring((lua_State)arg);
				}
			}), 
			new luaL_Reg((String)null, (String)null) };

	public static void createmeta(lua_State L) {
		luaL_newmetatable(L, LUA_FILEHANDLE); // create metatable for file files
		lua_pushvalue(L, -1); // push metatable
		lua_setfield(L, -2, "__index"); // metatable.__index = metatable
		luaL_register(L, (CharPtr) null, flib); // file methods
	}

	public static void createstdfile(lua_State L, InputStream f, int k,
			CharPtr fname) {
		newfile(L).file = (InputStream) f;
		if (k > 0) {
			lua_pushvalue(L, -1);
			lua_rawseti(L, LUA_ENVIRONINDEX, k);
		}
		lua_pushvalue(L, -2); // copy environment
		lua_setfenv(L, -2); // set it
		lua_setfield(L, -3, fname);
	}

	public static void createstdfile(lua_State L, InputStream f, int k,
			String fnameS) {
		CharPtr fname = new CharPtr(fnameS);
		newfile(L).file = (InputStream) f;
		if (k > 0) {
			lua_pushvalue(L, -1);
			lua_rawseti(L, LUA_ENVIRONINDEX, k);
		}
		lua_pushvalue(L, -2); // copy environment
		lua_setfenv(L, -2); // set it
		lua_setfield(L, -3, fname);
	}
	
	public static void createstdofile(lua_State L, OutputStream f, int k,
			String fnameS) {
		CharPtr fname = new CharPtr(fnameS);
		newfile(L).fileOut = f;
		if (k > 0) {
			lua_pushvalue(L, -1);
			lua_rawseti(L, LUA_ENVIRONINDEX, k);
		}
		lua_pushvalue(L, -2); // copy environment
		lua_setfenv(L, -2); // set it
		lua_setfield(L, -3, fname);
	}

	// public static void newfenv(lua_State L, lua_CFunction cls)
	public static void newfenv(lua_State L, IDelegate cls) {
		lua_createtable(L, 0, 1);
		lua_pushcfunction(L, cls);
		lua_setfield(L, -2, "__close");
	}

	/*
	 * static IDelegate io_fclose = lua_CFunction.build(Lua.class, "io_fclose");
	 * static IDelegate io_noclose = lua_CFunction.build(Lua.class,
	 * "io_noclose"); static IDelegate io_pclose =
	 * lua_CFunction.build(Lua.class, "io_pclose");
	 */
	static IDelegate io_fclose = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return io_fclose((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	static IDelegate io_noclose = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return io_noclose((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	static IDelegate io_pclose = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return io_pclose((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int luaopen_io(lua_State L) {
		createmeta(L);
		/*
		 * create (private) environment (with fields IO_INPUT, IO_OUTPUT,
		 * __close)
		 */
		newfenv(L, io_fclose);
		lua_replace(L, LUA_ENVIRONINDEX);
		/* open library */
		luaL_register(L, LUA_IOLIBNAME, iolib);
		/* create (and set) default files */
		newfenv(L, io_noclose); // close function for default files
		createstdfile(L, stdin, IO_INPUT, "stdin");
		createstdofile(L, stdout, IO_OUTPUT, "stdout");
		createstdofile(L, stderr, 0, "stderr");
		lua_pop(L, 1); // pop environment for default files
		lua_getfield(L, -1, "popen");
		newfenv(L, io_pclose); // create environment for 'popen'
		lua_setfenv(L, -2); // set fenv for 'popen'
		lua_pop(L, 1); // pop 'popen'
		return 1;
	}

	public static final int FIRST_RESERVED = 257;

	/* maximum length of a reserved word */
	public static final int TOKEN_LEN = 9; // "function"

	/*
	 * WARNING: if you change the order of this enumeration, grep
	 * "ORDER RESERVED"
	 */
	public enum RESERVED {
		/* terminal symbols denoted by reserved words */
		TK_AND(FIRST_RESERVED), TK_BREAK(FIRST_RESERVED + 1), TK_DO(
				FIRST_RESERVED + 2), TK_ELSE(FIRST_RESERVED + 2 + 1), TK_ELSEIF(
				FIRST_RESERVED + 2 + 2), TK_END(FIRST_RESERVED + 2 + 2 + 1), TK_FALSE(
				FIRST_RESERVED + 2 + 2 + 2), TK_FOR(FIRST_RESERVED + 2 + 2 + 2
				+ 1), TK_FUNCTION(FIRST_RESERVED + 2 + 2 + 2 + 2), TK_IF(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 1), TK_IN(FIRST_RESERVED + 2
				+ 2 + 2 + 2 + 2), TK_LOCAL(FIRST_RESERVED + 2 + 2 + 2 + 2 + 2
				+ 1), TK_NIL(FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2), TK_NOT(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 1), TK_OR(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2), TK_REPEAT(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1), TK_RETURN(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2), TK_THEN(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1), TK_TRUE(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2), TK_UNTIL(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1), TK_WHILE(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2),
		/* other terminal symbols */
		TK_CONCAT(FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1), TK_DOTS(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2), TK_EQ(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1), TK_GE(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2), TK_LE(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2
						+ 1), TK_NE(FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2
				+ 2 + 2 + 2 + 2 + 2 + 2), TK_NUMBER(FIRST_RESERVED + 2 + 2 + 2
				+ 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1), TK_NAME(
				FIRST_RESERVED + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2
						+ 2 + 2), TK_STRING(FIRST_RESERVED + 2 + 2 + 2 + 2 + 2
				+ 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 1), TK_EOS(FIRST_RESERVED
				+ 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2);

		private int intValue;
		public static java.util.HashMap<Integer, RESERVED> mappings;

		private synchronized static java.util.HashMap<Integer, RESERVED> getMappings() {
			if (mappings == null) {
				mappings = new java.util.HashMap<Integer, RESERVED>();
			}
			return mappings;
		}

		private RESERVED(int value) {
			intValue = value;
			RESERVED.getMappings().put(value, this);
		}

		public int getValue() {
			return intValue;
		}

		public static RESERVED forValue(int value) {
			return getMappings().get(value);
		}
	}

	/* number of reserved words */
	public static final int NUM_RESERVED = RESERVED.TK_WHILE.getValue()
			- FIRST_RESERVED + 1;

	public static class SemInfo {
		public SemInfo() {
		}

		public SemInfo(SemInfo copy) {
			this.r = copy.r;
			this.ts = copy.ts;
		}

		public double r;
		public TString ts;
	} // semantics information

	public static class Token {
		public Token() {
		}

		public Token(Token copy) {
			this.token = copy.token;
			this.seminfo = new SemInfo(copy.seminfo);
		}

		public int token;
		public SemInfo seminfo = new SemInfo();
	}

	public static class LexState {
		public int current; // current character (charint)
		public int linenumber; // input line counter
		public int lastline; // line of last token `consumed'
		public Token t = new Token(); // current token
		public Token lookahead = new Token(); // look ahead token
		public FuncState fs; // `FuncState' is private to the parser
		public lua_State L;
		public Zio z; // input stream
		public Mbuffer buff; // buffer for tokens
		public TString source; // current source name
		public char decpoint; // locale decimal point
	}

	public static void next(LexState ls) {
		ls.current = zgetc(ls.z);
	}

	public static boolean currIsNewline(LexState ls) {
		return (ls.current == '\n' || ls.current == '\r');
	}

	/* ORDER RESERVED */
	public static final String[] luaX_tokens = { "and", "break", "do", "else",
			"elseif", "end", "false", "for", "function", "if", "in", "local",
			"nil", "not", "or", "repeat", "return", "then", "true", "until",
			"while", "..", "...", "==", ">=", "<=", "~=", "<number>", "<name>",
			"<string>", "<eof>" };

	public static void save_and_next(LexState ls) {
		save(ls, ls.current);
		next(ls);
	}

	public static void save(LexState ls, int c) {
		Mbuffer b = ls.buff;
		if (b.n + 1 > b.buffsize) {
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint newsize;
			int newsize;
			if (b.buffsize >= MAX_SIZET / 2) {
				luaX_lexerror(ls, "lexical element too long", 0);
			}
			newsize = b.buffsize * 2;
			luaZ_resizebuffer(ls.L, b, (int) newsize);
		}
		b.buffer.setItem(b.n++, (char) c);
	}

	public static void luaX_init(lua_State L) {
		int i;
		for (i = 0; i < NUM_RESERVED; i++) {
			TString ts = luaS_new(L, luaX_tokens[i]);
			luaS_fix(ts); // reserved words are never collected
			lua_assert(luaX_tokens[i].length() + 1 <= TOKEN_LEN);
			ts.gettsv().SetReserved(cast_byte(i + 1)); // reserved word
		}
	}

	public static final int MAXSRC = 80;

	public static CharPtr luaX_token2str(LexState ls, int token) {
		if (token < FIRST_RESERVED) {
			lua_assert(token == (byte) token);
			return (iscntrl(token)) ? luaO_pushfstring(ls.L, "char(%d)", token)
					: luaO_pushfstring(ls.L, "%c", token);
		} else {
			if((int)token - FIRST_RESERVED < luaX_tokens.length)
				return new CharPtr(luaX_tokens[(int) token - FIRST_RESERVED]);
			else
				return new CharPtr("");
		}
	}

	public static CharPtr txtToken(LexState ls, int token) {
		if (token == RESERVED.TK_NAME.getValue()
				|| token == RESERVED.TK_STRING.getValue()
				|| token == RESERVED.TK_NUMBER.getValue()) {
			save(ls, '\0');
			return luaZ_buffer(ls.buff);
		} else
			return luaX_token2str(ls, token);
	}

	public static void luaX_lexerror(LexState ls, CharPtr msg, int token) {
		CharPtr buff = new CharPtr(new char[MAXSRC]);
		luaO_chunkid(buff, getstr(ls.source), MAXSRC);
		msg = luaO_pushfstring(ls.L, "%s:%d: %s", buff, ls.linenumber, msg);
		if (token != 0) {
			luaO_pushfstring(ls.L, "%s near " + getLUA_QS(), msg,
					txtToken(ls, token));
		}
		luaD_throw(ls.L, LUA_ERRSYNTAX);
	}

	public static void luaX_lexerror(LexState ls, String msgS, int token) {
		CharPtr msg = new CharPtr(msgS);
		CharPtr buff = new CharPtr(new char[MAXSRC]);
		luaO_chunkid(buff, getstr(ls.source), MAXSRC);
		msg = luaO_pushfstring(ls.L, "%s:%d: %s", buff, ls.linenumber, msg);
		if (token != 0) {
			luaO_pushfstring(ls.L, "%s near " + getLUA_QS(), msg,
					txtToken(ls, token));
		}
		luaD_throw(ls.L, LUA_ERRSYNTAX);
	}

	public static void luaX_syntaxerror(LexState ls, CharPtr msg) {
		luaX_lexerror(ls, msg, ls.t.token);
	}

	public static void luaX_syntaxerror(LexState ls, String msg) {
		luaX_lexerror(ls, msg, ls.t.token);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static TString luaX_newstring(LexState ls, CharPtr
	// str, uint l)
	public static TString luaX_newstring(LexState ls, CharPtr str, int l) {
		lua_State L = ls.L;
		TString ts = luaS_newlstr(L, str, l);
		lua_TValue o = luaH_setstr(L, ls.fs.h, ts); // entry for `str'
		if (ttisnil(o)) {
			setbvalue(o, 1); // make sure `str' will not be collected
		}
		return ts;
	}

	public static TString luaX_newstring(LexState ls, String strS, int l) {
		CharPtr str = new CharPtr(strS);
		lua_State L = ls.L;
		TString ts = luaS_newlstr(L, str, l);
		lua_TValue o = luaH_setstr(L, ls.fs.h, ts); // entry for `str'
		if (ttisnil(o)) {
			setbvalue(o, 1); // make sure `str' will not be collected
		}
		return ts;
	}

	public static void inclinenumber(LexState ls) {
		int old = ls.current;
		lua_assert(currIsNewline(ls));
		next(ls); // skip `\n' or `\r'
		if (currIsNewline(ls) && ls.current != old) {
			next(ls); // skip `\n\r' or `\r\n'
		}
		if (++ls.linenumber >= MAX_INT) {
			luaX_syntaxerror(ls, "chunk has too many lines");
		}
	}

	public static void luaX_setinput(lua_State L, LexState ls, Zio z,
			TString source) {
		ls.decpoint = '.';
		ls.L = L;
		ls.lookahead.token = RESERVED.TK_EOS.getValue(); // no look-ahead token
		ls.z = z;
		ls.fs = null;
		ls.linenumber = 1;
		ls.lastline = 1;
		ls.source = source;
		luaZ_resizebuffer(ls.L, ls.buff, LUA_MINBUFFER); // initialize buffer
		next(ls); // read first char
	}

	/*
	 * * =======================================================* LEXICAL
	 * ANALYZER* =======================================================
	 */

	public static int check_next(LexState ls, CharPtr set) {
		if (strchr(set, (char) ls.current) == null) {
			return 0;
		}
		save_and_next(ls);
		return 1;
	}

	public static int check_next(LexState ls, String setS) {
		CharPtr set = new CharPtr(setS);
		if (strchr(set, (char) ls.current) == null) {
			return 0;
		}
		save_and_next(ls);
		return 1;
	}

	public static void buffreplace(LexState ls, char from, char to) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint n = luaZ_bufflen(ls.buff);
		int n = luaZ_bufflen(ls.buff);
		CharPtr p = luaZ_buffer(ls.buff);
		while ((n--) != 0) {
			if (p.getItem(n) == from) {
				p.setItem(n, to);
			}
		}
	}

	public static void trydecpoint(LexState ls, SemInfo seminfo) {
		/* format error: try to update decimal point separator */
		// todo: add proper support for localeconv - mjf
		// lconv cv = localeconv();
		char old = ls.decpoint;
		ls.decpoint = '.'; // (cv ? cv.decimal_point[0] : '.');
		buffreplace(ls, old, ls.decpoint); // try updated decimal separator
		RefObject<Double> tempRef_r = new RefObject<Double>(seminfo.r);
		boolean tempVar = luaO_str2d(luaZ_buffer(ls.buff), tempRef_r) == 0;
		seminfo.r = tempRef_r.argvalue;
		if (tempVar) {
			/* format error with correct decimal point: no more options */
			buffreplace(ls, ls.decpoint, '.'); // undo change (for error
												// message)
			luaX_lexerror(ls, "malformed number", RESERVED.TK_NUMBER.getValue());
		}
	}

	/* double */
	public static void read_numeral(LexState ls, SemInfo seminfo) {
		lua_assert(isdigit(ls.current));
		do {
			save_and_next(ls);
		} while (isdigit(ls.current) || ls.current == '.');
		if (check_next(ls, "Ee") != 0) // `E'?
		{
			check_next(ls, "+-"); // optional exponent sign
		}
		while (isalnum(ls.current) || ls.current == '_') {
			save_and_next(ls);
		}
		save(ls, '\0');
		buffreplace(ls, '.', ls.decpoint); // follow locale for decimal point
		RefObject<Double> tempRef_r = new RefObject<Double>(seminfo.r);
		boolean tempVar = luaO_str2d(luaZ_buffer(ls.buff), tempRef_r) == 0;
		seminfo.r = tempRef_r.argvalue;
		if (tempVar) // format error?
		{
			trydecpoint(ls, seminfo); // try to update decimal point separator
		}
	}

	public static int skip_sep(LexState ls) {
		int count = 0;
		int s = ls.current;
		lua_assert(s == '[' || s == ']');
		save_and_next(ls);
		while (ls.current == '=') {
			save_and_next(ls);
			count++;
		}
		return (ls.current == s) ? count : (-count) - 1;
	}

	public static void read_long_string(LexState ls, SemInfo seminfo, int sep) {
		// int cont = 0;
		// (void)(cont); /* avoid warnings when `cont' is not used */
		save_and_next(ls); // skip 2nd `['
		if (currIsNewline(ls)) // string starts with a newline?
		{
			inclinenumber(ls); // skip it
		}
		for (;;) {
			boolean breakMe = false;
			switch (ls.current) {
			case EOZ:
				luaX_lexerror(ls, (seminfo != null) ? "unfinished long string"
						: "unfinished long comment", RESERVED.TK_EOS.getValue());
				break; // to avoid warnings
				// FUCK TODO TASK: There is no preprocessor in Java:
				// #if LUA_COMPAT_LSTR
			/*
			 * case '[': { if (skip_sep(ls) == sep) { save_and_next(ls); // skip
			 * 2nd `[' cont++; //FUCK TODO TASK: There is no preprocessor in
			 * Java: //#if LUA_COMPAT_LSTR if (sep == 0) { luaX_lexerror(ls,
			 * "nesting of [[...]] is deprecated", '['); } //#endif } break; }
			 */
			// #endif
			case ']':
				if (skip_sep(ls) == sep) {
					save_and_next(ls); // skip 2nd `]'
					// /#if defined(LUA_COMPAT_LSTR) && LUA_COMPAT_LSTR == 2
					// cont--;
					// if (sep == 0 && cont >= 0) break;
					// /#endif
					// FUCK TODO TASK: There is no 'goto' in Java:
					// goto endloop;
					breakMe = true;
					break;
				}
				break;
			case '\n':
			case '\r':
				save(ls, '\n');
				inclinenumber(ls);
				if (seminfo == null) // avoid wasting space
				{
					luaZ_resetbuffer(ls.buff);
				}
				break;
			default: {
				if (seminfo != null) {
					save_and_next(ls);
				} else {
					next(ls);
				}
			}
				break;
			}
			if (breakMe)
				break;
		}
		// endloop:
		if (seminfo != null) {
			seminfo.ts = luaX_newstring(ls,
					CharPtr.OpAddition(luaZ_buffer(ls.buff), (2 + sep)),
					(int) (luaZ_bufflen(ls.buff) - 2 * (2 + sep)));// seminfo.ts
																	// =
																	// luaX_newstring(ls,
																	// luaZ_buffer(ls.buff)
																	// + (2 +
																	// sep),
																	// (int)(luaZ_bufflen(ls.buff)
																	// - 2*(2 +
																	// sep)));
		}
	}

	public static void read_string(LexState ls, int del, SemInfo seminfo) {
		save_and_next(ls);
		while (ls.current != del) {
			switch (ls.current) {
			case EOZ:
				luaX_lexerror(ls, "unfinished string",
						RESERVED.TK_EOS.getValue());
				continue; // to avoid warnings
			case '\n':
			case '\r':
				luaX_lexerror(ls, "unfinished string",
						RESERVED.TK_STRING.getValue());
				continue; // to avoid warnings
			case '\\': {
				int c;
				next(ls); // do not save the `\'
				switch (ls.current) {
				/*
				 * case 'a': c = '\a'; break;
				 */
				case 'b':
					c = '\b';
					break;
				case 'f':
					c = '\f';
					break;
				case 'n':
					c = '\n';
					break;
				case 'r':
					c = '\r';
					break;
				case 't':
					c = '\t';
					break;
				/*
				 * case 'v': c = '\v'; break;
				 */
				case '\n': // go through
				case '\r':
					save(ls, '\n');
					inclinenumber(ls);
					continue;
				case EOZ: // will raise an error next loop
					continue;
				default: {
					if (!isdigit(ls.current)) {
						save_and_next(ls); // handles \\, \", \', and \?
					} else // \xxx
					{
						int i = 0;
						c = 0;
						do {
							c = 10 * c + (ls.current - '0');
							next(ls);
						} while (++i < 3 && isdigit(ls.current));
						if (c > Byte.MAX_VALUE) {
							luaX_lexerror(ls, "escape sequence too large",
									RESERVED.TK_STRING.getValue());
						}
						save(ls, c);
					}
					continue;
				}
				}
				save(ls, c);
				next(ls);
				continue;
			}
			default:
				save_and_next(ls);
				break;
			}
		}
		save_and_next(ls); // skip delimiter
		seminfo.ts = luaX_newstring(ls,
				CharPtr.OpAddition(luaZ_buffer(ls.buff), 1),
				luaZ_bufflen(ls.buff) - 2);// seminfo.ts = luaX_newstring(ls,
											// luaZ_buffer(ls.buff) + 1,
											// luaZ_bufflen(ls.buff) - 2);
	}

	public static int llex(LexState ls, SemInfo seminfo) {
		luaZ_resetbuffer(ls.buff);
		for (;;) {
			switch (ls.current) {
			case '\n':
			case '\r': {
				inclinenumber(ls);
				continue;
			}
			case 65279:
			{
				next(ls);
			}break;
			case '-': {
				next(ls);
				if (ls.current != '-') {
					return '-';
				}
				/* else is a comment */
				next(ls);
				if (ls.current == '[') {
					int sep = skip_sep(ls);
					luaZ_resetbuffer(ls.buff); // `skip_sep' may dirty the
												// buffer
					if (sep >= 0) {
						read_long_string(ls, null, sep); // long comment
						luaZ_resetbuffer(ls.buff);
						continue;
					}
				}
				/* else short comment */
				while (!currIsNewline(ls) && ls.current != EOZ) {
					next(ls);
				}
				continue;
			}
			case '[': {
				int sep = skip_sep(ls);
				if (sep >= 0) {
					read_long_string(ls, seminfo, sep);
					return RESERVED.TK_STRING.getValue();
				} else if (sep == -1) {
					return '[';
				} else {
					luaX_lexerror(ls, "invalid long string delimiter",
							RESERVED.TK_STRING.getValue());
				}
			}
				break;
			case '=': {
				next(ls);
				if (ls.current != '=') {
					return '=';
				} else {
					next(ls);
					return RESERVED.TK_EQ.getValue();
				}
			}
			case '<': {
				next(ls);
				if (ls.current != '=') {
					return '<';
				} else {
					next(ls);
					return RESERVED.TK_LE.getValue();
				}
			}
			case '>': {
				next(ls);
				if (ls.current != '=') {
					return '>';
				} else {
					next(ls);
					return RESERVED.TK_GE.getValue();
				}
			}
			case '~': {
				next(ls);
				if (ls.current != '=') {
					return '~';
				} else {
					next(ls);
					return RESERVED.TK_NE.getValue();
				}
			}
			case '"':
			case '\'': {
				read_string(ls, ls.current, seminfo);
				return RESERVED.TK_STRING.getValue();
			}
			case '.': {
				save_and_next(ls);
				if (check_next(ls, ".") != 0) {
					if (check_next(ls, ".") != 0) {
						return RESERVED.TK_DOTS.getValue(); // ...
					} else // ..
					{
						return RESERVED.TK_CONCAT.getValue();
					}
				} else if (!isdigit(ls.current)) {
					return '.';
				} else {
					read_numeral(ls, seminfo);
					return RESERVED.TK_NUMBER.getValue();
				}
			}
			case EOZ: {
				return RESERVED.TK_EOS.getValue();
			}
			default: {
				if (isspace(ls.current)) {
					lua_assert(!currIsNewline(ls));
					next(ls);
					continue;
				} else if (isdigit(ls.current)) {
					read_numeral(ls, seminfo);
					return RESERVED.TK_NUMBER.getValue();
				} else if (isalpha(ls.current) || ls.current == '_') {
					/* identifier or reserved word */
					TString ts;
					do {
						save_and_next(ls);
					} while (isalnum(ls.current) || ls.current == '_' 
						/*|| ls.current == '=' 
							|| ls.current == '<' 
								|| ls.current == '>' 
									|| ls.current == '~'*/);
					ts = luaX_newstring(ls, luaZ_buffer(ls.buff),
							luaZ_bufflen(ls.buff));
					if (ts.gettsv().reserved > 0) // reserved word?
					{
						return ts.gettsv().reserved - 1 + FIRST_RESERVED;
					} else {
						seminfo.ts = ts;
						return RESERVED.TK_NAME.getValue();
					}
				} else {
					int c = ls.current;
					next(ls);
					return c; // single-char tokens (+ - /...)
				}
			}
			}
		}
	}

	public static void luaX_next(LexState ls) {
		ls.lastline = ls.linenumber;
		if (ls.lookahead.token != RESERVED.TK_EOS.getValue()) { // is there a
																// look-ahead
																// token?
			ls.t = new Token(ls.lookahead); // use this one
			ls.lookahead.token = RESERVED.TK_EOS.getValue(); // and discharge it
		} else {
			ls.t.token = llex(ls, ls.t.seminfo); // read next token
		}
	}

	public static void luaX_lookahead(LexState ls) {
		lua_assert(ls.lookahead.token == RESERVED.TK_EOS.getValue());
		ls.lookahead.token = llex(ls, ls.lookahead.seminfo);
	}

	// typedef LUAI_UINT32 int;

	// typedef LUAI_UMEM int;

	// typedef LUAI_MEM int;

	/* chars used as small naturals (so that `char' is reserved for characters) */
	// typedef unsigned char byte;

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public const uint MAX_SIZET = uint.MaxValue - 2;
	public static final int MAX_SIZET = Integer.MAX_VALUE - 2;

	public static final int MAX_LUMEM = Integer.MAX_VALUE - 2;

	public static final int MAX_INT = (Integer.MAX_VALUE - 2); // maximum value
																// of an int (-2
																// for safety)

	/*
	 * * conversion of pointer to integer* this is for hashing only; there is no
	 * problem if the integer* cannot hold the whole pointer value
	 */
	// /#define IntPoint(p) ((uint)(int)(p))

	/* type to ensure maximum alignment */
	// typedef LUAI_USER_ALIGNMENT_T L_Umaxalign;

	/* result of a `usual argument conversion' over double */
	// typedef LUAI_UACNUMBER double;

	/* internal assertions for in-house debugging */

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if lua_assert
	/*
	 * //FUCK TODO TASK: Java annotations will not correspond to .NET
	 * attributes: //[Conditional("DEBUG")] public static void
	 * lua_assert(boolean c) { assert c; }
	 * 
	 * //FUCK TODO TASK: Java annotations will not correspond to .NET
	 * attributes: //[Conditional("DEBUG")] public static void lua_assert(int c)
	 * { assert c != 0; }
	 * 
	 * public static Object check_exp(boolean c, Object e) { lua_assert(c);
	 * return e; } public static Object check_exp(int c, Object e) {
	 * lua_assert(c != 0); return e; }
	 */

	// #else

	// FUCK TODO TASK: Java annotations will not correspond to .NET attributes:
	// [Conditional("DEBUG")]
	public static void lua_assert(boolean c) {
	}

	// FUCK TODO TASK: Java annotations will not correspond to .NET attributes:
	// [Conditional("DEBUG")]
	public static void lua_assert(int c) {
	}

	public static Object check_exp(boolean c, Object e) {
		return e;
	}

	public static Object check_exp(int c, Object e) {
		return e;
	}

	// #endif

	// FUCK TODO TASK: Java annotations will not correspond to .NET attributes:
	// [Conditional("DEBUG")]
	public static void api_check(Object o, boolean e) {
		lua_assert(e);
	}

	public static void api_check(Object o, int e) {
		lua_assert(e != 0);
	}

	// /#define UNUSED(x) ((void)(x)) /* to avoid warnings */

	public static short cast_byte(int i) {
		return new UnsignedByte(i).shortValue();
	}

	public static short cast_byte(long i) {
		return new UnsignedByte(i).shortValue();
	}

	public static short cast_byte(boolean i) {
		return i ? (short) 1 : (short) 0;
	}

	public static short cast_byte(double i) {
		return new UnsignedByte((long) i).shortValue();
	}

	public static short cast_byte(Object i) {
		return new UnsignedByte(((Integer) i).longValue()).shortValue();// (byte)(int)(i);
	}

	public static int cast_int(int i) {
		return (int) i;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: internal static int cast_int(uint i)
	/*
	 * public static int cast_int(int i) { return (int)i; }
	 */
	public static int cast_int(long i) {
		return (int) (int) i;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: internal static int cast_int(ulong i)
	/*
	 * public static int cast_int(long i) { return (int)(int)i; }
	 */
	public static int cast_int(boolean i) {
		return i ? (int) 1 : (int) 0;
	}

	public static int cast_int(double i) {
		return (int) i;
	}

	public static int cast_int(Object i) {
		// Debug.Assert(false, "Can't convert int.");
		return (Integer) i;
	}

	public static double cast_num(int i) {
		return (double) i;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: internal static double cast_num(uint i)
	/*
	 * public static double cast_num(int i) { return (double)i; }
	 */
	public static double cast_num(long i) {
		return (double) i;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: internal static double cast_num(ulong i)
	/*
	 * public static double cast_num(long i) { return (double)i; }
	 */
	public static double cast_num(boolean i) {
		return i ? (double) 1 : (double) 0;
	}

	public static double cast_num(Object i) {
		// Debug.Assert(false, "Can't convert number.");
		return (Float) (i);// Float.parseFloat(i);
	}

	/*
	 * * type for virtual-machine instructions* must be an unsigned with (at
	 * least) 4 bytes (see details in lopcodes.h)
	 */
	// typedef int int;

	/* maximum stack for a Lua function */
	public static final int MAXSTACK = 250;

	/* minimum size for the string table (must be power of 2) */
	public static final int MINSTRTABSIZE = 32;

	/* minimum size for string buffer */
	public static final int LUA_MINBUFFER = 32;

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if !lua_lock
	public static void lua_lock(lua_State L) {
	}

	public static void lua_unlock(lua_State L) {
	}

	// #endif

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if !luai_threadyield
	public static void luai_threadyield(lua_State L) {
		lua_unlock(L);
		lua_lock(L);
	}

	// #endif

	/*
	 * * macro to control inclusion of some hard tests on stack reallocation
	 */
	// /#ifndef HARDSTACKTESTS
	// /#define condhardstacktests(x) ((void)0)
	// /#else
	// /#define condhardstacktests(x) x
	// /#endif

	public static final double PI = 3.14159265358979323846;
	public static final double RADIANS_PER_DEGREE = PI / 180.0;

	public static int math_abs(lua_State L) {
		lua_pushnumber(L, Math.abs(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_sin(lua_State L) {
		lua_pushnumber(L, Math.sin(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_sinh(lua_State L) {
		lua_pushnumber(L, Math.sinh(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_cos(lua_State L) {
		lua_pushnumber(L, Math.cos(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_cosh(lua_State L) {
		lua_pushnumber(L, Math.cosh(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_tan(lua_State L) {
		lua_pushnumber(L, Math.tan(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_tanh(lua_State L) {
		lua_pushnumber(L, Math.tanh(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_asin(lua_State L) {
		lua_pushnumber(L, Math.asin(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_acos(lua_State L) {
		lua_pushnumber(L, Math.acos(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_atan(lua_State L) {
		lua_pushnumber(L, Math.atan(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_atan2(lua_State L) {
		lua_pushnumber(L,
				Math.atan2(luaL_checknumber(L, 1), luaL_checknumber(L, 2)));
		return 1;
	}

	public static int math_ceil(lua_State L) {
		lua_pushnumber(L, Math.ceil(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_floor(lua_State L) {
		lua_pushnumber(L, Math.floor(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_fmod(lua_State L) {
		lua_pushnumber(L, fmod(luaL_checknumber(L, 1), luaL_checknumber(L, 2)));
		return 1;
	}

	public static int math_modf(lua_State L) {
		double ip = 0;
		RefObject<Double> tempRef_ip = new RefObject<Double>(ip);
		double fp = modf(luaL_checknumber(L, 1), tempRef_ip);
		ip = tempRef_ip.argvalue;
		lua_pushnumber(L, ip);
		lua_pushnumber(L, fp);
		return 2;
	}

	public static int math_sqrt(lua_State L) {
		lua_pushnumber(L, Math.sqrt(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_pow(lua_State L) {
		lua_pushnumber(L,
				Math.pow(luaL_checknumber(L, 1), luaL_checknumber(L, 2)));
		return 1;
	}

	public static int math_log(lua_State L) {
		lua_pushnumber(L, Math.log(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_log10(lua_State L) {
		lua_pushnumber(L, Math.log10(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_exp(lua_State L) {
		lua_pushnumber(L, Math.exp(luaL_checknumber(L, 1)));
		return 1;
	}

	public static int math_deg(lua_State L) {
		lua_pushnumber(L, luaL_checknumber(L, 1) / RADIANS_PER_DEGREE);
		return 1;
	}

	public static int math_rad(lua_State L) {
		lua_pushnumber(L, luaL_checknumber(L, 1) * RADIANS_PER_DEGREE);
		return 1;
	}

	public static int math_frexp(lua_State L) {
		int e = 0;
		RefObject<Integer> tempRef_e = new RefObject<Integer>(e);
		lua_pushnumber(L, frexp(luaL_checknumber(L, 1), tempRef_e));
		e = tempRef_e.argvalue;
		lua_pushinteger(L, e);
		return 2;
	}

	public static int math_ldexp(lua_State L) {
		lua_pushnumber(L, ldexp(luaL_checknumber(L, 1), luaL_checkint(L, 2)));
		return 1;
	}

	public static int math_min(lua_State L) {
		int n = lua_gettop(L); // number of arguments
		double dmin = luaL_checknumber(L, 1);
		int i;
		for (i = 2; i <= n; i++) {
			double d = luaL_checknumber(L, i);
			if (d < dmin) {
				dmin = d;
			}
		}
		lua_pushnumber(L, dmin);
		return 1;
	}

	public static int math_max(lua_State L) {
		int n = lua_gettop(L); // number of arguments
		double dmax = luaL_checknumber(L, 1);
		int i;
		for (i = 2; i <= n; i++) {
			double d = luaL_checknumber(L, i);
			if (d > dmax) {
				dmax = d;
			}
		}
		lua_pushnumber(L, dmax);
		return 1;
	}

	public static java.util.Random rng = new java.util.Random();

	public static int math_random(lua_State L) {
		/*
		 * the `%' avoids the (rare) case of r==1, and is needed also because on
		 * some systems (SunOS!) `rand()' may return a value larger than
		 * RAND_MAX
		 */
		// double r = (double)(rng.Next()%RAND_MAX) / (double)RAND_MAX;
		double r = (double) rng.nextDouble();
		switch (lua_gettop(L)) // check number of arguments
		{
		case 0: // no arguments
		{
			lua_pushnumber(L, r); // Number between 0 and 1
			break;
		}
		case 1: // only upper limit
		{
			int u = luaL_checkint(L, 1);
			luaL_argcheck(L, 1 <= u, 1, "interval is empty");
			lua_pushnumber(L, Math.floor(r * u) + 1); // int between 1 and `u'
			break;
		}
		case 2: // lower and upper limits
		{
			int l = luaL_checkint(L, 1);
			int u = luaL_checkint(L, 2);
			luaL_argcheck(L, l <= u, 2, "interval is empty");
			lua_pushnumber(L, Math.floor(r * (u - l + 1)) + l); // int between
																// `l' and `u'
			break;
		}
		default:
			return luaL_error(L, "wrong number of arguments");
		}
		return 1;
	}

	public static int math_randomseed(lua_State L) {
		// srand(luaL_checkint(L, 1));
		rng = new java.util.Random(luaL_checkint(L, 1));
		return 0;
	}

	private final static luaL_Reg[] mathlib = {
			new luaL_Reg("abs", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_abs((lua_State)arg);
				}
			}),
			new luaL_Reg("acos", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_acos((lua_State)arg);
				}
			}),
			new luaL_Reg("asin", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_asin((lua_State)arg);
				}
			}),
			new luaL_Reg("atan2", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_atan2((lua_State)arg);
				}
			}),
			new luaL_Reg("atan", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_atan((lua_State)arg);
				}
			}),
			new luaL_Reg("ceil", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_ceil((lua_State)arg);
				}
			}),
			new luaL_Reg("cosh", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_cosh((lua_State)arg);
				}
			}), 
			new luaL_Reg("cos", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_cos((lua_State)arg);
				}
			}),
			new luaL_Reg("deg", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_deg((lua_State)arg);
				}
			}), 
			new luaL_Reg("exp", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_exp((lua_State)arg);
				}
			}),
			new luaL_Reg("floor", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_floor((lua_State)arg);
				}
			}),
			new luaL_Reg("fmod", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_fmod((lua_State)arg);
				}
			}),
			new luaL_Reg("frexp", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_frexp((lua_State)arg);
				}
			}),
			new luaL_Reg("ldexp", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_ldexp((lua_State)arg);
				}
			}),
			new luaL_Reg("log10", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_log10((lua_State)arg);
				}
			}),
			new luaL_Reg("log", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_log((lua_State)arg);
				}
			}), 
			new luaL_Reg("max", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_max((lua_State)arg);
				}
			}),
			new luaL_Reg("min", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_min((lua_State)arg);
				}
			}), 
			new luaL_Reg("modf", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_modf((lua_State)arg);
				}
			}),
			new luaL_Reg("pow", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_pow((lua_State)arg);
				}
			}), 
			new luaL_Reg("rad", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_rad((lua_State)arg);
				}
			}),
			new luaL_Reg("random", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_random((lua_State)arg);
				}
			}),
			new luaL_Reg("randomseed", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_randomseed((lua_State)arg);
				}
			}),
			new luaL_Reg("sinh", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_sinh((lua_State)arg);
				}
			}), 
			new luaL_Reg("sin", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_sin((lua_State)arg);
				}
			}),
			new luaL_Reg("sqrt", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_sqrt((lua_State)arg);
				}
			}),
			new luaL_Reg("tanh", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_tanh((lua_State)arg);
				}
			}), 
			new luaL_Reg("tan", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return math_tan((lua_State)arg);
				}
			}),
			new luaL_Reg((String)null, (String)null) };

	/*
	 * * Open math library
	 */
	public static int luaopen_math(lua_State L) {
		luaL_register(L, LUA_MATHLIBNAME, mathlib);
		lua_pushnumber(L, PI);
		lua_setfield(L, -2, "pi");
		lua_pushnumber(L, HUGE_VAL);
		lua_setfield(L, -2, "huge");
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if LUA_COMPAT_MOD
		lua_getfield(L, -1, "fmod");
		lua_setfield(L, -2, "mod");
		// #endif
		return 1;
	}

	public static final String MEMERRMSG = "not enough memory";

	public static <T> T[] luaM_reallocv(lua_State L, T[] block, int new_size,
			Class<?> tclass) {
		return (T[]) luaM_realloc_(L, block, new_size, tclass);
	}
	
	public static char[] cluaM_reallocv(lua_State L, char[] block, int new_size,
			Class<?> tclass) {
		return (char[]) cluaM_realloc_(L, block, new_size, tclass);
	}

	// /#define luaM_freemem(L, b, s) luaM_realloc_(L, (b), (s), 0)
	// /#define luaM_free(L, b) luaM_realloc_(L, (b), sizeof(*(b)), 0)
	// public static void luaM_freearray(lua_State L, object b, int n, Type t) {
	// luaM_reallocv(L, b, n, 0, Marshal.SizeOf(b)); }

	// C# has it's own gc, so nothing to do here...in theory...
	public static <T> void luaM_freemem(lua_State L, T b) {
		// luaM_realloc_(L, new T[] {b}, 0);
	}

	public static <T> void luaM_free(lua_State L, T b) {
		// luaM_realloc_(L, new T[] {b}, 0);
	}

	public static <T> void luaM_freearray(lua_State L, T[] b) {
		// luaM_reallocv(L, b, 0, tclass);
	}

	public static <T> void luaM_freearray(lua_State L, HashMap<Long, T> b) {

	}

	public static <T> T luaM_malloc(lua_State L, Class<T> tclass) {
		return (T) luaM_realloc_(L, tclass);
	}

	public static <T> T luaM_new(lua_State L, Class<T> tclass) {
		return (T) luaM_realloc_(L, tclass);
	}

	public static <T> T[] luaM_newvector(lua_State L, int n, Class<T> tclass) {
		return luaM_reallocv(L, null, n, tclass);
	}

	public static <T> void luaM_growvector(lua_State L, RefObject<T[]> v,
			int nelems, RefObject<Integer> size, int limit, CharPtr e,
			Class<T> tclass) {
		if (nelems + 1 > size.argvalue) {
			v.argvalue = (T[]) luaM_growaux_(L, v, size, limit, e, tclass);
		}
	}

	public static <T> T[] luaM_reallocvector(lua_State L, RefObject<T[]> v,
			int oldn, int n, Class<?> tclass) {
		assert (v.argvalue == null && oldn == 0) || (v.argvalue.length == oldn);
		v.argvalue = luaM_reallocv(L, v.argvalue, n, tclass);
		return v.argvalue;
	}
	
	public static char[] cluaM_reallocvector(lua_State L, RefObject<char[]> v,
			int oldn, int n, Class<?> tclass) {
		assert (v.argvalue == null && oldn == 0) || (v.argvalue.length == oldn);
		v.argvalue = cluaM_reallocv(L, v.argvalue, n, tclass);
		return v.argvalue;
	}

	/*
	 * * About the realloc function:* void * frealloc (void *ud, void *ptr, uint
	 * osize, uint nsize);* (`osize' is the old size, `nsize' is the new size)**
	 * Lua ensures that (ptr == null) iff (osize == 0).** * frealloc(ud, null,
	 * 0, x) creates a new block of size `x'** * frealloc(ud, p, x, 0) frees the
	 * block `p'* (in this specific case, frealloc must return null).*
	 * particularly, frealloc(ud, null, 0, 0) does nothing* (which is equivalent
	 * to free(null) in ANSI C)** frealloc returns null if it cannot create or
	 * reallocate the area* (any reallocation to an equal or smaller size cannot
	 * fail!)
	 */

	public static final int MINSIZEARRAY = 4;

	public static <T> T[] luaM_growaux_(lua_State L, RefObject<T[]> block,
			RefObject<Integer> size, int limit, CharPtr errormsg,
			Class<T> tclass) {
		T[] newblock;
		int newsize;
		if (size.argvalue >= limit / 2) { // cannot double it?
			if (size.argvalue >= limit) // cannot grow even a little?
			{
				luaG_runerror(L, errormsg);
			}
			newsize = limit; // still have at least one free place
		} else {
			newsize = size.argvalue * 2;
			if (newsize < MINSIZEARRAY) {
				newsize = MINSIZEARRAY; // minimum size
			}
		}
		newblock = luaM_reallocv(L, block.argvalue, newsize, tclass);
		size.argvalue = newsize; // update only when everything else is OK
		return newblock;
	}

	public static Object luaM_toobig(lua_State L) {
		luaG_runerror(L, "memory allocation error: block too big");
		return null; // to avoid warnings
	}

	/*
	 * * generic allocation routine.
	 */

	public static Object luaM_realloc_(lua_State L, Class<?> t) {
		int unmanaged_size = (int) GetUnmanagedSize(t);
		int nsize = unmanaged_size;
		Object new_obj = null;
		try {
			new_obj = t.newInstance();
		} catch (IllegalAccessException e) {
			Tools.LogException("Lua.java", e);
		} catch (InstantiationException e) {
			Tools.LogException("Lua.java", e);
		}// Object new_obj = System.Activator.CreateInstance(t);
		AddTotalBytes(L, nsize);
		return new_obj;
	}

	// TODO do we need this yavrum
	/*
	 * public static <T> Object luaM_realloc_(lua_State L) { int unmanaged_size
	 * = (int)GetUnmanagedSize(T.class); int nsize = unmanaged_size; T new_obj =
	 * (T)System.Activator.CreateInstance(T.class); AddTotalBytes(L, nsize);
	 * return new_obj; }
	 */

	public static <T> Object luaM_realloc_(lua_State L, T obj) {
		int unmanaged_size = (int) GetUnmanagedSize(obj.getClass());// int
																	// unmanaged_size
																	// =
																	// (int)GetUnmanagedSize(T.class);
		int old_size = (obj == null) ? 0 : unmanaged_size;
		int osize = old_size * unmanaged_size;
		int nsize = unmanaged_size;
		T new_obj = null;
		try {
			new_obj = (T) obj.getClass().newInstance();
		} catch (IllegalAccessException e) {
			Tools.LogException("Lua.java", e);
		} catch (InstantiationException e) {
			Tools.LogException("Lua.java", e);
		}// T new_obj = (T)System.Activator.CreateInstance(T.class);
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_obj;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T Instantianate(Class<?> tclass) throws IllegalAccessException, InstantiationException
	{
		if (tclass == Boolean.class
				|| tclass == boolean.class)
			return (T) Boolean.valueOf(false);
		else if (tclass == Character.class
				|| tclass == char.class)
			return (T) Character.valueOf('\0');
		else if(tclass == Byte.class
				|| tclass == byte.class)
			return (T) Byte.valueOf((byte)0);
		else if(tclass == Short.class
				|| tclass == short.class)
			return (T) Short.valueOf((short)0);
		else if(tclass == Integer.class
				|| tclass == int.class)
			return (T) Integer.valueOf(0);
		else if(tclass == Long.class
				|| tclass == long.class)
			return (T) Long.valueOf(0);
		else if(tclass == Float.class
				|| tclass == float.class)
			return (T) Float.valueOf(0);
		else if(tclass == Double.class
				|| tclass == double.class)
			return (T) Double.valueOf(0);
		else if(tclass == String.class)
			return (T) "";
		else if(tclass == Lua.lua_TValue.class)
			return (T) new lua_TValue();
		else if(tclass == Lua.CallInfo.class)
 			return (T) new CallInfo();
		else if(tclass == Lua.Node.class)
			return (T) new Node();
		else if(tclass == Lua.LocVar.class)
			return (T) new LocVar();
		else if(tclass == Lua.TString.class)
			return (T) new TString();
		else if(tclass == Lua.Proto.class)
			return (T) new Proto();
		else
		{
			Log.d("Reflection on", tclass.getName());
			return (T) tclass.newInstance();
		}
	}

	public static <T> Object[] luaM_realloc_(lua_State L, T[] old_block,
			int new_size, Class<?> tclass) {
		int unmanaged_size = (int) GetUnmanagedSize(tclass); // T.class?
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		T[] new_block = (T[]) Array.newInstance(tclass, new_size);// T[]
																	// new_block
																	// = new
																	// T[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			try {
					new_block[i] = Instantianate(tclass);
			} catch (IllegalAccessException e) {
				
				Tools.LogException("Lua.java", e);
			} catch (InstantiationException e) {
				
				Tools.LogException("Lua.java", e);
			}
		}
		if (CanIndex(tclass)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement) ((new_block[i] instanceof ArrayElement) ? new_block[i]
						: null);
				// Debug.Assert(elem != null,
				// String.format("Need to derive type %1$s from ArrayElement",
				// T.class.toString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}
	
	public static char[] cluaM_realloc_(lua_State L, char[] old_block,
			int new_size, Class<?> tclass) {
		int unmanaged_size = (int) GetUnmanagedSize(tclass); // T.class?
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		char[] new_block = new char[new_size];
																	// new_block
																	// = new
																	// T[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = '\0';
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static boolean CanIndex(java.lang.Class t) {
		if (t == Character.class) {
			return false;
		}
		if (t == Byte.class) {
			return false;
		}
		if (t == Integer.class) {
			return false;
		}
		if (t == Long.class) {
			return false;
		}
		if (t == Float.class)
		{
			return false;
		}
		if (t == Double.class)
		{
			return false;
		}
		if (t == LocVar.class) {
			return false;
		}
		return true;
	}

	public static void AddTotalBytes(lua_State L, int num_bytes) {
		G(L).totalbytes += (int) num_bytes;
	}

	public static void SubtractTotalBytes(lua_State L, int num_bytes) {
		G(L).totalbytes -= (int) num_bytes;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: static void AddTotalBytes(lua_State L, uint num_bytes)
	/*
	 * public static void AddTotalBytes(lua_State L, int num_bytes) {
	 * G(L).totalbytes += num_bytes; }
	 */
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: static void SubtractTotalBytes(lua_State L, uint
	// num_bytes)
	/*
	 * public static void SubtractTotalBytes(lua_State L, int num_bytes) {
	 * G(L).totalbytes -= num_bytes; }
	 */

	/* prefix for open functions in C libraries */
	public static final String LUA_POF = "luaopen_";

	/* separator for open functions in C libraries */
	public static final String LUA_OFSEP = "_";

	public static final String LIBPREFIX = "LOADLIB: ";

	public static final String POF = LUA_POF;
	public static final String LIB_FAIL = "open";

	/* error codes for ll_loadfunc */
	public static final int ERRLIB = 1;
	public static final int ERRFUNC = 2;

	// public static void setprogdir(lua_State L) { }

	public static void setprogdir(lua_State L) {
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if PocketPC
		CharPtr buff = new CharPtr("");// App.getApp().getApplicationContext().getFilesDir().getAbsolutePath());
		// CharPtr buff =
		// Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().GetName().CodeBase);
		// #else
		// CharPtr buff = Directory.GetCurrentDirectory();
		// #endif
		luaL_gsub(L, lua_tostring(L, -1), LUA_EXECDIR, buff);
		lua_remove(L, -2); // remove original string
	}

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_DL_DLOPEN
	/*
	 * *
	 * {========================================================================
	 * * This is an implementation of loadlib based on the dlfcn interface.* The
	 * dlfcn interface is available in Linux, SunOS, Solaris, IRIX, FreeBSD,*
	 * NetBSD, AIX 4.2, HPUX 11, and probably most other Unix flavors, at least*
	 * as an emulation layer on top of native functions.*
	 * =========================================================================
	 */

	// /#include <dlfcn.h>

	/*
	 * public static void ll_unloadlib(Object lib) { dlclose(lib); }
	 * 
	 * 
	 * public static void * ll_load(lua_State L, readonly CharPtr path) { void
	 * *lib = dlopen(path, RTLD_NOW); if (lib == null) { lua_pushstring(L,
	 * dlerror()); } return lib; }
	 * 
	 * 
	 * public static lua_CFunction ll_sym(lua_State L, Object lib, readonly
	 * CharPtr sym) { lua_CFunction f = (lua_CFunction)dlsym(lib, sym); if (f ==
	 * null) { lua_pushstring(L, dlerror()); } return f; }
	 */

	/* }====================================================== */

	// /#elif defined(LUA_DL_DLL)
	/*
	 * * {======================================================================
	 * * This is an implementation of loadlib for Windows using native
	 * functions.*
	 * =======================================================================
	 */

	// /#include <windows.h>

	// /#undef setprogdir

	/*
	 * public static void setprogdir(lua_State L) { char buff[MAX_PATH + 1];
	 * char *lb; //FUCK TODO TASK: There is no Java equivalent to 'sizeof':
	 * DWORD nsize = sizeof(buff)/GetUnmanagedSize(Character.class); DWORD n =
	 * GetModuleFileNameA(null, buff, nsize); if (n == 0 || n == nsize || (lb =
	 * strrchr(buff, '\\')) == null) { luaL_error(L,
	 * "unable to get ModuleFileName"); } else {lb = '\0'; luaL_gsub(L,
	 * lua_tostring(L, -1), LUA_EXECDIR, buff); lua_remove(L, -2); // remove
	 * original string } }
	 * 
	 * 
	 * public static void pusherror(lua_State L) { int error = GetLastError();
	 * char buffer[128]; //FUCK TODO TASK: There is no Java equivalent to
	 * 'sizeof': if (FormatMessageA(FORMAT_MESSAGE_IGNORE_INSERTS |
	 * FORMAT_MESSAGE_FROM_SYSTEM, null, error, 0, buffer, sizeof(buffer),
	 * null)) { lua_pushstring(L, buffer); } else { lua_pushfstring(L,
	 * "system error %d\n", error); } }
	 * 
	 * public static void ll_unloadlib(Object lib) {
	 * FreeLibrary((HINSTANCE)lib); }
	 * 
	 * 
	 * public static void * ll_load(lua_State L, readonly CharPtr path) {
	 * HINSTANCE lib = LoadLibraryA(path); if (lib == null) { pusherror(L); }
	 * return lib; }
	 * 
	 * 
	 * public static lua_CFunction ll_sym(lua_State L, Object lib, readonly
	 * CharPtr sym) { lua_CFunction f =
	 * (lua_CFunction)GetProcAddress((HINSTANCE)lib, sym); if (f == null) {
	 * pusherror(L); } return f; }
	 */

	/* }====================================================== */

	// #elif LUA_DL_DYLD
	/*
	 * * {======================================================================
	 * * Native Mac OS X / Darwin Implementation*
	 * =======================================================================
	 */

	// /#include <mach-o/dyld.h>

	/* Mac appends a `_' before C function names */
	// /#undef POF
	// /#define POF "_" LUA_POF

	/*
	 * public static void pusherror(lua_State L) { CharPtr err_str; CharPtr
	 * err_file; NSLinkEditErrors err; int err_num; NSLinkEditError(err,
	 * err_num, err_file, err_str); lua_pushstring(L, err_str); }
	 * 
	 * 
	 * public static CharPtr errorfromcode(NSObjectFileImageReturnCode ret) {
	 * switch (ret) { case NSObjectFileImageInappropriateFile: return
	 * "file is not a bundle"; case NSObjectFileImageArch: return
	 * "library is for wrong CPU type"; case NSObjectFileImageFormat: return
	 * "bad format"; case NSObjectFileImageAccess: return "cannot access file";
	 * case NSObjectFileImageFailure: default: return "unable to load library";
	 * } }
	 * 
	 * 
	 * public static void ll_unloadlib(Object lib) {
	 * NSUnLinkModule((NSModule)lib,
	 * NSUNLINKMODULE_OPTION_RESET_LAZY_REFERENCES); }
	 */

	/*
	 * public static void * ll_load(lua_State L, readonly CharPtr path) {
	 * NSObjectFileImage img; NSObjectFileImageReturnCode ret;
	 */
	/* this would be a rare case, but prevents crashing if it happens */
	/*
	 * if(!_dyld_present()) { lua_pushliteral(L, "dyld not present"); return
	 * null; } ret = NSCreateObjectFileImageFromFile(path, img); if (ret ==
	 * NSObjectFileImageSuccess) { NSModule mod = NSLinkModule(img, path,
	 * NSLINKMODULE_OPTION_PRIVATE | NSLINKMODULE_OPTION_RETURN_ON_ERROR);
	 * NSDestroyObjectFileImage(img); if (mod == null) { pusherror(L); } return
	 * mod; } lua_pushstring(L, errorfromcode(ret)); return null; }
	 * 
	 * 
	 * public static lua_CFunction ll_sym(lua_State L, Object lib, readonly
	 * CharPtr sym) { NSSymbol nss = NSLookupSymbolInModule((NSModule)lib, sym);
	 * if (nss == null) { lua_pushfstring(L, "symbol " + getLUA_QS() +
	 * " not found", sym); return null; } return
	 * (lua_CFunction)NSAddressOfSymbol(nss); }
	 */

	/* }====================================================== */

	// #else
	/*
	 * * {======================================================* Fallback for
	 * other systems* =======================================================
	 */

	// /#undef LIB_FAIL
	// /#define LIB_FAIL "absent"

	public static final String DLMSG = "dynamic libraries not enabled; check your Lua installation";

	public static void ll_unloadlib(Object lib) {
		// (void)lib; /* to avoid warnings */
	}

	public static Object ll_load(lua_State L, CharPtr path) {
		// (void)path; /* to avoid warnings */
		lua_pushliteral(L, DLMSG);
		return null;
	}

	// public static lua_CFunction ll_sym(lua_State L, Object lib, CharPtr sym)
	public static IDelegate ll_sym(lua_State L, Object lib, CharPtr sym) {
		// (void)lib; (void)sym; /* to avoid warnings */
		lua_pushliteral(L, DLMSG);
		return null;
	}

	/* }====================================================== */
	// #endif

	public static Object ll_register(lua_State L, CharPtr path) {
		// todo: the whole usage of plib here is wrong, fix it - mjf
		// void **plib;
		Object plib = null;
		lua_pushfstring(L, "%s%s", LIBPREFIX, path);
		lua_gettable(L, LUA_REGISTRYINDEX); // check library in registry?
		if (!lua_isnil(L, -1)) // is there an entry?
		{
			plib = lua_touserdata(L, -1);
		} else // no entry yet; create one
		{
			lua_pop(L, 1);
			// plib = lua_newuserdata(L, (uint)Marshal.SizeOf(plib));
			// plib[0] = null;
			luaL_getmetatable(L, "_LOADLIB");
			lua_setmetatable(L, -2);
			lua_pushfstring(L, "%s%s", LIBPREFIX, path);
			lua_pushvalue(L, -2);
			lua_settable(L, LUA_REGISTRYINDEX);
		}
		return plib;
	}

	/*
	 * * __gc tag method: calls library's `ll_unloadlib' function with the lib*
	 * handle
	 */
	public static int gctm(lua_State L) {
		Object lib = luaL_checkudata(L, 1, "_LOADLIB");
		if (lib != null) {
			ll_unloadlib(lib);
		}
		lib = null; // mark library as closed
		return 0;
	}

	public static int ll_loadfunc(lua_State L, CharPtr path, CharPtr sym) {
		Object reg = ll_register(L, path);
		if (reg == null) {
			reg = ll_load(L, path);
		}
		if (reg == null) {
			return ERRLIB; // unable to load library
		} else {
			IDelegate f = ll_sym(L, reg, sym);// lua_CFunction f = ll_sym(L,
												// reg, sym);
			if (f == null) {
				return ERRFUNC; // unable to find function
			}
			lua_pushcfunction(L, f);
			return 0; // return function
		}
	}

	public static int ll_loadlib(lua_State L) {
		CharPtr path = luaL_checkstring(L, 1);
		CharPtr init = luaL_checkstring(L, 2);
		int stat = ll_loadfunc(L, path, init);
		if (stat == 0) // no errors?
		{
			return 1; // return the loaded function
		} else // error; error message is on stack top
		{
			lua_pushnil(L);
			lua_insert(L, -2);
			lua_pushstring(L, (stat == ERRLIB) ? LIB_FAIL : "init");
			return 3; // return nil, error message, and where
		}
	}

	/*
	 * * {======================================================* 'require'
	 * function* =======================================================
	 */

	public static int readable(CharPtr filename) {
		InputStream f = (InputStream) fopen(filename, "r"); // try to open file
		if (f == null) // open failed
		{
			return 0;
		}
		fclose(f);
		return 1;
	}

	public static CharPtr pushnexttemplate(lua_State L, CharPtr path) {
		CharPtr l;
		while (path.getItem(0) == LUA_PATHSEP.charAt(0)) // skip separators
		{
			path = path.next();
		}
		if (path.getItem(0) == '\0') // no more templates
		{
			return null;
		}
		l = strchr(path, LUA_PATHSEP.charAt(0)); // find next separator
		if (l == null) {
			l = CharPtr.OpAddition(path, strlen(path));// l = path +
														// strlen(path);
		}
		lua_pushlstring(L, path, (int) CharPtr.OpSubtraction(l, path));// lua_pushlstring(L,
																		// path,
																		// (int)(l
																		// -
																		// path));
																		// //
																		// template
		return l;
	}

	public static CharPtr findfile(lua_State L, CharPtr name, CharPtr pname) {
		CharPtr path;
		name = luaL_gsub(L, name, ".", LUA_DIRSEP);
		lua_getfield(L, LUA_ENVIRONINDEX, pname);
		path = lua_tostring(L, -1);
		if (path == null) {
			luaL_error(L, LUA_QL("package.%s") + " must be a string", pname);
		}
		lua_pushliteral(L, ""); // error accumulator
		while ((path = pushnexttemplate(L, path)) != null) {
			CharPtr filename;
			filename = luaL_gsub(L, lua_tostring(L, -1), LUA_PATH_MARK, name);
			lua_remove(L, -2); // remove path template
			if (readable(filename) != 0) // does file exist and is readable?
			{
				return filename; // return that file name
			}
			lua_pushfstring(L, "\n\tno file " + getLUA_QS(), filename);
			lua_remove(L, -2); // remove file name
			lua_concat(L, 2); // add entry to possible error message
		}
		return null; // not found
	}

	public static CharPtr findfile(lua_State L, CharPtr name, String pname) {
		CharPtr path;
		name = luaL_gsub(L, name, ".", LUA_DIRSEP);
		lua_getfield(L, LUA_ENVIRONINDEX, pname);
		path = lua_tostring(L, -1);
		if (path == null) {
			luaL_error(L, LUA_QL("package.%s") + " must be a string", pname);
		}
		lua_pushliteral(L, ""); // error accumulator
		while ((path = pushnexttemplate(L, path)) != null) {
			CharPtr filename;
			filename = luaL_gsub(L, lua_tostring(L, -1), LUA_PATH_MARK, name);
			lua_remove(L, -2); // remove path template
			if (readable(filename) != 0) // does file exist and is readable?
			{
				return filename; // return that file name
			}
			lua_pushfstring(L, "\n\tno file " + getLUA_QS(), filename);
			lua_remove(L, -2); // remove file name
			lua_concat(L, 2); // add entry to possible error message
		}
		return null; // not found
	}

	public static void loaderror(lua_State L, CharPtr filename) {
		luaL_error(L, "error loading module " + getLUA_QS() + " from file "
				+ getLUA_QS() + ":\n\t%s", lua_tostring(L, 1), filename,
				lua_tostring(L, -1));
	}

	public static int loader_Lua(lua_State L) {
		CharPtr filename;
		CharPtr name = luaL_checkstring(L, 1);
		filename = findfile(L, name, "path");
		if (filename == null) // library not found in this path
		{
			return 1;
		}
		if (luaL_loadfile(L, filename) != 0) {
			loaderror(L, filename);
		}
		return 1; // library loaded successfully
	}

	public static CharPtr mkfuncname(lua_State L, CharPtr modname) {
		CharPtr funcname;
		CharPtr mark = strchr(modname, LUA_IGMARK.charAt(0));
		if (mark != null) {
			modname = CharPtr.OpAddition(mark, 1);// modname = mark + 1;
		}
		funcname = luaL_gsub(L, modname, ".", LUA_OFSEP);
		funcname = lua_pushfstring(L, POF + "%s", funcname);
		lua_remove(L, -2); // remove 'gsub' result
		return funcname;
	}

	public static int loader_C(lua_State L) {
		CharPtr funcname;
		CharPtr name = luaL_checkstring(L, 1);
		CharPtr filename = findfile(L, name, "cpath");
		if (filename == null) // library not found in this path
		{
			return 1;
		}
		funcname = mkfuncname(L, name);
		if (ll_loadfunc(L, filename, funcname) != 0) {
			loaderror(L, filename);
		}
		return 1; // library loaded successfully
	}

	public static int loader_Croot(lua_State L) {
		CharPtr funcname;
		CharPtr filename;
		CharPtr name = luaL_checkstring(L, 1);
		CharPtr p = strchr(name, '.');
		int stat;
		if (p == null) // is root
		{
			return 0;
		}
		lua_pushlstring(L, name, (int) CharPtr.OpSubtraction(p, name));// lua_pushlstring(L,
																		// name,
																		// (int)(p
																		// -
																		// name));
		filename = findfile(L, lua_tostring(L, -1), "cpath");
		if (filename == null) // root not found
		{
			return 1;
		}
		funcname = mkfuncname(L, name);
		if ((stat = ll_loadfunc(L, filename, funcname)) != 0) {
			if (stat != ERRFUNC) // real error
			{
				loaderror(L, filename);
			}
			lua_pushfstring(L, "\n\tno module " + getLUA_QS() + " in file "
					+ getLUA_QS(), name, filename);
			return 1; // function not found
		}
		return 1;
	}

	public static int loader_preload(lua_State L) {
		CharPtr name = luaL_checkstring(L, 1);
		lua_getfield(L, LUA_ENVIRONINDEX, "preload");
		if (!lua_istable(L, -1)) {
			luaL_error(L, LUA_QL("package.preload") + " must be a table");
		}
		lua_getfield(L, -1, name);
		if (lua_isnil(L, -1)) // not found?
		{
			lua_pushfstring(L, "\n\tno field package.preload['%s']", name);
		}
		return 1;
	}

	public static Object sentinel = new Object();

	public static int ll_require(lua_State L) {
		CharPtr name = luaL_checkstring(L, 1);
		int i;
		lua_settop(L, 1); // _LOADED table will be at index 2
		lua_getfield(L, LUA_REGISTRYINDEX, "_LOADED");
		lua_getfield(L, 2, name);
		if (lua_toboolean(L, -1) != 0) // is it there?
		{
			if (lua_touserdata(L, -1) == sentinel) // check loops
			{
				luaL_error(L, "loop or previous error loading module "
						+ getLUA_QS(), name);
			}
			return 1; // package is already loaded
		}
		/* else must load it; iterate over available loaders */
		lua_getfield(L, LUA_ENVIRONINDEX, "loaders");
		if (!lua_istable(L, -1)) {
			luaL_error(L, LUA_QL("package.loaders") + " must be a table");
		}
		lua_pushliteral(L, ""); // error message accumulator
		for (i = 1;; i++) {
			lua_rawgeti(L, -2, i); // get a loader
			if (lua_isnil(L, -1)) {
				luaL_error(L, "module " + getLUA_QS() + " not found:%s", name,
						lua_tostring(L, -2));
			}
			lua_pushstring(L, name);
			lua_call(L, 1, 1); // call it
			if (lua_isfunction(L, -1)) // did it find module?
			{
				break; // module loaded successfully
			} else if (lua_isstring(L, -1) != 0) // loader returned error
													// message?
			{
				lua_concat(L, 2); // accumulate it
			} else {
				lua_pop(L, 1);
			}
		}
		lua_pushlightuserdata(L, sentinel);
		lua_setfield(L, 2, name); // _LOADED[name] = sentinel
		lua_pushstring(L, name); // pass name as argument to module
		lua_call(L, 1, 1); // run loaded module
		if (!lua_isnil(L, -1)) // non-nil return?
		{
			lua_setfield(L, 2, name); // _LOADED[name] = returned value
		}
		lua_getfield(L, 2, name);
		if (lua_touserdata(L, -1) == sentinel) // module did not set a value?
		{
			lua_pushboolean(L, 1); // use true as result
			lua_pushvalue(L, -1); // extra copy to be returned
			lua_setfield(L, 2, name); // _LOADED[name] = true
		}
		return 1;
	}

	/* }====================================================== */

	/*
	 * * {======================================================* 'module'
	 * function* =======================================================
	 */

	public static void setfenv(lua_State L) {
		lua_Debug ar = new lua_Debug();
		if (lua_getstack(L, 1, ar) == 0 || lua_getinfo(L, "f", ar) == 0
				|| lua_iscfunction(L, -1)) // get calling function
		{
			luaL_error(L, LUA_QL("module") + " not called from a Lua function");
		}
		lua_pushvalue(L, -2);
		lua_setfenv(L, -2);
		lua_pop(L, 1);
	}

	public static void dooptions(lua_State L, int n) {
		int i;
		for (i = 2; i <= n; i++) {
			lua_pushvalue(L, i); // get option (a function)
			lua_pushvalue(L, -2); // module
			lua_call(L, 1, 0);
		}
	}

	public static void modinit(lua_State L, CharPtr modname) {
		CharPtr dot;
		lua_pushvalue(L, -1);
		lua_setfield(L, -2, "_M"); // module._M = module
		lua_pushstring(L, modname);
		lua_setfield(L, -2, "_NAME");
		dot = strrchr(modname, '.'); // look for last dot in module name
		if (dot == null) {
			dot = modname;
		} else {
			dot = dot.next();
		}
		/* set _PACKAGE as package name (full module name minus last part) */
		lua_pushlstring(L, modname, (int) CharPtr.OpSubtraction(dot, modname));// lua_pushlstring(L,
																				// modname,
																				// (int)(dot
																				// -
																				// modname));
		lua_setfield(L, -2, "_PACKAGE");
	}

	public static int ll_module(lua_State L) {
		CharPtr modname = luaL_checkstring(L, 1);
		int loaded = lua_gettop(L) + 1; // index of _LOADED table
		lua_getfield(L, LUA_REGISTRYINDEX, "_LOADED");
		lua_getfield(L, loaded, modname); // get _LOADED[modname]
		if (!lua_istable(L, -1)) // not found?
		{
			lua_pop(L, 1); // remove previous result
			/* try global variable (and create one if it does not exist) */
			if (luaL_findtable(L, LUA_GLOBALSINDEX, modname, 1) != null) {
				return luaL_error(L, "name conflict for module " + getLUA_QS(),
						modname);
			}
			lua_pushvalue(L, -1);
			lua_setfield(L, loaded, modname); // _LOADED[modname] = new table
		}
		/* check whether table already has a _NAME field */
		lua_getfield(L, -1, "_NAME");
		if (!lua_isnil(L, -1)) // is table an initialized module?
		{
			lua_pop(L, 1);
		} else // no; initialize it
		{
			lua_pop(L, 1);
			modinit(L, modname);
		}
		lua_pushvalue(L, -1);
		setfenv(L);
		dooptions(L, loaded - 1);
		return 0;
	}

	public static int ll_seeall(lua_State L) {
		luaL_checktype(L, 1, LUA_TTABLE);
		if (lua_getmetatable(L, 1) == 0) {
			lua_createtable(L, 0, 1); // create new metatable
			lua_pushvalue(L, -1);
			lua_setmetatable(L, 1);
		}
		lua_pushvalue(L, LUA_GLOBALSINDEX);
		lua_setfield(L, -2, "__index"); // mt.__index = _G
		return 0;
	}

	/* }====================================================== */

	/* auxiliary mark (for internal use) */
	public final static String AUXMARK = String.format("%1$s", (char) 1);

	// public static void setpath(lua_State L, CharPtr fieldname, CharPtr
	// envname, CharPtr def)
	public static void setpath(lua_State L, Object fieldnameS, Object envnameS,
			Object defS) {
		CharPtr fieldname = null;
		CharPtr envname = null;
		CharPtr def = null;

		if (fieldnameS instanceof CharPtr)
			fieldname = (CharPtr) fieldnameS;
		else
			fieldname = new CharPtr((String) fieldnameS);
		if (envnameS instanceof CharPtr)
			envname = (CharPtr) envnameS;
		else
			envname = new CharPtr((String) envnameS);
		if (defS instanceof CharPtr)
			def = (CharPtr) defS;
		else
			def = new CharPtr((String) defS);
		CharPtr path = getenv(envname);
		if (path == null) // no environment variable?
		{
			lua_pushstring(L, def); // use default
		} else {
			/* replace ";;" by ";AUXMARK;" and then AUXMARK by default path */
			path = luaL_gsub(L, path, LUA_PATHSEP + LUA_PATHSEP, LUA_PATHSEP
					+ AUXMARK + LUA_PATHSEP);
			luaL_gsub(L, path, AUXMARK, def);
			lua_remove(L, -2);
		}
		setprogdir(L);
		lua_setfield(L, -2, fieldname);
	}

	private final static luaL_Reg[] pk_funcs = {
			new luaL_Reg("loadlib", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return ll_loadlib((lua_State)arg);
				}
			}),
			new luaL_Reg("seeall", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return ll_seeall((lua_State)arg);
				}
			}), new luaL_Reg((String)null, (String)null) };

	private final static luaL_Reg[] ll_funcs = {
			new luaL_Reg("module", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return ll_module((lua_State)arg);
				}
			}),
			new luaL_Reg("require", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return ll_require((lua_State)arg);
				}
			}), new luaL_Reg((String)null, (String)null) };

	public final static IDelegate[] loaders = {
			/*lua_CFunction.build(Lua.class, "loader_preload"),
			lua_CFunction.build(Lua.class, "loader_Lua"),
			lua_CFunction.build(Lua.class, "loader_C"),
			lua_CFunction.build(Lua.class, "loader_Croot"), null };*/
		new OneDelegate()
		{	
			@Override
			public Object invoke(Object arg)
			{
				return loader_preload((lua_State)arg);
			}
		},
		new OneDelegate()
		{	
			@Override
			public Object invoke(Object arg)
			{
				return loader_Lua((lua_State)arg);
			}
		},
		new OneDelegate()
		{	
			@Override
			public Object invoke(Object arg)
			{
				return loader_C((lua_State)arg);
			}
		},
		new OneDelegate()
		{	
			@Override
			public Object invoke(Object arg)
			{
				return loader_Croot((lua_State)arg);
			}
		},
		null };
		
	// public final static lua_CFunction[] loaders = {loader_preload,
	// loader_Lua, loader_C, loader_Croot, null};

	// static IDelegate gctm = lua_CFunction.build(Lua.class, "gctm");
	static IDelegate gctm = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return gctm((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int luaopen_package(lua_State L) {
		int i;
		/* create new type _LOADLIB */
		luaL_newmetatable(L, "_LOADLIB");
		lua_pushcfunction(L, gctm);
		lua_setfield(L, -2, "__gc");
		/* create `package' table */
		luaL_register(L, LUA_LOADLIBNAME, pk_funcs);
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if LUA_COMPAT_LOADLIB
		/*
		 * lua_getfield(L, -1, "loadlib"); lua_setfield(L, LUA_GLOBALSINDEX,
		 * "loadlib");
		 */
		// #endif
		lua_pushvalue(L, -1);
		lua_replace(L, LUA_ENVIRONINDEX);
		/* create `loaders' table */
		lua_createtable(L, 0, loaders.length - 1);
		/* fill it with pre-defined loaders */
		for (i = 0; loaders[i] != null; i++) {
			lua_pushcfunction(L, loaders[i]);
			lua_rawseti(L, -2, i + 1);
		}
		lua_setfield(L, -2, "loaders"); // put it in field `loaders'
		setpath(L, "path", LUA_PATH, LUA_PATH_DEFAULT); // set field `path'
		setpath(L, "cpath", LUA_CPATH, LUA_CPATH_DEFAULT); // set field `cpath'
		/* store config information */
		lua_pushliteral(L, LUA_DIRSEP + "\n" + LUA_PATHSEP + "\n"
				+ LUA_PATH_MARK + "\n" + LUA_EXECDIR + "\n" + LUA_IGMARK);
		lua_setfield(L, -2, "config");
		/* set field `loaded' */
		luaL_findtable(L, LUA_REGISTRYINDEX, "_LOADED", 2);
		lua_setfield(L, -2, "loaded");
		/* set field `preload' */
		lua_newtable(L);
		lua_setfield(L, -2, "preload");
		lua_pushvalue(L, LUA_GLOBALSINDEX);
		luaL_register(L, (CharPtr) null, ll_funcs); // open lib into global
													// table
		lua_pop(L, 1);
		return 1; // return 'package' table
	}

	/* tags for values visible from Lua */
	public static final int LAST_TAG = LUA_TTHREAD;

	public static final int NUM_TAGS = (LAST_TAG + 1);

	/*
	 * * Extra tags for non-values
	 */
	public static final int LUA_TPROTO = (LAST_TAG + 1);
	public static final int LUA_TUPVAL = (LAST_TAG + 2);
	public static final int LUA_TDEADKEY = (LAST_TAG + 3);

	public interface ArrayElement {
		void set_index(int index);

		void set_array(Object array);
	}

	/*
	 * * Common Header for all collectable objects (in macro form, to be*
	 * included in other objects)
	 */
	public static class CommonHeader {
		public GCObject next;
		public byte tt;
		public byte marked;

		public GCObject GetNext() {
			return next;
		}

		public GCObject SetNext(GCObject val) {
			next = val;
			return next;
		}

		public byte GetTT() {
			return tt;
		}

		public void SetTT(byte val) {
			tt = val;
		}

		public byte GetMarked() {
			return marked;
		}

		public void SetMarked(byte val) {
			marked = val;
		}
	}

	/*
	 * * Common header in struct form
	 */
	public static class GCheader extends CommonHeader {
	}

	/*
	 * * Union of all Lua values (in c# we use virtual data members and boxing)
	 */
	public static class Value {

		// in the original code Value is a struct, so all assignments in the
		// code
		// need to be replaced with a call to Copy. as it turns out, there are
		// only
		// a couple. the vast majority of references to Value are the instance
		// that
		// appears in the lua_TValue class, so if you make that a virtual data
		// member and
		// omit the set accessor then you'll get a compiler error if anything
		// tries
		// to set it.
		public final void Copy(Value copy) {
			this.p = copy.p;
		}

		public final GCObject getgc() {
			return (GCObject) this.p;
		}

		public final void setgc(GCObject value) {
			this.p = value;
		}

		public Object p;

		public final double getn() {
			return ((Double) this.p).doubleValue();
		}

		public final void setn(double value) {
			this.p = (Object) value;
		}

		public final int getb() {
			return ((Integer) this.p).intValue();
		}

		public final void setb(int value) {
			this.p = (Object) (Integer) value;
		}
	}

	/*
	 * * Tagged Values
	 */

	// /#define TValuefields Value value; int tt

	public static class lua_TValue implements ArrayElement {
		private lua_TValue[] values = null;
		private int index = -1;

		public final void set_index(int index) {
			this.index = index;
		}

		public final void set_array(Object array) {
			this.values = (lua_TValue[]) array;
			assert this.values != null;
		}

		public final lua_TValue getItem(int offset) {
			return this.values[this.index + offset];
		}

		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public lua_TValue getItem(uint offset)
		/*
		 * public final lua_TValue getItem(int offset) { return
		 * this.values[this.index + (int)offset]; }
		 */

		public static lua_TValue OpAddition(lua_TValue value, int offset) {
			return value.values[value.index + offset];
		}

		public static lua_TValue OpAddition(int offset, lua_TValue value) {
			return value.values[value.index + offset];
		}

		public static lua_TValue OpSubtraction(lua_TValue value, int offset) {
			return value.values[value.index - offset];
		}

		public static int OpSubtraction(lua_TValue value, lua_TValue[] array) {
			assert value.values == array;
			return value.index;
		}

		public static int OpSubtraction(lua_TValue a, lua_TValue b) {
			assert a.values == b.values;
			return a.index - b.index;
		}

		public static boolean OpLessThan(lua_TValue a, lua_TValue b) {
			assert a.values == b.values;
			return a.index < b.index;
		}

		public static boolean OpLessThanOrEqual(lua_TValue a, lua_TValue b) {
			assert a.values == b.values;
			return a.index <= b.index;
		}

		public static boolean OpGreaterThan(lua_TValue a, lua_TValue b) {
			assert a.values == b.values;
			return a.index > b.index;
		}

		public static boolean OpGreaterThanOrEqual(lua_TValue a, lua_TValue b) {
			assert a.values == b.values;
			return a.index >= b.index;
		}

		public static lua_TValue inc(RefObject<lua_TValue> value) {
			value.argvalue = value.argvalue.getItem(1);
			return value.argvalue.getItem(-1);
		}

		public static lua_TValue dec(RefObject<lua_TValue> value) {
			value.argvalue = value.argvalue.getItem(-1);
			return value.argvalue.getItem(1);
		}

		// FUCK TODO TASK: The following operator overload is not converted by
		// Fuck:
		/*
		 * public static implicit operator int(lua_TValue value) { return
		 * value.index; }
		 */

		public lua_TValue() {
		}

		public lua_TValue(lua_TValue copy) {
			this.values = copy.values;
			this.index = copy.index;
			this.value.Copy(copy.value);
			this.tt = copy.tt;
		}

		public lua_TValue(Value value, int tt) {
			this.values = null;
			this.index = 0;
			this.value.Copy(value);
			this.tt = tt;
		}

		public Value value = new Value();
		public int tt;
	}

	/* Macros to test type */
	public static boolean ttisnil(lua_TValue o) {
		return (ttype(o) == LUA_TNIL);
	}

	public static boolean ttisnumber(lua_TValue o) {
		return (ttype(o) == LUA_TNUMBER);
	}

	public static boolean ttisstring(lua_TValue o) {
		return (ttype(o) == LUA_TSTRING);
	}

	public static boolean ttistable(lua_TValue o) {
		return (ttype(o) == LUA_TTABLE);
	}

	public static boolean ttisfunction(lua_TValue o) {
		return (ttype(o) == LUA_TFUNCTION);
	}

	public static boolean ttisboolean(lua_TValue o) {
		return (ttype(o) == LUA_TBOOLEAN);
	}

	public static boolean ttisuserdata(lua_TValue o) {
		return (ttype(o) == LUA_TUSERDATA);
	}

	public static boolean ttisthread(lua_TValue o) {
		return (ttype(o) == LUA_TTHREAD);
	}

	public static boolean ttislightuserdata(lua_TValue o) {
		return (ttype(o) == LUA_TLIGHTUSERDATA);
	}

	/* Macros to access values */
	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if DEBUG
	/*
	 * public static int ttype(lua_TValue o) { return o.tt; } public static int
	 * ttype(CommonHeader o) { return o.tt; } public static GCObject
	 * gcvalue(lua_TValue o) { return (GCObject)check_exp(iscollectable(o),
	 * o.value.gc); } public static Object pvalue(lua_TValue o) { return
	 * (Object)check_exp(ttislightuserdata(o), o.value.p); } public static
	 * double nvalue(lua_TValue o) { return (double)check_exp(ttisnumber(o),
	 * o.value.n); } public static TString rawtsvalue(lua_TValue o) { return
	 * (TString)check_exp(ttisstring(o), o.value.gc.ts); } public static
	 * TString_tsv tsvalue(lua_TValue o) { return rawtsvalue(o).gettsv(); }
	 * public static Udata rawuvalue(lua_TValue o) { return
	 * (Udata)check_exp(ttisuserdata(o), o.value.gc.u); } public static Udata_uv
	 * uvalue(lua_TValue o) { return rawuvalue(o).uv; } public static Closure
	 * clvalue(lua_TValue o) { return (Closure)check_exp(ttisfunction(o),
	 * o.value.gc.cl); } public static Table hvalue(lua_TValue o) { return
	 * (Table)check_exp(ttistable(o), o.value.gc.h); } public static int
	 * bvalue(lua_TValue o) { return ((Integer)check_exp(ttisboolean(o),
	 * o.value.b)).intValue(); } public static lua_State thvalue(lua_TValue o) {
	 * return (lua_State)check_exp(ttisthread(o), o.value.gc.th); }
	 */
	// #else
	public static int ttype(lua_TValue o) {
		return o.tt;
	}

	public static int ttype(CommonHeader o) {
		return o.tt;
	}

	public static GCObject gcvalue(lua_TValue o) {
		return o.value.getgc();
	}

	public static Object pvalue(lua_TValue o) {
		return o.value.p;
	}

	public static double nvalue(lua_TValue o) {
		return o.value.getn();
	}

	public static TString rawtsvalue(lua_TValue o) {
		return o.value.getgc().getts();
	}

	public static TString_tsv tsvalue(lua_TValue o) {
		return rawtsvalue(o).gettsv();
	}

	public static Udata rawuvalue(lua_TValue o) {
		return o.value.getgc().getu();
	}

	public static Udata_uv uvalue(lua_TValue o) {
		return rawuvalue(o).uv;
	}

	public static Closure clvalue(lua_TValue o) {
		return o.value.getgc().getcl();
	}

	public static Table hvalue(lua_TValue o) {
		return o.value.getgc().geth();
	}

	public static int bvalue(lua_TValue o) {
		return o.value.getb();
	}

	public static lua_State thvalue(lua_TValue o) {
		return (lua_State) check_exp(ttisthread(o), o.value.getgc().getth());
	}

	// #endif

	public static int l_isfalse(lua_TValue o) {
		return ((ttisnil(o) || (ttisboolean(o) && bvalue(o) == 0))) ? 1 : 0;
	}

	/*
	 * * for internal debug only
	 */
	// FUCK TODO TASK: Java annotations will not correspond to .NET attributes:
	// [Conditional("DEBUG")]
	public static void checkconsistency(lua_TValue obj) {
		lua_assert(!iscollectable(obj)
				|| (ttype(obj) == (obj).value.getgc().getgch().tt));
	}

	// FUCK TODO TASK: Java annotations will not correspond to .NET attributes:
	// [Conditional("DEBUG")]
	public static void checkliveness(global_State g, lua_TValue obj) {
		lua_assert(!iscollectable(obj)
				|| ((ttype(obj) == obj.value.getgc().getgch().tt) && !isdead(g,
						obj.value.getgc())));
	}

	/* Macros to set values */
	public static void setnilvalue(lua_TValue obj) {
		obj.tt = LUA_TNIL;
	}

	public static void setnvalue(lua_TValue obj, double x) {
		obj.value.setn(x);
		obj.tt = LUA_TNUMBER;
	}

	public static void setpvalue(lua_TValue obj, Object x) {
		obj.value.p = x;
		obj.tt = LUA_TLIGHTUSERDATA;
	}

	public static void setbvalue(lua_TValue obj, int x) {
		obj.value.setb(x);
		obj.tt = LUA_TBOOLEAN;
	}

	public static void setsvalue(lua_State L, lua_TValue obj, GCObject x) {
		obj.value.setgc(x);
		obj.tt = LUA_TSTRING;
		checkliveness(G(L), obj);
	}

	public static void setuvalue(lua_State L, lua_TValue obj, GCObject x) {
		obj.value.setgc(x);
		obj.tt = LUA_TUSERDATA;
		checkliveness(G(L), obj);
	}

	public static void setthvalue(lua_State L, lua_TValue obj, GCObject x) {
		obj.value.setgc(x);
		obj.tt = LUA_TTHREAD;
		checkliveness(G(L), obj);
	}

	public static void setclvalue(lua_State L, lua_TValue obj, Closure x) {
		obj.value.setgc(x);
		obj.tt = LUA_TFUNCTION;
		checkliveness(G(L), obj);
	}

	public static void sethvalue(lua_State L, lua_TValue obj, Table x) {
		obj.value.setgc(x);
		obj.tt = LUA_TTABLE;
		checkliveness(G(L), obj);
	}

	public static void setptvalue(lua_State L, lua_TValue obj, Proto x) {
		obj.value.setgc(x);
		obj.tt = LUA_TPROTO;
		checkliveness(G(L), obj);
	}

	public static void setobj(lua_State L, lua_TValue obj1, lua_TValue obj2) {
		obj1.value.Copy(obj2.value);
		obj1.tt = obj2.tt;
		checkliveness(G(L), obj1);
	}

	/*
	 * * different types of sets, according to destination
	 */

	/* from stack to (same) stack */
	// /#define setobjs2s setobj
	public static void setobjs2s(lua_State L, lua_TValue obj, lua_TValue x) {
		setobj(L, obj, x);
	}

	/*** to stack (not from same stack) */

	// /#define setobj2s setobj
	public static void setobj2s(lua_State L, lua_TValue obj, lua_TValue x) {
		setobj(L, obj, x);
	}

	// /#define setsvalue2s setsvalue
	public static void setsvalue2s(lua_State L, lua_TValue obj, TString x) {
		setsvalue(L, obj, x);
	}

	// /#define sethvalue2s sethvalue
	public static void sethvalue2s(lua_State L, lua_TValue obj, Table x) {
		sethvalue(L, obj, x);
	}

	// /#define setptvalue2s setptvalue
	public static void setptvalue2s(lua_State L, lua_TValue obj, Proto x) {
		setptvalue(L, obj, x);
	}

	/*** from table to same table */
	// /#define setobjt2t setobj
	public static void setobjt2t(lua_State L, lua_TValue obj, lua_TValue x) {
		setobj(L, obj, x);
	}

	/*** to table */
	// /#define setobj2t setobj
	public static void setobj2t(lua_State L, lua_TValue obj, lua_TValue x) {
		setobj(L, obj, x);
	}

	/*** to new object */
	// /#define setobj2n setobj
	public static void setobj2n(lua_State L, lua_TValue obj, lua_TValue x) {
		setobj(L, obj, x);
	}

	// /#define setsvalue2n setsvalue
	public static void setsvalue2n(lua_State L, lua_TValue obj, TString x) {
		setsvalue(L, obj, x);
	}

	public static void setttype(lua_TValue obj, int tt) {
		obj.tt = tt;
	}

	public static boolean iscollectable(lua_TValue o) {
		return (ttype(o) >= LUA_TSTRING);
	}

	// typedef lua_TValue *lua_TValue; /* index to stack elements */

	/*
	 * * String headers for string table
	 */
	public static class TString_tsv extends GCObject {
		public short reserved;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public uint hash;
		public Long hash;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public uint len;
		public int len;

		public void SetReserved(short val) {
			reserved = val;
		}

		public void SetLen(int val) {
			len = val;
		}

		public void SetHash(Long val) {
			hash = val;
		}
	}

	public static class TString extends TString_tsv {
		// public L_Umaxalign dummy; /* ensures maximum alignment for strings */
		public final TString_tsv gettsv() {
			return this;
		}

		public TString() {
		}

		public TString(CharPtr str) {
			this.str = str;
		}

		public TString(char[] str) {
			this.str = new CharPtr(str);
		}

		public TString(String str) {
			this.str = new CharPtr(str);
		}

		public CharPtr str;

		@Override
		public String toString() // for debugging
		{
			return str.toString();
		}
	}

	public static CharPtr getstr(TString ts) {
		return ts.str;
	}

	public static CharPtr svalue(lua_TValue o) {
		return getstr(rawtsvalue(o));
	}

	public static class Udata_uv extends GCObject {
		public Table metatable;
		public Table env;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public uint len;
		public int len;
	}

	public static class Udata extends Udata_uv {
		public Udata() {
			this.uv = this;
		}

		// FUCK WARNING: There is no Java equivalent to C#'s shadowing via the
		// 'new' keyword:
		// ORIGINAL LINE: public new Udata_uv uv;
		public Udata_uv uv;

		// public L_Umaxalign dummy; /* ensures maximum alignment for `local'
		// udata */

		// in the original C code this was allocated alongside the structure
		// memory. it would probably
		// be possible to still do that by allocating memory and pinning it
		// down, but we can do the
		// same thing just as easily by allocating a seperate byte array for it
		// instead.
		public Object user_data;
	}

	/*
	 * * Function Prototypes
	 */
	public static class Proto extends GCObject {

		public Proto[] protos = null;
		public int index = 0;

		public final Proto getItem(int offset) {
			return this.protos[this.index + offset];
		}

		public lua_TValue[] k; // constants used by the function
		public Long[] code;
		// FUCK WARNING: There is no Java equivalent to C#'s shadowing via the
		// 'new' keyword:
		// ORIGINAL LINE: public new Proto[] p;
		public Proto[] p; // functions defined inside the function
		public Integer[] lineinfo; // map from opcodes to source lines
		public LocVar[] locvars; // information about local variables
		public TString[] upvalues; // upvalue names
		public TString source;
		public int sizeupvalues;
		public int sizek; // size of `k'
		public int sizecode;
		public int sizelineinfo;
		public int sizep; // size of `p'
		public int sizelocvars;
		public int linedefined;
		public int lastlinedefined;
		public GCObject gclist;
		public byte nups; // number of upvalues
		public short numparams;
		public byte is_vararg;
		public short maxstacksize;
	}

	/* masks for new-style vararg */
	public static final int VARARG_HASARG = 1;
	public static final int VARARG_ISVARARG = 2;
	public static final int VARARG_NEEDSARG = 4;

	public static class LocVar {
		public TString varname;
		public int startpc; // first point where variable is active
		public int endpc; // first point where variable is dead
	}

	/*
	 * * Upvalues
	 */

	public static class UpVal extends GCObject {
		public lua_TValue v; // points to stack or to its own value

		public static class _u {
			public lua_TValue value = new lua_TValue(); // the value (when
														// closed)

			public static class _l // double linked list (when open)
			{
				public UpVal prev;
				public UpVal next;
			}

			public _l l = new _l();
		}

		// FUCK WARNING: There is no Java equivalent to C#'s shadowing via the
		// 'new' keyword:
		// ORIGINAL LINE: public new _u u = new _u();
		public _u u = new _u();
	}

	/*
	 * * Closures
	 */

	public static class ClosureHeader extends GCObject {
		public byte isC;
		public short nupvalues;
		public GCObject gclist;
		public Table env;
	}

	public static class ClosureType {

		private ClosureHeader header;

		// FUCK TODO TASK: The following operator overload is not converted by
		// Fuck:
		/*
		 * public static implicit operator ClosureHeader(ClosureType ctype) {
		 * return ctype.header; }
		 */
		public ClosureType(ClosureHeader header) {
			this.header = header;
		}

		public final byte getisC() {
			return header.isC;
		}

		public final void setisC(byte value) {
			header.isC = value;
		}

		public final short getnupvalues() {
			return header.nupvalues;
		}

		public final void setnupvalues(short value) {
			header.nupvalues = value;
		}

		public final GCObject getgclist() {
			return header.gclist;
		}

		public final void setgclist(GCObject value) {
			header.gclist = value;
		}

		public final Table getenv() {
			return header.env;
		}

		public final void setenv(Table value) {
			header.env = value;
		}
	}

	public static class CClosure extends ClosureType {
		public CClosure(ClosureHeader header) {
			super(header);
		}

		public IDelegate f;// public lua_CFunction f;
		public lua_TValue[] upvalue;
	}

	public static class LClosure extends ClosureType {
		public LClosure(ClosureHeader header) {
			super(header);
		}

		public Proto p;
		public UpVal[] upvals;
	}

	public static class Closure extends ClosureHeader {
		public Closure() {
			c = new CClosure(this);
			l = new LClosure(this);
		}

		public CClosure c;
		public LClosure l;
	}

	public static boolean iscfunction(lua_TValue o) {
		return ((ttype(o) == LUA_TFUNCTION) && (clvalue(o).c.getisC() != 0));
	}

	public static boolean isLfunction(lua_TValue o) {
		return ((ttype(o) == LUA_TFUNCTION) && (clvalue(o).c.getisC() == 0));
	}

	/*
	 * * Tables
	 */

	public static class TKey_nk extends lua_TValue {
		public TKey_nk() {
		}

		public TKey_nk(Value value, int tt, Node next) {
			super(value, tt);
			this.next = next;
		}

		public Node next; // for chaining
	}

	public static class TKey {
		public TKey() {
			this.nk = new TKey_nk();
		}

		public TKey(TKey copy) {
			this.nk = new TKey_nk(copy.nk.value, copy.nk.tt, copy.nk.next);
		}

		public TKey(Value value, int tt, Node next) {
			this.nk = new TKey_nk(value, tt, next);
		}

		public TKey_nk nk /*= new TKey_nk()*/;

		public final lua_TValue gettvk() {
			return this.nk;
		}
	}

	public static class Node implements ArrayElement {
		private Node[] values = null;
		private int index = -1;

		public final void set_index(int index) {
			this.index = index;
		}

		public final void set_array(Object array) {
			this.values = (Node[]) array;
			assert this.values != null;
		}

		public Node() {
			this.i_val = new lua_TValue();
			this.i_key = new TKey();
		}

		public Node(Node copy) {
			this.values = copy.values;
			this.index = copy.index;
			this.i_val = new lua_TValue(copy.i_val);
			this.i_key = new TKey(copy.i_key);
		}

		public Node(lua_TValue i_val, TKey i_key) {
			this.values = new Node[] { this };
			this.index = 0;
			this.i_val = i_val;
			this.i_key = i_key;
		}

		public lua_TValue i_val;
		public TKey i_key;

		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public Node getItem(uint offset)
		/*
		 * public final Node getItem(int offset) { return this.values[this.index
		 * + (int)offset]; }
		 */

		public final Node getItem(int offset) {
			return this.values[this.index + offset];
		}

		public static int OpSubtraction(Node n1, Node n2) {
			assert n1.values == n2.values;
			return n1.index - n2.index;
		}

		public static Node inc(RefObject<Node> node) {
			node.argvalue = node.argvalue.getItem(1);
			return node.argvalue.getItem(-1);
		}

		public static Node dec(RefObject<Node> node) {
			node.argvalue = node.argvalue.getItem(-1);
			return node.argvalue.getItem(1);
		}

		public static boolean OpGreaterThan(Node n1, Node n2) {
			assert n1.values == n2.values;
			return n1.index > n2.index;
		}

		public static boolean OpGreaterThanOrEqual(Node n1, Node n2) {
			assert n1.values == n2.values;
			return n1.index >= n2.index;
		}

		public static boolean OpLessThan(Node n1, Node n2) {
			assert n1.values == n2.values;
			return n1.index < n2.index;
		}

		public static boolean OpLessThanOrEqual(Node n1, Node n2) {
			assert n1.values == n2.values;
			return n1.index <= n2.index;
		}

		public static boolean OpEquality(Node n1, Node n2) {
			Object o1 = (Node) ((n1 instanceof Node) ? n1 : null);
			Object o2 = (Node) ((n2 instanceof Node) ? n2 : null);
			if ((o1 == null) && (o2 == null)) {
				return true;
			}
			if (o1 == null) {
				return false;
			}
			if (o2 == null) {
				return false;
			}
			if (n1.values != n2.values) {
				return false;
			}
			return n1.index == n2.index;
		}

		public static boolean OpInequality(Node n1, Node n2) {
			return !(n1 == n2);
		}

		@Override
		public boolean equals(Object o) {
			return this == (Node) o;
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	public static class Table extends GCObject {
		public short flags; // 1<<p means tagmethod(p) is not present
		public short lsizenode; // log2 of size of `node' array
		public Table metatable;
		public lua_TValue[] array; // array part
		public Node[] node;
		public int lastfree; // any free position is before this position
		public GCObject gclist;
		public int sizearray; // size of `array' array
	}

	/*
	 * * `module' operation for hashing (size is always a power of 2)
	 */
	// /#define lmod(s,size) \
	// (check_exp((size&(size-1))==0, (cast(int, (s) & ((size)-1)))))

	public static int twoto(int x) {
		return 1 << x;
	}

	public static int sizenode(Table t) {
		return twoto(t.lsizenode);
	}

	public static lua_TValue luaO_nilobject_ = new lua_TValue(new Value(),
			LUA_TNIL);
	public static lua_TValue luaO_nilobject = luaO_nilobject_;

	public static int ceillog2(int x) {
		return luaO_log2((int) (x - 1)) + 1;
	}

	/*
	 * * converts an integer to a "floating point byte", represented as*
	 * (eeeeexxx), where the real value is (1xxx) * 2^(eeeee - 1) if* eeeee != 0
	 * and (xxx) otherwise.
	 */
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int luaO_int2fb (uint x)
	public static int luaO_int2fb(int x) {
		int e = 0; // expoent
		while (x >= 16) {
			x = (x + 1) >> 1;
			e++;
		}
		if (x < 8) {
			return (int) x;
		} else {
			return ((e + 1) << 3) | (cast_int(x) - 8);
		}
	}

	/* converts back */
	public static int luaO_fb2int(int x) {
		int e = (x >> 3) & 31;
		if (e == 0) {
			return x;
		} else {
			return ((x & 7) + 8) << (e - 1);
		}
	}

	private final static byte[] log_2 = { 0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4,
			4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
			6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
			7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
			8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
			8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
			8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
			8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
			8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
			8, 8 };

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int luaO_log2 (uint x)
	public static int luaO_log2(int x) {
		int l = -1;
		while (x >= 256) {
			l += 8;
			x >>= 8;
		}
		return l + log_2[x];

	}

	public static int luaO_rawequalObj(lua_TValue t1, lua_TValue t2) {
		if (ttype(t1) != ttype(t2)) {
			return 0;
		} else {
			switch (ttype(t1)) {
			case LUA_TNIL:
				return 1;
			case LUA_TNUMBER:
				return luai_numeq(nvalue(t1), nvalue(t2)) ? 1 : 0;
			case LUA_TBOOLEAN:
				return bvalue(t1) == bvalue(t2) ? 1 : 0; // boolean true must be
															// 1....but not in
															// C# !!
			case LUA_TLIGHTUSERDATA:
				return pvalue(t1) == pvalue(t2) ? 1 : 0;
			default:
				lua_assert(iscollectable(t1));
				return gcvalue(t1) == gcvalue(t2) ? 1 : 0;
			}
		}
	}

	public static int luaO_str2d(CharPtr s, RefObject<Double> result) {
		CharPtr endptr = null;
		RefObject<CharPtr> tempRef_endptr = new RefObject<CharPtr>(endptr);
		result.argvalue = lua_str2number(s, tempRef_endptr);
		endptr = tempRef_endptr.argvalue;
		if (endptr == s) // conversion failed
		{
			return 0;
		}
		RefObject<CharPtr> tempRef_endptr2 = new RefObject<CharPtr>(endptr);
		if (endptr.getItem(0) == 'x' || endptr.getItem(0) == 'X') // maybe an
																	// hexadecimal
																	// constant?
		{
			result.argvalue = cast_num(strtoul(s, tempRef_endptr2, 16));
		}
		endptr = tempRef_endptr2.argvalue;
		if (endptr.getItem(0) == '\0') // most common case
		{
			return 1;
		}
		while (isspace(endptr.getItem(0))) {
			endptr = endptr.next();
		}
		if (endptr.getItem(0) != '\0') // invalid trailing characters?
		{
			return 0;
		}
		return 1;
	}

	public static void pushstr(lua_State L, CharPtr str) {
		setsvalue2s(L, L.top, luaS_new(L, str));
		incr_top(L);
	}

	public static void pushstr(lua_State L, String str) {
		setsvalue2s(L, L.top, luaS_new(L, str));
		incr_top(L);
	}

	/* this function handles only `%d', `%c', %f, %p, and `%s' formats */
	public static CharPtr luaO_pushvfstring(lua_State L, CharPtr fmt,
			Object... argp) {
		int parm_index = 0;
		int n = 1;
		pushstr(L, "");
		for (;;) {
			CharPtr e = strchr(fmt, '%');
			if (e == null) {
				break;
			}
			setsvalue2s(L, L.top,
					luaS_newlstr(L, fmt, (int) CharPtr.OpSubtraction(e, fmt)));// setsvalue2s(L,
																				// L.top,
																				// luaS_newlstr(L,
																				// fmt,
																				// (int)(e-fmt)));
			incr_top(L);
			switch (e.getItem(1)) {
			case 's': {
				Object o = argp[parm_index++];
				CharPtr s = (CharPtr) ((o instanceof CharPtr) ? o : null);
				if (s == null) {
					s = new CharPtr((String) o);
				}
				if (s == null) {
					s = new CharPtr("(null)");
				}
				pushstr(L, s);
				break;
			}
			case 'c': {
				CharPtr buff = new CharPtr(new char[2]);
				buff.setItem(0,
						(char) ((Integer) argp[parm_index++]).intValue());
				buff.setItem(1, '\0');
				pushstr(L, buff);
				break;
			}
			case 'd': {
				setnvalue(L.top, ((Integer) argp[parm_index++]).intValue());
				incr_top(L);
				break;
			}
			case 'f': {
				setnvalue(L.top, (Double) argp[parm_index++]);
				incr_top(L);
				break;
			}
			case 'p': {
				// CharPtr buff = new char[4*sizeof(void *) + 8]; /* should be
				// enough space for a `%p' */
				CharPtr buff = new CharPtr(new char[32]);
				sprintf(buff, "0x%08x", argp[parm_index++].hashCode());
				pushstr(L, buff);
				break;
			}
			case '%': {
				pushstr(L, "%");
				break;
			}
			default: {
				CharPtr buff = new CharPtr(new char[3]);
				buff.setItem(0, '%');
				buff.setItem(1, e.getItem(1));
				buff.setItem(2, '\0');
				pushstr(L, buff);
				break;
			}
			}
			n += 2;
			fmt = CharPtr.OpAddition(e, 2);// fmt = e+2;
		}
		pushstr(L, fmt);
		luaV_concat(L, n + 1,
				cast_int(Lua.lua_TValue.OpSubtraction(L.top, L.base_)) - 1);
		L.top = lua_TValue.OpSubtraction(L.top, n);// L.top -= n;
		return svalue(Lua.lua_TValue.OpSubtraction(L.top, 1));
	}

	public static CharPtr luaO_pushfstring(lua_State L, CharPtr fmt,
			Object... args) {
		return luaO_pushvfstring(L, fmt, args);
	}

	public static CharPtr luaO_pushfstring(lua_State L, String fmt,
			Object... args) {
		return luaO_pushvfstring(L, new CharPtr(fmt), args);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static void luaO_chunkid (CharPtr out_, CharPtr
	// source, uint bufflen)
	public static void luaO_chunkid(CharPtr out_, CharPtr source, int bufflen) {
		// out_ = "";
		if (source.getItem(0) == '=') {
			strncpy(out_, CharPtr.OpAddition(source, 1), (int) bufflen);// strncpy(out_,
																		// source+1,
																		// (int)bufflen);
																		// //
																		// remove
																		// first
																		// char
			out_.setItem(bufflen - 1, '\0'); // ensures null termination
		} else // out = "source", or "...source"
		{
			if (source.getItem(0) == '@') {
				// FUCK WARNING: Unsigned integer types have no direct
				// equivalent in Java:
				// ORIGINAL LINE: uint l;
				int l;
				source = source.next(); // skip the `@'
				bufflen -= (int) ((new String(" '...' ")).length() + 1);
				l = (int) strlen(source);
				strcpy(out_, "");
				if (l > bufflen) {
					source = CharPtr.OpAddition(source, (l - bufflen));// source
																		// +=
																		// (l-bufflen);
																		// //
																		// get
																		// last
																		// part
																		// of
																		// file
																		// name
					strcat(out_, "...");
				}
				strcat(out_, source);
			} else // out = [string "string"]
			{
				// FUCK WARNING: Unsigned integer types have no direct
				// equivalent in Java:
				// ORIGINAL LINE: uint len = strcspn(source, "\n\r");
				int len = strcspn(source, "\n\r"); // stop at first newline
				bufflen -= (int) ((new String(" [string \"...\"] ")).length() + 1);
				if (len > bufflen) {
					len = bufflen;
				}
				strcpy(out_, "[string \"");
				if (source.getItem(len) != '\0') // must truncate?
				{
					strncat(out_, source, (int) len);
					strcat(out_, "...");
				} else {
					strcat(out_, source);
				}
				strcat(out_, "\"]");
			}
		}
	}

	/*
	 * ==========================================================================
	 * = We assume that instructions are unsigned numbers. All instructions have
	 * an opcode in the first 6 bits. Instructions can have the following
	 * fields: `A' : 8 bits `B' : 9 bits `C' : 9 bits `Bx' : 18 bits (`B' and
	 * `C' together) `sBx' : signed Bx
	 * 
	 * A signed argument is represented in excess K; that is, the number value
	 * is the unsigned value minus K. K is exactly the maximum value for that
	 * argument (so that -max is represented by 0, and +max is represented by
	 * 2*max), which is half the maximum for the corresponding unsigned
	 * argument.
	 * ================================================================
	 * ===========
	 */

	public enum OpMode // basic int format
	{
		iABC, iABx, iAsBx;

		public int getValue() {
			return this.ordinal();
		}

		public static OpMode forValue(int value) {
			return values()[value];
		}
	}

	/*
	 * * size and position of opcode arguments.
	 */
	public static final int SIZE_C = 9;
	public static final int SIZE_B = 9;
	public static final int SIZE_Bx = (SIZE_C + SIZE_B);
	public static final int SIZE_A = 8;

	public static final int SIZE_OP = 6;

	public static final int POS_OP = 0;
	public static final int POS_A = (POS_OP + SIZE_OP);
	public static final int POS_C = (POS_A + SIZE_A);
	public static final int POS_B = (POS_C + SIZE_C);
	public static final int POS_Bx = POS_C;

	/*
	 * * limits for opcode arguments.* we use (signed) int to manipulate most
	 * arguments,* so they must fit in LUAI_BITSINT-1 bits (-1 for sign)
	 */
	// /#if SIZE_Bx < LUAI_BITSINT-1
	public static final int MAXARG_Bx = ((1 << SIZE_Bx) - 1);
	public static final int MAXARG_sBx = (MAXARG_Bx >> 1); // `sBx' is signed
	// /#else
	// public const int MAXARG_Bx = System.Int32.MaxValue;
	// public const int MAXARG_sBx = System.Int32.MaxValue;
	// /#endif

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public const uint MAXARG_A = (uint)((1 << (int)SIZE_A)
	// -1);
	public static final int MAXARG_A = (int) ((1 << (int) SIZE_A) - 1);
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public const uint MAXARG_B = (uint)((1 << (int)SIZE_B)
	// -1);
	public static final int MAXARG_B = (int) ((1 << (int) SIZE_B) - 1);
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public const uint MAXARG_C = (uint)((1 << (int)SIZE_C)
	// -1);
	public static final int MAXARG_C = (int) ((1 << (int) SIZE_C) - 1);

	/* creates a mask with `n' 1 bits at position `p' */
	// public static int MASK1(int n, int p) { return ((~((~(int)0) << n)) <<
	// p); }
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: internal static uint MASK1(int n, int p)
	public static int MASK1(int n, int p) {
		return (int) ((~((~0) << n)) << p);
	}

	/* creates a mask with `n' 0 bits at position `p' */
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: internal static uint MASK0(int n, int p)
	public static int MASK0(int n, int p) {
		return (int) (~MASK1(n, p));
	}

	/*
	 * * the following macros help to manipulate instructions
	 */

	public static OpCode GET_OPCODE(long i) {
		return OpCode.forValue((int) ((i >> POS_OP) & MASK1(SIZE_OP, 0)));// (OpCode)((i
																	// >>
																	// POS_OP) &
																	// MASK1(SIZE_OP,
																	// 0));
	}

	public static OpCode GET_OPCODE(InstructionPtr i) {
		return GET_OPCODE(i.getItem(0));
	}

	public static void SET_OPCODE(RefObject<Integer> i, int o) {
		i.argvalue = (int) (i.argvalue & MASK0(SIZE_OP, POS_OP))
				| ((o << POS_OP) & MASK1(SIZE_OP, POS_OP));
	}

	public static void SET_OPCODE(RefObject<Long> i, OpCode opcode) {
		i.argvalue = (i.argvalue & MASK0(SIZE_OP, POS_OP))
				| ((opcode.getValue() << POS_OP) & MASK1(SIZE_OP, POS_OP));
	}

	public static void SET_OPCODE(InstructionPtr i, OpCode opcode) {
		RefObject<Long> tempRef_Object = new RefObject<Long>(
				i.codes[i.pc]);
		SET_OPCODE(tempRef_Object, opcode);
		i.codes[i.pc] = tempRef_Object.argvalue;
	}

	public static int GETARG_A(long i) {
		return (int) ((i >> POS_A) & MASK1(SIZE_A, 0));
	}

	public static int GETARG_A(InstructionPtr i) {
		return GETARG_A(i.getItem(0));
	}

	public static void SETARG_A(InstructionPtr i, int u) {
		i.setItem(
				0,
				((i.getItem(0) & MASK0(SIZE_A, POS_A)) | ((u << POS_A) & MASK1(
						SIZE_A, POS_A))));
	}

	public static int GETARG_B(long i) {
		return (int) ((i >> POS_B) & MASK1(SIZE_B, 0));
	}

	public static int GETARG_B(InstructionPtr i) {
		return GETARG_B(i.getItem(0));
	}

	public static void SETARG_B(InstructionPtr i, int b) {
		long bVal = b;
		for(int j = 0; j < POS_B; j++)
			bVal *= 2;
		i.setItem(0,
				((i.getItem(0) & MASK0(SIZE_B, POS_B)) | ((bVal) & MASK1(
						SIZE_B, POS_B))));
		/*i.setItem(
				0,
				(int) ((i.getItem(0) & MASK0(SIZE_B, POS_B)) | ((b << POS_B) & MASK1(
						SIZE_B, POS_B))));*/
	}

	public static int GETARG_C(long i) {
		return (int) ((i >> POS_C) & MASK1(SIZE_C, 0));
	}

	public static int GETARG_C(InstructionPtr i) {
		return GETARG_C(i.getItem(0));
	}

	public static void SETARG_C(InstructionPtr i, int b) {
		i.setItem(
				0,
				((i.getItem(0) & MASK0(SIZE_C, POS_C)) | ((b << POS_C) & MASK1(
						SIZE_C, POS_C))));
	}

	public static int GETARG_Bx(long i) {
		return (int) ((i >> POS_Bx) & MASK1(SIZE_Bx, 0));
	}

	public static int GETARG_Bx(InstructionPtr i) {
		return GETARG_Bx(i.getItem(0));
	}

	public static void SETARG_Bx(InstructionPtr i, int b) {
		i.setItem(
				0,
				(int) ((i.getItem(0) & MASK0(SIZE_Bx, POS_Bx)) | ((b << POS_Bx) & MASK1(
						SIZE_Bx, POS_Bx))));
	}

	public static int GETARG_sBx(long i) {
		return (GETARG_Bx(i) - MAXARG_sBx);
	}

	public static int GETARG_sBx(InstructionPtr i) {
		return GETARG_sBx(i.getItem(0));
	}

	public static void SETARG_sBx(InstructionPtr i, int b) {
		SETARG_Bx(i, b + MAXARG_sBx);
	}

	public static long CREATE_ABC(OpCode o, int a, int b, int c) {
		long bVal = b;
		for(int i = 0; i < POS_B; i++)
			bVal *= 2;
		return ((long)(o.getValue() << POS_OP)) | ((long)(a << POS_A)) | bVal | ((long)(c << POS_C));
		//return (int) ((o.getValue() << POS_OP) | (a << POS_A) | (b << POS_B) | (c << POS_C));
	}

	public static int CREATE_ABx(OpCode o, int a, int bc) {
		return (int) ((o.getValue() << POS_OP) | (a << POS_A) | (bc << POS_Bx));
	}

	/*
	 * * Macros to operate RK indices
	 */

	/* this bit 1 means constant (0 means register) */
	public final static int BITRK = (1 << (SIZE_B - 1));

	/* test whether value is a constant */
	public static int ISK(int x) {
		return x & BITRK;
	}

	/* gets the index of the constant */
	public static int INDEXK(int r) {
		return r & (~BITRK);
	}

	public static final int MAXINDEXRK = BITRK - 1;

	/* code a constant index as a RK value */
	public static int RKASK(int x) {
		return x | BITRK;
	}

	/*
	 * * invalid register that fits in 8 bits
	 */
	public static final int NO_REG = (int) MAXARG_A;

	/*
	 * * R(x) - register* Kst(x) - constant (in constant table)* RK(x) == if
	 * ISK(x) then Kst(INDEXK(x)) else R(x)
	 */

	/*
	 * * grep "ORDER OP" if you change these enums
	 */

	public enum OpCode {
		/*----------------------------------------------------------------------
		name		args	description
		------------------------------------------------------------------------*/
		OP_MOVE(0), // A B R(A) := R(B)
		OP_LOADK(1), // A Bx R(A) := Kst(Bx)
		OP_LOADBOOL(2), // A B C R(A) := (Bool)B; if (C) pc++
		OP_LOADNIL(3), // A B R(A) :=... := R(B) := nil
		OP_GETUPVAL(4), // A B R(A) := UpValue[B]

		OP_GETGLOBAL(5), // A Bx R(A) := Gbl[Kst(Bx)]
		OP_GETTABLE(6), // A B C R(A) := R(B)[RK(C)]

		OP_SETGLOBAL(7), // A Bx Gbl[Kst(Bx)] := R(A)
		OP_SETUPVAL(8), // A B UpValue[B] := R(A)
		OP_SETTABLE(9), // A B C R(A)[RK(B)] := RK(C)

		OP_NEWTABLE(10), // A B C R(A) := {} (size = B,C)

		OP_SELF(11), // A B C R(A+1) := R(B); R(A) := R(B)[RK(C)]

		OP_ADD(12), // A B C R(A) := RK(B) + RK(C)
		OP_SUB(13), // A B C R(A) := RK(B) - RK(C)
		OP_MUL(14), // A B C R(A) := RK(B) * RK(C)
		OP_DIV(15), // A B C R(A) := RK(B) / RK(C)
		OP_MOD(16), // A B C R(A) := RK(B) % RK(C)
		OP_POW(17), // A B C R(A) := RK(B) ^ RK(C)
		OP_UNM(18), // A B R(A) := -R(B)
		OP_NOT(19), // A B R(A) := not R(B)
		OP_LEN(20), // A B R(A) := length of R(B)

		OP_CONCAT(21), // A B C R(A) := R(B).......R(C)

		OP_JMP(22), // sBx pc+=sBx

		OP_EQ(23), // A B C if ((RK(B) == RK(C)) ~= A) then pc++
		OP_LT(24), // A B C if ((RK(B) < RK(C)) ~= A) then pc++
		OP_LE(25), // A B C if ((RK(B) <= RK(C)) ~= A) then pc++

		OP_TEST(26), // A C if not (R(A) <=> C) then pc++
		OP_TESTSET(27), // A B C if (R(B) <=> C) then R(A) := R(B) else pc++

		OP_CALL(28), // A B C R(A),... ,R(A+C-2) := R(A)(R(A+1),... ,R(A+B-1))
		OP_TAILCALL(29), // A B C return R(A)(R(A+1),... ,R(A+B-1))
		OP_RETURN(30), // A B return R(A),... ,R(A+B-2) (see note)

		OP_FORLOOP(31), /*
						 * A sBx R(A)+=R(A+2); if R(A) <?= R(A+1) then {
						 * pc+=sBx; R(A+3)=R(A) }
						 */
		OP_FORPREP(32), // A sBx R(A)-=R(A+2); pc+=sBx

		OP_TFORLOOP(33), /*
						 * A C R(A+3),... ,R(A+2+C) := R(A)(R(A+1), R(A+2)); if
						 * R(A+3) ~= nil then R(A+2)=R(A+3) else pc++
						 */
		OP_SETLIST(34), // A B C R(A)[(C-1)*FPF+i] := R(A+i), 1 <= i <= B

		OP_CLOSE(35), // A close all variables in the stack up to (>=) R(A)
		OP_CLOSURE(36), // A Bx R(A) := closure(KPROTO[Bx], R(A),... ,R(A+n))

		OP_VARARG(37); // A B R(A), R(A+1),..., R(A+B-1) = vararg

		private int intValue;
		public static java.util.HashMap<Integer, OpCode> mappings;

		private synchronized static java.util.HashMap<Integer, OpCode> getMappings() {
			if (mappings == null) {
				mappings = new java.util.HashMap<Integer, OpCode>();
			}
			return mappings;
		}

		private OpCode(int value) {
			intValue = value;
			OpCode.getMappings().put(value, this);
		}

		public int getValue() {
			return intValue;
		}

		public static OpCode forValue(int value) {
			return getMappings().get(value);
		}
	}

	public static final int NUM_OPCODES = OpCode.OP_VARARG.getValue();

	/*
	 * ==========================================================================
	 * = Notes: (*) In OP_CALL, if (B == 0) then B = top. C is the number of
	 * returns - 1, and can be 0: OP_CALL then sets `top' to last_result+1, so
	 * next open int (OP_CALL, OP_RETURN, OP_SETLIST) may use `top'.
	 * 
	 * (*) In OP_VARARG, if (B == 0) then use actual number of varargs and set
	 * top (like in OP_CALL with C == 0).
	 * 
	 * (*) In OP_RETURN, if (B == 0) then return up to `top'
	 * 
	 * (*) In OP_SETLIST, if (B == 0) then B = `top'; if (C == 0) then next
	 * `int' is real C
	 * 
	 * (*) For comparisons, A specifies what condition the test should accept
	 * (true or false).
	 * 
	 * (*) All `skips' (pc++) assume that next int is a jump
	 * ====================
	 * =======================================================
	 */

	/*
	 * * masks for int properties. The format is:* bits 0-1: op mode* bits 2-3:
	 * C arg mode* bits 4-5: B arg mode* bit 6: int set register A* bit 7:
	 * operator is a test
	 */

	public enum OpArgMask {
		OpArgN, // argument is not used
		OpArgU, // argument is used
		OpArgR, // argument is a register or a jump offset
		OpArgK; // argument is a constant or register/constant

		public int getValue() {
			return this.ordinal();
		}

		public static OpArgMask forValue(int value) {
			return values()[value];
		}
	}

	public static OpMode getOpMode(OpCode m) {
		return OpMode.forValue((luaP_opmodes[m.getValue()] & 3));// (OpMode)(luaP_opmodes[m.getValue()]
																	// & 3);
	}

	public static OpArgMask getBMode(OpCode m) {
		return OpArgMask.forValue(((luaP_opmodes[m.getValue()] >> 4) & 3));// (OpArgMask)((luaP_opmodes[m.getValue()]
																			// >>
																			// 4)
																			// &
																			// 3);
	}

	public static OpArgMask getCMode(OpCode m) {
		return OpArgMask.forValue((luaP_opmodes[m.getValue()] >> 2) & 3);// (OpArgMask)((luaP_opmodes[m.getValue()]
																			// >>
																			// 2)
																			// &
																			// 3);
	}

	public static int testAMode(OpCode m) {
		return luaP_opmodes[m.getValue()] & (1 << 6);
	}

	public static int testTMode(OpCode m) {
		return luaP_opmodes[m.getValue()] & (1 << 7);
	}

	/* number of list items to accumulate before a SETLIST int */
	public static final int LFIELDS_PER_FLUSH = 50;

	/* ORDER OP */

	private final static CharPtr[] luaP_opnames = { new CharPtr("MOVE"),
			new CharPtr("LOADK"), new CharPtr("LOADBOOL"),
			new CharPtr("LOADNIL"), new CharPtr("GETUPVAL"),
			new CharPtr("GETGLOBAL"), new CharPtr("GETTABLE"),
			new CharPtr("SETGLOBAL"), new CharPtr("SETUPVAL"),
			new CharPtr("SETTABLE"), new CharPtr("NEWTABLE"),
			new CharPtr("SELF"), new CharPtr("ADD"), new CharPtr("SUB"),
			new CharPtr("MUL"), new CharPtr("DIV"), new CharPtr("MOD"),
			new CharPtr("POW"), new CharPtr("UNM"), new CharPtr("NOT"),
			new CharPtr("LEN"), new CharPtr("CONCAT"), new CharPtr("JMP"),
			new CharPtr("EQ"), new CharPtr("LT"), new CharPtr("LE"),
			new CharPtr("TEST"), new CharPtr("TESTSET"), new CharPtr("CALL"),
			new CharPtr("TAILCALL"), new CharPtr("RETURN"),
			new CharPtr("FORLOOP"), new CharPtr("FORPREP"),
			new CharPtr("TFORLOOP"), new CharPtr("SETLIST"),
			new CharPtr("CLOSE"), new CharPtr("CLOSURE"), new CharPtr("VARARG") };

	public static byte opmode(int tP, int aP, OpArgMask b, OpArgMask c, OpMode m) {
		byte t = (byte) tP;
		byte a = (byte) aP;
		return (byte) (((t) << 7) | ((a) << 6) | (((byte) b.getValue()) << 4)
				| (((byte) c.getValue()) << 2) | ((byte) m.getValue()));// return
																		// (byte)(((t)
																		// << 7)
																		// |
																		// ((a)
																		// << 6)
																		// |
																		// (((byte)b)
																		// << 4)
																		// |
																		// (((byte)c)
																		// << 2)
																		// |
																		// ((byte)m));
	}

	private final static byte[] luaP_opmodes = {
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgK, OpArgMask.OpArgN, OpMode.iABx),
			opmode(0, 1, OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgK, OpArgMask.OpArgN, OpMode.iABx),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 0, OpArgMask.OpArgK, OpArgMask.OpArgN, OpMode.iABx),
			opmode(0, 0, OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 0, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgR, OpMode.iABC),
			opmode(0, 0, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iAsBx),
			opmode(1, 0, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(1, 0, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(1, 0, OpArgMask.OpArgK, OpArgMask.OpArgK, OpMode.iABC),
			opmode(1, 1, OpArgMask.OpArgR, OpArgMask.OpArgU, OpMode.iABC),
			opmode(1, 1, OpArgMask.OpArgR, OpArgMask.OpArgU, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),
			opmode(0, 0, OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iAsBx),
			opmode(0, 1, OpArgMask.OpArgR, OpArgMask.OpArgN, OpMode.iAsBx),
			opmode(1, 0, OpArgMask.OpArgN, OpArgMask.OpArgU, OpMode.iABC),
			opmode(0, 0, OpArgMask.OpArgU, OpArgMask.OpArgU, OpMode.iABC),
			opmode(0, 0, OpArgMask.OpArgN, OpArgMask.OpArgN, OpMode.iABC),
			opmode(0, 1, OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABx),
			opmode(0, 1, OpArgMask.OpArgU, OpArgMask.OpArgN, OpMode.iABC) };

	/* T A B C mode opcode */

	public static int os_pushresult(lua_State L, int i, CharPtr filename) {
		int en = errno(); // calls to Lua API may change this value
		if (i != 0) {
			lua_pushboolean(L, 1);
			return 1;
		} else {
			lua_pushnil(L);
			lua_pushfstring(L, "%s: %s", filename, strerror(en));
			lua_pushinteger(L, en);
			return 3;
		}
	}

	public static int os_execute(lua_State L) {
		// FUCK TODO TASK: There is no preprocessor in Java:
		/*
		 * //#if XBOX luaL_error(L, "os_execute not supported on XBox360");
		 * //#else CharPtr strCmdLine = "/C regenresx " + luaL_optstring(L, 1,
		 * null); System.Diagnostics.Process proc = new
		 * System.Diagnostics.Process(); proc.EnableRaisingEvents=false;
		 * proc.StartInfo.FileName = "CMD.exe"; proc.StartInfo.Arguments =
		 * strCmdLine.toString(); proc.Start(); proc.WaitForExit();
		 * lua_pushinteger(L, proc.ExitCode); //#endif
		 */

		luaL_error(L, "os_execute not supported");
		return 1;
	}

	public static int os_remove(lua_State L) {
		CharPtr filename = luaL_checkstring(L, 1);
		int result = 1;
		try {
			(new java.io.File(filename.toString())).delete();
		} catch (java.lang.Exception e) {
			result = 0;
		}
		return os_pushresult(L, result, filename);
	}

	public static int os_rename(lua_State L) {
		CharPtr fromname = luaL_checkstring(L, 1);
		CharPtr toname = luaL_checkstring(L, 2);
		int result;
		try {
			// TODO yap bunu
			// File.Move(fromname.toString(), toname.toString());
			result = 0;
		} catch (java.lang.Exception e) {
			result = 1; // todo: this should be a proper error code
		}
		return os_pushresult(L, result, fromname);
	}

	public static int os_tmpname(lua_State L) {
		/*
		 * //FUCK TODO TASK: There is no preprocessor in Java: //#if XBOX
		 * luaL_error(L, "os_tmpname not supported on Xbox360"); //#else
		 * lua_pushstring(L, Path.GetTempFileName()); //#endif
		 */

		luaL_error(L, "os_tmpname not supported");
		return 1;
	}

	public static int os_getenv(lua_State L) {
		lua_pushstring(L, getenv(luaL_checkstring(L, 1))); // if null push nil
		return 1;
	}

	public static int os_clock(lua_State L) {
		long ticks = new java.util.Date().getTime();// /
													// TimeSpan.TicksPerMillisecond;
		lua_pushnumber(L, ((double) ticks) / (double) 1000);
		return 1;
	}

	/*
	 * * {======================================================* Time/Date
	 * operations* { year=%Y, month=%m, day=%d, hour=%H, min=%M, sec=%S,*
	 * wday=%w+1, yday=%j, isdst=? }*
	 * =======================================================
	 */

	public static void setfield(lua_State L, CharPtr key, int value) {
		lua_pushinteger(L, value);
		lua_setfield(L, -2, key);
	}

	public static void setfield(lua_State L, String key, int value) {
		lua_pushinteger(L, value);
		lua_setfield(L, -2, key);
	}

	public static void setboolfield(lua_State L, CharPtr key, int value) {
		if (value < 0) // undefined?
		{
			return; // does not set field
		}
		lua_pushboolean(L, value);
		lua_setfield(L, -2, key);
	}

	public static void setboolfield(lua_State L, String key, int value) {
		if (value < 0) // undefined?
		{
			return; // does not set field
		}
		lua_pushboolean(L, value);
		lua_setfield(L, -2, key);
	}

	public static int getboolfield(lua_State L, CharPtr key) {
		int res;
		lua_getfield(L, -1, key);
		res = lua_isnil(L, -1) ? -1 : lua_toboolean(L, -1);
		lua_pop(L, 1);
		return res;
	}

	public static int getboolfield(lua_State L, String keyS) {
		CharPtr key = new CharPtr(keyS);
		int res;
		lua_getfield(L, -1, key);
		res = lua_isnil(L, -1) ? -1 : lua_toboolean(L, -1);
		lua_pop(L, 1);
		return res;
	}

	public static int getfield(lua_State L, CharPtr key, int d) {
		int res;
		lua_getfield(L, -1, key);
		if (lua_isnumber(L, -1) != 0) {
			res = (int) lua_tointeger(L, -1);
		} else {
			if (d < 0) {
				return luaL_error(L, "field " + getLUA_QS()
						+ " missing in date table", key);
			}
			res = d;
		}
		lua_pop(L, 1);
		return res;
	}

	public static int getfield(lua_State L, String keyS, int d) {
		CharPtr key = new CharPtr(keyS);
		int res;
		lua_getfield(L, -1, key);
		if (lua_isnumber(L, -1) != 0) {
			res = (int) lua_tointeger(L, -1);
		} else {
			if (d < 0) {
				return luaL_error(L, "field " + getLUA_QS()
						+ " missing in date table", key);
			}
			res = d;
		}
		lua_pop(L, 1);
		return res;
	}

	public static int os_date(lua_State L) {
		CharPtr s = luaL_optstring(L, 1, "%c");
		java.util.Date stm = new java.util.Date(0);
		if (s.getItem(0) == '!') // UTC?
		{
			// stm = stm
			s.inc(); // skip `!'
		} else {
			stm = new java.util.Date();
		}
		if (strcmp(s, "*t") == 0) {
			lua_createtable(L, 0, 9); // 9 = number of fields
			setfield(L, "sec", DotNetToJavaDateHelper.DatePart(
					java.util.Calendar.SECOND, stm));
			setfield(L, "min", DotNetToJavaDateHelper.DatePart(
					java.util.Calendar.MINUTE, stm));
			setfield(L, "hour", DotNetToJavaDateHelper.DatePart(
					java.util.Calendar.HOUR_OF_DAY, stm));
			setfield(L, "day", DotNetToJavaDateHelper.DatePart(
					java.util.Calendar.DAY_OF_MONTH, stm));
			setfield(L, "month", DotNetToJavaDateHelper.DatePart(
					java.util.Calendar.MONTH, stm));
			setfield(L, "year", DotNetToJavaDateHelper.DatePart(
					java.util.Calendar.YEAR, stm));
			setfield(L, "wday",
					(int) Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
			setfield(L, "yday", DotNetToJavaDateHelper.DatePart(
					java.util.Calendar.DAY_OF_YEAR, stm));
			// TODO fixme
			setboolfield(L, "isdst", 1);// setboolfield(L, "isdst",
										// stm.IsDaylightSavingTime() ? 1 : 0);
		} else {
			luaL_error(L, "strftime not implemented yet"); // todo: implement
															// this - mjf
			// FUCK TODO TASK: There is no preprocessor in Java:
			// #if false
			/*
			 * CharPtr cc = new char[3]; luaL_Buffer b; cc.setItem(0, '%');
			 * cc.setItem(2, '\0'); luaL_buffinit(L, b); for (; s.getItem(0) !=
			 * 0; s.inc()) { if (s.getItem(0) != '%' || s.getItem(1) == '\0') //
			 * no conversion specifier? { luaL_addchar(b, s.getItem(0)); } else
			 * { //FUCK WARNING: Unsigned integer types have no direct
			 * equivalent in Java: //ORIGINAL LINE: uint reslen; int reslen;
			 * CharPtr buff = new char[200]; // should be big enough for any
			 * conversion result s.inc(); cc.setItem(1, s.getItem(0)); reslen =
			 * strftime(buff, buff.getLength(), cc, stm); luaL_addlstring(b,
			 * buff, reslen); } } luaL_pushresult(b);
			 */
			// #endif // #if 0
		}
		return 1;
	}

	public static int os_time(lua_State L) {
		java.util.Date t = new java.util.Date(0);
		if (lua_isnoneornil(L, 1)) // called without args?
		{
			t = new java.util.Date(); // get current time
		} else {
			luaL_checktype(L, 1, LUA_TTABLE);
			lua_settop(L, 1); // make sure table is at the top
			int sec = getfield(L, "sec", 0);
			int min = getfield(L, "min", 0);
			int hour = getfield(L, "hour", 12);
			int day = getfield(L, "day", -1);
			int month = getfield(L, "month", -1) - 1;
			int year = getfield(L, "year", -1) - 1900;
			int isdst = getboolfield(L, "isdst"); // todo: implement this - mjf
			t = new java.util.Date(year, month, day, hour, min, sec);
		}
		lua_pushnumber(L, t.getTime());
		return 1;
	}

	public static int os_difftime(lua_State L) {
		long ticks = (long) luaL_checknumber(L, 1)
				- (long) luaL_optnumber(L, 2, 0);
		lua_pushnumber(L, ticks);// /TimeSpan.TicksPerSecond);
		return 1;
	}

	/* }====================================================== */

	// locale not supported yet
	public static int os_setlocale(lua_State L) {
		/*
		 * static string[] cat = {LC_ALL, LC_COLLATE, LC_CTYPE, LC_MONETARY,
		 * LC_NUMERIC, LC_TIME}; static string[] catnames[] = {"all", "collate",
		 * "ctype", "monetary", "numeric", "time", null}; CharPtr l =
		 * luaL_optstring(L, 1, null); int op = luaL_checkoption(L, 2, "all",
		 * catnames); lua_pushstring(L, setlocale(cat[op], l));
		 */
		CharPtr l = luaL_optstring(L, 1, (CharPtr) null);
		lua_pushstring(L, "C");
		return (l.toString().equals("C")) ? 1 : 0;
	}

	public static int os_exit(lua_State L) {
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if XBOX
		luaL_error(L, "os_exit not supported on XBox360");
		// #else
		// Environment.Exit(EXIT_SUCCESS);
		// #endif
		return 0;
	}

	private final static luaL_Reg[] syslib = {
			new luaL_Reg("clock", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_clock((lua_State)arg);
				}
			}), 
			new luaL_Reg("date", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_date((lua_State)arg);
				}
			}),
			new luaL_Reg("difftime", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_difftime((lua_State)arg);
				}
			}),
			new luaL_Reg("execute", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_execute((lua_State)arg);
				}
			}),
			new luaL_Reg("exit", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_exit((lua_State)arg);
				}
			}),
			new luaL_Reg("getenv", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_getenv((lua_State)arg);
				}
			}),
			new luaL_Reg("remove", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_remove((lua_State)arg);
				}
			}),
			new luaL_Reg("rename", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_rename((lua_State)arg);
				}
			}),
			new luaL_Reg("setlocale", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_setlocale((lua_State)arg);
				}
			}),
			new luaL_Reg("time", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_time((lua_State)arg);
				}
			}),
			new luaL_Reg("tmpname", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return os_tmpname((lua_State)arg);
				}
			}), 
			new luaL_Reg((String)null, (String)null) };

	/* }====================================================== */

	public static int luaopen_os(lua_State L) {
		luaL_register(L, LUA_OSLIBNAME, syslib);
		return 1;
	}

	/*
	 * * Expression descriptor
	 */

	public enum expkind {
		VVOID(0), // no value
		VNIL(1), VTRUE(2), VFALSE(3), VK(4), // info = index of constant in `k'
		VKNUM(5), // nval = numerical value
		VLOCAL(6), // info = local register
		VUPVAL(7), // info = index of upvalue in `upvalues'
		VGLOBAL(8), // info = index of table; aux = index of global name in `k'
		VINDEXED(9), // info = table register; aux = index register (or `k')
		VJMP(10), // info = int pc
		VRELOCABLE(11), // info = int pc
		VNONRELOC(12), // info = result register
		VCALL(13), // info = int pc
		VVARARG(14); // info = int pc

		private int intValue;
		public static java.util.HashMap<Integer, expkind> mappings;

		private synchronized static java.util.HashMap<Integer, expkind> getMappings() {
			if (mappings == null) {
				mappings = new java.util.HashMap<Integer, expkind>();
			}
			return mappings;
		}

		private expkind(int value) {
			intValue = value;
			expkind.getMappings().put(value, this);
		}

		public int getValue() {
			return intValue;
		}

		public static expkind forValue(int value) {
			return getMappings().get(value);
		}
	}

	public static class expdesc {

		public final void Copy(expdesc e) {
			this.k = e.k;
			this.u.Copy(e.u);
			this.t = e.t;
			this.f = e.f;
		}

		public expkind k = expkind.forValue(0);

		public static class _u {
			public final void Copy(_u u) {
				this.s.Copy(u.s);
				this.nval = u.nval;
			}

			public static class _s {
				public final void Copy(_s s) {
					this.info = s.info;
					this.aux = s.aux;
				}

				public int info, aux;
			}

			public _s s = new _s();
			public double nval;
		}

		public _u u = new _u();

		public int t; // patch list of `exit when true'
		public int f; // patch list of `exit when false'
	}

	public static class upvaldesc {
		public short k;
		public short info;
	}

	/* state needed to generate code for a given function */
	public static class FuncState {
		public FuncState() {
			for (int i = 0; i < this.upvalues.length; i++) {
				this.upvalues[i] = new upvaldesc();
			}
		}

		public Proto f; // current function header
		public Table h; // table to find (and reuse) elements in `k'
		public FuncState prev; // enclosing function
		public LexState ls; // lexical state
		public lua_State L; // copy of the Lua state
		public BlockCnt bl; // chain of current blocks
		public int pc; // next position to code (equivalent to `ncode')
		public int lasttarget; // `pc' of last `jump target'
		public int jpc; // list of pending jumps to `pc'
		public int freereg; // first free register
		public int nk; // number of elements in `k'
		public int np; // number of elements in `p'
		public short nlocvars; // number of elements in `locvars'
		public short nactvar; // number of active local variables
		public upvaldesc[] upvalues = new upvaldesc[LUAI_MAXUPVALUES]; // upvalues
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public ushort[] actvar = new ushort[LUAI_MAXVARS];
		public short[] actvar = new short[LUAI_MAXVARS]; // declared-variable
															// stack
	}

	public static int hasmultret(expkind k) {
		return ((k) == expkind.VCALL || (k) == expkind.VVARARG) ? 1 : 0;
	}

	public static LocVar getlocvar(FuncState fs, int i) {
		return fs.f.locvars[fs.actvar[i]];
	}

	public static void luaY_checklimit(FuncState fs, int v, int l, CharPtr m) {
		if ((v) > (l)) {
			errorlimit(fs, l, m);
		}
	}

	public static void luaY_checklimit(FuncState fs, int v, int l, String mS) {
		CharPtr m = new CharPtr(mS);
		if ((v) > (l)) {
			errorlimit(fs, l, m);
		}
	}

	/*
	 * * nodes for block list (list of active blocks)
	 */
	public static class BlockCnt {
		public BlockCnt previous; // chain
		public int breaklist; // list of jumps out of this loop
		public short nactvar; // # active locals outside the breakable structure
		public byte upval; // true if some variable in the block is an upvalue
		public byte isbreakable; // true if `block' is a loop
	}

	public static void anchor_token(LexState ls) {
		if (ls.t.token == RESERVED.TK_NAME.getValue()
				|| ls.t.token == RESERVED.TK_STRING.getValue()) {
			TString ts = ls.t.seminfo.ts;
			luaX_newstring(ls, getstr(ts), ts.gettsv().len);
		}
	}

	public static void error_expected(LexState ls, int token) {
		luaX_syntaxerror(
				ls,
				luaO_pushfstring(ls.L, getLUA_QS() + " expected",
						luaX_token2str(ls, token)));
	}

	public static void errorlimit(FuncState fs, int limit, CharPtr what) {
		CharPtr msg = (fs.f.linedefined == 0) ? luaO_pushfstring(fs.L,
				"main function has more than %d %s", limit, what)
				: luaO_pushfstring(fs.L,
						"function at line %d has more than %d %s",
						fs.f.linedefined, limit, what);
		luaX_lexerror(fs.ls, msg, 0);
	}

	public static int testnext(LexState ls, int c) {
		if (ls.t.token == c) {
			luaX_next(ls);
			return 1;
		} else {
			return 0;
		}
	}

	public static void check(LexState ls, int c) {
		if (ls.t.token != c) {
			error_expected(ls, c);
		}
	}

	public static void checknext(LexState ls, int c) {
		check(ls, c);
		luaX_next(ls);
	}

	public static void check_condition(LexState ls, boolean c, CharPtr msg) {
		if (!(c)) {
			luaX_syntaxerror(ls, msg);
		}
	}

	public static void check_condition(LexState ls, boolean c, String msgS) {
		if (!(c)) {
			CharPtr msg = new CharPtr(msgS);
			luaX_syntaxerror(ls, msg);
		}
	}

	public static void check_match(LexState ls, int what, int who, int where) {
		if (testnext(ls, what) == 0) {
			if (where == ls.linenumber) {
				error_expected(ls, what);
			} else {
				luaX_syntaxerror(
						ls,
						luaO_pushfstring(ls.L, getLUA_QS()
								+ " expected (to close " + getLUA_QS()
								+ " at line %d)", luaX_token2str(ls, what),
								luaX_token2str(ls, who), where));
			}
		}
	}

	public static TString str_checkname(LexState ls) {
		TString ts;
		check(ls, RESERVED.TK_NAME.getValue());
		ts = ls.t.seminfo.ts;
		luaX_next(ls);
		return ts;
	}

	public static void init_exp(expdesc e, expkind k, int i) {
		e.f = e.t = NO_JUMP;
		e.k = k;
		e.u.s.info = i;
	}

	public static void codestring(LexState ls, expdesc e, TString s) {
		init_exp(e, expkind.VK, luaK_stringK(ls.fs, s));
	}

	public static void checkname(LexState ls, expdesc e) {
		codestring(ls, e, str_checkname(ls));
	}

	public static int registerlocalvar(LexState ls, TString varname) {
		FuncState fs = ls.fs;
		Proto f = fs.f;
		int oldsize = f.sizelocvars;
		RefObject<LocVar[]> tempRef_locvars = new RefObject<LocVar[]>(f.locvars);
		RefObject<Integer> tempRef_sizelocvars = new RefObject<Integer>(
				f.sizelocvars);
		Lua.<LocVar> luaM_growvector(ls.L, tempRef_locvars, fs.nlocvars,
				tempRef_sizelocvars, (int) SHRT_MAX, new CharPtr(
						"too many local variables"), LocVar.class);
		f.locvars = tempRef_locvars.argvalue;
		f.sizelocvars = tempRef_sizelocvars.argvalue;
		while (oldsize < f.sizelocvars) {
			f.locvars[oldsize++].varname = null;
		}
		f.locvars[fs.nlocvars].varname = varname;
		luaC_objbarrier(ls.L, f, varname);
		return fs.nlocvars++;
	}

	public static void new_localvarliteral(LexState ls, CharPtr v, int n) {
		new_localvar(ls,
				luaX_newstring(ls, "" + v, (int) (v.chars.length - 1)), n);
	}

	public static void new_localvarliteral(LexState ls, String vS, int n) {
		CharPtr v = new CharPtr(vS);
		new_localvar(ls,
				luaX_newstring(ls, "" + v, (int) (v.chars.length - 1)), n);
	}

	public static void new_localvar(LexState ls, TString name, int n) {
		FuncState fs = ls.fs;
		luaY_checklimit(fs, fs.nactvar + n + 1, LUAI_MAXVARS, "local variables");
		fs.actvar[fs.nactvar + n] = (short) registerlocalvar(ls, name);
	}

	public static void adjustlocalvars(LexState ls, int nvars) {
		FuncState fs = ls.fs;
		fs.nactvar = cast_byte(fs.nactvar + nvars);
		for (; nvars != 0; nvars--) {
			getlocvar(fs, fs.nactvar - nvars).startpc = fs.pc;
		}
	}

	public static void removevars(LexState ls, int tolevel) {
		FuncState fs = ls.fs;
		while (fs.nactvar > tolevel) {
			getlocvar(fs, --fs.nactvar).endpc = fs.pc;
		}
	}

	public static int indexupvalue(FuncState fs, TString name, expdesc v) {
		int i;
		Proto f = fs.f;
		int oldsize = f.sizeupvalues;
		for (i = 0; i < f.nups; i++) {
			if ((int) fs.upvalues[i].k == (int) v.k.getValue()
					&& fs.upvalues[i].info == v.u.s.info) {
				lua_assert(f.upvalues[i] == name);
				return i;
			}
		}
		/* new one */
		luaY_checklimit(fs, f.nups + 1, LUAI_MAXUPVALUES, "upvalues");
		RefObject<TString[]> tempRef_upvalues = new RefObject<TString[]>(
				f.upvalues);
		RefObject<Integer> tempRef_sizeupvalues = new RefObject<Integer>(
				f.sizeupvalues);
		Lua.<TString> luaM_growvector(fs.L, tempRef_upvalues, f.nups,
				tempRef_sizeupvalues, MAX_INT, new CharPtr(""), TString.class);
		f.upvalues = tempRef_upvalues.argvalue;
		f.sizeupvalues = tempRef_sizeupvalues.argvalue;
		while (oldsize < f.sizeupvalues) {
			f.upvalues[oldsize++] = null;
		}
		f.upvalues[f.nups] = name;
		luaC_objbarrier(fs.L, f, name);
		lua_assert(v.k == expkind.VLOCAL || v.k == expkind.VUPVAL);
		fs.upvalues[f.nups].k = cast_byte(v.k.getValue());
		fs.upvalues[f.nups].info = cast_byte(v.u.s.info);
		return f.nups++;
	}

	public static int searchvar(FuncState fs, TString n) {
		int i;
		for (i = fs.nactvar - 1; i >= 0; i--) {
			if (n.toString().compareTo(getlocvar(fs,i).varname.toString()) == 0) {
				return i;
			}
		}
		return -1; // not found
	}

	public static void markupval(FuncState fs, int level) {
		BlockCnt bl = fs.bl;
		while ((bl != null) && bl.nactvar > level) {
			bl = bl.previous;
		}
		if (bl != null) {
			bl.upval = 1;
		}
	}

	public static expkind singlevaraux(FuncState fs, TString n, expdesc var,
			int base_) {
		if (fs == null) // no more levels?
		{
			init_exp(var, expkind.VGLOBAL, NO_REG); // default is global
													// variable
			return expkind.VGLOBAL;
		} else {
			int v = searchvar(fs, n); // look up at current level
			if (v >= 0) {
				init_exp(var, expkind.VLOCAL, v);
				if (base_ == 0) {
					markupval(fs, v); // local will be used as an upval
				}
				return expkind.VLOCAL;
			} else // not found at current level; try upper one
			{
				if (singlevaraux(fs.prev, n, var, 0) == expkind.VGLOBAL) {
					return expkind.VGLOBAL;
				}
				var.u.s.info = indexupvalue(fs, n, var); // else was LOCAL or
															// UPVAL
				var.k = expkind.VUPVAL; // upvalue in this level
				return expkind.VUPVAL;
			}
		}
	}

	public static void singlevar(LexState ls, expdesc var) {
		TString varname = str_checkname(ls);
		FuncState fs = ls.fs;
		if (singlevaraux(fs, varname, var, 1) == expkind.VGLOBAL) {
			var.u.s.info = luaK_stringK(fs, varname); // info points to global
														// name
		}
	}

	public static void adjust_assign(LexState ls, int nvars, int nexps,
			expdesc e) {
		FuncState fs = ls.fs;
		int extra = nvars - nexps;
		if (hasmultret(e.k) != 0) {
			extra++; // includes call itself
			if (extra < 0) {
				extra = 0;
			}
			luaK_setreturns(fs, e, extra); // last exp. provides the difference
			if (extra > 1) {
				luaK_reserveregs(fs, extra - 1);
			}
		} else {
			if (e.k != expkind.VVOID) // close last expression
			{
				luaK_exp2nextreg(fs, e);
			}
			if (extra > 0) {
				int reg = fs.freereg;
				luaK_reserveregs(fs, extra);
				luaK_nil(fs, reg, extra);
			}
		}
	}

	public static void enterlevel(LexState ls) {
		if (++ls.L.nCcalls > LUAI_MAXCCALLS) {
			luaX_lexerror(ls, "chunk has too many syntax levels", 0);
		}
	}

	public static void leavelevel(LexState ls) {
		ls.L.nCcalls--;
	}

	public static void enterblock(FuncState fs, BlockCnt bl, byte isbreakable) {
		bl.breaklist = NO_JUMP;
		bl.isbreakable = isbreakable;
		bl.nactvar = fs.nactvar;
		bl.upval = 0;
		bl.previous = fs.bl;
		fs.bl = bl;
		lua_assert(fs.freereg == fs.nactvar);
	}

	public static void leaveblock(FuncState fs) {
		BlockCnt bl = fs.bl;
		fs.bl = bl.previous;
		removevars(fs.ls, bl.nactvar);
		if (bl.upval != 0) {
			luaK_codeABC(fs, OpCode.OP_CLOSE, bl.nactvar, 0, 0);
		}
		/* a block either controls scope or breaks (never both) */
		lua_assert((bl.isbreakable == 0) || (bl.upval == 0));
		lua_assert(bl.nactvar == fs.nactvar);
		fs.freereg = fs.nactvar; // free registers
		luaK_patchtohere(fs, bl.breaklist);
	}

	public static void pushclosure(LexState ls, FuncState func, expdesc v) {
		FuncState fs = ls.fs;
		Proto f = fs.f;
		int oldsize = f.sizep;
		int i;
		RefObject<Proto[]> tempRef_p = new RefObject<Proto[]>(f.p);
		RefObject<Integer> tempRef_sizep = new RefObject<Integer>(f.sizep);
		Lua.<Proto> luaM_growvector(ls.L, tempRef_p, fs.np, tempRef_sizep,
				MAXARG_Bx, new CharPtr("constant table overflow"), Proto.class);
		f.p = tempRef_p.argvalue;
		f.sizep = tempRef_sizep.argvalue;
		while (oldsize < f.sizep) {
			f.p[oldsize++] = null;
		}
		f.p[fs.np++] = func.f;
		luaC_objbarrier(ls.L, f, func.f);
		init_exp(v, expkind.VRELOCABLE,
				luaK_codeABx(fs, OpCode.OP_CLOSURE, 0, fs.np - 1));
		for (i = 0; i < func.f.nups; i++) {
			OpCode o = ((int) func.upvalues[i].k == expkind.VLOCAL.getValue()) ? OpCode.OP_MOVE
					: OpCode.OP_GETUPVAL;
			luaK_codeABC(fs, o, 0, func.upvalues[i].info, 0);
		}
	}

	public static void open_func(LexState ls, FuncState fs) {
		lua_State L = ls.L;
		Proto f = luaF_newproto(L);
		fs.f = f;
		fs.prev = ls.fs; // linked list of funcstates
		fs.ls = ls;
		fs.L = L;
		ls.fs = fs;
		fs.pc = 0;
		fs.lasttarget = -1;
		fs.jpc = NO_JUMP;
		fs.freereg = 0;
		fs.nk = 0;
		fs.np = 0;
		fs.nlocvars = 0;
		fs.nactvar = 0;
		fs.bl = null;
		f.source = ls.source;
		f.maxstacksize = 2; // registers 0/1 are always valid
		fs.h = luaH_new(L, 0, 0);
		/* anchor table of constants and prototype (to avoid being collected) */
		sethvalue2s(L, L.top, fs.h);
		incr_top(L);
		setptvalue2s(L, L.top, f);
		incr_top(L);
	}

	public static void close_func(LexState ls) {
		lua_State L = ls.L;
		FuncState fs = ls.fs;
		Proto f = fs.f;
		removevars(ls, 0);
		luaK_ret(fs, 0, 0); // final return
		RefObject<Long[]> tempRef_code = new RefObject<Long[]>(f.code);
		Lua.<Long> luaM_reallocvector(L, tempRef_code, f.sizecode, fs.pc,
				Long.class); // , typeof(int)
		f.code = tempRef_code.argvalue;
		f.sizecode = fs.pc;
		RefObject<Integer[]> tempRef_lineinfo = new RefObject<Integer[]>(
				f.lineinfo);
		Lua.<Integer> luaM_reallocvector(L, tempRef_lineinfo, f.sizelineinfo,
				fs.pc, Integer.class); // , typeof(int)
		f.lineinfo = tempRef_lineinfo.argvalue;
		f.sizelineinfo = fs.pc;
		RefObject<lua_TValue[]> tempRef_k = new RefObject<lua_TValue[]>(f.k);
		Lua.<lua_TValue> luaM_reallocvector(L, tempRef_k, f.sizek, fs.nk,
				lua_TValue.class); // , lua_TValue
		f.k = tempRef_k.argvalue;
		f.sizek = fs.nk;
		RefObject<Proto[]> tempRef_p = new RefObject<Proto[]>(f.p);
		Lua.<Proto> luaM_reallocvector(L, tempRef_p, f.sizep, fs.np,
				Proto.class); // , Proto
		f.p = tempRef_p.argvalue;
		f.sizep = fs.np;
		for (int i = 0; i < f.p.length; i++) {
			f.p[i].protos = f.p;
			f.p[i].index = i;
		}
		RefObject<LocVar[]> tempRef_locvars = new RefObject<LocVar[]>(f.locvars);
		Lua.<LocVar> luaM_reallocvector(L, tempRef_locvars, f.sizelocvars,
				fs.nlocvars, LocVar.class); // , LocVar
		f.locvars = tempRef_locvars.argvalue;
		f.sizelocvars = fs.nlocvars;
		RefObject<TString[]> tempRef_upvalues = new RefObject<TString[]>(
				f.upvalues);
		Lua.<TString> luaM_reallocvector(L, tempRef_upvalues, f.sizeupvalues,
				f.nups, TString.class); // , TString
		f.upvalues = tempRef_upvalues.argvalue;
		f.sizeupvalues = f.nups;
		lua_assert(luaG_checkcode(f));
		lua_assert(fs.bl == null);
		ls.fs = fs.prev;
		L.top = lua_TValue.OpSubtraction(L.top, 2);// L.top -= 2; // remove
													// table and prototype from
													// the stack
		/* last token read was anchored in defunct function; must reanchor it */
		if (fs != null) {
			anchor_token(ls);
		}
	}

	public static Proto luaY_parser(lua_State L, Zio z, Mbuffer buff,
			CharPtr name) {
		LexState lexstate = new LexState();
		FuncState funcstate = new FuncState();
		lexstate.buff = buff;
		luaX_setinput(L, lexstate, z, luaS_new(L, name));
		open_func(lexstate, funcstate);
		funcstate.f.is_vararg = VARARG_ISVARARG; // main func. is always vararg
		luaX_next(lexstate); // read first token
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if !PocketPC
		// Thread.currentThread().CurrentCulture =
		// System.Globalization.CultureInfo.InvariantCulture;
		// #endif
		chunk(lexstate);
		check(lexstate, RESERVED.TK_EOS.getValue());
		close_func(lexstate);
		lua_assert(funcstate.prev == null);
		lua_assert(funcstate.f.nups == 0);
		lua_assert(lexstate.fs == null);
		return funcstate.f;
	}

	/* ============================================================ */
	/* GRAMMAR RULES */
	/* ============================================================ */

	public static void field(LexState ls, expdesc v) {
		/* field . ['.' | ':'] NAME */
		FuncState fs = ls.fs;
		expdesc key = new expdesc();
		luaK_exp2anyreg(fs, v);
		luaX_next(ls); // skip the dot or colon
		checkname(ls, key);
		luaK_indexed(fs, v, key);
	}

	public static void yindex(LexState ls, expdesc v) {
		/* index . '[' expr ']' */
		luaX_next(ls); // skip the '['
		expr(ls, v);
		luaK_exp2val(ls.fs, v);
		checknext(ls, ']');
	}

	/*
	 * * {======================================================================
	 * * Rules for Constructors*
	 * =======================================================================
	 */

	public static class ConsControl {
		public expdesc v = new expdesc(); // last list item read
		public expdesc t; // table descriptor
		public int nh; // total number of `record' elements
		public int na; // total number of array elements
		public int tostore; // number of array elements pending to be stored
	}

	public static void recfield(LexState ls, ConsControl cc) {
		/* recfield . (NAME | `['exp1`]') = exp1 */
		FuncState fs = ls.fs;
		int reg = ls.fs.freereg;
		expdesc key = new expdesc(), val = new expdesc();
		int rkkey;
		if (ls.t.token == RESERVED.TK_NAME.getValue()) {
			luaY_checklimit(fs, cc.nh, MAX_INT, "items in a constructor");
			checkname(ls, key);
		} else // ls.t.token == '['
		{
			yindex(ls, key);
		}
		cc.nh++;
		checknext(ls, '=');
		rkkey = luaK_exp2RK(fs, key);
		expr(ls, val);
		luaK_codeABC(fs, OpCode.OP_SETTABLE, cc.t.u.s.info, rkkey,
				luaK_exp2RK(fs, val));
		fs.freereg = reg; // free registers
	}

	public static void closelistfield(FuncState fs, ConsControl cc) {
		if (cc.v.k == expkind.VVOID) // there is no list item
		{
			return;
		}
		luaK_exp2nextreg(fs, cc.v);
		cc.v.k = expkind.VVOID;
		if (cc.tostore == LFIELDS_PER_FLUSH) {
			luaK_setlist(fs, cc.t.u.s.info, cc.na, cc.tostore); // flush
			cc.tostore = 0; // no more items pending
		}
	}

	public static void lastlistfield(FuncState fs, ConsControl cc) {
		if (cc.tostore == 0) {
			return;
		}
		if (hasmultret(cc.v.k) != 0) {
			luaK_setmultret(fs, cc.v);
			luaK_setlist(fs, cc.t.u.s.info, cc.na, LUA_MULTRET);
			cc.na--; // do not count last expression (unknown number of
						// elements)
		} else {
			if (cc.v.k != expkind.VVOID) {
				luaK_exp2nextreg(fs, cc.v);
			}
			luaK_setlist(fs, cc.t.u.s.info, cc.na, cc.tostore);
		}
	}

	public static void listfield(LexState ls, ConsControl cc) {
		expr(ls, cc.v);
		luaY_checklimit(ls.fs, cc.na, MAX_INT, "items in a constructor");
		cc.na++;
		cc.tostore++;
	}

	public static void constructor(LexState ls, expdesc t) {
		/* constructor . ?? */
		FuncState fs = ls.fs;
		int line = ls.linenumber;
		int pc = luaK_codeABC(fs, OpCode.OP_NEWTABLE, 0, 0, 0);
		ConsControl cc = new ConsControl();
		cc.na = cc.nh = cc.tostore = 0;
		cc.t = t;
		init_exp(t, expkind.VRELOCABLE, pc);
		init_exp(cc.v, expkind.VVOID, 0); // no value (yet)
		luaK_exp2nextreg(ls.fs, t); // fix it at stack top (for gc)
		checknext(ls, '{');
		do {
			lua_assert(cc.v.k == expkind.VVOID || cc.tostore > 0);
			if (ls.t.token == '}') {
				break;
			}
			closelistfield(fs, cc);
			if (ls.t.token == RESERVED.TK_NAME.getValue()) {
				luaX_lookahead(ls);
				if (ls.lookahead.token != '=') // expression?
				{
					listfield(ls, cc);
				} else {
					recfield(ls, cc);
				}
			} else if (ls.t.token == '[')
				recfield(ls, cc);
			else
				listfield(ls, cc);
		} while ((testnext(ls, ',') != 0) || (testnext(ls, ';') != 0));
		check_match(ls, '}', '{', line);
		lastlistfield(fs, cc);
		SETARG_B(new InstructionPtr(fs.f.code, pc), luaO_int2fb((int) cc.na)); // set
																				// initial
																				// array
																				// size
		SETARG_C(new InstructionPtr(fs.f.code, pc), luaO_int2fb((int) cc.nh)); // set
																				// initial
																				// table
																				// size
	}

	/* }====================================================================== */

	public static void parlist(LexState ls) {
		/* parlist . [ param { `,' param } ] */
		FuncState fs = ls.fs;
		Proto f = fs.f;
		int nparams = 0;
		f.is_vararg = 0;
		if (ls.t.token != ')') // is `parlist' not empty?
		{
			do {
				if (ls.t.token == RESERVED.TK_NAME.getValue())
					new_localvar(ls, str_checkname(ls), nparams++);
				else if (ls.t.token == RESERVED.TK_DOTS.getValue()) {
					luaX_next(ls);
					// FUCK TODO TASK: There is no preprocessor in Java:
					// #if LUA_COMPAT_VARARG
					/* use `arg' as default name */
					new_localvarliteral(ls, "arg", nparams++);
					f.is_vararg = VARARG_HASARG | VARARG_NEEDSARG;
					// #endif
					f.is_vararg |= VARARG_ISVARARG;
				} else
					luaX_syntaxerror(ls, "<name> or " + LUA_QL("...")
							+ " expected");
			} while ((f.is_vararg == 0) && (testnext(ls, ',') != 0));
		}
		adjustlocalvars(ls, nparams);
		f.numparams = cast_byte(fs.nactvar - (f.is_vararg & VARARG_HASARG));
		luaK_reserveregs(fs, fs.nactvar); // reserve register for parameters
	}

	public static void body(LexState ls, expdesc e, int needself, int line) {
		/* body . `(' parlist `)' chunk END */
		FuncState new_fs = new FuncState();
		open_func(ls, new_fs);
		new_fs.f.linedefined = line;
		checknext(ls, '(');
		if (needself != 0) {
			new_localvarliteral(ls, "self", 0);
			adjustlocalvars(ls, 1);
		}
		parlist(ls);
		checknext(ls, ')');
		chunk(ls);
		new_fs.f.lastlinedefined = ls.linenumber;
		check_match(ls, RESERVED.TK_END.getValue(),
				RESERVED.TK_FUNCTION.getValue(), line);
		close_func(ls);
		pushclosure(ls, new_fs, e);
	}

	public static int explist1(LexState ls, expdesc v) {
		/* explist1 . expr { `,' expr } */
		int n = 1; // at least one expression
		expr(ls, v);
		while (testnext(ls, ',') != 0) {
			luaK_exp2nextreg(ls.fs, v);
			expr(ls, v);
			n++;
		}
		return n;
	}

	public static void funcargs(LexState ls, expdesc f) {
		FuncState fs = ls.fs;
		expdesc args = new expdesc();
		int base_, nparams;
		int line = ls.linenumber;
		if (ls.t.token == '(') {
			if (line != ls.lastline) {
				luaX_syntaxerror(ls,
						"ambiguous syntax (function call x new statement)");
			}
			luaX_next(ls);
			if (ls.t.token == ')') // arg list is empty?
			{
				args.k = expkind.VVOID;
			} else {
				explist1(ls, args);
				luaK_setmultret(fs, args);
			}
			check_match(ls, ')', '(', line);
		} else if (ls.t.token == '{')
			constructor(ls, args);
		else if (ls.t.token == RESERVED.TK_STRING.getValue()) {
			codestring(ls, args, ls.t.seminfo.ts);
			luaX_next(ls); // must use `seminfo' before `next'
		} else
			luaX_syntaxerror(ls, "function arguments expected");

		lua_assert(f.k == expkind.VNONRELOC);
		base_ = f.u.s.info; // base_ register for call
		if (hasmultret(args.k) != 0) {
			nparams = LUA_MULTRET; // open call
		} else {
			if (args.k != expkind.VVOID) {
				luaK_exp2nextreg(fs, args); // close last argument
			}
			nparams = fs.freereg - (base_ + 1);
		}
		init_exp(f, expkind.VCALL,
				luaK_codeABC(fs, OpCode.OP_CALL, base_, nparams + 1, 2));
		luaK_fixline(fs, line);
		fs.freereg = base_ + 1; /*
								 * call remove function and arguments and leaves
								 * (unless changed) one result
								 */
	}

	/*
	 * * {======================================================================
	 * * Expression parsing*
	 * =======================================================================
	 */

	public static void prefixexp(LexState ls, expdesc v) {
		/* prefixexp . NAME | '(' expr ')' */
		switch (ls.t.token) {
		case '(': {
			int line = ls.linenumber;
			luaX_next(ls);
			expr(ls, v);
			check_match(ls, ')', '(', line);
			luaK_dischargevars(ls.fs, v);
			return;
		}
		default: {
			if (ls.t.token == RESERVED.TK_NAME.getValue())
				singlevar(ls, v);
			else
				luaX_syntaxerror(ls, "unexpected symbol");
			return;
		}
		}
	}

	public static void primaryexp(LexState ls, expdesc v) {
		/*
		 * primaryexp . prefixexp { `.' NAME | `[' exp `]' | `:' NAME funcargs |
		 * funcargs }
		 */
		FuncState fs = ls.fs;
		prefixexp(ls, v);
		for (;;) {
			switch (ls.t.token) {
			case '.': // field
			{
				field(ls, v);
				break;
			}
			case '[': // `[' exp1 `]'
			{
				expdesc key = new expdesc();
				luaK_exp2anyreg(fs, v);
				yindex(ls, key);
				luaK_indexed(fs, v, key);
				break;
			}
			case ':': // `:' NAME funcargs
			{
				expdesc key = new expdesc();
				luaX_next(ls);
				checkname(ls, key);
				luaK_self(fs, v, key);
				funcargs(ls, v);
				break;
			}
			case '(': // funcargs
			case '{': {
				luaK_exp2nextreg(fs, v);
				funcargs(ls, v);
				break;
			}
			default: {
				if (ls.t.token == RESERVED.TK_STRING.getValue()) {
					luaK_exp2nextreg(fs, v);
					funcargs(ls, v);
					// break;
				} else
					return;
			}
			}
		}
	}

	public static void simpleexp(LexState ls, expdesc v) {
		/*
		 * simpleexp . NUMBER | STRING | NIL | true | false | ... | constructor
		 * | FUNCTION body | primaryexp
		 */
		if (ls.t.token == RESERVED.TK_NUMBER.getValue()) {
			init_exp(v, expkind.VKNUM, 0);
			v.u.nval = ls.t.seminfo.r;
		} else if (ls.t.token == RESERVED.TK_STRING.getValue())
			codestring(ls, v, ls.t.seminfo.ts);
		else if (ls.t.token == RESERVED.TK_NIL.getValue())
			init_exp(v, expkind.VNIL, 0);
		else if (ls.t.token == RESERVED.TK_TRUE.getValue())
			init_exp(v, expkind.VTRUE, 0);
		else if (ls.t.token == RESERVED.TK_FALSE.getValue())
			init_exp(v, expkind.VFALSE, 0);
		else if (ls.t.token == RESERVED.TK_DOTS.getValue()) {
			FuncState fs = ls.fs;
			check_condition(ls, fs.f.is_vararg != 0, "cannot use "
					+ LUA_QL("...") + " outside a vararg function");
			// FUCK TODO TASK: There is no Java equivalent to 'unchecked' in
			// this context:
			// ORIGINAL LINE: fs.f.is_vararg &=
			// unchecked((byte)(~VARARG_NEEDSARG));
			fs.f.is_vararg &= (byte) (~VARARG_NEEDSARG); // don't need 'arg'
			init_exp(v, expkind.VVARARG,
					luaK_codeABC(fs, OpCode.OP_VARARG, 0, 1, 0));
		} else if (ls.t.token == '{') {
			constructor(ls, v);
			return;
		} else if (ls.t.token == RESERVED.TK_FUNCTION.getValue()) {
			luaX_next(ls);
			body(ls, v, 0, ls.linenumber);
			return;
		} else {
			primaryexp(ls, v);
			return;
		}
		luaX_next(ls);
	}

	public static UnOpr getunopr(int op) {
		if (op == RESERVED.TK_NOT.getValue())
			return UnOpr.OPR_NOT;
		else if (op == '-')
			return UnOpr.OPR_MINUS;
		else if (op == '#')
			return UnOpr.OPR_LEN;
		else
			return UnOpr.OPR_NOUNOPR;
	}

	public static BinOpr getbinopr(int op) {
		if (op == '+')
			return BinOpr.OPR_ADD;
		else if (op == '-')
			return BinOpr.OPR_SUB;
		else if (op == '*')
			return BinOpr.OPR_MUL;
		else if (op == '/')
			return BinOpr.OPR_DIV;
		else if (op == '%')
			return BinOpr.OPR_MOD;
		else if (op == '^')
			return BinOpr.OPR_POW;
		else if (op == RESERVED.TK_CONCAT.getValue())
			return BinOpr.OPR_CONCAT;
		else if (op == RESERVED.TK_NE.getValue())
			return BinOpr.OPR_NE;
		else if (op == RESERVED.TK_EQ.getValue())
			return BinOpr.OPR_EQ;
		else if (op == '<')
			return BinOpr.OPR_LT;
		else if (op == RESERVED.TK_LE.getValue())
			return BinOpr.OPR_LE;
		else if (op == '>')
			return BinOpr.OPR_GT;
		else if (op == BinOpr.OPR_GT.getValue())
			return BinOpr.OPR_GE;
		else if (op == RESERVED.TK_GE.getValue())
			return BinOpr.OPR_GE;
		else if (op == RESERVED.TK_AND.getValue())
			return BinOpr.OPR_AND;
		else if (op == RESERVED.TK_OR.getValue())
			return BinOpr.OPR_OR;
		else
			return BinOpr.OPR_NOBINOPR;
	}

	public static class priority_ {
		public priority_(int leftP, int rightP) {
			this.left = (byte) leftP;
			this.right = (byte) rightP;
		}

		public byte left; // left priority for each binary operator
		public byte right; // right priority
	}

	public static priority_[] priority = { new priority_(6, 6),
			new priority_(6, 6), new priority_(7, 7), new priority_(7, 7),
			new priority_(7, 7), new priority_(10, 9), new priority_(5, 4),
			new priority_(3, 3), new priority_(3, 3), new priority_(3, 3),
			new priority_(3, 3), new priority_(3, 3), new priority_(3, 3),
			new priority_(2, 2), new priority_(1, 1) }; // ORDER OPR

	public static final int UNARY_PRIORITY = 8; // priority for unary operators

	/*
	 * * subexpr . (simpleexp | unop subexpr) { binop subexpr }* where `binop'
	 * is any binary operator with a priority higher than `limit'
	 */
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static BinOpr subexpr (LexState ls, expdesc v, uint
	// limit)
	public static BinOpr subexpr(LexState ls, expdesc v, int limit) {
		BinOpr op = BinOpr.OPR_ADD;
		UnOpr uop = UnOpr.OPR_LEN;
		enterlevel(ls);
		uop = getunopr(ls.t.token);
		if (uop != UnOpr.OPR_NOUNOPR) {
			luaX_next(ls);
			subexpr(ls, v, UNARY_PRIORITY);
			luaK_prefix(ls.fs, uop, v);
		} else {
			simpleexp(ls, v);
		}
		/* expand while operators have priorities higher than `limit' */
		op = getbinopr(ls.t.token);
		while (op.getValue() != BinOpr.OPR_NOBINOPR.getValue()
				&& priority[op.getValue()].left > limit) {
			expdesc v2 = new expdesc();
			BinOpr nextop;
			luaX_next(ls);
			luaK_infix(ls.fs, op, v);
			/* read sub-expression with higher priority */
			nextop = subexpr(ls, v2, priority[op.getValue()].right);
			luaK_posfix(ls.fs, op, v, v2);
			op = nextop;
		}
		leavelevel(ls);
		return op; // return first untreated operator
	}

	public static void expr(LexState ls, expdesc v) {
		subexpr(ls, v, 0);
	}

	/* }==================================================================== */

	/*
	 * * {======================================================================
	 * * Rules for Statements*
	 * =======================================================================
	 */

	public static int block_follow(int token) {
		if (token == RESERVED.TK_ELSE.getValue()
				|| token == RESERVED.TK_ELSEIF.getValue()
				|| token == RESERVED.TK_END.getValue()
				|| token == RESERVED.TK_UNTIL.getValue()
				|| token == RESERVED.TK_EOS.getValue())
			return 1;
		else
			return 0;
	}

	public static void block(LexState ls) {
		/* block . chunk */
		FuncState fs = ls.fs;
		BlockCnt bl = new BlockCnt();
		enterblock(fs, bl, Integer.valueOf(0).byteValue());
		chunk(ls);
		lua_assert(bl.breaklist == NO_JUMP);
		leaveblock(fs);
	}

	/*
	 * * structure to chain all variables in the left-hand side of an*
	 * assignment
	 */
	public static class LHS_assign {
		public LHS_assign prev;
		public expdesc v = new expdesc(); // variable (global, local, upvalue,
											// or indexed)
	}

	/*
	 * * check whether, in an assignment to a local variable, the local variable
	 * * is needed in a previous assignment (to a table). If so, save original*
	 * local value in a safe place and use this safe copy in the previous*
	 * assignment.
	 */
	public static void check_conflict(LexState ls, LHS_assign lh, expdesc v) {
		FuncState fs = ls.fs;
		int extra = fs.freereg; // eventual position to save local variable
		int conflict = 0;
		for (; lh != null; lh = lh.prev) {
			if (lh.v.k == expkind.VINDEXED) {
				if (lh.v.u.s.info == v.u.s.info) // conflict?
				{
					conflict = 1;
					lh.v.u.s.info = extra; // previous assignment will use safe
											// copy
				}
				if (lh.v.u.s.aux == v.u.s.info) // conflict?
				{
					conflict = 1;
					lh.v.u.s.aux = extra; // previous assignment will use safe
											// copy
				}
			}
		}
		if (conflict != 0) {
			luaK_codeABC(fs, OpCode.OP_MOVE, fs.freereg, v.u.s.info, 0); // make
																			// copy
			luaK_reserveregs(fs, 1);
		}
	}

	public static void assignment(LexState ls, LHS_assign lh, int nvars) {
		expdesc e = new expdesc();
		check_condition(ls, expkind.VLOCAL.getValue() <= lh.v.k.getValue()
				&& lh.v.k.getValue() <= expkind.VINDEXED.getValue(),
				"syntax error");
		if (testnext(ls, ',') != 0) // assignment. `,' primaryexp assignment
		{
			LHS_assign nv = new LHS_assign();
			nv.prev = lh;
			primaryexp(ls, nv.v);
			if (nv.v.k == expkind.VLOCAL) {
				check_conflict(ls, lh, nv.v);
			}
			luaY_checklimit(ls.fs, nvars, LUAI_MAXCCALLS - ls.L.nCcalls,
					"variables in assignment");
			assignment(ls, nv, nvars + 1);
		} else // assignment. `=' explist1
		{
			int nexps;
			checknext(ls, '=');
			nexps = explist1(ls, e);
			if (nexps != nvars) {
				adjust_assign(ls, nvars, nexps, e);
				if (nexps > nvars) {
					ls.fs.freereg -= nexps - nvars; // remove extra values
				}
			} else {
				luaK_setoneret(ls.fs, e); // close last expression
				luaK_storevar(ls.fs, lh.v, e);
				return; // avoid default
			}
		}
		init_exp(e, expkind.VNONRELOC, ls.fs.freereg - 1); // default assignment
		luaK_storevar(ls.fs, lh.v, e);
	}

	public static int cond(LexState ls) {
		/* cond . exp */
		expdesc v = new expdesc();
		expr(ls, v); // read condition
		if (v.k == expkind.VNIL) // `falses' are all equal here
		{
			v.k = expkind.VFALSE;
		}
		luaK_goiftrue(ls.fs, v);
		return v.f;
	}

	public static void breakstat(LexState ls) {
		FuncState fs = ls.fs;
		BlockCnt bl = fs.bl;
		int upval = 0;
		while ((bl != null) && (bl.isbreakable == 0)) {
			upval |= bl.upval;
			bl = bl.previous;
		}
		if (bl == null) {
			luaX_syntaxerror(ls, "no loop to break");
		}
		if (upval != 0) {
			luaK_codeABC(fs, OpCode.OP_CLOSE, bl.nactvar, 0, 0);
		}
		RefObject<Integer> tempRef_breaklist = new RefObject<Integer>(
				bl.breaklist);
		luaK_concat(fs, tempRef_breaklist, luaK_jump(fs));
		bl.breaklist = tempRef_breaklist.argvalue;
	}

	public static void whilestat(LexState ls, int line) {
		/* whilestat . WHILE cond DO block END */
		FuncState fs = ls.fs;
		int whileinit;
		int condexit;
		BlockCnt bl = new BlockCnt();
		luaX_next(ls); // skip WHILE
		whileinit = luaK_getlabel(fs);
		condexit = cond(ls);
		enterblock(fs, bl, Integer.valueOf(1).byteValue());
		checknext(ls, RESERVED.TK_DO.getValue());
		block(ls);
		luaK_patchlist(fs, luaK_jump(fs), whileinit);
		check_match(ls, RESERVED.TK_END.getValue(),
				RESERVED.TK_WHILE.getValue(), line);
		leaveblock(fs);
		luaK_patchtohere(fs, condexit); // false conditions finish the loop
	}

	public static void repeatstat(LexState ls, int line) {
		/* repeatstat . REPEAT block UNTIL cond */
		int condexit;
		FuncState fs = ls.fs;
		int repeat_init = luaK_getlabel(fs);
		BlockCnt bl1 = new BlockCnt(), bl2 = new BlockCnt();
		enterblock(fs, bl1, Integer.valueOf(1).byteValue()); // loop block
		enterblock(fs, bl2, Integer.valueOf(0).byteValue()); // scope block
		luaX_next(ls); // skip REPEAT
		chunk(ls);
		check_match(ls, RESERVED.TK_UNTIL.getValue(),
				RESERVED.TK_REPEAT.getValue(), line);
		condexit = cond(ls); // read condition (inside scope block)
		if (bl2.upval == 0) // no upvalues?
		{
			leaveblock(fs); // finish scope
			luaK_patchlist(ls.fs, condexit, repeat_init); // close the loop
		} else // complete semantics when there are upvalues
		{
			breakstat(ls); // if condition then break
			luaK_patchtohere(ls.fs, condexit); // else...
			leaveblock(fs); // finish scope...
			luaK_patchlist(ls.fs, luaK_jump(fs), repeat_init); // and repeat
		}
		leaveblock(fs); // finish loop
	}

	public static int exp1(LexState ls) {
		expdesc e = new expdesc();
		int k;
		expr(ls, e);
		k = (int) e.k.getValue();
		luaK_exp2nextreg(ls.fs, e);
		return k;
	}

	public static void forbody(LexState ls, int base_, int line, int nvars,
			int isnum) {
		/* forbody . DO block */
		BlockCnt bl = new BlockCnt();
		FuncState fs = ls.fs;
		int prep, endfor;
		adjustlocalvars(ls, 3); // control variables
		checknext(ls, RESERVED.TK_DO.getValue());
		prep = (isnum != 0) ? luaK_codeAsBx(fs, OpCode.OP_FORPREP, base_,
				NO_JUMP) : luaK_jump(fs);
		enterblock(fs, bl, Integer.valueOf(0).byteValue()); // scope for
															// declared
															// variables
		adjustlocalvars(ls, nvars);
		luaK_reserveregs(fs, nvars);
		block(ls);
		leaveblock(fs); // end of scope for declared variables
		luaK_patchtohere(fs, prep);
		endfor = (isnum != 0) ? luaK_codeAsBx(fs, OpCode.OP_FORLOOP, base_,
				NO_JUMP)
				: luaK_codeABC(fs, OpCode.OP_TFORLOOP, base_, 0, nvars);
		luaK_fixline(fs, line); // pretend that `OP_FOR' starts the loop
		luaK_patchlist(fs, ((isnum != 0) ? endfor : luaK_jump(fs)), prep + 1);
	}

	public static void fornum(LexState ls, TString varname, int line) {
		/* fornum . NAME = exp1,exp1[,exp1] forbody */
		FuncState fs = ls.fs;
		int base_ = fs.freereg;
		new_localvarliteral(ls, "(for index)", 0);
		new_localvarliteral(ls, "(for limit)", 1);
		new_localvarliteral(ls, "(for step)", 2);
		new_localvar(ls, varname, 3);
		checknext(ls, '=');
		exp1(ls); // initial value
		checknext(ls, ',');
		exp1(ls); // limit
		if (testnext(ls, ',') != 0) {
			exp1(ls); // optional step
		} else // default step = 1
		{
			luaK_codeABx(fs, OpCode.OP_LOADK, fs.freereg, luaK_numberK(fs, 1));
			luaK_reserveregs(fs, 1);
		}
		forbody(ls, base_, line, 1, 1);
	}

	public static void forlist(LexState ls, TString indexname) {
		/* forlist . NAME {,NAME} IN explist1 forbody */
		FuncState fs = ls.fs;
		expdesc e = new expdesc();
		int nvars = 0;
		int line;
		int base_ = fs.freereg;
		/* create control variables */
		new_localvarliteral(ls, "(for generator)", nvars++);
		new_localvarliteral(ls, "(for state)", nvars++);
		new_localvarliteral(ls, "(for control)", nvars++);
		/* create declared variables */
		new_localvar(ls, indexname, nvars++);
		while (testnext(ls, ',') != 0) {
			new_localvar(ls, str_checkname(ls), nvars++);
		}
		checknext(ls, RESERVED.TK_IN.getValue());
		line = ls.linenumber;
		adjust_assign(ls, 3, explist1(ls, e), e);
		luaK_checkstack(fs, 3); // extra space to call generator
		forbody(ls, base_, line, nvars - 3, 0);
	}

	public static void forstat(LexState ls, int line) {
		/* forstat . FOR (fornum | forlist) END */
		FuncState fs = ls.fs;
		TString varname;
		BlockCnt bl = new BlockCnt();
		enterblock(fs, bl, Integer.valueOf(1).byteValue()); // scope for loop
															// and control
															// variables
		luaX_next(ls); // skip `for'
		varname = str_checkname(ls); // first variable name
		switch (ls.t.token) {
		case '=':
			fornum(ls, varname, line);
			break;
		case ',':
			forlist(ls, varname);
			break;
		default:
			if (ls.t.token == RESERVED.TK_IN.getValue())
				forlist(ls, varname);
			else
				luaX_syntaxerror(ls, LUA_QL("=") + " or " + LUA_QL("in")
						+ " expected");
			break;
		}
		check_match(ls, RESERVED.TK_END.getValue(), RESERVED.TK_FOR.getValue(),
				line);
		leaveblock(fs); // loop scope (`break' jumps to this point)
	}

	public static int test_then_block(LexState ls) {
		/* test_then_block . [IF | ELSEIF] cond THEN block */
		int condexit;
		luaX_next(ls); // skip IF or ELSEIF
		condexit = cond(ls);
		checknext(ls, RESERVED.TK_THEN.getValue());
		block(ls); // `then' part
		return condexit;
	}

	public static void ifstat(LexState ls, int line) {
		/* ifstat . IF cond THEN block {ELSEIF cond THEN block} [ELSE block] END */
		FuncState fs = ls.fs;
		int flist;
		int escapelist = NO_JUMP;
		flist = test_then_block(ls); // IF cond THEN block
		RefObject<Integer> tempRef_escapelist = new RefObject<Integer>(
				escapelist);
		while (ls.t.token == RESERVED.TK_ELSEIF.getValue()) {
			luaK_concat(fs, tempRef_escapelist, luaK_jump(fs));
			escapelist = tempRef_escapelist.argvalue;
			luaK_patchtohere(fs, flist);
			flist = test_then_block(ls); // ELSEIF cond THEN block
		}
		if (ls.t.token == RESERVED.TK_ELSE.getValue()) {
			luaK_concat(fs, tempRef_escapelist, luaK_jump(fs));
			escapelist = tempRef_escapelist.argvalue;
			luaK_patchtohere(fs, flist);
			luaX_next(ls); // skip ELSE (after patch, for correct line info)
			block(ls); // `else' part
		} else {
			luaK_concat(fs, tempRef_escapelist, flist);
		}
		escapelist = tempRef_escapelist.argvalue;
		luaK_patchtohere(fs, escapelist);
		check_match(ls, RESERVED.TK_END.getValue(), RESERVED.TK_IF.getValue(),
				line);
	}

	public static void localfunc(LexState ls) {
		expdesc v = new expdesc(), b = new expdesc();
		FuncState fs = ls.fs;
		new_localvar(ls, str_checkname(ls), 0);
		init_exp(v, expkind.VLOCAL, fs.freereg);
		luaK_reserveregs(fs, 1);
		adjustlocalvars(ls, 1);
		body(ls, b, 0, ls.linenumber);
		luaK_storevar(fs, v, b);
		/* debug information will only see the variable after this point! */
		getlocvar(fs, fs.nactvar - 1).startpc = fs.pc;
	}

	public static void localstat(LexState ls) {
		/* stat . LOCAL NAME {`,' NAME} [`=' explist1] */
		int nvars = 0;
		int nexps;
		expdesc e = new expdesc();
		do {
			new_localvar(ls, str_checkname(ls), nvars++);
		} while (testnext(ls, ',') != 0);
		if (testnext(ls, '=') != 0) {
			nexps = explist1(ls, e);
		} else {
			e.k = expkind.VVOID;
			nexps = 0;
		}
		adjust_assign(ls, nvars, nexps, e);
		adjustlocalvars(ls, nvars);
	}

	public static int funcname(LexState ls, expdesc v) {
		/* funcname . NAME {field} [`:' NAME] */
		int needself = 0;
		singlevar(ls, v);
		while (ls.t.token == '.') {
			field(ls, v);
		}
		if (ls.t.token == ':') {
			needself = 1;
			field(ls, v);
		}
		return needself;
	}

	public static void funcstat(LexState ls, int line) {
		/* funcstat . FUNCTION funcname body */
		int needself;
		expdesc v = new expdesc(), b = new expdesc();
		luaX_next(ls); // skip FUNCTION
		needself = funcname(ls, v);
		body(ls, b, needself, line);
		luaK_storevar(ls.fs, v, b);
		luaK_fixline(ls.fs, line); // definition `happens' in the first line
	}

	public static void exprstat(LexState ls) {
		/* stat . func | assignment */
		FuncState fs = ls.fs;
		LHS_assign v = new LHS_assign();
		primaryexp(ls, v.v);
		if (v.v.k == expkind.VCALL) // stat. func
		{
			SETARG_C(getcode(fs, v.v), 1); // call statement uses no results
		} else // stat. assignment
		{
			v.prev = null;
			assignment(ls, v, 1);
		}
	}

	public static void retstat(LexState ls) {
		/* stat . RETURN explist */
		FuncState fs = ls.fs;
		expdesc e = new expdesc();
		int first, nret; // registers with returned values
		luaX_next(ls); // skip RETURN
		if ((block_follow(ls.t.token) != 0) || ls.t.token == ';') {
			first = nret = 0; // return no values
		} else {
			nret = explist1(ls, e); // optional return values
			if (hasmultret(e.k) != 0) {
				luaK_setmultret(fs, e);
				if (e.k == expkind.VCALL && nret == 1) // tail call?
				{
					SET_OPCODE(getcode(fs, e), OpCode.OP_TAILCALL);
					lua_assert(GETARG_A(getcode(fs, e)) == fs.nactvar);
				}
				first = fs.nactvar;
				nret = LUA_MULTRET; // return all values
			} else {
				if (nret == 1) // only one single value?
				{
					first = luaK_exp2anyreg(fs, e);
				} else {
					luaK_exp2nextreg(fs, e); // values must go to the `stack'
					first = fs.nactvar; // return all `active' values
					lua_assert(nret == fs.freereg - first);
				}
			}
		}
		luaK_ret(fs, first, nret);
	}

	public static int statement(LexState ls) {
		int line = ls.linenumber; // may be needed for error messages
		if (ls.t.token == RESERVED.TK_IF.getValue()) {
			ifstat(ls, line);
			return 0;
		} else if (ls.t.token == RESERVED.TK_WHILE.getValue()) {
			whilestat(ls, line);
			return 0;
		} else if (ls.t.token == RESERVED.TK_DO.getValue()) {
			luaX_next(ls); // skip DO
			block(ls);
			check_match(ls, RESERVED.TK_END.getValue(),
					RESERVED.TK_DO.getValue(), line);
			return 0;
		} else if (ls.t.token == RESERVED.TK_FOR.getValue()) {
			forstat(ls, line);
			return 0;
		} else if (ls.t.token == RESERVED.TK_REPEAT.getValue()) {
			repeatstat(ls, line);
			return 0;
		} else if (ls.t.token == RESERVED.TK_FUNCTION.getValue()) {
			funcstat(ls, line); // stat. funcstat
			return 0;
		} else if (ls.t.token == RESERVED.TK_LOCAL.getValue()) {
			luaX_next(ls); // skip LOCAL
			if (testnext(ls, RESERVED.TK_FUNCTION.getValue()) != 0) // local
																	// function?
			{
				localfunc(ls);
			} else {
				localstat(ls);
			}
			return 0;
		} else if (ls.t.token == RESERVED.TK_RETURN.getValue()) {
			retstat(ls);
			return 1; // must be last statement
		} else if (ls.t.token == RESERVED.TK_BREAK.getValue()) {
			luaX_next(ls); // skip BREAK
			breakstat(ls);
			return 1; // must be last statement
		} else {
			exprstat(ls);
			return 0; // to avoid warnings
		}
	}

	public static void chunk(LexState ls) {
		/* chunk . { stat [`;'] } */
		int islast = 0;
		enterlevel(ls);
		while ((islast == 0) && (block_follow(ls.t.token) == 0)) {
			islast = statement(ls);
			testnext(ls, ';');
			lua_assert(ls.fs.f.maxstacksize >= ls.fs.freereg
					&& ls.fs.freereg >= ls.fs.nactvar);
			ls.fs.freereg = ls.fs.nactvar; // free registers
		}
		leavelevel(ls);
	}

	/* }====================================================================== */

	/* table of globals */
	public static lua_TValue gt(lua_State L) {
		return L.l_gt;
	}

	/* registry */
	public static lua_TValue registry(lua_State L) {
		return G(L).l_registry;
	}

	/* extra stack space to handle TM calls and some other extras */
	public static final int EXTRA_STACK = 5;

	public static final int BASIC_CI_SIZE = 8;

	public static final int BASIC_STACK_SIZE = (2 * LUA_MINSTACK);

	public static class stringtable {
		// public GCObject[] hash;
		public GCObject[] hash;
		public int nuse; // number of elements
		public int size;
	}

	/*
	 * * informations about a call
	 */
	public static class CallInfo implements ArrayElement {
		private CallInfo[] values = null;
		private int index = -1;

		public final void set_index(int index) {
			this.index = index;
		}

		public final void set_array(Object array) {
			this.values = (CallInfo[]) array;
			assert this.values != null;
		}

		public final CallInfo getItem(int offset) {
			return values[index + offset];
		}

		public static CallInfo OpAddition(CallInfo value, int offset) {
			return value.values[value.index + offset];
		}

		public static CallInfo OpSubtraction(CallInfo value, int offset) {
			return value.values[value.index - offset];
		}

		public static int OpSubtraction(CallInfo ci, CallInfo[] values) {
			assert ci.values == values;
			return ci.index;
		}

		public static int OpSubtraction(CallInfo ci1, CallInfo ci2) {
			assert ci1.values == ci2.values;
			return ci1.index - ci2.index;
		}

		public static boolean OpLessThan(CallInfo ci1, CallInfo ci2) {
			assert ci1.values == ci2.values;
			return ci1.index < ci2.index;
		}

		public static boolean OpLessThanOrEqual(CallInfo ci1, CallInfo ci2) {
			assert ci1.values == ci2.values;
			return ci1.index <= ci2.index;
		}

		public static boolean OpGreaterThan(CallInfo ci1, CallInfo ci2) {
			assert ci1.values == ci2.values;
			return ci1.index > ci2.index;
		}

		public static boolean OpGreaterThanOrEqual(CallInfo ci1, CallInfo ci2) {
			assert ci1.values == ci2.values;
			return ci1.index >= ci2.index;
		}

		public static CallInfo inc(RefObject<CallInfo> value) {
			value.argvalue = value.argvalue.getItem(1);
			return value.argvalue.getItem(-1);
		}

		public static CallInfo dec(RefObject<CallInfo> value) {
			value.argvalue = value.argvalue.getItem(-1);
			return value.argvalue.getItem(1);
		}

		public lua_TValue base_; // base for this function
		public lua_TValue func; // function index in the stack
		public lua_TValue top; // top for this function
		public InstructionPtr savedpc;
		public int nresults; // expected number of results from this function
		public int tailcalls; // number of tail calls lost under this entry
	}

	public static Closure curr_func(lua_State L) {
		return (clvalue(L.ci.func));
	}

	public static Closure ci_func(CallInfo ci) {
		return (clvalue(ci.func));
	}

	public static boolean f_isLua(CallInfo ci) {
		return ci_func(ci).c.getisC() == 0;
	}

	public static boolean isLua(CallInfo ci) {
		return (ttisfunction((ci).func) && f_isLua(ci));
	}

	/*
	 * * `global state', shared by all threads of this state
	 */
	public static class global_State {
		public stringtable strt = new stringtable(); // hash table for strings
		public IDelegate frealloc;// public lua_Alloc frealloc; // function to
									// reallocate memory
		public Object ud; // auxiliary data to `frealloc'
		public short currentwhite;
		public byte gcstate; // state of garbage collector
		public int sweepstrgc; // position of sweep in `strt'
		public GCObject rootgc; // list of all collectable objects
		public GCObjectRef sweepgc; // position of sweep in `rootgc'
		public GCObject gray; // list of gray objects
		public GCObject grayagain; // list of objects to be traversed atomically
		public GCObject weak; // list of weak tables (to be cleared)
		public GCObject tmudata; // last element of list of userdata to be GC
		public Mbuffer buff = new Mbuffer(); // temporary buffer for string
												// concatentation
		public int GCthreshold;
		public int totalbytes; // number of bytes currently allocated
		public int estimate; // an estimate of number of bytes actually in use
		public int gcdept; // how much GC is `behind schedule'
		public int gcpause; // size of pause between successive GCs
		public int gcstepmul; // GC `granularity'
		public IDelegate panic;// public lua_CFunction panic; // to be called in
								// unprotected errors
		public lua_TValue l_registry = new lua_TValue();
		public lua_State mainthread;
		public UpVal uvhead = new UpVal(); // head of double-linked list of all
											// open upvalues
		public Table[] mt = new Table[NUM_TAGS]; // metatables for basic types
		public TString[] tmname = new TString[TMS.TM_N.getValue()]; // array
																	// with
																	// tag-method
																	// names
	}

	/*
	 * * `per thread' state
	 */
	public static class lua_State extends GCObject {

		public short status;
		public lua_TValue top; // first free slot in the stack
		public lua_TValue base_; // base of current function
		public global_State l_G;
		public CallInfo ci; // call info for current function
		public InstructionPtr savedpc = new InstructionPtr(); // `savedpc' of
																// current
																// function
		public lua_TValue stack_last; // last free slot in the stack
		public lua_TValue[] stack; // stack base
		public CallInfo end_ci; // points after end of ci array
		public CallInfo[] base_ci; // array of CallInfo's
		public int stacksize;
		public int size_ci; // size of array `base_ci'
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public ushort nCcalls;
		public short nCcalls; // number of nested C calls
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public ushort baseCcalls;
		public short baseCcalls; // nested C calls when resuming coroutine
		public short hookmask;
		public byte allowhook;
		public int basehookcount;
		public int hookcount;
		IDelegate hook;// public lua_Hook hook;
		public lua_TValue l_gt = new lua_TValue(); // table of globals
		public lua_TValue env = new lua_TValue(); // temporary place for
													// environments
		public GCObject openupval; // list of open upvalues in this stack
		public GCObject gclist;
		public lua_longjmp errorJmp; // current error recover point
		public int errfunc; // current error handling function (stack index)
	}

	public static global_State G(lua_State L) {
		return L.l_G;
	}

	public static void G_set(lua_State L, global_State s) {
		L.l_G = s;
	}

	/*
	 * * Union of all collectable objects (not a union anymore in the C# port)
	 */
	public static class GCObject extends GCheader implements ArrayElement {
		public final void set_index(int index) {
			// this.index = index;
		}

		public final void set_array(Object array) {
			// this.values = (GCObject[])array;
			// Debug.Assert(this.values != null);
		}

		public final GCheader getgch() {
			return (GCheader) this;
		}

		public final TString getts() {
			return (TString) this;
		}

		public final Udata getu() {
			return (Udata) this;
		}

		public final Closure getcl() {
			return (Closure) this;
		}

		public final Table geth() {
			return (Table) this;
		}

		public final Proto getp() {
			return (Proto) this;
		}

		public final UpVal getuv() {
			return (UpVal) this;
		}

		public final lua_State getth() {
			return (lua_State) this;
		}
	}

	/*
	 * this interface and is used for implementing GCObject references, it's
	 * used to emulate the behaviour of a C-style GCObject **
	 */
	public interface GCObjectRef {
		void set(GCObject value);

		GCObject get();
	}

	public static class ArrayRef implements GCObjectRef, ArrayElement {
		public ArrayRef() {
			this.array_elements = null;
			this.array_index = 0;
			this.vals = null;
			this.index = 0;
		}

		public ArrayRef(GCObject[] array_elements, int array_index) {
			this.array_elements = array_elements;
			this.array_index = array_index;
			this.vals = null;
			this.index = 0;
		}

		public final void set(GCObject value) {
			array_elements[array_index] = value;
		}

		public final GCObject get() {
			return array_elements[array_index];
		}

		public final void set_index(int index) {
			this.index = index;
		}

		public final void set_array(Object vals) {
			// don't actually need this
			this.vals = (ArrayRef[]) vals;
			assert this.vals != null;
		}

		// ArrayRef is used to reference GCObject objects in an array, the next
		// two members
		// point to that array and the index of the GCObject element we are
		// referencing
		private GCObject[] array_elements;
		private int array_index;

		// ArrayRef is itself stored in an array and derived from ArrayElement,
		// the next
		// two members refer to itself i.e. the array and index of it's own
		// instance.
		private ArrayRef[] vals;
		private int index;
	}

	public static class OpenValRef implements GCObjectRef {
		public OpenValRef(lua_State L) {
			this.L = L;
		}

		public final void set(GCObject value) {
			this.L.openupval = value;
		}

		public final GCObject get() {
			return this.L.openupval;
		}

		private lua_State L;
	}

	public static class RootGCRef implements GCObjectRef {
		public RootGCRef(global_State g) {
			this.g = g;
		}

		public final void set(GCObject value) {
			this.g.rootgc = value;
		}

		public final GCObject get() {
			return this.g.rootgc;
		}

		private global_State g;
	}

	public static class NextRef implements GCObjectRef {
		public NextRef(GCheader header) {
			this.header = header;
		}

		public final void set(GCObject value) {
			this.header.next = value;
		}

		public final GCObject get() {
			return this.header.next;
		}

		private GCheader header;
	}

	/* macros to convert a GCObject into a specific value */
	public static TString rawgco2ts(GCObject o) {
		return (TString) check_exp(o.getgch().tt == LUA_TSTRING, o.getts());
	}

	public static TString gco2ts(GCObject o) {
		return (TString) (rawgco2ts(o).gettsv());
	}

	public static Udata rawgco2u(GCObject o) {
		return (Udata) check_exp(o.getgch().tt == LUA_TUSERDATA, o.getu());
	}

	public static Udata gco2u(GCObject o) {
		return (Udata) (rawgco2u(o).uv);
	}

	public static Closure gco2cl(GCObject o) {
		return (Closure) check_exp(o.getgch().tt == LUA_TFUNCTION, o.getcl());
	}

	public static Table gco2h(GCObject o) {
		return (Table) check_exp(o.getgch().tt == LUA_TTABLE, o.geth());
	}

	public static Proto gco2p(GCObject o) {
		return (Proto) check_exp(o.getgch().tt == LUA_TPROTO, o.getp());
	}

	public static UpVal gco2uv(GCObject o) {
		return (UpVal) check_exp(o.getgch().tt == LUA_TUPVAL, o.getuv());
	}

	public static UpVal ngcotouv(GCObject o) {
		return (UpVal) check_exp((o == null) || (o.getgch().tt == LUA_TUPVAL),
				o.getuv());
	}

	public static lua_State gco2th(GCObject o) {
		return (lua_State) check_exp(o.getgch().tt == LUA_TTHREAD, o.getth());
	}

	/* macro to convert any Lua object into a GCObject */
	public static GCObject obj2gco(Object v) {
		return (GCObject) v;
	}

	public static int state_size(Object x) {
		return SizeOf.sizeof(x);// return Marshal.SizeOf(x) + LUAI_EXTRASPACE;
	}

	/*
	 * public static byte fromstate(object l) { return (byte)(l -
	 * LUAI_EXTRASPACE); }
	 */
	public static lua_State tostate(Object l) {
		// Debug.Assert(LUAI_EXTRASPACE == 0, "LUAI_EXTRASPACE not supported");
		return (lua_State) l;
	}

	/*
	 * * Main thread combines a thread state and the global state
	 */
	public static class LG extends lua_State {
		public final lua_State getl() {
			return this;
		}

		public global_State g = new global_State();
	}

	public static void stack_init(lua_State L1, lua_State L) {
		/* initialize CallInfo array */
		L1.base_ci = Lua.<CallInfo> luaM_newvector(L, BASIC_CI_SIZE,
				CallInfo.class);
		L1.ci = L1.base_ci[0];
		L1.size_ci = BASIC_CI_SIZE;
		L1.end_ci = L1.base_ci[L1.size_ci - 1];
		/* initialize stack array */
		L1.stack = Lua.<lua_TValue> luaM_newvector(L, BASIC_STACK_SIZE
				+ EXTRA_STACK, lua_TValue.class);
		L1.stacksize = BASIC_STACK_SIZE + EXTRA_STACK;
		L1.top = L1.stack[0];
		L1.stack_last = L1.stack[L1.stacksize - EXTRA_STACK - 1];
		/* initialize first ci */
		L1.ci.func = L1.top;
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L1.top);
		setnilvalue(lua_TValue.inc(tempRef_top)); // `function' entry for this
													// `ci'
		L1.top = tempRef_top.argvalue;
		L1.base_ = L1.ci.base_ = L1.top;
		L1.ci.top = Lua.lua_TValue.OpAddition(L1.top, LUA_MINSTACK);
	}

	public static void freestack(lua_State L, lua_State L1) {
		luaM_freearray(L, L1.base_ci);
		luaM_freearray(L, L1.stack);
	}

	/*
	 * * open parts that may cause memory-allocation errors
	 */
	public static void f_luaopen(lua_State L, Object ud) {
		global_State g = G(L);
		// UNUSED(ud);
		stack_init(L, L); // init stack
		sethvalue(L, gt(L), luaH_new(L, 0, 2)); // table of globals
		sethvalue(L, registry(L), luaH_new(L, 0, 2)); // registry
		luaS_resize(L, MINSTRTABSIZE); // initial size of string table
		luaT_init(L);
		luaX_init(L);
		luaS_fix(luaS_newliteral(L, MEMERRMSG));
		g.GCthreshold = 4 * g.totalbytes;
	}

	public static void preinit_state(lua_State L, global_State g) {
		G_set(L, g);
		L.stack = null;
		L.stacksize = 0;
		L.errorJmp = null;
		L.hook = null;
		L.hookmask = 0;
		L.basehookcount = 0;
		L.allowhook = 1;
		resethookcount(L);
		L.openupval = null;
		L.size_ci = 0;
		L.nCcalls = L.baseCcalls = 0;
		L.status = 0;
		L.base_ci = null;
		L.ci = null;
		L.savedpc = new InstructionPtr();
		L.errfunc = 0;
		setnilvalue(gt(L));
	}

	public static void close_state(lua_State L) {
		global_State g = G(L);
		luaF_close(L, L.stack[0]); // close all upvalues for this thread
		luaC_freeall(L); // collect all objects
		lua_assert(g.rootgc == obj2gco(L));
		lua_assert(g.strt.nuse == 0);
		luaM_freearray(L, G(L).strt.hash);
		luaZ_freebuffer(L, g.buff);
		freestack(L, L);
		lua_assert(g.totalbytes == GetUnmanagedSize(LG.class));
		// g.frealloc(g.ud, fromstate(L), (uint)state_size(typeof(LG)), 0);
	}

	public static lua_State luaE_newthread(lua_State L) {
		// lua_State L1 = tostate(luaM_malloc(L,
		// state_size(typeof(lua_State))));
		lua_State L1 = Lua.<lua_State> luaM_new(L, lua_State.class);
		luaC_link(L, obj2gco(L1), Integer.valueOf(LUA_TTHREAD).byteValue());
		preinit_state(L1, G(L));
		stack_init(L1, L); // init stack
		setobj2n(L, gt(L1), gt(L)); // share table of globals
		L1.hookmask = L.hookmask;
		L1.basehookcount = L.basehookcount;
		L1.hook = L.hook;
		resethookcount(L1);
		lua_assert(iswhite(obj2gco(L1)));
		return L1;
	}

	public static void luaE_freethread(lua_State L, lua_State L1) {
		luaF_close(L1, L1.stack[0]); // close all upvalues for this thread
		lua_assert(L1.openupval == null);
		luai_userstatefree(L1);
		freestack(L, L1);
		// luaM_freemem(L, fromstate(L1));
	}

	// static IDelegate f_luaopen = Pfunc.build(Lua.class, "f_luaopen");
	static IDelegate f_luaopen = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			f_luaopen((lua_State) arg1, arg2);
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static lua_State lua_newstate(IDelegate f, Object ud)// lua_Alloc f
	{
		int i;
		lua_State L;
		global_State g;
		// object l = f(ud, null, 0, (uint)state_size(typeof(LG)));
		Object l = f.invoke(LG.class);
		if (l == null) {
			return null;
		}
		L = tostate(l);
		g = ((LG) ((L instanceof LG) ? L : null)).g;
		L.next = null;
		L.tt = LUA_TTHREAD;
		g.currentwhite = (byte) bit2mask(WHITE0BIT, FIXEDBIT);
		L.marked = luaC_white(g);
		byte marked = L.marked; // can't pass properties in as ref
		RefObject<Byte> tempRef_marked = new RefObject<Byte>(marked);
		set2bits(tempRef_marked, FIXEDBIT, SFIXEDBIT);
		marked = tempRef_marked.argvalue;
		L.marked = marked;
		preinit_state(L, g);
		g.frealloc = f;
		g.ud = ud;
		g.mainthread = L;
		g.uvhead.u.l.prev = g.uvhead;
		g.uvhead.u.l.next = g.uvhead;
		g.GCthreshold = 0; // mark it as unfinished state
		g.strt.size = 0;
		g.strt.nuse = 0;
		g.strt.hash = null;
		setnilvalue(registry(L));
		luaZ_initbuffer(L, g.buff);
		g.panic = null;
		g.gcstate = GCSpause;
		g.rootgc = obj2gco(L);
		g.sweepstrgc = 0;
		g.sweepgc = new RootGCRef(g);
		g.gray = null;
		g.grayagain = null;
		g.weak = null;
		g.tmudata = null;
		g.totalbytes = (int) GetUnmanagedSize(LG.class);
		g.gcpause = LUAI_GCPAUSE;
		g.gcstepmul = LUAI_GCMUL;
		g.gcdept = 0;
		for (i = 0; i < NUM_TAGS; i++) {
			g.mt[i] = null;
		}
		if (luaD_rawrunprotected(L, f_luaopen, null) != 0) {
			/* memory allocation error: free partial state */
			close_state(L);
			L = null;
		} else {
			luai_userstateopen(L);
		}
		return L;
	}

	public static void callallgcTM(lua_State L, Object ud) {
		// UNUSED(ud);
		luaC_callGCTM(L); // call GC metamethods for all udata
	}

	// static IDelegate callallgcTM = Pfunc.build(Lua.class, "callallgcTM");
	static IDelegate callallgcTM = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			callallgcTM((lua_State) arg1, arg2);
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static void lua_close(lua_State L) {
		L = G(L).mainthread; // only the main thread can be closed
		lua_lock(L);
		luaF_close(L, L.stack[0]); // close all upvalues for this thread
		luaC_separateudata(L, 1); // separate udata that have GC metamethods
		L.errfunc = 0; // no error function during GC metamethods
		do // repeat until no more errors
		{
			L.ci = L.base_ci[0];
			L.base_ = L.top = L.ci.base_;
			L.nCcalls = L.baseCcalls = 0;
		} while (luaD_rawrunprotected(L, callallgcTM, null) != 0);
		lua_assert(G(L).tmudata == null);
		luai_userstateclose(L);
		close_state(L);
	}

	public static int sizestring(TString s) {
		return ((int) s.len + 1) * GetUnmanagedSize(Character.class);
	}

	public static int sizeudata(Udata u) {
		return (int) u.len;
	}

	public static TString luaS_new(lua_State L, CharPtr s) {
		return luaS_newlstr(L, s, (int) strlen(s));
	}

	public static TString luaS_new(lua_State L, String sS) {
		CharPtr s = new CharPtr(sS);
		return luaS_newlstr(L, s, (int) strlen(s));
	}

	public static TString luaS_newliteral(lua_State L, CharPtr s) {
		return luaS_newlstr(L, s, (int) strlen(s));
	}

	public static TString luaS_newliteral(lua_State L, String sS) {
		CharPtr s = new CharPtr(sS);
		return luaS_newlstr(L, s, (int) strlen(s));
	}

	public static void luaS_fix(TString s) {
		byte marked = s.gettsv().marked; // can't pass properties in as ref
		RefObject<Byte> tempRef_marked = new RefObject<Byte>(marked);
		l_setbit(tempRef_marked, FIXEDBIT);
		marked = tempRef_marked.argvalue;
		s.gettsv().SetMarked(marked);
	}

	public static void luaS_resize(lua_State L, int newsize) {
		GCObject[] newhash;
		stringtable tb;
		int i;
		if (G(L).gcstate == GCSsweepstring) {
			return; // cannot resize during GC traverse
		}
		newhash = new GCObject[newsize];
		AddTotalBytes(L, newsize * GetUnmanagedSize(GCObjectRef.class));
		tb = G(L).strt;
		/*
		 * for (i=0; i<newsize; i++) { newhash[i] = null; }
		 */

		/* rehash */
		for (i = 0; i < tb.size; i++) {
			GCObject p = tb.hash[i];
			while (p != null) // for each node in the list
			{
				CharPtr ptr = gco2ts(p).str;
				GCObject next = p.getgch().next; // save next
				// FUCK WARNING: Unsigned integer types have no direct
				// equivalent in Java:
				// ORIGINAL LINE: uint h = gco2ts(p).hash;
				Long h = gco2ts(p).hash;
				int h1 = (int) lmod(h, newsize); // new position
				lua_assert((int) (h % newsize) == lmod(h, newsize));
				p.getgch().SetNext(newhash[h1]); // chain it
				newhash[h1] = p;
				p = next;
			}
		}
		// luaM_freearray(L, tb.hash);
		if (tb.hash != null) {
			SubtractTotalBytes(L, tb.hash.length
					* GetUnmanagedSize(GCObjectRef.class));
		}
		/*
		 * tb.size = newsize; tb.hash = newhash;
		 */
		G(L).strt.hash = newhash;
		G(L).strt.size = newsize;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static TString newlstr (lua_State L, CharPtr str,
	// uint l, uint h)
	public static TString newlstr(lua_State L, CharPtr str, int l, Long h) {
		TString ts;
		stringtable tb;
		if (l + 1 > MAX_SIZET / GetUnmanagedSize(Character.class)) {
			luaM_toobig(L);
		}
		ts = new TString(new char[l + 1]);
		AddTotalBytes(L, (int) (l + 1) * GetUnmanagedSize(Character.class)
				+ GetUnmanagedSize(TString.class));
		ts.gettsv().SetLen(l);
		ts.gettsv().SetHash(h);
		ts.gettsv().SetMarked(luaC_white(G(L)));
		ts.gettsv().SetTT((byte) LUA_TSTRING);
		ts.gettsv().SetReserved((byte) 0);
		// memcpy(ts+1, str, l*GetUnmanagedSize(typeof(char)));
		Lua.cmemcpy(ts.str.chars, str.chars, str.index, (int) l);
		ts.str.setItem(l, '\0'); // ending 0
		tb = G(L).strt;
		int hlmod = (int) lmod(h, tb.size);
		ts.gettsv().SetNext(tb.hash[hlmod]); // chain new entry
		tb.hash[hlmod] = obj2gco(ts);
		tb.nuse++;
		if ((tb.nuse > (int) tb.size) && (tb.size <= MAX_INT / 2)) {
			luaS_resize(L, tb.size * 2); // too crowded
		}
		return ts;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static TString luaS_newlstr (lua_State L, CharPtr
	// str, uint l)
	public static TString luaS_newlstr(lua_State L, CharPtr str, int l) {
		GCObject o;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint h = (uint)l;
		int h = (int)l; // seed
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint step = (l>>5)+1;
		int step = (l >> 5) + 1; // if string is too long, don't hash all its
									// chars
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l1;
		int l1;
		for (l1 = l; l1 >= step; l1 -= step) // compute hash
		{
			/*
			 * long ltest = h ^ ((h<<5)+(h>>2)+(byte)str.getItem(l1-1));
			 * if(ltest > Integer.MAX_VALUE) break; else h = (int)ltest;
			 */
			h = h ^ ((h << 5) + (h >> 2) + (byte) str.getItem(l1 - 1));
		}
		Long hl = h & 0xffffffffL;
		int hlmod = (int) lmod(hl, G(L).strt.size);
		for (o = G(L).strt.hash[hlmod]; o != null; o = o.getgch().next) {
			TString ts = rawgco2ts(o);
			if (ts.gettsv().len == l && (memcmp(str, getstr(ts), l) == 0)) {
				/* string may be dead */
				if (isdead(G(L), o)) {
					changewhite(o);
				}
				return ts;
			}
		}
		// return newlstr(L, str, l, h); /* not found */
		TString res = newlstr(L, str, l, hl);
		return res;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static Udata luaS_newudata(lua_State L, uint s,
	// Table e)
	public static Udata luaS_newudata(lua_State L, int s, Table e) {
		Udata u = new Udata();
		u.uv.marked = luaC_white(G(L)); // is not finalized
		u.uv.tt = LUA_TUSERDATA;
		u.uv.len = s;
		u.uv.metatable = null;
		u.uv.env = e;
		u.user_data = new byte[s];
		/* chain it on udata list (after main thread) */
		u.uv.next = G(L).mainthread.next;
		G(L).mainthread.next = obj2gco(u);
		return u;
	}

	public static Udata luaS_newudata(lua_State L, java.lang.Class t, Table e) {
		Udata u = new Udata();
		u.uv.marked = luaC_white(G(L)); // is not finalized
		u.uv.tt = LUA_TUSERDATA;
		u.uv.len = 0;
		u.uv.metatable = null;
		u.uv.env = e;
		u.user_data = luaM_realloc_(L, t);
		AddTotalBytes(L, GetUnmanagedSize(Udata.class));
		/* chain it on udata list (after main thread) */
		u.uv.next = G(L).mainthread.next;
		G(L).mainthread.next = obj2gco(u);
		return u;
	}

	public static int str_len(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		lua_pushinteger(L, (int) l);
		return 1;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int posrelat (int pos, uint len)
	public static int posrelat(int pos, int len) {
		/* relative string position: negative means back from end */
		if (pos < 0) {
			pos += (int) len + 1;
		}
		return (pos >= 0) ? pos : 0;
	}

	public static int str_sub(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		int start = posrelat(luaL_checkinteger(L, 2), l);
		int end = posrelat(luaL_optinteger(L, 3, -1), l);
		if (start < 1) {
			start = 1;
		}
		if (end > (int) l) {
			end = (int) l;
		}
		if (start <= end) {
			lua_pushlstring(L, CharPtr.OpAddition(s, (start - 1)), (int) (end
					- start + 1));// lua_pushlstring(L, s+start-1,
									// (int)(end-start+1));
		} else {
			lua_pushliteral(L, "");
		}
		return 1;
	}

	public static int str_reverse(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		luaL_Buffer b = new luaL_Buffer();
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		luaL_buffinit(L, b);
		while ((l--) != 0) {
			luaL_addchar(b, s.getItem(l));
		}
		luaL_pushresult(b);
		return 1;
	}

	public static int str_lower(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint i;
		int i;
		luaL_Buffer b = new luaL_Buffer();
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		luaL_buffinit(L, b);
		for (i = 0; i < l; i++) {
			luaL_addchar(b, tolower(s.getItem(i)));
		}
		luaL_pushresult(b);
		return 1;
	}

	public static int str_upper(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint i;
		int i;
		luaL_Buffer b = new luaL_Buffer();
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		luaL_buffinit(L, b);
		for (i = 0; i < l; i++) {
			luaL_addchar(b, toupper(s.getItem(i)));
		}
		luaL_pushresult(b);
		return 1;
	}
	
	public static int str_split(lua_State L) {
		int l = 0;
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		
		CharPtr splitChar = luaL_checkstring(L, 2);
		
		String sS = s.toString();
		String[] arr = sS.split(splitChar.toString());
		lua_newtable( L );
		int i = 0;
        for(String sp : arr)
        {
            lua_pushstring(L, sp);
            lua_rawseti(L, -2, i + 1);
            i++;
        }
        return 1;
	}

	public static int str_rep(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		luaL_Buffer b = new luaL_Buffer();
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		int n = luaL_checkint(L, 2);
		luaL_buffinit(L, b);
		while (n-- > 0) {
			luaL_addlstring(b, s, l);
		}
		luaL_pushresult(b);
		return 1;
	}

	public static int str_byte(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l);
		l = tempRef_l.argvalue;
		int posi = posrelat(luaL_optinteger(L, 2, 1), l);
		int pose = posrelat(luaL_optinteger(L, 3, posi), l);
		int n, i;
		if (posi <= 0) {
			posi = 1;
		}
		if ((int) pose > l) {
			pose = (int) l;
		}
		if (posi > pose) // empty interval; return no values
		{
			return 0;
		}
		n = (int) (pose - posi + 1);
		if (posi + n <= pose) // overflow?
		{
			luaL_error(L, "string slice too long");
		}
		luaL_checkstack(L, n, "string slice too long");
		for (i = 0; i < n; i++) {
			lua_pushinteger(L, (byte) (s.getItem(posi + i - 1)));
		}
		return n;
	}

	public static int str_char(lua_State L) {
		int n = lua_gettop(L); // number of arguments
		int i;
		luaL_Buffer b = new luaL_Buffer();
		luaL_buffinit(L, b);
		for (i = 1; i <= n; i++) {
			int c = luaL_checkint(L, i);
			luaL_argcheck(L, (byte) (c) == c, i, "invalid value");
			luaL_addchar(b, (char) (byte) c);
		}
		luaL_pushresult(b);
		return 1;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int writer (lua_State L, object b, uint
	// size, object B)
	public static int writer(lua_State L, Object b, int size, Object B) {
		if (b.getClass() != CharPtr.class) {
			// FUCK NOTE: The following 'using' block is replaced by its Java
			// equivalent:
			// using (MemoryStream stream = new MemoryStream())
			/*
			 * MemoryStream stream = new MemoryStream(); try {
			 */
			// todo: figure out a way to do this
			/*
			 * BinaryFormatter formatter = new BinaryFormatter();
			 * formatter.Serialize(stream, b); stream.Flush(); byte[] bytes =
			 * stream.GetBuffer(); char[] chars = new char[bytes.Length]; for
			 * (int i = 0; i < bytes.Length; i++) chars[i] = (char)bytes[i]; b =
			 * new CharPtr(chars);
			 */
			/*
			 * } finally { stream.dispose(); }
			 */
		}
		luaL_addlstring((luaL_Buffer) B, (CharPtr) b, size);
		return 0;
	}

	// static IDelegate writer = lua_Writer.build(Lua.class, "writer");
	static IDelegate writer = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			return writer((lua_State) args[0], args[1], (Integer) args[2],
					args[3]);
		}
	};

	public static int str_dump(lua_State L) {
		luaL_Buffer b = new luaL_Buffer();
		luaL_checktype(L, 1, LUA_TFUNCTION);
		lua_settop(L, 1);
		luaL_buffinit(L, b);
		if (lua_dump(L, writer, b) != 0) {
			luaL_error(L, "unable to dump given function");
		}
		luaL_pushresult(b);
		return 1;
	}

	/*
	 * * {======================================================* PATTERN
	 * MATCHING* =======================================================
	 */

	public static final int CAP_UNFINISHED = (-1);
	public static final int CAP_POSITION = (-2);

	public static class MatchState {

		public MatchState() {
			for (int i = 0; i < LUA_MAXCAPTURES; i++) {
				capture[i] = new capture_();
			}
		}

		public CharPtr src_init; // init of source string
		public CharPtr src_end; // end (`\0') of source string
		public lua_State L;
		public int level; // total number of captures (finished or unfinished)

		public static class capture_ {
			public CharPtr init;
			public int len;
		}

		public capture_[] capture = new capture_[LUA_MAXCAPTURES];
	}

	public static final char L_ESC = '%';
	public static final String SPECIALS = "^$*+?.([%-";

	public static int check_capture(MatchState ms, int l) {
		l -= '1';
		if (l < 0 || l >= ms.level || ms.capture[l].len == CAP_UNFINISHED) {
			return luaL_error(ms.L, "invalid capture index");
		}
		return l;
	}

	public static int capture_to_close(MatchState ms) {
		int level = ms.level;
		for (level--; level >= 0; level--) {
			if (ms.capture[level].len == CAP_UNFINISHED) {
				return level;
			}
		}
		return luaL_error(ms.L, "invalid pattern capture");
	}

	public static CharPtr classend(MatchState ms, CharPtr p) {
		p = new CharPtr(p);
		char c = p.getItem(0);
		p = p.next();
		switch (c) {
		case L_ESC: {
			if (p.getItem(0) == '\0') {
				luaL_error(ms.L, "malformed pattern (ends with " + LUA_QL("%%")
						+ ")");
			}
			return CharPtr.OpAddition(p, 1);// return p+1;
		}
		case '[': {
			if (p.getItem(0) == '^') {
				p = p.next();
			}
			do // look for a `]'
			{
				if (p.getItem(0) == '\0') {
					luaL_error(ms.L, "malformed pattern (missing "
							+ LUA_QL("]") + ")");
				}
				c = p.getItem(0);
				p = p.next();
				if (c == L_ESC && p.getItem(0) != '\0') {
					p = p.next(); // skip escapes (e.g. `%]')
				}
			} while (p.getItem(0) != ']');
			return CharPtr.OpAddition(p, 1);// return p+1;
		}
		default: {
			return p;
		}
		}
	}

	public static int match_class(int c, int cl) {
		boolean res;
		switch (tolower(cl)) {
		case 'a':
			res = isalpha(c);
			break;
		case 'c':
			res = iscntrl(c);
			break;
		case 'd':
			res = isdigit(c);
			break;
		case 'l':
			res = islower(c);
			break;
		case 'p':
			res = ispunct(c);
			break;
		case 's':
			res = isspace(c);
			break;
		case 'u':
			res = isupper(c);
			break;
		case 'w':
			res = isalnum(c);
			break;
		case 'x':
			res = isxdigit((char) c);
			break;
		case 'z':
			res = (c == 0);
			break;
		default:
			return (cl == c) ? 1 : 0;
		}
		return (islower(cl) ? (res ? 1 : 0) : ((!res) ? 1 : 0));
	}

	public static int matchbracketclass(int c, CharPtr p, CharPtr ec) {
		int sig = 1;
		if (p.getItem(1) == '^') {
			sig = 0;
			p = p.next(); // skip the `^'
		}
		while (CharPtr.OpLessThan((p = p.next()), ec))// while ((p=p.next()) <
														// ec)
		{
			if (p.getItem() == L_ESC) {
				p = p.next();
				if (match_class(c, (byte) (p.getItem(0))) != 0) {
					return sig;
				}
			} else if ((p.getItem(1) == '-')
					&& (CharPtr.OpLessThan(CharPtr.OpAddition(p, 2), ec)))// else
																			// if
																			// ((p.getItem(1)
																			// ==
																			// '-')
																			// &&
																			// (p
																			// +
																			// 2
																			// <
																			// ec))
			{
				p = CharPtr.OpAddition(p, 2);// p+=2;
				if ((byte) ((p.getItem(-2))) <= c && (c <= (byte) p.getItem(0))) {
					return sig;
				}
			} else if ((byte) (p.getItem(0)) == c) {
				return sig;
			}
		}
		return (sig == 0) ? 1 : 0;
	}

	public static int singlematch(int c, CharPtr p, CharPtr ep) {
		switch (p.getItem(0)) {
		case '.': // matches any char
			return 1;
		case L_ESC:
			return match_class(c, (byte) (p.getItem(1)));
		case '[':
			return matchbracketclass(c, p, CharPtr.OpSubtraction(ep, 1));// return
																			// matchbracketclass(c,
																			// p,
																			// ep-1);
		default:
			return ((byte) (p.getItem(0)) == c) ? 1 : 0;
		}
	}

	public static CharPtr matchbalance(MatchState ms, CharPtr s, CharPtr p) {
		if ((p.getItem(0) == 0) || (p.getItem(1) == 0)) {
			luaL_error(ms.L, "unbalanced pattern");
		}
		if (s.getItem(0) != p.getItem(0)) {
			return null;
		} else {
			int b = p.getItem(0);
			int e = p.getItem(1);
			int cont = 1;
			while (CharPtr.OpLessThan((s = s.next()), ms.src_end))// while
																	// ((s=s.next())
																	// <
																	// ms.src_end)
			{
				if (s.getItem(0) == e) {
					if (--cont == 0) {
						return CharPtr.OpAddition(s, 1);// return s+1;
					}
				} else if (s.getItem(0) == b) {
					cont++;
				}
			}
		}
		return null; // string ends out of balance
	}

	public static CharPtr max_expand(MatchState ms, CharPtr s, CharPtr p,
			CharPtr ep) {
		int i = 0; // counts maximum expand for item
		while ((CharPtr.OpLessThan(CharPtr.OpAddition(s, i), ms.src_end))
				&& (singlematch((byte) (s.getItem(i)), p, ep) != 0))// while
																	// ((s+i <
																	// ms.src_end)
																	// &&
																	// (singlematch((byte)(s.getItem(i)),
																	// p, ep) !=
																	// 0))
		{
			i++;
		}
		/* keeps trying to match with the maximum repetitions */
		while (i >= 0) {
			CharPtr res = match(ms, (CharPtr.OpAddition(s, i)),
					CharPtr.OpAddition(ep, 1));// CharPtr res = match(ms, (s+i),
												// ep+1);
			if (res != null) {
				return res;
			}
			i--; // else didn't match; reduce 1 repetition to try again
		}
		return null;
	}

	public static CharPtr min_expand(MatchState ms, CharPtr s, CharPtr p,
			CharPtr ep) {
		for (;;) {
			CharPtr res = match(ms, s, CharPtr.OpAddition(ep, 1));// CharPtr res
																	// =
																	// match(ms,
																	// s, ep+1);
			if (res != null) {
				return res;
			} else if ((CharPtr.OpLessThan(s, ms.src_end))
					&& (singlematch((byte) (s.getItem(0)), p, ep) != 0))// else
																		// if
																		// ((s <
																		// ms.src_end)
																		// &&
																		// (singlematch((byte)(s.getItem(0)),
																		// p,
																		// ep)
																		// !=
																		// 0))
			{
				s = s.next(); // try with one more repetition
			} else {
				return null;
			}
		}
	}

	public static CharPtr start_capture(MatchState ms, CharPtr s, CharPtr p,
			int what) {
		CharPtr res;
		int level = ms.level;
		if (level >= LUA_MAXCAPTURES) {
			luaL_error(ms.L, "too many captures");
		}
		ms.capture[level].init = s;
		ms.capture[level].len = what;
		ms.level = level + 1;
		if ((res = match(ms, s, p)) == null) // match failed?
		{
			ms.level--; // undo capture
		}
		return res;
	}

	public static CharPtr end_capture(MatchState ms, CharPtr s, CharPtr p) {
		int l = capture_to_close(ms);
		CharPtr res;
		ms.capture[l].len = CharPtr.OpSubtraction(s, ms.capture[l].init);// ms.capture[l].len
																			// =
																			// s
																			// -
																			// ms.capture[l].init;
																			// //
																			// close
																			// capture
		if ((res = match(ms, s, p)) == null) // match failed?
		{
			ms.capture[l].len = CAP_UNFINISHED; // undo capture
		}
		return res;
	}

	public static CharPtr match_capture(MatchState ms, CharPtr s, int l) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint len;
		int len;
		l = check_capture(ms, l);
		len = (int) ms.capture[l].len;
		if ((int) (CharPtr.OpSubtraction(ms.src_end, s)) >= len
				&& memcmp(ms.capture[l].init, s, len) == 0)// if
															// ((int)(ms.src_end-s)
															// >= len &&
															// memcmp(ms.capture[l].init,
															// s, len) == 0)
		{
			return CharPtr.OpAddition(s, len);// return s+len;
		} else {
			return null;
		}
	}

	public static CharPtr match(MatchState ms, CharPtr s, CharPtr p) {
		s = new CharPtr(s);
		p = new CharPtr(p);
		boolean isContinue = true;
		boolean isDefault = false;
		while (isContinue) {
			isContinue = false;
			isDefault = false;
			switch (p.getItem(0)) {
			case '(': // start capture
			{
				if (p.getItem(1) == ')') // position capture?
				{
					return start_capture(ms, s, CharPtr.OpAddition(p, 2),
							CAP_POSITION);// return start_capture(ms, s, p+2,
											// CAP_POSITION);
				} else {
					return start_capture(ms, s, CharPtr.OpAddition(p, 1),
							CAP_UNFINISHED);// return start_capture(ms, s, p+1,
											// CAP_UNFINISHED);
				}
			}
			case ')': // end capture
			{
				return end_capture(ms, s, CharPtr.OpAddition(p, 1));// return
																	// end_capture(ms,
																	// s, p+1);
			}
			case L_ESC: {
				switch (p.getItem(1)) {
				case 'b': // balanced string?
				{
					s = matchbalance(ms, s, CharPtr.OpAddition(p, 2));// s =
																		// matchbalance(ms,
																		// s,
																		// p+2);
					if (s == null) {
						return null;
					}
					p = CharPtr.OpAddition(p, 4);// p+=4; // else return
													// match(ms, s, p+4);
					// FUCK TODO TASK: There is no 'goto' in Java:
					// goto init;
					isContinue = true;
					continue;
				}
				case 'f': // frontier?
				{
					CharPtr ep;
					char previous;
					p = CharPtr.OpAddition(p, 2);// p += 2;
					if (p.getItem(0) != '[') {
						luaL_error(ms.L, "missing " + LUA_QL("[") + " after "
								+ LUA_QL("%%f") + " in pattern");
					}
					ep = classend(ms, p); // points to what is next
					previous = (s == ms.src_init) ? '\0' : s.getItem(-1);
					if ((matchbracketclass((byte) (previous), p,
							CharPtr.OpSubtraction(ep, 1)) != 0)
							|| (matchbracketclass((byte) (s.getItem(0)), p,
									CharPtr.OpSubtraction(ep, 1)) == 0))// if
																		// ((matchbracketclass((byte)(previous),
																		// p,
																		// ep-1)!=0)
																		// ||
																		// (matchbracketclass((byte)(s.getItem(0)),
																		// p,
																		// ep-1)==0))
					{
						return null;
					}
					p = ep; // else return match(ms, s, ep);
					// FUCK TODO TASK: There is no 'goto' in Java:
					// goto init;
					isContinue = true;
					continue;
				}
				default: {
					if (isdigit((byte) (p.getItem(1)))) // capture results
														// (%0-%9)?
					{
						s = match_capture(ms, s, (byte) (p.getItem(1)));
						if (s == null) {
							return null;
						}
						p = CharPtr.OpAddition(p, 2);// p+=2; // else return
														// match(ms, s, p+2)
						// FUCK TODO TASK: There is no 'goto' in Java:
						// goto init;
						isContinue = true;
						continue;
					}
					// FUCK TODO TASK: There is no 'goto' in Java:
					// goto dflt; // case default
					isDefault = true;
				}
				}
				break;
			}
			case '\0': // end of pattern
			{
				return s; // match succeeded
			}
			case '$': {
				if (p.getItem(1) == '\0') // is the `$' the last char in
											// pattern?
				{
					return (CharPtr.OpEquality(s, ms.src_end)) ? s : null; // check end of string
				} else {
					// FUCK TODO TASK: There is no 'goto' in Java:
					// goto dflt;
					isDefault = true;
				}
			}
			default: {
				isDefault = true;
			}
			}
			;

			if (isDefault) {
				isDefault = false;

				CharPtr ep = classend(ms, p); // points to what is next
				int m = (CharPtr.OpLessThan(s, ms.src_end)
						&& (singlematch((byte) (s.getItem(0)), p, ep) != 0) ? 1
						: 0);// int m = (s<ms.src_end) &&
								// (singlematch((byte)(s.getItem(0)), p, ep)!=0)
								// ? 1 : 0;
				switch (ep.getItem(0)) {
				case '?': // optional
				{
					CharPtr res;
					if ((m != 0)
							&& ((res = match(ms, CharPtr.OpAddition(s, 1),
									CharPtr.OpAddition(ep, 1))) != null))// if
																			// ((m!=0)
																			// &&
																			// ((res=match(ms,
																			// s+1,
																			// ep+1))
																			// !=
																			// null))
					{
						return res;
					}
					p = CharPtr.OpAddition(ep, 1);// p=ep+1; // else return
													// match(ms, s, ep+1);
					// FUCK TODO TASK: There is no 'goto' in Java:
					// goto init;
					isContinue = true;
					continue;
				}
				case '*': // 0 or more repetitions
				{
					return max_expand(ms, s, p, ep);
				}
				case '+': // 1 or more repetitions
				{
					return ((m != 0) ? max_expand(ms, CharPtr.OpAddition(s, 1),
							p, ep) : null);// return ((m!=0) ? max_expand(ms,
											// s+1, p, ep) : null);
				}
				case '-': // 0 or more repetitions (minimum)
				{
					return min_expand(ms, s, p, ep);
				}
				default: {
					if (m == 0) {
						return null;
					}
					s = s.next(); // else return match(ms, s+1, ep);
					p = ep;
					// FUCK TODO TASK: There is no 'goto' in Java:
					// goto init;
					isContinue = true;
					continue;
				}
				}
			}
		}

		return null;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static CharPtr lmemfind (CharPtr s1, uint l1,
	// CharPtr s2, uint l2)
	public static CharPtr lmemfind(CharPtr s1, int l1, CharPtr s2, int l2) {
		if (l2 == 0) // empty strings are everywhere
		{
			return s1;
		} else if (l2 > l1) // avoids a negative `l1'
		{
			return null;
		} else {
			CharPtr init; // to search for a `*s2' inside `s1'
			l2--; // 1st char will be checked by `memchr'
			l1 = l1 - l2; // `s2' cannot be found after that
			while (l1 > 0 && (init = memchr(s1, s2.getItem(0), l1)) != null) {
				init = init.next(); // 1st char is already checked
				if (memcmp(init, CharPtr.OpAddition(s2, 1), l2) == 0)// if
																		// (memcmp(init,
																		// s2+1,
																		// l2)
																		// == 0)
				{
					return CharPtr.OpSubtraction(init, 1);// return init-1;
				} else // correct `l1' and `s1' to try again
				{
					l1 -= (int) (CharPtr.OpSubtraction(init, s1));// l1 -=
																	// (int)(init-s1);
					s1 = init;
				}
			}
			return null; // not found
		}
	}

	public static void push_onecapture(MatchState ms, int i, CharPtr s,
			CharPtr e) {
		if (i >= ms.level) {
			if (i == 0) // ms.level == 0, too
			{
				lua_pushlstring(ms.L, s, (int) (CharPtr.OpSubtraction(e, s)));// lua_pushlstring(ms.L,
																				// s,
																				// (int)(e
																				// -
																				// s));
																				// //
																				// add
																				// whole
																				// match
			} else {
				luaL_error(ms.L, "invalid capture index");
			}
		} else {
			int l = ms.capture[i].len;
			if (l == CAP_UNFINISHED) {
				luaL_error(ms.L, "unfinished capture");
			}
			if (l == CAP_POSITION) {
				lua_pushinteger(
						ms.L,
						CharPtr.OpSubtraction(ms.capture[i].init, ms.src_init) + 1);// lua_pushinteger(ms.L,
																					// ms.capture[i].init
																					// -
																					// ms.src_init
																					// +
																					// 1);
			} else {
				lua_pushlstring(ms.L, ms.capture[i].init, (int) l);
			}
		}
	}

	public static int push_captures(MatchState ms, CharPtr s, CharPtr e) {
		int i;
		int nlevels = ((ms.level == 0) && (s != null)) ? 1 : ms.level;
		luaL_checkstack(ms.L, nlevels, "too many captures");
		for (i = 0; i < nlevels; i++) {
			push_onecapture(ms, i, s, e);
		}
		return nlevels; // number of strings pushed
	}

	public static int str_find_aux(lua_State L, int find) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l1, l2;
		int l1 = 0, l2 = 0;
		RefObject<Integer> tempRef_l1 = new RefObject<Integer>(l1);
		CharPtr s = luaL_checklstring(L, 1, tempRef_l1);
		l1 = tempRef_l1.argvalue;
		RefObject<Integer> tempRef_l2 = new RefObject<Integer>(l2);
		CharPtr p = luaL_checklstring(L, 2, tempRef_l2);
		l2 = tempRef_l2.argvalue;
		int init = posrelat(luaL_optinteger(L, 3, 1), l1) - 1;
		if (init < 0) {
			init = 0;
		} else if ((int) (init) > l1) {
			init = (int) l1;
		}
		if ((find != 0)
				&& ((lua_toboolean(L, 4) != 0) || strpbrk(p, SPECIALS) == null)) // or
																					// no
																					// special
																					// characters?
																					// -
																					// explicit
																					// request?
		{
			/* do a plain search */
			CharPtr s2 = lmemfind(CharPtr.OpAddition(s, init),
					(int) (l1 - init), p, (int) (l2));// CharPtr s2 =
														// lmemfind(s+init,
														// (int)(l1-init), p,
														// (int)(l2));
			if (s2 != null) {
				lua_pushinteger(L, CharPtr.OpSubtraction(s2, s) + 1);// lua_pushinteger(L,
																		// s2-s+1);
				lua_pushinteger(L, CharPtr.OpSubtraction(s2, s) + l2);// lua_pushinteger(L,
																		// (int)(s2-s+l2));
				return 2;
			}
		} else {
			MatchState ms = new MatchState();
			int anchor = 0;
			if (p.getItem(0) == '^') {
				p = p.next();
				anchor = 1;
			}
			CharPtr s1 = CharPtr.OpAddition(s, init);// CharPtr s1=s+init;
			ms.L = L;
			ms.src_init = s;
			ms.src_end = CharPtr.OpAddition(s, l1);// ms.src_end = s+l1;
			do {
				CharPtr res;
				ms.level = 0;
				if ((res = match(ms, s1, p)) != null) {
					if (find != 0) {
						lua_pushinteger(L, CharPtr.OpSubtraction(s1, s) + 1);// lua_pushinteger(L,
																				// s1-s+1);
																				// //
																				// start
						lua_pushinteger(L, CharPtr.OpSubtraction(res, s));// lua_pushinteger(L,
																			// res-s);
																			// //
																			// end
						return push_captures(ms, null, null) + 2;
					} else {
						return push_captures(ms, s1, res);
					}
				}
			} while ((CharPtr.OpLessThanOrEqual(s1 = s1.next(), ms.src_end))
					&& (anchor == 0));// } while (((s1=s1.next()) <= ms.src_end)
										// && (anchor==0));
		}
		lua_pushnil(L); // not found
		return 1;
	}

	public static int str_find(lua_State L) {
		return str_find_aux(L, 1);
	}

	public static int str_match(lua_State L) {
		return str_find_aux(L, 0);
	}

	public static int gmatch_aux(lua_State L) {
		MatchState ms = new MatchState();
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint ls;
		int ls = 0;
		RefObject<Integer> tempRef_ls = new RefObject<Integer>(ls);
		CharPtr s = lua_tolstring(L, lua_upvalueindex(1), tempRef_ls);
		ls = tempRef_ls.argvalue;
		CharPtr p = lua_tostring(L, lua_upvalueindex(2));
		CharPtr src;
		ms.L = L;
		ms.src_init = s;
		ms.src_end = CharPtr.OpAddition(s, ls);// ms.src_end = s+ls;
		for (src = CharPtr.OpAddition(s,
				(int) lua_tointeger(L, lua_upvalueindex(3))); CharPtr
				.OpLessThanOrEqual(src, ms.src_end); src = src.next())// for
																		// (src
																		// = s +
																		// (int)lua_tointeger(L,
																		// lua_upvalueindex(3));
																		// src
																		// <=
																		// ms.src_end;
																		// src =
																		// src.next())
		{
			CharPtr e;
			ms.level = 0;
			if ((e = match(ms, src, p)) != null) {
				int newstart = CharPtr.OpSubtraction(e, s);// int newstart =
															// e-s;
				if (e == src) // empty match? go at least one position
				{
					newstart++;
				}
				lua_pushinteger(L, newstart);
				lua_replace(L, lua_upvalueindex(3));
				return push_captures(ms, src, e);
			}
		}
		return 0; // not found
	}

	// static IDelegate gmatch_aux = lua_CFunction.build(Lua.class,
	// "gmatch_aux");
	static IDelegate gmatch_aux = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg) {
			return gmatch_aux((lua_State) arg);
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static int gmatch(lua_State L) {
		luaL_checkstring(L, 1);
		luaL_checkstring(L, 2);
		lua_settop(L, 2);
		lua_pushinteger(L, 0);
		lua_pushcclosure(L, gmatch_aux, 3);
		return 1;
	}

	public static int gfind_nodef(lua_State L) {
		return luaL_error(L, LUA_QL("string.gfind") + " was renamed to "
				+ LUA_QL("string.gmatch"));
	}

	public static void add_s(MatchState ms, luaL_Buffer b, CharPtr s, CharPtr e) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l, i;
		int l = 0, i = 0;
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr news = lua_tolstring(ms.L, 3, tempRef_l);
		if(news.index != 0)
			news.index = 0;
		l = tempRef_l.argvalue;
		for (i = 0; i < l; i++) {
			if (news.getItem(i) != L_ESC) {
				luaL_addchar(b, news.getItem(i));
			} else {
				i++; // skip ESC
				if (!isdigit((byte) (news.getItem(i)))) {
					luaL_addchar(b, news.getItem(i));
				} else if (news.getItem(i) == '0') {
					luaL_addlstring(b, s, (int) (CharPtr.OpSubtraction(e, s)));// luaL_addlstring(b,
																				// s,
																				// (int)(e
																				// -
																				// s));
				} else {
					push_onecapture(ms, news.getItem(i) - '1', s, e);
					luaL_addvalue(b); // add capture to accumulated result
				}
			}
		}
	}

	public static void add_value(MatchState ms, luaL_Buffer b, CharPtr s,
			CharPtr e) {
		lua_State L = ms.L;
		switch (lua_type(L, 3)) {
		case LUA_TNUMBER:
		case LUA_TSTRING: {
			add_s(ms, b, s, e);
			return;
		}
		case LUA_TFUNCTION: {
			int n;
			lua_pushvalue(L, 3);
			n = push_captures(ms, s, e);
			lua_call(L, n, 1);
			break;
		}
		case LUA_TTABLE: {
			push_onecapture(ms, 0, s, e);
			lua_gettable(L, 3);
			break;
		}
		}
		if (lua_toboolean(L, -1) == 0) // nil or false?
		{
			lua_pop(L, 1);
			lua_pushlstring(L, s, (int) (CharPtr.OpSubtraction(e, s)));// lua_pushlstring(L,
																		// s,
																		// (int)(e
																		// -
																		// s));
																		// //
																		// keep
																		// original
																		// text
		} else if (lua_isstring(L, -1) == 0) {
			luaL_error(L, "invalid replacement value (a %s)",
					luaL_typename(L, -1));
		}
		luaL_addvalue(b); // add result to accumulator
	}

	public static int str_gsub(lua_State L) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint srcl;
		int srcl = 0;
		RefObject<Integer> tempRef_srcl = new RefObject<Integer>(srcl);
		CharPtr src = luaL_checklstring(L, 1, tempRef_srcl);
		srcl = tempRef_srcl.argvalue;
		CharPtr p = luaL_checkstring(L, 2);
		int tr = lua_type(L, 3);
		int max_s = luaL_optint(L, 4, (int) (srcl + 1));
		int anchor = 0;
		if (p.getItem(0) == '^') {
			p = p.next();
			anchor = 1;
		}
		int n = 0;
		MatchState ms = new MatchState();
		luaL_Buffer b = new luaL_Buffer();
		luaL_argcheck(L, tr == LUA_TNUMBER || tr == LUA_TSTRING
				|| tr == LUA_TFUNCTION || tr == LUA_TTABLE, 3,
				"string/function/table expected");
		luaL_buffinit(L, b);
		ms.L = L;
		ms.src_init = src;
		ms.src_end = CharPtr.OpAddition(src, srcl);// ms.src_end = src+srcl;
		while (n < max_s) {
			CharPtr e;
			ms.level = 0;
			e = match(ms, src, p);
			if (e != null) {
				n++;
				add_value(ms, b, src, e);
			}
			if ((e != null) && CharPtr.OpGreaterThan(e, src))// if ((e!=null) &&
																// e>src) // non
																// empty match?
			{
				src = e; // skip it
			} else if (CharPtr.OpLessThan(src, ms.src_end))// else if (src <
															// ms.src_end)
			{
				char c = src.getItem(0);
				src = src.next();
				luaL_addchar(b, c);
			} else {
				break;
			}
			if (anchor != 0) {
				break;
			}
		}
		luaL_addlstring(b, src, (int) (CharPtr.OpSubtraction(ms.src_end, src)));// luaL_addlstring(b,
																				// src,
																				// (int)(ms.src_end-src));
		luaL_pushresult(b);
		lua_pushinteger(L, n); // number of substitutions
		return 2;
	}

	/* }====================================================== */

	/* maximum size of each formatted item (> len(format('%99.99f', -1e308))) */
	public static final int MAX_ITEM = 512;
	/* valid flags in a format specification */
	public static final String FLAGS = "-+ #0";
	/*
	 * * maximum size of each format specification (such as '%-099.99d')* (+10
	 * accounts for %99.99x plus margin of error)
	 */
	public static final int MAX_FORMAT = (FLAGS.length() + 1)
			+ (LUA_INTFRMLEN.length() + 1) + 10;

	public static void addquoted(lua_State L, luaL_Buffer b, int arg) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l;
		int l = 0;
		RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
		CharPtr s = luaL_checklstring(L, arg, tempRef_l);
		l = tempRef_l.argvalue;
		luaL_addchar(b, '"');
		while ((l--) != 0) {
			switch (s.getItem(0)) {
			case '"':
			case '\\':
			case '\n': {
				luaL_addchar(b, '\\');
				luaL_addchar(b, s.getItem(0));
				break;
			}
			case '\r': {
				luaL_addlstring(b, "\\r", 2);
				break;
			}
			case '\0': {
				luaL_addlstring(b, "\\000", 4);
				break;
			}
			default: {
				luaL_addchar(b, s.getItem(0));
				break;
			}
			}
			s = s.next();
		}
		luaL_addchar(b, '"');
	}

	public static CharPtr scanformat(lua_State L, CharPtr strfrmt, CharPtr form) {
		CharPtr p = strfrmt;
		while (p.getItem(0) != '\0' && strchr(FLAGS, p.getItem(0)) != null) // skip
																			// flags
		{
			p = p.next();
		}
		if ((int) (CharPtr.OpSubtraction(p, strfrmt)) >= (FLAGS.length() + 1))// if
																				// ((int)(p
																				// -
																				// strfrmt)
																				// >=
																				// (FLAGS.length()+1))
		{
			luaL_error(L, "invalid format (repeated flags)");
		}
		if (isdigit((byte) (p.getItem(0)))) // skip width
		{
			p = p.next();
		}
		if (isdigit((byte) (p.getItem(0)))) // (2 digits at most)
		{
			p = p.next();
		}
		if (p.getItem(0) == '.') {
			p = p.next();
			if (isdigit((byte) (p.getItem(0)))) // skip precision
			{
				p = p.next();
			}
			if (isdigit((byte) (p.getItem(0)))) // (2 digits at most)
			{
				p = p.next();
			}
		}
		if (isdigit((byte) (p.getItem(0)))) {
			luaL_error(L, "invalid format (width or precision too long)");
		}
		form.setItem(0, '%');
		form = form.next();
		strncpy(form, strfrmt, CharPtr.OpSubtraction(p, strfrmt) + 1);// strncpy(form,
																		// strfrmt,
																		// p -
																		// strfrmt
																		// + 1);
		form = CharPtr.OpAddition(form, CharPtr.OpSubtraction(p, strfrmt) + 1);// form
																				// +=
																				// p
																				// -
																				// strfrmt
																				// +
																				// 1;
		form.setItem(0, '\0');
		return p;
	}

	public static void addintlen(CharPtr form) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint l = (uint)strlen(form);
		int l = (int) strlen(form);
		char spec = form.getItem(l - 1);
		strcpy(CharPtr.OpAddition(form, (l - 1)), LUA_INTFRMLEN);// strcpy(form
																	// + l - 1,
																	// LUA_INTFRMLEN);
		form.setItem(l + (LUA_INTFRMLEN.length() + 1) - 2, spec);
		form.setItem(l + (LUA_INTFRMLEN.length() + 1) - 1, '\0');
	}

	public static int str_format(lua_State L) {
		int arg = 1;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint sfl;
		int sfl = 0;
		RefObject<Integer> tempRef_sfl = new RefObject<Integer>(sfl);
		CharPtr strfrmt = luaL_checklstring(L, arg, tempRef_sfl);
		sfl = tempRef_sfl.argvalue;
		CharPtr strfrmt_end = CharPtr.OpAddition(strfrmt, sfl);// CharPtr
																// strfrmt_end =
																// strfrmt+sfl;
		luaL_Buffer b = new luaL_Buffer();
		luaL_buffinit(L, b);
		while (CharPtr.OpLessThan(strfrmt, strfrmt_end))// while (strfrmt <
														// strfrmt_end)
		{
			if (strfrmt.getItem(0) != L_ESC) {
				luaL_addchar(b, strfrmt.getItem(0));
				strfrmt = strfrmt.next();
			} else if (strfrmt.getItem(1) == L_ESC) {
				luaL_addchar(b, strfrmt.getItem(0)); // %%
				strfrmt = CharPtr.OpAddition(strfrmt, 2);// strfrmt = strfrmt +
															// 2;
			} else { // format item
				strfrmt = strfrmt.next();
				CharPtr form = new CharPtr(new char[MAX_FORMAT]); // to store
																	// the
																	// format
																	// (`%...')
				CharPtr buff = new CharPtr(new char[MAX_ITEM]); // to store the
																// formatted
																// item
				arg++;
				strfrmt = scanformat(L, strfrmt, form);
				char ch = strfrmt.getItem(0);
				strfrmt = strfrmt.next();
				switch (ch) {
				case 'c': {
					sprintf(buff, form, (int) luaL_checknumber(L, arg));
					break;
				}
				case 'd':
				case 'i': {
					addintlen(form);
					sprintf(buff, form, (long) luaL_checknumber(L, arg));
					break;
				}
				case 'o':
				case 'u':
				case 'x':
				case 'X': {
					addintlen(form);
					sprintf(buff, form, (long) luaL_checknumber(L, arg));
					break;
				}
				case 'e':
				case 'E':
				case 'f':
				case 'g':
				case 'G': {
					sprintf(buff, form, (double) luaL_checknumber(L, arg));
					break;
				}
				case 'q': {
					addquoted(L, b, arg);
					continue; // skip the 'addsize' at the end
				}
				case 's': {
					// FUCK WARNING: Unsigned integer types have no direct
					// equivalent in Java:
					// ORIGINAL LINE: uint l;
					int l = 0;
					RefObject<Integer> tempRef_l = new RefObject<Integer>(l);
					CharPtr s = luaL_checklstring(L, arg, tempRef_l);
					l = tempRef_l.argvalue;
					if ((strchr(form, '.') == null) && l >= 100) {
						/*
						 * no precision and string is too long to be formatted;
						 * keep original string
						 */
						lua_pushvalue(L, arg);
						luaL_addvalue(b);
						continue; // skip the `addsize' at the end
					} else {
						sprintf(buff, form, s);
						break;
					}
				}
				default: { // also treat cases `pnLlh'
					return luaL_error(L, "invalid option " + LUA_QL("%%%c")
							+ " to " + LUA_QL("format"), strfrmt.getItem(-1));
				}
				}
				luaL_addlstring(b, buff, (int) strlen(buff));
			}
		}
		luaL_pushresult(b);
		return 1;
	}

	private final static luaL_Reg[] strlib = {
			new luaL_Reg("byte", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_byte((lua_State)arg);
				}
			}), 
			new luaL_Reg("char", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_char((lua_State)arg);
				}
			}),
			new luaL_Reg("dump", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_dump((lua_State)arg);
				}
			}), 
			new luaL_Reg("find", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_find((lua_State)arg);
				}
			}),
			new luaL_Reg("format", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_format((lua_State)arg);
				}
			}),
			new luaL_Reg("gfind", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return gfind_nodef((lua_State)arg);
				}
			}),
			new luaL_Reg("gmatch", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return gmatch((lua_State)arg);
				}
			}), 
			new luaL_Reg("gsub", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_gsub((lua_State)arg);
				}
			}),
			new luaL_Reg("len", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_len((lua_State)arg);
				}
			}), 
			new luaL_Reg("lower", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_lower((lua_State)arg);
				}
			}),
			new luaL_Reg("match", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_match((lua_State)arg);
				}
			}), 
			new luaL_Reg("rep", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_rep((lua_State)arg);
				}
			}),
			new luaL_Reg("reverse", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_reverse((lua_State)arg);
				}
			}),
			new luaL_Reg("sub", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_sub((lua_State)arg);
				}
			}), 
			new luaL_Reg("upper", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return str_upper((lua_State)arg);
				}
			}),
			new luaL_Reg("split", new OneDelegate()
			{
				@Override
				public Object invoke(Object arg)
				{
					return str_split((lua_State)arg);
				}
			}),
			new luaL_Reg((String)null, (String)null) };

	public static void createmetatable(lua_State L) {
		lua_createtable(L, 0, 1); // create metatable for strings
		lua_pushliteral(L, ""); // dummy string
		lua_pushvalue(L, -2);
		lua_setmetatable(L, -2); // set string metatable
		lua_pop(L, 1); // pop dummy string
		lua_pushvalue(L, -2); // string library...
		lua_setfield(L, -2, "__index"); // ...is the __index metamethod
		lua_pop(L, 1); // pop metatable
	}

	/*
	 * * Open string library
	 */
	public static int luaopen_string(lua_State L) {
		luaL_register(L, LUA_STRLIBNAME, strlib);
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if LUA_COMPAT_GFIND
		lua_getfield(L, -1, "gmatch");
		lua_setfield(L, -2, "gfind");
		// #endif
		createmetatable(L);
		return 1;
	}

	/*
	 * * Implementation of tables (aka arrays, objects, or hash tables).* Tables
	 * keep its elements in two parts: an array part and a hash part.*
	 * Non-negative integer keys are all candidates to be kept in the array*
	 * part. The actual size of the array is the largest `n' such that at* least
	 * half the slots between 0 and n are in use.* Hash uses a mix of chained
	 * scatter table with Brent's variation.* A main invariant of these tables
	 * is that, if an element is not* in its main position (i.e. the `original'
	 * position that its hash gives* to it), then the colliding element is in
	 * its own main position.* Hence even when the load factor reaches 100%,
	 * performance remains good.
	 */

	public static Node gnode(Table t, int i) {
		return t.node[i];
	}

	public static TKey_nk gkey(Node n) {
		return n.i_key.nk;
	}

	public static lua_TValue gval(Node n) {
		return n.i_val;
	}

	public static Node gnext(Node n) {
		return n.i_key.nk.next;
	}

	public static void gnext_set(Node n, Node v) {
		n.i_key.nk.next = v;
	}

	public static lua_TValue key2tval(Node n) {
		return n.i_key.gettvk();
	}

	/*
	 * * max size of array part is 2^MAXBITS
	 */
	// /#if LUAI_BITSINT > 26
	public static final int MAXBITS = 26; // in the dotnet port LUAI_BITSINT is
											// 32
	// /#else
	// public const int MAXBITS = (LUAI_BITSINT-2);
	// /#endif

	public static final int MAXASIZE = (1 << MAXBITS);

	// public static Node gnode(Table t, int i) {return t.node[i];}
	public static Node hashpow2(Table t, double n) {
		return gnode(t, (int) lmod(n, sizenode(t)));
	}

	public static Node hashstr(Table t, TString str) {
		return hashpow2(t, str.gettsv().hash);
	}

	public static Node hashboolean(Table t, int p) {
		return hashpow2(t, p);
	}

	/*
	 * * for some types, it is better to avoid modulus by power of 2, as* they
	 * tend to have many 2 factors.
	 */
	public static Node hashmod(Table t, int n) {
		int mod = ((sizenode(t) - 1) | 1);
		int val = (n % mod);
		if(val < 0)
			val += mod;
		return gnode(t, val);
	}

	public static Node hashpointer(Table t, Object p) {
		return hashmod(t, Math.abs(p.hashCode()));
	}

	/*
	 * * number of ints inside a double
	 */
	public static final int numints = SizeOf.sizeof(double.class)
			/ SizeOf.sizeof(int.class);

	// static const Node dummynode_ = {
	// {{null}, LUA_TNIL}, /* value */
	// {{{null}, LUA_TNIL, null}} /* key */
	// };
	public static Node dummynode_ = new Node(new lua_TValue(new Value(),
			LUA_TNIL), new TKey(new Value(), LUA_TNIL, null));
	public static Node dummynode = dummynode_;

	/*
	 * * hash for lua_Numbers
	 */
	public static Node hashnum(Table t, double n) {
		//byte[] a = BitConverter.GetBytes(n);

		ByteArrayOutputStream bytestream =
		new ByteArrayOutputStream();
		DataOutputStream datastream =
		new DataOutputStream(bytestream);
		try {
			datastream.writeDouble(n);
			datastream.flush();
		} catch (IOException e) {
			Tools.LogException("Lua.java", e);
		}
		
		byte[] a = bytestream.toByteArray();
		for (int i = 1; i < a.length; i++) {
			a[0] += a[i];
		}
		return hashmod(t, (int) a[0]);
	}

	/*
	 * * returns the `main' position of an element in a table (that is, the
	 * index* of its hash value)
	 */
	public static Node mainposition(Table t, lua_TValue key) {
		switch (ttype(key)) {
		case LUA_TNUMBER:
			return hashnum(t, nvalue(key));
		case LUA_TSTRING:
			return hashstr(t, rawtsvalue(key));
		case LUA_TBOOLEAN:
			return hashboolean(t, bvalue(key));
		case LUA_TLIGHTUSERDATA:
			return hashpointer(t, pvalue(key));
		default:
			return hashpointer(t, gcvalue(key));
		}
	}

	/*
	 * * returns the index for `key' if `key' is an appropriate key to live in*
	 * the array part of the table, -1 otherwise.
	 */
	public static int arrayindex(lua_TValue key) {
		if (ttisnumber(key)) {
			double n = nvalue(key);
			int k = 0;
			RefObject<Integer> tempRef_k = new RefObject<Integer>(k);
			lua_number2int(tempRef_k, n);
			k = tempRef_k.argvalue;
			if (luai_numeq(cast_num(k), n)) {
				return k;
			}
		}
		return -1; // `key' did not match some condition
	}

	/*
	 * * returns the index of a `key' for table traversals. First goes all*
	 * elements in the array part, then elements in the hash part. The*
	 * beginning of a traversal is signalled by -1.
	 */
	public static int findindex(lua_State L, Table t, lua_TValue key) {
		int i;
		if (ttisnil(key)) // first iteration
		{
			return -1;
		}
		i = arrayindex(key);
		if (0 < i && i <= t.sizearray) // is `key' inside array part?
		{
			return i - 1; // yes; that's the index (corrected to C)
		} else {
			Node n = mainposition(t, key);
			do // check whether `key' is somewhere in the chain
			{
				/* key may be dead already, but it is ok to use it in `next' */
				if ((luaO_rawequalObj(key2tval(n), key) != 0)
						|| (ttype(gkey(n)) == LUA_TDEADKEY
								&& iscollectable(key) && gcvalue(gkey(n)) == gcvalue(key))) {
					i = cast_int(Node.OpSubtraction(n, gnode(t, 0))); // i =
																		// cast_int(n
																		// -
																		// gnode(t,
																		// 0));
																		// //
																		// key
																		// index
																		// in
																		// hash
																		// table
					/* hash elements are numbered after array ones */
					return i + t.sizearray;
				} else {
					n = gnext(n);
				}
			} while (n != null);
			luaG_runerror(L, "invalid key to " + LUA_QL("next")); // key not
																	// found
			return 0; // to avoid warnings
		}
	}

	public static int luaH_next(lua_State L, Table t, lua_TValue key) {
		int i = findindex(L, t, key); // find original element
		for (i++; i < t.sizearray; i++) // try first array part
		{
			if (!ttisnil(t.array[i])) // a non-nil value?
			{
				setnvalue(key, cast_num(i + 1));
				setobj2s(L, lua_TValue.OpAddition(key, 1), t.array[i]);// setobj2s(L,
																		// key+1,
																		// t.array[i]);
				return 1;
			}
		}
		for (i -= t.sizearray; i < sizenode(t); i++) // then hash part
		{
			if (!ttisnil(gval(gnode(t, i)))) // a non-nil value?
			{
				setobj2s(L, key, key2tval(gnode(t, i)));
				setobj2s(L, lua_TValue.OpAddition(key, 1), gval(gnode(t, i)));// setobj2s(L,
																				// key+1,
																				// gval(gnode(t,
																				// i)));
				return 1;
			}
		}
		return 0; // no more elements
	}

	/*
	 * * {=============================================================* Rehash*
	 * ==============================================================
	 */

	public static int computesizes(int[] nums, RefObject<Integer> narray) {
		int i;
		int twotoi; // 2^i
		int a = 0; // number of elements smaller than 2^i
		int na = 0; // number of elements to go to array part
		int n = 0; // optimal size for array part
		for (i = 0, twotoi = 1; twotoi / 2 < narray.argvalue; i++, twotoi *= 2) {
			if (nums[i] > 0) {
				a += nums[i];
				if (a > twotoi / 2) // more than half elements present?
				{
					n = twotoi; // optimal size (till now)
					na = a; // all elements smaller than n will go to array part
				}
			}
			if (a == narray.argvalue) // all elements already counted
			{
				break;
			}
		}
		narray.argvalue = n;
		lua_assert(narray.argvalue / 2 <= na && na <= narray.argvalue);
		return na;
	}

	public static int countint(lua_TValue key, int[] nums) {
		int k = arrayindex(key);
		if (0 < k && k <= MAXASIZE) // is `key' an appropriate array index?
		{
			nums[ceillog2(k)]++; // count as such
			return 1;
		} else {
			return 0;
		}
	}

	public static int numusearray(Table t, int[] nums) {
		int lg;
		int ttlg; // 2^lg
		int ause = 0; // summation of `nums'
		int i = 1; // count to traverse all array keys
		for (lg = 0, ttlg = 1; lg <= MAXBITS; lg++, ttlg *= 2) // for each slice
		{
			int lc = 0; // counter
			int lim = ttlg;
			if (lim > t.sizearray) {
				lim = t.sizearray; // adjust upper limit
				if (i > lim) {
					break; // no more elements to count
				}
			}
			/* count elements in range (2^(lg-1), 2^lg] */
			for (; i <= lim; i++) {
				if (!ttisnil(t.array[i - 1])) {
					lc++;
				}
			}
			nums[lg] += lc;
			ause += lc;
		}
		return ause;
	}

	public static int numusehash(Table t, int[] nums, RefObject<Integer> pnasize) {
		int totaluse = 0; // total number of elements
		int ause = 0; // summation of `nums'
		int i = sizenode(t);
		while ((i--) != 0) {
			Node n = t.node[i];
			if (!ttisnil(gval(n))) {
				ause += countint(key2tval(n), nums);
				totaluse++;
			}
		}
		pnasize.argvalue += ause;
		return totaluse;
	}

	public static void setarrayvector(lua_State L, Table t, int size) {
		int i;
		RefObject<Lua.lua_TValue[]> tempRef_array = new RefObject<Lua.lua_TValue[]>(
				t.array);
		Lua.<lua_TValue> luaM_reallocvector(L, tempRef_array, t.sizearray,
				size, lua_TValue.class); // , lua_TValue
		t.array = tempRef_array.argvalue;
		for (i = t.sizearray; i < size; i++) {
			setnilvalue(t.array[i]);
		}
		t.sizearray = size;
	}

	public static void setnodevector(lua_State L, Table t, int size) {
		int lsize;
		if (size == 0) // no elements to hash part?
		{
			t.node = new Node[] { dummynode }; // use common `dummynode'
			lsize = 0;
		} else {
			int i;
			lsize = ceillog2(size);
			if (lsize > MAXBITS) {
				luaG_runerror(L, "table overflow");
			}
			size = twoto(lsize);
			Node[] nodes = Lua.<Node> luaM_newvector(L, size, Node.class);
			t.node = nodes;
			for (i = 0; i < size; i++) {
				Node n = gnode(t, i);
				gnext_set(n, null);
				setnilvalue(gkey(n));
				setnilvalue(gval(n));
			}
		}
		t.lsizenode = cast_byte(lsize);
		t.lastfree = size; // all positions are free
	}

	public static void resize(lua_State L, Table t, int nasize, int nhsize) {
		int i;
		int oldasize = t.sizearray;
		int oldhsize = t.lsizenode;
		Node[] nold = t.node; // save old hash...
		if (nasize > oldasize) // array part must grow?
		{
			setarrayvector(L, t, nasize);
		}
		/* create new hash part with appropriate size */
		setnodevector(L, t, nhsize);
		if (nasize < oldasize) // array part must shrink?
		{
			t.sizearray = nasize;
			/* re-insert elements from vanishing slice */
			for (i = nasize; i < oldasize; i++) {
				if (!ttisnil(t.array[i])) {
					setobjt2t(L, luaH_setnum(L, t, i + 1), t.array[i]);
				}
			}
			/* shrink array */
			RefObject<Lua.lua_TValue[]> tempRef_array = new RefObject<Lua.lua_TValue[]>(
					t.array);
			Lua.<lua_TValue> luaM_reallocvector(L, tempRef_array, oldasize,
					nasize, lua_TValue.class); // , lua_TValue
			t.array = tempRef_array.argvalue;
		}
		/* re-insert elements from hash part */
		for (i = twoto(oldhsize) - 1; i >= 0; i--) {
			Node old = nold[i];
			if (!ttisnil(gval(old))) {
				setobjt2t(L, luaH_set(L, t, key2tval(old)), gval(old));
			}
		}
		if (nold[0] != dummynode) {
			luaM_freearray(L, nold); // free old array
		}
	}

	public static void luaH_resizearray(lua_State L, Table t, int nasize) {
		int nsize = (t.node[0] == dummynode) ? 0 : sizenode(t);
		resize(L, t, nasize, nsize);
	}

	public static void rehash(lua_State L, Table t, lua_TValue ek) {
		int nasize, na;
		int[] nums = new int[MAXBITS + 1]; // nums[i] = number of keys between
											// 2^(i-1) and 2^i
		int i;
		int totaluse;
		for (i = 0; i <= MAXBITS; i++) // reset counts
		{
			nums[i] = 0;
		}
		nasize = numusearray(t, nums); // count keys in array part
		totaluse = nasize; // all those keys are integer keys
		RefObject<Integer> tempRef_nasize = new RefObject<Integer>(nasize);
		totaluse += numusehash(t, nums, tempRef_nasize); // count keys in hash
															// part
		nasize = tempRef_nasize.argvalue;
		/* count extra key */
		nasize += countint(ek, nums);
		totaluse++;
		/* compute new size for array part */
		RefObject<Integer> tempRef_nasize2 = new RefObject<Integer>(nasize);
		na = computesizes(nums, tempRef_nasize2);
		nasize = tempRef_nasize2.argvalue;
		/* resize the table to new computed sizes */
		resize(L, t, nasize, totaluse - na);
	}

	/*
	 * * }=============================================================
	 */

	public static Table luaH_new(lua_State L, int narray, int nhash) {
		Table t = Lua.<Table> luaM_new(L, Table.class);
		luaC_link(L, obj2gco(t), Integer.valueOf(LUA_TTABLE).byteValue());
		t.metatable = null;
		t.flags = cast_byte(~0);
		/* temporary values (kept only if some malloc fails) */
		t.array = null;
		t.sizearray = 0;
		t.lsizenode = 0;
		t.node = new Node[] { dummynode };
		setarrayvector(L, t, narray);
		setnodevector(L, t, nhash);
		return t;
	}

	public static void luaH_free(lua_State L, Table t) {
		if (t.node[0] != dummynode) {
			luaM_freearray(L, t.node);
		}
		luaM_freearray(L, t.array);
		luaM_free(L, t);
	}

	public static Node getfreepos(Table t) {
		while (t.lastfree-- > 0) {
			if (ttisnil(gkey(t.node[t.lastfree]))) {
				return t.node[t.lastfree];
			}
		}
		return null; // could not find a free place
	}

	/*
	 * * inserts a new key into a hash table; first, check whether key's main*
	 * position is free. If not, check whether colliding node is in its main*
	 * position or not: if it is not, move colliding node to an empty place and*
	 * put new key in its main position; otherwise (colliding node is in its
	 * main* position), new key goes to an empty position.
	 */
	public static lua_TValue newkey(lua_State L, Table t, lua_TValue key) {
		Node mp = mainposition(t, key);
		if (!ttisnil(gval(mp)) || mp == dummynode) {
			Node othern;
			Node n = getfreepos(t); // get a free place
			if (n == null) // cannot find a free place?
			{
				rehash(L, t, key); // grow table
				return luaH_set(L, t, key); // re-insert key into grown table
			}
			lua_assert(n != dummynode);
			othern = mainposition(t, key2tval(mp));
			if (othern != mp) // is colliding node out of its main position?
			{
				/* yes; move colliding node into free position */
				while (gnext(othern) != mp) // find previous
				{
					othern = gnext(othern);
				}
				gnext_set(othern, n); // redo the chain with `n' in place of
										// `mp'
				n.i_val = new lua_TValue(mp.i_val); // copy colliding node into
													// free pos. (mp.next also
													// goes)
				n.i_key = new TKey(mp.i_key);
				gnext_set(mp, null); // now `mp' is free
				setnilvalue(gval(mp));
			} else // colliding node is in its own main position
			{
				/* new node will go into free position */
				gnext_set(n, gnext(mp)); // chain new position
				gnext_set(mp, n);
				mp = n;
			}
		}
		gkey(mp).value.Copy(key.value);
		gkey(mp).tt = key.tt;
		luaC_barriert(L, t, key);
		lua_assert(ttisnil(gval(mp)));
		return gval(mp);
	}

	/*
	 * * search function for integers
	 */
	public static lua_TValue luaH_getnum(Table t, int key) {
		/* (1 <= key && key <= t.sizearray) */
		//if ((int) (key - 1) < (int) t.sizearray) {
		if(1 <= key && key <= t.sizearray) {
			return t.array[key - 1];
		} else {
			double nk = cast_num(key);
			Node n = hashnum(t, nk);
			do // check whether `key' is somewhere in the chain
			{
				if (ttisnumber(gkey(n)) && luai_numeq(nvalue(gkey(n)), nk)) {
					return gval(n); // that's it
				} else {
					n = gnext(n);
				}
			} while (n != null);
			return luaO_nilobject;
		}
	}

	/*
	 * * search function for strings
	 */
	public static lua_TValue luaH_getstr(Table t, TString key) {
		Node n = hashstr(t, key);
		do // check whether `key' is somewhere in the chain
		{
			if (ttisstring(gkey(n)) && rawtsvalue(gkey(n)) == key) {
				return gval(n); // that's it
			} else {
				n = gnext(n);
			}
		} while (n != null);
		return luaO_nilobject;
	}

	/*
	 * * main search function
	 */
	public static lua_TValue luaH_get(Table t, lua_TValue key) {
		switch (ttype(key)) {
		case LUA_TNIL:
			return luaO_nilobject;
		case LUA_TSTRING:
			return luaH_getstr(t, rawtsvalue(key));
		case LUA_TNUMBER: {
			int k = 0;
			double n = nvalue(key);
			RefObject<Integer> tempRef_k = new RefObject<Integer>(k);
			lua_number2int(tempRef_k, n);
			k = tempRef_k.argvalue;
			if (luai_numeq(cast_num(k), nvalue(key))) // index is int?
			{
				return luaH_getnum(t, k); // use specialized version
			}
			/*
			 * else go through ... actually on second thoughts don't, because
			 * this is C#
			 */
			Node node = mainposition(t, key);
			do { // check whether `key' is somewhere in the chain
				if (luaO_rawequalObj(key2tval(node), key) != 0) {
					return gval(node); // that's it
				} else {
					node = gnext(node);
				}
			} while (node != null);
			return luaO_nilobject;
		}
		default: {
			Node node = mainposition(t, key);
			do // check whether `key' is somewhere in the chain
			{
				if (luaO_rawequalObj(key2tval(node), key) != 0) {
					return gval(node); // that's it
				} else {
					node = gnext(node);
				}
			} while (node != null);
			return luaO_nilobject;
		}
		}
	}

	public static lua_TValue luaH_set(lua_State L, Table t, lua_TValue key) {
		lua_TValue p = luaH_get(t, key);
		t.flags = 0;
		if (p != luaO_nilobject) {
			return (lua_TValue) p;
		} else {
			if (ttisnil(key)) {
				luaG_runerror(L, "table index is nil");
			} else if (ttisnumber(key) && luai_numisnan(nvalue(key))) {
				luaG_runerror(L, "table index is NaN");
			}
			return newkey(L, t, key);
		}
	}

	public static lua_TValue luaH_setnum(lua_State L, Table t, int key) {
		lua_TValue p = luaH_getnum(t, key);
		if (p != luaO_nilobject) {
			return (lua_TValue) p;
		} else {
			lua_TValue k = new lua_TValue();
			setnvalue(k, cast_num(key));
			return newkey(L, t, k);
		}
	}

	public static lua_TValue luaH_setstr(lua_State L, Table t, TString key) {
		lua_TValue p = luaH_getstr(t, key);
		if (p != luaO_nilobject) {
			return (lua_TValue) p;
		} else {
			lua_TValue k = new lua_TValue();
			setsvalue(L, k, key);
			return newkey(L, t, k);
		}
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int unbound_search (Table t, uint j)
	public static int unbound_search(Table t, int j) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint i = j;
		int i = j; // i is zero or a present index
		j++;
		/* find `i' and `j' such that i is present and j is not */
		while (!ttisnil(luaH_getnum(t, (int) j))) {
			i = j;
			j *= 2;
			if (j > (int) MAX_INT) // overflow?
			{
				/* table was built with bad purposes: resort to linear search */
				i = 1;
				while (!ttisnil(luaH_getnum(t, (int) i))) {
					i++;
				}
				return (int) (i - 1);
			}
		}
		/* now do a binary search between them */
		while (j - i > 1) {
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint m = (i+j)/2;
			int m = (i + j) / 2;
			if (ttisnil(luaH_getnum(t, (int) m))) {
				j = m;
			} else {
				i = m;
			}
		}
		return (int) i;
	}

	/*
	 * * Try to find a boundary in table `t'. A `boundary' is an integer index*
	 * such that t[i] is non-nil and t[i+1] is nil (and 0 if t[1] is nil).
	 */
	public static int luaH_getn(Table t) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint j = (uint)t.sizearray;
		int j = (int) t.sizearray;
		if (j > 0 && ttisnil(t.array[j - 1])) {
			/* there is a boundary in the array part: (binary) search for it */
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint i = 0;
			int i = 0;
			while (j - i > 1) {
				// FUCK WARNING: Unsigned integer types have no direct
				// equivalent in Java:
				// ORIGINAL LINE: uint m = (i+j)/2;
				int m = (i + j) / 2;
				if (ttisnil(t.array[m - 1])) {
					j = m;
				} else {
					i = m;
				}
			}
			return (int) i;
		}
		/* else must find a boundary in hash part */
		else if (t.node[0] == dummynode) // hash part is empty?
		{
			return (int) j; // that is easy...
		} else {
			return unbound_search(t, j);
		}
	}

	// /#if defined(LUA_DEBUG)

	// Node *luaH_mainposition (const Table *t, const lua_TValue *key) {
	// return mainposition(t, key);
	// }

	// int luaH_isdummy (Node *n) { return n == dummynode; }

	// /#endif

	public static int aux_getn(lua_State L, int n) {
		luaL_checktype(L, n, LUA_TTABLE);
		return luaL_getn(L, n);
	}

	public static int foreachi(lua_State L) {
		int i;
		int n = aux_getn(L, 1);
		luaL_checktype(L, 2, LUA_TFUNCTION);
		for (i = 1; i <= n; i++) {
			lua_pushvalue(L, 2); // function
			lua_pushinteger(L, i); // 1st argument
			lua_rawgeti(L, 1, i); // 2nd argument
			lua_call(L, 2, 1);
			if (!lua_isnil(L, -1)) {
				return 1;
			}
			lua_pop(L, 1); // remove nil result
		}
		return 0;
	}

	public static int _foreach(lua_State L) {
		luaL_checktype(L, 1, LUA_TTABLE);
		luaL_checktype(L, 2, LUA_TFUNCTION);
		lua_pushnil(L); // first key
		while (lua_next(L, 1) != 0) {
			lua_pushvalue(L, 2); // function
			lua_pushvalue(L, -3); // key
			lua_pushvalue(L, -3); // value
			lua_call(L, 2, 1);
			if (!lua_isnil(L, -1)) {
				return 1;
			}
			lua_pop(L, 2); // remove value and result
		}
		return 0;
	}

	public static int maxn(lua_State L) {
		double max = 0;
		luaL_checktype(L, 1, LUA_TTABLE);
		lua_pushnil(L); // first key
		while (lua_next(L, 1) != 0) {
			lua_pop(L, 1); // remove value
			if (lua_type(L, -1) == LUA_TNUMBER) {
				double v = lua_tonumber(L, -1);
				if (v > max) {
					max = v;
				}
			}
		}
		lua_pushnumber(L, max);
		return 1;
	}

	public static int getn(lua_State L) {
		lua_pushinteger(L, aux_getn(L, 1));
		return 1;
	}

	public static int setn(lua_State L) {
		luaL_checktype(L, 1, LUA_TTABLE);
		// /#ifndef luaL_setn
		// luaL_setn(L, 1, luaL_checkint(L, 2));
		// /#else
		luaL_error(L, LUA_QL("setn") + " is obsolete");
		// /#endif
		lua_pushvalue(L, 1);
		return 1;
	}

	public static int tinsert(lua_State L) {
		int e = aux_getn(L, 1) + 1; // first empty element
		int pos; // where to insert new element
		switch (lua_gettop(L)) {
		case 2: // called with only 2 arguments
		{
			pos = e; // insert new element at the end
			break;
		}
		case 3: {
			int i;
			pos = luaL_checkint(L, 2); // 2nd argument is the position
			if (pos > e) // `grow' array if necessary
			{
				e = pos;
			}
			for (i = e; i > pos; i--) // move up elements
			{
				lua_rawgeti(L, 1, i - 1);
				lua_rawseti(L, 1, i); // t[i] = t[i-1]
			}
			break;
		}
		default: {
			return luaL_error(L, "wrong number of arguments to "
					+ LUA_QL("insert"));
		}
		}
		luaL_setn(L, 1, e); // new size
		lua_rawseti(L, 1, pos); // t[pos] = v
		return 0;
	}

	public static int tremove(lua_State L) {
		int e = aux_getn(L, 1);
		int pos = luaL_optint(L, 2, e);
		if (!(1 <= pos && pos <= e)) // position is outside bounds?
		{
			return 0; // nothing to remove
		}
		luaL_setn(L, 1, e - 1); // t.n = n-1
		lua_rawgeti(L, 1, pos); // result = t[pos]
		for (; pos < e; pos++) {
			lua_rawgeti(L, 1, pos + 1);
			lua_rawseti(L, 1, pos); // t[pos] = t[pos+1]
		}
		lua_pushnil(L);
		lua_rawseti(L, 1, e); // t[e] = nil
		return 1;
	}

	public static void addfield(lua_State L, luaL_Buffer b, int i) {
		lua_rawgeti(L, 1, i);
		if (lua_isstring(L, -1) == 0) {
			luaL_error(L, "invalid value (%s) at index %d in table for "
					+ LUA_QL("concat"), luaL_typename(L, -1), i);
		}
		luaL_addvalue(b);
	}

	public static int tconcat(lua_State L) {
		luaL_Buffer b = new luaL_Buffer();
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint lsep;
		int lsep = 0;
		int i, last;
		RefObject<Integer> tempRef_lsep = new RefObject<Integer>(lsep);
		CharPtr sep = luaL_optlstring(L, 2, "", tempRef_lsep);
		lsep = tempRef_lsep.argvalue;
		luaL_checktype(L, 1, LUA_TTABLE);
		i = luaL_optint(L, 3, 1);
		last = luaL_opt_integer(L, luaL_checkint, 4, luaL_getn(L, 1));
		luaL_buffinit(L, b);
		for (; i < last; i++) {
			addfield(L, b, i);
			luaL_addlstring(b, sep, lsep);
		}
		if (i == last) // add last value (if interval was not empty)
		{
			addfield(L, b, i);
		}
		luaL_pushresult(b);
		return 1;
	}

	/*
	 * * {======================================================* Quicksort*
	 * (based on `Algorithms in MODULA-3', Robert Sedgewick;* Addison-Wesley,
	 * 1993.)
	 */

	public static void set2(lua_State L, int i, int j) {
		lua_rawseti(L, 1, i);
		lua_rawseti(L, 1, j);
	}

	public static int sort_comp(lua_State L, int a, int b) {
		if (!lua_isnil(L, 2)) // function?
		{
			int res;
			lua_pushvalue(L, 2);
			lua_pushvalue(L, a - 1); // -1 to compensate function
			lua_pushvalue(L, b - 2); // -2 to compensate function and `a'
			lua_call(L, 2, 1);
			res = lua_toboolean(L, -1);
			lua_pop(L, 1);
			return res;
		} else // a < b?
		{
			return lua_lessthan(L, a, b);
		}
	}

	public static int auxsort_loop1(lua_State L, RefObject<Integer> i) {
		lua_rawgeti(L, 1, ++i.argvalue);
		return sort_comp(L, -1, -2);
	}

	public static int auxsort_loop2(lua_State L, RefObject<Integer> j) {
		lua_rawgeti(L, 1, --j.argvalue);
		return sort_comp(L, -3, -1);
	}

	public static void auxsort(lua_State L, int l, int u) {
		while (l < u) // for tail recursion
		{
			int i, j;
			/* sort elements a[l], a[(l+u)/2] and a[u] */
			lua_rawgeti(L, 1, l);
			lua_rawgeti(L, 1, u);
			if (sort_comp(L, -1, -2) != 0) // a[u] < a[l]?
			{
				set2(L, l, u); // swap a[l] - a[u]
			} else {
				lua_pop(L, 2);
			}
			if (u - l == 1) // only 2 elements
			{
				break;
			}
			i = (l + u) / 2;
			lua_rawgeti(L, 1, i);
			lua_rawgeti(L, 1, l);
			if (sort_comp(L, -2, -1) != 0) // a[i]<a[l]?
			{
				set2(L, i, l);
			} else {
				lua_pop(L, 1); // remove a[l]
				lua_rawgeti(L, 1, u);
				if (sort_comp(L, -1, -2) != 0) // a[u]<a[i]?
				{
					set2(L, i, u);
				} else {
					lua_pop(L, 2);
				}
			}
			if (u - l == 2) // only 3 elements
			{
				break;
			}
			lua_rawgeti(L, 1, i); // Pivot
			lua_pushvalue(L, -1);
			lua_rawgeti(L, 1, u - 1);
			set2(L, i, u - 1);
			/* a[l] <= P == a[u-1] <= a[u], only need to sort from l+1 to u-2 */
			i = l;
			j = u - 1;
			for (;;) // invariant: a[l..i] <= P <= a[j..u]
			{
				/* repeat ++i until a[i] >= P */
				RefObject<Integer> tempRef_i = new RefObject<Integer>(i);
				boolean tempVar = auxsort_loop1(L, tempRef_i) != 0;
				i = tempRef_i.argvalue;
				while (tempVar) {
					if (i > u) {
						luaL_error(L, "invalid order function for sorting");
					}
					lua_pop(L, 1); // remove a[i]
					RefObject<Integer> tempRef_i2 = new RefObject<Integer>(i);
					tempVar = auxsort_loop1(L, tempRef_i2) != 0;
					i = tempRef_i2.argvalue;
				}
				/* repeat --j until a[j] <= P */
				RefObject<Integer> tempRef_j = new RefObject<Integer>(j);
				boolean tempVar2 = auxsort_loop2(L, tempRef_j) != 0;
				j = tempRef_j.argvalue;
				while (tempVar2) {
					if (j < l) {
						luaL_error(L, "invalid order function for sorting");
					}
					lua_pop(L, 1); // remove a[j]
					RefObject<Integer> tempRef_j2 = new RefObject<Integer>(j);
					tempVar2 = auxsort_loop2(L, tempRef_j2) != 0;
					j = tempRef_j2.argvalue;
				}
				if (j < i) {
					lua_pop(L, 3); // pop pivot, a[i], a[j]
					break;
				}
				set2(L, i, j);
			}
			lua_rawgeti(L, 1, u - 1);
			lua_rawgeti(L, 1, i);
			set2(L, u - 1, i); // swap pivot (a[u-1]) with a[i]
			/* a[l..i-1] <= a[i] == P <= a[i+1..u] */
			/* adjust so that smaller half is in [j..i] and larger one in [l..u] */
			if (i - l < u - i) {
				j = l;
				i = i - 1;
				l = i + 2;
			} else {
				j = i + 1;
				i = u;
				u = j - 2;
			}
			auxsort(L, j, i); // call recursively the smaller one
		} // repeat the routine for the larger one
	}

	public static int sort(lua_State L) {
		int n = aux_getn(L, 1);
		luaL_checkstack(L, 40, ""); // assume array is smaller than 2^40
		if (!lua_isnoneornil(L, 2)) // is there a 2nd argument?
		{
			luaL_checktype(L, 2, LUA_TFUNCTION);
		}
		lua_settop(L, 2); // make sure there is two arguments
		auxsort(L, 1, n);
		return 0;
	}

	/* }====================================================== */

	private final static luaL_Reg[] tab_funcs = {
			new luaL_Reg("concat", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return tconcat((lua_State)arg);
				}
			}),
			new luaL_Reg("foreach", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return _foreach((lua_State)arg);
				}
			}),
			new luaL_Reg("foreachi", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return foreachi((lua_State)arg);
				}
			}), 
			new luaL_Reg("getn", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return getn((lua_State)arg);
				}
			}),
			new luaL_Reg("maxn", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return maxn((lua_State)arg);
				}
			}), 
			new luaL_Reg("insert", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return tinsert((lua_State)arg);
				}
			}),
			new luaL_Reg("remove", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return tremove((lua_State)arg);
				}
			}), 
			new luaL_Reg("setn", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return setn((lua_State)arg);
				}
			}),
			new luaL_Reg("sort", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return sort((lua_State)arg);
				}
			}), 
			new luaL_Reg((String)null, (String)null) };

	public static int luaopen_table(lua_State L) {
		luaL_register(L, LUA_TABLIBNAME, tab_funcs);
		return 1;
	}

	/*
	 * WARNING: if you change the order of this enumeration, grep "ORDER TM"
	 */
	public enum TMS {
		TM_INDEX, TM_NEWINDEX, TM_GC, TM_MODE, TM_EQ, // last tag method with
														// `fast' access
		TM_ADD, TM_SUB, TM_MUL, TM_DIV, TM_MOD, TM_POW, TM_UNM, TM_LEN, TM_LT, TM_LE, TM_CONCAT, TM_CALL, TM_N; // number
																												// of
																												// elements
																												// in
																												// the
																												// enum

		public int getValue() {
			return this.ordinal();
		}

		public static TMS forValue(int value) {
			return values()[value];
		}
	}

	public static lua_TValue gfasttm(global_State g, Table et, TMS e) {
		return (et == null) ? null
				: ((et.flags & (1 << e.getValue())) != 0) ? null : luaT_gettm(
						et, e, g.tmname[e.getValue()]);
	}

	public static lua_TValue fasttm(lua_State l, Table et, TMS e) {
		return gfasttm(G(l), et, e);
	}

	public final static CharPtr[] luaT_typenames = { new CharPtr("nil"),
			new CharPtr("boolean"), new CharPtr("userdata"),
			new CharPtr("number"), new CharPtr("string"), new CharPtr("table"),
			new CharPtr("function"), new CharPtr("userdata"),
			new CharPtr("thread"), new CharPtr("proto"), new CharPtr("upval") };

	private final static CharPtr[] luaT_eventname = { new CharPtr("__index"),
			new CharPtr("__newindex"), new CharPtr("__gc"),
			new CharPtr("__mode"), new CharPtr("__eq"), new CharPtr("__add"),
			new CharPtr("__sub"), new CharPtr("__mul"), new CharPtr("__div"),
			new CharPtr("__mod"), new CharPtr("__pow"), new CharPtr("__unm"),
			new CharPtr("__len"), new CharPtr("__lt"), new CharPtr("__le"),
			new CharPtr("__concat"), new CharPtr("__call") }; // ORDER TM

	public static void luaT_init(lua_State L) {
		int i;
		for (i = 0; i < TMS.TM_N.getValue(); i++) {
			G(L).tmname[i] = luaS_new(L, luaT_eventname[i]);
			luaS_fix(G(L).tmname[i]); // never collect these names
		}
	}

	/*
	 * * function to be used with macro "fasttm": optimized for absence of* tag
	 * methods
	 */
	public static lua_TValue luaT_gettm(Table events, TMS event_, TString ename) {
		/* const */
		lua_TValue tm = luaH_getstr(events, ename);
		lua_assert(event_.getValue() <= TMS.TM_EQ.getValue());// lua_assert(event_
																// <=
																// TMS.TM_EQ);
		if (ttisnil(tm)) // no tag method?
		{
			events.flags |= (byte) (1 << event_.getValue()); // cache this fact
			return null;
		} else {
			return tm;
		}
	}

	public static lua_TValue luaT_gettmbyobj(lua_State L, lua_TValue o,
			TMS event_) {
		Table mt;
		switch (ttype(o)) {
		case LUA_TTABLE:
			mt = hvalue(o).metatable;
			break;
		case LUA_TUSERDATA:
			mt = uvalue(o).metatable;
			break;
		default:
			mt = G(L).mt[ttype(o)];
			break;
		}
		return ((mt != null) ? luaH_getstr(mt, G(L).tmname[event_.getValue()])
				: luaO_nilobject);
	}

	/*
	 * * ===============================================================* some
	 * useful macros*
	 * ===============================================================
	 */
	public static void lua_pop(lua_State L, int n) {
		lua_settop(L, -(n) - 1);
	}

	public static void lua_newtable(lua_State L) {
		lua_createtable(L, 0, 0);
	}

	public static void lua_register(lua_State L, CharPtr n, IDelegate f)// lua_CFunction
	{
		lua_pushcfunction(L, f);
		lua_setglobal(L, n);
	}

	public static void lua_register(lua_State L, String n, IDelegate f)// lua_CFunction
	{
		lua_pushcfunction(L, f);
		lua_setglobal(L, n);
	}

	// public static void lua_pushcfunction(lua_State L, lua_CFunction f)
	public static void lua_pushcfunction(lua_State L, IDelegate f) {
		lua_pushcclosure(L, f, 0);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static uint lua_strlen(lua_State L, int i)
	public static int lua_strlen(lua_State L, int i) {
		return lua_objlen(L, i);
	}

	public static boolean lua_isfunction(lua_State L, int n) {
		return lua_type(L, n) == LUA_TFUNCTION;
	}

	public static boolean lua_istable(lua_State L, int n) {
		return lua_type(L, n) == LUA_TTABLE;
	}

	public static boolean lua_islightuserdata(lua_State L, int n) {
		return lua_type(L, n) == LUA_TLIGHTUSERDATA;
	}

	public static boolean lua_isnil(lua_State L, int n) {
		return lua_type(L, n) == LUA_TNIL;
	}

	public static boolean lua_isboolean(lua_State L, int n) {
		return lua_type(L, n) == LUA_TBOOLEAN;
	}

	public static boolean lua_isthread(lua_State L, int n) {
		return lua_type(L, n) == LUA_TTHREAD;
	}

	public static boolean lua_isnone(lua_State L, int n) {
		return lua_type(L, n) == LUA_TNONE;
	}

	public static boolean lua_isnoneornil(lua_State L, double n) {
		return lua_type(L, (int) n) <= 0;
	}

	public static void lua_pushliteral(lua_State L, CharPtr s) {
		// TODO: Implement use using lua_pushlstring instead of lua_pushstring
		// lua_pushlstring(L, "" s,
		// (sizeof(s)/GetUnmanagedSize(typeof(char)))-1)
		lua_pushstring(L, s);
	}

	public static void lua_pushliteral(lua_State L, String s) {
		// TODO: Implement use using lua_pushlstring instead of lua_pushstring
		// lua_pushlstring(L, "" s,
		// (sizeof(s)/GetUnmanagedSize(typeof(char)))-1)
		lua_pushstring(L, s);
	}

	public static void lua_setglobal(lua_State L, CharPtr s) {
		lua_setfield(L, LUA_GLOBALSINDEX, s);
	}

	public static void lua_setglobal(lua_State L, String s) {
		lua_setfield(L, LUA_GLOBALSINDEX, s);
	}

	public static void lua_getglobal(lua_State L, CharPtr s) {
		lua_getfield(L, LUA_GLOBALSINDEX, s);
	}

	public static void lua_getglobal(lua_State L, String s) {
		lua_getfield(L, LUA_GLOBALSINDEX, s);
	}

	public static CharPtr lua_tostring(lua_State L, int i) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint blah;
		int blah = 0;
		RefObject<Integer> tempRef_blah = new RefObject<Integer>(blah);
		CharPtr tempVar = lua_tolstring(L, i, tempRef_blah);
		blah = tempRef_blah.argvalue;
		return tempVar;
	}

	// //#define lua_open() luaL_newstate()
	public static lua_State lua_open() {
		return luaL_newstate();
	}

	// //#define lua_getregistry(L) lua_pushvalue(L, LUA_REGISTRYINDEX)
	public static void lua_getregistry(lua_State L) {
		lua_pushvalue(L, LUA_REGISTRYINDEX);
	}

	// //#define lua_getgccount(L) lua_gc(L, LUA_GCCOUNT, 0)
	public static int lua_getgccount(lua_State L) {
		return lua_gc(L, LUA_GCCOUNT, 0);
	}

	// /#define lua_Chunkreader lua_Reader
	// /#define lua_Chunkwriter lua_Writer

	/*
	 * * {======================================================================
	 * * Debug API*
	 * =======================================================================
	 */

	/*
	 * * Event codes
	 */
	public static final int LUA_HOOKCALL = 0;
	public static final int LUA_HOOKRET = 1;
	public static final int LUA_HOOKLINE = 2;
	public static final int LUA_HOOKCOUNT = 3;
	public static final int LUA_HOOKTAILRET = 4;

	/*
	 * * Event masks
	 */
	public static final int LUA_MASKCALL = (1 << LUA_HOOKCALL);
	public static final int LUA_MASKRET = (1 << LUA_HOOKRET);
	public static final int LUA_MASKLINE = (1 << LUA_HOOKLINE);
	public static final int LUA_MASKCOUNT = (1 << LUA_HOOKCOUNT);

	public static class lua_Debug {
		public int event_;
		public CharPtr name; // (n)
		public CharPtr namewhat; // (n) `global', `local', `field', `method'
		public CharPtr what; // (S) `Lua', `C', `main', `tail'
		public CharPtr source; // (S)
		public int currentline; // (l)
		public int nups; // (u) number of upvalues
		public int linedefined; // (S)
		public int lastlinedefined; // (S)
		public CharPtr short_src = new CharPtr(new char[LUA_IDSIZE]); // (S)
		/* private part */
		public int i_ci; // active function
	}

	/* }====================================================================== */

	/******************************************************************************
	 * Copyright (C) 1994-2008 Lua.org, PUC-Rio. All rights reserved.
	 * 
	 * Permission is hereby granted, free of charge, to any person obtaining a
	 * copy of this software and associated documentation files (the
	 * "Software"), to deal in the Software without restriction, including
	 * without limitation the rights to use, copy, modify, merge, publish,
	 * distribute, sublicense, and/or sell copies of the Software, and to permit
	 * persons to whom the Software is furnished to do so, subject to the
	 * following conditions:
	 * 
	 * The above copyright notice and this permission notice shall be included
	 * in all copies or substantial portions of the Software.
	 * 
	 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
	 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
	 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
	 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
	 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
	 * USE OR OTHER DEALINGS IN THE SOFTWARE.
	 ******************************************************************************/

	/*
	 * * ==================================================================*
	 * Search for "@@" to find all configurable definitions.*
	 * ===================================================================
	 */

	/*
	 * @@ LUA_ANSI controls the use of non-ansi features.* CHANGE it (define it)
	 * if you want Lua to avoid the use of any* non-ansi feature or library.
	 */
	// /#if defined(__STRICT_ANSI__)
	// /#define LUA_ANSI
	// /#endif

	// /#if !defined(LUA_ANSI) && _WIN32
	// /#define LUA_WIN
	// /#endif

	// /#if defined(LUA_USE_LINUX)
	// /#define LUA_USE_POSIX
	// /#define LUA_USE_DLOPEN /* needs an extra library: -ldl */
	// /#define LUA_USE_READLINE /* needs some extra libraries */
	// /#endif

	// /#if defined(LUA_USE_MACOSX)
	// /#define LUA_USE_POSIX
	// /#define LUA_DL_DYLD /* does not need extra library */
	// /#endif

	/*
	 * @@ LUA_USE_POSIX includes all functionallity listed as X/Open System
	 * 
	 * @* Interfaces Extension (XSI).* CHANGE it (define it) if your system is
	 * XSI compatible.
	 */
	// /#if defined(LUA_USE_POSIX)
	// /#define LUA_USE_MKSTEMP
	// /#define LUA_USE_ISATTY
	// /#define LUA_USE_POPEN
	// /#define LUA_USE_ULONGJMP
	// /#endif

	/*
	 * @@ LUA_PATH and LUA_CPATH are the names of the environment variables that
	 * 
	 * @* Lua check to set its paths.
	 * 
	 * @@ LUA_INIT is the name of the environment variable that Lua
	 * 
	 * @* checks for initialization code.* CHANGE them if you want different
	 * names.
	 */
	public static final String LUA_PATH = "LUA_PATH";
	public static final String LUA_CPATH = "LUA_CPATH";
	public static final String LUA_INIT = "LUA_INIT";

	/*
	 * @@ LUA_PATH_DEFAULT is the default path that Lua uses to look for
	 * 
	 * @* Lua libraries.
	 * 
	 * @@ LUA_CPATH_DEFAULT is the default path that Lua uses to look for
	 * 
	 * @* C libraries.* CHANGE them if your machine has a non-conventional
	 * directory* hierarchy or if you want to install your libraries in*
	 * non-conventional directories.
	 */
	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if _WIN32
	/*
	 * * In Windows, any exclamation mark ('!') in the path is replaced by the*
	 * path of the directory of the executable file of the current process.
	 */
	/*
	 * public static final String LUA_LDIR = "!\\lua\\"; public static final
	 * String LUA_CDIR = "!\\"; public static final String LUA_PATH_DEFAULT =
	 * ".\\?.lua;" + LUA_LDIR + "?.lua;" + LUA_LDIR + "?\\init.lua;" + LUA_CDIR
	 * + "?.lua;" + LUA_CDIR + "?\\init.lua"; public static final String
	 * LUA_CPATH_DEFAULT = ".\\?.dll;" + LUA_CDIR + "?.dll;" + LUA_CDIR +
	 * "loadall.dll";
	 */

	// #else
	public static final String LUA_ROOT = "/usr/local/";
	public static final String LUA_LDIR = LUA_ROOT + "share/lua/5.1/";
	public static final String LUA_CDIR = LUA_ROOT + "lib/lua/5.1/";
	public static final String LUA_PATH_DEFAULT = "?.lua;" + "./?.lua;" + LUA_LDIR
			+ "?.lua;" + LUA_LDIR + "?/init.lua;" + LUA_CDIR + "?.lua;"
			+ LUA_CDIR + "?/init.lua";
	public static final String LUA_CPATH_DEFAULT = "./?.so;" + LUA_CDIR
			+ "?.so;" + LUA_CDIR + "loadall.so";
	// #endif

	/*
	 * @@ LUA_DIRSEP is the directory separator (for submodules).* CHANGE it if
	 * your machine does not use "/" as the directory separator* and is not
	 * Windows. (On Windows Lua automatically uses "\".)
	 */
	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if _WIN32
	// public static final String LUA_DIRSEP = "\\";
	// #else
	public static final String LUA_DIRSEP = "/";
	// #endif

	/*
	 * @@ LUA_PATHSEP is the character that separates templates in a path.
	 * 
	 * @@ LUA_PATH_MARK is the string that marks the substitution points in a
	 * 
	 * @* template.
	 * 
	 * @@ LUA_EXECDIR in a Windows path is replaced by the executable's
	 * 
	 * @* directory.
	 * 
	 * @@ LUA_IGMARK is a mark to ignore all before it when bulding the
	 * 
	 * @* luaopen_ function name.* CHANGE them if for some reason your system
	 * cannot use those* characters. (E.g., if one of those characters is a
	 * common character* in file/directory names.) Probably you do not need to
	 * change them.
	 */
	public static final String LUA_PATHSEP = ";";
	public static final String LUA_PATH_MARK = "?";
	public static final String LUA_EXECDIR = "!";
	public static final String LUA_IGMARK = "-";

	/*
	 * @@ int is the integral type used by lua_pushinteger/lua_tointeger.*
	 * CHANGE that if int is not adequate on your machine. (On most* machines,
	 * int gives a good choice between int or long.)
	 */
	// /#define int int

	/*
	 * @@ LUA_API is a mark for all core API functions.
	 * 
	 * @@ LUALIB_API is a mark for all standard library functions.* CHANGE them
	 * if you need to define those functions in some special way.* For instance,
	 * if you want to create one Windows DLL with the core and* the libraries,
	 * you may want to use the following definition (define* LUA_BUILD_AS_DLL to
	 * get it).
	 */
	// /#if LUA_BUILD_AS_DLL

	// /#if defined(LUA_CORE) || defined(LUA_LIB)
	// /#define LUA_API __declspec(dllexport)
	// /#else
	// /#define LUA_API __declspec(dllimport)
	// /#endif

	// /#else

	// /#define LUA_API extern

	// /#endif

	/* more often than not the libs go together with the core */
	// /#define LUALIB_API LUA_API

	/*
	 * @@ LUAI_FUNC is a mark for all extern functions that are not to be
	 * 
	 * @* exported to outside modules.
	 * 
	 * @@ LUAI_DATA is a mark for all extern (const) variables that are not to
	 * 
	 * @* be exported to outside modules.* CHANGE them if you need to mark them
	 * in some special way. Elf/gcc* (versions 3.2 and later) mark them as
	 * "hidden" to optimize access* when Lua is compiled as a shared library.
	 */
	// /#if defined(luaall_c)
	// /#define LUAI_FUNC static
	// /#define LUAI_DATA /* empty */

	// /#elif defined(__GNUC__) && ((__GNUC__*100 + __GNUC_MINOR__) >= 302) && \
	// defined(__ELF__)
	// /#define LUAI_FUNC __attribute__((visibility("hidden"))) extern
	// /#define LUAI_DATA LUAI_FUNC

	// /#else
	// /#define LUAI_FUNC extern
	// /#define LUAI_DATA extern
	// /#endif

	/*
	 * @@ LUA_QL describes how error messages quote program elements.* CHANGE it
	 * if you want a different appearance.
	 */
	public static String LUA_QL(String x) {
		return "'" + x + "'";
	}

	public static CharPtr getLUA_QS() {
		return new CharPtr(LUA_QL("%s"));
	}

	/*
	 * @@ LUA_IDSIZE gives the maximum size for the description of the source
	 * 
	 * @* of a function in debug information.* CHANGE it if you want a different
	 * size.
	 */
	public static final int LUA_IDSIZE = 60;

	/*
	 * * {==================================================================*
	 * Stand-alone configuration*
	 * ===================================================================
	 */

	// /#if lua_c || luaall_c

	/*
	 * @@ lua_stdin_is_tty detects whether the standard input is a 'tty' (that
	 * 
	 * @* is, whether we're running lua interactively).* CHANGE it if you have a
	 * better definition for non-POSIX/non-Windows* systems.
	 */
	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_USE_ISATTY
	// /#include <unistd.h>
	// /#define lua_stdin_is_tty() isatty(0)
	// #elif LUA_WIN
	// /#include <io.h>
	// /#include <stdio.h>
	// /#define lua_stdin_is_tty() _isatty(_fileno(stdin))
	// #else
	public static int lua_stdin_is_tty() // assume stdin is a tty
	{
		return 1;
	}

	// #endif

	/*
	 * @@ LUA_PROMPT is the default prompt used by stand-alone Lua.
	 * 
	 * @@ LUA_PROMPT2 is the default continuation prompt used by stand-alone
	 * Lua.* CHANGE them if you want different prompts. (You can also change the
	 * * prompts dynamically, assigning to globals _PROMPT/_PROMPT2.)
	 */
	public static final String LUA_PROMPT = "> ";
	public static final String LUA_PROMPT2 = ">> ";

	/*
	 * @@ LUA_PROGNAME is the default name for the stand-alone Lua program.*
	 * CHANGE it if your stand-alone interpreter has a different name and* your
	 * system is not able to detect that name automatically.
	 */
	public static final String LUA_PROGNAME = "lua";

	/*
	 * @@ LUA_MAXINPUT is the maximum length for an input line in the
	 * 
	 * @* stand-alone interpreter.* CHANGE it if you need longer lines.
	 */
	public static final int LUA_MAXINPUT = 512;

	/*
	 * @@ lua_readline defines how to show a prompt and then read a line from
	 * 
	 * @* the standard input.
	 * 
	 * @@ lua_saveline defines how to "save" a read line in a "history".
	 * 
	 * @@ lua_freeline defines how to free a line read by lua_readline.* CHANGE
	 * them if you want to improve this functionality (e.g., by using* GNU
	 * readline and history facilities).
	 */
	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_USE_READLINE
	// /#include <stdio.h>
	// /#include <readline/readline.h>
	// /#include <readline/history.h>
	// /#define lua_readline(L,b,p) ((void)L, ((b)=readline(p)) != null)
	// /#define lua_saveline(L,idx) \
	// if (lua_strlen(L,idx) > 0) /* non-empty line? */ \
	// add_history(lua_tostring(L, idx)); /* add it to history */
	// /#define lua_freeline(L,b) ((void)L, free(b))
	// #else
	public static boolean lua_readline(lua_State L, CharPtr b, CharPtr p) {
		fputs(p, stdout);
		fflush(stdout); // show prompt
		return (fgets(b, stdin) != null); // get line
	}

	public static void lua_saveline(lua_State L, int idx) {
	}

	public static void lua_freeline(lua_State L, CharPtr b) {
	}

	// #endif

	// /#endif

	/* }================================================================== */

	/*
	 * @@ LUAI_GCPAUSE defines the default pause between garbage-collector
	 * cycles
	 * 
	 * @* as a percentage.* CHANGE it if you want the GC to run faster or slower
	 * (higher values* mean larger pauses which mean slower collection.) You can
	 * also change* this value dynamically.
	 */
	public static final int LUAI_GCPAUSE = 200; // 200% (wait memory to double
												// before next GC)

	/*
	 * @@ LUAI_GCMUL defines the default speed of garbage collection relative to
	 * 
	 * @* memory allocation as a percentage.* CHANGE it if you want to change
	 * the granularity of the garbage* collection. (Higher values mean coarser
	 * collections. 0 represents* infinity, where each step performs a full
	 * collection.) You can also* change this value dynamically.
	 */
	public static final int LUAI_GCMUL = 200; // GC runs 'twice the speed' of
												// memory allocation

	/*
	 * @@ LUA_COMPAT_GETN controls compatibility with old getn behavior.* CHANGE
	 * it (define it) if you want exact compatibility with the* behavior of
	 * setn/getn in Lua 5.0.
	 */
	// /#undef LUA_COMPAT_GETN /* dotnet port doesn't define in the first place
	// */

	/*
	 * @@ LUA_COMPAT_LOADLIB controls compatibility about global loadlib.*
	 * CHANGE it to undefined as soon as you do not need a global 'loadlib'*
	 * function (the function is still available as 'package.loadlib').
	 */
	// /#undef LUA_COMPAT_LOADLIB /* dotnet port doesn't define in the first
	// place */

	/*
	 * @@ LUA_COMPAT_VARARG controls compatibility with old vararg feature.*
	 * CHANGE it to undefined as soon as your programs use only '...' to* access
	 * vararg parameters (instead of the old 'arg' table).
	 */
	// /#define LUA_COMPAT_VARARG /* defined higher up */

	/*
	 * @@ LUA_COMPAT_MOD controls compatibility with old math.mod function.*
	 * CHANGE it to undefined as soon as your programs use 'math.fmod' or* the
	 * new '%' operator instead of 'math.mod'.
	 */
	// /#define LUA_COMPAT_MOD /* defined higher up */

	/*
	 * @@ LUA_COMPAT_LSTR controls compatibility with old long string nesting
	 * 
	 * @* facility.* CHANGE it to 2 if you want the old behaviour, or undefine
	 * it to turn* off the advisory error when nesting [[...]].
	 */
	// /#define LUA_COMPAT_LSTR 1
	// /#define LUA_COMPAT_LSTR /* defined higher up */

	/*
	 * @@ LUA_COMPAT_GFIND controls compatibility with old 'string.gfind' name.*
	 * CHANGE it to undefined as soon as you rename 'string.gfind' to*
	 * 'string.gmatch'.
	 */
	// /#define LUA_COMPAT_GFIND /* defined higher up */

	/*
	 * @@ LUA_COMPAT_OPENLIB controls compatibility with old 'luaL_openlib'
	 * 
	 * @* behavior.* CHANGE it to undefined as soon as you replace to
	 * 'luaL_register'* your uses of 'luaL_openlib'
	 */
	// /#define LUA_COMPAT_OPENLIB /* defined higher up */

	/*
	 * @@ luai_apicheck is the assert macro used by the Lua-C API.* CHANGE
	 * luai_apicheck if you want Lua to perform some checks in the* parameters
	 * it gets from API calls. This may slow down the interpreter* a bit, but
	 * may be quite useful when debugging C code that interfaces* with Lua. A
	 * useful redefinition is to use assert.h.
	 */
	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_USE_APICHECK
	/*
	 * public static void luai_apicheck(lua_State L, boolean o) { assert o; }
	 * public static void luai_apicheck(lua_State L, int o) { assert o != 0; }
	 */
	// #else
	public static void luai_apicheck(lua_State L, boolean o) {
	}

	public static void luai_apicheck(lua_State L, int o) {
	}

	// #endif

	/*
	 * @@ LUAI_BITSINT defines the number of bits in an int.* CHANGE here if Lua
	 * cannot automatically detect the number of bits of* your machine. Probably
	 * you do not need to change this.
	 */
	/* avoid overflows in comparison */
	// /#if INT_MAX-20 < 32760
	// public const int LUAI_BITSINT = 16
	// /#elif INT_MAX > 2147483640L
	/* int has at least 32 bits */
	public static final int LUAI_BITSINT = 32;
	// /#else
	// /#error "you must define LUA_BITSINT with number of bits in an integer"
	// /#endif

	/*
	 * @@ LUAI_UINT32 is an unsigned integer with at least 32 bits.
	 * 
	 * @@ LUAI_INT32 is an signed integer with at least 32 bits.
	 * 
	 * @@ LUAI_UMEM is an unsigned integer big enough to count the total
	 * 
	 * @* memory used by Lua.
	 * 
	 * @@ LUAI_MEM is a signed integer big enough to count the total memory
	 * 
	 * @* used by Lua.* CHANGE here if for some weird reason the default
	 * definitions are not* good enough for your machine. (The definitions in
	 * the 'else'* part always works, but may waste space on machines with
	 * 64-bit* longs.) Probably you do not need to change this.
	 */
	// /#if LUAI_BITSINT >= 32
	// /#define LUAI_UINT32 unsigned int
	// /#define LUAI_INT32 int
	// /#define LUAI_MAXINT32 INT_MAX
	// /#define LUAI_UMEM uint
	// /#define LUAI_MEM int
	// /#else
	/*** 16-bit ints */
	// /#define LUAI_UINT32 unsigned long
	// /#define LUAI_INT32 long
	// /#define LUAI_MAXINT32 LONG_MAX
	// /#define LUAI_UMEM unsigned long
	// /#define LUAI_MEM long
	// /#endif

	/*
	 * @@ LUAI_MAXCALLS limits the number of nested calls.* CHANGE it if you
	 * need really deep recursive calls. This limit is* arbitrary; its only
	 * purpose is to stop infinite recursion before* exhausting memory.
	 */
	public static final int LUAI_MAXCALLS = 20000;

	/*
	 * @@ LUAI_MAXCSTACK limits the number of Lua stack slots that a C function
	 * 
	 * @* can use.* CHANGE it if you need lots of (Lua) stack space for your C*
	 * functions. This limit is arbitrary; its only purpose is to stop C*
	 * functions to consume unlimited stack space. (must be smaller than*
	 * -LUA_REGISTRYINDEX)
	 */
	public static final int LUAI_MAXCSTACK = 8000;

	/*
	 * * {==================================================================*
	 * CHANGE (to smaller values) the following definitions if your system* has
	 * a small C stack. (Or you may want to change them to larger* values if
	 * your system has a large C stack and these limits are* too rigid for you.)
	 * Some of these constants control the size of* stack-allocated arrays used
	 * by the compiler or the interpreter, while* others limit the maximum
	 * number of recursive calls that the compiler* or the interpreter can
	 * perform. Values too large may cause a C stack* overflow for some forms of
	 * deep constructs.*
	 * ===================================================================
	 */

	/*
	 * @@ LUAI_MAXCCALLS is the maximum depth for nested C calls (short) and
	 * 
	 * @* syntactical nested non-terminals in a program.
	 */
	public static final int LUAI_MAXCCALLS = 200;

	/*
	 * @@ LUAI_MAXVARS is the maximum number of local variables per function
	 * 
	 * @* (must be smaller than 250).
	 */
	public static final int LUAI_MAXVARS = 200;

	/*
	 * @@ LUAI_MAXUPVALUES is the maximum number of upvalues per function
	 * 
	 * @* (must be smaller than 250).
	 */
	public static final int LUAI_MAXUPVALUES = 60;

	/*
	 * @@ LUAL_BUFFERSIZE is the buffer size used by the lauxlib buffer system.
	 */
	public static int LUAL_BUFFERSIZE = 1024*8; // BUFSIZ; todo: check this
													// - mjf

	/* }================================================================== */

	/*
	 * * {==================================================================
	 * 
	 * @@ double is the type of numbers in Lua.* CHANGE the following
	 * definitions only if you want to build Lua* with a number type different
	 * from double. You may also need to* change lua_number2int &
	 * lua_number2integer.*
	 * ===================================================================
	 */

	// /#define LUA_NUMBER_DOUBLE
	// /#define double double /* declared in dotnet build with using statement
	// */

	/*
	 * @@ LUAI_UACNUMBER is the result of an 'usual argument conversion'
	 * 
	 * @* over a number.
	 */
	// /#define LUAI_UACNUMBER double /* declared in dotnet build with using
	// statement */

	/*
	 * @@ LUA_NUMBER_SCAN is the format for reading numbers.
	 * 
	 * @@ LUA_NUMBER_FMT is the format for writing numbers.
	 * 
	 * @@ lua_number2str converts a number to a string.
	 * 
	 * @@ LUAI_MAXNUMBER2STR is maximum size of previous conversion.
	 * 
	 * @@ lua_str2number converts a string to a number.
	 */
	public static final String LUA_NUMBER_SCAN = "%lf";
	public static final String LUA_NUMBER_FMT = "%.14g";

	public static CharPtr lua_number2str(double n) {
		if(n == Math.floor(n) && n < Integer.MAX_VALUE)
			return new CharPtr(String.format("%1$s", ((int)n)));
		else
			return new CharPtr(String.format("%1$s", n));
	}

	public static final int LUAI_MAXNUMBER2STR = 32; // 16 digits, sign, point,
														// and \0

	public static final String number_chars = "0123456789+-eE.";

	public static double lua_str2number(CharPtr s, RefObject<CharPtr> end) {
		end.argvalue = new CharPtr(s.chars, s.index);
		String str = "";
		while (end.argvalue.getItem(0) == ' ') {
			end.argvalue = end.argvalue.next();
		}
		while (number_chars.indexOf(end.argvalue.getItem(0)) >= 0) {
			str += end.argvalue.getItem(0);
			end.argvalue = end.argvalue.next();
		}

		try {
			return Double.parseDouble(str.toString());
		}
		/*
		 * catch (NumberFormatException e) { // this is a hack, fix it - mjf if
		 * (str.charAt(0) == '-') { return Double.NEGATIVE_INFINITY; } else {
		 * return Double.POSITIVE_INFINITY; } }
		 */
		catch (java.lang.Exception e2) {
			end.argvalue = new CharPtr(s.chars, s.index);
			return 0;
		}
	}

	/*
	 * @@ The luai_num* macros define the primitive operations over numbers.
	 */
	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_CORE
	// /#include <math.h>
	// FUCK TODO TASK: Delegates are not available in Java:
	// public delegate double op_delegate(double a, double b);
	static Delegator op_delegate = new Delegator(new Class[] { double.class,
			double.class }, Double.TYPE);

	public static double luai_numadd(double a, double b) {
		return ((a) + (b));
	}

	public static double luai_numsub(double a, double b) {
		return ((a) - (b));
	}

	public static double luai_nummul(double a, double b) {
		return ((a) * (b));
	}

	public static double luai_numdiv(double a, double b) {
		return ((a) / (b));
	}

	public static double luai_nummod(double a, double b) {
		return ((a) - Math.floor((a) / (b)) * (b));
	}

	public static double luai_numpow(double a, double b) {
		return (Math.pow(a, b));
	}

	public static double luai_numunm(double a) {
		return (-(a));
	}

	public static boolean luai_numeq(double a, double b) {
		return ((a) == (b));
	}

	public static boolean luai_numlt(double a, double b) {
		return ((a) < (b));
	}

	public static boolean luai_numle(double a, double b) {
		return ((a) <= (b));
	}

	public static boolean luai_numisnan(double a) {
		return Double.isNaN(a);
	}

	// #endif

	/*
	 * @@ lua_number2int is a macro to convert double to int.
	 * 
	 * @@ lua_number2integer is a macro to convert double to int.* CHANGE them
	 * if you know a faster way to convert a double to* int (with any rounding
	 * method and without throwing errors) in your* system. In Pentium machines,
	 * a naive typecast from double to int* in C is extremely slow, so any
	 * alternative is worth trying.
	 */

	/* On a Pentium, resort to a trick */
	// /#if defined(LUA_NUMBER_DOUBLE) && !defined(LUA_ANSI) &&
	// !defined(__SSE2__) && \
	// (defined(__i386) || defined (_M_IX86) || defined(__i386__))

	/* On a Microsoft compiler, use assembler */
	// /#if defined(_MSC_VER)

	// /#define lua_number2int(i,d) __asm fld d __asm fistp i
	// /#define lua_number2integer(i,n) lua_number2int(i, n)

	/*
	 * the next trick should work on any Pentium, but sometimes clashes with a
	 * DirectX idiosyncrasy
	 */
	// /#else

	// union luai_Cast { double l_d; long l_l; };
	// /#define lua_number2int(i,d) \
	// { volatile union luai_Cast u; u.l_d = (d) + 6755399441055744.0; (i) =
	// u.l_l; }
	// /#define lua_number2integer(i,n) lua_number2int(i, n)

	// /#endif

	/* this option always works, but may be slow */
	// /#else
	// /#define lua_number2int(i,d) ((i)=(int)(d))
	// /#define lua_number2integer(i,d) ((i)=(int)(d))

	// /#endif

	public static void lua_number2int(RefObject<Integer> i, double d) {
		i.argvalue = (int) d;
	}

	public static void lua_number2integer(RefObject<Integer> i, double n) {
		i.argvalue = (int) n;
	}

	/* }================================================================== */

	/*
	 * @@ LUAI_USER_ALIGNMENT_T is a type that requires maximum alignment.*
	 * CHANGE it if your system requires alignments larger than double. (For*
	 * instance, if your system supports long doubles and they must be* aligned
	 * in 16-byte boundaries, then you should add long double in the* union.)
	 * Probably you do not need to change this.
	 */
	// /#define LUAI_USER_ALIGNMENT_T union { double u; void *s; long l; }

	public static class LuaException extends RuntimeException {
		public lua_State L;
		public lua_longjmp c;

		public LuaException(lua_State L, lua_longjmp c) {
			this.L = L;
			this.c = c;
		}
	}

	/*
	 * @@ LUAI_THROW/LUAI_TRY define how Lua does exception handling.* CHANGE
	 * them if you prefer to use longjmp/setjmp even with C++* or if want/don't
	 * to use _longjmp/_setjmp instead of regular* longjmp/setjmp. By default,
	 * Lua handles errors with exceptions when* compiling as C++ code, with
	 * _longjmp/_setjmp when asked to use them,* and with longjmp/setjmp
	 * otherwise.
	 */
	// /#if defined(__cplusplus)
	/*** C++ exceptions */
	public static void LUAI_THROW(lua_State L, lua_longjmp c) {
		if(LuaEngine.getInstance().ThrowExceptions())
			throw new LuaException(L, c);
	}

	// /#define LUAI_TRY(L,c,a) try { a } catch(...) \
	// { if ((c).status == 0) (c).status = -1; }
	public static void LUAI_TRY(lua_State L, lua_longjmp c, Object a) {
		if (c.status == 0) {
			c.status = -1;
		}
	}

	// /#define luai_jmpbuf int /* dummy variable */

	// /#elif defined(LUA_USE_ULONGJMP)
	/*** in Unix, try _longjmp/_setjmp (more efficient) */
	// /#define LUAI_THROW(L,c) _longjmp((c).b, 1)
	// /#define LUAI_TRY(L,c,a) if (_setjmp((c).b) == 0) { a }
	// /#define luai_jmpbuf jmp_buf

	// /#else
	/*** default handling with long jumps */
	// public static void LUAI_THROW(lua_State L, lua_longjmp c) { c.b(1); }
	// /#define LUAI_TRY(L,c,a) if (setjmp((c).b) == 0) { a }
	// /#define luai_jmpbuf jmp_buf

	// /#endif

	/*
	 * @@ LUA_MAXCAPTURES is the maximum number of captures that a pattern
	 * 
	 * @* can do during pattern-matching.* CHANGE it if you need more captures.
	 * This limit is arbitrary.
	 */
	public static final int LUA_MAXCAPTURES = 32;

	/*
	 * @@ lua_tmpnam is the function that the OS library uses to create a
	 * 
	 * @* temporary name.
	 * 
	 * @@ LUA_TMPNAMBUFSIZE is the maximum size of a name created by lua_tmpnam.
	 * * CHANGE them if you have an alternative to tmpnam (which is considered*
	 * insecure) or if you want the original tmpnam anyway. By default, Lua*
	 * uses tmpnam except when POSIX is available, where it uses mkstemp.
	 */
	// FUCK TODO TASK: There is no preprocessor in Java:
	/*
	 * //#if loslib_c || luaall_c
	 * 
	 * //FUCK TODO TASK: There is no preprocessor in Java: //#if LUA_USE_MKSTEMP
	 * ///#include <unistd.h> public static final int LUA_TMPNAMBUFSIZE = 32;
	 * ///#define lua_tmpnam(b,e) { \ // strcpy(b, "/tmp/lua_XXXXXX"); \ // e =
	 * mkstemp(b); \ // if (e != -1) close(e); \ // e = (e == -1); }
	 * 
	 * //#else public static final int LUA_TMPNAMBUFSIZE = L_tmpnam; public
	 * static void lua_tmpnam(CharPtr b, int e) { e = (tmpnam(b) == null) ? 1 :
	 * 0; } //#endif
	 * 
	 * //#endif
	 */

	/*
	 * @@ lua_popen spawns a new process connected to the current one through
	 * 
	 * @* the file streams.* CHANGE it if you have a way to implement it in your
	 * system.
	 */
	// /#if LUA_USE_POPEN

	// /#define lua_popen(L,c,m) ((void)L, fflush(null), popen(c,m))
	// /#define lua_pclose(L,file) ((void)L, (pclose(file) != -1))

	// /#elif LUA_WIN

	// /#define lua_popen(L,c,m) ((void)L, _popen(c,m))
	// /#define lua_pclose(L,file) ((void)L, (_pclose(file) != -1))

	// /#else

	public static Object lua_popen(lua_State L, CharPtr c, CharPtr m) {
		luaL_error(L, LUA_QL("popen") + " not supported");
		return null;
	}

	public static int lua_pclose(lua_State L, InputStream file) {
		return 0;
	}

	public static int lua_pclose(lua_State L, OutputStream file) {
		return 0;
	}

	// /#endif

	/*
	 * @@ LUA_DL_* define which dynamic-library system Lua should use.* CHANGE
	 * here if Lua has problems choosing the appropriate* dynamic-library system
	 * for your platform (either Windows' DLL, Mac's* dyld, or Unix's dlopen).
	 * If your system is some kind of Unix, there* is a good chance that it has
	 * dlopen, so LUA_DL_DLOPEN will work for* it. To use dlopen you also need
	 * to adapt the src/Makefile (probably* adding -ldl to the linker options),
	 * so Lua does not select it* automatically. (When you change the makefile
	 * to add -ldl, you must* also add -DLUA_USE_DLOPEN.)* If you do not want
	 * any kind of dynamic library, undefine all these* options.* By default,
	 * _WIN32 gets LUA_DL_DLL and MAC OS X gets LUA_DL_DYLD.
	 */
	// /#if LUA_USE_DLOPEN
	// /#define LUA_DL_DLOPEN
	// /#endif

	// /#if LUA_WIN
	// /#define LUA_DL_DLL
	// /#endif

	/*
	 * @@ LUAI_EXTRASPACE allows you to add user-specific data in a lua_State
	 * 
	 * @* (the data goes just *before* the lua_State pointer).* CHANGE (define)
	 * this if you really need that. This value must be* a multiple of the
	 * maximum alignment required for your machine.
	 */
	public static final int LUAI_EXTRASPACE = 0;

	/*
	 * @@ luai_userstate* allow user-specific actions on threads.* CHANGE them
	 * if you defined LUAI_EXTRASPACE and need to do something* extra when a
	 * thread is created/deleted/resumed/yielded.
	 */
	public static void luai_userstateopen(lua_State L) {
	}

	public static void luai_userstateclose(lua_State L) {
	}

	public static void luai_userstatethread(lua_State L, lua_State L1) {
	}

	public static void luai_userstatefree(lua_State L) {
	}

	public static void luai_userstateresume(lua_State L, int n) {
	}

	public static void luai_userstateyield(lua_State L, int n) {
	}

	/*
	 * @@ LUA_INTFRMLEN is the length modifier for integer conversions
	 * 
	 * @* in 'string.format'.
	 * 
	 * @@ LUA_INTFRM_T is the integer type correspoding to the previous length
	 * 
	 * @* modifier.* CHANGE them if your system supports long long or does not
	 * support long.
	 */

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if LUA_USELONGLONG

	// public static final String LUA_INTFRMLEN = "ll";
	// /#define LUA_INTFRM_T long long

	// #else

	// /#define LUA_INTFRM_T long /* declared in dotnet build with using
	// statement */

	// #endif

	/* =================================================================== */

	/*
	 * * Local configuration. You can use this space to add your redefinitions*
	 * without modifying the main part of the file.
	 */

	// misc stuff needed for the compile

	public static boolean isalpha(char c) {
		return Character.isLetter(c);
	}

	public static boolean iscntrl(char c) {
		return Character.isISOControl(c);
	}

	public static boolean isdigit(char c) {
		return Character.isDigit(c);
	}

	public static boolean islower(char c) {
		return Character.isLowerCase(c);
	}

	public static boolean ispunct(char c) {
		return (new String(".,;:?!(){}\"\"")).indexOf(c) >= 0;
		// return Character.IsPunctuation(c);
	}

	public static boolean isspace(char c) {
		return (c == ' ') || (c >= (char) 0x09 && c <= (char) 0x0D);
	}

	public static boolean isupper(char c) {
		return Character.isUpperCase(c);
	}

	public static boolean isalnum(char c) {
		return Character.isLetterOrDigit(c);
	}

	public static boolean isxdigit(char c) {
		return (new String("0123456789ABCDEFabcdef")).indexOf(c) >= 0;
	}

	public static boolean isalpha(int c) {
		return Character.isLetter((char) c);
	}

	public static boolean iscntrl(int c) {
		return Character.isISOControl((char) c);
	}

	public static boolean isdigit(int c) {
		return Character.isDigit((char) c);
	}

	public static boolean islower(int c) {
		return Character.isLowerCase((char) c);
	}

	public static boolean ispunct(int c) // *not* the same as Char.IsPunctuation
	{
		return ((char) c != ' ') && !isalnum((char) c);
	}

	public static boolean isspace(int c) {
		return ((char) c == ' ')
				|| ((char) c >= (char) 0x09 && (char) c <= (char) 0x0D);
	}

	public static boolean isupper(int c) {
		return Character.isUpperCase((char) c);
	}
 
	public static boolean isalnum(int c) {
		if((c >= 97 /*a*/ && c <= 122 /*z*/) 
				|| (c >= 65 /*A*/ && c <= 90 /*Z*/)
				|| (c >= 48 /*0*/ && c <= 57 /*9*/))
			return true;
		return false;
	}

	public static char tolower(char c) {
		return Character.toLowerCase(c);
	}

	public static char toupper(char c) {
		return Character.toUpperCase(c);
	}

	public static char tolower(int c) {
		return Character.toLowerCase((char) c);
	}

	public static char toupper(int c) {
		return Character.toUpperCase((char) c);
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static ulong strtoul(CharPtr s, out CharPtr end,
	// int base_)
	public static long strtoul(CharPtr s, RefObject<CharPtr> end, int base_) {
		try {
			end.argvalue = new CharPtr(s.chars, s.index);

			// skip over any leading whitespace
			while (end.argvalue.getItem(0) == ' ') {
				end.argvalue = end.argvalue.next();
			}

			// ignore any leading 0x
			if ((end.argvalue.getItem(0) == '0')
					&& (end.argvalue.getItem(1) == 'x')) {
				end.argvalue = end.argvalue.next().next();
			} else if ((end.argvalue.getItem(0) == '0')
					&& (end.argvalue.getItem(1) == 'X')) {
				end.argvalue = end.argvalue.next().next();
			}

			// do we have a leading + or - sign?
			boolean negate = false;
			if (end.argvalue.getItem(0) == '+') {
				end.argvalue = end.argvalue.next();
			} else if (end.argvalue.getItem(0) == '-') {
				negate = true;
				end.argvalue = end.argvalue.next();
			}

			// loop through all chars
			boolean invalid = false;
			boolean had_digits = false;
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: ulong result = 0;
			long result = 0;
			while (true) {
				// get this char
				char ch = end.argvalue.getItem(0);

				// which digit is this?
				int this_digit = 0;
				if (isdigit(ch)) {
					this_digit = ch - '0';
				} else if (isalpha(ch)) {
					this_digit = tolower(ch) - 'a' + 10;
				} else {
					break;
				}

				// is this digit valid?
				if (this_digit >= base_) {
					invalid = true;
				} else {
					had_digits = true;
					result = result * (long) base_ + (long) this_digit;
				}

				end.argvalue = end.argvalue.next();
			}

			// were any of the digits invalid?
			if (invalid || (!had_digits)) {
				end.argvalue = s;
				return Long.MAX_VALUE;
			}

			// if the value was a negative then negate it here
			if (negate) {
				result = (long) -(long) result;
			}

			// ok, we're done
			return (long) result;
		} catch (java.lang.Exception e) {
			end.argvalue = s;
			return 0;
		}
	}

	public static void putchar(char ch) {
		System.out.print(ch);
	}

	public static void putchar(int ch) {
		System.out.print((char) ch);
	}

	public static boolean isprint(byte c) {
		return (c >= (byte) ' ') && (c <= (byte) 127);
	}

	public static int parse_scanf(String str, CharPtr fmt, Object... argp) {
		int parm_index = 0;
		int index = 0;
		while (fmt.getItem(index) != 0) {
			if (fmt.getItem(index++) == '%') {
				switch (fmt.getItem(index++)) {
				case 's': {
					argp[parm_index++] = str;
					break;
				}
				case 'c': {
					argp[parm_index++] = str.charAt(0);// Convert.ToChar(str);
					break;
				}
				case 'd': {
					argp[parm_index++] = Integer.parseInt(str);
					break;
				}
				case 'l': {
					argp[parm_index++] = Double.parseDouble(str);
					break;
				}
				case 'f': {
					argp[parm_index++] = Double.parseDouble(str);
					break;
				}
					// case 'p':
					// {
					// result += "(pointer)";
					// break;
					// }
				}
			}
		}
		return parm_index;
	}

	public static void printf(CharPtr str, Object... argv) {
		Tools.printf(str.toString(), argv);
	}

	public static void printf(String str, Object... argv) {
		Tools.printf(str, argv);
	}

	public static void sprintf(CharPtr buffer, CharPtr str, Object... argv) {
		String temp = Tools.sprintf(str.toString(), argv);
		strcpy(buffer, temp);
	}

	public static void sprintf(CharPtr buffer, String strS, Object... argv) {
		CharPtr str = new CharPtr(strS);
		String temp = Tools.sprintf(str.toString(), argv);
		strcpy(buffer, temp);
	}

	public static int fprintf(InputStream stream, CharPtr str,
			Object... argv) {
		String result = Tools.sprintf(str.toString(), argv);
		char[] chars = result.toCharArray();
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) {
			bytes[i] = (byte) chars[i];
		}
		// TODO fixme
		// stream.Write(bytes, 0, bytes.length);
		return 1;
	}

	public static int fprintf(OutputStream stream, String strS,
			Object... argv) {
		CharPtr str = new CharPtr(strS);
		String result = Tools.sprintf(str.toString(), argv);
		char[] chars = result.toCharArray();
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) {
			bytes[i] = (byte) chars[i];
		}
		try
		{
			stream.write(bytes, 0, bytes.length);
		}
		catch(IOException e)
		{
			Tools.LogException("Lua.java", e);
		}
		return 1;
	}

	public static final int EXIT_SUCCESS = 0;
	public static final int EXIT_FAILURE = 1;

	public static int errno() {
		return -1; // todo: fix this - mjf
	}

	public static CharPtr strerror(int error) {
		return new CharPtr(String.format("error #%1$s", error)); // todo: check
																	// how this
																	// works -
																	// mjf
	}

	public static CharPtr getenv(CharPtr envname) {
		if(envname.toString().compareTo("PWD") == 0)
		{
			return new CharPtr(Defines.GetPathForLua());
		}
		return null;
	}

	public static class CharPtr {
		public char[] chars;
		public int index;
		
		public final char getItem() {
			int val = index;
			if(val >= chars.length)
				val = chars.length - 1;
			return chars[index];
		}

		public final char getItem(int offset) {
			int val = index + offset;
			if(val >= chars.length)
				val = chars.length - 1;
			return chars[val];
		}

		public final void setItem(int offset, char value) {
			chars[index + offset] = value;
		}

		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public char getItem(uint offset)
		/*
		 * public final char getItem(int offset) { return chars[index + offset];
		 * }
		 */
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public void setItem(uint offset, char value)
		/*
		 * public final void setItem(int offset, char value) { chars[index +
		 * offset] = value; }
		 */
		public final char getItem(long offset) {
			return chars[index + (int) offset];
		}

		public final void setItem(long offset, char value) {
			chars[index + (int) offset] = value;
		}

		// FUCK TODO TASK: The following operator overload is not converted by
		// Fuck:
		/*
		 * public static implicit operator CharPtr(String str) { return new
		 * CharPtr(str); }
		 */
		// FUCK TODO TASK: The following operator overload is not converted by
		// Fuck:
		/*
		 * public static implicit operator CharPtr(char[] chars) { return new
		 * CharPtr(chars); }
		 */

		public CharPtr() {
			this.chars = null;
			this.index = 0;
		}

		public CharPtr(String str) {
			/*char[] charr = (str + '\0').toCharArray();
			//this.chars = new Character[charr.length];
			this.chars = new char[charr.length];
			for (int i = 0; i < charr.length; i++)
				chars[i] = charr[i];*/
			this.chars = (str + '\0').toCharArray();
		}

		public CharPtr(CharPtr ptr) {
			this.chars = ptr.chars;
			this.index = ptr.index;
		}

		public CharPtr(CharPtr ptr, int index) {
			this.chars = ptr.chars;
			this.index = index;
		}

		public CharPtr(char[] chars) {
			if (chars[chars.length - 1] != '\0') {
				this.chars = new char[chars.length + 1];
				for (int i = 0; i < chars.length; i++)
					this.chars[i] = chars[i];
				this.chars[chars.length] = '\0';
			} else
				this.chars = chars;
			this.index = 0;
		}

		public CharPtr(char[] chars, int index) {
			if (chars[chars.length - 1] != '\0') {
				this.chars = new char[chars.length + 1];
				for (int i = 0; i < chars.length; i++)
					this.chars[i] = chars[i];
				this.chars[chars.length] = '\0';
			} else
				this.chars = chars;
			this.index = index;
		}

		public CharPtr(Character[] chars) {
			if (chars[chars.length - 1] != null && chars[chars.length - 1] != '\0') {
				this.chars = new char[chars.length + 1];
				for (int i = 0; i < chars.length; i++)
					this.chars[i] = chars[i];
				this.chars[chars.length] = '\0';
			} else {
				this.chars = new char[chars.length];
				for (int i = 0; i < chars.length; i++)
					chars[i] = chars[i];
			}
			this.index = 0;
		}

		public CharPtr(Character[] chars, int index) {
			if (chars[chars.length - 1] != null && chars[chars.length - 1] != '\0') {
				this.chars = new char[chars.length + 1];
				for (int i = 0; i < chars.length; i++)
					this.chars[i] = chars[i];
				this.chars[chars.length] = '\0';
			} else {
				this.chars = new char[chars.length];
				for (int i = 0; i < chars.length; i++)
					chars[i] = chars[i];
			}
			this.index = index;
		}

		public CharPtr(int ptr) {
			this.chars = new char[0];
			this.index = 0;
		}

		/*
		 * public static CharPtr OpAddition(CharPtr ptr, int offset) { return
		 * new CharPtr(ptr.chars, ptr.index+offset); } public static CharPtr
		 * OpSubtraction(CharPtr ptr, int offset) { return new
		 * CharPtr(ptr.chars, ptr.index-offset); }
		 */
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public static CharPtr operator +(CharPtr ptr, uint
		// offset)
		public static CharPtr OpAddition(CharPtr ptr, int offset) {
			return new CharPtr(ptr.chars, ptr.index + (int) offset);
		}

		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public static CharPtr operator -(CharPtr ptr, uint
		// offset)
		public static CharPtr OpSubtraction(CharPtr ptr, int offset) {
			return new CharPtr(ptr.chars, ptr.index - (int) offset);
		}

		public final void inc() {
			this.index++;
		}

		public final void dec() {
			this.index--;
		}

		public final CharPtr next() {
			return new CharPtr(this.chars, this.index + 1);
		}

		public final CharPtr prev() {
			return new CharPtr(this.chars, this.index - 1);
		}

		public final CharPtr add(int ofs) {
			return new CharPtr(this.chars, this.index + ofs);
		}

		public final CharPtr sub(int ofs) {
			return new CharPtr(this.chars, this.index - ofs);
		}

		public static boolean OpEquality(CharPtr ptr, char ch) {
			return ptr.getItem(0) == ch;
		}

		public static boolean OpEquality(char ch, CharPtr ptr) {
			return ptr.getItem(0) == ch;
		}

		public static boolean OpInequality(CharPtr ptr, char ch) {
			return ptr.getItem(0) != ch;
		}

		public static boolean OpInequality(char ch, CharPtr ptr) {
			return ptr.getItem(0) != ch;
		}

		public static CharPtr OpAddition(CharPtr ptr1, CharPtr ptr2) {
			String result = "";
			for (int i = 0; ptr1.getItem(i) != '\0'; i++) {
				result += ptr1.getItem(i);
			}
			for (int i = 0; ptr2.getItem(i) != '\0'; i++) {
				result += ptr2.getItem(i);
			}
			return new CharPtr(result);
		}

		public static int OpSubtraction(CharPtr ptr1, CharPtr ptr2) {
			assert ptr1.chars == ptr2.chars;
			return ptr1.index - ptr2.index;
		}

		public static boolean OpLessThan(CharPtr ptr1, CharPtr ptr2) {
			assert ptr1.chars == ptr2.chars;
			return ptr1.index < ptr2.index;
		}

		public static boolean OpLessThanOrEqual(CharPtr ptr1, CharPtr ptr2) {
			assert ptr1.chars == ptr2.chars;
			return ptr1.index <= ptr2.index;
		}

		public static boolean OpGreaterThan(CharPtr ptr1, CharPtr ptr2) {
			assert ptr1.chars == ptr2.chars;
			return ptr1.index > ptr2.index;
		}

		public static boolean OpGreaterThanOrEqual(CharPtr ptr1, CharPtr ptr2) {
			assert ptr1.chars == ptr2.chars;
			return ptr1.index >= ptr2.index;
		}

		public static boolean OpEquality(CharPtr ptr1, CharPtr ptr2) {
			Object o1 = (CharPtr) ((ptr1 instanceof CharPtr) ? ptr1 : null);
			Object o2 = (CharPtr) ((ptr2 instanceof CharPtr) ? ptr2 : null);
			if ((o1 == null) && (o2 == null)) {
				return true;
			}
			if (o1 == null) {
				return false;
			}
			if (o2 == null) {
				return false;
			}
			return (ptr1.chars == ptr2.chars) && (ptr1.index == ptr2.index);
		}

		public static boolean OpInequality(CharPtr ptr1, CharPtr ptr2) {
			return !(ptr1 == ptr2);
		}
		
		public void setByteArray(byte []values)
		{
			index = 0;
			//chars = new Character[values.length + 1];
			chars = new char[values.length + 1];
			for(int i = 0; i < values.length; i++)
			{
				chars[i] = (char) values[i];
			}
			chars[values.length] = '\0';
		}
		
		public byte[] toByteArray()
		{
			Charset charset = Charset.forName("US-ASCII");
			CharsetEncoder encoder = charset.newEncoder();
			ByteBuffer rylaiedo = ByteBuffer.allocate(0);
			try
			{
				rylaiedo = encoder.encode(CharBuffer.wrap(toCharArray()));
			}
			catch(CharacterCodingException e)
			{
				e.printStackTrace();
			}
			return rylaiedo.array();
			/*byte[] b = new byte[chars.length];
			 for (int i = 0; i < b.length; i++) {
			  b[i] = (byte) ;
			 }
			 return b;*/
		}
		
		public byte[] toByteArrayNoFinisher()
		{
			Charset charset = Charset.forName("US-ASCII");
			CharsetEncoder encoder = charset.newEncoder();
			ByteBuffer rylaiedo = ByteBuffer.allocate(0);
			char[] carr = toCharArray();
			try
			{
				//encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
				rylaiedo = encoder.encode(CharBuffer.wrap(carr));
			}
			catch(CharacterCodingException e)
			{
				//rylaiedo = Unicode2ASCII.toJAVASeq(CharBuffer.wrap(toCharArray()));
				Charset charsetU = Charset.forName("UTF-8");
				CharsetEncoder encoderU = charsetU.newEncoder();
				try
				{
					encoderU.onUnmappableCharacter(CodingErrorAction.REPLACE);
					rylaiedo = encoderU.encode(CharBuffer.wrap(carr));
				}
				catch(CharacterCodingException e1)
				{
					e1.printStackTrace();
				}
				Log.e("Lua.java", "Cannot encode tostring");
				e.printStackTrace();
			}
			byte[] arr = rylaiedo.array();
			if(arr.length > 0)
			{
				if(arr[arr.length - 1] == 0)
				{
					byte[] b = new byte[arr.length - 1];
					for (int i = 0; i < b.length; i++)
						b[i] = arr[i];
					
					return b;
				}
			}
			return arr;
		}
		
		public char[] toCharArray()
		{
			char[] b = new char[chars.length];
			 for (int i = 0; i < b.length; i++) {
			  //b[i] = (char) chars[i].charValue();
				 b[i] = chars[i];
			 }
			 return b;
		}

		@Override
		public boolean equals(Object o) {
			return this == ((CharPtr) ((o instanceof CharPtr) ? o : null));
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			//String result = "";
			StringBuilder result = new StringBuilder(chars.length);
			for (int i = index; (i < chars.length) && (chars[i] != '\0'); i++) {
				//result += chars[i];
				result.append(chars[i]);
			}
			return result.toString();
		}
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int memcmp(CharPtr ptr1, CharPtr ptr2, uint
	// size)
	/*
	 * public static int memcmp(CharPtr ptr1, CharPtr ptr2, int size) { return
	 * memcmp(ptr1, ptr2, (int)size); }
	 */
	public static int memcmp(CharPtr ptr1, CharPtr ptr2, int size) {
		for (int i = 0; i < size; i++) {
			if (ptr1.getItem(i) != ptr2.getItem(i)) {
				if (ptr1.getItem(i) < ptr2.getItem(i)) {
					return -1;
				} else {
					return 1;
				}
			}
		}
		return 0;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static CharPtr memchr(CharPtr ptr, char c, uint
	// count)
	public static CharPtr memchr(CharPtr ptr, char c, int count) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: for (uint i = 0; i < count; i++)
		for (int i = 0; i < count; i++) {
			if (ptr.getItem(i) == c) {
				return new CharPtr(ptr.chars, (int) (ptr.index + i));
			}
		}
		return null;
	}

	public static CharPtr strpbrk(CharPtr str, CharPtr charset) {
		for (int i = 0; str.getItem(i) != '\0'; i++) {
			for (int j = 0; charset.getItem(j) != '\0'; j++) {
				if (str.getItem(i) == charset.getItem(j)) {
					return new CharPtr(str.chars, str.index + i);
				}
			}
		}
		return null;
	}

	public static CharPtr strpbrk(CharPtr str, String charsetS) {
		CharPtr charset = new CharPtr(charsetS);
		for (int i = 0; str.getItem(i) != '\0'; i++) {
			for (int j = 0; charset.getItem(j) != '\0'; j++) {
				if (str.getItem(i) == charset.getItem(j)) {
					return new CharPtr(str.chars, str.index + i);
				}
			}
		}
		return null;
	}

	// find c in str
	public static CharPtr strchr(CharPtr str, char c) {
		for (int index = str.index; str.chars[index] != 0; index++) {
			if (str.chars[index] == c) {
				return new CharPtr(str.chars, index);
			}
		}
		return null;
	}

	public static CharPtr strchr(String strS, char c) {
		CharPtr str = new CharPtr(strS);
		for (int index = str.index; str.chars[index] != 0; index++) {
			if (str.chars[index] == c) {
				return new CharPtr(str.chars, index);
			}
		}
		return null;
	}

	public static CharPtr strcpy(CharPtr dst, CharPtr src) {
		int i;
		for (i = 0; src.getItem(i) != '\0'; i++) {
			dst.setItem(i, src.getItem(i));
		}
		dst.setItem(i, '\0');
		return dst;
	}

	public static CharPtr strcpy(CharPtr dst, String srcS) {
		CharPtr src = new CharPtr(srcS);
		int i;
		for (i = 0; src.getItem(i) != '\0'; i++) {
			dst.setItem(i, src.getItem(i));
		}
		dst.setItem(i, '\0');
		return dst;
	}

	public static CharPtr strcat(CharPtr dst, CharPtr src) {
		int dst_index = 0;
		while (dst.getItem(dst_index) != '\0') {
			dst_index++;
		}
		int src_index = 0;
		while (src.getItem(src_index) != '\0') {
			dst.setItem(dst_index++, src.getItem(src_index++));
		}
		dst.setItem(dst_index++, '\0');
		return dst;
	}

	public static CharPtr strcat(CharPtr dst, String srcS) {
		CharPtr src = new CharPtr(srcS);
		int dst_index = 0;
		while (dst.getItem(dst_index) != '\0') {
			dst_index++;
		}
		int src_index = 0;
		while (src.getItem(src_index) != '\0') {
			dst.setItem(dst_index++, src.getItem(src_index++));
		}
		dst.setItem(dst_index++, '\0');
		return dst;
	}

	public static CharPtr strncat(CharPtr dst, CharPtr src, int count) {
		int dst_index = 0;
		while (dst.getItem(dst_index) != '\0') {
			dst_index++;
		}
		int src_index = 0;
		while ((src.getItem(src_index) != '\0') && (count-- > 0)) {
			dst.setItem(dst_index++, src.getItem(src_index++));
		}
		dst.setItem(dst_index++, '\0');
		return dst;
	}

	public static int IndexOfAny(String s, char[] arr) {
		char[] sarr = s.toCharArray();
		for (int i = 0; i < sarr.length; i++) {
			for (int j = 0; j < arr.length; j++) {
				if (arr[j] == sarr[i])
					return i;
			}
		}
		return -1;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static uint strcspn(CharPtr str, CharPtr charset)
	public static int strcspn(CharPtr str, CharPtr charset) {
		int index = IndexOfAny(str.toString(),
				(charset.toString().toCharArray()));
		if (index < 0) {
			index = str.toString().length();
		}
		return (int) index;
	}

	public static int strcspn(CharPtr str, String charsetS) {
		CharPtr charset = new CharPtr(charsetS);
		int index = IndexOfAny(str.toString(),
				(charset.toString().toCharArray()));
		if (index < 0) {
			index = str.toString().length();
		}
		return (int) index;
	}

	public static CharPtr strncpy(CharPtr dst, CharPtr src, int length) {
		int index = 0;
		while ((src.getItem(index) != '\0') && (index < length)) {
			dst.setItem(index, src.getItem(index));
			index++;
		}
		while (index < length) {
			dst.setItem(index++, '\0');
		}
		return dst;
	}

	public static int strlen(CharPtr str) {
		int index = 0;
		while (str.getItem(index) != '\0') {
			index++;
		}
		return index;
		/*int length = str.chars.length;
		if(str.chars[length - 1] == '\0')
			length--;
		return length;*/
	}

	public static double fmod(double a, double b) {
		float quotient = (int) Math.floor(a / b);
		return a - quotient * b;
	}

	public static double modf(double a, RefObject<Double> b) {
		b.argvalue = Math.floor(a);
		return a - Math.floor(a);
	}

	public static long lmod(double a, double b) {
		return (long) a % (long) b;
	}

	public static int getc(InputStream f) {
		// TODO
		try
		{
			return f.read();
		}
		catch(IOException e)
		{
			return 0;
		}
	}

	public static void ungetc(int c, InputStream f) {
		// TODO
		/*
		 * if (f.Position > 0) { f.Seek(-1, SeekOrigin.Current); }
		 */
		try
		{
			if(f instanceof PushbackInputStream)
				((PushbackInputStream)f).unread(c);
			else
				f.reset();
		}
		catch(IOException e)
		{
			Tools.LogException("Lua.java", e);
		}
	}

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if XBOX
	/*
	 * public static Stream stdout; public static Stream stdin; public static
	 * Stream stderr;
	 */
	// #elif PocketPC
	public static OutputStream stdout = new LuaLogStream(0);
	public static OutputStream dbgstdout = new LuaLogStream(0);
	public static InputStream stdin;
	public static OutputStream stderr = new LuaLogStream(1);
	// #else
	/*
	 * public static Stream stdout = Console.OpenStandardOutput(); public static
	 * Stream stdin = Console.OpenStandardInput(); public static Stream stderr =
	 * Console.OpenStandardError();
	 */
	// #endif
	public static int EOF = -1;

	public static void fputs(CharPtr str, OutputStream stream) {
		if(stream == null)
			Log.i("LuaEngine", str.toString());
		else
		{
			try
			{
				stream.write(str.toString().getBytes("UTF-8"));
			}
			catch(UnsupportedEncodingException e)
			{
				Tools.LogException("Lua.java", e);
			}
			catch(IOException e)
			{
				Tools.LogException("Lua.java", e);
			}
		}
	}

	public static int feof(InputStream s) {
		InputStream fs = (InputStream) s;
		try {
			return (fs.available() == 0 ? 1 : 0);
		} catch (IOException e) {
			Tools.LogException("Lua.java", e);
		}
		return 0;
	}

	public static int fread(CharPtr ptr, int size, int num, InputStream stream) {
		int num_bytes = num * size;
		int result = 0;
		byte[] bytes = new byte[num_bytes]; 
		try 
		{
			result = stream.read(bytes, 0, num_bytes);
			CharPtr ptrTest = new CharPtr(new String(bytes, 0, result, "UTF-8"));
			for (int i = 0; i < result; i++) 
			{ 
				ptr.setItem(i, ptrTest.getItem(i)); 
			}
			return result/size;
		}
		catch (Exception e) 
		{
			Tools.LogException("Lua.java", e);
			return 0;
		}		
	}

	public static int fwrite(CharPtr ptr, int size, int num,
			OutputStream stream) 
	{
		int num_bytes = num * size;
		try 
		{
			byte[] bytes = ptr.toString().getBytes("UTF-8");
			stream.write(bytes, 0, num_bytes);
		} 
		catch (Exception e) 
		{
			Tools.LogException("Lua.java", e);
			return 0;
		}
		return num;
	}

	public static int strcmp(CharPtr s1, CharPtr s2) {
		if (s1 == s2) {
			return 0;
		}
		if (s1 == null) {
			return -1;
		}
		if (s2 == null) {
			return 1;
		}

		for (int i = 0;; i++) {
			if (s1.getItem(i) != s2.getItem(i)) {
				if (s1.getItem(i) < s2.getItem(i)) {
					return -1;
				} else {
					return 1;
				}
			}
			if (s1.getItem(i) == '\0') {
				return 0;
			}
		}
	}

	public static int strcmp(CharPtr s1, String s2S) {
		CharPtr s2 = new CharPtr(s2S);
		if (s1 == s2) {
			return 0;
		}
		if (s1 == null) {
			return -1;
		}
		if (s2 == null) {
			return 1;
		}

		for (int i = 0;; i++) {
			if (s1.getItem(i) != s2.getItem(i)) {
				if (s1.getItem(i) < s2.getItem(i)) {
					return -1;
				} else {
					return 1;
				}
			}
			if (s1.getItem(i) == '\0') {
				return 0;
			}
		}
	}

	public static CharPtr fgets(CharPtr str, InputStream stream) {
		// TODO as
		/*
		 * int index = 0; try { while (true) { str.setItem(index,
		 * (char)stream.ReadByte()); if (str.getItem(index) == '\n') { break; }
		 * if (index >= str.chars.length) { break; } index++; } } catch
		 * (java.lang.Exception e) { } return str;
		 */
		int index = 0; 
		try 
		{ 
			while (true) 
			{ 
				str.setItem(index, (char)stream.read()); 
				if(str.getItem(index) == '\n')
				{ 
					break;
				}
				if (index >= str.chars.length)
				{ 
					break;
				} 
				index++; 
			} 
		}
		catch(Exception e) 
		{ 
			Tools.LogException("Lua.java", e);
		} 
		return str;
	}

	public static double frexp(double x, RefObject<Integer> expptr) {
		// FUCK TODO TASK: There is no preprocessor in Java:
		// #if XBOX
		// expptr.argvalue = (int)(Math.log(x) / Math.log(2)) + 1;
		// #elif PocketPC
		expptr.argvalue = (int) (Math.log(x) / Math.log(2)) + 1;
		// #else
		// expptr.argvalue = (int)Math.log(x, 2) + 1;
		// #endif
		double s = x / Math.pow(2, expptr.argvalue);
		return s;
	}

	public static double ldexp(double x, int expptr) {
		return x * Math.pow(2, expptr);
	}

	public static CharPtr strstr(CharPtr str, CharPtr substr) {
		int index = str.toString().indexOf(substr.toString());
		if (index < 0) {
			return null;
		}
		return new CharPtr(CharPtr.OpAddition(str, index));// return new
															// CharPtr(str +
															// index);
	}

	public static CharPtr strrchr(CharPtr str, char ch) {
		int index = str.toString().lastIndexOf(ch);
		if (index < 0) {
			return null;
		}
		return CharPtr.OpAddition(str, index);// return str + index;
	}

	public static Object fopen(CharPtr filenameP, Object mode) {
		/*
		 * String str = filename.toString(); FileMode filemode = FileMode.Open;
		 * FileAccess fileaccess = (FileAccess)0; for (int i=0; mode.getItem(i)
		 * != '\0'; i++) { switch (mode.getItem(i)) { case 'r': fileaccess =
		 * fileaccess | FileAccess.Read; if (!(new java.io.File(str)).isFile())
		 * { return null; } break;
		 * 
		 * case 'w': filemode = FileMode.Create; fileaccess = fileaccess |
		 * FileAccess.Write; break; } } try { return new FileStream(str,
		 * filemode, fileaccess); } catch (java.lang.Exception e) { return null;
		 * }
		 */
		String filename = filenameP.toString();
		String scriptsRoot = LuaEngine.getInstance().GetScriptsRoot();
		if(filename.startsWith("./"))
		{
			scriptsRoot = "";
			filename = filename.substring(2, filename.length());
		}
		int primaryLoad = LuaEngine.getInstance().GetPrimaryLoad();
		switch(primaryLoad)
		{
			case LuaEngine.EXTERNAL_DATA:
			{
				InputStream istr = Defines.GetResourceSdAsset(scriptsRoot + "/", filename);
				if(istr == null)
					return null;
				return new PushbackInputStream(istr);
			}
			case LuaEngine.INTERNAL_DATA:
			{
				InputStream istr = Defines.GetResourceInternalAsset(scriptsRoot + "/", filename);
				if(istr == null)
					return null;
				return new PushbackInputStream(istr);
			}
			case LuaEngine.RESOURCE_DATA:
			{
				InputStream istr = Defines.GetResourceAsset(scriptsRoot + "/", filename);
				if(istr == null)
					return null;
				return new PushbackInputStream(istr);
			}
			default:
			{
				InputStream istr = Defines.GetResourceAsset(scriptsRoot + "/", filename);
				if(istr == null)
					return null;
				return new PushbackInputStream(istr);
			}
		}
	}

	public static Object fopen(CharPtr filename, String modeS) {
		/*
		 * CharPtr mode = new CharPtr(modeS); String str = filename.toString();
		 * FileMode filemode = FileMode.Open; FileAccess fileaccess =
		 * (FileAccess)0; for (int i=0; mode.getItem(i) != '\0'; i++) { switch
		 * (mode.getItem(i)) { case 'r': fileaccess = fileaccess |
		 * FileAccess.Read; if (!(new java.io.File(str)).isFile()) { return
		 * null; } break;
		 * 
		 * case 'w': filemode = FileMode.Create; fileaccess = fileaccess |
		 * FileAccess.Write; break; } } try { return new FileStream(str,
		 * filemode, fileaccess); } catch (java.lang.Exception e) { return null;
		 * }
		 */
		String scriptsRoot = LuaEngine.getInstance().GetScriptsRoot();
		int primaryLoad = LuaEngine.getInstance().GetPrimaryLoad();
		switch(primaryLoad)
		{
			case LuaEngine.EXTERNAL_DATA:
			{
				return new PushbackInputStream(Defines.GetResourceSdAsset(scriptsRoot + "/", filename.toString()));
			}
			case LuaEngine.INTERNAL_DATA:
			{
				return new PushbackInputStream(Defines.GetResourceInternalAsset(scriptsRoot + "/", filename.toString()));
			}
			case LuaEngine.RESOURCE_DATA:
			{
				return new PushbackInputStream(Defines.GetResourceAsset(scriptsRoot + "/", filename.toString()));
			}
			default:
			{
				return new PushbackInputStream(Defines.GetResourceAsset(scriptsRoot + "/", filename.toString()));
			}
		}
	}

	public static InputStream freopen(CharPtr filename, CharPtr mode,
			InputStream stream) {
		/*
		 * try { //stream.Flush(); stream.close(); } catch (java.lang.Exception
		 * e) { }
		 * 
		 * return fopen(filename, mode);
		 */
		try
		{
			stream.close();
		}
		catch(IOException e)
		{
			Tools.LogException("Lua.java", e);
		}
		return (InputStream) fopen(filename, mode);
	}

	public static void fflush(OutputStream stream) 
	{
		try
		{
			stream.flush();
		}
		catch(IOException e)
		{
			Tools.LogException("Lua.java", e);
		}
	}

	public static int ferror(InputStream stream) 
	{
		try
		{
			return ((stream.available() == 0) ? 0 : 1);
		}
		catch(IOException e)
		{
			return 1;
		}
	}

	public static int fclose(InputStream stream) {
		try
		{
			stream.close();
			return -1;
		}
		catch(IOException e)
		{
			return 0;
		}
	}

	// FUCK TODO TASK: There is no preprocessor in Java:
	// #if !XBOX
	public static InputStream tmpfile() {
		// TODO as
		// return new FileStream(Path.GetTempFileName(), FileMode.Create,
		// FileAccess.ReadWrite);
		return null;
	}

	// #endif

	public static int fscanf(InputStream f, CharPtr format, Object... argp) 
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(f));
		String str = "";
		try
		{
			str = in.readLine();
		}
		catch(IOException e)
		{
			Tools.LogException("Lua.java", e);
		}
		return parse_scanf(str, format, argp);
	}

	public static int fscanf(InputStream f, String format, Object... argp) 
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(f));
		String str = "";
		try
		{
			str = in.readLine();
		}
		catch(IOException e)
		{
			Tools.LogException("Lua.java", e);
		}
		return parse_scanf(str, new CharPtr(format), argp);
	}

	public static int fseek(Object f, long offset, int origin) 
	{
		try 
		{ 
			InputStream is = (InputStream) f;
			if(origin == 0) //SEEK_SET
				is.reset();
			is.skip(offset);
			return 0; 
		} 
		catch(java.lang.Exception e)
		{ 
			return 1;
		}		
	}

	public static int ftell(InputStream f) 
	{
		try
		{
			return (int)f.available();
		}
		catch(IOException e)
		{
			return 0;
		}
	}

	public static int clearerr(InputStream f) {
		// Debug.Assert(false, "clearerr not implemented yet - mjf");
		return 0;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static int setvbuf(Stream stream, CharPtr buffer,
	// int mode, uint size)
	public static int setvbuf(InputStream stream, CharPtr buffer, int mode,
			int size) {
		// Debug.Assert(false, "setvbuf not implemented yet - mjf");
		return 0;
	}

	public static <T> void memcpy(T[] dst, T[] src, int length) {
		for (int i = 0; i < length; i++) {
			dst[i] = src[i];
		}
	}

	public static <T> void memcpy(T[] dst, int offset, T[] src, int length) {
		for (int i = 0; i < length; i++) {
			dst[offset + i] = src[i];
		}
	}
	
	public static void cmemcpy(char[] dst, int offset, char[] src, int length) {
		for (int i = 0; i < length; i++) {
			dst[offset + i] = src[i];
		}
	}

	public static <T> void memcpy(T[] dst, T[] src, int srcofs, int length) {
		for (int i = 0; i < length; i++) {
			dst[i] = src[srcofs + i];
		}
	}
	
	public static void cmemcpy(char[] dst, char[] src, int srcofs, int length) {
		for (int i = 0; i < length; i++) {
			if((srcofs + 1) >= src.length)
				break;
			dst[i] = src[srcofs + i];
		}
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static void memcpy(CharPtr ptr1, CharPtr ptr2, uint
	// size)
	/*
	 * public static void memcpy(CharPtr ptr1, CharPtr ptr2, int size) {
	 * memcpy(ptr1, ptr2, (int)size); }
	 */
	public static void memcpy(CharPtr ptr1, CharPtr ptr2, int size) {
		for (int i = 0; i < size; i++) {
			ptr1.setItem(i, ptr2.getItem(i));
		}
	}

	public static void memcpy(CharPtr ptr1, String ptr2S, int size) {
		CharPtr ptr2 = new CharPtr(ptr2S);
		for (int i = 0; i < size; i++) {
			ptr1.setItem(i, ptr2.getItem(i));
		}
	}

	public static Object VOID(Object f) {
		return f;
	}

	public static final double HUGE_VAL = Double.MAX_VALUE;
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public const uint SHRT_MAX = System.UInt16.MaxValue;
	public static final int SHRT_MAX = Short.MAX_VALUE;

	public static final int _IONBF = 0;
	public static final int _IOFBF = 1;
	public static final int _IOLBF = 2;

	public static final int SEEK_SET = 0;
	public static final int SEEK_CUR = 1;
	public static final int SEEK_END = 2;

	// one of the primary objectives of this port is to match the C version of
	// Lua as closely as
	// possible. a key part of this is also matching the behaviour of the
	// garbage collector, as
	// that affects the operation of things such as weak tables. in order for
	// this to occur the
	// size of structures that are allocated must be reported as identical to
	// their C++ equivelents.
	// that this means that variables such as global_State.totalbytes no longer
	// indicate the true
	// amount of memory allocated.
	public static int GetUnmanagedSize(java.lang.Class t) {
		if (t == global_State.class) {
			return 228;
		} else if (t == LG.class) {
			return 376;
		} else if (t == CallInfo.class) {
			return 24;
		} else if (t == lua_TValue.class) {
			return 16;
		} else if (t == Table.class) {
			return 32;
		} else if (t == Node.class) {
			return 32;
		} else if (t == GCObject.class) {
			return 120;
		} else if (t == GCObjectRef.class) {
			return 4;
		} else if (t == ArrayRef.class) {
			return 4;
		} else if (t == Closure.class) {
			return 0; // handle this one manually in the code
		} else if (t == Proto.class) {
			return 76;
		} else if (t == luaL_Reg.class) {
			return 8;
		} else if (t == luaL_Buffer.class) {
			return 524;
		} else if (t == lua_State.class) {
			return 120;
		} else if (t == lua_Debug.class) {
			return 100;
		} else if (t == CallS.class) {
			return 8;
		} else if (t == LoadF.class) {
			return 520;
		} else if (t == LoadS.class) {
			return 8;
		} else if (t == lua_longjmp.class) {
			return 72;
		} else if (t == SParser.class) {
			return 20;
		} else if (t == Token.class) {
			return 16;
		} else if (t == LexState.class) {
			return 52;
		} else if (t == FuncState.class) {
			return 572;
		} else if (t == GCheader.class) {
			return 8;
		} else if (t == lua_TValue.class) {
			return 16;
		} else if (t == TString.class) {
			return 16;
		} else if (t == LocVar.class) {
			return 12;
		} else if (t == UpVal.class) {
			return 32;
		} else if (t == CClosure.class) {
			return 40;
		} else if (t == LClosure.class) {
			return 24;
		} else if (t == TKey.class) {
			return 16;
		} else if (t == ConsControl.class) {
			return 40;
		} else if (t == LHS_assign.class) {
			return 32;
		} else if (t == expdesc.class) {
			return 24;
		} else if (t == upvaldesc.class) {
			return 2;
		} else if (t == BlockCnt.class) {
			return 12;
		} else if (t == Zio.class) {
			return 20;
		} else if (t == Mbuffer.class) {
			return 12;
		} else if (t == LoadState.class) {
			return 16;
		} else if (t == MatchState.class) {
			return 272;
		} else if (t == stringtable.class) {
			return 12;
		} else if (t == FilePtr.class) {
			return 4;
		} else if (t == Udata.class) {
			return 24;
		} else if (t == Character.class) {
			return 1;
		} else if (t == Short.class) {
			return 2;
		} else if (t == Short.class) {
			return 2;
		} else if (t == Integer.class) {
			return 4;
		} else if (t == Integer.class) {
			return 4;
		} else if (t == Float.class) {
			return 4;
		}
		// Debug.Assert(false, "Trying to get unknown sized of unmanaged type "
		// + t.toString());
		return 0;
	}

	public static class LoadState {
		public lua_State L;
		public Zio Z;
		public Mbuffer b;
		public CharPtr name;
	}

	// /#ifdef LUAC_TRUST_BINARIES
	// /#define IF(c,s)
	// /#define error(S,s)
	// /#else
	// /#define IF(c,s) if (c) error(S,s)

	public static void IF(int c, String s) {
	}

	public static void IF(boolean c, String s) {
	}

	public static void error(LoadState S, CharPtr why) {
		luaO_pushfstring(S.L, "%s: %s in precompiled chunk", S.name, why);
		luaD_throw(S.L, LUA_ERRSYNTAX);
	}

	public static void error(LoadState S, String why) {
		luaO_pushfstring(S.L, "%s: %s in precompiled chunk", S.name, why);
		luaD_throw(S.L, LUA_ERRSYNTAX);
	}

	// /#endif

	public static Object LoadMem(LoadState S, java.lang.Class t) {
		Object b = null;
		try {
			b = t.newInstance();
		} catch (IllegalAccessException e) {
			Tools.LogException("Lua.java", e);
		} catch (InstantiationException e) {
			Tools.LogException("Lua.java", e);
		}
		int size = SizeOf.sizeof(b);// int size = Marshal.SizeOf(t);
		CharPtr str = new CharPtr(new char[size]);
		LoadBlock(S, str, size);
		/*
		 * byte[] bytes = new byte[str.chars.length]; for (int i = 0; i <
		 * str.chars.length; i++) { bytes[i] = (byte)str.chars[i]; } GCHandle
		 * pinnedPacket = GCHandle.Alloc(bytes, GCHandleType.Pinned); Object b =
		 * Marshal.PtrToStructure(pinnedPacket.AddrOfPinnedObject(), t);
		 * pinnedPacket.Free();
		 */
		return b;
	}

	public static Object LoadMem(LoadState S, java.lang.Class t, int n) {
		java.util.ArrayList array = new java.util.ArrayList();
		for (int i = 0; i < n; i++) {
			array.add(LoadMem(S, t));
		}
		return array.toArray();
	}

	public static byte LoadByte(LoadState S) {
		return (byte) LoadChar(S);
	}

	public static Object LoadVar(LoadState S, java.lang.Class t) {
		return LoadMem(S, t);
	}

	public static Object LoadVector(LoadState S, java.lang.Class t, int n) {
		return LoadMem(S, t, n);
	}

	public static void LoadBlock(LoadState S, CharPtr b, int size) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint r=luaZ_read(S.Z, b, (uint)size);
		int r = luaZ_read(S.Z, b, (int) size);
		IF(r != 0, "unexpected end");
	}

	public static int LoadChar(LoadState S) {
		return ((Character) LoadVar(S, Character.class)).charValue();
	}

	public static int LoadInt(LoadState S) {
		int x = ((Integer) LoadVar(S, Integer.class)).intValue();
		IF(x < 0, "bad integer");
		return x;
	}

	public static double LoadNumber(LoadState S) {
		return ((Double) LoadVar(S, double.class)).doubleValue();
	}

	public static TString LoadString(LoadState S) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint size = (uint)LoadVar(S, typeof(uint));
		int size = ((Integer) LoadVar(S, Integer.class)).intValue();
		if (size == 0) {
			return null;
		} else {
			CharPtr s = luaZ_openspace(S.L, S.b, size);
			LoadBlock(S, s, (int) size);
			return luaS_newlstr(S.L, s, size - 1); // remove trailing '\0'
		}
	}

	public static void LoadCode(LoadState S, Proto f) {
		int n = LoadInt(S);
		f.code = Lua.<Long> luaM_newvector(S.L, n, long.class);
		f.sizecode = n;
		f.code = (Long[]) LoadVector(S, long.class, n);
	}

	public static void LoadConstants(LoadState S, Proto f) {
		int i, n;
		n = LoadInt(S);
		f.k = Lua.<lua_TValue> luaM_newvector(S.L, n, lua_TValue.class);
		f.sizek = n;
		for (i = 0; i < n; i++) {
			setnilvalue(f.k[i]);
		}
		for (i = 0; i < n; i++) {
			lua_TValue o = f.k[i];
			int t = LoadChar(S);
			switch (t) {
			case LUA_TNIL:
				setnilvalue(o);
				break;
			case LUA_TBOOLEAN:
				setbvalue(o, LoadChar(S));
				break;
			case LUA_TNUMBER:
				setnvalue(o, LoadNumber(S));
				break;
			case LUA_TSTRING:
				setsvalue2n(S.L, o, LoadString(S));
				break;
			default:
				error(S, "bad constant");
				break;
			}
		}
		n = LoadInt(S);
		f.p = Lua.<Proto> luaM_newvector(S.L, n, Proto.class);
		f.sizep = n;
		for (i = 0; i < n; i++) {
			f.p[i] = null;
		}
		for (i = 0; i < n; i++) {
			f.p[i] = LoadFunction(S, f.source);
		}
	}

	public static void LoadDebug(LoadState S, Proto f) {
		int i, n;
		n = LoadInt(S);
		f.lineinfo = Lua.<Integer> luaM_newvector(S.L, n, Integer.class);
		f.sizelineinfo = n;
		f.lineinfo = (Integer[]) LoadVector(S, Integer.class, n);
		n = LoadInt(S);
		f.locvars = Lua.<LocVar> luaM_newvector(S.L, n, LocVar.class);
		f.sizelocvars = n;
		for (i = 0; i < n; i++) {
			f.locvars[i].varname = null;
		}
		for (i = 0; i < n; i++) {
			f.locvars[i].varname = LoadString(S);
			f.locvars[i].startpc = LoadInt(S);
			f.locvars[i].endpc = LoadInt(S);
		}
		n = LoadInt(S);
		f.upvalues = Lua.<TString> luaM_newvector(S.L, n, TString.class);
		f.sizeupvalues = n;
		for (i = 0; i < n; i++) {
			f.upvalues[i] = null;
		}
		for (i = 0; i < n; i++) {
			f.upvalues[i] = LoadString(S);
		}
	}

	public static Proto LoadFunction(LoadState S, TString p) {
		Proto f;
		if (++S.L.nCcalls > LUAI_MAXCCALLS) {
			error(S, "code too deep");
		}
		f = luaF_newproto(S.L);
		setptvalue2s(S.L, S.L.top, f);
		incr_top(S.L);
		f.source = LoadString(S);
		if (f.source == null) {
			f.source = p;
		}
		f.linedefined = LoadInt(S);
		f.lastlinedefined = LoadInt(S);
		f.nups = LoadByte(S);
		f.numparams = LoadByte(S);
		f.is_vararg = LoadByte(S);
		f.maxstacksize = LoadByte(S);
		LoadCode(S, f);
		LoadConstants(S, f);
		LoadDebug(S, f);
		IF(luaG_checkcode(f) == 0 ? 1 : 0, "bad code");
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				S.L.top);
		lua_TValue.dec(tempRef_top);
		S.L.top = tempRef_top.argvalue;
		S.L.nCcalls--;
		return f;
	}

	public static void LoadHeader(LoadState S) {
		CharPtr h = new CharPtr(new char[LUAC_HEADERSIZE]);
		CharPtr s = new CharPtr(new char[LUAC_HEADERSIZE]);
		luaU_header(h);
		LoadBlock(S, s, LUAC_HEADERSIZE);
		IF(memcmp(h, s, LUAC_HEADERSIZE) != 0, "bad header");
	}

	/*
	 * * load precompiled chunk
	 */
	public static Proto luaU_undump(lua_State L, Zio Z, Mbuffer buff,
			CharPtr name) {
		LoadState S = new LoadState();
		if (name.getItem(0) == '@' || name.getItem(0) == '=') {
			S.name = CharPtr.OpAddition(name, 1);// S.name = name+1;
		} else if (name.getItem(0) == LUA_SIGNATURE.charAt(0)) {
			S.name = new CharPtr("binary string");
		} else {
			S.name = name;
		}
		S.L = L;
		S.Z = Z;
		S.b = buff;
		LoadHeader(S);
		return LoadFunction(S, luaS_newliteral(L, "=?"));
	}

	/*
	 * make header
	 */
	public static void luaU_header(CharPtr h) {
		h = new CharPtr(h);
		int x = 1;
		memcpy(h, LUA_SIGNATURE, LUA_SIGNATURE.length());
		h = h.add(LUA_SIGNATURE.length());
		h.setItem(0, (char) LUAC_VERSION);
		h.inc();
		h.setItem(0, (char) LUAC_FORMAT);
		h.inc();
		// *h++=(char)*(char*)&x; /* endianness */
		h.setItem(0, (char) x); // endianness
		h.inc();
		h.setItem(0, (char) SizeOf.sizeof(Integer.class));
		h.inc();
		h.setItem(0, (char) SizeOf.sizeof(Integer.class)); // Unsigned Integer
		h.inc();
		h.setItem(0, (char) SizeOf.sizeof(Integer.class)); // Instruction
		h.inc();
		h.setItem(0, (char) SizeOf.sizeof(Double.class));
		h.inc();

		// (h++)[0] = ((double)0.5 == 0) ? 0 : 1; /* is double integral? */
		h.setItem(0, (char) 0); // always 0 on this build
	}

	public static int tostring(lua_State L, lua_TValue o) {
		return ((ttype(o) == LUA_TSTRING) || (luaV_tostring(L, o) != 0)) ? 1
				: 0;
	}

	public static int tonumber(RefObject<lua_TValue> o, lua_TValue n) {
		return ((ttype(o.argvalue) == LUA_TNUMBER || (((o.argvalue) = luaV_tonumber(
				o.argvalue, n)) != null))) ? 1 : 0;
	}

	public static int equalobj(lua_State L, lua_TValue o1, lua_TValue o2) {
		return ((ttype(o1) == ttype(o2)) && (luaV_equalval(L, o1, o2) != 0)) ? 1
				: 0;
	}

	/* limit for table tag-method chains (to avoid loops) */
	public static final int MAXTAGLOOP = 100;

	public static lua_TValue luaV_tonumber(lua_TValue obj, lua_TValue n) {
		double num = 0;
		if (ttisnumber(obj)) {
			return obj;
		}
		RefObject<Double> tempRef_num = new RefObject<Double>(num);
		boolean tempVar = ttisstring(obj)
				&& (luaO_str2d(svalue(obj), tempRef_num) != 0);
		num = tempRef_num.argvalue;
		if (tempVar) {
			setnvalue(n, num);
			return n;
		} else {
			return null;
		}
	}

	public static int luaV_tostring(lua_State L, lua_TValue obj) {
		if (!ttisnumber(obj)) {
			return 0;
		} else {
			double n = nvalue(obj);
			CharPtr s = lua_number2str(n);
			setsvalue2s(L, obj, luaS_new(L, s));
			return 1;
		}
	}

	public static void traceexec(lua_State L, InstructionPtr pc) {
		short mask = L.hookmask;
		InstructionPtr oldpc = InstructionPtr.Assign(L.savedpc);
		L.savedpc = InstructionPtr.Assign(pc);
		if (((mask & LUA_MASKCOUNT) != 0) && (L.hookcount == 0)) {
			resethookcount(L);
			luaD_callhook(L, LUA_HOOKCOUNT, -1);
		}
		if ((mask & LUA_MASKLINE) != 0) {
			Proto p = ci_func(L.ci).l.p;
			int npc = pcRel(pc, p);
			int newline = getline(p, npc);
			/*
			 * call linehook when enter a new function, when jump back (loop),
			 * or when enter a new line
			 */
			if (npc == 0 || InstructionPtr.OpLessThanOrEqual(pc, oldpc)
					|| newline != getline(p, pcRel(oldpc, p))) {
				luaD_callhook(L, LUA_HOOKLINE, newline);
			}
		}
	}

	public static void callTMres(lua_State L, lua_TValue res, lua_TValue f,
			lua_TValue p1, lua_TValue p2) {
		int result = savestack(L, res);
		setobj2s(L, L.top, f); // push function
		setobj2s(L, Lua.lua_TValue.OpAddition(L.top, 1), p1); // 1st argument
		setobj2s(L, Lua.lua_TValue.OpAddition(L.top, 2), p2); // 2nd argument
		luaD_checkstack(L, 3);
		L.top = lua_TValue.OpAddition(L.top, 3);// L.top += 3;
		luaD_call(L, Lua.lua_TValue.OpSubtraction(L.top, 3), 1);
		res = restorestack(L, result);
		RefObject<Lua.lua_TValue> tempRef_top = new RefObject<Lua.lua_TValue>(
				L.top);
		lua_TValue.dec(tempRef_top);
		L.top = tempRef_top.argvalue;
		setobjs2s(L, res, L.top);
	}

	public static void callTM(lua_State L, lua_TValue f, lua_TValue p1,
			lua_TValue p2, lua_TValue p3) {
		setobj2s(L, L.top, f); // push function
		setobj2s(L, Lua.lua_TValue.OpAddition(L.top, 1), p1); // 1st argument
		setobj2s(L, Lua.lua_TValue.OpAddition(L.top, 2), p2); // 2nd argument
		setobj2s(L, Lua.lua_TValue.OpAddition(L.top, 3), p3); // 3th argument
		luaD_checkstack(L, 4);
		L.top = lua_TValue.OpAddition(L.top, 4);// L.top += 4;
		luaD_call(L, Lua.lua_TValue.OpSubtraction(L.top, 4), 0);
	}

	public static void luaV_gettable(lua_State L, lua_TValue t, lua_TValue key,
			lua_TValue val) {
		int loop;
		for (loop = 0; loop < MAXTAGLOOP; loop++) {
			lua_TValue tm;
			if (ttistable(t)) // `t' is a table?
			{
				Table h = hvalue(t);
				lua_TValue res = luaH_get(h, key); // do a primitive get
				if (!ttisnil(res)
						|| (tm = fasttm(L, h.metatable, TMS.TM_INDEX)) == null) // or
																				// no
																				// TM?
																				// -
																				// result
																				// is
																				// no
																				// nil?
				{
					setobj2s(L, val, res);
					return;
				}
				/* else will try the tag method */
			} else if (ttisnil(tm = luaT_gettmbyobj(L, t, TMS.TM_INDEX))) {
				luaG_typeerror(L, t, "index");
			}
			if (ttisfunction(tm)) {
				callTMres(L, val, tm, t, key);
				return;
			}
			t = tm; // else repeat with `tm'
		}
		luaG_runerror(L, "loop in gettable");
	}

	public static void luaV_settable(lua_State L, lua_TValue t, lua_TValue key,
			lua_TValue val) {
		int loop;

		for (loop = 0; loop < MAXTAGLOOP; loop++) {
			lua_TValue tm;
			if (ttistable(t)) // `t' is a table?
			{
				Table h = hvalue(t);
				lua_TValue oldval = luaH_set(L, h, key); // do a primitive set
				if (!ttisnil(oldval)
						|| (tm = fasttm(L, h.metatable, TMS.TM_NEWINDEX)) == null) // or
																					// no
																					// TM?
																					// -
																					// result
																					// is
																					// no
																					// nil?
				{
					setobj2t(L, oldval, val);
					luaC_barriert(L, h, val);
					return;
				}
				/* else will try the tag method */
			} else if (ttisnil(tm = luaT_gettmbyobj(L, t, TMS.TM_NEWINDEX))) {
				luaG_typeerror(L, t, "index");
			}
			if (ttisfunction(tm)) {
				callTM(L, tm, t, key, val);
				return;
			}
			t = tm; // else repeat with `tm'
		}
		luaG_runerror(L, "loop in settable");
	}

	public static int call_binTM(lua_State L, lua_TValue p1, lua_TValue p2,
			lua_TValue res, TMS event_) {
		lua_TValue tm = luaT_gettmbyobj(L, p1, event_); // try first operand
		if (ttisnil(tm)) {
			tm = luaT_gettmbyobj(L, p2, event_); // try second operand
		}
		if (ttisnil(tm)) {
			return 0;
		}
		callTMres(L, res, tm, p1, p2);
		return 1;
	}

	public static lua_TValue get_compTM(lua_State L, Table mt1, Table mt2,
			TMS event_) {
		lua_TValue tm1 = fasttm(L, mt1, event_);
		lua_TValue tm2;
		if (tm1 == null) // no metamethod
		{
			return null;
		}
		if (mt1 == mt2) // same metatables => same metamethods
		{
			return tm1;
		}
		tm2 = fasttm(L, mt2, event_);
		if (tm2 == null) // no metamethod
		{
			return null;
		}
		if (luaO_rawequalObj(tm1, tm2) != 0) // same metamethods?
		{
			return tm1;
		}
		return null;
	}

	public static int call_orderTM(lua_State L, lua_TValue p1, lua_TValue p2,
			TMS event_) {
		lua_TValue tm1 = luaT_gettmbyobj(L, p1, event_);
		lua_TValue tm2;
		if (ttisnil(tm1)) // no metamethod?
		{
			return -1;
		}
		tm2 = luaT_gettmbyobj(L, p2, event_);
		if (luaO_rawequalObj(tm1, tm2) == 0) // different metamethods?
		{
			return -1;
		}
		callTMres(L, L.top, tm1, p1, p2);
		return l_isfalse(L.top) == 0 ? 1 : 0;
	}

	public static int l_strcmp(TString ls, TString rs) {
		CharPtr l = getstr(ls);
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint ll = ls.tsv.len;
		int ll = ls.gettsv().len;
		CharPtr r = getstr(rs);
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint lr = rs.tsv.len;
		int lr = rs.gettsv().len;
		for (;;) {
			// int temp = strcoll(l, r);
			int temp = l.toString().compareTo(r.toString());
			if (temp != 0) {
				return temp;
			} else // strings are equal up to a `\0'
			{
				// FUCK WARNING: Unsigned integer types have no direct
				// equivalent in Java:
				// ORIGINAL LINE: uint len = (uint)l.ToString().Length;
				int len = (int) l.toString().length(); // index of first `\0' in
														// both strings
				if (len == lr) // r is finished?
				{
					return (len == ll) ? 0 : 1;
				} else if (len == ll) // l is finished?
				{
					return -1; // l is smaller than r (because r is not
								// finished)
				}
				/*
				 * both strings longer than `len'; go on comparing (after the
				 * `\0')
				 */
				len++;
				l = CharPtr.OpAddition(l, len);// l += len;
				ll -= len;
				r = CharPtr.OpAddition(r, len);// r += len;
				lr -= len;
			}
		}
	}

	public static int luaV_lessthan(lua_State L, lua_TValue l, lua_TValue r) {
		int res;
		if (ttype(l) != ttype(r)) {
			return luaG_ordererror(L, l, r);
		} else if (ttisnumber(l)) {
			return luai_numlt(nvalue(l), nvalue(r)) ? 1 : 0;
		} else if (ttisstring(l)) {
			return (l_strcmp(rawtsvalue(l), rawtsvalue(r)) < 0) ? 1 : 0;
		} else if ((res = call_orderTM(L, l, r, TMS.TM_LT)) != -1) {
			return res;
		}
		return luaG_ordererror(L, l, r);
	}

	public static int lessequal(lua_State L, lua_TValue l, lua_TValue r) {
		int res;
		if (ttype(l) != ttype(r)) {
			return luaG_ordererror(L, l, r);
		} else if (ttisnumber(l)) {
			return luai_numle(nvalue(l), nvalue(r)) ? 1 : 0;
		} else if (ttisstring(l)) {
			return (l_strcmp(rawtsvalue(l), rawtsvalue(r)) <= 0) ? 1 : 0;
		} else if ((res = call_orderTM(L, l, r, TMS.TM_LE)) != -1) // first try
																	// `le'
		{
			return res;
		} else if ((res = call_orderTM(L, r, l, TMS.TM_LT)) != -1) // else try
																	// `lt'
		{
			return (res == 0) ? 1 : 0;
		}
		return luaG_ordererror(L, l, r);
	}

	public static CharPtr mybuff = null;

	public static int luaV_equalval(lua_State L, lua_TValue t1, lua_TValue t2) {
		lua_TValue tm = null;
		lua_assert(ttype(t1) == ttype(t2));
		switch (ttype(t1)) {
		case LUA_TNIL:
			return 1;
		case LUA_TNUMBER:
			return luai_numeq(nvalue(t1), nvalue(t2)) ? 1 : 0;
		case LUA_TBOOLEAN: // true must be 1 !!
			return (bvalue(t1) == bvalue(t2)) ? 1 : 0;
		case LUA_TLIGHTUSERDATA:
			return (pvalue(t1) == pvalue(t2)) ? 1 : 0;
		case LUA_TUSERDATA: {
			if (uvalue(t1) == uvalue(t2)) {
				return 1;
			}
			tm = get_compTM(L, uvalue(t1).metatable, uvalue(t2).metatable,
					TMS.TM_EQ);
			break; // will try TM
		}
		case LUA_TTABLE: {
			if (hvalue(t1) == hvalue(t2)) {
				return 1;
			}
			tm = get_compTM(L, hvalue(t1).metatable, hvalue(t2).metatable,
					TMS.TM_EQ);
			break; // will try TM
		}
		default:
			return (gcvalue(t1) == gcvalue(t2)) ? 1 : 0;
		}
		if (tm == null) // no TM?
		{
			return 0;
		}
		callTMres(L, L.top, tm, t1, t2); // call TM
		return l_isfalse(L.top) == 0 ? 1 : 0;
	}

	public static void luaV_concat(lua_State L, int total, int last) {
		do {
			lua_TValue top = lua_TValue.OpAddition(
					lua_TValue.OpAddition(L.base_, last), 1);
			int n = 2; // number of elements handled in this pass (at least 2)
			lua_TValue topmin2 = lua_TValue.OpSubtraction(top, 2);
			lua_TValue topmin1 = lua_TValue.OpSubtraction(top, 1);
			if (!(ttisstring(topmin2) || ttisnumber(topmin2))
					|| (tostring(L, topmin1) == 0))// if (!(ttisstring(top-2) ||
													// ttisnumber(top-2)) ||
													// (tostring(L, top-1)==0))
			{
				if (call_binTM(L, topmin2, topmin1, topmin2, TMS.TM_CONCAT) == 0)// if
																					// (call_binTM(L,
																					// top-2,
																					// top-1,
																					// top-2,
																					// TMS.TM_CONCAT)==0)
				{
					luaG_concaterror(L, topmin2, topmin1);// luaG_concaterror(L,
															// top-2, top-1);
				}
			} // second op is empty?
			else if (tsvalue(topmin1).len == 0)// else if (tsvalue(top-1).len ==
												// 0)
			{
				tostring(L, topmin2);// tostring(L, top - 2); // result is first
										// op (as string)
			} else {
				/* at least two string values; get as many as possible */
				// FUCK WARNING: Unsigned integer types have no direct
				// equivalent in Java:
				// ORIGINAL LINE: uint tl = tsvalue(top-1).len;
				int tl = tsvalue(topmin1).len;// int tl = tsvalue(top-1).len;
				CharPtr buffer;
				int i;
				/* collect total length */
				for (n = 1; n < total
						&& (tostring(
								L,
								lua_TValue.OpSubtraction(
										lua_TValue.OpSubtraction(top, n), 1)) != 0); n++)// for
																							// (n
																							// =
																							// 1;
																							// n
																							// <
																							// total
																							// &&
																							// (tostring(L,
																							// top-n-1)!=0);
																							// n++)
				{
					// FUCK WARNING: Unsigned integer types have no direct
					// equivalent in Java:
					// ORIGINAL LINE: uint l = tsvalue(top-n-1).len;
					int l = tsvalue(lua_TValue.OpSubtraction(
							lua_TValue.OpSubtraction(top, n), 1)).len;// int l =
																		// tsvalue(top-n-1).len;
					if (l >= MAX_SIZET - tl) {
						luaG_runerror(L, "string length overflow");
					}
					tl += l;
				}
				buffer = luaZ_openspace(L, G(L).buff, tl);
				if (mybuff == null) {
					mybuff = buffer;
				}
				tl = 0;
				for (i = n; i > 0; i--) // concat all strings
				{
					// FUCK WARNING: Unsigned integer types have no direct
					// equivalent in Java:
					// ORIGINAL LINE: uint l = tsvalue(top-i).len;
					int l = tsvalue(lua_TValue.OpSubtraction(top, i)).len;// int
																			// l
																			// =
																			// tsvalue(top-i).len;
					cmemcpy(buffer.chars, (int) tl,
							svalue(lua_TValue.OpSubtraction(top, i)).chars,
							(int) l);// memcpy(buffer.chars, (int)tl,
										// svalue(top-i).chars, (int)l);
					tl += l;
				}
				setsvalue2s(L, lua_TValue.OpSubtraction(top, n),
						luaS_newlstr(L, buffer, tl));// setsvalue2s(L, top-n,
														// luaS_newlstr(L,
														// buffer, tl));
			}
			total -= n - 1; // got `n' strings to create 1 new
			last -= n - 1;
		} while (total > 1); // repeat until only 1 result left
	}

	public static void Arith(lua_State L, lua_TValue ra, lua_TValue rb,
			lua_TValue rc, TMS op) {
		lua_TValue tempb = new lua_TValue(), tempc = new lua_TValue();
		lua_TValue b, c;
		if ((b = luaV_tonumber(rb, tempb)) != null
				&& (c = luaV_tonumber(rc, tempc)) != null) {
			double nb = nvalue(b), nc = nvalue(c);
			switch (op) {
			case TM_ADD:
				setnvalue(ra, luai_numadd(nb, nc));
				break;
			case TM_SUB:
				setnvalue(ra, luai_numsub(nb, nc));
				break;
			case TM_MUL:
				setnvalue(ra, luai_nummul(nb, nc));
				break;
			case TM_DIV:
				setnvalue(ra, luai_numdiv(nb, nc));
				break;
			case TM_MOD:
				setnvalue(ra, luai_nummod(nb, nc));
				break;
			case TM_POW:
				setnvalue(ra, luai_numpow(nb, nc));
				break;
			case TM_UNM:
				setnvalue(ra, luai_numunm(nb));
				break;
			default:
				lua_assert(false);
				break;
			}
		} else if (call_binTM(L, rb, rc, ra, op) == 0) {
			luaG_aritherror(L, rb, rc);
		}
	}

	/*
	 * * some macros for common tasks in `luaV_execute'
	 */

	public static void runtime_check(lua_State L, boolean c) {
		assert c;
	}

	// /#define RA(i) (base+GETARG_A(i))
	/* to be used after possible stack reallocation */
	// /#define RB(i) check_exp(getBMode(GET_OPCODE(i)) == OpArgMask.OpArgR,
	// base+GETARG_B(i))
	// /#define RC(i) check_exp(getCMode(GET_OPCODE(i)) == OpArgMask.OpArgR,
	// base+GETARG_C(i))
	// /#define RKB(i) check_exp(getBMode(GET_OPCODE(i)) == OpArgMask.OpArgK, \
	// ISK(GETARG_B(i)) ? k+INDEXK(GETARG_B(i)) : base+GETARG_B(i))
	// /#define RKC(i) check_exp(getCMode(GET_OPCODE(i)) == OpArgMask.OpArgK, \
	// ISK(GETARG_C(i)) ? k+INDEXK(GETARG_C(i)) : base+GETARG_C(i))
	// /#define KBx(i) check_exp(getBMode(GET_OPCODE(i)) == OpArgMask.OpArgK,
	// k+GETARG_Bx(i))

	// todo: implement proper checks, as above
	public static lua_TValue RA(lua_State L, lua_TValue base_, long i) {
		return lua_TValue.OpAddition(base_, GETARG_A(i));// return base_ +
															// GETARG_A(i);
	}

	public static lua_TValue RB(lua_State L, lua_TValue base_, long i) {
		return lua_TValue.OpAddition(base_, GETARG_B(i));// return base_ +
															// GETARG_B(i);
	}

	public static lua_TValue RC(lua_State L, lua_TValue base_, int i) {
		return lua_TValue.OpAddition(base_, GETARG_C(i));// return base_ +
															// GETARG_C(i);
	}

	public static lua_TValue RKB(lua_State L, lua_TValue base_, long i,
			lua_TValue[] k) {
		return ISK(GETARG_B(i)) != 0 ? k[INDEXK(GETARG_B(i))] : lua_TValue
				.OpAddition(base_, GETARG_B(i));// return ISK(GETARG_B(i)) != 0
												// ? k[INDEXK(GETARG_B(i))] :
												// base_ + GETARG_B(i);
	}

	public static lua_TValue RKC(lua_State L, lua_TValue base_, long i,
			lua_TValue[] k) {
		return ISK(GETARG_C(i)) != 0 ? k[INDEXK(GETARG_C(i))] : lua_TValue
				.OpAddition(base_, GETARG_C(i));// return ISK(GETARG_C(i)) != 0
												// ? k[INDEXK(GETARG_C(i))] :
												// base_ + GETARG_C(i);
	}

	public static lua_TValue KBx(lua_State L, long i, lua_TValue[] k) {
		return k[GETARG_Bx(i)];
	}

	public static void dojump(lua_State L, InstructionPtr pc, int i) {
		pc.pc += i;
		luai_threadyield(L);
	}

	// /#define Protect(x) { L.savedpc = pc; {x;}; base = L.base_; }

	// public static void arith_op(lua_State L, op_delegate op, TMS tm,
	// lua_TValue base_, int i, lua_TValue[] k, lua_TValue ra, InstructionPtr
	// pc)
	public static void arith_op(lua_State L, IDelegate op, TMS tm,
			lua_TValue base_, long i, lua_TValue[] k, lua_TValue ra,
			InstructionPtr pc) {
		lua_TValue rb = RKB(L, base_, i, k);
		lua_TValue rc = RKC(L, base_, i, k);
		if (ttisnumber(rb) && ttisnumber(rc)) {
			double nb = nvalue(rb), nc = nvalue(rc);
			setnvalue(ra, (Double) op.invoke(nb, nc));
		} else {
			// Protect(
			L.savedpc = InstructionPtr.Assign(pc);
			Arith(L, ra, rb, rc, tm);
			base_ = L.base_;
			// );
		}
	}

	public static void Dump(int pc, int i) {
		int A = GETARG_A(i);
		int B = GETARG_B(i);
		int C = GETARG_C(i);
		int Bx = GETARG_Bx(i);
		int sBx = GETARG_sBx(i);
		if ((sBx & 0x100) != 0) {
			sBx = -(sBx & 0xff);
		}

		System.out.printf("%1$5s (%2$10s): ", pc, i);
		System.out.printf("%1$-10s\t", luaP_opnames[GET_OPCODE(i).getValue()]);
		switch (GET_OPCODE(i)) {
		case OP_CLOSE:
			System.out.printf("%1$s", A);
			break;

		case OP_MOVE:
		case OP_LOADNIL:
		case OP_GETUPVAL:
		case OP_SETUPVAL:
		case OP_UNM:
		case OP_NOT:
		case OP_RETURN:
			System.out.printf("%1$s, %2$s", A, B);
			break;

		case OP_LOADBOOL:
		case OP_GETTABLE:
		case OP_SETTABLE:
		case OP_NEWTABLE:
		case OP_SELF:
		case OP_ADD:
		case OP_SUB:
		case OP_MUL:
		case OP_DIV:
		case OP_POW:
		case OP_CONCAT:
		case OP_EQ:
		case OP_LT:
		case OP_LE:
		case OP_TEST:
		case OP_CALL:
		case OP_TAILCALL:
			System.out.printf("%1$s, %2$s, %3$s", A, B, C);
			break;

		case OP_LOADK:
			System.out.printf("%1$s, %2$s", A, Bx);
			break;

		case OP_GETGLOBAL:
		case OP_SETGLOBAL:
		case OP_SETLIST:
		case OP_CLOSURE:
			System.out.printf("%1$s, %2$s", A, Bx);
			break;

		case OP_TFORLOOP:
			System.out.printf("%1$s, %2$s", A, C);
			break;

		case OP_JMP:
		case OP_FORLOOP:
		case OP_FORPREP:
			System.out.printf("%1$s, %2$s", A, sBx);
			break;
		}
		System.out.println();

	}

	/*
	 * static IDelegate luai_numadd = op_delegate.build(Lua.class,
	 * "luai_numadd"); static IDelegate luai_numsub =
	 * op_delegate.build(Lua.class, "luai_numsub"); static IDelegate luai_nummul
	 * = op_delegate.build(Lua.class, "luai_nummul"); static IDelegate
	 * luai_numdiv = op_delegate.build(Lua.class, "luai_numdiv"); static
	 * IDelegate luai_nummod = op_delegate.build(Lua.class, "luai_nummod");
	 * static IDelegate luai_numpow = op_delegate.build(Lua.class,
	 * "luai_numpow");
	 */
	static IDelegate luai_numadd = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luai_numadd((Double) arg1, (Double) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	static IDelegate luai_numsub = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luai_numsub((Double) arg1, (Double) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	static IDelegate luai_nummul = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luai_nummul((Double) arg1, (Double) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	static IDelegate luai_numdiv = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luai_numdiv((Double) arg1, (Double) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	static IDelegate luai_nummod = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luai_nummod((Double) arg1, (Double) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	static IDelegate luai_numpow = new IDelegate() {

		@Override
		public Object invoke() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object arg1, Object arg2) {
			return luai_numpow((Double) arg1, (Double) arg2);
		}

		@Override
		public Object invoke(Object arg) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object invoke(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public static void luaV_execute(lua_State L, int nexeccalls) {
		LClosure cl;
		lua_TValue base_;
		lua_TValue[] k;
		/* const */
		InstructionPtr pc;
		boolean isReEntry = true;
		while (isReEntry)// reentry: // entry point
		{
			isReEntry = false;
			lua_assert(isLua(L.ci));
			pc = InstructionPtr.Assign(L.savedpc);
			cl = clvalue(L.ci.func).l;
			base_ = L.base_;
			k = cl.p.k;
			/* main loop of interpreter */
			for (;;) {
				if(isReEntry == true)
					break;
				/* const */
				RefObject<InstructionPtr> tempRef_pc = new RefObject<InstructionPtr>(
						pc);
				long i = InstructionPtr.inc(tempRef_pc).getItem(0);
				pc = tempRef_pc.argvalue;
				lua_TValue ra;
				if (((L.hookmask & (LUA_MASKLINE | LUA_MASKCOUNT)) != 0)
						&& (((--L.hookcount) == 0) || ((L.hookmask & LUA_MASKLINE) != 0))) {
					traceexec(L, pc);
					if (L.status == LUA_YIELD) // did hook yield?
					{
						L.savedpc = new InstructionPtr(pc.codes, pc.pc - 1);
						return;
					}
					base_ = L.base_;
				}
				/*
				 * warning!! several calls may realloc the stack and invalidate
				 * `ra'
				 */
				ra = RA(L, base_, i);
				lua_assert(base_ == L.base_ && L.base_ == L.ci.base_);
				lua_assert(lua_TValue.OpLessThanOrEqual(base_, L.top)
						&& ((Lua.lua_TValue.OpSubtraction(L.top, L.stack)) <= L.stacksize));// lua_assert(base_
																							// <=
																							// L.top
																							// &&
																							// ((Lua.lua_TValue.OpSubtraction(L.top,
																							// L.stack))
																							// <=
																							// L.stacksize));
				lua_assert(L.top == L.ci.top || (luaG_checkopenop(i) != 0));
				// Dump(pc.pc, i);
				switch (GET_OPCODE(i)) {
				case OP_MOVE: {
					setobjs2s(L, ra, RB(L, base_, i));
					continue;
				}
				case OP_LOADK: {
					setobj2s(L, ra, KBx(L, i, k));
					continue;
				}
				case OP_LOADBOOL: {
					setbvalue(ra, GETARG_B(i));
					if (GETARG_C(i) != 0) // skip next int (if C)
					{
						RefObject<InstructionPtr> tempRef_pc2 = new RefObject<InstructionPtr>(
								pc);
						InstructionPtr.inc(tempRef_pc2);
						pc = tempRef_pc2.argvalue;
					}
					continue;
				}
				case OP_LOADNIL: {
					lua_TValue rb = RB(L, base_, i);
					do {
						RefObject<lua_TValue> tempRef_rb = new RefObject<lua_TValue>(
								rb);
						setnilvalue(lua_TValue.dec(tempRef_rb));
						rb = tempRef_rb.argvalue;
					} while (lua_TValue.OpGreaterThanOrEqual(rb, ra));// } while
																		// (rb
																		// >=
																		// ra);
					continue;
				}
				case OP_GETUPVAL: {
					int b = GETARG_B(i);
					setobj2s(L, ra, cl.upvals[b].v);
					continue;
				}
				case OP_GETGLOBAL: {
					lua_TValue g = new lua_TValue();
					lua_TValue rb = KBx(L, i, k);
					sethvalue(L, g, cl.getenv());
					lua_assert(ttisstring(rb));
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaV_gettable(L, g, rb, ra);
					base_ = L.base_;
					// );
					L.savedpc = InstructionPtr.Assign(pc);
					continue;
				}
				case OP_GETTABLE: {
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaV_gettable(L, RB(L, base_, i), RKC(L, base_, i, k), ra);
					base_ = L.base_;
					// );
					L.savedpc = InstructionPtr.Assign(pc);
					continue;
				}
				case OP_SETGLOBAL: {
					lua_TValue g = new lua_TValue();
					sethvalue(L, g, cl.getenv());
					lua_assert(ttisstring(KBx(L, i, k)));
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaV_settable(L, g, KBx(L, i, k), ra);
					base_ = L.base_;
					// );
					L.savedpc = InstructionPtr.Assign(pc);
					continue;
				}
				case OP_SETUPVAL: {
					UpVal uv = cl.upvals[GETARG_B(i)];
					setobj(L, uv.v, ra);
					luaC_barrier(L, uv, ra);
					continue;
				}
				case OP_SETTABLE: {
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaV_settable(L, ra, RKB(L, base_, i, k),
							RKC(L, base_, i, k));
					base_ = L.base_;
					// );
					L.savedpc = InstructionPtr.Assign(pc);
					continue;
				}
				case OP_NEWTABLE: {
					int b = GETARG_B(i);
					int c = GETARG_C(i);
					sethvalue(L, ra,
							luaH_new(L, luaO_fb2int(b), luaO_fb2int(c)));
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaC_checkGC(L);
					base_ = L.base_;
					// );
					L.savedpc = InstructionPtr.Assign(pc);
					continue;
				}
				case OP_SELF: {
					lua_TValue rb = RB(L, base_, i);
					setobjs2s(L, lua_TValue.OpAddition(ra, 1), rb);// setobjs2s(L,
																	// ra + 1,
																	// rb);
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaV_gettable(L, rb, RKC(L, base_, i, k), ra);
					base_ = L.base_;
					// );
					L.savedpc = InstructionPtr.Assign(pc);
					continue;
				}
				case OP_ADD: {
					arith_op(L, luai_numadd, TMS.TM_ADD, base_, i, k, ra, pc);
					continue;
				}
				case OP_SUB: {
					arith_op(L, luai_numsub, TMS.TM_SUB, base_, i, k, ra, pc);
					continue;
				}
				case OP_MUL: {
					arith_op(L, luai_nummul, TMS.TM_MUL, base_, i, k, ra, pc);
					continue;
				}
				case OP_DIV: {
					arith_op(L, luai_numdiv, TMS.TM_DIV, base_, i, k, ra, pc);
					continue;
				}
				case OP_MOD: {
					arith_op(L, luai_nummod, TMS.TM_MOD, base_, i, k, ra, pc);
					continue;
				}
				case OP_POW: {
					arith_op(L, luai_numpow, TMS.TM_POW, base_, i, k, ra, pc);
					continue;
				}
				case OP_UNM: {
					lua_TValue rb = RB(L, base_, i);
					if (ttisnumber(rb)) {
						double nb = nvalue(rb);
						setnvalue(ra, luai_numunm(nb));
					} else {
						// Protect(
						L.savedpc = InstructionPtr.Assign(pc);
						Arith(L, ra, rb, rb, TMS.TM_UNM);
						base_ = L.base_;
						// );
						L.savedpc = InstructionPtr.Assign(pc);
					}
					continue;
				}
				case OP_NOT: {
					int res = l_isfalse(RB(L, base_, i)) == 0 ? 0 : 1; // next
																		// assignment
																		// may
																		// change
																		// this
																		// value
					setbvalue(ra, res);
					continue;
				}
				case OP_LEN: {
					lua_TValue rb = RB(L, base_, i);
					switch (ttype(rb)) {
					case LUA_TTABLE: {
						setnvalue(ra, cast_num(luaH_getn(hvalue(rb))));
						break;
					}
					case LUA_TSTRING: {
						setnvalue(ra, cast_num(tsvalue(rb).len));
						break;
					}
					default: // try metamethod
					{
						// Protect(
						L.savedpc = InstructionPtr.Assign(pc);
						if (call_binTM(L, rb, luaO_nilobject, ra, TMS.TM_LEN) == 0) {
							luaG_typeerror(L, rb, "get length of");
						}
						base_ = L.base_;
						// )
						break;
					}
					}
					continue;
				}
				case OP_CONCAT: {
					int b = GETARG_B(i);
					int c = GETARG_C(i);
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaV_concat(L, c - b + 1, c);
					luaC_checkGC(L);
					base_ = L.base_;
					// );
					setobjs2s(L, RA(L, base_, i),
							lua_TValue.OpAddition(base_, b));// setobjs2s(L,
																// RA(L, base_,
																// i), base_ +
																// b);
					continue;
				}
				case OP_JMP: {
					dojump(L, pc, GETARG_sBx(i));
					continue;
				}
				case OP_EQ: {
					lua_TValue rb = RKB(L, base_, i, k);
					lua_TValue rc = RKC(L, base_, i, k);
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					if (equalobj(L, rb, rc) == GETARG_A(i)) {
						dojump(L, pc, GETARG_sBx(pc.getItem(0)));
					}
					base_ = L.base_;
					// );
					RefObject<InstructionPtr> tempRef_pc3 = new RefObject<InstructionPtr>(
							pc);
					InstructionPtr.inc(tempRef_pc3);
					pc = tempRef_pc3.argvalue;
					continue;
				}
				case OP_LT: {
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					if (luaV_lessthan(L, RKB(L, base_, i, k),
							RKC(L, base_, i, k)) == GETARG_A(i)) {
						dojump(L, pc, GETARG_sBx(pc.getItem(0)));
					}
					base_ = L.base_;
					// );
					RefObject<InstructionPtr> tempRef_pc4 = new RefObject<InstructionPtr>(
							pc);
					InstructionPtr.inc(tempRef_pc4);
					pc = tempRef_pc4.argvalue;
					continue;
				}
				case OP_LE: {
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					if (lessequal(L, RKB(L, base_, i, k), RKC(L, base_, i, k)) == GETARG_A(i)) {
						dojump(L, pc, GETARG_sBx(pc.getItem(0)));
					}
					base_ = L.base_;
					// );
					RefObject<InstructionPtr> tempRef_pc5 = new RefObject<InstructionPtr>(
							pc);
					InstructionPtr.inc(tempRef_pc5);
					pc = tempRef_pc5.argvalue;
					continue;
				}
				case OP_TEST: {
					if (l_isfalse(ra) != GETARG_C(i)) {
						dojump(L, pc, GETARG_sBx(pc.getItem(0)));
					}
					RefObject<InstructionPtr> tempRef_pc6 = new RefObject<InstructionPtr>(
							pc);
					InstructionPtr.inc(tempRef_pc6);
					pc = tempRef_pc6.argvalue;
					continue;
				}
				case OP_TESTSET: {
					lua_TValue rb = RB(L, base_, i);
					if (l_isfalse(rb) != GETARG_C(i)) {
						setobjs2s(L, ra, rb);
						dojump(L, pc, GETARG_sBx(pc.getItem(0)));
					}
					RefObject<InstructionPtr> tempRef_pc7 = new RefObject<InstructionPtr>(
							pc);
					InstructionPtr.inc(tempRef_pc7);
					pc = tempRef_pc7.argvalue;
					continue;
				}
				case OP_CALL: {
					int b = GETARG_B(i);
					int nresults = GETARG_C(i) - 1;
					if (b != 0) // else previous int set top
					{
						L.top = lua_TValue.OpAddition(ra, b);// L.top = ra + b;
					}
					L.savedpc = InstructionPtr.Assign(pc);
					switch (luaD_precall(L, ra, nresults)) {
					case PCRLUA: {
						nexeccalls++;
						// FUCK TODO TASK: There is no 'goto' in Java:
						// goto reentry; // restart luaV_execute over new Lua
						// function
						isReEntry = true;
						continue;
					}
					case PCRC: {
						/*
						 * it was a C function (`precall' called it); adjust
						 * results
						 */
						if (nresults >= 0) {
							L.top = L.ci.top;
						}
						base_ = L.base_;
						continue;
					}
					default: {
						return; // yield
					}
					}
				}
				case OP_TAILCALL: {
					int b = GETARG_B(i);
					if (b != 0) // else previous int set top
					{
						L.top = lua_TValue.OpAddition(ra, b);// L.top = ra + b;
					}
					L.savedpc = InstructionPtr.Assign(pc);
					lua_assert(GETARG_C(i) - 1 == LUA_MULTRET);
					switch (luaD_precall(L, ra, LUA_MULTRET)) {
					case PCRLUA: {
						/* tail call: put new frame in place of previous one */
						CallInfo ci = CallInfo.OpSubtraction(L.ci, 1);// CallInfo
																		// ci =
																		// L.ci
																		// - 1;
																		// //
																		// previous
																		// frame
						int aux;
						lua_TValue func = ci.func;
						lua_TValue pfunc = CallInfo.OpAddition(ci, 1).func;// lua_TValue
																			// pfunc
																			// =
																			// (ci+1).func;
																			// //
																			// previous
																			// function
																			// index
						if (L.openupval != null) {
							luaF_close(L, ci.base_);
						}
						L.base_ = ci.base_ = Lua.lua_TValue.OpAddition(ci.func,
								(Lua.lua_TValue.OpSubtraction(
										ci.getItem(1).base_, pfunc)));
						for (aux = 0; lua_TValue.OpLessThan(
								lua_TValue.OpAddition(pfunc, aux), L.top); aux++)// for
																					// (aux
																					// =
																					// 0;
																					// pfunc+aux
																					// <
																					// L.top;
																					// aux++)
																					// //
																					// move
																					// frame
																					// down
						{
							setobjs2s(L, lua_TValue.OpAddition(func, aux),
									lua_TValue.OpAddition(pfunc, aux));// setobjs2s(L,
																		// func+aux,
																		// pfunc+aux);
						}
						ci.top = L.top = lua_TValue.OpAddition(func, aux);// ci.top
																			// =
																			// L.top
																			// =
																			// func+aux;
																			// //
																			// correct
																			// top
						lua_assert(L.top == Lua.lua_TValue.OpAddition(L.base_,
								clvalue(func).l.p.maxstacksize));
						ci.savedpc = InstructionPtr.Assign(L.savedpc);
						ci.tailcalls++; // one more call lost
						RefObject<CallInfo> tempRef_ci = new RefObject<CallInfo>(
								L.ci);
						CallInfo.dec(tempRef_ci); // remove new frame
						L.ci = tempRef_ci.argvalue;
						// FUCK TODO TASK: There is no 'goto' in Java:
						// goto reentry;
						isReEntry = true;
						continue;
					}
					case PCRC: // it was a C function (`precall' called it)
					{
						base_ = L.base_;
						continue;
					}
					default: {
						return; // yield
					}
					}
				}
				case OP_RETURN: {
					int b = GETARG_B(i);
					if (b != 0) {
						L.top = lua_TValue.OpSubtraction(
								lua_TValue.OpAddition(ra, b), 1);// L.top =
																	// ra+b-1;
					}
					if (L.openupval != null) {
						luaF_close(L, base_);
					}
					L.savedpc = InstructionPtr.Assign(pc);
					b = luaD_poscall(L, ra);
					if (--nexeccalls == 0) // was previous function running
											// `here'?
					{
						return; // no: return
					} else // yes: continue its execution
					{
						if (b != 0) {
							L.top = L.ci.top;
						}
						lua_assert(isLua(L.ci));
						lua_assert(GET_OPCODE(L.ci.savedpc.getItem(-1)) == OpCode.OP_CALL);
						// FUCK TODO TASK: There is no 'goto' in Java:
						// goto reentry;
						isReEntry = true;
						continue;
					}
				}
				case OP_FORLOOP: {
					double step = nvalue(lua_TValue.OpAddition(ra, 2));// double
																		// step
																		// =
																		// nvalue(ra+2);
					double idx = luai_numadd(nvalue(ra), step); // increment
																// index
					double limit = nvalue(lua_TValue.OpAddition(ra, 1));// double
																		// limit
																		// =
																		// nvalue(ra+1);
					if (luai_numlt(0, step) ? luai_numle(idx, limit)
							: luai_numle(limit, idx)) {
						dojump(L, pc, GETARG_sBx(i)); // jump back
						setnvalue(ra, idx); // update internal index...
						setnvalue(lua_TValue.OpAddition(ra, 3), idx);// setnvalue(ra+3,
																		// idx);
																		// //...and
																		// external
																		// index
					}
					continue;
				}
				case OP_FORPREP: {
					lua_TValue init = ra;
					lua_TValue plimit = lua_TValue.OpAddition(ra, 1);// lua_TValue
																		// plimit
																		// =
																		// ra+1;
					lua_TValue pstep = lua_TValue.OpAddition(ra, 2);// lua_TValue
																	// pstep =
																	// ra+2;
					L.savedpc = InstructionPtr.Assign(pc); // next steps may
															// throw errors
					RefObject<Lua.lua_TValue> tempRef_init = new RefObject<Lua.lua_TValue>(
							init);
					boolean tempVar = tonumber(tempRef_init, ra) == 0;
					init = tempRef_init.argvalue;
					if (tempVar) {
						luaG_runerror(L, LUA_QL("for")
								+ " initial value must be a number");
					} else {
						RefObject<Lua.lua_TValue> tempRef_plimit = new RefObject<Lua.lua_TValue>(
								plimit);
						boolean tempVar2 = tonumber(tempRef_plimit,
								lua_TValue.OpAddition(ra, 1)) == 0;// boolean
																	// tempVar2
																	// =
																	// tonumber(tempRef_plimit,
																	// ra+1) ==
																	// 0;
						plimit = tempRef_plimit.argvalue;
						if (tempVar2) {
							luaG_runerror(L, LUA_QL("for")
									+ " limit must be a number");
						} else {
							RefObject<Lua.lua_TValue> tempRef_pstep = new RefObject<Lua.lua_TValue>(
									pstep);
							boolean tempVar3 = tonumber(tempRef_pstep,
									lua_TValue.OpAddition(ra, 2)) == 0;// boolean
																		// tempVar3
																		// =
																		// tonumber(tempRef_pstep,
																		// ra+2)
																		// == 0;
							pstep = tempRef_pstep.argvalue;
							if (tempVar3) {
								luaG_runerror(L, LUA_QL("for")
										+ " step must be a number");
							}
						}
					}
					setnvalue(ra, luai_numsub(nvalue(ra), nvalue(pstep)));
					dojump(L, pc, GETARG_sBx(i));
					continue;
				}
				case OP_TFORLOOP: {
					lua_TValue cb = lua_TValue.OpAddition(ra, 3);// lua_TValue
																	// cb = ra +
																	// 3; //
																	// call base
					setobjs2s(L, lua_TValue.OpAddition(cb, 2),
							lua_TValue.OpAddition(ra, 2));// setobjs2s(L, cb+2,
															// ra+2);
					setobjs2s(L, lua_TValue.OpAddition(cb, 1),
							lua_TValue.OpAddition(ra, 1));// setobjs2s(L, cb+1,
															// ra+1);
					setobjs2s(L, cb, ra);// setobjs2s(L, cb, ra);
					L.top = lua_TValue.OpAddition(cb, 3);// L.top = cb+3; //
															// func. + 2 args
															// (state and index)
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaD_call(L, cb, GETARG_C(i));
					base_ = L.base_;
					// );
					L.top = L.ci.top;
					cb = Lua.lua_TValue.OpAddition(RA(L, base_, i), 3); // previous
																		// call
																		// may
																		// change
																		// the
																		// stack
					if (!ttisnil(cb)) // continue loop?
					{
						setobjs2s(L, lua_TValue.OpSubtraction(cb, 1), cb);// setobjs2s(L,
																			// cb-1,
																			// cb);
																			// //
																			// save
																			// control
																			// variable
						dojump(L, pc, GETARG_sBx(pc.getItem(0))); // jump back
					}
					RefObject<InstructionPtr> tempRef_pc8 = new RefObject<InstructionPtr>(
							pc);
					InstructionPtr.inc(tempRef_pc8);
					pc = tempRef_pc8.argvalue;
					continue;
				}
				case OP_SETLIST: {
					int n = GETARG_B(i);
					int c = GETARG_C(i);
					int last;
					Table h;
					if (n == 0) {
						n = cast_int(Lua.lua_TValue.OpSubtraction(L.top, ra)) - 1;
						L.top = L.ci.top;
					}
					if (c == 0) {
						c = cast_int(pc.getItem(0));
						RefObject<InstructionPtr> tempRef_pc9 = new RefObject<InstructionPtr>(
								pc);
						InstructionPtr.inc(tempRef_pc9);
						pc = tempRef_pc9.argvalue;
					}
					runtime_check(L, ttistable(ra));
					h = hvalue(ra);
					last = ((c - 1) * LFIELDS_PER_FLUSH) + n;
					if (last > h.sizearray) // needs more space?
					{
						luaH_resizearray(L, h, last); // pre-alloc it at once
					}
					for (; n > 0; n--) {
						lua_TValue val = lua_TValue.OpAddition(ra, n);// lua_TValue
																		// val =
																		// ra+n;
						setobj2t(L, luaH_setnum(L, h, last--), val);
						luaC_barriert(L, h, val);
					}
					continue;
				}
				case OP_CLOSE: {
					luaF_close(L, ra);
					continue;
				}
				case OP_CLOSURE: {
					Proto p;
					Closure ncl;
					int nup, j;
					p = cl.p.p[GETARG_Bx(i)];
					nup = p.nups;
					ncl = luaF_newLclosure(L, nup, cl.getenv());
					ncl.l.p = p;
					RefObject<InstructionPtr> refpc = new RefObject<InstructionPtr>(
							pc);
					for (j = 0; j < nup; j++, InstructionPtr.inc(refpc)) {
						if (GET_OPCODE(refpc.argvalue.getItem(0)) == OpCode.OP_GETUPVAL) {
							ncl.l.upvals[j] = cl.upvals[GETARG_B(refpc.argvalue
									.getItem(0))];
						} else {
							lua_assert(GET_OPCODE(refpc.argvalue.getItem(0)) == OpCode.OP_MOVE);
							ncl.l.upvals[j] = luaF_findupval(L,
									lua_TValue
											.OpAddition(base_,
													GETARG_B(refpc.argvalue
															.getItem(0))));// ncl.l.upvals[j]
																			// =
																			// luaF_findupval(L,
																			// base_
																			// +
																			// GETARG_B(pc.getItem(0)));
						}
					}
					pc = refpc.argvalue;
					setclvalue(L, ra, ncl);
					// Protect(
					L.savedpc = InstructionPtr.Assign(pc);
					luaC_checkGC(L);
					base_ = L.base_;
					// );
					continue;
				}
				case OP_VARARG: {
					int b = GETARG_B(i) - 1;
					int j;
					CallInfo ci = L.ci;
					int n = cast_int(Lua.lua_TValue.OpSubtraction(ci.base_,
							ci.func)) - cl.p.numparams - 1;
					if (b == LUA_MULTRET) {
						// Protect(
						L.savedpc = InstructionPtr.Assign(pc);
						luaD_checkstack(L, n);
						base_ = L.base_;
						// );
						ra = RA(L, base_, i); // previous call may change the
												// stack
						b = n;
						L.top = lua_TValue.OpAddition(ra, n);// L.top = ra + n;
					}
					for (j = 0; j < b; j++) {
						if (j < n) {
							setobjs2s(L, lua_TValue.OpAddition(ra, j),
									lua_TValue.OpAddition(lua_TValue
											.OpSubtraction(ci.base_, n), j));// setobjs2s(L,
																				// ra
																				// +
																				// j,
																				// Lua.lua_TValue.OpSubtraction(ci.base_,
																				// n)
																				// +
																				// j);
						} else {
							setnilvalue(lua_TValue.OpAddition(ra, j));// setnilvalue(ra
																		// + j);
						}
					}
					continue;
				}
				}
			}
		}
	}

	public static final int EOZ = -1; // end of stream

	// public class Zio : Zio { };

	public static int char2int(char c) {
		return (int) c;
	}

	public static int zgetc(Zio z) {
		if (z.n-- > 0) {
			int ch = char2int(z.p.getItem(0));
			z.p.inc();
			return ch;
		} else {
			return luaZ_fill(z);
		}
	}

	public static class Mbuffer {
		public CharPtr buffer = new CharPtr();
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public uint n;
		public int n;
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public uint buffsize;
		public int buffsize;
	}

	public static void luaZ_initbuffer(lua_State L, Mbuffer buff) {
		buff.buffer = null;
	}

	public static CharPtr luaZ_buffer(Mbuffer buff) {
		return buff.buffer;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static uint luaZ_sizebuffer(Mbuffer buff)
	public static int luaZ_sizebuffer(Mbuffer buff) {
		return buff.buffsize;
	}

	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static uint luaZ_bufflen(Mbuffer buff)
	public static int luaZ_bufflen(Mbuffer buff) {
		return buff.n;
	}

	public static void luaZ_resetbuffer(Mbuffer buff) {
		buff.n = 0;
	}

	public static void luaZ_resizebuffer(lua_State L, Mbuffer buff, int size) {
		if (buff.buffer == null) {
			buff.buffer = new CharPtr();
		}
		RefObject<char[]> tempRef_chars = new RefObject<char[]>(
				buff.buffer.chars);
		Lua.cluaM_reallocvector(L, tempRef_chars,
				(int) buff.buffsize, size, Character.class);
		buff.buffer.chars = tempRef_chars.argvalue;
		buff.buffsize = (int) buff.buffer.chars.length;
	}

	public static void luaZ_freebuffer(lua_State L, Mbuffer buff) {
		luaZ_resizebuffer(L, buff, 0);
	}

	/* --------- Private Part ------------------ */

	public static class Zio {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: public uint n;
		public int n; // bytes still unread
		public CharPtr p; // current position in buffer
		public IDelegate reader;// public lua_Reader reader;
		public Object data; // additional data
		public lua_State L; // Lua state (for reader)
	}

	public static int luaZ_fill(Zio z) {
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint size;
		int size = 0;
		lua_State L = z.L;
		CharPtr buff;
		lua_unlock(L);
		RefObject<Integer> tempRef_size = new RefObject<Integer>(size);
		buff = (CharPtr) z.reader
				.invoke(new Object[] { L, z.data, tempRef_size });
		size = tempRef_size.argvalue;
		lua_lock(L);
		if (buff == null || size == 0) {
			return EOZ;
		}
		z.n = size - 1;
		z.p = new CharPtr(buff);
		int result = char2int(z.p.getItem(0));
		z.p.inc();
		return result;
	}

	public static int luaZ_lookahead(Zio z) {
		if (z.n == 0) {
			if (luaZ_fill(z) == EOZ) {
				return EOZ;
			} else {
				z.n++; // luaZ_fill removed first byte; put back it
				z.p.dec();
			}
		}
		return char2int(z.p.getItem(0));
	}

	public static void luaZ_init(lua_State L, Zio z, IDelegate reader,
			Object data) {
		z.L = L;
		z.reader = reader;
		z.data = data;
		z.n = 0;
		z.p = null;
	}

	/* --------------------------------------------------------------- read --- */
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static uint luaZ_read (Zio z, CharPtr b, uint n)
	public static int luaZ_read(Zio z, CharPtr b, int n) {
		b = new CharPtr(b);
		while (n != 0) {
			// FUCK WARNING: Unsigned integer types have no direct equivalent in
			// Java:
			// ORIGINAL LINE: uint m;
			int m;
			if (luaZ_lookahead(z) == EOZ) {
				return n; // return number of missing bytes
			}
			m = (n <= z.n) ? n : z.n; // min. between n and z.n
			memcpy(b, z.p, m);
			z.n -= m;
			z.p = CharPtr.OpAddition(z.p, m);// z.p += m;
			b = CharPtr.OpAddition(b, m);// b = b + m;
			n -= m;
		}
		return 0;
	}

	/* ------------------------------------------------------------------------ */
	// FUCK WARNING: Unsigned integer types have no direct equivalent in Java:
	// ORIGINAL LINE: public static CharPtr luaZ_openspace (lua_State L, Mbuffer
	// buff, uint n)
	public static CharPtr luaZ_openspace(lua_State L, Mbuffer buff, int n) {
		if (n > buff.buffsize) {
			if (n < LUA_MINBUFFER) {
				n = LUA_MINBUFFER;
			}
			luaZ_resizebuffer(L, buff, (int) n);
		}
		return buff.buffer;
	}

	public static void luaU_print(Proto f, int full) {
		PrintFunction(f, full);
	}

	// /#define Sizeof(x) ((int)sizeof(x))
	// /#define VOID(p) ((const void*)(p))

	public static void PrintString(TString ts) {
		CharPtr s = getstr(ts);
		// FUCK WARNING: Unsigned integer types have no direct equivalent in
		// Java:
		// ORIGINAL LINE: uint i,n=ts.tsv.len;
		int i, n = ts.gettsv().len;
		putchar('"');
		for (i = 0; i < n; i++) {
			int c = s.getItem(i);
			switch (c) {
			case '"':
				printf("\\\"");
				break;
			case '\\':
				printf("\\\\");
				break;
			/*
			 * case '\a': printf("\\a"); break;
			 */
			case '\b':
				printf("\\b");
				break;
			case '\f':
				printf("\\f");
				break;
			case '\n':
				printf("\\n");
				break;
			case '\r':
				printf("\\r");
				break;
			case '\t':
				printf("\\t");
				break;
			/*
			 * case '\v': printf("\\v"); break;
			 */
			default:
				if (isprint((byte) c)) {
					putchar(c);
				} else {
					printf("\\%03u", (byte) c);
				}
				break;
			}
		}
		putchar('"');
	}

	public static void PrintConstant(Proto f, int i) {
		/* const */
		lua_TValue o = f.k[i];
		switch (ttype(o)) {
		case LUA_TNIL:
			printf("nil");
			break;
		case LUA_TBOOLEAN:
			printf(bvalue(o) != 0 ? "true" : "false");
			break;
		case LUA_TNUMBER:
			printf(LUA_NUMBER_FMT, nvalue(o));
			break;
		case LUA_TSTRING:
			PrintString(rawtsvalue(o));
			break;
		default: // cannot happen
			printf("? type=%d", ttype(o));
			break;
		}
	}

	public static void PrintCode(Proto f) {
		Long[] code = f.code;
		int pc, n = f.sizecode;
		for (pc = 0; pc < n; pc++) {
			long i = f.code[pc];
			OpCode o = GET_OPCODE(i);
			int a = GETARG_A(i);
			int b = GETARG_B(i);
			int c = GETARG_C(i);
			int bx = GETARG_Bx(i);
			int sbx = GETARG_sBx(i);
			int line = getline(f, pc);
			printf("\t%d\t", pc + 1);
			if (line > 0) {
				printf("[%d]\t", line);
			} else {
				printf("[-]\t");
			}
			printf("%-9s\t", luaP_opnames[o.getValue()]);
			switch (getOpMode(o)) {
			case iABC:
				printf("%d", a);
				if (getBMode(o) != OpArgMask.OpArgN) {
					printf(" %d", (ISK(b) != 0) ? (-1 - INDEXK(b)) : b);
				}
				if (getCMode(o) != OpArgMask.OpArgN) {
					printf(" %d", (ISK(c) != 0) ? (-1 - INDEXK(c)) : c);
				}
				break;
			case iABx:
				if (getBMode(o) == OpArgMask.OpArgK) {
					printf("%d %d", a, -1 - bx);
				} else {
					printf("%d %d", a, bx);
				}
				break;
			case iAsBx:
				if (o == OpCode.OP_JMP) {
					printf("%d", sbx);
				} else {
					printf("%d %d", a, sbx);
				}
				break;
			}
			switch (o) {
			case OP_LOADK:
				printf("\t; ");
				PrintConstant(f, bx);
				break;
			case OP_GETUPVAL:
			case OP_SETUPVAL:
				printf("\t; %s", (f.sizeupvalues > 0) ? getstr(f.upvalues[b])
						: "-");
				break;
			case OP_GETGLOBAL:
			case OP_SETGLOBAL:
				printf("\t; %s", svalue(f.k[bx]));
				break;
			case OP_GETTABLE:
			case OP_SELF:
				if (ISK(c) != 0) {
					printf("\t; ");
					PrintConstant(f, INDEXK(c));
				}
				break;
			case OP_SETTABLE:
			case OP_ADD:
			case OP_SUB:
			case OP_MUL:
			case OP_DIV:
			case OP_POW:
			case OP_EQ:
			case OP_LT:
			case OP_LE:
				if (ISK(b) != 0 || ISK(c) != 0) {
					printf("\t; ");
					if (ISK(b) != 0) {
						PrintConstant(f, INDEXK(b));
					} else {
						printf("-");
					}
					printf(" ");
					if (ISK(c) != 0) {
						PrintConstant(f, INDEXK(c));
					} else {
						printf("-");
					}
				}
				break;
			case OP_JMP:
			case OP_FORLOOP:
			case OP_FORPREP:
				printf("\t; to %d", sbx + pc + 2);
				break;
			case OP_CLOSURE:
				printf("\t; %p", VOID(f.p[bx]));
				break;
			case OP_SETLIST:
				if (c == 0) {
					printf("\t; %d", (long) code[++pc]);
				} else {
					printf("\t; %d", c);
				}
				break;
			default:
				break;
			}
			printf("\n");
		}
	}

	public static String SS(int x) {
		return (x == 1) ? "" : "s";
	}

	// /#define S(x) x,SS(x)

	public static void PrintHeader(Proto f) {
		CharPtr s = getstr(f.source);
		if (s.getItem(0) == '@' || s.getItem(0) == '=') {
			s = s.next();
		} else if (s.getItem(0) == LUA_SIGNATURE.charAt(0)) {
			s = new CharPtr("(bstring)");
		} else {
			s = new CharPtr("(string)");
		}
		printf("\n%s <%s:%d,%d> (%d int%s, %d bytes at %p)\n",
				(f.linedefined == 0) ? "main" : "function", s, f.linedefined,
				f.lastlinedefined, f.sizecode, SS(f.sizecode), f.sizecode
						* GetUnmanagedSize(int.class), VOID(f));
		printf("%d%s param%s, %d slot%s, %d upvalue%s, ", f.numparams,
				(f.is_vararg != 0) ? "+" : "", SS(f.numparams), f.maxstacksize,
				SS(f.maxstacksize), f.nups, SS(f.nups));
		printf("%d local%s, %d constant%s, %d function%s\n", f.sizelocvars,
				SS(f.sizelocvars), f.sizek, SS(f.sizek), f.sizep, SS(f.sizep));
	}

	public static void PrintConstants(Proto f) {
		int i, n = f.sizek;
		printf("constants (%d) for %p:\n", n, VOID(f));
		for (i = 0; i < n; i++) {
			printf("\t%d\t", i + 1);
			PrintConstant(f, i);
			printf("\n");
		}
	}

	public static void PrintLocals(Proto f) {
		int i, n = f.sizelocvars;
		printf("locals (%d) for %p:\n", n, VOID(f));
		for (i = 0; i < n; i++) {
			printf("\t%d\t%s\t%d\t%d\n", i, getstr(f.locvars[i].varname),
					f.locvars[i].startpc + 1, f.locvars[i].endpc + 1);
		}
	}

	public static void PrintUpvalues(Proto f) {
		int i, n = f.sizeupvalues;
		printf("upvalues (%d) for %p:\n", n, VOID(f));
		if (f.upvalues == null) {
			return;
		}
		for (i = 0; i < n; i++) {
			printf("\t%d\t%s\n", i, getstr(f.upvalues[i]));
		}
	}

	public static void PrintFunction(Proto f, int full) {
		int i, n = f.sizep;
		PrintHeader(f);
		PrintCode(f);
		if (full != 0) {
			PrintConstants(f);
			PrintLocals(f);
			PrintUpvalues(f);
		}
		for (i = 0; i < n; i++) {
			PrintFunction(f.p[i], full);
		}
	}
	
	
/******************************************************************
 * **********************Lua Socket Functions**********************
 * ***************************************************************/
	
	public static int auxiliar_open(lua_State L)
	{
		return 0;
	}
	
	public static void auxiliar_newclass(lua_State L, CharPtr classname, luaL_Reg[] func)
	{
		luaL_newmetatable(L, classname); /* mt */
	    /* create __index table to place methods */
	    lua_pushstring(L, new CharPtr("__index"));    /* mt,"__index" */
	    lua_newtable(L);                 /* mt,"__index",it */ 
	    /* put class name into class metatable */
	    lua_pushstring(L, "class");      /* mt,"__index",it,"class" */
	    lua_pushstring(L, classname);    /* mt,"__index",it,"class",classname */
	    lua_rawset(L, -3);               /* mt,"__index",it */
	    /* pass all methods that start with _ to the metatable, and all others
	     * to the index table */
	    int reg_num = 0;
	    for (; func[reg_num].name != null; reg_num++) {     /* mt,"__index",it */
	        lua_pushstring(L, func[reg_num].name);
	        lua_pushcfunction(L, func[reg_num].func);
	        lua_rawset(L, func[reg_num].name.getItem(0) == '_' ? -5: -3);
	    }
	    lua_rawset(L, -3);               /* mt */
	    lua_pop(L, 1);
	}
	
	public static int auxiliar_tostring(lua_State L) 
	{
	    if (lua_getmetatable(L, 1) == 0)
	    {
	    	lua_pushstring(L, "invalid object passed to 'auxiliar.c:__tostring'");
		    lua_error(L);
		    return 1;
	    }
	    lua_pushstring(L, "__index");
	    lua_gettable(L, -2);
	    if (!lua_istable(L, -1))
	    {
	    	lua_pushstring(L, "invalid object passed to 'auxiliar.c:__tostring'");
		    lua_error(L);
		    return 1;
	    }
	    lua_pushstring(L, "class");
	    lua_gettable(L, -2);
	    if (lua_isstring(L, -1) == 0)
	    {
	    	lua_pushstring(L, "invalid object passed to 'auxiliar.c:__tostring'");
		    lua_error(L);
		    return 1;
	    }
	    Object obj = lua_touserdata(L, 1);
	    lua_pushfstring(L, "%s: %s", lua_tostring(L, -1), obj.toString());
	    return 1;	    
	}
	
	public static void auxiliar_add2group(lua_State L, CharPtr classname, CharPtr groupname) 
	{
	    luaL_getmetatable(L, classname);
	    lua_pushstring(L, groupname);
	    lua_pushboolean(L, 1);
	    lua_rawset(L, -3);
	    lua_pop(L, 1);
	}
	
	public static int auxiliar_checkboolean(lua_State L, int objidx) {
	    if (!lua_isboolean(L, objidx))
	        luaL_typerror(L, objidx, lua_typename(L, LUA_TBOOLEAN));
	    return lua_toboolean(L, objidx);
	}
	
	public static Object auxiliar_checkclass(lua_State L, CharPtr classname, int objidx) {
	    Object data = auxiliar_getclassudata(L, classname, objidx);
	    if (data == null) {
	        luaL_argerror(L, objidx, new CharPtr(classname.toString() + " expected"));
	    }
	    return data;
	}
	
	public static Object auxiliar_checkgroup(lua_State L, CharPtr groupname, int objidx) {
	    Object data = auxiliar_getgroupudata(L, groupname, objidx);
	    if (data == null) {
	    	luaL_argerror(L, objidx, new CharPtr(groupname.toString() + " expected"));
	    }
	    return data;
	}
	
	public static void auxiliar_setclass(lua_State L, CharPtr classname, int objidx) {
	    luaL_getmetatable(L, classname);
	    if (objidx < 0) objidx--;
	    lua_setmetatable(L, objidx);
	}
	
	public static Object auxiliar_getgroupudata(lua_State L, CharPtr groupname, int objidx) {
	    if (lua_getmetatable(L, objidx) == 0)
	        return null;
	    lua_pushstring(L, groupname);
	    lua_rawget(L, -2);
	    if (lua_isnil(L, -1)) {
	        lua_pop(L, 2);
	        return null;
	    } else {
	        lua_pop(L, 2);
	        return lua_touserdata(L, objidx);
	    }
	}

	public static Object auxiliar_getclassudata(lua_State L, CharPtr classname, int objidx) {
	    return luaL_checkudata(L, objidx, classname);
	}
	
	public static int buffer_open(lua_State L) {
	    return 0;
	}

	/*-------------------------------------------------------------------------*\
	* Initializes C structure 
	\*-------------------------------------------------------------------------*/
	public static void buffer_init(pBuffer buf, pIO io, pTimeout tm) {
		buf.first = buf.last = 0;
	    buf.io = io;
	    buf.tm = tm;
	    buf.received = buf.sent = 0;
	    buf.birthday = timeout_gettime();
	}

	/*-------------------------------------------------------------------------*\
	* object:getstats() interface
	\*-------------------------------------------------------------------------*/
	public static int buffer_meth_getstats(lua_State L, pBuffer buf) {
	    lua_pushnumber(L, buf.received);
	    lua_pushnumber(L, buf.sent);
	    lua_pushnumber(L, timeout_gettime() - buf.birthday);
	    return 3;
	}

	/*-------------------------------------------------------------------------*\
	* object:setstats() interface
	\*-------------------------------------------------------------------------*/
	public static int buffer_meth_setstats(lua_State L, pBuffer buf) {
	    buf.received = (long) luaL_optnumber(L, 2, buf.received); 
	    buf.sent = (long) luaL_optnumber(L, 3, buf.sent); 
	    if (lua_isnumber(L, 4) > 0) buf.birthday = timeout_gettime() - lua_tonumber(L, 4);
	    lua_pushnumber(L, 1);
	    return 1;
	}

	/*-------------------------------------------------------------------------*\
	* object:send() interface
	\*-------------------------------------------------------------------------*/
	public static int buffer_meth_send(lua_State L, pBuffer buf) {
	    int top = lua_gettop(L);
	    int err = pIO.IO_DONE;
	    int size = 0, sent = 0;
	    RefObject<Integer> sizeP = new RefObject<Integer>(size);
	    CharPtr data = luaL_checklstring(L, 2, sizeP);
	    size = sizeP.argvalue;
	    long start = (long) luaL_optnumber(L, 3, 1);
	    long end = (long) luaL_optnumber(L, 4, -1);
	    pTimeout tm = timeout_markstart(buf.tm);
	    if (start < 0) start = (long) (size+start+1);
	    if (end < 0) end = (long) (size+end+1);
	    if (start < 1) start = (long) 1;
	    if (end > (long) size) end = (long) size;
	    RefObject<Integer> sentP = new RefObject<Integer>(sent);
	    if (start <= end) err = sendraw(buf, CharPtr.OpAddition(data, (int)start- 1), ((int)(end-start+1)), sentP);
	    sent = sentP.argvalue;
	    /* check if there was an error */
	    if (err != pIO.IO_DONE) {
	        lua_pushnil(L);
	        lua_pushstring(L, (CharPtr) buf.io.error.invoke(new Object[] { buf.io.ctx, err })); 
	        lua_pushnumber(L, sent+start-1);
	    } else {
	        lua_pushnumber(L, sent+start-1);
	        lua_pushnil(L);
	        lua_pushnil(L);
	    }
	    return lua_gettop(L) - top;
	}

	/*-------------------------------------------------------------------------*\
	* object:receive() interface
	\*-------------------------------------------------------------------------*/
	public static int buffer_meth_receive(lua_State L, pBuffer buf) {
	    int err = pIO.IO_DONE, top = lua_gettop(L);
	    luaL_Buffer b;
	    int size = 0;
	    RefObject<Integer> sizeP = new RefObject<Integer>(size);
	    CharPtr part = luaL_optlstring(L, 3, "", sizeP);
	    size = sizeP.argvalue;
	    pTimeout tm = timeout_markstart(buf.tm);
	    /* initialize buffer with optional extra prefix 
	     * (useful for concatenating previous partial results) */
	    b = new luaL_Buffer();
		luaL_buffinit(L, b);
	    luaL_addlstring(b, part, size);
	    /* receive new patterns */
	    if (lua_isnumber(L, 2) == 0) {
	        CharPtr p= luaL_optstring(L, 2, "*l");
	        if (p.getItem(0) == '*' && p.getItem(1) == 'l') err = recvline(buf, b);
	        else if (p.getItem(0) == '*' && p.getItem(1) == 'a') err = recvall(buf, b); 
	        else luaL_argcheck(L, false, 2, "invalid receive pattern");
	        /* get a fixed number of bytes (minus what was already partially 
	         * received) */
	    } else err = recvraw(buf, (int) lua_tonumber(L, 2)-size, b);
	    /* check if there was an error */
	    if (err != pIO.IO_DONE) {
	        /* we can't push anyting in the stack before pushing the
	         * contents of the buffer. this is the reason for the complication */
	        luaL_pushresult(b);
	        lua_pushstring(L, new CharPtr((String)buf.io.error.invoke(new Object[] {buf.io.ctx, err}))); 
	        lua_pushvalue(L, -2); 
	        lua_pushnil(L);
	        lua_replace(L, -4);
	    } else {
	        luaL_pushresult(b);
	        lua_pushnil(L);
	        lua_pushnil(L);
	    }
	    return lua_gettop(L) - top;
	}

	/*-------------------------------------------------------------------------*\
	* Determines if there is any data in the read buffer
	\*-------------------------------------------------------------------------*/
	public static boolean buffer_isempty(pBuffer buf) {
	    return buf.first >= buf.last;
	}

	/*=========================================================================*\
	* Internal functions
	\*=========================================================================*/
	/*-------------------------------------------------------------------------*\
	* Sends a block of data (unbuffered)
	\*-------------------------------------------------------------------------*/
	public static int STEPSIZE = 8192;
	public static int sendraw(pBuffer buf, CharPtr data, int count, RefObject<Integer> sent) {
	    pIO io = buf.io;
	    pTimeout tm = buf.tm;
	    int total = 0;
	    int err = pIO.IO_DONE;
	    while (total < count && err == pIO.IO_DONE) {
	        int done = 0;
	        int step = (count-total <= STEPSIZE)? count-total: STEPSIZE;
	        RefObject<Integer> doneP = new RefObject<Integer>(done);
	        err = (Integer) io.send.invoke(new Object[] { io.ctx, CharPtr.OpAddition(data, total), step, doneP, tm });
	        done = doneP.argvalue;
	        total += done;
	    }
	    sent.argvalue = total;
	    buf.sent += total;
	    return err;
	}

	/*-------------------------------------------------------------------------*\
	* Reads a fixed number of bytes (buffered)
	\*-------------------------------------------------------------------------*/
	public static int recvraw(pBuffer buf, int wanted, luaL_Buffer b) {
	    int err = pIO.IO_DONE;
	    int total = 0;
	    while (err == pIO.IO_DONE) {
	        int count = 0; CharPtr data = new CharPtr();
	        RefObject<CharPtr> dataP = new RefObject<Lua.CharPtr>(data);
	        RefObject<Integer> countP = new RefObject<Integer>(count);
	        err = buffer_get(buf, dataP, countP);
	        data = dataP.argvalue;
	        count = countP.argvalue;
	        count = Math.min(count, wanted - total);
	        luaL_addlstring(b, data, count);
	        buffer_skip(buf, count);
	        total += count;
	        if (total >= wanted) break;
	    }
	    return err;
	}

	/*-------------------------------------------------------------------------*\
	* Reads everything until the connection is closed (buffered)
	\*-------------------------------------------------------------------------*/
	public static int recvall(pBuffer buf, luaL_Buffer b) {
	    int err = pIO.IO_DONE;
	    int total = 0;
	    while (err == pIO.IO_DONE) {
	        CharPtr data = new CharPtr(); int count = 0;
	        RefObject<CharPtr> dataP = new RefObject<Lua.CharPtr>(data);
	        RefObject<Integer> countP = new RefObject<Integer>(count);
	        err = buffer_get(buf, dataP, countP);
	        data = dataP.argvalue;
	        count = countP.argvalue;
	        total += count;
	        luaL_addlstring(b, data, count);
	        buffer_skip(buf, count);
	    }
	    if (err == pIO.IO_CLOSED) {
	        if (total > 0) return pIO.IO_DONE;
	        else return pIO.IO_CLOSED;
	    } else return err;
	}

	/*-------------------------------------------------------------------------*\
	* Reads a line terminated by a CR LF pair or just by a LF. The CR and LF 
	* are not returned by the function and are discarded from the buffer
	\*-------------------------------------------------------------------------*/
	public static int recvline(pBuffer buf, luaL_Buffer b) {
	    int err = pIO.IO_DONE;
	    while (err == pIO.IO_DONE) {
	        int count = 0, pos; CharPtr data = new CharPtr();
	        RefObject<CharPtr> dataP = new RefObject<CharPtr>(data);
	        RefObject<Integer> countP = new RefObject<Integer>(count);
	        err = buffer_get(buf, dataP, countP);
	        data = dataP.argvalue;
	        count = countP.argvalue;
	        pos = 0;
	        while (pos < count && data.getItem(pos) != '\n') {
	            /* we ignore all \r's */
	            if (data.getItem(pos) != '\r') luaL_putchar(b, data.getItem(pos));
	            pos++;
	        }
	        if (pos < count) { /* found '\n' */
	            buffer_skip(buf, pos+1); /* skip '\n' too */
	            break; /* we are done */
	        } else /* reached the end of the buffer */
	            buffer_skip(buf, pos);
	    }
	    return err;
	}

	/*-------------------------------------------------------------------------*\
	* Skips a given number of bytes from read buffer. No data is read from the
	* transport layer
	\*-------------------------------------------------------------------------*/
	public static void buffer_skip(pBuffer buf, int count) {
	    buf.received += count;
	    buf.first += count;
	    if (buffer_isempty(buf)) 
	        buf.first = buf.last = 0;
	}

	/*-------------------------------------------------------------------------*\
	* Return any data available in buffer, or get more data from transport layer
	* if buffer is empty
	\*-------------------------------------------------------------------------*/
	public static int buffer_get(pBuffer buf, RefObject<CharPtr> data, RefObject<Integer> count) {
	    int err = pIO.IO_DONE;
	    pIO io = buf.io;
	    pTimeout tm = buf.tm;
	    if (buffer_isempty(buf)) {
	        int got = 0;
	        RefObject<Integer> gotP = new RefObject<Integer>(got);
	        err = (Integer)io.recv.invoke(new Object[] { io.ctx, buf.data, pBuffer.BUF_SIZE, gotP, tm });
	        got = gotP.argvalue;
	        buf.first = 0;
	        buf.last = got;
	    }
	    count.argvalue = buf.last - buf.first;
	    data.argvalue = CharPtr.OpAddition(buf.data, buf.first);
	    return err;
	}
	
	/* except functions */
	private final static luaL_Reg[] except = {
		new luaL_Reg("newtry", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return global_newtry((lua_State)arg);
				}
			}),
		new luaL_Reg("protect", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return global_protect((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null) };

	/*-------------------------------------------------------------------------*\
	* Try factory
	\*-------------------------------------------------------------------------*/
	public static void wrap(lua_State L) {
	    lua_newtable(L);
	    lua_pushnumber(L, 1);
	    lua_pushvalue(L, -3);
	    lua_settable(L, -3);
	    lua_insert(L, -2);
	    lua_pop(L, 1);
	}

	public static int finalize(lua_State L) {
	    if (lua_toboolean(L, 1) == 0) {
	        lua_pushvalue(L, lua_upvalueindex(1));
	        lua_pcall(L, 0, 0, 0);
	        lua_settop(L, 2);
	        wrap(L);
	        lua_error(L);
	        return 0;
	    } else return lua_gettop(L);
	}

	public static int do_nothing(lua_State L) { 
	    return 0; 
	}

	public final static IDelegate do_nothing = new IDelegate()
	{
		
		@Override
		public Object invoke()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object arg1, Object arg2)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object arg)
		{
			do_nothing((lua_State)arg);
			return null;
		}
		
		@Override
		public Object invoke(Object[] args)
		{
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	public final static IDelegate finalize = new IDelegate()
	{
		
		@Override
		public Object invoke()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object arg1, Object arg2)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object arg)
		{
			return Lua.finalize(((lua_State)arg));
		}
		
		@Override
		public Object invoke(Object[] args)
		{
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	public static int global_newtry(lua_State L) {
	    lua_settop(L, 1);
	    if (lua_isnil(L, 1)) lua_pushcfunction(L, do_nothing);
	    lua_pushcclosure(L, finalize, 1);
	    return 1;
	}

	/*-------------------------------------------------------------------------*\
	* Protect factory
	\*-------------------------------------------------------------------------*/
	public static int unwrap(lua_State L) {
	    if (lua_istable(L, -1)) {
	        lua_pushnumber(L, 1);
	        lua_gettable(L, -2);
	        lua_pushnil(L);
	        lua_insert(L, -2);
	        return 1;
	    } else return 0;
	}

	public static int protected_(lua_State L) {
	    lua_pushvalue(L, lua_upvalueindex(1));
	    lua_insert(L, 1);
	    if (lua_pcall(L, lua_gettop(L) - 1, LUA_MULTRET, 0) != 0) {
	        if (unwrap(L) > 0) return 2;
	        else lua_error(L);
	        return 0;
	    } else return lua_gettop(L);
	}

	public final static IDelegate protected_ = new IDelegate()
	{
		
		@Override
		public Object invoke()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object arg1, Object arg2)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object arg)
		{
			protected_((lua_State)arg);
			return null;
		}
		
		@Override
		public Object invoke(Object[] args)
		{
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	public static int global_protect(lua_State L) {
	    lua_pushcclosure(L, protected_, 1);
	    return 1;
	}

	/*-------------------------------------------------------------------------*\
	* Init module
	\*-------------------------------------------------------------------------*/
	public static int except_open(lua_State L) {
		luaI_openlib(L, null, except, 0);
	    return 0;
	}
	
	public static void io_init(pIO io, IDelegate send, IDelegate recv, IDelegate error, Object ctx) {
	    io.send = send;
	    io.recv = recv;
	    io.error = error;
	    io.ctx = ctx;
	}

	/*-------------------------------------------------------------------------*\
	* I/O error strings
	\*-------------------------------------------------------------------------*/
	public static CharPtr io_strerror(int err) {
	    switch (err) {
	        case pIO.IO_DONE: return null;
	        case pIO.IO_CLOSED: return new CharPtr("closed");
	        case pIO.IO_TIMEOUT: return new CharPtr("timeout");
	        default: return new CharPtr("unknown error"); 
	    }
	}
	
	private final static String MIME_VERSION = "MIME 1.0.2";
	private final static String MIME_COPYRIGHT = "Copyright (C) 2004-2007 Diego Nehab";
	private final static String MIME_AUTHORS = "Diego Nehab";
	
	public final static CharPtr CRLF = new CharPtr("\r\n");
	public final static CharPtr EQCRLF = new CharPtr("=\r\n");
	
	public static luaL_Reg[] mime = {
		new luaL_Reg("dot", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return mime_global_dot((lua_State)arg);
				}
			}),
		new luaL_Reg("b64", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return mime_global_b64((lua_State)arg);
				}
			}),
		new luaL_Reg("eol", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return mime_global_eol((lua_State)arg);
				}
			}),
		new luaL_Reg("qp", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return mime_global_qp((lua_State)arg);
				}
			}),
		new luaL_Reg("qpwrp", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return mime_global_qpwrp((lua_State)arg);
				}
			}),
		new luaL_Reg("unb64", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return mime_global_unb64((lua_State)arg);
				}
			}),
		new luaL_Reg("unqp", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return mime_global_unqp((lua_State)arg);
				}
			}),
		new luaL_Reg("wrp", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return mime_global_wrp((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null)
	};
	
	final static CharPtr qpclass = new CharPtr(new char[256]);
	final static CharPtr qpbase = new CharPtr("0123456789ABCDEF");
	final static CharPtr qpunbase = new CharPtr(new char[256]);
	
	private static final int QP_PLAIN = 0;
	private static final int QP_QUOTED = 1;
	private static final int QP_CR = 2;
	private static final int QP_IF_LAST = 3;
	
	final static CharPtr b64base = new CharPtr("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
	final static CharPtr b64unbase = new CharPtr(new char[256]);
	
	public static int luaopen_mime_core(lua_State L)
	{
	    luaI_openlib(L, new CharPtr("mime"), mime, 0);
	    /* make version string available to scripts */
	    lua_pushstring(L, "_VERSION");
	    lua_pushstring(L, MIME_VERSION);
	    lua_rawset(L, -3);
	    /* initialize lookup tables */
	    qpsetup(qpclass, qpunbase);
	    b64setup(b64unbase);
	    return 1;
	}
	
	public static int mime_global_wrp(lua_State L)
	{
	    int size = 0;
	    int left = (int) luaL_checknumber(L, 1);
	    RefObject<Integer> sizeP = new RefObject<Integer>(size);
	    CharPtr input = luaL_optlstring(L, 2, (CharPtr)null, sizeP);
	    size = sizeP.argvalue;
	    int length = (int) luaL_optnumber(L, 3, 76);
	    luaL_Buffer buffer = new luaL_Buffer();
	    /* end of input black-hole */
	    if (input == null) {
	        /* if last line has not been terminated, add a line break */
	        if (left < length) lua_pushstring(L, CRLF);
	        /* otherwise, we are done */
	        else lua_pushnil(L);
	        lua_pushnumber(L, length);
	        return 2;
	    } 
	    CharPtr last = CharPtr.OpAddition(input, size);
	    luaL_buffinit(L, buffer);
	    while (CharPtr.OpLessThan(input, last)) 
	    {
	        switch (input.chars[input.index]) {
	            case '\r':
	                break;
	            case '\n':
	                luaL_addstring(buffer, CRLF);
	                left = length;
	                break;
	            default:
	                if (left <= 0) {
	                    left = length;
	                    luaL_addstring(buffer, CRLF);
	                }
	                luaL_putchar(buffer, input.chars[input.index]);
	                left--;
	                break;
	        }
	        input.inc();
	    }
	    luaL_pushresult(buffer);
	    lua_pushnumber(L, left);
	    return 2;
	}
	
	public static void b64setup(CharPtr b64unbase) 
	{
	    int i;
	    for (i = 0; i <= 255; i++) b64unbase.setItem(i, (char)255);
	    for (i = 0; i < 64; i++) b64unbase.setItem(b64base.getItem(i), (char) i);
	    b64unbase.setItem((int)'=', (char)0);
	}
	
	public static long b64encode(char c, CharPtr input, int size, 
	        luaL_Buffer buffer)
	{
	    input.setItem(size++, c);
	    if (size == 3) {
	        char code[] = new char[4];
	        long value = 0;
	        value += input.getItem(0); value <<= 8;
	        value += input.getItem(1); value <<= 8;
	        value += input.getItem(2); 
	        code[3] = b64base.getItem(value & 0x3f); value >>= 6;
	        code[2] = b64base.getItem(value & 0x3f); value >>= 6;
	        code[1] = b64base.getItem(value & 0x3f); value >>= 6;
	        code[0] = b64base.getItem(value);
	        luaL_addlstring(buffer, new CharPtr(code), 4);
	        size = 0;
	    }
	    return size;
	}
	
	public static long b64pad(CharPtr input, int size, 
	        luaL_Buffer buffer)
	{
	    long value = 0;
	    char code[] = {'=', '=', '=', '='};
	    switch (size) {
	        case 1:
	            value = input.getItem(0) << 4;
	            code[1] = b64base.getItem(value & 0x3f); value >>= 6;
	            code[0] = b64base.getItem(value);
	            luaL_addlstring(buffer, new CharPtr(code), 4);
	            break;
	        case 2:
	            value = input.getItem(0); value <<= 8; 
	            value |= input.getItem(1); value <<= 2;
	            code[2] = b64base.getItem(value & 0x3f); value >>= 6;
	            code[1] = b64base.getItem(value & 0x3f); value >>= 6;
	            code[0] = b64base.getItem(value);
	            luaL_addlstring(buffer, new CharPtr(code), 4);
	            break;
	        default:
	            break;
	    }
	    return 0;
	}
	
	public static long b64decode(char c, CharPtr input, int size, 
	        luaL_Buffer buffer)
	{
	    /* ignore invalid characters */
	    if (b64unbase.getItem(c) > 64) return size;
	    input.setItem(size++, c);
	    /* decode atom */
	    if (size == 4) {
	        char[] decoded = new char[3];
	        int valid, value = 0;
	        value =  b64unbase.getItem(input.getItem(0)); value <<= 6;
	        value |= b64unbase.getItem(input.getItem(1)); value <<= 6;
	        value |= b64unbase.getItem(input.getItem(2)); value <<= 6;
	        value |= b64unbase.getItem(input.getItem(3));
	        decoded[2] = (char) (value & 0xff); value >>= 8;
	        decoded[1] = (char) (value & 0xff); value >>= 8;
	        decoded[0] = (char) value;
	        /* take care of paddding */
	        valid = (input.getItem(2) == '=') ? 1 : (input.getItem(3) == '=') ? 2 : 3; 
	        luaL_addlstring(buffer, new CharPtr(decoded), valid);
	        return 0;
	    /* need more data */
	    } else return size;
	}
	
	public static int mime_global_b64(lua_State L)
	{
		CharPtr atom = new CharPtr(new char[3]);
	    int isize = 0, asize = 0;
	    RefObject<Integer> isizeP = new RefObject<Integer>(isize);
	    CharPtr input = (CharPtr) luaL_optlstring(L, 1, (CharPtr)null, isizeP);
	    isize = isizeP.argvalue;
	    /* end-of-input blackhole */
	    if (input == null || isize == 0) {
	        lua_pushnil(L);
	        lua_pushnil(L);
	        return 2;
	    }
	    //CharPtr last = CharPtr.OpAddition(input, isize);
	    luaL_Buffer buffer = new luaL_Buffer();
	    /* process first part of the input */
	    luaL_buffinit(L, buffer);
	    //byte[] encoded = Base64.encodeBytesToBytes(input.toByteArrayNoFinisher());
	    byte[] encoded = com.dk.base64.Base64.encode(input.toByteArrayNoFinisher());
	    /*while (CharPtr.OpLessThan(input, last)) 
	    {
	        asize = (int) b64encode(input.getItem(), atom, (int)asize, buffer);
	        input.inc();
	    }*/
	    CharPtr result = new CharPtr();
	    result.setByteArray(encoded);
	    luaL_addlstring(buffer, result, encoded.length);
	    /*DK Comment-isizeP.argvalue = isize;
	    input = (CharPtr)luaL_optlstring(L, 2, (CharPtr)null, isizeP);
	    isize = isizeP.argvalue;*/
	    /* if second part is nil, we are done */
	    //DK Comment-if (input == null || isize == 0) {
	        asize = (int) b64pad(atom, (int)asize, buffer);
	        luaL_pushresult(buffer);
	        if (lua_tostring(L, -1) == null) lua_pushnil(L);
	        lua_pushnil(L);
	        return 2;
	    //}
	    /* otherwise process the second part */
	    /*last = CharPtr.OpAddition(input, isize);
	    while (CharPtr.OpLessThan(input,last))
	    {
	    	asize = (int) b64encode(input.getItem(), atom, (int)asize, buffer);
	    	input.inc();
	    }*/
	    /*DK Comment - encoded = Base64.encodeBytesToBytes(input.toByteArray());
	    result = new CharPtr();
	    result.setByteArray(encoded);
	    luaL_addlstring(buffer, result, encoded.length);
	    luaL_pushresult(buffer);
	    lua_pushlstring(L, new CharPtr(atom), asize);
	    return 2;*/
	}
	
	public static int mime_global_unb64(lua_State L)
	{
		CharPtr atom = new CharPtr(new char[4]);
	    int isize = 0, asize = 0;
	    
	    RefObject<Integer> isizeP = new RefObject<Integer>(isize);
	    CharPtr input = luaL_optlstring(L, 1, (CharPtr)null, isizeP);
	    isize = isizeP.argvalue;
	    /* end-of-input blackhole */
	    if (input == null) {
	        lua_pushnil(L);
	        lua_pushnil(L);
	        return 2;
	    }
	    CharPtr last = CharPtr.OpAddition(input, isize);
	    luaL_Buffer buffer = new luaL_Buffer();
	    /* process first part of the input */
	    luaL_buffinit(L, buffer);
	    
	    byte[] decoded = new byte[1];
	    try
		{
			 decoded = Base64.decode(input.toByteArray());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		CharPtr result = new CharPtr();
		result.setByteArray(decoded);
		luaL_addstring(buffer, result);
	    /*while (CharPtr.OpLessThan(input,last))
	    {
	        asize = (int) b64decode(input.getItem(), atom, (int)asize, buffer);
	        input.inc();
	    }*/
	    isizeP.argvalue = isize;
	    input = (CharPtr)luaL_optlstring(L, 2, (CharPtr)null, isizeP);
	    isize = isizeP.argvalue;
	    /* if second is nil, we are done */
	    if (input == null) {
	        luaL_pushresult(buffer);
	        if (lua_tostring(L, -1) == null) lua_pushnil(L);
	        lua_pushnil(L);
	        return 2;
	    }
	    
	    decoded = new byte[1];
	    try
		{
			 decoded = Base64.decode(input.toByteArray());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	    result = new CharPtr();
		result.setByteArray(decoded);
		luaL_addstring(buffer, result);
	    /* otherwise, process the rest of the input */
	    /*last = CharPtr.OpAddition(input, isize);
	    while (CharPtr.OpLessThan(input,last))
	    {
	    	asize = (int) b64decode(input.getItem(), atom, (int)asize, buffer);
	    	input.inc();
	    }*/
	    luaL_pushresult(buffer);
	    lua_pushlstring(L, atom, asize);
	    return 2;
	}
	
	public static void qpsetup(CharPtr qpclass, CharPtr qpunbase)
	{
	    int i;
	    for (i = 0; i < 256; i++) qpclass.setItem(i,(char) QP_QUOTED);
	    for (i = 33; i <= 60; i++) qpclass.setItem(i,(char) QP_PLAIN);
	    for (i = 62; i <= 126; i++) qpclass.setItem(i,(char) QP_PLAIN);
	    qpclass.setItem('\t', (char) QP_IF_LAST);; 
	    qpclass.setItem(' ', (char) QP_IF_LAST);;
	    qpclass.setItem('\r', (char) QP_CR);;
	    for (i = 0; i < 256; i++) qpunbase.setItem(i, (char) 255);
	    qpunbase.setItem('0', (char) 0); qpunbase.setItem('1', (char) 1); qpunbase.setItem('2', (char) 2);
	    qpunbase.setItem('3', (char) 3); qpunbase.setItem('4', (char) 4); qpunbase.setItem('5', (char) 5);
	    qpunbase.setItem('6', (char) 6); qpunbase.setItem('7', (char) 7); qpunbase.setItem('8',(char)  8);
	    qpunbase.setItem('9', (char) 9); qpunbase.setItem('A', (char) 10); qpunbase.setItem('a', (char) 10);
	    qpunbase.setItem('B', (char) 11); qpunbase.setItem('b', (char) 11); qpunbase.setItem('C', (char) 12);
	    qpunbase.setItem('c', (char) 12); qpunbase.setItem('D', (char) 13); qpunbase.setItem('d', (char) 13);
	    qpunbase.setItem('E', (char) 14); qpunbase.setItem('e', (char) 14); qpunbase.setItem('F', (char) 15);
	    qpunbase.setItem('f', (char) 15);
	}
	
	public static void qpquote(char c, luaL_Buffer buffer)
	{
	    luaL_putchar(buffer, '=');
	    luaL_putchar(buffer, qpbase.getItem(c >> 4));
	    luaL_putchar(buffer, qpbase.getItem(c & 0x0F));
	}
	
	public static int qpencode(char c, CharPtr input, int size, 
	        CharPtr marker, luaL_Buffer buffer)
	{
	    input.setItem(size++, c);
	    /* deal with all characters we can have */
	    while (size > 0) {
	        switch (qpclass.getItem(input.getItem(0))) {
	            /* might be the CR of a CRLF sequence */
	            case QP_CR:
	                if (size < 2) return size;
	                if (input.getItem(1) == '\n') {
	                    luaL_addstring(buffer, marker);
	                    return 0;
	                } else qpquote(input.getItem(0), buffer);
	                break;
	            /* might be a space and that has to be quoted if last in line */
	            case QP_IF_LAST:
	                if (size < 3) return size;
	                /* if it is the last, quote it and we are done */
	                if (input.getItem(1) == '\r' && input.getItem(2) == '\n') {
	                    qpquote(input.getItem(0), buffer);
	                    luaL_addstring(buffer, marker);
	                    return 0;
	                } else luaL_putchar(buffer, input.getItem(0));
	                break;
	                /* might have to be quoted always */
	            case QP_QUOTED:
	                qpquote(input.getItem(0), buffer);
	                break;
	                /* might never have to be quoted */
	            default:
	                luaL_putchar(buffer, input.getItem(0));
	                break;
	        }
	        input.setItem(0, input.getItem(1)); input.setItem(1, input.getItem(2));
	        size--;
	    }
	    return 0;
	}
	
	public static int qppad(CharPtr input, int size, luaL_Buffer buffer)
	{
	    int i;
	    for (i = 0; i < size; i++) {
	        if (qpclass.getItem(input.getItem(i)) == QP_PLAIN) luaL_putchar(buffer, input.getItem(i));
	        else qpquote(input.getItem(i), buffer);
	    }
	    if (size > 0) luaL_addstring(buffer, EQCRLF);
	    return 0;
	}
	
	public static int mime_global_qp(lua_State L)
	{

	    int asize = 0, isize = 0;
	    CharPtr atom = new CharPtr(new char[3]);
	    
	    RefObject<Integer> isizeP = new RefObject<Integer>(isize);
	    CharPtr input = luaL_optlstring(L, 1, (CharPtr)null, isizeP);
	    isize = isizeP.argvalue;
	    CharPtr marker = luaL_optstring(L, 3, CRLF);
	    luaL_Buffer buffer = new luaL_Buffer();
	    /* end-of-input blackhole */
	    if (input == null) {
	        lua_pushnil(L);
	        lua_pushnil(L);
	        return 2;
	    }
	    CharPtr last = CharPtr.OpAddition(input, isize);
	    /* process first part of input */
	    luaL_buffinit(L, buffer);
	    while (CharPtr.OpLessThan(input, last))
	    {
	        asize = qpencode(input.getItem(), atom, asize, marker, buffer);
	        input.inc();
	    }
	    
	    isizeP.argvalue = isize;
	    input = luaL_optlstring(L, 2, (CharPtr) null, isizeP);
	    isize = isizeP.argvalue;
	    /* if second part is nil, we are done */
	    if (input == null) {
	        asize = qppad(atom, asize, buffer);
	        luaL_pushresult(buffer);
	        if (lua_tostring(L, -1) == null) lua_pushnil(L);
	        lua_pushnil(L);
	        return 2;
	    }
	    /* otherwise process rest of input */
	    last = CharPtr.OpAddition(input, isize);
	    while (CharPtr.OpLessThan(input, last))
	    {
	    	 asize = qpencode(input.getItem(), atom, asize, marker, buffer);
		        input.inc();
	    }
	    luaL_pushresult(buffer);
	    lua_pushlstring(L, atom, asize);
	    return 2;
	}
	
	public static int qpdecode(char c, CharPtr input, int size, luaL_Buffer buffer) {
	    int d;
	    input.setItem(size++, c);
	    /* deal with all characters we can deal */
	    switch (input.getItem(0)) {
	        /* if we have an escape character */
	        case '=': 
	            if (size < 3) return size; 
	            /* eliminate soft line break */
	            if (input.getItem(1) == '\r' && input.getItem(2) == '\n') return 0;
	            /* decode quoted representation */
	            c = qpunbase.getItem(input.getItem(1)); d = qpunbase.getItem(input.getItem(2));
	            /* if it is an invalid, do not decode */
	            if (c > 15 || d > 15) luaL_addlstring(buffer, input, 3);
	            else luaL_putchar(buffer, (char) ((c << 4) + d));
	            return 0;
	        case '\r':
	            if (size < 2) return size; 
	            if (input.getItem(1) == '\n') luaL_addlstring(buffer, input, 2);
	            return 0;
	        default:
	            if (input.getItem(0) == '\t' || (input.getItem(0) > 31 && input.getItem(0) < 127))
	                luaL_putchar(buffer, input.getItem(0));
	            return 0;
	    }
	}
	
	public static int mime_global_unqp(lua_State L)
	{
	    int asize = 0, isize = 0;
	    CharPtr atom = new CharPtr(new char[3]);
	    
	    RefObject<Integer> isizeP = new RefObject<Integer>(isize);
	    CharPtr input = luaL_optlstring(L, 1, (CharPtr)null, isizeP);
	    isize = isizeP.argvalue;
	    luaL_Buffer buffer = new luaL_Buffer();
	    /* end-of-input blackhole */
	    if (input == null) {
	        lua_pushnil(L);
	        lua_pushnil(L);
	        return 2;
	    }
	    CharPtr last = CharPtr.OpAddition(input, isize);
	    /* process first part of input */
	    luaL_buffinit(L, buffer);
	    while (CharPtr.OpLessThan(input, last))
	    {
	        asize = qpdecode(input.getItem(), atom, asize, buffer);
	        input.inc();
	    }
	    isizeP.argvalue = isize;
	    input = luaL_optlstring(L, 2, (CharPtr) null, isizeP);
	    isize = isizeP.argvalue;
	    /* if second part is nil, we are done */
	    if (input == null) {
	        luaL_pushresult(buffer);
	        if (lua_tostring(L, -1) == null) lua_pushnil(L);
	        lua_pushnil(L);
	        return 2;
	    } 
	    /* otherwise process rest of input */
	    last = CharPtr.OpAddition(input, isize);
	    while (CharPtr.OpLessThan(input, last))
	    {
	    	asize = qpdecode(input.getItem(), atom, asize, buffer);
	        input.inc();
	    }
	    luaL_pushresult(buffer);
	    lua_pushlstring(L, atom, asize);
	    return 2;
	}
	
	public static int mime_global_qpwrp(lua_State L)
	{
	    int size = 0;
	    int left = (int) luaL_checknumber(L, 1);
	    RefObject<Integer> sizeP = new RefObject<Integer>(size);
	    CharPtr input = luaL_optlstring(L, 2, (CharPtr)null, sizeP);
	    size = sizeP.argvalue;
	    int length = (int) luaL_optnumber(L, 3, 76);
	    luaL_Buffer buffer = new luaL_Buffer();
	    /* end-of-input blackhole */
	    if (input == null) {
	        if (left < length) lua_pushstring(L, EQCRLF);
	        else lua_pushnil(L);
	        lua_pushnumber(L, length);
	        return 2;
	    }
	    CharPtr last = CharPtr.OpAddition(input, size);
	    /* process all input */
	    luaL_buffinit(L, buffer);
	    while (CharPtr.OpLessThan(input, last)) {
	        switch (input.getItem()) {
	            case '\r':
	                break;
	            case '\n':
	                left = length;
	                luaL_addstring(buffer, CRLF);
	                break;
	            case '=':
	                if (left <= 3) {
	                    left = length;
	                    luaL_addstring(buffer, EQCRLF);
	                } 
	                luaL_putchar(buffer, input.getItem());
	                left--;
	                break;
	            default: 
	                if (left <= 1) {
	                    left = length;
	                    luaL_addstring(buffer, EQCRLF);
	                }
	                luaL_putchar(buffer, input.getItem());
	                left--;
	                break;
	        }
	        input.inc();
	    }
	    luaL_pushresult(buffer);
	    lua_pushnumber(L, left);
	    return 2;
	}
	
	public static boolean eolcandidate(int c) { return (c == '\r' || c == '\n'); }
	public static int eolprocess(int c, int last, CharPtr marker, 
	        luaL_Buffer buffer)
	{
	    if (eolcandidate(c)) {
	        if (eolcandidate(last)) {
	            if (c == last) luaL_addstring(buffer, marker);
	            return 0;
	        } else {
	            luaL_addstring(buffer, marker);
	            return c;
	        }
	    } else {
	        luaL_putchar(buffer, (char) c);
	        return 0;
	    }
	}
	
	public static int mime_global_eol(lua_State L)
	{
	    int ctx = luaL_checkint(L, 1);
	    int isize = 0;
	    RefObject<Integer> isizeP = new RefObject<Integer>(isize);
	    CharPtr input = luaL_optlstring(L, 2, (CharPtr)null, isizeP);
	    isize = isizeP.argvalue;
	    CharPtr marker = luaL_optstring(L, 3, CRLF);
	    luaL_Buffer buffer = new luaL_Buffer();
	    luaL_buffinit(L, buffer);
	    /* end of input blackhole */
	    if (input == null) {
	       lua_pushnil(L);
	       lua_pushnumber(L, 0);
	       return 2;
	    }
	    CharPtr last = CharPtr.OpAddition(input, isize);
	    /* process all input */
	    while (CharPtr.OpLessThan(input, last))
	    {
	        ctx = eolprocess(input.getItem(), ctx, marker, buffer);
	        input.inc();
	    }
	    luaL_pushresult(buffer);
	    lua_pushnumber(L, ctx);
	    return 2;
	}
	
	public static int dot(int c, int state, luaL_Buffer buffer)
	{
	    luaL_putchar(buffer, (char) c);
	    switch (c) {
	        case '\r': 
	            return 1;
	        case '\n': 
	            return (state == 1)? 2: 0; 
	        case '.':  
	            if (state == 2) 
	                luaL_putchar(buffer, '.');
	        default:
	            return 0;
	    }
	}
	
	public static int mime_global_dot(lua_State L)
	{
	    int isize = 0, state = (int) luaL_checknumber(L, 1);
	    RefObject<Integer> isizeP = new RefObject<Integer>(isize);
	    CharPtr input = luaL_optlstring(L, 2, (CharPtr)null, isizeP);
	    isize = isizeP.argvalue;
	    luaL_Buffer buffer = new luaL_Buffer();
	    /* end-of-input blackhole */
	    if (input == null) {
	        lua_pushnil(L);
	        lua_pushnumber(L, 2);
	        return 2;
	    }
	    CharPtr last = CharPtr.OpAddition(input, isize);
	    /* process all input */
	    luaL_buffinit(L, buffer);
	    while (CharPtr.OpLessThan(input, last)) 
	    {
	        state = dot(input.getItem(), state, buffer);
	        input.inc();
	    }
	    luaL_pushresult(buffer);
	    lua_pushnumber(L, state);
	    return 2;
	}
	
	public final static int SO_REUSEADDR = 0;
	public final static int TCP_NODELAY = 1;
	public final static int SO_KEEPALIVE = 2;
	public final static int SO_DONTROUTE = 3;
	public final static int SO_BROADCAST = 4;
	public final static int IP_MULTICAST_LOOP = 5;
	public final static int SO_LINGER = 6;
	public final static int IP_ADD_MEMBERSHIP = 7;
	public final static int IP_DROP_MEMBERSHIP = 8;
	
	
	public static int opt_meth_setoption(lua_State L, luaL_pOptReg[] opt2, pSocket ps)
	{
	    CharPtr name = luaL_checkstring(L, 2);      /* obj, name, ... */
	    int i = 0;
	    luaL_pOptReg opt = opt2[i];
	    while (opt.name != null && strcmp(name, opt.name) != 0)
	    {
	        i++;
	        opt = opt2[i];
	    }
	    if (opt.func == null) {
	        luaL_argerror(L, 2, new CharPtr("unsupported option " + name));
	    }
	    return (Integer) opt.func.invoke(L, ps);
	}
	
	/* enables reuse of local address */
	public static int opt_reuseaddr(lua_State L, pSocket ps)
	{
	    return opt_setboolean(L, ps, SO_REUSEADDR);
	}
	
	/* disables the Naggle algorithm */
	public static int opt_tcp_nodelay(lua_State L, pSocket ps)
	{
	    return opt_setboolean(L, ps, TCP_NODELAY); 
	}

	public static int opt_keepalive(lua_State L, pSocket ps)
	{
	    return opt_setboolean(L, ps, SO_KEEPALIVE); 
	}

	public static int opt_dontroute(lua_State L, pSocket ps)
	{
	    return opt_setboolean(L, ps, SO_DONTROUTE);
	}

	public static int opt_broadcast(lua_State L, pSocket ps)
	{
	    return opt_setboolean(L, ps, SO_BROADCAST);
	}

	public static int opt_ip_multicast_loop(lua_State L, pSocket ps)
	{
	    return opt_setboolean(L, ps, IP_MULTICAST_LOOP);
	}
	
	public static class linger {
        int l_onoff;                /* option on/off */
        int l_linger;               /* linger time */
	};
	
	public static int opt_linger(lua_State L, pSocket ps)
	{
	    linger li = new linger();                      /* obj, name, table */
	    if (!lua_istable(L, 3)) luaL_typerror(L, 3, lua_typename(L, LUA_TTABLE));
	    lua_pushstring(L, "on");
	    lua_gettable(L, 3);
	    if (!lua_isboolean(L, -1)) 
	        luaL_argerror(L, 3, "boolean 'on' field expected");
	    li.l_onoff = (int) lua_toboolean(L, -1);
	    lua_pushstring(L, "timeout");
	    lua_gettable(L, 3);
	    if (lua_isnumber(L, -1) == 0) 
	        luaL_argerror(L, 3, "number 'timeout' field expected");
	    li.l_linger = (int) lua_tonumber(L, -1);
	    return opt_set(L, ps, SO_LINGER, li);
	}
	
	public static int opt_ip_multicast_ttl(lua_State L, pSocket ps)
	{
	    int val = (int) luaL_checknumber(L, 3);    /* obj, name, int */
	    return opt_set(L, ps, SO_LINGER, new CharPtr(val));
	}

	public static int opt_ip_add_membership(lua_State L, pSocket ps)
	{
	    return opt_setmembership(L, ps, IP_ADD_MEMBERSHIP);
	}

	public static int opt_ip_drop_membersip(lua_State L, pSocket ps)
	{
	    return opt_setmembership(L, ps, IP_DROP_MEMBERSHIP);
	}
	
	public static class ip_mreq {
        public InetAddress imr_multiaddr;  /* IP multicast address of group */
        public InetAddress imr_interface;  /* local IP address of interface */
	};
	public static int opt_setmembership(lua_State L, pSocket ps, int name)
	{
	    ip_mreq val = new ip_mreq();                   /* obj, name, table */
	    if (!lua_istable(L, 3)) luaL_typerror(L, 3, lua_typename(L, LUA_TTABLE));
	    lua_pushstring(L, "multiaddr");
	    lua_gettable(L, 3);
	    if (lua_isstring(L, -1) == 0) 
	        luaL_argerror(L, 3, "string 'multiaddr' field expected");
	    val.imr_multiaddr = null;
	    try
		{
			val.imr_multiaddr = InetAddress.getByName(lua_tostring(L, -1).toString());
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
	    if (val.imr_multiaddr == null) 
	        luaL_argerror(L, 3, "invalid 'multiaddr' ip address");
	    lua_pushstring(L, "interface");
	    lua_gettable(L, 3);
	    if (lua_isstring(L, -1) == 0) 
	        luaL_argerror(L, 3, "string 'interface' field expected");
	    //val.imr_interface.s_addr = htonl(INADDR_ANY);
	    val.imr_interface = null;
	    try
		{
			val.imr_interface = InetAddress.getByName("0.0.0.0");
		}
		catch(UnknownHostException e1)
		{
			e1.printStackTrace();
		}
	    try
		{
			val.imr_interface = InetAddress.getByName(lua_tostring(L, -1).toString());
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
	    if (strcmp(lua_tostring(L, -1), "*") != 0 &&
	    		val.imr_interface == null) 
	        luaL_argerror(L, 3, "invalid 'interface' ip address");
	    return opt_set(L, ps, name, val);
	}
	
	public static 
	int opt_set(lua_State L, pSocket ps, int name, Object val)
	{
		switch(name)
		{
			case SO_REUSEADDR:
				if(ps.GetTcpSocket() != null)
				{
					try
					{
						ps.GetTcpSocket().setReuseAddress(Boolean.valueOf(val.toString()));
					}
					catch(SocketException e)
					{
						e.printStackTrace();
					}
				}
				break;
			case TCP_NODELAY:
				if(ps.GetTcpSocket() != null)
				{
					try
					{
						ps.GetTcpSocket().setTcpNoDelay(Boolean.valueOf(val.toString()));
					}
					catch(SocketException e)
					{
						e.printStackTrace();
					}
				}
				break;
			case SO_KEEPALIVE:
				if(ps.GetTcpSocket() != null)
				{
					try
					{
						ps.GetTcpSocket().setKeepAlive(Boolean.valueOf(val.toString()));
					}
					catch(SocketException e)
					{
						e.printStackTrace();
					}
				}
				break;
			case SO_DONTROUTE:
				
				break;
			case SO_BROADCAST:
				if(ps.GetUdpSocket() != null)
				{
					try
					{
						ps.GetUdpSocket().setBroadcast(Boolean.valueOf(val.toString()));
					}
					catch(SocketException e)
					{
						e.printStackTrace();
					}
				}
				break;
			case IP_MULTICAST_LOOP:
				/*if(ps.GetUdpSocket() != null)
				{
					if(MulticastSocket.class.isInstance(ps.GetUdpSocket()))
						((MulticastSocket)ps.GetUdpSocket()).(Boolean.valueOf(val.toString()));
				}*/
				break;
			case SO_LINGER:
				if(ps.GetTcpSocket() != null)
				{
					linger l = (linger)val;
					try
					{
						ps.GetTcpSocket().setSoLinger(Boolean.valueOf(String.valueOf(l.l_onoff)), l.l_linger);
					}
					catch(SocketException e)
					{
						e.printStackTrace();
					}
				}
				break;
			case IP_ADD_MEMBERSHIP:
				if(ps.GetUdpSocket() != null)
				{
					if(MulticastSocket.class.isInstance(ps.GetUdpSocket()))
					{
						/*MulticastSocket ms = ((MulticastSocket)ps.GetUdpSocket());
						ms.joinGroup(new InetSocketAddress(address, port) , netInterface)
						.(Boolean.valueOf(val.toString()));*/
					}
				}
				break;
			case IP_DROP_MEMBERSHIP:
				break;
		}
	    /*if (setsockopt(*ps, level, name, (char *) val, len) < 0) {
	        lua_pushnil(L);
	        lua_pushstring(L, "setsockopt failed");
	        return 2;
	    }*/
	    lua_pushnumber(L, 1);
	    return 1;
	}

	public static int opt_setboolean(lua_State L, pSocket ps, int name)
	{
	    int val = auxiliar_checkboolean(L, 3);             /* obj, name, bool */
	    return opt_set(L, ps, name, val);
	}
	
	public static luaL_Reg[] select = {
		new luaL_Reg("select", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return global_select((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null)
	};
	
	public static int select_open(lua_State L) {
	    luaI_openlib(L, null, select, 0);
	    return 0;
	}
	
	public static int global_select(lua_State L) {
	    /*int rtab, wtab, itab, ret, ndirty;
	    pSocket max_fd;
	    fd_set rset, wset;
	    t_timeout tm;
	    double t = luaL_optnumber(L, 3, -1);
	    FD_ZERO(&rset); FD_ZERO(&wset);
	    lua_settop(L, 3);
	    lua_newtable(L); itab = lua_gettop(L);
	    lua_newtable(L); rtab = lua_gettop(L);
	    lua_newtable(L); wtab = lua_gettop(L);
	    max_fd = collect_fd(L, 1, SOCKET_INVALID, itab, &rset);
	    ndirty = check_dirty(L, 1, rtab, &rset);
	    t = ndirty > 0? 0.0: t;
	    timeout_init(&tm, t, -1);
	    timeout_markstart(&tm);
	    max_fd = collect_fd(L, 2, max_fd, itab, &wset);
	    ret = socket_select(max_fd+1, &rset, &wset, NULL, &tm);
	    if (ret > 0 || ndirty > 0) {
	        return_fd(L, &rset, max_fd+1, itab, rtab, ndirty);
	        return_fd(L, &wset, max_fd+1, itab, wtab, 0);
	        make_assoc(L, rtab);
	        make_assoc(L, wtab);
	        return 2;
	    } else if (ret == 0) {
	        lua_pushstring(L, "timeout");
	        return 3;
	    } else {*/
	        lua_pushstring(L, "error");
	        return 3;
	    //}
	}
	
	public static luaL_Reg[] tcpCreateFunc = {
		new luaL_Reg("select", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return global_select((lua_State)arg);
				}
			}),
	    new luaL_Reg("__gc",        new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_close((lua_State)arg);
				}
			}),
	    new luaL_Reg("__tostring",  new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return auxiliar_tostring((lua_State)arg);
				}
			}),
	    new luaL_Reg("accept",      new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_accept((lua_State)arg);
				}
			}),
	    new luaL_Reg("bind",        new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_bind((lua_State)arg);
				}
			}),
	    new luaL_Reg("close",       new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_close((lua_State)arg);
				}
			}),
	    new luaL_Reg("connect",     new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_connect((lua_State)arg);
				}
			}),
	    new luaL_Reg("dirty",       new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_dirty((lua_State)arg);
				}
			}),
	    new luaL_Reg("getfd",       new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_getfd((lua_State)arg);
				}
			}),
	    new luaL_Reg("getpeername", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_getpeername((lua_State)arg);
				}
			}),
	    new luaL_Reg("getsockname", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_getsockname((lua_State)arg);
				}
			}),
	    new luaL_Reg("getstats",    new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_getstats((lua_State)arg);
				}
			}),
	    new luaL_Reg("setstats",    new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_setstats((lua_State)arg);
				}
			}),
	    new luaL_Reg("listen",      new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_listen((lua_State)arg);
				}
			}),
	    new luaL_Reg("receive",     new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_receive((lua_State)arg);
				}
			}),
	    new luaL_Reg("send",        new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_send((lua_State)arg);
				}
			}),
	    new luaL_Reg("setfd",       new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_setfd((lua_State)arg);
				}
			}),
	    new luaL_Reg("setoption",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_setoption((lua_State)arg);
				}
			}),
	    new luaL_Reg("setpeername", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_connect((lua_State)arg);
				}
			}),
	    new luaL_Reg("setsockname", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_bind((lua_State)arg);
				}
			}),
	    new luaL_Reg("settimeout",  new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_settimeout((lua_State)arg);
				}
			}),
	    new luaL_Reg("shutdown",    new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return meth_shutdown((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null)
	};
	
	public static Delegator pOpt_function = new Delegator(new Class[] {
			lua_State.class, pSocket.class },
			Integer.TYPE);
	
	public static class luaL_pOptReg {

		private CharPtr name;
		private IDelegate func;
		private String funcName;

		public luaL_pOptReg(String strName, String javaFunction) {
			if (strName != null) {
				name = new CharPtr(strName);
				func = pOpt_function.build(Lua.class, javaFunction);
			} else
				name = null;
			funcName = javaFunction;
		}
		
		public luaL_pOptReg(String strName, TwoDelegate javaFunction) {
			if (strName != null) {
				name = new CharPtr(strName);
				func = javaFunction;
			} else
				name = null;
			funcName = strName;
		}

		public final String GetFunctionName() {
			return this.funcName;
		}

		public final IDelegate GetJavaFunction() {
			return this.func;
		}
	}
	public static luaL_pOptReg[] opt = {
		new luaL_pOptReg("keepalive", new TwoDelegate()
		{
			@Override
			public Object invoke(Object arg1, Object arg2)
			{
				return opt_keepalive((lua_State)arg1, (pSocket)arg2);
			}
		}),
		new luaL_pOptReg("reuseaddr", new TwoDelegate()
		{
			@Override
			public Object invoke(Object arg1, Object arg2)
			{
				return opt_reuseaddr((lua_State)arg1, (pSocket)arg2);
			}
		}),
		new luaL_pOptReg("tcp-nodelay", new TwoDelegate()
		{
			@Override
			public Object invoke(Object arg1, Object arg2)
			{
				return opt_tcp_nodelay((lua_State)arg1, (pSocket)arg2);
			}
		}),
		new luaL_pOptReg("linger", new TwoDelegate()
		{
			@Override
			public Object invoke(Object arg1, Object arg2)
			{
				return opt_linger((lua_State)arg1, (pSocket)arg2);
			}
		}),
		new luaL_pOptReg((String)null, (String)null)
	};
	
	public static luaL_Reg[] tcpCreate = {
		new luaL_Reg("tcp", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return global_create((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null)
	};
	
	public static int tcp_open(lua_State L)
	{
	    /* create classes */
	    auxiliar_newclass(L, new CharPtr("tcp{master}"), tcpCreateFunc);
	    auxiliar_newclass(L, new CharPtr("tcp{client}"), tcpCreateFunc);
	    auxiliar_newclass(L, new CharPtr("tcp{server}"), tcpCreateFunc);
	    /* create class groups */
	    auxiliar_add2group(L, new CharPtr("tcp{master}"), new CharPtr("tcp{any}"));
	    auxiliar_add2group(L, new CharPtr("tcp{client}"), new CharPtr("tcp{any}"));
	    auxiliar_add2group(L, new CharPtr("tcp{server}"), new CharPtr("tcp{any}"));
	    /* define library functions */
	    luaI_openlib(L, null, tcpCreate, 0); 
	    return 0;
	}
	
	public static int meth_send(lua_State L) {
	    pTcp tcp = (pTcp) auxiliar_checkclass(L, new CharPtr("tcp{client}"), 1);
	    return buffer_meth_send(L, tcp.buf);
	}

	public static int meth_receive(lua_State L) {
		pTcp tcp = (pTcp) auxiliar_checkclass(L, new CharPtr("tcp{client}"), 1);
	    return buffer_meth_receive(L, tcp.buf);
	}

	public static int meth_getstats(lua_State L) {
		pTcp tcp = (pTcp) auxiliar_checkclass(L, new CharPtr("tcp{client}"), 1);
	    return buffer_meth_getstats(L, tcp.buf);
	}

	public static int meth_setstats(lua_State L) {
		pTcp tcp = (pTcp) auxiliar_checkclass(L, new CharPtr("tcp{client}"), 1);
	    return buffer_meth_setstats(L, tcp.buf);
	}
	
	public static int meth_setoption(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
	    return opt_meth_setoption(L, opt, tcp.sock);
	}
	
	public static int meth_getfd(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
		
	    Lunar.push(L, tcp.sock, false);
	    return 1;
	}

	/* this is very dangerous, but can be handy for those that are brave enough */
	public static int meth_setfd(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
	    tcp.sock = (pSocket) Lua.lua_touserdata(L, 2);
	    return 0;
	}

	public static int meth_dirty(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
	    lua_pushboolean(L, !buffer_isempty(tcp.buf));
	    return 1;
	}
	
	public static int meth_accept(lua_State L)
	{
		pTcp server = (pTcp) auxiliar_checkclass(L,  new CharPtr("tcp{server}"), 1);
	    pTimeout tm = timeout_markstart(server.tm);
	    pSocket sock = new pSocket(pSocket.SOCKET_TYPE_LISTENER);
	    RefObject<pSocket> sockP = new RefObject<pSocket>(sock);
	    int err = server.sock.Accept(sockP, null, null, tm);
	    sock = sockP.argvalue;
	    /* if successful, push client socket */
	    if (err == pIO.IO_DONE) {
	        pTcp clnt = (pTcp) lua_newuserdata(L, pTcp.class);
	        auxiliar_setclass(L, new CharPtr("tcp{client}"), -1);
	        /* initialize structure fields */
	        
	        sock.SetNonBlocking(false);
	        clnt.sock = sock;
	        io_init(clnt.io, JSocket.socket_send, JSocket.socket_recv, 
	        		JSocket.socket_ioerror, clnt.sock);
	        timeout_init(clnt.tm, -1, -1);
	        buffer_init(clnt.buf, clnt.io, clnt.tm);
	        return 1;
	    } else {
	        lua_pushnil(L); 
	        lua_pushstring(L, "error socket accept tcp");
	        return 2;
	    }
	}
	
	public static int meth_bind(lua_State L)
	{
	    pTcp tcp = (pTcp) auxiliar_checkclass(L, new CharPtr("tcp{master}"), 1);
	    CharPtr address =  luaL_checkstring(L, 2);
	    int port = (int) luaL_checknumber(L, 3);
	    CharPtr err = tcp.sock.Bind(address, port);
	    if (err != null) {
	        lua_pushnil(L);
	        lua_pushstring(L, err);
	        return 2;
	    }
	    lua_pushnumber(L, 1);
	    return 1;
	}
	
	public static int meth_connect(lua_State L)
	{
	    pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
	    CharPtr address =  luaL_checkstring(L, 2);
	    int port = (int) luaL_checknumber(L, 3);
	    pTimeout tm = timeout_markstart(tcp.tm);
	    CharPtr err = tcp.sock.Connect(address, port, tm);
	    /* have to set the class even if it failed due to non-blocking connects */
	    auxiliar_setclass(L, new CharPtr("tcp{client}"), 1);
	    if (err != null) {
	        lua_pushnil(L);
	        lua_pushstring(L, err);
	        return 2;
	    }
	    /* turn master object into a client object */
	    lua_pushnumber(L, 1);
	    return 1;
	}
	
	public static int meth_close(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
	    tcp.sock.Destroy();
	    lua_pushnumber(L, 1);
	    return 1;
	}
	
	public static int meth_listen(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkclass(L, new CharPtr("tcp{master}"), 1);
	    int backlog = (int) luaL_optnumber(L, 2, 32);
	    int err = tcp.sock.Listen(backlog);
	    if (err != pIO.IO_DONE) {
	        lua_pushnil(L);
	        lua_pushstring(L, new CharPtr("Cannot listen"));
	        return 2;
	    }
	    /* turn master object into a server object */
	    auxiliar_setclass(L, new CharPtr("tcp{server}"), 1);
	    lua_pushnumber(L, 1);
	    return 1;
	}
	
	public static int meth_shutdown(lua_State L)
	{
	    pTcp tcp = (pTcp) auxiliar_checkclass(L, new CharPtr("tcp{client}"), 1);
	    CharPtr how = luaL_optstring(L, 2, new CharPtr("both"));
	    switch (how.getItem(0)) {
	        case 'b':
	            if (strcmp(how, new CharPtr("both")) != 0)
	            {
	            	luaL_argerror(L, 2, "invalid shutdown method");
	        	    return 0;
	            }
	            tcp.sock.Shutdown(2);
	            break;
	        case 's':
	            if (strcmp(how, new CharPtr("send")) != 0)
	            {
	            	luaL_argerror(L, 2, "invalid shutdown method");
	        	    return 0;
	            }
	            tcp.sock.Shutdown(1);
	            break;
	        case 'r':
	            if (strcmp(how, new CharPtr("receive")) != 0)
	            {
	            	luaL_argerror(L, 2, "invalid shutdown method");
	        	    return 0;
	            }
	            tcp.sock.Shutdown(0);
	            break;
	    }
	    lua_pushnumber(L, 1);
	    return 1;	    
	}
	
	public static int meth_getpeername(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
		RefObject<Integer> portP = new RefObject<Integer>(0);
	    CharPtr peerName = tcp.sock.GetPeerName(portP);
	    if (peerName == null) {
	        lua_pushnil(L);
	        lua_pushstring(L, new CharPtr("getpeername failed"));
	    } else {
	        lua_pushstring(L, peerName);
	        lua_pushnumber(L, portP.argvalue);
	    }
	    return 2;
	}

	public static int meth_getsockname(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
		RefObject<Integer> portP = new RefObject<Integer>(0);
	    CharPtr sockName = tcp.sock.GetSockName(portP);
	    if (sockName == null) {
	        lua_pushnil(L);
	        lua_pushstring(L, new CharPtr("getsockname failed"));
	    } else {
	        lua_pushstring(L, sockName);
	        lua_pushnumber(L, portP.argvalue);
	    }
	    return 2;
	}
	
	public static int meth_settimeout(lua_State L)
	{
		pTcp tcp = (pTcp) auxiliar_checkgroup(L, new CharPtr("tcp{any}"), 1);
	    return timeout_meth_settimeout(L, tcp);
	}
	
	public static int global_create(lua_State L)
	{
		pSocket sock;
		sock = new pSocket(pSocket.SOCKET_TYPE_TCP);
	    /* try to allocate a system socket */
	    if (sock != null) { 
	        /* allocate tcp object */
	        pTcp tcp = (pTcp) lua_newuserdata(L, pTcp.class);
	        /* set its type as master object */
	        auxiliar_setclass(L, new CharPtr("tcp{master}"), -1);
	        /* initialize remaining structure fields */
	        sock.SetNonBlocking(false);
	        tcp.sock = sock;
	        io_init(tcp.io, JSocket.socket_send, JSocket.socket_recv, 
	                JSocket.socket_ioerror, tcp.sock);
	        timeout_init(tcp.tm, -1, -1);
	        buffer_init(tcp.buf, tcp.io, tcp.tm);
	        return 1;
	    } else {
	        lua_pushnil(L);
	        lua_pushstring(L, "Cannot create socket");
	        return 2;
	    }
	}
	
	public static luaL_Reg[] timeout = {
		new luaL_Reg("gettime",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return timeout_lua_gettime((lua_State)arg);
				}
			}),
		new luaL_Reg("sleep",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return timeout_lua_sleep((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null)
	};
	
	public static void timeout_init(pTimeout tm, double block, double total) {
	    tm.block = block;
	    tm.total = total;
	}
	
	public static double timeout_get(pTimeout tm) {
	    if (tm.block < 0.0 && tm.total < 0.0) {
	        return -1;
	    } else if (tm.block < 0.0) {
	        double t = tm.total - timeout_gettime() + tm.start;
	        return Math.max(t, 0.0);
	    } else if (tm.total < 0.0) {
	        return tm.block;
	    } else {
	        double t = tm.total - timeout_gettime() + tm.start;
	        return Math.min(tm.block, Math.max(t, 0.0));
	    }
	}
	
	public static double timeout_getstart(pTimeout tm) {
	    return tm.start;
	}
	
	public static double timeout_getretry(pTimeout tm) {
	    if (tm.block < 0.0 && tm.total < 0.0) {
	        return -1;
	    } else if (tm.block < 0.0) {
	        double t = tm.total - timeout_gettime() + tm.start;
	        return Math.max(t, 0.0);
	    } else if (tm.total < 0.0) {
	        double t = tm.block - timeout_gettime() + tm.start;
	        return Math.max(t, 0.0);
	    } else {
	        double t = tm.total - timeout_gettime() + tm.start;
	        return Math.min(tm.block, Math.max(t, 0.0));
	    }
	}
	
	public static pTimeout timeout_markstart(pTimeout tm) {
	    tm.start = timeout_gettime();
	    return tm;
	}
	
	public static double timeout_gettime() {
		return System.currentTimeMillis();
	}
	
	public static int timeout_open(lua_State L) {
	    luaI_openlib(L, null, timeout, 0);
	    return 0;
	}
	
	public static int timeout_meth_settimeout(lua_State L, pTcp tcp) {
	    double t = luaL_optnumber(L, 2, -1);
	    CharPtr mode = luaL_optstring(L, 3, "b");
	    pTimeout tm = tcp.tm;
	    if(tcp.sock != null)
	    	tcp.sock.SetTimeout((int)t);
	    switch (mode.getItem()) {
	        case 'b':
	            tm.block = t; 
	            break;
	        case 'r': case 't':
	            tm.total = t;
	            break;
	        default:
	            luaL_argcheck(L, false, 3, "invalid timeout mode");
	            break;
	    }
	    lua_pushnumber(L, 1);
	    return 1;
	}
	
	public static int timeout_lua_gettime(lua_State L)
	{
	    lua_pushnumber(L, timeout_gettime());
	    return 1;
	}
	
	public static int timeout_lua_sleep(lua_State L)
	{
	    double n = luaL_checknumber(L, 1);
	    try
		{
			Thread.sleep((long) n);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		return 0; 
	}
	
	public static luaL_Reg inet[] = {
		new luaL_Reg("toip", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return inet_global_toip((lua_State)arg);
				}
			}),
		new luaL_Reg("tohostname", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return inet_global_tohostname((lua_State)arg);
				}
			}),
		new luaL_Reg("gethostname", new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return inet_global_gethostname((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null)
	};
	
	public static int inet_open(lua_State L)
	{
	    lua_pushstring(L, "dns");
	    lua_newtable(L);
	    luaI_openlib(L, null, inet, 0);
	    lua_settable(L, -3);
	    return 0;
	}
	
	public static int inet_global_tohostname(lua_State L) {
	    CharPtr address = luaL_checkstring(L, 1);
	    InetAddress addr = null;
		try
		{
			addr = InetAddress.getByName(address.toString());
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
	    if (addr == null) {
	        lua_pushnil(L);
	        lua_pushstring(L, "cannot get hostname");
	        return 2;
	    }
	    lua_pushstring(L, addr.getHostName());
	    inet_pushresolved(L, addr);
	    return 2;
	}
	
	public static int inet_global_toip(lua_State L)
	{
	    CharPtr address = luaL_checkstring(L, 1);
	    InetAddress addr = null;
		try
		{
			addr = InetAddress.getByName(address.toString());
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
	    if (addr == null) {
	        lua_pushnil(L);
	        lua_pushstring(L, "cannot get ip");
	        return 2;
	    }
	    lua_pushstring(L, addr.getHostAddress());
	    inet_pushresolved(L, addr);
	    return 2;
	}
	
	public static int inet_global_gethostname(lua_State L)
	{
		String name = null;
		try
		{
			name = InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
	    if (name == null) {
	        lua_pushnil(L);
	        lua_pushstring(L, "gethostname failed");
	        return 2;
	    } else {
	        lua_pushstring(L, name);
	        return 1;
	    }
	}
	
	public static void inet_pushresolved(lua_State L, InetAddress addr)
	{
	    int i, resolved;
	    lua_newtable(L); resolved = lua_gettop(L);
	    lua_pushstring(L, "name");
	    lua_pushstring(L, addr.getHostName());
	    lua_settable(L, resolved);
	    lua_pushstring(L, "ip");
	    lua_pushstring(L, "alias");
	    i = 1;
	    InetAddress[] alias = null;
		try
		{
			alias = InetAddress.getAllByName(addr.getHostName());
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
		}
	    lua_newtable(L);
	    if (alias != null) 
	    {
	        for(InetAddress aliasaddr : alias)
	        {
	            lua_pushnumber(L, i);
	            lua_pushstring(L, aliasaddr.getHostName());
	            lua_settable(L, -3);
	        }
	    }
	    lua_settable(L, resolved);
	    i = 1;
	    lua_newtable(L);
	    if (alias != null) 
	    {
	    	for(InetAddress aliasaddr : alias)
	        {
	            lua_pushnumber(L, i);
	            lua_pushstring(L, aliasaddr.getHostAddress());
	            lua_settable(L, -3);
	        }
	    }
	    lua_settable(L, resolved);
	}
	
	public static luaL_Reg socketmod[] = {
		new luaL_Reg("auxiliar",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return auxiliar_open((lua_State)arg);
				}
			}),
		new luaL_Reg("except",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return except_open((lua_State)arg);
				}
			}),
		new luaL_Reg("timeout",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return timeout_open((lua_State)arg);
				}
			}),
		new luaL_Reg("buffer",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return buffer_open((lua_State)arg);
				}
			}),
		new luaL_Reg("inet",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return inet_open((lua_State)arg);
				}
			}),
		new luaL_Reg("tcp",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return tcp_open((lua_State)arg);
				}
			}),
		/*new luaL_Reg("udp",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return timeout_lua_sleep((lua_State)arg);
				}
			})"timeout_lua_sleep"),*/
		new luaL_Reg("select",   new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return select_open((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null)
	};

	public static luaL_Reg socket[] = {
		new luaL_Reg("skip",      new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return global_skip((lua_State)arg);
				}
			}),
		new luaL_Reg("__unload",  new OneDelegate()
			{	
				@Override
				public Object invoke(Object arg)
				{
					return global_unload((lua_State)arg);
				}
			}),
		new luaL_Reg((String)null, (String)null)
	};

	/*-------------------------------------------------------------------------*\
	* Skip a few arguments
	\*-------------------------------------------------------------------------*/
	public static int global_skip(lua_State L) {
	    int amount = luaL_checkint(L, 1);
	    int ret = lua_gettop(L) - amount - 1;
	    return ret >= 0 ? ret : 0;
	}

	/*-------------------------------------------------------------------------*\
	* Unloads the library
	\*-------------------------------------------------------------------------*/
	public static int global_unload(lua_State L) {
	    //socket_close();
	    return 0;
	}

	/*-------------------------------------------------------------------------*\
	* Setup basic stuff.
	\*-------------------------------------------------------------------------*/
	public static int socket_base_open(lua_State L) {
	    //if (socket_open()) {
	        /* export functions (and leave namespace table on top of stack) */
	        luaI_openlib(L, new CharPtr("socket"), socket, 0);
	        /* make version string available to scripts */
	        lua_pushstring(L, "_VERSION");
	        lua_pushstring(L, "Java Socket 1.0");
	        lua_rawset(L, -3);
	        return 1;
	    /*} else {
	        lua_pushstring(L, "unable to initialize library");
	        lua_error(L);
	        return 0;
	    }*/
	}

	/*-------------------------------------------------------------------------*\
	* Initializes all library modules.
	\*-------------------------------------------------------------------------*/
	public static int luaopen_socket_core(lua_State L) {
	    int i;
	    socket_base_open(L);
	    for (i = 0; socketmod[i].name != null; i++)
	    {
	    	socketmod[i].func.invoke(L);
	    }
	    return 1;
	}
}

