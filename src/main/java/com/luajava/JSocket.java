package com.luajava;

import com.luajava.Lua.CharPtr;
import com.lordjoe.csharp.IDelegate;

public class JSocket
{
	public static IDelegate socket_send = new IDelegate()
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
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object[] args)
		{
			//p_socket ps, const char *data, size_t count, size_t *sent, p_timeout tm
			pSocket ps = (pSocket) args[0];
			CharPtr data = (CharPtr) args[1];
			int count = (Integer) args[2];
			RefObject<Integer> sent = (RefObject<Integer>) args[3];
			pTimeout tm = (pTimeout) args[4];
			
			int err = 0;
		    sent.argvalue = 0;
		    /* avoid making system calls on closed sockets */
		    if (ps == null) return pIO.IO_CLOSED;
		    /* loop until we send something or we give up on error */
		    for ( ;; ) {
		        /* try to send something */
		    	RefObject<Integer> errP = new RefObject<Integer>(err);
				int put = ps.Send(data, (int) count, 0, errP);
				err = errP.argvalue;
		        /* if we sent something, we are done */
		        if (put > 0) {
		            sent.argvalue = put;
		            return pIO.IO_DONE;
		        }
		        if(err > 0) return err;
		        else return 0;
		        
		        //if ((err = socket_waitfd(ps, WAITFD_W, tm)) != IO_DONE) return err;
		    } 
		    /* can't reach here */
		    //return pIO.IO_UNKNOWN;
		}
	};
	
	public static IDelegate socket_recv = new IDelegate()
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
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object[] args)
		{
			//p_socket ps, char *data, size_t count, size_t *got, p_timeout tm
			pSocket ps = (pSocket) args[0];
			CharPtr data = (CharPtr) args[1];
			int count = (Integer) args[2];
			RefObject<Integer> got = (RefObject<Integer>) args[3];
			pTimeout tm = (pTimeout) args[4];
			
			int err;
		    got.argvalue = 0;
		    if (ps == null) return pIO.IO_CLOSED;
		    for ( ;; ) {
		        int taken = ps.Recv(data, (int) count, 0);
		        if (taken > 0) {
		            got.argvalue = taken;
		            return pIO.IO_DONE;
		        }
		        if (taken == 0 || taken == -1) return pIO.IO_CLOSED;
		        //if ((err = socket_waitfd(ps, WAITFD_R, tm)) != IO_DONE) return err;
		    }
		}
	};
	
	public static IDelegate socket_ioerror = new IDelegate()
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
			return 0;
		}
		
		@Override
		public Object invoke(Object arg)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Object invoke(Object[] args)
		{
			// TODO Auto-generated method stub
			return null;
		}
	}; 
}
