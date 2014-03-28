package model;

public class SimilarityRate {
	private Integer doc;
	private double broInRange;
	private double firstMetPos;
	public double getBroInRange() {
		return broInRange;
	}
	public void setBroInRange(double broInRange) {
		this.broInRange = broInRange;
	}
	public double getFirstMetPos() {
		return firstMetPos;
	}
	public void setFirstMetPos(double firstMetPos) {
		this.firstMetPos = firstMetPos;
	}
	public Integer getDoc() {
		return doc;
	}
	public void setDoc(Integer doc) {
		this.doc = doc;
	}
}
