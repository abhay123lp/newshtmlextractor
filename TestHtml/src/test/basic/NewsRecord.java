package test.basic;

import java.util.Date;
import java.util.GregorianCalendar;

public class NewsRecord 
{
	public String NewsTitle;
	public String NewsContent;
	public GregorianCalendar NewsTime;
	
	//
	public String titleHtmlPathString = null;
	public String contentHtmlPathString = null;
	public String timeHtmlPathString = null;
	
	public int Index;//auxillary variable to indicate the index within a html archive
	public NewsRecord(String title,String content,GregorianCalendar time)
	{
		NewsContent = new String(content);
		NewsTitle = new String(title);
		NewsTime = time;
	}
	public NewsRecord()
	{
		
	}
}
