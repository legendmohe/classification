package similarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import model.DocumentVector;
import model.ObjectPair;
import model.SimilarityRate;
import template.Template;
import constants.Constants;

public class Similarity {
	
	public static ArrayList<ObjectPair<DocumentVector, Double>> getSimilarityPairArrayList(
			ArrayList<DocumentVector> trainArrayList,
			DocumentVector testDocument,
			Distance distance) {
		
		
		ArrayList<ObjectPair<DocumentVector, Double>> pairArrayList = new ArrayList<ObjectPair<DocumentVector, Double>>();
		for (DocumentVector trainDocument : trainArrayList) 
		{
			pairArrayList.add(new ObjectPair<DocumentVector, Double>(trainDocument, distance.getSimilarityBetweenDocuments(testDocument, trainDocument)));
		}
		
		return pairArrayList;
	}

	public static Double[][] getSimilarityMap(ArrayList<DocumentVector> trainArrayList, Distance distance) {
//		HashMap<Integer, HashMap<Integer, Double>> resultMap = new HashMap<Integer, HashMap<Integer, Double>>();
		
		int tsize = trainArrayList.size();
		System.out.println("开始计算相似矩阵：" + tsize + "*" + tsize);
		
		Double[][] simM = new Double[tsize + 1][tsize + 1];
		for (DocumentVector documentVector1 : trainArrayList) {
//			HashMap<Integer, Double> simHashMap = new HashMap<Integer, Double>();
//			resultMap.put(documentVector1.getDoc(), simHashMap);
			int doc1 = documentVector1.getDoc();
			for (DocumentVector documentVector2 : trainArrayList) {
				int doc2 = documentVector2.getDoc();
				if (documentVector2.getDoc() == documentVector1.getDoc()) { //自己也要算上
					simM[doc1][doc2] = distance.getZeroDistance();
				}
//				Double simValue = null;
//				HashMap<Integer, Double> simHashMap2 = resultMap.get(documentVector2.getDoc());
//				if (simHashMap2 != null) {
//					simValue = simHashMap2.get(documentVector1.getDoc());
//				}
				Double simValue = simM[doc2][doc1];
				
				if (simValue == null) {
					simValue = distance.getSimilarityBetweenDocuments(documentVector1, documentVector2);
				}
//				simHashMap.put(documentVector2.getDoc(), simValue);
				simM[doc1][doc2] = simValue;
			}
			
//			System.out.println(documentVector1.getDoc());
		}
		
		System.out.println("相似矩阵计算完毕.");
		
		return simM;
	}
	public static HashMap<Integer, SimilarityRate> getSimilarityRate(ArrayList<DocumentVector> srcVectors, Template template, Distance distance) {
		
        HashMap<Integer, SimilarityRate> similarityRateHashMap = new HashMap<Integer, SimilarityRate>();
		HashMap<Integer, ObjectPair<Integer, Double>> eachFirstMetHashMap = new HashMap<Integer, ObjectPair<Integer, Double>>();
		HashMap<Integer, ObjectPair<Integer, Double>> eachBroInRangeHashMap = new HashMap<Integer, ObjectPair<Integer, Double>>();
		for (double i = 0; i <= 1.0; i += 0.1) {
			double threshold = Math.round(i*100)/100.0;
			System.out.println("开始计算：threshold=" + threshold);

			ArrayList<DocumentVector> trainArrayList = new ArrayList<DocumentVector>();
			for (DocumentVector documentVector : srcVectors) {
				trainArrayList.add(new DocumentVector(documentVector));
			}

	        template.threshold = threshold;
	        template.processTemplateOnTrainDocumentVector(trainArrayList, false);
	        Double[][] simM = Similarity.getSimilarityMap(trainArrayList, distance);
			
			HashMap<Integer, DocumentVector> doc2DocumentHashMap = new HashMap<Integer, DocumentVector>();
			
			for (DocumentVector documentVector:trainArrayList) {
				doc2DocumentHashMap.put(documentVector.getDoc(), documentVector);
			}
			HashMap<String, Double> firstMetHashMap = new HashMap<String, Double>();
			HashMap<String, Double> broInRangeHashMap = new HashMap<String, Double>();
			HashMap<String, Integer> classNumberHashMap = new HashMap<String, Integer>();
//			for (Integer doc1 : similarityHashMap.keySet()) {
//			for (int doc1 = 0; doc1 < simM.length; doc1++) {
			for (DocumentVector curDocumentVector:trainArrayList) {
//				HashMap<Integer, Double> simMap = similarityHashMap.get(doc1);
//				ArrayList<Map.Entry<Integer, Double>> simEntries = new ArrayList<Map.Entry<Integer, Double>>();
//				simEntries.addAll(simMap.entrySet());
//				Collections.sort(simEntries, new Comparator<Map.Entry<Integer, Double>>() {
//					@Override
//					public int compare(Entry<Integer, Double> o1,
//							Entry<Integer, Double> o2) {
//						if (o1.getValue() > o2.getValue()) {
//							return -1;
//						}else if (o1.getValue() < o2.getValue()) {
//							return 1;
//						}else {
//							return 0;
//						}
//					}
//				});
//				DocumentVector curDocumentVector = doc2DocumentHashMap.get(doc1);
				int doc1 = curDocumentVector.getDoc();
				ArrayList<ObjectPair<Integer, Double>> simEntries = new ArrayList<ObjectPair<Integer, Double>>();
				for (DocumentVector documentVector2: trainArrayList) {
					int doc2 = documentVector2.getDoc();
					simEntries.add(new ObjectPair<Integer, Double>(doc2, simM[doc1][doc2]));
				}
								
				Collections.sort(simEntries, new Comparator<ObjectPair<Integer, Double>>() {
					@Override
					public int compare(ObjectPair<Integer, Double> o1,
							ObjectPair<Integer, Double> o2) {
						if ((Double)o1.getTwo() > (Double)o2.getTwo()) {
							return -1;
						}else if ((Double)o1.getTwo() < (Double)o2.getTwo()) {
							return 1;
						}else {
							return 0;
						}
					}
				});
				simEntries.remove(0); //remove self
				Integer classNumberInteger = classNumberHashMap.get(curDocumentVector.getClassName());
				if (classNumberInteger == null) {
					classNumberInteger = 0;
				}
				classNumberHashMap.put(curDocumentVector.getClassName(), ++classNumberInteger);
				
				int broRange = Constants.BroRange;
				int broNum = 0;
				int metBro = -1;
				int index = 0;
				for (ObjectPair<Integer, Double> entry : simEntries) {
					if (doc2DocumentHashMap.get(entry.getOne()).getClassName().equals(curDocumentVector.getClassName())) {
						if (broRange > index) {
							broNum++;
						}
						if (metBro == -1) {
							metBro = index;
						}
					}
					index++;
					if (broRange <= index && metBro != -1) {
						break;
					}
				}
				
				ObjectPair<Integer, Double> eachFirstMetPair = eachFirstMetHashMap.get(curDocumentVector.getDoc());
				if (eachFirstMetPair == null) {
					eachFirstMetPair = new ObjectPair<Integer, Double>(Integer.MAX_VALUE, threshold);
				}
				if (metBro <= (Integer)eachFirstMetPair.getOne()) {
					eachFirstMetPair.setOne(metBro);
					eachFirstMetPair.setTwo(threshold);
					eachFirstMetHashMap.put(curDocumentVector.getDoc(), eachFirstMetPair);
				}
				ObjectPair<Integer, Double> eachBroInRangePair = eachBroInRangeHashMap.get(curDocumentVector.getDoc());
				if (eachBroInRangePair == null) {
					eachBroInRangePair = new ObjectPair<Integer, Double>(-1, threshold);
				}
				if (broNum >= (Integer)eachBroInRangePair.getOne()) {
					eachBroInRangePair.setOne(broNum);
					eachBroInRangePair.setTwo(threshold);
					eachBroInRangeHashMap.put(curDocumentVector.getDoc(), eachBroInRangePair);
				}
				
				Double classBroNum = broInRangeHashMap.get(curDocumentVector.getClassName());
				if (classBroNum == null) {
					classBroNum = 0.0;
				}
				classBroNum += broNum;
				broInRangeHashMap.put(curDocumentVector.getClassName(), classBroNum);
				
				Double classFirstMetNum = firstMetHashMap.get(curDocumentVector.getClassName());
				if (classFirstMetNum == null) {
					classFirstMetNum = 0.0;
				}
				classFirstMetNum += metBro;
				firstMetHashMap.put(curDocumentVector.getClassName(), classFirstMetNum);
			}
			
//			for(String className: broInRangeHashMap.keySet()) {
//				System.out.println("class: " + className + " | broInRange: " + broInRangeHashMap.get(className)/classNumberHashMap.get(className)); 
//			}
//			for(String className: firstMetHashMap.keySet()) {
//				System.out.println("class: " + className + " | firstMet: " + firstMetHashMap.get(className)/classNumberHashMap.get(className)); 
//			}
		}
		
		for (Integer doc : eachFirstMetHashMap.keySet()) {
			double broDouble = (Double) eachBroInRangeHashMap.get(doc).getTwo();
			double firstDouble = (Double) eachFirstMetHashMap.get(doc).getTwo();
//			if (Math.abs(broDouble - firstDouble) <= 0.0001) {
//				System.out.println("same: " + doc + " | " + firstDouble);
//			}
			SimilarityRate newRate = new SimilarityRate();
			newRate.setDoc(doc);
			newRate.setFirstMetPos(firstDouble);
			newRate.setBroInRange(broDouble);
			similarityRateHashMap.put(doc, newRate);
//			System.out.println("doc:" + doc + " bro:" + broDouble + " first:" + firstDouble);
		}
		
		System.out.println("计算完毕.");
		
		return similarityRateHashMap;
	}
}
