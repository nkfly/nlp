package ntu.nlp.format;

public class Word implements Comparable<Word>{
	private String word;
	private double value;
	public Word(String w, double v){
		setWord(w);
		value = v;

	}
	@Override
	public int compareTo(Word w) {
		if(this.value < w.getValue())return 1;
		else if(this.value > w.getValue())return -1;
		return 0;
	}

	
	public double getValue(){
		return value;

	}
	public void setValue(double value){
		this.value =  value;

	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}




}