package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageSortOut 
{
	public static void main(String[] args) 
	{
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(new File("D:\\TDDOWNLOAD\\part-00000")),
							"utf-8"
							
							)
					);
			String outputString = "D:\\TDDOWNLOAD\\websites\\";
			String text = "";
			Pattern pattern = Pattern.compile("CRAWLEDPAGESTART",Pattern.CASE_INSENSITIVE);
			Pattern endPattern = Pattern.compile("CRAWLEDPAGEEND",Pattern.CASE_INSENSITIVE);
			
			BufferedWriter gloBufferedWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(
							new File(outputString + "map.txt")
					),"utf-8"
			));
			while ((text = reader.readLine()) != null)
			{
				Matcher matcher = pattern.matcher(text);
				if(matcher.find())
				{
					String urlString = reader.readLine();//url
					//reader.readLine();//an empty row
					String contentString = "";
					StringBuffer stringBuffer = new StringBuffer();
					//String directoryString = "";
					
					while((text = reader.readLine()) != null)
					{
						if(endPattern.matcher(text).find())
						{
							
							Pattern validHtmlPattern = Pattern.compile("<!Doctype[^>]*?>",Pattern.CASE_INSENSITIVE);
							contentString = stringBuffer.toString();
							Matcher validHtmlMatcher = validHtmlPattern.matcher(contentString);
							if(validHtmlMatcher.find())
							{
								String filepathString = urlString.replaceAll("/", "_");
								filepathString = filepathString.replaceAll(":", "_");
								BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
										new FileOutputStream(
												new File(outputString + filepathString)
										),"utf-8"
								));
								writer.write(contentString);
								writer.close();
								gloBufferedWriter.write(urlString + "::" + outputString + filepathString);
								gloBufferedWriter.newLine();
								gloBufferedWriter.flush();
							}
							
							break;
						}
						stringBuffer.append(text + "\n");
					}
					
				}
				
			}
			gloBufferedWriter.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
