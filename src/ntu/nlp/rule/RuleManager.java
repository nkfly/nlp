package ntu.nlp.rule;

import java.util.List;

import ntu.nlp.format.DependencyPair;

public class RuleManager {
	public static boolean isAdjective(String word){
		return word.equals("形谓词") || word.equals("形容词"); 
		
	}
	
	public static boolean isRoot(String word){
		return word.equals("Root"); 
	}
	
	public static boolean isSubject(String word){
		return word.equals("主语"); 
	}
	
	public static String findSubject(List <List <String> > wordPropertyMatrix){
		for (List <String> wordProperty : wordPropertyMatrix) {
			if (isSubject(wordProperty.get(3))) {
				return wordProperty.get(0);
			}
		}
		return null;
		
	}
	public static DependencyPair checkDependencyPair(List <List <String> > wordPropertyMatrix ){
		for (List <String> wordProperty : wordPropertyMatrix) {
			//System.out.println(wordProperty);
			// wordProperty format: word, word type like 名詞, dependency id, grammar type like 主語
			if (isAdjective(wordProperty.get(1)) && isRoot(wordProperty.get(3))) {
				String subject = findSubject(wordPropertyMatrix);
				if (subject != null)return new DependencyPair(wordProperty.get(0), subject);
				
				
			}
			
			
			
		}
		
		
		return null;
		
	}

}
