package com.dk.scriptingengine;

import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.osspecific.Context;

/**
 * Lua Graphics manipulation class
 */
@LuaClass(className = "LuaGraphics")
public class LuaGraphics implements LuaInterface
{
    //Pen == paint android
    //private Canvas gr;
    private Context c;
    /*private Paint lastPen;
    private Paint lastBrush;
    //private HashMap<String, Paint> m_fontStore;
    private Rect rectStore1;
    private Rect rectStore2;*/

    /**
     * (Ignore)
     */
    public LuaGraphics(/*Canvas g,*/ Context cP)
    {
        //gr = g;
        c = cP;
        /*rectStore1 = new Rect();
        rectStore2 = new Rect();
        lastPen = new Paint();
        lastBrush = new Paint();*/
        //m_fontStore = new HashMap<String, Paint>();
    }

    /**
     * (Ignore)
     */
    public static LuaGraphics FromGraphics(/*Canvas gr,*/ Context c)
    {
        return new LuaGraphics(/*gr,*/ c);
    }

    /**
     * Sets the pen color.
     * @param r
     * @param g
     * @param b
     */
    @LuaFunction(manual = false, methodName= "SetPen", arguments = { Integer.class, Integer.class, Integer.class })
    public void SetPen(int r, int g, int b)
    {
        /*lastPen = new Paint();
        lastPen.setColor(Color.rgb(r, g, b));*/
    }

    /**
     * Sets the pen color and width
     * @param r
     * @param g
     * @param b
     * @param width
     */
    @LuaFunction(manual = false, methodName= "SetPenEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class })
    public void SetPenEx(int r, int g, int b, float width)
    {
        /*lastPen = new Paint();
        lastPen.setColor(Color.rgb(r, g, b));
        lastPen.setStrokeWidth(width);*/
    }

    /**
     * Sets the brush type and color
     * @param type
     * @param r
     * @param g
     * @param b
     */
    @LuaFunction(manual = false, methodName= "SetBrush", arguments = { Integer.class, Integer.class, Integer.class, Integer.class })
    public void SetBrush(int type, int r, int g, int b)
    {
        /*if (type == 0)
        {
            lastBrush = new SolidBrush(Color.FromArgb(r, g, b));
        }
        else
        {
            lastBrush = new TextureBrush(new Bitmap("asd"));
        }*/
    }

    /**
     * Sets the rectangle store based on id
     * @param id
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "SetRectStore", arguments = { Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void SetRectStore(int id, int x, int y, int width, int height)
    {
        /*if (id == 0)
            rectStore1 = new Rect(x, y, x+width, y+height);
        else
            rectStore2 = new Rect(x, y, x+width, y+height);*/
    }

    /**
     * Draws an ellipse from rectangle store
     */
    @LuaFunction(manual = false, methodName= "DrawEllipsePenRectCache", arguments = { Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawEllipsePenRectCache()
    {
    	//gr.drawOval(new RectF(rectStore1), lastPen);
    }

    /**
     * Draws an ellipse from rectangle store
     * @param r
     * @param g
     * @param b
     */
    @LuaFunction(manual = false, methodName= "DrawEllipseRectCache", arguments = { Integer.class, Integer.class, Integer.class })
    public void DrawEllipseRectCache(int r, int g, int b)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
        gr.drawOval(new RectF(rectStore1), p);*/
    }

    /**
     * Draws an ellipse from rectangle store
     * @param r
     * @param g
     * @param b
     * @param width
     */
    @LuaFunction(manual = false, methodName= "DrawEllipseRectCacheEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class })
    public void DrawEllipseRectCacheEx(int r, int g, int b, float width)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	p.setStrokeWidth(width);    	
        gr.drawOval(new RectF(rectStore1), p);*/
    }

    /**
     * Draws an ellipse from last pen used
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "DrawEllipsePenCache", arguments = { Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawEllipsePenCache(int x, int y, int width, int height)
    {    	
        //gr.drawOval(new RectF(x, y, width, height), lastPen);
    }

    /**
     * Draws an ellipse
     * @param r
     * @param g
     * @param b
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "DrawEllipse", arguments = { Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawEllipse(int r, int g, int b, int x, int y, int width, int height)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
        gr.drawOval(new RectF(x, y, width, height), p);*/
    }

    /**
     * Draws an ellipse
     * @param r
     * @param g
     * @param b
     * @param penWidth
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "DrawEllipseEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawEllipseEx(int r, int g, int b, float penWidth, int x, int y, int width, int height)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	p.setStrokeWidth(width);
    	gr.drawOval(new RectF(x, y, width, height), p);*/
    }

    /**
     * Draws circle based on last pen
     * @param x
     * @param y
     * @param radius
     */
    @LuaFunction(manual = false, methodName= "DrawCirclePenCache", arguments = { Integer.class, Integer.class, Integer.class })
    public void DrawCirclePenCache(int x, int y, int radius)
    {
    	//gr.drawCircle(x, y, radius, lastPen);
    }

    /**
     * Draws cicle
     * @param r
     * @param g
     * @param b
     * @param x
     * @param y
     * @param radius
     */
    @LuaFunction(manual = false, methodName= "DrawCircle", arguments = { Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawCircle(int r, int g, int b, int x, int y, int radius)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	gr.drawCircle(x, y, radius, p);*/
    }

    /**
     * Draws circle
     * @param r
     * @param g
     * @param b
     * @param width
     * @param x
     * @param y
     * @param radius
     */
    @LuaFunction(manual = false, methodName= "DrawCircleEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class, Integer.class, Integer.class, Integer.class })
    public void DrawCircleEx(int r, int g, int b, float width, int x, int y, int radius)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	p.setStrokeWidth(width);    	
    	gr.drawCircle(x, y, radius, p);*/
    }

    /**
     * Draws icon
     * @param i
     * @param x
     * @param y
     */
    @LuaFunction(manual = false, methodName= "DrawIcon", arguments = { String.class, Integer.class, Integer.class })
    public void DrawIcon(String i, int x, int y)
    {
    	/*int id = c.getResources().getIdentifier(i, "drawable", c.getPackageName());
    	Bitmap b = ((BitmapDrawable)c.getResources().getDrawable(id)).getBitmap();
        gr.drawBitmap(b, x, y, null);*/
    }

    /**
     * Draws image
     * @param i
     * @param x
     * @param y
     */
    @LuaFunction(manual = false, methodName= "DrawImage", arguments = { String.class, Integer.class, Integer.class })
    public void DrawImage(String i, int x, int y)
    {
    	/*int id = c.getResources().getIdentifier(i, "drawable", c.getPackageName());
    	Bitmap b = ((BitmapDrawable)c.getResources().getDrawable(id)).getBitmap();
    	gr.drawBitmap(b, x, y, null);*/
    }

    /**
     * Draws image on rectangle cache based on source
     * @param i
     * @param srcX
     * @param srcY
     * @param srcWidth
     * @param srcHeight
     * @param rL
     * @param gL
     * @param bL
     * @param rH
     * @param gH
     * @param bH
     */
    @LuaFunction(manual = false, methodName= "DrawImageRectCacheEx", arguments = { String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawImageRectCacheEx(String i, int srcX, int srcY, int srcWidth, int srcHeight, int rL, int gL, int bL, int rH, int gH, int bH)
    {
    	/*int id = c.getResources().getIdentifier(i, "drawable", c.getPackageName());
    	Bitmap b = ((BitmapDrawable)c.getResources().getDrawable(id)).getBitmap();
    	Paint p = new Paint();
        gr.drawBitmap(b, new Rect(srcX, srcY, srcWidth, srcHeight), rectStore1, p);*/
    }

    /**
     * Draws image on rectangle cache
     * @param i
     */
    @LuaFunction(manual = false, methodName= "DrawImageRectCache", arguments = { String.class })
    public void DrawImageRectCache(String i)
    {
    	/*int id = c.getResources().getIdentifier(i, "drawable", c.getPackageName());
    	Bitmap b = ((BitmapDrawable)c.getResources().getDrawable(id)).getBitmap();
    	Paint p = new Paint();
        gr.drawBitmap(b, rectStore1, rectStore2, p);*/
    }

    /**
     * Draws image
     * @param i
     * @param x
     * @param y
     * @param srcX
     * @param srcY
     * @param srcWidth
     * @param srcHeight
     */
    @LuaFunction(manual = false, methodName= "DrawImageEx", arguments = { String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawImageEx(String i, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight)
    {
    	/*int id = c.getResources().getIdentifier(i, "drawable", c.getPackageName());
    	Bitmap b = ((BitmapDrawable)c.getResources().getDrawable(id)).getBitmap();
    	Paint p = new Paint();
    	gr.drawBitmap(b, x, y, p);*/
    }

    /**
     * Draws line
     * @param r
     * @param g
     * @param b
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    @LuaFunction(manual = false, methodName= "DrawLine", arguments = { Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawLine(int r, int g, int b, int x1, int y1, int x2, int y2)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));    	
    	gr.drawLine(x1, y1, x2, y2, p);*/
    }

    /**
     * Draws line
     * @param r
     * @param g
     * @param b
     * @param width
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    @LuaFunction(manual = false, methodName= "DrawLineEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawLineEx(int r, int g, int b, float width, int x1, int y1, int x2, int y2)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));   
    	p.setStrokeWidth(width);
        gr.drawLine(x1, y1, x2, y2, p);*/
    }

    /**
     * Draws line from last pen
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    @LuaFunction(manual = false, methodName= "DrawLinePenCache", arguments = { Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawLinePenCache(int x1, int y1, int x2, int y2)
    {
        //gr.drawLine(x1, y1, x2, y2, lastPen);
    }

    /**
     * Draws lines
     * @param r
     * @param g
     * @param b
     * @param points
     */
    @LuaFunction(manual = false, methodName= "DrawLines", arguments = { Integer.class, Integer.class, Integer.class, String.class })
    public void DrawLines(int r, int g, int b, String points)
    {
        String[] arr = points.split(",");
        float[] pointArr = new float[arr.length];
        for (int i = 0; i < arr.length; i += 2)
        {
            pointArr[i] = Float.valueOf(arr[i]);
        }
        
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
        gr.drawLines(pointArr, p);*/
    }

    /**
     * Draws lines
     * @param r
     * @param g
     * @param b
     * @param width
     * @param points
     */
    @LuaFunction(manual = false, methodName= "DrawLinesEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class, String.class })
    public void DrawLinesEx(int r, int g, int b, float width, String points)
    {
        String[] arr = points.split(",");
        float[] pointArr = new float[arr.length];
        for (int i = 0; i < arr.length; i += 2)
        {
            pointArr[i] = Float.valueOf(arr[i]);
        }

    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	p.setStrokeWidth(width);
        gr.drawLines(pointArr, p);*/
    }

    /**
     * Draws lines from pen caches
     * @param points
     */
    @LuaFunction(manual = false, methodName= "DrawLinesPenCache", arguments = { String.class })
    public void DrawLinesPenCache(String points)
    {
        String[] arr = points.split(",");
        float[] pointArr = new float[arr.length];
        for (int i = 0; i < arr.length; i += 2)
        {
            pointArr[i] = Float.valueOf(arr[i]);
        }

        
        //gr.drawLines(pointArr, lastPen);
    }

    /**
     * Draws polygon
     * @param r
     * @param g
     * @param b
     * @param points
     */
    @LuaFunction(manual = false, methodName= "DrawPolygon", arguments = { Integer.class, Integer.class, Integer.class, String.class })
    public void DrawPolygon(int r, int g, int b, String points)
    {
        String[] arr = points.split(",");
        float[] pointArr = new float[arr.length];
        for (int i = 0; i < arr.length; i += 2)
        {
            pointArr[i] = Float.valueOf(arr[i]);
        }

    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	gr.drawPoints(pointArr, p);*/
    }

    /**
     * Draws polygon
     * @param r
     * @param g
     * @param b
     * @param width
     * @param points
     */
    @LuaFunction(manual = false, methodName= "DrawPolygonEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class, String.class })
    public void DrawPolygonEx(int r, int g, int b, float width, String points)
    {
        String[] arr = points.split(",");
        float[] pointArr = new float[arr.length];
        for (int i = 0; i < arr.length; i += 2)
        {
            pointArr[i] = Float.valueOf(arr[i]);
        }

    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	p.setStrokeWidth(width);
        gr.drawPoints(pointArr, p);*/
    }

    /**
     * Draws polygon from pen cache
     * @param points
     */
    @LuaFunction(manual = false, methodName= "DrawPolygonPenCache", arguments = { String.class })
    public void DrawPolygonPenCache(String points)
    {
        String[] arr = points.split(",");
        float[] pointArr = new float[arr.length];
        for (int i = 0; i < arr.length; i += 2)
        {
            pointArr[i] = Float.valueOf(arr[i]);
        }

        //gr.drawPoints(pointArr, lastPen);
    }

    /**
     * Draws rectangle
     * @param r
     * @param g
     * @param b
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "DrawRectangle", arguments = { Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawRectangle(int r, int g, int b, int x, int y, int width, int height)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
        gr.drawRect(x, y, x+width, y+width, p);*/
    }

    /**
     * Draws rectangle
     * @param r
     * @param g
     * @param b
     * @param widthP
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "DrawRectangleEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class, Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawRectangleEx(int r, int g, int b, float widthP, int x, int y, int width, int height)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	p.setStrokeWidth(width);
        gr.drawRect(x, y, x+width, y+width, p);*/
    }

    /**
     * Draws rectangle from rectangle store
     * @param r
     * @param g
     * @param b
     */
    @LuaFunction(manual = false, methodName= "DrawRectangleRectCache", arguments = { Integer.class, Integer.class, Integer.class })
    public void DrawRectangleRectCache(int r, int g, int b)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
        gr.drawRect(rectStore1, p);*/
    }

    /**
     * Draws rectangle from rectangle store
     * @param r
     * @param g
     * @param b
     * @param width
     */
    @LuaFunction(manual = false, methodName= "DrawRectangleRectCacheEx", arguments = { Integer.class, Integer.class, Integer.class, Float.class })
    public void DrawRectangleRectCacheEx(int r, int g, int b, float width)
    {
    	/*Paint p = new Paint();
    	p.setColor(Color.rgb(r, g, b));
    	p.setStrokeWidth(width);
        gr.drawRect(rectStore1, p);*/
    }

    /**
     * Draws rectangle from rectangle store with pen cache
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "DrawRectanglePenCache", arguments = { Integer.class, Integer.class, Integer.class, Integer.class })
    public void DrawRectanglePenCache(int x, int y, int width, int height)
    {
        //gr.drawRect(new Rect(x, y, x+width, y+height), lastPen);
    }

    /**
     * Draws rectangle from rectangle store with pen cache
     */
    @LuaFunction(manual = false, methodName="DrawRectanglePenRectCache")
    public void DrawRectanglePenRectCache()
    {
    	//gr.drawRect(rectStore1, lastPen);
    }

    /**
     * Draw string
     * @param s
     * @param f
     * @param size
     * @param style
     * @param x
     * @param y
     * @param valign
     * @param halign
     * @param flags
     */
    @LuaFunction(manual = false, methodName="DrawString", arguments = { String.class, String.class, Float.class, Integer.class, Float.class, Float.class, Integer.class, Integer.class, Integer.class })
    public void DrawString(String s, String f, float size, int style, float x, float y, int valign, int halign, int flags)
    {
    	/*
    	 *      // Summary:
        //     Specifies the text be aligned near the layout. In a left-to-right layout,
        //     the near position is left. In a right-to-left layout, the near position is
        //     right.
        Near = 0,
        //
        // Summary:
        //     Specifies that text is aligned in the center of the layout rectangle.
        Center = 1,
        //
        // Summary:
        //     Specifies that text is aligned far from the origin position of the layout
        //     rectangle. In a left-to-right layout, the far position is right. In a right-to-left
        //     layout, the far position is left.
        		Far = 2,
    	 */
        
        /*Paint p = new Paint(lastBrush);
        switch(halign)
        {
        case 0:
        	p.setTextAlign(Paint.Align.LEFT);
        	break;
        case 1:
        	p.setTextAlign(Paint.Align.CENTER);
        	break;
        case 2:
        	p.setTextAlign(Paint.Align.RIGHT);
        	break;
        }
        
        p.setTextSize(size);*/
    	/*
        Regular = 0,
        Bold = 1,
        Italic = 2,
        Underline = 4,
        Strikeout = 8,
    	 */
        /*int flagsAndroid = 0;
        if((flags & 4) > 0)
        	flagsAndroid |= Paint.STRIKE_THRU_TEXT_FLAG;
        if((flags & 8) > 0)
        	flagsAndroid |= Paint.UNDERLINE_TEXT_FLAG;
        p.setFlags(flagsAndroid);
        
        if((flags & 1) > 0)
    		p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        if((flags & 2) > 0)
        	p.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        if((flags & 3) > 0)
        	p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
     
        gr.drawText(s, x, y, p);*/
    }

    /**
     * Fill ellipse
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "FillEllipse", arguments = { Integer.class, Integer.class, Integer.class, Integer.class })
    public void FillEllipse(int x, int y, int width, int height)
    {
    	/*Paint p = new Paint(lastBrush);
    	p.setStyle(Paint.Style.FILL);
    	gr.drawOval(new RectF(x, y, width, height), p);*/
    }

    /**
     * Fill polygon
     * @param points
     */
    @LuaFunction(manual = false, methodName= "FillPolygon", arguments = { String.class })
    public void FillPolygon(String points)
    {
        String[] arr = points.split(",");
        float[] pointArr = new float[arr.length];
        for (int i = 0; i < arr.length; i += 2)
        {
            pointArr[i] = Float.valueOf(arr[i]);
        }
    	/*Paint p = new Paint(lastBrush);
    	p.setStyle(Paint.Style.FILL);
    	gr.drawPoints(pointArr, p);*/
    }

    /**
     * Fill rectangle
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "FillRectangle", arguments = { Integer.class, Integer.class, Integer.class, Integer.class })
    public void FillRectangle(int x, int y, int width, int height)
    {
    	/*Paint p = new Paint(lastBrush);
    	p.setStyle(Paint.Style.FILL);
    	gr.drawRect(x, y, x+width, y+height, p);*/
    }

    /**
     * Fil region
     */
    @LuaFunction(manual = false, methodName= "FillRegion", arguments = { })
    public void FillRegion()
    {
    	/*Paint p = new Paint(lastBrush);
    	p.setStyle(Paint.Style.FILL);
    	gr.drawRect(rectStore1, p);*/
    }

    /**
     * Clear
     * @param red
     * @param green
     * @param blue
     */
    @LuaFunction(manual = false, methodName= "Clear", arguments = { Integer.class, Integer.class, Integer.class })
    public void Clear(int red, int green, int blue)
    {
    	//gr.drawColor(Color.rgb(red, green, blue));
    }

    /**
     * Set drawing clip
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @LuaFunction(manual = false, methodName= "SetClip", arguments = { Integer.class, Integer.class, Integer.class, Integer.class })
    public void SetClip(int x, int y, int width, int height)
    {
    	//gr.clipRect(x, y, x+width, y+width);
    }
    
    /**
	 * Frees the created object.
	 */
	@LuaFunction(manual = false, methodName = "Free")
	public void Free()
	{
	}

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
		return "LuaGraphics";
	}
}
