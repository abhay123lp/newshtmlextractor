package org.newsextractor.visitor;

import org.htmlparser.Node;
import org.htmlparser.Text;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.tags.*;
import org.htmlparser.nodes.*;
import org.newsextractor.nodes.AdvanceTextNode;
import org.newsextractor.util.HtmlPath;
import org.newsextractor.util.HtmlPathNode;
/**
 * 
 * @author firstprayer
 * visitor of node,initialize the fields while visiting
 */

public class InitCountVisitor {

	protected static InitCountVisitor sSingleton = new InitCountVisitor();
	
	static public InitCountVisitor getSingleton()
	{
		return sSingleton;
	}
	
	public void initNodeExtra(AbstractNode node) throws ParserException
	{
		if(node.IsVisited)
			return;
		//nows let's init the html path
		if(node instanceof Text)
		{
			String parentName = "";
			
			Node tempNode = node;
			HtmlPath tempPath = new HtmlPath();
			while((tempNode = tempNode.getParent()) != null)
			{
				//while processing we calculate its htmlPath
				int sequence = 0;
				Node siblingNode = tempNode;
				parentName = ((TagNode)tempNode).getTagName();
				while((siblingNode = siblingNode.getPreviousSibling()) != null)
				{
					if(siblingNode instanceof TagNode)
					{
						if(((TagNode)siblingNode).getTagName().equals(parentName) == true)
						{
							sequence++;
						}
					}
					
				}
				tempPath.PathNodeList.insertElementAt(new HtmlPathNode(parentName, sequence),0);
			}
			node.mHtmlPath = tempPath;
		}
		
		//the following initialize the count,the withinhref
		
		if(node instanceof Text)
		{
			if(((TextNode)node).isWhiteSpace())
			{
				node.setLinkCount(0);
				node.setTextCount(0);
			}
			else{
				AbstractNode parentNode = (AbstractNode) node.getParent();
				if(parentNode == null)
				{
					int count = ((TextNode)node).getText()/*.replaceAll("[^\\u4E00-\\u9FA5]", "")*/.length();
					node.setTextCount(count);
					node.setLinkCount(0);
				}
				else if(parentNode.getWithinHref())
				{
					node.setWithinHref(true);
					int count = ((TextNode)node).getText()/*.replaceAll("[^\\u4E00-\\u9FA5]", "")*/.length();
					node.setTextCount(count);
					node.setLinkCount(count);
					node.setTextCount(0);
				}
				else {
					int count = ((TextNode)node).getText()/*.replaceAll("[^\\u4E00-\\u9FA5]", "")*/.length();
					node.setTextCount(count);
					node.setLinkCount(0);
				}
			}
			
		}
		else if(node instanceof TagNode)
		{
			AbstractNode parentNode = (AbstractNode) node.getParent();
			if((parentNode != null && parentNode.getWithinHref()) || node instanceof LinkTag)
			{
				node.setWithinHref(true);
			}
			NodeList nodeList = node.getChildren();
			if(nodeList == null || nodeList.size() == 0)
			{
				node.setLinkCount(0);
				node.setTextCount(0);
			}
			else {
				NodeIterator iterator = nodeList.elements();
				int textCount = 0,linkCount = 0;
				while(iterator.hasMoreNodes())
				{
					AbstractNode tempNode = (AbstractNode) iterator.nextNode();
					initNodeExtra(tempNode);
					textCount += tempNode.getTextCount();
					linkCount += tempNode.getLinkCount();
				}
				node.setTextCount(textCount);
				node.setLinkCount(linkCount);
			}
		}
		else {
			node.setTextCount(0);
			node.setLinkCount(0);
		}
		node.IsVisited = true;
	}
}
