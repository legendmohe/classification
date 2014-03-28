package classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import similarity.Distance;
import similarity.EuclideanDistance;
import similarity.Similarity;

import constants.Constants;
import controller.Scorer;

import model.ClassifyResult;
import model.DocumentVector;
import model.ObjectPair;

public class FastKNNClassifier extends Classifier {

	int K;
	protected HashMap<Integer, ArrayList<Map.Entry<Integer, Double>>> similarHashMap;
	
	public FastKNNClassifier(int K, HashMap<Integer, HashMap<Integer, Double>> similarHashMap) {
		this.K = K;
		this.similarHashMap = new HashMap<Integer, ArrayList<Entry<Integer,Double>>>();
		for (Integer doc : similarHashMap.keySet()) {
			ArrayList<Map.Entry<Integer, Double>> tempArrayList = new ArrayList<Map.Entry<Integer,Double>>();
			tempArrayList.addAll(similarHashMap.get(doc).entrySet());
			Collections.sort(tempArrayList, new Comparator<Map.Entry<Integer, Double>>() {
				@Override
				public int compare(Entry<Integer, Double> o1,
						Entry<Integer, Double> o2) {
					if (o1.getValue() > o2.getValue()) {
						return 1;
					}else if(o1.getValue() < o2.getValue()){
						return -1;
					}else {
						return 0;
					}
				}
			});
			
			this.similarHashMap.put(doc, tempArrayList);
		}
		
	}

	@Override
	protected ArrayList<ClassifyResult> classify(
			ArrayList<DocumentVector> testTrainArrayList,
			ArrayList<DocumentVector> trainArrayList) {

		ArrayList<ClassifyResult> resultArrayList = new ArrayList<ClassifyResult>();
		double calSum = 0.0;
//		double calTop = 0;
		for (DocumentVector testDocument : testTrainArrayList) {
			System.out.print(".");
			//计算相似度
			ArrayList<ObjectPair<DocumentVector, Double>> pairArrayList = this.getFastSimilarityPairArrayList(trainArrayList, testDocument);
			calSum += pairArrayList.size();
			Collections.sort(pairArrayList, new Comparator<ObjectPair<DocumentVector, Double>>() {//相似度排序
				@Override
				public int compare(ObjectPair<DocumentVector, Double> o1, ObjectPair<DocumentVector, Double> o2) {
					if ((Double)(o1.getTwo()) > (Double)(o2.getTwo()))
						return 1;
					if ((Double)(o1.getTwo()) < (Double)(o2.getTwo()))
						return -1;
					else
						return 0;
				}
			});
			
//			ArrayList<Map.Entry<Integer, Double>> simTopArrayList = this.similarHashMap.get(testDocument.getDoc());
//			int hitCount = 0;
//			for (int i = 0; i < Constants.K; i++) {
//				if (simTopArrayList.get(i).getKey() == ((DocumentVector)pairArrayList.get(i).getOne()).getDoc()) {
//					hitCount++;
//				}
//			}
//			if (hitCount == Constants.K) {
//				calTop++;
//			}
			
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
		
		System.out.println("\n平均计算了：" + calSum/testTrainArrayList.size() + "次");
//		System.out.println("hit：" + calTop/testTrainArrayList.size() + "次");
		
		return resultArrayList;//返回结果
	}
	
	private ArrayList<ObjectPair<DocumentVector, Double>> getFastSimilarityPairArrayList(ArrayList<DocumentVector> trainArrayList,
			DocumentVector testDocument) {
		HashMap<Integer, DocumentVector> documentHashMap = new HashMap<Integer, DocumentVector>();
		for (DocumentVector documentVector : trainArrayList) {
			documentHashMap.put(documentVector.getDoc(), documentVector);
		}
		Distance distance = new EuclideanDistance();
		double simR = Double.MAX_VALUE;
		int index = 0;
		int size = trainArrayList.size();
		for (int i = 0; i < size; i++) {//获得随机初始点
			int random = (int) Math.round(Math.random()*(size - 1));
			simR = distance.getSimilarityBetweenDocuments(trainArrayList.get(random), testDocument);
			if (simR > Constants.FastKNNt) {//令初始点在K个之外
				index = random;
				break;
			}
		}
		simR = Constants.FastKNN6*simR;//两倍距离
		
		ArrayList<ObjectPair<DocumentVector, Double>> resultArrayList = new ArrayList<ObjectPair<DocumentVector, Double>>();
		ArrayList<Map.Entry<Integer, Double>> simArrayList = this.similarHashMap.get(index);
		for (int i = 0; i < trainArrayList.size(); i++) { //取前>sim个
			double tempSim = simArrayList.get(i).getValue();
			int doc = simArrayList.get(i).getKey();
			if (tempSim <= simR || resultArrayList.size() <= Constants.K) {
				DocumentVector resultDocumentVector = documentHashMap.get(doc);
				double simValue = distance.getSimilarityBetweenDocuments(testDocument, resultDocumentVector);
				ObjectPair<DocumentVector, Double> resultPair = new ObjectPair<DocumentVector, Double>(resultDocumentVector, simValue);
				resultArrayList.add(resultPair);
			}else {
				break;
			}
		}
		return resultArrayList;
	}
}
