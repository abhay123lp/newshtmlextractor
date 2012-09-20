package org.newsextractor.textprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.directory.DirContext;

import org.htmlparser.*;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.newsextractor.nodes.AdvanceTextNode;
import org.newsextractor.production.HtmlWrapper.Block;
import org.newsextractor.util.NewsRecord;

/**
 * 
 * @author firstprayer
 * Definition of some methods to process text
 */
public class HtmlManipulator {
	public HtmlManipulator(){
		
	}
	
	protected static HtmlManipulator sSingleton = new HtmlManipulator();
	public static HtmlManipulator getSingleton(){
		return sSingleton;
	}
	/**
	 * 
	 * @param HtmlContent
	 * Rule out useless content in HtmlContent
	 * 
	 * @return the content after processing
	 */
	public String RuleOutUselessText(String HtmlContent)
	{
		String resultString = new String(HtmlContent);
		Pattern scriptPattern;Matcher scriptMatcher;
		Pattern stylePattern;Matcher styleMatcher;
		try {
			//remove remarks/comments
			resultString = Pattern.compile("<!--[\\w\\W\r\\n]*?-->").matcher(resultString).replaceAll("");
			
			//remove style and scripts
			String regEx_ScriptString = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[^<>]*?/script[\\s]*?>";
			String regEx_StyleString = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[^<>]*?/style[\\s]*?>";
			
			scriptPattern = Pattern.compile(regEx_ScriptString,Pattern.CASE_INSENSITIVE);
			scriptMatcher = scriptPattern.matcher(resultString);
			resultString = scriptMatcher.replaceAll("");
			stylePattern = Pattern.compile(regEx_StyleString,Pattern.CASE_INSENSITIVE);
			styleMatcher = stylePattern.matcher(resultString);
			resultString = styleMatcher.replaceAll("");
			
			//resultString = Pattern.compile("<style[\u005e>]*?>*?</style[\u005e>]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			//remove the content within head
			resultString = Pattern.compile("<[\\s]*?head[^>]*?>[\\s\\S]*?<[^<>]*?/head[\\s]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			//remove all forms
			resultString = Pattern.compile("<[\\s]*?form[^>]*?>[\\s\\S]*?<[^<>]*?/form[\\s]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			//remove the tag <span> and </span>,but keep the content inside them
			resultString = Pattern.compile("<[\\s]*?span[^>]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll(" ");
			resultString = Pattern.compile("<[^<>]*?/span[^>]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll(" ");
			//remove xml
			resultString = Pattern.compile("<[\\s]*?\\?xml[^>]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			
			//remove 转义字符
			resultString = resultString.replaceAll("\n", "");
			resultString = resultString.replaceAll("\t", "");
			resultString = resultString.replaceAll("&nbsp[;]?", " ");//空格可能是有意义的
			resultString = resultString.replaceAll("&#?[0-9a-zA-Z]*[;]?", "");//其他的先一概删掉
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return resultString;
	}
	
	/**
	 * 
	 * @param str
	 * Judge whether str indicators the source of a news by regex
	 * @return
	 */
	public boolean isSourceString(String str)
	{
		final int leastLengthToBeSource = 2;
		final int maxLengthToBeSource = 8;
		String tempString = str;
		Pattern sourcePattern = Pattern.compile(".+[网报线区坛娱台视频播乐]",Pattern.CASE_INSENSITIVE);
		Matcher sourceMatcher = sourcePattern.matcher(tempString);
		int length = 0;
		boolean atleastOneMatch = false;
		while (sourceMatcher.find()) 
		{
			tempString = sourceMatcher.group();
			length += tempString.length();
			if(tempString.length() >= leastLengthToBeSource && tempString.length() <= maxLengthToBeSource)
			{
				//a source should not contain the following characters
				if(tempString.matches(".*?[，。！？：:；;\'\"‘’“”、].*?") == false)
				{
					atleastOneMatch = true;
				}
			}
		}
		if(atleastOneMatch && (float)length / str.length() >= 0.5)//ensure that the match is relevant
		{
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param timeString
	 * extract time from timeString using regex
	 * @return the time
	 */
	public GregorianCalendar extractTimeFromString(String timeString)
	{
		Pattern timePattern = Pattern.compile("\\d{4}\\D+?[0,1]?\\d\\D+?[0-3]?\\d\\D+?\\d?\\d\\D+?\\d?\\d");
		Matcher timeMatcher = timePattern.matcher(timeString);
		if(timeMatcher.find())
		{
			String tempString = timeMatcher.group();
			int e = timeMatcher.end();
			Pattern intPattern = Pattern.compile("\\d{1,4}");
			Matcher intMatcher = intPattern.matcher(tempString);
			intMatcher.find();
			int year = Integer.parseInt(intMatcher.group()) ;
			intMatcher.find();
			int month = Integer.parseInt(intMatcher.group()) ;
			intMatcher.find();
			int day = Integer.parseInt(intMatcher.group()) ;
			intMatcher.find();
			int hours = Integer.parseInt(intMatcher.group()) ;
			intMatcher.find();
			int minutes = Integer.parseInt(intMatcher.group()) ;


			int sec = 0;
			tempString = timeString.substring(e);
			intMatcher = intPattern.matcher(tempString);
			if(intMatcher.find())
			{
				sec = Integer.parseInt(intMatcher.group());
			}
			return new GregorianCalendar(year, month - 1, day, hours,minutes, sec);
		}
		else {
			return null;
		}
	}

	/**
	 * 
	 * @param urlString
	 * @return
	 */
	public boolean isNewsUrl(String urlString){
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

}
