package com.dk.scriptingengine.osspecific;

import com.dk.scriptingengine.LuaEngine;
import com.dk.scriptingengine.LuaResource;
import com.dk.scriptingengine.LuaStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;

public class LGParser
{
    public static final int MATCH_ID_LANGUAGE = -1;
    public static final int MATCH_ID_LAYOUT_DIRECTION = 0;
    public static final int MATCH_ID_SMALLEST_WIDTH = 1;
    public static final int MATCH_ID_AVAILABLE_WIDTH = 2;
    public static final int MATCH_ID_AVAILABLE_HEIGHT = 3;
    public static final int MATCH_ID_SCREEN_SIZE = 4;
    public static final int MATCH_ID_SCREEN_ORIENTATION = 5;
    public static final int MATCH_ID_SCREEN_PIXEL_DENSITY = 6;
    public static final int MATCH_ID_VERSION = 7;
    public static final int MATCH_ID_COUNT = 9;

    public static int ORIENTATION_PORTRAIT = 0x1;
    public static int ORIENTATION_LANDSCAPE = 0x2;

    public static String ORIENTATION_PORTRAIT_S = "1";
    public static String ORIENTATION_LANDSCAPE_S = "2";

    public class DynamicResource
    {
        int orientation;
        Object data;
    }

    private XmlPullParserFactory xmlPullParserFactory;

    LGLayoutParser pLayout;
    LGDrawableParser pDrawable;
    LGDimensionParser pDimen;
    LGColorParser pColor;
    LGStringParser pString;

    ArrayList<ArrayList<String>> MatchStringStart = new ArrayList<>();
    ArrayList<ArrayList<String>> MatchStringEnd = new ArrayList<>();

    private static LGParser sInstance;
    public static LGParser GetInstance()
    {
        if(sInstance == null)
            sInstance = new LGParser();
        return sInstance;
    }

    private LGParser()
    {
    }

    public void Initialize()
    {
        pLayout = new LGLayoutParser();
        pDrawable = new LGDrawableParser();
        pDimen = new LGDimensionParser();
        pColor = new LGColorParser();
        pString = new LGStringParser();

        ArrayList<String> lst = new ArrayList<>();
        lst.add("ld");
        MatchStringStart.add(lst);
        lst = new ArrayList<>();
        lst.add("sw");
        MatchStringStart.add(lst);
        lst = new ArrayList<>();
        lst.add("w");
        MatchStringStart.add(lst);
        lst = new ArrayList<>();
        lst.add("h");
        MatchStringStart.add(lst);
        lst = new ArrayList<>();
        lst.add("small");
        lst.add("normal");
        lst.add("large");
        lst.add("xlarge");
        MatchStringStart.add(lst);
        lst = new ArrayList<>();
        lst.add("port");
        lst.add("land");
        MatchStringStart.add(lst);
        lst = new ArrayList<>();
        lst.add("ldpi");
        lst.add("mdpi");
        lst.add("hdpi");
        lst.add("xhdpi");
        lst.add("xxhdpi");
        lst.add("xxxhdpi");
        lst.add("nodpi");
        MatchStringStart.add(lst);
        lst = new ArrayList<>();
        lst.add("v");
        MatchStringStart.add(lst);
        
        lst = new ArrayList<>();
        MatchStringEnd.add(lst);
        lst = new ArrayList<>();
        lst.add("dp");
        MatchStringEnd.add(lst);
        lst = new ArrayList<>();
        lst.add("dp");
        MatchStringEnd.add(lst);
        lst = new ArrayList<>();
        lst.add("dp");
        MatchStringEnd.add(lst);
        lst = new ArrayList<>();
        MatchStringEnd.add(lst);
        lst = new ArrayList<>();
        MatchStringEnd.add(lst);
        lst = new ArrayList<>();
        MatchStringEnd.add(lst);

        ParseValues();
        pDrawable.Initialize();
        pLayout.Initialize();
    }

    public void ParseValues()
    {
        ArrayList<String> directoryList = LuaResource.GetResourceDirectories(Resources.LUA_VALUES_FOLDER);
        ArrayList<DynamicResource> clearedDirectoryList = Tester(directoryList, Resources.LUA_VALUES_FOLDER);
        for(DynamicResource dr : clearedDirectoryList)
        {
            String path = LuaEngine.getInstance().GetUIRoot() + "/" + (String) dr.data;
            ArrayList<String> files = LuaResource.GetResourceFiles(path);
            for(String file : files)
            {
                LuaStream ls = LuaResource.GetResource(path, file);
                if(ls.HasStream())
                {
                    XmlPullParser parse;
                    try
                    {
                        parse = getXmlPullParserFactory().newPullParser();
                        parse.setInput((InputStream) ls.GetStreamInternal(), null);

                        Stack<StringBuffer> data = new Stack<StringBuffer>();
                        int evt = parse.getEventType();
                        boolean rootFound = false;
                        boolean rootTextFound = false;
                        String name = "";
                        HashMap<String, String> atts = new HashMap<>();
                        while (evt != XmlPullParser.END_DOCUMENT) {
                            switch (evt) {
                            case XmlPullParser.START_DOCUMENT:
                                data.clear();
                                break;
                            case XmlPullParser.START_TAG:
                                data.push(new StringBuffer());
                                name = parse.getName();
                                if(name.equals("resources"))
                                    rootFound = true;
                                else
                                {
                                    for (int i = 0; i < parse.getAttributeCount(); i++)
                                    {
                                        if (parse.getAttributePrefix(i) == null)
                                            atts.put(parse.getAttributeName(i), parse.getAttributeValue(i));
                                        else
                                            atts.put(parse.getAttributePrefix(i) + ":" + parse.getAttributeName(i), parse.getAttributeValue(i));
                                    }
                                }
                                break;
                            case XmlPullParser.TEXT:
                                data.peek().append(parse.getText());
                                if(rootFound)
                                {
                                    if(!rootTextFound)
                                        rootTextFound = true;
                                    else
                                    {
                                        if(name.equals("color"))
                                        {
                                            pColor.ParseXML(dr.orientation, atts, parse.getText());
                                        }
                                        else if(name.equals("dimen"))
                                        {
                                            pDimen.ParseXML(dr.orientation, atts, parse.getText());
                                        }
                                        else if(name.equals("string"))
                                        {
                                            pString.ParseXML(dr.orientation, atts, parse.getText());
                                        }
                                    }
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                data.pop();
                                name = "";
                                atts.clear();
                                break;
                            }
                            evt = parse.next();
                        }
                    }
                    catch (Exception ex)
                    {

                    }
                }
            }
        }
    }

    public ArrayList<DynamicResource> Tester(ArrayList<String> directoryList, String directoryType)
    {
        ArrayList<DynamicResource> clearedDirectoryList = new ArrayList<>();
        for(String dirName : directoryList)
        {
            if(dirName.equals(directoryType))
            {
                DynamicResource dr = new DynamicResource();
                dr.orientation = ORIENTATION_PORTRAIT | ORIENTATION_LANDSCAPE;
                dr.data = dirName;
                clearedDirectoryList.add(dr);
            }
            else
            {
                String[] dirResourceTypes = dirName.split("-");
                int count = 0;
                Ref<Boolean> result = new Ref<Boolean>(false);
                int orientation = ORIENTATION_PORTRAIT | ORIENTATION_LANDSCAPE;
                for(String toMatch : dirResourceTypes)
                {
                    if(toMatch.equals(directoryType))
                        continue;
                    result.ref = false;
                    count = Matcher(count, toMatch, result);
                    if(!result.ref)
                    {
                        String language = Locale.getDefault().getLanguage();
                        String replaced = language.replace("-", "-r");
                        String[] langSplit = language.split("-");
                        if(toMatch.equals(langSplit[0]) || dirName.contains(replaced))
                        {
                            result.ref = true;
                            count = MATCH_ID_LANGUAGE;
                        }
                    }
                    else
                    {
                        switch (count)
                        {
                            case MATCH_ID_LAYOUT_DIRECTION:
                            {

                            }break;
                            case MATCH_ID_SMALLEST_WIDTH:
                            {

                            }break;
                            case MATCH_ID_AVAILABLE_WIDTH:
                            {

                            }break;
                            case MATCH_ID_AVAILABLE_HEIGHT:
                            {

                            }break;
                            case MATCH_ID_SCREEN_SIZE:
                            {

                            }break;
                            case MATCH_ID_SCREEN_ORIENTATION:
                            {

                            }break;
                            case MATCH_ID_SCREEN_PIXEL_DENSITY:
                            {

                            }break;
                            case MATCH_ID_VERSION:
                            {

                            }break;
                        }
                    }
                }
                if(result.ref)
                {
                    DynamicResource dr = new DynamicResource();
                    dr.orientation = orientation;
                    dr.data = dirName;
                    clearedDirectoryList.add(dr);
                }
            }
        }

        return clearedDirectoryList;
    }

    public int Matcher(int count, String toMatch, Ref<Boolean> result)
    {
        boolean found = false;
        int lastCount = 0;
        for(int i = count; i < MatchStringStart.size(); i++)
        {
            lastCount = i;
            ArrayList<String> matchList = MatchStringStart.get(i);
            for(int j = 0; j < matchList.size(); j++)
            {
                String s = matchList.get(j);
                if(toMatch.startsWith(s))
                {
                    ArrayList<String> matchListEnd = MatchStringEnd.get(i);
                    if(matchListEnd.size() == 0)
                    {
                        found = true;
                        break;
                    }
                    else
                    {
                        String es = matchListEnd.get(j);
                        if(toMatch.endsWith(es))
                        {
                            found = true;
                            break;
                        }
                    }
                }
            }
            if(found)
                break;
        }
        result.ref = found;
        return lastCount;
    }

    public XmlPullParserFactory getXmlPullParserFactory() throws XmlPullParserException
    {
        if (xmlPullParserFactory == null)
        {
            xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParserFactory.setNamespaceAware(true);
        }

        return xmlPullParserFactory;
    }
}
