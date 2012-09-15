package org.htmlcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.*;

public class HtmlCrawler {
	HttpURLConnection huc;
	InputStream is;
	BufferedReader reader;
	Queue<String> urlQueue;
	String url;
	String Path = "C:\\Users\\firstprayer\\Desktop\\testNewsIdentifier\\";
	Hashtable<String, Boolean> visitedUrlHashtable = new Hashtable<>();
	public boolean Access()
	{
        try {
            huc=(HttpURLConnection)new URL(url).openConnection();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
	}
	public void craw(String targetDomain)
	{
		int depth=0;
        url = targetDomain;
		urlQueue=new LinkedBlockingQueue<String>();
		visitedUrlHashtable.clear();
		while(Access())
		{
			String Content=SaveContent();
			if(Content != null && depth < 1)
			{
				extractURLs(Content);
				depth++;
			}
			url=urlQueue.poll() ;
			if(url == null)
				break;
		}
	}
	public void extractURLs(String Content)
	{
		String[] contents = Content.split("<a href=\"");
		for(int i=1; i < contents.length; i++)
		{
			int endHref=contents[i].indexOf("\"");
			String aHref= contents[i].substring(0, endHref);
			if(visitedUrlHashtable.get(aHref) != null)
				continue;
			if(aHref.startsWith("http"))
				urlQueue.add(aHref);
			else if(aHref.startsWith("/"))
				urlQueue.add(url+aHref);
		}
	}
	public String SaveContent()
	{
        try {
            huc.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        try {
            huc.setUseCaches(true);
            huc.connect();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is=huc.getInputStream();
            reader=new BufferedReader(new InputStreamReader(is));
            StringBuffer temp=new StringBuffer();
            String str;
            while((str=reader.readLine())!=null){
                temp.append(str+"\n");
            }
            String Content=new String(temp);
            System.out.println(url);
            BufferedWriter writer=new BufferedWriter
            		(
            				new OutputStreamWriter(new FileOutputStream(Path+url.replaceAll("[/:\\.\\?#]", "_")+".html"),"utf-8")
            		);
            visitedUrlHashtable.put(url, new Boolean(true));
            writer.write(Content);
            writer.close();
            return Content;
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                reader.close();
                is.close();
                huc.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
	}
}

