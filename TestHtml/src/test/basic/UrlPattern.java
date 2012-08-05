package test.basic;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class UrlPattern 
{
	public final static class UrlMapPath
	{
		public String Url;
		public String Path;
		public UrlMapPath(String url,String path)
		{
			Url = url;
			Path = path;
		}
	}
	private String mHostUrl;
	private String[] mRelativeRegex;
	private String mPhysicalPath;
	private Vector<UrlMapPath> mUrlMapPathList;
	public UrlPattern(String host,String[] regex)
	{
		mHostUrl = host;
		mRelativeRegex = regex;
		mUrlMapPathList = new Vector<UrlMapPath>();
	}
	public void addUrlMapPath(UrlMapPath targetMapPath)
	{
		mUrlMapPathList.add(targetMapPath);
	}
	public Iterator<UrlMapPath> getUrlMapPathElements()
	{
		return mUrlMapPathList.iterator();
	}
	public String getHostUrl()
	{
		return mHostUrl;
	}
	public String[] getRelativeRegexes()
	{
		return mRelativeRegex;
	}
	public void setHostUrl(String str)
	{
		mHostUrl = str;
	}
	public void setRelativeRegex(String[] str)
	{
		mRelativeRegex = str;
	}
}
