package text.filter;
import org.htmlparser.*;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;

import test.basic.AdvanceTextNode;
import test.visitor.InitCountVisitor;
public class MyNodeFilter implements NodeFilter
{
	public MyNodeFilter()
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
			
			String htmlPathString = "", exactPathString = "";
			String parentName = "";
			//nows let's init the html path
			Node tempNode = targetNode;
			while((targetNode = targetNode.getParent()) != null)
			{
				//while processing we calculate its htmlPath
				int sequence = 0;
				Node siblingNode = targetNode;
				parentName = ((TagNode)targetNode).getTagName();
				htmlPathString = ((TagNode)targetNode).getTagName() + "/" + htmlPathString;
				while((siblingNode = siblingNode.getPreviousSibling()) != null)
				{
					if(siblingNode.getClass().equals(targetNode) != true)
					{
						continue;
					}
					if(((TagNode)siblingNode).getTagName().equals(parentName) == true)
					{
						sequence++;
					}
				}
				exactPathString = ((TagNode)targetNode).getTagName() + sequence + "/" + exactPathString;
				/*if(targetNode instanceof LinkTag)
				{
					((AdvanceTextNode)tempNode).setWithinHref(true);	
				}*/
			}
			((AdvanceTextNode)tempNode).setHtmlPath(htmlPathString);
			((AdvanceTextNode)tempNode).setExactHtmlPath(exactPathString);
			targetNode = tempNode;
		}
		if(targetNode instanceof Tag || targetNode instanceof Text)
			return true;
		else
			return false;
	}
}
