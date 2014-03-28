package model;

public class ClassifyResult {
	private int doc;
	private String originalClassString;
	private String classString;
	
	public String getClassString() {
		return classString;
	}
	public void setClassString(String classString) {
		this.classString = classString;
	}
	public int getDoc() {
		return doc;
	}
	public void setDoc(int doc) {
		this.doc = doc;
	}
	public String getOriginalClassString() {
		return originalClassString;
	}
	public void setOriginalClassString(String originalClassString) {
		this.originalClassString = originalClassString;
	}

}
