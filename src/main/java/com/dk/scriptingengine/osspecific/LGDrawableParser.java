package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaResource;
import com.dk.scriptingengine.LuaStream;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

public class LGDrawableParser
{
    private ArrayList<LGParser.DynamicResource> clearedDirectoryList;
    private HashMap<String, HashMap<String, String>> stateListMap = new HashMap<>();

    public class DrawableReturn
    {
        public String imagePath;
        public String state;
        public boolean tile;
        public String js;
    }

    public static LGDrawableParser GetInstance()
    {
        return LGParser.GetInstance().pDrawable;
    }

    public void Initialize()
    {
        ArrayList<String> directoryList = LuaResource.GetResourceDirectories(Resources.LUA_DRAWABLE_FOLDER);
        clearedDirectoryList = LGParser.GetInstance().Tester(directoryList, Resources.LUA_DRAWABLE_FOLDER);
        clearedDirectoryList.sort(new Comparator<LGParser.DynamicResource>()
        {
            @Override
            public int compare(LGParser.DynamicResource obj1, LGParser.DynamicResource obj2)
            {
                String aData = (String) obj1.data;
                String bData = (String) obj2.data;
                if(aData.equals(bData))
                    return 0;
                else if(aData.length() > bData.length())
                    return -1;
                else
                    return 1;

            }
        });
    }

    public DrawableReturn ParseDrawable(String drawable)
    {
        return ParseDrawable(drawable, 0);
    }

    private DrawableReturn ParseDrawable(String drawable, int tileMode)
    {
        String[] arr = drawable.split("/");
        String retVal = null;
        if(arr[0].contains("drawable"))
        {
            if(arr.length > 1)
            {
                String name = arr[1];
                for(LGParser.DynamicResource dr : clearedDirectoryList)
                {
                    LuaStream stream = null;
                    String path = LuaEngine.getInstance().GetUIRoot() + "/" + dr.data;
                    String file = name + ".png";
                    stream = LuaResource.GetResource(path, file);
                    if(stream == null || !stream.HasStream())
                    {
                        file = name + ".jpg";
                        stream = LuaResource.GetResource(path, file);
                        if(stream == null || !stream.HasStream())
                        {
                            file = name + ".gif";
                            stream = LuaResource.GetResource(path, file);
                            if(stream == null || !stream.HasStream())
                            {
                                DrawableReturn drRet = ParseXML(name + ".xml", tileMode);
                                if(drRet != null)
                                    return drRet;
                            }
                        }
                    }
                    if(stream != null && stream.HasStream())
                    {
                        retVal = path + "/" + file;
                    }
                    if(retVal != null)
                        break;
                }
            }
            DrawableReturn dr = new DrawableReturn();
            if(tileMode > 0)
                dr.tile = true;
            dr.imagePath = retVal;
            return dr;
        }
        DrawableReturn dr = new DrawableReturn();
        dr.imagePath = "";
        return dr;
    }

    private DrawableReturn ParseXML(String fileName, int tileMode)
    {
        DrawableReturn ret = null;

        for(LGParser.DynamicResource dr : clearedDirectoryList)
        {
            LuaStream stream = null;
            String path = LuaEngine.getInstance().GetUIRoot() + dr.data;
            stream = LuaResource.GetResource(path, fileName);
            if(stream == null)
                continue;

            SAXBuilder saxBuilder = new SAXBuilder();
            try
            {
                Document document = saxBuilder.build((InputStream) stream.GetStreamInternal());
                Element root = document.getRootElement();
                if(root.getName().equals("bitmap")
                        || root.getName().equals("nine-patch"))
                    return ParseBitmap(root);
                else if(root.getName().equals("layer-list"))
                    return ParseLayer(root);
                else if(root.getName().equals("selector"))
                    return ParseStateList(root);
                else if(root.getName().equals("shape"))
                    return ParseShape(root);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return ret;
    }

    private DrawableReturn ParseBitmap(Element root)
    {
        List<Attribute> attrs = root.getAttributes();

        String imgPath = null;
        String tileMode = null;

        for(Attribute node : attrs)
        {
            if(node.getName().equals("android:src"))
                imgPath = node.getValue();
            else if(node.getName().equals("android:tileMode"))
                tileMode = node.getValue();
        }

        int tileModeInt = 0;
        if(tileMode.equals("repeat"))
            tileModeInt = 1;
        return ParseDrawable(imgPath, tileModeInt);
    }

    private DrawableReturn ParseLayer(Element root)
    {
        ArrayList<DrawableReturn> imgArr = new ArrayList<>();
        int maxWidth = 0, maxHeight = 0;
        for(Element child : root.getChildren())
        {
            int left = 0,top = 0,right = 0,bottom = 0;
            DrawableReturn ldr = null;
            String name = child.getName();
            if(name.equals("item"))
            {
                if(child.getChildren().size() > 0)
                {
                    Element childItem = child.getChildren().get(0);
                    ldr = ParseBitmap(childItem);
                }
                for(Attribute node : child.getAttributes())
                {
                    String attr = node.getName();
                    if(attr.equals("android:drawable"))
                    {
                        ldr = ParseDrawable(node.getValue());
                    }
                    else if(attr.equals("android:id"))
                    {

                    }
                    else if(attr.equals("android:top"))
                    {
                        top = LGDimensionParser.GetInstance().GetDimension(node.getValue());
                    }
                    else if(attr.equals("android:right"))
                    {
                        right = LGDimensionParser.GetInstance().GetDimension(node.getValue());
                    }
                    else if(attr.equals("android:bottom"))
                    {
                        bottom = LGDimensionParser.GetInstance().GetDimension(node.getValue());
                    }
                    else if(attr.equals("android:left"))
                    {
                        left = LGDimensionParser.GetInstance().GetDimension(node.getValue());
                    }

                }
            }
            /*if(ldr != null && ldr.imagePath != null)
            {
                imgArr.add(ldr);
                if(ldr.imagePath != null)
                {
                    if(ldr.imagePath)
                }
            }*/
        }

        if(imgArr.size() != root.getChildren().size())
            return null;

        //Create javascript code to embed here;
        return null;
    }

    private DrawableReturn ParseStateList(Element root)
    {
        HashMap<String, String> stateList = new HashMap<>();
        for(Element child : root.getChildren())
        {
            int left,top,right,bottom = 0;
            DrawableReturn ldr = null;
            String name = child.getName();
            if(name.equals("item"))
            {
                if (child.getChildren().size() > 0)
                {
                    Element childItem = child.getChildren().get(0);
                    ldr = ParseBitmap(childItem);
                }
                for (Attribute node : child.getAttributes())
                {
                    String attr = node.getName();
                    if(attr.equals("android:drawable"))
                    {
                        ldr = ParseDrawable(node.getValue());
                    }
                    else if(attr.equals("android:state_pressed"))
                    {
                        if(ldr != null)
                        {
                            stateList.put("active", ldr.imagePath);
                        }
                    }
                    else if(attr.equals("android:state_focused"))
                    {
                        if(ldr != null)
                        {
                            stateList.put("focus", ldr.imagePath);
                        }
                    }
                    else if(attr.equals("android:state_hovered"))
                    {
                        if(ldr != null)
                        {
                            stateList.put("hover", ldr.imagePath);
                        }
                    }
                    else if(attr.equals("android:state_selected"))
                    {
                        if(ldr != null)
                        {
                            stateList.put("checked", ldr.imagePath);
                        }
                    }
                    else if(attr.equals("android:state_checkable"))
                    {
                        if(ldr != null)
                        {
                            /*stateList.put("active", ldr.imagePath);
                            stateList.put("visited", ldr.imagePath);
                            stateList.put("checked", ldr.imagePath);*/
                        }
                    }
                    else if(attr.equals("android:state_checked"))
                    {
                        if(ldr != null)
                        {
                            stateList.put("checked", ldr.imagePath);
                        }
                    }
                    else if(attr.equals("android:state_enabled"))
                    {
                        if(ldr != null)
                        {
                            stateList.put("enabled", ldr.imagePath);
                        }
                    }
                    else if(attr.equals("android:state_activated"))
                    {
                        if(ldr != null)
                        {
                            stateList.put("active", ldr.imagePath);;
                        }
                    }
                    else if(attr.equals("android:state_window_focused"))
                    {
                        if(ldr != null)
                        {
                            stateList.put("focus", ldr.imagePath);
                        }
                    }
                }
            }
        }

        stateListMap.put(root.getName(), stateList);
        DrawableReturn ret = new DrawableReturn();
        ret.imagePath = null;
        ret.state = root.getName();
        return ret;
    }

    private DrawableReturn ParseShape(Element root)
    {
        return null;
    }
}
