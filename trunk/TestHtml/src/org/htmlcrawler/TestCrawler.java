package org.htmlcrawler;

import java.io.IOException;

public class TestCrawler {
	public static void main(String[] args) throws IOException
	{
		HtmlCrawler crawler=new HtmlCrawler();
		crawler.craw("http://news.sina.com.cn/","C:\\Users\\firstprayer\\Desktop\\sina\\");
		//crawler.craw("http://news.qq.com/a/20120925/000064.htm","C:\\Users\\firstprayer\\Desktop\\qq\\");
	}
}
