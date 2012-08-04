package test;

import java.io.File;
import java.io.FileFilter;

import org.htmlparser.*;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import test.basic.HtmlArchive;
import test.logic.HtmlManager;
import test.logic.HtmlManipulator;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		File rootFile = new File("C:\\Users\\firstprayer\\Desktop\\新建文件夹");
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
		//HtmlManipulator.Test_HtmlExtractionImportantContent("C:\\Users\\firstprayer\\Desktop\\NewsMinerTestDataBase");
	}

}
