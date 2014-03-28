package classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import similarity.Distance;
import similarity.EuclideanDistance;
import similarity.Similarity;

import constants.Constants;
import controller.Scorer;

import model.ClassifyResult;
import model.DocumentVector;
import model.FeatureTerm;
import model.ObjectPair;

public class DDKNNClassifier extends Classifier {
	
	private HashMap<String, ArrayList<HashSet<Integer>>> DDVectorMap;
	private Set<String> featureTermSet;
	private BigDecimal step;
	int K;
	
	private HashMap<Integer, ArrayList<Map.Entry<Integer, Double>>> similarHashMap;
	
	public DDKNNClassifier(int K, double step, String filePath, HashMap<String, FeatureTerm> featureTermMap){
		if (filePath == null || filePath.length() == 0) {
			System.err.println("维度分区文件路径错误：" + filePath);
			return;
		}
		this.step = new BigDecimal(Double.toString(step));
		this.K = K;
		this.DDVectorMap = DDKNNClassifier.loadDDVectorMapToPath(filePath);
		this.featureTermSet = featureTermMap.keySet();
	}
	
	public DDKNNClassifier(ArrayList<DocumentVector> trainArrayList, HashMap<String, FeatureTerm> featureTermMap, int K, double step, String saveFilePath) {
		this.step = new BigDecimal(Double.toString(step));
		this.K = K;
		this.featureTermSet = featureTermMap.keySet();
		
		this.DDVectorMap = this.DDVectorMapFromDocumentVectors(trainArrayList, featureTermMap, this.step);
		if (saveFilePath != null && saveFilePath.length() != 0) {
			DDKNNClassifier.saveDDVectorMapToPath(saveFilePath, this.DDVectorMap);
		}
	}

	@Override
	protected ArrayList<ClassifyResult> classify(
			ArrayList<DocumentVector> testTrainArrayList,
			ArrayList<DocumentVector> trainArrayList) {
		
		HashMap<Integer, DocumentVector> trainDocumentVectorHashMap = new HashMap<Integer, DocumentVector>();
		for (DocumentVector documentVector : trainArrayList) {
			trainDocumentVectorHashMap.put(documentVector.getDoc(), documentVector);
		}
		
		double calTime = 0;
		double calTop = 0;
		double calRadius = 0;
		ArrayList<ClassifyResult> resultArrayList = new ArrayList<ClassifyResult>();
		for (DocumentVector testDocumentVector : testTrainArrayList) {
			System.out.print(".");
			
			HashSet<Integer> kSet = new HashSet<Integer>();//结果集
			BigDecimal radius = Constants.beginRadius;
			BigDecimal R = null;
			while (radius.compareTo(Constants.maxScope) <= 0) {
				R = new BigDecimal(radius.toString());
				radius =  radius.add(this.step);//递增
				
				HashSet<Integer> retainSet = new HashSet<Integer>();
				retainSet.addAll(trainDocumentVectorHashMap.keySet());//初始化
				
				for (String termString : testDocumentVector.getTerms()) {
//				for (String termString : this.featureTermSet) {
					ArrayList<HashSet<Integer>> termArrayList = this.DDVectorMap.get(termString);
					//根据范围取出桶内的文档，注意数组越界
					Double termWeight = testDocumentVector.getTermWeight(termString);
					if (termWeight == null) {
						termWeight = 0.0;
					}
					int leftBucketIndex = this.bucketNumberFromWeight(new BigDecimal(Double.toString(termWeight)).subtract(R), this.step);
					int rightBucketIndex = this.bucketNumberFromWeight(new BigDecimal(Double.toString(termWeight)).add(R), this.step);
					leftBucketIndex =  leftBucketIndex >= 0 ? leftBucketIndex : 0;
					rightBucketIndex = rightBucketIndex <= termArrayList.size() - 1? rightBucketIndex : termArrayList.size() - 1;//101个桶？
					
					//求交集、累计结果
					HashSet<Integer> currentScopeSet = new HashSet<Integer>();
					for (int i = leftBucketIndex; i <= rightBucketIndex; i++) {
						if (termArrayList.get(i) != null) {// 从文本读进来时，会有null
							currentScopeSet.addAll(termArrayList.get(i));
						}
					}
					if (currentScopeSet.size() == 0) {
						break;	
					}
					retainSet.retainAll(currentScopeSet);
					if (retainSet.size() == 0) {
						break;	
					}
				}
				
				if (retainSet.size() >= this.K) {
					kSet.addAll(retainSet);
					break;
				}
			}
			
			if (kSet.size() < Constants.K) {
				kSet.addAll(trainDocumentVectorHashMap.keySet());
			}
			
			calRadius += R.doubleValue();
//			System.out.println("R:" + R + " kSet.size():" + kSet.size());
			
			//计算相似度
			ArrayList<ObjectPair<DocumentVector, Double>> pairArrayList = new ArrayList<ObjectPair<DocumentVector, Double>>();
			Distance distance = new EuclideanDistance();
//			for (Integer doc : trainDocumentVectorHashMap.keySet())
			for (Integer doc : kSet)
			{
				calTime += 1;
				DocumentVector keyDocumentVector = trainDocumentVectorHashMap.get(doc);
				pairArrayList.add(new ObjectPair<DocumentVector, Double>(keyDocumentVector, distance.getSimilarityBetweenDocuments(testDocumentVector, keyDocumentVector)));
			}
			
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
			/*
			ArrayList<Map.Entry<Integer, Double>> simTopArrayList = this.getSimilarHashMap().get(testDocumentVector.getDoc());
			int hitCount = 0;
			for (int i = 0; i < Constants.K; i++) {
				if (simTopArrayList.get(i).getKey() == ((DocumentVector)pairArrayList.get(i).getOne()).getDoc()) {
					hitCount++;
				}
			}
			if (hitCount >= Constants.K) {
				System.out.println("hit:" + calTop + 1);
				calTop++;
			}else {
//				for (int i = 0; i < Constants.K; i++) {
					System.out.println(simTopArrayList.get(i).getKey() + ":" + simTopArrayList.get(i).getValue() 
//							+ "  |  " + ((DocumentVector)pairArrayList.get(i).getOne()).getDoc()  + ":" + (Double)pairArrayList.get(i).getTwo());
//				}
			}
			*/
			
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
			classifyResult.setDoc(testDocumentVector.getDoc());
			classifyResult.setClassString(resultClass);
			classifyResult.setOriginalClassString(testDocumentVector.getClassName());
			
			resultArrayList.add(classifyResult);
		}
		
		System.out.println("\n平均查找：" + calTime/testTrainArrayList.size() + "次");
		System.out.println("平均半径：" + calRadius/testTrainArrayList.size());
		System.out.println("hit：" + calTop/testTrainArrayList.size() + "次");
		
		return resultArrayList;
	}
	
	//保存各个训练样本在某个维度上的值排序后的map
	public static void saveDDVectorMapToPath(String filePath, HashMap<String, ArrayList<HashSet<Integer>>> DDVectorMap) {
		File saveFile = new File(filePath);
		if (!saveFile.exists()) {
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!saveFile.isFile()) {
			System.err.println("saveDDVectorMapToPath:save file invaild");
			return;
		}
		System.out.print("保存维度离散化文件...");
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(saveFile));
			for (String term : DDVectorMap.keySet()) {
				ArrayList<HashSet<Integer>> bucketArrayList = DDVectorMap.get(term);
				bufferedWriter.write(term + ":" + bucketArrayList.size() + "\n");//保存桶数
				for (int i = 0; i < bucketArrayList.size(); i++) {
					HashSet<Integer> docHashSet = bucketArrayList.get(i);
					if (!(docHashSet == null || docHashSet.size() == 0)) {//空桶用～号标记
						bufferedWriter.write(i + ":");
						for (Integer integer : docHashSet) {
							bufferedWriter.write(String.valueOf(integer) + " ");
						}
						bufferedWriter.write("\n");
					}
				}
				bufferedWriter.write("-\n");
			}
			System.out.println("保存完毕.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static HashMap<String, ArrayList<HashSet<Integer>>> loadDDVectorMapToPath(String filePath) {
		File saveFile = new File(filePath);
		if (!saveFile.exists()) {
			System.err.println("loadDDVectorMapToPath:filePath invaild");
			return null;
		}
		
		System.out.print("正在载入维度离散化文件...");
		
		HashMap<String, ArrayList<HashSet<Integer>>> DDVectorHashMap = new HashMap<String, ArrayList<HashSet<Integer>>>();
		BufferedReader bufferedReader = null;
		try {
			String readLine	= null;
			bufferedReader = new BufferedReader(new FileReader(saveFile));
			while((readLine = bufferedReader.readLine()) != null){
				String[] termPairStrings = readLine.split(":");
				int bucketNumber = Integer.parseInt(termPairStrings[1]);
				ArrayList<HashSet<Integer>> bucketArrayList = new ArrayList<HashSet<Integer>>(bucketNumber);
				for (int i = 0; i < bucketNumber; i++) {//初始化数组长度
					bucketArrayList.add(null);
				}
				
				DDVectorHashMap.put(termPairStrings[0], bucketArrayList);
				while(!(readLine = bufferedReader.readLine()).equals("-")){
					HashSet<Integer> docSet = null;
					String[] pairStrings = readLine.split(":");
					docSet = new HashSet<Integer>();
					String[] docSetString = pairStrings[1].split(" ");
					for (String doc : docSetString) {
						docSet.add(Integer.parseInt(doc));
					}
					bucketArrayList.set(Integer.parseInt(pairStrings[0]), docSet);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("载入完毕.");
		
		return DDVectorHashMap;
	}

	public HashMap<String, ArrayList<HashSet<Integer>>> DDVectorMapFromDocumentVectors(ArrayList<DocumentVector> trainArrayList, HashMap<String, FeatureTerm> featureTermMap, BigDecimal step) {
		HashMap<String, ArrayList<HashSet<Integer>>> DDVectorHashMap = new HashMap<String, ArrayList<HashSet<Integer>>>();
		for (String featureTerm : featureTermMap.keySet()) {
			int numberOfBucket = this.totalBucketNumberFromWeight(Constants.minScope, Constants.maxScope, step);
//			System.out.println("分区数: " + numberOfBucket);
			
			ArrayList<HashSet<Integer>> bucketArrayList = new ArrayList<HashSet<Integer>>(numberOfBucket);
			for (int i = 0; i <= numberOfBucket; i++) {//初始化空桶，多一个
				if (i == 0) {
					bucketArrayList.add(new HashSet<Integer>());//0号空桶一开始就要用
				}else {
					bucketArrayList.add(null);
				}
			}
			for (DocumentVector documentVector : trainArrayList) {
				Double weight = documentVector.getTermWeight(featureTerm);
				if (weight == null) {//0也要加上，注意范围
					bucketArrayList.get(0).add(documentVector.getDoc());
					continue;
				}
				int bucketIndex = this.bucketNumberFromWeight(new BigDecimal(Double.toString(weight)), this.step);
				HashSet<Integer> bucket = bucketArrayList.get(bucketIndex);
				if (bucket == null) {
					bucket = new HashSet<Integer>();
					bucketArrayList.set(bucketIndex, bucket);
				}
				bucket.add(documentVector.getDoc());
			}
			DDVectorHashMap.put(featureTerm, bucketArrayList);
		}
		
		return DDVectorHashMap;
	}
	
	private int totalBucketNumberFromWeight(BigDecimal min, BigDecimal max, BigDecimal step) {
		return max.subtract(min).divide(step, 0, BigDecimal.ROUND_CEILING).intValue();
	}
	
	private int bucketNumberFromWeight(BigDecimal weight, BigDecimal step) {
		return weight.divide(this.step, 0, BigDecimal.ROUND_CEILING).intValue();
	}

	public HashMap<Integer, ArrayList<Map.Entry<Integer, Double>>> getSimilarHashMap() {
		return similarHashMap;
	}

	public void setSimilarHashMap(HashMap<Integer, HashMap<Integer, Double>> similarHashMap) {
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
}
