package test.basic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.jar.JarException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.omg.CORBA.PRIVATE_MEMBER;

import test.logic.HtmlManipulator;
import test.logic.HtmlWrapper;
import test.logic.HtmlWrapper.Block;
import test.logic.MyNodeFilter;


public class HtmlArchive 
{
	private String mUrlPattern;
	public String ArchiveName;
	private File mDirectory;
	private Vector<String> mFilenameList = new Vector<String>();
	private Vector<NewsRecord> mRecordList = new Vector<NewsRecord>();
	private String getHtmlContentSafely(String htmlPath) throws IOException
	{
		InputStreamReader inputStreamReader = new InputStreamReader
				(
						new FileInputStream(
								new File(htmlPath)
							),"utf-8"
				);
		String encodingString = inputStreamReader.getEncoding();
		BufferedReader reader = new BufferedReader(inputStreamReader);
		
		StringBuffer htmlContentStringBuffer = new StringBuffer();
		String text = "";
		while((text = reader.readLine()) != null)
		{
			htmlContentStringBuffer.append(text);
		}
		text = htmlContentStringBuffer.toString();
		reader.close();
		/*Pattern encodingRegex = Pattern.compile("<.*?meta.*?charset=.+?[\"\\s/>]",Pattern.CASE_INSENSITIVE);
		Matcher encodingMatcher = encodingRegex.matcher(text);
		if(encodingMatcher.find())
		{
			String encodingblockString = encodingMatcher.group().toLowerCase();
			Pattern charsetPattern = Pattern.compile("charset=",Pattern.CASE_INSENSITIVE);
			Matcher charsetMatcher = charsetPattern.matcher(encodingblockString);
			
			int index = encodingblockString.indexOf("charset=");
			if(index != -1)
			{
				String string = encodingblockString.substring(index + 8);
				
				for (int i = 0; i < string.length(); i++) {
					char c = string.charAt(i);
					if(c == ' ' || c == '/' || c == '>' || c == '\"' || c== '\'')
					{
						string = string.substring(0, i);
						break;
					}
				}
				try {
					if(encodingString.toLowerCase().equals(string) == false)
					{
						inputStreamReader = new InputStreamReader(new FileInputStream(new File(htmlPath)),string);
						reader = new BufferedReader(inputStreamReader);
						htmlContentStringBuffer = new StringBuffer();
						text = "";
						while((text = reader.readLine()) != null)
						{
							htmlContentStringBuffer.append(text);
						}
						text = htmlContentStringBuffer.toString();
						reader.close();
					}
				} catch (java.io.UnsupportedEncodingException e) {
					// TODO: handle exception
				}
				
				
			}
			
		}*/
		return text;
		
	}
	public HtmlArchive(File path,String name)
	{
		mDirectory = path;
		ArchiveName = name;
	}
	public boolean CollectFile()
	{
		if(mDirectory == null)
			return false;
		File[] fileList = mDirectory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				Pattern htmlPattern = Pattern.compile(".*\\.s?html?$",Pattern.CASE_INSENSITIVE);
				Matcher htmlMatcher = htmlPattern.matcher(name);
				if(htmlMatcher.find())
					return true;
				return false;
			}
		});
		for (int i = 0; i < fileList.length; i++) {
			mFilenameList.add(fileList[i].getAbsolutePath());
		}
		return true;
	}
	public void ExtractRecord()
	{
		HtmlWrapper htmlWrapper = new HtmlWrapper();
		for (int i = 0; i < mFilenameList.size(); i++) 
		{
			try {
				String text = getHtmlContentSafely(mFilenameList.get(i));
				text = HtmlManipulator.RuleOutUselessText(text);
				
				Parser parser = new Parser(text);
				PrototypicalNodeFactory prototypicalNodeFactory = new PrototypicalNodeFactory();
				prototypicalNodeFactory.setTextPrototype(new AdvanceTextNode(null,i,i));
				parser.setNodeFactory(prototypicalNodeFactory);
				NodeList nodeList = parser.extractAllNodesThatMatch(new MyNodeFilter());
				Node[] nodes = nodeList.toNodeArray();
				//clear the wrapper to new nodes to be added into and processed
				htmlWrapper.clear();
				Vector<AdvanceTextNode> ruledOutHrefTextNodes = new Vector<AdvanceTextNode>();
				for (int j = 0; j < nodes.length; j++) 
				{
					if(nodes[j] instanceof TextNode)
					{
						//if(((AdvanceTextNode)nodes[j]).getWithinHref() == false)
							htmlWrapper.addNode((AdvanceTextNode)nodes[j]);
						//else 
						//{
						//	ruledOutHrefTextNodes.add((AdvanceTextNode)nodes[j]);
						//}
					}
				}
				htmlWrapper.InitializeBlockList();
				NewsRecord resultNewsRecord = htmlWrapper.Manipulate();
				if(resultNewsRecord != null)
				{
					resultNewsRecord.Index = i;
					mRecordList.add(resultNewsRecord);
				}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	public void UpdateRecord()
	{
		final class PathPossibility
		{
			public String pathString = null;
			public int corresponseCount = 1;
			public PathPossibility(String str)
			{
				pathString = new String(str);
			}
		}
		//we store all the path within the news record,and record the number of each path
		Vector<PathPossibility> titlePathAccumulator = new Vector<PathPossibility>();
		Vector<PathPossibility> timePathAccumulator = new Vector<PathPossibility>();
		Vector<PathPossibility> contentPathAccumulator = new Vector<PathPossibility>();
		for (int i = 0; i < mRecordList.size(); i++)
		{
			NewsRecord record = mRecordList.get(i);
			boolean found;
			found = false;
			for (int j = 0; j < titlePathAccumulator.size(); j++) {
				PathPossibility ppPathPossibility = titlePathAccumulator.get(j);
				if(ppPathPossibility.pathString.equals(record.titleHtmlPathString))
				{
					found = true;
					ppPathPossibility.corresponseCount++;
					break;
				}
			}
			if(!found)
			{
				if(record.titleHtmlPathString != null)
					titlePathAccumulator.add(new PathPossibility(record.titleHtmlPathString));
			}
			
			found = false;
			for (int j = 0; j < timePathAccumulator.size(); j++) {
				PathPossibility ppPathPossibility = timePathAccumulator.get(j);
				if(ppPathPossibility.pathString.equals(record.timeHtmlPathString))
				{
					found = true;
					ppPathPossibility.corresponseCount++;
					break;
				}
			}
			if(!found)
			{
				if(record.timeHtmlPathString != null)
				timePathAccumulator.add(new PathPossibility(record.timeHtmlPathString));
			}
			
			found = false;
			for (int j = 0; j < contentPathAccumulator.size(); j++) {
				PathPossibility ppPathPossibility = contentPathAccumulator.get(j);
				if(ppPathPossibility.pathString.equals(record.contentHtmlPathString))
				{
					found = true;
					ppPathPossibility.corresponseCount++;
					break;
				}
			}
			if(!found)
			{
				if(record.contentHtmlPathString != null)
				contentPathAccumulator.add(new PathPossibility(record.contentHtmlPathString));
			}
			
		}
		//then we figure out which path should most possibly be the right one
		String mostPossibleTitlePath = null;
		String mostPossibleTimePath = null;
		String mostPossibleContentPath = null;
		int mostNum;
		mostNum = 0;
		for (int j = 0; j < titlePathAccumulator.size(); j++) {
			if(titlePathAccumulator.get(j).corresponseCount > mostNum)
			{
				mostNum = titlePathAccumulator.get(j).corresponseCount;
				mostPossibleTitlePath = titlePathAccumulator.get(j).pathString;
			}
		}
		
		mostNum = 0;
		for (int j = 0; j < timePathAccumulator.size(); j++) {
			if(timePathAccumulator.get(j).corresponseCount > mostNum)
			{
				mostNum = timePathAccumulator.get(j).corresponseCount;
				mostPossibleTimePath = timePathAccumulator.get(j).pathString;
			}
		}
		
		mostNum = 0;
		for (int j = 0; j < contentPathAccumulator.size(); j++) {
			if(contentPathAccumulator.get(j).corresponseCount > mostNum)
			{
				mostNum = contentPathAccumulator.get(j).corresponseCount;
				mostPossibleContentPath = contentPathAccumulator.get(j).pathString;
			}
		}
		
		//after this ,we will check each record and see whether their path is right
		for (int i = 0; i < mRecordList.size(); i++) {
			NewsRecord record = mRecordList.get(i);
			boolean contentMatch = (record.contentHtmlPathString == null)?false:record.contentHtmlPathString.equals(mostPossibleContentPath);
			boolean titleMatch = (record.titleHtmlPathString == null)?false:record.titleHtmlPathString.equals(mostPossibleTitlePath);
			boolean timeMatch = (record.timeHtmlPathString == null)?false:record.timeHtmlPathString.equals(mostPossibleTimePath);
			if(!contentMatch || !timeMatch || !titleMatch)
			{
				//Ok,we need to update it now
				HtmlWrapper htmlWrapper = new HtmlWrapper();
				
				try {
					String text = getHtmlContentSafely(mFilenameList.get(record.Index));
					text = HtmlManipulator.RuleOutUselessText(text);
					
					Parser parser = new Parser(text);
					PrototypicalNodeFactory prototypicalNodeFactory = new PrototypicalNodeFactory();
					prototypicalNodeFactory.setTextPrototype(new AdvanceTextNode(null,i,i));
					parser.setNodeFactory(prototypicalNodeFactory);
					NodeList nodeList = parser.extractAllNodesThatMatch(new MyNodeFilter());
					Node[] nodes = nodeList.toNodeArray();
					//clear the wrapper to new nodes to be added into and processed
					htmlWrapper.clear();
					for (int j = 0; j < nodes.length; j++) 
					{
						if(nodes[j] instanceof TextNode)
						{
							htmlWrapper.addNode((AdvanceTextNode)nodes[j]);
						}
					}
					htmlWrapper.InitializeBlockList();
					int lastImportantFactor = 0;
					int originBodyIndex = -1, bodyIndex = -1, updateBodyIndex = -1;
					int originTimeIndex = -1, timeIndex = -1, updateTimeIndex = -1;
					for (int j = 0; j < htmlWrapper.getBlockNumber(); j++) {
						Block block = htmlWrapper.getBlock(j);
						if(!contentMatch && block.HtmlPath.equals(mostPossibleContentPath))
						{
							if(block.ImportanceFactor > lastImportantFactor)
							{
								record.contentHtmlPathString = mostPossibleContentPath;
								record.NewsContent = htmlWrapper.extractBlockText(block);
								lastImportantFactor = block.ImportanceFactor;
								updateBodyIndex = j;
							}		
						}	
						if(block.ImportanceFactor > lastImportantFactor)
						{
							lastImportantFactor = block.ImportanceFactor;
							originBodyIndex = j;
						}
					}
					if(updateBodyIndex < 0)
					{
						bodyIndex = originBodyIndex;
					}
					else
					{
						bodyIndex = updateBodyIndex;
					}
					
					for (int m = bodyIndex - 1;m >= 0; m--) {
						Block block = htmlWrapper.getBlock(m);
						if(!timeMatch && block.HtmlPath.equals(mostPossibleTimePath))
						{
							Pattern timePattern = Pattern.compile("\\d{4}\\D+?[0,1]?\\d\\D+?[0-3]?\\d\\D+?\\d?\\d\\D+?\\d?\\d");
							String timeContentString = htmlWrapper.extractBlockText(block);
							Matcher timeMatcher = timePattern.matcher(timeContentString);
							if(timeMatcher.find())
							{
								String timeString =  timeMatcher.group();
								Pattern intPattern = Pattern.compile("\\d{1,4}");
								Matcher intMatcher = intPattern.matcher(timeString);
								intMatcher.find();
								int year = Integer.parseInt(intMatcher.group()) ;
								intMatcher.find();
								int month = Integer.parseInt(intMatcher.group()) ;
								intMatcher.find();
								int day = Integer.parseInt(intMatcher.group()) ;
								intMatcher.find();
								int hours = Integer.parseInt(intMatcher.group()) ;
								intMatcher.find();
								int minutes = Integer.parseInt(intMatcher.group()) ;
								int sec = 0;
								if(intMatcher.find())
								{
									sec = Integer.parseInt(intMatcher.group());
								}
								record.timeHtmlPathString = block.HtmlPath;
								record.NewsTime = new GregorianCalendar(year, month, day, hours,minutes, sec);
								updateTimeIndex = m;
								break;
							}
						}
						Pattern timePattern = Pattern.compile("\\d{4}\\D+?[0,1]?\\d\\D+?[0-3]?\\d\\D+?\\d?\\d\\D+?\\d?\\d");
						String timeContentString = htmlWrapper.extractBlockText(block);
						Matcher timeMatcher = timePattern.matcher(timeContentString);
						if(timeMatcher.find())
						{
							originTimeIndex = m;
							break;
						}
					}
					if(updateTimeIndex < 0)
					{
						timeIndex = originTimeIndex;
					}
					else
					{
						timeIndex = updateTimeIndex;
					}
					
					Block block = htmlWrapper.getBlock(timeIndex);
					for(int n = block.StartIndex - 1; n >= 0; n--) {
						AdvanceTextNode titleTextNode = htmlWrapper.getNode(n);
						if(!titleMatch && titleTextNode.exactHtmlPath.equals(mostPossibleTitlePath))
						{
							record.titleHtmlPathString = mostPossibleTitlePath;
							record.NewsTitle = titleTextNode.getText();
							break;
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			}
		}
	}
	public void saveRecord()
	{
		for (int i = 0; i < mRecordList.size(); i++) {
			NewsRecord resultNewsRecord = mRecordList.get(i);
			String pathString = mFilenameList.get(resultNewsRecord.Index) + ".txt";
			try {
				BufferedWriter writer = new BufferedWriter
						(
								new OutputStreamWriter
								(
								new FileOutputStream(pathString),
								"utf-8"
								)
						);
				//DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				//Date date = resultNewsRecord.NewsTime.getTime();
				String timeString = resultNewsRecord.NewsTime == null?null:resultNewsRecord.NewsTime.getTime().getYear() + 1900
						+ "-"
						+ (resultNewsRecord.NewsTime.getTime().getMonth() + 1)
						+ "-"
						+ resultNewsRecord.NewsTime.getTime().getDate()
						+ " "
						+ resultNewsRecord.NewsTime.getTime().getHours()
						+ ":"
						+ resultNewsRecord.NewsTime.getTime().getMinutes()
						+ ":"
						+ resultNewsRecord.NewsTime.getTime().getSeconds();
				writer.write("Title:" + resultNewsRecord.NewsTitle + "(" + resultNewsRecord.titleHtmlPathString + ")");
				writer.newLine();
				writer.write("Time:" +  timeString +  "(" + resultNewsRecord.timeHtmlPathString + ")");
				writer.newLine();
				writer.write("Content:" + resultNewsRecord.NewsContent + "(" + resultNewsRecord.contentHtmlPathString + ")");
				writer.newLine();
				writer.close();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
