package com.lordjoe.csharp;

public abstract class OneDelegate implements IDelegate
{
	@Override
	public Object invoke(Object[] args)
	{
		return null;
	}

	@Override
	public abstract Object invoke(Object arg);

	@Override
	public Object invoke(Object arg1, Object arg2)
	{
		return null;
	}

	@Override
	public Object invoke()
	{
		return null;
	}
}
