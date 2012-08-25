package test.logic;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import test.basic.HtmlArchive;
import test.basic.UrlPattern;

public class HtmlManager 
{
	
	public List<UrlPattern> UrlCluster(List<UrlPattern.UrlMapPath> mapList)
	{
		List<UrlPattern> resultPatternList = new Vector<UrlPattern>();
		Iterator<UrlPattern.UrlMapPath> iterator = mapList.iterator();
		while(iterator.hasNext())
		{
			UrlPattern.UrlMapPath newmap = iterator.next();
			String url = newmap.Url.replaceAll("https?://","" );
			url = url.replaceAll(".s?html?$", "");
			String[] strings = url.split("[/_\\-\\.]");
			Iterator<UrlPattern> pattIterator = resultPatternList.iterator();
			boolean match = false;
			while(pattIterator.hasNext())
			{
				match = true;
				UrlPattern urlPattern = pattIterator.next();
				if(strings[0].equals(urlPattern.getHostUrl()))
				{
					String[] targetRegexesStrings = urlPattern.getRelativeRegexes();
					if(strings.length - 1 == targetRegexesStrings.length)
					{
						for (int i = 0; i < targetRegexesStrings.length; i++) 
						{
							if(strings[i + 1].matches(targetRegexesStrings[i]) == false)
							{
								match = false;
								break;
							}
						}
					}
					else {
						match = false;
					}
				}
				else {
					match = false;
				}
				if(match) //if match,we add the map into the corrisponding pattern
				{
					urlPattern.addUrlMapPath(newmap);
					break;
				}
			}
			if(!match) // create a new pattern
			{
				String[] newRegexeStrings = new String[strings.length - 1];
				for (int i = 1; i < strings.length; i++) {
					if(strings[i].matches("^\\d+?$")) // all digits
					{
						newRegexeStrings[i - 1] = "^\\d{" + strings[i].length() + "}$";
						continue;
					}
					else if (strings[i].matches("^[a-zA-Z]+?$"))  // all characters
					{
						newRegexeStrings[i - 1] = strings[i]; // we want to be more strict right here
					}
					else if(strings[i].matches("^[a-zA-Z0-9]+?$")) // contains digit and English character
					{
						newRegexeStrings[i - 1] = "^[a-zA-Z0-9]{" + strings[i].length() + "}$";
					}
					else {
						System.out.println("not implemented:" + strings[i]);
						//throw new Exception("not implemented");
					}
				}
				UrlPattern urlPattern = new UrlPattern(strings[0], newRegexeStrings);
				urlPattern.addUrlMapPath(newmap);
				resultPatternList.add(urlPattern);
			}
		}
		//we shall be more specific here
		//we are going to retrieve the skeleton of the html
		
		return resultPatternList;
	}
	protected Vector<HtmlArchive> mHtmlArchives = new Vector<HtmlArchive>();
	public void ProcessArchive(HtmlArchive archive)
	{
		
	}
	public void ProcessAll()
	{
		for (int i = 0; i < mHtmlArchives.size(); i++) {
			HtmlArchive archive = mHtmlArchives.get(i);
			archive.CollectFile();
			archive.Initialize();
			archive.ruleOutUselessText();
			archive.ExtractRecord();
			//archive.UpdateRecord();
			archive.saveRecord();
			archive.ReleaseUselessSpace();
		}
		
	}
	public void AddArchive(HtmlArchive archive)
	{
		mHtmlArchives.add(archive);
	}
}
