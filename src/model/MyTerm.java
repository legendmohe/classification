package model;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.index.TermDocs;

public class MyTerm {
	public int tf;
	public String nameString;
	public TermDocs termDocs;
	public HashSet<Integer> docsHashSet;
	public HashMap<String, Double> occourenceDocsHashMap;
	
	public MyTerm() {
		tf = 0;
		docsHashSet = new HashSet<Integer>();
		occourenceDocsHashMap = new HashMap<String, Double>();
	}
}
