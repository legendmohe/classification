package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import model.MyTerm;
import model.WeightTerm;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.store.FSDirectory;

import constants.Constants;

public class Processor {
	private static IndexReader shareReader;
	private static int totalTermCount;
	
	public static IndexReader shareIndexReader() {
		if (shareReader == null) {
			try {
				shareReader = IndexReader.open(FSDirectory.open(new File(Constants.INDEX_STORE_PATH)));
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return shareReader;
	}
	
	public static int getTotalTermCount() {
		if (totalTermCount == 0) {
			try {
				TermEnum termEnum=shareReader.terms(new Term("content"));
				while (termEnum.next()){
					if (!termEnum.term().field().equals("content")) { //要过滤
						continue;
					}
					TermDocs termDocs = shareReader.termDocs(termEnum.term());
					while (termDocs.next()) {
						totalTermCount += termDocs.freq();
					}
				}
				System.out.println(totalTermCount);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return totalTermCount;
	}
	
	public ArrayList<MyTerm> myTermsArrayFromIndexReader(IndexReader indexReader) throws Exception {
		System.out.println("begin myTermsArrayFromIndexReader");
		
		TermEnum termEnum=indexReader.terms(new Term("content"));
		ArrayList<MyTerm> resultArrayList = new ArrayList<MyTerm>();
		while(termEnum.next()) {
			if (!termEnum.term().field().equals("content")) { //要过滤
				continue;
			}
        	String textString = termEnum.term().text();
        	if (textString.length() < Constants.minTremLength || termEnum.docFreq() < Constants.minDocFreq) {
				continue;
			}
        	MyTerm myTerm = new MyTerm();
        	myTerm.nameString = textString;
        	myTerm.termDocs = indexReader.termDocs(termEnum.term());
        	
        	while (myTerm.termDocs.next()) {
        		myTerm.tf += myTerm.termDocs.freq();
        		myTerm.docsHashSet.add(myTerm.termDocs.doc());
			}
        	if (myTerm.tf < Constants.minTremFreq) {
				continue;
			}
        	resultArrayList.add(myTerm);
        }
        
        System.out.println("end myTermsArrayFromIndexReader");
		return resultArrayList;
	}
	
	public void calculateRelevanceBetweenTwoTermsForMyTermArray(ArrayList<MyTerm> myTermArray) {
		System.out.println("begin calculateOccourenceCountForMyTermArray");
		int count = 0;
        for (MyTerm myTerm : myTermArray) {
        	System.out.println(myTerm.nameString + ": " + ++count + "/" + myTermArray.size());
        	for (MyTerm oMyTerm : myTermArray) {
        		if (myTerm == oMyTerm) {
					continue;
				}
        		Double score = oMyTerm.occourenceDocsHashMap.get(myTerm.nameString);
        		if (score != null){
        			myTerm.occourenceDocsHashMap.put(oMyTerm.nameString, score);
        		}else {
        			score = Scorer.getRelevanceBetweenTwoTerms(myTerm, oMyTerm);
        			if (score > 0) {
        				myTerm.occourenceDocsHashMap.put(oMyTerm.nameString, score);
					}
        		}
			}
		}
        System.out.println("end calculateOccourenceCountForMyTermArray");
	}
	
	public ArrayList<WeightTerm> sortedTermWeightArrayOfDoc(int doc, IndexReader indexReader, Constants.KEYWORD_TYPE type) {
		ArrayList<WeightTerm> resultArrayList = null;
		try {
			resultArrayList = new ArrayList<WeightTerm>();
			
			switch (type) {
			case KEYWORD_TFIDFSCORE:
				Scorer.TFIDFWeightTermArrayOfDoc(doc, indexReader, resultArrayList);
				break;
			case KEYWORD_MAXENTROPY:
				Scorer.maximumEntropyweightTermArrayOfDoc(doc, indexReader, resultArrayList);
				break;
			case KEYWORD_BM25:
				Scorer.BM25WeightTermArrayOfDoc(doc, indexReader, resultArrayList);
				break;
			case KEYWORD_DLH:
				Scorer.DLHWeightTermArrayOfDoc(doc, indexReader, resultArrayList);
				break;
			default:
				break;
			}
			
			
			Collections.sort(resultArrayList, new Comparator<WeightTerm>() {
				public int compare(WeightTerm t1, WeightTerm t2) {
				    if (t1.weight < t2.weight) {
						return 1;
					}else if (t1.weight == t2.weight) {
						return 0;
					}else {
						return -1;
					}
				}
			});
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return resultArrayList;
	}

	public ArrayList<Integer> distributionOfTermInDoc(Term term, int doc, IndexReader indexReader) {
		ArrayList<Integer> resultArrayList = new ArrayList<Integer>();
		try {
			TermPositions termPositions = indexReader.termPositions(term);
			
			boolean isFound = false;
			while (termPositions.next()){
				if (termPositions.doc() == doc) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				System.out.println(term.text() + "不存在");
				return null;
			}
			
			System.out.println(term.text() + "出现了" + termPositions.freq() + "次");
			for(int j = 0; j < termPositions.freq(); j++) {
				resultArrayList.add(termPositions.nextPosition());
		    }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return resultArrayList;
	}
	
}
