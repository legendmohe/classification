package model;

public class FeatureTerm {
	private int N01;
	private int N11;
	private int N00;
	private int N10;
	private double filiterWeight;
	private String text;
	private String className;
	
	public int getN01() {
		return N01;
	}
	public void setN01(int n01) {
		N01 = n01;
	}
	public int getN11() {
		return N11;
	}
	public void setN11(int n11) {
		N11 = n11;
	}
	public double getFiliterWeight() {
		return filiterWeight;
	}
	public void setFiliterWeight(double filiterWeight) {
		this.filiterWeight = filiterWeight;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public int getN00() {
		return N00;
	}
	public void setN00(int n00) {
		N00 = n00;
	}
	public int getN10() {
		return N10;
	}
	public void setN10(int n10) {
		N10 = n10;
	}
}
