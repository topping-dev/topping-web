package android.widget;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Application;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.Gravity;
import com.dk.scriptingengine.osspecific.LayoutParams;
import com.dk.scriptingengine.osspecific.Rect;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;
import com.dk.scriptingengine.osspecific.socketobjects.MethodModel;
import com.dk.scriptingengine.osspecific.socketobjects.WatchModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import jodd.jerry.Jerry;
import jodd.lagarto.dom.DOMBuilder;
import jodd.lagarto.dom.Node;

@LuaClass(className = "LGView")
public class LGView extends Jerry implements LuaInterface, Serializable
{
	public static final String SET_VALUE = "__SetValue";
	/**
	 * 
	 */
	private static final long serialVersionUID = -3912083677201213682L;
	private static int idCounter = 1000;
	protected boolean loaded = false;
	public LuaContext lc;
	public String luaId = null;
	protected ArrayList<LGView> subviews = new ArrayList<LGView>();
	protected LGView parent;
	public String styleId = "";
	private Object tag;
	protected ArrayList<JSModel> jsModel = new ArrayList<>();
	protected ArrayList<MethodModel> methodModel = new ArrayList<>();
	protected String jsCoreModel = "";
	protected ArrayList<WatchModel> watchModel = new ArrayList<>();
	//protected String methodModel = "";
	private String backgroundColor;
    private int visibility;
    private Rect padding;
    private LayoutParams layoutParams;
    private Integer gravityToSet = null;

    /**
	 * Creates LGView Object From Lua.
	 * @param lc
	 * @return LGView
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class }, self = LGView.class)
	public static LGView Create(LuaContext lc)
	{
		return new LGView(lc.GetContext());
	}
		
	/**
	 * (Ignore)
	 */
	public LGView(Context context)
	{
		super(Application.DOM_BUILDER, Jerry.jerry("<div></div>").get());
		this.styleId  = "LuaWeb" + idCounter++;
		lc = LuaContext.CreateLuaContext(context);
		Setup(context);
	}

	/**
	 * (Ignore)
	 */
	public LGView(Context context, String luaId)
	{
        super(Application.DOM_BUILDER, Jerry.jerry("<div></div>").get());
		this.styleId  = "LuaWeb" + idCounter++;
		this.luaId = luaId;
		lc = LuaContext.CreateLuaContext(context);
		Setup(context);
	}

    /**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	addClass(styleId);
    }

    /**
	 * (Ignore)
	 */
	public void AfterSetup(Context context)
	{
		if (!loaded)
        {
			LuaEngine.getInstance().OnGuiEvent(this, LuaEngine.GuiEvents.GUI_EVENT_CREATE, lc);
            loaded = true;
        }	
	}

    /**
     * (Ignore)
     */
    public void onLayoutCreated()
    {

    }

	/**
	 * (Ignore)
	 */
	public void onCreate()
	{
		for(LGView w : subviews)
			w.onCreate();
	}
	
	/**
	 * (Ignore)
	 */
	public void onResume()
	{
		for(LGView w : subviews)
			w.onResume();
	}
	
	/**
	 * (Ignore)
	 */
	public void onPause()
	{
		for(LGView w : subviews)
			w.onPause();
	}
	
	/**
	 * (Ignore)
	 */
	public void onDestroy()
	{
		for(LGView w : subviews)
			w.onDestroy();
	}
       
	/**
	 * (Ignore)
	 */
    public LGView GetView() { return this; }

	/**
	 * (Ignore)
	 */
	public void removeAllViews()
	{
		for(Jerry sibling : siblings())
			sibling.remove();
	}
    
    /**
	 * (Ignore)
	 */
    public void PrintDescription(String last)
    {
    	/*ViewGroup.LayoutParams lps = getLayoutParams();
    	ViewGroup.LayoutParams lpsv = view.getLayoutParams();
    	Log.e("PrintDescription", last + toString() + " Width: " + lps.width + " Height: " + lps.height);
		Log.e("PrintDescriptionView", last + view.toString() + " Width: " + lpsv.width + " Height: " + lpsv.height);
    	for(LGView w : subviews)
		{
			w.PrintDescription(last + "--");
		}*/
    }

    /**
     * Get context
     * @return LuaContext
     */
    @LuaFunction(manual = false, methodName = "GetContext")
    public LuaContext GetContext()
    {
        return lc;
    }

	/**
     * Get view by id
     * @param lId
     * @return LGView
     */
    @LuaFunction(manual = false, methodName = "GetViewById", arguments = { String.class })
    public LGView GetViewById(String lId)
	{
		if(this.GetId() != null && this.GetId().compareTo(lId) == 0)
			return this;
		else
		{
			for(LGView w : subviews)
			{
				LGView wFound = w.GetViewById(lId);
				if(wFound != null)
					return wFound;
			}
		}
		return null;
	}
    
    /**
     * Set enabled
     * @param value
     */
    @LuaFunction(manual = false, methodName = "SetEnabled", arguments = { Integer.class })
    public void SetEnabled(Integer value)
    {
		attr("disabled", value.intValue() == 1 ? "true" : "false");
    }
    
    /**
     * Set focusable
     * @param value
     */
    @LuaFunction(manual = false, methodName = "SetFocusable", arguments = { Integer.class })
    public void SetFocusable(Integer value)
    {
		attr("disabled", value.intValue() == 1 ? "true" : "false");
    }
    
    /**
     * Set background
     * @param background
     */
    @LuaFunction(manual = false, methodName = "SetBackground", arguments = { String.class })
    public void SetBackground(String background)
    {
		css("background-color", background);
    	/*int backVal = LuaViewInflator.parseColor(lc, background);
		if(backVal != Integer.MAX_VALUE)
			view.setBackgroundColor(backVal);
		else
			view.setBackgroundResource(lc.GetContext().getResources().getIdentifier(background, (String)null, lc.GetContext().getPackageName()));*/
    }

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     */
	public void RegisterEventFunction(String var, final LuaTranslator lt)
	{
		if(var.compareTo("Click") == 0)
		{
			/*view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) 
				{
					lt.Call(this, v);
				}
			});*/
		}
	}

	/**
	 * (Ignore)
	 */
	@Override
	public String GetId()
	{
		if(luaId != null)
			return luaId;
		//TODO Fixme
		/*String customId = (String) view.getTag(-1);
		if(customId == null)
		{
			//TODO:Check this
			TypedArray a = view.getContext().obtainStyledAttributes(attrs, R.styleable.lua, 0, 0);
			//BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("icon_combo_box_arrow", "drawable", Common.pack));
			String str = a.getString(R.styleable.lua_id);
			a.recycle();
			return str;
		}
		return customId;*/
		return "";
	}
	
	/**
	 * (Ignore)
	 */
	public void SetLuaId(String val)
	{
		luaId = val;
	}

	/**
	 * (Ignore)
	 */
	public Object getTag()
	{
		return tag;
	}

	/**
	 * (Ignore)
	 */
	public void setTag(Object tag)
	{
		this.tag = tag;
	}

    /**
     * (Ignore)
     */
	public Node getRootNode()
    {
        return get(0).getChild(0);
    }

    /**
     * (Ignore)
     */
    public String toWeb()
    {
        if(this instanceof LGViewGroup)
        {
            StringBuilder sb = new StringBuilder();
            for(LGView v : subviews)
            {
                sb.append(v.toWeb()).append("\n");
            }
            return html().replace("¨¨~~", sb.toString());
        }
        return html();
    }

	/**
	 * (Ignore)
	 */
	public String toJsCoreModel()
	{
		if(this instanceof LGViewGroup)
		{
			StringBuilder sb = new StringBuilder();
			for(LGView v : subviews)
			{
				String js = v.toJsCoreModel();
				if(!js.equals(""))
				{
					sb.append(v.toJsCoreModel());
				}
			}
			return sb.toString();
		}
		return jsCoreModel;
	}

	/**
	 * (Ignore)
	 */
    public String toJs()
    {
        StringBuilder sb = new StringBuilder();
        if(this instanceof LGViewGroup)
        {
            for(LGView v : subviews)
            {
                String js = v.toJs();
                if(!js.equals(""))
                {
                    sb.append(v.toJs()).append(",");
                }
            }
        }
        for(JSModel js : jsModel)
        	sb.append(js.toString()).append(",");
        if(sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public void setJsValue(String model, Object val)
    {
        for(JSModel js : jsModel)
        {
            if(js.name.equals(model))
            {
                js.value = val;
                break;
            }
        }
    }

    /**
     * (Ignore)
     */
    public String toWatch()
    {
		StringBuilder sb = new StringBuilder();
        if(this instanceof LGViewGroup)
        {
            for(LGView v : subviews)
            {
                String watch = v.toWatch();
                if(!watch.equals(""))
                {
                    sb.append(v.toWatch()).append(",");
                }
            }
        }
        for(WatchModel js : watchModel)
            sb.append(js.toString()).append(",");
        if(sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * (Ignore)
     */
    public String toMethod()
    {
        StringBuilder sb = new StringBuilder();
        if(this instanceof LGViewGroup)
        {
            for(LGView v : subviews)
            {
                String watch = v.toMethod();
                if(!watch.equals(""))
                {
                    sb.append(v.toMethod()).append(",");
                }
            }
        }
        for(MethodModel js : methodModel)
            sb.append(js.toString()).append(",");
        if(sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

	/**
	 * (Ignore)
	 */
    public void setGravity(int gravity)
    {
        if(this.layoutParams == null)
            gravityToSet = gravity;
        else
            layoutParams.gravity = gravity;
        //TODO:
        /*Rect in = new Rect(LayoutServer.)
        Gravity.applyDisplay();*/
    }

	/**
	 * (Ignore)
	 */
    public int getGravity()
    {
        return layoutParams.gravity;
    }

	/**
	 * (Ignore)
	 */
	public void setBackgroundColor(String backgroundColor)
	{
		this.backgroundColor = backgroundColor;
		String s = "";
		if(getRootNode().getAttribute("style") != null)
			s = getRootNode().getAttribute("style");
		s += "background-color:" + backgroundColor + ";";
		getRootNode().setAttribute("style", s);

	}


    /**
     * (Ignore)
     */
	public String getBackgroundColor()
	{
		return backgroundColor;
	}

    /**
     * (Ignore)
     */
    public void setVisibility(int visibility)
    {
        this.visibility = visibility;
        String attr = getRootNode().getAttribute("style");
        Map<String, String> styles = createPropertiesMap(attr, ';', ':');
        if(visibility == 0)
        {
            styles.remove("visibility");
            styles.remove( "display");
        }
        else if(visibility == 1)
        {
            styles.remove( "display");
            styles.put("visibility", "hidden");
        }
        else
        {
            styles.remove( "visibility");
            styles.put("display", "none");
        }
        attr = generateAttributeValue(styles, ';', ':');
        getRootNode().setAttribute("style", attr);
    }

    /**
     * (Ignore)
     */
    public int getVisibility()
    {
        return visibility;
    }

    /**
     * (Ignore)
     */
    public void setPadding(int pleft, int ptop, int pright, int pbottom)
    {
        this.padding = new Rect();
        this.padding.set(pleft, ptop, pright, pbottom);
    }

	/**
	 * (Ignore)
	 */
    public LayoutParams getLayoutParams()
	{
    	return layoutParams;
	}

    /**
     * (Ignore)
     */
    public void setLayoutParams(LayoutParams lps)
    {
        this.layoutParams = lps;
        if(gravityToSet != null)
        {
            this.layoutParams.gravity = gravityToSet;
            gravityToSet = null;
        }

        if(layoutParams.w == LayoutParams.MATCH_PARENT)
        {
            wrapWithHtml("<v-flex grow>", "</v-flex>");
        }
        else
        {
            if(layoutParams.w != LayoutParams.WRAP_CONTENT)
            {
                if(layoutParams.w == 0)
                {
                    /*float weightSum = 1;
                    if(parent != null && parent.layoutParams != null)
                        weightSum = parent.layoutParams.weightSum;

                    int widthPercent = (int) ((layoutParams.weight * 100) / weightSum);
                        addStyle("width:" + widthPercent + "%;");*/
                }
                else
                    addStyle("width:" + layoutParams.w + "px;");
            }
            else if(this instanceof LGFrameLayout)
                getRootNode().setAttribute("calculateHeight");
            else
            {
                wrapWithHtml("<v-flex shrink calculateWidth>", "</v-flex>");
            }
        }

        if(layoutParams.h == LayoutParams.MATCH_PARENT)
        {
            addStyle("height:100%;");
            wrapWithHtml("<v-flex grow style=\"height:100%;\">", "</v-flex>");

            /*String sj = "";
            if(getRootNode().getAttribute(":style") != null)
                sj = getRootNode().getAttribute(":style");

            ObjectMapper om = new ObjectMapper();
            HashMap<String, String> styleMap = new HashMap<>();
            try
            {
                styleMap = om.readValue(sj, new TypeReference<HashMap<String,Object>>() {});
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            styleMap.put("height", "100%%");
            try
            {
                getRootNode().setAttribute(":style", om.writeValueAsString(styleMap));
            }
            catch (JsonProcessingException e)
            {
                e.printStackTrace();
            }*/
        }
        else
        {
            if(layoutParams.h != LayoutParams.WRAP_CONTENT)
            {
                if(layoutParams.w == 0)
                {
                    /*float weightSum = 1;
                    if(parent != null && parent.layoutParams != null)
                        weightSum = parent.layoutParams.weightSum;

                    int widthPercent = (int) ((layoutParams.weight * 100) / weightSum);
                        addStyle("height:" + widthPercent + "%;");*/
                }
                else
                    addStyle("height:" + layoutParams.h + "px;");
            }
            else if(this instanceof LGFrameLayout)
                getRootNode().setAttribute("calculateHeight");
            else
                wrapWithHtml("<v-flex shrink>", "</v-flex>");

        }

        if(parent instanceof LGFrameLayout)
            layoutParams.gravity = Gravity.LEFT;

        if(layoutParams.gravity > 0)
        {
            if(parent instanceof LGFrameLayout)
            {
                addStyle("position:absolute;");
                getRootNode().setAttribute("absolute");
            }

            if(parent instanceof LGLinearLayout)
            {
                /*String s = "";
                if (getRootNode().getAttribute("style") != null)
                    s = getRootNode().getAttribute("style");
                s += "position:absolute;";
                getRootNode().setAttribute("style", s);
                getRootNode().setAttribute("absolute");*/
            }

            /*String c = "";
            if(getRootNode().getAttribute("class") != null)
                c = getRootNode().getAttribute("class");
            c += " test";
            getRootNode().setAttribute("class", c);*/
        }
        if((layoutParams.gravity & Gravity.RIGHT) > 0)
        {
            if(parent instanceof LGFrameLayout)
            {
                addStyle("right:0px;");
            }

            if(parent instanceof LGLinearLayout)
            {
                addStyle("align-self:flex-end !important;");
            }
        }
        if((layoutParams.gravity & Gravity.BOTTOM) > 0)
        {
            if(parent instanceof LGFrameLayout)
            {
                addStyle("bottom:0px;");
            }

        }
        if((layoutParams.gravity & Gravity.CENTER) > 0)
        {
            if(parent instanceof LGFrameLayout)
            {
                addStyle("top:50%;left:50%;transform:translate(-50%, -50%);");
            }

            if(parent instanceof LGLinearLayout)
            {
                addStyle("align-self:center !important;");
            }
        }
        else if((layoutParams.gravity & Gravity.CENTER_VERTICAL) > 0)
        {
            if(parent instanceof LGFrameLayout)
            {
                addStyle("left:50%;transform:translate(-50%, 0);");
            }

            if(parent instanceof LGLinearLayout)
            {
                addStyle("align-self:center !important;");
            }
        }
        else if((layoutParams.gravity & Gravity.CENTER_HORIZONTAL) > 0)
        {
            if(parent instanceof LGFrameLayout)
            {
                addStyle("top:50%;transform:translate(0, -50%);");
            }

            if(parent instanceof LGLinearLayout)
            {
                addStyle("align-self:center !important;");
            }
        }
    }

	/**
	 * (Ignore)
	 * @param id
	 * @param event
	 * @param vals
	 */
	public void CallEventValue(String id, String event, Object ... vals)
	{

	}

    /**
     * (Ignore)
     */
    protected void addMethodModel(MethodModel methodModel)
    {
        this.methodModel.add(methodModel);
    }

    /**
     * (Ignore)
     */
    public void addView(LGView v)
    {
        v.parent = this;
        this.subviews.add(v);
    }

    /**
     * (Ignore)
     */
    protected void addStyle(String style)
    {
        String s = "";
        Node node = getRootNode();
        if(node.getHtml().contains("v-flex"))
            node = node.getFirstChild();
        if(node.getAttribute("style") != null)
            s = node.getAttribute("style");
        s += style;
        node.setAttribute("style", s);
    }

    /**
     * (Ignore)
     */
    protected void wrapWithHtml(String htmlStart, String htmlEnd)
    {
        html(htmlStart + html() + htmlEnd);
    }
}
