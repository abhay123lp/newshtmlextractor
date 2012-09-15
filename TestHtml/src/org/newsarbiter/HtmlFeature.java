package org.newsarbiter;

import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlFeature {
	
	public Vector<Integer> UrlFeature = null;
	public Vector<Integer> WordFeature = null;
	public HtmlFeature(){
		UrlFeature = new Vector<>();
		WordFeature = new Vector<>();
	}
	
	public HtmlFeature(String url,String text){
		UrlFeature = DrawUrlFeature(url);
		WordFeature = DrawFeatureWord(text);
	}
	
	private Vector<Integer> DrawUrlFeature(String Url)
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
				if(Pattern.compile(Words[i],Pattern.CASE_INSENSITIVE).matcher(Url).find())
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
		//FIXME:�Ծ�̬��������ʽ�洢���Ҹ���������ʽ��
		Vector<String> FeatureWord=new Vector<String>(Arrays.asList("�������ġ�","���ġ�","������","���ߡ����ߡ�","����Ѷ��",
				"���α༭����ࡢ","��Դ��������Դ��","��ر��������ר�⡢������ӡ�������š�","�ȵ����š��������ȵ����ۡ�","������̳���������������Ŷ��ġ��������С��ֻ������š�",
				"���ۡ�","���š�"));
		for(int i=0; i < FeatureWord.size(); i++)
		{
			String Word=FeatureWord.get(i);
			WordFreq.add(CalculateFreq(Text,Word));
		}
		return WordFreq;
	}
	/**
	 * Calculate the frequency of a group of Feature Word,
	 * which is splitted by "��"
	 */
	public int CalculateFreq(String Text,String Word)
	{
		String[] Words=Word.split("��");
		//For "����" we calculate the frequency
		//Else if we find it, return 1
		if(Word !="���š�")
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
	
	/**
	 * Check if the url contains time information
	 * @return
	 */
	public boolean isUrlContainTime(){
		if(UrlFeature.get(0) != 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Check if the url is within a sub-category,like "sport", "celebrity", "internatial" and so on
	 * @return
	 */
	public boolean isUrlContainSubCategory(){
		if(UrlFeature.get(1) != 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * See if it contains words like "newscenter" "news"...
	 * @return
	 */
	public boolean isUrlContainNewsKeyword(){
		if(UrlFeature.get(2) != 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * See if it contains "index"
	 * @return
	 */
	public boolean isUrlContainIndex(){
		if(UrlFeature.get(3) != 0){
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * See if it contains "bbs"
	 * @return
	 */
	public boolean isUrlContainBBS(){
		if(UrlFeature.get(4) != 0){
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * See if it contains "blog"
	 * @return
	 */
	public boolean isUrlContainBlog(){
		if(UrlFeature.get(6) != 0){
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * See if it contains "video"
	 * @return
	 */
	public boolean isUrlContainVideo(){
		if(UrlFeature.get(7) != 0){
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * @return the number of the occurrence of "����" in the text
	 */
	public int getTextContainNewsKeywordNumber(){
		return WordFeature.get(11);
	}
	
	/**
	 * More features to be finished 
	 * 
	 */
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
