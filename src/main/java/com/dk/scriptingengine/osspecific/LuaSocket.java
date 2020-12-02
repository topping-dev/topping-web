package com.dk.scriptingengine.osspecific;

import java.io.IOException;
import java.net.Socket;

import com.luajava.RefObject;
import com.luajava.Tools;
import com.luajava.Lua.CharPtr;

public class LuaSocket extends Socket
{
	Socket tcpSocket;
	boolean blocking;
		
	public int Recv(final CharPtr data, final int count, final AsyncResult recvResult)
	{
		if(blocking)
		{
			byte[] arr = new byte[count];
			try
			{
				int read = tcpSocket.getInputStream().read(arr);
				data.setByteArray(arr);
				return read;
			}
			catch(Exception e)
			{
				Tools.LogException("pSocket", e);
			}
		}
		else
		{
			AsyncTask<Integer, Integer, Integer> recvTask = new AsyncTask<Integer, Integer, Integer>()
			{
				
				@Override
				protected Integer doInBackground(Integer... params)
				{
					byte[] arr = new byte[count];
					try
					{
						int read = tcpSocket.getInputStream().read(arr);
						data.setByteArray(arr);
						return read;
					}
					catch (Exception e) 
					{
					}
					return 0;
				}
				
				@Override
				protected void onPostExecute(Integer result)
				{
					recvResult.Call(data, result);
				}
			};
			recvTask.execute(0);
		}
		return 0;
	}
	
	public int Send(final CharPtr data, final int count, int flag, RefObject<Integer> errP)
	{
		if(blocking)
		{
			try
			{
				tcpSocket.getOutputStream().write(data.toByteArray(), 0, count);
				return count;
			}
			catch(Exception e)
			{
				errP.argvalue = 1;
			}
		}
		else
		{
			AsyncTask<Integer, Integer, Integer> sendTask = new AsyncTask<Integer, Integer, Integer>()
			{
				@Override
				protected Integer doInBackground(Integer... params)
				{
					try
					{
						tcpSocket.getOutputStream().write(data.toByteArray(), 0, count);
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
					return 0;
				}
			};
			sendTask.execute(0);
		}
		return 0;
	}
	
	public void SetBlocking(boolean value)
	{
		blocking = value;
	}
}
