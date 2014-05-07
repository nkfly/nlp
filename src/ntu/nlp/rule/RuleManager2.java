package ntu.nlp.rule;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import ntu.nlp.format.DependencyPair;

import edu.fudan.nlp.cn.ChineseTrans;

public class RuleManager2 {
	public static boolean isAdjective(String word){
		return word.equals("形谓词") || word.equals("形容词");

	}

	public static boolean isNoun(String word){
		return word.equals("名词");
	}

	public static int searchDistance(Map <Integer, Integer>index_dependency, int src, int dest){
		if ( src == dest )
			return 0;
		if ( index_dependency.get(src) == -1 ){
			return 1 + searchDistance(index_dependency, (int)index_dependency.get(dest), src);
		}
		else{
			return 1 + searchDistance(index_dependency, (int)index_dependency.get(src), dest);
		}
	}

	public static DependencyPair checkDependencyPair(List <List <String> > wordPropertyMatrix ){
		Map <Integer, Integer> index_dependency = new HashMap<Integer, Integer>();
		for ( int i = 0; i < wordPropertyMatrix.size(); i++ ){
			List <String> word = wordPropertyMatrix.get(i);
			index_dependency.put(i, Integer.valueOf(word.get(2)));
		}

		int min_dis = 1000;
		String noun = null;
		String adjective = null;
		for ( int i = 0; i < wordPropertyMatrix.size(); i++ ){
			List <String> word1 = wordPropertyMatrix.get(i);
			if ( !isAdjective(word1.get(1)) )
				continue;
			for ( int j = 0; j < wordPropertyMatrix.size(); j++ ){
				List <String> word2 = wordPropertyMatrix.get(j);
				if ( !isNoun(word2.get(1)) )
					continue;
				int dis =  searchDistance(index_dependency, i, j);
				if ( min_dis > dis ){
					min_dis = dis;
					adjective = word1.get(0);
					noun = word2.get(0);
				}
			}
		}

		if ( noun != null && adjective != null ){
			return new DependencyPair(adjective, noun);
		}
		return null;

	}

}
