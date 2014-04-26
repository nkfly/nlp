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

import kevin.zhang.NLPIR;
import ntu.nlp.format.HotelComment;
import ntu.nlp.format.InputFormatProcessor;

class Spliter {
    NLPIR nlpir = null;
    public Spliter(){
        this.nlpir = new NLPIR();
        String argu = "././";
        try {
            if (this.nlpir.NLPIR_Init(argu.getBytes("GB2312"),0,"0".getBytes("GB2312")) == false){
                System.out.println("init failed");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
       
    }

    public String getSplitString(String text,int tag){
       
        String splitStr = null;
        byte nativeBytes[];
        try {
            nativeBytes = nlpir.NLPIR_ParagraphProcess(text.getBytes("gbk"), tag);
            //因为分词是用c++编写的所以最后有一个\0，这边就不需要了，不然转码有问题的
            splitStr = new String(nativeBytes, 0, nativeBytes.length - 1, "gbk");
            System.out.println("分词结果为： " + splitStr);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
           
        return splitStr;  
    }
}

public class NLP {

	public static void main(String [] args){
		//System.out.println((new File(".")).getAbsolutePath());
		File hotelTraining = new File("207884_hotel_training.txt");
		List <HotelComment> hotelCommentList = InputFormatProcessor.process(hotelTraining);
		
		Spliter s = new Spliter();
        s.getSplitString(hotelCommentList.get(0).getOpinion(),0);
		
	}

}
