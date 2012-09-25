package org.htmlcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HtmlCrawler {
	WebClient client = new WebClient();
	HtmlPage page = null;
	//InputStream is;
	//BufferedReader reader;
	String mHtmlContent = null;
	Queue<String> urlQueue;
	String url;
	String Path = "C:\\Users\\firstprayer\\Desktop\\testNewsIdentifier\\";
	Hashtable<String, Boolean> visitedUrlHashtable = new Hashtable<>();
	public HtmlCrawler(){
		client.setJavaScriptEnabled(false);
	}
	public boolean Access()
	{
        try {
        	HttpClient client = new DefaultHttpClient();     
            HttpGet httpGet = new HttpGet(url);     
            StringBuffer strBuf = new StringBuffer();     
            HttpResponse response = client.execute(httpGet);     
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {     
                HttpEntity entity = response.getEntity();     
                if (entity != null) {     
                    BufferedReader reader = new BufferedReader(     
                        new InputStreamReader(entity.getContent(), "UTF-8"));     
                    String line = null;     
                    strBuf = new StringBuffer();     
                    while ((line = reader.readLine()) != null) {     
                        strBuf.append(line); 
                    }
                    if (entity.getContentLength() > 0) {     
                           
                        
                    }     
                }     
                if (entity != null) {     
                    entity.consumeContent();     
                }     
            }     
            //将url标记为已访问     
            markUrlAsVisited(url);     
            mHtmlContent = strBuf.toString(); 
            return true;
        } catch (MalformedURLException e) {
        	System.out.println(url);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(url);
        }
        return false;
	}
	private void markUrlAsVisited(String url) {
		// TODO Auto-generated method stub
		visitedUrlHashtable.put(url, true);
	}
	public void craw(String targetDomain,String savePath) throws IOException
	{
		int depth=0;
        url = targetDomain;
		urlQueue = new LinkedBlockingQueue<String>();
		visitedUrlHashtable.clear();
		Path = savePath;
		while(Access())
		{
			if(depth < 2)
			{
				extractURLs(mHtmlContent);
				
				depth++;
			}
			if(mHtmlContent != null){
				BufferedWriter writer=new BufferedWriter
	            		(
	            				new OutputStreamWriter(new FileOutputStream(Path+url.replaceAll("[/:\\.\\?#]", "_")+".html"),"utf-8")
	            		);
	            writer.write(mHtmlContent);
	            writer.close();
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
			if(aHref.contains("?"))
				continue;
			if(aHref.startsWith("http"))
				urlQueue.add(aHref);
			else if(aHref.startsWith("/"))
				urlQueue.add(url+aHref);
		}
	}
	/*
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
	}*/
}

