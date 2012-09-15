package org.newsarbiter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//FIXME:更改类架构
public class NewsArbiter {
	/** The entrance of the NewsArbiter
	 * Based on decision tree
	 * @throws IOException 
	 */
	public void Arbiter() throws IOException
	{
		String PathIn="C:\\Users\\firstprayer\\Desktop\\testNewsIdentifier";
		//String PathIn="D:\\html new\\testdatabase\\testdatabase\\731";
		String PathNewsOut="C:\\Users\\firstprayer\\Desktop\\news\\";
		String PathOthersOut="C:\\Users\\firstprayer\\Desktop\\Others\\";
		File rootfile=new File(PathIn);
		File[] ListFiles = rootfile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if(pathname.isFile() && 
						(pathname.getName().endsWith(".html") 
								|| pathname.getName().endsWith(".htm")))
					return true;
				return false;
			}
		});
		for(int i=0; i < ListFiles.length; i++)
		{
			InputStream is=new FileInputStream(ListFiles[i]);
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"));
			StringBuffer temp=new StringBuffer();
            String str;
            while((str=reader.readLine())!=null){
                temp.append(str+"\n");
            }
            String Content=new String(temp);
            HtmlFeature feature = new HtmlFeature(ListFiles[i].getName(),Content);
            if(DecisionTree(feature))
            	//System.out.println("yes");
            	SaveFile(PathNewsOut,ListFiles[i].getName(),Content);
            else
            	//System.out.println("no");
            	SaveFile(PathOthersOut,ListFiles[i].getName(),Content);
		}
	}
	/**
	 * The core of the Arbiter to decide if a html file is a news
	 * @return
	 */
	public boolean DecisionTree(HtmlFeature feature)
	{
		Vector<Integer> UrlFeature = feature.UrlFeature;
		Vector<Integer> WordFeature = feature.WordFeature;
		//Include Url Time
		if(UrlFeature.get(0) == 1)
		{
			//"bbs"
			if(UrlFeature.get(5) == -1)
				return false;
			//"index"
			else if(UrlFeature.get(3) == -1)
				return false;
			//End with "/"
			else if(UrlFeature.get(4) == -1)
				return false;
			//"blog"
			else if(UrlFeature.get(6) == -1)
				return false;
			//"video"
			else if(UrlFeature.get(7) == -1)
				return false;
			//"新闻"词频大于2
			else if(WordFeature.get(11) > 2)
			{
				//news feature word in content
				int score=UrlFeature.get(2);
				for(int i=0; i < WordFeature.size()-1; i++)
				{
					score += WordFeature.get(i);
				}
				if(score > 0)
					return true;
				else
					return false;
				
			}
			else
				return false;
		}
		//No time feature in Url
		else
		{
			//No first-level dir
			if (UrlFeature.get(2) == 0)
				return false;
			//Not end with "/"
			else if(UrlFeature.get(4) == 0)
				return true;
			else
				return false;
		}
	}
	/**
	 * Save file to the Path
	 * @param Path
	 * @param Name
	 * @param Text
	 * @throws IOException
	 */
	private void SaveFile(String Path, String Name, String Text) throws IOException
	{
        BufferedWriter writer=new BufferedWriter
        		(
        				new OutputStreamWriter(new FileOutputStream(Path+Name),"utf-8")
        		);
        writer.write(Text);
        writer.close();
	}
	
	
	
}
