package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import model.FeatureTerm;
import model.MyTerm;
import model.WeightTerm;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;

import constants.Constants;
import filter.CDDFilter;

public class Scorer {
	public static double getRelevanceBetweenTwoTerms(MyTerm aTerm, MyTerm otherTerm) {
		double score = 0;
		
		int coCount = 0;
		for (Integer doc : aTerm.docsHashSet) {
			if (otherTerm.docsHashSet.contains(doc)) {
				++coCount;
			}
		}
		
		if (coCount < Constants.minOccourence) {
			return 0;
		}
		
		if (aTerm.tf - otherTerm.tf == 0) {
			score = coCount;
		}else {
			score = coCount
					/(Math.log(Math.abs(aTerm.tf - otherTerm.tf) + 1));
		}
		
		return score;
	}

	public static void TFIDFWeightTermArrayOfDoc(int doc, IndexReader indexReader,
			ArrayList<WeightTerm> resultArrayList) throws IOException {
		final double log2 = Math.log(2);
		
		TermFreqVector termFreqVector = indexReader.getTermFreqVector(doc, "content");
		for (int i = 0; i < termFreqVector.size(); i++) {
			TermEnum termEnum = indexReader.terms(new Term("content", termFreqVector.getTerms()[i]));
			if (termEnum.term().text().length() < Constants.minTremLength) {
				continue;
			}
			
			double tf = termFreqVector.getTermFrequencies()[i];
			if (tf < Constants.minTremFreq) {
				continue;
			}
			
			double idf = Math.log((double)indexReader.numDocs()/termEnum.docFreq())/log2;
			
			double weight = idf*tf;
			
			WeightTerm term = new WeightTerm();
			term.nameString = termEnum.term().text();
			term.weight = weight;
			term.tf = tf;
			term.idf = idf;
			
			resultArrayList.add(term);
		}
	}
	
	public static void BM25WeightTermArrayOfDoc(int doc, IndexReader indexReader,
			ArrayList<WeightTerm> resultArrayList) throws IOException {
		final double log2 = Math.log(2);
		
		//获得该文档出现的term
		TermFreqVector termFreqVector = indexReader.getTermFreqVector(doc, "content");
		
		//长度normalize
		long currentDoclength = Preprocessor.fileLengArrayList.get(doc);
		double tfn = ((1-Constants.b) + Constants.b*(currentDoclength/(double)Preprocessor.avgFileLength));
		
		//平均单词长度
		double avgTermLangth = 0;
		int termNum = 0;
		TermEnum totalTermEnum = indexReader.terms(new Term("content"));
		while (totalTermEnum.next()) {
			if (!totalTermEnum.term().field().equals("content")) { //要过滤
				continue;
			}
			avgTermLangth += totalTermEnum.term().text().length();
			termNum++;
		}
		avgTermLangth = avgTermLangth/termNum;
		
		for (int i = 0; i < termFreqVector.size(); i++) {
			TermEnum termEnum = indexReader.terms(new Term("content", termFreqVector.getTerms()[i]));
			if (termEnum.term().text().length() < Constants.minTremLength) {
				continue;
			}
			
			double tf = termFreqVector.getTermFrequencies()[i];
			if (tf < Constants.minTremFreq) {
				continue;
			}
			tf = ((Constants.a + 1)*(tf/tfn))/(Constants.a + (tf/tfn));
			
			//平均出现位置
			TermPositions termPositions = indexReader.termPositions(new Term("content", termFreqVector.getTerms()[i]));
			while (termPositions.next()) {
				if (termPositions.doc() == doc) {
					break;//找到对应的文档
				}
			}
			
			double posSum = 0;
			for (int j = 0; j < termPositions.freq()/2; j++) {
				posSum += termPositions.nextPosition();
			}

			double avgPos = posSum/termPositions.freq();
			
			avgPos = ((1-Constants.d) + Constants.d*(Math.abs(1 - avgPos/(currentDoclength))));
			
			double idf = Math.log((indexReader.numDocs() - termEnum.docFreq() + 0.5)/(termEnum.docFreq() + 0.5))/log2;
			double norLength = ((1-Constants.c) + Constants.c*(termEnum.term().text().length()/avgTermLangth));
			double weight = idf*tf*norLength*avgPos;
			
			WeightTerm term = new WeightTerm();
			term.nameString = termEnum.term().text();
			term.weight = weight;
			term.tf = tf;
			term.idf = idf;
			term.norLength = norLength;
			term.norPos = avgPos;
			
			resultArrayList.add(term);
		}
	}

	public static void maximumEntropyweightTermArrayOfDoc(int doc, IndexReader indexReader,
			ArrayList<WeightTerm> resultArrayList) throws IOException {
		final double log2 = Math.log(2);
		//长度normalize
		double tfn = ((1-Constants.b) + Constants.b*(Preprocessor.fileLengArrayList.get(doc)/(double)Preprocessor.avgFileLength));
		
		TermFreqVector termFreqVector = indexReader.getTermFreqVector(doc, "content");
		
		int totalTermCount = Processor.getTotalTermCount();
		//词频
		HashMap<Integer, Integer> tfHashMap = new HashMap<Integer, Integer>();
		double notExistH = 0;
		for (int i = 0; i < termFreqVector.size(); i++) {
			TermDocs termDocs = indexReader.termDocs(new Term("content", termFreqVector.getTerms()[i]));
			Integer docTF = 0;
			while (termDocs.next()) {
				docTF += termDocs.freq();
			}
			tfHashMap.put(i, docTF);
			
			double p = (double)docTF/(totalTermCount);
			notExistH += -(p*Math.log(p));
		}
		
		for (int i = 0; i < termFreqVector.size(); i++) {
			TermEnum termEnum = indexReader.terms(new Term("content", termFreqVector.getTerms()[i]));
			if (termEnum.term().text().length() < Constants.minTremLength) {
				continue;
			}
			
			double tf = termFreqVector.getTermFrequencies()[i];
			if (tf < Constants.minTremFreq) continue;
			
			double dtf = tfHashMap.get(i);
			double totalH = 0;
			for (int j = 0; j < termFreqVector.size(); j++) {
				if (i == j) {
					continue;//不和自己算计
				}
				double subDtf = tfHashMap.get(j);
				double p = subDtf/(totalTermCount - dtf);
				totalH += -(p*Math.log(p));
			}
			//计算权重
			double weight = notExistH - totalH;
			
			WeightTerm term = new WeightTerm();
			term.nameString = termEnum.term().text();
			term.weight = weight;
			term.tf = tf;
			
			resultArrayList.add(term);
		}
	}

	public static void DLHWeightTermArrayOfDoc(int doc, IndexReader indexReader,
			ArrayList<WeightTerm> resultArrayList) throws IOException {
		final double log2 = Math.log(2);
		//长度normalize
		double avgFileLength = Preprocessor.fileLengArrayList.get(doc)/(double)Preprocessor.avgFileLength;
		
		TermFreqVector termFreqVector = indexReader.getTermFreqVector(doc, "content");
		//collectionTF
		HashMap<Integer, Integer> tfHashMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < termFreqVector.size(); i++) {
			TermDocs termDocs = indexReader.termDocs(new Term("content", termFreqVector.getTerms()[i]));
			Integer docTF = 0;
			while (termDocs.next()) {
				docTF += termDocs.freq();
			} 
			tfHashMap.put(i, docTF);
		}
		
		//关键词词宽
		double avgTermLangth = 0;
		int termNum = 0;
		TermEnum totalTermEnum = indexReader.terms(new Term("content"));
		while (totalTermEnum.next()) {
			if (!totalTermEnum.term().field().equals("content")) { //要过滤
				continue;
			}
			avgTermLangth += totalTermEnum.term().text().length();
			termNum++;
		}
		avgTermLangth = avgTermLangth/termNum;
	
		for (int i = 0; i < termFreqVector.size(); i++) {
			TermEnum termEnum = indexReader.terms(new Term("content", termFreqVector.getTerms()[i]));
			if (termEnum.term().text().length() < Constants.minTremLength) {
				continue;
			}
			
			double tf = termFreqVector.getTermFrequencies()[i];
			if (tf < Constants.minTremFreq) {
				continue;
			}

			double norLength = ((1-Constants.c) + Constants.c*(termEnum.term().text().length()/avgTermLangth));
			double P = (double)tfHashMap.get(i)/indexReader.numDocs();
			double qtw = (Math.log(1/P + 1)+Math.log(P + 1))/log2;
			double DLH = norLength*qtw*(
					1/(tf + 0.5))*(Math.log(avgFileLength*tf*(1/P))/log2
							+ (0.5*Math.log(
									2*Math.PI*tf*(1-(double)tf/Preprocessor.fileLengArrayList.get(doc))
							)/log2
						)
					);
			
			
			WeightTerm term = new WeightTerm();
			term.nameString = termEnum.term().text();
			term.weight = DLH;
			
			resultArrayList.add(term);
		}
	}
	
	public static double getFeatureWeight(FeatureTerm featureTerm, int tf) {
		double TF = Math.log(tf + 1);
		int M = featureTerm.getN11() + featureTerm.getN10() + featureTerm.getN01() + featureTerm.getN00();
		int df = featureTerm.getN11() + featureTerm.getN10();
		double IDF = Math.log(M*1.0/df)/Math.log(2.0);
//		return TF*IDF*(Math.log(featureTerm.getFiliterWeight() + 1));
		
		double featureValue = (new CDDFilter()).getFeatureValue(featureTerm);
		if (featureValue > 0) 
			return TF*IDF*Math.log(featureValue + 1);
		else 
			return TF*IDF;
	}
	
	public static double getCHIValue(FeatureTerm classifierTerm) {
		
		int N11 = classifierTerm.getN11();//在此类含此词
		int N01 = classifierTerm.getN01();//在此类不含此词
		int N10 = classifierTerm.getN10();//不在此类含此词
		int N00 = classifierTerm.getN00();//不在此类不含此词
		double value = Math.pow((1.0*N11*N00 - N10*N01), 2)/(1.0*(N11 + N01)*(N11 + N10)*(N10 + N00)*(N01 + N00));
		return value;
	}
	
	public static double getMyValue(FeatureTerm classifierTerm) {
		
		int N11 = classifierTerm.getN11();//在此类含此词
//		int N01 = classifierTerm.getN01();//在此类不含此词
		int N10 = classifierTerm.getN10();//不在此类含此词
//		int N00 = classifierTerm.getN00();//不在此类不含此词
		double between = Math.log(N11)*Math.log(((double)N11)/(N10 + 1) + 1.0);
//		double value = Math.pow((1.0*N11*N00 - N10*N01), 2)/(1.0*(N11 + N01)*(N11 + N10)*(N10 + N00)*(N01 + N00));
		return between;
	}

}
