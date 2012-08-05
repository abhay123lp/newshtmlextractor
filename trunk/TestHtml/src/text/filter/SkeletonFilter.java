package text.filter;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Text;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.TableTag;

public class SkeletonFilter implements NodeFilter
{

	@Override
	public boolean accept(Node node) {
		// TODO Auto-generated method stub
		if(node instanceof Text)
			return false;
		if(node instanceof ParagraphTag)
			return false;
		if(node instanceof Div || node instanceof TableTag)
			return true;
		return false;
	}
	
}
