package android.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.dk.scriptingengine.LuaColor;
import com.dk.scriptingengine.LuaMapCircle;
import com.dk.scriptingengine.LuaMapImage;
import com.dk.scriptingengine.LuaMapMarker;
import com.dk.scriptingengine.LuaMapPolygon;
import com.dk.scriptingengine.LuaPoint;
import com.dk.scriptingengine.LuaTranslator;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.luagui.LuaContext;
import com.dk.scriptingengine.osspecific.Context;
import com.dk.scriptingengine.osspecific.LatLng;
import com.dk.scriptingengine.osspecific.LayoutServer;
import com.dk.scriptingengine.osspecific.socketobjects.JSModel;
import com.dk.scriptingengine.osspecific.socketobjects.MethodModel;

@LuaClass(className = "LGMapView")
public class LGMapView extends LGView implements LuaInterface
{
	private LuaContext context;
	private String apiKey;
	private int viewIdToSet = 0;

	private LatLng center = new LatLng(10.0f, 10.0f);
	private ArrayList<LuaMapMarker> markers = new ArrayList<>();
	private ArrayList<LuaMapImage> images = new ArrayList<>();
	private ArrayList<LuaMapCircle> circles = new ArrayList<>();
	private ArrayList<LuaMapPolygon> polygons = new ArrayList<>();

	private String modelCenter;
	private String modelZoom;
	private String modelMarkers;
	private String modelImages;
	private String modelCircles;
	private String modelPolygons;
	
	/**
	 * Creates LGMapView Object From Lua.
	 * @param lc
	 * @param apikey
	 * @return LGMapView
	 */
	@LuaFunction(manual = false, methodName = "Create", arguments = { LuaContext.class, String.class }, self = LGMapView.class)
	public static LGMapView Create(LuaContext lc, String apikey)
	{
		LGMapView mapView = new LGMapView(lc);
		mapView.SetApiKey(apikey);
		return mapView;
	}

	/**
	 * (Ignore)
	 */
	private void SetApiKey(String apikey)
	{
		apiKey = apikey;
	}
	
	/**
	 * (Ignore)
	 */
	public LGMapView(LuaContext context)
	{
		super(context.GetContext());
		this.lc = context;
	}

	/**
	 * (Ignore)
	 */
	public LGMapView(LuaContext context, String luaId)
	{
		super(context.GetContext(), luaId);
		this.lc = context;
	}
    
	/**
	 * (Ignore)
	 */
    public void Setup(Context context)
    {
    	//TODO:Map
    	//view = new FrameLayout(context);
    	CommonSetup(context);
    }
    
    /**
     * (Ignore)
     */
    public void CommonSetup(Context context)
    {
    	
    }
    
	/**
	 * (Ignore) 
	 */	
    @Override
    public void onCreate()
    {
    	super.onCreate();
		if(luaId != null)
		{
			modelCenter = luaId + "Center";
			modelZoom = luaId + "Zoom";
			modelMarkers = luaId + "Markers";
			modelImages = luaId + "Images";
			modelCircles = luaId + "Circles";
			modelPolygons = luaId + "Polygons";
			html("<google-map :center=\"" + modelCenter + "\" :zoom=\"" + modelZoom + "\">" +
					"<google-marker v-for=\"m in " + modelMarkers + "\" :position=\"m.position\" :clickable=\"m.clickable\" :draggable=\"m.draggable\" "
					+ ":icon=\"m.icon\" :title=\"m.title\" :opacity=\"m.opacity\" :visible=\"m.visible\" "
					+ "@click=\"" + luaId + "Click" + "\"></google-marker>"
					+ "<google-map-ground-overlay v-for=\"m in " + modelImages + "\" :source=\"m.image\" :bounds=\"m.bounds\" :opacity=\"m.opacity\" "
                    + "@click=\"" + luaId + "Click" + "\"></google-map-ground-overlay>"
                    + "<google-map-circle v-for=\"m in " + modelCircles + "\" :center=\"m.center\" :radius=\"m.radius\"></google-map-circle>"
                    + "<google-map-polygon v-for=\"m in " + modelPolygons + "\" :path=\"m.path\"></google-map-polygon>"
					+ "</google-map>");
			jsModel.add(new JSModel(modelCenter, center));
			jsModel.add(new JSModel(modelZoom, 8));
			jsModel.add(new JSModel(modelMarkers, markers));
			jsModel.add(new JSModel(modelImages, images));
			jsModel.add(new JSModel(modelCircles, circles));
			jsModel.add(new JSModel(modelPolygons, polygons));
			methodModel.add(new MethodModel(luaId, "Click"));
		}
		else
			html("<google-map></google-map>");

        jsCoreModel = "Vue.use(VueGoogleMaps, {" +
                "      load: {" +
                "        key: 'AIzaSyBzlLYISGjL_ovJwAehh6ydhB56fCCpPQw'" +
                "      }," +
                "      installComponents: false," +
                "    });" +
                "    Vue.component('google-map', VueGoogleMaps.Map);" +
                "    Vue.component('google-map-circle', VueGoogleMaps.Circle);" +
                "    Vue.component('google-map-polygon', VueGoogleMaps.Polygon);" +
                "    Vue.component('google-map-marker', VueGoogleMaps.Marker);" +
				"    Vue.component('google-map-ground-overlay', VueGoogleMaps.MapElementFactory({\n" +
				"        mappedProps: {\n" +
				"          'opacity': {}\n" +
				"        },\n" +
				"        props: {\n" +
				"          'source': {type: String},\n" +
				"          'bounds': {type: Object},\n" +
				"        },\n" +
				"        events: ['click', 'dblclick'],\n" +
				"        name: 'groundOverlay',\n" +
				"        ctr: () => google.maps.GroundOverlay,\n" +
				"        ctrArgs: (options, {source, bounds}) => [source, bounds, options],\n" +
				"      }));";
    }
    
    /**
	 * Adds circle to map based on parameters
	 * @param geoLoc LuaPoint
	 * @param radius
	 * @param strokeColor LuaColor
	 * @param fillColor LuaColor
	 * @return LuaMapCircle
	 */
    @LuaFunction(manual = false, methodName = "AddCircle", arguments = { LuaPoint.class, Double.class, LuaColor.class, LuaColor.class })
    public LuaMapCircle AddCircle(LuaPoint geoLoc, double radius, LuaColor strokeColor, LuaColor fillColor)
    {
        LuaMapCircle lmc = new LuaMapCircle();
        lmc.SetCenter(geoLoc);
        lmc.SetRadius(radius);
        lmc.SetStrokeColor(strokeColor);
        lmc.SetFillColor(fillColor);

        circles.add(lmc);

        LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
                new String[]{ modelCircles },
                new Integer[] { LayoutServer.TYPE_OBJECT },
                new Object[] { circles });

        return lmc;
    }
    
    /**
	 * Adds marker to map
	 * @param geoLoc LuaPoint
	 * @param path path of the icon (can be null)
	 * @param icon filename of to icon (can be null)
	 * @return LuaMapMarker
	 */
    @LuaFunction(manual = false, methodName = "AddMarker", arguments = { LuaPoint.class, String.class, String.class })
    public LuaMapMarker AddMarker(LuaPoint geoLoc, String path, String icon)
	{
		LuaMapMarker lmm = new LuaMapMarker(geoLoc);
		lmm.icon = path + icon;

		markers.add(lmm);

		LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
				new String[]{ modelMarkers },
				new Integer[] { LayoutServer.TYPE_OBJECT },
				new Object[] { markers });

		return lmm;
    }
    
    /**
	 * Adds marker to map with extended options
	 * @param geoLoc LuaPoint
	 * @param path path of the icon (can be null)
	 * @param icon filename of to icon (can be null)
	 * @param anchor LuaPoint
	 * @return LuaMapMarker
	 */
    @LuaFunction(manual = false, methodName = "AddMarkerEx", arguments = { LuaPoint.class, String.class, String.class, LuaPoint.class })
    public LuaMapMarker AddMarkerEx(LuaPoint geoLoc, String path, String icon, LuaPoint anchor)
    {
		LuaMapMarker lmm = new LuaMapMarker(geoLoc);
		lmm.icon = path + icon;

		markers.add(lmm);

		LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
				new String[]{ modelMarkers },
				new Integer[] { LayoutServer.TYPE_OBJECT },
				new Object[] { markers });

		return lmm;
    }
    
    /**
	 * Adds image to map
	 * @param geoPoint LuaPoint
	 * @param path path of the icon (can be null)
	 * @param icon filename of to icon (can be null)
	 * @param width
	 * @return LuaMapImage
	 */
    public LuaMapImage AddImage(LuaPoint geoPoint, String path, String icon, float width)
    {
    	LuaMapImage lmi = new LuaMapImage(path + icon);
    	lmi.SetPosition(geoPoint);
    	lmi.SetDimensions(width);

    	this.images.add(lmi);

        LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
                new String[]{ modelImages },
                new Integer[] { LayoutServer.TYPE_OBJECT },
                new Object[] { images });

    	return lmi;
    }
    
    /**
	 * Adds marker to map with extended options
	 * @param points
	 * @param strokeColor LuaColor
	 * @param fillColor LuaColor
	 * @return LuaMapPolygon
	 */
    public LuaMapPolygon AddPolygon(HashMap<Integer, LuaPoint> points, LuaColor strokeColor, LuaColor fillColor)
    {
        LuaMapPolygon lmp = new LuaMapPolygon();
        for(Map.Entry<Integer, LuaPoint> entry : points.entrySet())
        {
            lmp.path.add(new LatLng(entry.getValue()));
        }
        lmp.SetStrokeColor(strokeColor);
        lmp.SetFillColor(fillColor);
        polygons.add(lmp);

        LayoutServer.getInstance().notifyDataChanged(context.GetContext().getClient(),
                new String[]{ modelPolygons },
                new Integer[] { LayoutServer.TYPE_OBJECT },
                new Object[] { polygons });

        return lmp;
    }

    /**
     * Registers Event for
     * "Click" happens when user clicks on view
     */
	@Override
	@LuaFunction(manual = false, methodName = "RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, final LuaTranslator lt) 
	{
		super.RegisterEventFunction(var, lt);
	}
}
