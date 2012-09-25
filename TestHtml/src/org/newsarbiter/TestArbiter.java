package org.newsarbiter;

import java.io.IOException;

public class TestArbiter {
	public static void main(String[] args) throws IOException
	{
		HtmlFeature.initFeatureDescription();
		NewsArbiter arbiter=new NewsArbiter();
		String[] archiveStrings = 
		{
				"C:\\Users\\firstprayer\\Desktop\\sina"
				,"C:\\Users\\firstprayer\\Desktop\\qq"
				,"C:\\Users\\firstprayer\\Desktop\\163"
				,"C:\\Users\\firstprayer\\Desktop\\xinhua"
				,"C:\\Users\\firstprayer\\Desktop\\china"
		
		};
		
		for (int i = 0; i < archiveStrings.length; i++) {
			arbiter.ArbitrateDirectoryAndDivide(archiveStrings[i], archiveStrings[i] + "_news\\", archiveStrings[i] + "_others\\");
		}
		/*
		arbiter.ArbitrateDirectoryAndDivide(
				//"C:\\Users\\firstprayer\\Desktop\\²âÊÔÊý¾Ý\\temp4",
				//"C:\\Users\\firstprayer\\Desktop\\othersTest\\Others",
				"C:\\Users\\firstprayer\\Desktop\\1\\shoulebenewsbutnotbenews",
				"C:\\Users\\firstprayer\\Desktop\\news\\",
				"C:\\Users\\firstprayer\\Desktop\\others\\");*/
	}
}
