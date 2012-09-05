package org.newsextractor.util;

import java.util.Iterator;
import java.util.Vector;

/**
 * 
 * @author firstprayer
 * HtmlPath is defined to describe the Path from the tree root to a node
 * It looks like this: Html0/Body0/Div1/...
 */
public class HtmlPath {

	/**
	 * Vector of nodes construct a path
	 */
	public Vector<HtmlPathNode> PathNodeList = new Vector<>(); 
	
	@Override
	public String toString()
	{
		Iterator<HtmlPathNode> iterator = PathNodeList.iterator();
		StringBuffer buffer = new StringBuffer();
		while(iterator.hasNext())
		{
			buffer.append(iterator.next().toString());
			buffer.append('/');
		}
		return buffer.toString();
	}
	public HtmlPath(HtmlPath mHtmlPath) {
		// TODO Auto-generated constructor stub
		Iterator<HtmlPathNode> iterator = mHtmlPath.PathNodeList.iterator();
		while(iterator.hasNext())
		{
			PathNodeList.add(iterator.next());
		}
	}
	/**
	 * chech the last element,if <a> return true,otherwise return false
	 * @return
	 */
	public boolean isLink()
	{
		if(PathNodeList.size() > 0)
		{
			if(PathNodeList.get(PathNodeList.size() - 1).getTagName().equals("A"))
			{
				return true;
			}
			else
				return false;
		}
		else {
			return false;
		}
	}
	public HtmlPath()
	{
		
	}
	/**
	 * Calculate the discrepancy between two path
	 */
	public int findDiscrepancyLevel(HtmlPath targetPath)
	{
		if(targetPath == null)
			return Integer.MAX_VALUE;
		int thisLength = PathNodeList.size();
		int targetLength = targetPath.PathNodeList.size();
		int length = thisLength > targetLength?targetLength:thisLength;
		int i;
		for(i = 0;i < length;i++)
		{
			if(PathNodeList.get(i).equals(targetPath.PathNodeList.get(i)) == false)
			{
				break;
			}
		}
		
		return length - i;
	}
	/**
	 * The tag names between two corresponding nodes are the same
	 * Ignore the index
	 * @param targetPath
	 * @return
	 */
	public boolean isPartlyEqualTo(HtmlPath targetPath)
	{
		if(targetPath == null)
			return false;
		if(PathNodeList.size() != targetPath.PathNodeList.size())
			return false;
		for (int i = 0; i < PathNodeList.size(); i++) {
			if(PathNodeList.get(i).getTagName().equals(
					targetPath.PathNodeList.get(i).getTagName()
					) == false)
			{
				return false;
			}
		}
		return true;
	}
	/**
	 * The tag names and index between two corresponding nodes are both the same
	 * 
	 * @param targetPath
	 * @return
	 */
	public boolean isCompletelyEqualTo(HtmlPath targetPath)
	{
		if(targetPath == null)
			return false;
		if(PathNodeList.size() != targetPath.PathNodeList.size())
			return false;
		for (int i = 0; i < PathNodeList.size(); i++) {
			if(PathNodeList.get(i).equals(
					targetPath.PathNodeList.get(i)
					) == false)
			{
				return false;
			}
		}
		return true;
	}
	@Override
	public boolean equals(Object obj)
	{
		HtmlPath targetPath = (HtmlPath) obj;
		if(targetPath == null)
			return false;
		if(PathNodeList.size() != targetPath.PathNodeList.size())
			return false;
		for (int i = 0; i < PathNodeList.size(); i++) {
			if(PathNodeList.get(i).equals(
					targetPath.PathNodeList.get(i)
					) == false)
			{
				return false;
			}
		}
		return true;
	}
}
