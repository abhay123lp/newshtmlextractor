package org.newsarbiter;

import java.io.IOException;

public class TestArbiter {
	public static void main(String[] args) throws IOException
	{
		HtmlFeature.initFeatureDescription();
		NewsArbiter arbiter=new NewsArbiter();
		arbiter.ArbitrateDirectoryAndDivide(
				"C:\\Users\\firstprayer\\Desktop\\��������\\temp4",
				"C:\\Users\\firstprayer\\Desktop\\news\\",
				"C:\\Users\\firstprayer\\Desktop\\others\\");
	}
}
