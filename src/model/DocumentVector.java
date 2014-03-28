package model;

import java.util.HashMap;
import java.util.Set;

public class DocumentVector {
	
	private int doc;
	private String className;
	public HashMap<String, Double> termMap;
	
	public DocumentVector() {
		termMap = new HashMap<String, Double>();
	}
	
	public DocumentVector(DocumentVector aDocumentVector) {
		termMap = new HashMap<String, Double>();
		this.doc = aDocumentVector.getDoc();
		this.className = aDocumentVector.getClassName();
		this.termMap = new HashMap<String, Double>(aDocumentVector.termMap);
	}
	
	public void setTermWeight(String text, double weight) {
		termMap.put(text, weight);
	}
	
	public Double getTermWeight(String text) {
		return termMap.get(text);
	}
	
	public Set<String> getTerms() {
		return termMap.keySet();
	}
	
	public boolean hasTerm(String term) {
		return termMap.containsKey(term);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getDoc() {
		return doc;
	}

	public void setDoc(int doc) {
		this.doc = doc;
	}
	
	@Override
	public int hashCode() {
		return this.doc;
	}
}
