package com.luajava;

import java.io.BufferedWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.dk.scriptingengine.osspecific.Log;
import com.luajava.Lua.CharPtr;
import com.dk.scriptingengine.osspecific.LuaSocket;

public class pSocket
{
	Socket tcpSocket;
	SocketChannel tcpSocketChannel;
	LuaSocket tcpSocketAndroid;
	BufferedWriter tcpSocketWriter = null;
	DatagramSocket udpSocket;
	ServerSocket listener;
	
	int type = -1;
	
	private boolean cantSetNonBlocking = false;
	private boolean cantSetNonBlockingValue = true;
	private boolean isNonBlocking = false;
	
	public final static int SOCKET_TYPE_TCP = 0;
	public final static int SOCKET_TYPE_LISTENER = 1;
	public final static int SOCKET_TYPE_UDP = 2;
	public final static int SOCKET_TYPE_TCP_CHANNEL = 3;
	public final static int SOCKET_TYPE_TCP_ANDROID = 4;
	
	public pSocket(int type)
	{
		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
		java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");

		this.type = type;
		switch(type)
		{
			case SOCKET_TYPE_TCP:
				tcpSocket = new Socket();
				try
				{
					tcpSocket.setSendBufferSize(pBuffer.BUF_SIZE);
					tcpSocket.setReceiveBufferSize(pBuffer.BUF_SIZE);
				}
				catch (Exception e) 
				{
					Log.d("pSocket.java", e.getMessage());
				}
				break;
			case SOCKET_TYPE_LISTENER:
				try
				{
					listener = new ServerSocket();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			case SOCKET_TYPE_UDP:
				try
				{
					udpSocket = new MulticastSocket();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			case SOCKET_TYPE_TCP_CHANNEL:
				try
				{
					tcpSocketChannel = SocketChannel.open();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			case SOCKET_TYPE_TCP_ANDROID:
				try
				{
					tcpSocketAndroid = new LuaSocket();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
		}
	}
	
	public Socket GetTcpSocket()
	{
		if(type == SOCKET_TYPE_TCP)
			return tcpSocket;
		else if(type == SOCKET_TYPE_TCP_CHANNEL)
			return tcpSocketChannel.socket();
		
		return null;
	}
	
	public void SetTcpSocket(Socket value)
	{
		tcpSocket = value;
	}
	
	public DatagramSocket GetUdpSocket()
	{
		return udpSocket;
	}

	public int Accept(RefObject<pSocket> sockP, Object object, Object object2, pTimeout tm)
	{
		if(listener != null)
		{
			try
			{		
				sockP.argvalue.SetTcpSocket(listener.accept());
			}
			catch(Exception e)
			{
				return pIO.IO_UNKNOWN;
			}
		}
		else
			return pIO.IO_UNKNOWN;
		return pIO.IO_DONE;
	}

	public void SetNonBlocking(boolean value)
	{
		if(tcpSocketChannel != null)
		{
			try
			{
				tcpSocketChannel.socket();
				tcpSocketChannel.configureBlocking(!value);
				//isNonBlocking  = value;
			}
			catch(Exception e)
			{
				Log.e("LuaSocket", "Cannot set blocking, will try later: " + e.getMessage());
				cantSetNonBlocking = true;
				cantSetNonBlockingValue = value;
			}
		}
		else if(tcpSocketAndroid != null)
		{
			tcpSocketAndroid.SetBlocking(!value);
		}
	}
	
	public static byte[] stringToBytesASCII(String str) {
		 char[] buffer = str.toCharArray();
		 byte[] b = new byte[buffer.length];
		 for (int i = 0; i < b.length; i++) {
		  b[i] = (byte) buffer[i];
		 }
		 return b;
	}

	public static byte[] stringToBytesUTFCustom(String str) {
		 char[] buffer = str.toCharArray();
		 byte[] b = new byte[buffer.length << 1];
		 for(int i = 0; i < buffer.length; i++) {
		  int bpos = i << 1;
		  b[bpos] = (byte) ((buffer[i]&0xFF00)>>8);
		  b[bpos + 1] = (byte) (buffer[i]&0x00FF);
		 }
		 return b;
	}
	
	public static String bytesToStringUTFCustom(byte[] bytes) {
		 char[] buffer = new char[bytes.length >> 1];
		 for(int i = 0; i < buffer.length; i++) {
		  int bpos = i << 1;
		  char c = (char)(((bytes[bpos]&0x00FF)<<8) + (bytes[bpos+1]&0x00FF));
		  buffer[i] = c;
		 }
		 return new String(buffer);
	}


	public int Send(CharPtr data, int count, int flag, RefObject<Integer> errP)
	{
		if(tcpSocket != null)
		{
			try
			{
				if(tcpSocketWriter == null)
					tcpSocket.getOutputStream().write(data.toByteArray(), 0, count);
				else
					tcpSocketWriter.write(data.toCharArray(), 0, count);
				
				return count;
			}
			catch(Exception e)
			{
				errP.argvalue = 1;
			}
		}
		else if(tcpSocketChannel != null)
		{
			try
			{
				if(tcpSocketWriter == null)
				{
					byte[] arr = data.toByteArray();
					/*if(!isNonBlocking)
						tcpSocketChannel.socket().getOutputStream().write(arr);
					else*/
						tcpSocketChannel.write(ByteBuffer.wrap(arr));
				}
				else
					tcpSocketWriter.write(data.toCharArray(), 0, count);
				return count;
			}
			catch (Exception e) 
			{
				errP.argvalue = 1;
			}
		}
		else if(tcpSocketAndroid != null)
		{
			return tcpSocketAndroid.Send(data, count, flag, errP);
		}
		return 0;
	}

	public int Recv(CharPtr data, int count, int flag)
	{
		if(tcpSocket != null)
		{
			byte[] arr = new byte[count];
			try
			{
				int read = tcpSocket.getInputStream().read(arr);
				data.setByteArray(arr);
				return read;
			}
			catch(SocketTimeoutException e)
			{
				
			}
			catch(Exception e)
			{
				Tools.LogException("pSocket", e);
			}
		}
		else if(tcpSocketChannel != null)
		{
			byte[] arr = new byte[count];
			try
			{
				int read = 0;
				/*if(!isNonBlocking)
					read = tcpSocketChannel.socket().getInputStream().read(arr);
				else*/
					read = tcpSocketChannel.read(ByteBuffer.wrap(arr));
				data.setByteArray(arr);
				return read;
			}
			catch (Exception e) 
			{
				Tools.LogException("pSocket", e);
			}
		}
		else if(tcpSocketAndroid != null)
		{
			tcpSocketAndroid.Recv(data, count, null);
		}
		return 0;
	}

	public CharPtr Bind(CharPtr address, int port)
	{
		if(tcpSocket != null)
		{
			try
			{
				tcpSocket.bind(new InetSocketAddress(InetAddress.getByName(address.toString()), port));
			}
			catch(Exception e)
			{
				return address;
			}
		}
		return null;
	}

	public CharPtr Connect(CharPtr address, int port, pTimeout tm)
	{
		if(tcpSocket != null)
		{
			try
			{
				int timeout = (int) tm.block * 1000;
				if(timeout < 0)
					timeout = 0;
				tcpSocket.connect(new InetSocketAddress(InetAddress.getByName(address.toString()), port), timeout);
				tcpSocket.setSoTimeout(timeout);
				//tcpSocketWriter = new BufferedWriter(new OutputStreamWriter(tcpSocket.getOutputStream()));
				if(cantSetNonBlocking)
				{
					SetNonBlocking(cantSetNonBlockingValue);
					cantSetNonBlocking = false;
				}
			}
			catch(Exception e)
			{
				return address;
			}
		}
		else if(tcpSocketChannel != null)
		{
			try
			{
				int timeout = (int) tm.block * 1000;
				if(timeout < 0)
					timeout = 0;
				tcpSocketChannel.configureBlocking(true);
				boolean connected = tcpSocketChannel.connect(new InetSocketAddress(InetAddress.getByName(address.toString()), port));
				while(!tcpSocketChannel.finishConnect())
				{
					Thread.sleep(50);
				}
				tcpSocketChannel.socket().setSoTimeout(timeout);
				tcpSocketChannel.socket().setKeepAlive(true);
				tcpSocketChannel.socket().setTcpNoDelay(true);
				//tcpSocketWriter = new BufferedWriter(new OutputStreamWriter(tcpSocketChannel.socket().getOutputStream()));
				if(cantSetNonBlocking)
				{
					SetNonBlocking(cantSetNonBlockingValue);
					cantSetNonBlocking = false;
				}
			}
			catch (Exception e) 
			{
				return address;
			}
		}
		return null;
	}

	public void Destroy()
	{
		if(tcpSocket != null)
		{
			try
			{
				tcpSocket.close();
			}
			catch(Exception e)
			{
				Tools.LogException("pSocket", e);
			}
		}
		else if(tcpSocketChannel != null)
		{
			try
			{
				tcpSocketChannel.close();
			}
			catch (Exception e) 
			{
				Tools.LogException("pSocket", e);
			}
		}
	}

	public int Listen(int backlog)
	{
		return pIO.IO_DONE;
	}
	
	public void Shutdown(int flag)
	{
		try
		{
			if(tcpSocket != null)
			{
				switch(flag)
				{
					case 0:
						tcpSocket.shutdownInput();
						break;
					case 1:
						tcpSocket.shutdownOutput();
						break;
					case 2:
					default:
						tcpSocket.shutdownInput();
						tcpSocket.shutdownOutput();
						break;
				}
			}
			else if(tcpSocketChannel != null)
			{
				switch(flag)
				{
					case 0:
						tcpSocketChannel.socket().shutdownInput();
						break;
					case 1:
						tcpSocketChannel.socket().shutdownOutput();
						break;
					case 2:
					default:
						tcpSocketChannel.socket().shutdownInput();
						tcpSocketChannel.socket().shutdownOutput();
						break;
				}
			}
		}
		catch (Exception e) 
		{
			
		}
	}
	
	public CharPtr GetPeerName(RefObject<Integer> portP)
	{
		return new CharPtr("");
	}
	
	public CharPtr GetSockName(RefObject<Integer> portP)
	{
		return new CharPtr("");
	}
	
	public void SetTimeout(int val)
	{
		int timeout = val;
		if(timeout == 0)
			timeout = 1;
		else if(timeout < 0)
			timeout = 0;
			
		if(tcpSocket != null)
		{
			try
			{
				tcpSocket.setSoTimeout(timeout);
			}
			catch(SocketException e)
			{
				e.printStackTrace();
			}
		}
		else if(tcpSocketChannel != null)
		{
			try
			{
				tcpSocketChannel.socket().setSoTimeout(timeout);
			}
			catch(SocketException e)
			{
				e.printStackTrace();
			}
		}
	}
}
