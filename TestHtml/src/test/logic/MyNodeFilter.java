package test.logic;
import org.htmlparser.*;
import org.htmlparser.nodes.TagNode;
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
			String string = targetNode.getText();
			string = string.replaceAll(" ", "");
			if(string.length() < 2) //Ö±½ÓÈ¥µô
				return false;
			//targetNode.setText(string);
			String htmlPathString = "";
			//nows let's see whether it's inside an <A>
			Node tempNode = targetNode;
			while((targetNode = targetNode.getParent()) != null)
			{
				htmlPathString = ((TagNode)targetNode).getTagName() + "/" + htmlPathString;
				if(targetNode instanceof LinkTag)
				{
					((AdvanceTextNode)tempNode).setWithinHref(true);
					return false;//for testing,temporarily remove the <a>
				}
			}
			((AdvanceTextNode)tempNode).setHtmlPath(htmlPathString);
			targetNode = tempNode;
		}
		
		return true;
	}
	
}
