package com.dk.scriptingengine;

import com.dk.helpers.MultipartUtility;
import com.dk.scriptingengine.backend.LuaClass;
import com.dk.scriptingengine.backend.LuaFunction;
import com.dk.scriptingengine.backend.LuaInterface;
import com.dk.scriptingengine.osspecific.AsyncTask;
import com.dk.scriptingengine.osspecific.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Class that handles HTTP POST and GET requests
 */
@LuaClass(className = "LuaHttpClient")
public class LuaHttpClient implements LuaInterface
{
	public URLConnection httpClient;
	public String tag;
	public LuaTranslator onComplete = null;
	public LuaTranslator onFail = null;
	public String contentType;

	private static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private static byte[] convertStreamToByteArray(java.io.InputStream is) {
		int nRead;
		byte[] data = new byte[16384];
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	
	/**
	 * Creates LuaHttpClient Object From Lua.
	 * @param tag
	 * @return LuaHttpClient
	 */
	@LuaFunction(manual = false, methodName = "Create", self = LuaHttpClient.class, arguments = { String.class })
	public static LuaHttpClient Create(String tag)
	{
		LuaHttpClient lhc = new LuaHttpClient();
		lhc.httpClient = LuaEngine.getInstance().GetHttpClient(tag).getConnection();
		lhc.tag = tag;
		return lhc;
	}
	
	/**
	 * Sets the content type of the connection
	 * @param type type
	 */
	@LuaFunction(manual = false, methodName = "SetContentType", arguments = { String.class })
	public void SetContentType(String type)
	{
		contentType = type;
	}
	
	/**
	 * Start Form data.
	 * This is used to create multipart form data. After this use AppendPostData or AppendImageData.
	 * To end form use EndForm.
	 * return LuaObjectStore
	 */
	@LuaFunction(manual = false, methodName = "StartForm", arguments = { })
	public LuaObjectStore StartForm()
	{
		LuaObjectStore los = new LuaObjectStore();
        try
        {
            MultipartUtility mu = new MultipartUtility(httpClient);
            los.obj = mu;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
		return los;
	}
	
	/**
	 * Add data to form.
	 * @param formData Form data created by StartForm.
	 * @param name id of the data.
	 * @param value value of the data.
	 */
	@LuaFunction(manual = false, methodName = "AppendPostData", arguments = { Object.class, String.class, String.class })
	public void AppendPostData(Object formData, String name, String value)
	{
		LuaObjectStore los = (LuaObjectStore)formData;
		@SuppressWarnings("unchecked")
		MultipartUtility mu = (MultipartUtility) los.obj;
        try
        {
            mu.addFormField(name, value);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
	
	/**
	 * Add file to form.
	 * @param formData Form data created by StartForm.
	 * @param name id of the data.
	 * @param file data of the file.
	 */
	@LuaFunction(manual = false, methodName = "AppendFileData", arguments = { Object.class, String.class, String.class })
	public void AppendFileData(Object formData, String name, Object file)
	{
		LuaObjectStore los = (LuaObjectStore)formData;
        @SuppressWarnings("unchecked")
        MultipartUtility mu = (MultipartUtility) los.obj;
		byte[] data = (byte[])file;
        try
        {
            mu.addFilePart(name, "upload.png", data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
	
	/**
	 * Finishes the form data created.
	 * @param formData Form data created by StartForm.
	 */
	@LuaFunction(manual = false, methodName = "EndForm", arguments = { Object.class })
	public void EndForm(Object formData)
	{
	}
	
	/**
	 * Start asynchronous load of form.
	 * @param url url to send.
	 * @param formData Form data finished by EndForm.
	 * @param tag tag that is used to identify connection.
	 */
	@LuaFunction(manual = false, methodName = "StartAsyncLoadForm", arguments = { String.class, Object.class, String.class })
	public void StartAsyncLoadForm(String url, Object formData, String tag)
	{
        LuaObjectStore los = (LuaObjectStore)formData;
        @SuppressWarnings("unchecked")
        MultipartUtility mu = (MultipartUtility) los.obj;

        MultipartUtility.ByteArrayWriter parts = mu.getMultipart();

		URLConnection connection = null;
		try
		{
			 connection = new URL(url).openConnection();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if(connection == null)
			return;

		if(contentType != null && contentType.compareTo("") != 0)
			connection.setRequestProperty("Content-Type", contentType);

        try
        {
            ((HttpURLConnection)connection).setRequestMethod("POST");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        DataOutputStream dos = null;
        try
        {
            dos = (DataOutputStream) httpClient.getOutputStream();
            parts.flush();
            dos.writeBytes(parts.toString());
            dos.flush();

            parts.close();
            dos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

	    
	    AsyncTask<Object, String, String> helper = new AsyncTask<Object, String, String>()
		{	
			CharSequence errorText = null;
			String resultString = "";
			@Override
			protected String doInBackground(Object... params)
			{
				URLConnection httpclient = (URLConnection) params[1];
				try 
				{
					InputStream in = new BufferedInputStream(httpclient.getInputStream());
					resultString = convertStreamToString(in);
				} 
				catch (Exception e) 
				{
					Log.e("LuaHttpClient", "Error downloading list." + e.getMessage());
					errorText = "Error";
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String result) 
		    {
				if(errorText != null)
					onFail.CallIn(resultString, errorText);
				else
					onComplete.CallIn(resultString);
		    }
		};
		try 
		{
			helper.execute(this, httpClient);
		} 
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}
	
	/**
	 * Start asynchronous load.
	 * @param url url to send.
	 * @param data post data string.
	 * @param tag tag that is used to identify connection.
	 */
	@LuaFunction(manual = false, methodName = "StartAsyncLoad", arguments = { String.class, String.class, String.class })
	public void StartAsyncLoad(String url, String data, String tag)
	{
		URLConnection connection = null;
		try
		{
			connection = new URL(url).openConnection();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if(connection == null)
			return;

		if(contentType != null && contentType.compareTo("") != 0)
			connection.setRequestProperty("Content-Type", contentType);

        try
        {
            ((HttpURLConnection)connection).setRequestMethod("POST");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String[] arr = data.split("#");
		StringBuilder sbPostData = new StringBuilder();
		try
		{
			for(String s : arr)
			{
				String[] arrIn = s.split("=");
				sbPostData.append(arrIn[0]).append("=").append(URLEncoder.encode(arrIn[1], "UTF-8"));
			}
		}
		catch (Exception e) 
		{
			sbPostData = new StringBuilder();
			sbPostData.append(data);
		}

		AsyncTask<Object, String, String> helper = new AsyncTask<Object, String, String>()
		{	
			CharSequence errorText = null;
			String resultString = "";
			@Override
			protected String doInBackground(Object... params)
			{
				URLConnection httpclient = (URLConnection) params[1];
				StringBuilder postData = (StringBuilder) params[2];
				try
				{
					DataOutputStream wr = new DataOutputStream( httpclient.getOutputStream());
					wr.write(postData.toString().getBytes("UTF-8"));
					InputStream in = new BufferedInputStream(httpclient.getInputStream());
					resultString = convertStreamToString(in);
				}
				catch (Exception e) 
				{
					Log.e("LuaHttpClient", "Error downloading list." + e.getMessage());
					errorText = "Error";
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String result) 
		    {
				if(errorText != null && onFail != null)
					onFail.CallIn(resultString, errorText);
				else if(errorText == null && onComplete != null)
					onComplete.CallIn(resultString);
		    }
		};
		try 
		{
			helper.execute(this, httpClient, sbPostData);
		} 
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}
	
	/**
	 * Start asynchronous load.
	 * @param url url to send.
	 * @param tag tag that is used to identify connection.
	 */
	@LuaFunction(manual = false, methodName = "StartAsyncLoadGet", arguments = { String.class, String.class })
	public void StartAsyncLoadGet(String url, String tag)
	{
        URLConnection connection = null;
        try
        {
            connection = new URL(url).openConnection();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if(connection == null)
            return;

        if(contentType != null && contentType.compareTo("") != 0)
            connection.setRequestProperty("Content-Type", contentType);

        try
        {
            ((HttpURLConnection)connection).setRequestMethod("GET");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
		
	    AsyncTask<Object, String, String> helper = new AsyncTask<Object, String, String>()
		{	
			CharSequence errorText = null;
			String resultString = "";
			@Override
			protected String doInBackground(Object... params)
			{
                URLConnection httpclient = (URLConnection) params[1];
                StringBuilder postData = (StringBuilder) params[2];
                try
                {
                    InputStream in = new BufferedInputStream(httpclient.getInputStream());
                    resultString = convertStreamToString(in);
                }
                catch (Exception e)
                {
                    Log.e("LuaHttpClient", "Error downloading list." + e.getMessage());
                    errorText = "Error";
                }
				return null;
			}
			
			@Override
			protected void onPostExecute(String result) 
		    {
				if(errorText != null)
					onFail.CallIn(resultString, errorText);
				else
					onComplete.CallIn(resultString);
		    }
		};
		try 
		{
			helper.execute(this, httpClient);
		} 
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}
	
	/**
	 * Start synchronous load.
	 * @param url url to send.
	 * @param data post data string.
	 * @return String value of returned data.
	 */
	@LuaFunction(manual = false, methodName = "StartLoad", arguments = { String.class, String.class })
	public String StartLoad(String url, String data)
	{
        URLConnection connection = null;
        try
        {
            connection = new URL(url).openConnection();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if(connection == null)
            return "";

        if(contentType != null && contentType.compareTo("") != 0)
            connection.setRequestProperty("Content-Type", contentType);

        String[] arr = data.split("#");
        StringBuilder sbPostData = new StringBuilder();
        try
        {
            for(String s : arr)
            {
                String[] arrIn = s.split("=");
                sbPostData.append(arrIn[0]).append("=").append(URLEncoder.encode(arrIn[1], "UTF-8"));
            }
        }
        catch (Exception e)
        {
            sbPostData = new StringBuilder();
            sbPostData.append(data);
        }
		
		CharSequence errorText = null;
		String resultString = "";

        try
        {
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(sbPostData.toString().getBytes("UTF-8"));
            InputStream in = new BufferedInputStream(connection.getInputStream());
            resultString = convertStreamToString(in);
        }
        catch (Exception e)
        {
            Log.e("LuaHttpClient", "Error downloading list." + e.getMessage());
            errorText = "Hata, daha sonra tekrar deneyin!";
        }
		
		if(errorText != null)
			return errorText.toString();
		else
			return resultString;
	}
	
	/**
	 * Start synchronous load.
	 * @param url url to send.
	 * @return String value of returned data.
	 */
	@LuaFunction(manual = false, methodName = "StartLoadGet", arguments = { String.class })	
	public String StartLoadGet(String url)
	{
        URLConnection connection = null;
        try
        {
            connection = new URL(url).openConnection();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if(connection == null)
            return "";

        if(contentType != null && contentType.compareTo("") != 0)
            connection.setRequestProperty("Content-Type", contentType);
		
		CharSequence errorText = null;
		String resultString = "";

        try
        {
            InputStream in = new BufferedInputStream(connection.getInputStream());
            resultString = convertStreamToString(in);
        }
        catch (Exception e)
        {
            Log.e("LuaHttpClient", "Error downloading list." + e.getMessage());
            errorText = "Hata, daha sonra tekrar deneyin!";
        }
		
		if(errorText != null)
			return errorText.toString();
		else
			return resultString;
	}
	
	/**
	 * Set timeout of connection
	 * @param timeout timeout value seconds
	 */
	@LuaFunction(manual = false, methodName = "SetTimeout", arguments = { Integer.class })
	public void SetTimeout(Integer timeout)
	{
		Integer timeoutMs = new Integer(timeout * 1000);
        httpClient.setReadTimeout(timeoutMs);
        httpClient.setConnectTimeout(timeoutMs);
	}
	
	/**
	 * Frees the object.
	 */
	@LuaFunction(manual = false, methodName="Free")
	public void Free()
	{
		
	}
	
	/**
     * Registers Event for
     * "OnFinish" happens when data is finished
     * "OnFail" happens when data is failed
     */
	@Override
	@LuaFunction(manual = false, methodName="RegisterEventFunction", arguments = { String.class, LuaTranslator.class })
	public void RegisterEventFunction(String var, LuaTranslator lt)
	{
		if(var.compareTo("OnFinish") == 0)
		{
			onComplete = lt;
		}
		else if(var.compareTo("OnFail") == 0)
		{
			onFail = lt;
		}
	}

	/**
	 * (Ignore)
	 */
	@Override
	public String GetId()
	{
		return "LuaHttpClient";
	}
	
}
