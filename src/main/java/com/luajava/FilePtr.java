package com.luajava;

import java.io.InputStream;
import java.io.OutputStream;

/*
** $Id: liolib.c,v 2.73.1.3 2008/01/18 17:47:43 roberto Exp $
** Standard I/O (and system) library
** See Copyright Notice in lua.h
*/


//FUCK NOTE: There is no Java equivalent to C# namespace aliases:
//using lua_Number = System.Double;
//FUCK NOTE: There is no Java equivalent to C# namespace aliases:
//using lua_Integer = System.Int32;

public class FilePtr
{
    public InputStream file;
    public OutputStream fileOut;
}