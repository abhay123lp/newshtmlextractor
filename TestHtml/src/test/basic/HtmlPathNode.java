package test.basic;

public class HtmlPathNode {

	private String mTagName;
	private int mIndex = -1;
	@Override
	public String toString()
	{
		return mTagName + mIndex;
	}
	@Override
	public boolean equals(Object obj)
	{
		if(mTagName.equals(((HtmlPathNode)obj).getTagName()) && mIndex == ((HtmlPathNode)obj).getIndex())
			return true;
		return false;
	}
	public String getTagName()
	{
		return mTagName;
	}
	public int getIndex()
	{
		return mIndex;
	}
	
	public void setTagName(String tagName)
	{
		mTagName = new String(tagName);
	}
	public void setIndex(int index)
	{
		mIndex = index;
	}
	
	public HtmlPathNode(String tagName,int index)
	{
		mTagName = new String(tagName);
		mIndex = index;
	}
	
}
