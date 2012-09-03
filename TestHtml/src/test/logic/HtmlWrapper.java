package test.logic;
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

import test.basic.AdvanceTextNode;
import test.basic.HtmlPath;
import test.basic.NewsRecord;
import test.classifier.SourceIdentifier;

public class HtmlWrapper 
{
	public class Block
	{
		public int StartIndex;
		public int EndIndex;
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
	public static GregorianCalendar extractTimeFromString(String timeString)
	{
		Pattern timePattern = Pattern.compile("\\d{4}\\D+?[0,1]?\\d\\D+?[0-3]?\\d\\D+?\\d?\\d\\D+?\\d?\\d");
		Matcher timeMatcher = timePattern.matcher(timeString);
		if(timeMatcher.find())
		{
			String tempString = timeMatcher.group();
			int e = timeMatcher.end();
			Pattern intPattern = Pattern.compile("\\d{1,4}");
			Matcher intMatcher = intPattern.matcher(tempString);
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
			tempString = timeString.substring(e);
			intMatcher = intPattern.matcher(tempString);
			if(intMatcher.find())
			{
				sec = Integer.parseInt(intMatcher.group());
			}
			return new GregorianCalendar(year, month - 1, day, hours,minutes, sec);
		}
		else {
			return null;
		}
	
	}
	private String extractTagNodeText(TagNode node)
	{
		String resultString = "";
		NodeList nodes = node.getChildren();
		if(nodes == null)
			return resultString;
		SimpleNodeIterator iterator = nodes.elements();
		while(iterator.hasMoreNodes())
		{
			Node newNode = iterator.nextNode();
			if(newNode instanceof TextNode)
				resultString += ((AdvanceTextNode)newNode).getText();
			else if(newNode instanceof TagNode)
			{
				resultString += extractTagNodeText(node);
			}
		}
		return resultString;
	}
	public String extractBlockText(Block block)
	{
		String resultString = "";
		/*Node startNode = mTextNodes.get(block.StartIndex).getParent(),endNode = mTextNodes.get(block.EndIndex).getParent();
		Node tempNode = startNode;
		while((tempNode) != null)
		{
			if(tempNode == endNode)
			{
				resultString += extractTagNodeText((TagNode)tempNode);//tempNode.getText() + "\n";
				break;
			}
			if(tempNode instanceof ParagraphTag)
			{
				resultString += extractTagNodeText((TagNode)tempNode);//tempNode.getText() + "\n";
			}
			else if(tempNode instanceof LinkTag)
			{
				resultString += extractTagNodeText((TagNode)tempNode);
				//NodeList nodeList = tempNode.getChildren();
				//if(nodeList != null)
				//{
					//SimpleNodeIterator iterator = nodeList.elements();
					//while(iterator.hasMoreNodes())
					//{
					//	Node node = iterator.nextNode();
					//	if(node instanceof TextNode)
					//	{
					//		resultString += ((AdvanceTextNode)node).getText();
					//	}
					//}
				//}
			}
			tempNode = tempNode.getNextSibling();
		}*/
		for (int i = block.StartIndex; i <= block.EndIndex; i++) {
			resultString += mTextNodes.get(i).getText() + "\n";
		}
		return resultString;
	}
	private Vector<Block> mBlockList = new Vector<Block>();
	private Vector<AdvanceTextNode> mTextNodes = new Vector<AdvanceTextNode>();
	public void clear()
	{
		mBlockList.clear();
		mTextNodes.clear();
	}
	public void addNode(AdvanceTextNode node)
	{
		mTextNodes.add(node);
	}
	public AdvanceTextNode getNode(int index)
	{
		return mTextNodes.get(index);
	}
	public Iterator<AdvanceTextNode> getTextNodeElement()
	{
		return mTextNodes.iterator();
	}
	public Iterator<Block> getBlockElement()
	{
		return mBlockList.iterator();
	}
	public int getNodeNumber()
	{
		return mTextNodes.size();
	}
	public void deleteTextNode(int index)
	{
		mTextNodes.removeElementAt(index);
	}
	public void addBlock(Block b)
	{
		mBlockList.add(b);
	}
	public Block getBlock(int index)
	{
		return mBlockList.get(index);
	}
	public void deleteBlock(int index)
	{
		mBlockList.removeElementAt(index);
	}
	public int getBlockNumber()
	{
		return mBlockList.size();
	}
	//when finished adding nodes to the text node vector,call this to init the block list
	//so that we can find that which part of the html could mostly possibly be the content
	public void InitializeBlockList()
	{
		//for the threhold we set here,we need to figure out one question:
		//do we need to include all the content within the original html inside the blocks
		//or we just need to roughly locate the content,and we will fix the content up later by other methods
		//20120731 now I just do it in the first way
		int theLeastCharacterNumberNeededToBeConsideredAsContent = 10;
		int theLeastImportanceFactorForABlockToAddIntoList = 40;
		for (int i = 0; i < mTextNodes.size(); i++) 
		{
			AdvanceTextNode node = mTextNodes.get(i);
			if(node.getWithinHref())
				continue;
			//if(node.getText().length() < theLeastCharacterNumberNeededToBeConsideredAsContent)
			//	continue;
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
			/*if(block.TextNumber == 1)
				block.ImportanceFactor = length;
			else {
				block.ImportanceFactor = (int) (length * (Math.log(block.TextNumber + 1)));
				}*/
			//should we set a limitation of the Importance Factor here?
			i = j - 1;
			mBlockList.add(block);
		}
	}
	//when everything is ready,call this to figure out the information we want to 
	//know from this html based on the blocks and textnodes
	
	public NewsRecord Manipulate() throws Exception
	{
		int indexOfMostPossibleBlock = -1;
		int lastImportantFactor = 0;
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
				resultNewsRecord.NewsTime = extractTimeFromString(timeNode.getText());
				resultNewsRecord.timeHtmlPath = timeNode.mHtmlPath;
			}
		}
		else {
			resultNewsRecord.NewsTime = extractTimeFromString(timeNode.getText());
			resultNewsRecord.timeHtmlPath = timeNode.mHtmlPath;
		}
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
		//and match the first one
		resultNewsRecord.NewsSource = null;
		Pattern sourcePattern = Pattern.compile("来[源于自]于?[:：]");
		for (int i = titleIndex + 1; i < contentBlock.StartIndex; i++) {
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
			for (int i = titleIndex + 1; i < contentBlock.StartIndex; i++) 
			{
				tempAdvanceTextNode = mTextNodes.get(i);
				String tempString = tempAdvanceTextNode.getText();
				String[] splitedStrings = tempString.split("[\\s\\u3000]+");
				for (int j = 0; j < splitedStrings.length; j++) 
				{
					tempString = splitedStrings[j];
					if(found = SourceIdentifier.isSourceString(tempString))
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
						if(found = SourceIdentifier.isSourceString(tempString))
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

