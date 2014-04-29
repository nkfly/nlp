package ntu.nlp.format;

public class DependencyPair implements Comparable<DependencyPair>{
	private String adjective;
	private String noun;
	private int count = 0;
	public DependencyPair(String adjective, String noun){
		this(adjective, noun, 1);
	}
	public DependencyPair(String adjective, String noun, int count){
		this.adjective = adjective;
		this.noun = noun;
		this.count = count;
	}
	
	@Override
	public boolean equals(Object o){
		DependencyPair p = (DependencyPair)o;
		if (adjective.equals(p.adjective) && noun.equals(p.noun))return true;
		return false;
	} 
	public String getAdjective(){
		return adjective;
	}
	
	public String getNoun(){
		return noun;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	@Override
	public int compareTo(DependencyPair dp) {
		// TODO Auto-generated method stub
		if (count > dp.count)return -1;
		else if (count < dp.count)return 1;
		return 0;
	}
	

}
