package classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import model.ClassifyResult;
import model.DocumentVector;
import model.ObjectPair;
import similarity.CosineDistance;
import similarity.Distance;
import similarity.EuclideanDistance;
import similarity.MahalanobisDistance;
import similarity.Similarity;

public class KNNClassifier extends Classifier {
	
	int K;
	Distance distance;
	
	public KNNClassifier(int K, Distance distance) {
		this.K = K;
		this.distance = distance;
	}

	@Override
	protected ArrayList<ClassifyResult> classify(
			ArrayList<DocumentVector> testTrainArrayList,
			ArrayList<DocumentVector> trainArrayList) {

		HashMap<Integer, Double> avgSimilarityHashMap = new HashMap<Integer, Double>();
		
		ArrayList<ClassifyResult> resultArrayList = new ArrayList<ClassifyResult>();
		for (DocumentVector testDocument : testTrainArrayList) {
			System.out.print(".");
			//计算相似度
			ArrayList<ObjectPair<DocumentVector, Double>> pairArrayList = Similarity.getSimilarityPairArrayList(trainArrayList, testDocument, this.distance);
			
			Comparator<ObjectPair<DocumentVector, Double>> comparator = null;
			if (this.distance instanceof CosineDistance) {
				comparator = new Comparator<ObjectPair<DocumentVector, Double>>() {//相似度排序
					@Override
					public int compare(ObjectPair<DocumentVector, Double> o1, ObjectPair<DocumentVector, Double> o2) {
						if ((Double)(o1.getTwo()) > (Double)(o2.getTwo()))
							return -1;
						if ((Double)(o1.getTwo()) < (Double)(o2.getTwo()))
							return 1;
						else
							return 0;
					}
				};
			}else if (this.distance instanceof EuclideanDistance) {
				comparator = new Comparator<ObjectPair<DocumentVector, Double>>() {//相似度排序
					@Override
					public int compare(ObjectPair<DocumentVector, Double> o1, ObjectPair<DocumentVector, Double> o2) {
						if ((Double)(o1.getTwo()) > (Double)(o2.getTwo()))
							return 1;
						if ((Double)(o1.getTwo()) < (Double)(o2.getTwo()))
							return -1;
						else
							return 0;
					}
				};
			}else if (this.distance instanceof MahalanobisDistance) {
				comparator = new Comparator<ObjectPair<DocumentVector, Double>>() {//相似度排序
					@Override
					public int compare(ObjectPair<DocumentVector, Double> o1, ObjectPair<DocumentVector, Double> o2) {
						if ((Double)(o1.getTwo()) > (Double)(o2.getTwo()))
							return 1;
						if ((Double)(o1.getTwo()) < (Double)(o2.getTwo()))
							return -1;
						else
							return 0;
					}
				};
			}
			
			Collections.sort(pairArrayList, comparator);
			
			HashMap<String, Integer> scoreHashMap = new HashMap<String, Integer>();//分类投票
			int end = this.K < pairArrayList.size() ? this.K : pairArrayList.size();
			for (int i = 0; i < end; i++) {
				DocumentVector documentVector = (DocumentVector) pairArrayList.get(i).getOne();
				Integer score = scoreHashMap.get(documentVector.getClassName());
				if (score == null) {
					scoreHashMap.put(documentVector.getClassName(), 1);
				}else {
					scoreHashMap.put(documentVector.getClassName(), score + 1);
				}
				
				Double avgWeight = avgSimilarityHashMap.get(i);
				if (avgWeight == null) {
					avgWeight = 0.0;
				}
				avgSimilarityHashMap.put(i, avgWeight + (Double) pairArrayList.get(i).getTwo());
			}
			
			Integer maxScore = -1;//选出最高票数
			String resultClass = null;
			for (String className : scoreHashMap.keySet()) {
				if (scoreHashMap.get(className) > maxScore) {
					resultClass = className;
					maxScore = scoreHashMap.get(className);
				}
			}
			
			ClassifyResult classifyResult = new ClassifyResult();//包装分类结果
			classifyResult.setDoc(testDocument.getDoc());
			classifyResult.setClassString(resultClass);
			classifyResult.setOriginalClassString(testDocument.getClassName());
			
			resultArrayList.add(classifyResult);
		}
		/*
		for (Integer i : avgSimilarityHashMap.keySet()) {
			System.out.println(avgSimilarityHashMap.get(i)/testTrainArrayList.size());
			avgSimilarityHashMap.put(i, avgSimilarityHashMap.get(i)/testTrainArrayList.size());
		}
		*/
		return resultArrayList;//返回结果
	}
}
