package com.dk.scriptingengine.osspecific.socketobjects;

public class CMSGClientData
{
    private String name;
    private String codename;
    private String version;
    private String platform;
    private String javaEnabled;
    private String screenWidth;
    private String screenHeight;
    private String href;
    private String os;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCodename()
    {
        return codename;
    }

    public void setCodename(String codename)
    {
        this.codename = codename;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getPlatform()
    {
        return platform;
    }

    public void setPlatform(String platform)
    {
        this.platform = platform;
    }

    public String getJavaEnabled()
    {
        return javaEnabled;
    }

    public void setJavaEnabled(String javaEnabled)
    {
        this.javaEnabled = javaEnabled;
    }

    public String getScreenWidth()
    {
        return screenWidth;
    }

    public void setScreenWidth(String screenWidth)
    {
        this.screenWidth = screenWidth;
    }

    public String getScreenHeight()
    {
        return screenHeight;
    }

    public void setScreenHeight(String screenHeight)
    {
        this.screenHeight = screenHeight;
    }

    public String getHref()
    {
        return href;
    }

    public void setHref(String href)
    {
        this.href = href;
    }

    public String getOs()
    {
        return os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }
}
