package org.newsarbiter;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.nodes.AbstractNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.newsextractor.filter.TextExtractionFilter;
import org.newsextractor.nodes.AdvanceTextNode;
import org.newsextractor.textprocess.HtmlManipulator;

public class RatioArbiter {
	static double ratioThreshold=0.98;
	static int TextCountThreshold=1000;
	/**
	 * Judge the file according to the ratio of text and link+text
	 * Input the text of content
	 * Out put if it seems like a news 
	 */
	static boolean RatioJudge(String Text) throws ParserException
	{
		try
		{
			//we get the text which rule out the useless text
			Text = HtmlManipulator.getSingleton().RuleOutUselessText(Text);
			//get the parser tree of html file
			Parser parser=new Parser(Text);
			//set the typical of the parser node
			PrototypicalNodeFactory prototypicalNodeFactory = new PrototypicalNodeFactory();
			prototypicalNodeFactory.setTextPrototype(new AdvanceTextNode(null,0,0));
			parser.setNodeFactory(prototypicalNodeFactory);
			
			//get the regular node
			NodeList nodelist=parser.extractAllNodesThatMatch(new TextExtractionFilter());
			Node[] nodes=nodelist.toNodeArray();
			float Ratio=0;
			//for each node
			for(int i=0; i < nodes.length; i++)
			{
				//calculate the ratio 
				if(((AbstractNode)nodes[i]).getTextCount() == 0)
					Ratio=0;
				else
					Ratio=(float) (((AbstractNode)nodes[i]).getTextCount()/
						(((AbstractNode)nodes[i]).getTextCount()+(float)(((AbstractNode)nodes[i]).getLinkCount())));
				//if a Div node is good enough
				//that's to say its ratio and textcount are beyond the threshold
				if(Ratio > ratioThreshold
						&& nodes[i] instanceof TagNode 
						&& ((TagNode)nodes[i]).getTagName().equals("DIV")
						&& ((AbstractNode)nodes[i]).getTextCount() > TextCountThreshold)
				{
					return true;
				}
			}
			return false;
		}
		catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
