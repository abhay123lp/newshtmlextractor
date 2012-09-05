package org.newsextractor.util;

import java.util.Date;
import java.util.GregorianCalendar;
/**
 * 
 * @author firstprayer
 * The result of extraction
 */
public class NewsRecord 
{
	/**
	 * The information we really need
	 */
	public String NewsTitle;
	public String NewsContent;
	public GregorianCalendar NewsTime;
	public String NewsSource;
	/**
	 * The html paths of the information above in the original html file
	 */
	public HtmlPath titleHtmlPath = null;
	public HtmlPath contentHtmlPath = null;
	public HtmlPath timeHtmlPath = null;
	public HtmlPath sourceHtmlPath = null;
	/**
	 * auxillary variable to indicate the index within a html archive
	 */
	public int Index;
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
	/**
	 * To judge whether a record is valid
	 * Not finished yet
	 * @return
	 */
	public boolean isValid()
	{
		final int sLeastNumberToBeValidContent = 30;
		final int sMaxDistanceToBeCloseEnough = 3;
		if(NewsContent == null)
			return false;
		/**
		 * records containing too few Chinese characters shouldn't be the ones we will be interested in
		 */
		if(NewsContent.replaceAll("[^(\\u4E00-\\u9FA5)]", "").length() < sLeastNumberToBeValidContent)
			return false;
		/**
		 * The following is under experiment, not in use yet
		 */
		/*
		if(NewsTime != null)
		{
			if(contentHtmlPath.findDiscrepancyLevel(timeHtmlPath) >= sMaxDistanceToBeCloseEnough)
			{
				//return false;
			}
		}*/
		
		return true;
	}
}
