package ntu.nlp.format;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class InputFormatProcessor {
	public static List <DocumentVector> convertToDocumentVector(Map <String, Integer> wordToDimensionMap ,List <HotelComment> hotelCommentList){
		List <DocumentVector> documentVectorList = new ArrayList <DocumentVector>();
		Map <Integer, Double> dimensionToIdf = new HashMap<Integer, Double>();
		for (int dimension = 1;dimension <= wordToDimensionMap.size();dimension++) {
			dimensionToIdf.put(dimension, 0.0);
		}
		
		for (HotelComment hc : hotelCommentList) {
			Map <Integer, Integer> dimensionToTermFrequency = new HashMap <Integer, Integer>();
			
			String [] sentences = hc.getSentences();
			String concatSentences = StringUtils.join(sentences);
			for (String word: wordToDimensionMap.keySet()) {
				if (concatSentences.indexOf(word) == -1)continue;
				dimensionToIdf.put(wordToDimensionMap.get(word), dimensionToIdf.get(wordToDimensionMap.get(word)) + 1  );				
			}
			
			for (int i = 0;i < sentences.length;i++) {
				for (String word : wordToDimensionMap.keySet()) {
					if (sentences[i].indexOf(word) == -1)continue;
					
					if ( dimensionToTermFrequency.get(wordToDimensionMap.get(word)) == null ) {
						dimensionToTermFrequency.put(wordToDimensionMap.get(word), StringUtils.countMatches(sentences[i], word));
					} else {
						dimensionToTermFrequency.put(wordToDimensionMap.get(word), dimensionToTermFrequency.get(wordToDimensionMap.get(word)) + StringUtils.countMatches(sentences[i], word));
					}
				}
			}
			
			
			Dimension [] dimensionArray = new Dimension[dimensionToTermFrequency.size()];
			int index = 0;
			for (Integer dimension : dimensionToTermFrequency.keySet()) {
				dimensionArray[index++] = new Dimension(dimension, dimensionToTermFrequency.get(dimension));
			}
			Arrays.sort(dimensionArray);
			documentVectorList.add(new DocumentVector(hc.getId(), hc.getLike(), dimensionArray));
			
		}
		
		int numberOfDocument = hotelCommentList.size();
		for (Integer dimension : dimensionToIdf.keySet()) {
			dimensionToIdf.put(dimension, Math.log(numberOfDocument/(dimensionToIdf.get(dimension)+1)) );			
		}
		
		for (DocumentVector dv : documentVectorList) {
			Dimension [] dimensionArray = dv.getDimensionArray();
			for (Dimension d : dimensionArray) {
				d.setValue(d.getValue()*dimensionToIdf.get(d.getDimension()));
			}
			
		}
		return documentVectorList;
		
	}
	public static List <HotelComment> process(File hotelTraining){
		List <HotelComment> hotelCommentList = new ArrayList <HotelComment>();
		try {
			//BufferedReader bf = new BufferedReader(new FileReader(hotelTraining));
			BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(hotelTraining), "UTF8"));
			String line = null;
			while ( (line = bf.readLine()) != null) {
				int like = Integer.parseInt(line);
				line = bf.readLine();
				int indexOfDelimiter = line.indexOf('|');
				int id = Integer.parseInt(line.substring(0, indexOfDelimiter));
				String opinion = line.substring(indexOfDelimiter+1);
				hotelCommentList.add(new HotelComment(id, like, opinion));

			}

			bf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hotelCommentList;

	}

}