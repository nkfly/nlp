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
import java.util.Vector;

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
			stageThreeProcess();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
	
	public static void calculateOpinionAccuracy() throws IOException {
		Set <String> positiveOpinion = makeWordSet(new File("positive_opinion.txt"));
		Set <String> negativeOpinion = makeWordSet(new File("negative_opinion.txt"));
		Map <String, Integer> lmEstimation = new HashMap<String, Integer>();
		for (String p : positiveOpinion)lmEstimation.put(p, 1);
		for (String n : negativeOpinion)lmEstimation.put(n, -1);
		Map <String, Integer> recursiveEstimation = makeOpinionMap(new File("recursive_opinion.txt"));
		Map <String, Integer> answer = makeOpinionMap(new File("labeled_opinion.txt"));
		System.out.println("linear model accuracy : " + calculateAccuracy(lmEstimation, answer));
		System.out.println("recursive model accuracy : " + calculateAccuracy(recursiveEstimation, answer));
		
	}
	
	public static Map <String, Integer>makeOpinionMap(File f) throws IOException {
		Map <String, Integer> result = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while((line = br.readLine()) != null) {
			System.out.println(line);
			String [] tokens = line.split("\\s+|:");
			result.put(tokens[0], Integer.parseInt(tokens[1]));
		}
		br.close();
		return result;
	}
	
	public static double calculateAccuracy(Map <String, Integer> estimation, Map <String, Integer> answer){
		double match = 0.0;
		for (String word : estimation.keySet()) {
			if (estimation.get(word) == answer.get(word)) {
				match += 1;
			}
		}
		return match / estimation.size();
		
	}
	
	public static void runRecursiveDetermineMLEOpinionWord() throws IOException {
		File hotelTraining = new File("207884_hotel_training.txt");
		List <HotelComment> hotelCommentList = InputFormatProcessor.process(hotelTraining);
		Vector <Vector <Word> > opinionInSentences = new Vector< Vector <Word> > ();
		Set <String> gtOpinion = makeWordSet(new File("gt_opinion.txt"));
		for (HotelComment hc : hotelCommentList) {
			String concatSentence = StringUtils.join(hc.getSentences(), "");
			Vector <Word> opinions = new Vector <Word>();
			for (String opinion : gtOpinion) {
				if (concatSentence.indexOf(opinion) != -1) {
					if (InputFormatProcessor.isPrefixNegative(concatSentence, opinion)) {
						opinions.add(new Word(opinion, -1));
					} else {
						opinions.add(new Word(opinion, 1));
					}
					
				}
			}
			opinionInSentences.add(opinions);
		}
		Map <String, Integer> wordToLike = new HashMap <String, Integer>(); 
		recursiveDetermineMLEOpinionWord(opinionInSentences, wordToLike, hotelCommentList);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("recursive_opinion.txt")));
		for (String key : wordToLike.keySet()) {
			bw.write(key + ":" + wordToLike.get(key)+"\n");
		}
		bw.close();

		
	}
	
	
	
	public static void recursiveDetermineMLEOpinionWord(Vector <Vector <Word>> opinionInSentences , Map <String, Integer> wordToLike, List<HotelComment> hcl)  {
		
		int minIndex = -1;
		int minSize = 1000000;
		for (int i = 0;i < opinionInSentences.size();i++) {
			if (opinionInSentences.get(i).size() != 0 && opinionInSentences.get(i).size() < minSize) {
				minSize = opinionInSentences.get(i).size();
				minIndex = i;
			}
		}
		
		if (minIndex == -1)return;
		
		
		
		String determinedWord = opinionInSentences.get(minIndex).get(0).getWord();
		
		double sumOfWeight = 0.0;
		
		
		for (int i = 0;i < opinionInSentences.size(); i++) {
			Vector <Word> row = opinionInSentences.get(i);
			for (int j = 0;j < row.size();j++){
				if (row.get(j).getWord().equals(determinedWord)) {
					sumOfWeight += row.get(j).getValue()/Math.pow(row.size(), 0.5)*(hcl.get(i).getLike() == 1 ? 1 : -1);
					row.remove(j);
					break;
				}
			}
		}
		if (sumOfWeight > 0)wordToLike.put(determinedWord, 1);
		else if (sumOfWeight < 0)wordToLike.put(determinedWord, -1);
		else wordToLike.put(determinedWord, hcl.get(minIndex).getLike());
		
		recursiveDetermineMLEOpinionWord(opinionInSentences, wordToLike, hcl);
				
	}
	
	public static void stageThreeProcess() throws Exception {
		File hotelTraining = new File("207884_hotel_training.txt");
		List <HotelComment> hotelCommentList = InputFormatProcessor.process(hotelTraining);
		
		
		Set <String> aspectSet = makeWordSet(new File("gt_aspect.txt"));
		
		Map <String, Double> opinionToPolarity = new HashMap<String, Double>();
		BufferedReader br = new BufferedReader(new FileReader(new File("recursive_opinion.txt")));
		String line;
		while ((line = br.readLine()) != null) {
			String [] tokens = line.split(":");
			opinionToPolarity.put(tokens[0], Double.valueOf(tokens[1]));
		}
		br.close();
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("hotel_test_12.out")));
		JointParser parser;
		parser = new JointParser("models/dep.m");
		POSTagger tag = new POSTagger("models/seg.m","models/pos.m");
					
					
		for (HotelComment hotelComment : hotelCommentList) {
			Map <String, Double> aspectToPolarity = new HashMap <String, Double>();
			for (String sentence : hotelComment.getSentences()) {
				for (String aspect : aspectSet) {
					if (sentence.indexOf(aspect) == -1) continue;
					
					// use dependency parsing to get correct adjective
					String[][] s = tag.tag2Array(sentence);
					if (s == null)continue;
					DependencyTree tree = parser.parse2T(s[0],s[1]);
					List <List <String> > wordPropertyMatrix = tree.toList();
					boolean dependencySuccess = false;
					for (int i = 0;i < wordPropertyMatrix.size();i++) {
						if (wordPropertyMatrix.get(i).get(0).equals(aspect)){
							int aspectDependencyId = Integer.parseInt(wordPropertyMatrix.get(i).get(2));
							if (aspectDependencyId == -1)continue;
							
							String opinion = wordPropertyMatrix.get( aspectDependencyId ).get(0);
							if (opinionToPolarity.get(opinion) == null)continue;// this opinion is not in the set
							
							if (aspectToPolarity.get(aspect) != null) {
								if (InputFormatProcessor.isPrefixNegative(sentence, opinion)) {
									aspectToPolarity.put(aspect, aspectToPolarity.get(aspect) - opinionToPolarity.get( opinion  ));
								} else {
									aspectToPolarity.put(aspect, aspectToPolarity.get(aspect) + opinionToPolarity.get( opinion  ));
								}
							} else {
								if (InputFormatProcessor.isPrefixNegative(sentence, opinion)) {
									aspectToPolarity.put(aspect, - opinionToPolarity.get( opinion  ));
								} else {
									aspectToPolarity.put(aspect, opinionToPolarity.get( opinion  ));
								}
							}
							dependencySuccess = true;
							break;
						} 
					}
					if (dependencySuccess)continue;
					
					
					// dependency parsing failed, use the primitive approach
					if (aspectToPolarity.get(aspect) != null) {
						aspectToPolarity.put(aspect, aspectToPolarity.get(aspect) + calculatePositionalPolarity(sentence, aspect, opinionToPolarity));
					} else {
						aspectToPolarity.put(aspect, calculatePositionalPolarity(sentence, aspect, opinionToPolarity));
					}
				}
			}
			bw.write(hotelComment.getId() + "\n");
			String positiveAspect = "";
			String negativeAspect = "";
			double sumOfPolarity = 0.0;
			for (String aspect : aspectToPolarity.keySet()) {
				if (aspectToPolarity.get(aspect) > 0) {
					positiveAspect += (aspect + "\t");
				} else if (aspectToPolarity.get(aspect) < 0){
					negativeAspect += (aspect + "\t");
				} else {// the aspect polarity is 0, so it is assumed to be the same as the document
					if (hotelComment.getLike() == 1) {
						positiveAspect += (aspect + "\t");
					} else {
						negativeAspect += (aspect + "\t");
					}
				}
				sumOfPolarity += aspectToPolarity.get(aspect);
			}
			bw.write(positiveAspect.trim() + "\n");
			bw.write(negativeAspect.trim() + "\n");
			bw.write( (sumOfPolarity >= 0 ? 1 : 2) + "\n");
		}
		bw.close();
		
	}
	
	public static double calculatePositionalPolarity( String sentence , String aspect, Map <String, Double> opinionToPolarity) {
		int aspectIndex = sentence.indexOf(aspect);
		double polarity = 0.0;
		for (String opinion : opinionToPolarity.keySet()) {
			int opinionWordIndex = sentence.indexOf(opinion); 
			if (opinionWordIndex == -1)continue;
			
			int distance = Math.abs(opinionWordIndex - aspectIndex);
			if (distance == 0)continue;
			
			double coeff = 5/distance;// 5 is a magic number
			if (InputFormatProcessor.isPrefixNegative(sentence, opinion)) {
				polarity += -1*opinionToPolarity.get(opinion)*coeff;
			} else {
				polarity += opinionToPolarity.get(opinion)*coeff;
			}
			
			
		}
		return polarity;
		
		
	}
	public static Set<String> makeWordSet(File file) throws IOException {
		String word;
		BufferedReader br = new BufferedReader(new FileReader(file));
		Set <String> set = new HashSet <String>(); 
		while ((word = br.readLine()) != null) {
			set.add(word.split("\\s+")[0]);			
		}
		br.close();
		return set;
	}
	
	public static Set<String> makeIntersectionWordSet(File file1, File file2) throws IOException {
		
		Set <String> set1 = makeWordSet(file1);
		Set <String> set2 = makeWordSet(file2); 
		set1.retainAll(set2);
		return set1;
	}
	
	public static void stageTwoProcess() throws IOException{
		
		Set <String>  opinionWords = makeWordSet(new File("gt_opinion.txt"));
		System.out.println(opinionWords.size());
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
		Map <Integer, String> dimensionToWord = makeDimensionToWordMap(wordToDimensionMap);
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
	
	public static Map <String, Integer> makeWordToDimensionMap(Set <String> opinionWords) throws IOException{
		Map <String, Integer> wordToDimensionMap = new HashMap<String, Integer>();
		int dimension = 1;// note the dimension starts with 1, because LIBSVM requires that
		for (String word : opinionWords) {
			wordToDimensionMap.put(word, dimension++);			
		}
		return wordToDimensionMap;
	}
	
	public static Map <Integer, String> makeDimensionToWordMap(Map <String, Integer> wordToDimensionMap) throws IOException{
		
		Map <Integer, String> dimensionToWordMap = new HashMap<Integer, String>();
		for (String word : wordToDimensionMap.keySet()) {
			dimensionToWordMap.put(wordToDimensionMap.get(word), word);			
		}
		return dimensionToWordMap;
	}

	public static void printTree(DependencyTree tree) throws Exception{
		ChineseTrans ct = new ChineseTrans();
		System.out.println(ct.toTrad(tree.toString()));
	}
	
	public static void stageOneProcess() throws IOException{
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
					
					int teamId = 12;
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
						//if (outputCount >= 100)break;
						adjOutput.write(dp.getAdjective() +"\n");
						outputCount++;
					}
					outputCount = 0;
					for (DependencyPair dp : sortNounArray) {
						if ( stopWords.contains(dp.getNoun()))continue;
						//if (outputCount >= 100)break;
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