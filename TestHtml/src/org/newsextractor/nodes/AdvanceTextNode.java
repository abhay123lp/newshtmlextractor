package org.newsextractor.nodes;

import org.htmlparser.*;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;

/**
 * 
 * @author firstprayer
 * The extension of TextNode,simply add a variable
 */
public class AdvanceTextNode extends TextNode
{
	/**
	 * Auxiliary variable to help locating in array
	 */
	private int mIndex;
	public AdvanceTextNode(String text) {
		super(text);
	}
	@Override
	public boolean isWhiteSpace()
	{
		String string = this.getText().replaceAll("[\\s\\u3000]", "");
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
