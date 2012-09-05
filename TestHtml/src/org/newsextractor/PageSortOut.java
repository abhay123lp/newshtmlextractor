package org.newsextractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class PageSortOut 
{
	public static void main(String[] args) 
	{
		try {
			/*
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
			*/
			File rootFile = new File("C:\\Users\\firstprayer\\Desktop\\testdatabase\\testdatabase");
			String outputfolder = "C:\\Users\\firstprayer\\Desktop\\outputs\\";
					
			File[] archiveListFiles = rootFile.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					// TODO Auto-generated method stub
					if(pathname.isDirectory())
						return true;
					return false;
				}
			});
			Vector<File> fileBufferFiles = new Vector<>();
			int leastClusterNum = 60;
			int index = 0;
			int size = 0;
			for (int i = 0; i < archiveListFiles.length; i++) {
				File dirFile = archiveListFiles[i];
				File[] htmlFiles = dirFile.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						// TODO Auto-generated method stub
						Pattern htmlPattern = Pattern.compile(".*\\.s?html?$",Pattern.CASE_INSENSITIVE);
						Matcher htmlMatcher = htmlPattern.matcher(name);
						if(htmlMatcher.find())
							return true;
						return false;
					}
				});
				for (int j = 0; j < htmlFiles.length; j++) {
					fileBufferFiles.add(htmlFiles[j]);
				}
				if(fileBufferFiles.size() > leastClusterNum)
				{
					size += fileBufferFiles.size();
					String subfolderPathString = outputfolder + index;
					File newDirFile = new File(subfolderPathString);
					newDirFile.mkdir();
					Iterator<File> iterator = fileBufferFiles.iterator();
					while(iterator.hasNext())
					{
						File originalHtmlFile = iterator.next();
						File newHtmlFile = new File(newDirFile.getAbsoluteFile() + "\\" + originalHtmlFile.getName());
						StringBuffer stringBuffer = new StringBuffer();
						if(originalHtmlFile.isFile() == false)
						{
							System.out.println("what?!");
						}
						BufferedReader htmlReader = new BufferedReader(new InputStreamReader(new FileInputStream(originalHtmlFile),"utf-8"));
						String string = "";
						while((string = htmlReader.readLine()) != null)
						{
							stringBuffer.append(string);
						}
						string = stringBuffer.toString();
						htmlReader.close();
						BufferedWriter newHtmlWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newHtmlFile),"utf-8"));
						newHtmlWriter.write(string);
						newHtmlWriter.close();
					}
					fileBufferFiles.clear();
					index++;
				}
			}
			System.out.println(size);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
