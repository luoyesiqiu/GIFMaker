package com.wocao.gifmaker.other;
import org.apache.http.client.methods.*;
import org.apache.http.client.*;
import org.apache.http.util.*;
import org.apache.http.impl.client.*;
import org.apache.http.*;

public class Http
{
	public static  String get(String url, String cookies)
	{
		String strResult="";
		HttpGet hg = new HttpGet(url);
		hg.addHeader("Cookie", cookies);
		HttpClient client=new DefaultHttpClient();
		try
		{
			HttpResponse response=client.execute(hg);
			if (response.getStatusLine().getStatusCode() == 200)
				strResult = EntityUtils.toString(response.getEntity(), "utf-8");
		}
		catch (Exception e)
		{}
		return strResult;
	}
}
