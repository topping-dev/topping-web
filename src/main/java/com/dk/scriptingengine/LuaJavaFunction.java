package com.dk.scriptingengine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.dk.scriptingengine.backend.LuaFunction;

public class LuaJavaFunction implements LuaFunction
{
	boolean manual = false;
	Class<?> self = Void.class;
	String methodName;
	Class<?>[] arguments;
	Method mi;
	
	public LuaJavaFunction(boolean manualP, Class<?> selfP, String methodNameP, Class<?>[] argumentsP, Method miP)
	{
		manual = manualP;
		self = selfP;
		methodName = methodNameP;
		arguments = argumentsP;
		mi = miP;
	}
	
	@Override
	public Class<? extends Annotation> annotationType() {
		return null;
	}

	@Override
	public boolean manual() {
		return manual;
	}

	@Override
	public Class<?> self() {
		return self;
	}

	@Override
	public String methodName() {
		return methodName;
	}

	@Override
	public Class<?>[] arguments() {
		return arguments;
	}

	public Method method()
	{
		return mi;
	}
}
