package test.logic;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
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
import test.basic.NewsRecord;

public class HtmlWrapper 
{
	public class Block
	{
		public int StartIndex;
		public int EndIndex;
		public int ImportanceFactor;
		public int TextNumber;
		public String HtmlPath;
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
	public int getNodeNumber()
	{
		return mTextNodes.size();
	}
	public void addBlock(Block b)
	{
		mBlockList.add(b);
	}
	public Block getBlock(int index)
	{
		return mBlockList.get(index);
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
			block.HtmlPath = node.getHtmlPath();
			int length = node.getText().length();
			int j = i + 1;
			//we agregate all **consecutive** text nodes that has the **same htmlPath**
			while(j < mTextNodes.size())
			{
				AdvanceTextNode nextNode = mTextNodes.get(j);
				if(nextNode.getHtmlPath().equals(node.getHtmlPath()))
				{
					length += nextNode.getText().length();
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
	@SuppressWarnings("deprecation")
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
		Block block = mBlockList.get(indexOfMostPossibleBlock);
		//for content,we simply concat all the text within the block
		String contentString = "";
		contentString = this.extractBlockText(block);
		resultNewsRecord.NewsContent = contentString;
		resultNewsRecord.contentHtmlPathString = block.HtmlPath;
		//for time, we use regex expression, and search all the text nodes before the content block
				//also, we assume that the right time is the closest time before the content block.
				AdvanceTextNode tempAdvanceTextNode, timeNode = null;
				int timeIndex = block.StartIndex;
				Pattern timePattern = Pattern.compile("\\d{4}\\D+?[0,1]?\\d\\D+?[0-3]?\\d\\D+?\\d?\\d\\D+?\\d?\\d");
				Matcher timeMatcher = null;
				for (int i = block.StartIndex; i >= 0; i--) {
					tempAdvanceTextNode = mTextNodes.get(i);
					timeMatcher = timePattern.matcher(tempAdvanceTextNode.getText());
					if(timeMatcher.find())
					{
						timeIndex = i;
						timeNode = tempAdvanceTextNode;
						break;
					}
				}
				if(timeNode == null || timeMatcher == null)
					resultNewsRecord.NewsTime = null;
				else {
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
					resultNewsRecord.NewsTime = new GregorianCalendar(year, month, day, hours,minutes, sec);
					resultNewsRecord.timeHtmlPathString = timeNode.getHtmlPath();
				}
				AdvanceTextNode titleTextNode = null;
				Pattern titlePattern = Pattern.compile("[\u4E00-\u9FA5]{2,}.*?[\u4E00-\u9FA5]{2,}");
				Matcher titleMatcher = null;
				for (int i = timeIndex - 1; i >= 0; i--) {
					tempAdvanceTextNode = mTextNodes.get(i);
					titleMatcher = titlePattern.matcher(tempAdvanceTextNode.getText());
					if(titleMatcher.find())
					{
						titleTextNode = tempAdvanceTextNode;
						break;
					}
				}
				if(titleTextNode == null)
					resultNewsRecord.NewsTitle = null;
				else {
					resultNewsRecord.titleHtmlPathString = titleTextNode.getHtmlPath();
					resultNewsRecord.NewsTitle = titleTextNode.getText();
				}
				return resultNewsRecord;
				/*
		//for title,we search all the text nodes before the content block
		//and assume the title is inside the h1-h7 tag which is cloest to the content block
		AdvanceTextNode tempAdvanceTextNode,titleTextNode = null;int titleTextIndex = -1;
		Pattern titlePattern = Pattern.compile("/h[1-7]/$",Pattern.CASE_INSENSITIVE);
		for (int i = 0; i < block.StartIndex; i++) {
			tempAdvanceTextNode = mTextNodes.get(i);
			if(titlePattern.matcher(tempAdvanceTextNode.getHtmlPath()).find())
			{
				titleTextIndex = i;
				titleTextNode = tempAdvanceTextNode;
			}
		}
		if(titleTextNode == null)
			resultNewsRecord.NewsTitle = null;
		else {
			resultNewsRecord.titleHtmlPathString = titleTextNode.getHtmlPath();
			resultNewsRecord.NewsTitle = titleTextNode.getText();
		}
		//for time,we use regex expression,and assume that the time text will be within the title and the content
		//what's different is that we assume the first node we find to be the correct one
		Pattern timePattern = Pattern.compile("\\d{4}\\D+?[0,1]?\\d\\D+?[0-3]?\\d\\D+?\\d?\\d.*?$");
		AdvanceTextNode timeNode = null;
		Matcher timeMatcher = null;
		for (int i = titleTextIndex + 1; i < block.StartIndex; i++) {
			tempAdvanceTextNode = mTextNodes.get(i);
			timeMatcher = timePattern.matcher(tempAdvanceTextNode.getText());
			if(timeMatcher.find())
			{
				timeNode = tempAdvanceTextNode;
				break;
			}
		}
		if(timeNode == null || timeMatcher == null)
			resultNewsRecord.NewsTime = null;
		else {
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
			resultNewsRecord.NewsTime = new GregorianCalendar(year,month - 1,day,hours,minutes,sec);// Date(year, month, day, hours,minutes, sec);
			resultNewsRecord.timeHtmlPathString = timeNode.getHtmlPath();
		}
		return resultNewsRecord;*/
	}
}

