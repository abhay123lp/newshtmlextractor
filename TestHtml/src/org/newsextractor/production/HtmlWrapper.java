package org.newsextractor.production;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.*;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.newsextractor.nodes.AdvanceTextNode;
import org.newsextractor.textprocess.HtmlManipulator;
import org.newsextractor.util.HtmlPath;
import org.newsextractor.util.NewsRecord;

/**
 * 
 * @author firstprayer
 * This class is the one that really do the extraction job
 */
public class HtmlWrapper 
{
	/**
	 * 
	 * @author firstprayer
	 * A block is defined as the aggregation of adjacent text nodes with similar Html Path
	 */
	public class Block
	{
		public int StartIndex;
		public int EndIndex;
		/**
		 * We define ImportanceFactor as the sum of Chinese characters inside the block 
		 */
		public int ImportanceFactor;
		public int TextNumber;
		public HtmlPath mHtmlPath;
		public Block()
		{
		}
		public Block(int s,int e,int i,int tn)
		{
			StartIndex = s;
			EndIndex = e;
			ImportanceFactor = i;
			TextNumber = tn;
		}
	}
	/**
	 * 
	 */
	private Vector<Block> mBlockList = new Vector<Block>();
	private Vector<AdvanceTextNode> mTextNodes = new Vector<AdvanceTextNode>();
	/**
	 * 
	 * @param block
	 * extract the text content according to the block's startIndex and endIndex
	 * @return the text as String
	 * 
	 */
	public String extractBlockText(Block block)
	{
		String resultString = "";
		for (int i = block.StartIndex; i <= block.EndIndex; i++) {
			resultString += mTextNodes.get(i).getText() + "\n";
		}
		return resultString;
	}
	/**
	 * Clear the vectors
	 */
	public void clear()
	{
		mBlockList.clear();
		mTextNodes.clear();
	}
	/**
	 * Add node into the vecotr
	 * @param node
	 */
	public void addNode(AdvanceTextNode node)
	{
		mTextNodes.add(node);
	}
	/**
	 * 
	 * @param index
	 * @return the node in the index of vector
	 */
	public AdvanceTextNode getNode(int index)
	{
		return mTextNodes.get(index);
	}
	/**
	 * For another type of text nodes access
	 * @return the iterator
	 */
	public Iterator<AdvanceTextNode> getTextNodeElement()
	{
		return mTextNodes.iterator();
	}
	/**
	 * 
	 * @return the iterator of the block vector
	 */
	public Iterator<Block> getBlockElement()
	{
		return mBlockList.iterator();
	}
	/**
	 * 
	 * @return the count of nodes
	 */
	public int getNodeNumber()
	{
		return mTextNodes.size();
	}
	/**
	 * remove textnode
	 * @param index
	 */
	public void deleteTextNode(int index)
	{
		mTextNodes.removeElementAt(index);
	}
	/**
	 * 
	 * @param b
	 * add b into the vector
	 */
	public void addBlock(Block b)
	{
		mBlockList.add(b);
	}
	/**
	 * 
	 * @param index
	 * @return the block in index of block vector
	 */
	public Block getBlock(int index)
	{
		return mBlockList.get(index);
	}
	/**
	 * delete block at index
	 * @param index
	 */
	public void deleteBlock(int index)
	{
		mBlockList.removeElementAt(index);
	}
	/**
	 * 
	 * @return the number of blocks
	 */
	public int getBlockNumber()
	{
		return mBlockList.size();
	}
	/**
	 * when finished adding nodes to the text node vector,call this to init the block list
	 * so that we can find that which part of the html could mostly possibly be the content
	 */
	
	public void InitializeBlockList()
	{
		//for the threhold we set here,we need to figure out one question:
		//do we need to include all the content within the original html inside the blocks
		//or we just need to roughly locate the content,and we will fix the content up later by other methods
		//20120731 now I just do it in the first way
		//int theLeastCharacterNumberNeededToBeConsideredAsContent = 10;
		//int theLeastImportanceFactorForABlockToAddIntoList = 40;
		
		/**
		 * Build blocks by the definition of block
		 * See the definition of block above
		 */
		for (int i = 0; i < mTextNodes.size(); i++) 
		{
			AdvanceTextNode node = mTextNodes.get(i);
			if(node.getWithinHref())
				continue;
			Block block = new Block();
			block.StartIndex = i;
			block.TextNumber = 1;
			block.mHtmlPath = new HtmlPath(node.mHtmlPath);
			int length = node.getText().length();
			int j = i + 1;
			//we agregate all **consecutive** text nodes that has the **same htmlPath**
			int accumulateContinuousHrefNum = 0;
			while(j < mTextNodes.size())
			{
				AdvanceTextNode nextNode = mTextNodes.get(j);
				if(nextNode.getWithinHref())
				{
					if(accumulateContinuousHrefNum > 1)
					{
						j--;
						break;
					}
					j++;
					accumulateContinuousHrefNum++;
					continue;
				}
				accumulateContinuousHrefNum = 0;
				if(nextNode.mHtmlPath.isPartlyEqualTo(node.mHtmlPath))
				{
					//the length of chinese characters
					length += nextNode.getText().replaceAll("[^(\\u4E00-\\u9FA5)]", "").length();
					j++;
					block.TextNumber ++;
				}
				else {
					break;
				}
			}
			block.EndIndex = j - 1;
			block.ImportanceFactor = length;
			i = j - 1;
			mBlockList.add(block);
		}
	}
	/**
	 * when everything is ready,call this to figure out the information we want to 
	 * know from this html based on the blocks and textnodes
	 * @return
	 * @throws Exception
	 */
	public NewsRecord Manipulate() throws Exception
	{
		int indexOfMostPossibleBlock = -1;
		int lastImportantFactor = 0;
		//1. locate the content block: the one that contains the most chinese charactor
		for (int i = 0; i < mBlockList.size(); i++) {
			Block block = mBlockList.get(i);
			if(block.ImportanceFactor > lastImportantFactor)
			{
				lastImportantFactor = block.ImportanceFactor;
				indexOfMostPossibleBlock = i;
			}
		}
		if(indexOfMostPossibleBlock == -1)
			return null;
		NewsRecord resultNewsRecord = new NewsRecord();
		Block contentBlock = mBlockList.get(indexOfMostPossibleBlock);
		//for content,we simply concat all the text within the block
		String contentString = "";
		contentString = this.extractBlockText(contentBlock);
		resultNewsRecord.NewsContent = contentString;
		resultNewsRecord.contentHtmlPath = contentBlock.mHtmlPath;
		
		//for time, we use regex expression, and search all the text nodes before the content block
		//also, we assume that the right time is the closest time before the content block.
		AdvanceTextNode tempAdvanceTextNode, timeNode = null;
		int timeIndex = contentBlock.StartIndex;
		Pattern timePattern = Pattern.compile("\\d{4}\\D+?[0,1]?\\d\\D+?[0-3]?\\d\\D+?\\d?\\d\\D+?\\d?\\d");
		Matcher timeMatcher = null;
		for (int i = contentBlock.StartIndex; i >= 0; i--) {
			tempAdvanceTextNode = mTextNodes.get(i);
			if(tempAdvanceTextNode.getWithinHref())
				continue;
			timeMatcher = timePattern.matcher(tempAdvanceTextNode.getText());
			if(timeMatcher.find())
			{
				timeIndex = i;
				timeNode = tempAdvanceTextNode;
				break;
			}
		}
		if(timeNode == null)
		{
			//if it's not before the content,it might be after it
			for (int i = contentBlock.EndIndex + 1; i < this.getNodeNumber(); i++) {
				tempAdvanceTextNode = mTextNodes.get(i);
				if(tempAdvanceTextNode.getWithinHref())
					continue;
				timeMatcher = timePattern.matcher(tempAdvanceTextNode.getText());
				if(timeMatcher.find())
				{
					timeIndex = i;
					timeNode = tempAdvanceTextNode;
					break;
				}
			}
			if(timeNode == null)//still not found
			{
				resultNewsRecord.NewsTime = null;
				resultNewsRecord.timeHtmlPath = null;
			}
			else {
				resultNewsRecord.NewsTime = HtmlManipulator.getSingleton().extractTimeFromString(timeNode.getText());
				resultNewsRecord.timeHtmlPath = timeNode.mHtmlPath;
			}
		}
		else {
			resultNewsRecord.NewsTime = HtmlManipulator.getSingleton().extractTimeFromString(timeNode.getText());
			resultNewsRecord.timeHtmlPath = timeNode.mHtmlPath;
		}
		/**
		 * For title,we search from text nodes before the timeNode
		 * Or the start node of the content block if the timeNode is behind the content
		 */
		AdvanceTextNode titleTextNode = null;int titleIndex = -1;
		Pattern titlePattern = Pattern.compile("[\\u4E00-\\u9FA5]{2,}.*?[\\u4E00-\\u9FA5]{2,}");
		Matcher titleMatcher = null;
		int startIndex = timeIndex < contentBlock.StartIndex?timeIndex:contentBlock.StartIndex;
		for (int i = startIndex - 1; i >= 0; i--) {
			tempAdvanceTextNode = mTextNodes.get(i);
			if(tempAdvanceTextNode.getWithinHref())
				continue;
			titleMatcher = titlePattern.matcher(tempAdvanceTextNode.getText());
			if(titleMatcher.find())
			{
				titleIndex = i;
				titleTextNode = tempAdvanceTextNode;
				break;
			}
		}
		if(titleTextNode == null)
			resultNewsRecord.NewsTitle = null;
		else {
			resultNewsRecord.titleHtmlPath = titleTextNode.mHtmlPath;
			resultNewsRecord.NewsTitle = titleTextNode.getText();
		}
		
		//Now for source,we search text nodes between content and title
		//and match the one closest to the body
		resultNewsRecord.NewsSource = null;
		Pattern sourcePattern = Pattern.compile("来[源于自]于?[:：]");
		for (int i = contentBlock.StartIndex - 1; i > titleIndex; i--) {
			tempAdvanceTextNode = mTextNodes.get(i);
			String tempString = tempAdvanceTextNode.getText();
			Matcher sourceMatcher = sourcePattern.matcher(tempString);
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
						for (sourceend = 0; sourceend < subString.length();sourceend++) {
							//System.out.print((int)subString.charAt(sourceend));
							if(subString.charAt(sourceend) == ' ' || (int)subString.charAt(sourceend) == 12288)
							{
								break;
							}
								
						}
						
						String sourceString = subString.substring(0,sourceend);
						resultNewsRecord.NewsSource = sourceString.trim();
						resultNewsRecord.sourceHtmlPath = tempAdvanceTextNode.mHtmlPath;
					}
				}
				else //maybe in the next node
				{
					if(i < contentBlock.StartIndex - 1)
					{
						tempAdvanceTextNode = mTextNodes.get(i + 1);
						if(tempAdvanceTextNode.getWithinHref())
						{
							resultNewsRecord.NewsSource = tempAdvanceTextNode.getText();
							resultNewsRecord.sourceHtmlPath = tempAdvanceTextNode.mHtmlPath;
						}
					}
				}
				
			}
		}
		if(resultNewsRecord.NewsSource == null) // haven't found any specifier word between the title and body
		{
			//try to search from the bottom
			boolean found = false;
			for (int i = contentBlock.EndIndex + 1; i < this.getNodeNumber(); i++) 
			{
				tempAdvanceTextNode = mTextNodes.get(i);
				String tempString = tempAdvanceTextNode.getText();
				Matcher sourceMatcher = sourcePattern.matcher(tempString);
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
							for (sourceend = 0; sourceend < subString.length();sourceend++) {
								if(subString.charAt(sourceend) == ' ' || (int)subString.charAt(sourceend) == 12288)
								{
									break;
								}
									
							}
							
							String sourceString = subString.substring(0,sourceend);
							resultNewsRecord.NewsSource = sourceString.trim();
							resultNewsRecord.sourceHtmlPath = tempAdvanceTextNode.mHtmlPath;
						}
					}
					else //maybe in the next node
					{
						if(i < this.getNodeNumber() - 1)
						{
							tempAdvanceTextNode = mTextNodes.get(i + 1);
							if(tempAdvanceTextNode.getWithinHref())
							{
								resultNewsRecord.NewsSource = tempAdvanceTextNode.getText();
								resultNewsRecord.sourceHtmlPath = tempAdvanceTextNode.mHtmlPath;
							}
						}
					}
					
				}
			}
		}
		if(resultNewsRecord.NewsSource == null) // haven't found any specifier word
		{
			//try regex matching
			//first search before
			boolean found = false;
			for (int i = contentBlock.StartIndex - 1; i > titleIndex; i--) 
			{
				tempAdvanceTextNode = mTextNodes.get(i);
				String tempString = tempAdvanceTextNode.getText();
				String[] splitedStrings = tempString.split("[\\s\\u3000]+");
				for (int j = 0; j < splitedStrings.length; j++) 
				{
					tempString = splitedStrings[j];
					if(found = HtmlManipulator.getSingleton().isSourceString(tempString))
					{
						resultNewsRecord.NewsSource = tempString;
						resultNewsRecord.sourceHtmlPath = tempAdvanceTextNode.mHtmlPath;
						break;
					}
				}
				if(found)
					break;
			}
			if(!found)
			{
				//then try after the body
				for (int i = contentBlock.EndIndex + 1; i < getNodeNumber(); i++) 
				{
					tempAdvanceTextNode = mTextNodes.get(i);
					String tempString = tempAdvanceTextNode.getText();
					String[] splitedStrings = tempString.split("[\\s\\u3000]+");
					for (int j = 0; j < splitedStrings.length; j++) 
					{
						tempString = splitedStrings[j];
						if(found = HtmlManipulator.getSingleton().isSourceString(tempString))
						{
							resultNewsRecord.NewsSource = tempString;
							resultNewsRecord.sourceHtmlPath = tempAdvanceTextNode.mHtmlPath;
							break;
						}
					}
					if(found)
						break;
				}
			}
		}
		return resultNewsRecord;
		
	}
}

