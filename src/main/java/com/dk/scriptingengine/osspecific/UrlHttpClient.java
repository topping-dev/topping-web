package com.dk.scriptingengine.osspecific;

import java.net.URLConnection;

public class UrlHttpClient
{
    private URLConnection connection;

    public URLConnection getConnection()
    {
        return connection;
    }

    public void setConnection(URLConnection connection)
    {
        this.connection = connection;
    }
}
