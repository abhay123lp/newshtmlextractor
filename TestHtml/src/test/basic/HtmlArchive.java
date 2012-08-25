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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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

import test.classifier.SourceIdentifier;
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
	private Vector<HtmlWrapper> mWrapperList = new Vector<HtmlWrapper>();
	public void ReleaseUselessSpace()
	{
		mWrapperList.clear();
	}
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
	public void Initialize()
	{
		for (int i = 0; i < mFilenameList.size(); i++) 
		{
			try {
				HtmlWrapper htmlWrapper = new HtmlWrapper();
				String text = getHtmlContentSafely(mFilenameList.get(i));
				text = HtmlManipulator.RuleOutUselessText(text);
				
				Parser parser = new Parser(text);
				PrototypicalNodeFactory prototypicalNodeFactory = new PrototypicalNodeFactory();
				prototypicalNodeFactory.setTextPrototype(new AdvanceTextNode(null,i,i));
				parser.setNodeFactory(prototypicalNodeFactory);
				NodeList nodeList = parser.extractAllNodesThatMatch(new MyNodeFilter());
				Node[] nodes = nodeList.toNodeArray();
				//clear the wrapper to new nodes to be added into and processed
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
				
				this.mWrapperList.add(htmlWrapper);
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
	public void ruleOutUselessText()
	{
		final class TextRecord
		{
			public int wrapperIndex;
			public int nodeIndex;
			public TextRecord(int w,int n)
			{
				wrapperIndex = w;
				nodeIndex = n;
			}
		}
		if(this.mWrapperList.size() < 10) // too few htmls
			return;
		Hashtable<String,Vector<TextRecord>> stringHash = new Hashtable<String,Vector<TextRecord>>();
		
		for (int i = 0; i < mWrapperList.size(); i++) {
			HtmlWrapper htmlWrapper = mWrapperList.get(i);
			for (int j = 0; j < htmlWrapper.getNodeNumber(); j++) {
				AdvanceTextNode node = htmlWrapper.getNode(j);
				String text = node.getText();
				Vector<TextRecord> record = stringHash.get(text);
				if(record != null)
				{
					record.add(new TextRecord(i, j));
				}
				else {
					Vector<TextRecord> vector = new Vector<>();
					vector.add(new TextRecord(i, j));
					stringHash.put(text, vector);
				}
			}
		}
		Vector<TextRecord> theRecordToBeRuleOutVector = new Vector<>();
		float leastRatio = 0.8f;
		int totalNumber = mWrapperList.size();
		Enumeration enumeration = stringHash.elements();
		while(enumeration.hasMoreElements())
		{
			Vector<TextRecord> records = (Vector<TextRecord>) enumeration.nextElement();
			if((float)records.size() / totalNumber > leastRatio)
			{
				
				theRecordToBeRuleOutVector.addAll(records);
			}
		}
		Collections.sort(theRecordToBeRuleOutVector,new Comparator<TextRecord>() {

			
			@Override
			public int compare(TextRecord o1, TextRecord o2) {
				// TODO Auto-generated method stub
				
				if(o1.wrapperIndex != o2.wrapperIndex)
					return o2.wrapperIndex - o1.wrapperIndex;//descending order
				if(o1.nodeIndex != o2.nodeIndex)
					return o2.nodeIndex - o1.nodeIndex;
				return 0;
			}
			
		});
			Iterator<TextRecord> recordIterator = theRecordToBeRuleOutVector.iterator();
			while(recordIterator.hasNext())
			{
				TextRecord record = recordIterator.next();
				try {
					mWrapperList.get(record.wrapperIndex).deleteTextNode(record.nodeIndex);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
			}
	}
	public void ExtractRecord()
	{
		
		for (int i = 0; i < mWrapperList.size(); i++) 
		{
			try{
				HtmlWrapper htmlWrapper = mWrapperList.get(i);
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
		Vector<PathPossibility> sourcePathAccumulator = new Vector<PathPossibility>();
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
			found = false;
			for (int j = 0; j < sourcePathAccumulator.size(); j++) {
				PathPossibility ppPathPossibility = sourcePathAccumulator.get(j);
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
					sourcePathAccumulator.add(new PathPossibility(record.contentHtmlPathString));
			}
		}
		//then we figure out which path should most possibly be the right one
		String mostPossibleTitlePath = null;
		String mostPossibleTimePath = null;
		String mostPossibleContentPath = null;
		String mostPossibleSourcePath = null;
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
		for (int j = 0; j < sourcePathAccumulator.size(); j++) {
			if(sourcePathAccumulator.get(j).corresponseCount > mostNum)
			{
				mostNum = sourcePathAccumulator.get(j).corresponseCount;
				mostPossibleSourcePath = sourcePathAccumulator.get(j).pathString;
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
			boolean sourceMatch = record.sourceHtmlPathString == null?false:record.sourceHtmlPathString.equals(mostPossibleSourcePath);
			if(!contentMatch || !timeMatch || !titleMatch || !sourceMatch)
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
					int originalMostImportantFactor = 0;
					for (int j = 0; j < htmlWrapper.getBlockNumber(); j++) {
						Block block = htmlWrapper.getBlock(j);
						
							if(!contentMatch && block.HtmlPath.equals(mostPossibleContentPath))
							{
								if(block.ImportanceFactor > lastImportantFactor)
								{

									lastImportantFactor = block.ImportanceFactor;
									bodyIndex = j;
								}	
							}
						if(block.ImportanceFactor > originalMostImportantFactor)
						{
							originalMostImportantFactor = block.ImportanceFactor;
							originBodyIndex = j;
						}
					}
					if(bodyIndex == -1)
					{
						bodyIndex = originBodyIndex;
						record.contentHtmlPathString = htmlWrapper.getBlock(bodyIndex).HtmlPath;
						record.NewsContent = htmlWrapper.extractBlockText(htmlWrapper.getBlock(bodyIndex));
					}
					else if(!contentMatch)
					{
						record.contentHtmlPathString = mostPossibleContentPath;
						record.NewsContent = htmlWrapper.extractBlockText(htmlWrapper.getBlock(bodyIndex));
					}
					timeIndex = htmlWrapper.getBlock(bodyIndex).StartIndex;
					for (int m = timeIndex - 1;m >= 0; m--) {
						AdvanceTextNode node = htmlWrapper.getNode(m);
						if(node.getWithinHref())
							continue;
						if(timeMatch)
						{
							GregorianCalendar timeCalendar = HtmlWrapper.extractTimeFromString(node.getText());
							if(timeCalendar != null)
							{
								timeIndex = m;
								break;
							}
						}
						else {
							if(node.getHtmlPath().equals(mostPossibleTimePath))
							{
								GregorianCalendar timeCalendar = HtmlWrapper.extractTimeFromString(node.getText());
								if(timeCalendar != null)
								{
									timeIndex = m;
									record.NewsTime = timeCalendar;
									record.timeHtmlPathString = mostPossibleTimePath;
									break;
								}
							}
						}
					}
					
					int titleIndex = -1;
					for(int n = timeIndex - 1; n >= 0; n--) {
						AdvanceTextNode titleTextNode = htmlWrapper.getNode(n);
						if(titleTextNode.getWithinHref())
							continue;
						if(!titleMatch)
						{
							if(titleTextNode.exactHtmlPath.equals(mostPossibleTitlePath))
							{
								record.titleHtmlPathString = mostPossibleTitlePath;
								record.NewsTitle = titleTextNode.getText();
								titleIndex = n;
								break;
							}
							
						}
						else {
							if(titleTextNode.getText().equals(record.NewsTitle))
							{
								titleIndex = n;
								break;
							}
						}
					}
					
					if(!sourceMatch)
					{
						AdvanceTextNode tempAdvanceTextNode = null;
						Pattern sourcePattern = Pattern.compile("来[源于自]于?[:：]");
						boolean found = false;
						int endlimit = htmlWrapper.getBlock(bodyIndex).StartIndex; //the limitation of the range of searching source
						for (int i1 = endlimit - 1; i1 > titleIndex; i1--) {
							tempAdvanceTextNode = htmlWrapper.getNode(i1);
							String tempString = tempAdvanceTextNode.getText();
							Matcher sourceMatcher = sourcePattern.matcher(tempString);
							if(sourceMatcher.find())
							{
								int e = sourceMatcher.end();
								if(tempString.length() - e > 3)//something behind
								{
									String subString = tempString.substring(e + 1);
									Pattern chinesePattern = Pattern.compile("[\u4E00-\u9FA5]");
									Matcher chineseMatcher = chinesePattern.matcher(subString);
									if(chineseMatcher.find())
									{
										int sourceend;
										for (sourceend = 0; sourceend < subString.length();sourceend++) {
											//System.out.print((int)subString.charAt(sourceend));
											if(subString.charAt(sourceend) == ' ' || (int)subString.charAt(sourceend) == 12288)
											{
												break;
											}
												
										}
										
										String sourceString = subString.substring(0,sourceend);
										found = true;
										record.NewsSource = sourceString.trim();
										record.sourceHtmlPathString = tempAdvanceTextNode.getExactHtmlPath();
										if(record.sourceHtmlPathString.equals(mostPossibleSourcePath))
											break;
									}
								}
								else //maybe in the next node
								{
									if(i1 < endlimit - 1)
									{
										tempAdvanceTextNode = htmlWrapper.getNode(i1 + 1);
										//if(tempAdvanceTextNode.getWithinHref())
										//{
										found = true;
										record.NewsSource = tempAdvanceTextNode.getText();
										record.sourceHtmlPathString = tempAdvanceTextNode.getExactHtmlPath();
										if(record.sourceHtmlPathString.equals(mostPossibleSourcePath))
											break;
										//}
									}
								}
								
							}
						}
						if(!found) // haven't found any specifier word
						{
							for (int i1 = endlimit - 1; i1 > titleIndex; i1--) 
							{
								tempAdvanceTextNode = htmlWrapper.getNode(i1);
								String tempString = tempAdvanceTextNode.getText();
								String[] splitedStrings = tempString.split("[\\s ]+");
								for (int j = 0; j < splitedStrings.length; j++) 
								{
									tempString = splitedStrings[j];
									if(found = SourceIdentifier.isSourceString(tempString))
									{
										record.NewsSource = tempString;
										record.sourceHtmlPathString = tempAdvanceTextNode.getExactHtmlPath();// HtmlPath();
										if(record.sourceHtmlPathString.equals(mostPossibleSourcePath))
											break;
									}
								}
								if(found)
									break;
							}
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
				writer.write("Source:" + resultNewsRecord.NewsSource + "(" + resultNewsRecord.sourceHtmlPathString + ")");
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
