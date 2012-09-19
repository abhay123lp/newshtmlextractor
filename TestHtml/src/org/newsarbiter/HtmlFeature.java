package org.newsarbiter;

import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.util.ParserException;

public class HtmlFeature {
	
	static private Vector<String> UrlFeatureDescriptionList = null;
	static private Vector<String> ContentFeatureDescriptionList = null;
	
	static void initFeatureDescription(){
		UrlFeatureDescriptionList = new Vector<String>(Arrays.asList(
				"news/tech/stock1/ent/sports/auto/finance/book/edu/comic/games/baby/astro/lady/chanye/www/mil/bj/eladies/2008/business/money/it/digi/teamchina/yule/house/cul/learning/health/travel/women/nba/golf/weiqi/music/mobile/war/discover/history/"
		//		"news/newshtml/newscenter/",
				,"index/bbs/blog"
	//			,"bbs/"
		//		,"blog/"
	//			,"video/"
				));
		ContentFeatureDescriptionList = new Vector<String>(Arrays.asList(
				"正文"
				//,"新闻中心"
				,"报导/本报讯"
				,"记者/作者"
				,"责[任]?编[辑]"
				,"来源"
				//,"相关报导/相关专题/相关链接/相关新闻、"
				//,"热点新闻、热评榜、热点评论、"
				//,"新闻论坛、新闻搜索、新闻订阅、新闻排行、手机看新闻、"
				,"评论"
				//,"新闻、"
				));
		
	}
	/**
	 * Url feature is a four-dimension vector
	 * It looks like this:
	 * (
	 * dimension 0: 0/1,whether the url contains time information
	 * dimension 1: 0/1,whether the url contains sub-directory
	 * dimension 2: 0/1,whether the url contains some words that shouldn't appear in a content page,such as "index" or "bbs" or "blog"
	 * dimension 3: 0/1,whether the url ends with '/'
	 * )
	 */
	public Vector<Integer> UrlFeature = new Vector<>();
	
	/**
	 * Content feature is a six-dimension vector
	 * dimension 0: occurrence of "正文"
	 * dimension 1: occurrence of "报导/本报讯"
	 * dimension 2: occurrence of "记者/作者"
	 * dimension 3: occurrence of "责[任]?编[辑]?"
	 * dimension 4: occurrence of "来源"
	 * dimension 5: occurrence of "评论"
	 * dimension 6: 0/1,whether exist a block seems to be the one we want
	 */
	public Vector<Integer> WordFeature = new Vector<>();
	public HtmlFeature(){
		
	}
	
	public HtmlFeature(String url,String text){
		UrlFeature = DrawUrlFeature(url);
		WordFeature = DrawFeatureWord(text);
	}
	/**
	 * Input the main text of the html file
	 * Return the Feature Vector of the file based on statistics
	 */
	public Vector<Integer> DrawFeatureWord(String Text)
	{
		Vector<Integer> WordFreq = new Vector<Integer>();
		//Initialize all Feature Word of a news
		//FIXME:以静态常量的形式存储，且改用正则表达式？
		/*Vector<String> FeatureWord=new Vector<String>(Arrays.asList("新闻中心、","正文、","报导、","记者、作者、","本报讯、",
				"责任编辑。责编、","来源、本文来源、","相关报导、相关专题、相关链接、相关新闻、","热点新闻、热评榜、热点评论、","新闻论坛、新闻搜索、新闻订阅、新闻排行、手机看新闻、",
				"评论、","新闻、"));*/
		for(int i=0; i < ContentFeatureDescriptionList.size(); i++)
		{
			String keywords = ContentFeatureDescriptionList.get(i);
			WordFreq.add(keywordCalculateFreq(Text,keywords));
		}
		try {
			if(RatioArbiter.RatioJudge(Text)){
				WordFreq.add(1);
			}
			else{
				WordFreq.add(0);
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return WordFreq;
	}
	
	private Vector<Integer> DrawUrlFeature(String urlFileString)
	{
		String Url = urlFileString.replaceAll("\\.s?html?$", "");
		Vector<Integer> vec=new Vector<Integer>();
		if(isTimeInUrl(Url))
			vec.add(1);
		else{
			vec.add(0);
		}
		for(int i=0; i < UrlFeatureDescriptionList.size(); i++){
			if(keywordDetection(Url,UrlFeatureDescriptionList.get(i)))
				vec.add(1);
			else
				vec.add(0);
		}
		if(Url.endsWith("_"))
			vec.add(1);
		else {
			vec.add(0);
		}
		return vec;
	}
	
	/**
	 * Judge if Url has time feature
	 */
	private boolean isTimeInUrl(String urlString){
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
	 * Calculate keyword feature
	 * keywords should be split into array by '/'
	 */
	private boolean keywordDetection(String targetContent, String keywords)
	{
		String[] Words=keywords.split("/");
		for(int i=0; i < Words.length; i++)
		{
			if(Pattern.compile(Words[i],Pattern.CASE_INSENSITIVE).matcher(targetContent).find())
				return true;
		}
		return false;
	}
	
	
	/**
	 * Calculate the frequency of a group of Feature Word,
	 * which is splitted by "/"
	 */
	private int keywordCalculateFreq(String Text,String keywords)
	{
		String[] Words=keywords.split("/");
		int count = 0;
		for (int i = 0; i < Words.length; i++) {
			Matcher matcher = Pattern.compile(Words[i],Pattern.CASE_INSENSITIVE).matcher(Text);
			while(matcher.find())
				count++;
		}
		return count;
		/**
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
		}*/
	}
	
	
	
	/**
	 * Public methods:
	 */
	
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
	 * See if it contains words like "index" "blog"...
	 * @return
	 */
	public boolean isUrlContainNotContentInfo(){
		if(UrlFeature.get(2) != 0){
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * See if it ends with '/'
	 * @return
	 */
	public boolean isUrlNotLikelyContentPageEnding(){
		if(UrlFeature.get(3) != 0){
			return true;
		}
		else{
			return false;
		}
	}

	
	/**
	 * 
	 * @return occurrence of "正文"
	 */
	public int getTextContainContentIndicatorCount(){
		return WordFeature.get(0);
	}
	

	 /**
	  * 
	  * @return occurrence of "报导/本报讯"
	  */	 
	public int getTextContainReportCount(){
		return WordFeature.get(1);
	}
	/**
	  * 
	  * @return occurrence of "记者/作者"
	  */
	public int getTextContainJournalistCount(){
		return WordFeature.get(2);
	}
	/**
	  * 
	  * @return occurrence of "责[任]?编[辑]?"
	  */
	public int getTextContainEditorCount(){
		return WordFeature.get(3);
	}
	/**
	  * 
	  * @return occurrence of "来源"
	  */
	public int getTextContainSourceCount(){
		return WordFeature.get(4);
	}
	/**
	  * 
	  * @return occurrence of "评论"
	  */
	public int getTextContainReviewCount(){
		return WordFeature.get(5);
	}
	
	public boolean isTextContainDesiringBlock(){
		if(WordFeature.get(6) > 0)
			return true;
		else{
			return false;
		}
	}
	/**
	 * More features to be finished 
	 * 
	 */
	
	
	
	
	

}
