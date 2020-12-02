package com.dk.scriptingengine.luagui;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.widget.LGAbsListView;
import android.widget.LGAutoCompleteTextView;
import android.widget.LGButton;
import android.widget.LGCheckBox;
import android.widget.LGComboBox;
import android.widget.LGDatePicker;
import android.widget.LGEditText;
import android.widget.LGFrameLayout;
import android.widget.LGHorizontalScrollView;
import android.widget.LGImageView;
import android.widget.LGLinearLayout;
import android.widget.LGListView;
import android.widget.LGMapView;
import android.widget.LGProgressBar;
import android.widget.LGRadioButton;
import android.widget.LGRadioGroup;
import android.widget.LGRelativeLayout;
import android.widget.LGScrollView;
import android.widget.LGTableLayout;
import android.widget.LGTableRow;
import android.widget.LGTextView;
import android.widget.LGView;
import android.widget.LGViewGroup;

import com.dk.scriptingengine.LuaColor;
import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaEngine.GuiEvents;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.Gravity;
import com.dk.scriptingengine.osspecific.InputType;
import com.dk.scriptingengine.osspecific.LGColorParser;
import com.dk.scriptingengine.osspecific.LGDimensionParser;
import com.dk.scriptingengine.osspecific.LGDrawableParser;
import com.dk.scriptingengine.osspecific.LGLayoutParser;
import com.dk.scriptingengine.osspecific.LGParser;
import com.dk.scriptingengine.osspecific.LayoutParams;
import com.dk.scriptingengine.osspecific.Log;

@LuaClass(className = "LuaViewInflator")
public class LuaViewInflator implements LuaInterface 
{
	Stack<LGView> lgStack;
	Hashtable<String, Integer> ids;
	Context context;
	LuaContext lc;
	int idg;
	
	/**
	 * (Ignore)
	 */
	public LuaViewInflator(LuaContext lc) 
	{
		this.lgStack = new Stack<LGView>();
		this.ids = new Hashtable<String, Integer>();
		this.context = lc.GetContext();
		this.lc = lc;
		this.idg = 0;
		DisplayMetrics.density = 1;
		DisplayMetrics.xdpi = 1;
		DisplayMetrics.ydpi = 1;
		DisplayMetrics.scaledDensity = 1;
	}
	
	/**
	 * (Ignore)
	 */
	public String convertStreamToString(java.io.InputStream is) 
	{
	    try {
	        return new java.util.Scanner(is).useDelimiter("\\A").next();
	    } catch (java.util.NoSuchElementException e) {
	        return "";
	    }
	}
	
	/**
	 * (Ignore)
	 */
	public static String parseColor(LuaContext lc, String val)
	{
		if(val == null)
			return LuaColor.WHITE;
		else
		{
			if(val.startsWith("#"))
				return val;
			else
			{
				int identifier = lc.GetContext().getResources().getIdentifier(val, "color", lc.GetContext().getPackageName());
				try
				{
					return lc.GetContext().getResources().getColor(identifier);
				}
				catch (Exception e) 
				{
					return LuaColor.BLACK;
				}
			}
		}
	}
	
	/**
	 * Creates LuaViewInflater Object From Lua.
	 * @param lc
	 * @return LuaViewInflator
	 */
    @LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LuaViewInflator.class)
    public static LuaViewInflator Create(LuaContext lc)
    {
    	LuaViewInflator lvi = new LuaViewInflator(lc);
    	return lvi;
    }
    
	public static int getResourceId(Context c, String pVariableName, String pResourcename, String pPackageName) 
	{
		//TODO:Return
		return 0;
	    /*try {
	        return c.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return -1;
	    } */
	}
    
    /**
     * Parses xml file
     * @param filename
     * @param parent
     * @return LGView
     */
    @LuaFunction(manual = false, methodName = "ParseFile", arguments = { String.class, LGView.class })
    public LGView ParseFile(String filename, LGView parent)
    {
    	XmlPullParser parse;		
		try {
			parse = LGParser.GetInstance().getXmlPullParserFactory().newPullParser();
			InputStream is = (InputStream) LGLayoutParser.GetInstance().GetLayout(filename).GetStreamInternal();//context.getAssets().open(LuaEngine.getInstance().GetUIRoot() + "/" + filename);
			parse.setInput(is, null);

			ArrayList<LGView> lgRoot = new ArrayList<LGView>();
			LGView v = inflate(parse, lgRoot);
			return v;
			/*if(v.getClass().isInstance(LGView.class))
				return (LGView)v;
			else
			{
				String luaId = v.GetId();
				LGLinearLayout lgll = new LGLinearLayout(context);
				LayoutmeParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				lgll.setLayoutParams(lp);
				lgll.luaId = luaId;
				//lgll.addView(v);
				//lgll.view = v;
				for(LGView w : lgRoot)
					lgll.addView(w);

				//TODO:Check this
				//LuaEngine.getInstance().OnGuiEvent(lgll, GuiEvents.GUI_EVENT_CREATE, lc);
				return lgll;
			}*/
		}
		catch (XmlPullParserException ex) { Log.e("LuaViewInflator", ex.getMessage()); }
		catch (IOException ex) {  }
		return null;
    }
    
    /**
     * Frees the native object
     */
    @LuaFunction(manual = false, methodName = "Free")
    public void Free()
    {
    	
    }
	
	/**
	 * (Ignore)
	 */
	public LGView inflate(String text) {
		XmlPullParser parse;		
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			parse = factory.newPullParser();
			parse.setInput(new StringReader(text));
			ArrayList<LGView> lgRoot = new ArrayList<LGView>();
			return inflate(parse, lgRoot);
		}
		catch (XmlPullParserException ex) { return null; }
		catch (IOException ex) { return null; }
	}
	
	/**
	 * (Ignore)
	 */
	public LGView inflate(XmlPullParser parse, ArrayList<LGView> lgroot)
			throws XmlPullParserException, IOException
			{
		ids.clear();

		Stack<StringBuffer> data = new Stack<StringBuffer>();
		int evt = parse.getEventType();
		LGView root = null;
		while (evt != XmlPullParser.END_DOCUMENT) {
			switch (evt) {
			case XmlPullParser.START_DOCUMENT:
				data.clear();
				break;
			case XmlPullParser.START_TAG:
				data.push(new StringBuffer());
				LGView v = createView(parse);
				if(v instanceof LGView)
				{
					if(lgStack.size() > 0)
					{
						LGView vParent = (LGView) lgStack.peek();
						if(vParent != null)
							vParent.addView((LGView) v);
						else
							lgroot.add((LGView) v);
					}
					else
						lgroot.add((LGView)v);

					HashMap<String, String> atts = new HashMap<>();
					for(int i = 0; i < parse.getAttributeCount(); i++)
						atts.put(parse.getAttributePrefix(i) + ":" + parse.getAttributeName(i), parse.getAttributeValue(i));

					if (lgStack.size() > 0)
					{
						LayoutParams lps = loadLayoutParams(atts, (LGView)v, (LGViewGroup) lgStack.peek());
						v.setLayoutParams(lps);
					}
					else
						v.setLayoutParams(loadLayoutParams(atts, v));
				}
				if (v == null)
				{
					evt = parse.next();
					continue;
				}					
				if (root == null) {
					root = v;
				}
				if(v instanceof LGView)
				{
					if (((LGView)v).GetView() instanceof LGViewGroup
							&& !(((LGView)v).GetView() instanceof LGAbsListView)
							&& !(((LGView)v).GetView() instanceof LGComboBox))
					{
						lgStack.add((LGView) v);
					}
				}
				else
				{
					if (v instanceof LGViewGroup && !(v instanceof LGAbsListView))
					{
					}
				}
				break;
			case XmlPullParser.TEXT:
				data.peek().append(parse.getText());
				break;
			case XmlPullParser.END_TAG:
				data.pop();
				if(isLGLayout(parse.getName()))
				{
					lgStack.peek().onLayoutCreated();
					lgStack.pop();
				}

			}
			evt = parse.next();
		}
		return root;
	}
	
	/**
	 * (Ignore)
	 */
	public static int getResId(String variableName, Context context, Class<?> c) {
		return 0;
		//TODO:
		/*String packageName = context.getPackageName();
	      int resId = context.getResources().getIdentifier(variableName, "drawable", packageName);
	      return resId;*/
	}

	
	/**
	 * (Ignore)
	 */
	protected LGView createView(XmlPullParser parse)
	{
		String name = parse.getName();
		LGView lgresult = null;
		LGView result = null;
		HashMap<String, String> atts = new HashMap<>();
		for(int i = 0; i < parse.getAttributeCount(); i++)
			atts.put(parse.getAttributePrefix(i) + ":" + parse.getAttributeName(i), parse.getAttributeValue(i));
		String luaId = findAttribute(atts, "lua:id");
		if (name.equals("LinearLayout")) {
            lgresult = new LGLinearLayout(context, luaId);
		}
		else if(name.equals("LGLinearLayout")){
			lgresult = new LGLinearLayout(context, luaId);
		}
		else if (name.equals("RadioGroup")) {
			lgresult = new LGRadioGroup(context, luaId);
		}
		else if (name.equals("LGRadioGroup")) {
			lgresult = new LGRadioGroup(context, luaId);
		}
		else if (name.equals("TableRow")) {
			lgresult = new LGTableRow(context, luaId);
		}
		else if (name.equals("LGTableRow")) {
			lgresult = new LGTableRow(context, luaId);
		}
		else if (name.equals("TableLayout")) {
			lgresult = new LGTableLayout(context, luaId);
		}
		else if (name.equals("LGTableLayout")) {
			lgresult = new LGTableLayout(context, luaId);
		}
		/*else if (name.equals("AbsoluteLayout")) {
			result = new LGAbsoluteLayout(context, luaId);
		}*/
		else if (name.equals("RelativeLayout")) {
			lgresult = new LGRelativeLayout(context, luaId);
		}
		else if (name.equals("LGRelativeLayout")) {
			lgresult = new LGRelativeLayout(context, luaId);
		}
		else if (name.equals("ScrollView")) {
			lgresult = new LGScrollView(context, luaId);
		}
		else if (name.equals("LGScrollView")) {
			lgresult = new LGScrollView(context, luaId);
		}
		else if (name.equals("FrameLayout")) {
			lgresult = new LGFrameLayout(context, luaId);
		}
		else if (name.equals("LGFrameLayout")) {
			lgresult = new LGFrameLayout(context, luaId);
		}
		else if (name.equals("TextView")) {
			lgresult = new LGTextView(context, luaId);
		}
		else if (name.equals("LGTextView")) {
			lgresult = new LGTextView(context, luaId);
		}
		else if (name.equals("AutoCompleteTextView")) {
			lgresult = new LGAutoCompleteTextView(context, luaId);
		}
		else if (name.equals("LGAutoCompleteTextView")) {
			lgresult = new LGAutoCompleteTextView(context, luaId);
		}
		/*else if (name.equals("AnalogClock")) {
			result = new LGAnalogClock(context, luaId);
		}*/
		else if (name.equals("Button")) {
			lgresult = new LGButton(context, luaId);
		}
		else if (name.equals("LGButton")) {
			lgresult = new LGButton(context, luaId);
		}
		else if (name.equals("CheckBox")) {
			lgresult = new LGCheckBox(context, luaId);
		}
		else if (name.equals("LGCheckBox")) {
			lgresult = new LGCheckBox(context, luaId);
		}
		else if (name.equals("Spinner")) {
			lgresult = new LGComboBox(context, luaId);
		}
		else if (name.equals("LGSpinner")) {
			lgresult = new LGComboBox(context, luaId);
		}
		else if (name.equals("ComboBox")) {
			lgresult = new LGComboBox(context, luaId);
		}
		else if (name.equals("LGComboBox")) {
			lgresult = new LGComboBox(context, luaId);
		}
		else if (name.equals("DatePicker")) {
			lgresult = new LGDatePicker(context, luaId);
		}
		else if (name.equals("LGDatePicker")) {
			lgresult = new LGDatePicker(context, luaId);
		}
		/*else if (name.equals("DigitalClock")) {
			result = new LGDigitalClock(context, luaId);
		}*/
		else if (name.equals("EditText")) {
			lgresult = new LGEditText(context, luaId);
		}
		else if (name.equals("LGEditText")) {
			lgresult = new LGEditText(context, luaId);
		}
		else if (name.equals("ProgressBar")) {
			lgresult = new LGProgressBar(context, luaId);
		}
		else if (name.equals("LGProgressBar")) {
			lgresult = new LGProgressBar(context, luaId);
		}
		else if (name.equals("RadioButton")) {
			lgresult = new LGRadioButton(context, luaId);
		}
		else if (name.equals("LGRadioButton")) {
			lgresult = new LGRadioButton(context, luaId);
		}
		else if (name.equals("ListView")) {
			lgresult = new LGListView(context, luaId);
		}
		else if (name.equals("LGListView")) {
			lgresult = new LGListView(context, luaId);
		}
		else if (name.equals("ImageView"))
		{
			lgresult = new LGImageView(context, luaId);
		}
		else if (name.equals("LGImageView"))
		{
			lgresult = new LGImageView(context, luaId);
		}
		else if(name.equals("LGHorizontalScrollView")
				|| name.equals("HorizontalScrollView"))
		{
			lgresult = new LGHorizontalScrollView(context, luaId);
		}
		else if (name.equals("LGView")
			|| name.equals("View"))
		{
			lgresult = new LGView(context, luaId);
		}
		else if(name.equals("LGMapView")
				|| name.equals("MapView"))
		{
			lgresult = new LGMapView(lc, luaId);
		}
		else {
			Log.e("LuaViewInflator", "Unhandled tag:"+name);
		}
		
		if (lgresult == null)
			return null;
		
		/*if(lgresult instanceof LGView)
			result = ((LGView)lgresult).GetView();
		else*/
			result = lgresult;
		
		String id = findAttribute(atts, "android:id");

		if (id != null) {
			int idNumber = lookupId(id);
			if (idNumber > -1) {
				result.attr("id", String.valueOf(idNumber));
			}
		}
		
		if (result instanceof LGEditText)
		{
            LGEditText et = (LGEditText)result;
			
			String hintTextColor = findAttribute(atts, "android:textColorHint");
			if(hintTextColor != null)
			{
				et.setHintTextColor(LGColorParser.GetInstance().ParseColor(hintTextColor));
			}
		}
		
		if (result instanceof LGTextView) {
            LGTextView tv = (LGTextView)result;
			String text = findAttribute(atts, "android:text");
			if (text != null) {
				text = text.replace("\\n", "\n");
				tv.SetText(text);
			}
			String textColor = findAttribute(atts, "android:textColor");
			if(textColor != null)
			{
			    tv.SetTextColor(LGColorParser.GetInstance().ParseColor(textColor));
			}
			else
				tv.SetTextColor(LuaColor.BLACK);
			
			String textSize = findAttribute(atts, "android:textSize");
			if(textSize != null)
			{
			    tv.setTextSize(LGDimensionParser.GetInstance().GetDimension(textSize));
			}
			
			String minLines = findAttribute(atts, "android:minLines");
			/*if(minLines != null)
				tv.setMinLines(Integer.valueOf(minLines));*/
			
			String singleLine = findAttribute(atts, "android:singleLine");
			if(singleLine != null)
				tv.setSingleLine(singleLine.compareTo("true") == 0 ? true : false);
			
			String lines = findAttribute(atts, "android:lines");
			/*if(lines != null)
				tv.setLines(Integer.valueOf(lines));*/
			
			String inputType = findAttribute(atts, "android:inputType");
			if(inputType != null)
			{
				String inputTypeOr = "";
				String[] arr = inputType.split("\\|");
				for(String it : arr)
				{
					if(it.equals("text"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textCapCharacters"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textCapWords"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textCapSentences"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textAutoCorrect"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textAutoComplete"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textMultiLine"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textImeMultiLine"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textNoSuggestions"))
						inputTypeOr = InputType.TYPE_CLASS_TEXT;
					else if(it.equals("textUri"))
						inputTypeOr = InputType.TYPE_TEXT_VARIATION_URI;
					else if(it.equals("textEmailAddress"))
						inputTypeOr = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
					else if(it.equals("textEmailSubject"))
						inputTypeOr = InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT;
					else if(it.equals("textPersonName"))
						inputTypeOr = InputType.TYPE_TEXT_VARIATION_PERSON_NAME;
					else if(it.equals("textPostalAddress"))
						inputTypeOr = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS;
					else if(it.equals("textPassword"))
						inputTypeOr = InputType.TYPE_TEXT_VARIATION_PASSWORD;
					else if(it.equals("textVisiblePassword"))
						inputTypeOr = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
					else if(it.equals("number"))
						inputTypeOr = InputType.TYPE_CLASS_NUMBER;
					else if(it.equals("numberSigned"))
						inputTypeOr = InputType.TYPE_CLASS_NUMBER;
					else if(it.equals("numberDecimal"))
						inputTypeOr = InputType.TYPE_CLASS_NUMBER;
					else if(it.equals("numberPassword"))
						inputTypeOr = InputType.TYPE_TEXT_VARIATION_PASSWORD;
					else if(it.equals("phone"))
						inputTypeOr = InputType.TYPE_CLASS_PHONE;
					else if(it.equals("datetime"))
						inputTypeOr = InputType.TYPE_CLASS_DATETIME;
					else if(it.equals("date"))
						inputTypeOr = InputType.TYPE_DATETIME_VARIATION_DATE;
					else if(it.equals("time"))
						inputTypeOr = InputType.TYPE_DATETIME_VARIATION_TIME;
				}
				
				tv.setInputType(inputTypeOr);
			}
			
			String gravity = findAttribute(atts, "android:gravity");
			if (gravity != null) {
				int gravityOr = 0;
				String[] arr = gravity.split("\\|");
				for(String g : arr)
				{
					if(g.equals("top"))
						gravityOr |= Gravity.TOP;
					else if(g.equals("bottom"))
						gravityOr |= Gravity.BOTTOM;
					else if(g.equals("left"))
						gravityOr |= Gravity.LEFT;
					else if(g.equals("right"))
						gravityOr |= Gravity.RIGHT;
					else if(g.equals("center_vertical"))
						gravityOr |= Gravity.CENTER_VERTICAL;
					else if(g.equals("fill_vertical"))
						gravityOr |= Gravity.FILL_VERTICAL;
					else if(g.equals("center_horizontal"))
						gravityOr |= Gravity.CENTER_HORIZONTAL;
					else if(g.equals("fill_horizontal"))
						gravityOr |= Gravity.FILL_HORIZONTAL;
					else if(g.equals("center"))
						gravityOr |= Gravity.CENTER;
					else if(g.equals("fill"))
						gravityOr |= Gravity.FILL;
				}
						
				tv.setGravity(gravityOr);
			}
			
			String hint = findAttribute(atts, "android:hint");
			if (hint != null) 
			{
				//tv.setHint(hint);
			}
		}
		
		if(result instanceof LGImageView)
		{
			LGImageView iv = (LGImageView)result;
			String image = findAttribute(atts, "android:src");
			if(image != null)
			{
                LGDrawableParser.DrawableReturn dr = LGDrawableParser.GetInstance().ParseDrawable(image);
				if(dr.imagePath != null)
				    iv.setImagePath(dr.imagePath);
			}
		}
		
		if (result instanceof LGCheckBox) {
            LGCheckBox cb = (LGCheckBox) result;
			String checked = findAttribute(atts, "android:checked");
			cb.SetChecked("true".equals(checked));
		}
		
		if (result instanceof LGProgressBar) {
			LGProgressBar pb = (LGProgressBar)result;
			String indet = findAttribute(atts, "android:indeterminate");
			if (indet != null) {
				pb.setIndeterminate("true".equals(indet));
			}
			String max = findAttribute(atts, "android:max");
			if (max != null) {
				pb.setMax(Integer.parseInt(max));
			}
			String style = findAttribute(atts, "null:style");
			if (style != null)
            {
                if(style.contains("Widget.ProgressBar.Horizontal"))
                    pb.setStyle(LGProgressBar.Style.Horizontal);
                else if(style.contains("Widget.ProgressBar.Small"))
                    pb.setStyle(LGProgressBar.Style.Small);
                else if(style.contains("Widget.ProgressBar.Large"))
                    pb.setStyle(LGProgressBar.Style.Large);
                else if(style.contains("Widget.ProgressBar.Inverse"))
                    pb.setStyle(LGProgressBar.Style.Inverse);
                else if(style.contains("Widget.ProgressBar.Small.Inverse"))
                    pb.setStyle(LGProgressBar.Style.SmallInverse);
                else if(style.contains("Widget.ProgressBar.Large.Inverse"))
                    pb.setStyle(LGProgressBar.Style.LargeInverse);
            }
		}
		
		if (result instanceof LGLinearLayout) {
            LGLinearLayout ll = (LGLinearLayout)result;
			String orient = findAttribute(atts, "android:orientation");
			if (orient != null) {
				if (orient.equals("horizontal"))
					ll.setOrientation(0);
				else if (orient.equals("vertical"))
					ll.setOrientation(1);
			}
			
			String focusable = findAttribute(atts, "android:focusable");
			/*if(focusable != null)
				ll.setFocusable(focusable.compareTo("true") == 0 ? true : false);*/

			String focusableInTouchMode = findAttribute(atts, "android:focusableInTouchMode");
			/*if(focusableInTouchMode != null)
				ll.setFocusableInTouchMode(focusableInTouchMode.compareTo("true") == 0 ? true : false);*/
		}
		
		if (result instanceof LGRadioGroup) {
            LGRadioGroup rg = (LGRadioGroup)result;
			String cid = findAttribute(atts, "android:checkedButton");
			if (cid != null) {
				//rg.check(Integer.parseInt(cid));
			}
		}
		
		if (result instanceof LGView) {
            LGView v = (LGView)result;

			maybeSetBoolean(v, "setClickable", atts, "android:clickable");
			maybeSetBoolean(v, "setFocusable", atts, "android:focusable");
			maybeSetBoolean(v, "setHapticFeedbackEnabled", atts, "android:hapticFeedbackEnabled");
			
			String background = findAttribute(atts, "android:background");
			if (background != null)
			{
				String backVal = LGColorParser.GetInstance().ParseColor(background);
				v.setBackgroundColor(backVal);
			}
			
			String visibility = findAttribute(atts, "android:visibility");
		    if (visibility != null){
		    	int code = -1;
		    	if ("visible".equals(visibility)) {
		    		code = 0;
		    	} else if ("invisible".equals(visibility)) {
		    		code = 1;
		    	} else if ("gone".equals(visibility)) {
		    		code = 2;
		    	}
		    	else
		    		code = Integer.valueOf(visibility);
		    	if (code != -1) {
		    		v.setVisibility(code);
		    	}
		    }
		}
		
		/*if (lgStack.size() > 0)
		{
			LayoutParams lps = loadLayoutParams(atts, (LGView)lgresult, (LGViewGroup) lgStack.peek());
			lgresult.setLayoutParams(lps);
		}
		else
			lgresult.setLayoutParams(loadLayoutParams(atts, lgresult));*/
		
		if(lgresult instanceof LGView)
			((LGView)lgresult).onCreate();
		
		return lgresult;
	}
	
	/**
	 * (Ignore)
	 */
	private boolean maybeSetBoolean(LGView v, String method, HashMap<String, String> atts, String arg) {
		return maybeSetBoolean(v, method, findAttribute(atts, arg));
	}
	
	/**
	 * (Ignore)
	 */
	protected static boolean isLayout(String name) {
		return name.endsWith("Layout") ||
				name.equals("RadioGroup") ||
				name.equals("TableRow") ||
				name.equals("ScrollView");
	}
	
	/**
	 * (Ignore)
	 */
	protected static boolean isLGLayout(String name) {
		return (name.startsWith("LG") && name.endsWith("Layout")) ||
				name.equals("LGRadioGroup") ||
				name.equals("LGTableRow") ||
				name.equals("LGScrollView");
	}
	
	/**
	 * (Ignore)
	 */
	protected int lookupId(String id) {
		int ix = id.indexOf("/");
		if (ix != -1) {
			String idName = id.substring(ix+1);
			Integer n = ids.get(idName);
			if (n == null && id.startsWith("@+")) {
				n = new Integer(idg++);
				ids.put(idName, n);
			}
			if (n != null)
				return n.intValue();
		}
		return -1;
	}
	
	/**
	 * (Ignore)
	 */
	protected String findAttribute(HashMap<String, String> atts, String id) {
		if(atts.containsKey(id))
			return atts.get(id);

		//TODO:look
		/*int ix = id.indexOf(":");
		String[] attsNs = id.split(":");
		if (ix != -1) {
			if(attsNs[0].compareTo("lua") == 0)
				return atts.getAttributeValue("http://schemas.android.com/apk/res-auto", id.substring(ix + 1));
			else
				return atts.getAttributeValue("http://schemas.android.com/apk/res/android", id.substring(ix+1));
		}
		else {
			return null;
		}*/
		return null;
	}
	
	/**
	 * (Ignore)
	 */
	protected LayoutParams loadLayoutParams(HashMap<String, String> atts, LGView v)
	{
		LayoutParams lps = null;
		String width = findAttribute(atts, "android:layout_width");
		String height = findAttribute(atts, "android:layout_height");
		int w, h;
		w = LGDimensionParser.GetInstance().GetDimension(width);
		h = LGDimensionParser.GetInstance().GetDimension(height);
		
		lps = new LayoutParams(w, h);
		
		String weight = findAttribute(atts, "android:layout_weight");
		if (weight != null) {
			lps.weight = Float.parseFloat(weight);
		}
		
		String gravity = findAttribute(atts, "android:layout_gravity");
		if (gravity != null) {
			int gravityOr = 0;
			String[] arr = gravity.split("\\|");
			for(String g : arr)
			{
				if(g.equals("top"))
					gravityOr |= Gravity.TOP;
				else if(g.equals("bottom"))
					gravityOr |= Gravity.BOTTOM;
				else if(g.equals("left"))
					gravityOr |= Gravity.LEFT;
				else if(g.equals("right"))
					gravityOr |= Gravity.RIGHT;
				else if(g.equals("center_vertical"))
					gravityOr |= Gravity.CENTER_VERTICAL;
				else if(g.equals("fill_vertical"))
					gravityOr |= Gravity.FILL_VERTICAL;
				else if(g.equals("center_horizontal"))
					gravityOr |= Gravity.CENTER_HORIZONTAL;
				else if(g.equals("fill_horizontal"))
					gravityOr |= Gravity.FILL_HORIZONTAL;
				else if(g.equals("center"))
					gravityOr |= Gravity.CENTER;
				else if(g.equals("fill"))
					gravityOr |= Gravity.FILL;
			}
					
			lps.gravity = gravityOr;
		}
		
		// Margin handling
		// Contributed by Vishal Choudhary - Thanks!
		String bottom = findAttribute(atts, "android:layout_marginBottom");
        String left = findAttribute(atts, "android:layout_marginLeft");
        String right = findAttribute(atts, "android:layout_marginRight");
        String top = findAttribute(atts, "android:layout_marginTop");
        String margin = findAttribute(atts, "android:layout_margin");
        if(margin != null)
        {
        	bottom = margin;
        	left = margin;
        	right = margin;
        	top = margin;
        }
        int bottomInt=0, leftInt=0, rightInt=0, topInt=0;
        if (bottom != null)
            bottomInt = DisplayMetrics.readSize(bottom);
        if (left != null)
            leftInt = DisplayMetrics.readSize(left);
        if (right != null)
            rightInt = DisplayMetrics.readSize(right);
        if (top != null)
            topInt = DisplayMetrics.readSize(top);
	    
	    lps.setMargins(leftInt, topInt, rightInt, bottomInt);
	    
	    String pbottom = findAttribute(atts, "android:layout_paddingBottom");
        String pleft = findAttribute(atts, "android:layout_paddingLeft");
        String pright = findAttribute(atts, "android:layout_paddingRight");
        String ptop = findAttribute(atts, "android:layout_paddingTop");
        String ppadding = findAttribute(atts, "android:layout_padding");
        if(ppadding != null)
        {
        	pbottom = ppadding;
        	pleft = ppadding;
        	pright = ppadding;
        	ptop = ppadding;
        }
        int pbottomInt=0, pleftInt=0, prightInt=0, ptopInt=0;
        if (pbottom != null)
            pbottomInt = DisplayMetrics.readSize(pbottom);
        if (left != null)
            pleftInt = DisplayMetrics.readSize(pleft);
        if (right != null)
            prightInt = DisplayMetrics.readSize(pright);
        if (top != null)
            ptopInt = DisplayMetrics.readSize(ptop);
	    
	    v.setPadding(pleftInt, ptopInt, prightInt, pbottomInt);
	    
	    return lps;
	}
	
	/**
	 * (Ignore)
	 */
	protected LayoutParams loadLayoutParams(HashMap<String, String> atts, LGView current, LGViewGroup vg) {
		LayoutParams lps = null;
		
		String width = findAttribute(atts, "android:layout_width");
		String height = findAttribute(atts, "android:layout_height");
		int w, h;
		w = LGDimensionParser.GetInstance().GetDimension(width);
		h = LGDimensionParser.GetInstance().GetDimension(height);
		
		/*if (vg instanceof LGListView)
		{
			//lps = new AbsListView.LayoutParams(w, h);
			lps = new LayoutParams(w, h);
		}
		else*/ if (vg instanceof LGRadioGroup) {
			lps = new LayoutParams(w, h);
		}
		else if (vg instanceof LGTableRow) {
			lps = new LayoutParams();
		}
		else if (vg instanceof LGTableLayout) {
			lps = new LayoutParams();
		}
		else if (vg instanceof LGLinearLayout) {
			lps = new LayoutParams(w,h);
		}
		else if (vg instanceof LGRelativeLayout) {
			lps = new LayoutParams(w,h);
		}
		else if (vg instanceof LGScrollView) {
			//lps = new ScrollView.LayoutParams(w,h);
			lps = new LayoutParams(w,h);
		}
		else if (vg instanceof LGFrameLayout) {
			lps = new LayoutParams(w,h);
		}

		String weightSum = findAttribute(atts, "android:weightSum");
		if(weightSum != null)
			lps.weightSum = Float.valueOf(weightSum);

		String weight = findAttribute(atts, "android:layout_weight");
		if (weight != null) {
			lps.weight = Float.parseFloat(weight);
		}
		String gravity = findAttribute(atts, "android:layout_gravity");
		if (gravity != null)
		{
			int gravityOr = 0;
			String[] arr = gravity.split("\\|");
			for(String g : arr)
			{
				if(g.equals("top"))
					gravityOr |= Gravity.TOP;
				else if(g.equals("bottom"))
					gravityOr |= Gravity.BOTTOM;
				else if(g.equals("left"))
					gravityOr |= Gravity.LEFT;
				else if(g.equals("start"))
					gravityOr |= Gravity.START;
				else if(g.equals("right"))
					gravityOr |= Gravity.RIGHT;
				else if(g.equals("end"))
					gravityOr |= Gravity.END;
				else if(g.equals("center_vertical"))
					gravityOr |= Gravity.CENTER_VERTICAL;
				else if(g.equals("fill_vertical"))
					gravityOr |= Gravity.FILL_VERTICAL;
				else if(g.equals("center_horizontal"))
					gravityOr |= Gravity.CENTER_HORIZONTAL;
				else if(g.equals("fill_horizontal"))
					gravityOr |= Gravity.FILL_HORIZONTAL;
				else if(g.equals("center"))
					gravityOr |= Gravity.CENTER;
				else if(g.equals("fill"))
					gravityOr |= Gravity.FILL;
			}

			if(gravityOr == 0)
				lps.gravity = Integer.valueOf(gravity);
			else
				lps.gravity = gravityOr;
		}

		//TODO:
		/*if (lps instanceof RelativeLayout.LayoutParams)
		{
			RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams)lps;
			for (int i=0;i<relative_strings.length;i++) {
				String id  = findAttribute(atts, relative_strings[i]);
				if (id != null) 
				{
					int idN = lookupId(id);
					l.addRule(relative_verbs[i], idN);
				}
			}
		}*/

		String bottom = findAttribute(atts, "android:layout_marginBottom");
		String left = findAttribute(atts, "android:layout_marginLeft");
		String right = findAttribute(atts, "android:layout_marginRight");
		String top = findAttribute(atts, "android:layout_marginTop");
		String margin = findAttribute(atts, "android:margin");
		if(margin != null)
		{
			bottom = margin;
			left = margin;
			right = margin;
			top = margin;
		}
		int bottomInt=0, leftInt=0, rightInt=0, topInt=0;
		if (bottom != null)
			bottomInt = DisplayMetrics.readSize(bottom);
		if (left != null)
			leftInt = DisplayMetrics.readSize(left);
		if (right != null)
			rightInt = DisplayMetrics.readSize(right);
		if (top != null)
			topInt = DisplayMetrics.readSize(top);
		    
		lps.setMargins(leftInt, topInt, rightInt, bottomInt);
		    
		String pbottom = findAttribute(atts, "android:layout_paddingBottom");
		String pleft = findAttribute(atts, "android:layout_paddingLeft");
		String pright = findAttribute(atts, "android:layout_paddingRight");
		String ptop = findAttribute(atts, "android:layout_paddingTop");
		String ppadding = findAttribute(atts, "android:padding");
		if(ppadding != null)
		{
			pbottom = ppadding;
			pleft = ppadding;
			pright = ppadding;
			ptop = ppadding;
		}
		int pbottomInt=0, pleftInt=0, prightInt=0, ptopInt=0;
		if (pbottom != null)
			pbottomInt = DisplayMetrics.readSize(pbottom);
		if (left != null)
			pleftInt = DisplayMetrics.readSize(pleft);
		if (right != null)
			prightInt = DisplayMetrics.readSize(pright);
		if (top != null)
			ptopInt = DisplayMetrics.readSize(ptop);
		    
		vg.setPadding(pleftInt, ptopInt, prightInt, pbottomInt);
		
		return lps;
	}
	
	/**
	 * (Ignore)
	 */
	protected int readSize(String val) {
		/*if ("wrap_content".equals(val)) {
			return ViewGroup.LayoutParams.WRAP_CONTENT;
		}
		else if ("fill_parent".equals(val)) {
			return ViewGroup.LayoutParams.FILL_PARENT;
		}
		else if (val != null) {
			int ix = val.indexOf("px");
			if (ix != -1)
				return Integer.parseInt(val.substring(0, ix));
			else
				return 0;
		}
		else {
			return ViewGroup.LayoutParams.WRAP_CONTENT;
		}*/
		return 0;
	}
	
	/**
	 * (Ignore)
	 */
	boolean maybeSetBoolean(LGView view, String method, String value) {
		if (value == null) {
			return false;
		}
		value = value.toLowerCase();
		Boolean boolValue = null;
		if ("true".equals(value)) {
			boolValue = Boolean.TRUE;
		} else if ("false".equals(value)) {
			boolValue = Boolean.FALSE;
		} else {
			return false;
		}
		try {
			Method m = LGView.class.getMethod(method, boolean.class);
			m.invoke(view, boolValue);
			return true;
		} catch (NoSuchMethodException ex) {
			Log.e("ViewInflater", "No such method: " + method, ex);
		} catch (IllegalArgumentException e) {
			Log.e("ViewInflater", "Call", e);
		} catch (IllegalAccessException e) {
			Log.e("ViewInflater", "Call", e);
		} catch (InvocationTargetException e) {
			Log.e("ViewInflater", "Call", e);
		}
		return false;
	}
	
	/*String[] relative_strings = new String[]
	                            	       {"android:layout_above", 
	                            			"android:layout_alignBaseline", 
	                            			"android:layout_alignBottom", 
	                            			"android:layout_alignLeft",
	                            			"android:layout_alignParentBottom",
	                            			"android:layout_alignParentLeft",
	                            			"android:layout_alignParentRight",
	                            			"android:layout_alignParentTop",
	                            			"android:layout_alignRight", 
	                            			"android:layout_alignTop", 
	                            			"android:layout_below",
	                            			"android:layout_centerHorizontal",
	                            			"android:layout_centerInParent",
	                            			"android:layout_centerVertical",
	                            			"android:layout_toLeft",
	                            			"android:layout_toRight"};
	                            	
	                            	int[] relative_verbs = new int[]
	                            	       {RelativeLayout.ABOVE,
	                            			RelativeLayout.ALIGN_BASELINE,
	                            			RelativeLayout.ALIGN_BOTTOM,
	                            			RelativeLayout.ALIGN_LEFT,
	                            			RelativeLayout.ALIGN_PARENT_BOTTOM,
	                            			RelativeLayout.ALIGN_PARENT_LEFT,
	                            			RelativeLayout.ALIGN_PARENT_RIGHT,
	                            			RelativeLayout.ALIGN_PARENT_TOP,
	                            			RelativeLayout.ALIGN_RIGHT,
	                            			RelativeLayout.ALIGN_TOP,
	                            			RelativeLayout.BELOW,
	                            			RelativeLayout.CENTER_HORIZONTAL,
	                            			RelativeLayout.CENTER_IN_PARENT,
	                            			RelativeLayout.CENTER_VERTICAL,
	                            			RelativeLayout.LEFT_OF,
	                            			RelativeLayout.RIGHT_OF,
	                            	      };*/
	                            	
	/**
	 * (Ignore)
	 */
	@Override
	public void RegisterEventFunction(String var, LuaTranslator lt)
	{
	
	}
	
	/**
	 * (Ignore)
	 */
	@Override
	public String GetId()
	{
		return "LuaViewInflator";
	}
}
