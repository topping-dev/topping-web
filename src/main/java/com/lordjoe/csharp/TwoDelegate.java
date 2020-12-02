package com.lordjoe.csharp;

public abstract class TwoDelegate implements IDelegate
{
	@Override
	public Object invoke(Object[] args)
	{
		return null;
	}

	@Override
	public  Object invoke(Object arg)
	{
		return null;
	}

	@Override
	public abstract Object invoke(Object arg1, Object arg2);

	@Override
	public Object invoke()
	{
		return null;
	}
}
