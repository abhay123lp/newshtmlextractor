package test.basic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.html.HTMLEditorKit.Parser;

import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.*;
import org.omg.CORBA.PRIVATE_MEMBER;

import test.basic.UrlPattern.UrlMapPath;
import test.logic.HtmlManipulator;
import test.logic.HtmlWrapper;
import text.filter.SkeletonFilter;

public class HtmlPattern {

	/**
	 * @param args
	 */
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
	public final static class UrlPattern 
	{
		private String mHostUrl;
		private String[] mRelativeRegex;
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
	public static class SkeletonPattern
	{
		public String preorderHtmlTagList;
		public Vector<String> pathList = new Vector<>();
		public SkeletonPattern(String taglist)
		{
			preorderHtmlTagList = taglist;
		}
		@Override
		public boolean equals(Object obj)
		{
			if(((SkeletonPattern)obj).preorderHtmlTagList.equals(this.preorderHtmlTagList))
			{
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	private UrlPattern mUrlPattern;
	private SkeletonPattern mSkeletonPattern;
	public UrlPattern getUrlPattern()
	{
		return mUrlPattern;
	}
	public SkeletonPattern getSkeletonPattern()
	{
		return mSkeletonPattern;
	}
	public HtmlPattern(UrlPattern urlp,SkeletonPattern skeletonp)
	{
		mUrlPattern = urlp;
		mSkeletonPattern = skeletonp;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		class UrlPatternAndHtmls
		{
			public UrlPattern urlPattern;
			public List<String> htmlPhysicalPaths = new Vector<String>();
			public UrlPatternAndHtmls(UrlPattern pattern) {
				// TODO Auto-generated constructor stub
				urlPattern = pattern;
			}
		}
		try {
			String pathString = "C:\\Users\\firstprayer\\Desktop\\新建文件夹\\";
			List<UrlMapPath> mapList = new Vector<UrlMapPath>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pathString + "map.txt"),"utf-8"));
			String text = "";
			while((text = reader.readLine()) != null)
			{
				String[] strings = text.split("::");
				if(strings.length == 2)
				{
					mapList.add(new UrlMapPath(strings[0],strings[1]));
				}
			}
			List<UrlPatternAndHtmls> resultPatternList = new Vector<UrlPatternAndHtmls>();
			
			Iterator<UrlMapPath> iterator = mapList.iterator();
			while(iterator.hasNext())//process each map
			{
				UrlMapPath newmap = iterator.next();
				String url = newmap.Url.replaceAll("https?://","" );
				url = url.replaceAll(".s?html?$", "");

				Pattern firstSpliterPattern = Pattern.compile("[/\\\\]");
				Matcher firstSpliterMatcher = firstSpliterPattern.matcher(url);
				firstSpliterMatcher.find();
				int index = firstSpliterMatcher.start();
				String hostString = url.substring(0,index);
				url = url.substring(index + 1);
				String[] strings = url.split("[/_\\-\\.]");
				Iterator<UrlPatternAndHtmls> pattIterator = resultPatternList.iterator();
				boolean match = false;
				while(pattIterator.hasNext())
				{
					match = true;
					UrlPattern urlPattern = pattIterator.next().urlPattern;
					if(hostString.equals(urlPattern.getHostUrl()))
					{
						String[] targetRegexesStrings = urlPattern.getRelativeRegexes();
						if(strings.length == targetRegexesStrings.length)
						{
							for (int i = 0; i < targetRegexesStrings.length; i++) 
							{
								if(strings[i].matches(targetRegexesStrings[i]) == false)
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
					String[] newRegexeStrings = new String[strings.length];
					for (int i = 0; i < strings.length; i++) {
						if(strings[i].matches("^\\d+?$")) // all digits
						{
							newRegexeStrings[i] = "^\\d{" + strings[i].length() + "}$";
							continue;
						}
						else if (strings[i].matches("^[a-zA-Z]+?$"))  // all characters
						{
							newRegexeStrings[i] = strings[i]; // we want to be more strict right here
						}
						else if(strings[i].matches("^[a-zA-Z0-9]+?$")) // contains digit and English character
						{
							newRegexeStrings[i] = "^[a-zA-Z0-9]{" + strings[i].length() + "}$";
						}
						else {
							System.out.println("not implemented:" + strings[i]);
							//throw new Exception("not implemented");
						}
					}
					UrlPattern urlPattern = new UrlPattern(hostString, newRegexeStrings);
					urlPattern.addUrlMapPath(newmap);
					resultPatternList.add(new UrlPatternAndHtmls(urlPattern));
				}
				
				
			}//process each map ends
			//now we have the html clustered into groups by according to the url
			//now we are going to be more specific,divide each group into several subgroups according to html skeleton
			Iterator<UrlPatternAndHtmls> urlPatternAndHtmlsIterator = resultPatternList.iterator();
			Vector<HtmlPattern> htmlPatterns = new Vector<>();
			Vector<Vector<String>> corrispondingFilePathOfHtmlPatternsVector = new Vector<>();
			while((urlPatternAndHtmlsIterator.hasNext()))
			{
				//for each cluster (attained by url),divide it into several groups according to skeleton
				UrlPatternAndHtmls patternAndHtmls = urlPatternAndHtmlsIterator.next();
				Vector<SkeletonPattern> skeletonPatterns = new Vector<>();
				Iterator<UrlMapPath> filePathIterator = patternAndHtmls.urlPattern.getUrlMapPathElements();//iterator();
				
				while(filePathIterator.hasNext())
				{
					String filePathString = filePathIterator.next().Path;
					String textContent = HtmlArchive.getHtmlContentSafely(filePathString);
					textContent = HtmlManipulator.RuleOutUselessText(textContent);
					org.htmlparser.Parser parser = new org.htmlparser.Parser(textContent);
					
					NodeList nodes = parser.extractAllNodesThatMatch(new SkeletonFilter());
					NodeIterator nodeIterator = nodes.elements();
					String string = "";
					while(nodeIterator.hasMoreNodes())
					{
						TagNode node = (TagNode)nodeIterator.nextNode();
						String tagNameString = node.getTagName();
						Node preNode = node;
						int number = 0;
						while((preNode = preNode.getPreviousSibling()) != null)
						{
							if(preNode instanceof Tag)
							{
								if(((TagNode)preNode).getTagName().equals(tagNameString))
								{
									number++;
								}
							}
							
						}
						
						string += tagNameString + number + "/";
					}
					boolean found = false;
					for (int j = 0; j < skeletonPatterns.size(); j++) {
						if(skeletonPatterns.get(j).preorderHtmlTagList.equals(string))
						{
							found = true;
							skeletonPatterns.get(j).pathList.add(filePathString);
							break;
						}
					}
					if(!found)
					{
						SkeletonPattern newSkeletonPattern = new SkeletonPattern(string);
						newSkeletonPattern.pathList.add(filePathString);
						skeletonPatterns.add(newSkeletonPattern);
						//System.out.println(string);
					}
				}
				for (int j = 0; j < skeletonPatterns.size(); j++) {
					htmlPatterns.add(new HtmlPattern(patternAndHtmls.urlPattern, skeletonPatterns.get(j)));
				}	
			}//process all url pattern done
			String clusterOutputPathString = "C:\\Users\\firstprayer\\Desktop\\clusteroutput";
			File rootDirFile = new File(clusterOutputPathString);
			File[] subDirList = rootDirFile.listFiles();
			for (int i = 0; i < subDirList.length; i++) {
				subDirList[i].delete();
			}
			
			Iterator<HtmlPattern> htmlPatternIterator = htmlPatterns.iterator();
			int i = 1;
			while(htmlPatternIterator.hasNext())
			{
				String subDirNameString = clusterOutputPathString + "\\" + i;
				i++;
				File newDirFile = new File(subDirNameString);
				newDirFile.mkdir();
				
				HtmlPattern htmlPattern = htmlPatternIterator.next();
				Iterator<String> fileIterator = htmlPattern.getSkeletonPattern().pathList.iterator();
				File newReadMeFile = new File(subDirNameString + "\\ReadMe.txt");
				BufferedWriter newReadMeWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newReadMeFile)));
				newReadMeWriter.write(htmlPattern.getUrlPattern().getHostUrl());
				newReadMeWriter.newLine();
				String[] strings = htmlPattern.getUrlPattern().getRelativeRegexes();
				for (int j = 0; j < strings.length; j++) {
					newReadMeWriter.write(strings[j] + "/");
				}
				newReadMeWriter.newLine();
				newReadMeWriter.write(htmlPattern.getSkeletonPattern().preorderHtmlTagList);
				newReadMeWriter.close();
				while(fileIterator.hasNext())
				{
					String path = fileIterator.next();
					File originalHtmlFile = new File(path);
					File newHtmlFile = new File(newDirFile.getAbsoluteFile() + "\\" + originalHtmlFile.getName());
					StringBuffer stringBuffer = new StringBuffer();
					if(originalHtmlFile.isFile() == false)
					{
						System.out.println("what?!");
					}
					BufferedReader htmlReader = new BufferedReader(new InputStreamReader(new FileInputStream(originalHtmlFile),"utf-8"));
					String string = "";
					while((string = htmlReader.readLine()) != null)
					{
						stringBuffer.append(string);
					}
					string = stringBuffer.toString();
					htmlReader.close();
					newHtmlFile.createNewFile();
					BufferedWriter newHtmlWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newHtmlFile),"utf-8"));
					newHtmlWriter.write(string);
					newHtmlWriter.close();
				}
			}
			System.out.println(htmlPatterns.size());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}

}
