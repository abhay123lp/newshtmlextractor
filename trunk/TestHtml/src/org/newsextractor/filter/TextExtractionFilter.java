package org.newsextractor.filter;
import org.htmlparser.*;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;
import org.newsextractor.nodes.AdvanceTextNode;
import org.newsextractor.util.HtmlPath;
import org.newsextractor.util.HtmlPathNode;
import org.newsextractor.visitor.InitCountVisitor;
/**
 * 
 * @author firstprayer
 * Filter to extract nodes that we will be insterested in when we do the procession
 */
public class TextExtractionFilter implements NodeFilter
{
	public TextExtractionFilter()
	{
		
	}
	@Override
	public boolean accept(Node targetNode) {
		// TODO Auto-generated method stub
		
		try {
			InitCountVisitor.getSingleton().initNodeExtra((AbstractNode) targetNode);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(targetNode instanceof Text) // rule out useless text
		{
			if(((AdvanceTextNode)targetNode).isWhiteSpace())
				return false;
		}
		if(targetNode instanceof Tag || targetNode instanceof Text)
			return true;
		else
			return false;
	}
}
