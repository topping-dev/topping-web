package com.dk.scriptingengine.backend;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented //we want the annotation to show up in the Javadocs 
@Retention(RetentionPolicy.RUNTIME) //we want annotation metadata to be exposed at runtime
public @interface LuaGlobalNumber
{
	String[] keys();
	double[] vals();
}
