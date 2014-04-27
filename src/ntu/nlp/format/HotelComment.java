package ntu.nlp.format;

public class HotelComment {
	private int id;
	private int like;
	private String opinion;
	public HotelComment(int id, int like, String opinion) {
		this.id = id;
		this.like = like;
		this.opinion = opinion;
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
	public String getOpinion() {
		return opinion;
	}
	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

}