package ntu.nlp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import edu.fudan.nlp.cn.tag.POSTagger;
import edu.fudan.nlp.parser.dep.DependencyTree;
import edu.fudan.nlp.parser.dep.JointParser;
import edu.fudan.util.exception.LoadModelException;
import ntu.nlp.format.HotelComment;
import ntu.nlp.format.InputFormatProcessor;


public class NLP {

	public static void main(String [] args){
		//System.out.println((new File(".")).getAbsolutePath());
		File hotelTraining = new File("207884_hotel_training.txt");
		List <HotelComment> hotelCommentList = InputFormatProcessor.process(hotelTraining);
		
		JointParser parser;
		try {
			parser = new JointParser("models/dep.m");
			System.out.println("得到支持的依存关系类型集合");
			System.out.println(parser.getSupportedTypes());
			
			String word = hotelCommentList.get(0).getOpinion();
			test(parser, word);
		} catch (LoadModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		

	}
	private static void test(JointParser parser, String word) throws Exception {		
		POSTagger tag = new POSTagger("models/seg.m","models/pos.m");
		String[][] s = tag.tag2Array(word);
		try {
			DependencyTree tree = parser.parse2T(s[0],s[1]);
			System.out.println(tree.toString());
			String stree = parser.parse2String(s[0],s[1],true);
			System.out.println(stree);
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}

}