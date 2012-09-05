package org.newsextractor.production;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * 
 * @author firstprayer
 * This class could be considered as the global controller of our business pipeline
 * 
 */
public class HtmlManager 
{
	protected Vector<HtmlArchive> mHtmlArchives = new Vector<HtmlArchive>();
	/**
	 * When we've finished adding archives into it,call this to process them all
	 */
	public void ProcessAll()
	{
		int size = 0;
		for (int i = mHtmlArchives.size() - 1; i >= 0; i--) {
			HtmlArchive archive = mHtmlArchives.get(i);
			archive.CollectFile();
			size += archive.getFilesNumber();
			archive.Initialize();
			//archive.ruleOutUselessText();
			archive.ExtractRecord();
			//archive.UpdateRecord();
			archive.saveRecord();
			//archive.ReleaseUselessSpace();
			/**
			 * remove it to save memory space
			 */
			mHtmlArchives.remove(i);
		}
		System.out.println(size);
	}
	/**
	 * Add an archive into it
	 * @param archive
	 */
	public void AddArchive(HtmlArchive archive)
	{
		mHtmlArchives.add(archive);
	}
}
