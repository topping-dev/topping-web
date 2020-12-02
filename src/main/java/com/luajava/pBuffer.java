package com.luajava;

import com.luajava.Lua.CharPtr;

public class pBuffer
{
	public static int BUF_SIZE = 8192;
	
    double birthday;        /* throttle support info: creation time, */
    long sent, received;  /* bytes sent, and bytes received */
    pIO io;                /* IO driver used for this buffer */
    pTimeout tm;           /* timeout management for this buffer */
	int first, last;     /* index of first and last bytes of stored data */
	CharPtr data;    /* storage space for buffer data */
	
	public pBuffer()
	{
		data = new CharPtr(new char[BUF_SIZE]);
	}
}
