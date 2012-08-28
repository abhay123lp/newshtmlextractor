package test.visitor;

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
public class InitCountVisitor {

	protected static InitCountVisitor sSingleton = new InitCountVisitor();
	
	static public InitCountVisitor getSingleton()
	{
		
		return sSingleton;
	}
	
	public void initNodeExtra(AbstractNode node) throws ParserException
	{
		
		//first,initialize the html path string
		/*
		if(node instanceof TagNode)
		{
			AbstractNode parentNode = (AbstractNode) node.getParent();
			if(parentNode == null)
			{
				String pathString = ((TagNode)node).getTagName();
				Node preNode = node;
				int number = 0;
				while((preNode = preNode.getPreviousSibling()) != null)
				{
					if(preNode instanceof TagNode)
					{
						if(((TagNode)preNode).getTagName().equals(pathString));
						{
							number++;
						}
					}
				}
				node.setHtmlPath(pathString);
				node.setExactHtmlPath(pathString + number);
			}
			else {
				Node preNode = node;
				int number = 0;
				String tagName = ((TagNode)node).getTagName();
				while((preNode = preNode.getPreviousSibling()) != null)
				{
					if(preNode instanceof TagNode)
					{
						if(((TagNode)preNode).getTagName().equals(tagName))
						{
							number++;
						}
					}
				}
				String pathString = parentNode.getHtmlPath() + "/" + ((TagNode)node).getHtmlPath();
				node.setHtmlPath(pathString);
				node.setExactHtmlPath(pathString + number);
			}
		}
		else if(node instanceof TextNode){
			AbstractNode parentNode = (AbstractNode) node.getParent();
			node.setHtmlPath(parentNode.getHtmlPath());
			node.setExactHtmlPath(parentNode.getExactHtmlPath());
		}
		else {
			node.setHtmlPath(null);
			node.setExactHtmlPath(null);
		}
		*/
		
		
		//the following initialize the count,the withinhref
		
		if(node instanceof Text)
		{
			AbstractNode parentNode = (AbstractNode) node.getParent();
			if(parentNode == null)
			{
				node.setTextCount(((TextNode)node).getText().length());
				node.setLinkCount(0);
			}
			else if(parentNode.getWithinHref())
			{
				node.setWithinHref(true);
				node.setLinkCount(((TextNode)node).getText().length());
				node.setTextCount(0);
			}
			else {
				node.setTextCount(((TextNode)node).getText().length());
				node.setLinkCount(0);
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
				return;
			}
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
		else {
			node.setTextCount(0);
			node.setLinkCount(0);
			return;
		}
		
	}
}
