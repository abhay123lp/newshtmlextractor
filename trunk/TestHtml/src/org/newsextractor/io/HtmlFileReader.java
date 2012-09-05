package org.newsextractor.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * @author firstprayer
 * This class just contains some methods about I/O
 */
public class HtmlFileReader {

	/**
	 * retrieve the content of a html file
	 * @param htmlPath
	 * @return the content in String
	 * @throws IOException
	 */
	public static String getHtmlContentSafely(String htmlPath) throws IOException
	{
		InputStreamReader inputStreamReader = new InputStreamReader
				(
						new FileInputStream(
								new File(htmlPath)
							),"utf-8"
				);
		BufferedReader reader = new BufferedReader(inputStreamReader);
		
		StringBuffer htmlContentStringBuffer = new StringBuffer();
		String text = "";
		while((text = reader.readLine()) != null)
		{
			htmlContentStringBuffer.append(text);
		}
		text = htmlContentStringBuffer.toString();
		reader.close();
		return text;
		
	}

}
