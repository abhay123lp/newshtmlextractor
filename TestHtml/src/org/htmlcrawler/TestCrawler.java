package org.htmlcrawler;

import java.io.IOException;

public class TestCrawler {
	public static void main(String[] args) throws IOException
	{
		HtmlCrawler crawler=new HtmlCrawler();
		crawler.craw("http://news.qq.com/society_index.shtml");
		
	}
}
