package com.luajava;

public class pTcp
{
	public pTcp()
	{
		io = new pIO();
		buf = new pBuffer();
		tm = new pTimeout();
	}
	
	public pSocket sock;
	public pIO io;
	public pBuffer buf;
	public pTimeout tm;
}
