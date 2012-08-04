package test.logic;

import java.util.Vector;

import test.basic.HtmlArchive;

public class HtmlManager 
{
	protected Vector<HtmlArchive> mHtmlArchives = new Vector<HtmlArchive>();
	public void ProcessArchive(HtmlArchive archive)
	{
		
	}
	public void ProcessAll()
	{
		for (int i = 0; i < mHtmlArchives.size(); i++) {
			mHtmlArchives.get(i).CollectFile();
			mHtmlArchives.get(i).ExtractRecord();
			mHtmlArchives.get(i).UpdateRecord();
			mHtmlArchives.get(i).saveRecord();
		}
		
	}
	public void AddArchive(HtmlArchive archive)
	{
		mHtmlArchives.add(archive);
	}
}
