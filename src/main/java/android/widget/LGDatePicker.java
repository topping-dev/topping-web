package android.widget;

import java.util.Calendar;
import java.util.Locale;

import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;
import com.dk.scriptingengine.osspecific.socketobjects.MethodModel;
import com.dk.scriptingengine.osspecific.socketobjects.WatchModel;

@LuaClass(className = "LGDatePicker")
public class LGDatePicker extends LGView implements LuaInterface
{
	LuaTranslator onDateChanged = null;
	int startDay = -1;
	int startMonth = -1;
	int startYear = -1;
	private String date;

	private String modelDate;
	private String modelLandscape;
	private String modelReactive;
	private String modelDateChanged;

	/**
	 * Creates LGDatePicker Object From Lua.
	 * @param lc
	 * @return LGDatePicker
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGDatePicker.class)
	public static LGDatePicker Create(LuaContext lc)
	{
		return new LGDatePicker(lc.GetContext());
	}
	
	/**
	 * Creates LGDatePicker Object From Lua.
	 * @param lc
	 * @param day
	 * @param month
	 * @param year
	 * @return LGDatePicker
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGDatePicker.class)
	public static LGDatePicker Create(LuaContext lc, int day, int month, int year)
	{
		LGDatePicker dp =  new LGDatePicker(lc.GetContext());
		dp.startDay = day;
		dp.startMonth = month;
		dp.startYear = year;
		return dp;
	}
	
	/**
	 * (Ignore)
	 */
	public LGDatePicker(Context context)
	{
		super(context);
	}
	
	/**
	 * (Ignore)
	 */
	public LGDatePicker(Context context, String luaId)
	{
		super(context, luaId);
	}

    /**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
		if(luaId != null)
		{
			modelDate = luaId + "Date";
			modelLandscape = luaId + "Landscape";
			modelReactive = luaId + "Reactive";
			modelDateChanged = luaId + "DateChanged";
			html("<v-date-picker v-model=\"" + modelDate + "\" :landscape=\"" + modelLandscape + "\" :reactive=\"" + modelReactive + "\" :picker-date:sync=\"" + modelDateChanged + "\"></v-date-picker>");
			jsModel.add(new JSModel(modelDate, ""));
			jsModel.add(new JSModel(modelLandscape, false));
			jsModel.add(new JSModel(modelReactive, false));
			jsModel.add(new JSModel(modelDateChanged, ""));

			watchModel.add(new WatchModel(luaId, "Date"));
			watchModel.add(new WatchModel(luaId, "DateChanged"));
		}
		else
			html("<v-date-picker></v-date-picker>");
    }
    
    /**
     * (Ignore)
     */
    @Override
    public void AfterSetup(Context context)
    {
    	super.AfterSetup(context);
    	if(startDay == -1)
    	{
			Calendar now = Calendar.getInstance(Locale.getDefault());
			startDay = now.get(Calendar.DAY_OF_MONTH);
			startMonth = now.get(Calendar.MONTH);
			startYear = now.get(Calendar.YEAR);
    	}
    	
    	final LGDatePicker self = this;
    	//TODO: Init date picker
    	/*((DatePicker)view).init(startYear, startMonth, startDay, new OnDateChangedListener()
		{
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			{
				if(onDateChanged != null)
					onDateChanged.CallIn(self, Integer.valueOf(dayOfMonth), Integer.valueOf(monthOfYear), Integer.valueOf(year));
			}
		});*/
    }
    
    /**
     * Gets the day value
     * @return int day
     */
    @LuaFunction(manual = false, methodName = "GetDay")
    public Integer GetDay()
    {
    	//TODO:
		return 0;
    	//return ((DatePicker)view).getDayOfMonth();
    }
    
    /**
     * Gets the month value
     * @return int month
     */
    @LuaFunction(manual = false, methodName = "GetMonth")
    public Integer GetMonth()
    {
		//TODO:
		return 0;
		//return ((DatePicker)view).getMonth();
    }
    
    /**
     * Gets the year value
     * @return int year
     */
    @LuaFunction(manual = false, methodName = "GetYear")
    public Integer GetYear()
    {
		//TODO:
		return 0;
		//return ((DatePicker)view).getYear();
    }
    
    /**
     * Update the date value of picker
     * @param day
     * @param month
     * @param year
     */
    @LuaFunction(manual = false, methodName = "GetYear", arguments = { Integer.class, Integer.class, Integer.class })
    public void UpdateDate(Integer day, Integer month, Integer year)
    {
		//TODO:
		//((DatePicker)view).updateDate(year, month, day);
    }

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     * "Changed" happens when user changes the date
     * @param var Event
     * @param lt
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
		if(var.compareTo("Changed") == 0)
		{
			onDateChanged = lt;
		}
	}

	@Override
	public void CallEventValue(String id, String event, Object... vals)
	{
		if(event.equals(SET_VALUE))
		{
			if(vals == null)
				return;
			date = (String)vals[0];
		}
		else if(event.equals("Changed"))
		{
			if(onDateChanged != null)
			{
				onDateChanged.CallIn(lc);
			}
		}
	}
}
