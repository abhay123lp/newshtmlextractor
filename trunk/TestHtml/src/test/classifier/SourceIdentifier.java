package test.classifier;

import java.lang.invoke.ConstantCallSite;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceIdentifier {

	static public boolean isSourceString(String str)
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
	

}
