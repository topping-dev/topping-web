package com.luajava;

import com.lordjoe.csharp.IDelegate;

public class pIO
{
	public static final int IO_DONE = 0;        /* operation completed successfully */
	public static final int IO_TIMEOUT = -1;    /* operation timed out */
	public static final int IO_CLOSED = -2;     /* the connection has been closed */
	public static final int IO_UNKNOWN = -3;
	
	Object ctx;
	IDelegate send;
	IDelegate recv;
	IDelegate error;
}
