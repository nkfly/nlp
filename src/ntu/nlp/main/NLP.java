package ntu.nlp.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import sun.security.util.Length;

import edu.fudan.nlp.cn.tag.POSTagger;
import edu.fudan.nlp.parser.dep.DependencyTree;
import edu.fudan.nlp.parser.dep.JointParser;
import edu.fudan.util.exception.LoadModelException;
import ntu.nlp.format.DependencyPair;
import ntu.nlp.format.DocumentVector;
import ntu.nlp.format.HotelComment;
import ntu.nlp.format.InputFormatProcessor;
import ntu.nlp.format.Word;
import ntu.nlp.rule.RuleManager;

import ntu.nlp.rule.RuleManager2;

import edu.fudan.nlp.cn.ChineseTrans;

public class NLP {
	private static Set <String> stopWords = new HashSet(Arrays.asList("還","來","會","沒","個","較","卻","麼",
			"並","這個","什麼","整個","再次","總","不過","這家","讓","於","這種","換","們","般","別","覺","女","這樣",""));

	public static void main(String [] args){
		try {
			stageTwoProcess();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
	
	public static void stageThreeProcess() {
		
	}
	
	public static void stageTwoProcess() throws IOException{
		
		File opinionWords = new File("gt_opinion.txt");
		Map <String, Integer> wordToDimensionMap = makeWordToDimensionMap(opinionWords);
		int maxDimension = wordToDimensionMap.size();
		File hotelTraining = new File("207884_hotel_training.txt");
		List <HotelComment> hotelCommentList = InputFormatProcessor.process(hotelTraining);
		
		List <DocumentVector> documentVectorList = InputFormatProcessor.convertToDocumentVector(wordToDimensionMap, hotelCommentList);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("svm.in")));
		for (DocumentVector dv : documentVectorList ) {
			bw.write(dv.toLIBSVMString(maxDimension) + "\n");			
		}
		bw.close();
		
		bw = new BufferedWriter(new FileWriter(new File("doucment_vector.csv")));
		for (int i = 1;i <= maxDimension;i++) {
			bw.write(i + ",");
		}
		bw.write("class\n");
		
		for (DocumentVector dv : documentVectorList ) {
			bw.write(dv.toCSVString(maxDimension) + "\n");			
		}
		bw.close();
		
		File regression = new File("regression.txt");
		BufferedReader br = new BufferedReader(new FileReader(regression));
		String line;
		br.readLine();
		br.readLine();
		
		List <Word> positiveOpinion = new ArrayList <Word>();
		List <Word> negativeOpinion = new ArrayList <Word>();
		
		int index = 1;
		Map <Integer, String> dimensionToWord = makeDimensionToWordMap(opinionWords);
		while ((line = br.readLine()) != null) {
			double value = Double.valueOf(line.replaceAll("\\s+", " ").split(" ")[1]); 
			if ( value < 0) {
				positiveOpinion.add( new Word(dimensionToWord.get(index), -1*value) );
			} else {
				negativeOpinion.add(new Word(dimensionToWord.get(index), value));
			}
			index++;
		}
		br.close();
		
		Word [] positiveOpinionArray = new Word[positiveOpinion.size()];
		positiveOpinionArray = positiveOpinion.toArray(positiveOpinionArray);
		Word [] negativeOpinionArray = new Word[negativeOpinion.size()];
		negativeOpinionArray = negativeOpinion.toArray(negativeOpinionArray);
		
		Arrays.sort(positiveOpinionArray);
		Arrays.sort(negativeOpinionArray);

		bw = new BufferedWriter(new FileWriter(new File("positive_opinion.txt")));
		for (int i = 0;i < positiveOpinionArray.length;i++) {
			bw.write(positiveOpinionArray[i].getWord() + " " + positiveOpinionArray[i].getValue() +"\n");
		}
		bw.close();
		
		bw = new BufferedWriter(new FileWriter(new File("negative_opinion.txt")));
		for (int i = 0;i < negativeOpinionArray.length;i++) {
			bw.write(negativeOpinionArray[i].getWord() + " " + negativeOpinionArray[i].getValue()+"\n");
		}
		bw.close();
		
		
	}
	
	public static Map <String, Integer> makeWordToDimensionMap(File opinionWords) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(opinionWords));
		String word;
		Map <String, Integer> wordToDimensionMap = new HashMap<String, Integer>();
		int dimension = 1;// note the dimension starts with 1, because LIBSVM requires that
		while ((word = br.readLine()) != null) {
			wordToDimensionMap.put(word, dimension++);			
		}
		br.close();
		return wordToDimensionMap;
	}
	
	public static Map <Integer, String> makeDimensionToWordMap(File opinionWords) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(opinionWords));
		String word;
		Map <Integer, String> wordToDimensionMap = new HashMap<Integer, String>();
		int dimension = 1;// note the dimension starts with 1, because LIBSVM requires that
		while ((word = br.readLine()) != null) {
			wordToDimensionMap.put(dimension++, word);			
		}
		br.close();
		return wordToDimensionMap;
	}

	public static void printTree(DependencyTree tree) throws Exception{
		ChineseTrans ct = new ChineseTrans();
		System.out.println(ct.toTrad(tree.toString()));
	}
	
	public static void stageOneProcess(){
		//System.out.println((new File(".")).getAbsolutePath());
				File hotelTraining = new File("207884_hotel_training.txt");
				List <HotelComment> hotelCommentList = InputFormatProcessor.process(hotelTraining);

				JointParser parser;
				Map <String, DependencyPair> dependencyPairToCount = new HashMap<String, DependencyPair>();
				Map <String, Integer> adjectiveToCount = new HashMap<String, Integer>();
				Map <String, Integer> nounToCount = new HashMap<String, Integer>();
				try {
					parser = new JointParser("models/dep.m");
					//System.out.println("得到支持的依存关系类型集合");
					//System.out.println(parser.getSupportedTypes());
					POSTagger tag = new POSTagger("models/seg.m","models/pos.m");
					for (HotelComment hotelComment : hotelCommentList) {
						for (String sentence : hotelComment.getSentences()){
							String[][] s = tag.tag2Array(sentence);
							if (s == null)continue;
							DependencyTree tree = parser.parse2T(s[0],s[1]);
							//System.out.println(tree);
							List <List <String> > wordPropertyMatrix = tree.toList();
							//DependencyPair dp = RuleManager.checkDependencyPair(wordPropertyMatrix);
							DependencyPair dp = RuleManager2.checkDependencyPair(wordPropertyMatrix);
							if (dp == null) continue;
							//System.out.println(dp.getNoun() + " " + dp.getAdjective());
							if (dependencyPairToCount.get(dp.getAdjective()+dp.getNoun()) == null) {
								dependencyPairToCount.put(dp.getAdjective()+dp.getNoun(), dp);
							} else {
								dp.setCount(dp.getCount()+1);
								dependencyPairToCount.put(dp.getAdjective()+dp.getNoun(), dp);
							}
							if (adjectiveToCount.get(dp.getAdjective()) == null) {
								adjectiveToCount.put(dp.getAdjective(), 1);
							} else {
								adjectiveToCount.put(dp.getAdjective(), adjectiveToCount.get(dp.getAdjective())+1);
							}
							if (nounToCount.get(dp.getNoun()) == null) {
								nounToCount.put(dp.getNoun(), 1);
							} else {
								nounToCount.put(dp.getNoun(), nounToCount.get(dp.getNoun())+1);
							}



						}

					}
					
					int teamId = 0;
					BufferedWriter output = new BufferedWriter(new FileWriter(new File("pair.txt")));
					BufferedWriter adjOutput = new BufferedWriter(new FileWriter(new File("opinion_"+teamId+".txt")));
					BufferedWriter nounOutput = new BufferedWriter(new FileWriter(new File("aspect_"+teamId+".txt")));
					DependencyPair [] sortDependencyPairArray = new DependencyPair[dependencyPairToCount.size()];
					DependencyPair [] sortAdjArray = new DependencyPair[adjectiveToCount.size()];
					DependencyPair [] sortNounArray = new DependencyPair[nounToCount.size()];
					int index = 0;
					for (DependencyPair value : dependencyPairToCount.values()) {
						sortDependencyPairArray[index++] = value;
					}
					index = 0;
					for (String key : adjectiveToCount.keySet()) {
						sortAdjArray[index++] = new DependencyPair(key, "", adjectiveToCount.get(key));
					}
					index = 0;
					for (String key : nounToCount.keySet()) {
						sortNounArray[index++] = new DependencyPair("", key, nounToCount.get(key));
					}
					Arrays.sort(sortDependencyPairArray);
					Arrays.sort(sortAdjArray);
					Arrays.sort(sortNounArray);

					for (DependencyPair dp : sortDependencyPairArray) {
						output.write(dp.getAdjective() + "->" + dp.getNoun() + ":" + dp.getCount()+"\n");
					}
					int outputCount = 0;
					for (DependencyPair dp : sortAdjArray) {
						if ( stopWords.contains(dp.getAdjective()))continue;
						if (outputCount >= 100)break;
						adjOutput.write(dp.getAdjective() +"\n");
						outputCount++;
					}
					outputCount = 0;
					for (DependencyPair dp : sortNounArray) {
						if ( stopWords.contains(dp.getNoun()))continue;
						if (outputCount >= 100)break;
						nounOutput.write(dp.getNoun() + "\n");
						outputCount++;
					}


					output.close();
					adjOutput.close();
					nounOutput.close();



				} catch (LoadModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
	}


}