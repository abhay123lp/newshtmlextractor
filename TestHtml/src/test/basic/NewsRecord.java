package test.basic;

import java.util.Date;
import java.util.GregorianCalendar;

public class NewsRecord 
{
	public String NewsTitle;
	public String NewsContent;
	public GregorianCalendar NewsTime;
	public String NewsSource;
	//
	public HtmlPath titleHtmlPath = null;
	public HtmlPath contentHtmlPath = null;
	public HtmlPath timeHtmlPath = null;
	public HtmlPath sourceHtmlPath = null;
	public int Index;//auxillary variable to indicate the index within a html archive
	public NewsRecord(String title,String content,GregorianCalendar time,String source)
	{
		NewsContent = new String(content);
		NewsTitle = new String(title);
		NewsTime = time;
		NewsSource = new String(source);
	}
	public NewsRecord()
	{
		
	}
	public boolean isValid()
	{
		final int sLeastNumberToBeValidContent = 20;
		final int sMaxDistanceToBeCloseEnough = 3;
		if(NewsContent == null)
			return false;
		if(NewsContent.length() < sLeastNumberToBeValidContent)
			return false;
		if(NewsTime != null)
		{
			if(contentHtmlPath.findDiscrepancyLevel(timeHtmlPath) >= sMaxDistanceToBeCloseEnough)
			{
				//return false;
			}
		}
		
		return true;
	}
}
