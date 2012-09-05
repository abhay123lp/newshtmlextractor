package org.newsextractor.filter;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Text;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.HeadingTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;

/**
 * 
 * @author firstprayer
 * Filter to form the dom tree skeleton to cluster html
 */
public class SkeletonFilter implements NodeFilter
{

	@Override
	public boolean accept(Node node) {
		// TODO Auto-generated method stub
		/*if(
				node instanceof Text ||
				node instanceof ParagraphTag || 
				node instanceof LinkTag ||
				node instanceof HeadingTag || 
				node instanceof HeadTag ||
				node instanceof RemarkNode
		   )
			return false;
		
		return true;*/
		if(node instanceof Div || node instanceof TableTag || node instanceof TableRow || node instanceof TableColumn)
			return true;
		return false;
	}
	
}
