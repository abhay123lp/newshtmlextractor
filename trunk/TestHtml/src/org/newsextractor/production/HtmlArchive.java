package org.newsextractor.production;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.tags.*;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.newsextractor.filter.TextExtractionFilter;
import org.newsextractor.io.HtmlFileReader;
import org.newsextractor.nodes.AdvanceTextNode;
import org.newsextractor.production.HtmlWrapper.Block;
import org.newsextractor.textprocess.HtmlManipulator;
import org.newsextractor.util.HtmlPath;
import org.newsextractor.util.NewsRecord;

/**
 * 
 * @author firstprayer
 * This class contains all the methods we need to process html files and extract the information we want
 * including:initialize,extraction,update,and save
 */
public class HtmlArchive 
{
	/**
	 * We've define TextRatio of a node as the ratio of non-link text count to overall text count
	 * If a node has a TextRatio lower than the sLeastRatioToBeContent,then all its springs that are
	 * text nodes won't be consider by us when we extract the information we need
	 */
	final static protected float sLeastRatioToBeContent = 0.3f;
	/**
	 * We don't need this for now
	 */
	//private String mUrlPattern;
	/**
	 * The name
	 */
	public String ArchiveName;
	/**
	 * Indicator the sub-directory of the cluster
	 */
	private File mDirectory;
	/**
	 * The path of the all the html files in this cluster
	 */
	private Vector<String> mFilenameList = new Vector<String>();
	/**
	 * The corresponding records for all files
	 */
	private Vector<NewsRecord> mRecordList = new Vector<NewsRecord>();
	/**
	 * The corresponding wrapper for all files
	 */
	private Vector<HtmlWrapper> mWrapperList = new Vector<HtmlWrapper>();
	/**
	 * Maintaining too many wrappers will cause the heap to crash,so we have to release them 
	 */
	public void ReleaseUselessSpace()
	{
		mWrapperList.clear();
	}
	/**
	 * 
	 * @return the number of files in this cluster
	 */
	public int getFilesNumber()
	{
		return mFilenameList.size();
	}
	
	/**
	 * Basic constructor
	 * @param file
	 * @param name
	 */
	public HtmlArchive(File file, String name) {
		// TODO Auto-generated constructor stub
		mDirectory = file;
		ArchiveName = name;
	}
	/**
	 * retrieve the paths of all files under this directory,store it for later use
	 * @return whether the operation is successful
	 */
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
	/**
	 * For each htmls under one cluster,create and initialize a HtmlWrapper Object with it,and
	 * store the object in memory
	 */
	public void Initialize()
	{
		for (int i = 0; i < mFilenameList.size(); i++) 
		{
			try {
				//first,we retrieve the text of the file,rule out useless content
				HtmlWrapper htmlWrapper = new HtmlWrapper();
				String text = HtmlFileReader.getHtmlContentSafely(mFilenameList.get(i));
				text = HtmlManipulator.getSingleton().RuleOutUselessText(text);
				//use the text to init a parser
				Parser parser = new Parser(text);
				//here we use the class defined by us as the prototype of textnode in the factory
				PrototypicalNodeFactory prototypicalNodeFactory = new PrototypicalNodeFactory();
				prototypicalNodeFactory.setTextPrototype(new AdvanceTextNode(null,i,i));
				parser.setNodeFactory(prototypicalNodeFactory);
				
				//while we do the filter job,we also initialize the fields we need
				NodeList nodeList = parser.extractAllNodesThatMatch(new TextExtractionFilter());
				Node[] nodes = nodeList.toNodeArray();
				
				for (int j = 0; j < nodes.length; j++) 
				{
					if(nodes[j] instanceof TextNode)
					{
						//the following steps rules out texts that are nearly impossible to be content
						//because they are under a node that has a very low text ratio
						if(((AdvanceTextNode)nodes[j]).getWithinHref() == false)
						{
							AbstractNode parentNode = (AbstractNode) nodes[j].getParent();
							if(parentNode != null && parentNode.getNormalTextRatio() > sLeastRatioToBeContent)
							{
								htmlWrapper.addNode((AdvanceTextNode)nodes[j]);
							}
							
						}
						else {
							Node linkNode = nodes[j];
							while(((linkNode = linkNode.getParent()) instanceof LinkTag) == false && linkNode != null);
							if(linkNode == null)
							{
								System.out.println("parser error!");
							}
							else {
								linkNode = linkNode.getParent();
								if(linkNode != null)
								{
									float ratio = ((AbstractNode)linkNode).getNormalTextRatio();
									if(ratio > sLeastRatioToBeContent)
										htmlWrapper.addNode((AdvanceTextNode)nodes[j]);
								}
								
							}
							
						}
						
					}
				}
				//store it in the memory
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
	/**
	 * We don't use it for now
	 * This function was designed for rule out text that appears many times within one cluster
	 * But it seems not to work well
	 */
	@Deprecated
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
	/**
	 * According to the nodes stored within each wrapper,extract the information we need
	 */
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
	/**
	 * This function is used to rectify errors that occasionally happen
	 * We use the similarity between the html files within one cluster to update some records
	 * that doesn't match with the similarity
	 * It's deprecated for now because the algorithm to cluster htmls is not good enough
	 */
	@Deprecated
	public void UpdateRecord()
	{
		final class PathPossibility
		{
			public HtmlPath path = null;
			public int corresponseCount = 1;
			public PathPossibility(HtmlPath str)
			{
				path = new HtmlPath(str);
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
				if(ppPathPossibility.path.isCompletelyEqualTo(record.titleHtmlPath))
				{
					found = true;
					ppPathPossibility.corresponseCount++;
					break;
				}
			}
			if(!found)
			{
				if(record.titleHtmlPath != null)
					titlePathAccumulator.add(new PathPossibility(record.titleHtmlPath));
			}
			
			found = false;
			for (int j = 0; j < timePathAccumulator.size(); j++) {
				PathPossibility ppPathPossibility = timePathAccumulator.get(j);
				if(ppPathPossibility.path.isCompletelyEqualTo(record.timeHtmlPath))
				{
					found = true;
					ppPathPossibility.corresponseCount++;
					break;
				}
			}
			if(!found)
			{
				if(record.timeHtmlPath != null)
				timePathAccumulator.add(new PathPossibility(record.timeHtmlPath));
			}
			
			found = false;
			for (int j = 0; j < contentPathAccumulator.size(); j++) {
				PathPossibility ppPathPossibility = contentPathAccumulator.get(j);
				if(ppPathPossibility.path.isPartlyEqualTo(record.contentHtmlPath))
				{
					found = true;
					ppPathPossibility.corresponseCount++;
					break;
				}
			}
			if(!found)
			{
				if(record.contentHtmlPath != null)
				contentPathAccumulator.add(new PathPossibility(record.contentHtmlPath));
			}
			found = false;
			for (int j = 0; j < sourcePathAccumulator.size(); j++) {
				PathPossibility ppPathPossibility = sourcePathAccumulator.get(j);
				if(ppPathPossibility.path.isCompletelyEqualTo(record.sourceHtmlPath))
				{
					found = true;
					ppPathPossibility.corresponseCount++;
					break;
				}
			}
			if(!found)
			{
				if(record.sourceHtmlPath != null)
					sourcePathAccumulator.add(new PathPossibility(record.sourceHtmlPath));
			}
		}
		//then we figure out which path should most possibly be the right one
		HtmlPath mostPossibleTitlePath = null;
		HtmlPath mostPossibleTimePath = null;
		HtmlPath mostPossibleContentPath = null;
		HtmlPath mostPossibleSourcePath = null;
		int mostNum;
		mostNum = 0;
		for (int j = 0; j < titlePathAccumulator.size(); j++) {
			if(titlePathAccumulator.get(j).corresponseCount > mostNum)
			{
				mostNum = titlePathAccumulator.get(j).corresponseCount;
				mostPossibleTitlePath = titlePathAccumulator.get(j).path;
			}
		}
		
		mostNum = 0;
		for (int j = 0; j < sourcePathAccumulator.size(); j++) {
			if(sourcePathAccumulator.get(j).corresponseCount > mostNum)
			{
				mostNum = sourcePathAccumulator.get(j).corresponseCount;
				mostPossibleSourcePath = sourcePathAccumulator.get(j).path;
			}
		}
		
		mostNum = 0;
		for (int j = 0; j < timePathAccumulator.size(); j++) {
			if(timePathAccumulator.get(j).corresponseCount > mostNum)
			{
				mostNum = timePathAccumulator.get(j).corresponseCount;
				mostPossibleTimePath = timePathAccumulator.get(j).path;
			}
		}
		
		mostNum = 0;
		for (int j = 0; j < contentPathAccumulator.size(); j++) {
			if(contentPathAccumulator.get(j).corresponseCount > mostNum)
			{
				mostNum = contentPathAccumulator.get(j).corresponseCount;
				mostPossibleContentPath = contentPathAccumulator.get(j).path;
			}
		}
		
		//after this ,we will check each record and see whether their path is right
		for (int i = 0; i < mRecordList.size(); i++) {
			NewsRecord record = mRecordList.get(i);
			boolean contentMatch = (record.contentHtmlPath == null)?false:record.contentHtmlPath.isCompletelyEqualTo(mostPossibleContentPath);
			boolean titleMatch = (record.titleHtmlPath == null)?false:record.titleHtmlPath.isCompletelyEqualTo(mostPossibleTitlePath);
			boolean timeMatch = (record.timeHtmlPath == null)?false:record.timeHtmlPath.isCompletelyEqualTo(mostPossibleTimePath);
			boolean sourceMatch = record.sourceHtmlPath == null?false:record.sourceHtmlPath.isCompletelyEqualTo(mostPossibleSourcePath);
			if(!contentMatch || !timeMatch || !titleMatch || !sourceMatch)
			{
				//Ok,we need to update it now
				HtmlWrapper htmlWrapper = new HtmlWrapper();
				
				try {
					String text = HtmlFileReader.getHtmlContentSafely(mFilenameList.get(record.Index));
					text = HtmlManipulator.getSingleton().RuleOutUselessText(text);
					
					Parser parser = new Parser(text);
					PrototypicalNodeFactory prototypicalNodeFactory = new PrototypicalNodeFactory();
					prototypicalNodeFactory.setTextPrototype(new AdvanceTextNode(null,i,i));
					parser.setNodeFactory(prototypicalNodeFactory);
					NodeList nodeList = parser.extractAllNodesThatMatch(new TextExtractionFilter());
					Node[] nodes = nodeList.toNodeArray();
					//clear the wrapper to new nodes to be added into and processed
					htmlWrapper.clear();
					for (int j = 0; j < nodes.length; j++) 
					{
						if(nodes[j] instanceof TextNode)
						{
							if(((AdvanceTextNode)nodes[j]).getWithinHref() == false)
							{
								AbstractNode parentNode = (AbstractNode) nodes[j].getParent();
								if(parentNode != null && parentNode.getNormalTextRatio() > sLeastRatioToBeContent)
								{
									htmlWrapper.addNode((AdvanceTextNode)nodes[j]);
								}
								
							}
							else {
								Node linkNode = nodes[j];
								while(((linkNode = linkNode.getParent()) instanceof LinkTag) == false && linkNode != null);
								if(linkNode == null)
								{
									System.out.println("parser error!");
								}
								else {
									linkNode = linkNode.getParent();
									if(linkNode != null)
									{
										float ratio = ((AbstractNode)linkNode).getNormalTextRatio();
										if(ratio > sLeastRatioToBeContent)
											htmlWrapper.addNode((AdvanceTextNode)nodes[j]);
									}
									
								}
								
							}
						}
					}
					htmlWrapper.InitializeBlockList();
					//update the content following the htmlpath(if not null)
					//for content body,if we are going to update it, we search for the first block to match the html path
					//if we don't need to update it or we cannot find the match block,we set the bodyIndex to the original block index
					int bodyIndex = -1;
					if(mostPossibleContentPath !=  null && !contentMatch)
					{
						int lastImportantFactor = 0;
						
						for (int j = 0; j < htmlWrapper.getBlockNumber(); j++) {
							Block block = htmlWrapper.getBlock(j);
							
								if(!contentMatch && block.mHtmlPath.isCompletelyEqualTo(mostPossibleContentPath))
								{
									if(block.ImportanceFactor > lastImportantFactor)
									{
										lastImportantFactor = block.ImportanceFactor;
										bodyIndex = j;
									}	
								}
						}
						if(bodyIndex != -1)
						{
							record.contentHtmlPath = mostPossibleContentPath;
							record.NewsContent = htmlWrapper.extractBlockText(htmlWrapper.getBlock(bodyIndex));
						}
					}
					if(bodyIndex == -1)
					{
						//find the original one
						for (int j = 0; j < htmlWrapper.getBlockNumber(); j++) {
							Block block = htmlWrapper.getBlock(j);
							if(block.mHtmlPath.isCompletelyEqualTo(record.contentHtmlPath))
							{
								bodyIndex = j;
								break;
							}
					
						}
					}
					//for time,we simply find the first matched textnode if we want it updated
					if(mostPossibleTimePath != null && !timeMatch)
					{
						for (int m = 0;m < htmlWrapper.getNodeNumber(); m++) {
							AdvanceTextNode node = htmlWrapper.getNode(m);
							if(node.getWithinHref())
								continue;
							
							if(node.mHtmlPath.isCompletelyEqualTo(mostPossibleTimePath))
							{
								GregorianCalendar timeCalendar = HtmlManipulator.getSingleton().extractTimeFromString(node.getText());
								if(timeCalendar != null)
								{
									record.NewsTime = timeCalendar;
									record.timeHtmlPath = mostPossibleTimePath;
									break;
								}
							}
						}
					}
					
					//for title,it's quite similar to the body,since we need both of them to search for the source
					int titleIndex = -1;
					if(mostPossibleTitlePath != null && !titleMatch)
					{
						for (int m = 0;m < htmlWrapper.getNodeNumber(); m++) {
							AdvanceTextNode node = htmlWrapper.getNode(m);
							if(node.getWithinHref())
								continue;
							
							if(node.mHtmlPath.isCompletelyEqualTo(mostPossibleTitlePath))
							{
								
									titleIndex = m;
									record.NewsTitle = ((AdvanceTextNode)node).getText();
									record.titleHtmlPath = mostPossibleTitlePath;
									break;
							}
						}
					}
					if(titleIndex == -1)
					{
						if(record.titleHtmlPath != null)
						{
							for (int k = 0; k < htmlWrapper.getNodeNumber(); k++) {
								AdvanceTextNode node = htmlWrapper.getNode(k);
								if(node.mHtmlPath.isCompletelyEqualTo(record.titleHtmlPath))
								{
									titleIndex = k;
									break;
								}
							}
						}
						
					}
					if(mostPossibleSourcePath != null)
					{
						if(!sourceMatch)
						{
							if(mostPossibleSourcePath.isLink())//is a link
							{
								//just search the one that strictly match the path
								for (int m = 0;m < htmlWrapper.getNodeNumber(); m++) {
									AdvanceTextNode node = htmlWrapper.getNode(m);
									
									if(node.mHtmlPath.isCompletelyEqualTo(mostPossibleSourcePath))
									{
											String tempString = ((AdvanceTextNode)node).getText();
											if(HtmlManipulator.getSingleton().isSourceString(tempString))//for ensurance
											{
												record.NewsSource = ((AdvanceTextNode)node).getText();
												record.sourceHtmlPath = mostPossibleSourcePath;
												break;
											}
											
									}
								}
							}// is a link ends
							else  //not a link
							{
								//first,find the text node,and analyze its text,try to extract the source from it
								for (int m = 0;m < htmlWrapper.getNodeNumber(); m++) 
								{
									AdvanceTextNode node = htmlWrapper.getNode(m);
									
									if(node.mHtmlPath.isCompletelyEqualTo(mostPossibleSourcePath))
									{
										String tempString = ((AdvanceTextNode)node).getText();
										Pattern sourcePattern = Pattern.compile("来[源于自]于?[:：]");
										Matcher sourceMatcher = sourcePattern.matcher(tempString);
										
										boolean found = false;
										if(sourceMatcher.find())
										{
											int e = sourceMatcher.end();
											if(tempString.length() - e >= 2)//something behind
											{
												String subString = tempString.substring(e);
												Pattern chinesePattern = Pattern.compile("[\\u4E00-\\u9FA5]");
												Matcher chineseMatcher = chinesePattern.matcher(subString);
												if(chineseMatcher.find())
												{
													int sourceend;
													for (sourceend = 0; sourceend < subString.length();sourceend++) 
													{
														if(subString.charAt(sourceend) == ' ' || (int)subString.charAt(sourceend) == 12288)
														{
															break;
														}
													}
													String sourceString = subString.substring(0,sourceend).trim();
													if(HtmlManipulator.getSingleton().isSourceString(sourceString))
													{
														found = true;
														record.NewsSource = sourceString.trim();
														record.sourceHtmlPath = node.mHtmlPath;
													}
												}
											}
										}//is there indicator ends
										if(!found)//found no indicator 
										{
											tempString  = node.getText();
											String[] splitedStrings = tempString.split("[\\s\\u3000]+");
											for (int j = 0; j < splitedStrings.length; j++) 
											{
												tempString = splitedStrings[j];
												if(found = HtmlManipulator.getSingleton().isSourceString(tempString))
												{
													record.NewsSource = tempString;
													record.sourceHtmlPath = node.mHtmlPath;
													found = true;
													break;
												}
											}
										}//found no indicator ends
									}//path string matches ends
								}//cycle the nodes ends
							}//not a link ends
							
						}//source not matched
					}//most possible path not null
					
					
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
	/**
	 * Here we save all of the records on local files
	 * the filename will be :filename of the html + '.txt'
	 */
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
					if(resultNewsRecord.isValid() == false)
					{
						writer.write("invalid results");
						writer.close();
						continue;
					}
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
					writer.write("Title:" + resultNewsRecord.NewsTitle + "(" + resultNewsRecord.titleHtmlPath + ")");
					writer.newLine();
					writer.write("Time:" +  timeString +  "(" + resultNewsRecord.timeHtmlPath + ")");
					writer.newLine();
					writer.write("Source:" + resultNewsRecord.NewsSource + "(" + resultNewsRecord.sourceHtmlPath + ")");
					writer.newLine();
					writer.write("Content:" + resultNewsRecord.NewsContent + "(" + resultNewsRecord.contentHtmlPath + ")");
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
	public void locateROI()
	{
		
	}
}
