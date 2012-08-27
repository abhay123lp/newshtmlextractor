package text.filter;
import org.htmlparser.*;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;

import test.basic.AdvanceTextNode;
public class MyNodeFilter implements NodeFilter
{
	public MyNodeFilter()
	{
		
	}
	@Override
	public boolean accept(Node targetNode) {
		// TODO Auto-generated method stub
		
		
		if(targetNode instanceof Text) // rule out useless text
		{
			if(((AdvanceTextNode)targetNode).isWhiteSpace())
				return false;
			//String string = targetNode.getText();
			//string = string.replaceAll(" ", "");
			//if(string.length() < 2) //Ö±½ÓÈ¥µô
				//return false;
			//targetNode.setText(string);
			String htmlPathString = "", exactPathString = "";
			String parentName = "";
			//nows let's see whether it's inside an <A>
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
				if(targetNode instanceof LinkTag)
				{
					((AdvanceTextNode)tempNode).setWithinHref(true);	
				}
			}
			((AdvanceTextNode)tempNode).setHtmlPath(htmlPathString);
			((AdvanceTextNode)tempNode).setExactHtmlPath(exactPathString);
			targetNode = tempNode;
		}
		
		return true;
	}
}
