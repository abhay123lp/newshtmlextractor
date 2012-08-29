package test.logic;

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

import test.basic.AdvanceTextNode;
import test.basic.NewsRecord;
import test.logic.HtmlWrapper.Block;
public class HtmlManipulator {
	public static String RuleOutUselessText(String HtmlContent)
	{
		String resultString = new String(HtmlContent);
		Pattern scriptPattern;Matcher scriptMatcher;
		Pattern stylePattern;Matcher styleMatcher;
		try {
			String regEx_ScriptString = "<[\\s]*?script[^>]*?>.*?<[^<>]*?/script[\\s]*?>";
			String regEx_StyleString = "<[\\s]*?style[^>]*?>.*?<[^<>]*?/style[\\s]*?>";
			resultString = Pattern.compile("<!--[\\w\\W\r\\n]*?-->").matcher(resultString).replaceAll("");
			scriptPattern = Pattern.compile(regEx_ScriptString,Pattern.CASE_INSENSITIVE);
			scriptMatcher = scriptPattern.matcher(resultString);
			resultString = scriptMatcher.replaceAll("");
			stylePattern = Pattern.compile(regEx_StyleString,Pattern.CASE_INSENSITIVE);
			styleMatcher = stylePattern.matcher(resultString);
			resultString = styleMatcher.replaceAll("");
			
			
			resultString = Pattern.compile("<[\\s]*?head[^>]*?>.*?<[^<>]*?/head[\\s]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			resultString = Pattern.compile("<[\\s]*?form[^>]*?>.*?<[^<>]*?/form[\\s]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			resultString = Pattern.compile("<[\\s]*?span[^>]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			resultString = Pattern.compile("<[^<>]*?/span[^>]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			resultString = Pattern.compile("<[\\s]*?\\?xml[^>]*?>",Pattern.CASE_INSENSITIVE).matcher(resultString).replaceAll("");
			
			resultString = resultString.replaceAll("\n", "");
			resultString = resultString.replaceAll("\t", "");
			resultString = resultString.replaceAll("&nbsp[;]?", " ");//空格可能是有意义的
			resultString = resultString.replaceAll("&#?[0-9a-zA-Z]*[;]?", "");//其他的先一概删掉
		} catch (Exception e) {
			// TODO: handle exception
		}
		return resultString;
	}
	
}
