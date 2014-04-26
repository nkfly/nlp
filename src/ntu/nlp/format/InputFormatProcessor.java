package ntu.nlp.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputFormatProcessor {
	public static List <HotelComment> process(File hotelTraining){
		List <HotelComment> hotelCommentList = new ArrayList <HotelComment>();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(hotelTraining));
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
