package org.newsextractor;

import java.io.File;
import java.io.FileFilter;

import org.htmlparser.*;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;
import org.newsextractor.production.HtmlArchive;
import org.newsextractor.production.HtmlManager;
import org.newsextractor.textprocess.HtmlManipulator;

/**
 * 
 * @author firstprayer
 * This is the main extrance of the news extractor
 * It takes the path of the root directory as a input. The root directory should contains many
 * sub-directories, each one of which contains html files that belong to one cluster
 * The information of each directory will be used to initialize a HtmlArchive Object
 * 
 */
public class TestMain {

	/**
	 * @param args:useless
	 */
	public static void main(String[] args) 
	{
		//File rootFile = new File("C:\\Users\\firstprayer\\Desktop\\testdatabase\\testdatabase");//
		//File rootFile = new File("C:\\Users\\firstprayer\\Desktop\\新建文件夹");
		File rootFile = new File("C:\\Users\\firstprayer\\Desktop\\outputs");
		//File rootFile = new File("C:\\Users\\firstprayer\\Desktop\\clusteroutput");
		
		File[] archiveListFiles = rootFile.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if(pathname.isDirectory())
					return true;
				return false;
			}
		});
		HtmlManager htmlManager = new HtmlManager();
		for (int i = 0; i < archiveListFiles.length; i++) {
			htmlManager.AddArchive(new HtmlArchive(archiveListFiles[i], archiveListFiles[i].getName()));
		}
		htmlManager.ProcessAll();
		
	}

}
