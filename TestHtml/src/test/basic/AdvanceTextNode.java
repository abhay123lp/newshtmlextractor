package test.basic;

import org.htmlparser.*;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;
public class AdvanceTextNode extends TextNode
{
	private boolean mIsWithinHref = false;
	public String mHtmlPath;
	public String exactHtmlPath;
	private int mIndex;//auxillary variable to help locating in array
	public AdvanceTextNode(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean isWhiteSpace()
	{
		String string = this.getText();
		for (int i = 0; i < string.length(); i++) {
			if(string.indexOf(i) != ' ')
				return false;
		}
		return true;
	}
	public AdvanceTextNode(Page page,int start,int end)
	{
		super(page,start,end);
	}
	public void setWithinHref(boolean b)
	{
		mIsWithinHref = b;
	}
	public boolean getWithinHref(){
		return mIsWithinHref;
	}
	public void setHtmlPath(String str)
	{
		mHtmlPath = str;
	}
	public void setExactHtmlPath(String str)
	{
		exactHtmlPath = str;
	}
	public String getHtmlPath()
	{
		return mHtmlPath;
	}
	public String getExactHtmlPath()
	{
		return exactHtmlPath;
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
