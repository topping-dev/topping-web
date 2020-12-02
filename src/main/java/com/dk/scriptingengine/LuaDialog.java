package com.dk.scriptingengine;

import android.widget.LGView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaGlobalInt;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.LayoutServer;
import com.dk.scriptingengine.osspecific.LuaEventInterface;
import com.dk.scriptingengine.osspecific.Utilities;

/**
 * Lua dialog class.
 * This class is used to create dialogs and manupilate it from lua.
 * There are five types of dialogs.
 * DIALOG_TYPE_NORMAL
 * DIALOG_TYPE_PROGRESS
 * DIALOG_TYPE_PROGRESS_INDETERMINATE
 * DIALOG_TYPE_DATEPICKER
 * DIALOG_TYPE_TIMEPICKER
 */
@LuaClass(className = "LuaDialog")
@LuaGlobalInt(
		keys = 
		{
			"LuaDialog_TYPE_NORMAL",
			"LuaDialog_TYPE_PROGRESS",
			"LuaDialog_DIALOG_TYPE_PROGRESS_INDETERMINATE",
			"LuaDialog_DIALOG_TYPE_DATEPICKER",
			"LuaDialog_DIALOG_TYPE_TIMEPICKER"
		},
		vals = 
		{
			1,
			2,
			6,
			8,
			16
		})
public class LuaDialog implements LuaEventInterface
{
	private final static String DIALOG_MODEL = "dialog_model";
	private final static String DIALOG_PERSISTENT = "dialog_persistent";
	private final static String DIALOG_TITLE = "dialog_title";
	private final static String DIALOG_CONTENT = "dialog_content";
	private final static String DIALOG_POSITIVE_BUTTON = "dialog_positive_button";
	private final static String DIALOG_POSITIVE_BUTTON_VISIBLE = "dialog_positive_button_visible";
	private final static String DIALOG_POSITIVE_ACTION = "dialog_positive_action";
	private final static String DIALOG_NEGATIVE_BUTTON = "dialog_negative_button";
	private final static String DIALOG_NEGATIVE_BUTTON_VISIBLE = "dialog_negative_button_visible";
	private final static String DIALOG_NEGATIVE_ACTION = "dialog_negative_action";

	private final static String PROGRESS_DIALOG_MODEL = "progress_dialog_model";
	private final static String PROGRESS_DIALOG_PERSISTENT = "progress_dialog_persistent";
	private final static String PROGRESS_MODEL = "progress_model";
	private final static String PROGRESS_INDETERMINATE = "progress_indeterminate";
	private final static String PROGRESS_DIALOG_POSITIVE_BUTTON = "progress_dialog_positive_button";
	private final static String PROGRESS_DIALOG_POSITIVE_BUTTON_VISIBLE = "progress_dialog_positive_button_visible";
	private final static String PROGRESS_DIALOG_POSITIVE_ACTION = "progress_dialog_positive_action";
	private final static String PROGRESS_DIALOG_NEGATIVE_BUTTON = "progress_dialog_negative_button";
	private final static String PROGRESS_DIALOG_NEGATIVE_BUTTON_VISIBLE = "progress_dialog_negative_button_visible";
	private final static String PROGRESS_DIALOG_NEGATIVE_ACTION = "progress_dialog_negative_action";

	private final static String DATE_PICKER_DIALOG_MODEL = "date_picker_dialog_model";
	private final static String DATE_PICKER_DIALOG_PERSISTENT = "date_picker_dialog_persisten";
	private final static String DATE_PICKER_MODEL = "date_picker_model";
	private final static String DATE_PICKER_LANDSCAPE = "date_picker_landscape";
	private final static String DATE_PICKER_DIALOG_POSITIVE_BUTTON = "date_picker_dialog_positive_button";
	private final static String DATE_PICKER_DIALOG_POSITIVE_BUTTON_VISIBLE = "date_picker_dialog_positive_button_visible";
	private final static String DATE_PICKER_DIALOG_POSITIVE_ACTION = "date_picker_dialog_positive_action";
	private final static String DATE_PICKER_DIALOG_NEGATIVE_BUTTON = "date_picker_dialog_negative_button";
	private final static String DATE_PICKER_DIALOG_NEGATIVE_BUTTON_VISIBLE = "date_picker_dialog_negative_button_visible";
	private final static String DATE_PICKER_DIALOG_NEGATIVE_ACTION = "date_picker_dialog_negative_action";

	private final static String TIME_PICKER_DIALOG_MODEL = "time_picker_dialog_model";
	private final static String TIME_PICKER_DIALOG_PERSISTENT = "time_picker_dialog_persistent";
	private final static String TIME_PICKER_MODEL = "time_picker_model";
	private final static String TIME_PICKER_LANDSCAPE = "time_picker_landscape";
	private final static String TIME_PICKER_DIALOG_POSITIVE_BUTTON = "time_picker_dialog_positive_button";
	private final static String TIME_PICKER_DIALOG_POSITIVE_BUTTON_VISIBLE = "time_picker_dialog_positive_button_visible";
	private final static String TIME_PICKER_DIALOG_POSITIVE_ACTION = "time_picker_dialog_positive_action";
	private final static String TIME_PICKER_DIALOG_NEGATIVE_BUTTON = "time_picker_dialog_negative_button";
	private final static String TIME_PICKER_DIALOG_NEGATIVE_BUTTON_VISIBLE = "time_picker_dialog_negative_button_visible";
	private final static String TIME_PICKER_DIALOG_NEGATIVE_ACTION = "time_picker_dialog_negative_action";

	private final LuaContext context;
	private int dialogType = DIALOG_TYPE_NORMAL;
	private LuaTranslator ltDateSelected;
	private LuaTranslator ltTimeSelected;
	private LuaTranslator ltDialogPositiveAction;
	private LuaTranslator ltDialogNegativeAction;
	private final static int DIALOG_TYPE_NORMAL = 0x01;
	private final static int DIALOG_TYPE_PROGRESS = 0x02;
	private final static int DIALOG_TYPE_PROGRESS_INDETERMINATE = DIALOG_TYPE_PROGRESS | 0x04;
	private final static int DIALOG_TYPE_DATEPICKER = 0x08;
	private final static int DIALOG_TYPE_TIMEPICKER = 0x10;

	private Integer current = 0;
	private Integer max = 100;

	public LuaDialog(LuaContext context)
	{
		this.context = context;
	}

	/**
	 * Shows a messagebox
	 * @param context lua context value
	 * @param title title text
	 * @param content content text
	 */
	@LuaFunction(manual = false, methodName = "MessageBox", arguments = { LuaContext.class, String.class, String.class }, self = LuaDialog.class)
	public static void MessageBox(LuaContext context, String title, String content)
	{
		LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
				new String[]{ DIALOG_MODEL, DIALOG_TITLE, DIALOG_CONTENT, DIALOG_POSITIVE_BUTTON, DIALOG_POSITIVE_BUTTON_VISIBLE },
				new Integer[] { LayoutServer.TYPE_BOOLEAN, LayoutServer.TYPE_STRING, LayoutServer.TYPE_STRING, LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
				new Object[] { true, title, content, "Ok", true });
		LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", DIALOG_POSITIVE_ACTION));
	}
	
	/**
	 * Creates LuaDialog for build
	 * @param context
	 * @param dialogType
	 * @return LuaDialog
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class, Integer.class }, self = LuaDialog.class)
	public static LuaDialog Create(LuaContext context, int dialogType)
	{
		LuaDialog ld = new LuaDialog(context);
		ld.dialogType = dialogType;
		switch(dialogType)
		{
			case DIALOG_TYPE_NORMAL:
			{
			} break;
			case DIALOG_TYPE_PROGRESS:
			{
			} break;
			case DIALOG_TYPE_PROGRESS_INDETERMINATE:
			{
				LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
						new String[]{ PROGRESS_INDETERMINATE },
						new Integer[] { LayoutServer.TYPE_BOOLEAN },
						new Object[] { true });
			} break;
		case DIALOG_TYPE_DATEPICKER:
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar c = Calendar.getInstance(Locale.getDefault());
				LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
						new String[]{ DATE_PICKER_MODEL },
						new Integer[] { LayoutServer.TYPE_STRING },
						new Object[] { sdf.format(c.getTime()) });
			} break;
			case DIALOG_TYPE_TIMEPICKER:
			{
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				Calendar c = Calendar.getInstance(Locale.getDefault());
				LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
						new String[]{ TIME_PICKER_MODEL },
						new Integer[] { LayoutServer.TYPE_STRING },
						new Object[] { sdf.format(c.getTime()) });
			} break;
			default:
			{
				ld.dialogType = DIALOG_TYPE_NORMAL;
			} break;
		}
		return ld;
	}	
	
	/**
	 * Sets the positive button of LuaDialog
	 * @param title title of the button
	 * @param action action to do when button is pressed
	 */
	@LuaFunction(manual = false, methodName = "SetPositiveButton", arguments = { String.class, LuaTranslator.class })
	public void SetPositiveButton(String title, final LuaTranslator action)
	{
		if(dialogType == DIALOG_TYPE_NORMAL)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DIALOG_POSITIVE_BUTTON, DIALOG_POSITIVE_BUTTON_VISIBLE },
					new Integer[] { LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
					new Object[] { title, true });
			LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", DIALOG_POSITIVE_ACTION));
		}
		else if(dialogType == DIALOG_TYPE_PROGRESS
		|| dialogType == DIALOG_TYPE_PROGRESS_INDETERMINATE)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ PROGRESS_DIALOG_POSITIVE_BUTTON, PROGRESS_DIALOG_POSITIVE_BUTTON_VISIBLE },
					new Integer[] { LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
					new Object[] { title, true });
			LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", PROGRESS_DIALOG_POSITIVE_ACTION));
		}
		else if(dialogType == DIALOG_TYPE_DATEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DATE_PICKER_DIALOG_POSITIVE_BUTTON, DATE_PICKER_DIALOG_POSITIVE_BUTTON_VISIBLE },
					new Integer[] { LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
					new Object[] { title, true });
			LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", DATE_PICKER_DIALOG_POSITIVE_ACTION));
		}
		else if(dialogType == DIALOG_TYPE_TIMEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ TIME_PICKER_DIALOG_POSITIVE_BUTTON, TIME_PICKER_DIALOG_POSITIVE_BUTTON_VISIBLE },
					new Integer[] { LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
					new Object[] { title, true });
			LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", TIME_PICKER_DIALOG_POSITIVE_ACTION));
		}

		ltDialogPositiveAction = action;
	}
	
	/**
	 * Sets the negative button of LuaDialog
	 * @param title title of the button
	 * @param action action to do when button is pressed
	 */
	@LuaFunction(manual = false, methodName = "SetNegativeButton", arguments = { String.class, LuaTranslator.class })
	public void SetNegativeButton(String title, final LuaTranslator action)
	{
		if(dialogType == DIALOG_TYPE_NORMAL)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DIALOG_NEGATIVE_BUTTON, DIALOG_NEGATIVE_BUTTON_VISIBLE },
					new Integer[]{ LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
					new Object[]{ title, true });
			LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", DIALOG_NEGATIVE_ACTION));
		}
		else if(dialogType == DIALOG_TYPE_PROGRESS
				|| dialogType == DIALOG_TYPE_PROGRESS_INDETERMINATE)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ PROGRESS_DIALOG_NEGATIVE_BUTTON, PROGRESS_DIALOG_NEGATIVE_BUTTON_VISIBLE },
					new Integer[]{ LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
					new Object[]{ title, true });
			LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", PROGRESS_DIALOG_NEGATIVE_ACTION));
		}
		else if(dialogType == DIALOG_TYPE_DATEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DATE_PICKER_DIALOG_NEGATIVE_BUTTON, DATE_PICKER_DIALOG_NEGATIVE_BUTTON_VISIBLE },
					new Integer[]{ LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
					new Object[]{ title, true });
			LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", DATE_PICKER_DIALOG_NEGATIVE_ACTION));
		}
		else if(dialogType == DIALOG_TYPE_TIMEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ TIME_PICKER_DIALOG_NEGATIVE_BUTTON, TIME_PICKER_DIALOG_NEGATIVE_BUTTON_VISIBLE },
					new Integer[]{ LayoutServer.TYPE_STRING, LayoutServer.TYPE_BOOLEAN },
					new Object[]{ title, true });
			LayoutServer.getInstance().sendPacket(context.GetContext().getClient(), Utilities.MethodAction("", TIME_PICKER_DIALOG_NEGATIVE_ACTION));
		}

		ltDialogNegativeAction = action;
	}
	
	/**
	 * Sets the title of the LuaDialog
	 * @param title
	 */
	@LuaFunction(manual = false, methodName = "SetTitle", arguments = { String.class })
	public void SetTitle(String title)
	{
		LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
				new String[]{ DIALOG_TITLE },
				new Integer[] { LayoutServer.TYPE_STRING },
				new Object[] { title });
	}
	
	/**
	 * Sets the message of the LuaDialog
	 * @param message
	 */
	@LuaFunction(manual = false, methodName = "SetMessage", arguments = { String.class })
	public void SetMessage(String message)
	{
		LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
				new String[]{ DIALOG_CONTENT },
				new Integer[] { LayoutServer.TYPE_STRING },
				new Object[] { message });
	}
	
	/**
	 * Sets the value of the progress bar
	 * (progress bar is needed otherwise it wont effect anything)
	 * @param intvalue
	 */
	@LuaFunction(manual = false, methodName = "SetProgress", arguments = { Integer.class })
	public void SetProgress(Integer value)
	{
		LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
				new String[]{ PROGRESS_MODEL},
				new Integer[] { LayoutServer.TYPE_INTEGER },
				new Object[] { value * max / 100.0f });
	}
	
	/**
	 * Sets the maximum value of the progress bar
	 * (progress bar is needed otherwise it wont effect anything)
	 * @param intvalue
	 */
	@LuaFunction(manual = false, methodName = "SetMax", arguments = { Integer.class })
	public void SetMax(Integer value)
	{
		max = value;
		LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
				new String[]{ PROGRESS_MODEL},
				new Integer[] { LayoutServer.TYPE_INTEGER },
				new Object[] { value * max / 100.0f });
	}
	
	/**
	 * Sets the date of the date picker
	 * (date picker dialog is needed otherwise it wort effect anything)
	 * @param date
	 */
	@LuaFunction(manual = false, methodName = "SetDate", arguments = { LuaDate.class })
	public void SetDate(LuaDate date)
	{
		if(dialogType == DIALOG_TYPE_DATEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DATE_PICKER_MODEL },
					new Integer[] { LayoutServer.TYPE_STRING },
					new Object[] { date.ToString("yyyy-MM-dd") });
		}
	}
	
	/**
	 * Sets the date of the date picker
	 * (date picker dialog is needed otherwise it wort effect anything)
	 * @param day
	 * @param month
	 * @param year
	 */
	@LuaFunction(manual = false, methodName = "SetDateManual", arguments = { Integer.class, Integer.class, Integer.class })
	public void SetDateManual(int day, int month, int year)
	{
		if(dialogType == DIALOG_TYPE_DATEPICKER)
		{
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.MONTH, month - 1);
			c.set(Calendar.YEAR, year);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(c.getTime());
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DATE_PICKER_MODEL },
					new Integer[] { LayoutServer.TYPE_STRING },
					new Object[] { date });
		}
	}

	/**
	 * Sets the time of the time picker
	 * (time picker dialog is needed otherwise it wort effect anything)
	 * @param date
	 */
	@LuaFunction(manual = false, methodName = "SetTime", arguments = { LuaDate.class })
	public void SetTime(LuaDate date)
	{
		if(dialogType == DIALOG_TYPE_TIMEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ TIME_PICKER_MODEL },
					new Integer[] { LayoutServer.TYPE_STRING },
					new Object[] { date.ToString("HH-mm") });
		}
	}
	
	/**
	 * Sets the time of the time picker
	 * (time picker dialog is needed otherwise it wort effect anything)
	 * @param hour
	 * @param minute
	 */
	@LuaFunction(manual = false, methodName = "SetTimeManual", arguments = { Integer.class, Integer.class })
	public void SetTimeManual(int hour, int minute)
	{
		if(dialogType == DIALOG_TYPE_TIMEPICKER)
		{
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			String date = sdf.format(c.getTime());
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ TIME_PICKER_MODEL },
					new Integer[] { LayoutServer.TYPE_STRING },
					new Object[] { date });
		}
	}
	
	/**
	 * Shows the created dialog of LuaDialog
	 */
	@LuaFunction(manual = false, methodName = "Show")
	public void Show()
	{
		if(dialogType == DIALOG_TYPE_NORMAL)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DIALOG_MODEL },
					new Integer[] { LayoutServer.TYPE_BOOLEAN },
					new Object[] { true });
		}
		else if(dialogType == DIALOG_TYPE_PROGRESS)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DIALOG_MODEL },
					new Integer[] { LayoutServer.TYPE_BOOLEAN },
					new Object[] { true });
		}
		else if(dialogType == DIALOG_TYPE_DATEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DATE_PICKER_DIALOG_MODEL },
					new Integer[] { LayoutServer.TYPE_BOOLEAN },
					new Object[] { true });
		}
		else if(dialogType == DIALOG_TYPE_TIMEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ TIME_PICKER_DIALOG_MODEL },
					new Integer[] { LayoutServer.TYPE_BOOLEAN },
					new Object[] { true });
		}
		LayoutServer.getInstance().addActiveGlobal(this);
	}
	
	/**
	 * Dismiss the created dialog
	 */
	@LuaFunction(manual = false, methodName = "Dismiss")
	public void Dismiss()
	{
		if(dialogType == DIALOG_TYPE_NORMAL)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DIALOG_MODEL },
					new Integer[] { LayoutServer.TYPE_BOOLEAN },
					new Object[] { false });
		}
		else if(dialogType == DIALOG_TYPE_DATEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ DATE_PICKER_DIALOG_MODEL },
					new Integer[] { LayoutServer.TYPE_BOOLEAN },
					new Object[] { false });
		}
		else if(dialogType == DIALOG_TYPE_TIMEPICKER)
		{
			LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
					new String[]{ TIME_PICKER_DIALOG_MODEL },
					new Integer[] { LayoutServer.TYPE_BOOLEAN },
					new Object[] { false });
		}
		LayoutServer.getInstance().removeActiveGlobal(this);
	}
	
	/**
	 * Frees LuaDialog.
	 */
	@LuaFunction(manual = false, methodName = "Free", arguments = {  })
	public void Free()
	{
		//dialog = null;
	}
	
	/**
	 * (Ignore)
	 */
	@Override
	public void RegisterEventFunction(String var, LuaTranslator lt) 
	{
		if(var.compareTo("DateSelected") == 0)
		{
			if(dialogType == DIALOG_TYPE_DATEPICKER)
			    this.ltDateSelected = lt;
		}
		else if(var.compareTo("TimeSelected") == 0)
		{
			if(dialogType == DIALOG_TYPE_TIMEPICKER)
				this.ltTimeSelected = lt;
		}
	}

	/**
	 * (Ignore)
	 */
	@Override
	public String GetId() 
	{
		return "LuaDialog";
	}

	@Override
	public void CallEventFunction(String id, String event, Object... vals)
	{
		if(event.equals(LGView.SET_VALUE))
        {
            if(id.equals(DIALOG_MODEL))
            {
                if(!(boolean)vals[0])
                    LayoutServer.getInstance().removeActiveGlobal(this);
            }
            else if(id.equals(DATE_PICKER_DIALOG_MODEL))
            {
                if(!(boolean)vals[0])
                    LayoutServer.getInstance().removeActiveGlobal(this);
            }
            else if(id.equals(DATE_PICKER_MODEL))
            {
                if(ltDateSelected != null)
                    ltDateSelected.CallIn(vals);
            }
            else if(id.equals(TIME_PICKER_DIALOG_MODEL))
            {
                if(!(boolean)vals[0])
                    LayoutServer.getInstance().removeActiveGlobal(this);
            }
            else if(id.equals(TIME_PICKER_MODEL))
            {
                if(ltTimeSelected != null)
                    ltTimeSelected.CallIn(vals);
            }
        }
		else if(id.equals(DIALOG_POSITIVE_ACTION)
				|| id.equals(DATE_PICKER_DIALOG_POSITIVE_ACTION)
				|| id.equals(TIME_PICKER_DIALOG_POSITIVE_ACTION))
		{
			if(ltDialogNegativeAction != null)
				ltDialogPositiveAction.CallIn(vals);
			else
				Dismiss();
		}
		else if(id.equals(DIALOG_NEGATIVE_ACTION)
				|| id.equals(DATE_PICKER_DIALOG_NEGATIVE_ACTION)
				|| id.equals(TIME_PICKER_DIALOG_NEGATIVE_ACTION))
		{
			if(ltDialogNegativeAction != null)
				ltDialogNegativeAction.CallIn(vals);
			else
				Dismiss();
		}
	}
}
