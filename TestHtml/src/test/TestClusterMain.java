package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import test.basic.UrlPattern;
import test.logic.HtmlManager;

public class TestClusterMain {
	public static void main(String[] args)
	{
		try {
			String pathString = "C:\\Users\\firstprayer\\Desktop\\新建文件夹\\";
			HtmlManager htmlManager = new HtmlManager();
			List<UrlPattern.UrlMapPath> mapList = new Vector<UrlPattern.UrlMapPath>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pathString + "map.txt"),"utf-8"));
			String text = "";
			while((text = reader.readLine()) != null)
			{
				String[] strings = text.split("::");
				if(strings.length == 2)
				{
					//UrlPattern.UrlMapPath path = (new UrlPattern()).new UrlMapPath(strings[0], strings[1]);
					mapList.add(new UrlPattern.UrlMapPath(strings[0],strings[1]));
				}
			}
			List<UrlPattern> patterns = htmlManager.UrlCluster(mapList);
			for (int i = 0; i < patterns.size(); i++) {
				UrlPattern pattern = patterns.get(i);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathString + i + ".txt"), "utf-8"));
				Iterator<UrlPattern.UrlMapPath> iterator = pattern.getUrlMapPathElements();
				while(iterator.hasNext())
				{
					UrlPattern.UrlMapPath mapPath = iterator.next();
					writer.write(mapPath.Url);
					writer.newLine();
				}
				writer.close();
			}
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
