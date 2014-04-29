package ntu.nlp.format;

public class HotelComment {
	private int id;
	private int like;
	private String[] sentences;
	public HotelComment(int id, int like, String opinion) {
		this.id = id;
		this.like = like;
		this.setSentences(opinion.split("\\，|\\!|\\,|\\.|\\？|\\。|！"));
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getLike() {
		return like;
	}
	public void setLike(int like) {
		this.like = like;
	}
	public String[] getSentences() {
		return sentences;
	}
	public void setSentences(String[] sentences) {
		this.sentences = sentences;
	}
	

}