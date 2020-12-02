package android.widget;

import com.dk.scriptingengine.LuaColor;
import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaEngine.GuiEvents;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.DisplayMetrics;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.DynamicByteBuf;
import com.dk.scriptingengine.osspecific.LayoutServer;
import com.dk.scriptingengine.osspecific.Surface;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@LuaClass(className = "LGAdapterView")
public class LGAdapterView /*extends BaseAdapter*/ implements /*SectionIndexer,*/ LuaInterface
{
	public Map<String, LGAdapterView> sections = new LinkedHashMap<String,LGAdapterView>();
	public final List<LGAdapterView> headers;
	public ArrayList<String> sectionsValues = new ArrayList<String>();
	public LinkedHashMap<Integer, Object> values = new LinkedHashMap<Integer, Object>();
	public final static int TYPE_SECTION_HEADER = 0;
	
	private LuaContext mLc;
	private String id;
	private LuaTranslator ltItemChanged = null;
	private boolean itemChangeRegistered = false;
	private LGListView parent = null;
	private LGView lastSelectedView = null;
	private int lastPosition = -1;
	
    /**
	 * Creates LGAdapterView Object From Lua.
	 * @return LGAdapterView
	 */
    @LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class, String.class }, self = LGAdapterView.class)
    public static LGAdapterView Create(LuaContext lc, String id)
    {
    	LGAdapterView lgav = new LGAdapterView(lc, id);
    	return lgav;
    }
    
	/**
	 * (Ignore)
	 */
    public LGAdapterView(LuaContext lc, String id) 
    {
    	//mInflater = (LayoutInflater)lc.GetContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	mLc = lc;
    	this.id = id;
    	this.headers = null; 
    }
	
    /**
     * (Ignore)
     */
	public int getCount()
	{
		if(sections.size() > 0)
		{
			int total = 0;
			for(LGAdapterView adapter : this.sections.values())
				total += adapter.getCount() + 1;
			return total;
		}
		else
			return values.size();
	}

	/**
	 * (Ignore)
	 */
	public ArrayList<Object> getItems()
	{
		ArrayList<Object> lst = new ArrayList<>();
		if(sections.size() > 0)
		{
			for(Map.Entry<String, LGAdapterView> entry : sections.entrySet())
			{
				for(int i = 0; i < entry.getValue().getCount(); i++)
					lst.add(entry.getValue().getItem(i));
			}
		}
		else
		{
			for(int i = 0; i < getCount(); i++)
				lst.add(getItem(i));
		}
		
		return lst;
	}

	/**
     * (Ignore)
     */
	public Object getItem(int position)
	{
		if(sections.size() > 0)
		{
			for(Object section : this.sections.keySet()) 
			{
				LGAdapterView adapter = sections.get(section);
				int size = adapter.getCount() + 1;
	
				// check if position inside this section
				if(position == 0) return section;
				if(position < size) return adapter.getItem(position - 1);
	
				// otherwise jump into next section
				position -= size;
			}
		}
		else
			return (Object) values.values().toArray()[position];
		return null;
	}

	/**
     * (Ignore)
     */
	public long getItemId(int position)
	{
		return position;
	}

	/**
     * (Ignore)
     */
	public LGView getView(int position, LGView convertView, LGViewGroup parent)
	{
		if(sections.size() > 0)
		{
			int sectionnum = 0;
			for(Object section : this.sections.keySet()) 
			{
				LGAdapterView adapter = sections.get(section);
				int size = adapter.getCount() + 1;
	
				// check if position inside this section
				if(position == 0) return headers.get(sectionnum).getView(sectionnum, convertView, parent);
				if(position < size) return adapter.getView(position - 1, convertView, parent);
	
				// otherwise jump into next section
				position -= size;
				sectionnum++;
			}
		}
		else
		{
			LGView v = (LGView)convertView;
			if(v != null)
				v.removeAllViews();
			v = (LGView)LuaEngine.getInstance().OnGuiEvent(this, GuiEvents.GUI_EVENT_ADAPTERVIEW, parent, position, getItem(position), v, mLc);
			//v.removeAllViews();
			
			int rot = DisplayMetrics.GetRotation(mLc.GetContext());
			if(DisplayMetrics.isTablet && rot == Surface.ROTATION_90 && position == lastPosition)
			{
				if(lastSelectedView == null)
					lastSelectedView  = v;	
				v.setBackgroundColor(LuaColor.ColorToHex(this.parent.GetSelectedCellColor()));
			}
			else
				v.setBackgroundColor(LuaColor.TRANSPARENT);

			//TODO:This
			/*LayoutParams lps = v.getLayoutParams();
			v.setLayoutParams(new AbsListView.LayoutParams(lps.width, lps.height));
			//v.view.setLayoutParams(new LinearLayout.LayoutParams(lps.width, lps.height));
			v.addView(v.view);
			v.PrintDescription("");
	        final int pos = position;
	        final LGListView par = this.par;
	        par.setClickable(true);
	        //Fix for nonvisib
	        final LGView vf = v;
	        v.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					int rot = DisplayMetrics.GetRotation(mLc.GetContext());
					if(DisplayMetrics.isTablet && rot == Surface.ROTATION_90)
					{
						if(lastSelectedView != null)
							lastSelectedView.setBackgroundColor(Color.TRANSPARENT);
						v.setBackgroundColor(par.GetSelectedCellColor());
						lastSelectedView = vf;
						lastPosition = pos;
					}
					
					if(ltItemChanged != null)
						ltItemChanged.CallIn(par, par.GetDetailFragment(), pos, getItem(pos));
					
				}
			});*/
	        return v;
		}
		return null;
	}
	
	/**
     * (Ignore)
     */
	public int getPositionForSection(int section)
	{
		if(section > sections.size())
			return 0;
		
		int total = 1;
		int sectionCount = 0;
		for(LGAdapterView adapter : this.sections.values())
		{
			sectionCount++;
			total += adapter.getCount() + 1;
			if(sectionCount >= section)
				return total;
		}
		return sectionCount;
	}

	/**
     * (Ignore)
     */
	public int getSectionForPosition(int position)
	{
		int total = 1;
		int sectionCount = 0;		
		for(LGAdapterView adapter : this.sections.values())
		{
			if(position <= total)
				return sectionCount;
			total += adapter.getCount() + 1;
			sectionCount++;
		}
		return sectionCount;
	}

	public LuaTranslator getItemSelected()
	{
		return ltItemChanged;
	}

	/**
     * (Ignore)
     */
	public Object[] getSections()
	{
		return sectionsValues.toArray();
	}
	
	/**
     * (Ignore)
     */
	public void DoExternalClick(int pos, LGView view)
	{
		if(ltItemChanged != null)
		{
			ltItemChanged.CallIn(parent, pos, getItem(pos));
		}
	}

	public LGListView getParent()
	{
		return parent;
	}

	public void setParent(LGListView parent)
	{
		this.parent = parent;
		if(!itemChangeRegistered && ltItemChanged != null)
		{
			itemChangeRegistered = true;
			String id = parent.GetId() + "ItemSelected";
			DynamicByteBuf buf = DynamicByteBuf.create();
			buf.writeInt(LayoutServer.SMSG_REGISTER_EVENT);
			buf.writeInt(LayoutServer.EVENT_TYPE_METHOD);
			buf.writeInt(parent.GetId().length());
			buf.writeString(parent.GetId());
			buf.writeInt(id.length());
			buf.writeString(id);
			buf.writeInt("ItemSelected".length());
			buf.writeString("ItemSelected");

			LayoutServer.getInstance().sendPacket(mLc.GetContext().getClient(), buf);
		}
	}

	/**
     * Add section
     * @param header of section
     * @param id of LGAdapterView
     */
	@LuaFunction(manual = false, methodName = "AddSection", arguments = { String.class, String.class })
	public LGAdapterView AddSection(String header, String id)
	{
		LGAdapterView lgav = new LGAdapterView(mLc, id);
		sectionsValues.add(header);
		sections.put(header, lgav);
		return lgav;
	}
	
	/**
	 * Remove section
	 * @param header value
	 */
	@LuaFunction(manual = false, methodName = "RemoveSection", arguments = { String.class })
	public void RemoveSection(String header)
	{
		sectionsValues.remove(header);
		sections.remove(header);
	}
	
	/**
	 * Add Value to adapter
	 * @param id of value
	 * @param value
	 */
	@LuaFunction(manual = false, methodName = "AddValue", arguments = { Integer.class, Object.class })
	public void AddValue(Integer id, Object value)
	{
		values.put(id, value);
		//notifyDataSetChanged();
	}
	
	/**
	 * Remove Value from adapter
	 * @param id of value
	 */
	@LuaFunction(manual = false, methodName = "RemoveValue", arguments = { Integer.class })
	public void RemoveValue(Integer id)
	{
		values.remove(id);
		//notifyDataSetChanged();
	}
	
	/**
	 * Remove all values from adapter
	 */
	@LuaFunction(manual = false, methodName = "RemoveAllValues")
	public void RemoveAllValues()
	{
		values.clear();
		//notifyDataSetChanged();
	}

	/**
	 * Add section
	 * @return LuaContext
	 */
	@LuaFunction(manual = false, methodName = "GetContext")
	public LuaContext GetContext()
	{
		return mLc;
	}
	
	/**
	 * (Ignore)
	 */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class} )
	public void RegisterEventFunction(String var, LuaTranslator lt)
	{
		if(var.compareTo("ItemSelected") == 0)
		{
			ltItemChanged = lt;

			if(parent == null || parent.GetId() == null)
			{
				itemChangeRegistered = false;
				return;
			}

			itemChangeRegistered = true;
			String id = parent.GetId() + "ItemSelected";
			DynamicByteBuf buf = DynamicByteBuf.create();
			buf.writeInt(LayoutServer.SMSG_REGISTER_EVENT);
			buf.writeInt(LayoutServer.EVENT_TYPE_METHOD);
			buf.writeInt(parent.GetId().length());
			buf.writeString(parent.GetId());
			buf.writeInt(id.length());
			buf.writeString(id);
			buf.writeInt("ItemSelected".length());
			buf.writeString("ItemSelected");

			LayoutServer.getInstance().sendPacket(mLc.GetContext().getClient(), buf);
		}
	}

	/**
	 * (Ignore)
	 */
	@Override
	public String GetId()
	{
		if (id == null)
			return "LGAdapterView";
		return id;
	}
}
