package test.basic;

public class UrlPattern 
{
	private String mHostUrl;
	private String mRelativeRegex;
	public UrlPattern(String host,String regex)
	{
		mHostUrl = host;
		mRelativeRegex = regex;
	}
	public String getHostUrl()
	{
		return mHostUrl;
	}
	public String getRelativeRegex()
	{
		return mRelativeRegex;
	}
	public void setHostUrl(String str)
	{
		mHostUrl = str;
	}
	public void setRelativeRegex(String str)
	{
		mRelativeRegex = str;
	}
}
