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

public class NewsArbiter {
	/**
	 * The Feature Vector of URL
	 */
	private Vector<Integer> UrlFeature;
	/**
	 * The Feature Vector of Word
	 */
	private Vector<Integer> WordFeature;
	/** The entrance of the NewsArbiter
	 * Based on decision tree
	 * @throws IOException 
	 */
	public void Arbiter() throws IOException
	{
		String PathIn="C:\\Users\\zhaoxin\\Desktop\\temp2";
		//String PathIn="D:\\html new\\testdatabase\\testdatabase\\731";
		String PathNewsOut="C:\\Users\\zhaoxin\\Desktop\\News\\";
		String PathOthersOut="C:\\Users\\zhaoxin\\Desktop\\Others\\";
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
			UrlFeature=DrawUrlFeature(ListFiles[i].getName());
			InputStream is=new FileInputStream(ListFiles[i]);
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"));
			StringBuffer temp=new StringBuffer();
            String str;
            while((str=reader.readLine())!=null){
                temp.append(str+"\n");
            }
            String Content=new String(temp);
            WordFeature=DrawFeatureWord(Content);
            if(DecisionTree())
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
	public boolean DecisionTree()
	{
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
	public void SaveFile(String Path, String Name, String Text) throws IOException
	{
        BufferedWriter writer=new BufferedWriter
        		(
        				new OutputStreamWriter(new FileOutputStream(Path+Name),"utf-8")
        		);
        writer.write(Text);
        writer.close();
	}
	/**
	 * Input the Url
	 * Out put the Feature Vector of Url
	 * @param Url
	 * @return
	 */
	public Vector<Integer> DrawUrlFeature(String Url)
	{
		//TODO:
		Vector<Integer> vec=new Vector<Integer>(8);
		Vector<String> Feature=new Vector<String>(Arrays.asList("time/",
				"news/tech/stock1/ent/sports/auto/finance/book/edu/comic/games/baby/astro/lady/chanye/www/mil/bj/eladies/2008/business/money/it/digi/teamchina/yule/house/cul/learning/health/travel/women/nba/golf/weiqi/music/mobile/war/discover/history/",
				"news/newshtml/newscenter/","index/","_","bbs/","blog/","video/"));
		for(int i=0; i < Feature.size(); i++)
		{
			if(i < 3)
			{
				if(CalculateUrl(Url,Feature.get(i)))
					vec.add(1);
				else
					vec.add(0);
			}
			else
			{
				if(CalculateUrl(Url,Feature.get(i)))
					vec.add(-1);
				else
					vec.add(0);
			}
		}
		return vec;
	}
	/**
	 * Judge if Url has time feature
	 */
	public boolean isTimeInUrl(String urlString){
		if(Pattern.compile("index",Pattern.CASE_INSENSITIVE).matcher(urlString).find())
			return false;
		Pattern newsUrlPattern = Pattern.compile("\\d{2}[:\\./\\-_]*?\\d{2}[:\\./\\-_]*?\\d{2}");
		Matcher matcher = newsUrlPattern.matcher(urlString);
		if(matcher.find()){
			return true;
		}
		else {
			return false;
		}
	}
	/**
	 * CalculateUrl feature
	 */
	public boolean CalculateUrl(String Url, String Word)
	{
		if(Word == "time/")
		{
			return isTimeInUrl(Url);
		}
		else if(Word != "_")
		{
			String[] Words=Word.split("/");
			for(int i=0; i < Words.length; i++)
			{
				if(Url.contains(Words[i]))
					return true;
			}
			return false;
		}
		else if(Url.endsWith("_.html"))
			return true;
		else
			return false;
	}
	/**
	 * Input the main text of the html file
	 * Return the Feature Vector of the file based on statistics
	 */
	public Vector<Integer> DrawFeatureWord(String Text)
	{
		Vector<Integer> WordFreq = new Vector<Integer>(12);
		//Initialize all Feature Word of a news
		Vector<String> FeatureWord=new Vector<String>(Arrays.asList("新闻中心、","正文、","报导、","记者、作者、","本报讯、",
				"责任编辑。责编、","来源、本文来源、","相关报导、相关专题、相关链接、相关新闻、","热点新闻、热评榜、热点评论、","新闻论坛、新闻搜索、新闻订阅、新闻排行、手机看新闻、",
				"评论、","新闻、"));
		for(int i=0; i < FeatureWord.size(); i++)
		{
			String Word=FeatureWord.get(i);
			WordFreq.add(CalculateFreq(Text,Word));
		}
		return WordFreq;
	}
	/**
	 * Calculate the frequency of a group of Feature Word,
	 * which is splitted by "、"
	 */
	public int CalculateFreq(String Text,String Word)
	{
		String[] Words=Word.split("、");
		//For "新闻" we calculate the frequency
		//Else if we find it, return 1
		if(Word !="新闻、")
		{
			for(int i=0; i < Words.length; i++)
			{
				if(Text.contains(Words[i]))
					return 1;
			}
			return 0;
		}
		else
		{
			int index=0;
			int count=0;
			while(true)
			{
				index=Text.indexOf(Words[0], index+1);
				if(index < 0)
					break;
				count++;
			}
			return count;
		}
	}
}
