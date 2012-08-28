package test.basic;

import org.htmlparser.*;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;
public class AdvanceTextNode extends TextNode
{
	
	
	private int mIndex;//auxillary variable to help locating in array
	public AdvanceTextNode(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean isWhiteSpace()
	{
		String string = this.getText().replaceAll("[\\s\\u3000]", "");
		/*String string = this.getText();
		for (int i = 0; i < string.length(); i++) {
			if(string.indexOf(i) != ' ')
				return false;
		return true;
		}*/
		if(string.length() < 1)
			return true;
		else
			return false;
		
	}
	public AdvanceTextNode(Page page,int start,int end)
	{
		super(page,start,end);
	}
	
	
	public void setIndex(int i)
	{
		mIndex = i;
	}
	public int getIndex()
	{
		return mIndex;
	}
}
